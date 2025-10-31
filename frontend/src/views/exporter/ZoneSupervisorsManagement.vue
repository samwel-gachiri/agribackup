<template>
  <Default>
    <div class="tw-p-6 tw-space-y-8 tw-bg-gradient-to-br tw-from-slate-50 tw-via-white tw-to-slate-100 tw-min-h-screen">
      <!-- Header & Actions -->
      <div class="tw-flex tw-items-start tw-justify-between tw-flex-wrap tw-gap-6">
        <div class="tw-space-y-2">
<!--          <div class="tw-flex tw-items-center tw-gap-3">-->
<!--            <h1 class="tw-text-3xl tw-font-black tw-tracking-tight tw-bg-clip-text tw-text-transparent tw-bg-gradient-to-r tw-from-indigo-700 tw-to-teal-500">Zone Supervisors</h1>-->
<!--            <v-chip small color="indigo darken-2" text-color="white" class="tw-font-semibold tw-uppercase">Managed</v-chip>-->
<!--          </div>-->
<!--          <p class="tw-text-sm tw-text-slate-600 tw-max-w-xl">Coordinate operational coverage: supervise zones, balance workload, and monitor farmer distribution.</p>-->
          <div class="tw-flex tw-gap-2 tw-flex-wrap">
            <v-btn color="primary" small :loading="loading" class="tw-rounded-xl tw-shadow-sm tw-font-medium" @click="refreshAll">
              <v-icon left small>mdi-refresh</v-icon>Refresh
            </v-btn>
            <v-btn color="success" small class="tw-rounded-xl tw-shadow-sm tw-font-medium" @click="openCreateDialog">
              <v-icon left small>mdi-account-plus</v-icon>New Supervisor
            </v-btn>
            <v-btn small outlined class="tw-rounded-xl tw-font-medium" @click="toggleView">
              <v-icon left small>{{ viewMode==='list' ? 'mdi-view-grid' : 'mdi-format-list-bulleted' }}</v-icon>{{ viewMode==='list' ? 'Grid View' : 'List View' }}
            </v-btn>
            <v-btn small outlined class="tw-rounded-xl tw-font-medium" @click="clearFilters">
              <v-icon left small>mdi-filter-remove</v-icon>Reset
            </v-btn>
          </div>
        </div>
        <!-- Metrics -->
        <div class="tw-grid tw-grid-cols-2 md:tw-grid-cols-5 tw-gap-3 tw-w-full md:tw-w-auto">
          <div v-for="m in metrics" :key="m.key" class="tw-bg-white tw-rounded-2xl tw-border tw-border-slate-200 tw-px-4 tw-py-3 tw-flex tw-flex-col tw-gap-1 tw-shadow-sm hover:tw-shadow transition">
            <div class="tw-text-[11px] tw-font-semibold tw-tracking-wide tw-text-slate-500 tw-uppercase">{{ m.label }}</div>
            <div class="tw-flex tw-items-end tw-gap-2">
              <div class="tw-text-xl tw-font-bold tw-text-slate-800">{{ m.value }}</div>
            </div>
            <div v-if="m.sub" class="tw-text-[10px] tw-text-slate-400 tw-font-medium">{{ m.sub }}</div>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <div class="tw-bg-white/80 tw-backdrop-blur-sm tw-shadow-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-p-6 tw-space-y-6">
        <div class="tw-grid md:tw-grid-cols-4 tw-gap-5">
          <div class="md:tw-col-span-2">
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Search</label>
            <input v-model.trim="search" type="text" placeholder="Search by name, email, phone" class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-indigo-500 focus:tw-bg-white tw-transition" />
          </div>
          <div>
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Min Zones</label>
            <input v-model.number="minZones" type="number" min="0" class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-indigo-500" />
          </div>
          <div>
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Min Farmers</label>
            <input v-model.number="minFarmers" type="number" min="0" class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-indigo-500" />
          </div>
        </div>
        <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
          <v-chip v-if="search" close small color="indigo" outlined @click:close="search=''">Search: {{ search }}</v-chip>
          <v-chip v-if="minZones" close small color="teal" outlined @click:close="minZones=0">Min Zones: {{ minZones }}</v-chip>
          <v-chip v-if="minFarmers" close small color="purple" outlined @click:close="minFarmers=0">Min Farmers: {{ minFarmers }}</v-chip>
          <div v-if="!search && !minZones && !minFarmers" class="tw-text-xs tw-text-gray-500">No filters applied</div>
        </div>
      </div>

      <div class="tw-grid lg:tw-grid-cols-3 tw-gap-8">
        <!-- List / Cards -->
        <div class="lg:tw-col-span-2 tw-space-y-4">
          <div class="tw-bg-white/90 tw-backdrop-blur-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-shadow-sm tw-overflow-hidden" style="min-height:420px;display:flex;flex-direction:column;">
            <div class="tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-4 tw-bg-gradient-to-r tw-from-indigo-600 tw-to-teal-600 tw-text-white">
              <div class="tw-font-semibold tw-text-sm md:tw-text-base tw-flex tw-items-center tw-gap-2">
                <v-icon small>mdi-account-tie</v-icon>
                Supervisors <span class="tw-text-white/80 tw-text-xs">({{ filteredSupervisors.length }})</span>
              </div>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-btn small text class="tw-text-white tw-font-medium tw-rounded-lg" :disabled="!filteredSupervisors.length" @click="exportCsv"><v-icon left small>mdi-download</v-icon>Export CSV</v-btn>
              </div>
            </div>
            <div v-if="loading" class="tw-px-6 tw-pt-6 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-4 tw-w-full" style="flex:1;">
              <div v-for="n in 6" :key="n" class="tw-animate-pulse tw-rounded-xl tw-bg-slate-100 tw-h-32 tw-border tw-border-slate-200"></div>
            </div>
            <div v-else style="flex:1;display:flex;flex-direction:column;">
              <div v-if="!filteredSupervisors.length" class="tw-p-10 tw-text-center tw-text-slate-500 tw-text-sm">No supervisors match the current filters.</div>
              <!-- Cards -->
              <div v-if="viewMode==='cards'" class="tw-px-6 tw-py-5 tw-grid sm:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-5" style="flex:1;overflow:auto;">
                <div v-for="s in paginatedSupervisors" :key="s.id" class="tw-group tw-relative tw-rounded-xl tw-border tw-border-slate-200 tw-bg-gradient-to-br tw-from-white tw-to-slate-50 hover:tw-from-white hover:tw-to-white tw-shadow-sm hover:tw-shadow transition tw-p-4 tw-flex tw-flex-col tw-gap-3">
                  <div class="tw-flex tw-justify-between tw-items-start">
                    <div class="tw-flex tw-flex-col">
                      <div class="tw-font-semibold tw-text-slate-800 tw-text-sm tw-flex tw-items-center tw-gap-2">
                        <v-icon x-small color="indigo">mdi-account</v-icon>{{ s.fullName }}
                      </div>
                      <div class="tw-text-[11px] tw-text-slate-500 tw-truncate">{{ s.email || '—' }}</div>
                    </div>
                    <v-chip x-small :color="s.assignedZonesCount ? 'teal' : 'grey'" text-color="white" class="tw-font-semibold tw-uppercase tw-tracking-wide">{{ s.assignedZonesCount }} Z</v-chip>
                  </div>
                  <div class="tw-text-[11px] tw-text-slate-500 tw-flex tw-flex-wrap tw-gap-x-3 tw-gap-y-1">
                    <div class="tw-flex tw-items-center tw-gap-1"><v-icon x-small color="purple">mdi-account-group</v-icon>{{ s.farmerCount }} farmers</div>
                    <div class="tw-flex tw-items-center tw-gap-1"><v-icon x-small color="amber">mdi-calendar-clock</v-icon>{{ s.earliestPredictedHarvestDate || '—' }}</div>
                  </div>
                  <div class="tw-flex tw-items-center tw-justify-end tw-gap-2 tw-mt-auto">
                    <v-btn icon x-small color="primary" @click="selectSupervisor(s)"><v-icon x-small>mdi-eye</v-icon></v-btn>
                    <v-btn icon x-small color="indigo" @click="openAssignZonesDialog(s)"><v-icon x-small>mdi-vector-polygon</v-icon></v-btn>
                  </div>
                  <div class="tw-absolute -tw-top-2 -tw-left-2 tw-bg-white tw-rounded-full tw-shadow tw-border tw-border-slate-200 tw-px-2 tw-py-0.5 tw-text-[10px] tw-font-semibold tw-text-slate-500">ID {{ s.id.substring(0,4) }}</div>
                </div>
              </div>
              <!-- List -->
              <ul v-else class="tw-divide-y tw-bg-slate-50/60" style="flex:1;overflow:auto;">
                <li v-for="s in paginatedSupervisors" :key="s.id" class="tw-p-4 tw-grid md:tw-grid-cols-12 tw-gap-4 hover:tw-bg-white tw-transition">
                  <div class="md:tw-col-span-4">
                    <div class="tw-font-semibold tw-text-gray-800 tw-flex tw-items-center tw-gap-2"><v-icon small color="indigo">mdi-account</v-icon>{{ s.fullName }}</div>
                    <div class="tw-text-xs tw-text-gray-500 tw-truncate">{{ s.email || '—' }}</div>
                  </div>
                  <div class="md:tw-col-span-2 tw-text-sm">
                    <div class="tw-text-gray-700">{{ s.assignedZonesCount }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Zones</div>
                  </div>
                  <div class="md:tw-col-span-2 tw-text-sm">
                    <div class="tw-text-gray-700">{{ s.farmerCount }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Farmers</div>
                  </div>
                  <div class="md:tw-col-span-3 tw-text-sm">
                    <div class="tw-text-gray-700">{{ s.earliestPredictedHarvestDate || '—' }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Earliest Harvest</div>
                  </div>
                  <div class="md:tw-col-span-1 tw-flex tw-items-center tw-gap-2 tw-justify-end">
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="primary" @click="selectSupervisor(s)">
                          <v-icon small>mdi-eye</v-icon>
                        </v-btn>
                      </template>
                      <span>View Details</span>
                    </v-tooltip>
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="indigo" @click="openAssignZonesDialog(s)">
                          <v-icon small>mdi-vector-polygon</v-icon>
                        </v-btn>
                      </template>
                      <span>Assign Zones</span>
                    </v-tooltip>
                  </div>
                </li>
              </ul>
              <div v-if="pages > 1" class="tw-flex tw-items-center tw-justify-between tw-p-4 tw-bg-white tw-border-t tw-border-gray-200">
                <div class="tw-text-xs tw-text-gray-500">Page {{ page }} / {{ pages }}</div>
                <div class="tw-flex tw-gap-2">
                  <v-btn small outlined :disabled="page===1" @click="page--">Prev</v-btn>
                  <v-btn small outlined :disabled="page===pages" @click="page++">Next</v-btn>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- Map / Details -->
        <div class="tw-space-y-4">
          <div class="tw-bg-white/90 tw-backdrop-blur-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-shadow-sm tw-overflow-hidden">
            <div class="tw-bg-gradient-to-r tw-from-teal-600 tw-to-indigo-600 tw-text-white tw-px-5 tw-py-3 tw-font-semibold tw-flex tw-items-center tw-justify-between">
              <span class="tw-flex tw-items-center tw-gap-2"><v-icon small>mdi-map</v-icon> Coverage Map</span>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-btn icon small text class="tw-text-white" @click="fitAllZones" :disabled="!mapView || !zonesGraphics.length"><v-icon small>mdi-crosshairs-gps</v-icon></v-btn>
              </div>
            </div>
            <div class="tw-relative" style="height:400px;">
              <div v-if="!mapView" class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center tw-text-gray-400 tw-text-sm">Map initializing...</div>
              <div :id="mapId" class="tw-w-full tw-h-full tw-bg-slate-200 tw-rounded-b-2xl"></div>
              <div v-if="mapView" class="tw-absolute tw-bottom-3 tw-right-3 tw-bg-white/90 tw-backdrop-blur tw-rounded-xl tw-shadow tw-border tw-border-slate-200 tw-px-3 tw-py-2 tw-flex tw-flex-col tw-gap-1 tw-text-[10px] tw-font-medium tw-text-slate-600">
                <div class="tw-flex tw-items-center tw-gap-1"><span class="tw-inline-block tw-w-3 tw-h-3 tw-rounded-full tw-bg-indigo-500"></span> Zone Center</div>
                <div class="tw-flex tw-items-center tw-gap-1"><span class="tw-inline-block tw-w-3 tw-h-3 tw-rounded-full tw-bg-teal-500"></span> Zone Radius</div>
              </div>
            </div>
          </div>
          <div v-if="selectedSupervisor" class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-shadow-sm tw-p-5 tw-space-y-3">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h3 class="tw-font-semibold tw-text-gray-800 tw-text-lg">Supervisor Details</h3>
              <v-btn icon small @click="selectedSupervisor=null"><v-icon small>mdi-close</v-icon></v-btn>
            </div>
            <div class="tw-text-sm tw-text-gray-700 tw-font-medium">{{ selectedSupervisor.fullName }}</div>
            <div class="tw-text-xs tw-text-gray-500">{{ selectedSupervisor.email || 'No email' }} | {{ selectedSupervisor.phoneNumber || 'No phone' }}</div>
            <div class="tw-text-xs tw-text-gray-500">Zones: {{ selectedSupervisor.assignedZonesCount }} | Farmers: {{ selectedSupervisor.farmerCount }}</div>
            <div class="tw-text-xs tw-text-gray-500">Earliest Harvest: {{ selectedSupervisor.earliestPredictedHarvestDate || '—' }}</div>
            <div class="tw-flex tw-gap-2 tw-mt-2">
              <v-btn small outlined color="indigo" @click="openAssignZonesDialog(selectedSupervisor)"><v-icon left small>mdi-vector-polygon</v-icon>Assign Zones</v-btn>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Supervisor Dialog (simplified minimal form) -->
      <v-dialog v-model="showCreateDialog" max-width="520">
        <v-card>
          <v-card-title class="tw-font-semibold tw-text-slate-800">Create Zone Supervisor</v-card-title>
            <v-card-text class="tw-space-y-4">
              <v-text-field v-model="createForm.fullName" label="Full Name" dense outlined hide-details></v-text-field>
              <v-text-field v-model="createForm.email" label="Email" dense outlined hide-details></v-text-field>
              <v-text-field v-model="createForm.phoneNumber" label="Phone" dense outlined hide-details></v-text-field>
            </v-card-text>
            <v-card-actions class="tw-justify-end tw-gap-2">
              <v-btn text small @click="showCreateDialog=false">Cancel</v-btn>
              <v-btn color="primary" small :loading="creating" @click="submitCreate">Create</v-btn>
            </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Assign Zones Dialog -->
      <v-dialog v-model="showAssignZonesDialog" max-width="640">
        <v-card>
          <v-card-title class="tw-font-semibold tw-text-slate-800">Assign Zones</v-card-title>
          <v-card-text>
            <div v-if="loadingZones" class="tw-text-xs tw-text-slate-500 tw-py-4">Loading zones...</div>
            <div v-else class="tw-space-y-2">
              <div v-for="z in zones" :key="z.id" class="tw-flex tw-items-center tw-justify-between tw-py-2 tw-border-b tw-border-slate-100 last:tw-border-none">
                <div class="tw-flex tw-flex-col">
                  <span class="tw-text-sm tw-font-medium tw-text-slate-800">{{ z.name }}</span>
                  <span class="tw-text-[11px] tw-text-slate-500">{{ z.produceType || 'No produce' }} • {{ z.farmerCount }} farmers</span>
                </div>
                <div class="tw-flex tw-gap-2">
                  <v-btn x-small :color="isZoneAssigned(z.id) ? 'red' : 'teal'" text @click="toggleZoneAssignment(z.id)">
                    <v-icon x-small left>{{ isZoneAssigned(z.id) ? 'mdi-link-off' : 'mdi-link' }}</v-icon>
                    {{ isZoneAssigned(z.id) ? 'Unassign' : 'Assign' }}
                  </v-btn>
                </div>
              </div>
            </div>
          </v-card-text>
          <v-card-actions class="tw-justify-end tw-gap-2">
            <v-btn text small @click="showAssignZonesDialog=false">Close</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Snackbar -->
      <v-snackbar v-model="snackbar.show" :timeout="4500" top right :color="snackbarColor(snackbar.color)" elevation="6">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-icon small v-if="snackbar.color==='success'">mdi-check-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='error'">mdi-alert-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='warning'">mdi-alert</v-icon>
          <v-icon small v-else>mdi-information</v-icon>
          <span class="tw-font-medium tw-text-sm">{{ snackbar.message }}</span>
          <v-spacer></v-spacer>
          <v-btn icon small text @click="snackbar.show=false"><v-icon small>mdi-close</v-icon></v-btn>
        </div>
      </v-snackbar>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'ZoneSupervisorsManagement',
  components: { Default },
  data() {
    return {
      supervisors: [],
      zones: [],
      loading: false,
      loadingZones: false,
      search: '',
      minZones: 0,
      minFarmers: 0,
      page: 1,
      perPage: 15,
      viewMode: 'list',
      selectedSupervisor: null,
      mapId: `zs-map-${Math.random().toString(36).substr(2, 8)}`,
      mapView: null,
      EsriMap: null,
      EsriMapView: null,
      EsriGraphic: null,
      EsriPoint: null,
      EsriCircle: null,
      zonesGraphics: [],
      showCreateDialog: false,
      createForm: { fullName: '', email: '', phoneNumber: '' },
      creating: false,
      showAssignZonesDialog: false,
      assignTarget: null,
      snackbar: { show: false, message: '', color: 'info' },
    };
  },
  computed: {
    filteredSupervisors() {
      let list = this.supervisors;
      if (this.search) {
        const q = this.search.toLowerCase();
        list = list.filter((s) => [s.fullName, s.email, s.phoneNumber].filter(Boolean).some((v) => v.toLowerCase().includes(q)));
      }
      if (this.minZones) list = list.filter((s) => s.assignedZonesCount >= this.minZones);
      if (this.minFarmers) list = list.filter((s) => s.farmerCount >= this.minFarmers);
      return list;
    },
    paginatedSupervisors() {
      const start = (this.page - 1) * this.perPage;
      return this.filteredSupervisors.slice(start, start + this.perPage);
    },
    pages() { return Math.ceil(this.filteredSupervisors.length / this.perPage) || 1; },
    metrics() {
      const total = this.supervisors.length;
      const totalZones = this.supervisors.reduce((sum, s) => sum + s.assignedZonesCount, 0);
      const avgZones = total ? (totalZones / total).toFixed(1) : 0;
      const totalFarmers = this.supervisors.reduce((sum, s) => sum + s.farmerCount, 0);
      const avgFarmers = total ? (totalFarmers / total).toFixed(1) : 0;
      const uncoveredZones = this.zones.filter((z) => !z.supervisorIds || !z.supervisorIds.length).length;
      return [
        { key: 'total', label: 'Supervisors', value: total },
        {
          key: 'zones', label: 'Avg Zones', value: avgZones, sub: `${totalZones} total`,
        },
        {
          key: 'farmers', label: 'Avg Farmers', value: avgFarmers, sub: `${totalFarmers} total`,
        },
        { key: 'uncovered', label: 'Uncovered Zones', value: uncoveredZones },
        { key: 'coverage', label: 'Coverage %', value: this.zones.length ? `${Math.round(((this.zones.length - uncoveredZones) / this.zones.length) * 100)}%` : '0%' },
      ];
    },
  },
  watch: { filteredSupervisors() { if (this.page > this.pages) this.page = 1; } },
  async mounted() {
    await this.loadArcGIS();
    this.initMap();
    this.refreshAll();
  },
  methods: {
    async refreshAll() {
      this.loading = true;
      try {
        const [overviewResp, mapResp] = await Promise.all([
          axios.get('/api/admin-service/zone-supervisors-overview'),
          // eslint-disable-next-line sonarjs/no-duplicate-string
          axios.get('/api/admin-service/zone-supervisors-map-data'),
        ]);
        this.supervisors = (overviewResp.data.data || []);
        const mapData = mapResp.data.data || {};
        this.zones = mapData.zones || [];
        this.renderZonesOnMap();
      } catch (e) { this.showSnackbar('Failed to load supervisors', 'error'); } finally { this.loading = false; }
    },
    toggleView() { this.viewMode = this.viewMode === 'list' ? 'cards' : 'list'; },
    clearFilters() { this.search = ''; this.minZones = 0; this.minFarmers = 0; },
    selectSupervisor(s) { this.selectedSupervisor = s; },
    openCreateDialog() { this.showCreateDialog = true; },
    async submitCreate() {
      if (!this.createForm.fullName || !this.createForm.email) { this.showSnackbar('Name & email required', 'warning'); return; }
      this.creating = true;
      try {
        await axios.post('/api/admin-service/zone-supervisors', this.createForm);
        this.showSnackbar('Supervisor created', 'success');
        this.showCreateDialog = false; this.createForm = { fullName: '', email: '', phoneNumber: '' }; this.refreshAll();
      } catch (e) { this.showSnackbar('Create failed', 'error'); } finally { this.creating = false; }
    },
    openAssignZonesDialog(s) { this.assignTarget = s; this.showAssignZonesDialog = true; this.fetchZones(); },
    async fetchZones() {
      if (this.loadingZones) return; this.loadingZones = true;
      try {
        if (!this.zones.length) {
          const mapResp = await axios.get('/api/admin-service/zone-supervisors-map-data');
          this.zones = (mapResp.data.data?.zones) || [];
        }
      } catch (e) { this.showSnackbar('Failed to load zones', 'error'); } finally { this.loadingZones = false; }
    },
    isZoneAssigned(zoneId) { return this.assignTarget && (this.assignTarget.assignedZoneIds || []).includes(zoneId); },
    async toggleZoneAssignment(zoneId) {
      if (!this.assignTarget) return;
      try {
        if (this.isZoneAssigned(zoneId)) {
          await axios.delete(`/api/admin-service/zones/${zoneId}/supervisors/${this.assignTarget.id}`);
          this.assignTarget.assignedZoneIds = this.assignTarget.assignedZoneIds.filter((z) => z !== zoneId);
          this.assignTarget.assignedZonesCount -= 1;
          this.showSnackbar('Unassigned', 'success');
        } else {
          await axios.post(`/api/admin-service/zones/${zoneId}/supervisors`, { zoneSupervisorId: this.assignTarget.id });
          this.assignTarget.assignedZoneIds.push(zoneId);
          this.assignTarget.assignedZonesCount += 1;
          this.showSnackbar('Assigned', 'success');
        }
        // Refresh zones for coverage metrics
        const mapResp = await axios.get('/api/admin-service/zone-supervisors-map-data');
        this.zones = (mapResp.data.data?.zones) || [];
        this.renderZonesOnMap();
      } catch (e) { this.showSnackbar('Assignment failed', 'error'); }
    },
    exportCsv() {
      const rows = [['ID', 'Name', 'Email', 'Phone', 'Zones', 'Farmers', 'EarliestHarvest']];
      this.filteredSupervisors.forEach((s) => rows.push([
        s.id, s.fullName, s.email || '', s.phoneNumber || '', s.assignedZonesCount, s.farmerCount, s.earliestPredictedHarvestDate || '',
      ]));
      const csv = rows.map((r) => r.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = 'zone-supervisors.csv'; a.click(); URL.revokeObjectURL(url);
      this.showSnackbar('Export complete', 'success');
    },
    showSnackbar(message, color = 'info') { this.snackbar = { show: true, message, color }; },
    snackbarColor(kind) {
      switch (kind) {
        case 'success': return 'green lighten-1';
        case 'error': return 'red lighten-1';
        case 'warning': return 'amber darken-2';
        case 'info': return 'blue lighten-1';
        default: return 'primary';
      }
    },
    async loadArcGIS() {
      if (!window.require) {
        await new Promise((resolve, reject) => {
          const s = document.createElement('script'); s.src = 'https://js.arcgis.com/4.25/init.js'; s.onload = resolve; s.onerror = reject; document.head.appendChild(s);
          const l = document.createElement('link'); l.rel = 'stylesheet'; l.href = 'https://js.arcgis.com/4.25/esri/themes/light/main.css'; document.head.appendChild(l);
        });
      }
      if (!this.EsriMap) {
        await new Promise((resolve, reject) => {
          try { window.require(['esri/Map', 'esri/views/MapView', 'esri/Graphic', 'esri/geometry/Point', 'esri/geometry/Circle'], (Map, MapView, Graphic, Point, Circle) => { this.EsriMap = Map; this.EsriMapView = MapView; this.EsriGraphic = Graphic; this.EsriPoint = Point; this.EsriCircle = Circle; resolve(); }); } catch (e) { reject(e); }
        });
      }
    },
    initMap() {
      if (this.mapView || !this.EsriMapView) return;
      const el = document.getElementById(this.mapId); if (!el) return;
      this.mapView = new this.EsriMapView({
        container: el,
        map: new this.EsriMap({ basemap: 'streets-vector' }),
        center: [36.8219, -1.2921],
        zoom: 7,
      });
    },
    renderZonesOnMap() {
      if (!this.mapView || !this.EsriGraphic || !this.EsriPoint) return;
      this.mapView.graphics.removeAll();
      this.zonesGraphics = [];
      this.zones.forEach((z) => {
        const center = new this.EsriPoint({ longitude: z.centerLongitude, latitude: z.centerLatitude });
        const centerGraphic = new this.EsriGraphic({
          geometry: center,
          symbol: { type: 'simple-marker', color: 'indigo', size: 6 },
          attributes: z,
          popupTemplate: { title: z.name, content: `${z.farmerCount} farmers • ${z.supervisorCount} supervisors` },
        });
        this.mapView.graphics.add(centerGraphic);
        this.zonesGraphics.push(centerGraphic);
      });
    },
    fitAllZones() { if (!this.mapView || !this.zonesGraphics.length) return; this.mapView.goTo(this.zonesGraphics).catch(() => {}); },
  },
  beforeDestroy() { if (this.mapView) { this.mapView.destroy(); this.mapView = null; } },
};
</script>

<style scoped>
</style>
