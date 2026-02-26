/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.processing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;

/**
 * Parses LIDAR specific CloudWatch log output produced by the SilviMetric/COPC shell script and
 * generates user-facing messages. If in the future you want to adapt this to other processing
 * types, you may subclass this type.
 *
 * Key principle:
 * - End-user messages should be about *their data* (bad input, missing expected
 *   artifacts, format problems, not enough data, etc.).
 * - Infrastructure/permission/network/AWS issues should be collapsed into a
 *   generic "critical error" (admins can inspect raw logs).
 *
 * The script uses `set -euo pipefail`, so the first critical failure typically
 * terminates execution; however CloudWatch streams can include extra noise, so
 * we scan all lines.
 */
public class FargateResponseProcessor
{
  public static final String SUCCESS_SENTINEL = "IDM_PROCESSING_COMPLETE";
  
  public static final String OUT_OF_MEMORY =
      "The processing server ran out of memory. Please contact your technical support. You can also try turning down the quality settings and re-processing.";

  private final Set<String> errors = new LinkedHashSet<>();
  private final Set<String> warnings = new LinkedHashSet<>();

  private boolean sawDone = false;
  private boolean hasFatal = false;

  public void process(FargateTaskIF task, String fargateOutput)
  {
    errors.clear();
    warnings.clear();
    sawDone = false;
    hasFatal = false;

    processOutput(task, fargateOutput);
    
    if (errors.size() == 1 && warnings.isEmpty())
    {
      task.setMessage(errors.iterator().next());
      task.setStatus(ProcessingTaskStatus.FAILED.getLabel());
      return;
    }
    
    for (String msg : errors)
    {
      task.createAction(msg, TaskActionType.ERROR);
    }
    for (String msg : warnings)
    {
      task.createAction(msg, TaskActionType.WARNING);
    }
    
    if (sawDone && !hasFatal && errors.isEmpty())
    {
      final String seeWarnings = warnings.isEmpty() ? "." : ", but with warnings. See messages for more information.";
      task.setMessage("Processing completed successfully" + seeWarnings);
      task.setStatus(ProcessingTaskStatus.COMPLETED.getLabel());
      return;
    }
    
    final String seeWarnings = (warnings.isEmpty() && errors.isEmpty()) ? "Contact your technical support for more information." : "See messages for more information.";
    task.setMessage("An error occurred while processing your data. " + seeWarnings);
    task.setStatus(ProcessingTaskStatus.FAILED.getLabel());
  }

