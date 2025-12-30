<template>
  <div class="eudr-workflow-layout">
    <!-- Professional Two-Column Layout -->
    <div class="tw-flex tw-gap-6 tw-h-full">

      <!-- LEFT SIDEBAR: Stage Navigation -->
      <div v-if="false" class="stage-sidebar tw-w-72 tw-flex-shrink-0">
        <div class="tw-sticky tw-top-0">
          <!-- Progress Summary Card -->
          <div class="progress-summary-card tw-rounded-2xl tw-p-5 tw-mb-6">
            <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
              <div>
                <p class="tw-text-xs tw-text-white/70 tw-uppercase tw-tracking-wider tw-font-medium">EUDR Progress</p>
                <h3 class="tw-text-2xl tw-font-bold tw-text-white">{{ overallProgress }}%</h3>
              </div>
              <!-- Circular Progress -->
              <div class="tw-relative tw-w-14 tw-h-14">
                <svg class="tw-w-full tw-h-full tw-transform -tw-rotate-90">
                  <circle cx="28" cy="28" r="24" stroke="rgba(255,255,255,0.2)" stroke-width="4" fill="none" />
                  <circle
                    cx="28" cy="28" r="24" stroke="white" stroke-width="4" fill="none" stroke-linecap="round"
                    :stroke-dasharray="150" :stroke-dashoffset="150 - (150 * overallProgress / 100)"
                    class="tw-transition-all tw-duration-500"
                  />
                </svg>
                <div class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center">
                  <span class="tw-text-white tw-font-bold tw-text-xs">{{ getCurrentStageNumber() }}/10</span>
                </div>
              </div>
            </div>
            <div class="tw-w-full tw-bg-white/20 tw-rounded-full tw-h-1.5">
              <div class="tw-bg-white tw-rounded-full tw-h-1.5 tw-transition-all" :style="{ width: overallProgress + '%' }"></div>
            </div>
          </div>

          <!-- Vertical Stage Steps -->
          <div class="tw-space-y-1">
            <div
              v-for="(group, index) in stageGroups"
              :key="group.key"
              class="stage-nav-item tw-rounded-xl tw-p-3 tw-cursor-pointer tw-transition-all"
              :class="getNavItemClass(group)"
              @click="selectGroup(group)"
            >
              <div class="tw-flex tw-items-center tw-gap-3">
                <!-- Step Number/Check -->
                <div
                  class="tw-w-9 tw-h-9 tw-rounded-lg tw-flex tw-items-center tw-justify-center tw-font-bold tw-text-sm tw-flex-shrink-0"
                  :class="getStepBadgeClass(group)"
                >
                  <v-icon v-if="isGroupCompleted(group)" color="white" size="18">mdi-check</v-icon>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <!-- Step Info -->
                <div class="tw-flex-1 tw-min-w-0">
                  <p class="tw-font-semibold tw-text-sm tw-truncate" :class="isGroupActive(group) ? 'tw-text-blue-700' : 'tw-text-gray-700'">
                    {{ group.label }}
                  </p>
                  <p class="tw-text-xs tw-truncate" :class="isGroupActive(group) ? 'tw-text-blue-500' : 'tw-text-gray-400'">
                    {{ getGroupStatus(group) }}
                  </p>
                </div>
                <!-- Active Indicator -->
                <v-icon v-if="isGroupActive(group)" color="primary" size="18">mdi-chevron-right</v-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- MAIN CONTENT AREA -->
      <div class="tw-flex-1 tw-flex tw-flex-col tw-min-w-0">
        <!-- Content Header -->
        <div class="content-header tw-rounded-xl tw-p-4 tw-mb-4 tw-flex tw-items-center tw-justify-between">
          <div class="tw-flex tw-items-center tw-gap-3">
            <div class="tw-w-10 tw-h-10 tw-rounded-lg tw-bg-primary tw-flex tw-items-center tw-justify-center">
              <v-icon color="white" size="22">{{ getCurrentStageIcon() }}</v-icon>
            </div>
            <div>
              <h2 class="tw-text-lg tw-font-bold tw-text-gray-900">{{ getCurrentStageName() }}</h2>
              <p class="tw-text-sm tw-text-gray-500">{{ getCurrentStageDescription() }}</p>
            </div>
          </div>
          <!-- Quick Actions -->
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-chip v-if="blockers.length > 0" color="warning" small outlined>
              <v-icon left x-small>mdi-alert</v-icon>
              {{ blockers.length }} Blocker{{ blockers.length > 1 ? 's' : '' }}
            </v-chip>
          </div>
        </div>

        <!-- Blockers Alert -->
        <v-alert
          v-if="blockers.length > 0"
          type="warning"
          border="left"
          colored-border
          elevation="0"
          class="tw-mb-4 tw-rounded-xl"
        >
          <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
            <span class="tw-font-semibold">Action Required</span>
          </div>
          <ul class="tw-text-sm tw-ml-4">
            <li v-for="(blocker, idx) in blockers" :key="idx">{{ blocker }}</li>
          </ul>
        </v-alert>

        <!-- Dynamic Panel Content -->
        <div class="panel-content tw-flex-1 tw-overflow-auto">
          <!-- Production Units Panel (Source Farms Stage) -->
          <ProductionUnitsPanel
            v-if="isSourceFarmsStage"
            :workflow-id="workflowId"
            :exporter-id="effectiveExporterId"
            :current-stage="currentStageInfo.stage"
            @navigate-to-farmers="$emit('navigate-to-farmers')"
            @unit-linked="handleUnitLinked"
            @unit-verified="handleUnitVerified"
            @deforestation-checked="handleDeforestationChecked"
          />

          <!-- Collection Events Panel (Collection Stage) -->
          <CollectionEventsPanel
            v-else-if="isCollectionStage"
            :workflow-id="workflowId"
            :exporter-id="effectiveExporterId"
            @collection-added="handleCollectionAdded"
            @error="$emit('error', $event)"
          />

          <!-- Processing Stage: Consolidation + Processing Events -->
          <div v-else-if="isProcessingStage" class="tw-space-y-4">
            <ConsolidationEventsPanel
              :workflow-id="workflowId"
              :exporter-id="effectiveExporterId"
              @consolidation-added="handleConsolidationAdded"
              @error="$emit('error', $event)"
            />
            <ProcessingEventsPanel
              :workflow-id="workflowId"
              :exporter-id="effectiveExporterId"
              @processing-added="handleProcessingAdded"
              @stage-skipped="handleProcessingSkipped"
              @error="$emit('error', $event)"
            />
          </div>

          <!-- Compliance Panel (Risk Assessment & DDS Stage) -->
          <CompliancePanel
            v-else-if="isComplianceStage"
            :workflow-id="workflowId"
            :current-stage="currentStageInfo.stage"
            @risk-assessed="handleRiskAssessed"
            @dds-generated="handleDDSGenerated"
            @error="$emit('error', $event)"
          />

          <!-- Export & Shipment Panel (Export Stages) -->
          <ExportShipmentPanel
            v-else-if="isExportStage"
            :workflow-id="workflowId"
            :exporter-id="effectiveExporterId"
            :current-stage="currentStageInfo.stage"
            @shipment-created="handleShipmentCreated"
            @shipment-updated="handleShipmentUpdated"
            @error="$emit('error', $event)"
          />

          <!-- Fallback Content -->
          <div v-else class="tw-bg-white tw-rounded-xl tw-border tw-p-6 tw-text-center tw-text-gray-500">
            <v-icon size="48" color="grey lighten-1">mdi-information-outline</v-icon>
            <p class="tw-mt-3">{{ getCurrentStageDescription() }}</p>
          </div>
        </div>

        <!-- Action Buttons - Fixed Bottom Bar -->
        <div class="action-buttons-bar tw-flex tw-items-center tw-justify-between tw-rounded-xl tw-p-4 tw-mt-4">
          <v-btn
            outlined
            color="grey darken-1"
            :disabled="!canRevert"
            @click="revertStage"
            :loading="reverting"
          >
            <v-icon left>mdi-arrow-left</v-icon>
            Previous
          </v-btn>

          <div class="tw-flex tw-items-center tw-gap-3">
            <v-btn
              v-if="showRiskAssessmentButton"
              color="amber darken-1"
              depressed
              @click="triggerRiskAssessment"
              :loading="assessingRisk"
            >
              <v-icon left>mdi-shield-search</v-icon>
              Assess Risk
            </v-btn>

            <v-btn
              v-if="showDDSButton"
              color="primary"
              depressed
              @click="generateDDS"
              :loading="generatingDDS"
            >
              <v-icon left>mdi-file-document</v-icon>
              Generate DDS
            </v-btn>

            <v-btn
              color="success"
              depressed
              :disabled="!canAdvance || blockers.length > 0"
              @click="advanceStage"
              :loading="advancing"
              class="advance-btn tw-px-6"
            >
              <span class="tw-font-semibold">Advance</span>
              <v-icon right>mdi-arrow-right</v-icon>
            </v-btn>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import ProductionUnitsPanel from './ProductionUnitsPanel.vue';
