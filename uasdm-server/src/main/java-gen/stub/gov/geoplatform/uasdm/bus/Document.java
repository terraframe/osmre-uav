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
package gov.geoplatform.uasdm.bus;

import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.command.RemoteFileDeleteCommand;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.ProductIF;
import gov.geoplatform.uasdm.model.UasComponentIF;

public class Document extends DocumentBase implements DocumentIF
{
  private static final long serialVersionUID = -877956259;

  public Document()
  {
    super();
  }

  @Override
  public Boolean getExclude()
  {
    return false;
  }

  @Override
  public void delete()
  {
    this.delete(true);
  }

  @Transaction
  public void delete(boolean removeFromS3)
  {
    super.delete();

    if (removeFromS3 && !this.getS3location().trim().equals(""))
    {
      this.deleteS3File(this.getS3location());
    }
  }

  protected void deleteS3File(String key)
  {
    new RemoteFileDeleteCommand(key).doIt();
  }

  @Override
  protected String buildKey()
  {
    return this.getS3location();
  }
  
  @Override
  public void setExclude(Boolean exclude)
  {
    // Do nothing   
  }

  public void addGeneratedProduct(ProductIF product)
  {
    DocumentGeneratedProduct pd = this.getDocumentGeneratedProduct(product);

    if (pd == null)
    {
      this.addGeneratedProducts((Product) product).apply();
    }
  }

  public DocumentGeneratedProduct getDocumentGeneratedProduct(ProductIF product)
  {
    DocumentGeneratedProductQuery query = new DocumentGeneratedProductQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(this));
    query.AND(query.getChild().EQ((Product) product));

    try (OIterator<? extends DocumentGeneratedProduct> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("id", this.getOid());
    object.put("key", this.getS3location());
    object.put("name", this.getName());
    object.put("component", this.getComponentOid());

    return object;
  }

  public static Document find(String key)
  {
    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getS3location().EQ(key));

    try (OIterator<? extends Document> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static Document createIfNotExist(UasComponentIF uasComponent, String key, String name)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setS3location(key);
    }
    else
    {
      document.appLock();
    }

    document.setComponent((UasComponent) uasComponent);
    document.setName(name);
    document.apply();

    return document;
  }

}
