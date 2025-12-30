<template>
  <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200">
    <!-- Header -->
    <div class="tw-p-4 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between">
      <div class="tw-flex tw-items-center tw-gap-3">
        <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-purple-100 tw-flex tw-items-center tw-justify-center">
          <v-icon color="purple">mdi-cog</v-icon>
        </div>
        <div>
          <h3 class="tw-font-semibold tw-text-gray-900">Processing Events</h3>
          <p class="tw-text-sm tw-text-gray-500">Record processing activities (washing, drying, grading, etc.)</p>
        </div>
      </div>
      <div class="tw-flex tw-gap-2">
        <v-btn text color="grey" @click="skipProcessing" :disabled="processingEvents.length > 0">
          <v-icon left small>mdi-skip-next</v-icon>
          Skip (Raw Export)
        </v-btn>
        <v-btn color="primary" depressed @click="showAddDialog = true">
          <v-icon left>mdi-plus</v-icon>
          Add Processing
        </v-btn>
      </div>
    </div>

    <!-- Summary Stats -->
    <div class="tw-grid tw-grid-cols-4 tw-gap-4 tw-p-4 tw-bg-gray-50 tw-border-b">
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-purple-600">{{ processingEvents.length }}</p>
        <p class="tw-text-xs tw-text-gray-500">Processing Events</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-blue-600">{{ totalInputKg.toFixed(0) }}</p>
        <p class="tw-text-xs tw-text-gray-500">Input (kg)</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-green-600">{{ totalOutputKg.toFixed(0) }}</p>
        <p class="tw-text-xs tw-text-gray-500">Output (kg)</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold" :class="yieldPercentage >= 80 ? 'tw-text-green-600' : 'tw-text-orange-600'">
          {{ yieldPercentage.toFixed(0) }}%
        </p>
        <p class="tw-text-xs tw-text-gray-500">Yield</p>
      </div>
    </div>

    <!-- Info Alert for Skipping -->
    <v-alert v-if="processingEvents.length === 0" type="info" text dense class="tw-mx-4 tw-mt-4">
      <strong>Optional Stage:</strong> If exporting raw commodities without processing, you can skip this stage.
    </v-alert>

    <!-- Loading State -->
    <div v-if="loading" class="tw-p-8 tw-text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="tw-text-gray-500 tw-mt-2">Loading processing events...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="processingEvents.length === 0" class="tw-p-8 tw-text-center">
      <v-icon size="48" color="grey lighten-2">mdi-cog-outline</v-icon>
      <p class="tw-text-gray-500 tw-mt-2">No processing events yet</p>
      <p class="tw-text-sm tw-text-gray-400">Add processing events or skip if exporting raw commodities</p>
    </div>

    <!-- Processing Events List -->
    <div v-else class="tw-divide-y tw-divide-gray-100">
      <div
        v-for="event in processingEvents"
        :key="event.id"
        class="tw-p-4 hover:tw-bg-gray-50 tw-transition-colors"
      >
        <div class="tw-flex tw-items-start tw-justify-between">
          <div class="tw-flex tw-items-start tw-gap-3">
            <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-purple-100 tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
              <v-icon color="purple">mdi-cog</v-icon>
            </div>
            <div>
              <p class="tw-font-medium tw-text-gray-900">
                {{ event.processorName || 'Unknown Processor' }}
              </p>
              <p class="tw-text-sm tw-text-gray-500">
                {{ event.processType || 'Processing' }}
                <span v-if="event.batchNumber" class="tw-ml-2 tw-text-gray-400">
                  Batch: {{ event.batchNumber }}
                </span>
              </p>
              <p class="tw-text-xs tw-text-gray-400 tw-mt-1">
                {{ formatDate(event.processingDate) }}
              </p>
            </div>
          </div>
          <div class="tw-text-right">
            <p class="tw-text-sm tw-text-gray-500">
              {{ event.inputQuantityKg?.toFixed(1) }} kg → {{ event.outputQuantityKg?.toFixed(1) }} kg
            </p>
            <p class="tw-text-xs" :class="getYieldClass(event)">
              {{ getEventYield(event) }}% yield
            </p>
            <v-chip v-if="event.hederaHash" x-small color="purple" text-color="white" class="tw-mt-1">
              <v-icon left x-small>mdi-check-decagram</v-icon>
              Blockchain
            </v-chip>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Processing Dialog -->
    <v-dialog v-model="showAddDialog" max-width="500" persistent>
      <v-card>
        <v-card-title class="tw-text-lg">
          <v-icon left color="purple">mdi-cog-plus</v-icon>
          Record Processing Event
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="tw-pt-4">
          <v-form ref="processingForm" v-model="formValid">
            <v-select
              v-model="newEvent.processorId"
              :items="processors"
              item-text="name"
              item-value="id"
              label="Processor"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-factory"
            ></v-select>

            <v-select
              v-model="newEvent.processType"
              :items="processTypes"
              label="Process Type"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-cog"
            ></v-select>

            <v-text-field
              v-model="newEvent.inputQuantityKg"
              label="Input Quantity (kg)"
              type="number"
              outlined
              dense
              :rules="[rules.required, rules.positive]"
              prepend-inner-icon="mdi-arrow-down"
              suffix="kg"
            ></v-text-field>

            <v-text-field
              v-model="newEvent.outputQuantityKg"
              label="Output Quantity (kg)"
              type="number"
              outlined
              dense
              :rules="[rules.required, rules.positive]"
              prepend-inner-icon="mdi-arrow-up"
              suffix="kg"
            ></v-text-field>

            <v-text-field
              v-model="newEvent.processingDate"
              label="Processing Date"
              type="date"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-calendar"
            ></v-text-field>

            <v-text-field
              v-model="newEvent.batchNumber"
              label="Batch Number (Optional)"
              outlined
              dense
              prepend-inner-icon="mdi-tag"
            ></v-text-field>

            <v-textarea
              v-model="newEvent.notes"
              label="Notes (Optional)"
              outlined
              dense
              rows="2"
              prepend-inner-icon="mdi-note-text"
            ></v-textarea>
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
            @click="saveProcessingEvent"
          >
            Save Processing
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProcessingEventsPanel',

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
      saving: false,
      showAddDialog: false,
      formValid: false,

      processingEvents: [],
      processors: [],

      newEvent: {
        processorId: null,
        processType: null,
        inputQuantityKg: null,
        outputQuantityKg: null,
        processingDate: new Date().toISOString().split('T')[0],
        batchNumber: '',
        notes: '',
      },

      processTypes: [
        'Washing',
        'Drying',
        'Hulling',
        'Grading',
        'Sorting',
        'Roasting',
        'Milling',
        'Fermentation',
        'Packaging',
        'Other',
      ],

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

    totalInputKg() {
      return this.processingEvents.reduce((sum, e) => sum + (e.inputQuantityKg || 0), 0);
    },

    totalOutputKg() {
      return this.processingEvents.reduce((sum, e) => sum + (e.outputQuantityKg || 0), 0);
    },

    yieldPercentage() {
      if (this.totalInputKg === 0) return 0;
      return (this.totalOutputKg / this.totalInputKg) * 100;
    },
  },

  async mounted() {
    await Promise.all([
      this.loadProcessingEvents(),
      this.loadProcessors(),
    ]);
  },

  methods: {
    async loadProcessingEvents() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/processing`);
        this.processingEvents = response.data || [];
      } catch (error) {
        this.$toast.error('Failed to load processing events:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadProcessors() {
      try {
        // Load suppliers connected to this exporter that can process
        const response = await axios.get(`/api/v1/supply-chain/exporter/${this.effectiveExporterId}/connected-suppliers`);
        const suppliers = response.data?.data || response.data || [];

        // Filter to only show supplier types that can process
        const processorTypes = ['PROCESSOR', 'WAREHOUSE', 'COOPERATIVE'];
        this.processors = suppliers
          .filter((s) => processorTypes.includes(s.supplierType))
          .map((s) => ({
            id: s.id,
            name: s.supplierName || s.name || `Processor ${s.id}`,
            supplierType: s.supplierType,
          }));

        // If no connected processors found, try to get all processors
        if (this.processors.length === 0) {
          const allSuppliersResponse = await axios.get('/api/v1/supply-chain/suppliers', {
            params: { supplierType: 'PROCESSOR', isActive: true },
          });
          const allProcessors = allSuppliersResponse.data?.content || allSuppliersResponse.data || [];
          this.processors = allProcessors.map((s) => ({
            id: s.id,
            name: s.supplierName || s.name || `Processor ${s.id}`,
            supplierType: s.supplierType,
          }));
        }
      } catch (error) {
        this.$toast.error('Failed to load processors:', error.message);
      }
    },

    async saveProcessingEvent() {
      if (!this.$refs.processingForm.validate()) return;

      this.saving = true;
      try {
        // Convert date to LocalDateTime format (add time component)
        const processingDateTime = `${this.newEvent.processingDate}T00:00:00`;

        await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/processing`, {
          processorId: this.newEvent.processorId,
          processType: this.newEvent.processType,
          inputQuantityKg: parseFloat(this.newEvent.inputQuantityKg),
          outputQuantityKg: parseFloat(this.newEvent.outputQuantityKg),
          processingDate: processingDateTime,
          batchNumber: this.newEvent.batchNumber,
          notes: this.newEvent.notes,
        });

        this.$emit('processing-added');
        await this.loadProcessingEvents();
        this.closeDialog();
      } catch (error) {
        this.$toast.error('Failed to save processing event:', error.message);
        this.$emit('error', error.response?.data?.message || 'Failed to save processing event');
      } finally {
        this.saving = false;
      }
    },

    skipProcessing() {
      this.$emit('stage-skipped');
    },

    closeDialog() {
      this.showAddDialog = false;
      this.newEvent = {
        processorId: null,
        processType: null,
        inputQuantityKg: null,
        outputQuantityKg: null,
        processingDate: new Date().toISOString().split('T')[0],
        batchNumber: '',
        notes: '',
      };
      // eslint-disable-next-line no-unused-expressions
      this.$refs.processingForm?.reset();
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    getEventYield(event) {
      if (!event.inputQuantityKg || event.inputQuantityKg === 0) return 0;
      return ((event.outputQuantityKg / event.inputQuantityKg) * 100).toFixed(0);
    },

    getYieldClass(event) {
      const yieldPct = this.getEventYield(event);
      return yieldPct >= 80 ? 'tw-text-green-600' : 'tw-text-orange-600';
    },
  },
};
</script>
