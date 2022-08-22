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
package gov.geoplatform.uasdm.view;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.json.JSONArray;

import gov.geoplatform.uasdm.model.ImageryComponent;

public class RequestParser implements RequestParserIF
{
  private static String FILENAME_PARAM = "qqfile";

  private static String PART_INDEX_PARAM = "qqpartindex";

  private static String FILE_SIZE_PARAM = "qqtotalfilesize";

  private static String TOTAL_PARTS_PARAM = "qqtotalparts";

  private static String UUID_PARAM = "qquuid";

  private static String PART_FILENAME_PARAM = "qqfilename";

  private static String PART_RESUME_PARAM = "qqresume";

  private static String METHOD_PARAM = "_method";

  private static String GENERATE_ERROR_PARAM = "generateError";

  private static String UAS_COMPONENT_OID = "uasComponentOid";

  private static String PROCESS_UPLOAD = "processUpload";

  private static String PROCESS_DEM = "processDem";

  private static String PROCESS_ORTHO = "processOrtho";

  private static String PROCESS_PTCLOUD = "processPtcloud";

  private static String SELECTIONS = "selections";

  private static String UPLOAD_TARGET = "uploadTarget";

  private static String DESCRIPTION = "description";

  private static String TOOL = "tool";

  private String filename;

  private FileItem uploadItem;

  private boolean generateError;

  private int partIndex = -1;

  private long totalFileSize;

  private int totalParts;

  private String uuid;

  private String originalFilename;

  private String method;

  private Map<String, String> customParams = new HashMap<>();

  private Boolean resume;

  private String uasComponentOid;

  private String uploadTarget;

  private String description;

  private String tool;

  private Boolean processUpload;

  private Boolean processPtcloud;

  private Boolean processOrtho;

  private Boolean processDem;

  private JSONArray selections;

  private RequestParser()
  {
  }

  // 2nd param is null unless a MPFR
  public static RequestParserIF getInstance(HttpServletRequest request, MultipartUploadParser multipartUploadParser) throws Exception
  {
    RequestParser requestParser = new RequestParser();

    if (multipartUploadParser == null)
    {
      if (request.getMethod().equals("POST") && request.getContentType() == null)
      {
        parseXdrPostParams(request, requestParser);
      }
      else
      {
        requestParser.filename = request.getParameter(FILENAME_PARAM);
        parseQueryStringParams(requestParser, request);
      }
    }
    else
    {
      requestParser.uploadItem = multipartUploadParser.getFirstFile();
      requestParser.filename = multipartUploadParser.getFirstFile().getName();

      // params could be in body or query string, depending on Fine Uploader
      // request option properties
      parseRequestBodyParams(requestParser, multipartUploadParser);
      parseQueryStringParams(requestParser, request);
    }

    removeQqParams(requestParser.customParams);

    return requestParser;
  }

  @Override
  public String getFilename()
  {
    return originalFilename != null ? originalFilename : filename;
  }

  // only non-null for MPFRs
  @Override
  public FileItem getUploadItem()
  {
    return uploadItem;
  }

  @Override
  public boolean generateError()
  {
    return generateError;
  }

  @Override
  public int getPartIndex()
  {
    return partIndex;
  }

  @Override
  public long getTotalFileSize()
  {
    return totalFileSize;
  }

  @Override
  public int getTotalParts()
  {
    return totalParts;
  }

  @Override
  public String getUuid()
  {
    return uuid;
  }

  @Override
  public String getUasComponentOid()
  {
    return uasComponentOid;
  }

  @Override
  public Boolean getProcessUpload()
  {
    return processUpload;
  }

  @Override
  public Boolean getProcessDem()
  {
    return processDem;
  }

  @Override
  public Boolean getProcessOrtho()
  {
    return processOrtho;
  }

  @Override
  public Boolean getProcessPtcloud()
  {
    return processPtcloud;
  }

  @Override
  public JSONArray getSelections()
  {
    return selections;
  }

