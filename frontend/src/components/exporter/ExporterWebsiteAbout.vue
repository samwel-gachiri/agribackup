<template>
  <div class="exporter-about">
    <v-container>
      <!-- Company Overview -->
      <v-row>
        <v-col cols="12">
          <h2 class="text-center mb-8">About {{ exporter?.companyName || 'Our Company' }}</h2>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card elevation="2" class="company-card">
            <v-card-text class="pa-8">
              <div class="company-description">
                <p class="text-h6 mb-4">
                  {{ exporter?.companyDesc || 'We are a leading exporter committed to sustainable agricultural practices and full EUDR compliance.' }}
                </p>

                <p class="text-body-1 mb-4">
                  Our commitment to sustainability and transparency sets us apart in the agricultural export industry.
                  We work directly with verified farmers and maintain complete traceability throughout our supply chain.
                </p>

                <p class="text-body-1">
                  Every product we export is backed by comprehensive documentation, risk assessments, and blockchain-verified
                  certificates ensuring compliance with the European Union's Deforestation Regulation (EUDR).
                </p>
              </div>

              <!-- Company Details -->
              <v-divider class="my-6"></v-divider>

              <v-row class="company-details">
                <v-col cols="12" sm="6">
                  <div class="detail-section">
                    <h4 class="mb-3">
                      <v-icon left color="primary">mdi-office-building</v-icon>
                      Company Information
                    </h4>
                    <div class="detail-item">
                      <strong>License ID:</strong> {{ exporter?.licenseId || 'Verified License' }}
                    </div>
                    <div class="detail-item">
                      <strong>Status:</strong>
                      <v-chip
                        :color="getStatusColor(exporter?.verificationStatus)"
                        small
                        class="ml-2"
                      >
                        {{ exporter?.verificationStatus || 'VERIFIED' }}
                      </v-chip>
                    </div>
                  </div>
                </v-col>

                <v-col cols="12" sm="6">
                  <div class="detail-section">
                    <h4 class="mb-3">
                      <v-icon left color="primary">mdi-shield-check</v-icon>
                      Compliance Standards
                    </h4>
                    <div class="compliance-item">
                      <v-icon color="success" small>mdi-check-circle</v-icon>
                      <span>EUDR Regulation Compliant</span>
                    </div>
                    <div class="compliance-item">
                      <v-icon color="success" small>mdi-check-circle</v-icon>
                      <span>Blockchain Verified</span>
                    </div>
                    <div class="compliance-item">
                      <v-icon color="success" small>mdi-check-circle</v-icon>
                      <span>Supply Chain Traceability</span>
                    </div>
                    <div class="compliance-item">
                      <v-icon color="success" small>mdi-check-circle</v-icon>
                      <span>Deforestation Risk Assessment</span>
                    </div>
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Our Mission -->
      <v-row class="mt-12">
        <v-col cols="12">
          <h3 class="text-center mb-8">Our Mission</h3>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="4">
          <v-card class="mission-card" elevation="3">
            <v-card-text class="text-center pa-6">
              <v-icon color="primary" size="48" class="mb-4">mdi-leaf</v-icon>
              <h4>Sustainability</h4>
              <p class="text-body-2">
                We are committed to protecting forests and promoting sustainable agricultural practices
                that benefit both our farmers and the environment.
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card class="mission-card" elevation="3">
            <v-card-text class="text-center pa-6">
              <v-icon color="primary" size="48" class="mb-4">mdi-handshake</v-icon>
              <h4>Transparency</h4>
              <p class="text-body-2">
                Through blockchain technology and comprehensive documentation,
                we ensure complete transparency in our supply chain.
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card class="mission-card" elevation="3">
            <v-card-text class="text-center pa-6">
              <v-icon color="primary" size="48" class="mb-4">mdi-account-group</v-icon>
              <h4>Farmer Empowerment</h4>
              <p class="text-body-2">
                We work directly with farmers to provide them with tools and knowledge
                to meet international compliance standards.
              </p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Why Choose Us -->
      <v-row class="mt-12">
        <v-col cols="12">
          <h3 class="text-center mb-8">Why Choose Us</h3>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card class="advantage-card" elevation="2">
            <v-card-text class="pa-6">
              <div class="d-flex align-center mb-3">
                <v-icon color="success" class="mr-3">mdi-shield-lock</v-icon>
                <h4>EUDR Compliance Guarantee</h4>
              </div>
              <p>
                All our products come with comprehensive EUDR compliance documentation,
                risk assessments, and blockchain-verified certificates.
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="advantage-card" elevation="2">
            <v-card-text class="pa-6">
              <div class="d-flex align-center mb-3">
                <v-icon color="primary" class="mr-3">mdi-link</v-icon>
                <h4>Complete Traceability</h4>
              </div>
              <p>
                Track every product from farm to export with GPS coordinates,
                processing records, and immutable blockchain records.
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="advantage-card" elevation="2">
            <v-card-text class="pa-6">
              <div class="d-flex align-center mb-3">
                <v-icon color="warning" class="mr-3">mdi-alert-circle-check</v-icon>
                <h4>Risk Management</h4>
              </div>
              <p>
                Advanced risk assessment algorithms identify and mitigate potential
                compliance issues before they become problems.
              </p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="advantage-card" elevation="2">
            <v-card-text class="pa-6">
              <div class="d-flex align-center mb-3">
                <v-icon color="info" class="mr-3">mdi-earth</v-icon>
                <h4>Sustainable Practices</h4>
              </div>
              <p>
                We partner with farmers committed to deforestation-free agriculture
                and sustainable land management practices.
              </p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'ExporterWebsiteAbout',
  props: {
    exporter: {
      type: Object,
      default: () => null
    }
  },
  methods: {
    getStatusColor(status) {
      const colors = {
        'VERIFIED': 'success',
        'PENDING': 'warning',
        'REJECTED': 'error'
      }
      return colors[status] || 'grey'
    }
  }
}
</script>

<style scoped>
.exporter-about {
  padding: 40px 0;
  background: white;
}

.company-card {
  border-radius: 12px;
}

.company-description {
  line-height: 1.6;
}

.company-details {
  margin-top: 32px;
}

.detail-section h4 {
  color: #1976d2;
  font-weight: 600;
}

.detail-item {
  margin-bottom: 12px;
  font-size: 0.95rem;
}

.compliance-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.compliance-item span {
  margin-left: 8px;
}

.mission-card {
  height: 100%;
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.mission-card:hover {
  transform: translateY(-4px);
}

.advantage-card {
  height: 100%;
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.advantage-card:hover {
  transform: transform 0.3s ease;
}

@media (max-width: 960px) {
  .company-card .pa-8 {
    padding: 16px !important;
  }

  .mission-card .pa-6,
  .advantage-card .pa-6 {
    padding: 20px !important;
  }
}
</style>