<template>
  <div class="tw-relative tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-shadow-sm">
    <div
      ref="mapContainer"
      class="tw-h-[360px] tw-w-full tw-rounded-2xl tw-bg-slate-100"
    ></div>

    <div
      class="tw-pointer-events-none tw-absolute tw-inset-0 tw-flex tw-flex-col tw-items-end tw-justify-start tw-gap-3 tw-p-4"
    >
      <div class="tw-pointer-events-auto tw-flex tw-flex-col tw-gap-2">
        <button
          type="button"
          class="tw-inline-flex tw-items-center tw-justify-center tw-rounded-full tw-bg-emerald-600 tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-white tw-shadow-sm tw-transition hover:tw-bg-emerald-700 disabled:tw-cursor-not-allowed disabled:tw-bg-emerald-300"
          @click="startDrawing"
          :disabled="isDrawing || loading"
        >
          <span v-if="!isDrawing">Draw Polygon</span>
          <span v-else>Drawing...</span>
        </button>
        <button
          type="button"
          class="tw-inline-flex tw-items-center tw-justify-center tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-slate-600 tw-transition hover:tw-border-slate-300 hover:tw-text-slate-700 disabled:tw-cursor-not-allowed disabled:tw-text-slate-300"
          @click="clearGeometry"
          :disabled="!hasGeometry || loading"
        >
          Clear
        </button>
      </div>
    </div>

    <transition name="fade">
      <div
        v-if="loading"
        class="tw-absolute tw-inset-0 tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-3 tw-rounded-2xl tw-bg-white/80 tw-backdrop-blur"
      >
        <div class="tw-h-10 tw-w-10 tw-animate-spin tw-rounded-full tw-border-4 tw-border-emerald-200 tw-border-t-emerald-600"></div>
        <p class="tw-text-sm tw-font-medium tw-text-slate-500">{{ loadingMessage }}</p>
      </div>
    </transition>

    <transition name="fade">
      <div
        v-if="errorMessage"
        class="tw-absolute tw-bottom-4 tw-left-1/2 tw-w-[90%] tw-max-w-lg tw--translate-x-1/2 tw-rounded-xl tw-border tw-border-red-200 tw-bg-red-50 tw-px-4 tw-py-3 tw-text-sm tw-text-red-700 tw-shadow"
      >
        {{ errorMessage }}
      </div>
    </transition>
  </div>
</template>

<script>
/* eslint-disable */
import { loadModules } from 'esri-loader';

