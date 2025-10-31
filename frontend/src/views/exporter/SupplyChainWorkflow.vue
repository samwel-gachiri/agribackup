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

          <!-- Stage Badge -->
          <div class="tw-mb-4">
            <v-chip small outlined :color="getStageColor(workflow.currentStage)">
              <v-icon left small>{{ getStageIcon(workflow.currentStage) }}</v-icon>
              {{ workflow.currentStage }}
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
        <v-card v-if="selectedWorkflow">
          <!-- Compact Header -->
          <v-toolbar flat dense class="tw-border-b">
            <v-toolbar-title>
              <span class="tw-font-medium">{{ selectedWorkflow.workflowName }}</span>
              <span class="tw-text-sm tw-text-gray-500 tw-ml-2">{{ selectedWorkflow.produceType }}</span>
            </v-toolbar-title>

            <!-- Progress Chips - Inline -->
            <div class="tw-flex tw-items-center tw-gap-1 tw-ml-6">
              <v-chip
                v-for="stage in ['Collection', 'Consolidation', 'Processing', 'Shipment']"
                :key="stage"
                x-small
                :color="getProgressStageColor(stage, selectedWorkflow.currentStage)"
                :text-color="isStageActive(stage, selectedWorkflow.currentStage) ? 'white' : 'grey'"
              >
                <v-icon v-if="isStageCompleted(stage, selectedWorkflow.currentStage)" left x-small>mdi-check</v-icon>
                {{ stage }}
              </v-chip>
            </div>

            <!-- Stats - Inline -->
            <v-spacer></v-spacer>
            <div class="tw-flex tw-items-center tw-gap-4 tw-text-xs">
              <div>
                <span class="tw-text-gray-600">Collected:</span>
                <span class="tw-font-bold tw-text-blue-600 tw-ml-1">{{ selectedWorkflow.totalCollected.toFixed(0) }} kg</span>
              </div>
              <div>
                <span class="tw-text-gray-600">Processed:</span>
                <span class="tw-font-bold tw-text-orange-600 tw-ml-1">{{ (selectedWorkflow.totalProcessed || 0).toFixed(0) }} kg</span>
              </div>
              <div>
                <span class="tw-text-gray-600">Shipped:</span>
                <span class="tw-font-bold tw-text-green-600 tw-ml-1">{{ selectedWorkflow.totalShipped.toFixed(0) }} kg</span>
              </div>
            </div>

            <v-btn icon @click="showWorkflowDetails = false" class="tw-ml-4">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-toolbar>

          <!-- Canvas - Full Height -->
          <v-card-text class="tw-p-4" style="height: calc(100vh - 64px); overflow-y: auto;">
            <div class="tw-h-full">
              <!-- Visual Workflow Canvas -->
              <div class="tw-border-2 tw-border-dashed tw-border-gray-300 tw-rounded-xl tw-p-6 tw-bg-gray-50">
                <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
                  <h3 class="tw-text-sm tw-font-semibold tw-text-gray-700 tw-flex tw-items-center">
                    <v-icon small class="tw-mr-2">mdi-transit-connection-variant</v-icon>
                    Interactive Workflow Canvas
                  </h3>
                  <div class="tw-text-xs tw-text-gray-500">
                    Click entities to select, then connect them across stages
                  </div>
                </div>

                <!-- Canvas Grid -->
                <div class="tw-grid tw-grid-cols-4 tw-gap-6">
                  <!-- Column 1: Production Units (from connected farmers) -->
                  <div class="tw-space-y-3">
                    <div class="tw-text-xs tw-font-semibold tw-text-gray-600 tw-uppercase tw-tracking-wide tw-mb-3 tw-flex tw-items-center tw-justify-between">
                      <span>
                        <v-icon x-small class="tw-mr-1">mdi-barn</v-icon>
                        Production Units
                      </span>
                      <v-chip x-small color="blue" text-color="white">{{ connectedProductionUnits.length }}</v-chip>
                    </div>

                    <!-- Production Unit Cards -->
                    <div
                      v-for="unit in connectedProductionUnits"
                      :key="'unit-' + unit.id"
                      class="tw-bg-white tw-rounded-lg tw-p-3 tw-border-2 tw-shadow-sm tw-transition-all"
                      :class="{
                        'tw-border-blue-500 tw-bg-blue-50': selectedSource?.type === 'productionUnit' && selectedSource?.id === unit.id,
                        'tw-border-blue-200': selectedSource?.type !== 'productionUnit' || selectedSource?.id !== unit.id
                      }"
                    >
                      <div class="tw-mb-3">
                        <!-- Production Unit Name -->
                        <div class="tw-flex tw-items-start tw-gap-2">
                          <v-icon x-small color="blue" class="tw-mt-0.5">mdi-map-marker</v-icon>
                          <div class="tw-flex-1">
                            <p class="tw-text-xs tw-font-bold tw-text-gray-900 tw-leading-tight">
                              {{ unit.unitName || unit.name || 'Unnamed Unit' }}
                            </p>
                          </div>
                        </div>

                        <!-- Farmer Name -->
                        <div v-if="unit.farmer" class="tw-flex tw-items-center tw-gap-2 tw-mt-2">
                          <v-icon x-small color="blue-grey">mdi-account</v-icon>
                          <p class="tw-text-xs tw-text-gray-700">
                            <span class="tw-font-medium">Farmer:</span>
                            {{ unit.farmer.firstName }} {{ unit.farmer.lastName }}
                          </p>
                        </div>

                        <!-- Location -->
                        <div v-if="unit.administrativeRegion" class="tw-flex tw-items-center tw-gap-2 tw-mt-1.5">
                          <v-icon x-small color="blue-grey">mdi-map</v-icon>
                          <p class="tw-text-xs tw-text-gray-600">
                            {{ unit.administrativeRegion }}
                          </p>
                        </div>

                        <!-- Area -->
                        <div v-if="unit.areaHectares" class="tw-flex tw-items-center tw-gap-2 tw-mt-1.5">
                          <v-icon x-small color="blue-grey">mdi-texture-box</v-icon>
                          <p class="tw-text-xs tw-text-gray-600">
                            <span class="tw-font-medium">Size:</span> {{ unit.areaHectares }} hectares
                          </p>
                        </div>

                        <!-- Crops (if available) -->
                        <div v-if="unit.primaryCrops" class="tw-flex tw-items-center tw-gap-2 tw-mt-1.5">
                          <v-icon x-small color="blue-grey">mdi-leaf</v-icon>
                          <p class="tw-text-xs tw-text-gray-600">
                            <span class="tw-font-medium">Crops:</span> {{ unit.primaryCrops }}
                          </p>
                        </div>
                      </div>

                      <!-- Collection Events for this Production Unit -->
                      <div v-if="getCollectionEventsForUnit(unit.id).length > 0" class="tw-mt-2 tw-mb-2 tw-pt-2 tw-border-t tw-border-gray-200">
                        <p class="tw-text-xs tw-font-semibold tw-text-green-700 tw-mb-1">
                          <v-icon x-small color="green" class="tw-mr-1">mdi-check-circle</v-icon>
                          {{ getCollectionEventsForUnit(unit.id).length }} Collection(s)
                        </p>
                        <div v-for="event in getCollectionEventsForUnit(unit.id).slice(0, 2)" :key="event.id" class="tw-mb-1.5">
                          <div class="tw-flex tw-items-center tw-text-xs tw-text-gray-600">
                            <v-icon x-small color="blue" class="tw-mr-0.5">mdi-arrow-right</v-icon>
                            {{ event.quantityCollectedKg }} kg
                            <v-icon x-small color="orange" class="tw-ml-0.5">mdi-package-variant</v-icon>
                          </div>
                          <div v-if="event.hederaTransactionId" class="tw-flex tw-items-center tw-text-xs tw-text-gray-500 tw-mt-0.5">
                            <v-icon x-small color="purple" class="tw-mr-0.5">mdi-shield-check</v-icon>
                            <span class="tw-font-mono tw-text-xs">{{ event.hederaTransactionId.substring(0, 15) }}...</span>
                          </div>
                        </div>
                        <p v-if="getCollectionEventsForUnit(unit.id).length > 2" class="tw-text-xs tw-text-gray-400 tw-italic">
                          +{{ getCollectionEventsForUnit(unit.id).length - 2 }} more
                        </p>
                      </div>

                      <v-btn
                        x-small
                        block
                        :color="selectedSource?.type === 'productionUnit' && selectedSource?.id === unit.id ? 'blue' : 'blue-grey lighten-4'"
                        :dark="selectedSource?.type === 'productionUnit' && selectedSource?.id === unit.id"
                        @click="selectEntity('productionUnit', unit)"
                        class="tw-mt-2"
                      >
                        <v-icon x-small left v-if="selectedSource?.type === 'productionUnit' && selectedSource?.id === unit.id">mdi-check</v-icon>
                        {{ selectedSource?.type === 'productionUnit' && selectedSource?.id === unit.id ? 'Selected' : 'Select' }}
                      </v-btn>
                    </div>

                    <div v-if="!connectedProductionUnits.length" class="tw-text-center tw-py-8">
                      <v-icon size="32" color="grey lighten-3">mdi-barn</v-icon>
                      <p class="tw-text-xs tw-text-gray-400 tw-mt-2">No production units</p>
                    </div>
                  </div>

                  <!-- Column 2: Aggregators -->
                  <div class="tw-space-y-3">
                    <div class="tw-text-xs tw-font-semibold tw-text-gray-600 tw-uppercase tw-tracking-wide tw-mb-3 tw-flex tw-items-center tw-justify-between">
                      <span>
                        <v-icon x-small class="tw-mr-1">mdi-package-variant</v-icon>
                        Aggregators
                      </span>
                      <v-chip x-small color="orange" text-color="white">{{ aggregators.length }}</v-chip>
                    </div>

                    <!-- Aggregator Cards -->
                    <div
                      v-for="aggregator in aggregators"
                      :key="'agg-' + aggregator.id"
                      class="tw-bg-white tw-rounded-lg tw-p-3 tw-border-2 tw-shadow-sm tw-transition-all"
                      :class="{
                        'tw-border-orange-500 tw-bg-orange-50': (selectedSource?.type === 'aggregator' && selectedSource?.id === aggregator.id) || (selectedTarget?.type === 'aggregator' && selectedTarget?.id === aggregator.id),
                        'tw-border-orange-200': (selectedSource?.type !== 'aggregator' || selectedSource?.id !== aggregator.id) && (selectedTarget?.type !== 'aggregator' || selectedTarget?.id !== aggregator.id)
                      }"
                    >
                      <div class="tw-mb-2">
                        <p class="tw-text-xs tw-font-semibold tw-text-gray-900">{{ aggregator.organizationName || aggregator.facilityName || aggregator.companyName || 'Unnamed Aggregator' }}</p>
                        <p class="tw-text-xs tw-text-gray-500 tw-mt-1" v-if="aggregator.organizationType">{{ aggregator.organizationType }}</p>
                        <p class="tw-text-xs tw-text-gray-400 tw-mt-1" v-if="aggregator.facilityAddress">{{ aggregator.facilityAddress }}</p>
                      </div>

                      <!-- Collection Events received by this Aggregator -->
                      <div v-if="getCollectionEventsForAggregator(aggregator.id).length > 0" class="tw-mt-2 tw-mb-2 tw-pt-2 tw-border-t tw-border-gray-200">
                        <p class="tw-text-xs tw-font-semibold tw-text-blue-700 tw-mb-1">
                          <v-icon x-small color="blue" class="tw-mr-1">mdi-basket</v-icon>
                          Received: {{ getTotalCollectedForAggregator(aggregator.id) }} kg
                        </p>
                        <p class="tw-text-xs tw-text-gray-600 tw-mb-1">
                          From {{ getCollectionEventsForAggregator(aggregator.id).length }} collection(s)
                        </p>
                        <div v-if="getBlockchainVerifiedCount(aggregator.id) > 0" class="tw-flex tw-items-center tw-text-xs tw-text-purple-600">
                          <v-icon x-small color="purple" class="tw-mr-0.5">mdi-shield-check</v-icon>
                          {{ getBlockchainVerifiedCount(aggregator.id) }} blockchain verified
                        </div>
                      </div>

                      <v-btn
                        x-small
                        block
                        :color="(selectedSource?.type === 'aggregator' && selectedSource?.id === aggregator.id) || (selectedTarget?.type === 'aggregator' && selectedTarget?.id === aggregator.id) ? 'orange' : 'orange lighten-4'"
                        :dark="(selectedSource?.type === 'aggregator' && selectedSource?.id === aggregator.id) || (selectedTarget?.type === 'aggregator' && selectedTarget?.id === aggregator.id)"
                        @click="selectEntity('aggregator', aggregator)"
                        class="tw-mt-2"
                      >
                        <v-icon x-small left v-if="(selectedSource?.type === 'aggregator' && selectedSource?.id === aggregator.id) || (selectedTarget?.type === 'aggregator' && selectedTarget?.id === aggregator.id)">mdi-check</v-icon>
                        {{ (selectedSource?.type === 'aggregator' && selectedSource?.id === aggregator.id) || (selectedTarget?.type === 'aggregator' && selectedTarget?.id === aggregator.id) ? 'Selected' : 'Select' }}
                      </v-btn>
                    </div>

                    <div v-if="!aggregators.length" class="tw-text-center tw-py-8">
                      <v-icon size="32" color="grey lighten-3">mdi-package-variant</v-icon>
                      <p class="tw-text-xs tw-text-gray-400 tw-mt-2">No aggregators</p>
                    </div>
                  </div>

                  <!-- Column 3: Processors -->
                  <div class="tw-space-y-3">
                    <div class="tw-text-xs tw-font-semibold tw-text-gray-600 tw-uppercase tw-tracking-wide tw-mb-3 tw-flex tw-items-center tw-justify-between">
                      <span>
                        <v-icon x-small class="tw-mr-1">mdi-cog</v-icon>
                        Processors
                      </span>
                      <v-chip x-small color="purple" text-color="white">{{ processors.length }}</v-chip>
                    </div>

                    <!-- Processor Cards -->
                    <div
                      v-for="processor in processors"
                      :key="'proc-' + processor.id"
                      class="tw-bg-white tw-rounded-lg tw-p-3 tw-border-2 tw-shadow-sm tw-transition-all"
                      :class="{
                        'tw-border-purple-500 tw-bg-purple-50': (selectedSource?.type === 'processor' && selectedSource?.id === processor.id) || (selectedTarget?.type === 'processor' && selectedTarget?.id === processor.id),
                        'tw-border-purple-200': (selectedSource?.type !== 'processor' || selectedSource?.id !== processor.id) && (selectedTarget?.type !== 'processor' || selectedTarget?.id !== processor.id)
                      }"
                    >
                      <div class="tw-mb-2">
                        <p class="tw-text-xs tw-font-semibold tw-text-gray-900">{{ processor.facilityName || processor.companyName || 'Unnamed Processor' }}</p>
                        <p class="tw-text-xs tw-text-gray-500 tw-mt-1" v-if="processor.processorType">{{ processor.processorType }}</p>
                        <p class="tw-text-xs tw-text-gray-400 tw-mt-1" v-if="processor.facilityAddress || processor.location">{{ processor.facilityAddress || processor.location }}</p>
                      </div>

                      <!-- Display Consolidation Events for this Processor -->
                      <div v-if="getConsolidationEventsForProcessor(processor.id).length > 0" class="tw-mt-2 tw-pt-2 tw-border-t tw-border-gray-200">
                        <div class="tw-flex tw-items-center tw-justify-between tw-mb-1">
                          <p class="tw-text-xs tw-font-medium tw-text-orange-700">
                            <v-icon x-small color="orange" class="tw-mr-1">mdi-package-variant</v-icon>
                            Received: {{ getTotalConsolidatedForProcessor(processor.id) }} kg
                          </p>
                          <v-chip x-small color="purple" outlined v-if="getConsolidationBlockchainVerifiedCount(processor.id) > 0">
                            <v-icon x-small left color="purple">mdi-shield-check</v-icon>
                            {{ getConsolidationBlockchainVerifiedCount(processor.id) }}
                          </v-chip>
                        </div>
                        <div class="tw-space-y-1">
                          <div
                            v-for="event in getConsolidationEventsForProcessor(processor.id)"
                            :key="'consol-' + event.id"
                            class="tw-text-xs tw-p-2 tw-bg-orange-50 tw-rounded tw-border tw-border-orange-200"
                          >
                            <div class="tw-flex tw-items-center tw-justify-between">
                              <span class="tw-text-orange-900 tw-font-medium">
                                <v-icon x-small color="orange">mdi-arrow-right-bold</v-icon>
                                {{ event.quantitySentKg }} kg
                              </span>
                              <span class="tw-text-orange-600 tw-text-xs">{{ new Date(event.consolidationDate).toLocaleDateString() }}</span>
                            </div>
                            <div v-if="event.batchNumber" class="tw-text-xs tw-text-orange-700 tw-mt-1">
                              Batch: {{ event.batchNumber }}
                            </div>
                            <div v-if="event.hederaTransactionId" class="tw-flex tw-items-center tw-gap-1 tw-mt-1">
                              <v-icon x-small color="purple">mdi-shield-check</v-icon>
                              <span class="tw-text-xs tw-text-purple-600 tw-font-mono">{{ event.hederaTransactionId.substring(0, 15) }}...</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <v-btn
                        x-small
                        block
                        :color="(selectedSource?.type === 'processor' && selectedSource?.id === processor.id) || (selectedTarget?.type === 'processor' && selectedTarget?.id === processor.id) ? 'purple' : 'purple lighten-4'"
                        :dark="(selectedSource?.type === 'processor' && selectedSource?.id === processor.id) || (selectedTarget?.type === 'processor' && selectedTarget?.id === processor.id)"
                        @click="selectEntity('processor', processor)"
                        class="tw-mt-2"
                      >
                        <v-icon x-small left v-if="(selectedSource?.type === 'processor' && selectedSource?.id === processor.id) || (selectedTarget?.type === 'processor' && selectedTarget?.id === processor.id)">mdi-check</v-icon>
                        {{ (selectedSource?.type === 'processor' && selectedSource?.id === processor.id) || (selectedTarget?.type === 'processor' && selectedTarget?.id === processor.id) ? 'Selected' : 'Select' }}
                      </v-btn>
                    </div>

                    <div v-if="!processors.length" class="tw-text-center tw-py-8">
                      <v-icon size="32" color="grey lighten-3">mdi-cog</v-icon>
                      <p class="tw-text-xs tw-text-gray-400 tw-mt-2">No processors</p>
                    </div>
                  </div>

                  <!-- Column 4: Importers -->
                  <div class="tw-space-y-3">
                    <div class="tw-text-xs tw-font-semibold tw-text-gray-600 tw-uppercase tw-tracking-wide tw-mb-3 tw-flex tw-items-center tw-justify-between">
                      <span>
                        <v-icon x-small class="tw-mr-1">mdi-truck</v-icon>
                        Importers
                      </span>
                      <v-chip x-small color="green" text-color="white">{{ importers.length }}</v-chip>
                    </div>

                    <!-- Importer Cards -->
                    <div
                      v-for="importer in importers"
                      :key="'imp-' + importer.id"
                      class="tw-bg-white tw-rounded-lg tw-p-3 tw-border-2 tw-shadow-sm tw-transition-all"
                      :class="{
                        'tw-border-green-500 tw-bg-green-50': selectedTarget?.type === 'importer' && selectedTarget?.id === importer.id,
                        'tw-border-green-200': selectedTarget?.type !== 'importer' || selectedTarget?.id !== importer.id
                      }"
                    >
                      <div class="tw-mb-2">
                        <p class="tw-text-xs tw-font-semibold tw-text-gray-900">{{ importer.companyName || 'Unnamed Importer' }}</p>
                        <p class="tw-text-xs tw-text-gray-500 tw-mt-1" v-if="importer.contactPerson">{{ importer.contactPerson }}</p>
                        <p class="tw-text-xs tw-text-gray-400 tw-mt-1" v-if="importer.country">{{ importer.country }}</p>
                      </div>

                      <!-- Display Shipment Events for this Importer -->
                      <div v-if="getShipmentEventsForImporter(importer.id).length > 0" class="tw-mt-2 tw-pt-2 tw-border-t tw-border-gray-200">
                        <div class="tw-flex tw-items-center tw-justify-between tw-mb-1">
                          <p class="tw-text-xs tw-font-medium tw-text-green-700">
                            <v-icon x-small color="green" class="tw-mr-1">mdi-truck</v-icon>
                            Imported: {{ getTotalShippedForImporter(importer.id) }} kg
                          </p>
                          <v-chip x-small color="purple" outlined v-if="getShipmentBlockchainVerifiedCount(importer.id) > 0">
                            <v-icon x-small left color="purple">mdi-shield-check</v-icon>
                            {{ getShipmentBlockchainVerifiedCount(importer.id) }}
                          </v-chip>
                        </div>
                        <div class="tw-space-y-1">
                          <div
                            v-for="event in getShipmentEventsForImporter(importer.id)"
                            :key="'ship-' + event.id"
                            class="tw-text-xs tw-p-2 tw-bg-green-50 tw-rounded tw-border tw-border-green-200"
                          >
                            <div class="tw-flex tw-items-center tw-justify-between">
                              <span class="tw-text-green-900 tw-font-medium">
                                <v-icon x-small color="green">mdi-arrow-right-bold</v-icon>
                                {{ event.quantityShippedKg }} kg
                              </span>
                              <span class="tw-text-green-600 tw-text-xs">{{ new Date(event.shipmentDate).toLocaleDateString() }}</span>
                            </div>
                            <div v-if="event.trackingNumber" class="tw-text-xs tw-text-green-700 tw-mt-1">
                              Tracking: {{ event.trackingNumber }}
                            </div>
                            <div v-if="event.hederaTransactionId" class="tw-flex tw-items-center tw-gap-1 tw-mt-1">
                              <v-icon x-small color="purple">mdi-shield-check</v-icon>
                              <span class="tw-text-xs tw-text-purple-600 tw-font-mono">{{ event.hederaTransactionId.substring(0, 15) }}...</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <v-btn
                        x-small
                        block
                        :color="selectedTarget?.type === 'importer' && selectedTarget?.id === importer.id ? 'green' : 'green lighten-4'"
                        :dark="selectedTarget?.type === 'importer' && selectedTarget?.id === importer.id"
                        @click="selectEntity('importer', importer)"
                        class="tw-mt-2"
                      >
                        <v-icon x-small left v-if="selectedTarget?.type === 'importer' && selectedTarget?.id === importer.id">mdi-check</v-icon>
                        {{ selectedTarget?.type === 'importer' && selectedTarget?.id === importer.id ? 'Selected' : 'Select' }}
                      </v-btn>
                    </div>

                    <div v-if="!importers.length" class="tw-text-center tw-py-8">
                      <v-icon size="32" color="grey lighten-3">mdi-truck</v-icon>
                      <p class="tw-text-xs tw-text-gray-400 tw-mt-2">No importers</p>
                    </div>
                  </div>
                </div>

                <!-- Selection Status & Actions -->
                <div v-if="selectedSource || selectedTarget" class="tw-mt-6 tw-pt-4 tw-border-t tw-border-gray-300">
                  <div class="tw-flex tw-items-center tw-justify-between">
                    <div class="tw-flex tw-items-center tw-gap-4">
                      <div v-if="selectedSource" class="tw-text-xs">
                        <span class="tw-text-gray-600">From:</span>
                        <v-chip x-small :color="getEntityColor(selectedSource.type)" text-color="white" class="tw-ml-1">
                          {{ selectedSource.type }}: {{ getEntityName(selectedSource) }}
                        </v-chip>
                      </div>
                      <v-icon v-if="selectedSource && selectedTarget" small>mdi-arrow-right</v-icon>
                      <div v-if="selectedTarget" class="tw-text-xs">
                        <span class="tw-text-gray-600">To:</span>
                        <v-chip x-small :color="getEntityColor(selectedTarget.type)" text-color="white" class="tw-ml-1">
                          {{ selectedTarget.type }}: {{ getEntityName(selectedTarget) }}
                        </v-chip>
                      </div>
                    </div>
                    <div class="tw-flex tw-gap-2">
                      <v-btn x-small text @click="clearSelection">Clear</v-btn>
                      <v-btn
                        v-if="selectedSource && selectedTarget && canConnect(selectedSource.type, selectedTarget.type)"
                        x-small
                        color="primary"
                        @click="openConnectionDialog"
                      >
                        <v-icon left x-small>mdi-link</v-icon>
                        Create Connection
                      </v-btn>
                    </div>
                  </div>
                </div>

                <!-- Empty State -->
                <div v-if="!connectedProductionUnits.length && !aggregators.length && !processors.length && !importers.length"
                     class="tw-text-center tw-py-12">
                  <v-icon size="64" color="grey lighten-2">mdi-transit-connection-variant</v-icon>
                  <p class="tw-text-gray-400 tw-mt-4">No connected entities</p>
                  <p class="tw-text-gray-400 tw-text-sm tw-mt-2">Connect with farmers, aggregators, processors, and importers first</p>
                </div>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>

      <!-- Connection Dialog -->
      <v-dialog v-model="showConnectionDialog" max-width="700" persistent>
        <v-card class="tw-rounded-xl">
          <v-card-title class="tw-text-lg tw-font-medium tw-px-6 tw-py-4">
            <div>
              <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
                <v-chip x-small :color="getEntityColor(selectedSource?.type)" text-color="white">
                  {{ selectedSource?.type }}
                </v-chip>
                <v-icon small>mdi-arrow-right</v-icon>
                <v-chip x-small :color="getEntityColor(selectedTarget?.type)" text-color="white">
                  {{ selectedTarget?.type }}
                </v-chip>
              </div>
              <div class="tw-text-base tw-text-gray-600">
                {{ getEntityName(selectedSource) }} → {{ getEntityName(selectedTarget) }}
              </div>
            </div>
          </v-card-title>
          <v-divider></v-divider>
          <v-card-text class="tw-px-6 tw-py-6">
            <v-form ref="connectionForm" v-model="connectionFormValid">
              <!-- COLLECTION FORM: Production Unit → Aggregator -->
              <div v-if="selectedSource?.type === 'productionUnit' && selectedTarget?.type === 'aggregator'">
                <div class="tw-mb-4 tw-p-4 tw-bg-blue-50 tw-rounded-lg">
                  <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
                    <v-icon color="blue">mdi-basket</v-icon>
                    <span class="tw-font-semibold tw-text-blue-900">Farm Collection Event</span>
                  </div>
                  <p class="tw-text-sm tw-text-blue-700">Record produce collected from the production unit by the aggregator</p>
                </div>

                <!-- Display Farmer Info (Read-only) -->
                <div class="tw-mb-4 tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-200">
                  <div class="tw-flex tw-items-center tw-gap-2 tw-mb-1">
                    <v-icon small color="blue-grey">mdi-account</v-icon>
                    <span class="tw-text-xs tw-font-semibold tw-text-gray-600">Farmer Information</span>
                  </div>
                  <p class="tw-text-sm tw-text-gray-900" v-if="selectedSource?.data?.farmer">
                    {{ selectedSource.data.farmer.firstName }} {{ selectedSource.data.farmer.lastName }}
                  </p>
                  <p class="tw-text-xs tw-text-gray-500 tw-mt-0.5" v-if="selectedSource?.data?.unitName">
                    From: {{ selectedSource.data.unitName }}
                  </p>
                </div>

                <v-text-field
                  v-model="connectionData.quantity"
                  label="Quantity Collected (kg) *"
                  type="number"
                  step="0.01"
                  :rules="[rules.required, rules.positive]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.date"
                  label="Collection Date *"
                  type="datetime-local"
                  :rules="[rules.required]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-select
                  v-model="connectionData.qualityGrade"
                  :items="['A', 'B', 'C', 'Premium', 'Standard']"
                  label="Quality Grade"
                  outlined
                  dense
                  clearable
                  class="tw-mb-2"
                ></v-select>
              </div>

              <!-- CONSOLIDATION FORM: Aggregator → Processor -->
              <div v-else-if="selectedSource?.type === 'aggregator' && selectedTarget?.type === 'processor'">
                <div class="tw-mb-4 tw-p-4 tw-bg-orange-50 tw-rounded-lg">
                  <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
                    <v-icon color="orange">mdi-package-variant</v-icon>
                    <span class="tw-font-semibold tw-text-orange-900">Consolidation Event</span>
                  </div>
                  <p class="tw-text-sm tw-text-orange-700">Record consolidated produce sent from aggregator to processor</p>
                </div>

                <v-text-field
                  v-model="connectionData.quantity"
                  label="Quantity Sent (kg) *"
                  type="number"
                  step="0.01"
                  :rules="[rules.required, rules.positive]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.date"
                  label="Consolidation Date *"
                  type="datetime-local"
                  :rules="[rules.required]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.batchNumber"
                  label="Batch Number (Auto-generated)"
                  outlined
                  dense
                  hint="EUDR-compliant batch reference number"
                  persistent-hint
                  readonly
                  class="tw-mb-2"
                ></v-text-field>

                <!-- Transport Details and Additional Notes hidden for now -->
              </div>

              <!-- SHIPMENT FORM: Processor → Importer -->
              <div v-else-if="selectedSource?.type === 'processor' && selectedTarget?.type === 'importer'">
                <div class="tw-mb-4 tw-p-4 tw-bg-green-50 tw-rounded-lg">
                  <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
                    <v-icon color="green">mdi-truck</v-icon>
                    <span class="tw-font-semibold tw-text-green-900">Shipment Event</span>
                  </div>
                  <p class="tw-text-sm tw-text-green-700">Record final shipment from processor to importer</p>
                </div>

                <v-text-field
                  v-model="connectionData.quantity"
                  label="Quantity Shipped (kg) *"
                  type="number"
                  step="0.01"
                  :rules="[rules.required, rules.positive]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.date"
                  label="Shipment Date *"
                  type="datetime-local"
                  :rules="[rules.required]"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.expectedArrivalDate"
                  label="Expected Arrival Date"
                  type="datetime-local"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.shippingCompany"
                  label="Shipping Company"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.trackingNumber"
                  label="Tracking Number (Auto-generated)"
                  outlined
                  dense
                  hint="Auto-generated shipment tracking reference"
                  persistent-hint
                  readonly
                  class="tw-mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="connectionData.destinationPort"
                  label="Destination Port/Location"
                  outlined
                  dense
                  class="tw-mb-2"
                ></v-text-field>

                <!-- Shipment Notes hidden for now -->
              </div>
            </v-form>
          </v-card-text>
          <v-divider></v-divider>
          <v-card-actions class="tw-px-6 tw-py-4">
            <v-spacer></v-spacer>
            <v-btn text @click="showConnectionDialog = false">Cancel</v-btn>
            <v-btn color="primary" @click="createConnection" :loading="connecting" :disabled="!connectionFormValid">
              Create {{ getConnectionTypeName() }}
            </v-btn>
          </v-card-actions>
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

      <!-- Stage Update Dialog -->
      <v-dialog v-model="showStageDialog" max-width="700" persistent>
        <v-card v-if="currentStage" class="tw-rounded-2xl">
          <v-card-title class="tw-bg-gradient-to-r tw-from-indigo-600 tw-to-purple-600 tw-text-white">
            <v-icon left color="white">{{ currentStage.icon }}</v-icon>
            <span class="tw-font-bold">Update: {{ currentStage.name }}</span>
          </v-card-title>
          <v-card-text class="tw-p-6">
            <component :is="getStageComponent(currentStage.type)"
                       :stage="currentStage"
                       :aggregators="aggregators"
                       :processors="processors"
                       :importers="importers"
                       @update="updateStageData" />
          </v-card-text>
          <v-divider></v-divider>
          <v-card-actions class="tw-p-4">
            <v-spacer></v-spacer>
            <v-btn text @click="showStageDialog = false">Cancel</v-btn>
            <v-btn color="primary" @click="saveStageUpdate" :loading="updating">
              Save & Continue
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

