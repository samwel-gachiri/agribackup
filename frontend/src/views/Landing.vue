<!-- eslint-disable sonarjs/no-duplicate-string -->
<template>
  <div class="professional-landing">
    <!-- Professional Navbar -->
    <AppHeader />

    <!-- Above the Fold -->
    <section class="tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between tw-px-10 tw-pt-20 tw-pb-12 tw-max-w-6xl tw-mx-auto">
      <div class="tw-flex-1 tw-mb-12 md:tw-mb-0">
        <h1 class="tw-text-4xl md:tw-text-6xl tw-font-bold tw-text-green-900 tw-mb-6 tw-drop-shadow">
          EUDR-Compliant Supply Chain Platform
        </h1>
        <p class="tw-text-xl tw-text-[#6b4f2c] tw-mr-3 tw-mb-8 tw-drop-shadow-sm">
          Deforestation-free agriculture tracking powered by Hedera DLT. Full transparency from farm to export.
        </p>
        <div class="tw-flex tw-gap-6 tw-mb-6">
          <button
            @click="handleLogin"
            class="tw-bg-green-900 hover:tw-bg-[#5e9442] tw-text-white tw-font-semibold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-border tw-border-[#e6e1c3] tw-transition tw-text-xl"
            aria-label="Join Now"
          >
            Join Now
          </button>
          <button
            @click="showDialog = true"
            class="tw-bg-[#f7e9b0] hover:tw-bg-[#f3d97c] tw-text-[#6b4f2c] tw-font-semibold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-border tw-border-[#e6e1c3] tw-transition tw-text-xl"
            aria-label="Learn How It Works"
          >
            Learn How It Works
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

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">
            <v-icon small color="white">mdi-shield-check</v-icon>
            <span>EUDR Compliance Solution</span>
          </div>

          <h1 class="hero-title">
            EUDR compliant Supply Chain Platform
          </h1>

          <p class="hero-subtitle">
            Enterprise-grade platform for EUDR compliance. Track your agricultural supply chain from farm to export with blockchain-verified transparency.
          </p>

          <!-- EUDR Countdown -->
          <div class="countdown-section">
            <div class="countdown-header">
              <v-icon color="error" class="mr-2">mdi-clock-alert-outline</v-icon>
              <span class="countdown-title">EUDR Compliance Deadline Approaching: December 31, 2025</span>
            </div>
            <div class="countdown-timer">
              <div class="countdown-item">
                <div class="countdown-value">{{ timeLeft.days }}</div>
                <div class="countdown-label">Days</div>
              </div>
              <div class="countdown-divider">:</div>
              <div class="countdown-item">
                <div class="countdown-value">{{ timeLeft.hours }}</div>
                <div class="countdown-label">Hours</div>
              </div>
              <div class="countdown-divider">:</div>
              <div class="countdown-item">
                <div class="countdown-value">{{ timeLeft.minutes }}</div>
                <div class="countdown-label">Minutes</div>
              </div>
              <div class="countdown-divider">:</div>
              <div class="countdown-item">
                <div class="countdown-value">{{ timeLeft.seconds }}</div>
                <div class="countdown-label">Seconds</div>
              </div>
            </div>
          </div>

          <!-- CTA Buttons -->
          <div class="hero-actions">
            <button class="hero-cta-primary" @click="handleSignup">
              Get Started
              <v-icon right small>mdi-arrow-right</v-icon>
            </button>
            <button class="hero-cta-secondary" @click="handleLearnMore">
              Learn More
            </button>
          </div>

          <!-- Trust Indicators -->
          <div class="trust-indicators">
            <div class="trust-item">
              <v-icon color="green darken-2" small>mdi-check-circle</v-icon>
              <span>Blockchain Verified</span>
            </div>
            <div class="trust-item">
              <v-icon color="green darken-2" small>mdi-check-circle</v-icon>
              <span>ISO Certified</span>
            </div>
            <div class="trust-item">
              <v-icon color="green darken-2" small>mdi-check-circle</v-icon>
              <span>GDPR Compliant</span>
            </div>
          </div>
        </div>

        <!-- Hero Visual - Video Showcase -->
        <div class="hero-visual">
          <div class="video-container">
            <video
              ref="heroVideo"
              class="hero-video"
              autoplay
              muted
              loop
              playsinline
              poster="/images/video-poster.png"
            >
              <source src="/videos/eudr-compliance-globe.mp4" type="video/mp4">
              <!-- <source src="/videos/eudr-compliance-globe.webm" type="video/webm"> -->
              Your browser does not support the video tag.
            </video>

            <!-- Video Overlay Controls -->
            <div class="video-overlay">
              <button
                class="video-play-btn"
                @click="toggleVideo"
                :aria-label="isVideoPlaying ? 'Pause video' : 'Play video'"
              >
                <v-icon large color="white">
                  {{ isVideoPlaying ? 'mdi-pause' : 'mdi-play' }}
                </v-icon>
              </button>
            </div>

            <!-- Video Caption -->
            <div class="video-caption">
              <v-icon small color="white" class="mr-2">mdi-earth</v-icon>
              <span>EUDR Compliance Visualization</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Platform Demo Video Section -->
    <section class="video-showcase-section">
      <div class="section-container">
        <!-- Platform Demo Video -->
        <div class="showcase-video-container">
          <video
            ref="demoVideo"
            class="showcase-video"
            controls
            poster="/images/platform-demo-poster.png"
          >
            <source src="/videos/platform-demo.mp4" type="video/mp4">
            <!-- <source src="/videos/platform-demo.webm" type="video/webm"> -->
            Your browser does not support the video tag.
          </video>
        </div>

        <!-- Animated Feature Highlights -->
        <div class="feature-highlights">
          <div class="feature-highlight" v-for="(feature, index) in platformFeatures" :key="index">
            <div class="feature-highlight-icon">
              <v-icon color="green darken-2">{{ feature.icon }}</v-icon>
            </div>
            <span class="feature-highlight-text">{{ feature.name }}</span>
          </div>
        </div>
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

    <!-- EUDR & Traceability Section -->
    <section class="reveal tw-bg-green-50 tw-py-16 tw-px-6">
      <div class="tw-max-w-6xl tw-mx-auto tw-grid md:tw-grid-cols-2 tw-gap-12 tw-items-center">
        <div>
          <h2 class="tw-text-3xl tw-font-bold tw-text-green-800 tw-mb-6">EUDR Compliance & Traceability</h2>
          <p class="tw-text-lg tw-text-gray-700 tw-mb-6">
            AgriBackup is designed to help you meet the strict requirements of the EU Deforestation Regulation (EUDR).
          </p>
          <ul class="tw-space-y-4">
            <li class="tw-flex tw-items-start">
              <v-icon color="green" class="tw-mr-3 tw-mt-1">mdi-check-circle</v-icon>
              <span class="tw-text-gray-700"><strong>Geolocation Verification:</strong> Verify production plots with precise GPS coordinates</span>
            </li>
            <li class="tw-flex tw-items-start">
              <v-icon color="green" class="tw-mr-3 tw-mt-1">mdi-check-circle</v-icon>
              <span class="tw-text-gray-700"><strong>Deforestation-Free Proof:</strong> Satellite imagery integration to prove land compliance</span>
            </li>
            <li class="tw-flex tw-items-start">
              <v-icon color="green" class="tw-mr-3 tw-mt-1">mdi-check-circle</v-icon>
              <span class="tw-text-gray-700"><strong>Due Diligence Statements:</strong> Generate and manage required compliance documents</span>
            </li>
            <li class="tw-flex tw-items-start">
              <v-icon color="green" class="tw-mr-3 tw-mt-1">mdi-check-circle</v-icon>
              <span class="tw-text-gray-700"><strong>Risk Assessment:</strong> Automated risk analysis for your supply chain</span>
            </li>
          </ul>
          <div class="tw-mt-8">
            <button
              @click="handleLearnMore"
              class="tw-text-green-700 tw-font-semibold tw-flex tw-items-center hover:tw-underline"
            >
              Learn more about our compliance tools <v-icon small color="green" class="tw-ml-1">mdi-arrow-right</v-icon>
            </button>
          </div>
        </div>
        <div class="tw-relative">
          <div class="tw-absolute tw-inset-0 tw-bg-green-200 tw-rounded-2xl tw-transform tw-rotate-3"></div>
          <img
            src="@/assets/images/eudr-compliance.jpg"
            alt="EUDR Compliance Dashboard"
            class="tw-relative tw-rounded-2xl tw-shadow-xl tw-w-full tw-object-cover"
            style="min-height: 300px;"
          />
        </div>
      </div>
    </section>

    <!-- Join the Network CTA -->
    <section class="reveal tw-py-20 tw-px-6 tw-bg-white">
      <div class="tw-max-w-4xl tw-mx-auto tw-text-center">
        <h2 class="tw-text-4xl tw-font-bold tw-text-gray-900 tw-mb-6">Join the Global Agricultural Network</h2>
        <p class="tw-text-xl tw-text-gray-600 tw-mb-12">
          Whether you're a farmer, buyer, exporter, or institution, AgriBackup provides the tools you need to succeed in the modern agricultural market.
        </p>

        <div class="tw-grid md:tw-grid-cols-3 tw-gap-8">
          <!-- Farmer Card -->
          <div class="tw-bg-white tw-p-8 tw-rounded-xl tw-shadow-lg tw-border tw-border-gray-100 hover:tw-shadow-xl tw-transition tw-transform hover:tw--translate-y-1">
            <div class="tw-w-16 tw-h-16 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="32" color="green darken-2">mdi-sprout</v-icon>
            </div>
            <h3 class="tw-text-xl tw-font-bold tw-text-gray-900 tw-mb-3">For Farmers</h3>
            <p class="tw-text-gray-600 tw-mb-6 tw-text-sm">Sell directly to buyers, access fair prices, and manage your farm efficiently.</p>
            <button
              @click="joinAsFarmer"
              class="tw-w-full tw-py-2 tw-px-4 tw-border-2 tw-border-green-600 tw-text-green-700 tw-font-semibold tw-rounded-lg hover:tw-bg-green-50 tw-transition"
            >
              Join as Farmer
            </button>
          </div>

          <!-- Buyer Card -->
          <div class="tw-bg-white tw-p-8 tw-rounded-xl tw-shadow-lg tw-border tw-border-gray-100 hover:tw-shadow-xl tw-transition tw-transform hover:tw--translate-y-1">
            <div class="tw-w-16 tw-h-16 tw-bg-blue-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="32" color="blue darken-2">mdi-cart</v-icon>
            </div>
            <h3 class="tw-text-xl tw-font-bold tw-text-gray-900 tw-mb-3">For Buyers</h3>
            <p class="tw-text-gray-600 tw-mb-6 tw-text-sm">Source quality produce directly from verified farmers with full traceability.</p>
            <button
              @click="joinAsBuyer"
              class="tw-w-full tw-py-2 tw-px-4 tw-border-2 tw-border-blue-600 tw-text-blue-700 tw-font-semibold tw-rounded-lg hover:tw-bg-blue-50 tw-transition"
            >
              Join as Buyer
            </button>
          </div>

          <!-- Exporter Card -->
          <div class="tw-bg-white tw-p-8 tw-rounded-xl tw-shadow-lg tw-border tw-border-gray-100 hover:tw-shadow-xl tw-transition tw-transform hover:tw--translate-y-1">
            <div class="tw-w-16 tw-h-16 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="32" color="purple darken-2">mdi-earth</v-icon>
            </div>
            <h3 class="tw-text-xl tw-font-bold tw-text-gray-900 tw-mb-3">For Exporters</h3>
            <p class="tw-text-gray-600 tw-mb-6 tw-text-sm">Manage compliance, track supply chains, and export with confidence.</p>
            <button
              @click="becomeExporter"
              class="tw-w-full tw-py-2 tw-px-4 tw-border-2 tw-border-purple-600 tw-text-purple-700 tw-font-semibold tw-rounded-lg hover:tw-bg-purple-50 tw-transition"
            >
              Join as Exporter
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <AppFooter />

    <!-- Learn How It Works Dialog -->
    <div
      v-if="showDialog"
      class="tw-fixed tw-inset-0 tw-z-50 tw-flex tw-items-start tw-justify-center tw-pt-16 tw-bg-transparent"
      style="backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);"
    >
      <div class="tw-bg-white tw-rounded-xl tw-shadow-xl tw-p-8 tw-max-w-lg tw-w-full tw-relative tw-mx-4">
        <button
          @click="handleDialogClose"
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
            @click="handleDialogClose"
            class="tw-bg-green-600 tw-text-white tw-px-4 tw-py-2 tw-rounded-lg tw-font-semibold hover:tw-bg-green-700 tw-transition"
          >Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import analyticsMixin from '@/mixins/analyticsMixin.js';
