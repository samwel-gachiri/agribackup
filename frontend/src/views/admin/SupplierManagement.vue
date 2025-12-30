<template>
  <default-layout>
    <v-container fluid class="tw-py-6">
      <!-- Header -->
      <div class="tw-mb-6 tw-flex tw-items-center tw-justify-between">
        <div>
          <h1 class="tw-text-3xl tw-font-bold tw-text-slate-900">Supplier Management</h1>
          <p class="tw-text-slate-500 tw-mt-1">Manage your supply chain partners and connections</p>
        </div>
        <div class="tw-flex tw-gap-2">
          <v-btn
            color="success"
            prepend-icon="mdi-email-plus"
            @click="openInviteDialog"
          >
            Invite Supplier
          </v-btn>
          <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            @click="openAddDialog"
          >
            Add Supplier
          </v-btn>
        </div>
      </div>

      <!-- Filter Tabs -->
      <v-card class="tw-mb-4">
        <v-tabs v-model="activeTabIndex" color="primary">
          <v-tab>All Suppliers</v-tab>
          <v-tab>Connected</v-tab>
          <v-tab>Available</v-tab>
          <v-tab>Pending Invites</v-tab>
        </v-tabs>
      </v-card>

      <!-- Supplier Type Filter -->
      <v-card class="tw-mb-4 tw-pa-4">
        <v-row align="center">
          <v-col cols="12" md="4">
            <v-text-field
              v-model="searchQuery"
              prepend-inner-icon="mdi-magnify"
              label="Search suppliers..."
              single-line
              hide-details
              clearable
              variant="outlined"
              density="compact"
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="supplierTypeFilter"
              :items="supplierTypeOptions"
              item-text="text"
              item-value="value"
              label="Supplier Type"
              clearable
              outlined
              dense
              hide-details
            />
          </v-col>
          <v-col cols="12" md="3">
            <v-select
              v-model="verificationFilter"
              :items="verificationOptions"
              item-text="text"
              item-value="value"
              label="Verification Status"
              clearable
              outlined
              dense
              hide-details
            />
          </v-col>
          <v-col cols="12" md="2" class="tw-text-right">
            <v-btn variant="text" @click="clearFilters">
              <v-icon left>mdi-filter-remove</v-icon>
              Clear
            </v-btn>
          </v-col>
        </v-row>
      </v-card>

      <!-- Suppliers Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="filteredSuppliers"
          :loading="loading"
          class="elevation-1"
          :items-per-page="10"
        >
          <!-- Supplier column -->
          <template v-slot:item.supplierName="{ item }">
            <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
              <v-avatar :color="getSupplierColor(item.supplierType)" size="40">
                <v-icon color="white">{{ getSupplierIcon(item.supplierType) }}</v-icon>
              </v-avatar>
              <div>
                <div class="tw-font-semibold tw-text-slate-900">{{ item.supplierName }}</div>
                <v-chip x-small :color="getSupplierColor(item.supplierType)" text-color="white" class="tw-mt-1">
                  {{ formatSupplierType(item.supplierType) }}
                </v-chip>
              </div>
            </div>
          </template>

          <!-- Contact column -->
          <template v-slot:item.contact="{ item }">
            <div class="tw-space-y-1">
              <div class="tw-text-sm">{{ item.email || item.userProfile?.email || 'N/A' }}</div>
              <div class="tw-text-xs tw-text-slate-500">{{ item.phoneNumber || item.userProfile?.phoneNumber || 'N/A' }}</div>
            </div>
          </template>

          <!-- Location column -->
          <template v-slot:item.location="{ item }">
            <div class="tw-text-sm">
              <v-icon x-small class="tw-mr-1">mdi-map-marker</v-icon>
              {{ item.countryCode || 'N/A' }}
            </div>
          </template>

          <!-- Verification Status column -->
          <template v-slot:item.verificationStatus="{ item }">
            <v-chip
              :color="getVerificationColor(item.verificationStatus)"
              size="small"
              variant="flat"
            >
              {{ formatStatus(item.verificationStatus) }}
            </v-chip>
          </template>

          <!-- Connection Status column -->
          <template v-slot:item.connectionStatus="{ item }">
            <v-chip
              :color="item.isConnected ? 'success' : 'grey'"
              size="small"
              variant="flat"
            >
              {{ item.isConnected ? 'Connected' : 'Not Connected' }}
            </v-chip>
          </template>

          <!-- Actions column -->
          <template v-slot:item.actions="{ item }">
            <div class="tw-flex tw-gap-1">
              <v-btn
                v-if="item.isConnected"
                color="error"
                variant="text"
                size="small"
                :loading="disconnectingId === item.id"
                @click="disconnectFromSupplier(item.id)"
              >
                Disconnect
              </v-btn>
              <v-btn
                v-else
                color="primary"
                variant="flat"
                size="small"
                :loading="connectingId === item.id"
                @click="connectToSupplier(item.id)"
              >
                Connect
              </v-btn>
              <v-btn
                icon
                variant="text"
                size="small"
                @click="viewSupplierDetails(item)"
              >
                <v-icon>mdi-eye</v-icon>
              </v-btn>
            </div>
          </template>

          <!-- Loading state -->
          <template v-slot:loading>
            <v-skeleton-loader type="table-row@5" />
          </template>

          <!-- No data state -->
          <template v-slot:no-data>
            <div class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-4 tw-py-12">
              <v-icon size="64" color="grey-lighten-1">mdi-account-group</v-icon>
              <div class="tw-text-center">
                <p class="tw-text-lg tw-font-medium tw-text-slate-600">No suppliers found</p>
                <p class="tw-text-sm tw-text-slate-400">Add your first supplier or invite one to get started</p>
              </div>
              <div class="tw-flex tw-gap-2">
                <v-btn color="success" @click="openInviteDialog">
                  Invite Supplier
                </v-btn>
                <v-btn color="primary" @click="openAddDialog">
                  Add Supplier
                </v-btn>
              </div>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <!-- Add Supplier Dialog -->
      <v-dialog v-model="showAddDialog" max-width="600px" persistent>
        <v-card>
          <v-card-title class="tw-bg-primary tw-text-white">
            <span class="tw-text-xl tw-font-bold">Add New Supplier</span>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-form ref="supplierForm" v-model="formValid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="formData.supplierName"
                    label="Supplier/Business Name *"
                    :rules="[rules.required]"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-select
                    v-model="formData.supplierType"
                    label="Supplier Type *"
                    :items="supplierTypeItems"
                    item-text="text"
                    item-value="value"
                    :rules="[rules.required]"
                    outlined
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.email"
                    label="Email *"
                    type="email"
                    :rules="[rules.required, rules.email]"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.phone"
                    label="Phone Number"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.countryCode"
                    label="Country"
                    :items="countryOptions"
                    item-text="text"
                    item-value="value"
                    outlined
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.region"
                    label="Region/State"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-textarea
                    v-model="formData.address"
                    label="Address"
                    rows="2"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-checkbox
                    v-model="formData.autoConnect"
                    label="Automatically connect to this supplier after creation"
                    color="primary"
                    hide-details
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>

          <v-card-actions class="tw-px-6 tw-pb-4">
            <v-spacer />
            <v-btn variant="text" @click="closeAddDialog">Cancel</v-btn>
            <v-btn
              color="primary"
              :loading="saving"
              :disabled="!formValid"
              @click="saveSupplier"
            >
              Add Supplier
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Invite Supplier Dialog -->
      <v-dialog v-model="showInviteDialog" max-width="500px" persistent>
        <v-card>
          <v-card-title class="tw-bg-success tw-text-white">
            <v-icon left>mdi-email-plus</v-icon>
            <span class="tw-text-xl tw-font-bold">Invite Supplier</span>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-alert type="info" variant="tonal" class="tw-mb-4">
              Send an invitation email to a supplier. They will receive a link to register and connect with you.
            </v-alert>

            <v-form ref="inviteForm" v-model="inviteFormValid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="inviteData.email"
                    label="Supplier Email *"
                    type="email"
                    :rules="[rules.required, rules.email]"
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-email"
                  />
                </v-col>

                <v-col cols="12">
                  <v-text-field
                    v-model="inviteData.supplierName"
                    label="Supplier/Business Name"
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-domain"
                    hint="Optional - helps personalize the invite"
                    persistent-hint
                  />
                </v-col>

                <v-col cols="12">
                  <v-select
                    v-model="inviteData.supplierType"
                    label="Expected Supplier Type"
                    :items="supplierTypeItems"
                    item-text="text"
                    item-value="value"
                    outlined
                    prepend-inner-icon="mdi-tag"
                    hint="Optional - pre-selects the supplier type for them"
                    persistent-hint
                  />
                </v-col>

                <v-col cols="12">
                  <v-textarea
                    v-model="inviteData.message"
                    label="Personal Message (Optional)"
                    rows="3"
                    variant="outlined"
                    density="comfortable"
                    placeholder="Add a personal note to the invitation..."
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>

          <v-card-actions class="tw-px-6 tw-pb-4">
            <v-spacer />
            <v-btn variant="text" @click="closeInviteDialog">Cancel</v-btn>
            <v-btn
              color="success"
              :loading="sendingInvite"
              :disabled="!inviteFormValid"
              @click="sendInvite"
            >
              <v-icon left>mdi-send</v-icon>
              Send Invitation
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Supplier Details Dialog -->
      <v-dialog v-model="showDetailsDialog" max-width="700px">
        <v-card v-if="selectedSupplier">
          <v-card-title class="tw-bg-primary tw-text-white tw-flex tw-justify-between tw-items-center">
            <span class="tw-text-xl tw-font-bold">{{ selectedSupplier.supplierName }}</span>
            <v-chip :color="getSupplierColor(selectedSupplier.supplierType)" text-color="white" small>
              {{ formatSupplierType(selectedSupplier.supplierType) }}
            </v-chip>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-row>
              <v-col cols="12" md="6">
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Supplier Code</div>
                  <div class="tw-font-semibold">{{ selectedSupplier.supplierCode || 'N/A' }}</div>
                </div>
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Email</div>
                  <div class="tw-font-semibold">{{ selectedSupplier.email || selectedSupplier.userProfile?.email || 'N/A' }}</div>
                </div>
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Phone</div>
                  <div class="tw-font-semibold">{{ selectedSupplier.phoneNumber || selectedSupplier.userProfile?.phoneNumber || 'N/A' }}</div>
                </div>
              </v-col>
              <v-col cols="12" md="6">
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Country</div>
                  <div class="tw-font-semibold">{{ selectedSupplier.countryCode || 'N/A' }}</div>
                </div>
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Verification Status</div>
                  <v-chip :color="getVerificationColor(selectedSupplier.verificationStatus)" small>
                    {{ formatStatus(selectedSupplier.verificationStatus) }}
                  </v-chip>
                </div>
                <div class="tw-mb-4">
                  <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Active</div>
                  <v-chip :color="selectedSupplier.isActive ? 'success' : 'error'" small>
                    {{ selectedSupplier.isActive ? 'Active' : 'Inactive' }}
                  </v-chip>
                </div>
              </v-col>
            </v-row>

            <v-divider class="tw-my-4" />

            <div class="tw-mb-4">
              <div class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Hedera Account ID</div>
              <div class="tw-font-mono tw-text-sm">{{ selectedSupplier.hederaAccountId || 'Not linked' }}</div>
            </div>
          </v-card-text>

          <v-card-actions class="tw-px-6 tw-pb-4">
            <v-spacer />
            <v-btn variant="text" @click="showDetailsDialog = false">Close</v-btn>
            <v-btn
              v-if="!selectedSupplier.isConnected"
              color="primary"
              @click="connectToSupplier(selectedSupplier.id); showDetailsDialog = false;"
            >
              Connect
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </default-layout>
</template>

