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
package gov.geoplatform.uasdm.service.business;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.SingleActor;

import gov.geoplatform.uasdm.UserInfo;
import gov.geoplatform.uasdm.doc.HelpPageContent;
import net.geoprism.registry.Organization;
import net.geoprism.registry.graph.GraphOrganization;

/**
 * This component was initially built with support for a 'null is root' concept that isn't currently used. At the moment, the
 * front-end does not allow for editing of this 'root' page. Rather than ripping the concept out, I'm keeping support for it
 * (for now) since it was found to work. If in the future maintaining support isn't worth it, feel free to remove it.
 */
@Service
@Primary
public class HelpPageBusinessService
{
  public JsonObject content(String orgCode)
  {
    GraphOrganization graphOrg = findOrg(orgCode);
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    String statement;
    
    if (graphOrg != null) {
      parameters.put("org", graphOrg.getRID());
      
      statement = "SELECT FROM help_page_content\n"
          + "WHERE organization IN (\n"
          + "  SELECT FROM (\n"
          + "    TRAVERSE in('organization_hierarchy')\n"
          + "    FROM :org\n"
          + "    WHILE $depth <= 50\n"
          + "  )\n"
          + "  WHERE @class = 'graph_organization'\n"
          + ")\n"
          + "LIMIT 1";
    } else {
      statement = "SELECT FROM help_page_content WHERE organization IS NULL LIMIT 1;";
    }
    
    var gq = new GraphQuery<HelpPageContent>(statement, parameters);
    
    var content = gq.getSingleResult();
    
    if (content == null)
      content = HelpPageContent.getByOrg(null); // "fake root" universal fallback
    
    JsonObject json = new JsonObject();
    
    if (content != null) {
        json.addProperty("markdown", content.getMarkdown());
    }
    
    JsonObject jsonOrg = new JsonObject();
    
    if (graphOrg != null) {
      var graphDto = graphOrg.toDTO();
      
//      json.add("organization", graphDto.toJSON());
      
      jsonOrg.addProperty("code", graphOrg.getCode());
      jsonOrg.add("label", graphDto.getLabel().toJSON());
      json.add("organization", jsonOrg);
    }
    
    return json;
  }
  
  public void edit(String orgCode, String content)
  {
    GraphOrganization graphOrg = orgCode == null ? null : findOrg(orgCode);
    
    var helpPage = HelpPageContent.getByOrg(graphOrg);
    
    if (helpPage == null) {
      helpPage = new HelpPageContent();
      helpPage.setOrganization(graphOrg);
    }
    
    helpPage.setMarkdown(content);
    helpPage.apply();
  }
  
  protected GraphOrganization findOrg(String orgCode)
  {
    Organization org = null;
    if (orgCode != null) {
      org = Organization.getByCode(orgCode);
    } else {
      SessionIF session = Session.getCurrentSession();
      SingleActorDAOIF user = session == null ? null : session.getUser();
      UserInfo userInfo = user == null ? null : UserInfo.getUserInfo(user.getOid());
      
      if (userInfo != null)
        org = userInfo.getOrganizations().stream().findFirst().orElse(null);
    }
    
    if (org == null)
      return null; // null here means "root"
    
    var graphOrg = GraphOrganization.get(org);
    
    return graphOrg;
  }

}
