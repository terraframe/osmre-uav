#!/bin/bash
#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

## set shell options ##
set -euo pipefail

## ensure required parameters are set ##
if [ -z "${S3_COMPONENT:-}" ]; then
  echo "Missing required env var: S3_COMPONENT (example: s3://bucket/path/to/component)" >&2
  exit 1
fi
if [ -z "${JOB_ID:-}" ]; then
  echo "Missing required env var: JOB_ID" >&2
  exit 1
fi

# Optional excludes (comma-separated basenames). If unset, treat as empty.
EXCLUDES="${EXCLUDES:-}"

AWS_REGION="${AWS_REGION:-us-west-2}"

# ---------------------------
# Scratch/workdir management
# ---------------------------

# Base mount for persistent scratch (EFS). We'll create per-job subdir under this.
WORK_ROOT="${WORK_ROOT:-/work}"           # The EFS mount point in the container
JOB_WORKDIR="${WORK_ROOT}/${JOB_ID}"      # Per-job scratch dir

# Cleanup controls
CLEANUP_ON_EXIT="${CLEANUP_ON_EXIT:-true}"        # "true" to rm -rf JOB_WORKDIR on exit
SCRATCH_TTL_DAYS="${SCRATCH_TTL_DAYS:-14}"         # prune /work/* older than N days on boot
SCRATCH_PRUNE_ON_BOOT="${SCRATCH_PRUNE_ON_BOOT:-true}"

# (A) On boot: prune forgotten job dirs older than N days (best-effort; don't fail the job)
prune_old_scratch() {
  [ -d "$WORK_ROOT" ] || return 0

  # Only consider immediate children of WORK_ROOT, and only directories.
  # Use mtime to decide "old"; ignores current JOB_ID dir.
  find "$WORK_ROOT" -mindepth 1 -maxdepth 1 -type d \
    ! -name "$JOB_ID" \
    -mtime +"$SCRATCH_TTL_DAYS" \
    -print0 2>/dev/null \
    | while IFS= read -r -d '' d; do
        echo "Pruning old scratch dir (>${SCRATCH_TTL_DAYS}d): $d"
        rm -rf "$d" || echo "Warning: failed to delete $d" >&2
      done || true
}

# (B) Trap: always cleanup this job's scratch on exit (success/fail/ctrl-c/kill where possible)
cleanup() {
  local code=$?
  # If we ever created the dir, try to remove it (best-effort)
  if [ "${CLEANUP_ON_EXIT}" = "true" ] && [ -n "${JOB_WORKDIR:-}" ] && [ -d "${JOB_WORKDIR:-}" ]; then
    echo "Cleaning up job scratch: $JOB_WORKDIR"
    rm -rf "$JOB_WORKDIR" || echo "Warning: failed to remove $JOB_WORKDIR" >&2
  else
    echo "Skipping cleanup (CLEANUP_ON_EXIT=${CLEANUP_ON_EXIT}). Scratch kept at: ${JOB_WORKDIR:-<unset>}"
  fi
  exit $code
}
trap cleanup EXIT INT TERM

if [ "${SCRATCH_PRUNE_ON_BOOT}" = "true" ]; then
  prune_old_scratch
fi

# (C) Use /work/$JOB_ID as the working directory
WORKDIR="${JOB_WORKDIR}"
INPUT_DIR="${WORKDIR}/input"
OUTPUT_DIR="${WORKDIR}/output"

mkdir -p "$INPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "WORK_ROOT      : $WORK_ROOT"
echo "JOB_WORKDIR    : $WORKDIR"
echo "INPUT_DIR      : $INPUT_DIR"
echo "OUTPUT_DIR     : $OUTPUT_DIR"
echo "SCRATCH_TTL_DAYS: $SCRATCH_TTL_DAYS"
echo "CLEANUP_ON_EXIT: $CLEANUP_ON_EXIT"

# ---- helpers ----
trim() {
  local s="$1"
  s="$(echo "$s" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
  echo -n "$s"
}

