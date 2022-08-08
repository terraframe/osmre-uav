package gov.geoplatform.uasdm.view;

import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONArray;

public interface RequestParserIF
{

  String getFilename();

  // only non-null for MPFRs
  FileItem getUploadItem();

  boolean generateError();

  int getPartIndex();

  long getTotalFileSize();

  int getTotalParts();

  String getUuid();

  String getUasComponentOid();

  Boolean getProcessUpload();

  Boolean getProcessDem();

  Boolean getProcessOrtho();

  Boolean getProcessPtcloud();

  JSONArray getSelections();

  String getUploadTarget();

  String getDescription();

  String getTool();

  String getOriginalFilename();

  String getMethod();

  boolean isResume();

  boolean isFirst();

  Map<String, String> getCustomParams();

  int getPercent();

}