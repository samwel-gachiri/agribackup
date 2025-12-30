<template>
  <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200">
    <!-- Header -->
    <div class="tw-p-4 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between">
      <div class="tw-flex tw-items-center tw-gap-3">
        <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-indigo-100 tw-flex tw-items-center tw-justify-center">
          <v-icon color="indigo">mdi-truck-delivery</v-icon>
        </div>
        <div>
          <h3 class="tw-font-semibold tw-text-gray-900">Export & Shipment</h3>
          <p class="tw-text-sm tw-text-gray-500">Manage shipment details and customs clearance</p>
        </div>
      </div>
      <v-btn v-if="!hasShipment" color="primary" depressed @click="showAddDialog = true">
        <v-icon left>mdi-plus</v-icon>
        Create Shipment
      </v-btn>
    </div>

    <!-- Summary Stats -->
    <div v-if="shipmentEvents.length > 0" class="tw-grid tw-grid-cols-4 tw-gap-4 tw-p-4 tw-bg-gray-50 tw-border-b">
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-indigo-600">{{ shipmentEvents.length }}</p>
        <p class="tw-text-xs tw-text-gray-500">Shipments</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-green-600">{{ totalShippedKg.toFixed(0) }}</p>
        <p class="tw-text-xs tw-text-gray-500">Total (kg)</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-blue-600">{{ uniqueDestinations }}</p>
        <p class="tw-text-xs tw-text-gray-500">Destinations</p>
      </div>
      <div class="tw-text-center">
        <v-chip :color="overallStatus === 'DELIVERED' ? 'success' : 'info'" small text-color="white">
          {{ overallStatus }}
        </v-chip>
        <p class="tw-text-xs tw-text-gray-500 tw-mt-1">Status</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="tw-p-8 tw-text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="tw-text-gray-500 tw-mt-2">Loading shipment data...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="shipmentEvents.length === 0" class="tw-p-8 tw-text-center">
      <v-icon size="48" color="grey lighten-2">mdi-truck-outline</v-icon>
      <p class="tw-text-gray-500 tw-mt-2">No shipments created yet</p>
      <p class="tw-text-sm tw-text-gray-400">Create a shipment to begin export process</p>
    </div>

    <!-- Shipment Events List -->
    <div v-else class="tw-divide-y tw-divide-gray-100">
      <div
        v-for="shipment in shipmentEvents"
        :key="shipment.id"
        class="tw-p-4"
      >
        <div class="tw-flex tw-items-start tw-justify-between tw-mb-3">
          <div class="tw-flex tw-items-start tw-gap-3">
            <div class="tw-w-10 tw-h-10 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0"
                 :class="getStatusBgClass(shipment.status)">
              <v-icon :color="getStatusColor(shipment.status)">{{ getStatusIcon(shipment.status) }}</v-icon>
            </div>
            <div>
              <p class="tw-font-medium tw-text-gray-900">
                {{ shipment.destinationCountry || 'EU' }} - {{ shipment.destinationPort || 'TBD' }}
              </p>
              <p class="tw-text-sm tw-text-gray-500">
                {{ shipment.importerName || 'Unknown Importer' }}
              </p>
              <p class="tw-text-xs tw-text-gray-400 tw-mt-1">
                Bill of Lading: {{ shipment.billOfLading || 'Pending' }}
              </p>
            </div>
          </div>
          <div class="tw-text-right">
            <p class="tw-text-lg tw-font-semibold tw-text-gray-900">{{ shipment.quantityShippedKg?.toFixed(0) || 0 }} kg</p>
            <v-chip :color="getStatusColor(shipment.status)" text-color="white" x-small>
              {{ shipment.status || 'PENDING' }}
            </v-chip>
          </div>
        </div>

        <!-- Shipment Timeline -->
        <div class="tw-ml-13 tw-border-l-2 tw-border-gray-200 tw-pl-4 tw-space-y-2">
          <div class="tw-flex tw-items-center tw-gap-2 tw-text-sm">
            <v-icon x-small :color="shipment.departureDate ? 'success' : 'grey'">
              {{ shipment.departureDate ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span class="tw-text-gray-600">Departed:</span>
            <span class="tw-font-medium">{{ formatDate(shipment.departureDate) || 'Pending' }}</span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2 tw-text-sm">
            <v-icon x-small :color="shipment.customsClearedDate ? 'success' : 'grey'">
              {{ shipment.customsClearedDate ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span class="tw-text-gray-600">Customs Cleared:</span>
            <span class="tw-font-medium">{{ formatDate(shipment.customsClearedDate) || 'Pending' }}</span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2 tw-text-sm">
            <v-icon x-small :color="shipment.deliveryDate ? 'success' : 'grey'">
              {{ shipment.deliveryDate ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span class="tw-text-gray-600">Delivered:</span>
            <span class="tw-font-medium">{{ formatDate(shipment.deliveryDate) || 'Pending' }}</span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="tw-flex tw-gap-2 tw-mt-3 tw-ml-13">
          <v-btn
            v-if="shipment.status === 'PENDING' || shipment.status === 'IN_TRANSIT'"
            x-small
            outlined
            color="primary"
            @click="updateShipmentStatus(shipment, 'IN_TRANSIT')"
            :disabled="shipment.status === 'IN_TRANSIT'"
          >
            <v-icon left x-small>mdi-truck-fast</v-icon>
            Mark Departed
          </v-btn>
          <v-btn
            v-if="shipment.status === 'IN_TRANSIT'"
            x-small
            outlined
            color="teal"
            @click="updateShipmentStatus(shipment, 'CUSTOMS_CLEARED')"
          >
            <v-icon left x-small>mdi-shield-check</v-icon>
            Customs Cleared
          </v-btn>
          <v-btn
            v-if="shipment.status === 'CUSTOMS_CLEARED'"
            x-small
            outlined
            color="success"
            @click="updateShipmentStatus(shipment, 'DELIVERED')"
          >
            <v-icon left x-small>mdi-package-variant-closed</v-icon>
            Confirm Delivery
          </v-btn>
          <v-btn
            v-if="shipment.hederaHash"
            x-small
            text
            color="purple"
            @click="viewOnBlockchain(shipment.hederaHash)"
          >
            <v-icon left x-small>mdi-check-decagram</v-icon>
            Blockchain
          </v-btn>
        </div>
      </div>
    </div>

    <!-- Add Shipment Dialog -->
    <v-dialog v-model="showAddDialog" max-width="650" persistent>
      <v-card>
        <v-card-title class="tw-text-lg">
          <v-icon left color="indigo">mdi-truck-plus</v-icon>
          Create Shipment
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="tw-pt-4">
          <v-form ref="shipmentForm" v-model="formValid">
            <div class="tw-grid tw-grid-cols-2 tw-gap-4">
              <v-autocomplete
                v-model="newShipment.importerId"
                :items="importers"
                item-text="name"
                item-value="id"
                label="Importer"
                outlined
                dense
                :rules="[rules.required]"
                prepend-inner-icon="mdi-domain"
                @change="onImporterSelected"
                :loading="loadingImporters"
                clearable
              >
                <template v-slot:item="{ item }">
                  <v-list-item-content>
                    <v-list-item-title>{{ item.name }}</v-list-item-title>
                    <v-list-item-subtitle v-if="item.country">
                      <v-icon x-small class="mr-1">mdi-map-marker</v-icon>
                      {{ item.country }} {{ item.port ? `- ${item.port}` : '' }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                </template>
              </v-autocomplete>

              <v-text-field
                v-model="newShipment.quantityShippedKg"
                label="Quantity (kg)"
                type="number"
                outlined
                dense
                :rules="[rules.required, rules.positive]"
                prepend-inner-icon="mdi-weight-kilogram"
                suffix="kg"
              ></v-text-field>

              <v-autocomplete
                v-model="newShipment.destinationCountry"
                :items="countries"
                item-text="name"
                item-value="name"
                label="Destination Country"
                outlined
                dense
                :rules="[rules.required]"
                prepend-inner-icon="mdi-earth"
                :loading="loadingCountries"
                :search-input.sync="countrySearch"
                @change="onCountrySelected"
                clearable
                return-object
              >
                <template v-slot:item="{ item }">
                  <v-list-item-content>
                    <v-list-item-title>
                      <span class="mr-2">{{ item.flag }}</span>
                      {{ item.name }}
                    </v-list-item-title>
                    <v-list-item-subtitle>{{ item.region }}</v-list-item-subtitle>
                  </v-list-item-content>
                </template>
                <template v-slot:selection="{ item }">
                  <span class="mr-2">{{ item.flag }}</span>
                  {{ item.name }}
                </template>
              </v-autocomplete>

              <v-autocomplete
                v-model="newShipment.destinationPort"
                :items="availablePorts"
                item-text="name"
                item-value="name"
                label="Destination Port"
                outlined
                dense
                :rules="[rules.required]"
                prepend-inner-icon="mdi-anchor"
                :loading="loadingPorts"
                :disabled="!newShipment.destinationCountry"
                clearable
                :hint="!newShipment.destinationCountry ? 'Select a country first' : ''"
                persistent-hint
              >
                <template v-slot:item="{ item }">
                  <v-list-item-content>
                    <v-list-item-title>{{ item.name }}</v-list-item-title>
                    <v-list-item-subtitle v-if="item.type">
                      <v-icon x-small class="mr-1">mdi-information</v-icon>
                      {{ item.type }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                </template>
              </v-autocomplete>

              <v-text-field
                v-model="newShipment.departureDate"
                label="Expected Departure"
                type="date"
                outlined
                dense
                prepend-inner-icon="mdi-calendar"
              ></v-text-field>

              <v-text-field
                v-model="newShipment.billOfLading"
                label="Bill of Lading (Optional)"
                outlined
                dense
                prepend-inner-icon="mdi-file-document"
              ></v-text-field>
            </div>

            <v-text-field
              v-model="newShipment.containerNumber"
              label="Container Number (Optional)"
              outlined
              dense
              prepend-inner-icon="mdi-package-variant"
              class="tw-mt-2"
            ></v-text-field>

            <v-textarea
              v-model="newShipment.notes"
              label="Notes (Optional)"
              outlined
              dense
              rows="2"
              prepend-inner-icon="mdi-note-text"
            ></v-textarea>

            <!-- Auto-prefilled indicator -->
            <v-alert
              v-if="prefillApplied"
              type="info"
              dense
              outlined
              dismissible
              class="tw-mt-2"
            >
              <v-icon left small>mdi-auto-fix</v-icon>
              Destination details pre-filled from selected importer
            </v-alert>
          </v-form>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions class="tw-px-4 tw-py-3">
          <v-spacer></v-spacer>
          <v-btn text @click="closeDialog">Cancel</v-btn>
          <v-btn
            color="primary"
            depressed
            :loading="saving"
            :disabled="!formValid"
            @click="saveShipment"
          >
            Create Shipment
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ExportShipmentPanel',

  props: {
    workflowId: {
      type: String,
      required: true,
    },
    exporterId: {
      type: String,
      default: null,
    },
    currentStage: {
      type: String,
      default: 'EXPORT_SHIPMENT',
    },
  },

  data() {
    return {
      loading: false,
      saving: false,
      showAddDialog: false,
      formValid: false,

      // Loading states
      loadingImporters: false,
      loadingCountries: false,
      loadingPorts: false,

      shipmentEvents: [],
      importers: [],
      countries: [],
      availablePorts: [],
      countrySearch: '',
      prefillApplied: false,

      newShipment: {
        importerId: null,
        quantityShippedKg: null,
        destinationCountry: null,
        destinationPort: '',
        departureDate: '',
        billOfLading: '',
        containerNumber: '',
        notes: '',
      },

      // Major ports by country (fallback data)
      portsByCountry: {
        Germany: [
          // eslint-disable-next-line sonarjs/no-duplicate-string
          { name: 'Hamburg', type: 'Major Container Port' },
          // eslint-disable-next-line sonarjs/no-duplicate-string
          { name: 'Bremerhaven', type: 'Container Port' },
          { name: 'Wilhelmshaven', type: 'Deep Water Port' },
        ],
        Netherlands: [
          { name: 'Rotterdam', type: 'Europe\'s Largest Port' },
          { name: 'Amsterdam', type: 'Container Port' },
        ],
        Belgium: [
          { name: 'Antwerp', type: 'Major Container Port' },
          { name: 'Zeebrugge', type: 'Container Port' },
        ],
        France: [
          { name: 'Le Havre', type: 'Major Container Port' },
          { name: 'Marseille', type: 'Mediterranean Port' },
          { name: 'Dunkirk', type: 'Container Port' },
        ],
        Italy: [
          { name: 'Genoa', type: 'Major Container Port' },
          { name: 'La Spezia', type: 'Container Port' },
          { name: 'Trieste', type: 'Adriatic Port' },
          { name: 'Gioia Tauro', type: 'Transhipment Hub' },
        ],
        Spain: [
          { name: 'Valencia', type: 'Major Container Port' },
          { name: 'Barcelona', type: 'Container Port' },
          { name: 'Algeciras', type: 'Mediterranean Port' },
        ],
        'United Kingdom': [
          { name: 'Felixstowe', type: 'UK\'s Largest Container Port' },
          { name: 'Southampton', type: 'Container Port' },
          { name: 'London Gateway', type: 'Deep Sea Port' },
          { name: 'Liverpool', type: 'Container Port' },
        ],
        Poland: [
          { name: 'Gdansk', type: 'Baltic Container Port' },
          { name: 'Gdynia', type: 'Container Port' },
        ],
        Sweden: [
          { name: 'Gothenburg', type: 'Scandinavia\'s Largest Port' },
          { name: 'Stockholm', type: 'Baltic Port' },
        ],
        Denmark: [
          { name: 'Copenhagen', type: 'Container Port' },
          { name: 'Aarhus', type: 'Container Port' },
        ],
        Finland: [
          { name: 'Helsinki', type: 'Container Port' },
          { name: 'Kotka', type: 'Container Port' },
        ],
        Portugal: [
          { name: 'Lisbon', type: 'Container Port' },
          { name: 'Sines', type: 'Deep Water Port' },
        ],
        Greece: [
          { name: 'Piraeus', type: 'Major Container Port' },
          { name: 'Thessaloniki', type: 'Container Port' },
        ],
        'United States': [
          { name: 'Los Angeles', type: 'West Coast Major Port' },
          { name: 'Long Beach', type: 'West Coast Port' },
          { name: 'New York/New Jersey', type: 'East Coast Major Port' },
          { name: 'Savannah', type: 'East Coast Port' },
          { name: 'Houston', type: 'Gulf Coast Port' },
          { name: 'Seattle', type: 'Pacific Northwest Port' },
        ],
        China: [
          { name: 'Shanghai', type: 'World\'s Busiest Port' },
          { name: 'Shenzhen', type: 'Major Container Port' },
          { name: 'Ningbo-Zhoushan', type: 'Major Container Port' },
          { name: 'Guangzhou', type: 'Container Port' },
          { name: 'Qingdao', type: 'Container Port' },
        ],
        Japan: [
          { name: 'Tokyo', type: 'Major Container Port' },
          { name: 'Yokohama', type: 'Container Port' },
          { name: 'Kobe', type: 'Container Port' },
          { name: 'Osaka', type: 'Container Port' },
        ],
        'South Korea': [
          { name: 'Busan', type: 'Major Container Port' },
          { name: 'Incheon', type: 'Container Port' },
        ],
        Singapore: [
          { name: 'Singapore', type: 'World\'s Busiest Transhipment Hub' },
        ],
        'United Arab Emirates': [
          { name: 'Jebel Ali (Dubai)', type: 'Middle East\'s Largest Port' },
          { name: 'Abu Dhabi', type: 'Container Port' },
        ],
        Australia: [
          { name: 'Melbourne', type: 'Australia\'s Busiest Port' },
          { name: 'Sydney', type: 'Container Port' },
          { name: 'Brisbane', type: 'Container Port' },
          { name: 'Fremantle', type: 'West Coast Port' },
        ],
        Brazil: [
          { name: 'Santos', type: 'Latin America\'s Largest Port' },
          { name: 'Paranagua', type: 'Container Port' },
          { name: 'Rio Grande', type: 'Container Port' },
        ],
        Canada: [
          { name: 'Vancouver', type: 'Canada\'s Largest Port' },
          { name: 'Montreal', type: 'East Coast Port' },
          { name: 'Halifax', type: 'Atlantic Port' },
        ],
        India: [
          { name: 'Nhava Sheva (JNPT)', type: 'India\'s Largest Container Port' },
          { name: 'Mundra', type: 'Private Container Port' },
          { name: 'Chennai', type: 'Container Port' },
        ],
        Austria: [
          { name: 'Vienna (Rail Terminal)', type: 'Inland Terminal' },
        ],
        Switzerland: [
          { name: 'Basel (Rhine Port)', type: 'Inland Port' },
        ],
        'Czech Republic': [
          { name: 'Prague (Rail Terminal)', type: 'Inland Terminal' },
        ],
        Ireland: [
          { name: 'Dublin', type: 'Ireland\'s Main Port' },
          { name: 'Cork', type: 'Container Port' },
        ],
      },

      rules: {
        required: (v) => !!v || 'Required',
        positive: (v) => (v && parseFloat(v) > 0) || 'Must be greater than 0',
      },
    };
  },

  computed: {
    effectiveExporterId() {
      return this.exporterId || this.$store.state.auth?.user?.id;
    },

    hasShipment() {
      return this.shipmentEvents.length > 0;
    },

    totalShippedKg() {
      return this.shipmentEvents.reduce((sum, s) => sum + (s.quantityShippedKg || 0), 0);
    },

    uniqueDestinations() {
      const destinations = this.shipmentEvents.map((s) => s.destinationCountry).filter(Boolean);
      return new Set(destinations).size;
    },

    overallStatus() {
      if (this.shipmentEvents.length === 0) return 'PENDING';
      const statuses = this.shipmentEvents.map((s) => s.status);
      if (statuses.every((s) => s === 'DELIVERED')) return 'DELIVERED';
      if (statuses.some((s) => s === 'IN_TRANSIT')) return 'IN_TRANSIT';
      if (statuses.some((s) => s === 'CUSTOMS_CLEARED')) return 'CUSTOMS_CLEARED';
      return 'PENDING';
    },
  },

  async mounted() {
    await Promise.all([
      this.loadShipmentEvents(),
      this.loadImporters(),
      this.loadCountries(),
    ]);
  },

  methods: {
    async loadShipmentEvents() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/shipment`);
        this.shipmentEvents = response.data || [];
      } catch (error) {
        this.$toast.error('Failed to load shipment events:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadCountries() {
      this.loadingCountries = true;
      try {
        // Use REST Countries API to get list of countries
        const response = await axios.get('https://restcountries.com/v3.1/all?fields=name,cca2,flags,region');
        this.countries = response.data
          .map((c) => ({
            name: c.name.common,
            code: c.cca2,
            flag: c.flags?.emoji || '',
            region: c.region,
          }))
          .sort((a, b) => a.name.localeCompare(b.name));
      } catch (error) {
        this.$toast.error('Failed to load countries from API, using fallback:', error.message);
        // Fallback to static list of common countries
        this.countries = [
          {
            name: 'Germany', code: 'DE', flag: '🇩🇪', region: 'Europe',
          },
          {
            name: 'Netherlands', code: 'NL', flag: '🇳🇱', region: 'Europe',
          },
          {
            name: 'Belgium', code: 'BE', flag: '🇧🇪', region: 'Europe',
          },
          {
            name: 'France', code: 'FR', flag: '🇫🇷', region: 'Europe',
          },
          {
            name: 'Italy', code: 'IT', flag: '🇮🇹', region: 'Europe',
          },
          {
            name: 'Spain', code: 'ES', flag: '🇪🇸', region: 'Europe',
          },
          {
            name: 'United Kingdom', code: 'GB', flag: '🇬🇧', region: 'Europe',
          },
          {
            name: 'Poland', code: 'PL', flag: '🇵🇱', region: 'Europe',
          },
          {
            name: 'Sweden', code: 'SE', flag: '🇸🇪', region: 'Europe',
          },
          {
            name: 'Austria', code: 'AT', flag: '🇦🇹', region: 'Europe',
          },
          {
            name: 'Switzerland', code: 'CH', flag: '🇨🇭', region: 'Europe',
          },
          {
            name: 'United States', code: 'US', flag: '🇺🇸', region: 'Americas',
          },
          {
            name: 'Canada', code: 'CA', flag: '🇨🇦', region: 'Americas',
          },
          {
            name: 'China', code: 'CN', flag: '🇨🇳', region: 'Asia',
          },
          {
            name: 'Japan', code: 'JP', flag: '🇯🇵', region: 'Asia',
          },
          {
            name: 'South Korea', code: 'KR', flag: '🇰🇷', region: 'Asia',
          },
          {
            name: 'Singapore', code: 'SG', flag: '🇸🇬', region: 'Asia',
          },
          {
            name: 'United Arab Emirates', code: 'AE', flag: '🇦🇪', region: 'Asia',
          },
          {
            name: 'Australia', code: 'AU', flag: '🇦🇺', region: 'Oceania',
          },
          {
            name: 'Brazil', code: 'BR', flag: '🇧🇷', region: 'Americas',
          },
          {
            name: 'India', code: 'IN', flag: '🇮🇳', region: 'Asia',
          },
        ].sort((a, b) => a.name.localeCompare(b.name));
      } finally {
        this.loadingCountries = false;
      }
    },

    async loadImporters() {
      this.loadingImporters = true;
      try {
        // Get the exporterId from props or store
        const exporterId = this.exporterId || this.$store?.state?.auth?.user?.id;

        if (!exporterId) {
          this.$toast.error('No exporterId available, cannot load connected importers');
          this.importers = [];
          return;
        }

        // Load connected importers for this exporter
        const response = await axios.get('/api/v1/importers/connected', {
          params: {
            exporterId,
            page: 0,
            size: 100,
          },
        });
        const importerList = response.data.content || response.data || [];
        this.importers = importerList.map((i) => ({
          id: i.id,
          name: i.businessName || i.companyName || `Importer ${i.id}`,
          country: i.destinationCountry,
          port: i.destinationPort,
        }));
      } catch (error) {
        this.$toast.error('Failed to load importers:', error.message);
        // Fallback to empty list
        this.importers = [];
      } finally {
        this.loadingImporters = false;
      }
    },

    onImporterSelected(importerId) {
      if (!importerId) {
        this.prefillApplied = false;
        return;
      }

      // Find the selected importer
      const importer = this.importers.find((i) => i.id === importerId);
      if (importer && (importer.country || importer.port)) {
        // Auto-prefill destination country
        if (importer.country) {
          const country = this.countries.find((c) => c.name === importer.country
            || c.code === importer.country);
          if (country) {
            this.newShipment.destinationCountry = country;
            this.onCountrySelected(country);
          } else {
            // Country not in list - still try to set it
            this.newShipment.destinationCountry = {
              name: importer.country, code: '', flag: '', region: '',
            };
            this.updatePortsForCountry(importer.country);
          }
        }

        // Auto-prefill destination port
        if (importer.port) {
          this.$nextTick(() => {
            this.newShipment.destinationPort = importer.port;
          });
        }

        this.prefillApplied = true;
      }
    },

    onCountrySelected(country) {
      if (!country) {
        this.availablePorts = [];
        this.newShipment.destinationPort = '';
        return;
      }

      const countryName = typeof country === 'object' ? country.name : country;
      this.updatePortsForCountry(countryName);

      // Clear port selection when country changes (unless prefilling)
      if (!this.prefillApplied) {
        this.newShipment.destinationPort = '';
      }
    },

    updatePortsForCountry(countryName) {
      this.loadingPorts = true;

      // Get ports from our static list
      const ports = this.portsByCountry[countryName];

      if (ports && ports.length > 0) {
        this.availablePorts = ports;
      } else {
        // For countries not in our list, provide a generic option
        this.availablePorts = [
          { name: `${countryName} - Main Port`, type: 'Primary Port' },
          { name: `${countryName} - Secondary Port`, type: 'Secondary Port' },
        ];
      }

      this.loadingPorts = false;
    },

    async saveShipment() {
      if (!this.$refs.shipmentForm.validate()) return;

      this.saving = true;
      try {
        // Extract country name from the country object
        const countryName = typeof this.newShipment.destinationCountry === 'object'
          ? this.newShipment.destinationCountry.name
          : this.newShipment.destinationCountry;

        await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/shipment`, {
          importerId: this.newShipment.importerId,
          quantityShippedKg: parseFloat(this.newShipment.quantityShippedKg),
          destinationCountry: countryName,
          destinationPort: this.newShipment.destinationPort,
          shipmentDate: this.newShipment.departureDate ? `${this.newShipment.departureDate}T00:00:00` : null,
          billOfLading: this.newShipment.billOfLading || null,
          containerNumber: this.newShipment.containerNumber || null,
          shipmentNotes: this.newShipment.notes || null,
        });

        this.$emit('shipment-created');
        await this.loadShipmentEvents();
        this.closeDialog();
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to create shipment');
      } finally {
        this.saving = false;
      }
    },

    async updateShipmentStatus(shipment, newStatus) {
      try {
        await axios.patch(`/api/v1/supply-chain/workflows/${this.workflowId}/shipment/${shipment.id}/status`, {
          status: newStatus,
        });

        this.$emit('shipment-updated');
        await this.loadShipmentEvents();
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to update shipment');
      }
    },

    viewOnBlockchain(hash) {
      window.open(`https://hashscan.io/testnet/transaction/${hash}`, '_blank');
    },

    closeDialog() {
      this.showAddDialog = false;
      this.prefillApplied = false;
      this.availablePorts = [];
      this.newShipment = {
        importerId: null,
        quantityShippedKg: null,
        destinationCountry: null,
        destinationPort: '',
        departureDate: '',
        billOfLading: '',
        containerNumber: '',
        notes: '',
      };
      // eslint-disable-next-line no-unused-expressions
      this.$refs.shipmentForm?.reset();
    },

    formatDate(dateString) {
      if (!dateString) return null;
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    getStatusColor(status) {
      const colors = {
        PENDING: 'grey',
        IN_TRANSIT: 'blue',
        CUSTOMS_CLEARED: 'teal',
        DELIVERED: 'success',
      };
      return colors[status] || 'grey';
    },

    getStatusBgClass(status) {
      const classes = {
        PENDING: 'tw-bg-gray-100',
        IN_TRANSIT: 'tw-bg-blue-100',
        CUSTOMS_CLEARED: 'tw-bg-teal-100',
        DELIVERED: 'tw-bg-green-100',
      };
      return classes[status] || 'tw-bg-gray-100';
    },

    getStatusIcon(status) {
      const icons = {
        PENDING: 'mdi-clock-outline',
        IN_TRANSIT: 'mdi-truck-fast',
        CUSTOMS_CLEARED: 'mdi-shield-check',
        DELIVERED: 'mdi-package-variant-closed-check',
      };
      return icons[status] || 'mdi-truck';
    },
  },
};
</script>
