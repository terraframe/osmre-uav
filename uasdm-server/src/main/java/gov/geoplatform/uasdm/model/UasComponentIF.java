/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.uasdm.model;

import java.io.InputStream;
import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.system.Actor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public interface UasComponentIF extends ComponentIF
{
  public void apply();

  public void delete();

  public void deleteObject(String key);

  public String getOid();

  public String getName();

  public void setName(String name);

  public String getFolderName();

  public String getDescription();

  public String getSolrIdField();

  public String getSolrNameField();

  public UasComponentIF createChild(String type);

  public List<AttributeType> attributes();

  public void setValue(String name, Object value);

  public void setGeoPoint(Point geometry);

  public MdClassDAOIF getMdClass();

  public <T> T getObjectValue(String name);

  public Geometry getGeoPoint();

  public Integer getNumberOfChildren();

  public void applyWithParent(UasComponentIF parent);

  public String getS3location();

  public SiteObjectsResultSet getSiteObjects(String key, Long pageNumber, Long pageSize);

  public JSONObject getArtifacts();

  public void removeArtifacts(String folder);

  public List<DocumentIF> getDocuments();

  public AbstractWorkflowTask createWorkflowTask(String uuid, String uploadTarget);

  public Actor getOwner();

  public List<UasComponentIF> getAncestors();

  public RemoteFileObject download(String key);

  public RemoteFileObject download(String key, List<Range> ranges);

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationResource archive, String uploadTarget);

  public DocumentIF putFile(String folder, String fileName, RemoteFileMetadata metadata, InputStream stream);

  public DocumentIF createDocumentIfNotExist(String key, String name, String description, String tool);

  public ProductIF createProductIfNotExist();

  public List<ProductIF> getDerivedProducts();

  public List<UasComponentIF> getChildren();

  public UasComponentIF getChild(String name);

  public UasComponentIF createDefaultChild();

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }

  public static boolean isValid(char c)
  {
    if (c == ' ' || c == '<' || c == '>' || c == '-' || c == '+' || c == '=' || c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^' || c == '&' || c == '*' || c == '?' || c == ';' || c == ':' || c == ',' || c == '^' || c == '{' || c == '}' || c == ']' || c == '[' || c == '`' || c == '~' || c == '|' || c == '/' || c == '\\')
    {
      return false;
    }

    return true;
  }

}