import AppHeader from '@/components/layout/AppHeader.vue';
import AppFooter from '@/components/layout/AppFooter.vue';

export default {
  name: 'LandingPage',
  components: {
    AppHeader,
    AppFooter,
  },
  mixins: [analyticsMixin],
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
      isVideoPlaying: true,
      timeLeft: {
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0,
      },
      platformFeatures: [
        { name: 'Geolocation Mapping', icon: 'mdi-map-marker-radius' },
        { name: 'Blockchain Traceability', icon: 'mdi-link-variant' },
        { name: 'Due Diligence Reports', icon: 'mdi-file-document-check' },
        { name: 'Supply Chain Management', icon: 'mdi-account-network' },
        { name: 'Risk Monitoring', icon: 'mdi-alert-octagon' },
        { name: 'QR Verification', icon: 'mdi-qrcode' },
      ],
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
      this.trackButtonClick('login_navbar', { source: 'navbar' });
      this.clearViewRole();
      this.$router.push({ name: 'SignIn' });
    },
    handleSignup() {
      this.trackButtonClick('create_account_navbar', { source: 'navbar' });
      this.clearViewRole();
      this.$router.push({ name: 'SignUp' });
    },
    goHome() {
      this.trackButtonClick('get_started', {
        source: 'hero_section',
        section: 'above_fold',
      });
      this.$router.push({ name: 'Home' });
    },
    joinAsBuyer() {
      this.trackPortalSelection('buyer', 'landing_page');
      this.trackButtonClick('join_as_buyer', {
        source: 'landing_page',
        section: 'for_buyers',
      });
      // eslint-disable-next-line sonarjs/no-duplicate-string
      this.$store.dispatch('auth/setViewRole', 'buyer');
      this.$router.push({ path: '/browse-listings' });
    },
    becomeExporter() {
      this.trackPortalSelection('exporter', 'landing_page');
      this.trackButtonClick('join_eudr_platform', {
        source: 'landing_page',
        section: 'for_exporters',
      });
      this.$store.dispatch('auth/setViewRole', 'exporter');
      this.$router.push({ name: 'SignIn' });
    },
    joinAsFarmer() {
      this.trackPortalSelection('farmer', 'landing_page');
      this.trackButtonClick('join_as_farmer', {
        source: 'landing_page',
        section: 'for_farmers',
      });
      this.$store.dispatch('auth/setViewRole', 'farmer');
      this.$router.push({ name: 'Listings' });
    },

    // Navigate to EUDR info page
    goToEUDRInfo() {
      this.trackButtonClick('what_is_eudr', { source: 'navbar' });
      this.$router.push({ name: 'EUDRInfo' });
    },

    handleLearnMore() {
      this.trackDialogInteraction('how_it_works', 'open');
      this.trackButtonClick('learn_more', {
        source: 'hero_section',
        section: 'above_fold',
      });
      this.showDialog = true;
    },

    handleDialogClose() {
      this.trackDialogInteraction('how_it_works', 'close');
      this.showDialog = false;
    },

    // Video Controls
    toggleVideo() {
      const video = this.$refs.heroVideo;
      if (video) {
        if (video.paused) {
          video.play();
          this.isVideoPlaying = true;
        } else {
          video.pause();
          this.isVideoPlaying = false;
        }
      }
    },

    // Scroll to video
    scrollToVideo() {
      this.trackButtonClick('watch_demo', { source: 'benefits_section' });
      const demoSection = document.querySelector('.platform-demo-section');
      if (demoSection) {
        demoSection.scrollIntoView({
          behavior: 'smooth',
          block: 'center',
        });
        // Play video after scroll
        setTimeout(() => {
          const video = this.$refs.demoVideo;
          if (video && video.paused) {
            video.play();
          }
        }, 800);
      }
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
/* Professional Landing Page Styles */
.professional-landing {
  background: #ffffff;
  min-height: 100vh;
}

/* Video Showcase Section */
.video-showcase-section {
  padding: 80px 24px;
  background: #f8f9fa;
}

.showcase-video-container {
  max-width: 1000px;
  margin: 0 auto 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  background: #000;
}

.showcase-video {
  width: 100%;
  height: auto;
  display: block;
}

/* Feature Highlights */
.feature-highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
  max-width: 900px;
  margin: 0 auto;
}

