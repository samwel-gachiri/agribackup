<template>
  <div class="eudr-stage-progress">
    <!-- Simple 5-Step Progress Bar -->
    <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-p-6 tw-mb-6">
      <!-- 5 Stage Groups -->
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-6">
        <div
          v-for="(group, index) in stageGroups"
          :key="group.key"
          class="tw-flex tw-items-center tw-flex-1"
        >
          <!-- Stage Group Circle -->
          <div class="tw-flex tw-flex-col tw-items-center">
            <div
              class="tw-w-12 tw-h-12 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-transition-all tw-cursor-pointer"
              :class="getGroupClass(group)"
              @click="selectGroup(group)"
            >
              <v-icon v-if="isGroupCompleted(group)" color="white">mdi-check</v-icon>
              <v-icon v-else :color="isGroupActive(group) ? 'white' : 'grey'">{{ group.icon }}</v-icon>
            </div>
            <span
              class="tw-text-xs tw-font-medium tw-mt-2 tw-text-center tw-max-w-20"
              :class="isGroupActive(group) ? 'tw-text-blue-600' : 'tw-text-gray-500'"
            >
              {{ group.label }}
            </span>
          </div>
          <!-- Connector Line -->
          <div
            v-if="index < stageGroups.length - 1"
            class="tw-flex-1 tw-h-1 tw-mx-3 tw-rounded"
            :class="isGroupCompleted(group) ? 'tw-bg-green-500' : 'tw-bg-gray-200'"
          ></div>
        </div>
      </div>

      <!-- Current Stage Info Bar -->
      <div class="tw-bg-gradient-to-r tw-from-blue-500 tw-to-blue-600 tw-rounded-lg tw-p-4 tw-flex tw-items-center tw-justify-between">
        <div class="tw-text-white">
          <p class="tw-text-xs tw-text-blue-100 tw-uppercase tw-tracking-wide">Current Step</p>
          <h3 class="tw-text-lg tw-font-semibold">{{ getCurrentStageName() }}</h3>
        </div>
        <div class="tw-flex tw-items-center tw-gap-4">
          <div class="tw-text-right tw-text-white">
            <span class="tw-text-2xl tw-font-bold">{{ overallProgress }}%</span>
            <p class="tw-text-xs tw-text-blue-100">Complete</p>
          </div>
          <div class="tw-w-32 tw-bg-blue-700 tw-rounded-full tw-h-2">
            <div
              class="tw-bg-white tw-rounded-full tw-h-2 tw-transition-all tw-duration-500"
              :style="{ width: overallProgress + '%' }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stage Content Area -->
    <div class="tw-mb-6">
      <!-- Main Content -->
      <div>
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
        <div v-else-if="isProcessingStage" class="tw-space-y-6">
          <!-- Consolidation Events Panel -->
          <ConsolidationEventsPanel
            :workflow-id="workflowId"
            :exporter-id="effectiveExporterId"
            @consolidation-added="handleConsolidationAdded"
            @error="$emit('error', $event)"
          />

          <!-- Processing Events Panel -->
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

        <!-- Fallback Generic Stage Content -->
        <div v-else class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-p-6">
          <div class="tw-flex tw-items-start tw-gap-4">
            <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-blue-100 tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
              <v-icon color="blue">{{ getCurrentStageIcon() }}</v-icon>
            </div>
            <div class="tw-flex-1">
              <h4 class="tw-text-lg tw-font-semibold tw-text-gray-900 tw-mb-2">{{ getCurrentStageName() }}</h4>
              <p class="tw-text-gray-600 tw-mb-4">{{ getCurrentStageDescription() }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Blockers Alert -->
    <v-alert
      v-if="blockers.length > 0"
      type="warning"
      border="left"
      colored-border
      elevation="1"
      class="tw-mb-6"
    >
      <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
        <v-icon color="orange">mdi-alert-circle</v-icon>
        <span class="tw-font-semibold">Action Required Before Proceeding</span>
      </div>
      <p v-for="(blocker, index) in blockers" :key="index" class="tw-text-sm tw-ml-8">
        • {{ blocker }}
      </p>
    </v-alert>

    <!-- Risk Assessment Display (only shown after risk assessment stage) -->
    <div
      v-if="showRiskDisplay"
      class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-p-5 tw-mb-6 tw-flex tw-items-center tw-justify-between"
    >
      <div>
        <h4 class="tw-font-semibold tw-text-gray-900">Risk Assessment</h4>
        <p class="tw-text-sm tw-text-gray-600">Based on country of origin and supply chain complexity</p>
      </div>
      <v-chip
        :color="getRiskColor(riskClassification)"
        text-color="white"
        class="tw-font-semibold"
      >
        <v-icon left small>{{ getRiskIcon(riskClassification) }}</v-icon>
        {{ riskClassification }}
      </v-chip>
    </div>

    <!-- Action Buttons -->
    <div class="tw-flex tw-items-center tw-justify-between tw-bg-gray-50 tw-rounded-xl tw-p-4">
      <v-btn
        text
        :disabled="!canRevert"
        @click="revertStage"
        :loading="reverting"
      >
        <v-icon left>mdi-arrow-left</v-icon>
        PREVIOUS STAGE
      </v-btn>

      <div class="tw-flex tw-items-center tw-gap-3">
        <v-btn
          v-if="showRiskAssessmentButton"
          color="amber"
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
        >
          ADVANCE TO NEXT STAGE
          <v-icon right>mdi-arrow-right</v-icon>
        </v-btn>
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
        console.error('Failed to load workflow progress:', error);
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
      console.log('Selected group:', group.key);
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
        console.error('Failed to advance stage:', error);
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
        console.error('Failed to revert stage:', error);
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
        console.error('Failed to run risk assessment:', error);
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
        console.error('Failed to generate DDS:', error);
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
</style>
