<template>
  <div class="tw-flex tw-min-h-screen tw-flex-col tw-gap-6 tw-bg-slate-50 tw-p-6">
    <!-- Header -->
    <div class="tw-flex tw-items-center tw-justify-between">
      <div>
        <div class="tw-mb-2 tw-inline-flex tw-items-center tw-rounded-full tw-bg-emerald-100 tw-px-3 tw-py-1 tw-text-sm tw-font-semibold tw-text-emerald-700">
          <span class="tw-mr-2">🗺️</span>
          Production Unit Registration
        </div>
        <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">
          {{ editMode ? 'Edit Production Unit' : 'Add New Production Unit' }}
        </h1>
        <p class="tw-mt-1 tw-text-sm tw-text-slate-600">
          Draw your farm boundary on the map. The system will validate your polygon for EUDR compliance.
        </p>
      </div>
      <button
        v-if="onCancel"
        type="button"
        class="tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-medium tw-text-slate-600 tw-transition hover:tw-bg-slate-50"
        @click="handleCancel"
      >
        Cancel
      </button>
    </div>

    <!-- Progress Steps -->
    <div class="tw-flex tw-items-center tw-gap-4">
      <div
        :class="[
          'tw-flex tw-items-center tw-gap-2 tw-rounded-full tw-px-4 tw-py-2 tw-text-sm tw-font-semibold',
          currentStep >= 1 ? 'tw-bg-emerald-600 tw-text-white' : 'tw-bg-slate-200 tw-text-slate-500',
        ]"
      >
        <span>1</span>
        <span>Draw Polygon</span>
      </div>
      <div class="tw-h-1 tw-w-12 tw-rounded-full" :class="currentStep >= 2 ? 'tw-bg-emerald-600' : 'tw-bg-slate-200'"></div>
      <div
        :class="[
          'tw-flex tw-items-center tw-gap-2 tw-rounded-full tw-px-4 tw-py-2 tw-text-sm tw-font-semibold',
          currentStep >= 2 ? 'tw-bg-emerald-600 tw-text-white' : 'tw-bg-slate-200 tw-text-slate-500',
        ]"
      >
        <span>2</span>
        <span>Validate</span>
      </div>
      <div class="tw-h-1 tw-w-12 tw-rounded-full" :class="currentStep >= 3 ? 'tw-bg-emerald-600' : 'tw-bg-slate-200'"></div>
      <div
        :class="[
          'tw-flex tw-items-center tw-gap-2 tw-rounded-full tw-px-4 tw-py-2 tw-text-sm tw-font-semibold',
          currentStep >= 3 ? 'tw-bg-emerald-600 tw-text-white' : 'tw-bg-slate-200 tw-text-slate-500',
        ]"
      >
        <span>3</span>
        <span>Details</span>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="tw-grid tw-gap-6 lg:tw-grid-cols-[1.5fr_1fr]">
      <!-- Left: Map Section -->
      <div class="tw-space-y-4">
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-shadow-sm">
          <div class="tw-border-b tw-border-slate-100 tw-p-4">
            <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Draw Your Farm Boundary</h2>
            <p class="tw-mt-1 tw-text-sm tw-text-slate-600">
              Use the drawing tools to outline your production unit. Click to place points, double-click to complete.
            </p>
          </div>

          <!-- ArcGIS Map Container -->
          <div class="tw-relative">
            <div
              ref="mapContainer"
              class="tw-h-[500px] tw-w-full tw-bg-slate-100"
              :class="{ 'tw-opacity-50': mapLoading }"
            ></div>

            <!-- Map Controls Overlay -->
            <div class="tw-pointer-events-none tw-absolute tw-inset-0 tw-flex tw-flex-col tw-items-end tw-justify-start tw-gap-3 tw-p-4">
              <div class="tw-pointer-events-auto tw-flex tw-flex-col tw-gap-2">
                <button
                  type="button"
                  class="tw-inline-flex tw-items-center tw-justify-center tw-rounded-full tw-bg-emerald-600 tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-white tw-shadow-lg tw-transition hover:tw-bg-emerald-700 disabled:tw-cursor-not-allowed disabled:tw-bg-emerald-300"
                  @click="startDrawing"
                  :disabled="isDrawing || mapLoading || hasValidPolygon"
                >
                  <span v-if="!isDrawing">{{ hasValidPolygon ? 'Polygon Complete' : 'Draw Polygon' }}</span>
                  <span v-else>Drawing...</span>
                </button>
                <button
                  v-if="currentPolygon"
                  type="button"
                  class="tw-inline-flex tw-items-center tw-justify-center tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-slate-600 tw-shadow-lg tw-transition hover:tw-border-slate-300 hover:tw-text-slate-700"
                  @click="clearPolygon"
                >
                  Clear & Redraw
                </button>
              </div>
            </div>

            <!-- Map Loading Overlay -->
            <transition name="fade">
              <div
                v-if="mapLoading"
                class="tw-absolute tw-inset-0 tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-3 tw-bg-white/90"
              >
                <div class="tw-h-10 tw-w-10 tw-animate-spin tw-rounded-full tw-border-4 tw-border-emerald-200 tw-border-t-emerald-600"></div>
                <p class="tw-text-sm tw-font-medium tw-text-slate-500">Loading map...</p>
              </div>
            </transition>
          </div>

          <!-- Map Metrics Display -->
          <div v-if="polygonMetrics" class="tw-border-t tw-border-slate-100 tw-p-4">
            <div class="tw-grid tw-grid-cols-3 tw-gap-4">
              <div class="tw-rounded-lg tw-border tw-border-emerald-100 tw-bg-emerald-50 tw-p-3">
                <div class="tw-text-xs tw-font-medium tw-text-emerald-600">Area</div>
                <div class="tw-mt-1 tw-text-lg tw-font-bold tw-text-emerald-900">
                  {{ polygonMetrics.areaHectares.toFixed(2) }} ha
                </div>
              </div>
              <div class="tw-rounded-lg tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-3">
                <div class="tw-text-xs tw-font-medium tw-text-slate-500">Perimeter</div>
                <div class="tw-mt-1 tw-text-lg tw-font-bold tw-text-slate-900">
                  {{ (polygonMetrics.perimeterMeters / 1000).toFixed(2) }} km
                </div>
              </div>
              <div class="tw-rounded-lg tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-3">
                <div class="tw-text-xs tw-font-medium tw-text-slate-500">Points</div>
                <div class="tw-mt-1 tw-text-lg tw-font-bold tw-text-slate-900">
                  {{ polygonMetrics.pointCount || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Validation & Form Section -->
      <div class="tw-space-y-4">
        <!-- Validation Results -->
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
          <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Validation Status</h2>

          <div v-if="!currentPolygon" class="tw-mt-4 tw-text-center tw-text-sm tw-text-slate-500">
            Draw a polygon to see validation results
          </div>

          <div v-else-if="validating" class="tw-mt-4 tw-flex tw-items-center tw-justify-center tw-gap-3">
            <div class="tw-h-6 tw-w-6 tw-animate-spin tw-rounded-full tw-border-2 tw-border-emerald-200 tw-border-t-emerald-600"></div>
            <span class="tw-text-sm tw-text-slate-600">Validating polygon...</span>
          </div>

          <div v-else-if="validationResult" class="tw-mt-4 tw-space-y-4">
            <!-- Overall Status -->
            <div
              :class="[
                'tw-rounded-xl tw-border tw-p-4',
                validationResult.isValid
                  ? 'tw-border-emerald-200 tw-bg-emerald-50'
                  : 'tw-border-red-200 tw-bg-red-50',
              ]"
            >
              <div class="tw-flex tw-items-center tw-gap-3">
                <div
                  :class="[
                    'tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full',
                    validationResult.isValid ? 'tw-bg-emerald-100' : 'tw-bg-red-100',
                  ]"
                >
                  <span class="tw-text-xl">{{ validationResult.isValid ? '✓' : '✗' }}</span>
                </div>
                <div>
                  <div
                    :class="[
                      'tw-font-semibold',
                      validationResult.isValid ? 'tw-text-emerald-900' : 'tw-text-red-900',
                    ]"
                  >
                    {{ validationResult.isValid ? 'Polygon is Valid' : 'Polygon Has Issues' }}
                  </div>
                  <div
                    :class="[
                      'tw-text-xs',
                      validationResult.isValid ? 'tw-text-emerald-600' : 'tw-text-red-600',
                    ]"
                  >
                    {{ validationResult.isValid ? 'Ready for submission' : 'Please fix the issues below' }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Errors -->
            <div v-if="validationResult.errors && validationResult.errors.length > 0" class="tw-space-y-2">
              <div class="tw-text-sm tw-font-semibold tw-text-red-700">Errors</div>
              <div
                v-for="(error, index) in validationResult.errors"
                :key="`error-${index}`"
                class="tw-rounded-lg tw-border tw-border-red-200 tw-bg-red-50 tw-p-3 tw-text-sm tw-text-red-700"
              >
                {{ error }}
              </div>
            </div>

            <!-- Warnings -->
            <div v-if="validationResult.warnings && validationResult.warnings.length > 0" class="tw-space-y-2">
              <div class="tw-text-sm tw-font-semibold tw-text-amber-700">Warnings</div>
              <div
                v-for="(warning, index) in validationResult.warnings"
                :key="`warning-${index}`"
                class="tw-rounded-lg tw-border tw-border-amber-200 tw-bg-amber-50 tw-p-3 tw-text-sm tw-text-amber-700"
              >
                {{ warning }}
              </div>
            </div>

            <!-- Success Message -->
            <div v-if="validationResult.isValid" class="tw-text-xs tw-text-slate-500">
              Your polygon meets all EUDR compliance requirements. You can now proceed to add details.
            </div>
          </div>
        </div>

        <!-- Production Unit Details Form -->
        <div
          v-if="hasValidPolygon"
          class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm"
        >
          <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Production Unit Details</h2>

          <form class="tw-mt-4 tw-space-y-4" @submit.prevent="handleSubmit">
            <div>
              <label class="tw-mb-1 tw-block tw-text-sm tw-font-medium tw-text-slate-700">
                Unit Name *
              </label>
              <input
                v-model="formData.unitName"
                type="text"
                required
                class="tw-w-full tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-px-4 tw-py-2 tw-text-sm tw-outline-none focus:tw-border-emerald-500 focus:tw-ring-2 focus:tw-ring-emerald-100"
                placeholder="e.g., North Coffee Field"
              />
            </div>

            <div>
              <label class="tw-mb-1 tw-block tw-text-sm tw-font-medium tw-text-slate-700">
                Administrative Region
              </label>
              <input
                v-model="formData.administrativeRegion"
                type="text"
                class="tw-w-full tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-px-4 tw-py-2 tw-text-sm tw-outline-none focus:tw-border-emerald-500 focus:tw-ring-2 focus:tw-ring-emerald-100"
                placeholder="e.g., Central Region"
              />
            </div>

            <div>
              <label class="tw-mb-1 tw-block tw-text-sm tw-font-medium tw-text-slate-700">
                Description
              </label>
              <textarea
                v-model="formData.description"
                rows="3"
                class="tw-w-full tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-px-4 tw-py-2 tw-text-sm tw-outline-none focus:tw-border-emerald-500 focus:tw-ring-2 focus:tw-ring-emerald-100"
                placeholder="Additional information about this production unit..."
              ></textarea>
            </div>

            <div class="tw-flex tw-items-center tw-gap-3 tw-pt-4">
              <button
                type="submit"
                class="tw-flex-1 tw-rounded-xl tw-bg-emerald-600 tw-px-5 tw-py-3 tw-text-sm tw-font-semibold tw-text-white tw-transition hover:tw-bg-emerald-700 disabled:tw-cursor-not-allowed disabled:tw-bg-emerald-300"
                :disabled="saving || !hasValidPolygon"
              >
                <span v-if="saving">Saving...</span>
                <span v-else>{{ editMode ? 'Update Production Unit' : 'Create Production Unit' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { loadModules } from 'esri-loader';
import { productionUnitService } from '@/services/productionUnitService.js';

export default {
  name: 'ProductionUnitDrawingForm',
  props: {
    existingUnit: {
      type: Object,
      default: null,
    },
    onCancel: {
      type: Function,
      default: null,
    },
    onSuccess: {
      type: Function,
      default: null,
    },
  },
  data() {
    return {
      currentStep: 1,
      mapLoading: true,
      isDrawing: false,
      validating: false,
      saving: false,

      map: null,
      view: null,
      graphicsLayer: null,
      sketchViewModel: null,
      geometryEngine: null,

      currentPolygon: null,
      polygonMetrics: null,
      validationResult: null,

      formData: {
        unitName: '',
        administrativeRegion: '',
        description: '',
      },
    };
  },
  computed: {
    editMode() {
      return !!this.existingUnit;
    },
    hasValidPolygon() {
      return this.currentPolygon && this.validationResult?.isValid;
    },
  },
  async mounted() {
    await this.initializeMap();

    if (this.existingUnit && this.existingUnit.polygonGeoJson) {
      this.loadExistingPolygon(this.existingUnit.polygonGeoJson);
      this.formData = {
        unitName: this.existingUnit.unitName || '',
        administrativeRegion: this.existingUnit.administrativeRegion || '',
        description: this.existingUnit.description || '',
      };
    }
  },
  beforeDestroy() {
    if (this.view) {
      this.view.destroy();
    }
  },
  methods: {
    async initializeMap() {
      try {
        const [
          EsriMap,
          MapView,
          GraphicsLayer,
          SketchViewModel,
          // eslint-disable-next-line no-unused-vars
          Graphic,
          geometryEngine,
        ] = await loadModules([
          'esri/Map',
          'esri/views/MapView',
          'esri/layers/GraphicsLayer',
          'esri/widgets/Sketch/SketchViewModel',
          'esri/Graphic',
          'esri/geometry/geometryEngine',
        ], {
          url: 'https://js.arcgis.com/4.28/',
          css: 'https://js.arcgis.com/4.28/esri/themes/light/main.css',
        });

        this.geometryEngine = geometryEngine;
        this.graphicsLayer = new GraphicsLayer();

        this.map = new EsriMap({
          basemap: 'satellite',
          layers: [this.graphicsLayer],
        });

        this.view = new MapView({
          container: this.$refs.mapContainer,
          map: this.map,
          center: [36.8219, -1.2921], // Kenya coordinates
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
            color: [16, 185, 129, 0.3],
            outline: {
              color: [16, 185, 129, 1],
              width: 3,
            },
          },
        });

        this.sketchViewModel.on('create', (event) => {
          if (event.state === 'start') {
            this.isDrawing = true;
            this.currentStep = 1;
          }
          if (event.state === 'complete') {
            this.isDrawing = false;
            this.handlePolygonComplete(event.graphic);
          }
        });

        this.mapLoading = false;
      } catch (error) {
        this.$toast.error('Failed to load map. Please refresh the page.');
        this.mapLoading = false;
      }
    },

    startDrawing() {
      if (this.sketchViewModel && !this.isDrawing) {
        this.clearPolygon();
        this.sketchViewModel.create('polygon');
      }
    },

    clearPolygon() {
      this.graphicsLayer.removeAll();
      this.currentPolygon = null;
      this.polygonMetrics = null;
      this.validationResult = null;
      this.currentStep = 1;
    },

    async handlePolygonComplete(graphic) {
      const geometry = graphic.geometry;

      // Calculate metrics
      const areaSqMeters = Math.abs(this.geometryEngine.geodesicArea(geometry, 'square-meters'));
      const perimeterMeters = Math.abs(this.geometryEngine.geodesicLength(geometry, 'meters'));

      this.polygonMetrics = {
        areaHectares: areaSqMeters / 10000,
        areaSquareMeters: areaSqMeters,
        perimeterMeters,
        pointCount: geometry.rings[0].length - 1,
      };

      // Store GeoJSON
      const geoJson = {
        type: 'Polygon',
        coordinates: geometry.rings,
      };
      this.currentPolygon = JSON.stringify(geoJson);

      // Validate polygon
      await this.validatePolygon();
    },

    async validatePolygon() {
      if (!this.currentPolygon) return;

      this.validating = true;
      this.currentStep = 2;

      try {
        const response = await productionUnitService.validatePolygon(this.currentPolygon);
        this.validationResult = response;

        if (response.isValid) {
          this.currentStep = 3;
          this.$toast.success('Polygon validated successfully!');
        } else {
          this.$toast.warning('Polygon has validation issues. Please review.');
        }
      } catch (error) {
        this.validationResult = {
          isValid: false,
          errors: ['Failed to validate polygon. Please try again.'],
        };
        this.$toast.error('Validation failed');
      } finally {
        this.validating = false;
      }
    },

    async loadExistingPolygon(geoJsonString) {
      // TODO: Implement loading existing polygon on map
      try {
        // eslint-disable-next-line no-unused-vars
        const geoJson = JSON.parse(geoJsonString);
        // Render polygon on map
        this.currentPolygon = geoJsonString;
        await this.validatePolygon();
      } catch (error) {
      }
    },

    async handleSubmit() {
      if (!this.hasValidPolygon || !this.formData.unitName) {
        this.$toast.error('Please complete all required fields');
        return;
      }

      this.saving = true;

      try {
        const payload = {
          unitName: this.formData.unitName,
          administrativeRegion: this.formData.administrativeRegion,
          description: this.formData.description,
          polygonGeoJson: this.currentPolygon,
          areaHectares: this.polygonMetrics.areaHectares,
        };

        let result;
        if (this.editMode) {
          result = await productionUnitService.updateProductionUnitPolygon(
            this.existingUnit.id,
            this.currentPolygon,
          );
        } else {
          result = await productionUnitService.createProductionUnit(payload);
        }

        this.$toast.success(`Production unit ${this.editMode ? 'updated' : 'created'} successfully!`);

        if (this.onSuccess) {
          this.onSuccess(result);
        }
      } catch (error) {
        this.$toast.error('Failed to save production unit');
      } finally {
        this.saving = false;
      }
    },

    handleCancel() {
      if (this.onCancel) {
        this.onCancel();
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