  /**
   * Read through the output and attempt to identify issues that may have occurred.
   *
   * We classify issues into:
   * - Data-specific: show to end user as actionable messages.
   * - Non-data critical: mark sawNonDataCritical=true (end user sees generic message).
   */
  private void processOutput(FargateTaskIF task, String output)
  {
    if (output == null || output.trim().isEmpty())
    {
      hasFatal = true;
      return;
    }

    List<String> lines = splitLines(output);

    for (int i = 0; i < lines.size(); i++)
    {
      String line = safe(lines.get(i));
      if (line.isEmpty())
      {
        continue;
      }

      // Success sentinel
      if (line.contains(SUCCESS_SENTINEL))
      {
        sawDone = true;
        continue;
      }

      // --------------------------
      // Data-specific messages
      // --------------------------

      // Missing configuration is not the user's fault; treat as critical non-data.
      if (line.contains("Missing required env var:"))
      {
        hasFatal = true;
        continue;
      }

      // Bad S3 URI is also a system/job config issue (not drone data).
      if (line.contains("Invalid S3 URI (must start with s3://):"))
      {
        hasFatal = true;
        continue;
      }

      // No eligible input under raw/: this is a "data not found / wrong upload" type issue.
      if (line.startsWith("No eligible input found under:"))
      {
        String uri = extractAfter(line, "No eligible input found under:", null);

        String msg;
        if (uri != null && !uri.isEmpty())
        {
          msg = "No usable point cloud file was found in your upload.";
        }
        else
        {
          msg = "No usable point cloud file was found in your upload.";
        }

        // Keep this end-user friendly; do not mention S3/raw prefixes.
        msg += " Please upload a LAS/LAZ point cloud ('.laz' or '.copc.laz') and try again.";

        // Criteria is too implementation-specific; omit.
        errors.add(msg);
        continue;
      }

      // Download failure is usually not user data; treat as critical non-data.
      if (line.startsWith("Download failed:") || line.contains("Download failed:"))
      {
        hasFatal = true;
        continue;
      }

      // COPC translate failed sentinel from script - often indicates conversion step failed.
      // We want a data-specific message *if* we can detect a format/data complaint around it.
      if (line.startsWith("COPC translate failed:") || line.contains("COPC translate failed:"))
      {
        // This sentinel alone doesn't tell us why; mark as data-related conversion failure
        // (still likely due to input format/corruption) and add a user-friendly message.
        errors.add("We couldn’t convert your point cloud into the required internal format. Your file may be corrupted or not a supported LAS/LAZ variant. Please try re-uploading the point cloud and processing again.");
        continue;
      }

      // Script warning about missing expected outputs -> warnings
      if (line.startsWith("Warning: expected file not found, skipping:"))
      {
        String file = extractAfter(line, "Warning: expected file not found, skipping:", null);
        if (file != null && !file.isEmpty())
        {
          warnings.add("An expected output product was not generated (" + file.trim() + "). Results may be incomplete.");
        }
        else
        {
          warnings.add("An expected output product was not generated. Results may be incomplete.");
        }
        continue;
      }

      // Out-of-memory: not drone-data-specific, but user-actionable (reduce quality / retry).
      if (looksLikeOutOfMemory(line))
      {
        errors.add(OUT_OF_MEMORY);
        continue;
      }

      // --------------------------
      // PDAL / point cloud format issues (data-specific)
      // --------------------------
      if (looksLikePdalDataFormatError(line))
      {
        errors.add("Your point cloud could not be read/converted (LAS/LAZ format issue). Please verify the file is a valid LAS/LAZ point cloud and try re-uploading it before processing again.");
        continue;
      }

      // --------------------------
      // GDAL/GeoTIFF/COG issues that are often data-driven (rare here, but possible if rasters are malformed)
      // --------------------------
      if (looksLikeGdalDataError(line))
      {
        errors.add("A raster output could not be generated due to a file format issue. Please contact technical support and include the collection details.");
        continue;
      }

      // --------------------------
      // Infrastructure / non-data issues (collapse to generic)
      // --------------------------
      if (looksLikeAwsCliError(line) || looksLikeNetworkOrPermission(line))
      {
        hasFatal = true;
        continue;
      }

      if (looksLikeGenericNonDataFailure(line))
      {
        // Default: treat as critical; we don't want to scare users with internals.
        hasFatal = true;
        continue;
      }
    }
  }

  // ---------------------------
  // Classification helpers
  // ---------------------------

  private boolean looksLikeOutOfMemory(String line)
  {
    String s = line.toLowerCase();
    return s.contains("out of memory")
        || s.contains("cannot allocate memory")
        || s.contains("std::bad_alloc")
        || s.contains("bad_alloc")
        || s.contains("memoryerror")
        || s.contains("oom-kill")
        || s.contains("exit status 137")
        || s.contains("signal: killed");
  }

