/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.TreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.georegistry.service.RegistryService;

@Controller(url = "registry")
public class RegistryController
{
  private RegistryService registryService;
  
  public RegistryController()
  {
    this.registryService = new RegistryService();
  }
  
  /**
   * Returns a GeoObject with the given uid.
   *
   * @pre
   * @post
   *
   * @param uid The UID of the GeoObject.
   *
   * @returns a GeoObject in GeoJSON format with the given uid.
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
   public ResponseIF getGeoObject(ClientRequestIF request, @RequestParamter(name = "uid") String uid) throws JSONException
   {
     GeoObject geoObject = this.registryService.getGeoObject(request.getSessionId(), uid);
     
     return new RestBodyResponse(geoObject.toJSON());
   }
   
   /**
   * Update a new GeoObject in the Common Geo-Registry
   *
   * @pre 
   * @post 
   *
   * @param geoObject in GeoJSON format to be updated.
   *
   * @returns 
   * @throws //TODO
   **/
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
   public ResponseIF updateGeoObject(ClientRequestIF request, @RequestParamter(name = "geoObject") String jGeoObj)
   {
     GeoObject geoObject = this.registryService.updateGeoObject(request.getSessionId(), jGeoObj);
     
     return new RestBodyResponse(geoObject.toJSON());
   }
   
   /**
   * Get children of the given GeoObject
   *
   * @pre 
   * @post 
   *
   * @param parentUid UID of the parent object for which the call fetches children.
   * @param childrentTypes An array of GeoObjectType names of the types of children GeoObjects to fetch. If blank then return children of all types.
   * @param recursive TRUE if recursive children of the given parent with the given types should be returned, FALSE if only single level children should be returned.  
   * 
   * @returns
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
   public ResponseIF getChildGeoObjects(ClientRequestIF request, @RequestParamter(name = "parentUid") String parentUid, @RequestParamter(name = "childrenTypes") String[] childrenTypes, @RequestParamter(name = "recursive") Boolean recursive)
   {
     TreeNode tn = this.registryService.getChildGeoObjects(request.getSessionId(), parentUid, childrenTypes, recursive);
     
     return new RestBodyResponse(tn.toJSON());
   }
    
   /**
   * Get parents of the given GeoObject
   *
   * @pre 
   * @post 
   *
   * @param childUid UID of the child object for which the call fetches parents.
   * @param parentTypes An array of GeoObjectType names of the types of parent GeoObjects to fetch. If blank then return parents of all types.
   * @param recursive TRUE if recursive parents of the given parent with the given types should be returned, FALSE if only single level parents should be returned.  
   * 
   * @returns
   * @throws
   **/   
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
   public ResponseIF getParentGeoObjects(ClientRequestIF request, @RequestParamter(name = "childUid") String childUid, @RequestParamter(name = "parentTypes") String[] parentTypes, @RequestParamter(name = "recursive") Boolean recursive)
   {
     TreeNode tn = this.registryService.getParentGeoObjects(request.getSessionId(), childUid, parentTypes, recursive);
     
     return new RestBodyResponse(tn.toJSON());
   }
   
   
   /**
   * Get list of valid UIDs for use in creating new GeoObjects. The Common Geo-Registry will only accept newly created GeoObjects with a UID that was issued from the Common GeoRegistry.
   *
   * @pre 
   * @post 
   *
   * @param amount Number of globally unique ids that the Common Geo-Registry will issue to the mobile device.
   *
   * @returns
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
   public ResponseIF getUIDs(ClientRequestIF request, @RequestParamter(name = "amount") Integer amount)
   {
     String[] ids = this.registryService.getUIDS(request.getSessionId(), amount);
     
     return new RestBodyResponse(ids);
   }
   
   
   /**
   * Return GeoOjectType objects that define the given list of types.
   *
   * @pre 
   * @post 
   *
   * @param types An array of GeoObjectType codes. If blank then all GeoObjectType objects are returned.
   *
   * @returns
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
   public ResponseIF getGeoObjectTypes(ClientRequestIF request, @RequestParamter(name = "types") String[] types)
   {
     GeoObjectType[] gots = this.registryService.getGeoObjectTypes(request.getSessionId(), types);
     
     JsonArray jarray = new JsonArray();
     for (int i = 0; i < gots.length; ++i)
     {
       jarray.add(gots[i].toJSON());
     }
     
     return new RestBodyResponse(jarray);
   }
}
