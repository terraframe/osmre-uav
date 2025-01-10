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
package gov.geoplatform.uasdm.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.system.Actor;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.remote.RemoteFileMetadata;
import gov.geoplatform.uasdm.remote.RemoteFileObject;
import gov.geoplatform.uasdm.view.Artifact;
import gov.geoplatform.uasdm.view.AttributeType;
import gov.geoplatform.uasdm.view.ComponentProductDTO;
import gov.geoplatform.uasdm.view.SiteObjectsResultSet;

public interface UasComponentIF extends ComponentWithAttributes
{
  public void apply();

  public void delete();

  public void deleteObject(String key);

  public String getOid();

  public String getOwnerOid();

  public String getName();

  public void setName(String name);

  public boolean isPrivate();

  public String getFolderName();

  public String getDescription();

  public String getSolrIdField();

  public String getSolrNameField();

  public UasComponentIF createChild(String type);

  public List<AttributeType> attributes();

  public void setGeoPoint(Point geometry);

  public MdClassDAOIF getMdClass();

  public Geometry getGeoPoint();

  public Long getNumberOfChildren();

  public void applyWithParent(UasComponentIF parent);

  public String getS3location();

  public String getS3location(ProductIF product, String folderOrFilename);

  public SiteObjectsResultSet getSiteObjects(String key, Long pageNumber, Long pageSize);

  public JSONArray getArtifacts();

  public Artifact[] getArtifactObjects(ProductIF product);

  public void removeArtifacts(ProductIF product, String folder, boolean updateMetadata);

  public List<DocumentIF> getDocuments();

  public AbstractWorkflowTask createWorkflowTask(String uuid, String uploadTarget);

  public Actor getOwner();

  public List<UasComponentIF> getAncestors();
  
  public List<UasComponentIF> getAncestors(boolean filterByPermissions);

  public RemoteFileObject download(String key);

  public RemoteFileObject download(String key, List<Range> ranges);

  public List<String> uploadArchive(AbstractWorkflowTask task, ApplicationFileResource archive, String uploadTarget, ProductIF product);

  public DocumentIF putFile(String folder, String fileName, ProductIF product, RemoteFileMetadata metadata, InputStream stream);

  public DocumentIF createDocumentIfNotExist(String key, String name, DocumentIF.Metadata metadata);

  public ProductIF createProductIfNotExist(String productName);
  
  public Optional<ProductIF> getPrimaryProduct();

  public void setPrimaryProduct(ProductIF product);

  public List<ComponentProductDTO> getDerivedProducts(String sortField, String sortOrder);

  public List<UasComponentIF> getChildren();

  public List<UasComponentIF> getChildrenWithConditions(String conditions);

  public UasComponentIF getChild(String name);

  public UasComponentIF createDefaultChild();

  public void writeFeature(JSONWriter writer) throws IOException;

  public static final String DISALLOWED_FILENAME_REGEX = "[^a-zA-Z0-9._]";

  public static boolean isValidName(String name)
  {
    return !Pattern.compile(DISALLOWED_FILENAME_REGEX).matcher(name == null ? "" : name).find();
  }

  public static boolean isValid(char c)
  {
    return Character.isLetterOrDigit(c) || c == '_';
  }

  public Optional<ProductIF> getProduct(String productName);

  void removeProduct(String productName);

  public <T extends ProductIF> List<T> getProducts();

  public List<Pair<ComponentWithAttributes, List<AttributeType>>> getCompositeAttributes();

  public void regenerateMetadata();
}