export default {
  name: 'PolygonIntersectionMap',
  props: {
    geoJson: {
      type: String,
      default: '',
    },
  },
  emits: ['geometry-change'],
  data() {
    return {
      loading: true,
      loadingMessage: 'Loading map...',
      errorMessage: '',
      map: null,
      view: null,
      graphicsLayer: null,
      sketchViewModel: null,
      geometryEngine: null,
      PolygonClass: null,
      GraphicClass: null,
      currentGraphic: null,
      isDrawing: false,
    };
  },
  computed: {
    hasGeometry() {
      return !!this.currentGraphic;
    },
  },
  async mounted() {
    await this.initializeMap();
    if (this.geoJson) {
      this.renderGeoJson(this.geoJson);
    }
  },
  beforeDestroy() {
    if (this.view) {
      this.view.destroy();
    }
  },
  watch: {
    geoJson(newValue) {
      if (!newValue) {
        this.clearGeometry();
        return;
      }
      this.renderGeoJson(newValue);
    },
  },
  methods: {
    async initializeMap() {
      try {
        const [EsriMap, MapView, GraphicsLayer, SketchViewModel, Graphic, geometryEngine, Polygon] = await loadModules(
          [
            'esri/Map',
            'esri/views/MapView',
            'esri/layers/GraphicsLayer',
            'esri/widgets/Sketch/SketchViewModel',
            'esri/Graphic',
            'esri/geometry/geometryEngine',
            'esri/geometry/Polygon',
          ],
          {
            url: 'https://js.arcgis.com/4.28/',
            css: 'https://js.arcgis.com/4.28/esri/themes/light/main.css',
          },
        );

        this.geometryEngine = geometryEngine;
        this.GraphicClass = Graphic;
        this.PolygonClass = Polygon;
        this.graphicsLayer = new GraphicsLayer();
        this.map = new EsriMap({
          basemap: 'satellite',
          layers: [this.graphicsLayer],
        });

        this.view = new MapView({
          container: this.$refs.mapContainer,
          map: this.map,
          center: [36.8219, -1.2921],
          zoom: 12,
          constraints: {
            snapToZoom: false,
          },
        });

        await this.view.when();

        this.sketchViewModel = new SketchViewModel({
          view: this.view,
          layer: this.graphicsLayer,
          polygonSymbol: {
            type: 'simple-fill',
            color: [16, 185, 129, 0.25],
            outline: {
              color: [16, 185, 129, 0.85],
              width: 2,
            },
          },
          defaultUpdateOptions: {
            toggleToolOnClick: false,
          },
        });

        this.sketchViewModel.on('create', (event) => {
          if (event.state === 'start') {
            this.isDrawing = true;
            this.errorMessage = '';
          }
          if (event.state === 'complete') {
            this.isDrawing = false;
            this.onGeometryCreated(event.graphic);
          }
          if (event.state === 'cancel') {
            this.isDrawing = false;
          }
        });

        this.sketchViewModel.on('update', (event) => {
          if (event.state === 'complete' && event.graphics?.length) {
            this.onGeometryCreated(event.graphics[0]);
          }
        });

        this.loading = false;
      } catch (error) {
        // eslint-disable-next-line no-console
        this.errorMessage = 'Unable to initialise map. Please refresh the page.';
        this.loading = false;
      }
    },
    onGeometryCreated(graphic) {
      if (!graphic?.geometry) return;

      if (this.currentGraphic) {
        this.graphicsLayer.remove(this.currentGraphic);
      }
      this.currentGraphic = graphic;
      this.graphicsLayer.add(graphic);

      this.view.goTo({ target: graphic.geometry.extent.expand(1.2) });

      this.emitGeometry(graphic.geometry);
    },
    emitGeometry(geometry) {
      try {
        const geoJson = {
          type: geometry.type === 'polygon' ? 'Polygon' : geometry.type,
          coordinates: geometry.rings,
        };

        const areaSqMeters = Math.abs(this.geometryEngine.geodesicArea(geometry, 'square-meters')) || 0;
        const perimeterMeters = Math.abs(this.geometryEngine.geodesicLength(geometry, 'meters')) || 0;

        this.$emit('geometry-change', {
          geoJson: JSON.stringify(geoJson),
          areaHectares: +(areaSqMeters / 10000).toFixed(4),
          areaSquareMeters: +areaSqMeters.toFixed(2),
          perimeterMeters: +perimeterMeters.toFixed(2),
          centroid: geometry.centroid ? [geometry.centroid.latitude, geometry.centroid.longitude] : null,
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        this.errorMessage = 'Something went wrong while processing the polygon.';
      }
    },
    startDrawing() {
      if (!this.sketchViewModel || this.loading) return;
      this.sketchViewModel.create('polygon');
    },
    clearGeometry() {
      if (this.currentGraphic) {
        this.graphicsLayer.remove(this.currentGraphic);
        this.currentGraphic = null;
        this.$emit('geometry-change', null);
      }
    },
    async renderGeoJson(geoJsonString) {
      try {
        if (!this.sketchViewModel) return;
        const parsed = typeof geoJsonString === 'string' ? JSON.parse(geoJsonString) : geoJsonString;
        if (!parsed || parsed.type !== 'Polygon' || !parsed.coordinates?.length) {
          throw new Error('Unsupported GeoJSON geometry supplied. Only Polygon geometries are supported.');
        }

        const polygon = new this.PolygonClass({
          rings: parsed.coordinates,
          spatialReference: { wkid: 4326 },
        });

        const graphic = new this.GraphicClass({
          geometry: polygon,
          symbol: {
            type: 'simple-fill',
            color: [16, 185, 129, 0.25],
            outline: {
              color: [16, 185, 129, 0.85],
              width: 2,
            },
          },
        });

        if (this.currentGraphic) {
          this.graphicsLayer.remove(this.currentGraphic);
        }
        this.graphicsLayer.add(graphic);
        this.currentGraphic = graphic;

        this.view.goTo({ target: polygon.extent.expand(1.15) });
        this.emitGeometry(polygon);
      } catch (error) {
        // eslint-disable-next-line no-console
        this.errorMessage = 'Failed to display the provided GeoJSON polygon.';
      }
    },
  },
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