  @Override
  public String getUploadTarget()
  {
    return ( uploadTarget != null ) ? uploadTarget : ImageryComponent.RAW;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public String getTool()
  {
    return tool;
  }

  @Override
  public String getOriginalFilename()
  {
    return originalFilename;
  }

  @Override
  public String getMethod()
  {
    return method;
  }

  @Override
  public boolean isResume()
  {
    return resume;
  }

  @Override
  public boolean isFirst()
  {
    return ( this.partIndex == 0 || this.partIndex < 0 );
  }

  @Override
  public Map<String, String> getCustomParams()
  {
    return customParams;
  }

  private static void parseRequestBodyParams(RequestParser requestParser, MultipartUploadParser multipartUploadParser) throws Exception
  {
    if (multipartUploadParser.getParams().get(GENERATE_ERROR_PARAM) != null)
    {
      requestParser.generateError = Boolean.parseBoolean(multipartUploadParser.getParams().get(GENERATE_ERROR_PARAM));
    }

    String partNumStr = multipartUploadParser.getParams().get(PART_INDEX_PARAM);
    if (partNumStr != null)
    {
      requestParser.partIndex = Integer.parseInt(partNumStr);

      requestParser.totalFileSize = Long.parseLong(multipartUploadParser.getParams().get(FILE_SIZE_PARAM));
      requestParser.totalParts = Integer.parseInt(multipartUploadParser.getParams().get(TOTAL_PARTS_PARAM));
    }

    for (Map.Entry<String, String> paramEntry : multipartUploadParser.getParams().entrySet())
    {
      requestParser.customParams.put(paramEntry.getKey(), paramEntry.getValue());
    }

    if (requestParser.uuid == null)
    {
      requestParser.uuid = multipartUploadParser.getParams().get(UUID_PARAM);
    }

    if (requestParser.uasComponentOid == null)
    {
      requestParser.uasComponentOid = multipartUploadParser.getParams().get(UAS_COMPONENT_OID);
    }

    if (requestParser.processUpload == null)
    {
      requestParser.processUpload = Boolean.valueOf(multipartUploadParser.getParams().get(PROCESS_UPLOAD));
    }

    if (requestParser.processPtcloud == null)
    {
      requestParser.processPtcloud = Boolean.valueOf(multipartUploadParser.getParams().get(PROCESS_PTCLOUD));
    }

    if (requestParser.processOrtho == null)
    {
      requestParser.processOrtho = Boolean.valueOf(multipartUploadParser.getParams().get(PROCESS_ORTHO));
    }

    if (requestParser.processDem == null)
    {
      requestParser.processDem = Boolean.valueOf(multipartUploadParser.getParams().get(PROCESS_DEM));
    }

    if (requestParser.selections == null && multipartUploadParser.getParams().containsKey(SELECTIONS))
    {
      requestParser.selections = new JSONArray(multipartUploadParser.getParams().get(SELECTIONS));
    }

    if (requestParser.uploadTarget == null)
    {
      requestParser.uploadTarget = multipartUploadParser.getParams().get(UPLOAD_TARGET);
    }

    if (requestParser.description == null)
    {
      requestParser.description = multipartUploadParser.getParams().get(DESCRIPTION);
    }

    if (requestParser.tool == null)
    {
      requestParser.tool = multipartUploadParser.getParams().get(TOOL);
    }

    if (requestParser.originalFilename == null)
    {
      requestParser.originalFilename = multipartUploadParser.getParams().get(PART_FILENAME_PARAM);
    }

    String resume = multipartUploadParser.getParams().get(PART_RESUME_PARAM);

    if (resume != null)
    {
      requestParser.resume = Boolean.parseBoolean(resume);
    }
    else
    {
      requestParser.resume = false;
    }
  }

  @Override
  public int getPercent()
  {
    return ( (int) ( (double) this.getPartIndex() / this.getTotalParts() * 100 ) );
  }

  private static void parseQueryStringParams(RequestParser requestParser, HttpServletRequest req)
  {
    if (req.getParameter(GENERATE_ERROR_PARAM) != null)
    {
      requestParser.generateError = Boolean.parseBoolean(req.getParameter(GENERATE_ERROR_PARAM));
    }

    String partNumStr = req.getParameter(PART_INDEX_PARAM);
    if (partNumStr != null)
    {
      requestParser.partIndex = Integer.parseInt(partNumStr);
      requestParser.totalFileSize = Long.parseLong(req.getParameter(FILE_SIZE_PARAM));
      requestParser.totalParts = Integer.parseInt(req.getParameter(TOTAL_PARTS_PARAM));
    }

    Enumeration<String> paramNames = req.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = paramNames.nextElement();
      requestParser.customParams.put(paramName, req.getParameter(paramName));
    }

    if (requestParser.uuid == null)
    {
      requestParser.uuid = req.getParameter(UUID_PARAM);
    }

    if (requestParser.uasComponentOid == null)
    {
      requestParser.uasComponentOid = req.getParameter(UAS_COMPONENT_OID);
    }

    if (requestParser.processUpload == null)
    {
      requestParser.processUpload = Boolean.valueOf(req.getParameter(PROCESS_UPLOAD));
    }

    if (requestParser.processPtcloud == null)
    {
      requestParser.processPtcloud = Boolean.valueOf(req.getParameter(PROCESS_PTCLOUD));
    }

    if (requestParser.processOrtho == null)
    {
      requestParser.processOrtho = Boolean.valueOf(req.getParameter(PROCESS_ORTHO));
    }

    if (requestParser.processDem == null)
    {
      requestParser.processDem = Boolean.valueOf(req.getParameter(PROCESS_DEM));
    }

    if (requestParser.uploadTarget == null)
    {
      requestParser.uploadTarget = req.getParameter(UPLOAD_TARGET);
    }

    if (requestParser.description == null)
    {
      requestParser.description = req.getParameter(DESCRIPTION);
    }

    if (requestParser.tool == null)
    {
      requestParser.tool = req.getParameter(TOOL);
    }

    if (requestParser.method == null)
    {
      requestParser.method = req.getParameter(METHOD_PARAM);
    }

    if (requestParser.originalFilename == null)
    {
      requestParser.originalFilename = req.getParameter(PART_FILENAME_PARAM);
    }
  }

