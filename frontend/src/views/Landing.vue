<!-- eslint-disable sonarjs/no-duplicate-string -->
<template>
  <div class="tw-bg-gradient-to-br tw-from-[#f7f5ee] tw-to-[#e6f4ea] tw-min-h-screen tw-flex tw-flex-col tw-relative">
    <!-- Responsive Glassmorphism Navbar -->
    <nav class="tw-fixed tw-top-0 tw-left-0 tw-w-full tw-z-50 tw-bg-[#f7f5ee] tw-bg-opacity-80 tw-backdrop-blur-lg tw-shadow-lg tw-border-b tw-border-[#e6e1c3]">
      <div class="tw-max-w-7xl tw-mx-auto tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-3">
        <div class="tw-flex tw-items-center">
          <!-- Creative logo: AGRIBackUp with SVG leaf accent -->
          <span class="tw-font-extrabold tw-text-green-800 tw-text-3xl md:tw-text-4xl tw-tracking-tight tw-select-none tw-drop-shadow-sm tw-flex tw-items-center">
            Agri
            <span class="tw-text-green-900 tw-ml-1">
              Backup
            </span>
          </span>
        </div>
        <div class="tw-flex tw-items-center tw-space-x-2">
          <button
            class="tw-hidden sm:tw-inline-block tw-text-green-700 tw-font-semibold tw-px-4 tw-py-2 tw-rounded-lg hover:tw-bg-green-100 tw-transition"
            aria-label="Login"
            @click="handleLogin"
          >
            Login
          </button>
          <button
            class="tw-bg-green-600 hover:tw-bg-green-700 tw-text-white tw-font-semibold tw-px-6 tw-py-2 tw-rounded-lg tw-shadow tw-transition"
            aria-label="Create Account"
            @click="handleSignup"
          >
            Create Account
          </button>
        </div>
      </div>
    </nav>
    <!-- Spacer for navbar height -->
    <div class="tw-h-24"></div>
    <!-- Cozy Curved SVG Line Background with agricultural colors -->
    <svg
      class="tw-absolute tw-left-0 tw-w-full tw-h-56 tw-z-0 tw-pointer-events-none"
      style="top: 90px; min-width:100vw;"
      viewBox="0 0 1440 224"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <path
        d="M0,112 Q720,224 1440,112"
        stroke="#b6a16b"
        stroke-width="12"
        fill="none"
        opacity="0.18"
      />
      <ellipse cx="200" cy="60" rx="120" ry="32" fill="#e6e1c3" opacity="0.18"/>
      <ellipse cx="1240" cy="180" rx="80" ry="24" fill="#b6e4a3" opacity="0.18"/>
    </svg>

  <!-- Above the Fold -->
  <section class="reveal tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between tw-px-10 tw-pt-20 tw-pb-12 tw-max-w-6xl tw-mx-auto">
      <div class="tw-flex-1 tw-mb-12 md:tw-mb-0">
        <h1 class="tw-text-4xl md:tw-text-6xl tw-font-bold tw-text-green-900 tw-mb-6 tw-drop-shadow">
          EUDR-Compliant Supply Chain Platform
        </h1>
        <p class="tw-text-xl tw-text-[#6b4f2c] tw-mr-3 tw-mb-8 tw-drop-shadow-sm">
          Deforestation-free agriculture tracking powered by Hedera DLT. Full transparency from farm to export.
        </p>
        <!-- EUDR Compliance Countdown Timer -->
        <div class="tw-text-center tw-mb-8">
          <h3 class="tw-text-2xl tw-font-bold tw-text-red-600 tw-mb-4">EUDR Compliance Deadline Approaching</h3>
          <div class="tw-flex tw-justify-center tw-space-x-4 tw-mb-4">
            <div class="tw-bg-white tw-rounded-lg tw-shadow-lg tw-p-4 tw-text-center tw-min-w-[80px]">
              <div class="tw-text-3xl tw-font-bold tw-text-green-800">{{ timeLeft.days }}</div>
              <div class="tw-text-sm tw-text-gray-600">Days</div>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-shadow-lg tw-p-4 tw-text-center tw-min-w-[80px]">
              <div class="tw-text-3xl tw-font-bold tw-text-green-800">{{ timeLeft.hours }}</div>
              <div class="tw-text-sm tw-text-gray-600">Hours</div>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-shadow-lg tw-p-4 tw-text-center tw-min-w-[80px]">
              <div class="tw-text-3xl tw-font-bold tw-text-green-800">{{ timeLeft.minutes }}</div>
              <div class="tw-text-sm tw-text-gray-600">Minutes</div>
            </div>
            <div class="tw-bg-white tw-rounded-lg tw-shadow-lg tw-p-4 tw-text-center tw-min-w-[80px]">
              <div class="tw-text-3xl tw-font-bold tw-text-green-800">{{ timeLeft.seconds }}</div>
              <div class="tw-text-sm tw-text-gray-600">Seconds</div>
            </div>
          </div>
          <p class="tw-text-lg tw-text-gray-700">Until December 31, 2025 - Ensure Your Supply Chain is EUDR Compliant!</p>
        </div>
        <div class="tw-flex tw-gap-6 tw-mb-6">
          <button
            @click="goHome"
            class="tw-bg-green-900 hover:tw-bg-[#5e9442] tw-text-white tw-font-semibold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-border tw-border-[#e6e1c3] tw-transition tw-text-xl"
            aria-label="Join Now"
          >
            Get Started
          </button>
          <button
            @click="showDialog = true"
            class="tw-bg-[#f7e9b0] hover:tw-bg-[#f3d97c] tw-text-[#6b4f2c] tw-font-semibold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-border tw-border-[#e6e1c3] tw-transition tw-text-xl"
            aria-label="Learn How It Works"
          >
            Learn More
          </button>
        </div>
      </div>
      <div class="tw-flex-1 tw-flex tw-justify-center">
        <img
          src="@/assets/images/happy-farmer.jpg"
          alt="Smiling farmer using AgriBackup app"
          class="tw-rounded-2xl tw-shadow-2xl tw-w-96 tw-h-96 tw-object-cover tw-bg-[#f7f5ee] tw-border-4 tw-border-[#e6e1c3]"
        />
      </div>
    </section>

    <!-- Supply Chain Flow Diagram -->
    <section class="reveal tw-bg-white tw-py-16 tw-px-6 tw-max-w-7xl tw-mx-auto tw-mb-16">
      <div class="tw-text-center tw-mb-12">
        <h2 class="tw-text-4xl tw-font-bold tw-text-green-800 tw-mb-4">How the Supply Chain Works</h2>
        <p class="tw-text-xl tw-text-[#6b4f2c] tw-max-w-3xl tw-mx-auto">
          From farm to export: Complete traceability through every stage of the agricultural supply chain
        </p>
      </div>

      <!-- Flow Diagram -->
      <div class="tw-relative">
        <!-- Desktop Flow (hidden on mobile) -->
        <div class="tw-hidden md:tw-block">
          <div class="tw-flex tw-items-center tw-justify-between tw-max-w-6xl tw-mx-auto">
            <!-- Farmer -->
            <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-max-w-xs">
              <div class="tw-w-20 tw-h-20 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4 tw-shadow-lg">
                <v-icon size="40" color="green darken-2">mdi-account-hard-hat</v-icon>
              </div>
              <h3 class="tw-text-lg tw-font-bold tw-text-green-800 tw-mb-2">Farmer</h3>
              <p class="tw-text-sm tw-text-[#6b4f2c] tw-leading-tight">
                Grows and harvests produce, manages production units, ensures EUDR compliance
              </p>
            </div>

            <!-- Arrow 1 -->
            <div class="tw-flex tw-items-center tw-flex-1 tw-justify-center">
              <div class="tw-flex tw-items-center">
                <div class="tw-w-8 tw-h-0.5 tw-bg-green-400"></div>
                <v-icon color="green" class="tw-mx-2">mdi-arrow-right</v-icon>
                <div class="tw-w-8 tw-h-0.5 tw-bg-green-400"></div>
              </div>
            </div>

            <!-- Aggregator/Collector -->
            <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-max-w-xs">
              <div class="tw-w-20 tw-h-20 tw-bg-orange-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4 tw-shadow-lg">
                <v-icon size="40" color="orange darken-2">mdi-package-variant</v-icon>
              </div>
              <h3 class="tw-text-lg tw-font-bold tw-text-orange-800 tw-mb-2">Aggregator/Collector</h3>
              <p class="tw-text-sm tw-text-[#6b4f2c] tw-leading-tight">
                Collects produce from farmers, performs initial quality checks and consolidation
              </p>
            </div>

            <!-- Arrow 2 -->
            <div class="tw-flex tw-items-center tw-flex-1 tw-justify-center">
              <div class="tw-flex tw-items-center">
                <div class="tw-w-8 tw-h-0.5 tw-bg-orange-400"></div>
                <v-icon color="orange" class="tw-mx-2">mdi-arrow-right</v-icon>
                <div class="tw-w-8 tw-h-0.5 tw-bg-orange-400"></div>
              </div>
            </div>

            <!-- Processor -->
            <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-max-w-xs">
              <div class="tw-w-20 tw-h-20 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4 tw-shadow-lg">
                <v-icon size="40" color="purple darken-2">mdi-cog</v-icon>
              </div>
              <h3 class="tw-text-lg tw-font-bold tw-text-purple-800 tw-mb-2">Processor</h3>
              <p class="tw-text-sm tw-text-[#6b4f2c] tw-leading-tight">
                Processes and prepares produce for export, creates EUDR-compliant batches
              </p>
            </div>

            <!-- Arrow 3 -->
            <div class="tw-flex tw-items-center tw-flex-1 tw-justify-center">
              <div class="tw-flex tw-items-center">
                <div class="tw-w-8 tw-h-0.5 tw-bg-purple-400"></div>
                <v-icon color="purple" class="tw-mx-2">mdi-arrow-right</v-icon>
                <div class="tw-w-8 tw-h-0.5 tw-bg-purple-400"></div>
              </div>
            </div>

            <!-- Exporter -->
            <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-max-w-xs">
              <div class="tw-w-20 tw-h-20 tw-bg-blue-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4 tw-shadow-lg">
                <v-icon size="40" color="blue darken-2">mdi-truck-delivery</v-icon>
              </div>
              <h3 class="tw-text-lg tw-font-bold tw-text-blue-800 tw-mb-2">Exporter</h3>
              <p class="tw-text-sm tw-text-[#6b4f2c] tw-leading-tight">
                Manages supply chain workflows, ensures traceability, handles export logistics
              </p>
            </div>

            <!-- Arrow 4 -->
            <div class="tw-flex tw-items-center tw-flex-1 tw-justify-center">
              <div class="tw-flex tw-items-center">
                <div class="tw-w-8 tw-h-0.5 tw-bg-blue-400"></div>
                <v-icon color="blue" class="tw-mx-2">mdi-arrow-right</v-icon>
                <div class="tw-w-8 tw-h-0.5 tw-bg-blue-400"></div>
              </div>
            </div>

            <!-- Importer -->
            <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-max-w-xs">
              <div class="tw-w-20 tw-h-20 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4 tw-shadow-lg">
                <v-icon size="40" color="green darken-2">mdi-storefront</v-icon>
              </div>
              <h3 class="tw-text-lg tw-font-bold tw-text-green-800 tw-mb-2">Importer</h3>
              <p class="tw-text-sm tw-text-[#6b4f2c] tw-leading-tight">
                Receives verified produce, completes the supply chain with full traceability
              </p>
            </div>
          </div>
        </div>

        <!-- Mobile Flow (hidden on desktop) -->
        <div class="tw-block md:tw-hidden">
          <div class="tw-space-y-8">
            <!-- Farmer -->
            <div class="tw-flex tw-items-center tw-space-x-4">
              <div class="tw-w-16 tw-h-16 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon size="32" color="green darken-2">mdi-account-hard-hat</v-icon>
              </div>
              <div>
                <h3 class="tw-text-lg tw-font-bold tw-text-green-800">1. Farmer</h3>
                <p class="tw-text-sm tw-text-[#6b4f2c]">Grows and harvests produce, ensures EUDR compliance</p>
              </div>
            </div>

            <!-- Arrow -->
            <div class="tw-flex tw-justify-center">
              <v-icon color="green" size="24">mdi-arrow-down</v-icon>
            </div>

            <!-- Aggregator/Collector -->
            <div class="tw-flex tw-items-center tw-space-x-4">
              <div class="tw-w-16 tw-h-16 tw-bg-orange-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon size="32" color="orange darken-2">mdi-package-variant</v-icon>
              </div>
              <div>
                <h3 class="tw-text-lg tw-font-bold tw-text-orange-800">2. Aggregator/Collector</h3>
                <p class="tw-text-sm tw-text-[#6b4f2c]">Collects and consolidates produce from farmers</p>
              </div>
            </div>

            <!-- Arrow -->
            <div class="tw-flex tw-justify-center">
              <v-icon color="orange" size="24">mdi-arrow-down</v-icon>
            </div>

            <!-- Processor -->
            <div class="tw-flex tw-items-center tw-space-x-4">
              <div class="tw-w-16 tw-h-16 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon size="32" color="purple darken-2">mdi-cog</v-icon>
              </div>
              <div>
                <h3 class="tw-text-lg tw-font-bold tw-text-purple-800">3. Processor</h3>
                <p class="tw-text-sm tw-text-[#6b4f2c]">Processes produce and creates EUDR-compliant batches</p>
              </div>
            </div>

            <!-- Arrow -->
            <div class="tw-flex tw-justify-center">
              <v-icon color="purple" size="24">mdi-arrow-down</v-icon>
            </div>

            <!-- Exporter -->
            <div class="tw-flex tw-items-center tw-space-x-4">
              <div class="tw-w-16 tw-h-16 tw-bg-blue-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon size="32" color="blue darken-2">mdi-truck-delivery</v-icon>
              </div>
              <div>
                <h3 class="tw-text-lg tw-font-bold tw-text-blue-800">4. Exporter</h3>
                <p class="tw-text-sm tw-text-[#6b4f2c]">Manages workflows and handles export logistics</p>
              </div>
            </div>

            <!-- Arrow -->
            <div class="tw-flex tw-justify-center">
              <v-icon color="blue" size="24">mdi-arrow-down</v-icon>
            </div>

            <!-- Importer -->
            <div class="tw-flex tw-items-center tw-space-x-4">
              <div class="tw-w-16 tw-h-16 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon size="32" color="green darken-2">mdi-storefront</v-icon>
              </div>
              <div>
                <h3 class="tw-text-lg tw-font-bold tw-text-green-800">5. Importer</h3>
                <p class="tw-text-sm tw-text-[#6b4f2c]">Receives verified produce with full traceability</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Blockchain Integration Note -->
        <div class="tw-mt-12 tw-text-center">
          <div class="tw-inline-flex tw-items-center tw-bg-gradient-to-r tw-from-blue-50 tw-to-purple-50 tw-px-6 tw-py-3 tw-rounded-full tw-border tw-border-blue-200">
            <v-icon color="purple" class="tw-mr-2">mdi-shield-check</v-icon>
            <span class="tw-text-sm tw-font-medium tw-text-purple-800">
              Every step is recorded on Hedera blockchain for immutable traceability
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- For Farmers -->
  <section class="reveal tw-bg-[#f7f5ee] tw-py-12 tw-px-10 tw-max-w-6xl tw-mx-auto tw-rounded-2xl tw-shadow-lg tw-mb-12 tw-flex tw-flex-col md:tw-flex-row tw-items-start tw-border-2 tw-border-[#b6a16b] tw-w-2/3">
        <div class="tw-flex-1 tw-mb-8 md:tw-mb-0">
          <h2 class="tw-text-3xl tw-font-semibold tw-text-[#3d5a2a] tw-mb-4">For Farmers</h2>
          <ul class="tw-list-disc tw-list-inside tw-text-lg tw-text-[#6b4f2c] tw-mb-6">
            <li>Draw and verify your production unit polygons for EUDR compliance</li>
            <li>Monitor deforestation alerts with satellite imagery</li>
            <li>Manage compliance documents and certificates</li>
            <li>Track your batches through the supply chain on Hedera blockchain</li>
          </ul>
          <button
            @click="joinAsFarmer"
            class="tw-bg-green-800 hover:tw-bg-[#5e9442] tw-text-white tw-font-semibold tw-py-3 tw-px-8 tw-rounded-xl tw-shadow tw-border tw-border-[#e6e1c3] tw-transition tw-text-lg"
            aria-label="Join as a Farmer"
          >
            Join as a Farmer
          </button>
        </div>
        <div class="tw-flex-1 tw-flex tw-justify-center">
          <img
            src="@/assets/images/farmer-phone.jpg"
            alt="Farmer checking prices on phone"
            class="tw-rounded-2xl tw-shadow-lg tw-w-72 tw-h-72 tw-object-cover tw-bg-[#f7f5ee] tw-border-4 tw-border-[#e6e1c3]"
          />
        </div>
      </section>

    <!-- For Buyers -->
  <section class="reveal tw-bg-[#f7f5ee] tw-py-12 tw-px-10 tw-max-w-6xl tw-mx-auto tw-rounded-2xl tw-shadow-lg tw-mb-12 tw-flex tw-flex-col md:tw-flex-row-reverse tw-items-center">
      <div class="tw-flex-1 tw-mb-8 md:tw-mb-0">
        <h2 class="tw-text-3xl tw-font-semibold tw-text-[#2a4f6b] tw-mb-4">For Buyers</h2>
        <ul class="tw-list-disc tw-list-inside tw-text-lg tw-text-[#6b4f2c] tw-mb-6">
          <li>Source quality fresh produce directly from verified farmers</li>
          <li>Registers your own farmers and manage them</li>
          <li>Send Automated SMS to farmers</li>
          <li>Optimize your supply chain with data-driven insights</li>
        </ul>
        <button
          @click="joinAsBuyer"
          class="tw-bg-blue-800 hover:tw-bg-[#7bb661] tw-text-white tw-font-semibold tw-py-3 tw-px-8 tw-rounded-xl tw-shadow tw-border tw-border-[#e6e1c3] tw-transition tw-text-lg tw-mb-2"
          aria-label="Join as a Buyer"
        >
          Join as a Buyer
        </button>
      </div>
      <div class="tw-flex-1 tw-flex tw-justify-center tw-px-3">
        <img
          src="@/assets/images/buyer-inspect.jpg"
          alt="Buyer inspecting produce"
          class="tw-rounded-2xl tw-shadow-lg tw-w-72 tw-h-72 tw-object-cover tw-bg-[#f7f5ee] tw-border-4 tw-border-[#e6e1c3]"
        />
      </div>
    </section>

    <!-- For Exporters & Supply Chain Actors -->
    <div class="tw-w-full tw-flex tw-justify-start tw-items-start">
  <section class="reveal tw-bg-[#f7f5ee] tw-py-12 tw-px-10 tw-max-w-6xl tw-mx-auto tw-rounded-2xl tw-shadow-lg tw-mb-12 tw-flex tw-flex-col md:tw-flex-row-reverse tw-items-center">
        <div class="tw-flex-1 tw-mb-8 md:tw-mb-0">
          <h2 class="tw-text-3xl tw-font-semibold tw-text-[#b6a16b] tw-mb-4">For Exporters & Supply Chain</h2>
          <ul class="tw-list-disc tw-list-inside tw-text-lg tw-text-[#6b4f2c] tw-mb-6">
            <li>Conduct comprehensive due diligence for EUDR compliance</li>
            <li>Track and verify deforestation-free supply chains</li>
            <li>Manage aggregators, processors, and importers in your network</li>
            <li>Generate compliance reports for regulatory submission</li>
            <li>Monitor risk indicators and spatial intersections</li>
            <li>Blockchain-verified traceability from farm to export</li>
          </ul>
          <button
            @click="becomeExporter"
            class="tw-bg-black hover:tw-bg-black tw-text-white tw-font-semibold tw-py-3 tw-px-8 tw-rounded-xl tw-shadow tw-border tw-border-[#e6e1c3] tw-transition tw-text-lg"
            aria-label="Join EUDR Platform"
          >
            Join EUDR Platform
          </button>
        </div>
        <div class="tw-flex-1 tw-flex tw-justify-center tw-px-3">
          <img
            src="@/assets/images/exporter.jpg"
            alt="Buyer inspecting produce"
            class="tw-rounded-2xl tw-shadow-lg tw-w-72 tw-h-72 tw-object-cover tw-bg-[#f7f5ee] tw-border-4 tw-border-[#e6e1c3]"
          />
        </div>
      </section>
    </div>

    <!-- Digital Inclusion / USSD -->
    <section v-if="false" class="tw-bg-white tw-py-10 tw-px-6 tw-max-w-6xl tw-mx-auto tw-rounded-xl tw-shadow-md tw-mb-8 tw-flex tw-flex-col md:tw-flex-row tw-items-center">
      <div class="tw-flex-1 tw-mb-6 md:tw-mb-0">
        <h2 class="tw-text-2xl tw-font-semibold tw-text-yellow-700 tw-mb-2">Digital Inclusion</h2>
        <ul class="tw-list-disc tw-list-inside tw-text-gray-700 tw-mb-4">
          <li>No smartphone? Use USSD to connect</li>
          <li>Text <span class="tw-font-bold tw-text-green-700">*123#</span> to start selling</li>
          <li>Inclusive for all Kenyan farmers</li>
        </ul>
        <router-link
          to="/ussd"
          class="tw-bg-yellow-500 hover:tw-bg-yellow-600 tw-text-white tw-font-semibold tw-py-2 tw-px-5 tw-rounded-lg tw-shadow tw-transition"
          aria-label="Explore USSD Access"
        >
          Explore USSD Access
        </router-link>
      </div>
      <!-- <div class="tw-flex-1 tw-flex tw-justify-center">
        <img
          src="@/assets/images/farmer-ussd.jpg"
          alt="Farmer texting on basic phone"
          class="tw-rounded-xl tw-shadow-md tw-w-64 tw-h-64 tw-object-cover tw-bg-gray-100"
        />
      </div> -->
    </section>

    <!-- Map Preview & AI Mention -->
    <section v-if="false" class="tw-max-w-6xl tw-mx-auto tw-px-6 tw-py-8 tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-gap-8">
      <div class="tw-flex-1 tw-flex tw-flex-col tw-items-start">
        <h3 class="tw-text-xl tw-font-semibold tw-text-green-700 tw-mb-2">See Connections Near You</h3>
        <p class="tw-text-gray-700 tw-mb-4">AgriBackup’s map shows buyers and farmers nearby for fast, local deals.</p>
        <img
          src="@/assets/images/map-preview.jpg"
          alt="AgriBackup geolocation map preview"
          class="tw-rounded-xl tw-shadow-md tw-w-full tw-max-w-md tw-bg-gray-100"
        />
      </div>
      <div class="tw-flex-1 tw-flex tw-flex-col tw-items-start">
        <h3 class="tw-text-xl tw-font-semibold tw-text-blue-700 tw-mb-2">AI-Powered Farming</h3>
        <p class="tw-text-gray-700 tw-mb-4">Get personalized crop advice and market insights. <router-link to="/ai" class="tw-text-blue-600 hover:tw-underline">Read More</router-link></p>
        <img
          src="@/assets/images/ai-advice.jpg"
          alt="AI-powered farming tips"
          class="tw-rounded-xl tw-shadow-md tw-w-full tw-max-w-md tw-bg-gray-100"
        />
      </div>
    </section>

    <!-- Screenshots Gallery -->
  <section class="reveal tw-max-w-6xl tw-mx-auto tw-px-6 tw-py-12 tw-mb-12">
      <h2 class="tw-text-3xl tw-font-semibold tw-text-green-800 tw-mb-6 tw-text-center">AgriBackup in Action</h2>
      <p class="tw-text-center tw-text-[#6b4f2c] tw-mb-8">A quick preview of the AgriBackup desktop experience — farm management, reports, and mapping.</p>

      <div class="tw-grid tw-grid-cols-1 sm:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
        <div class="tw-relative tw-rounded-xl tw-overflow-hidden tw-bg-white tw-shadow-lg">
          <img src="@/assets/images/screenshots/gis_on_farm.png" alt="GIS on Farm" class="tw-w-full tw-h-56 md:tw-h-64 tw-object-cover" />
          <div class="tw-p-4">
            <h3 class="tw-text-lg tw-font-semibold tw-text-green-800">Field Mapping</h3>
            <p class="tw-text-sm tw-text-[#6b4f2c] tw-mt-1">Visualize your farm zones and GPS boundaries.</p>
          </div>
        </div>

        <div class="tw-relative tw-rounded-xl tw-overflow-hidden tw-bg-white tw-shadow-lg">
          <img src="@/assets/images/screenshots/zones.jpg" alt="Zone Management" class="tw-w-full tw-h-56 md:tw-h-64 tw-object-cover" />
          <div class="tw-p-4">
            <h3 class="tw-text-lg tw-font-semibold tw-text-green-800">Zone Management</h3>
            <p class="tw-text-sm tw-text-[#6b4f2c] tw-mt-1">Organize fields by zones for efficient operations.</p>
          </div>
        </div>

        <div class="tw-relative tw-rounded-xl tw-overflow-hidden tw-bg-white tw-shadow-lg">
          <img src="@/assets/images/screenshots/reports.jpg" alt="Reports" class="tw-w-full tw-h-56 md:tw-h-64 tw-object-cover" />
          <div class="tw-p-4">
            <h3 class="tw-text-lg tw-font-semibold tw-text-green-800">Reports & Analytics</h3>
            <p class="tw-text-sm tw-text-[#6b4f2c] tw-mt-1">Generate insights for yields, pickups and sales.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Testimonials & Partners -->
    <section v-if="false" class="tw-bg-gray-50 tw-py-10 tw-px-6 tw-max-w-6xl tw-mx-auto tw-rounded-xl tw-shadow-md tw-mb-8">
      <h2 class="tw-text-2xl tw-font-semibold tw-text-green-700 tw-mb-6">What Our Users Say</h2>
      <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-8">
        <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6 tw-flex-1">
          <p class="tw-text-gray-800 tw-italic">“AgriBackup helped me double my profits in 3 months!”</p>
          <span class="tw-block tw-mt-2 tw-font-semibold tw-text-green-700">– Mary, Smallholder Farmer</span>
        </div>
        <div class="tw-bg-white tw-rounded-lg tw-shadow tw-p-6 tw-flex-1">
          <p class="tw-text-gray-800 tw-italic">“I source fresh produce directly, saving time and money.”</p>
          <span class="tw-block tw-mt-2 tw-font-semibold tw-text-blue-700">– John, Buyer</span>
        </div>
      </div>
      <div class="tw-flex tw-items-center tw-justify-center tw-mt-8 tw-gap-6">
        <!-- <img src="@/assets/images/partner-logo1.png" alt="Partner Logo 1" class="tw-h-10 tw-object-contain" />
        <img src="@/assets/images/partner-logo2.png" alt="Partner Logo 2" class="tw-h-10 tw-object-contain" />
        <img src="@/assets/images/partner-logo3.png" alt="Partner Logo 3" class="tw-h-10 tw-object-contain" /> -->
      </div>
    </section>

    <!-- EUDR & Traceability (visible for SEO and users) -->
  <section class="reveal tw-bg-white tw-py-12 tw-px-6 tw-max-w-6xl tw-mx-auto tw-rounded-2xl tw-shadow-md tw-mb-12">
      <div class="tw-flex tw-flex-col md:tw-flex-row tw-items-start tw-gap-6">
        <div class="tw-flex-1">
          <h2 class="tw-text-2xl tw-font-semibold tw-text-green-800 tw-mb-3">EUDR Compliance & Supply-Chain Traceability</h2>
          <p class="tw-text-[#6b4f2c] tw-mb-4">AgriBackup helps farms and exporters meet the EU Deforestation Regulation (EUDR) requirements by enabling secure document uploads, exporter licensing workflows, and per-item traceability.</p>
          <ul class="tw-list-disc tw-list-inside tw-text-[#6b4f2c] tw-mb-4">
            <li>Upload permits & certificates (PDF, JPG, PNG) and store them securely in private S3 buckets.</li>
            <li>Generate time-limited presigned URLs for auditors and regulators to verify documents without exposing raw storage.</li>
            <li>Track produce from farm to buyer with timestamps and location metadata to support chain-of-custody reporting.</li>
          </ul>
          <router-link to="/contact" class="tw-inline-block tw-bg-green-700 hover:tw-bg-green-800 tw-text-white tw-py-2 tw-px-4 tw-rounded">Contact us to learn about EUDR support</router-link>
        </div>
        <!-- <div class="tw-flex-1 tw-flex tw-justify-center">
          <img src="@/assets/images/eudr-diagram.png" alt="EUDR traceability diagram" class="tw-w-full tw-max-w-sm tw-object-contain tw-rounded" />
        </div> -->
      </div>
    </section>

    <!-- Final CTA & Footer -->
    <footer class="tw-bg-green-700 tw-text-white tw-py-8 tw-px-6 tw-mt-auto">
      <div class="tw-max-w-6xl tw-mx-auto tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between">
        <div class="tw-mb-4 md:tw-mb-0">
          <h2 class="tw-text-xl tw-font-semibold">Ready to transform your farm?</h2>
        </div>
        <router-link
          to="/signup"
          class="tw-bg-white tw-text-green-700 tw-font-semibold tw-py-3 tw-px-6 tw-rounded-lg tw-shadow tw-transition hover:tw-bg-green-100"
          aria-label="Join AgriBackup Now"
        >
          Join Now
        </router-link>
      </div>
      <div class="tw-mt-6 tw-text-center tw-text-sm tw-opacity-80">
        &copy; 2024 AgriBackup. All rights reserved. | <router-link to="/contact" class="tw-underline tw-text-white">Contact Us</router-link>
      </div>
    </footer>

    <!-- Learn How It Works Dialog -->
    <div
      v-if="showDialog"
      class="tw-fixed tw-inset-0 tw-z-50 tw-flex tw-items-start tw-justify-center tw-pt-16 tw-bg-transparent"
      style="backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);"
    >
      <div class="tw-bg-white tw-rounded-xl tw-shadow-xl tw-p-8 tw-max-w-lg tw-w-full tw-relative tw-mx-4">
        <button
          @click="showDialog = false"
          class="tw-absolute tw-top-3 tw-right-3 tw-text-gray-400 hover:tw-text-gray-700 tw-text-xl"
          aria-label="Close"
        >&times;</button>
        <h2 class="tw-text-2xl tw-font-bold tw-text-green-700 tw-mb-4">How AgriBackup Works</h2>
        <ul class="tw-list-disc tw-list-inside tw-text-gray-700 tw-mb-4">
          <li><span class="tw-font-semibold">For Farmers:</span>
            We connect you to buyers without the need of middlemen for you to make more profit from your farm
          </li>
          <li><span class="tw-font-semibold">For Buyers:</span>
            Source fresh quality produce from nearby farmers at transparent prices with secure payment system
          </li>
          <li><span class="tw-font-semibold">For Exporters:</span>
            Register your own farmers and be able to source quality produce in bulk from farmers, and get QR code of produce vendors
          </li>
        </ul>
        <div class="tw-flex tw-justify-end">
          <button
            @click="showDialog = false"
            class="tw-bg-green-600 tw-text-white tw-px-4 tw-py-2 tw-rounded-lg tw-font-semibold hover:tw-bg-green-700 tw-transition"
          >Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LandingPage',
  metaInfo: {
    title: 'AgriBackup - Connect Farmers, Buyers, Exporters & Institutions | Eliminate Middlemen',
    meta: [
      { name: 'description', content: 'AgriBackup connects farmers, buyers, exporters, governments, and institutions worldwide. Sell and buy farm produce directly, eliminate middlemen, access AI-powered insights, and join a global agricultural marketplace. Supports EUDR traceability and compliant document workflows.' },
      { name: 'keywords', content: 'AgriBackup, EUDR, EU Deforestation Regulation, traceability, sustainable sourcing, farmers, buyers, exporters, agriculture, farm produce, compliance' },
      { name: 'robots', content: 'index,follow' },
      { property: 'og:title', content: 'AgriBackup - Global Agricultural Marketplace' },
      { property: 'og:description', content: 'Join AgriBackup to connect farmers, buyers, exporters, and institutions. Sell and buy farm produce directly, eliminate middlemen, access AI-powered insights, and manage supply-chain traceability for EUDR compliance.' },
      { property: 'og:type', content: 'website' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:url', content: 'https://agribackup.com/' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { name: 'twitter:card', content: 'summary_large_image' },
      { name: 'twitter:title', content: 'AgriBackup - Global Agricultural Marketplace' },
      { name: 'twitter:description', content: 'Connect farmers, buyers, exporters, and institutions worldwide. Sell and buy farm produce directly, eliminate middlemen, access AI-powered insights, and support EUDR traceability.' },
      { name: 'twitter:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { rel: 'canonical', href: 'https://agribackup.com/' },
    ],
    script: [
      {
        type: 'application/ld+json',
        json: {
          '@context': 'https://schema.org',
          '@type': 'WebSite',
          name: 'AgriBackup',
          url: 'https://agribackup.com/',
          description: 'AgriBackup connects farmers, buyers, exporters, governments, and institutions worldwide. Sell and buy farm produce directly, eliminate middlemen, access AI-powered insights, and join a global agricultural marketplace. Provides features to support EUDR compliance and supply-chain traceability.',
          image: 'https://agribackup.com/assets/images/happy-farmer.jpg',
          potentialAction: {
            '@type': 'SearchAction',
            target: 'https://agribackup.com/search?q={search_term_string}',
            'query-input': 'required name=search_term_string',
          },
        },
      },
    ],
  },
  data() {
    return {
      showDialog: false,
      revealObserver: null,
      timerInterval: null,
      timeLeft: {
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0,
      },
    };
  },
  mounted() {
    this.initRevealObserver();
    this.startTimer();
  },
  beforeDestroy() {
    if (this.revealObserver) {
      this.revealObserver.disconnect();
      this.revealObserver = null;
    }
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  },
  methods: {
    clearViewRole() {
      // eslint-disable-next-line sonarjs/no-duplicate-string
      this.$store.dispatch('auth/setViewRole', '');
    },
    handleLogin() {
      this.clearViewRole();
      this.$router.push({ name: 'Home' });
    },
    handleSignup() {
      this.clearViewRole();
      this.$router.push({ name: 'Home' });
    },
    goHome() {
      this.$router.push({ name: 'Home' });
    },
    joinAsBuyer() {
      // eslint-disable-next-line sonarjs/no-duplicate-string
      this.$store.dispatch('auth/setViewRole', 'buyer');
      this.$router.push({ path: '/browse-listings' });
    },
    becomeExporter() {
      this.$store.dispatch('auth/setViewRole', 'exporter');
      this.$router.push({ name: 'SignIn' });
    },
    joinAsFarmer() {
      this.$store.dispatch('auth/setViewRole', 'farmer');
      this.$router.push({ name: 'Listings' });
    },

    // IntersectionObserver-based reveal animations for sections
    initRevealObserver() {
      if (typeof IntersectionObserver === 'undefined') return;
      const options = { root: null, rootMargin: '0px', threshold: 0.12 };
      this.revealObserver = new IntersectionObserver((entries, obs) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            obs.unobserve(entry.target);
          }
        });
      }, options);

      this.$nextTick(() => {
        const els = this.$el.querySelectorAll('.reveal');
        els.forEach((el) => {
          el.classList.remove('revealed');
          this.revealObserver.observe(el);
        });
      });
    },
    // EUDR Compliance Countdown Timer
    startTimer() {
      this.updateTimer();
      this.timerInterval = setInterval(this.updateTimer, 1000);
    },
    updateTimer() {
      const now = new Date();
      const target = new Date('2025-12-31T23:59:59');
      const diff = target - now;
      if (diff > 0) {
        this.timeLeft.days = Math.floor(diff / (1000 * 60 * 60 * 24));
        this.timeLeft.hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        this.timeLeft.minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        this.timeLeft.seconds = Math.floor((diff % (1000 * 60)) / 1000);
      } else {
        this.timeLeft = {
          days: 0,
          hours: 0,
          minutes: 0,
          seconds: 0,
        };
      }
    },
  },
};
</script>

<style scoped>
/* Glassmorphism effect for navbar */
nav {
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
}
/* Scroll reveal styles */
.reveal {
  opacity: 0;
  transform: translateY(20px) scale(0.995);
  transition: opacity 520ms cubic-bezier(.22,.9,.26,1), transform 520ms cubic-bezier(.22,.9,.26,1);
  will-change: opacity, transform;
}
.reveal.revealed {
  opacity: 1;
  transform: translateY(0) scale(1);
}

/* No extra styles needed; Tailwind handles layout and responsiveness */
</style>
