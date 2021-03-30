<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="description" content="">
	<meta name="author" content="">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
	<title>Potree Viewer</title>

	<link rel="stylesheet" type="text/css" href="../resource/build/potree/potree.css">
	<link rel="stylesheet" type="text/css" href="../resource/libs/jquery-ui/jquery-ui.min.css">
	<link rel="stylesheet" type="text/css" href="../resource/libs/openlayers3/ol.css">
	<link rel="stylesheet" type="text/css" href="../resource/libs/spectrum/spectrum.css">
	<link rel="stylesheet" type="text/css" href="../resource/libs/jstree/themes/mixed/style.css">
</head>

<body>
	<script src="../resource/libs/jquery/jquery-3.1.1.min.js"></script>
	<script src="../resource/libs/spectrum/spectrum.js"></script>
	<script src="../resource/libs/jquery-ui/jquery-ui.min.js"></script>

	<script src="../resource/libs/other/BinaryHeap.js"></script>
	<script src="../resource/libs/tween/tween.min.js"></script>
	<script src="../resource/libs/d3/d3.js"></script>
	<script src="../resource/libs/proj4/proj4.js"></script>
	<script src="../resource/libs/openlayers3/ol.js"></script>
	<script src="../resource/libs/i18next/i18next.js"></script>
	<script src="../resource/libs/jstree/jstree.js"></script>
	<script src="../resource/build/potree/potree.js"></script>
	<script src="../resource/libs/plasio/js/laslaz.js"></script>

	<!-- INCLUDE ADDITIONAL DEPENDENCIES HERE -->
	<!-- INCLUDE SETTINGS HERE -->

	<div class="potree_container" style="position: absolute; width: 100%; height: 100%; left: 0px; top: 0px; ">
		<div id="potree_render_area"></div>
		<div id="potree_sidebar_container"> </div>
	</div>

	<script type="module">

	import * as THREE from "../resource/libs/three.js/build/three.module.js";

		window.viewer = new Potree.Viewer(document.getElementById("potree_render_area"));

		viewer.setEDLEnabled(true);
		viewer.setFOV(60);
		viewer.setPointBudget(1_000_000);
		viewer.setClipTask(Potree.ClipTask.SHOW_INSIDE);
		viewer.loadSettingsFromURL();


		viewer.loadGUI(() => {
			viewer.setLanguage('en');
			$("#menu_appearance").next().show();
			$("#menu_tools").next().show();
			$("#menu_scene").next().show();
			viewer.toggleSidebar();

			$('#sldPointBudget').slider({
				max: 3_000_000,
				step: 1000,
			});
		});

		// Load and add point cloud to scene
		Potree.loadPointCloud("/uasdm/pointcloud/<%= request.getAttribute("componentId") %>/cloud.js", "Dynamically Loaded Data", function(e){
			let scene = viewer.scene;
			let pointcloud = e.pointcloud;

			// let material = pointcloud.material;
			// material.size = 1;
			// material.pointSizeType = Potree.PointSizeType.ADAPTIVE;
			// material.shape = Potree.PointShape.SQUARE;
			// material.activeAttributeName = "elevation";

			scene.addPointCloud(pointcloud);

			// let volume = new Potree.BoxVolume();
			// volume.position.set(18.11, 14.94, 1.50);
			// volume.scale.set(28.08, 19.07, 4.41);
			// volume.clip = true;
			// scene.addVolume(volume);

			// scene.view.setView(
			// 	[5.69, 15.51, 10.62],
			// 	[15.80, 15.27, 0.72],
			// );

			viewer.setTopView();

		});

	</script>


  </body>
</html>
