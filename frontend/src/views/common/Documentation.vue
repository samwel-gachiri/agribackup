<template>
  <div class="documentation-page tw-bg-white tw-font-sans tw-text-slate-900">
    <!-- Reusable Professional Header -->
    <AppHeader />

    <!-- Main Content Area -->
    <div class="tw-max-w-7xl tw-mx-auto tw-px-6 tw-pt-24 tw-pb-12">

       <!-- Layout Container -->
       <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-12">

          <!-- Sticky Sidebar -->
          <aside class="tw-hidden md:tw-block tw-w-64 tw-flex-shrink-0">
             <div class="tw-sticky tw-top-28">
                <nav class="tw-space-y-1">
                   <a
                     v-for="item in navItems"
                     :key="item.id"
                     :href="'#' + item.id"
                     class="tw-block tw-py-2 tw-px-4 tw-rounded-lg tw-text-sm tw-font-medium tw-transition-colors"
                     :class="activeSection === item.id ? 'tw-bg-green-50 tw-text-green-700' : 'tw-text-slate-600 hover:tw-text-green-600 hover:tw-bg-slate-50'"
                     @click.prevent="scrollTo(item.id)"
                   >
                     {{ item.label }}
                   </a>
                </nav>

                <div class="tw-mt-8 tw-p-4 tw-bg-slate-50 tw-rounded-xl tw-border tw-border-slate-100">
                   <p class="tw-text-xs tw-font-bold tw-text-slate-900 tw-mb-2">Need Help?</p>
                   <p class="tw-text-xs tw-text-slate-500 tw-mb-3">Our support team is available 24/7 for compliance assistance.</p>
                   <button class="tw-w-full tw-py-2 tw-bg-white tw-border tw-border-slate-200 tw-rounded-lg tw-text-xs tw-font-bold tw-text-slate-700 hover:tw-bg-slate-50 tw-transition">Contact Support</button>
                </div>
             </div>
          </aside>

          <!-- Content Column -->
          <main class="tw-flex-1 tw-min-w-0">

             <!-- ONE: GETTING STARTED -->
             <section id="getting-started" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-blue-100 tw-text-blue-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="blue">mdi-rocket-launch</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Getting Started</h2>
                </div>

                <div class="tw-grid md:tw-grid-cols-3 tw-gap-6">
                    <!-- Step 1 -->
                    <div class="doc-card">
                       <div class="tw-h-48 tw-bg-slate-50 tw-flex tw-items-center tw-justify-center tw-p-4 tw-border-b tw-border-slate-100">
                          <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/001_create_your_account.png')">
                            <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/001_create_your_account.png" class="tw-max-h-full tw-w-auto tw-shadow-sm tw-rounded" />
                            <div class="image-overlay">
                              <v-icon large color="white">mdi-eye</v-icon>
                            </div>
                          </div>
                       </div>
                       <div class="tw-p-6">
                          <span class="tw-text-xs tw-font-bold tw-text-blue-600 tw-uppercase tw-tracking-wide">Step 01</span>
                          <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-2 tw-mb-2">Create Account</h3>
                          <p class="tw-text-sm tw-text-slate-600">Register with email or Google.</p>
                       </div>
                    </div>
                     <!-- Step 2 -->
                    <div class="doc-card">
                       <div class="tw-h-48 tw-bg-slate-50 tw-flex tw-items-center tw-justify-center tw-p-4 tw-border-b tw-border-slate-100">
                          <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/002_select_role.png')">
                            <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/002_select_role.png" class="tw-max-h-full tw-w-auto tw-shadow-sm tw-rounded" />
                            <div class="image-overlay">
                              <v-icon large color="white">mdi-eye</v-icon>
                            </div>
                          </div>
                       </div>
                       <div class="tw-p-6">
                          <span class="tw-text-xs tw-font-bold tw-text-blue-600 tw-uppercase tw-tracking-wide">Step 02</span>
                          <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-2 tw-mb-2">Select Role</h3>
                          <p class="tw-text-sm tw-text-slate-600">Choose 'Exporter' from the role list.</p>
                       </div>
                    </div>
                     <!-- Step 3 -->
                    <div class="doc-card">
                       <div class="tw-h-48 tw-bg-slate-50 tw-flex tw-items-center tw-justify-center tw-p-4 tw-border-b tw-border-slate-100">
                          <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/003_sign_in.png')">
                            <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/onboarding/003_sign_in.png" class="tw-max-h-full tw-w-auto tw-shadow-sm tw-rounded" />
                            <div class="image-overlay">
                              <v-icon large color="white">mdi-eye</v-icon>
                            </div>
                          </div>
                       </div>
                       <div class="tw-p-6">
                          <span class="tw-text-xs tw-font-bold tw-text-blue-600 tw-uppercase tw-tracking-wide">Step 03</span>
                          <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-2 tw-mb-2">Sign In</h3>
                          <p class="tw-text-sm tw-text-slate-600">Access your secure dashboard.</p>
                       </div>
                    </div>
                </div>
             </section>

             <!-- TWO: ZONE MANAGEMENT -->
             <section id="zone-management" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-text-purple-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="purple">mdi-map-marker-radius</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Zone & Partner Management</h2>
                </div>

                <p class="tw-text-lg tw-text-slate-600 tw-mb-8">
                    Organize your supply chain by creating zones and managing your network of farmers, aggregators, and processors. This structure is essential for EUDR traceability.
                </p>

                <!-- Creating Zones -->
                <div class="tw-bg-slate-50 tw-rounded-2xl tw-p-8 tw-mb-8 tw-border tw-border-slate-200">
                   <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-8 tw-items-center">
                      <div class="md:tw-w-1/2">
                         <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-4">Defining Operational Zones</h3>
                         <p class="tw-text-slate-600 tw-mb-6 tw-leading-relaxed">
                            Zones are geographical groupings of farmers. This allows you to organize collections logistically and conduct regional risk assessments for deforestation alerts.
                         </p>
                         <div class="tw-space-y-3">
                            <div class="tw-flex tw-items-center tw-gap-3">
                               <v-icon small color="green">mdi-check</v-icon>
                               <span class="tw-text-sm tw-font-medium tw-text-slate-700">Streamlined collection routes</span>
                            </div>
                            <div class="tw-flex tw-items-center tw-gap-3">
                               <v-icon small color="green">mdi-check</v-icon>
                               <span class="tw-text-sm tw-font-medium tw-text-slate-700">Regional risk monitoring</span>
                            </div>
                            <div class="tw-flex tw-items-center tw-gap-3">
                               <v-icon small color="green">mdi-check</v-icon>
                               <span class="tw-text-sm tw-font-medium tw-text-slate-700">Better batch tracking</span>
                            </div>
                         </div>
                      </div>
                      <div class="md:tw-w-1/2">
                         <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/zone_mgmt_zone_create.png')">
                           <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/zone_mgmt_zone_create.png" class="tw-w-full tw-rounded-lg tw-shadow-lg tw-border tw-border-slate-200" />
                           <div class="image-overlay">
                             <v-icon large color="white">mdi-eye</v-icon>
                           </div>
                         </div>
                      </div>
                   </div>
                </div>

                <!-- Farmer Management -->
                <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-6">Farmer Management & EUDR Compliance</h3>
                <div class="tw-grid md:tw-grid-cols-2 tw-gap-8 tw-mb-12">
                   <div class="doc-feature-card">
                       <h4 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">1. Add Farmer & Plot Polygon</h4>
                       <p class="tw-text-sm tw-text-slate-600 tw-mb-4">
                           Register each farmer to a zone. <span class="tw-text-green-600 tw-font-bold">Critical for EUDR:</span> You must map the exact geolocation and draw the polygon boundary of every farm plot.
                       </p>
                       <div class="tw-bg-blue-50 tw-p-4 tw-rounded-lg tw-mb-4 tw-border tw-border-blue-100">
                           <p class="tw-text-xs tw-text-blue-900 tw-font-medium"><strong>Why this matters:</strong> The polygon data is cross-referenced with satellite imagery to verify the land was not deforested after <strong>December 31, 2020</strong>—the EUDR cut-off date.</p>
                       </div>
                       <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/farmers_mgmt_add_farmer_plot_polygon.png')">
                         <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/farmers_mgmt_add_farmer_plot_polygon.png" class="tw-w-full tw-rounded-lg tw-shadow-md tw-border tw-border-slate-100" />
                         <div class="image-overlay">
                           <v-icon large color="white">mdi-eye</v-icon>
                         </div>
                       </div>
                   </div>

                   <div class="doc-feature-card">
                       <h4 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">2. Upload Documents</h4>
                       <p class="tw-text-sm tw-text-slate-600 tw-mb-4">
                           Upload identification documents, land deeds, and farm certifications. All documents are hashed and stored on <strong>Hedera Hashgraph</strong> for immutable proof of land tenure.
                       </p>
                       <div class="tw-bg-green-50 tw-p-4 tw-rounded-lg tw-mb-4 tw-border tw-border-green-100">
                           <p class="tw-text-xs tw-text-green-900 tw-font-medium"><strong>Decentralized Storage:</strong> Images are stored on Hedera, ensuring they cannot be tampered with and providing transparent proof to authorities.</p>
                       </div>
                       <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/farmers_mgmt_upload_farmer_doc.png')">
                         <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/farmers_mgmt_upload_farmer_doc.png" class="tw-w-full tw-rounded-lg tw-shadow-md tw-border tw-border-slate-100" />
                         <div class="image-overlay">
                           <v-icon large color="white">mdi-eye</v-icon>
                         </div>
                       </div>
                   </div>
                </div>

                <!-- Aggregators & Processors -->
                <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-6">Connecting to Supply Chain Partners</h3>
                <p class="tw-text-slate-600 tw-mb-6">
                    The supply chain flow is: <strong>Farm → Aggregator → Processor → Exporter → Importer</strong>. You need to register your intermediaries.
                </p>
                <div class="tw-grid md:tw-grid-cols-2 tw-gap-8">
                    <div class="tw-bg-orange-50 tw-p-6 tw-rounded-xl tw-border tw-border-orange-100">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-orange-500 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
                                <v-icon small color="white">mdi-warehouse</v-icon>
                            </div>
                            <h4 class="tw-font-bold tw-text-orange-900">Aggregators</h4>
                        </div>
                        <p class="tw-text-sm tw-text-orange-800 tw-mb-4">Aggregators collect produce from multiple farm points and consolidate them into larger batches for processing.</p>
                        <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/aggregator_mgmt_add_aggregator_you_work_with.png')">
                          <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/aggregator_mgmt_add_aggregator_you_work_with.png" class="tw-w-full tw-rounded-lg tw-shadow-sm" />
                          <div class="image-overlay">
                            <v-icon large color="white">mdi-eye</v-icon>
                          </div>
                        </div>
                    </div>
                    <div class="tw-bg-purple-50 tw-p-6 tw-rounded-xl tw-border tw-border-purple-100">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-purple-500 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
                                <v-icon small color="white">mdi-factory</v-icon>
                            </div>
                            <h4 class="tw-font-bold tw-text-purple-900">Processors</h4>
                        </div>
                        <p class="tw-text-sm tw-text-purple-800 tw-mb-4">Processors perform value-addition activities (e.g., roasting coffee, processing cocoa) before the produce is exported.</p>
                        <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/processor_mgmt_add_processor_you_work_with.png')">
                          <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/processor_mgmt_add_processor_you_work_with.png" class="tw-w-full tw-rounded-lg tw-shadow-sm" />
                          <div class="image-overlay">
                            <v-icon large color="white">mdi-eye</v-icon>
                          </div>
                        </div>
                    </div>
                </div>
             </section>

             <!-- THREE: SUPPLY CHAIN WORKFLOW -->
             <section id="supply-chain" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-orange-100 tw-text-orange-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="orange">mdi-sitemap</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Supply Chain Workflow</h2>
                </div>

                <p class="tw-text-lg tw-text-slate-600 tw-mb-8 tw-border-l-4 tw-border-orange-400 tw-pl-4">
                   Design workflows that trace your produce from farm to export market. The workflow engine guides each stakeholder through specific tasks, ensuring full traceability.
                </p>

                <div class="tw-space-y-12">
                    <div>
                        <h3 class="tw-text-xl tw-font-bold tw-text-slate-900 tw-mb-4">Create a Workflow</h3>
                        <p class="tw-text-slate-600 tw-mb-6">Define the produce type (e.g., Coffee, Cocoa, Palm Oil) and map the flow from farm to importer.</p>
                        <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_003canvas_inside_workflow.png')">
                          <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_003canvas_inside_workflow.png" class="tw-w-full tw-rounded-xl tw-shadow-xl tw-border tw-border-slate-200" />
                          <div class="image-overlay">
                            <v-icon large color="white">mdi-eye</v-icon>
                          </div>
                        </div>
                        <p class="tw-text-center tw-text-xs tw-text-slate-400 tw-mt-2">Interactive Workflow Canvas</p>
                    </div>

                    <div class="tw-bg-slate-50 tw-p-8 tw-rounded-2xl tw-border tw-border-slate-200">
                        <h3 class="tw-text-xl tw-font-bold tw-text-slate-900 tw-mb-6">Workflow Steps</h3>
                        <div class="tw-grid md:tw-grid-cols-3 tw-gap-6">
                            <div class="tw-border tw-border-slate-200 tw-rounded-xl tw-p-4 tw-bg-white">
                                <div class="tw-aspect-video tw-bg-slate-50 tw-rounded-lg tw-mb-4 tw-overflow-hidden tw-flex tw-items-center tw-justify-center">
                                    <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_005create_farm_collection_event.png')">
                                      <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_005create_farm_collection_event.png" class="tw-w-full tw-h-auto" />
                                      <div class="image-overlay">
                                        <v-icon large color="white">mdi-eye</v-icon>
                                      </div>
                                    </div>
                                </div>
                                <h4 class="tw-font-bold tw-text-slate-900">1. Collection Event</h4>
                                <p class="tw-text-xs tw-text-slate-500">Record produce collection from individual farms. This establishes the origin point for traceability.</p>
                            </div>
                            <div class="tw-border tw-border-slate-200 tw-rounded-xl tw-p-4 tw-bg-white">
                                <div class="tw-aspect-video tw-bg-slate-50 tw-rounded-lg tw-mb-4 tw-overflow-hidden tw-flex tw-items-center tw-justify-center">
                                    <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_006create_collection_event.png')">
                                      <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_006create_collection_event.png" class="tw-w-full tw-h-auto" />
                                      <div class="image-overlay">
                                        <v-icon large color="white">mdi-eye</v-icon>
                                      </div>
                                    </div>
                                </div>
                                <h4 class="tw-font-bold tw-text-slate-900">2. Consolidation Event</h4>
                                <p class="tw-text-xs tw-text-slate-500">Combine batches from multiple farms into larger units for processing or export.</p>
                            </div>
                            <div class="tw-border tw-border-slate-200 tw-rounded-xl tw-p-4 tw-bg-white">
                                <div class="tw-aspect-video tw-bg-slate-50 tw-rounded-lg tw-mb-4 tw-overflow-hidden tw-flex tw-items-center tw-justify-center">
                                    <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_007_assess_batch.png')">
                                      <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_007_assess_batch.png" class="tw-w-full tw-h-auto" />
                                      <div class="image-overlay">
                                        <v-icon large color="white">mdi-eye</v-icon>
                                      </div>
                                    </div>
                                </div>
                                <h4 class="tw-font-bold tw-text-slate-900">3. Risk Assessment</h4>
                                <p class="tw-text-xs tw-text-slate-500">Automated EUDR compliance check verifies all source farms are deforestation-free.</p>
                            </div>
                        </div>
                    </div>
                </div>
             </section>

             <!-- FOUR: RISK & MITIGATION -->
             <section id="risk-mitigation" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-red-100 tw-text-red-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="red">mdi-shield-alert</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Risk Assessment & Mitigation</h2>
                </div>

                <p class="tw-text-lg tw-text-slate-600 tw-mb-8">
                    Before export approval, consolidated batches undergo automated risk assessment. If risks are detected, you can create mitigation workflows to improve compliance.
                </p>

                <div class="tw-bg-red-50 tw-p-8 tw-rounded-2xl tw-border tw-border-red-100 tw-mb-12">
                    <h3 class="tw-text-xl tw-font-bold tw-text-red-900 tw-mb-4">How Risk Assessment Works</h3>
                    <p class="tw-text-red-800 tw-mb-6">
                        The system traces back every farm in the consolidated batch and checks:
                    </p>
                    <ul class="tw-space-y-3 tw-text-sm tw-text-red-900">
                        <li class="tw-flex tw-items-start tw-gap-3">
                            <v-icon small color="red">mdi-alert-circle</v-icon>
                            <span><strong>Geolocation Verification:</strong> Are the farm coordinates within high-risk deforestation zones?</span>
                        </li>
                        <li class="tw-flex tw-items-start tw-gap-3">
                            <v-icon small color="red">mdi-alert-circle</v-icon>
                            <span><strong>Satellite Analysis:</strong> Has any deforestation occurred after December 31, 2020 (EUDR cut-off)?</span>
                        </li>
                        <li class="tw-flex tw-items-start tw-gap-3">
                            <v-icon small color="red">mdi-alert-circle</v-icon>
                            <span><strong>Fire Alerts:</strong> Are there recent fire alert hotspots near the farm plots?</span>
                        </li>
                        <li class="tw-flex tw-items-start tw-gap-3">
                            <v-icon small color="red">mdi-alert-circle</v-icon>
                            <span><strong>Documentation:</strong> Are all land tenure documents in order?</span>
                        </li>
                    </ul>
                    <p class="tw-text-red-800 tw-mt-6 tw-text-sm">
                        If <strong>any farm</strong> in the batch is flagged, the entire batch is marked as <strong>non-compliant</strong> and requires mitigation.
                    </p>
                </div>

                <div class="tw-grid md:tw-grid-cols-2 tw-gap-8">
                    <div>
                        <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Assess Your Batch</h3>
                        <p class="tw-text-sm tw-text-slate-600 tw-mb-4">
                            Run the assessment tool on your consolidation. It will return a compliance score and identify problematic farms.
                        </p>
                        <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_007_assess_batch.png')">
                          <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_007_assess_batch.png" class="tw-w-full tw-rounded-lg tw-shadow-md" />
                          <div class="image-overlay">
                            <v-icon large color="white">mdi-eye</v-icon>
                          </div>
                        </div>
                    </div>
                    <div>
                        <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Initiate Mitigation</h3>
                        <p class="tw-text-sm tw-text-slate-600 tw-mb-4">
                            If rejected, create a mitigation workflow. This could involve re-sourcing from compliant farms, obtaining additional certifications, or re-mapping land boundaries.
                        </p>
                        <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_008mitigation_tracking.png')">
                          <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_008mitigation_tracking.png" class="tw-w-full tw-rounded-lg tw-shadow-md" />
                          <div class="image-overlay">
                            <v-icon large color="white">mdi-eye</v-icon>
                          </div>
                        </div>
                        <p class="tw-text-xs tw-text-slate-400 tw-mt-2">Mitigation tracking interface</p>
                    </div>
                </div>
             </section>

             <!-- FIVE: EUDR COMPLIANCE -->
             <section id="eudr-compliance" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-indigo-100 tw-text-indigo-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="indigo">mdi-shield-check</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Understanding EUDR Compliance</h2>
                </div>

                <div class="tw-bg-indigo-50 tw-p-8 tw-rounded-2xl tw-border tw-border-indigo-200 tw-mb-8">
                    <h3 class="tw-text-xl tw-font-bold tw-text-indigo-900 tw-mb-4">What is the EU Deforestation Regulation (EUDR)?</h3>
                    <p class="tw-text-indigo-800 tw-leading-relaxed tw-mb-4">
                        The EUDR, effective from <strong>December 29, 2024</strong>, prohibits placing commodities on the EU market if they:
                    </p>
                    <ul class="tw-space-y-2 tw-text-indigo-900">
                        <li class="tw-flex tw-items-start tw-gap-2">
                            <v-icon small color="indigo">mdi-circle-small</v-icon>
                            <span>Were produced on land deforested after <strong>December 31, 2020</strong></span>
                        </li>
                        <li class="tw-flex tw-items-start tw-gap-2">
                            <v-icon small color="indigo">mdi-circle-small</v-icon>
                            <span>Were produced in violation of laws in the country of production</span>
                        </li>
                    </ul>
                </div>

                <h4 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Key EUDR Requirements</h4>
                <div class="tw-grid md:tw-grid-cols-2 tw-gap-6 tw-mb-8">
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="green">mdi-map-marker</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Geolocation Data</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            You must provide the exact GPS coordinates and polygon boundaries of <strong>all production plots</strong>. AgriBackup records this during farmer registration.
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="blue">mdi-earth</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Deforestation-Free Status</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            Satellite imagery analysis verifies no deforestation occurred after Dec 31, 2020. AgriBackup stores this verification result on <strong>Hedera DLT</strong>.
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="orange">mdi-file-document</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Due Diligence Statement</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            Before import, operators must submit a <strong>DDS</strong> to authorities. AgriBackup generates this automatically from your workflow data.
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="purple">mdi-link-variant</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Full Traceability</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            Complete chain of custody from farm to port. Every transfer between Farmer, Aggregator, Processor, Exporter, and Importer is recorded.
                        </p>
                    </div>
                </div>

                <div class="tw-bg-yellow-50 tw-p-6 tw-rounded-xl tw-border tw-border-yellow-200">
                    <div class="tw-flex tw-items-start tw-gap-3">
                        <v-icon color="amber">mdi-alert</v-icon>
                        <div>
                            <p class="tw-font-bold tw-text-yellow-900 tw-mb-2">Non-Compliance Penalties</p>
                            <p class="tw-text-sm tw-text-yellow-800">
                                Violations can result in fines up to <strong>4% of annual EU turnover</strong>, confiscation of goods, and exclusion from public procurement. AgriBackup helps you avoid these risks.
                            </p>
                        </div>
                    </div>
                </div>
             </section>

             <!-- SIX: AUTHORISED REPRESENTATIVE -->
             <section id="authorised-representative" class="tw-mb-24 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-text-purple-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="purple">mdi-account-tie</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Authorised Representative (EUDR Article 6)</h2>
                </div>

                <div class="tw-bg-purple-50 tw-p-8 tw-rounded-2xl tw-border tw-border-purple-200 tw-mb-8">
                    <h3 class="tw-text-xl tw-font-bold tw-text-purple-900 tw-mb-4">Who needs an Authorised Representative?</h3>
                    <p class="tw-text-purple-800 tw-leading-relaxed tw-mb-4">
                        Under <strong>EUDR Article 6</strong>, operators established <strong>outside the European Union</strong> who wish to place commodities on the EU market must designate an <strong>Authorised Representative (AR)</strong> established within an EU Member State.
                    </p>
                    <div class="tw-bg-white tw-p-4 tw-rounded-lg tw-border tw-border-purple-100">
                        <p class="tw-text-sm tw-text-purple-900 tw-font-medium tw-mb-2">
                            <v-icon small color="purple">mdi-information</v-icon>
                            <strong>Example:</strong> A coffee exporter in Kenya needs an AR in Germany, Netherlands, or any EU country to submit Due Diligence Statements on their behalf.
                        </p>
                    </div>
                </div>

                <h4 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Key AR Requirements</h4>
                <div class="tw-grid md:tw-grid-cols-2 tw-gap-6 tw-mb-8">
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="blue">mdi-card-account-details</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">EORI Number</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            Your AR must have a valid <strong>Economic Operators Registration and Identification (EORI)</strong> number. This is required for EU customs and TRACES system access.
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="green">mdi-map-marker-check</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">EU Establishment</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            The AR must be legally established in one of the <strong>27 EU Member States</strong>. They take on legal responsibility for DDS submissions.
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="orange">mdi-file-sign</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Mandate Agreement</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            You must formally appoint your AR through a <strong>mandate</strong> specifying the scope of authority (Full, DDS-only, or View-only access).
                        </p>
                    </div>
                    <div class="tw-bg-white tw-p-6 tw-rounded-xl tw-border tw-border-slate-200">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-3">
                            <v-icon color="red">mdi-gavel</v-icon>
                            <h5 class="tw-font-bold tw-text-slate-900">Legal Responsibility</h5>
                        </div>
                        <p class="tw-text-sm tw-text-slate-600">
                            The AR shares responsibility for EUDR compliance. They can be held liable if due diligence obligations are not met.
                        </p>
                    </div>
                </div>

                <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-6">How to Designate an AR in AgriBackup</h3>
                <div class="tw-bg-slate-50 tw-p-8 tw-rounded-2xl tw-border tw-border-slate-200">
                    <div class="tw-space-y-6">
                        <div class="tw-flex tw-items-start tw-gap-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-purple-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-text-white tw-font-bold tw-flex-shrink-0">1</div>
                            <div>
                                <h4 class="tw-font-bold tw-text-slate-900 tw-mb-1">Access AR Management</h4>
                                <p class="tw-text-slate-600 tw-text-sm">From your EUDR Dashboard, click "Manage AR" in the alert banner, or navigate to EUDR → Authorised Representative.</p>
                            </div>
                        </div>
                        <div class="tw-flex tw-items-start tw-gap-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-purple-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-text-white tw-font-bold tw-flex-shrink-0">2</div>
                            <div>
                                <h4 class="tw-font-bold tw-text-slate-900 tw-mb-1">Search for Available ARs</h4>
                                <p class="tw-text-slate-600 tw-text-sm">Browse verified ARs by country, name, or EORI number. All listed ARs are verified and accepting new mandates.</p>
                            </div>
                        </div>
                        <div class="tw-flex tw-items-start tw-gap-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-purple-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-text-white tw-font-bold tw-flex-shrink-0">3</div>
                            <div>
                                <h4 class="tw-font-bold tw-text-slate-900 tw-mb-1">Send Invitation</h4>
                                <p class="tw-text-slate-600 tw-text-sm">Select an AR and send an invitation. Choose the mandate scope and optional expiry date.</p>
                            </div>
                        </div>
                        <div class="tw-flex tw-items-start tw-gap-4">
                            <div class="tw-w-10 tw-h-10 tw-bg-green-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-text-white tw-font-bold tw-flex-shrink-0">4</div>
                            <div>
                                <h4 class="tw-font-bold tw-text-slate-900 tw-mb-1">AR Accepts Mandate</h4>
                                <p class="tw-text-slate-600 tw-text-sm">Once the AR accepts, they can submit DDS through EU TRACES using their EORI credentials on your behalf.</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="tw-mt-8 tw-bg-blue-50 tw-p-6 tw-rounded-xl tw-border tw-border-blue-200">
                    <div class="tw-flex tw-items-start tw-gap-3">
                        <v-icon color="blue">mdi-lightbulb</v-icon>
                        <div>
                            <p class="tw-font-bold tw-text-blue-900 tw-mb-2">Tip: Becoming an Authorised Representative</p>
                            <p class="tw-text-sm tw-text-blue-800">
                                If you're based in the EU and want to offer AR services, you can register as an Authorised Representative from the Sign Up page. You'll need a valid EORI number and EU business registration.
                            </p>
                        </div>
                    </div>
                </div>
             </section>

             <!-- SEVEN: CERTIFICATES & SHARING -->
             <section id="certificates" class="tw-mb-16 tw-scroll-mt-32">
                <div class="tw-flex tw-items-center tw-gap-4 tw-mb-8">
                   <div class="tw-w-12 tw-h-12 tw-bg-green-100 tw-text-green-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="green">mdi-certificate-outline</v-icon>
                   </div>
                   <h2 class="tw-text-3xl tw-font-bold tw-text-slate-900 tw-m-0">Certificates & Reporting</h2>
                </div>

                <div class="tw-bg-gradient-to-br tw-from-slate-900 tw-to-slate-800 tw-text-white tw-rounded-3xl tw-p-8 md:tw-p-12 tw-relative tw-overflow-hidden tw-mb-12">
                    <!-- Background blobs -->
                    <div class="tw-absolute tw-top-0 tw-right-0 tw-w-96 tw-h-96 tw-bg-green-500/10 tw-rounded-full tw-blur-3xl tw--mr-20 tw--mt-20"></div>

                   <div class="tw-flex flex-col md:tw-flex-row tw-gap-12 tw-items-center tw-relative tw-z-10">
                       <div class="md:tw-w-1/2">
                           <div class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-green-500/20 tw-border tw-border-green-500/30 tw-rounded-full tw-px-3 tw-py-1 tw-mb-6">
                               <v-icon small color="green lighten-3">mdi-check-decagram</v-icon>
                               <span class="tw-text-xs tw-font-bold tw-text-green-300">Hedera Hashgraph Verified</span>
                           </div>
                           <h3 class="tw-text-3xl tw-font-bold tw-text-white tw-mb-4">NFT Digital Product Passports</h3>
                           <p class="tw-text-slate-300 tw-mb-6 tw-leading-relaxed">
                               When creating a <strong>Shipment Event</strong> to the Importer, AgriBackup automatically mints a unique <strong>Digital Product Passport (NFT)</strong> on the Hedera network. This certificate is immutable proof of EUDR compliance.
                           </p>
                           <div class="tw-space-y-3">
                               <div class="tw-flex tw-items-center tw-gap-3">
                                   <div class="tw-w-6 tw-h-6 tw-rounded-full tw-bg-green-500/20 tw-flex tw-items-center tw-justify-center">
                                       <v-icon x-small color="green lighten-3">mdi-check</v-icon>
                                   </div>
                                   <span class="tw-text-sm tw-text-slate-200">Blockchain-verified deforestation-free status</span>
                               </div>
                               <div class="tw-flex tw-items-center tw-gap-3">
                                   <div class="tw-w-6 tw-h-6 tw-rounded-full tw-bg-green-500/20 tw-flex tw-items-center tw-justify-center">
                                       <v-icon x-small color="green lighten-3">mdi-check</v-icon>
                                   </div>
                                   <span class="tw-text-sm tw-text-slate-200">Full traceability from farm to export</span>
                               </div>
                               <div class="tw-flex tw-items-center tw-gap-3">
                                   <div class="tw-w-6 tw-h-6 tw-rounded-full tw-bg-green-500/20 tw-flex tw-items-center tw-justify-center">
                                       <v-icon x-small color="green lighten-3">mdi-check</v-icon>
                                   </div>
                                   <span class="tw-text-sm tw-text-slate-200">Accepted by EU customs authorities</span>
                               </div>
                           </div>
                       </div>
                       <div class="md:tw-w-1/2">
                           <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_009Verify_minted_certificate.png')">
                             <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_009Verify_minted_certificate.png" class="tw-w-full tw-rounded-xl tw-shadow-2xl tw-border-4 tw-border-white/10" />
                             <div class="image-overlay">
                               <v-icon large color="white">mdi-eye</v-icon>
                             </div>
                           </div>
                           <p class="tw-text-center tw-text-xs tw-text-slate-400 tw-mt-3">Certificate Viewer Interface</p>
                       </div>
                   </div>
                </div>

                <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-6">Sharing Certificates</h3>
                <p class="tw-text-slate-600 tw-mb-6">
                    Once minted, you can share the certificate with authorities and importers in two ways:
                </p>
                <div class="tw-grid md:tw-grid-cols-2 tw-gap-8 tw-mb-12">
                    <div class="tw-bg-blue-50 tw-p-6 tw-rounded-xl tw-border tw-border-blue-100">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-4">
                            <div class="tw-w-12 tw-h-12 tw-bg-blue-500 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
                                <v-icon color="white">mdi-qrcode</v-icon>
                            </div>
                            <h4 class="tw-font-bold tw-text-blue-900">1. QR Code Scanning</h4>
                        </div>
                        <p class="tw-text-sm tw-text-blue-800 tw-mb-4">
                            The certificate viewer displays a <strong>QR code</strong>. Authorities or importers can scan this QR code with their phones to instantly verify the certificate on the Hedera blockchain.
                        </p>
                        <div class="tw-bg-blue-100 tw-p-3 tw-rounded tw-text-xs tw-text-blue-900">
                            <strong>Use Case:</strong> Physical inspections at ports or border crossings.
                        </div>
                    </div>
                    <div class="tw-bg-purple-50 tw-p-6 tw-rounded-xl tw-border tw-border-purple-100">
                        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-4">
                            <div class="tw-w-12 tw-h-12 tw-bg-purple-500 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
                                <v-icon color="white">mdi-link-variant</v-icon>
                            </div>
                            <h4 class="tw-font-bold tw-text-purple-900">2. Direct Certificate Link</h4>
                        </div>
                        <p class="tw-text-sm tw-text-purple-800 tw-mb-4">
                            Click the <strong>"Share Link"</strong> button to copy a direct URL to the certificate hosted on Hedera. You can email this link or include it in your compliance documentation.
                        </p>
                        <div class="tw-bg-purple-100 tw-p-3 tw-rounded tw-text-xs tw-text-purple-900">
                            <strong>Use Case:</strong> Digital submissions to regulatory authorities.
                        </div>
                    </div>
                </div>

                <h3 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-mb-6">Authority Reporting</h3>
                <p class="tw-text-slate-600 tw-mb-6">
                    Submit the comprehensive <strong>Due Diligence Statement (DDS)</strong> to regulatory authorities directly from AgriBackup. The report includes:
                </p>
                <ul class="tw-space-y-2 tw-text-slate-700 tw-mb-6">
                    <li class="tw-flex tw-items-start tw-gap-2">
                        <v-icon small color="green">mdi-check</v-icon>
                        <span>Complete list of all production units with geolocations</span>
                    </li>
                    <li class="tw-flex tw-items-start tw-gap-2">
                        <v-icon small color="green">mdi-check</v-icon>
                        <span>Deforestation-free verification results</span>
                    </li>
                    <li class="tw-flex tw-items-start tw-gap-2">
                        <v-icon small color="green">mdi-check</v-icon>
                        <span>Risk assessment outcomes</span>
                    </li>
                    <li class="tw-flex tw-items-start tw-gap-2">
                        <v-icon small color="green">mdi-check</v-icon>
                        <span>Chain of custody documentation</span>
                    </li>
                    <li class="tw-flex tw-items-start tw-gap-2">
                        <v-icon small color="green">mdi-check</v-icon>
                        <span>NFT certificate reference on Hedera</span>
                    </li>
                </ul>
                <div class="image-wrapper" @click="openFullscreen('https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_010_authority_reporting.png')">
                  <img src="https://agri-image.s3.us-east-1.amazonaws.com/doc/exporter/supply_chain_workflow_010_authority_reporting.png" class="tw-w-full tw-max-w-3xl tw-mx-auto tw-rounded-xl tw-shadow-lg tw-border tw-border-slate-200" />
                  <div class="image-overlay">
                    <v-icon large color="white">mdi-eye</v-icon>
                  </div>
                </div>
                <p class="tw-text-center tw-text-xs tw-text-slate-400 tw-mt-3">Authority Reporting Dashboard</p>
             </section>

          </main>
       </div>
    </div>

    <!-- Reusable Professional Footer -->
    <AppFooter />

    <!-- Fullscreen Image Modal -->
    <v-dialog v-model="isFullscreenOpen" max-width="1400px" content-class="fullscreen-dialog">
      <v-card dark class="tw-bg-black">
        <v-card-title class="tw-flex tw-justify-end tw-p-2">
          <v-btn icon @click="closeFullscreen">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text class="tw-p-4">
          <img :src="fullscreenImageSrc" class="tw-w-full tw-h-auto tw-max-h-[85vh] tw-object-contain tw-mx-auto" style="display: block;" />
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import AppHeader from '@/components/layout/AppHeader.vue';
import AppFooter from '@/components/layout/AppFooter.vue';

