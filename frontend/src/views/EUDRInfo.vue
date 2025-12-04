<template>
  <div class="eudr-info-page">
    <AppHeader />

    <!-- Hero Header -->
    <section class="eudr-header">
      <div class="header-container">
        <div class="header-content">
          <div class="header-badge">
            <v-icon small color="white">mdi-information-variant</v-icon>
            <span>Regulation Explained</span>
          </div>
          <h1 class="page-title">What is EUDR?</h1>
          <p class="page-subtitle">
            A comprehensive guide to the EU Deforestation Regulation and how it reshapes global agricultural supply chains.
          </p>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <section class="eudr-content">
      <div class="content-container">

        <!-- Introduction -->
        <div class="content-section intro-section">
          <div class="section-grid">
            <div class="text-column">
              <h2 class="section-title">Understanding the Regulation</h2>
              <p class="section-text">
                The <strong>EU Deforestation Regulation (EUDR)</strong> (Regulation (EU) 2023/1115) is a landmark legislative framework enacted by the European Union to curb the EU market's impact on global deforestation and forest degradation.
              </p>
              <p class="section-text">
                Entering into force on <strong>June 29, 2023</strong>, with full application starting <strong>December 30, 2024</strong> (for large operators) and <strong>June 30, 2025</strong> (for SMEs), it mandates that relevant commodities and products placed on or exported from the EU market must be:
              </p>
              <ul class="check-list">
                <li><v-icon color="green" small>mdi-check-bold</v-icon> <strong>Deforestation-free:</strong> Produced on land that has not been subject to deforestation after December 31, 2020.</li>
                <li><v-icon color="green" small>mdi-check-bold</v-icon> <strong>Legally produced:</strong> In accordance with the relevant legislation of the country of production.</li>
                <li><v-icon color="green" small>mdi-check-bold</v-icon> <strong>Covered by a Due Diligence Statement:</strong> Verifiable proof of compliance.</li>
              </ul>
            </div>
            <div class="stats-column">
              <div class="stat-card">
                <div class="stat-value">Dec 31, 2020</div>
                <div class="stat-label">Cut-off Date</div>
                <p class="stat-desc">Land must not be deforested after this date.</p>
              </div>
              <div class="stat-card">
                <div class="stat-value">100%</div>
                <div class="stat-label">Traceability</div>
                <p class="stat-desc">Required from plot of land to final product.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Scope of Commodities -->
        <div class="content-section">
          <h2 class="section-title">Scope & Commodities</h2>
          <p class="section-text">The regulation covers seven key commodities and their derived products, chosen for their high impact on deforestation.</p>
          <div class="commodities-grid">
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="brown">mdi-coffee</v-icon></div>
              <h3>Coffee</h3>
              <p>Roasted, decaffeinated, husks, skins.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="brown darken-2">mdi-food-apple</v-icon></div>
              <h3>Cocoa</h3>
              <p>Beans, paste, butter, powder, chocolate.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="green darken-3">mdi-tree</v-icon></div>
              <h3>Wood</h3>
              <p>Logs, lumber, furniture, paper, charcoal.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="orange darken-4">mdi-cow</v-icon></div>
              <h3>Cattle</h3>
              <p>Live cattle, beef, leather, hides.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="orange">mdi-leaf</v-icon></div>
              <h3>Palm Oil</h3>
              <p>Crude oil, fractions, kernels, derivatives.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="light-green darken-1">mdi-soy-sauce</v-icon></div>
              <h3>Soy</h3>
              <p>Beans, flour, oil, cake, residues.</p>
            </div>
            <div class="commodity-card">
              <div class="commodity-icon"><v-icon large color="grey darken-3">mdi-tire</v-icon></div>
              <h3>Rubber</h3>
              <p>Natural rubber, gums, sheets, tires.</p>
            </div>
          </div>
        </div>

        <!-- Due Diligence Obligations -->
        <div class="content-section bg-light">
          <h2 class="section-title">The Due Diligence System (DDS)</h2>
          <p class="section-text">Operators and traders must implement a robust Due Diligence System consisting of three pillars:</p>

          <div class="process-steps">
            <div class="step-card">
              <div class="step-number">01</div>
              <div class="step-content">
                <h3>Information Collection</h3>
                <p>Gathering data to prove traceability and legality. This includes:</p>
                <ul>
                  <li>Geolocation coordinates (latitude/longitude) of all production plots.</li>
                  <li>Date or time range of production.</li>
                  <li>Evidence of legal land tenure and usage rights.</li>
                </ul>
              </div>
            </div>
            <div class="step-card">
              <div class="step-number">02</div>
              <div class="step-content">
                <h3>Risk Assessment</h3>
                <p>Analyzing collected information to evaluate the risk of non-compliance.</p>
                <ul>
                  <li>Verifying deforestation-free status using satellite imagery.</li>
                  <li>Assessing country/regional risk levels (benchmarking).</li>
                  <li>Checking for indigenous peoples' rights concerns.</li>
                </ul>
              </div>
            </div>
            <div class="step-card">
              <div class="step-number">03</div>
              <div class="step-content">
                <h3>Risk Mitigation</h3>
                <p>If risk is non-negligible, measures must be taken to reduce it.</p>
                <ul>
                  <li>Requesting additional information or field audits.</li>
                  <li>Supporting suppliers with capacity building.</li>
                  <li>Documenting all mitigation decisions and actions.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- AgriBackup Solution -->
        <div class="content-section highlight-section">
          <div class="highlight-content">
            <h2 class="section-title text-white">How AgriBackup Ensures Compliance</h2>
            <p class="section-text text-white-opacity">
              AgriBackup is purpose-built to automate the EUDR compliance lifecycle, turning a regulatory burden into a competitive advantage.
            </p>

            <div class="solution-grid">
              <div class="solution-item">
                <v-icon color="white" x-large class="mb-4">mdi-map-marker-radius</v-icon>
                <h3>Precision Mapping</h3>
                <p>Farmers can draw production unit polygons directly on our platform. We verify these against historical satellite data to ensure no deforestation post-2020.</p>
              </div>
              <div class="solution-item">
                <v-icon color="white" x-large class="mb-4">mdi-link-variant</v-icon>
                <h3>Blockchain Traceability</h3>
                <p>Every batch of produce is recorded on the Hedera Hashgraph ledger. This creates an immutable, transparent history from farm to export.</p>
              </div>
              <div class="solution-item">
                <v-icon color="white" x-large class="mb-4">mdi-file-document-check</v-icon>
                <h3>Automated Reporting</h3>
                <p>Generate compliant Due Diligence Statements (DDS) with one click, aggregating all necessary geolocation and legality proofs.</p>
              </div>
              <div class="solution-item">
                <v-icon color="white" x-large class="mb-4">mdi-account-group</v-icon>
                <h3>Supplier Management</h3>
                <p>Onboard farmers, monitor their compliance status in real-time, and manage risk mitigation workflows all in one dashboard.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- CTA -->
        <div class="cta-section">
          <h2>Ready to Secure Your Supply Chain?</h2>
          <p>Don't wait for the deadline. Start building your deforestation-free supply chain today with AgriBackup.</p>
          <div class="cta-buttons">
            <button class="primary-btn" @click="getStarted">
              Get Started Now
              <v-icon right small>mdi-arrow-right</v-icon>
            </button>
            <button class="secondary-btn" @click="contactSales">
              Contact Sales
            </button>
          </div>
        </div>

      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script>
