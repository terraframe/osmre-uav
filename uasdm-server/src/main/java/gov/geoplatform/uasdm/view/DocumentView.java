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
package gov.geoplatform.uasdm.view;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.serialization.GeoJsonSerializer;

public class DocumentView
{
  private String id;

  private String name;

  private String key;

  private String component; // An oid

  @JsonSerialize(using = GeoJsonSerializer.class)
  private Point  point;

  public DocumentView()
  {

  }

  public static DocumentView fromDocument(DocumentIF doc)
  {
    DocumentView view = new DocumentView();

    view.setId(doc.getOid());
    view.setName(doc.getName());
    view.setKey(doc.getS3location());
    view.setComponent(doc.getComponent().getOid());
    view.setPoint(doc.getPoint());

    return view;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getComponent()
  {
    return component;
  }

  public void setComponent(String component)
  {
    this.component = component;
  }

  public Point getPoint()
  {
    return point;
  }

  public void setPoint(Point point)
  {
    this.point = point;
  }

  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();

    JsonObject jo = (JsonObject) builder.create().toJsonTree(this);

    return jo;
  }

  public ODMRunView parse(String json)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(json, ODMRunView.class);
  }
}
