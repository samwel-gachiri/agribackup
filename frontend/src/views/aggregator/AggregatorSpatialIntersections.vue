<template>
  <div class="tw-min-h-screen tw-bg-slate-50 tw-px-4 tw-py-8 lg:tw-px-10">
    <div class="tw-mx-auto tw-flex tw-max-w-7xl tw-flex-col tw-gap-8">
      <header class="tw-flex tw-flex-col tw-gap-4 md:tw-flex-row md:tw-items-center md:tw-justify-between">
        <div class="tw-space-y-2">
          <div class="tw-inline-flex tw-items-center tw-rounded-full tw-bg-emerald-100 tw-px-3 tw-py-1 tw-text-sm tw-font-semibold tw-text-emerald-700">
            <span class="tw-mr-2">🛰️</span>
            Spatial compliance tooling
          </div>
          <h1 class="tw-text-3xl tw-font-semibold tw-text-slate-900">Polygon Intersection Analysis</h1>
          <p class="tw-max-w-3xl tw-text-sm tw-text-slate-600">
            Draw or import a prospective sourcing polygon and instantly discover which farmer production units intersect it. All results
            come directly from the live EUDR spatial services, with validation and Hedera-backed metadata included.
          </p>
        </div>
        <div class="tw-flex tw-items-center tw-gap-3">
          <button
            type="button"
            class="tw-inline-flex tw-items-center tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-medium tw-text-slate-600 tw-transition hover:tw-border-slate-300 hover:tw-text-slate-800"
            @click="resetWorkspace"
          >
            Reset workspace
          </button>
        </div>
      </header>

      <section class="tw-grid tw-gap-6 lg:tw-grid-cols-[minmax(0,1.35fr)_minmax(0,1fr)]">
        <div class="tw-space-y-4">
          <div class="tw-flex tw-items-center tw-gap-3">
            <button
              type="button"
              :class="['tw-inline-flex tw-items-center tw-rounded-full tw-border tw-px-4 tw-py-2 tw-text-sm tw-font-semibold', searchMode === 'map' ? 'tw-border-emerald-600 tw-bg-emerald-50 tw-text-emerald-700' : 'tw-border-slate-200 tw-bg-white tw-text-slate-600 hover:tw-border-slate-300 hover:tw-text-slate-800']"
              @click="setSearchMode('map')"
            >
              Map sketch
            </button>
            <button
              type="button"
              :class="['tw-inline-flex tw-items-center tw-rounded-full tw-border tw-px-4 tw-py-2 tw-text-sm tw-font-semibold', searchMode === 'manual' ? 'tw-border-emerald-600 tw-bg-emerald-50 tw-text-emerald-700' : 'tw-border-slate-200 tw-bg-white tw-text-slate-600 hover:tw-border-slate-300 hover:tw-text-slate-800']"
              @click="setSearchMode('manual')"
            >
              Paste GeoJSON
            </button>
          </div>

          <PolygonIntersectionMap
            ref="intersectionMap"
            :geoJson="mapGeoJson"
            @geometry-change="handleGeometryChange"
          />

          <p class="tw-text-xs tw-text-slate-500">
            Tip: Use the map to sketch your target polygon. If you already have GeoJSON, switch to <span class="tw-font-semibold">Paste GeoJSON</span>
            mode and preview it on the map.
          </p>
        </div>

        <div class="tw-space-y-5">
          <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Polygon input</h2>
              <span class="tw-text-xs tw-uppercase tw-text-slate-400">required</span>
            </div>

            <div class="tw-mt-4 tw-space-y-4">
              <div v-if="searchMode === 'manual'" class="tw-space-y-3">
                <label for="geojson-textarea" class="tw-text-sm tw-font-medium tw-text-slate-700">GeoJSON polygon or feature collection</label>
                <textarea
                  id="geojson-textarea"
                  v-model="geoJsonInput"
                  class="tw-h-48 tw-w-full tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-3 tw-font-mono tw-text-xs tw-text-slate-700 tw-outline-none focus:tw-border-emerald-500 focus:tw-ring-2 focus:tw-ring-emerald-100"
                  placeholder='{"type":"Polygon","coordinates":[[...]]}'
                  spellcheck="false"
                ></textarea>
                <div class="tw-flex tw-items-center tw-gap-3">
                  <button
                    type="button"
                    class="tw-inline-flex tw-items-center tw-rounded-full tw-border tw-border-emerald-600 tw-bg-emerald-600 tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-white tw-transition hover:tw-bg-emerald-700"
                    @click="previewManualGeoJson"
                  >
                    Preview on map
                  </button>
                  <button
                    type="button"
                    class="tw-inline-flex tw-items-center tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-medium tw-text-slate-600 tw-transition hover:tw-border-slate-300 hover:tw-text-slate-800"
                    @click="beautifyGeoJson"
                  >
                    Format JSON
                  </button>
                </div>
              </div>

              <div v-else class="tw-space-y-2">
                <label class="tw-text-sm tw-font-medium tw-text-slate-700">Captured polygon</label>
                <textarea
                  v-model="geoJsonInput"
                  readonly
                  class="tw-h-40 tw-w-full tw-cursor-not-allowed tw-rounded-xl tw-border tw-border-slate-100 tw-bg-slate-50 tw-p-3 tw-font-mono tw-text-xs tw-text-slate-500"
                ></textarea>
              </div>

              <div class="tw-flex tw-items-center tw-gap-3">
                <button
                  type="button"
                  class="tw-inline-flex tw-items-center tw-rounded-full tw-border tw-border-emerald-600 tw-bg-emerald-600 tw-px-5 tw-py-2 tw-text-sm tw-font-semibold tw-text-white tw-transition hover:tw-bg-emerald-700 disabled:tw-cursor-not-allowed disabled:tw-bg-emerald-300"
                  @click="runIntersection"
                  :disabled="loadingSearch || !canRunIntersection"
                >
                  <span v-if="loadingSearch" class="tw-mr-2 tw-h-4 tw-w-4 tw-animate-spin tw-rounded-full tw-border-2 tw-border-white/40 tw-border-t-white"></span>
                  Run intersection
                </button>
                <button
                  type="button"
                  class="tw-inline-flex tw-items-center tw-rounded-full tw-border tw-border-slate-200 tw-bg-white tw-px-4 tw-py-2 tw-text-xs tw-font-medium tw-text-slate-500 tw-transition hover:tw-border-slate-300 hover:tw-text-slate-700"
                  @click="fetchInsights(effectiveGeoJson)"
                  :disabled="!canRunIntersection || insightsLoading"
                >
                  {{ insightsLoading ? 'Checking...' : 'Revalidate polygon' }}
                </button>
              </div>

              <p v-if="searchError" class="tw-text-xs tw-font-semibold tw-text-red-600">{{ searchError }}</p>
            </div>
          </div>

          <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h3 class="tw-text-base tw-font-semibold tw-text-slate-900">Polygon insights</h3>
              <span v-if="insightsLoading" class="tw-h-3 tw-w-3 tw-animate-spin tw-rounded-full tw-border-2 tw-border-emerald-200 tw-border-t-emerald-600"></span>
            </div>

            <div v-if="insightsError" class="tw-mt-3 tw-rounded-lg tw-border tw-border-red-200 tw-bg-red-50 tw-p-3 tw-text-xs tw-text-red-600">
              {{ insightsError }}
            </div>

            <div v-else class="tw-mt-4 tw-space-y-4">
              <div v-if="metricsDisplay" class="tw-grid tw-grid-cols-2 tw-gap-3">
                <div class="tw-rounded-xl tw-border tw-border-emerald-100 tw-bg-emerald-50 tw-p-4">
                  <div class="tw-text-xs tw-uppercase tw-text-emerald-600">Area (ha)</div>
                  <div class="tw-mt-1 tw-text-xl tw-font-semibold tw-text-emerald-900">{{ formatNumber(metricsDisplay.areaHectares, 3) }}</div>
                </div>
                <div class="tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-4">
                  <div class="tw-text-xs tw-uppercase tw-text-slate-500">Perimeter (km)</div>
                  <div class="tw-mt-1 tw-text-xl tw-font-semibold tw-text-slate-800">{{ formatNumber(metricsDisplay.perimeterKm || metricsDisplay.perimeterMeters / 1000, 3) }}</div>
                </div>
              </div>

              <div v-if="validationResult" class="tw-rounded-xl tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-4 tw-text-sm">
                <div class="tw-flex tw-items-center tw-gap-2">
                  <span
                    :class="['tw-inline-flex tw-items-center tw-rounded-full tw-px-2.5 tw-py-1 tw-text-xs tw-font-semibold', validationResult.isValid ? 'tw-bg-emerald-100 tw-text-emerald-700' : 'tw-bg-red-100 tw-text-red-700']"
                  >
                    {{ validationResult.isValid ? 'Valid geometry' : 'Polygon needs attention' }}
                  </span>
                  <span class="tw-text-xs tw-text-slate-500">{{ validationResult.geometryType || 'Polygon' }}</span>
                </div>

                <div v-if="validationResult.errors?.length" class="tw-mt-3">
                  <p class="tw-text-xs tw-font-semibold tw-text-red-600">Errors</p>
                  <ul class="tw-mt-1 tw-list-disc tw-pl-5 tw-text-xs tw-text-red-500">
                    <li v-for="error in validationResult.errors" :key="`error-${error}`">{{ error }}</li>
                  </ul>
                </div>

                <div v-if="validationResult.warnings?.length" class="tw-mt-3">
                  <p class="tw-text-xs tw-font-semibold tw-text-amber-600">Warnings</p>
                  <ul class="tw-mt-1 tw-list-disc tw-pl-5 tw-text-xs tw-text-amber-600">
                    <li v-for="warning in validationResult.warnings" :key="`warning-${warning}`">{{ warning }}</li>
                  </ul>
                </div>

                <p v-if="!validationResult.errors?.length && !validationResult.warnings?.length" class="tw-mt-3 tw-text-xs tw-text-slate-500">
                  No validation issues detected.
                </p>
              </div>

              <p v-if="!metricsDisplay && !validationResult && !insightsLoading" class="tw-text-xs tw-text-slate-500">
                Draw or provide a polygon to view automated validation and geometry metrics.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-shadow-sm">
        <div class="tw-flex tw-flex-col tw-gap-4 tw-border-b tw-border-slate-100 tw-p-6 md:tw-flex-row md:tw-items-center md:tw-justify-between">
          <div>
            <h2 class="tw-text-xl tw-font-semibold tw-text-slate-900">Intersecting production units</h2>
            <p class="tw-text-sm tw-text-slate-500">
              Showing live results from the EUDR production unit registry. Only units whose polygons intersect the supplied geometry are listed.
            </p>
          </div>
          <div class="tw-flex tw-gap-4">
            <div class="tw-flex tw-flex-col tw-items-end">
              <span class="tw-text-xs tw-uppercase tw-text-slate-400">Matches</span>
              <span class="tw-text-2xl tw-font-semibold tw-text-slate-900">{{ results.length }}</span>
            </div>
            <div class="tw-flex tw-flex-col tw-items-end">
              <span class="tw-text-xs tw-uppercase tw-text-slate-400">Total hectares</span>
              <span class="tw-text-2xl tw-font-semibold tw-text-emerald-700">{{ formatNumber(resultSummary.totalArea, 3) }}</span>
            </div>
            <div class="tw-flex tw-flex-col tw-items-end">
              <span class="tw-text-xs tw-uppercase tw-text-slate-400">Farmers involved</span>
              <span class="tw-text-2xl tw-font-semibold tw-text-slate-900">{{ resultSummary.farmerCount }}</span>
            </div>
          </div>
        </div>

        <div v-if="loadingSearch" class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-3 tw-p-10">
          <div class="tw-h-10 tw-w-10 tw-animate-spin tw-rounded-full tw-border-4 tw-border-emerald-200 tw-border-t-emerald-600"></div>
          <p class="tw-text-sm tw-font-medium tw-text-slate-500">Checking spatial intersections...</p>
        </div>

        <div v-else-if="showEmptyState" class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-3 tw-p-10">
          <div class="tw-rounded-full tw-bg-slate-100 tw-p-4">
            <span class="tw-text-2xl">🌱</span>
          </div>
          <p class="tw-text-sm tw-font-medium tw-text-slate-600">No production units intersect the supplied polygon.</p>
          <p class="tw-text-xs tw-text-slate-400">Try expanding the area slightly or revisiting your coordinates.</p>
        </div>

        <div v-else class="tw-overflow-x-auto">
          <table class="tw-min-w-full tw-divide-y tw-divide-slate-200">
            <thead class="tw-bg-slate-50">
              <tr class="tw-text-left tw-text-xs tw-font-semibold tw-uppercase tw-text-slate-500">
                <th class="tw-px-6 tw-py-3">Production unit</th>
                <th class="tw-px-6 tw-py-3">Farmer</th>
                <th class="tw-px-6 tw-py-3">Administrative region</th>
                <th class="tw-px-6 tw-py-3">Area (ha)</th>
                <th class="tw-px-6 tw-py-3">Last verified</th>
                <th class="tw-px-6 tw-py-3">Hedera</th>
              </tr>
            </thead>
            <tbody class="tw-divide-y tw-divide-slate-100 tw-bg-white">
              <tr
                v-for="unit in results"
                :key="unit.id || unit.unitId || unit.unitName"
                class="tw-text-sm tw-text-slate-700 hover:tw-bg-emerald-50/40"
              >
                <td class="tw-px-6 tw-py-4">
                  <div class="tw-font-semibold tw-text-slate-900">{{ unit.unitName || 'Unnamed unit' }}</div>
                  <div v-if="unit.id" class="tw-text-xs tw-text-slate-400">ID: {{ unit.id }}</div>
                </td>
                <td class="tw-px-6 tw-py-4">
                  <div class="tw-font-medium">{{ farmerName(unit) }}</div>
                  <div v-if="unit.farmer?.id" class="tw-text-xs tw-text-slate-400">{{ unit.farmer.id }}</div>
                </td>
                <td class="tw-px-6 tw-py-4">
                  {{ unit.administrativeRegion || '—' }}
                </td>
                <td class="tw-px-6 tw-py-4">
                  {{ formatNumber(extractArea(unit), 3) }}
                </td>
                <td class="tw-px-6 tw-py-4">
                  {{ formatDate(unit.lastVerifiedAt) }}
                </td>
                <td class="tw-px-6 tw-py-4">
                  <span
                    v-if="unit.hederaTransactionId"
                    class="tw-inline-flex tw-items-center tw-rounded-full tw-bg-emerald-100 tw-px-3 tw-py-1 tw-text-xs tw-font-semibold tw-text-emerald-700"
                  >
                    {{ truncate(unit.hederaTransactionId) }}
                  </span>
                  <span v-else class="tw-text-xs tw-text-slate-400">Not recorded</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import PolygonIntersectionMap from '@/components/eudr/PolygonIntersectionMap.vue';