  private static void removeQqParams(Map<String, String> customParams)
  {
    Iterator<Map.Entry<String, String>> paramIterator = customParams.entrySet().iterator();

    while (paramIterator.hasNext())
    {
      Map.Entry<String, String> paramEntry = paramIterator.next();
      if (paramEntry.getKey().startsWith("qq"))
      {
        paramIterator.remove();
      }
    }
  }

  private static void parseXdrPostParams(HttpServletRequest request, RequestParser requestParser) throws Exception
  {
    String queryString = getQueryStringFromRequestBody(request);
    String[] queryParams = queryString.split("&");

    for (String queryParam : queryParams)
    {
      String[] keyAndVal = queryParam.split("=");
      String key = URLDecoder.decode(keyAndVal[0], "UTF-8");
      String value = URLDecoder.decode(keyAndVal[1], "UTF-8");

      if (key.equals(UUID_PARAM))
      {
        requestParser.uuid = value;
      }
      else if (key.equals(UAS_COMPONENT_OID))
      {
        requestParser.uasComponentOid = value;
      }
      else if (key.equals(PROCESS_UPLOAD))
      {
        requestParser.processUpload = Boolean.valueOf(value);
      }
      else if (key.equals(PROCESS_DEM))
      {
        requestParser.processDem = Boolean.valueOf(value);
      }
      else if (key.equals(PROCESS_ORTHO))
      {
        requestParser.processOrtho = Boolean.valueOf(value);
      }
      else if (key.equals(PROCESS_PTCLOUD))
      {
        requestParser.processPtcloud = Boolean.valueOf(value);
      }

      else if (key.equals(UPLOAD_TARGET))
      {
        requestParser.uploadTarget = value;
      }
      else if (key.equals(DESCRIPTION))
      {
        requestParser.description = value;
      }
      else if (key.equals(TOOL))
      {
        requestParser.tool = value;
      }
      else if (key.equals(METHOD_PARAM))
      {
        requestParser.method = value;
      }
      else
      {
        requestParser.customParams.put(key, value);
      }
    }
  }

  private static String getQueryStringFromRequestBody(HttpServletRequest request) throws Exception
  {
    StringBuilder content = new StringBuilder();
    BufferedReader reader = null;

    try
    {
      reader = request.getReader();
      char[] chars = new char[128];
      int bytesRead;
      while ( ( bytesRead = reader.read(chars) ) != -1)
      {
        content.append(chars, 0, bytesRead);
      }
    }
    finally
    {
      if (reader != null)
      {
        reader.close();
      }
    }

    return content.toString();
  }

}