<template>
  <Default class="mitigation-tracking">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="warning" class="mr-3">mdi-shield-alert</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">Mitigation Tracking</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Manage and track mitigation actions for EUDR compliance risks
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="createWorkflowDialog = true"
              elevation="2"
            >
              <v-icon left>mdi-plus</v-icon>
              New Workflow
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card pending-card" elevation="4" @click="filterByStatus('PENDING')">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-clock-alert</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ statistics.pendingWorkflows }}</div>
              <div class="text-subtitle-2">Pending Workflows</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card progress-card" elevation="4" @click="filterByStatus('IN_PROGRESS')">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-progress-check</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ statistics.inProgressWorkflows }}</div>
              <div class="text-subtitle-2">In Progress</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card completed-card" elevation="4" @click="filterByStatus('COMPLETED')">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-check-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ statistics.completedWorkflows }}</div>
              <div class="text-subtitle-2">Completed</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card high-risk-card" elevation="4" @click="viewHighRiskWorkflows">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-alert-octagon</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ statistics.highRiskWorkflows }}</div>
              <div class="text-subtitle-2">High Risk</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Workflows List -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-checkbox</v-icon>
              Mitigation Workflows
              <v-spacer></v-spacer>
              <v-chip-group v-model="statusFilter" mandatory class="mr-4">
                <v-chip
                  v-for="status in statusOptions"
                  :key="status.value"
                  :value="status.value"
                  filter
                  outlined
                >
                  {{ status.text }}
                </v-chip>
              </v-chip-group>
              <v-text-field
                v-model="search"
                append-icon="mdi-magnify"
                label="Search workflows..."
                single-line
                hide-details
                dense
                outlined
                class="search-field"
              ></v-text-field>
            </v-card-title>
            <v-data-table
              :headers="workflowHeaders"
              :items="filteredWorkflows"
              :search="search"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.batchCode="{ item }">
                <div class="d-flex align-center">
                  <v-icon color="primary" class="mr-2">mdi-package-variant</v-icon>
                  <div>
                    <div class="font-weight-medium">{{ item.batchCode }}</div>
                    <div class="text-caption text--secondary">Batch ID: {{ item.batchId }}</div>
                  </div>
                </div>
              </template>
              <template v-slot:item.riskLevel="{ item }">
                <v-chip
                  :color="getRiskColor(item.riskLevel)"
                  text-color="white"
                  small
                >
                  {{ item.riskLevel }}
                </v-chip>
              </template>
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  text-color="white"
                  small
                >
                  {{ formatStatus(item.status) }}
                </v-chip>
              </template>
              <template v-slot:item.progress="{ item }">
                <div class="d-flex align-center">
                  <v-progress-linear
                    :value="calculateProgress(item)"
                    :color="getProgressColor(item)"
                    height="20"
                    rounded
                    class="mr-2"
                  >
                    <template v-slot:default>
                      <span class="text-caption white--text">
                        {{ item.completedActions }}/{{ item.totalActions }}
                      </span>
                    </template>
                  </v-progress-linear>
                </div>
              </template>
              <template v-slot:item.createdAt="{ item }">
                {{ formatDate(item.createdAt) }}
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewWorkflow(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="success"
                  @click="addAction(item)"
                  v-if="item.status !== 'COMPLETED'"
                >
                  <v-icon>mdi-plus-circle</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="info"
                  @click="viewOnHedera(item)"
                  v-if="item.hederaTransactionId"
                >
                  <v-icon>mdi-alpha-h-circle</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Create Workflow Dialog -->
    <v-dialog v-model="createWorkflowDialog" max-width="800px" persistent>
      <v-card>
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-shield-plus</v-icon>
          Create Mitigation Workflow
        </v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="createForm" v-model="createFormValid">
            <v-select
              v-model="newWorkflow.batchId"
              :items="availableBatches"
              item-text="batchCode"
              item-value="id"
              label="Select Batch"
              outlined
              required
              :rules="[v => !!v || 'Batch is required']"
            ></v-select>
            <v-select
              v-model="newWorkflow.riskLevel"
              :items="riskLevels"
              label="Risk Level"
              outlined
              required
              :rules="[v => !!v || 'Risk level is required']"
            ></v-select>
            <v-divider class="my-4"></v-divider>
            <div class="text-h6 mb-3">Initial Actions (Optional)</div>
            <v-expansion-panels v-model="expandedActions">
              <v-expansion-panel
                v-for="(action, index) in newWorkflow.initialActions"
                :key="index"
              >
                <v-expansion-panel-header>
                  Action {{ index + 1 }}: {{ action.actionType || 'Not specified' }}
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <v-select
                    v-model="action.actionType"
                    :items="actionTypes"
                    label="Action Type"
                    outlined
                    dense
                  ></v-select>
                  <v-textarea
                    v-model="action.description"
                    label="Description"
                    outlined
                    dense
                    rows="3"
                  ></v-textarea>
                  <v-text-field
                    v-model="action.assignedTo"
                    label="Assigned To (User ID)"
                    outlined
                    dense
                  ></v-text-field>
                  <v-menu
                    v-model="action.dueDateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    offset-y
                    min-width="auto"
                  >
                    <template v-slot:activator="{ on, attrs }">
                      <v-text-field
                        v-model="action.dueDate"
                        label="Due Date"
                        prepend-icon="mdi-calendar"
                        readonly
                        outlined
                        dense
                        v-bind="attrs"
                        v-on="on"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="action.dueDate"
                      @input="action.dueDateMenu = false"
                    ></v-date-picker>
                  </v-menu>
                  <v-btn
                    text
                    color="error"
                    @click="removeAction(index)"
                  >
                    <v-icon left>mdi-delete</v-icon>
                    Remove Action
                  </v-btn>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>
            <v-btn
              text
              color="primary"
              @click="addNewAction"
              class="mt-3"
            >
              <v-icon left>mdi-plus</v-icon>
              Add Action
            </v-btn>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="cancelCreateWorkflow">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="createWorkflow"
            :loading="creating"
            :disabled="!createFormValid"
          >
            Create Workflow
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- View Workflow Dialog -->
    <v-dialog v-model="viewWorkflowDialog" max-width="1200px">
      <v-card v-if="selectedWorkflow">
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-shield-check</v-icon>
          Workflow Details: {{ selectedWorkflow.batchCode }}
          <v-spacer></v-spacer>
          <v-chip
            :color="getStatusColor(selectedWorkflow.status)"
            text-color="white"
          >
            {{ formatStatus(selectedWorkflow.status) }}
          </v-chip>
        </v-card-title>
        <v-card-text class="pt-4">
          <v-row>
            <v-col cols="12" md="6">
              <div class="info-item">
                <div class="info-label">Batch Code:</div>
                <div class="info-value">{{ selectedWorkflow.batchCode }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">Risk Level:</div>
                <v-chip
                  :color="getRiskColor(selectedWorkflow.riskLevel)"
                  text-color="white"
                  small
                >
                  {{ selectedWorkflow.riskLevel }}
                </v-chip>
              </div>
              <div class="info-item">
                <div class="info-label">Created:</div>
                <div class="info-value">{{ formatDate(selectedWorkflow.createdAt) }}</div>
              </div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="info-item">
                <div class="info-label">Progress:</div>
                <v-progress-linear
                  :value="calculateProgress(selectedWorkflow)"
                  :color="getProgressColor(selectedWorkflow)"
                  height="25"
                  rounded
                >
                  <template v-slot:default>
                    <span class="white--text">
                      {{ selectedWorkflow.completedActions }}/{{ selectedWorkflow.totalActions }} Actions
                    </span>
                  </template>
                </v-progress-linear>
              </div>
              <div class="info-item" v-if="selectedWorkflow.hederaTransactionId">
                <div class="info-label">Hedera TX:</div>
                <v-chip
                  small
                  color="info"
                  text-color="white"
                  @click="viewOnHedera(selectedWorkflow)"
                >
                  <v-icon left small>mdi-alpha-h-circle</v-icon>
                  {{ selectedWorkflow.hederaTransactionId.substring(0, 20) }}...
                </v-chip>
              </div>
            </v-col>
          </v-row>

          <v-divider class="my-4"></v-divider>

          <div class="text-h6 mb-3">
            Mitigation Actions
            <v-btn
              small
              color="primary"
              class="ml-2"
              @click="showAddActionDialog = true"
              v-if="selectedWorkflow.status !== 'COMPLETED'"
            >
              <v-icon left small>mdi-plus</v-icon>
              Add Action
            </v-btn>
          </div>

          <!-- Actions Kanban Board -->
          <v-row>
            <v-col cols="12" md="4" v-for="status in actionStatuses" :key="status.value">
              <v-card outlined class="kanban-column">
                <v-card-title class="text-subtitle-1" :class="`${status.color}--text`">
                  <v-icon left :color="status.color">{{ status.icon }}</v-icon>
                  {{ status.text }}
                  <v-spacer></v-spacer>
                  <v-chip small :color="status.color" text-color="white">
                    {{ filterActionsByStatus(selectedWorkflow.actions, status.value).length }}
                  </v-chip>
                </v-card-title>
                <v-card-text class="kanban-items">
                  <v-card
                    v-for="action in filterActionsByStatus(selectedWorkflow.actions, status.value)"
                    :key="action.id"
                    class="action-card mb-3"
                    elevation="2"
                    @click="selectAction(action)"
                  >
                    <v-card-subtitle class="pb-0">
                      <v-chip x-small :color="getActionTypeColor(action.actionType)" text-color="white">
                        {{ formatActionType(action.actionType) }}
                      </v-chip>
                    </v-card-subtitle>
                    <v-card-text>
                      <div class="text-body-2 font-weight-medium mb-2">
                        {{ action.description }}
                      </div>
                      <div class="text-caption text--secondary" v-if="action.assignedTo">
                        <v-icon x-small>mdi-account</v-icon> {{ action.assignedTo }}
                      </div>
                      <div class="text-caption text--secondary" v-if="action.dueDate">
                        <v-icon x-small>mdi-calendar</v-icon> Due: {{ formatDate(action.dueDate) }}
                      </div>
                      <div class="text-caption success--text" v-if="action.completedAt">
                        <v-icon x-small color="success">mdi-check</v-icon>
                        Completed: {{ formatDate(action.completedAt) }}
                      </div>
                    </v-card-text>
                  </v-card>
                  <div v-if="filterActionsByStatus(selectedWorkflow.actions, status.value).length === 0"
                       class="text-center text--secondary py-4">
                    No actions
                  </div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>

          <v-divider class="my-4"></v-divider>

          <div v-if="selectedWorkflow.completedAt">
            <div class="text-h6 mb-2">Completion Notes:</div>
            <div class="text-body-2">{{ selectedWorkflow.completionNotes || 'No notes provided' }}</div>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-btn
            v-if="selectedWorkflow.status === 'IN_PROGRESS' && canCompleteWorkflow(selectedWorkflow)"
            color="success"
            @click="showCompleteWorkflowDialog = true"
          >
            <v-icon left>mdi-check-circle</v-icon>
            Complete Workflow
          </v-btn>
          <v-spacer></v-spacer>
          <v-btn text @click="viewWorkflowDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Add Action Dialog -->
    <v-dialog v-model="showAddActionDialog" max-width="600px">
      <v-card>
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-plus-circle</v-icon>
          Add Mitigation Action
        </v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="addActionForm" v-model="addActionFormValid">
            <v-select
              v-model="newAction.actionType"
              :items="actionTypes"
              label="Action Type"
              outlined
              required
              :rules="[v => !!v || 'Action type is required']"
            ></v-select>
            <v-textarea
              v-model="newAction.description"
              label="Description"
              outlined
              required
              rows="4"
              :rules="[v => !!v || 'Description is required']"
            ></v-textarea>
            <v-text-field
              v-model="newAction.assignedTo"
              label="Assigned To (User ID)"
              outlined
            ></v-text-field>
            <v-menu
              v-model="newAction.dueDateMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="auto"
            >
              <template v-slot:activator="{ on, attrs }">
                <v-text-field
                  v-model="newAction.dueDate"
                  label="Due Date"
                  prepend-icon="mdi-calendar"
                  readonly
                  outlined
                  v-bind="attrs"
                  v-on="on"
                ></v-text-field>
              </template>
              <v-date-picker
                v-model="newAction.dueDate"
                @input="newAction.dueDateMenu = false"
                :min="new Date().toISOString().substr(0, 10)"
              ></v-date-picker>
            </v-menu>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showAddActionDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="submitAddAction"
            :loading="addingAction"
            :disabled="!addActionFormValid"
          >
            Add Action
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Action Detail Dialog -->
    <v-dialog v-model="actionDetailDialog" max-width="700px">
      <v-card v-if="selectedAction">
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-clipboard-check</v-icon>
          Action Details
          <v-spacer></v-spacer>
          <v-chip
            :color="getActionStatusColor(selectedAction.status)"
            text-color="white"
          >
            {{ formatActionStatus(selectedAction.status) }}
          </v-chip>
        </v-card-title>
        <v-card-text class="pt-4">
          <div class="info-item">
            <div class="info-label">Action Type:</div>
            <v-chip small :color="getActionTypeColor(selectedAction.actionType)" text-color="white">
              {{ formatActionType(selectedAction.actionType) }}
            </v-chip>
          </div>
          <div class="info-item">
            <div class="info-label">Description:</div>
            <div class="info-value">{{ selectedAction.description }}</div>
          </div>
          <div class="info-item" v-if="selectedAction.assignedTo">
            <div class="info-label">Assigned To:</div>
            <div class="info-value">{{ selectedAction.assignedTo }}</div>
          </div>
          <div class="info-item" v-if="selectedAction.dueDate">
            <div class="info-label">Due Date:</div>
            <div class="info-value">{{ formatDate(selectedAction.dueDate) }}</div>
          </div>
          <div class="info-item">
            <div class="info-label">Created:</div>
            <div class="info-value">{{ formatDate(selectedAction.createdAt) }}</div>
          </div>
          <div class="info-item" v-if="selectedAction.completedAt">
            <div class="info-label">Completed:</div>
            <div class="info-value">{{ formatDate(selectedAction.completedAt) }}</div>
          </div>
          <div class="info-item" v-if="selectedAction.completionEvidence">
            <div class="info-label">Completion Evidence:</div>
            <div class="info-value">{{ selectedAction.completionEvidence }}</div>
          </div>
          <div class="info-item" v-if="selectedAction.hederaTransactionId">
            <div class="info-label">Hedera Transaction:</div>
            <v-chip
              small
              color="info"
              text-color="white"
              @click="viewActionOnHedera(selectedAction)"
            >
              <v-icon left small>mdi-alpha-h-circle</v-icon>
              {{ selectedAction.hederaTransactionId.substring(0, 30) }}...
            </v-chip>
          </div>

          <v-divider class="my-4"></v-divider>

          <div class="text-h6 mb-3">Update Status</div>
          <v-form ref="updateStatusForm" v-model="updateStatusFormValid">
            <v-select
              v-model="updateActionStatus.newStatus"
              :items="actionStatusOptions"
              label="New Status"
              outlined
              required
              :rules="[v => !!v || 'Status is required']"
            ></v-select>
            <v-textarea
              v-if="updateActionStatus.newStatus === 'COMPLETED'"
              v-model="updateActionStatus.completionEvidence"
              label="Completion Evidence"
              outlined
              rows="3"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="actionDetailDialog = false">Close</v-btn>
          <v-btn
            color="primary"
            @click="submitUpdateStatus"
            :loading="updatingStatus"
            :disabled="!updateStatusFormValid || selectedAction.status === updateActionStatus.newStatus"
          >
            Update Status
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Complete Workflow Dialog -->
    <v-dialog v-model="showCompleteWorkflowDialog" max-width="600px">
      <v-card>
        <v-card-title class="success white--text">
          <v-icon left color="white">mdi-check-circle</v-icon>
          Complete Workflow
        </v-card-title>
        <v-card-text class="pt-4">
          <v-alert type="info" outlined class="mb-4">
            All actions have been completed. You can now complete this workflow.
          </v-alert>
          <v-textarea
            v-model="completeWorkflowNotes"
            label="Completion Notes (Optional)"
            outlined
            rows="4"
            hint="Add any final notes or observations about the mitigation workflow"
          ></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showCompleteWorkflowDialog = false">Cancel</v-btn>
          <v-btn
            color="success"
            @click="submitCompleteWorkflow"
            :loading="completingWorkflow"
          >
            Complete Workflow
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </default>
</template>

<script>
/* eslint-disable */
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'MitigationTracking',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      workflows: [],
      statistics: {
        totalWorkflows: 0,
        pendingWorkflows: 0,
        inProgressWorkflows: 0,
        completedWorkflows: 0,
        rejectedWorkflows: 0,
        highRiskWorkflows: 0,
      },
      search: '',
      statusFilter: 'ALL',
      statusOptions: [
        { text: 'All', value: 'ALL' },
        { text: 'Pending', value: 'PENDING' },
        { text: 'In Progress', value: 'IN_PROGRESS' },
        { text: 'Completed', value: 'COMPLETED' },
      ],
      workflowHeaders: [
        { text: 'Batch', value: 'batchCode', sortable: true },
        { text: 'Risk Level', value: 'riskLevel', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Progress', value: 'progress', sortable: false, width: '200px' },
        { text: 'Created', value: 'createdAt', sortable: true },
        { text: 'Actions', value: 'actions', sortable: false, width: '150px' },
      ],
      createWorkflowDialog: false,
      createFormValid: false,
      creating: false,
      newWorkflow: {
        batchId: null,
        riskLevel: null,
        initialActions: [],
      },
      expandedActions: [],
      availableBatches: [],
      riskLevels: [
        { text: 'High', value: 'HIGH' },
        { text: 'Medium', value: 'MEDIUM' },
        { text: 'Low', value: 'LOW' },
      ],
      actionTypes: [
        { text: 'Additional Documentation', value: 'ADDITIONAL_DOCUMENTATION' },
        { text: 'Verification Visit', value: 'VERIFICATION_VISIT' },
        { text: 'Batch Hold', value: 'BATCH_HOLD' },
        { text: 'Supplier Questionnaire', value: 'SUPPLIER_QUESTIONNAIRE' },
        { text: 'Site Inspection', value: 'SITE_INSPECTION' },
      ],
      viewWorkflowDialog: false,
      selectedWorkflow: null,
      actionStatuses: [
        { text: 'Pending', value: 'PENDING', color: 'warning', icon: 'mdi-clock-alert' },
        { text: 'In Progress', value: 'IN_PROGRESS', color: 'info', icon: 'mdi-progress-check' },
        { text: 'Completed', value: 'COMPLETED', color: 'success', icon: 'mdi-check-circle' },
      ],
      showAddActionDialog: false,
      addActionFormValid: false,
      addingAction: false,
      newAction: {
        actionType: null,
        description: '',
        assignedTo: null,
        dueDate: null,
        dueDateMenu: false,
      },
      actionDetailDialog: false,
      selectedAction: null,
      updateStatusFormValid: false,
      updatingStatus: false,
      updateActionStatus: {
        newStatus: null,
        completionEvidence: null,
      },
      actionStatusOptions: [
        { text: 'Pending', value: 'PENDING' },
        { text: 'In Progress', value: 'IN_PROGRESS' },
        { text: 'Completed', value: 'COMPLETED' },
        { text: 'Rejected', value: 'REJECTED' },
      ],
      showCompleteWorkflowDialog: false,
      completeWorkflowNotes: '',
      completingWorkflow: false,
    };
  },
  computed: {
    filteredWorkflows() {
      if (this.statusFilter === 'ALL') {
        return this.workflows;
      }
      return this.workflows.filter((w) => w.status === this.statusFilter);
    },
  },
  mounted() {
    this.loadWorkflows();
    this.loadStatistics();
    this.loadAvailableBatches();
  },
  methods: {
    async loadWorkflows() {
      this.loading = true;
      try {
        const response = await axios.get('/api/eudr/mitigation/workflows/my');
        if (response.data.success) {
          this.workflows = response.data.data;
        }
      } catch (error) {
        this.$toast.error('Failed to load workflows');
      } finally {
        this.loading = false;
      }
    },
    async loadStatistics() {
      try {
        const response = await axios.get('/api/eudr/mitigation/statistics');
        if (response.data.success) {
          this.statistics = response.data.data;
        }
      } catch (error) {
      }
    },
    async loadAvailableBatches() {
      try {
        const response = await axios.get('/api/eudr/batches');
        if (response.data.success) {
          this.availableBatches = response.data.data;
        }
      } catch (error) {
      }
    },
    getRiskColor(riskLevel) {
      const colors = {
        HIGH: 'error',
        MEDIUM: 'warning',
        LOW: 'info',
        NONE: 'success',
      };
      return colors[riskLevel] || 'grey';
    },
    getStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        IN_PROGRESS: 'info',
        COMPLETED: 'success',
        REJECTED: 'error',
      };
      return colors[status] || 'grey';
    },
    getProgressColor(workflow) {
      const progress = this.calculateProgress(workflow);
      if (progress === 100) return 'success';
      if (progress >= 50) return 'info';
      return 'warning';
    },
    calculateProgress(workflow) {
      if (workflow.totalActions === 0) return 0;
      return (workflow.completedActions / workflow.totalActions) * 100;
    },
    formatStatus(status) {
      return status.replace(/_/g, ' ');
    },
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString();
    },
    filterByStatus(status) {
      this.statusFilter = status;
    },
    viewHighRiskWorkflows() {
      // Implement filter for high risk workflows
      this.$toast.info('Filtering high-risk workflows');
    },
    viewWorkflow(workflow) {
      this.selectedWorkflow = workflow;
      this.viewWorkflowDialog = true;
    },
    async addAction(workflow) {
      this.selectedWorkflow = workflow;
      this.showAddActionDialog = true;
    },
    viewOnHedera(workflow) {
      if (workflow.hederaTransactionId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/transaction/${workflow.hederaTransactionId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },
    addNewAction() {
      this.newWorkflow.initialActions.push({
        actionType: null,
        description: '',
        assignedTo: null,
        dueDate: null,
        dueDateMenu: false,
      });
      this.expandedActions = this.newWorkflow.initialActions.length - 1;
    },
    removeAction(index) {
      this.newWorkflow.initialActions.splice(index, 1);
    },
    cancelCreateWorkflow() {
      this.createWorkflowDialog = false;
      this.newWorkflow = {
        batchId: null,
        riskLevel: null,
        initialActions: [],
      };
      if (this.$refs.createForm) {
        this.$refs.createForm.reset();
      }
    },
    async createWorkflow() {
      if (!this.$refs.createForm.validate()) return;

      this.creating = true;
      try {
        const response = await axios.post('/api/eudr/mitigation/workflows', this.newWorkflow);
        if (response.data.success) {
          this.$toast.success('Workflow created successfully');
          this.createWorkflowDialog = false;
          this.loadWorkflows();
          this.loadStatistics();
          this.cancelCreateWorkflow();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to create workflow');
      } finally {
        this.creating = false;
      }
    },
    filterActionsByStatus(actions, status) {
      return actions.filter((a) => a.status === status);
    },
    getActionTypeColor(actionType) {
      const colors = {
        ADDITIONAL_DOCUMENTATION: 'blue',
        VERIFICATION_VISIT: 'purple',
        BATCH_HOLD: 'red',
        SUPPLIER_QUESTIONNAIRE: 'teal',
        SITE_INSPECTION: 'orange',
      };
      return colors[actionType] || 'grey';
    },
    formatActionType(actionType) {
      return actionType.replace(/_/g, ' ');
    },
    selectAction(action) {
      this.selectedAction = action;
      this.updateActionStatus = {
        newStatus: action.status,
        completionEvidence: null,
      };
      this.actionDetailDialog = true;
    },
    getActionStatusColor(status) {
      return this.getStatusColor(status);
    },
    formatActionStatus(status) {
      return this.formatStatus(status);
    },
    viewActionOnHedera(action) {
      if (action.hederaTransactionId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/transaction/${action.hederaTransactionId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },
    async submitAddAction() {
      if (!this.$refs.addActionForm.validate()) return;

      this.addingAction = true;
      try {
        const response = await axios.post(
          `/api/eudr/mitigation/workflows/${this.selectedWorkflow.id}/actions`,
          this.newAction
        );
        if (response.data.success) {
          this.$toast.success('Action added successfully');
          this.showAddActionDialog = false;
          this.newAction = {
            actionType: null,
            description: '',
            assignedTo: null,
            dueDate: null,
            dueDateMenu: false,
          };
          // Reload the selected workflow
          const workflowResponse = await axios.get(`/api/eudr/mitigation/workflows/${this.selectedWorkflow.id}`);
          if (workflowResponse.data.success) {
            this.selectedWorkflow = workflowResponse.data.data;
          }
          this.loadWorkflows();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to add action');
      } finally {
        this.addingAction = false;
      }
    },
    async submitUpdateStatus() {
      if (!this.$refs.updateStatusForm.validate()) return;

      this.updatingStatus = true;
      try {
        const response = await axios.put(
          `/api/eudr/mitigation/actions/${this.selectedAction.id}/status`,
          this.updateActionStatus
        );
        if (response.data.success) {
          this.$toast.success('Action status updated successfully');
          this.actionDetailDialog = false;
          // Reload the selected workflow
          const workflowResponse = await axios.get(`/api/eudr/mitigation/workflows/${this.selectedWorkflow.id}`);
          if (workflowResponse.data.success) {
            this.selectedWorkflow = workflowResponse.data.data;
          }
          this.loadWorkflows();
          this.loadStatistics();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to update action status');
      } finally {
        this.updatingStatus = false;
      }
    },
    canCompleteWorkflow(workflow) {
      return workflow.pendingActions === 0 && workflow.inProgressActions === 0;
    },
    async submitCompleteWorkflow() {
      this.completingWorkflow = true;
      try {
        const response = await axios.put(
          `/api/eudr/mitigation/workflows/${this.selectedWorkflow.id}/complete`,
          { completionNotes: this.completeWorkflowNotes }
        );
        if (response.data.success) {
          this.$toast.success('Workflow completed successfully');
          this.showCompleteWorkflowDialog = false;
          this.viewWorkflowDialog = false;
          this.completeWorkflowNotes = '';
          this.loadWorkflows();
          this.loadStatistics();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to complete workflow');
      } finally {
        this.completingWorkflow = false;
      }
    },
  },
};
</script>

<style scoped>
.mitigation-tracking {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.stat-card {
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  color: white;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2) !important;
}

.pending-card {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.progress-card {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}

.completed-card {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.high-risk-card {
  background: linear-gradient(135deg, #f44336, #c62828);
}

.search-field {
  max-width: 300px;
}

.kanban-column {
  min-height: 400px;
}

.kanban-items {
  max-height: 500px;
  overflow-y: auto;
}

.action-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: scale(1.02);
}

.info-item {
  margin-bottom: 16px;
}

.info-label {
  font-weight: 600;
  color: #666;
  margin-bottom: 4px;
}

.info-value {
  font-size: 16px;
  color: #333;
}
</style>