import { spatialAnalysisService } from '@/services/spatialAnalysisService.js';

export default {
  name: 'AggregatorSpatialIntersections',
  components: {
    PolygonIntersectionMap,
  },
  data() {
    return {
      searchMode: 'map',
      mapGeoJson: '',
      geoJsonInput: '',
      drawnGeometry: null,
      validationResult: null,
      metricsResult: null,
      loadingSearch: false,
      searchError: '',
      insightsLoading: false,
      insightsError: '',
      lastInsightsGeoJson: '',
      results: [],
      resultSummary: {
        totalArea: 0,
        farmerCount: 0,
      },
      searchHasBeenRun: false,
    };
  },
  computed: {
    effectiveGeoJson() {
      if (this.searchMode === 'manual') {
        return this.geoJsonInput?.trim();
      }
      return this.mapGeoJson;
    },
    canRunIntersection() {
      return Boolean(this.effectiveGeoJson);
    },
    metricsDisplay() {
      if (this.metricsResult) {
        return this.metricsResult;
      }
      if (this.drawnGeometry) {
        return {
          areaHectares: this.drawnGeometry.areaHectares,
          perimeterMeters: this.drawnGeometry.perimeterMeters,
          perimeterKm: this.drawnGeometry.perimeterMeters / 1000,
        };
      }
      return null;
    },
    showEmptyState() {
      return this.searchHasBeenRun && !this.loadingSearch && this.results.length === 0;
    },
  },
  methods: {
    setSearchMode(mode) {
      this.searchMode = mode;
      if (mode === 'map' && this.mapGeoJson) {
        this.geoJsonInput = this.stringifyGeoJson(this.mapGeoJson);
      }
    },
    resetWorkspace() {
      this.mapGeoJson = '';
      this.geoJsonInput = '';
      this.drawnGeometry = null;
      this.validationResult = null;
      this.metricsResult = null;
      this.results = [];
      this.resultSummary = { totalArea: 0, farmerCount: 0 };
      this.searchError = '';
      this.searchHasBeenRun = false;
      this.insightsError = '';
      this.lastInsightsGeoJson = '';
      if (this.$refs.intersectionMap?.clearGeometry) {
        this.$refs.intersectionMap.clearGeometry();
      }
      this.$toast.info('Workspace cleared');
    },
    stringifyGeoJson(raw) {
      try {
        if (!raw) return '';
        const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
        return JSON.stringify(parsed, null, 2);
      } catch (error) {
        return raw;
      }
    },
    async handleGeometryChange(payload) {
      if (!payload) {
        this.mapGeoJson = '';
        this.drawnGeometry = null;
        this.searchMode = this.searchMode === 'manual' ? 'manual' : 'map';
        if (this.searchMode !== 'manual') {
          this.validationResult = null;
          this.metricsResult = null;
          this.geoJsonInput = '';
        }
        return;
      }

      this.searchMode = 'map';
      this.drawnGeometry = payload;
      this.mapGeoJson = payload.geoJson;
      this.geoJsonInput = this.stringifyGeoJson(payload.geoJson);
      this.lastInsightsGeoJson = '';
      await this.fetchInsights(payload.geoJson);
    },
    async previewManualGeoJson() {
      try {
        const normalisedString = this.parseGeoJsonInput(this.geoJsonInput);
        this.mapGeoJson = normalisedString;
        this.searchMode = 'map';
        await this.$nextTick();
        if (this.$refs.intersectionMap?.renderGeoJson) {
          await this.$refs.intersectionMap.renderGeoJson(normalisedString);
        }
        this.lastInsightsGeoJson = '';
        await this.fetchInsights(normalisedString);
        this.$toast.success('GeoJSON previewed on map');
      } catch (error) {
        const message = error.message || 'Unable to preview GeoJSON.';
        this.$toast.error(message);
      }
    },
    beautifyGeoJson() {
      this.geoJsonInput = this.stringifyGeoJson(this.geoJsonInput);
    },
    async fetchInsights(geoJsonString) {
      const trimmed = geoJsonString?.trim();
      if (!trimmed) {
        this.validationResult = null;
        this.metricsResult = null;
        return;
      }
      if (trimmed === this.lastInsightsGeoJson) {
        return;
      }
      this.insightsLoading = true;
      this.insightsError = '';
      try {
        const [validationPayload, metricsPayload] = await Promise.all([
          spatialAnalysisService.validatePolygon(trimmed),
          spatialAnalysisService.calculatePolygonMetrics(trimmed),
        ]);
        this.validationResult = this.unwrapApiResponse(validationPayload) || null;
        const metricsRaw = this.unwrapApiResponse(metricsPayload) || null;
        this.metricsResult = metricsRaw
          ? {
            ...metricsRaw,
            areaHectares: this.toNumber(metricsRaw.areaHectares, 0),
            perimeterKm: this.toNumber(metricsRaw.perimeterKm, null),
            centroidLat: this.toNumber(metricsRaw.centroidLat, null),
            centroidLon: this.toNumber(metricsRaw.centroidLon, null),
          }
          : null;
        this.lastInsightsGeoJson = trimmed;
      } catch (error) {
        this.insightsError = error.response?.data?.message || error.message || 'Failed to validate polygon.';
        this.validationResult = null;
        this.metricsResult = null;
      } finally {
        this.insightsLoading = false;
      }
    },
    unwrapApiResponse(payload) {
      if (!payload || typeof payload !== 'object') return payload;
      if (Object.prototype.hasOwnProperty.call(payload, 'data')) {
        return payload.data;
      }
      return payload;
    },
    async runIntersection() {
      this.searchError = '';
      let normalisedGeoJson;
      try {
        normalisedGeoJson = this.parseGeoJsonInput(this.effectiveGeoJson);
      } catch (error) {
        const message = error.message || 'Invalid GeoJSON input.';
        this.searchError = message;
        this.$toast.error(message);
        return;
      }

      this.loadingSearch = true;
      this.searchHasBeenRun = true;
      try {
        await this.fetchInsights(normalisedGeoJson);
        const response = await spatialAnalysisService.findIntersectingProductionUnits(normalisedGeoJson);
        const intersections = this.unwrapApiResponse(response) || [];
        this.results = Array.isArray(intersections) ? intersections : [];
        this.updateSummary();
        if (this.results.length === 0) {
          this.$toast.info('No intersecting production units found.');
        } else {
          this.$toast.success(`Found ${this.results.length} intersecting production unit${this.results.length > 1 ? 's' : ''}.`);
        }
      } catch (error) {
        this.searchError = error.response?.data?.message || error.message || 'Failed to run spatial intersection.';
        this.$toast.error(this.searchError);
      } finally {
        this.loadingSearch = false;
      }
    },
    parseGeoJsonInput(raw) {
      const trimmed = raw?.trim();
      if (!trimmed) {
        throw new Error('Provide a GeoJSON polygon before running the analysis.');
      }
      let parsed;
      try {
        parsed = JSON.parse(trimmed);
      } catch (error) {
        throw new Error('GeoJSON must be valid JSON.');
      }

      const normalised = this.normaliseGeoJsonGeometry(parsed);
      return JSON.stringify(normalised);
    },
    normaliseGeoJsonGeometry(input) {
      if (!input) {
        throw new Error('GeoJSON payload is empty.');
      }
      if (input.type === 'FeatureCollection') {
        const feature = input.features?.[0];
        if (!feature) {
          throw new Error('GeoJSON FeatureCollection must include at least one feature.');
        }
        return this.normaliseGeoJsonGeometry(feature.geometry);
      }
      if (input.type === 'Feature') {
        return this.normaliseGeoJsonGeometry(input.geometry);
      }
      if (input.type === 'Polygon') {
        if (!Array.isArray(input.coordinates) || !input.coordinates.length) {
          throw new Error('Polygon coordinates are missing.');
        }
        return {
          type: 'Polygon',
          coordinates: input.coordinates,
        };
      }
      if (input.type === 'MultiPolygon') {
        if (!Array.isArray(input.coordinates) || !input.coordinates.length) {
          throw new Error('MultiPolygon coordinates are missing.');
        }
        return {
          type: 'MultiPolygon',
          coordinates: input.coordinates,
        };
      }
      throw new Error('GeoJSON must be a Polygon, MultiPolygon, Feature, or FeatureCollection.');
    },
    updateSummary() {
      const farmerIds = new Set();
      const totalArea = this.results.reduce((acc, unit) => {
        const farmerId = unit.farmer?.id || unit.farmerId;
        if (farmerId) {
          farmerIds.add(farmerId);
        }
        const area = Number(this.extractArea(unit)) || 0;
        return acc + area;
      }, 0);
      this.resultSummary = {
        totalArea: Number(totalArea.toFixed(4)),
        farmerCount: farmerIds.size,
      };
    },
    extractArea(unit) {
      if (!unit) return 0;
      const keys = ['areaHectares', 'area_hectares', 'area'];
      for (const key of keys) {
        if (unit[key] != null) {
          return Number(unit[key]);
        }
      }
      return 0;
    },
    toNumber(value, fallback = 0) {
      if (value === null || value === undefined || value === '') {
        return fallback;
      }
      const numeric = Number(value);
      return Number.isNaN(numeric) ? fallback : numeric;
    },
    formatNumber(value, decimals = 2) {
      const numeric = Number(value);
      if (Number.isNaN(numeric)) return '0.00';
      return numeric.toLocaleString(undefined, {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals,
      });
    },
    formatDate(value) {
      if (!value) return '—';
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return '—';
      return new Intl.DateTimeFormat(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      }).format(date);
    },
    farmerName(unit) {
      if (!unit || !unit.farmer) return 'Unknown farmer';
      const farmer = unit.farmer;
      const fullName = farmer.firstName && farmer.lastName ? `${farmer.firstName} ${farmer.lastName}` : null;
      return (
        farmer.name
        || farmer.fullName
        || farmer.displayName
        || farmer.businessName
        || farmer.companyName
        || fullName
        || farmer.userProfile?.fullName
        || farmer.userProfile?.username
        || farmer.userProfile?.email
        || 'Farmer'
      );
    },
    truncate(value, length = 12) {
      if (!value) return '';
      if (value.length <= length) return value;
      const segment = Math.floor((length - 3) / 2);
      return `${value.slice(0, segment)}...${value.slice(-segment)}`;
    },
  },
};
</script>
