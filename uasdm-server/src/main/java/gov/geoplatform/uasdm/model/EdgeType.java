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

public interface EdgeType
{
  public static final String PRODUCT_HAS_DOCUMENT       = "gov.geoplatform.uasdm.graph.ProductHasDocument";

  public static final String COMPONENT_HAS_PRODUCT      = "gov.geoplatform.uasdm.graph.ComponentHasProduct";

  public static final String COMPONENT_HAS_DOCUMENT     = "gov.geoplatform.uasdm.graph.ComponentHasDocument";

  public static final String DOCUMENT_GENERATED_PRODUCT = "gov.geoplatform.uasdm.graph.DocumentGeneratedProduct";

  public static final String SITE_HAS_PROJECT           = "gov.geoplatform.uasdm.graph.SiteHasProject";

  public static final String PROJECT_HAS_MISSION        = "gov.geoplatform.uasdm.graph.ProjectHasMission";

  public static final String MISSION_HAS_COLLECTION     = "gov.geoplatform.uasdm.graph.MissionHasCollection";
}
