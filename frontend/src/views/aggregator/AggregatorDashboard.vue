<template>
  <div class="aggregator-dashboard">
    <v-container fluid>
      <!-- Header Section -->
      <v-row>
        <v-col cols="12">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div>
              <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Aggregator Dashboard</h1>
              <p class="tw-text-gray-600 tw-mt-1">Manage farmer collections and consolidated batches</p>
            </div>
            <v-btn color="primary" large :to="{ name: 'AggregatorCollection' }">
              <v-icon left>mdi-plus</v-icon>
              New Collection
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="green lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-green-700">{{ statistics.totalCollectionEvents || 0 }}</div>
                  <div class="stat-label">Total Collections</div>
                </div>
                <v-icon size="48" color="green">mdi-package-variant</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="blue lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-blue-700">{{ formatNumber(statistics.totalVolumeCollectedKg) }} kg</div>
                  <div class="stat-label">Total Volume</div>
                </div>
                <v-icon size="48" color="blue">mdi-weight-kilogram</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="orange lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-orange-700">{{ statistics.totalBatchesCreated || 0 }}</div>
                  <div class="stat-label">Batches Created</div>
                </div>
                <v-icon size="48" color="orange">mdi-cube-send</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="red lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-red-700">{{ statistics.pendingPayments || 0 }}</div>
                  <div class="stat-label">Pending Payments</div>
                </div>
                <v-icon size="48" color="red">mdi-currency-usd-circle</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Tabs for Collections and Batches -->
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-tabs v-model="activeTab" background-color="primary" dark>
              <v-tab>
                <v-icon left>mdi-clipboard-list</v-icon>
                Collection Events
              </v-tab>
              <v-tab>
                <v-icon left>mdi-package-variant-closed</v-icon>
                Consolidated Batches
              </v-tab>
              <v-tab>
                <v-icon left>mdi-chart-bar</v-icon>
                Analytics
              </v-tab>
              <v-tab>
                <v-icon left>mdi-account-group</v-icon>
                Sub-Suppliers
              </v-tab>
            </v-tabs>

            <v-tabs-items v-model="activeTab">
              <!-- Collection Events Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
                    <v-text-field
                      v-model="collectionSearch"
                      prepend-inner-icon="mdi-magnify"
                      label="Search collections..."
                      outlined
                      dense
                      hide-details
                      clearable
                      style="max-width: 400px"
                    ></v-text-field>
                    <v-btn outlined color="primary" @click="createConsolidatedBatch">
                      <v-icon left>mdi-package-variant</v-icon>
                      Create Batch
                    </v-btn>
                  </div>

                  <v-data-table
                    :headers="collectionHeaders"
                    :items="collectionEvents"
                    :loading="loadingCollections"
                    :search="collectionSearch"
                    class="elevation-1"
                  >
                    <template v-slot:item.farmerName="{ item }">
                      <div class="tw-font-medium">{{ item.farmerName }}</div>
                    </template>

                    <template v-slot:item.quantityKg="{ item }">
                      <v-chip small color="blue lighten-4">
                        {{ formatNumber(item.quantityKg) }} kg
                      </v-chip>
                    </template>

                    <template v-slot:item.qualityGrade="{ item }">
                      <v-chip small :color="getQualityColor(item.qualityGrade)">
                        {{ item.qualityGrade }}
                      </v-chip>
                    </template>

                    <template v-slot:item.paymentStatus="{ item }">
                      <v-chip small :color="getPaymentStatusColor(item.paymentStatus)" text-color="white">
                        {{ item.paymentStatus }}
                      </v-chip>
                    </template>

                    <template v-slot:item.hederaTransactionId="{ item }">
                      <HederaTransactionViewer
                        v-if="item.hederaTransactionId"
                        mode="compact"
                        :transaction-id="item.hederaTransactionId"
                        :network="hederaNetwork"
                      />
                      <span v-else class="tw-text-gray-400">Not recorded</span>
                    </template>

                    <template v-slot:item.actions="{ item }">
                      <v-btn
                        v-if="item.paymentStatus === 'PENDING'"
                        x-small
                        color="success"
                        @click="markAsPaid(item)"
                      >
                        Mark Paid
                      </v-btn>
                      <v-btn x-small text color="primary" @click="viewCollectionDetails(item)">
                        <v-icon small>mdi-eye</v-icon>
                      </v-btn>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Consolidated Batches Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-data-table
                    :headers="batchHeaders"
                    :items="consolidatedBatches"
                    :loading="loadingBatches"
                    class="elevation-1"
                  >
                    <template v-slot:item.batchNumber="{ item }">
                      <div class="tw-font-medium tw-text-blue-600">{{ item.batchNumber }}</div>
                    </template>

                    <template v-slot:item.totalQuantityKg="{ item }">
                      <v-chip small color="blue lighten-4">
                        {{ formatNumber(item.totalQuantityKg) }} kg
                      </v-chip>
                    </template>

                    <template v-slot:item.numberOfFarmers="{ item }">
                      <v-chip small color="green lighten-4">
                        {{ item.numberOfFarmers }} farmers
                      </v-chip>
                    </template>

                    <template v-slot:item.status="{ item }">
                      <v-chip small :color="getBatchStatusColor(item.status)" text-color="white">
                        {{ item.status }}
                      </v-chip>
                    </template>

                    <template v-slot:item.hederaVerification="{ item }">
                      <HederaTransactionViewer
                        v-if="item.hederaTransactionId"
                        mode="compact"
                        :transaction-id="item.hederaTransactionId"
                        :network="hederaNetwork"
                        :document-hash="item.hederaBatchHash"
                      />
                      <span v-else class="tw-text-gray-400">Not recorded</span>
                    </template>

                    <template v-slot:item.actions="{ item }">
                      <v-menu offset-y>
                        <template v-slot:activator="{ on, attrs }">
                          <v-btn x-small text v-bind="attrs" v-on="on">
                            <v-icon small>mdi-dots-vertical</v-icon>
                          </v-btn>
                        </template>
                        <v-list dense>
                          <v-list-item @click="viewBatchDetails(item)">
                            <v-list-item-icon>
                              <v-icon small>mdi-eye</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>View Details</v-list-item-title>
                          </v-list-item>
                          <v-list-item v-if="item.status === 'PENDING'" @click="markBatchShipped(item)">
                            <v-list-item-icon>
                              <v-icon small>mdi-truck</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>Mark Shipped</v-list-item-title>
                          </v-list-item>
                        </v-list>
                      </v-menu>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Analytics Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="tw-text-base">Top Produce Types</v-card-title>
                        <v-card-text>
                          <div v-if="statistics.topProduceTypes && statistics.topProduceTypes.length > 0">
                            <v-list dense>
                              <v-list-item v-for="produce in statistics.topProduceTypes" :key="produce.produceType">
                                <v-list-item-content>
                                  <v-list-item-title>{{ produce.produceType }}</v-list-item-title>
                                </v-list-item-content>
                                <v-list-item-action>
                                  <v-chip small>{{ formatNumber(produce.totalVolumeKg) }} kg</v-chip>
                                </v-list-item-action>
                              </v-list-item>
                            </v-list>
                          </div>
                          <div v-else class="tw-text-center tw-py-8 tw-text-gray-500">
                            No produce data available
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>

                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="tw-text-base">Monthly Performance</v-card-title>
                        <v-card-text>
                          <div class="tw-text-center tw-py-4">
                            <div class="tw-text-4xl tw-font-bold tw-text-blue-600">
                              {{ formatNumber(statistics.currentMonthVolumeKg) }} kg
                            </div>
                            <div class="tw-text-sm tw-text-gray-600 tw-mt-2">Current Month Volume</div>
                          </div>
                          <v-divider class="tw-my-4"></v-divider>
                          <div class="tw-text-center">
                            <div class="tw-text-2xl tw-font-bold tw-text-green-600">
                              {{ formatNumber(statistics.averageCollectionSizeKg) }} kg
                            </div>
                            <div class="tw-text-sm tw-text-gray-600 tw-mt-2">Average Collection Size</div>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>

              <!-- Sub-Suppliers Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
                    <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800">Your Sub-Suppliers</h3>
                    <v-btn color="success" @click="showSubSupplierInviteDialog = true">
                      <v-icon left>mdi-email-plus</v-icon>
                      Invite Sub-Supplier
                    </v-btn>
                  </div>

                  <v-data-table
                    :headers="subSupplierHeaders"
                    :items="subSuppliers"
                    :loading="loadingSubSuppliers"
                    class="elevation-1"
                  >
                    <template v-slot:item.supplierName="{ item }">
                      <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
                        <v-avatar color="teal" size="40">
                          <v-icon color="white">mdi-account-tie</v-icon>
                        </v-avatar>
                        <div>
                          <div class="tw-font-semibold">{{ item.supplierName }}</div>
                          <v-chip x-small color="teal" text-color="white">{{ formatSupplierType(item.supplierType) }}</v-chip>
                        </div>
                      </div>
                    </template>

                    <template v-slot:item.contact="{ item }">
                      <div class="tw-text-sm">{{ item.email || 'N/A' }}</div>
                    </template>

                    <template v-slot:item.verificationStatus="{ item }">
                      <v-chip small :color="getVerificationColor(item.verificationStatus)">
                        {{ item.verificationStatus }}
                      </v-chip>
                    </template>

                    <template v-slot:no-data>
                      <div class="tw-text-center tw-py-8">
                        <v-icon size="64" color="grey lighten-1">mdi-account-group</v-icon>
                        <p class="tw-text-gray-500 tw-mt-2">No sub-suppliers yet</p>
                        <v-btn color="success" class="tw-mt-4" @click="showSubSupplierInviteDialog = true">
                          Invite Your First Sub-Supplier
                        </v-btn>
                      </div>
                    </template>
                  </v-data-table>

                  <!-- Pending Invites Section -->
                  <div class="tw-mt-6" v-if="pendingInvites.length > 0">
                    <h4 class="tw-font-semibold tw-text-gray-700 tw-mb-3">Pending Invitations</h4>
                    <v-list dense>
                      <v-list-item v-for="invite in pendingInvites" :key="invite.id">
                        <v-list-item-avatar>
                          <v-icon color="orange">mdi-email-outline</v-icon>
                        </v-list-item-avatar>
                        <v-list-item-content>
                          <v-list-item-title>{{ invite.email }}</v-list-item-title>
                          <v-list-item-subtitle>{{ invite.supplierName || 'Pending acceptance' }}</v-list-item-subtitle>
                        </v-list-item-content>
                        <v-list-item-action>
                          <v-btn x-small color="primary" @click="resendInvite(invite.id)">Resend</v-btn>
                        </v-list-item-action>
                      </v-list-item>
                    </v-list>
                  </div>
                </v-card-text>
              </v-tab-item>
            </v-tabs-items>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Collection Event Dialog -->
    <v-dialog v-model="showCollectionDialog" max-width="850" persistent>
      <v-card>
        <v-card-title class="tw-bg-primary tw-text-white tw-flex tw-items-center">
          <v-icon color="white" class="tw-mr-2">mdi-package-variant</v-icon>
          <span>{{ quickMode ? 'Quick Collection Mode' : 'Record New Collection' }}</span>
          <v-chip v-if="quickMode && sessionCount > 0" small color="white" class="tw-ml-3" text-color="primary">
            {{ sessionCount }} collected
          </v-chip>
          <v-spacer></v-spacer>
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn icon dark v-bind="attrs" v-on="on" @click="quickMode = !quickMode">
                <v-icon>{{ quickMode ? 'mdi-lightning-bolt' : 'mdi-lightning-bolt-outline' }}</v-icon>
              </v-btn>
            </template>
            <span>{{ quickMode ? 'Disable Quick Mode' : 'Enable Quick Mode (stay open after submit)' }}</span>
          </v-tooltip>
          <v-btn icon dark @click="closeCollectionDialog">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text class="tw-pt-4">
          <!-- Farmer Search Section -->
          <v-card outlined class="tw-mb-4 tw-bg-blue-50">
            <v-card-text class="tw-py-3">
              <div class="tw-flex tw-items-center tw-justify-between">
                <div class="tw-flex tw-items-center tw-gap-3">
                  <v-icon color="blue">mdi-account-search</v-icon>
                  <span class="tw-font-medium tw-text-blue-800">Find Farmer</span>
                </div>
                <v-btn small color="purple" dark @click="openQrScanner">
                  <v-icon left small>mdi-qrcode-scan</v-icon>
                  Scan QR
                </v-btn>
              </div>
              <v-row class="tw-mt-2">
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="farmerSearch"
                    label="Search by phone number or name"
                    outlined
                    dense
                    hide-details
                    prepend-inner-icon="mdi-magnify"
                    :loading="searchingFarmer"
                    @input="debouncedFarmerSearch"
                    clearable
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="selectedFarmer"
                    :items="farmerSearchResults"
                    item-text="displayName"
                    item-value="id"
                    label="Select farmer"
                    outlined
                    dense
                    hide-details
                    return-object
                    :disabled="farmerSearchResults.length === 0"
                    @change="onFarmerSelected"
                  >
                    <template v-slot:item="{ item }">
                      <div class="tw-py-1">
                        <div class="tw-font-medium">{{ item.name }}</div>
                        <div class="tw-text-sm tw-text-gray-500">{{ item.phoneNumber }}</div>
                      </div>
                    </template>
                  </v-select>
                </v-col>
              </v-row>

              <!-- Production Unit Selection (appears after farmer selected) -->
              <v-row v-if="selectedFarmer && farmerProductionUnits.length > 0" class="tw-mt-2">
                <v-col cols="12">
                  <v-select
                    v-model="newCollection.productionUnitId"
                    :items="farmerProductionUnits"
                    item-text="displayName"
                    item-value="id"
                    label="Select Production Unit (where produce was grown)"
                    outlined
                    dense
                    hide-details
                    prepend-inner-icon="mdi-map-marker"
                    :loading="loadingProductionUnits"
                  >
                    <template v-slot:item="{ item }">
                      <div class="tw-py-1">
                        <div class="tw-font-medium">{{ item.unitName }}</div>
                        <div class="tw-text-xs tw-text-gray-500">
                          {{ item.areaHectares }} ha • {{ item.administrativeRegion || 'Unknown region' }}
                        </div>
                      </div>
                    </template>
                  </v-select>
                </v-col>
              </v-row>
              <v-alert v-if="selectedFarmer && farmerProductionUnits.length === 0 && !loadingProductionUnits"
                       type="warning" dense class="tw-mt-2">
                This farmer has no production units registered.
              </v-alert>
            </v-card-text>
          </v-card>

          <v-form ref="collectionForm" v-model="collectionFormValid">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.farmerId"
                  label="Farmer ID *"
                  outlined
                  dense
                  :rules="[rules.required]"
                  :readonly="!!selectedFarmer"
                  :background-color="selectedFarmer ? 'green lighten-5' : ''"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.farmerName"
                  label="Farmer Name *"
                  outlined
                  dense
                  :rules="[rules.required]"
                  :readonly="!!selectedFarmer"
                  :background-color="selectedFarmer ? 'green lighten-5' : ''"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-combobox
                  v-model="newCollection.produceType"
                  label="Produce Type *"
                  :items="commonProduceTypes"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-combobox>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="newCollection.quantityKg"
                  label="Quantity (kg) *"
                  type="number"
                  outlined
                  dense
                  :rules="[rules.required, rules.positive]"
                  ref="quantityField"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model="newCollection.collectionDate"
                  label="Collection Date *"
                  type="date"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="4">
                <v-btn-toggle v-model="newCollection.qualityGrade" mandatory class="tw-w-full">
                  <v-btn value="A" color="green" class="tw-flex-1" :outlined="newCollection.qualityGrade !== 'A'">
                    <v-icon left small>mdi-star</v-icon> A
                  </v-btn>
                  <v-btn value="B" color="blue" class="tw-flex-1" :outlined="newCollection.qualityGrade !== 'B'">
                    <v-icon left small>mdi-star-half-full</v-icon> B
                  </v-btn>
                  <v-btn value="C" color="orange" class="tw-flex-1" :outlined="newCollection.qualityGrade !== 'C'">
                    <v-icon left small>mdi-star-outline</v-icon> C
                  </v-btn>
                </v-btn-toggle>
              </v-col>
              <v-col cols="12" md="4">
                <v-text-field
                  v-model.number="newCollection.pricePerKg"
                  label="Price/kg"
                  type="number"
                  step="0.01"
                  outlined
                  dense
                  prefix="KES"
                ></v-text-field>
              </v-col>
            </v-row>

            <!-- Quick total display -->
            <v-alert v-if="calculateTotal > 0" type="info" dense class="tw-mb-3">
              <div class="tw-flex tw-justify-between tw-items-center">
                <span>Total Payment:</span>
                <span class="tw-font-bold tw-text-lg">KES {{ formatNumber(calculateTotal) }}</span>
              </div>
            </v-alert>
          </v-form>
        </v-card-text>

        <v-card-actions class="tw-px-6 tw-pb-4">
          <v-chip v-if="quickMode" small color="amber" text-color="black">
            <v-icon left small>mdi-lightning-bolt</v-icon>
            Quick Mode ON
          </v-chip>
          <v-spacer></v-spacer>
          <v-btn text @click="closeCollectionDialog">{{ quickMode ? 'Done' : 'Cancel' }}</v-btn>
          <v-btn color="primary" :loading="submitting" @click="submitCollection">
            <v-icon left>mdi-check</v-icon>
            {{ quickMode ? 'Save & Next' : 'Record Collection' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Sub-Supplier Invite Dialog -->
    <v-dialog v-model="showSubSupplierInviteDialog" max-width="500" persistent>
      <v-card>
        <v-card-title class="tw-bg-success tw-text-white">
          <v-icon color="white" class="tw-mr-2">mdi-email-plus</v-icon>
          Invite Sub-Supplier
        </v-card-title>

        <v-card-text class="tw-pt-4">
          <v-alert type="info" dense class="tw-mb-4">
            Invite a supplier to join your network as a sub-supplier. They'll receive an email with a registration link.
          </v-alert>

          <v-form ref="subSupplierInviteForm" v-model="subSupplierInviteFormValid">
            <v-text-field
              v-model="subSupplierInvite.email"
              label="Email *"
              type="email"
              outlined
              dense
              prepend-inner-icon="mdi-email"
              :rules="[rules.required, v => /.+@.+\..+/.test(v) || 'Valid email required']"
            ></v-text-field>

            <v-text-field
              v-model="subSupplierInvite.supplierName"
              label="Supplier/Business Name"
              outlined
              dense
              prepend-inner-icon="mdi-domain"
            ></v-text-field>

            <v-select
              v-model="subSupplierInvite.supplierType"
              label="Supplier Type"
              :items="supplierTypes"
              outlined
              dense
              prepend-inner-icon="mdi-tag"
            ></v-select>

            <v-textarea
              v-model="subSupplierInvite.message"
              label="Personal Message (Optional)"
              outlined
              dense
              rows="3"
              placeholder="Add a note to the invitation..."
            ></v-textarea>
          </v-form>
        </v-card-text>

        <v-card-actions class="tw-px-6 tw-pb-4">
          <v-spacer></v-spacer>
          <v-btn text @click="showSubSupplierInviteDialog = false">Cancel</v-btn>
          <v-btn color="success" :loading="sendingInvite" @click="sendSubSupplierInvite">
            <v-icon left>mdi-send</v-icon>
            Send Invitation
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import HederaTransactionViewer from '@/components/shared/HederaTransactionViewer.vue';

export default {
  name: 'AggregatorDashboard',
  components: {
    HederaTransactionViewer,
  },
  data() {
    return {
      activeTab: 0,
      hederaNetwork: 'testnet',
      statistics: {},
      collectionEvents: [],
      consolidatedBatches: [],
      loadingCollections: false,
      loadingBatches: false,
      collectionSearch: '',
      showCollectionDialog: false,
      collectionFormValid: false,
      submitting: false,
      newCollection: {
        farmerId: '',
        farmerName: '',
        productionUnitId: null,
        produceType: '',
        quantityKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: 'A',
        pricePerKg: null,
        notes: '',
      },
      rules: {
        required: (v) => !!v || 'Required field',
        positive: (v) => (v && v > 0) || 'Must be greater than 0',
      },
      collectionHeaders: [
        { text: 'Farmer', value: 'farmerName' },
        { text: 'Produce', value: 'produceType' },
        { text: 'Quantity', value: 'quantityKg' },
        { text: 'Quality', value: 'qualityGrade' },
        { text: 'Collection Date', value: 'collectionDate' },
        { text: 'Payment', value: 'paymentStatus' },
        { text: 'Hedera', value: 'hederaTransactionId' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      batchHeaders: [
        { text: 'Batch Number', value: 'batchNumber' },
        { text: 'Produce Type', value: 'produceType' },
        { text: 'Total Quantity', value: 'totalQuantityKg' },
        { text: 'Farmers', value: 'numberOfFarmers' },
        { text: 'Date', value: 'consolidationDate' },
        { text: 'Status', value: 'status' },
        { text: 'Hedera', value: 'hederaVerification' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      // Sub-supplier management
      subSuppliers: [],
      pendingInvites: [],
      loadingSubSuppliers: false,
      showSubSupplierInviteDialog: false,
      subSupplierInviteFormValid: false,
      sendingInvite: false,
      subSupplierInvite: {
        email: '',
        supplierName: '',
        supplierType: '',
        message: '',
      },
      subSupplierHeaders: [
        { text: 'Supplier', value: 'supplierName' },
        { text: 'Contact', value: 'contact' },
        { text: 'Country', value: 'countryCode' },
        { text: 'Status', value: 'verificationStatus' },
      ],
      supplierTypes: [
        { text: 'Farmer', value: 'FARMER' },
        { text: 'Farmer Group', value: 'FARMER_GROUP' },
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Cooperative', value: 'COOPERATIVE' },
        { text: 'Trader', value: 'TRADER' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Warehouse', value: 'WAREHOUSE' },
      ],
      // Quick Collection Mode
      quickMode: false,
      sessionCount: 0,
      farmerSearch: '',
      searchingFarmer: false,
      farmerSearchResults: [],
      selectedFarmer: null,
      searchDebounceTimer: null,
      commonProduceTypes: [
        'Coffee Arabica',
        'Coffee Robusta',
        'Tea',
        'Macadamia',
        'Avocado',
        'Maize',
        'Beans',
        'Sorghum',
        'Millet',
        'Cassava',
      ],
      // Production Units
      farmerProductionUnits: [],
      loadingProductionUnits: false,
      // QR Scanner
      showQrScanner: false,
    };
  },
  computed: {
    aggregatorId() {
      return this.$store.state.user?.entityId || null;
    },
    calculateTotal() {
      if (this.newCollection.quantityKg && this.newCollection.pricePerKg) {
        return this.newCollection.quantityKg * this.newCollection.pricePerKg;
      }
      return 0;
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadStatistics(),
        this.loadCollectionEvents(),
        this.loadConsolidatedBatches(),
        this.loadSubSuppliers(),
        this.loadPendingInvites(),
      ]);
    },

    async loadStatistics() {
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/statistics`);
      this.statistics = response.data;
    },

    async loadCollectionEvents() {
      this.loadingCollections = true;
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/collection-events`);
      this.collectionEvents = response.data.content;
      this.loadingCollections = false;
    },

    async loadConsolidatedBatches() {
      this.loadingBatches = true;
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/consolidated-batches`);
      this.consolidatedBatches = response.data.content;
      this.loadingBatches = false;
    },

    async submitCollection() {
      if (!this.$refs.collectionForm.validate()) return;

      this.submitting = true;
      try {
        const payload = {
          ...this.newCollection,
          aggregatorId: this.aggregatorId,
          totalPayment: this.calculateTotal,
          paymentStatus: 'PENDING',
        };

        await axios.post(`/api/v1/aggregators/${this.aggregatorId}/collection-events`, payload);

        this.$toast.success('Collection recorded successfully!');

        if (this.quickMode) {
          // Quick mode: stay open, reset form for next farmer
          this.sessionCount += 1;
          this.resetCollectionForm();
          // Focus on quantity field for speed
          this.$nextTick(() => {
            if (this.$refs.quantityField) {
              this.$refs.quantityField.focus();
            }
          });
        } else {
          // Normal mode: close dialog
          this.showCollectionDialog = false;
          this.$refs.collectionForm.reset();
        }

        await this.loadData();
      } catch (error) {
        this.$toast.error(`Failed to record collection: ${error.response?.data?.message || error.message}`);
      } finally {
        this.submitting = false;
      }
    },

    resetCollectionForm() {
      this.newCollection = {
        farmerId: '',
        farmerName: '',
        productionUnitId: null,
        produceType: this.newCollection.produceType, // Keep produce type
        quantityKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: this.newCollection.qualityGrade || 'A', // Keep quality grade
        pricePerKg: this.newCollection.pricePerKg, // Keep price
        notes: '',
      };
      this.selectedFarmer = null;
      this.farmerSearch = '';
      this.farmerSearchResults = [];
      this.farmerProductionUnits = [];
    },

    closeCollectionDialog() {
      this.showCollectionDialog = false;
      this.sessionCount = 0;
      this.quickMode = false;
      this.resetCollectionForm();
    },

    debouncedFarmerSearch() {
      clearTimeout(this.searchDebounceTimer);
      this.searchDebounceTimer = setTimeout(() => {
        this.searchFarmers();
      }, 300);
    },

    async searchFarmers() {
      if (!this.farmerSearch || this.farmerSearch.length < 2) {
        this.farmerSearchResults = [];
        return;
      }

      this.searchingFarmer = true;
      try {
        // Search farmers by phone or name using correct API
        const response = await axios.get('/api/farmers/search', {
          params: {
            phone: this.farmerSearch,
            name: this.farmerSearch,
          },
        });

        this.farmerSearchResults = (response.data || []).map((farmer) => ({
          id: farmer.farmerId || farmer.id,
          name: farmer.fullName || farmer.name || 'Unknown',
          phoneNumber: farmer.phoneNumber || 'N/A',
          displayName: `${farmer.fullName || farmer.name || 'Unknown'} (${farmer.phoneNumber || 'No phone'})`,
        }));
      } catch (error) {
        // console.error('Farmer search error:', error);
        this.farmerSearchResults = [];
      } finally {
        this.searchingFarmer = false;
      }
    },

    async onFarmerSelected(farmer) {
      if (farmer) {
        this.newCollection.farmerId = farmer.id;
        this.newCollection.farmerName = farmer.name;
        // Load production units for this farmer
        await this.loadFarmerProductionUnits(farmer.id);
      }
    },

    async loadFarmerProductionUnits(farmerId) {
      this.loadingProductionUnits = true;
      this.farmerProductionUnits = [];
      try {
        const response = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const units = response.data?.data || response.data || [];
        this.farmerProductionUnits = units.map((unit) => ({
          ...unit,
          displayName: `${unit.unitName} (${unit.areaHectares || 0} ha)`,
        }));
      } catch (error) {
        // console.error('Error loading production units:', error);
        this.farmerProductionUnits = [];
      } finally {
        this.loadingProductionUnits = false;
      }
    },

    openQrScanner() {
      // For now, show an alert - QR scanning requires camera access
      // In production, integrate with a QR scanning library like vue-qrcode-reader
      this.$toast.info('QR Scanner coming soon! For now, search by phone number.');
      // TODO: Implement QR scanning with vue-qrcode-reader or similar
      // this.showQrScanner = true;
    },

    onQrCodeScanned(decodedString) {
      // Expected format: farmer ID or phone number
      this.farmerSearch = decodedString;
      this.showQrScanner = false;
      this.searchFarmers();
    },

    async markAsPaid(item) {
      try {
        await axios.patch(`/api/v1/aggregators/collection-events/${item.id}/payment`, {
          eventId: item.id,
          paymentStatus: 'PAID',
        });
        this.$toast.success('Payment status updated!');
        await this.loadCollectionEvents();
      } catch (error) {
        this.$toast.error('Failed to update payment status');
      }
    },

    viewCollectionDetails() {
      // Navigate to details view
    },

    createConsolidatedBatch() {
      // Navigate to batch creation wizard
      this.$router.push({ name: 'CreateConsolidatedBatch' });
    },

    viewBatchDetails() {
      // Navigate to batch details
    },

    async markBatchShipped(item) {
      try {
        await axios.patch(`/api/v1/aggregators/consolidated-batches/${item.id}/status?status=SHIPPED`);
        this.$toast.success('Batch marked as shipped!');
        await this.loadConsolidatedBatches();
      } catch (error) {
        this.$toast.error('Failed to update batch status');
      }
    },

    formatNumber(num) {
      return num ? num.toLocaleString() : '0';
    },

    getQualityColor(grade) {
      const colors = { A: 'green', B: 'blue', C: 'orange' };
      return colors[grade] || 'grey';
    },

    getPaymentStatusColor(status) {
      const colors = { PAID: 'success', PENDING: 'warning', CANCELLED: 'error' };
      return colors[status] || 'grey';
    },

    getBatchStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        SHIPPED: 'info',
        DELIVERED: 'success',
        CANCELLED: 'error',
      };
      return colors[status] || 'grey';
    },

    // ========== SUB-SUPPLIER METHODS ==========

    async loadSubSuppliers() {
      this.loadingSubSuppliers = true;
      try {
        const supplierId = this.$store.state.user?.supplierId || this.aggregatorId;
        if (!supplierId) return;

        const response = await axios.get(`/api/v1/supply-chain/suppliers/${supplierId}/sub-suppliers`);
        this.subSuppliers = response.data?.data || [];
      } catch (error) {
        this.$toast.error('Failed to load sub-suppliers:', error.message);
        this.subSuppliers = [];
      } finally {
        this.loadingSubSuppliers = false;
      }
    },

    async loadPendingInvites() {
      try {
        const supplierId = this.$store.state.user?.supplierId || this.aggregatorId;
        if (!supplierId) return;

        const response = await axios.get(`/api/v1/supply-chain/suppliers/${supplierId}/invites`);
        this.pendingInvites = response.data?.data || [];
      } catch (error) {
        this.$toast.error('Failed to load pending invites:', error.message);
        this.pendingInvites = [];
      }
    },

    async sendSubSupplierInvite() {
      if (!this.$refs.subSupplierInviteForm.validate()) return;

      this.sendingInvite = true;
      try {
        const supplierId = this.$store.state.user?.supplierId || this.aggregatorId;

        await axios.post('/api/v1/supply-chain/invites', {
          email: this.subSupplierInvite.email,
          supplierName: this.subSupplierInvite.supplierName,
          supplierType: this.subSupplierInvite.supplierType,
          inviterId: supplierId,
          inviterType: 'SUPPLIER',
          message: this.subSupplierInvite.message,
        });

        if (this.$toast) {
          this.$toast.success('Invitation sent successfully!');
        }
        this.showSubSupplierInviteDialog = false;
        this.subSupplierInvite = {
          email: '',
          supplierName: '',
          supplierType: '',
          message: '',
        };
        await this.loadPendingInvites();
      } catch (err) {
        const msg = err.response?.data?.message || err.message;
        if (this.$toast) {
          this.$toast.error(`Failed to send invite: ${msg}`);
        }
      } finally {
        this.sendingInvite = false;
      }
    },

    async resendInvite(inviteId) {
      try {
        await axios.post(`/api/v1/supply-chain/invites/${inviteId}/resend`);
        if (this.$toast) {
          this.$toast.success('Invitation resent!');
        }
      } catch (err) {
        if (this.$toast) {
          this.$toast.error('Failed to resend invite');
        }
      }
    },

    formatSupplierType(type) {
      if (!type) return 'N/A';
      return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    },

    getVerificationColor(status) {
      const colors = {
        VERIFIED: 'success',
        PENDING: 'warning',
        UNDER_REVIEW: 'info',
        REJECTED: 'error',
      };
      return colors[status] || 'grey';
    },
  },
};
</script>

<style scoped>
.aggregator-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-card {
  height: 100%;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}
</style>