# Parse s3://bucket/prefix (prefix may be empty). Outputs: bucket newline prefix(without trailing slash)
parse_s3_uri() {
  local uri="$1"
  if [[ "$uri" != s3://* ]]; then
    echo "Invalid S3 URI (must start with s3://): $uri" >&2
    exit 1
  fi

  local rest="${uri#s3://}"
  local bucket="${rest%%/*}"
  local prefix=""
  if [[ "$rest" == *"/"* ]]; then
    prefix="${rest#*/}"
  fi

  # normalize: remove trailing slash from prefix
  prefix="${prefix%/}"

  echo "$bucket"
  echo "$prefix"
}

# Build an exclude set from comma-separated EXCLUDES (filenames only, no paths)
declare -A EXSET=()
if [ -n "$EXCLUDES" ]; then
  IFS=',' read -ra _EX_ARR <<< "$EXCLUDES"
  for e in "${_EX_ARR[@]}"; do
    e="$(trim "$e")"
    if [ -n "$e" ]; then
      EXSET["$e"]=1
    fi
  done
fi

# ---- compute S3_INPUT_URI and S3_OUTPUT_URI ----
S3_BUCKET=""
S3_PREFIX=""
{
  read -r S3_BUCKET
  read -r S3_PREFIX
} < <(parse_s3_uri "$S3_COMPONENT")

if [ -n "$S3_PREFIX" ]; then
  RAW_PREFIX="${S3_PREFIX}/raw/"
  OUTPUT_PREFIX="${S3_PREFIX}/jobs/${JOB_ID}/"
else
  RAW_PREFIX="raw/"
  OUTPUT_PREFIX="jobs/${JOB_ID}/"
fi

RAW_URI="s3://${S3_BUCKET}/${RAW_PREFIX}"
S3_OUTPUT_URI="s3://${S3_BUCKET}/${OUTPUT_PREFIX}"

echo "S3_COMPONENT   : $S3_COMPONENT"
echo "RAW listing URI: $RAW_URI"
echo "OUTPUT URI     : $S3_OUTPUT_URI"
echo "EXCLUDES       : ${EXCLUDES:-<none>}"

# List objects under raw/ and pick the first .copc.laz or .laz whose basename isn't excluded.
SELECTED_KEY=""
while IFS= read -r key; do
  [ -z "$key" ] && continue

  # Only consider .copc.laz or .laz
  if [[ "$key" != *.copc.laz && "$key" != *.laz ]]; then
    continue
  fi

  base="$(basename "$key")"
  if [[ -n "${EXSET[$base]+x}" ]]; then
    echo "Skipping excluded file: $base"
    continue
  fi

  SELECTED_KEY="$key"
  break
done < <(
    aws s3api list-objects-v2 \
    --bucket "$S3_BUCKET" \
    --prefix "$RAW_PREFIX" \
    --region "$AWS_REGION" \
    --query 'Contents[].Key' \
    --output text | tr '\t' '\n' | sed '/^None$/d'
)

if [ -z "$SELECTED_KEY" ]; then
  echo "No eligible input found under: $RAW_URI" >&2
  echo "Criteria: first key ending in .copc.laz or .laz, excluding basenames in EXCLUDES (if provided)." >&2
  exit 1
fi

S3_INPUT_URI="s3://${S3_BUCKET}/${SELECTED_KEY}"

echo "Selected input : $S3_INPUT_URI"

# ---- download selected input ----
INPUT_BASENAME="$(basename "$S3_INPUT_URI")"
LOCAL_INPUT_FILE="$INPUT_DIR/$INPUT_BASENAME"

echo "Downloading: $S3_INPUT_URI"
aws s3 cp "$S3_INPUT_URI" "$LOCAL_INPUT_FILE" --region "$AWS_REGION" --no-progress

if [ ! -f "$LOCAL_INPUT_FILE" ]; then
  echo "Download failed: '$LOCAL_INPUT_FILE' not found." >&2
  exit 1
fi

## Convert point cloud to COPC (robust: validate LAZ + detect COPC by metadata) ##

# Helper: confirm PDAL can read the file and that it's compressed (LAZ)
is_valid_laz() {
  local f="$1"

  # 1) PDAL must be able to read it (valid LAS/LAZ structure)
  conda run -n silvimetric pdal info "$f" >/dev/null 2>&1 || return 1

  # 2) Must be compressed (LAZ specifically)
  conda run -n silvimetric pdal info --metadata "$f" 2>/dev/null \
    | jq -e '.metadata.compressed == true' >/dev/null 2>&1
}

# Helper: detect COPC by presence of metadata.copc
is_copc() {
  local f="$1"
  conda run -n silvimetric pdal info --metadata "$f" 2>/dev/null \
    | jq -e '.metadata.copc != null' >/dev/null 2>&1
}

# ---- Step 1: verify LAZ ----
if ! is_valid_laz "$LOCAL_INPUT_FILE"; then
  echo "Error: Input is not a valid LAZ (compressed LAS) file: $LOCAL_INPUT_FILE" >&2
  exit 1
fi

# ---- Step 2: if not COPC, generate COPC ----
if ! is_copc "$LOCAL_INPUT_FILE"; then
  echo "Input is LAZ but not COPC; generating COPC..."

  ORIGINAL_BASENAME="$(basename "$LOCAL_INPUT_FILE")"

  # Build output name safely:
  # - if input ends with .copc.laz (but isn't COPC), strip that and re-add .copc.laz (no .copc.copc.laz)
  # - else strip trailing .laz and add .copc.laz
  if [[ "$ORIGINAL_BASENAME" == *.copc.laz ]]; then
    BASE_NO_EXT="${ORIGINAL_BASENAME%.copc.laz}"
  else
    BASE_NO_EXT="${ORIGINAL_BASENAME%.laz}"
  fi

  COPC_FILENAME="${BASE_NO_EXT}.copc.laz"
  COPC_FILE="$OUTPUT_DIR/$COPC_FILENAME"

  /opt/lidar/idm_pdal_translate_copc.sh \
    /opt/conda/etc/profile.d/conda.sh \
    "$LOCAL_INPUT_FILE" \
    "$COPC_FILE"

  if [ ! -f "$COPC_FILE" ]; then
    echo "COPC translate failed: '$COPC_FILE' not found." >&2
    exit 1
  fi

  # Optional sanity check: ensure output is actually COPC
  if ! is_copc "$COPC_FILE"; then
    echo "Error: COPC translation produced a file that does not appear to be COPC: $COPC_FILE" >&2
    exit 1
  fi

  echo "Uploading: $COPC_FILENAME -> $S3_OUTPUT_URI"
  aws s3 cp "$COPC_FILE" "$S3_OUTPUT_URI" --region "$AWS_REGION" --no-progress

  # Use COPC for downstream processing
  LOCAL_INPUT_FILE="$COPC_FILE"
else
  echo "Input is already COPC; using as-is."
fi


## run silvimetric ##
/opt/lidar/silvimetric_idm.sh /opt/conda/etc/profile.d/conda.sh "$LOCAL_INPUT_FILE" "$OUTPUT_DIR"

##
## Convert selected outputs to COG (gdaladdo -> gdal_translate) and upload
##
COG_FILES=( \
  "m_Z_max.tif" \
  "m_Z_min.tif" \
  "m_Z_diff.tif" \
  "m_Classification_veg_density.tif" \
)

echo "Generating COGs for generated artifacts and uploading..."
for fname in "${COG_FILES[@]}"; do
  src="$OUTPUT_DIR/$fname"
  if [ ! -f "$src" ]; then
    echo "Warning: expected file not found, skipping: $src" >&2
    continue
  fi

  base="${fname%.tif}"
  overview="$OUTPUT_DIR/${base}.overview.tif"
  cog="$OUTPUT_DIR/${base}.cog.tif"

  # Make a working copy to build overviews on (keeps original untouched)
  cp -f "$src" "$overview"

  # Build internal overviews
  conda run -n silvimetric gdaladdo -r average "$overview" 2 4 8 16

  # Translate to COG using the overviewed input
  conda run -n silvimetric gdal_translate "$overview" "$cog" -of COG -co COMPRESS=LZW -co BIGTIFF=YES

  # (Optional) remove intermediate overview file
  rm -f "$overview"

  echo "Uploading: $(basename "$cog") -> $S3_OUTPUT_URI"
  aws s3 cp "$cog" "$S3_OUTPUT_URI" --region "$AWS_REGION" --no-progress
done

echo "IDM_PROCESSING_COMPLETE"