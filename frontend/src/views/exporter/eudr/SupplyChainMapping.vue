<template>
  <div class="supply-chain-mapping">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="purple" class="mr-3">mdi-map-search</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">Supply Chain Mapping</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Visualize and map your entire supply chain for EUDR compliance
                </p>
              </div>
            </div>
            <v-btn-toggle v-model="viewMode" mandatory>
              <v-btn value="map">
                <v-icon>mdi-map</v-icon>
                Map View
              </v-btn>
              <v-btn value="network">
                <v-icon>mdi-graph</v-icon>
                Network View
              </v-btn>
            </v-btn-toggle>
          </div>
        </v-col>
      </v-row>

      <!-- Supply Chain Statistics -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card producers" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-account-group</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ totalProducers }}</div>
              <div class="text-caption">Total Producers</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card processors" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-factory</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ totalProcessors }}</div>
              <div class="text-caption">Processing Facilities</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card regions" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-map-marker</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ totalRegions }}</div>
              <div class="text-caption">Active Regions</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card traceability" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-link-variant</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ traceabilityScore }}%</div>
              <div class="text-caption">Traceability Score</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Map and Network Views -->
      <v-row class="mb-4">
        <v-col cols="12" md="8">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">
                {{ viewMode === 'map' ? 'mdi-map' : 'mdi-graph' }}
              </v-icon>
              {{ viewMode === 'map' ? 'Geographic Supply Chain Map' : 'Supply Chain Network' }}
              <v-spacer></v-spacer>
              <v-btn-toggle v-model="mapLayer" v-if="viewMode === 'map'">
                <v-btn small value="satellite">Satellite</v-btn>
                <v-btn small value="risk">Risk Overlay</v-btn>
                <v-btn small value="alerts">Alerts</v-btn>
              </v-btn-toggle>
            </v-card-title>
            <v-card-text>
              <div class="map-container">
                <div v-if="viewMode === 'map'" class="map-placeholder">
                  <v-row>
                    <v-col cols="12">
                      <div class="text-center py-12">
                        <v-icon size="80" color="grey lighten-2">mdi-map</v-icon>
                        <div class="text-h6 grey--text mt-2">Interactive Supply Chain Map</div>
                        <div class="text-caption grey--text">
                          Geographic visualization of your supply chain network
                        </div>
                        <div class="mt-4">
                          <v-chip
                            v-for="region in activeRegions"
                            :key="region.name"
                            :color="region.riskColor"
                            text-color="white"
                            small
                            class="mr-2 mb-2"
                          >
                            {{ region.name }} ({{ region.suppliers }})
                          </v-chip>
                        </div>
                      </div>
                    </v-col>
                  </v-row>
                </div>
                <div v-else class="network-placeholder">
                  <v-row>
                    <v-col cols="12">
                      <div class="text-center py-12">
                        <v-icon size="80" color="grey lighten-2">mdi-graph</v-icon>
                        <div class="text-h6 grey--text mt-2">Supply Chain Network Graph</div>
                        <div class="text-caption grey--text">
                          Network visualization showing relationships and flow
                        </div>
                        <div class="mt-4">
                          <div class="network-legend">
                            <div class="legend-item">
                              <v-icon color="green">mdi-circle</v-icon>
                              <span>Producers</span>
                            </div>
                            <div class="legend-item">
                              <v-icon color="orange">mdi-circle</v-icon>
                              <span>Processors</span>
                            </div>
                            <div class="legend-item">
                              <v-icon color="blue">mdi-circle</v-icon>
                              <span>Exporters</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </v-col>
                  </v-row>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="info" class="mr-2">mdi-information</v-icon>
              Supply Chain Insights
            </v-card-title>
            <v-card-text>
              <div class="insight-item">
                <div class="text-subtitle-2 mb-2">Regional Distribution</div>
                <div
                  v-for="region in regionalDistribution"
                  :key="region.name"
                  class="mb-2"
                >
                  <div class="d-flex justify-space-between align-center mb-1">
                    <span>{{ region.name }}</span>
                    <span class="font-weight-bold">{{ region.percentage }}%</span>
                  </div>
                  <v-progress-linear
                    :value="region.percentage"
                    :color="region.color"
                    height="6"
                    rounded
                  ></v-progress-linear>
                </div>
              </div>

              <v-divider class="my-4"></v-divider>

              <div class="insight-item">
                <div class="text-subtitle-2 mb-2">Commodity Flow</div>
                <div
                  v-for="commodity in commodityFlow"
                  :key="commodity.name"
                  class="mb-2"
                >
                  <div class="d-flex justify-space-between align-center mb-1">
                    <span>{{ commodity.name }}</span>
                    <span class="font-weight-bold">{{ commodity.volume }}kg</span>
                  </div>
                  <v-progress-linear
                    :value="(commodity.volume / totalVolume) * 100"
                    :color="commodity.color"
                    height="6"
                    rounded
                  ></v-progress-linear>
                </div>
              </div>

              <v-divider class="my-4"></v-divider>

              <div class="insight-item">
                <div class="text-subtitle-2 mb-2">Compliance Status</div>
                <div class="compliance-summary">
                  <div class="d-flex justify-space-between align-center mb-2">
                    <span>Fully Compliant</span>
                    <v-chip color="success" text-color="white" x-small>
                      {{ complianceStats.compliant }}%
                    </v-chip>
                  </div>
                  <div class="d-flex justify-space-between align-center mb-2">
                    <span>Pending Review</span>
                    <v-chip color="warning" text-color="white" x-small>
                      {{ complianceStats.pending }}%
                    </v-chip>
                  </div>
                  <div class="d-flex justify-space-between align-center">
                    <span>Non-Compliant</span>
                    <v-chip color="error" text-color="white" x-small>
                      {{ complianceStats.nonCompliant }}%
                    </v-chip>
                  </div>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Supply Chain Entities -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              Supply Chain Entities
              <v-spacer></v-spacer>
              <v-select
                v-model="entityFilter"
                :items="entityTypes"
                label="Filter by Type"
                dense
                outlined
                hide-details
                class="entity-filter"
              ></v-select>
            </v-card-title>
            <v-data-table
              :headers="entityHeaders"
              :items="filteredEntities"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.entity="{ item }">
                <div class="d-flex align-center">
                  <v-icon :color="getEntityColor(item.type)" class="mr-3">
                    {{ getEntityIcon(item.type) }}
                  </v-icon>
                  <div>
                    <div class="font-weight-medium">{{ item.name }}</div>
                    <div class="text-caption text--secondary">{{ item.id }}</div>
                  </div>
                </div>
              </template>
              <template v-slot:item.type="{ item }">
                <v-chip
                  :color="getEntityColor(item.type)"
                  text-color="white"
                  small
                >
                  {{ item.type }}
                </v-chip>
              </template>
              <template v-slot:item.complianceStatus="{ item }">
                <v-chip
                  :color="getComplianceColor(item.complianceStatus)"
                  text-color="white"
                  small
                >
                  {{ item.complianceStatus }}
                </v-chip>
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewEntity(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="info"
                  @click="traceEntity(item)"
                >
                  <v-icon>mdi-map-marker-path</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
/* eslint-disable */
export default {
  name: 'SupplyChainMapping',
  data() {
    return {
      loading: false,
      viewMode: 'map',
      mapLayer: 'satellite',
      entityFilter: 'All',
      entityTypes: ['All', 'Producer', 'Processor', 'Exporter'],
      entityHeaders: [
        { text: 'Entity', value: 'entity', sortable: true },
        { text: 'Type', value: 'type', sortable: true },
        { text: 'Region', value: 'region', sortable: true },
        { text: 'Compliance', value: 'complianceStatus', sortable: true },
        { text: 'Last Activity', value: 'lastActivity', sortable: true },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '120px',
        },
      ],
      entities: [
        {
          id: 'GVF-001',
          name: 'Green Valley Farm',
          type: 'Producer',
          region: 'Central Kenya',
          complianceStatus: 'Compliant',
          lastActivity: '2024-12-15',
        },
        {
          id: 'MCC-002',
          name: 'Mountain Coffee Co.',
          type: 'Producer',
          region: 'Western Region',
          complianceStatus: 'Pending',
          lastActivity: '2024-12-10',
        },
        {
          id: 'KCP-001',
          name: 'Kenya Coffee Processing',
          type: 'Processor',
          region: 'Central Kenya',
          complianceStatus: 'Compliant',
          lastActivity: '2024-12-14',
        },
      ],
      activeRegions: [
        { name: 'Central Kenya', suppliers: 15, riskColor: 'success' },
        { name: 'Western Region', suppliers: 8, riskColor: 'warning' },
        { name: 'Eastern Province', suppliers: 5, riskColor: 'error' },
      ],
      regionalDistribution: [
        { name: 'Central Kenya', percentage: 55, color: 'success' },
        { name: 'Western Region', percentage: 30, color: 'warning' },
        { name: 'Eastern Province', percentage: 15, color: 'error' },
      ],
      commodityFlow: [
        { name: 'Coffee', volume: 2500, color: 'brown' },
        { name: 'Cocoa', volume: 1200, color: 'orange' },
        { name: 'Palm Oil', volume: 800, color: 'green' },
      ],
      complianceStats: {
        compliant: 75,
        pending: 20,
        nonCompliant: 5,
      },
    };
  },
  computed: {
    totalProducers() {
      return this.entities.filter((e) => e.type === 'Producer').length;
    },
    totalProcessors() {
      return this.entities.filter((e) => e.type === 'Processor').length;
    },
    totalRegions() {
      return this.activeRegions.length;
    },
    traceabilityScore() {
      return 87; // Mock traceability score
    },
    totalVolume() {
      return this.commodityFlow.reduce((sum, c) => sum + c.volume, 0);
    },
    filteredEntities() {
      if (this.entityFilter === 'All') {
        return this.entities;
      }
      return this.entities.filter((entity) => entity.type === this.entityFilter);
    },
  },
  methods: {
    getEntityColor(type) {
      const colors = {
        Producer: 'success',
        Processor: 'warning',
        Exporter: 'primary',
      };
      return colors[type] || 'grey';
    },
    getEntityIcon(type) {
      const icons = {
        Producer: 'mdi-account',
        Processor: 'mdi-factory',
        Exporter: 'mdi-truck',
      };
      return icons[type] || 'mdi-help';
    },
    getComplianceColor(status) {
      const colors = {
        Compliant: 'success',
        Pending: 'warning',
        'Non-Compliant': 'error',
      };
      return colors[status] || 'grey';
    },
    viewEntity(entity) {
    },
    traceEntity(entity) {
    },
  },
};
</script>

<style scoped>
.supply-chain-mapping {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.stat-card {
  transition: transform 0.2s;
  color: white;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.producers {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.stat-card.processors {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.stat-card.regions {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}

.stat-card.traceability {
  background: linear-gradient(135deg, #9c27b0, #6a1b9a);
}

.map-container {
  height: 500px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  overflow: hidden;
}

.map-placeholder,
.network-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.network-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.insight-item {
  margin-bottom: 16px;
}

.compliance-summary {
  background: rgba(0, 0, 0, 0.02);
  padding: 12px;
  border-radius: 8px;
}

.entity-filter {
  max-width: 200px;
}
</style>
