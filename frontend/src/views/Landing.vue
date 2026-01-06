<!-- eslint-disable sonarjs/no-duplicate-string -->
<template>
  <div class="professional-landing">
    <!-- Professional Navbar -->
    <AppHeader />

    <!-- Fullscreen Video Hero Section -->
    <section class="video-hero-section">
      <!-- Background Video -->
      <video
        ref="bgVideo"
        class="video-bg"
        autoplay
        muted
        loop
        playsinline
        preload="auto"
        @error="onVideoError"
      >
        <source src="https://res.cloudinary.com/dvk7kw8ev/video/upload/v1767432435/forest-farm-border_hm6baj.mp4" type="video/mp4">
      </video>

      <!-- Fallback Background Image -->
      <div class="video-fallback-bg"></div>

      <!-- Dark Overlay for Text Readability -->
      <div class="video-overlay-dark"></div>

      <!-- Hero Content -->
      <div class="hero-content-overlay">
        <!-- Badge -->
        <div class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-white/20 tw-backdrop-blur-sm tw-border-2 tw-border-white/60 tw-rounded-full tw-px-4 tw-py-2 tw-mb-4">
          <v-icon small color="white">mdi-shield-check</v-icon>
          <span class="tw-text-sm tw-font-semibold tw-text-white">EUDR Compliance Solution</span>
        </div>

        <!-- Main Heading -->
        <h1 class="hero-main-title tw-text-4xl md:tw-text-6xl lg:tw-text-7xl tw-font-bold tw-text-white tw-mb-4 tw-max-w-4xl">
          EUDR-Compliant Supply Chain Platform
        </h1>

        <!-- Subtitle -->
        <p class="hero-subtitle tw-text-xl md:tw-text-2xl tw-text-white tw-mb-6 tw-max-w-2xl">
          Deforestation-free agriculture tracking powered by Hedera DLT. Full transparency from farm to export.
        </p>

        <!-- CTA Buttons -->
        <div class="tw-flex tw-flex-wrap tw-gap-4 tw-mb-6">
          <button
            @click="handleLogin"
            class="tw-bg-green-600 hover:tw-bg-green-700 tw-text-white tw-font-bold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-transition tw-text-lg"
            aria-label="Get Started"
          >
            Get Started
          </button>
          <button
            @click="handleLearnMore"
            class="tw-bg-transparent hover:tw-bg-white/20 tw-text-white tw-font-semibold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-border-2 tw-border-white tw-transition tw-text-lg"
            aria-label="Learn More"
          >
            Learn More
          </button>
        </div>

        <!-- Trust Indicators -->
        <div class="tw-flex tw-flex-wrap tw-gap-6">
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon small color="#4ade80">mdi-check-circle</v-icon>
            <span class="tw-text-sm tw-font-medium tw-text-white">Blockchain Verified</span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon small color="#4ade80">mdi-check-circle</v-icon>
            <span class="tw-text-sm tw-font-medium tw-text-white">ISO Certified</span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon small color="#4ade80">mdi-check-circle</v-icon>
            <span class="tw-text-sm tw-font-medium tw-text-white">GDPR Compliant</span>
          </div>
        </div>

        <!-- Scroll Down Indicator -->
        <div class="tw-absolute tw-bottom-8 tw-left-1/2 tw--translate-x-1/2 tw-animate-bounce">
          <v-icon large color="white">mdi-chevron-double-down</v-icon>
        </div>
      </div>
    </section>

    <!-- Hero Section -->
    <!-- Compliance Deadline Section -->
    <section class="reveal tw-py-24 tw-px-6 section-gradient-dark tw-text-white">
      <div class="tw-max-w-7xl tw-mx-auto">
        <div class="tw-grid md:tw-grid-cols-2 tw-gap-16 tw-items-center">
          <div>
            <div class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-white/10 tw-border tw-border-white/20 tw-rounded-full tw-px-3 tw-py-1 tw-mb-6 tw-backdrop-blur-sm">
               <v-icon x-small color="#4ade80">mdi-clock-alert-outline</v-icon>
               <span class="tw-text-xs tw-font-bold tw-text-green-400 tw-uppercase tw-tracking-widest">Urgent Action Required</span>
            </div>

            <h2 class="tw-text-5xl tw-font-bold tw-mb-6 tw-text-white tw-leading-tight">
              EUDR Enforcement <br/>
              <span class="tw-text-transparent tw-bg-clip-text tw-bg-gradient-to-r tw-from-green-400 tw-to-emerald-200">Begins Soon</span>
            </h2>

            <p class="tw-text-lg tw-text-gray-300 tw-mb-10 tw-leading-relaxed tw-max-w-lg">
              The EU Regulation on Deforestation-free products (EUDR) turns voluntary guidelines into mandatory law. Inaction is not an option.
            </p>

            <!-- EUDR Countdown -->
            <div class="glass-card tw-rounded-2xl tw-p-8 tw-mb-8">
              <div class="tw-flex tw-justify-between tw-items-center tw-text-center">
                <div class="tw-flex-1">
                  <div class="tw-text-4xl tw-font-bold tw-text-white tw-mb-1 tw-font-mono">{{ timeLeft.days }}</div>
                  <div class="tw-text-xs tw-text-green-300 tw-uppercase tw-tracking-widest">Days</div>
                </div>
                <div class="tw-text-3xl tw-text-white/20 tw-font-light">:</div>
                <div class="tw-flex-1">
                  <div class="tw-text-4xl tw-font-bold tw-text-white tw-mb-1 tw-font-mono">{{ timeLeft.hours }}</div>
                  <div class="tw-text-xs tw-text-green-300 tw-uppercase tw-tracking-widest">Hours</div>
                </div>
                <div class="tw-text-3xl tw-text-white/20 tw-font-light">:</div>
                <div class="tw-flex-1">
                  <div class="tw-text-4xl tw-font-bold tw-text-white tw-mb-1 tw-font-mono">{{ timeLeft.minutes }}</div>
                  <div class="tw-text-xs tw-text-green-300 tw-uppercase tw-tracking-widest">Minutes</div>
                </div>
                <div class="tw-text-3xl tw-text-white/20 tw-font-light">:</div>
                <div class="tw-flex-1">
                  <div class="tw-text-4xl tw-font-bold tw-text-green-400 tw-mb-1 tw-font-mono">{{ timeLeft.seconds }}</div>
                  <div class="tw-text-xs tw-text-green-300 tw-uppercase tw-tracking-widest">Seconds</div>
                </div>
              </div>
            </div>

            <div class="tw-flex tw-gap-4">
                <button class="tw-bg-green-500 hover:tw-bg-green-400 tw-text-white tw-font-bold tw-py-3 tw-px-8 tw-rounded-lg tw-transition tw-shadow-lg tw-shadow-green-900/40" @click="handleSignup">
                  Prepare Now
                </button>
               <button class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-transparent hover:tw-bg-white/5 tw-border tw-border-white/20 tw-text-white tw-px-6 tw-py-3 tw-rounded-lg tw-font-semibold tw-transition" @click="goToEUDRInfo">
                 Read Regulation
               </button>
            </div>
          </div>

          <!-- Globe Visualization -->
          <div class="tw-relative">
             <div class="tw-absolute tw--inset-4 tw-bg-green-500/20 tw-blur-3xl tw-rounded-full"></div>
             <div class="glass-card tw-relative tw-rounded-2xl tw-overflow-hidden tw-shadow-2xl tw-border-0">
                <!-- Using an image or illustration here instead of empty div would be better, keeping existing structure but enhancing container -->
                 <div class="tw-aspect-w-16 tw-aspect-h-9 tw-bg-gray-900 tw-flex tw-items-center tw-justify-center">
                    <!-- Placeholder visual if globe not available -->
                     <div class="tw-text-center tw-p-10">
                        <v-icon size="80" color="green lighten-1">mdi-earth</v-icon>
                        <p class="tw-text-green-100 tw-mt-4 tw-font-medium">Global Compliance Monitoring</p>
                     </div>
                 </div>
               <div class="tw-absolute tw-bottom-0 tw-left-0 tw-right-0 tw-bg-gradient-to-t tw-from-black/90 tw-to-transparent tw-p-6 tw-text-white tw-flex tw-items-center tw-justify-between">
                 <div class="tw-flex tw-items-center">
                   <v-icon small color="green accent-4" class="tw-mr-2">mdi-shield-check</v-icon>
                   <span class="tw-font-medium">Active Coverage</span>
                 </div>
                 <span class="tw-text-xs tw-text-green-300 tw-bg-green-900/50 tw-px-2 tw-py-1 tw-rounded">Live Data</span>
               </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Platform Demo Video Section -->
    <section class="video-showcase-section">
      <div class="section-container">

        <!-- Animated Feature Highlights -->
        <div class="tw-flex md:tw-flex-row tw-flex-col">
          <div class="tw-bg-white tw-shadow-md tw-w-full md:tw-h-64 tw-justify-center tw-p-12" v-for="(feature, index) in platformFeatures" :key="index">
            <div class="feature-highlight-icon">
              <v-icon color="tw-bg-green-600 tw-text-white">{{ feature.icon }}</v-icon>
            </div>
            <span class="tw-text-green-900 tw-font-semibold tw-line-clamp-2">{{ feature.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Join the Network (Simplified) -->
    <section class="reveal tw-py-24 tw-px-6 section-gradient-darker">
      <div class="tw-max-w-6xl tw-mx-auto">
        <div class="tw-text-center tw-mb-16">
          <span class="tw-text-sm tw-font-bold tw-text-green-400 tw-uppercase tw-tracking-widest">Who We Serve</span>
          <h2 class="tw-text-4xl tw-font-extrabold tw-text-white tw-mt-3 tw-mb-4">Join the Global Agricultural Network</h2>
          <p class="tw-text-xl tw-text-gray-300 tw-max-w-2xl tw-mx-auto">
            Simple, verified tools for every stakeholder. Connect, trade, and comply with confidence.
          </p>
        </div>

        <div class="tw-grid md:tw-grid-cols-3 tw-gap-8">
          <div class="glass-card tw-rounded-2xl tw-p-8 tw-text-center tw-border-t-4" style="border-top-color: #22c55e;">
            <div class="tw-bg-green-900/50 tw-w-20 tw-h-20 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="40" color="green accent-3">mdi-sprout</v-icon>
            </div>
            <h3 class="tw-text-2xl tw-font-bold tw-text-white">Farmers</h3>
            <p class="tw-text-base tw-text-gray-300 tw-my-4 tw-leading-relaxed">Get verified, demonstrate compliance, and sell directly to premium buyers without middlemen.</p>
            <button @click="joinAsFarmer" class="tw-text-green-400 tw-font-bold tw-flex tw-items-center tw-justify-center tw-gap-1 tw-mx-auto hover:tw-text-green-300">
              Start Farming <v-icon small color="green lighten-1">mdi-arrow-right</v-icon>
            </button>
          </div>

          <div class="glass-card tw-rounded-2xl tw-p-8 tw-text-center tw-border-t-4" style="border-top-color: #3b82f6;">
             <div class="tw-bg-blue-900/50 tw-w-20 tw-h-20 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="40" color="blue accent-3">mdi-warehouse</v-icon>
            </div>
            <h3 class="tw-text-2xl tw-font-bold tw-text-white">Suppliers</h3>
            <p class="tw-text-base tw-text-gray-300 tw-my-4 tw-leading-relaxed">Source verified deforestation-free produce with full chain-of-custody documentation.</p>
            <button class="tw-text-blue-400 tw-font-bold tw-flex tw-items-center tw-justify-center tw-gap-1 tw-mx-auto hover:tw-text-blue-300">
              Start Sourcing <v-icon small color="blue lighten-1">mdi-arrow-right</v-icon>
            </button>
          </div>

          <div class="glass-card tw-rounded-2xl tw-p-8 tw-text-center tw-border-t-4" style="border-top-color: #9333ea;">
            <div class="tw-bg-purple-900/50 tw-w-20 tw-h-20 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
              <v-icon size="40" color="purple accent-3">mdi-earth</v-icon>
            </div>
            <h3 class="tw-text-2xl tw-font-bold tw-text-white">Exporters</h3>
            <p class="tw-text-base tw-text-gray-300 tw-my-4 tw-leading-relaxed">Generate due diligence statements and export to the EU market with zero friction.</p>
            <button @click="becomeExporter" class="tw-text-purple-400 tw-font-bold tw-flex tw-items-center tw-justify-center tw-gap-1 tw-mx-auto hover:tw-text-purple-300">
              Start Exporting <v-icon small color="purple lighten-1">mdi-arrow-right</v-icon>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="reveal how-it-works-section">
      <div class="section-wrapper">
        <div class="section-header-minimal">
          <span class="section-label">Simple Process</span>
          <h2 class="section-title-minimal">How It Works</h2>
          <p class="section-desc-minimal">
            Get EUDR-compliant in four simple steps. Our streamlined onboarding process ensures you're ready for EU market access.
          </p>
        </div>

        <div class="steps-grid">
          <div class="step-card" v-for="(step, index) in howItWorksSteps" :key="index">
            <div class="step-number">{{ String(index + 1).padStart(2, '0') }}</div>
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-description">{{ step.description }}</p>
            <ul class="step-details">
              <li v-for="(detail, idx) in step.details" :key="idx">{{ detail }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Platform Features Section -->
    <section class="reveal features-grid-section">
      <div class="section-wrapper">
        <div class="section-header-minimal">
          <span class="section-label">Capabilities</span>
          <h2 class="section-title-minimal">Platform Features</h2>
          <p class="section-desc-minimal">
            Enterprise-grade tools designed for complete supply chain transparency and EUDR compliance.
          </p>
        </div>

        <div class="features-modern-grid">
          <div class="glass-card feature-modern-card-dark" v-for="(feature, index) in coreFeatures" :key="index">
            <div class="feature-icon-wrapper-dark">
              <v-icon size="28" color="green accent-3">{{ feature.icon }}</v-icon>
            </div>
            <h3 class="feature-title-dark">{{ feature.title }}</h3>
            <p class="feature-description-dark">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- EUDR Compliance Details Section -->
    <section class="reveal eudr-details-section">
      <div class="section-wrapper">
        <div class="eudr-content-grid">
          <div class="eudr-text-content">
            <span class="section-label">Regulation</span>
            <h2 class="section-title-minimal">Understanding EUDR Compliance</h2>
            <p class="eudr-intro">
              The EU Deforestation Regulation (EUDR) requires operators and traders placing certain commodities on the EU market to demonstrate that products are deforestation-free and legally produced.
            </p>

            <div class="eudr-requirements">
              <div class="requirement-item" v-for="(req, index) in eudrRequirements" :key="index">
                <div class="requirement-header">
                  <v-icon size="20" color="grey darken-2">{{ req.icon }}</v-icon>
                  <h4>{{ req.title }}</h4>
                </div>
                <p>{{ req.description }}</p>
              </div>
            </div>
          </div>

          <div class="eudr-info-cards">
            <div class="info-card deadline-card">
              <v-icon size="32" color="grey darken-3">mdi-calendar-alert</v-icon>
              <h4>Compliance Deadline</h4>
              <p class="deadline-date">December 31, 2026</p>
              <p class="deadline-note">For large operators and traders</p>
            </div>

            <div class="info-card commodities-card">
              <v-icon size="32" color="grey darken-3">mdi-leaf</v-icon>
              <h4>Covered Commodities</h4>
              <ul class="commodities-list">
                <li>Coffee</li>
                <li>Cocoa</li>
                <li>Palm Oil</li>
                <li>Soy</li>
                <li>Rubber</li>
                <li>Cattle</li>
                <li>Wood</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Integration Stack Section -->
    <section class="reveal integration-section">
      <div class="section-wrapper">
        <div class="section-header-minimal">
          <span class="section-label">Connectivity</span>
          <h2 class="section-title-minimal">Seamless Integration</h2>
          <p class="section-desc-minimal">
            AgriBackup connects with your existing systems and regulatory platforms for end-to-end compliance management.
          </p>
        </div>

        <div class="integration-grid">
          <div class="integration-card" v-for="(integration, index) in integrations" :key="index">
            <div class="integration-icon">
              <v-icon size="36" color="grey darken-2">{{ integration.icon }}</v-icon>
            </div>
            <h3>{{ integration.title }}</h3>
            <p>{{ integration.description }}</p>
            <ul class="integration-features">
              <li v-for="(feat, idx) in integration.features" :key="idx">{{ feat }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="reveal tw-py-32 tw-px-6 section-gradient-darker">
      <div class="tw-max-w-7xl tw-mx-auto">
        <div class="tw-text-center tw-mb-16">
          <span class="tw-text-sm tw-font-bold tw-text-green-400 tw-uppercase tw-tracking-widest">Support</span>
          <h2 class="tw-text-4xl tw-font-bold tw-text-white tw-mt-3">Frequently Asked Questions</h2>
        </div>

        <div class="tw-max-w-3xl tw-mx-auto">
          <div
            class="tw-border-b tw-border-white/10 tw-cursor-pointer tw-transition-colors hover:tw-bg-white/5 tw-rounded-lg tw-mb-2"
            v-for="(faq, index) in faqs"
            :key="index"
            @click="toggleFaq(index)"
          >
            <div class="tw-flex tw-justify-between tw-items-center tw-p-6 tw-gap-4">
              <h4 class="tw-text-lg tw-font-medium tw-text-white tw-flex-1 tw-m-0" :class="{'tw-text-green-400': openFaqIndex === index}">{{ faq.question }}</h4>
              <v-icon size="24" :color="openFaqIndex === index ? 'green accent-3' : 'grey lighten-1'">
                {{ openFaqIndex === index ? 'mdi-minus-circle' : 'mdi-plus-circle-outline' }}
              </v-icon>
            </div>
            <div v-show="openFaqIndex === index" class="tw-px-6 tw-pb-6 tw-animate-fade-in-down">
              <p class="tw-text-base tw-text-gray-400 tw-leading-relaxed tw-m-0">{{ faq.answer }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="reveal tw-py-32 tw-px-6 section-gradient-black-green">
      <div class="tw-max-w-7xl tw-mx-auto">
        <div class="tw-text-center">
          <h2 class="tw-text-5xl md:tw-text-6xl tw-font-extrabold tw-text-white tw-mb-6 tw-tracking-tight">
            Ready to achieve <span class="tw-text-green-500">EUDR compliance?</span>
          </h2>
          <p class="tw-text-xl tw-text-gray-400 tw-mb-12 tw-max-w-3xl tw-mx-auto">
            Join the platform that's transforming agricultural supply chain transparency. Trusted by industry leaders worldwide.
          </p>
          <div class="tw-flex tw-flex-col sm:tw-flex-row tw-gap-4 tw-justify-center">
            <button class="tw-bg-green-600 hover:tw-bg-green-500 tw-text-white tw-font-bold tw-px-10 tw-py-4 tw-rounded-xl tw-shadow-lg tw-shadow-green-900/50 tw-transition tw-flex tw-items-center tw-justify-center tw-gap-2 hover:tw--translate-y-1 tw-transform" @click="handleSignup">
              Get Started Now
              <v-icon small>mdi-arrow-right</v-icon>
            </button>
            <button class="tw-bg-white/10 hover:tw-bg-white/20 tw-text-white tw-font-semibold tw-px-10 tw-py-4 tw-rounded-xl tw-backdrop-blur-sm tw-border tw-border-white/10 hover:tw-border-white/30 tw-transition" @click="goToDocumentation">
              View Documentation
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
        { name: 'Due Diligence Reports', icon: 'mdi-file-pdf' },
        { name: 'Supply Chain Management', icon: 'mdi-account-network' },
        { name: 'Risk Monitoring', icon: 'mdi-alert-octagon' },
        { name: 'QR Verification', icon: 'mdi-qrcode' },
      ],
      openFaqIndex: null,
      howItWorksSteps: [
        {
          title: 'Register Your Business',
          description: 'Create your account and define your role in the supply chain.',
          details: [
            'Choose your operator type (Farmer, Aggregator, Processor, Exporter)',
            'Provide business registration details',
            'Set up your organization profile',
          ],
        },
        {
          title: 'Add Production Units',
          description: 'Map your farms and production areas with precise geolocation data.',
          details: [
            'Draw plot boundaries on interactive maps',
            'Upload GPS coordinates or shapefiles',
            'Link farmers to specific production units',
          ],
        },
        {
          title: 'Verify Compliance',
          description: 'Our system automatically checks for deforestation and generates risk assessments.',
          details: [
            'Satellite imagery analysis for land-use changes',
            'Automated deforestation detection since Dec 2020',
            'Real-time fire and risk monitoring alerts',
          ],
        },
        {
          title: 'Generate Certificates',
          description: 'Receive blockchain-verified certificates for EUDR-compliant products.',
          details: [
            'NFT certificates minted on Hedera blockchain',
            'QR codes for instant verification',
            'Due Diligence Statement generation',
          ],
        },
      ],
      coreFeatures: [
        {
          icon: 'mdi-map-marker-radius',
          title: 'Geolocation Mapping',
          description: 'Precise GPS coordinates and polygon mapping for every production unit. Interactive maps with satellite imagery integration.',
        },
        {
          icon: 'mdi-satellite-variant',
          title: 'Deforestation Monitoring',
          description: 'Automated satellite analysis to detect land-use changes. Verify no deforestation has occurred since December 31, 2020.',
        },
        {
          icon: 'mdi-shield-check',
          title: 'Risk Assessment',
          description: 'Automated risk scoring based on location, historical data, and regulatory requirements. Real-time alerts for potential compliance issues.',
        },
        {
          icon: 'mdi-file-document-check',
          title: 'Due Diligence Reports',
          description: 'Generate comprehensive compliance documentation ready for EU regulatory submission. Audit-ready records at all times.',
        },
        {
          icon: 'mdi-link-variant',
          title: 'Blockchain Traceability',
          description: 'Immutable transaction records on Hedera DLT. Complete chain of custody from farm to export with cryptographic verification.',
        },
        {
          icon: 'mdi-certificate',
          title: 'NFT Certificates',
          description: 'Unique digital certificates for each compliant batch. Shareable via QR codes for instant verification by importers and authorities.',
        },
      ],
      eudrRequirements: [
        {
          icon: 'mdi-earth',
          title: 'Geolocation Data',
          description: 'Precise coordinates of all production plots. For plots over 4 hectares, polygon boundaries are required.',
        },
        {
          icon: 'mdi-calendar-check',
          title: 'Cutoff Date Compliance',
          description: 'Products must come from land not deforested after December 31, 2020.',
        },
        {
          icon: 'mdi-scale-balance',
          title: 'Legal Production',
          description: 'Commodities must be produced in accordance with the laws of the country of production.',
        },
        {
          icon: 'mdi-file-search',
          title: 'Due Diligence',
          description: 'Operators must conduct risk assessment and mitigation before placing products on the EU market.',
        },
      ],
      integrations: [
        {
          icon: 'mdi-database',
          title: 'ERP Systems',
          description: 'Connect with your existing enterprise systems for seamless data flow.',
          features: ['SAP Integration', 'Oracle Compatibility', 'Custom API Access'],
        },
        {
          icon: 'mdi-bank',
          title: 'Regulatory Authorities',
          description: 'Direct submission channels to EU competent authorities.',
          features: ['DDS Submission', 'Audit Trail Export', 'Compliance Reporting'],
        },
        {
          icon: 'mdi-truck',
          title: 'Logistics Partners',
          description: 'Track shipments and maintain chain of custody through export.',
          features: ['Bill of Lading Integration', 'Container Tracking', 'Port Documentation'],
        },
      ],
      faqs: [
        {
          question: 'What is the EUDR and who does it apply to?',
          answer: 'The EU Deforestation Regulation (EUDR) is a law requiring operators and traders placing certain commodities (coffee, cocoa, soy, palm oil, wood, rubber, cattle) on the EU market to prove products are deforestation-free and legally produced. It applies to all businesses importing or trading these commodities in the EU.',
        },
        {
          question: 'What is the compliance deadline?',
          answer: 'The EUDR enters into force on December 30, 2024 for all operators except SMEs. Large operators must comply by December 31, 2025. Small and medium enterprises have until June 30, 2026.',
        },
        {
          question: 'How does AgriBackup help with EUDR compliance?',
          answer: 'AgriBackup provides end-to-end traceability from farm to export. We capture geolocation data for every production unit, perform automated deforestation checks using satellite imagery, generate risk assessments, and produce blockchain-verified certificates that satisfy EUDR requirements.',
        },
        {
          question: 'What geolocation data is required?',
          answer: 'EUDR requires GPS coordinates for all plots of land where commodities are produced. For plots larger than 4 hectares, polygon boundaries must be provided. AgriBackup supports both point coordinates and polygon mapping via our interactive platform.',
        },
        {
          question: 'How is blockchain used in the platform?',
          answer: 'AgriBackup uses Hedera Hashgraph distributed ledger technology to create immutable records of all transactions in the supply chain. Each compliant batch receives an NFT certificate that can be instantly verified by importers, regulators, and consumers.',
        },
        {
          question: 'Can I integrate AgriBackup with my existing systems?',
          answer: 'Yes, AgriBackup provides REST APIs for integration with ERP systems, trading platforms, and other supply chain software. We support standard data formats and can work with your technical team on custom integrations.',
        },
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
      this.$router.push({ name: 'SignIn' });
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
      this.$router.push({ name: 'SignIn' });
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

    onVideoLoaded() {
      const video = this.$refs.bgVideo;
      if (video) {
        video.style.opacity = '1';
      }
    },

    onVideoError() {
      // Video failed to load, fallback background will show
      this.$toast.info('Background video failed to load');
    },

    // FAQ Toggle
    toggleFaq(index) {
      this.openFaqIndex = this.openFaqIndex === index ? null : index;
    },

    // Navigate to Documentation
    goToDocumentation() {
      this.trackButtonClick('view_documentation', { source: 'cta_section' });
      this.$router.push({ name: 'Documentation' });
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
      const target = new Date('2026-12-31T23:59:59');
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

/* Fullscreen Video Hero Section */
.video-hero-section {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 600px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 2;
}

.video-fallback-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #a8aaa8;
  /* background: url('~@/assets/images/landing_page.jpg') center/cover no-repeat; */
  z-index: 1;
}

.video-overlay-dark {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.09) 0%,
    rgba(0, 0, 0, 0.045) 50%,
    rgba(0, 0, 0, 0.007) 100%
  ); */
  z-index: 3;
}

.hero-content-overlay {
  position: relative;
  z-index: 4;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 100px 40px 40px;
  max-width: 1200px;
  width: 100%;
  height: 100%;
}

/* Hero text styles with text shadow for readability */
.hero-main-title {
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5), 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* .hero-subtitle {
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5), 0 2px 8px rgba(0, 0, 0, 0.3);
} */

@media (max-width: 768px) {
  .hero-content-overlay {
    padding: 100px 24px 40px;
    align-items: center;
    text-align: center;
  }
}

/* Video Showcase Section */
.video-showcase-section {
  padding: 80px 24px;
  background: linear-gradient(135deg, #022c22 0%, #064e3b 100%); /* Matrix/Rainforest dark */
}

.showcase-video-container {
  max-width: 1000px;
  margin: 0 auto 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5); /* Darker shadow */
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
  color: #cbcbcd;
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

/* Modern Minimal Design System */
.section-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header-minimal {
  text-align: center;
  margin-bottom: 64px;
}

.section-label {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.section-title-minimal {
  font-size: 42px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 20px;
  letter-spacing: -1px;
  line-height: 1.1;
}

.section-desc-minimal {
  font-size: 18px;
  color: #6b7280;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.7;
}

/* How It Works Section (Dark Theme) */
.how-it-works-section {
  padding: 120px 0;
  background: linear-gradient(180deg, #022c22 0%, #064e3b 100%); /* matched to forest theme */
  color: #ffffff;
  position: relative;
}

/* Add a subtle pattern overlay */
.how-it-works-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 32px 32px;
  opacity: 0.3;
  pointer-events: none;
}

.how-it-works-section .section-label {
  color: #4ade80;
}

.how-it-works-section .section-title-minimal {
  color: #ffffff;
}

.how-it-works-section .section-desc-minimal {
  color: #d1d5db;
}

.steps-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.how-it-works-section .step-card {
  padding: 32px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.4s ease;
  color: #e6eef6;
  position: relative;
  overflow: hidden;
}

.how-it-works-section .step-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(74, 222, 128, 0.3);
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

/* Subtle glow on hover */
.how-it-works-section .step-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #4ade80, transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.how-it-works-section .step-card:hover::after {
  opacity: 1;
}

.how-it-works-section .step-number {
  font-size: 48px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.18);
  line-height: 1;
  margin-bottom: 18px;
}

.how-it-works-section .step-title {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 10px;
}

.how-it-works-section .step-description,
.how-it-works-section .step-details li {
  color: #e6eef6;
}

/* Responsive adjustments kept in existing media rules */

.step-description {
  font-size: 15px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 20px;
}

.step-details {
  list-style: none;
  padding: 0;
  margin: 0;
}

.step-details li {
  font-size: 14px;
  color: #9ca3af;
  padding: 6px 0;
  padding-left: 16px;
  position: relative;
}

.step-details li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: #d1d5db;
  border-radius: 50%;
}

/* Core Features Section */
.features-grid-section {
  padding: 120px 0;
  background: linear-gradient(180deg, #064e3b 0%, #022c22 100%); /* Dark Forest Gradient */
}

.features-modern-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.feature-modern-card-dark {
  padding: 40px 32px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.feature-modern-card-dark:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(74, 222, 128, 0.3);
  transform: translateY(-5px);
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.5);
}

.feature-icon-wrapper-dark {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.feature-title-dark {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12px;
}

.feature-description-dark {
  font-size: 15px;
  color: #d1d5db;
  line-height: 1.7;
  margin: 0;
}

/* EUDR Details Section */
.eudr-details-section {
  padding: 120px 0;
  background: #ffffff;
}

.eudr-content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 64px;
  align-items: start;
}

.eudr-intro {
  font-size: 18px;
  color: #374151;
  line-height: 1.8;
  margin-bottom: 40px;
}

.eudr-requirements {
  display: grid;
  gap: 24px;
}

.requirement-item {
  padding: 20px 0;
  border-bottom: 1px solid #e5e7eb;
}

.requirement-item:last-child {
  border-bottom: none;
}

.requirement-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.requirement-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.requirement-item p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.eudr-info-cards {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  padding: 32px;
  background: #fafafa;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.info-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 16px 0 12px;
}

.deadline-date {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
}

.deadline-note {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.commodities-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.commodities-list li {
  font-size: 14px;
  color: #6b7280;
  padding: 4px 0;
}

/* Integration Section */
.integration-section {
  padding: 120px 0;
  background: #fafafa;
}

.integration-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.integration-card {
  padding: 40px 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s ease;
}

.integration-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.integration-icon {
  width: 72px;
  height: 72px;
  background: #f3f4f6;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.integration-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.integration-card p {
  font-size: 15px;
  color: #6b7280;
  margin-bottom: 20px;
  line-height: 1.6;
}

.integration-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.integration-features li {
  font-size: 13px;
  color: #9ca3af;
  padding: 6px 0;
  border-top: 1px solid #f3f4f6;
}

.integration-features li:first-child {
  border-top: none;
}

/* FAQ Section */
.faq-section {
  padding: 120px 0;
  background: #ffffff;
}

.faq-container {
  max-width: 800px;
  margin: 0 auto;
}

.faq-item {
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.faq-item:hover {
  background: #fafafa;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  gap: 16px;
}

.faq-question h4 {
  font-size: 18px;
  font-weight: 500;
  color: #111827;
  margin: 0;
  flex: 1;
}

.faq-answer {
  padding: 0 0 24px;
}

.faq-answer p {
  font-size: 15px;
  color: #6b7280;
  line-height: 1.8;
  margin: 0;
}

.faq-open {
  background: #fafafa;
}

/* CTA Section */
.cta-section {
  padding: 120px 0;
  background: #111827;
}

.cta-content {
  text-align: center;
}

.cta-content h2 {
  font-size: 42px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.cta-content p {
  font-size: 18px;
  color: #9ca3af;
  margin-bottom: 40px;
}

.cta-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.cta-primary {
  background: #1e9924;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.cta-primary:hover {
  background: #1b5e20;
  transform: translateY(-2px);
}

.cta-secondary {
  background: transparent;
  color: #ffffff;
  border: 1px solid #374151;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 32px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cta-secondary:hover {
  background: #1f2937;
  border-color: #4b5563;
}

/* Responsive Design for New Sections */
@media (max-width: 1024px) {
  .steps-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .features-modern-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .integration-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .eudr-content-grid {
    grid-template-columns: 1fr;
    gap: 48px;
  }
}

@media (max-width: 768px) {
  .section-title-minimal {
    font-size: 32px;
  }

  .section-desc-minimal {
    font-size: 16px;
  }

  .steps-grid,
  .features-modern-grid,
  .integration-grid {
    grid-template-columns: 1fr;
  }

  .how-it-works-section,
  .features-grid-section,
  .eudr-details-section,
  .integration-section,
  .faq-section,
  .cta-section {
    padding: 80px 0;
  }

  .cta-content h2 {
    font-size: 32px;
  }

  .cta-buttons {
    flex-direction: column;
  }

  .cta-primary,
  .cta-secondary {
    width: 100%;
    justify-content: center;
  }

  .faq-question h4 {
    font-size: 16px;
  }

  .commodities-list {
    grid-template-columns: 1fr;
  }
}
/* Compliance Deadline Section */
.compliance-deadline-section {
  padding: 80px 0;
  background: #111827;
  color: #ffffff;
}

.deadline-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.deadline-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  margin-bottom: 24px;
  font-size: 13px;
  font-weight: 600;
  color: #fbbf24;
}

.deadline-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #ffffff;
}

.deadline-subtitle {
  font-size: 16px;
  color: #9ca3af;
  margin-bottom: 32px;
  line-height: 1.6;
}

.countdown-modern {
  display: flex;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 24px;
  gap: 16px;
  align-items: flex-start;
}

.countdown-unit {
  text-align: center;
  flex: 1;
}

.unit-value {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.unit-label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  margin-top: 8px;
  letter-spacing: 1px;
}

.unit-separator {
  font-size: 32px;
  color: #4b5563;
  margin-top: -4px;
}

.cta-primary-outline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 1px solid #4ade80;
  color: #4ade80;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.cta-primary-outline:hover {
  background: rgba(74, 222, 128, 0.1);
}

.video-container-minimal {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.visual-video {
  width: 100%;
  display: block;
}

.visual-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  padding: 20px 20px 12px;
  color: white;
  display: flex;
  align-items: center;
  font-size: 13px;
}

/* --- PREMIUM THEME OVERRIDES --- */

/* Deep Forest Gradients */
.section-gradient-dark {
  background: linear-gradient(135deg, #052e16 0%, #064e3b 100%); /* green-950 to green-900 */
  position: relative;
  overflow: hidden;
}

.section-gradient-dark::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle at 20% 50%, rgba(34, 197, 94, 0.08) 0%, transparent 50%),
                    radial-gradient(circle at 80% 80%, rgba(6, 95, 70, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.section-gradient-darker {
  background: linear-gradient(135deg, #022c22 0%, #064e3b 100%);
}

.section-gradient-black-green {
  background: linear-gradient(to bottom, #022c22 0%, #000000 100%);
}

.section-gradient-light {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); /* green-50 to green-100 */
}

/* Glassmorphism Cards */
.glass-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.glass-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-4px);
}

/* Premium Light Cards */
.premium-card-light {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 10px 30px -10px rgba(0, 128, 0, 0.15);
  transition: all 0.3s ease;
}

.premium-card-light:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px -12px rgba(0, 128, 0, 0.25);
  background: #ffffff;
}

/* Text Gradients */
.text-gradient-gold {
  background: linear-gradient(135deg, #fce7f3 0%, #fbbf24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
  .text-gradient-green {
    background: linear-gradient(135deg, #22c55e 0%, #15803d 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
</style>