  /**
   * Attempts to detect PDAL/LASzip/LAZ format issues that point to user data problems.
   * We intentionally look for more "file/format/corruption" language, not just the word "pdal".
   */
  private boolean looksLikePdalDataFormatError(String line)
  {
    String s = line.toLowerCase();

    boolean mentionsPdalOrLas = s.contains("pdal")
        || s.contains("laszip")
        || s.contains("laz")
        || s.contains("las");

    if (!mentionsPdalOrLas)
    {
      return false;
    }

    // Common format/corruption cues
    return s.contains("invalid")
        || s.contains("corrupt")
        || s.contains("unsupported")
        || s.contains("cannot read")
        || s.contains("could not read")
        || s.contains("failed to read")
        || s.contains("not recognized")
        || s.contains("unknown point format")
        || s.contains("bad header")
        || s.contains("wrong signature")
        || s.contains("unexpected end of file")
        || s.contains("decompression")
        || s.contains("laszip error")
        || s.contains("unable to open")
        || s.contains("no reader for")
        || s.contains("reader not found")
        || (s.contains("error") && (s.contains("reader") || s.contains("laszip") || s.contains("laz")));
  }

  /**
   * GDAL errors can be infra (missing binaries) or data (malformed tiffs).
   * Here we bias toward data-related cues only.
   */
  private boolean looksLikeGdalDataError(String line)
  {
    String s = line.toLowerCase();
    boolean mentionsGdal = s.contains("gdal") || s.contains("geotiff") || s.contains("tiff");

    if (!mentionsGdal)
    {
      return false;
    }

    return s.contains("corrupt")
        || s.contains("invalid")
        || s.contains("not a tiff")
        || s.contains("cannot open")
        || s.contains("failed to open")
        || s.contains("unsupported")
        || s.contains("read error")
        || s.contains("i/o error");
  }

  private boolean looksLikeAwsCliError(String line)
  {
    String s = line.toLowerCase();
    return s.startsWith("fatal error:")
        || s.contains("an error occurred (")
        || s.contains("unable to locate credentials")
        || s.contains("could not connect to the endpoint url")
        || s.contains("ssl validation failed")
        || s.contains("access denied")
        || s.contains("nosuchbucket")
        || s.contains("nosuchkey")
        || s.contains("slowdown");
  }

  private boolean looksLikeNetworkOrPermission(String line)
  {
    String s = line.toLowerCase();
    return s.contains("permission denied")
        || s.contains("timed out")
        || s.contains("timeout")
        || s.contains("temporary failure in name resolution")
        || s.contains("could not resolve host")
        || s.contains("connection refused")
        || s.contains("connection reset")
        || s.contains("network is unreachable");
  }

  /**
   * Generic failure patterns that are usually not user data (missing tools, missing env, etc.)
   * and should be collapsed to GENERIC_CRITICAL_ERROR.
   */
  private boolean looksLikeGenericNonDataFailure(String line)
  {
    String s = line.toLowerCase();

    // "no such file" could be data (missing input) OR infra (missing script).
    // We only classify as non-data when it looks like a missing binary/script.
    if (s.contains("no such file or directory"))
    {
      if (s.contains("/opt/") || s.contains("conda") || s.contains("gdal") || s.contains("aws"))
      {
        return true;
      }
    }

    return s.contains("command not found")
        || s.contains("exec format error")
        || s.contains("error: cannot find") // generic tool resolution
        || s.contains("module not found")
        || s.contains("unable to load")
        || s.contains("failed to start")
        || s.contains("task stopped")
        || s.contains("cannot run program");
  }

  // ---------------------------
  // Utility helpers
  // ---------------------------

  private static List<String> splitLines(String output)
  {
    String normalized = output.replace("\r\n", "\n").replace('\r', '\n');
    String[] parts = normalized.split("\n");
    List<String> lines = new ArrayList<>(parts.length);
    for (String p : parts)
    {
      lines.add(p);
    }
    return lines;
  }

  private static String safe(String s)
  {
    return s == null ? "" : s.trim();
  }

  private static String extractAfter(String line, String token, String fallback)
  {
    int idx = line.indexOf(token);
    if (idx < 0)
    {
      return fallback;
    }

    String rest = line.substring(idx + token.length()).trim();
    if (rest.isEmpty())
    {
      return fallback;
    }

    return rest;
  }
}