<script>
import axios from 'axios';
import DefaultLayout from '@/components/layout/Default.vue';

export default {
  name: 'SupplierManagement',
  components: {
    DefaultLayout,
  },
  data() {
    return {
      suppliers: [],
      pendingInvites: [],
      loading: false,
      saving: false,
      sendingInvite: false,
      searchQuery: '',
      activeTabIndex: 0, // 0=all, 1=connected, 2=available, 3=pending
      supplierTypeFilter: null,
      verificationFilter: null,
      showAddDialog: false,
      showInviteDialog: false,
      showDetailsDialog: false,
      formValid: false,
      inviteFormValid: false,
      connectingId: null,
      disconnectingId: null,
      selectedSupplier: null,
      headers: [
        { text: 'Supplier', value: 'supplierName', sortable: true },
        { text: 'Contact', value: 'contact', sortable: false },
        { text: 'Location', value: 'location', sortable: true },
        {
          title: 'Verification', value: 'verificationStatus', sortable: true, align: 'center',
        },
        {
          title: 'Connection', value: 'connectionStatus', sortable: true, align: 'center',
        },
        {
          title: 'Actions', value: 'actions', sortable: false, align: 'center',
        },
      ],
      supplierTypeOptions: [
        { text: 'All Types', value: null },
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Distributor', value: 'DISTRIBUTOR' },
        { text: 'Trader', value: 'TRADER' },
        { text: 'Warehouse', value: 'WAREHOUSE' },
        { text: 'Transporter', value: 'TRANSPORTER' },
        { text: 'Cooperative', value: 'COOPERATIVE' },
        { text: 'Farmer Group', value: 'FARMER_GROUP' },
        { text: 'Certification Body', value: 'CERTIFICATION_BODY' },
        { text: 'Other', value: 'OTHER' },
      ],
      supplierTypeItems: [
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Distributor', value: 'DISTRIBUTOR' },
        { text: 'Trader', value: 'TRADER' },
        { text: 'Warehouse', value: 'WAREHOUSE' },
        { text: 'Transporter', value: 'TRANSPORTER' },
        { text: 'Cooperative', value: 'COOPERATIVE' },
        { text: 'Farmer Group', value: 'FARMER_GROUP' },
        { text: 'Certification Body', value: 'CERTIFICATION_BODY' },
        { text: 'Other', value: 'OTHER' },
      ],
      verificationOptions: [
        { text: 'All Statuses', value: null },
        { text: 'Verified', value: 'VERIFIED' },
        { text: 'Pending', value: 'PENDING' },
        { text: 'Under Review', value: 'UNDER_REVIEW' },
        { text: 'Rejected', value: 'REJECTED' },
      ],
      countryOptions: [
        // Africa
        { text: 'Algeria', value: 'DZ' },
        { text: 'Angola', value: 'AO' },
        { text: 'Benin', value: 'BJ' },
        { text: 'Botswana', value: 'BW' },
        { text: 'Burkina Faso', value: 'BF' },
        { text: 'Burundi', value: 'BI' },
        { text: 'Cameroon', value: 'CM' },
        { text: 'Central African Republic', value: 'CF' },
        { text: 'Chad', value: 'TD' },
        { text: 'Comoros', value: 'KM' },
        { text: 'Congo (DRC)', value: 'CD' },
        { text: 'Congo (Republic)', value: 'CG' },
        { text: "Côte d'Ivoire", value: 'CI' },
        { text: 'Djibouti', value: 'DJ' },
        { text: 'Egypt', value: 'EG' },
        { text: 'Equatorial Guinea', value: 'GQ' },
        { text: 'Eritrea', value: 'ER' },
        { text: 'Eswatini', value: 'SZ' },
        { text: 'Ethiopia', value: 'ET' },
        { text: 'Gabon', value: 'GA' },
        { text: 'Gambia', value: 'GM' },
        { text: 'Ghana', value: 'GH' },
        { text: 'Guinea', value: 'GN' },
        { text: 'Guinea-Bissau', value: 'GW' },
        { text: 'Kenya', value: 'KE' },
        { text: 'Lesotho', value: 'LS' },
        { text: 'Liberia', value: 'LR' },
        { text: 'Libya', value: 'LY' },
        { text: 'Madagascar', value: 'MG' },
        { text: 'Malawi', value: 'MW' },
        { text: 'Mali', value: 'ML' },
        { text: 'Mauritania', value: 'MR' },
        { text: 'Mauritius', value: 'MU' },
        { text: 'Morocco', value: 'MA' },
        { text: 'Mozambique', value: 'MZ' },
        { text: 'Namibia', value: 'NA' },
        { text: 'Niger', value: 'NE' },
        { text: 'Nigeria', value: 'NG' },
        { text: 'Rwanda', value: 'RW' },
        { text: 'São Tomé and Príncipe', value: 'ST' },
        { text: 'Senegal', value: 'SN' },
        { text: 'Seychelles', value: 'SC' },
        { text: 'Sierra Leone', value: 'SL' },
        { text: 'Somalia', value: 'SO' },
        { text: 'South Africa', value: 'ZA' },
        { text: 'South Sudan', value: 'SS' },
        { text: 'Sudan', value: 'SD' },
        { text: 'Tanzania', value: 'TZ' },
        { text: 'Togo', value: 'TG' },
        { text: 'Tunisia', value: 'TN' },
        { text: 'Uganda', value: 'UG' },
        { text: 'Zambia', value: 'ZM' },
        { text: 'Zimbabwe', value: 'ZW' },
        // Europe
        { text: 'Austria', value: 'AT' },
        { text: 'Belgium', value: 'BE' },
        { text: 'Bulgaria', value: 'BG' },
        { text: 'Croatia', value: 'HR' },
        { text: 'Cyprus', value: 'CY' },
        { text: 'Czech Republic', value: 'CZ' },
        { text: 'Denmark', value: 'DK' },
        { text: 'Estonia', value: 'EE' },
        { text: 'Finland', value: 'FI' },
        { text: 'France', value: 'FR' },
        { text: 'Germany', value: 'DE' },
        { text: 'Greece', value: 'GR' },
        { text: 'Hungary', value: 'HU' },
        { text: 'Iceland', value: 'IS' },
        { text: 'Ireland', value: 'IE' },
        { text: 'Italy', value: 'IT' },
        { text: 'Latvia', value: 'LV' },
        { text: 'Lithuania', value: 'LT' },
        { text: 'Luxembourg', value: 'LU' },
        { text: 'Malta', value: 'MT' },
        { text: 'Netherlands', value: 'NL' },
        { text: 'Norway', value: 'NO' },
        { text: 'Poland', value: 'PL' },
        { text: 'Portugal', value: 'PT' },
        { text: 'Romania', value: 'RO' },
        { text: 'Slovakia', value: 'SK' },
        { text: 'Slovenia', value: 'SI' },
        { text: 'Spain', value: 'ES' },
        { text: 'Sweden', value: 'SE' },
        { text: 'Switzerland', value: 'CH' },
        { text: 'United Kingdom', value: 'GB' },
        // Americas
        { text: 'Argentina', value: 'AR' },
        { text: 'Bolivia', value: 'BO' },
        { text: 'Brazil', value: 'BR' },
        { text: 'Canada', value: 'CA' },
        { text: 'Chile', value: 'CL' },
        { text: 'Colombia', value: 'CO' },
        { text: 'Costa Rica', value: 'CR' },
        { text: 'Cuba', value: 'CU' },
        { text: 'Dominican Republic', value: 'DO' },
        { text: 'Ecuador', value: 'EC' },
        { text: 'El Salvador', value: 'SV' },
        { text: 'Guatemala', value: 'GT' },
        { text: 'Haiti', value: 'HT' },
        { text: 'Honduras', value: 'HN' },
        { text: 'Jamaica', value: 'JM' },
        { text: 'Mexico', value: 'MX' },
        { text: 'Nicaragua', value: 'NI' },
        { text: 'Panama', value: 'PA' },
        { text: 'Paraguay', value: 'PY' },
        { text: 'Peru', value: 'PE' },
        { text: 'Trinidad and Tobago', value: 'TT' },
        { text: 'United States', value: 'US' },
        { text: 'Uruguay', value: 'UY' },
        { text: 'Venezuela', value: 'VE' },
        // Asia
        { text: 'Bangladesh', value: 'BD' },
        { text: 'Cambodia', value: 'KH' },
        { text: 'China', value: 'CN' },
        { text: 'India', value: 'IN' },
        { text: 'Indonesia', value: 'ID' },
        { text: 'Japan', value: 'JP' },
        { text: 'Kazakhstan', value: 'KZ' },
        { text: 'Laos', value: 'LA' },
        { text: 'Malaysia', value: 'MY' },
        { text: 'Myanmar', value: 'MM' },
        { text: 'Nepal', value: 'NP' },
        { text: 'Pakistan', value: 'PK' },
        { text: 'Philippines', value: 'PH' },
        { text: 'Singapore', value: 'SG' },
        { text: 'South Korea', value: 'KR' },
        { text: 'Sri Lanka', value: 'LK' },
        { text: 'Taiwan', value: 'TW' },
        { text: 'Thailand', value: 'TH' },
        { text: 'Turkey', value: 'TR' },
        { text: 'United Arab Emirates', value: 'AE' },
        { text: 'Uzbekistan', value: 'UZ' },
        { text: 'Vietnam', value: 'VN' },
        // Oceania
        { text: 'Australia', value: 'AU' },
        { text: 'Fiji', value: 'FJ' },
        { text: 'New Zealand', value: 'NZ' },
        { text: 'Papua New Guinea', value: 'PG' },
      ],
      formData: {
        supplierName: '',
        supplierType: 'AGGREGATOR',
        email: '',
        phone: '',
        countryCode: 'KE',
        region: '',
        address: '',
        autoConnect: true,
      },
      inviteData: {
        email: '',
        supplierName: '',
        supplierType: null,
        message: '',
      },
      rules: {
        required: (v) => !!v || 'This field is required',
        email: (v) => /.+@.+\..+/.test(v) || 'Email must be valid',
      },
    };
  },
  computed: {
    exporterId() {
      return this.$store.state.auth?.user?.id;
    },
    filteredSuppliers() {
      let result = [...this.suppliers];

      // Filter by tab (0=all, 1=connected, 2=available, 3=pending)
      if (this.activeTabIndex === 1) {
        result = result.filter((s) => s.isConnected);
      } else if (this.activeTabIndex === 2) {
        result = result.filter((s) => !s.isConnected);
      } else if (this.activeTabIndex === 3) {
        return this.pendingInvites;
      }

      // Filter by search
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter((s) => s.supplierName?.toLowerCase().includes(query)
          || s.email?.toLowerCase().includes(query)
          || s.supplierCode?.toLowerCase().includes(query));
      }

      // Filter by supplier type
      if (this.supplierTypeFilter) {
        result = result.filter((s) => s.supplierType === this.supplierTypeFilter);
      }

      // Filter by verification status
      if (this.verificationFilter) {
        result = result.filter((s) => s.verificationStatus === this.verificationFilter);
      }

      return result;
    },
  },
  mounted() {
    this.fetchSuppliers();
    this.fetchPendingInvites();
  },
  methods: {
    async fetchSuppliers() {
      this.loading = true;
      try {
        // Fetch all suppliers - API returns paginated response with 'content' array
        const response = await axios.get('/api/v1/supply-chain/suppliers');
        const responseData = response.data;

        // Handle paginated response (Spring Page object) or direct array
        let allSuppliers = [];
        if (Array.isArray(responseData)) {
          allSuppliers = responseData;
        } else if (responseData?.content && Array.isArray(responseData.content)) {
          allSuppliers = responseData.content;
        } else if (responseData?.data && Array.isArray(responseData.data)) {
          allSuppliers = responseData.data;
        } else if (responseData?.data?.content && Array.isArray(responseData.data.content)) {
          allSuppliers = responseData.data.content;
        }

        // Fetch connected suppliers for this exporter
        if (this.exporterId) {
          try {
            const connectedResponse = await axios.get(`/api/v1/supply-chain/exporter/${this.exporterId}/connected-suppliers`);
            const connectedData = connectedResponse.data;

            // Handle different response formats for connected suppliers
            let connectedSuppliers = [];
            if (Array.isArray(connectedData)) {
              connectedSuppliers = connectedData;
            } else if (connectedData?.content && Array.isArray(connectedData.content)) {
              connectedSuppliers = connectedData.content;
            } else if (connectedData?.data && Array.isArray(connectedData.data)) {
              connectedSuppliers = connectedData.data;
            }

            const connectedIds = new Set(connectedSuppliers.map((s) => s.id));

            this.suppliers = allSuppliers.map((supplier) => ({
              ...supplier,
              isConnected: connectedIds.has(supplier.id),
            }));
          } catch (err) {
            // If connection fetch fails, show all as not connected
            this.suppliers = allSuppliers.map((supplier) => ({
              ...supplier,
              isConnected: false,
            }));
          }
        } else {
          this.suppliers = allSuppliers.map((supplier) => ({
            ...supplier,
            isConnected: false,
          }));
        }

        // Sort connected first
        this.suppliers.sort((a, b) => {
          if (a.isConnected && !b.isConnected) return -1;
          if (!a.isConnected && b.isConnected) return 1;
          return (a.supplierName || '').localeCompare(b.supplierName || '');
        });
      } catch (error) {
        this.$toast.error('Failed to load suppliers');
        this.suppliers = [];
      } finally {
        this.loading = false;
      }
    },
    async fetchPendingInvites() {
      try {
        const response = await axios.get('/api/v1/supply-chain/invites/pending');
        const responseData = response.data;

        // Handle different response formats
        if (Array.isArray(responseData)) {
          this.pendingInvites = responseData;
        } else if (responseData?.data && Array.isArray(responseData.data)) {
          this.pendingInvites = responseData.data;
        } else if (responseData?.content && Array.isArray(responseData.content)) {
          this.pendingInvites = responseData.content;
        } else {
          this.pendingInvites = [];
        }
      } catch (error) {
        this.$toast.error('Failed to load pending invites');
        this.pendingInvites = [];
      }
    },
    openAddDialog() {
      this.formData = {
        supplierName: '',
        supplierType: 'AGGREGATOR',
        email: '',
        phone: '',
        countryCode: 'KE',
        region: '',
        address: '',
        autoConnect: true,
      };
      this.showAddDialog = true;
    },
    closeAddDialog() {
      this.showAddDialog = false;
    },
    openInviteDialog() {
      this.inviteData = {
        email: '',
        supplierName: '',
        supplierType: null,
        message: '',
      };
      this.showInviteDialog = true;
    },
    closeInviteDialog() {
      this.showInviteDialog = false;
    },
    async saveSupplier() {
      if (!this.$refs.supplierForm.validate()) return;

      this.saving = true;
      try {
        const payload = {
          supplierName: this.formData.supplierName,
          supplierType: this.formData.supplierType,
          email: this.formData.email,
          phoneNumber: this.formData.phone,
          countryCode: this.formData.countryCode,
          region: this.formData.region,
          address: this.formData.address,
        };

        const response = await axios.post('/api/v1/supply-chain/suppliers', payload);
        const newSupplier = response.data?.data || response.data;

        this.$toast.success('Supplier added successfully');

        // Auto-connect if requested
        if (this.formData.autoConnect && newSupplier?.id) {
          await this.connectToSupplier(newSupplier.id);
        }

        this.closeAddDialog();
        await this.fetchSuppliers();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to add supplier');
      } finally {
        this.saving = false;
      }
    },
    async sendInvite() {
      if (!this.$refs.inviteForm.validate()) return;

      this.sendingInvite = true;
      try {
        const payload = {
          email: this.inviteData.email,
          supplierName: this.inviteData.supplierName,
          supplierType: this.inviteData.supplierType,
          message: this.inviteData.message,
          exporterId: this.exporterId,
        };

        await axios.post('/api/v1/supply-chain/invites', payload);
        this.$toast.success(`Invitation sent to ${this.inviteData.email}`);
        this.closeInviteDialog();
        await this.fetchPendingInvites();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to send invitation');
      } finally {
        this.sendingInvite = false;
      }
    },
    async connectToSupplier(supplierId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can connect to suppliers');
        return;
      }

      this.connectingId = supplierId;
      try {
        await axios.post('/api/v1/supply-chain/connections', {
          sourceSupplierId: supplierId,
          targetSupplierId: this.exporterId, // Exporter as target
          connectionType: 'SUPPLY_AGREEMENT',
        });
        this.$toast.success('Successfully connected to supplier');
        await this.fetchSuppliers();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to connect to supplier');
      } finally {
        this.connectingId = null;
      }
    },
    async disconnectFromSupplier(supplierId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can disconnect from suppliers');
        return;
      }

      this.disconnectingId = supplierId;
      try {
        await axios.delete(`/api/v1/supply-chain/connections/${supplierId}/${this.exporterId}`);
        this.$toast.success('Successfully disconnected from supplier');
        await this.fetchSuppliers();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to disconnect from supplier');
      } finally {
        this.disconnectingId = null;
      }
    },
    viewSupplierDetails(supplier) {
      this.selectedSupplier = supplier;
      this.showDetailsDialog = true;
    },
    clearFilters() {
      this.searchQuery = '';
      this.supplierTypeFilter = null;
      this.verificationFilter = null;
    },
    getSupplierColor(type) {
      const colors = {
        FARMER: 'green',
        FARMER_GROUP: 'light-green',
        COOPERATIVE: 'teal',
        AGGREGATOR: 'orange',
        TRADER: 'amber',
        PROCESSOR: 'indigo',
        DISTRIBUTOR: 'blue',
        WAREHOUSE: 'blue-grey',
        TRANSPORTER: 'cyan',
        EXPORTER: 'purple',
        IMPORTER: 'deep-purple',
        CERTIFICATION_BODY: 'pink',
        OTHER: 'grey',
      };
      return colors[type] || 'grey';
    },
    getSupplierIcon(type) {
      const icons = {
        FARMER: 'mdi-sprout',
        FARMER_GROUP: 'mdi-account-group',
        COOPERATIVE: 'mdi-handshake',
        AGGREGATOR: 'mdi-warehouse',
        TRADER: 'mdi-swap-horizontal',
        PROCESSOR: 'mdi-factory',
        DISTRIBUTOR: 'mdi-truck-delivery',
        WAREHOUSE: 'mdi-home-variant',
        TRANSPORTER: 'mdi-truck',
        EXPORTER: 'mdi-airplane-takeoff',
        IMPORTER: 'mdi-airplane-landing',
        CERTIFICATION_BODY: 'mdi-certificate',
        OTHER: 'mdi-help-circle',
      };
      return icons[type] || 'mdi-help-circle';
    },
    formatSupplierType(type) {
      if (!type) return 'Unknown';
      return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    },
    formatStatus(status) {
      if (!status) return 'Unknown';
      return status.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    },
    getVerificationColor(status) {
      const colors = {
        VERIFIED: 'success',
        PENDING: 'warning',
        UNDER_REVIEW: 'info',
        DOCUMENTS_SUBMITTED: 'info',
        REJECTED: 'error',
        SUSPENDED: 'error',
        EXPIRED: 'grey',
      };
      return colors[status] || 'grey';
    },
  },
};
</script>

<style scoped>
.v-data-table {
  border-radius: 8px;
}
</style>