import AppHeader from '@/components/layout/AppHeader.vue';
import AppFooter from '@/components/layout/AppFooter.vue';

export default {
  name: 'EUDRInfo',
  components: {
    AppHeader,
    AppFooter,
  },
  methods: {
    getStarted() {
      this.$router.push({ name: 'SignUp' });
    },
    contactSales() {
      this.$router.push({ name: 'Contact' });
    },
  },
};
</script>

<style scoped>
.eudr-info-page {
  background: #ffffff;
  min-height: 100vh;
  padding-top: 80px; /* Space for fixed header */
}

/* Header */
.eudr-header {
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 100%);
  color: white;
  padding: 100px 24px 80px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.eudr-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80') center/cover;
  opacity: 0.1;
  pointer-events: none;
}

.header-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.page-title {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 24px;
  letter-spacing: -1px;
  line-height: 1.1;
}

.page-subtitle {
  font-size: 22px;
  opacity: 0.9;
  line-height: 1.6;
  font-weight: 300;
}

/* Content */
.eudr-content {
  padding: 80px 24px;
}

.content-container {
  max-width: 1100px;
  margin: 0 auto;
}

.content-section {
  margin-bottom: 100px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 24px;
  position: relative;
  display: inline-block;
}

.section-text {
  font-size: 18px;
  color: #4b5563;
  line-height: 1.8;
  margin-bottom: 24px;
}

