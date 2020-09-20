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
package gov.geoplatform.uasdm.service;

import java.util.Set;
import java.util.function.Predicate;

import gov.geoplatform.uasdm.view.SiteObject;

public class ExcludeSiteObjectPredicate implements Predicate<SiteObject>
{
  private Set<String> filenames;

  public ExcludeSiteObjectPredicate(Set<String> filenames)
  {
    this.filenames = filenames;
  }

  @Override
  public boolean test(SiteObject t)
  {
    return !this.filenames.contains(t.getName());
  }
}
