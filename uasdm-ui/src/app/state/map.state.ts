///
///
///

import { createActionGroup, props, createReducer, on, createFeatureSelector, createSelector, emptyProps } from "@ngrx/store";
// @ts-ignore
import { StacCollection, StacItem, StacLink, ToggleableLayer, ToggleableLayerType } from "@site/model/layer";
import { ImageSet } from "@site/model/management";
import * as turf from '@turf/turf';
import { v4 as uuid } from "uuid";
import * as lodash from 'lodash';

export const MapActions = createActionGroup({
    source: 'map',
    events: {
        'Set Map Layers': props<{ mapLayers: ToggleableLayer[] }>(),
        'Add Map Layer': props<{ layer: ToggleableLayer, routeSet?: string }>(),
        'Remove Map Layer': props<{ id: string }>(),
        'Set Image Sets': props<{ sets: ImageSet[] }>(),
        'Add Image Set': props<{ set: ImageSet }>(),
        'Remove Image Set': props<{ id: string }>(),
        'Toggle Set': props<{ set: ImageSet }>(),
        'Toggle Visibility': props<{ layer: ToggleableLayer }>(),
        'Set Collection': props<{ collection: StacCollection }>(),
        'Toggle Link Item': props<{ link: StacLink, item?: StacItem }>(),
        'Set Link Item': props<{ link: StacLink, item?: StacItem }>(),
        'Toggle Collection Visibility': emptyProps(),
        'Clear': emptyProps()
    },
});


export interface MapStateModel {
    mapLayers: ToggleableLayer[];
    sets: ImageSet[];
    routeSet: string;
    collection: StacCollection | null;
    visible: boolean;
}

export const initialState: MapStateModel = {
    mapLayers: [],
    sets: [],
    routeSet: "",
    collection: null,
    visible: false
}

export function createSetLayer(set: ImageSet): ToggleableLayer {
    const component: string = set.components[set.components.length - 1].id;

    const features = set.documents.filter(d => d.point != null).map(d => {
        return turf.feature(d.point, { label: d.name, component, key: d.key, type: ToggleableLayerType.IMAGE_SET });
    });

    const layer: ToggleableLayer = {
        id: set.id,
        type: ToggleableLayerType.IMAGE_SET,
        layerName: set.name,
        active: true,
        item: set,
        geojson: {
            type: "FeatureCollection",
            features: features
        }
    };

    return layer;
}

export const mapReducer = createReducer(
    initialState,

    on(MapActions.setLinkItem, (state, { link, item }) => {

        const links = [...state.collection.links]

        const index = links.findIndex(s => s.id === link.id);

        if (index !== -1) {
            links[index] = { ...link, item: item };
        }

        return {
            ...state,
            collection: { ...state.collection, links }
        };
    }),
    on(MapActions.toggleCollectionVisibility, (state) => {
        return {
            ...state,
            visible: !state.visible
        };

    }),
    on(MapActions.toggleLinkItem, (state, { link, item }) => {

        const links = [...state.collection.links]

        const index = links.findIndex(s => s.id === link.id);

        if (index !== -1) {
            links[index] = { ...link, open: !link.open, item: item != null ? item : link.item };
        }

        return {
            ...state,
            collection: { ...state.collection, links }
        };
    }),
    on(MapActions.setCollection, (state, params) => {

        const collection = lodash.cloneDeep(params.collection);

        if (collection != null) {
            // Setup the bbox information
            for (let i = 1; i < collection.links.length; i++) {
                collection.links[i].bbox = collection.extent.spatial.bbox[i];
                collection.links[i].id = uuid();
            }
        }


        return {
            ...state,
            collection,
            visible: false
        };
    }),
    on(MapActions.setMapLayers, (state, { mapLayers }) => {

        return {
            ...state,
            mapLayers
        };
    }),
    on(MapActions.addMapLayer, (state, { layer, routeSet }) => {

        if (state.mapLayers.findIndex(m => m.id === layer.id) === -1) {
            return {
                ...state,
                mapLayers: [...state.mapLayers, layer],
                routeSet
            };
        }

        return state;

    }),
    on(MapActions.removeMapLayer, (state, { id }) => {

        const mapLayers = state.mapLayers.filter(f => f.id !== id)

        // Update the collection
        if (state.collection != null) {
            const links = [...state.collection.links]
                .filter(l => l.item != null && l.item.id === id)
                .map(link => {
                    const item = { ...link.item, enabled: false }
                    return { ...link, item }
                });

            return {
                ...state,
                mapLayers,
                collection: { ...state.collection, links }
            };
        }

        return {
            ...state,
            mapLayers
        };
    }),
    on(MapActions.setImageSets, (state, { sets }) => {

        const s = JSON.parse(JSON.stringify(sets));

        s.filter(s => s.id === state.routeSet).forEach(set => {
            set.mapped = true;
        })

        s.filter(s => state.mapLayers.findIndex(l => l.id === s.id) !== -1).forEach(set => {
            set.mapped = true;
        })


        return {
            ...state,
            sets: s
        };
    }),
    on(MapActions.addImageSet, (state, { set }) => {

        if (state.sets.findIndex(m => m.id === set.id) === -1) {
            return {
                ...state,
                sets: [...state.sets, set]
            };
        }

        return state;

    }),
    on(MapActions.removeMapLayer, (state, { id }) => {

        const sets = state.sets.filter(f => f.id !== id)

        return {
            ...state,
            sets
        };
    }),
    on(MapActions.toggleVisibility, (state, { layer }) => {

        const mapLayers = [...state.mapLayers]

        const index = mapLayers.findIndex(s => s.id === layer.id);

        if (index !== -1) {
            mapLayers[index] = { ...layer, active: !layer.active };
        }

        return {
            ...state,
            mapLayers
        };
    }),
    on(MapActions.toggleSet, (state, params) => {

        const set = { ...params.set, mapped: !params.set.mapped }

        const newSet = [...state.sets];
        const index = newSet.findIndex(s => s.id === set.id);

        if (index !== -1) {
            newSet[index] = set;
        }

        if (set.mapped) {

            if (state.mapLayers.findIndex(m => m.id === set.id) === -1) {

                const layer = createSetLayer(set);

                return {
                    ...state,
                    mapLayers: [...state.mapLayers, layer],
                    sets: newSet
                };
            }
        }
        else {
            const mapLayers = state.mapLayers.filter(f => f.id !== set.id)

            return {
                ...state,
                mapLayers,
                sets: newSet
            };
        }

    }),
    on(MapActions.clear, () => {
        return initialState;
    }),

);


const selector = createFeatureSelector<MapStateModel>('map');

export const getMapLayers = createSelector(selector, (s) => {
    return s.mapLayers;
});

export const getImageSets = createSelector(selector, (s) => {
    return s.sets;
});

export const getCollection = createSelector(selector, (s) => {
    return s.collection;
});

export const getVisible = createSelector(selector, (s) => {
    return s.visible;
});