/* Intro Section */
.section-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 60px;
  align-items: center;
}

.check-list {
  list-style: none;
  padding: 0;
  margin-top: 24px;
}

.check-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 17px;
  color: #374151;
}

.stats-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stat-card {
  background: #f8f9fa;
  padding: 32px;
  border-radius: 16px;
  border-left: 5px solid #2e7d32;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #2e7d32;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  color: #6b7280;
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 15px;
  color: #4b5563;
  margin: 0;
}

/* Commodities Grid */
.commodities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  margin-top: 40px;
}

.commodity-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

.commodity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  border-color: #2e7d32;
}

.commodity-icon {
  margin-bottom: 16px;
}

.commodity-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.commodity-card p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* Process Steps */
.bg-light {
  background: #f9fafb;
  padding: 60px;
  border-radius: 24px;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-top: 40px;
}

.step-card {
  position: relative;
}

.step-number {
  font-size: 64px;
  font-weight: 900;
  color: #e5e7eb;
  line-height: 1;
  margin-bottom: -20px;
  position: relative;
  z-index: 0;
}

.step-content {
  position: relative;
  z-index: 1;
}

.step-content h3 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.step-content p {
  font-size: 16px;
  color: #4b5563;
  margin-bottom: 16px;
  line-height: 1.6;
}

.step-content ul {
  padding-left: 20px;
  color: #4b5563;
}

.step-content li {
  margin-bottom: 8px;
  font-size: 15px;
}

/* Highlight Section */
.highlight-section {
  background: linear-gradient(135deg, #1b5e20 0%, #0d3311 100%);
  border-radius: 24px;
  padding: 80px 60px;
  color: white;
  position: relative;
  overflow: hidden;
}

.text-white { color: white !important; }
.text-white-opacity { color: rgba(255, 255, 255, 0.8) !important; }

.solution-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  margin-top: 40px;
}

.solution-item {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 32px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.solution-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
}

.solution-item h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
}

.solution-item p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin: 0;
}

/* CTA Section */
.cta-section {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.cta-section h2 {
  font-size: 40px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.cta-section p {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 40px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.primary-btn {
  background: #2e7d32;
  color: white;
  border: none;
  padding: 16px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.primary-btn:hover {
  background: #1b5e20;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
}

.secondary-btn {
  background: white;
  color: #374151;
  border: 2px solid #e5e7eb;
  padding: 14px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  border-color: #2e7d32;
  color: #2e7d32;
}

/* Responsive */
@media (max-width: 968px) {
  .section-grid,
  .process-steps,
  .solution-grid {
    grid-template-columns: 1fr;
  }

  .page-title { font-size: 40px; }
  .section-title { font-size: 32px; }

  .highlight-section { padding: 40px 24px; }
}
</style>