import CollectionEventsPanel from './CollectionEventsPanel.vue';
import ConsolidationEventsPanel from './ConsolidationEventsPanel.vue';
import ProcessingEventsPanel from './ProcessingEventsPanel.vue';
import CompliancePanel from './CompliancePanel.vue';
import ExportShipmentPanel from './ExportShipmentPanel.vue';

export default {
  name: 'EudrStageProgress',

  components: {
    ProductionUnitsPanel,
    CollectionEventsPanel,
    ConsolidationEventsPanel,
    ProcessingEventsPanel,
    CompliancePanel,
    ExportShipmentPanel,
  },

  props: {
    workflowId: {
      type: String,
      required: true,
    },
    exporterId: {
      type: String,
      default: null,
    },
  },

  data() {
    return {
      loading: false,
      advancing: false,
      reverting: false,
      assessingRisk: false,
      generatingDDS: false,

      // Stage data
      stages: [],
      currentStageInfo: {},
      overallProgress: 0,
      riskClassification: null,
      blockers: [],

      // Action tracking
      actionCompletion: [],

      // Simplified 5-group structure
      stageGroups: [
        {
          key: 'source', label: 'Source Farms', icon: 'mdi-sprout', stages: ['PRODUCTION_REGISTRATION', 'GEOLOCATION_VERIFICATION', 'DEFORESTATION_CHECK'],
        },
        {
          key: 'collection', label: 'Collection', icon: 'mdi-basket', stages: ['COLLECTION_AGGREGATION'],
        },
        {
          key: 'processing', label: 'Processing', icon: 'mdi-cog', stages: ['PROCESSING'],
        },
        {
          key: 'compliance', label: 'Compliance', icon: 'mdi-shield-check', stages: ['RISK_ASSESSMENT', 'DUE_DILIGENCE_STATEMENT'],
        },
        {
          key: 'export', label: 'Export', icon: 'mdi-truck-delivery', stages: ['EXPORT_SHIPMENT', 'CUSTOMS_CLEARANCE', 'DELIVERY_COMPLETE'],
        },
      ],

      // Stage metadata
      stageData: {
        PRODUCTION_REGISTRATION: {
          name: 'Register Production Units',
          description: 'Link production units (farms/plots) to this workflow. Each unit must have GPS coordinates and deforestation verification.',
          automatedActions: ['Generate unique production unit ID', 'Record on Hedera blockchain', 'Calculate plot area from coordinates'],
          eudrReference: 'Article 9(1)(a) - Geolocation of plots',
          tips: ['Use latest satellite imagery for boundary verification', 'Include buffer zone around production area'],
        },
        GEOLOCATION_VERIFICATION: {
          name: 'Verify Geolocation',
          description: 'Confirm GPS coordinates are accurate and within acceptable precision limits.',
          automatedActions: ['Validate coordinate format', 'Cross-reference with satellite imagery', 'Record verification timestamp'],
          eudrReference: 'Article 9(1)(a) - Geolocation accuracy requirements',
          tips: ['Ensure GPS accuracy is within 10 meters', 'Verify polygon boundaries match physical plots'],
        },
        DEFORESTATION_CHECK: {
          name: 'Deforestation Check',
          description: 'Verify no deforestation occurred on the production land after December 31, 2020.',
          automatedActions: ['Query Global Forest Watch API', 'Analyze GLAD alerts', 'Generate deforestation report'],
          eudrReference: 'Article 3 - Deforestation-free requirement',
          tips: ['Check for forest loss alerts regularly', 'Document any false positives with evidence'],
        },
        COLLECTION_AGGREGATION: {
          name: 'Record Collection',
          description: 'Record collection events from production units through aggregators.',
          automatedActions: ['Link collection to source plots', 'Calculate batch quantities', 'Generate traceability records'],
          eudrReference: 'Article 9(1)(d) - Traceability requirements',
          tips: ['Weigh produce at collection point', 'Document quality grades'],
        },
        PROCESSING: {
          name: 'Processing (Optional)',
          description: 'Record processing activities if applicable. This stage can be skipped for raw commodity exports.',
          automatedActions: ['Track input/output ratios', 'Record processing dates', 'Link to source batches'],
          eudrReference: 'Article 2(10) - Processing operations',
          tips: ['Document processing methods', 'Maintain batch separation'],
        },
        RISK_ASSESSMENT: {
          name: 'Risk Assessment',
          description: 'Evaluate the risk level based on country of origin, supply chain complexity, and other factors.',
          automatedActions: ['Calculate risk score', 'Apply country risk factors', 'Generate risk report'],
          eudrReference: 'Article 10 - Risk assessment requirements',
          tips: ['Higher risk requires more due diligence', 'Document risk mitigation measures'],
        },
        DUE_DILIGENCE_STATEMENT: {
          name: 'Due Diligence Statement',
          description: 'Generate the Due Diligence Statement (DDS) required for EU import.',
          automatedActions: ['Compile all evidence', 'Generate DDS document', 'Record on blockchain'],
          eudrReference: 'Article 4 - Due diligence obligation',
          tips: ['Review all linked documents', 'Ensure all stages are complete'],
        },
        EXPORT_SHIPMENT: {
          name: 'Export & Shipment',
          description: 'Record shipment details for export to the EU market.',
          automatedActions: ['Generate export documentation', 'Link to DDS reference', 'Create shipment manifest'],
          eudrReference: 'Article 4(2) - Placing on the market',
          tips: ['Include DDS reference in shipping documents', 'Coordinate with customs agent'],
        },
        CUSTOMS_CLEARANCE: {
          name: 'Customs Clearance',
          description: 'Complete customs clearance at EU port of entry.',
          automatedActions: ['Submit DDS to EU system', 'Await customs verification', 'Record clearance status'],
          eudrReference: 'Article 26 - Customs controls',
          tips: ['Prepare for potential inspections', 'Have supporting documents ready'],
        },
        DELIVERY_COMPLETE: {
          name: 'Delivery Complete',
          description: 'Commodity successfully delivered to EU importer.',
          automatedActions: ['Confirm delivery receipt', 'Close workflow', 'Archive records'],
          eudrReference: 'Article 4 - Compliance confirmation',
          tips: ['Obtain delivery confirmation', 'Retain records for 5 years'],
        },
      },
    };
  },

  computed: {
    effectiveExporterId() {
      return this.exporterId || this.$store.state.auth?.user?.id;
    },

    isSourceFarmsStage() {
      const sourceFarmsStages = ['PRODUCTION_REGISTRATION', 'GEOLOCATION_VERIFICATION', 'DEFORESTATION_CHECK'];
      return sourceFarmsStages.includes(this.currentStageInfo.stage);
    },

    isCollectionStage() {
      return this.currentStageInfo.stage === 'COLLECTION_AGGREGATION';
    },

    isProcessingStage() {
      return this.currentStageInfo.stage === 'PROCESSING';
    },

    isComplianceStage() {
      const complianceStages = ['RISK_ASSESSMENT', 'DUE_DILIGENCE_STATEMENT'];
      return complianceStages.includes(this.currentStageInfo.stage);
    },

    isExportStage() {
      const exportStages = ['EXPORT_SHIPMENT', 'CUSTOMS_CLEARANCE', 'DELIVERY_COMPLETE'];
      return exportStages.includes(this.currentStageInfo.stage);
    },

    canAdvance() {
      return this.currentStageInfo.stage !== 'DELIVERY_COMPLETE';
    },

    canRevert() {
      return this.currentStageInfo.stage !== 'PRODUCTION_REGISTRATION';
    },

    showRiskAssessmentButton() {
      return this.currentStageInfo.stage === 'RISK_ASSESSMENT' && !this.riskClassification;
    },

    showRiskDisplay() {
      // Only show risk classification from RISK_ASSESSMENT stage onwards
      const postRiskStages = ['RISK_ASSESSMENT', 'DUE_DILIGENCE_STATEMENT', 'EXPORT_SHIPMENT', 'CUSTOMS_CLEARANCE', 'DELIVERY_COMPLETE'];
      return this.riskClassification && postRiskStages.includes(this.currentStageInfo.stage);
    },

    showDDSButton() {
      return this.currentStageInfo.stage === 'DUE_DILIGENCE_STATEMENT';
    },
  },

  async mounted() {
    await this.loadWorkflowProgress();
  },

  methods: {
    async loadWorkflowProgress() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/eudr/workflow-stages/workflows/${this.workflowId}/progress`);

        this.stages = response.data.stages || [];
        this.overallProgress = response.data.overallProgress || 0;
        this.riskClassification = response.data.riskClassification;
        this.blockers = response.data.blockers || [];

        // Map currentStage - API returns string, we need object
        const currentStageName = response.data.currentStage || 'PRODUCTION_REGISTRATION';
        const currentStageFromArray = this.stages.find((s) => s.stage === currentStageName);
        this.currentStageInfo = currentStageFromArray || { stage: currentStageName, displayName: currentStageName };

        // Initialize action completion array
        if (this.currentStageInfo.requiredActions) {
          this.actionCompletion = new Array(this.currentStageInfo.requiredActions.length).fill(false);
        }

        // Notify parent of current stage (ensure we always have a value)
        const stageToEmit = this.currentStageInfo.stage;
        this.$emit('current-stage-changed', stageToEmit);
      } catch (error) {
        this.$toast.error('Failed to load workflow progress:', error.message);
        // Set default stage on error so UI doesn't get stuck
        this.currentStageInfo = { stage: 'PRODUCTION_REGISTRATION', displayName: 'Farm Registration' };
        this.stages = [];
        this.overallProgress = 0;
        this.$emit('current-stage-changed', 'PRODUCTION_REGISTRATION');
        this.$emit('error', 'Failed to load workflow progress');
      } finally {
        this.loading = false;
      }
    },

    getCurrentStageName() {
      return this.stageData[this.currentStageInfo.stage]?.name || this.currentStageInfo.displayName || 'Loading...';
    },

    getCurrentStageDescription() {
      return this.stageData[this.currentStageInfo.stage]?.description || '';
    },

    getCurrentStageIcon() {
      const icons = {
        PRODUCTION_REGISTRATION: 'mdi-sprout',
        GEOLOCATION_VERIFICATION: 'mdi-map-marker-check',
        DEFORESTATION_CHECK: 'mdi-forest',
        COLLECTION_AGGREGATION: 'mdi-basket',
        PROCESSING: 'mdi-cog',
        RISK_ASSESSMENT: 'mdi-shield-search',
        DUE_DILIGENCE_STATEMENT: 'mdi-file-document',
        EXPORT_SHIPMENT: 'mdi-truck',
        CUSTOMS_CLEARANCE: 'mdi-passport',
        DELIVERY_COMPLETE: 'mdi-check-decagram',
      };
      return icons[this.currentStageInfo.stage] || 'mdi-circle';
    },

    getGroupClass(group) {
      if (this.isGroupCompleted(group)) {
        return 'tw-bg-green-900 tw-text-white';
      }
      if (this.isGroupActive(group)) {
        return 'tw-bg-black tw-text-white tw-ring-4 tw-ring-blue-200';
      }
      return 'tw-bg-gray-100 tw-text-gray-400 tw-border-2 tw-border-gray-200';
    },

    getModernGroupClass(group) {
      if (this.isGroupCompleted(group)) {
        return 'stage-circle-completed';
      }
      if (this.isGroupActive(group)) {
        return 'stage-circle-active';
      }
      return 'stage-circle-pending';
    },

    getCurrentStageNumber() {
      const allStages = [
        'PRODUCTION_REGISTRATION', 'GEOLOCATION_VERIFICATION', 'DEFORESTATION_CHECK',
        'COLLECTION_AGGREGATION', 'PROCESSING', 'RISK_ASSESSMENT',
        'DUE_DILIGENCE_STATEMENT', 'EXPORT_SHIPMENT', 'CUSTOMS_CLEARANCE', 'DELIVERY_COMPLETE',
      ];
      const index = allStages.indexOf(this.currentStageInfo.stage);
      return index >= 0 ? index + 1 : 1;
    },

    isGroupActive(group) {
      return group.stages.includes(this.currentStageInfo.stage);
    },

    isGroupCompleted(group) {
      // Check if all stages in this group are completed
      return group.stages.every((stageName) => {
        const stage = this.stages.find((s) => s.stage === stageName);
        return stage && stage.status === 'COMPLETED';
      });
    },

    selectGroup(group) {
      // Optional: could show group details or allow navigation
      this.$toast.info('Selected group:', group.key);
    },

    getNavItemClass(group) {
      if (this.isGroupActive(group)) {
        return 'stage-nav-active';
      }
      if (this.isGroupCompleted(group)) {
        return 'stage-nav-completed';
      }
      return 'stage-nav-pending';
    },

    getStepBadgeClass(group) {
      if (this.isGroupCompleted(group)) {
        return 'tw-bg-green-600 tw-text-white';
      }
      if (this.isGroupActive(group)) {
        return 'tw-bg-blue-600 tw-text-white';
      }
      return 'tw-bg-gray-200 tw-text-gray-500';
    },

    getGroupStatus(group) {
      if (this.isGroupCompleted(group)) {
        return 'Completed';
      }
      if (this.isGroupActive(group)) {
        return 'In Progress';
      }
      return 'Pending';
    },

    async advanceStage() {
      this.advancing = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/advance`);

        if (response.data.success) {
          this.$emit('stage-advanced', response.data);
          await this.loadWorkflowProgress();
          // loadWorkflowProgress already emits stage-changed
        } else {
          this.blockers = response.data.blockers || [];
          this.$emit('error', response.data.message);
        }
      } catch (error) {
        this.$toast.error('Failed to advance stage:', error.message);
        this.$emit('error', 'Failed to advance to next stage');
      } finally {
        this.advancing = false;
      }
    },

    async revertStage() {
      // eslint-disable-next-line no-restricted-globals
      if (!confirm('Are you sure you want to go back to the previous stage?')) return;

      this.reverting = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/revert`, {
          reason: 'User requested revert',
        });

        if (response.data.success) {
          this.$emit('stage-reverted', response.data);
          await this.loadWorkflowProgress();
          // loadWorkflowProgress already emits stage-changed
        }
      } catch (error) {
        this.$toast.error('Failed to revert stage:', error.message);
        this.$emit('error', 'Failed to revert to previous stage');
      } finally {
        this.reverting = false;
      }
    },

    async triggerRiskAssessment() {
      this.assessingRisk = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/risk-assessment`);

        this.riskClassification = response.data.classification;
        this.$emit('risk-assessed', response.data);
        await this.loadWorkflowProgress();
      } catch (error) {
        this.$emit('error', 'Failed to run risk assessment');
      } finally {
        this.assessingRisk = false;
      }
    },

    async generateDDS() {
      this.generatingDDS = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/due-diligence-statement`);

        this.$emit('dds-generated', response.data);
        await this.loadWorkflowProgress();
      } catch (error) {
        this.$emit('error', 'Failed to generate Due Diligence Statement');
      } finally {
        this.generatingDDS = false;
      }
    },

    handleUnitLinked() {
      // Refresh progress when a production unit is linked
      this.loadWorkflowProgress();
    },

    handleUnitVerified() {
      // Refresh progress when geolocation is verified
      this.loadWorkflowProgress();
    },

    handleDeforestationChecked() {
      // Refresh progress when deforestation check is complete
      this.loadWorkflowProgress();
    },

    handleCollectionAdded() {
      // Refresh progress when a collection event is added
      this.loadWorkflowProgress();
    },

    handleConsolidationAdded() {
      // Refresh progress when a consolidation event is added
      this.loadWorkflowProgress();
    },

    handleProcessingAdded() {
      // Refresh progress when a processing event is added
      this.loadWorkflowProgress();
    },

    handleProcessingSkipped() {
      // User chose to skip processing (raw commodity export)
      this.advanceStage();
    },

    handleRiskAssessed(data) {
      this.riskClassification = data?.classification;
      this.$emit('risk-assessed', data);
      this.loadWorkflowProgress();
    },

    handleDDSGenerated(data) {
      this.$emit('dds-generated', data);
      this.loadWorkflowProgress();
    },

    handleShipmentCreated() {
      // Refresh progress when a shipment is created
      this.loadWorkflowProgress();
    },

    handleShipmentUpdated() {
      // Refresh progress when shipment status is updated
      this.loadWorkflowProgress();
    },

    getRiskColor(risk) {
      const colors = {
        NEGLIGIBLE: 'success', LOW: 'info', STANDARD: 'warning', HIGH: 'error',
      };
      return colors[risk] || 'grey';
    },

    getRiskIcon(risk) {
      const icons = {
        NEGLIGIBLE: 'mdi-shield-check', LOW: 'mdi-shield', STANDARD: 'mdi-shield-alert', HIGH: 'mdi-shield-off',
      };
      return icons[risk] || 'mdi-shield';
    },
  },
};
</script>

<style scoped>
.eudr-stage-progress {
  width: 100%;
}

/* Main Progress Container */
.stage-progress-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

/* Stage Circle Styles */
.stage-circle {
  transition: all 0.3s ease;
}

.stage-circle-completed {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  box-shadow: 0 4px 14px rgba(5, 150, 105, 0.4);
}

.stage-circle-active {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.5);
}

.stage-circle-pending {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border: 2px solid #d1d5db;
}

/* Connector Line */
.connector-completed {
  background: linear-gradient(90deg, #059669 0%, #10b981 100%);
}

.connector-progress {
  animation: progress-pulse 2s ease-in-out infinite;
}

@keyframes progress-pulse {
  0%, 100% {
    opacity: 0.7;
  }
  50% {
    opacity: 1;
  }
}

/* Current Stage Card */
.current-stage-card {
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 50%, #3b82f6 100%);
  position: relative;
  overflow: hidden;
}

.current-stage-card::before {
  content: '';
  position: absolute;
  top: -100%;
  left: -100%;
  width: 300%;
  height: 300%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 50%);
  animation: shimmer-rotate 20s linear infinite;
}

@keyframes shimmer-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Ping animation for active stage glow */
@keyframes ping {
  75%, 100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.tw-animate-ping {
  animation: ping 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}

/* Action Buttons Bar */
.action-buttons-bar {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.advance-btn {
  background: linear-gradient(135deg, #059669 0%, #047857 100%) !important;
  transition: all 0.3s ease;
}

.advance-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(5, 150, 105, 0.4) !important;
}

/* Two-Column Layout - Full Width */
.eudr-workflow-layout {
  width: 100%;
  max-width: none !important;
  height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
}

.eudr-workflow-layout > div:first-child {
  flex: 1;
  min-height: 0;
}

/* Sidebar - Fixed Position within container */
.stage-sidebar {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 16px;
  align-self: flex-start;
}

/* Progress Summary Card - Modern Teal/Green Gradient */
.progress-summary-card {
  background: linear-gradient(135deg, #0d9488 0%, #14b8a6 50%, #2dd4bf 100%);
  box-shadow: 0 4px 15px rgba(13, 148, 136, 0.3);
}

/* Stage Navigation Items */
.stage-nav-item {
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.stage-nav-active {
  background: linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%);
  border-color: #14b8a6;
}

.stage-nav-completed {
  background: #f0fdf4;
}

.stage-nav-completed:hover {
  background: #dcfce7;
}

.stage-nav-pending {
  background: #fafafa;
}

.stage-nav-pending:hover {
  background: #f5f5f5;
}

/* Content Header */
.content-header {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

/* Panel Content Area - Scrollable */
.panel-content {
  flex: 1;
  overflow-y: auto;
  min-height: 300px;
}

/* Responsive: Hide sidebar on small screens */
@media (max-width: 1024px) {
  .stage-sidebar {
    display: none;
  }
}
</style>
