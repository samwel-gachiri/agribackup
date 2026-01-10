<template>
  <Default>
    <v-container fluid class="tw-py-6 tw-bg-slate-50 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-mb-6">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <v-icon color="cyan" size="32">mdi-send</v-icon>
          <div>
            <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">Send to Supplier</h1>
            <p class="tw-text-slate-500 tw-text-sm">Transfer your produce to a connected aggregator or processor</p>
          </div>
        </div>
      </div>

      <v-row>
        <!-- Transfer Form -->
        <v-col cols="12" md="7">
          <v-card>
            <v-card-title class="tw-bg-cyan-600 tw-text-white">
              <v-icon color="white" class="tw-mr-2">mdi-tractor</v-icon>
              Send My Produce
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <v-form ref="transferForm" v-model="formValid">
                <!-- Select Production Unit -->
                <v-select
                  v-model="transfer.productionUnitId"
                  :items="productionUnits"
                  item-text="displayName"
                  item-value="id"
                  label="Select Production Unit *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-map-marker"
                  :loading="loadingUnits"
                  :rules="[v => !!v || 'Required']"
                  class="tw-mb-2"
                >
                  <template v-slot:item="{ item }">
                    <div class="tw-py-2">
                      <div class="tw-font-medium">{{ item.unitName }}</div>
                      <div class="tw-text-xs tw-text-slate-500">
                        {{ item.areaHectares }} ha • {{ item.administrativeRegion || 'Unknown' }}
                      </div>
                    </div>
                  </template>
                </v-select>

                <!-- Select Destination Supplier -->
                <v-autocomplete
                  v-model="transfer.toSupplierId"
                  :items="connectedSuppliers"
                  item-text="displayName"
                  item-value="id"
                  label="Send To (Supplier/Aggregator) *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-account-arrow-right"
                  :loading="loadingSuppliers"
                  :rules="[v => !!v || 'Required']"
                  class="tw-mb-2"
                >
                  <template v-slot:item="{ item }">
                    <div class="tw-py-2">
                      <div class="tw-font-medium">{{ item.supplierName }}</div>
                      <div class="tw-text-xs tw-text-slate-500">
                        {{ item.supplierType }} • {{ item.region || 'Unknown region' }}
                      </div>
                    </div>
                  </template>
                </v-autocomplete>

                <v-alert v-if="connectedSuppliers.length === 0 && !loadingSuppliers" type="warning" text dense class="tw-mb-4">
                  No connected suppliers found. Ask your aggregator to add you to their network.
                </v-alert>

                <v-row>
                  <v-col cols="12" md="6">
                    <v-combobox
                      v-model="transfer.produceType"
                      :items="produceTypes"
                      label="Produce Type *"
                      outlined
                      dense
                      prepend-inner-icon="mdi-leaf"
                      :rules="[v => !!v || 'Required']"
                    ></v-combobox>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model.number="transfer.quantityKg"
                      label="Quantity (kg) *"
                      type="number"
                      outlined
                      dense
                      prepend-inner-icon="mdi-weight-kilogram"
                      :rules="[v => v > 0 || 'Must be greater than 0']"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <v-row>
                  <v-col cols="12" md="6">
                    <div class="tw-text-sm tw-text-slate-600 tw-mb-1">Quality Grade *</div>
                    <v-btn-toggle v-model="transfer.qualityGrade" mandatory class="tw-w-full">
                      <v-btn value="A" color="green" class="tw-flex-1" :outlined="transfer.qualityGrade !== 'A'">
                        <v-icon left small>mdi-star</v-icon> A
                      </v-btn>
                      <v-btn value="B" color="blue" class="tw-flex-1" :outlined="transfer.qualityGrade !== 'B'">
                        <v-icon left small>mdi-star-half-full</v-icon> B
                      </v-btn>
                      <v-btn value="C" color="orange" class="tw-flex-1" :outlined="transfer.qualityGrade !== 'C'">
                        <v-icon left small>mdi-star-outline</v-icon> C
                      </v-btn>
                    </v-btn-toggle>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="transfer.harvestDate"
                      label="Harvest Date"
                      type="date"
                      outlined
                      dense
                      prepend-inner-icon="mdi-calendar"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <v-textarea
                  v-model="transfer.notes"
                  label="Notes (optional)"
                  outlined
                  dense
                  rows="2"
                  prepend-inner-icon="mdi-note-text"
                ></v-textarea>
              </v-form>
            </v-card-text>
            <v-card-actions class="tw-px-6 tw-pb-6">
              <v-spacer></v-spacer>
              <v-btn text @click="resetForm">Clear</v-btn>
              <v-btn
                color="cyan"
                dark
                large
                :loading="submitting"
                :disabled="!formValid"
                @click="submitTransfer"
              >
                <v-icon left>mdi-send</v-icon>
                Send to Supplier
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <!-- Info Panel -->
        <v-col cols="12" md="5">
          <v-card class="tw-mb-4">
            <v-card-title class="tw-text-base">
              <v-icon color="info" class="tw-mr-2">mdi-information</v-icon>
              How It Works
            </v-card-title>
            <v-card-text>
              <v-timeline dense>
                <v-timeline-item color="cyan" small>
                  <div class="tw-font-medium">1. You Send</div>
                  <div class="tw-text-sm tw-text-slate-500">Select your production unit and enter quantity</div>
                </v-timeline-item>
                <v-timeline-item color="orange" small>
                  <div class="tw-font-medium">2. Supplier Confirms</div>
                  <div class="tw-text-sm tw-text-slate-500">They verify they received your produce</div>
                </v-timeline-item>
                <v-timeline-item color="green" small>
                  <div class="tw-font-medium">3. Recorded Forever</div>
                  <div class="tw-text-sm tw-text-slate-500">Transaction saved on Hedera blockchain</div>
                </v-timeline-item>
              </v-timeline>

              <v-alert type="success" text dense class="tw-mt-4">
                <strong>Your data stays yours:</strong> By initiating the transfer, YOU control the data at the source. No one can claim they bought from you without your confirmation.
              </v-alert>
            </v-card-text>
          </v-card>

          <!-- Recent Transfers -->
          <v-card>
            <v-card-title class="tw-text-base">
              <v-icon color="green" class="tw-mr-2">mdi-history</v-icon>
              My Recent Transfers
            </v-card-title>
            <v-list dense v-if="recentTransfers.length > 0">
              <v-list-item v-for="t in recentTransfers" :key="t.id">
                <v-list-item-avatar>
                  <v-icon :color="getStatusColor(t.status)">mdi-package-variant</v-icon>
                </v-list-item-avatar>
                <v-list-item-content>
                  <v-list-item-title>{{ t.quantityKg }} kg {{ t.produceType }}</v-list-item-title>
                  <v-list-item-subtitle>
                    To {{ t.recipientName }}
                    <v-chip x-small :color="getStatusColor(t.status)" text-color="white" class="tw-ml-1">
                      {{ t.status }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="tw-text-center tw-text-slate-400 tw-py-6">
              <v-icon size="48" color="grey lighten-2">mdi-package-variant-closed</v-icon>
              <div class="tw-mt-2">No transfers yet</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'FarmerSendToSupplier',
  components: { Default },
  data() {
    return {
      formValid: false,
      submitting: false,
      loadingSuppliers: false,
      loadingUnits: false,
      connectedSuppliers: [],
      productionUnits: [],
      recentTransfers: [],
      transfer: {
        productionUnitId: null,
        toSupplierId: null,
        produceType: '',
        quantityKg: null,
        qualityGrade: 'A',
        harvestDate: new Date().toISOString().split('T')[0],
        notes: '',
      },
      produceTypes: [
        'Coffee Arabica',
        'Coffee Robusta',
        'Tea',
        'Macadamia',
        'Avocado',
        'Maize',
        'Beans',
      ],
    };
  },
  computed: {
    farmerId() {
      return this.$store.state.user?.farmerId || this.$store.state.auth?.user?.id;
    },
  },
  mounted() {
    this.loadProductionUnits();
    this.loadConnectedSuppliers();
    this.loadRecentTransfers();
  },
  methods: {
    async loadProductionUnits() {
      this.loadingUnits = true;
      try {
        const response = await axios.get(`/api/production-units/farmer/${this.farmerId}`);
        this.productionUnits = (response.data?.data || response.data || []).map((u) => ({
          ...u,
          displayName: `${u.unitName} (${u.areaHectares || 0} ha)`,
        }));
        if (this.productionUnits.length === 1) {
          this.transfer.productionUnitId = this.productionUnits[0].id;
        }
      } catch (error) {
        this.productionUnits = [];
      } finally {
        this.loadingUnits = false;
      }
    },

    async loadConnectedSuppliers() {
      this.loadingSuppliers = true;
      try {
        const response = await axios.get(`/api/farmers/${this.farmerId}/connected-suppliers`);
        this.connectedSuppliers = (response.data || []).map((s) => ({
          ...s,
          displayName: `${s.supplierName} (${s.supplierType})`,
        }));
      } catch (error) {
        this.$toast.error('Failed to fetch connected suppliers:', error.message);
        this.connectedSuppliers = [];
      } finally {
        this.loadingSuppliers = false;
      }
    },

    async loadRecentTransfers() {
      try {
        const response = await axios.get(`/api/v1/transfers/outgoing/farmer/${this.farmerId}?limit=5`);
        this.recentTransfers = response.data?.content || response.data || [];
      } catch (error) {
        this.recentTransfers = [];
      }
    },

    async submitTransfer() {
      if (!this.$refs.transferForm.validate()) return;

      this.submitting = true;
      try {
        const payload = {
          fromFarmerId: this.farmerId,
          productionUnitId: this.transfer.productionUnitId,
          toSupplierId: this.transfer.toSupplierId,
          produceType: this.transfer.produceType,
          quantityKg: this.transfer.quantityKg,
          qualityGrade: this.transfer.qualityGrade,
          harvestDate: this.transfer.harvestDate,
          notes: this.transfer.notes,
        };

        await axios.post('/api/v1/transfers/farmer', payload);
        this.$toast.success('Transfer sent! Waiting for supplier confirmation.');
        this.resetForm();
        await this.loadRecentTransfers();
      } catch (error) {
        this.$toast.error('Failed to send transfer');
      } finally {
        this.submitting = false;
      }
    },

    resetForm() {
      this.transfer = {
        productionUnitId: this.productionUnits.length === 1 ? this.productionUnits[0].id : null,
        toSupplierId: null,
        produceType: '',
        quantityKg: null,
        qualityGrade: 'A',
        harvestDate: new Date().toISOString().split('T')[0],
        notes: '',
      };
      // eslint-disable-next-line no-unused-expressions
      this.$refs.transferForm?.resetValidation();
    },

    getStatusColor(status) {
      const colors = {
        PENDING: 'orange',
        CONFIRMED: 'success',
        REJECTED: 'error',
        DISPUTED: 'warning',
      };
      return colors[status] || 'grey';
    },
  },
};
</script>