export default {
  name: 'DocumentationPage',
  components: {
    AppHeader,
    AppFooter,
  },
  data() {
    return {
      activeSection: 'getting-started',
      navItems: [
        { id: 'getting-started', label: '1. Getting Started' },
        { id: 'zone-management', label: '2. Zone & Partners' },
        { id: 'supply-chain', label: '3. Supply Chain' },
        { id: 'risk-mitigation', label: '4. Risk & Mitigation' },
        { id: 'eudr-compliance', label: '5. EUDR Compliance' },
        { id: 'authorised-representative', label: '6. Authorised Rep (EU)' },
        { id: 'certificates', label: '7. Certificates' },
      ],
      isFullscreenOpen: false,
      fullscreenImageSrc: '',
    };
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll);
  },
  methods: {
    openFullscreen(imageSrc) {
      this.fullscreenImageSrc = imageSrc;
      this.isFullscreenOpen = true;
    },
    closeFullscreen() {
      this.isFullscreenOpen = false;
      this.fullscreenImageSrc = '';
    },
    scrollTo(id) {
      const element = document.getElementById(id);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
        this.activeSection = id;
      }
    },
    handleScroll() {
      const sections = this.navItems.map((i) => i.id);
      // eslint-disable-next-line no-restricted-syntax
      for (const section of sections) {
        const element = document.getElementById(section);
        if (element) {
          const rect = element.getBoundingClientRect();
          // Simple logic: if top of section is near the top of viewport (considering header offset)
          // or if it's visible.
          if (rect.top >= 0 && rect.top <= 400) {
            this.activeSection = section;
            break;
          }
        }
      }
    },
  },
};
</script>

<style scoped>
.doc-card {
   background: white;
   border: 1px solid #e2e8f0;
   border-radius: 12px;
   overflow: hidden;
   transition: transform 0.2s, box-shadow 0.2s;
}
.doc-card:hover {
   transform: translateY(-4px);
   box-shadow: 0 12px 24px -10px rgba(0,0,0,0.1);
}

.doc-feature-card {
    background: #f8fafc; /* slate-50 */
    padding: 24px;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
}

/* Image Wrapper with Overlay */
.image-wrapper {
    position: relative;
    cursor: pointer;
    overflow: hidden;
    display: inline-block;
    width: 100%;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.image-wrapper:hover .image-overlay {
    opacity: 1;
}

/* Fullscreen dialog styling */
::v-deep .fullscreen-dialog {
    z-index: 9999 !important;
}
</style>
