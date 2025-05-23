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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class StacLink
{
  // string REQUIRED. The actual link in the format of an URL. Relative and
  // absolute links are both allowed.
  private String href;

  /*
   * STAC Items use a variety of rel types in the link object, to describe the
   * exact nature of the link between this Item and the entity it is linking to.
   * It is recommended to use the official IANA Link Relation Types where
   * possible. The following table explains places where STAC use custom rel
   * types are used with Items. This happens where there is not a clear official
   * option, or where STAC uses an official type but adds additional meaning for
   * the STAC context.
   * 
   * self - STRONGLY RECOMMENDED. Absolute URL to the Item if it is available at
   * a public URL. This is particularly useful when in a download package that
   * includes metadata, so that the downstream user can know where the data has
   * come from.
   * 
   * root - URL to the root STAC entity (Catalog or Collection).
   * 
   * parent - URL to the parent STAC entity (Catalog or Collection).
   * 
   * collection - STRONGLY RECOMMENDED. URL to a Collection. Absolute URLs
   * should be used whenever possible. The referenced Collection is STRONGLY
   * RECOMMENDED to implement the same STAC version as the Item. A link with
   * this rel type is required if the collection field in properties is present.
   * 
   * derived_from - URL to a STAC Item that was used as input data in the
   * creation of this Item.
   * 
   * A more complete list of potential rel types and their meaning in STAC can
   * be found in the Using Relation Types best practice.
   */
  private String rel;

  // string Media type of the referenced entity.
  private String type;

  // string A human readable title to be used in rendered displays of the
  // link.
  @JsonInclude(Include.NON_NULL)
  private String title;

  public String getHref()
  {
    return href;
  }

  public void setHref(String href)
  {
    this.href = href;
  }

  public String getRel()
  {
    return rel;
  }

  public void setRel(String rel)
  {
    this.rel = rel;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public static StacLink build(String href, String rel, String type)
  {
    StacLink link = new StacLink();
    link.setHref(href);
    link.setRel(rel);
    link.setType(type);

    return link;
  }
}
