<template>
  <Default>
    <!-- Modern Minimalist Design -->
    <div class="tw-min-h-screen tw-bg-white tw-p-8">
      <!-- Sleek Header -->
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-12">
        <h1 class="tw-text-3xl tw-font-light tw-text-gray-900">Supply Chain Workflow Builder</h1>
        <v-btn
          color="primary"
          depressed
          @click="openNewWorkflow"
          class="tw-rounded-lg"
        >
          <v-icon left>mdi-plus</v-icon>
          Create Workflow
        </v-btn>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="tw-flex tw-items-center tw-justify-center tw-py-32">
        <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
      </div>

      <!-- Empty State -->
      <div v-else-if="!workflows.length" class="tw-text-center tw-py-32">
        <v-icon size="80" color="grey lighten-3">mdi-transit-connection-variant</v-icon>
        <p class="tw-text-gray-400 tw-mt-6 tw-text-lg">No workflows yet</p>
        <p class="tw-text-gray-400 tw-text-sm tw-mt-2">Create your first workflow to track produce from farm to shipment</p>
      </div>

      <!-- Workflows Grid -->
      <div v-else class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
        <div
          v-for="workflow in workflows"
          :key="workflow.id"
          @click="selectWorkflow(workflow)"
          class="tw-bg-white tw-rounded-xl tw-p-6 tw-border tw-border-gray-200 tw-cursor-pointer tw-transition-all hover:tw-shadow-lg hover:tw-border-primary"
        >
          <!-- Workflow Header -->
          <div class="tw-flex tw-items-start tw-justify-between tw-mb-4">
            <div class="tw-flex-1">
              <p class="tw-text-xs tw-text-gray-400 tw-uppercase tw-tracking-wider tw-mb-1">{{ workflow.produceType }}</p>
              <h3 class="tw-text-lg tw-font-medium tw-text-gray-900">{{ workflow.workflowName }}</h3>
            </div>
            <v-chip
              small
              :color="getStatusColor(workflow.status)"
              text-color="white"
            >
              {{ workflow.status }}
            </v-chip>
          </div>

          <!-- Stage Badge & Certificate Status -->
          <div class="tw-mb-4 tw-flex tw-items-center tw-gap-2">
            <v-chip small outlined :color="getStageColor(workflow.currentStage)">
              <v-icon left small>{{ getStageIcon(workflow.currentStage) }}</v-icon>
              {{ workflow.currentStage }}
            </v-chip>
            <v-chip
              v-if="workflow.certificateStatus"
              x-small
              :color="getCertificateStatusColor(workflow.certificateStatus)"
              text-color="white"
            >
              <v-icon left x-small>{{ getCertificateStatusIcon(workflow.certificateStatus) }}</v-icon>
              {{ workflow.certificateStatus === 'NOT_CREATED' ? 'No Cert' : workflow.certificateStatus }}
            </v-chip>
          </div>

          <!-- Quantity Info -->
          <div class="tw-grid tw-grid-cols-2 tw-gap-4 tw-mb-4">
            <div>
              <p class="tw-text-xs tw-text-gray-500">Total Collected</p>
              <p class="tw-text-lg tw-font-semibold tw-text-gray-900">{{ workflow.totalCollected.toFixed(0) }} kg</p>
            </div>
            <div>
              <p class="tw-text-xs tw-text-gray-500">Total Shipped</p>
              <p class="tw-text-lg tw-font-semibold tw-text-gray-900">{{ workflow.totalShipped.toFixed(0) }} kg</p>
            </div>
          </div>

          <!-- Event Counts -->
          <div class="tw-flex tw-items-center tw-gap-4 tw-mb-4 tw-text-xs tw-text-gray-500">
            <span><v-icon x-small class="tw-mr-1">mdi-basket</v-icon>{{ workflow.collectionEventCount }}</span>
            <span><v-icon x-small class="tw-mr-1">mdi-package-variant</v-icon>{{ workflow.consolidationEventCount }}</span>
            <span><v-icon x-small class="tw-mr-1">mdi-cog</v-icon>{{ workflow.processingEventCount }}</span>
            <span><v-icon x-small class="tw-mr-1">mdi-truck</v-icon>{{ workflow.shipmentEventCount }}</span>
          </div>

          <!-- Workflow Footer -->
          <div class="tw-flex tw-items-center tw-justify-between">
            <span class="tw-text-xs tw-text-gray-400">{{ formatDate(workflow.createdAt) }}</span>
            <v-btn
              x-small
              text
              color="primary"
            >
              <v-icon left x-small>mdi-arrow-right</v-icon>
              View
            </v-btn>
          </div>
        </div>
      </div>

      <!-- Workflow Details Dialog -->
      <v-dialog v-model="showWorkflowDetails" fullscreen scrollable>
        <v-card v-if="selectedWorkflow" class="workflow-dialog-card">
          <!-- Modern Gradient Header -->
          <div class="workflow-dialog-header">
            <div class="tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-4">
              <!-- Left Section: Title & Produce Type -->
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-12 tw-h-12 tw-rounded-xl tw-bg-white/20 tw-backdrop-blur-sm tw-flex tw-items-center tw-justify-center">
                  <v-icon color="white" size="28">mdi-transit-connection-variant</v-icon>
                </div>
                <div>
                  <h1 class="tw-text-xl tw-font-bold tw-text-white tw-mb-1">{{ selectedWorkflow.workflowName }}</h1>
                  <div class="tw-flex tw-items-center tw-gap-2">
                    <v-chip x-small color="white" text-color="primary" class="tw-font-semibold">
                      {{ selectedWorkflow.produceType }}
                    </v-chip>
                    <span class="tw-text-white/70 tw-text-sm">•</span>
                    <span class="tw-text-white/70 tw-text-sm">{{ formatDate(selectedWorkflow.createdAt) }}</span>
                  </div>
                </div>
              </div>

              <!-- Center Section: Progress Steps -->
              <div class="tw-hidden lg:tw-flex tw-items-center tw-gap-2 tw-bg-white/10 tw-backdrop-blur-sm tw-rounded-full tw-px-4 tw-py-2">
                <div
                  v-for="(stage, idx) in ['Collection', 'Consolidation', 'Processing', 'Shipment']"
                  :key="stage"
                  class="tw-flex tw-items-center"
                >
                  <div
                    class="tw-w-8 tw-h-8 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-text-xs tw-font-bold tw-transition-all"
                    :class="getProgressBadgeClass(stage, selectedWorkflow.currentStage)"
                  >
                    <v-icon v-if="isStageCompleted(stage, selectedWorkflow.currentStage)" x-small color="white">mdi-check</v-icon>
                    <span v-else>{{ idx + 1 }}</span>
                  </div>
                  <div
                    v-if="idx < 3"
                    class="tw-w-8 tw-h-0.5 tw-mx-1"
                    :class="isStageCompleted(stage, selectedWorkflow.currentStage) ? 'tw-bg-green-400' : 'tw-bg-white/30'"
                  ></div>
                </div>
              </div>

              <!-- Right Section: Stats & Actions -->
              <div class="tw-flex tw-items-center tw-gap-6">
                <!-- Quantity Stats -->
                <div class="tw-hidden md:tw-flex tw-items-center tw-gap-4">
                  <div class="tw-text-center tw-px-3 tw-py-1 tw-bg-white/10 tw-rounded-lg">
                    <p class="tw-text-2xl tw-font-bold tw-text-white">{{ selectedWorkflow.totalCollected.toFixed(0) }}</p>
                    <p class="tw-text-xs tw-text-white/70">kg collected</p>
                  </div>
                  <div class="tw-text-center tw-px-3 tw-py-1 tw-bg-white/10 tw-rounded-lg">
                    <p class="tw-text-2xl tw-font-bold tw-text-white">{{ (selectedWorkflow.totalProcessed || 0).toFixed(0) }}</p>
                    <p class="tw-text-xs tw-text-white/70">kg processed</p>
                  </div>
                  <div class="tw-text-center tw-px-3 tw-py-1 tw-bg-white/10 tw-rounded-lg">
                    <p class="tw-text-2xl tw-font-bold tw-text-white">{{ selectedWorkflow.totalShipped.toFixed(0) }}</p>
                    <p class="tw-text-xs tw-text-white/70">kg shipped</p>
                  </div>
                </div>

                <!-- Certificate Action -->
                <v-btn
                  v-if="selectedWorkflow.certificateStatus === 'NOT_CREATED' && canIssueCertificate(selectedWorkflow)"
                  color="white"
                  depressed
                  @click.stop="openCertificateDialog"
                  class="tw-text-primary tw-font-semibold"
                >
                  <v-icon left>mdi-certificate</v-icon>
                  Issue Certificate
                </v-btn>
                <v-btn
                  v-else-if="canTransferCertificate(selectedWorkflow)"
                  color="white"
                  outlined
                  @click.stop="openTransferDialog"
                >
                  <v-icon left>mdi-swap-horizontal</v-icon>
                  Transfer
                </v-btn>
                <v-chip
                  v-else-if="selectedWorkflow.certificateStatus && selectedWorkflow.certificateStatus !== 'NOT_CREATED'"
                  color="white"
                  :text-color="getCertificateStatusColor(selectedWorkflow.certificateStatus)"
                  class="tw-font-semibold"
                >
                  <v-icon left small>{{ getCertificateStatusIcon(selectedWorkflow.certificateStatus) }}</v-icon>
                  {{ selectedWorkflow.certificateStatus }}
                </v-chip>

                <!-- Close Button -->
                <v-btn
                  icon
                  @click="showWorkflowDetails = false"
                  class="tw-bg-white/10 hover:tw-bg-white/20"
                >
                  <v-icon color="white">mdi-close</v-icon>
                </v-btn>
              </div>
            </div>
          </div>

          <!-- Canvas - Full Width Content Area -->
          <v-card-text class="workflow-dialog-content tw-p-4 tw-h-full" style="height: calc(100vh - 100px); overflow: hidden;">
            <div class="tw-w-full tw-h-full">

              <!-- EUDR Compliance Stage Progress -->
              <EudrStageProgress
                v-if="selectedWorkflow"
                :workflow-id="selectedWorkflow.id"
                :key="selectedWorkflow.id"
                @stage-advanced="handleStageAdvanced"
                @stage-reverted="handleStageReverted"
                @risk-assessed="handleRiskAssessed"
                @dds-generated="handleDDSGenerated"
                @current-stage-changed="handleCurrentStageChanged"
                @error="showSnackbar($event, 'error')"
              />
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <!-- New Workflow Dialog - Minimalist -->
      <v-dialog v-model="showNewWorkflowDialog" max-width="500" persistent>
        <v-card class="tw-rounded-xl">
          <v-card-title class="tw-text-xl tw-font-medium tw-px-6 tw-py-4">
            Create New Workflow
          </v-card-title>
          <v-divider></v-divider>
          <v-card-text class="tw-px-6 tw-py-6">
            <v-form ref="workflowForm" v-model="formValid">
              <v-select
                v-model="newWorkflow.produceType"
                :items="produceTypes"
                label="Produce Type"
                :rules="[rules.required]"
                outlined
                dense
                class="tw-mb-4"
                @change="generateWorkflowName"
              ></v-select>
              <v-text-field
                v-model="newWorkflow.workflowName"
                label="Workflow Name"
                placeholder="Auto-generated based on produce type"
                :rules="[rules.required]"
                outlined
                dense
                hint="You can edit this name if needed"
                persistent-hint
              ></v-text-field>
              <p class="tw-text-xs tw-text-gray-500 tw-mt-4">
                You'll add collection events, aggregators, processors, and shipment details after creating the workflow.
              </p>
            </v-form>
          </v-card-text>
          <v-divider></v-divider>
          <v-card-actions class="tw-px-6 tw-py-4">
            <v-spacer></v-spacer>
            <v-btn text @click="showNewWorkflowDialog = false" class="tw-mr-2">Cancel</v-btn>
            <v-btn color="primary" depressed @click="createWorkflow" :loading="creating" :disabled="!formValid" class="tw-rounded-lg">
              Create Workflow
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Certificate Issuance Dialog -->
      <v-dialog v-model="showCertificateDialog" max-width="700px" persistent>
        <v-card>
          <v-card-title class="primary white--text">
            <v-icon left color="white">mdi-certificate</v-icon>
            Issue EUDR Compliance Certificate
          </v-card-title>

          <v-card-text class="pt-4">
            <!-- Step 1: Pre-issuance Review -->
            <div v-if="certificateStep === 1 && selectedWorkflow">
              <v-alert type="info" outlined>
                Review the workflow compliance status before issuing the certificate.
              </v-alert>

              <v-simple-table class="mt-4">
                <tbody>
                  <tr>
                    <td><strong>Workflow Name:</strong></td>
                    <td>{{ selectedWorkflow.workflowName }}</td>
                  </tr>
                  <tr>
                    <td><strong>Produce Type:</strong></td>
                    <td>{{ selectedWorkflow.produceType }}</td>
                  </tr>
                  <tr>
                    <td><strong>Total Collected:</strong></td>
                    <td>{{ selectedWorkflow.totalCollected.toFixed(2) }} kg</td>
                  </tr>
                  <tr>
                    <td><strong>Collection Events:</strong></td>
                    <td>
                      <v-chip small :color="selectedWorkflow.collectionEventCount > 0 ? 'success' : 'error'">
                        {{ selectedWorkflow.collectionEventCount }} events
                      </v-chip>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Consolidation Events:</strong></td>
                    <td>
                      <v-chip small :color="selectedWorkflow.consolidationEventCount > 0 ? 'info' : 'grey'" outlined>
                        {{ selectedWorkflow.consolidationEventCount || 0 }} transfers
                      </v-chip>
                      <span v-if="selectedWorkflow.consolidationEventCount === 0" class="text-caption grey--text ml-2">(Optional)</span>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Processing Events:</strong></td>
                    <td>
                      <v-chip small :color="selectedWorkflow.processingEventCount > 0 ? 'purple' : 'grey'" outlined>
                        {{ selectedWorkflow.processingEventCount || 0 }} events
                      </v-chip>
                      <span v-if="selectedWorkflow.processingEventCount === 0" class="text-caption grey--text ml-2">(Optional)</span>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Production Units:</strong></td>
                    <td>
                      <v-chip small color="teal" outlined>
                        {{ selectedWorkflow.linkedProductionUnits || 0 }} farms linked
                      </v-chip>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Compliance Status:</strong></td>
                    <td>
                      <v-chip small :color="canIssueCertificate(selectedWorkflow) ? 'success' : 'warning'">
                        {{ canIssueCertificate(selectedWorkflow) ? 'Ready' : 'Pending' }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-simple-table>

              <v-alert v-if="!canIssueCertificate(selectedWorkflow)" type="warning" outlined class="mt-4">
                <strong>Requirements not met:</strong>
                <ul>
                  <li v-if="selectedWorkflow.collectionEventCount === 0">No collection events recorded</li>
                </ul>
              </v-alert>

              <v-alert type="warning" outlined class="mt-4">
                <strong>Note:</strong> This will mint an NFT certificate on Hedera blockchain. This action cannot be undone.
              </v-alert>
            </div>

            <!-- Step 2: Issuing (Loading) -->
            <div v-if="certificateStep === 2">
              <div class="text-center py-4">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  size="64"
                ></v-progress-circular>
                <p class="mt-4">Minting certificate on Hedera blockchain...</p>
                <p class="text-caption grey--text">This may take 5-10 seconds</p>
              </div>
            </div>

            <!-- Step 3: Success -->
            <div v-if="certificateStep === 3 && certificateData">
              <v-alert type="success" outlined>
                <strong>Certificate Issued Successfully!</strong>
                <p class="mb-0 mt-2">Your EUDR compliance certificate has been minted as an NFT on the Hedera blockchain.</p>
              </v-alert>

              <v-simple-table class="mt-4">
                <tbody>
                  <tr>
                    <td><strong>Transaction ID:</strong></td>
                    <td>
                      <a
                        :href="`https://hashscan.io/testnet/transaction/${certificateData.transactionId}`"
                        target="_blank"
                        class="text-decoration-none"
                      >
                        <code>{{ certificateData.transactionId }}</code>
                        <v-icon x-small color="primary" class="ml-1">mdi-open-in-new</v-icon>
                      </a>
                    </td>
                  </tr>
                  <tr v-if="certificateData.nftTokenId">
                    <td><strong>NFT Token:</strong></td>
                    <td>
                      <a
                        :href="`https://hashscan.io/testnet/token/${certificateData.nftTokenId}`"
                        target="_blank"
                        class="text-decoration-none"
                      >
                        <code>{{ certificateData.nftTokenId }}</code>
                        <v-icon x-small color="primary" class="ml-1">mdi-open-in-new</v-icon>
                      </a>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Serial Number:</strong></td>
                    <td>{{ certificateData.serialNumber }}</td>
                  </tr>
                  <tr>
                    <td><strong>Status:</strong></td>
                    <td>
                      <v-chip small color="success">{{ certificateData.certificateStatus }}</v-chip>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>Issued At:</strong></td>
                    <td>{{ formatDateTime(certificateData.issuedAt) }}</td>
                  </tr>
                </tbody>
              </v-simple-table>

              <div class="tw-flex tw-gap-2 tw-mt-4">
                <v-btn
                  v-if="certificateData.nftTokenId"
                  color="primary"
                  depressed
                  @click="viewNftOnHedera(certificateData.nftTokenId)"
                >
                  <v-icon left>mdi-certificate</v-icon>
                  View NFT Certificate
                </v-btn>
                <v-btn
                  outlined
                  color="primary"
                  @click="viewOnHedera(certificateData.transactionId)"
                >
                  <v-icon left>mdi-swap-horizontal</v-icon>
                  View Transaction
                </v-btn>
              </div>
            </div>

            <!-- Step 4: Error -->
            <div v-if="certificateStep === 4">
              <v-alert type="error" outlined>
                <strong>Failed to Issue Certificate</strong>
                <br>{{ certificateError }}
              </v-alert>

              <!-- Show Hedera account creation option if that's the issue -->
              <v-card v-if="needsHederaAccount" class="mt-4" outlined>
                <v-card-title class="text-subtitle-1">
                  <v-icon left color="primary">mdi-shield-account</v-icon>
                  Hedera Account Required
                </v-card-title>
                <v-card-text>
                  <p class="mb-3">
                    To issue EUDR compliance certificates as NFTs on Hedera blockchain,
                    you need a Hedera account.
                  </p>
                  <v-btn
                    color="primary"
                    @click="createHederaAccount"
                    :loading="creatingAccount"
                    block
                  >
                    <v-icon left>mdi-plus</v-icon>
                    Create Hedera Account Now
                  </v-btn>
                  <p class="text-caption grey--text mt-2">
                    This will create a new blockchain account for your organization.
                    The process takes about 5-10 seconds.
                  </p>
                </v-card-text>
              </v-card>
            </div>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="closeCertificateDialog" :disabled="certificateStep === 2">
              {{ certificateStep === 3 ? 'Close' : 'Cancel' }}
            </v-btn>
            <v-btn
              v-if="certificateStep === 1"
              color="primary"
              :disabled="!canIssueCertificate(selectedWorkflow)"
              @click="issueCertificate"
            >
              Issue Certificate
            </v-btn>
            <v-btn
              v-if="certificateStep === 4"
              color="primary"
              @click="certificateStep = 1"
            >
              Try Again
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Transfer Certificate Dialog -->
      <v-dialog v-model="showTransferDialog" max-width="550px" persistent>
        <v-card>
          <v-card-title class="purple white--text">
            <v-icon left color="white">mdi-swap-horizontal</v-icon>
            Transfer Certificate to Importer
          </v-card-title>

          <v-card-text class="pt-4">
            <!-- Step 1: Select Importer -->
            <div v-if="transferStep === 1">
              <v-alert type="info" outlined class="mb-4">
                Transfer the EUDR compliance certificate NFT to an importer's Hedera account.
                This enables them to verify the produce at customs.
              </v-alert>

              <v-select
                v-model="selectedImporterId"
                :items="availableImporters"
                item-text="name"
                item-value="id"
                label="Select Importer"
                :rules="[v => !!v || 'Please select an importer']"
                outlined
                dense
                :loading="loadingImporters"
                prepend-icon="mdi-account-arrow-right"
              >
                <template #selection="{ item }">
                  <span>{{ item.name }}</span>
                </template>
                <template #item="{ item }">
                  <div>
                    <div class="font-weight-medium">{{ item.name }}</div>
                    <div class="text-caption grey--text">{{ item.country || item.destinationCountry || 'International' }}</div>
                  </div>
                </template>
              </v-select>

              <v-alert v-if="selectedWorkflow" type="warning" outlined class="mt-4">
                <strong>Note:</strong> Once transferred, you will no longer own this certificate NFT.
                The importer will be able to present it at customs.
              </v-alert>
            </div>

            <!-- Step 2: Transferring -->
            <div v-if="transferStep === 2" class="text-center py-4">
              <v-progress-circular
                indeterminate
                color="purple"
                size="64"
              ></v-progress-circular>
              <p class="mt-4">Transferring certificate on Hedera blockchain...</p>
              <p class="text-caption grey--text">This may take 5-10 seconds</p>
            </div>

            <!-- Step 3: Success -->
            <div v-if="transferStep === 3">
              <v-alert type="success" outlined>
                <strong>Certificate Transferred Successfully!</strong>
                <p class="mb-0 mt-2">The EUDR compliance certificate has been transferred to the importer.</p>
              </v-alert>

              <v-simple-table class="mt-4">
                <tbody>
                  <tr>
                    <td><strong>Importer:</strong></td>
                    <td>{{ getImporterName(selectedImporterId) }}</td>
                  </tr>
                  <tr>
                    <td><strong>New Status:</strong></td>
                    <td>
                      <v-chip small color="purple" text-color="white">
                        TRANSFERRED_TO_IMPORTER
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-simple-table>
            </div>

            <!-- Step 4: Error -->
            <div v-if="transferStep === 4">
              <v-alert type="error" outlined>
                <strong>Transfer Failed</strong>
                <br>{{ transferError }}
              </v-alert>
            </div>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="closeTransferDialog" :disabled="transferStep === 2">
              {{ transferStep === 3 ? 'Close' : 'Cancel' }}
            </v-btn>
            <v-btn
              v-if="transferStep === 1"
              color="purple"
              :disabled="!selectedImporterId"
              :loading="transferringCertificate"
              @click="transferCertificate"
            >
              <v-icon left>mdi-swap-horizontal</v-icon>
              Transfer Certificate
            </v-btn>
            <v-btn
              v-if="transferStep === 4"
              color="primary"
              @click="transferStep = 1"
            >
              Try Again
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Snackbar -->
      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" top right>
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-icon v-if="snackbar.color === 'success'" color="white">mdi-check-circle</v-icon>
          <v-icon v-else-if="snackbar.color === 'error'" color="white">mdi-alert-circle</v-icon>
          <v-icon v-else color="white">mdi-information</v-icon>
          <span class="tw-font-medium">{{ snackbar.message }}</span>
        </div>
      </v-snackbar>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import EudrStageProgress from '@/components/workflow/EudrStageProgress.vue';

export default {
  name: 'SupplyChainWorkflow',
  components: {
    Default,
    EudrStageProgress,
  },
  data() {
    return {
      loading: false,
      creating: false,
      updating: false,
      workflows: [],
      selectedWorkflow: null,
      currentStage: null, // Track current workflow stage

      // Dialogs
      showNewWorkflowDialog: false,
      showWorkflowDetails: false,
      showCertificateDialog: false,

      // Certificate issuance
      certificateStep: 1, // 1: Review, 2: Issuing, 3: Success, 4: Error
      certificateData: null,
      certificateError: null,
      needsHederaAccount: false,
      creatingAccount: false,

      // Certificate transfer
      showTransferDialog: false,
      transferStep: 1, // 1: Select, 2: Transferring, 3: Success, 4: Error
      selectedImporterId: null,
      availableImporters: [],
      loadingImporters: false,
      transferringCertificate: false,
      transferError: null,

      // Form
      formValid: false,
      newWorkflow: {
        workflowName: '',
        produceType: '',
      },

      produceTypes: ['Coffee', 'Tea', 'Cocoa', 'Cotton', 'Maize', 'Beans', 'Rice', 'Soybeans', 'Palm Oil'],

      rules: {
        required: (v) => !!v || 'Required',
        positive: (v) => v > 0 || 'Must be greater than 0',
      },

      snackbar: {
        show: false,
        message: '',
        color: 'success',
      },
    };
  },
  async mounted() {
    await this.loadWorkflows();
  },
  methods: {
    async loadWorkflows() {
      this.loading = true;
      try {
        const exporterId = this.$store.state.auth?.user?.id;

        // Load workflows from backend
        const response = await axios.get(`/api/v1/supply-chain/workflows/exporter/${exporterId}`, {
          params: {
            page: 0,
            size: 100,
          },
        });

        this.workflows = response.data.content || [];
      } catch (error) {
        this.showSnackbar('Failed to load workflows', 'error');
      } finally {
        this.loading = false;
      }
    },

    selectWorkflow(workflow) {
      this.selectedWorkflow = workflow;
      this.showWorkflowDetails = true;
    },

    openNewWorkflow() {
      this.newWorkflow = {
        workflowName: '',
        produceType: '',
      };
      this.showNewWorkflowDialog = true;
    },

    generateWorkflowName() {
      if (!this.newWorkflow.produceType) {
        this.newWorkflow.workflowName = '';
        return;
      }

      // Count existing workflows for this produce type
      const sameProduceWorkflows = this.workflows.filter(
        (w) => w.produceType === this.newWorkflow.produceType,
      );
      const nextNumber = sameProduceWorkflows.length + 1;

      // Get current date for the workflow name
      const now = new Date();
      const month = now.toLocaleString('en-US', { month: 'short' });
      const year = now.getFullYear();

      // Generate name: "Coffee Workflow #3 - Oct 2025"
      this.newWorkflow.workflowName = `${this.newWorkflow.produceType} Workflow #${nextNumber} - ${month} ${year}`;
    },

    async createWorkflow() {
      if (!this.$refs.workflowForm.validate()) return;

      this.creating = true;
      try {
        const exporterId = this.$store.state.auth?.user?.id;

        // Create workflow using the new Supply Chain Workflow API
        const response = await axios.post(`/api/v1/supply-chain/workflows/exporter/${exporterId}`, {
          workflowName: this.newWorkflow.workflowName,
          produceType: this.newWorkflow.produceType,
        });

        // Add the new workflow to the list immediately (no reload needed)
        this.workflows.unshift(response.data);

        // Close dialog and show success
        this.showNewWorkflowDialog = false;
        this.showSnackbar('Workflow created successfully! Now add collection events.', 'success');

        // Open the newly created workflow immediately
        this.selectWorkflow(response.data);
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to create workflow', 'error');
      } finally {
        this.creating = false;
      }
    },

    async refreshData() {
      await this.loadWorkflows();
    },

    getStatusColor(stage) {
      const colors = {
        Collection: 'blue',
        Consolidation: 'orange',
        Processing: 'purple',
        Shipment: 'indigo',
        Completed: 'green',
      };
      return colors[stage] || 'grey';
    },

    getStageColor(stage) {
      const colors = {
        COLLECTION: 'blue',
        CONSOLIDATION: 'orange',
        PROCESSING: 'purple',
        SHIPMENT: 'indigo',
        COMPLETED: 'green',
      };
      return colors[stage] || 'grey';
    },

    getStageIcon(stage) {
      const icons = {
        Collection: 'mdi-basket',
        Consolidation: 'mdi-package-variant',
        Processing: 'mdi-cog',
        Shipment: 'mdi-truck-delivery',
        Completed: 'mdi-check-circle',
      };
      return icons[stage] || 'mdi-circle';
    },

    getProgressStageColor(stage, currentStage) {
      const stages = ['Collection', 'Consolidation', 'Processing', 'Shipment', 'Completed'];
      const stageIndex = stages.indexOf(stage);
      const currentIndex = stages.indexOf(currentStage);

      if (stageIndex < currentIndex) {
        return 'success'; // Completed
      }
      if (stageIndex === currentIndex) {
        return 'primary'; // Current
      }
      return 'grey lighten-2'; // Future
    },

    isStageActive(stage, currentStage) {
      return stage === currentStage;
    },

    isStageCompleted(stage, currentStage) {
      const stages = ['Collection', 'Consolidation', 'Processing', 'Shipment', 'Completed'];
      const stageIndex = stages.indexOf(stage);
      const currentIndex = stages.indexOf(currentStage);
      return stageIndex < currentIndex;
    },

    getProgressBadgeClass(stage, currentStage) {
      if (this.isStageCompleted(stage, currentStage)) {
        return 'progress-badge-completed';
      }
      if (this.isStageActive(stage, currentStage)) {
        return 'progress-badge-active';
      }
      return 'progress-badge-pending';
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    downloadCertificate(workflow) {
      // TODO: Implement certificate download
      this.showSnackbar(`Certificate download for ${workflow.name} coming soon`, 'info');
    },

    canIssueCertificate(workflow) {
      // Certificate can be issued if workflow has collection events
      return workflow && workflow.collectionEventCount > 0;
    },

    canTransferCertificate(workflow) {
      if (!workflow) return false;
      // Can transfer when certificate is COMPLIANT or IN_TRANSIT
      return workflow.certificateStatus === 'COMPLIANT' || workflow.certificateStatus === 'IN_TRANSIT';
    },

    async openTransferDialog() {
      this.showTransferDialog = true;
      this.transferStep = 1;
      this.selectedImporterId = null;
      this.transferError = null;
      await this.loadImporters();
    },

    closeTransferDialog() {
      this.showTransferDialog = false;
      this.transferStep = 1;
      this.selectedImporterId = null;
      this.transferError = null;
    },

    async loadImporters() {
      this.loadingImporters = true;
      try {
        const exporterId = this.$store.state.auth?.user?.id;

        // Load connected importers for this exporter
        const response = await axios.get('/api/v1/importers/connected', {
          params: {
            exporterId,
            page: 0,
            size: 100,
          },
        });

        const importerList = response.data.content || response.data || [];
        this.availableImporters = importerList.map((i) => ({
          id: i.id,
          name: i.businessName || i.companyName || i.name || `Importer ${i.id?.substring(0, 8)}`,
          country: i.destinationCountry || i.country,
          port: i.destinationPort,
        }));
      } catch (error) {
        this.$toast.error('Failed to load importers:', error.message);
        this.availableImporters = [];
        this.showSnackbar('Failed to load importers', 'error');
      } finally {
        this.loadingImporters = false;
      }
    },

    getImporterName(importerId) {
      const importer = this.availableImporters.find((i) => i.id === importerId);
      return importer?.name || 'Unknown Importer';
    },

    async transferCertificate() {
      if (!this.selectedWorkflow || !this.selectedImporterId) return;

      this.transferringCertificate = true;
      this.transferStep = 2;

      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/transfer-certificate`,
          null,
          {
            params: {
              importerId: this.selectedImporterId,
            },
          },
        );

        if (response.data.success) {
          this.transferStep = 3;
          this.showSnackbar('Certificate transferred successfully!', 'success');

          // Update local workflow state
          if (this.selectedWorkflow) {
            this.selectedWorkflow.certificateStatus = 'TRANSFERRED_TO_IMPORTER';
          }

          // Refresh workflows list
          await this.loadWorkflows();
        } else {
          this.transferError = response.data.message || 'Transfer failed';
          this.transferStep = 4;
        }
      } catch (error) {
        this.$toast.error('Failed to transfer certificate:', error.message);
        this.transferError = error.response?.data?.message || 'Failed to transfer certificate. Please try again.';
        this.transferStep = 4;
        this.showSnackbar('Failed to transfer certificate', 'error');
      } finally {
        this.transferringCertificate = false;
      }
    },

    openCertificateDialog() {
      this.showCertificateDialog = true;
      this.certificateStep = 1;
      this.certificateError = null;
    },

    async issueCertificate() {
      if (!this.selectedWorkflow) return;

      this.certificateStep = 2; // Show loading
      this.certificateError = null;
      this.needsHederaAccount = false;

      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/issue-certificate`,
        );

        if (response.data.success) {
          // Backend returns async response - certificate is being minted in background
          // Start polling for certificate completion
          const workflowId = this.selectedWorkflow.id;
          this.showSnackbar('Certificate minting initiated...', 'info');

          // Poll for certificate completion
          const maxAttempts = 30; // Max 30 attempts = 60 seconds
          let attempts = 0;

          const pollForCertificate = async () => {
            // eslint-disable-next-line no-plusplus
            attempts++;
            try {
              // Fetch updated workflow status
              const statusResponse = await axios.get(
                `/api/v1/supply-chain/workflows/${workflowId}`,
              );
              const workflow = statusResponse.data;

              if (workflow.certificateStatus === 'COMPLIANT') {
                // Certificate issued successfully
                this.certificateData = {
                  transactionId: workflow.complianceCertificateTransactionId,
                  serialNumber: workflow.complianceCertificateSerialNumber,
                  nftTokenId: workflow.complianceCertificateNftId,
                  certificateStatus: workflow.certificateStatus,
                  issuedAt: workflow.certificateIssuedAt,
                };
                this.certificateStep = 3; // Show success

                // Update local workflow
                this.selectedWorkflow.certificateStatus = workflow.certificateStatus;
                this.selectedWorkflow.complianceCertificateSerialNumber = workflow.complianceCertificateSerialNumber;
                this.selectedWorkflow.complianceCertificateTransactionId = workflow.complianceCertificateTransactionId;
                this.selectedWorkflow.complianceCertificateNftId = workflow.complianceCertificateNftId;
                this.selectedWorkflow.certificateIssuedAt = workflow.certificateIssuedAt;

                // Refresh workflows list
                await this.loadWorkflows();

                this.showSnackbar('Certificate issued successfully!', 'success');
              } else if (workflow.certificateStatus === 'NOT_CREATED' && attempts > 3) {
                // Certificate issuance failed (status reverted from PENDING_VERIFICATION)
                this.certificateError = 'Certificate minting failed on blockchain. Please try again.';
                this.certificateStep = 4; // Show error
              } else if (attempts >= maxAttempts) {
                // Timeout - certificate still pending
                this.certificateError = 'Certificate minting is taking longer than expected. Please refresh the page to check status.';
                this.certificateStep = 4; // Show error
              } else {
                // Still pending - continue polling
                setTimeout(pollForCertificate, 2000); // Poll every 2 seconds
              }
            } catch (pollError) {
              this.$toast.error('Error polling certificate status:', pollError.message);
              if (attempts >= maxAttempts) {
                this.certificateError = 'Failed to check certificate status. Please refresh.';
                this.certificateStep = 4;
              } else {
                setTimeout(pollForCertificate, 2000);
              }
            }
          };

          // Start polling after a short delay (give blockchain time to process)
          setTimeout(pollForCertificate, 3000);
        } else {
          this.certificateError = response.data.message;
          this.certificateStep = 4; // Show error
        }
      } catch (error) {
        this.$toast.error('Failed to issue certificate:', error.message);
        const errorData = error.response?.data;
        this.certificateError = errorData?.message || 'Failed to issue certificate. Please try again.';

        // Check if error is due to missing Hedera account
        this.needsHederaAccount = errorData?.actionRequired === 'CREATE_HEDERA_ACCOUNT';

        this.certificateStep = 4; // Show error
        this.showSnackbar('Failed to issue certificate', 'error');
      }
    },

    async createHederaAccount() {
      if (!this.selectedWorkflow) return;

      this.creatingAccount = true;

      try {
        const exporterId = this.$store.state.auth?.user?.id;
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/exporter/${exporterId}/hedera-account`,
        );

        if (response.data.success) {
          this.showSnackbar('Hedera account created successfully!', 'success');

          // Show success message with account details
          this.$toast.success(
            `Hedera Account Created: ${response.data.data.hederaAccountId}`,
            { duration: 8000 },
          );

          // Close certificate dialog and allow retry
          this.closeCertificateDialog();

          // Optionally reopen the dialog after a short delay
          setTimeout(() => {
            this.openCertificateDialog();
          }, 1000);
        }
      } catch (error) {
        this.$toast.error('Failed to create Hedera account:', error.message);
        const errorMessage = error.response?.data?.message || 'Failed to create Hedera account';
        this.showSnackbar(errorMessage, 'error');
      } finally {
        this.creatingAccount = false;
      }
    },

    closeCertificateDialog() {
      this.showCertificateDialog = false;
      this.certificateStep = 1;
      this.certificateData = null;
      this.certificateError = null;
    },

    viewOnHedera(transactionId) {
      if (transactionId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/transaction/${transactionId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },

    viewNftOnHedera(nftTokenId) {
      if (nftTokenId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/token/${nftTokenId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },

    getCertificateStatusColor(status) {
      const colors = {
        NOT_CREATED: 'grey',
        PENDING_VERIFICATION: 'warning',
        COMPLIANT: 'success',
        IN_TRANSIT: 'info',
        TRANSFERRED_TO_IMPORTER: 'purple',
        CUSTOMS_VERIFIED: 'teal',
        DELIVERED: 'success',
        FROZEN: 'error',
        EXPIRED: 'grey darken-2',
      };
      return colors[status] || 'grey';
    },

    getCertificateStatusIcon(status) {
      const icons = {
        NOT_CREATED: 'mdi-file-document-outline',
        PENDING_VERIFICATION: 'mdi-clock-alert-outline',
        COMPLIANT: 'mdi-check-circle',
        IN_TRANSIT: 'mdi-truck-delivery',
        TRANSFERRED_TO_IMPORTER: 'mdi-swap-horizontal',
        CUSTOMS_VERIFIED: 'mdi-shield-check',
        DELIVERED: 'mdi-package-variant-closed',
        FROZEN: 'mdi-snowflake-alert',
        EXPIRED: 'mdi-clock-remove',
      };
      return icons[status] || 'mdi-help-circle';
    },

    formatDateTime(dateString) {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },

    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
    },

    // EUDR Stage Progress Handlers
    handleStageAdvanced(result) {
      this.showSnackbar(`Advanced to ${result.currentStage}`, 'success');
      // Refresh workflow data
      this.loadWorkflows();
    },

    handleStageReverted(result) {
      this.showSnackbar(`Reverted to ${result.currentStage}`, 'info');
      // Refresh workflow data
      this.loadWorkflows();
    },

    handleRiskAssessed(result) {
      const color = result.classification === 'HIGH' ? 'warning' : 'success';
      this.showSnackbar(`Risk Assessment Complete: ${result.classification}`, color);
    },

    handleDDSGenerated(result) {
      this.showSnackbar(`Due Diligence Statement Generated: ${result.ddsReference}`, 'success');
    },

    handleChainUpdated(chainNodes) {
      // Handle flexible supply chain updates
      this.showSnackbar(`Supply chain updated: ${chainNodes.length} suppliers in chain`, 'success');
      // Refresh workflow list to get updated stats
      this.loadWorkflows();
    },

    handleCurrentStageChanged(stage) {
      // Track the current EUDR compliance stage
      this.currentStage = stage;
    },
  },
};
</script>

<style scoped>
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}

.tw-animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

/* Modern Workflow Dialog Styles */
.workflow-dialog-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.workflow-dialog-header {
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #3d7ab5 100%);
  position: relative;
  overflow: hidden;
}

.workflow-dialog-header::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: shimmer 15s infinite linear;
}

@keyframes shimmer {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.workflow-dialog-content {
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

/* Progress badge classes */
.progress-badge-completed {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.4);
}

.progress-badge-active {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  animation: pulse-soft 2s infinite;
}

.progress-badge-pending {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.7);
}

@keyframes pulse-soft {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.6);
  }
}
</style>