.feature-highlight {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 12px 20px;
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
}

.feature-highlight:nth-child(1) { animation-delay: 0.1s; }
.feature-highlight:nth-child(2) { animation-delay: 0.2s; }
.feature-highlight:nth-child(3) { animation-delay: 0.3s; }
.feature-highlight:nth-child(4) { animation-delay: 0.4s; }
.feature-highlight:nth-child(5) { animation-delay: 0.5s; }
.feature-highlight:nth-child(6) { animation-delay: 0.6s; }

.feature-highlight:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(46, 125, 50, 0.15);
}

.feature-highlight-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-highlight-text {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Hero Section */
.hero-section {
  padding: 120px 24px 80px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-content {
  max-width: 600px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #2e7d32;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 52px;
  font-weight: 800;
  color: #1a1a1a;
  line-height: 1.1;
  margin-bottom: 24px;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 20px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 32px;
}

/* Countdown Section */
.countdown-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.countdown-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.countdown-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.countdown-timer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.countdown-item {
  text-align: center;
}

.countdown-value {
  font-size: 36px;
  font-weight: 700;
  color: #2e7d32;
  line-height: 1;
}

.countdown-label {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.countdown-divider {
  font-size: 32px;
  font-weight: 700;
  color: #d1d5db;
}

/* Hero Actions */
.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.hero-cta-primary {
  background: #2e7d32;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-cta-primary:hover {
  background: #1b5e20;
  box-shadow: 0 8px 20px rgba(46, 125, 50, 0.3);
  transform: translateY(-2px);
}

.hero-cta-secondary {
  background: white;
  color: #374151;
  border: 2px solid #e5e7eb;
  font-size: 16px;
  font-weight: 600;
  padding: 14px 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.hero-cta-secondary:hover {
  border-color: #2e7d32;
  color: #2e7d32;
}

/* Trust Indicators */
.trust-indicators {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

/* Hero Visual */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Video Container */
.video-container {
  position: relative;
  width: 100%;
  max-width: 600px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.hero-video {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 16px;
}

/* Video Overlay */
.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.video-container:hover .video-overlay {
  opacity: 1;
  pointer-events: all;
}

.video-play-btn {
  background: rgba(46, 125, 50, 0.9);
  border: none;
  border-radius: 50%;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.video-play-btn:hover {
  background: rgba(27, 94, 32, 0.95);
  transform: scale(1.1);
}

.video-play-btn:active {
  transform: scale(0.95);
}

/* Video Caption */
.video-caption {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 12px 16px;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

/* Features Section */
.features-section {
  padding: 80px 24px;
  background: white;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 40px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.section-subtitle {
  font-size: 18px;
  color: #6b7280;
  max-width: 600px;
  margin: 0 auto;
}

/* Benefits Grid */
.benefits-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32px;
  margin-bottom: 60px;
}

.benefit-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 32px;
  transition: all 0.3s ease;
  position: relative;
}

.benefit-card:hover {
  border-color: #2e7d32;
  box-shadow: 0 8px 24px rgba(46, 125, 50, 0.1);
  transform: translateY(-4px);
}

.benefit-number {
  font-size: 48px;
  font-weight: 800;
  color: #e8f5e9;
  line-height: 1;
  margin-bottom: 16px;
}

.benefit-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.benefit-description {
  font-size: 15px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

/* Video Reference Section */
.video-reference-section {
  margin-top: 40px;
}

.video-reference-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f5e9 100%);
  border-radius: 16px;
  padding: 48px;
  text-align: center;
}

.video-reference-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-bottom: 24px;
}

.feature-tag {
  background: white;
  color: #2e7d32;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #2e7d32;
  transition: all 0.2s ease;
}

.feature-tag:hover {
  background: #2e7d32;
  color: white;
}

.video-reference-text {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

/* Platform Demo Video Section */
.platform-demo-section {
  margin-top: 60px;
}

.demo-video-container {
  position: relative;
  max-width: 900px;
  margin: 0 auto;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  background: #000;
}

.demo-video {
  width: 100%;
  height: auto;
  display: block;
}

.video-info {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 8px 16px;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

/* Responsive Design */
@media (max-width: 968px) {
  .hero-container {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .hero-title {
    font-size: 40px;
  }

  .hero-visual {
    order: -1;
  }

  .video-container {
    max-width: 100%;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .section-title {
    font-size: 32px;
  }

  .video-reference-card {
    padding: 32px 24px;
  }
}

@media (max-width: 640px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-cta-primary,
  .hero-cta-secondary {
    width: 100%;
    justify-content: center;
  }

  .countdown-value {
    font-size: 28px;
  }

  .brand-badge {
    display: none;
  }

  .video-container {
    border-radius: 12px;
  }

  .video-play-btn {
    width: 56px;
    height: 56px;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }

  .benefit-number {
    font-size: 36px;
  }

  .section-title {
    font-size: 28px;
  }

  .feature-tags {
    gap: 8px;
  }

  .feature-tag {
    font-size: 12px;
    padding: 6px 12px;
  }
}

/* Scroll reveal styles */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.reveal.revealed {
  opacity: 1;
  transform: translateY(0);
}
</style>
