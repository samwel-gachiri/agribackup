<template>
  <v-card class="tw-rounded-lg" :loading="loading">
    <v-card-title class="tw-bg-green-600 tw-text-white tw-py-3">
      <v-icon left color="white">mdi-office-building</v-icon>
      SME Classification - EUDR Article 13
    </v-card-title>

    <v-card-subtitle class="tw-pt-4 tw-pb-2">
      <v-alert type="info" dense outlined class="tw-mb-0">
        <div class="tw-text-sm">
          <strong>Why declare your SME status?</strong><br>
          Small and Medium Enterprises (SMEs) qualify for <strong>simplified due diligence</strong>
          under EUDR Article 13, reducing compliance burden.
        </div>
      </v-alert>
    </v-card-subtitle>

    <v-card-text>
      <!-- Current Classification Status -->
      <div v-if="currentClassification" class="tw-mb-6">
        <div class="tw-text-sm tw-text-gray-600 tw-mb-2">Current Classification</div>
        <div class="tw-flex tw-items-center tw-gap-4">
          <v-chip
            :color="getCategoryColor(currentClassification.smeCategory)"
            dark
            class="tw-font-medium"
          >
            {{ getCategoryLabel(currentClassification.smeCategory) }}
          </v-chip>
          <v-chip
            v-if="currentClassification.isEligibleForSimplifiedDD"
            color="success"
            outlined
            small
          >
            <v-icon left small>mdi-check-circle</v-icon>
            Simplified DD Eligible
          </v-chip>
          <v-chip
            v-else
            color="warning"
            outlined
            small
          >
            <v-icon left small>mdi-alert</v-icon>
            Standard DD Required
          </v-chip>
        </div>
        <div v-if="currentClassification.isDeclarationExpired" class="tw-mt-2">
          <v-alert type="warning" dense text class="tw-mb-0">
            <v-icon left small>mdi-calendar-alert</v-icon>
            Declaration expired. Please update your SME information.
          </v-alert>
        </div>
      </div>

      <v-divider class="tw-mb-4" />

      <!-- SME Declaration Form -->
      <v-form ref="form" v-model="formValid" @submit.prevent="submitDeclaration">
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 tw-gap-4">
          <!-- Employee Count -->
          <v-text-field
            v-model.number="form.employeeCount"
            label="Number of Employees"
            type="number"
            :rules="[rules.required, rules.minZero]"
            hint="Full-time equivalent (FTE) headcount"
            persistent-hint
            outlined
            dense
            prepend-inner-icon="mdi-account-group"
          >
            <template #append>
              <v-tooltip bottom>
                <template #activator="{ on }">
                  <v-icon small v-on="on">mdi-help-circle-outline</v-icon>
                </template>
                <span>Include all full-time and part-time employees (FTE basis)</span>
              </v-tooltip>
            </template>
          </v-text-field>

          <!-- Annual Turnover -->
          <v-text-field
            v-model.number="form.annualTurnover"
            label="Annual Turnover (EUR)"
            type="number"
            :rules="[rules.required, rules.minZero]"
            hint="Last financial year's total revenue"
            persistent-hint
            outlined
            dense
            prepend-inner-icon="mdi-currency-eur"
          />

          <!-- Balance Sheet Total -->
          <v-text-field
            v-model.number="form.balanceSheetTotal"
            label="Balance Sheet Total (EUR)"
            type="number"
            :rules="[rules.required, rules.minZero]"
            hint="Total assets from last balance sheet"
            persistent-hint
            outlined
            dense
            prepend-inner-icon="mdi-bank"
          />

          <!-- Declaration Date -->
          <v-menu
            v-model="dateMenu"
            :close-on-content-click="false"
            transition="scale-transition"
            offset-y
            min-width="auto"
          >
            <template #activator="{ on, attrs }">
              <v-text-field
                v-model="form.declarationDate"
                label="Declaration Date"
                readonly
                v-bind="attrs"
                v-on="on"
                :rules="[rules.required]"
                outlined
                dense
                prepend-inner-icon="mdi-calendar"
              />
            </template>
            <v-date-picker
              v-model="form.declarationDate"
              :max="today"
              @input="dateMenu = false"
            />
          </v-menu>
        </div>

        <!-- Calculated Category Preview -->
        <v-alert
          v-if="calculatedCategory"
          :color="getCategoryColor(calculatedCategory)"
          dark
          class="tw-mt-4"
        >
          <div class="tw-flex tw-items-center tw-justify-between">
            <div>
              <div class="tw-font-medium">Calculated Classification: {{ getCategoryLabel(calculatedCategory) }}</div>
              <div class="tw-text-sm tw-opacity-90">
                {{ getSimplifiedDDMessage(calculatedCategory) }}
              </div>
            </div>
            <v-icon large>{{ getCategoryIcon(calculatedCategory) }}</v-icon>
          </div>
        </v-alert>

        <!-- EU SME Thresholds Reference -->
        <v-expansion-panels flat class="tw-mt-4">
          <v-expansion-panel>
            <v-expansion-panel-header class="tw-text-sm tw-text-gray-600">
              <v-icon small left>mdi-information-outline</v-icon>
              View EU SME Classification Thresholds
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-simple-table dense>
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Employees</th>
                    <th>Turnover</th>
                    <th>Balance Sheet</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td><v-chip x-small color="teal" dark>Micro</v-chip></td>
                    <td>&lt; 10</td>
                    <td>≤ €2M</td>
                    <td>≤ €2M</td>
                  </tr>
                  <tr>
                    <td><v-chip x-small color="blue" dark>Small</v-chip></td>
                    <td>&lt; 50</td>
                    <td>≤ €10M</td>
                    <td>≤ €10M</td>
                  </tr>
                  <tr>
                    <td><v-chip x-small color="orange" dark>Medium</v-chip></td>
                    <td>&lt; 250</td>
                    <td>≤ €50M</td>
                    <td>≤ €43M</td>
                  </tr>
                  <tr>
                    <td><v-chip x-small color="red" dark>Large</v-chip></td>
                    <td>≥ 250</td>
                    <td>&gt; €50M</td>
                    <td>&gt; €43M</td>
                  </tr>
                </tbody>
              </v-simple-table>
              <div class="tw-text-xs tw-text-gray-500 tw-mt-2">
                Based on EU Commission Recommendation 2003/361/EC.
                For classification, <strong>employee count is primary</strong>; financial thresholds are OR conditions.
              </div>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-form>
    </v-card-text>

    <v-card-actions class="tw-px-4 tw-pb-4">
      <v-spacer />
      <v-btn text @click="$emit('cancel')">Cancel</v-btn>
      <v-btn
        color="green"
        :dark="formValid"
        :disabled="!formValid || loading"
        :loading="submitting"
        @click="submitDeclaration"
      >
        <v-icon left>mdi-content-save</v-icon>
        Save Declaration
      </v-btn>
    </v-card-actions>

    <!-- Success Snackbar -->
    <v-snackbar v-model="showSuccess" color="success" timeout="3000">
      SME declaration updated successfully!
      <template #action="{ attrs }">
        <v-btn text v-bind="attrs" @click="showSuccess = false">Close</v-btn>
      </template>
    </v-snackbar>

    <!-- Error Snackbar -->
    <v-snackbar v-model="showError" color="error" timeout="5000">
      {{ errorMessage }}
      <template #action="{ attrs }">
        <v-btn text v-bind="attrs" @click="showError = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>

