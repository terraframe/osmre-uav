<%--

    Copyright 2020 The Department of Interior

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

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
