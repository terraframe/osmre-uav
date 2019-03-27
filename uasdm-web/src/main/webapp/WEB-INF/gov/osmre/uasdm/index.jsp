<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>
<%@ taglib uri="http://jawr.net/tags" prefix="jwr"%>

<!DOCTYPE html>

<head>
<meta charset='utf-8' />
<meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />

<title><gdb:localize key="project.management.title" /></title>
<link rel="icon" href="${pageContext.request.contextPath}/net/geoprism/images/splash_logo_icon.png" />

<base href="<%=request.getContextPath()%>/project/management">

<style>
body {
	background-color: #efe9e1
}
</style>

<script>
    window.acp = "<%=request.getContextPath()%>";
	window.location.origin = window.location.protocol + "//"
			+ window.location.hostname
			+ (window.location.port ? ':' + window.location.port : '');
</script>

<!-- CSS imports -->
<jwr:style src="/bundles/datatable.css" useRandomParam="false" />
<jwr:style src="/bundles/main.css" useRandomParam="false" />

<!-- Default imports -->
<jwr:script src="/bundles/runway.js" useRandomParam="false" />
<jwr:script src="/bundles/main.js" useRandomParam="false" />
<jwr:script src="/bundles/localization.js" useRandomParam="false" />

<script type="text/javascript" src="${pageContext.request.contextPath}/net/geoprism/Localized.js.jsp"></script>

<!-- IE required polyfills, in this exact order -->

<!-- <script type="text/javascript" src="https://localhost:8080/dist/uasdm-polyfills.js"></script> -->
<!-- <script type="text/javascript" src="https://localhost:8080/dist/uasdm-vendor.js"></script> -->
  <script type="text/javascript" src="${pageContext.request.contextPath}/dist/uasdm-polyfills.js"></script>  
  <script type="text/javascript" src="${pageContext.request.contextPath}/dist/uasdm-vendor.js"></script>  


    <script type="text/template" id="qq-template">
        <div class="qq-uploader-selector qq-uploader" qq-drop-area-text="Drag & drop your files here">
           <%-- <div class="qq-total-progress-bar-container-selector qq-total-progress-bar-container">
                <div role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" class="qq-total-progress-bar-selector qq-progress-bar qq-total-progress-bar"></div>
            </div> --%>
            <div class="qq-upload-drop-area-selector qq-upload-drop-area" qq-hide-dropzone>
                <span class="qq-upload-drop-area-text-selector"></span>
            </div>
            <div class="buttons">
                <div class="qq-upload-button-selector qq-upload-button">
                    <div>Select file</div>
                </div>
            </div>
            <span class="qq-drop-processing-selector qq-drop-processing">
                <span>Processing dropped files...</span>
                <span class="qq-drop-processing-spinner-selector qq-drop-processing-spinner"></span>
            </span>
            <ul class="qq-upload-list-selector qq-upload-list" aria-live="polite" aria-relevant="additions removals">
                <li>
                    <div class="qq-progress-bar-container-selector">
                        <div role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" class="qq-progress-bar-selector qq-progress-bar"></div>
                    </div>
                    <span class="qq-upload-spinner-selector qq-upload-spinner"></span>
                    <img class="qq-thumbnail-selector" qq-max-size="100" qq-server-scale>
                    <span class="qq-upload-file-selector qq-upload-file"></span>
                    <span class="qq-edit-filename-icon-selector qq-edit-filename-icon" aria-label="Edit filename"></span>
                    <input class="qq-edit-filename-selector qq-edit-filename" tabindex="0" type="text">
                    <span class="qq-upload-size-selector qq-upload-size"></span>
                    <button type="button" class="qq-btn qq-upload-cancel-selector qq-upload-cancel">Cancel</button>
                    <button type="button" class="qq-btn qq-upload-retry-selector qq-upload-retry">Retry</button>
                    <button type="button" class="qq-upload-pause-selector qq-upload-pause">Pause</button>
                    <button type="button" class="qq-upload-continue-selector qq-upload-continue">Continue</button>
                    <span role="status" class="qq-upload-status-text-selector qq-upload-status-text"></span>
                </li>
            </ul>

            <dialog class="qq-alert-dialog-selector">
                <div class="qq-dialog-message-selector"></div>
                <div class="qq-dialog-buttons">
                    <button type="button" class="qq-cancel-button-selector">Close</button>
                </div>
            </dialog>

            <dialog class="qq-confirm-dialog-selector">
                <div class="qq-dialog-message-selector"></div>
                <div class="qq-dialog-buttons">
                    <button type="button" class="qq-cancel-button-selector">No</button>
                    <button type="button" class="qq-ok-button-selector">Yes</button>
                </div>
            </dialog>

            <dialog class="qq-prompt-dialog-selector">
                <div class="qq-dialog-message-selector"></div>
                <input type="text">
                <div class="qq-dialog-buttons">
                    <button type="button" class="qq-cancel-button-selector">Cancel</button>
                    <button type="button" class="qq-ok-button-selector">Ok</button>
                </div>
            </dialog>
        </div>
    </script>
</head>

<body>
  <!--   <div> -->
  <uasdm-app> </uasdm-app>

<!--     <script type="text/javascript" src="https://localhost:8080/dist/uasdm-app.js"></script>       -->
     <script type="text/javascript" src="${pageContext.request.contextPath}/dist/uasdm-app.js"></script>  
</body>
