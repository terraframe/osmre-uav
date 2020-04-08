<%--

    Copyright (c) 2015 TerraFrame, Inc. All rights reserved.

    This file is part of Geoprism(tm).

    Geoprism(tm) is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Geoprism(tm) is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Geoprism(tm).  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page language="java" contentType="text/javascript; charset=UTF-8" pageEncoding="UTF-8" %>

<%@page import="net.geoprism.localization.LocalizationFacadeDTO"%>
<%@page import="com.runwaysdk.constants.ClientConstants"%>
<%@page import="com.runwaysdk.constants.ClientRequestIF"%>

<!--It is very important to set the content type so that FF knows to read in these strings as UTF-8 -->
<%
ClientRequestIF clientRequest = (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);

String region = LocalizationFacadeDTO.getCalendarLocale(clientRequest);
String configuration = LocalizationFacadeDTO.getConfigurationJSON(clientRequest);
String locale = LocalizationFacadeDTO.getCLDRLocaleName(clientRequest);
%>

/**
 * Constants used for localization in javascript.
 */
com.runwaysdk.Localize.addLanguages(<%=LocalizationFacadeDTO.getJSON(clientRequest)%>, true);