<script>
import smeClassificationService from '@/services/smeClassificationService';

export default {
  name: 'SmeDeclarationForm',

  props: {
    entityId: {
      type: String,
      required: true,
    },
    entityType: {
      type: String,
      required: true,
      validator: (value) => ['EXPORTER', 'IMPORTER'].includes(value),
    },
  },

  data() {
    return {
      loading: false,
      submitting: false,
      formValid: false,
      dateMenu: false,
      showSuccess: false,
      showError: false,
      errorMessage: '',
      currentClassification: null,
      form: {
        employeeCount: null,
        annualTurnover: null,
        balanceSheetTotal: null,
        declarationDate: new Date().toISOString().substr(0, 10),
      },
      rules: {
        // eslint-disable-next-line no-mixed-operators
        required: (v) => v !== null && v !== '' || 'This field is required',
        minZero: (v) => v >= 0 || 'Value must be 0 or greater',
      },
    };
  },

  computed: {
    today() {
      return new Date().toISOString().substr(0, 10);
    },

    calculatedCategory() {
      if (
        this.form.employeeCount === null
        || this.form.annualTurnover === null
        || this.form.balanceSheetTotal === null
      ) {
        return null;
      }
      return smeClassificationService.calculateSmeCategory(
        this.form.employeeCount,
        this.form.annualTurnover,
        this.form.balanceSheetTotal,
      );
    },
  },

  async mounted() {
    await this.loadCurrentClassification();
  },

  methods: {
    async loadCurrentClassification() {
      this.loading = true;
      try {
        let response;
        if (this.entityType === 'EXPORTER') {
          response = await smeClassificationService.getExporterSmeClassification(this.entityId);
        } else {
          response = await smeClassificationService.getImporterSmeClassification(this.entityId);
        }

        this.currentClassification = response.data || response;

        // Pre-fill form with existing data
        if (this.currentClassification) {
          this.form.employeeCount = this.currentClassification.employeeCount;
          this.form.annualTurnover = this.currentClassification.annualTurnover;
          this.form.balanceSheetTotal = this.currentClassification.balanceSheetTotal;
          if (this.currentClassification.declarationDate) {
            this.form.declarationDate = this.currentClassification.declarationDate;
          }
        }
      } catch (error) {
        this.$toast.error('Error loading SME classification:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async submitDeclaration() {
      if (!this.$refs.form.validate()) return;

      this.submitting = true;
      try {
        let response;
        if (this.entityType === 'EXPORTER') {
          response = await smeClassificationService.updateExporterSmeDeclaration(
            this.entityId,
            this.form,
          );
        } else {
          response = await smeClassificationService.updateImporterSmeDeclaration(
            this.entityId,
            this.form,
          );
        }

        this.currentClassification = response.data || response;
        this.showSuccess = true;
        this.$emit('updated', this.currentClassification);
      } catch (error) {
        this.errorMessage = error.response?.data?.message || 'Failed to update SME declaration';
        this.showError = true;
      } finally {
        this.submitting = false;
      }
    },

    getCategoryLabel(category) {
      return smeClassificationService.getCategoryLabel(category);
    },

    getCategoryColor(category) {
      const colors = {
        MICRO: 'teal',
        SMALL: 'blue',
        MEDIUM: 'orange',
        LARGE: 'red',
      };
      return colors[category] || 'grey';
    },

    getCategoryIcon(category) {
      const icons = {
        MICRO: 'mdi-office-building-outline',
        SMALL: 'mdi-office-building',
        MEDIUM: 'mdi-office-building-cog',
        LARGE: 'mdi-domain',
      };
      return icons[category] || 'mdi-help-circle';
    },

    getSimplifiedDDMessage(category) {
      if (category === 'LARGE') {
        return 'Standard due diligence requirements apply - full risk assessment needed.';
      }
      return 'Eligible for simplified due diligence under EUDR Article 13.';
    },
  },
};
</script>

<style scoped>
.v-expansion-panel-header {
  min-height: 40px;
  padding: 8px 16px;
}
</style>