export default {
  name: 'SupplyChainWorkflow',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      creating: false,
      updating: false,
      connecting: false,
      workflows: [],
      selectedWorkflow: null,

      // Interactive Canvas Selection
      selectedSource: null,
      selectedTarget: null,
      connectedProductionUnits: [],

      // Workflow Events for Canvas
      collectionEvents: [],
      consolidationEvents: [],
      processingEvents: [],
      shipmentEvents: [],

      // Connected entities
      aggregators: [],
      processors: [],
      importers: [],

      // Dialogs
      showNewWorkflowDialog: false,
      showWorkflowDetails: false,
      showStageDialog: false,
      showConnectionDialog: false,
      currentStage: null,

      // Connection Form
      connectionFormValid: false,
      connectionData: {
        // Common fields
        quantity: null,
        date: null,
        notes: '',
        // Collection-specific
        farmerId: '',
        qualityGrade: null,
        // Consolidation-specific
        batchNumber: '',
        transportDetails: '',
        // Shipment-specific
        expectedArrivalDate: null,
        shippingCompany: '',
        trackingNumber: '',
        destinationPort: '',
      },

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
    await this.loadConnectedEntities();
    await this.loadWorkflows();
  },
  methods: {
    async loadConnectedEntities() {
      try {
        const exporterId = this.$store.state.auth?.user?.id;

        // Load connected aggregators, processors, and importers
        const [aggResp, procResp, impResp] = await Promise.all([
          axios.get('/api/v1/aggregators/connected', { params: { exporterId } }),
          axios.get('/api/v1/processors/connected', { params: { exporterId } }),
          axios.get('/api/v1/importers/connected', { params: { exporterId } }),
        ]);

        this.aggregators = aggResp.data.content || aggResp.data || [];
        this.processors = procResp.data.content || procResp.data || [];
        this.importers = impResp.data.content || impResp.data || [];

        // Load production units through aggregators' connected farmers
        await this.loadProductionUnits();
      } catch (error) {
        this.showSnackbar('Failed to load connected entities', 'error');
      }
    },

    async loadProductionUnits() {
      try {
        const exporterId = this.$store.state.auth?.user?.id;

        // Fetch production units from farmers connected to this exporter
        const response = await axios.get(`/api/production-units/exporter/${exporterId}/connected`);

        this.connectedProductionUnits = response.data.content || response.data.data || response.data || [];
      } catch (error) {
        this.$store.commit('showNotification', {
          type: 'error',
          message: 'Failed to load production units. Please try again.',
        });
        this.connectedProductionUnits = [];
      }
    },

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

    async selectWorkflow(workflow) {
      this.selectedWorkflow = workflow;
      this.showWorkflowDetails = true;

      // Load workflow events for the canvas
      await this.loadWorkflowEvents(workflow.id);
    },

    async loadWorkflowEvents(workflowId) {
      try {
        // Load all events for this workflow
        const [collectionResp, consolidationResp, processingResp, shipmentResp] = await Promise.all([
          axios.get(`/api/v1/supply-chain/workflows/${workflowId}/collection`),
          axios.get(`/api/v1/supply-chain/workflows/${workflowId}/consolidation`),
          axios.get(`/api/v1/supply-chain/workflows/${workflowId}/processing`),
          axios.get(`/api/v1/supply-chain/workflows/${workflowId}/shipment`),
        ]);

        this.collectionEvents = collectionResp.data || [];
        this.consolidationEvents = consolidationResp.data || [];
        this.processingEvents = processingResp.data || [];
        this.shipmentEvents = shipmentResp.data || [];
      } catch (error) {
        // Don't show error - events might not exist yet
        this.collectionEvents = [];
        this.consolidationEvents = [];
        this.processingEvents = [];
        this.shipmentEvents = [];
      }
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
      await this.loadConnectedEntities();
    },

    // Interactive Canvas Methods
    selectEntity(type, entity) {
      const entityData = { type, id: entity.id, data: entity };

      // Prevent selecting importer as source (terminal stage)
      if (type === 'importer' && !this.selectedSource) {
        this.showSnackbar('Importer is a terminal stage. Select a processor first to ship to this importer.', 'warning');
        return;
      }

      // If nothing selected yet, set as source
      if (!this.selectedSource) {
        this.selectedSource = entityData;
        return;
      }

      // If clicking on the same entity, deselect it
      if (this.selectedSource.type === type && this.selectedSource.id === entity.id) {
        this.clearSelection();
        return;
      }

      // If source is selected and we're clicking on a valid next stage, set as target
      if (this.selectedSource && this.canConnect(this.selectedSource.type, type)) {
        this.selectedTarget = entityData;
        return;
      }

      // Invalid connection - show feedback
      if (this.selectedSource) {
        const flowMessages = {
          productionUnit: 'Production Units can only connect to Aggregators',
          aggregator: 'Aggregators can only connect to Processors',
          processor: 'Processors can only connect to Importers',
          importer: 'Importers are the final stage and cannot connect further',
        };
        this.showSnackbar(flowMessages[this.selectedSource.type] || 'Invalid connection', 'warning');
      }

      // Reset and start fresh with new selection
      this.selectedSource = entityData;
      this.selectedTarget = null;
    },

    canConnect(sourceType, targetType) {
      // Define valid directional connections (no backwards movement)
      const validConnections = {
        productionUnit: ['aggregator'], // Can only go to aggregator
        aggregator: ['processor'], // Can only go to processor
        processor: ['importer'], // Can only go to importer
        importer: [], // Terminal stage - no outgoing connections
      };
      return validConnections[sourceType]?.includes(targetType) || false;
    },

    clearSelection() {
      this.selectedSource = null;
      this.selectedTarget = null;
    },

    openConnectionDialog() {
      // Get current datetime in local format for datetime-local input
      const now = new Date();
      const localDatetime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);

      // Calculate default quantity for consolidation (Aggregator → Processor)
      let defaultQuantity = null;
      let defaultBatchNumber = '';
      let defaultTrackingNumber = '';

      if (this.selectedSource?.type === 'aggregator' && this.selectedTarget?.type === 'processor') {
        // Sum of all collection quantities for this aggregator
        const totalCollected = this.getTotalCollectedForAggregator(this.selectedSource.data.id);

        // Sum of all previously sent consolidation quantities from this aggregator
        const previouslySent = this.consolidationEvents
          .filter((event) => event.aggregatorId === this.selectedSource.data.id)
          .reduce((sum, event) => sum + parseFloat(event.quantitySentKg || 0), 0);

        // Available quantity = total collected - previously sent
        defaultQuantity = Math.max(0, parseFloat(totalCollected) - previouslySent);

        // Generate EUDR-compliant batch number: EUDR-{PRODUCE}-{YYYYMMDD}-{SHORT_AGG_ID}-{SEQUENCE}
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
        const produceCode = this.selectedWorkflow?.produceType?.substring(0, 3).toUpperCase() || 'XXX';
        const aggregatorId = this.selectedSource.data.id;
        const shortAggId = aggregatorId.substring(0, 8).toUpperCase(); // Use first 8 chars of UUID
        const sequence = (this.consolidationEvents.filter((e) => e.aggregatorId === aggregatorId).length + 1)
          .toString().padStart(3, '0');
        defaultBatchNumber = `EUDR-${produceCode}-${dateStr}-${shortAggId}-${sequence}`;
      }

      // Calculate default quantity and tracking number for shipment (Processor → Importer)
      if (this.selectedSource?.type === 'processor' && this.selectedTarget?.type === 'importer') {
        // Sum of all consolidation quantities received by this processor
        const totalReceived = this.getTotalConsolidatedForProcessor(this.selectedSource.data.id);

        // Sum of all previously shipped quantities from this processor
        const previouslyShipped = this.shipmentEvents
          .filter((event) => event.processorId === this.selectedSource.data.id)
          .reduce((sum, event) => sum + parseFloat(event.quantityShippedKg || 0), 0);

        // Available quantity = total received - previously shipped
        defaultQuantity = Math.max(0, parseFloat(totalReceived) - previouslyShipped);

        // Generate tracking number: TRK-{PRODUCE}-{YYYYMMDD}-{SHORT_PROCESSOR_ID}-{SEQUENCE}
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
        const produceCode = this.selectedWorkflow?.produceType?.substring(0, 3).toUpperCase() || 'XXX';
        const processorId = this.selectedSource.data.id;
        const shortProcId = processorId.substring(0, 8).toUpperCase(); // Use first 8 chars of UUID
        const sequence = (this.shipmentEvents.filter((e) => e.processorId === processorId).length + 1)
          .toString().padStart(3, '0');
        defaultTrackingNumber = `TRK-${produceCode}-${dateStr}-${shortProcId}-${sequence}`;
      }

      // Reset connection form with stage-specific defaults
      this.connectionData = {
        // Common fields
        quantity: defaultQuantity,
        date: localDatetime, // Set to current datetime
        notes: '',
        // Collection-specific (Production Unit → Aggregator)
        farmerId: this.selectedSource?.type === 'productionUnit' && this.selectedSource.data.farmer
          ? this.selectedSource.data.farmer.id
          : '',
        qualityGrade: null,
        // Consolidation-specific (Aggregator → Processor)
        batchNumber: defaultBatchNumber,
        transportDetails: '',
        // Shipment-specific (Processor → Importer)
        expectedArrivalDate: null,
        shippingCompany: '',
        trackingNumber: defaultTrackingNumber,
        destinationPort: '',
      };

      this.showConnectionDialog = true;
    },

    async createConnection() {
      if (!this.$refs.connectionForm.validate()) return;

      this.connecting = true;
      try {
        const { source, target } = { source: this.selectedSource, target: this.selectedTarget };
        const stage = this.getConnectionStage();

        // Route to appropriate API based on connection type
        if (stage === 'collection') {
          // Create collection event (Production Unit → Aggregator)
          // Farmer ID is automatically populated from the production unit's farmer object
          await axios.post(`/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/collection`, {
            productionUnitId: source.data.id,
            aggregatorId: target.data.id,
            farmerId: this.connectionData.farmerId || source.data.farmer?.id,
            quantityCollectedKg: parseFloat(this.connectionData.quantity),
            collectionDate: this.connectionData.date,
            qualityGrade: this.connectionData.qualityGrade || null,
            notes: null, // Notes hidden for collection events
          });
        } else if (stage === 'consolidation') {
          // Create consolidation event (Aggregator → Processor)
          await axios.post(`/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/consolidation`, {
            aggregatorId: source.data.id,
            processorId: target.data.id,
            quantitySentKg: parseFloat(this.connectionData.quantity),
            consolidationDate: this.connectionData.date,
            batchNumber: this.connectionData.batchNumber || null,
            transportDetails: this.connectionData.transportDetails || null,
            notes: this.connectionData.notes || null,
          });
        } else if (stage === 'shipment') {
          // Create shipment event (Processor → Importer)
          await axios.post(`/api/v1/supply-chain/workflows/${this.selectedWorkflow.id}/shipment`, {
            processorId: source.data.id,
            importerId: target.data.id,
            quantityShippedKg: parseFloat(this.connectionData.quantity),
            shipmentDate: this.connectionData.date,
            expectedArrivalDate: this.connectionData.expectedArrivalDate || null,
            shippingCompany: this.connectionData.shippingCompany || null,
            trackingNumber: this.connectionData.trackingNumber || null,
            destinationPort: this.connectionData.destinationPort || null,
            notes: this.connectionData.notes || null,
          });
        }

        // Reload workflow events
        await this.loadWorkflowEvents(this.selectedWorkflow.id);

        // Close dialog and clear selection
        this.showConnectionDialog = false;
        this.clearSelection();

        this.showSnackbar(`${this.getConnectionTypeName()} created successfully`, 'success');
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to create connection', 'error');
      } finally {
        this.connecting = false;
      }
    },

    getEntityColor(type) {
      const colors = {
        productionUnit: 'blue',
        aggregator: 'orange',
        processor: 'purple',
        importer: 'green',
      };
      return colors[type] || 'grey';
    },

    getEntityName(entity) {
      if (!entity) return '';
      const { type, data } = entity;
      if (type === 'productionUnit') return data.name || data.farmerName;
      if (type === 'aggregator') return data.facilityName || data.companyName;
      if (type === 'processor') return data.facilityName || data.companyName;
      if (type === 'importer') return data.companyName;
      return 'Unknown';
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

    getConnectionTypeName() {
      if (!this.selectedSource || !this.selectedTarget) return 'Connection';

      const { type: sourceType } = this.selectedSource;
      const { type: targetType } = this.selectedTarget;

      if (sourceType === 'productionUnit' && targetType === 'aggregator') {
        return 'Collection Event';
      }
      if (sourceType === 'aggregator' && targetType === 'processor') {
        return 'Consolidation Event';
      }
      if (sourceType === 'processor' && targetType === 'importer') {
        return 'Shipment Event';
      }
      return 'Connection';
    },

    getConnectionStage() {
      if (!this.selectedSource || !this.selectedTarget) return null;

      const { type: sourceType } = this.selectedSource;
      const { type: targetType } = this.selectedTarget;

      if (sourceType === 'productionUnit' && targetType === 'aggregator') {
        return 'collection';
      }
      if (sourceType === 'aggregator' && targetType === 'processor') {
        return 'consolidation';
      }
      if (sourceType === 'processor' && targetType === 'importer') {
        return 'shipment';
      }
      return null;
    },

    // Helper methods to display collection events
    getCollectionEventsForUnit(productionUnitId) {
      return this.collectionEvents.filter((event) => event.productionUnitId === productionUnitId);
    },

    getCollectionEventsForAggregator(aggregatorId) {
      return this.collectionEvents.filter((event) => event.aggregatorId === aggregatorId);
    },

    getTotalCollectedForAggregator(aggregatorId) {
      const events = this.getCollectionEventsForAggregator(aggregatorId);
      const total = events.reduce((sum, event) => sum + parseFloat(event.quantityCollectedKg), 0);
      return total.toFixed(0);
    },

    getBlockchainVerifiedCount(aggregatorId) {
      const events = this.getCollectionEventsForAggregator(aggregatorId);
      return events.filter((event) => event.hederaTransactionId).length;
    },

    // Helper methods for consolidation events
    getConsolidationEventsForProcessor(processorId) {
      return this.consolidationEvents.filter((event) => event.processorId === processorId);
    },

    getTotalConsolidatedForProcessor(processorId) {
      const events = this.getConsolidationEventsForProcessor(processorId);
      const total = events.reduce((sum, event) => sum + parseFloat(event.quantitySentKg || 0), 0);
      return total.toFixed(0);
    },

    getConsolidationBlockchainVerifiedCount(processorId) {
      const events = this.getConsolidationEventsForProcessor(processorId);
      return events.filter((event) => event.hederaTransactionId).length;
    },

    // Helper methods for shipment events
    getShipmentEventsForImporter(importerId) {
      return this.shipmentEvents.filter((event) => event.importerId === importerId);
    },

    getTotalShippedForImporter(importerId) {
      const events = this.getShipmentEventsForImporter(importerId);
      const total = events.reduce((sum, event) => sum + parseFloat(event.quantityShippedKg || 0), 0);
      return total.toFixed(0);
    },

    getShipmentBlockchainVerifiedCount(importerId) {
      const events = this.getShipmentEventsForImporter(importerId);
      return events.filter((event) => event.hederaTransactionId).length;
    },

    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
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
</style>
