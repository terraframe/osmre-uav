import os

from opendm import context
from opendm import types
from opendm import io
from opendm import system
from opendm import log

from dataset import ODMLoadDatasetStage
from run_opensfm import ODMOpenSfMStage
from mve import ODMMveStage
from odm_slam import ODMSlamStage
from odm_meshing import ODMeshingStage
from mvstex import ODMMvsTexStage
from odm_georeferencing import ODMGeoreferencingStage
from odm_orthophoto import ODMOrthoPhotoStage
from odm_dem import ODMDEMStage
from odm_filterpoints import ODMFilterPoints
from splitmerge import ODMSplitStage, ODMMergeStage
from odm_micasense import ODMMicasenseStage


class ODMApp:
    def __init__(self, args):
        """
        Initializes the application and defines the ODM application pipeline stages
        """
        if args.debug:
            log.logger.show_debug = True

        self.args = args

        mikasense = ODMMicasenseStage('mikasense', args, progress=5)
        dataset = ODMLoadDatasetStage('dataset', args, progress=15.0,
                                          verbose=args.verbose)
        split = ODMSplitStage('split', args, progress=75.0)
        merge = ODMMergeStage('merge', args, progress=100.0)
        opensfm = ODMOpenSfMStage('opensfm', args, progress=25.0)
        slam = ODMSlamStage('slam', args)
        mve = ODMMveStage('mve', args, progress=50.0)
        filterpoints = ODMFilterPoints('odm_filterpoints', args, progress=52.0)
        meshing = ODMeshingStage('odm_meshing', args, progress=60.0,
                                    max_vertex=args.mesh_size,
                                    oct_tree=args.mesh_octree_depth,
                                    samples=args.mesh_samples,
                                    point_weight=args.mesh_point_weight,
                                    max_concurrency=args.max_concurrency,
                                    verbose=args.verbose)
        texturing = ODMMvsTexStage('mvs_texturing', args, progress=70.0,
                                    data_term=args.texturing_data_term,
                                    outlier_rem_type=args.texturing_outlier_removal_type,
                                    skip_vis_test=args.texturing_skip_visibility_test,
                                    skip_glob_seam_leveling=args.texturing_skip_global_seam_leveling,
                                    skip_loc_seam_leveling=args.texturing_skip_local_seam_leveling,
                                    skip_hole_fill=args.texturing_skip_hole_filling,
                                    keep_unseen_faces=args.texturing_keep_unseen_faces,
                                    tone_mapping=args.texturing_tone_mapping)
        georeferencing = ODMGeoreferencingStage('odm_georeferencing', args, progress=80.0,
                                                    gcp_file=args.gcp,
                                                    verbose=args.verbose)
        dem = ODMDEMStage('odm_dem', args, progress=90.0,
                            max_concurrency=args.max_concurrency,
                            verbose=args.verbose)
        orthophoto = ODMOrthoPhotoStage('odm_orthophoto', args, progress=100.0)

        # Normal pipeline
        self.first_stage = mikasense

        mikasense.connect(dataset)

        dataset.connect(split) \
                .connect(merge) \
                .connect(opensfm)

        if args.use_opensfm_dense or args.fast_orthophoto:
            opensfm.connect(filterpoints)
        else:
            opensfm.connect(mve) \
                    .connect(filterpoints)

        filterpoints \
            .connect(meshing) \
            .connect(texturing) \
            .connect(georeferencing) \
            .connect(dem) \
            .connect(orthophoto)

        # # SLAM pipeline
        # # TODO: this is broken and needs work
        # log.ODM_WARNING("SLAM module is currently broken. We could use some help fixing this. If you know Python, get in touch at https://community.opendronemap.org.")
        # self.first_stage = slam

        # slam.connect(mve) \
        #     .connect(meshing) \
        #     .connect(texturing)

    def execute(self):
        outputs = {}

        # Load tree
        tree = types.ODM_Tree(self.args.project_path, self.args.gcp)
        outputs['tree'] = tree

        if self.args.time and io.file_exists(tree.benchmarking):
            # Delete the previously made file
            os.remove(tree.benchmarking)
            with open(tree.benchmarking, 'a') as b:
                b.write('ODM Benchmarking file created %s\nNumber of Cores: %s\n\n' % (system.now(), context.num_cores))

        self.first_stage.run(outputs)
