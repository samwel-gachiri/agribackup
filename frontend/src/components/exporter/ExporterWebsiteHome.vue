<template>
  <div class="exporter-home">
    <!-- Hero Section -->
    <v-container class="hero-section">
      <v-row align="center" justify="center">
        <v-col cols="12" md="8" class="text-center">
          <v-avatar size="120" class="mb-4">
            <v-icon size="60" color="primary">mdi-office-building</v-icon>
          </v-avatar>

          <h1 class="display-2 font-weight-bold mb-4">
            {{ exporter?.companyName || 'Welcome to Our Company' }}
          </h1>

          <p class="subtitle-1 text--secondary mb-6">
            {{ exporter?.companyDesc || 'Leading exporter of sustainable agricultural products with full EUDR compliance.' }}
          </p>

          <div class="hero-stats">
            <v-row>
              <v-col cols="12" sm="4">
                <v-card elevation="2" class="stat-card">
                  <v-card-text class="text-center">
                    <div class="stat-number">{{ products.length }}</div>
                    <div class="stat-label">Products</div>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card elevation="2" class="stat-card">
                  <v-card-text class="text-center">
                    <div class="stat-number">{{ certificates.length }}</div>
                    <div class="stat-label">Certificates</div>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" sm="4">
                <v-card elevation="2" class="stat-card">
                  <v-card-text class="text-center">
                    <v-icon color="success" size="24" class="stat-icon">mdi-shield-check</v-icon>
                    <div class="stat-label">EUDR Compliant</div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </div>
        </v-col>
      </v-row>
    </v-container>

    <!-- Featured Products Section -->
    <v-container class="featured-products" v-if="products.length > 0">
      <v-row>
        <v-col cols="12">
          <h2 class="text-center mb-8">Our Products</h2>
        </v-col>
      </v-row>

      <v-row>
        <v-col
          v-for="product in featuredProducts"
          :key="product.id"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card
            class="product-card"
            elevation="3"
            hover
          >
            <v-card-title class="product-title">
              <v-icon left color="primary">mdi-package-variant</v-icon>
              {{ product.commodityDescription || product.commodity || 'Product' }}
            </v-card-title>

            <v-card-text>
              <div class="product-details">
                <div class="detail-item">
                  <v-icon small color="secondary">mdi-weight</v-icon>
                  <span>{{ product.quantityKg || 0 }} kg</span>
                </div>
                <div class="detail-item">
                  <v-icon small color="secondary">mdi-map-marker</v-icon>
                  <span>{{ product.countryOfProduction || 'Origin' }}</span>
                </div>
                <div class="detail-item">
                  <v-icon small color="secondary">mdi-calendar</v-icon>
                  <span>{{ formatDate(product.createdAt) }}</span>
                </div>
              </div>

              <v-chip
                v-if="product.riskLevel"
                :color="getRiskColor(product.riskLevel)"
                small
                class="mt-3"
              >
                {{ product.riskLevel }}
              </v-chip>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                text
                color="primary"
                small
              >
                Learn More
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row v-if="products.length > 6">
        <v-col cols="12" class="text-center mt-6">
          <v-btn
            color="primary"
            outlined
            @click="$emit('change-tab', 'products')"
          >
            View All Products
          </v-btn>
        </v-col>
      </v-row>
    </v-container>

    <!-- Certifications Section -->
    <v-container class="certifications-section" v-if="certificates.length > 0">
      <v-row>
        <v-col cols="12">
          <h2 class="text-center mb-8">Our Certifications</h2>
        </v-col>
      </v-row>

      <v-row>
        <v-col
          v-for="cert in featuredCertificates"
          :key="cert.id"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card class="cert-card" elevation="2">
            <v-card-text class="text-center">
              <v-icon color="success" size="48" class="mb-3">mdi-certificate</v-icon>
              <h4>{{ cert.batchCode || `Certificate ${cert.id}` }}</h4>
              <p class="text--secondary">
                {{ cert.commodityDescription || 'Certified Product' }}
              </p>
              <v-chip
                small
                color="success"
                class="mt-2"
              >
                <v-icon left small>mdi-shield-check</v-icon>
                Verified
              </v-chip>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Call to Action -->
    <v-container class="cta-section">
      <v-row align="center" justify="center">
        <v-col cols="12" md="8" class="text-center">
          <h3 class="mb-4">Ready to Partner With Us?</h3>
          <p class="text--secondary mb-6">
            Discover how our EUDR-compliant products can meet your sourcing needs.
          </p>
          <v-btn
            color="primary"
            size="large"
            @click="$emit('change-tab', 'about')"
          >
            Learn More About Us
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'ExporterWebsiteHome',
  props: {
    exporter: {
      type: Object,
      default: () => null
    },
    products: {
      type: Array,
      default: () => []
    },
    certificates: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    featuredProducts() {
      return this.products.slice(0, 6)
    },
    featuredCertificates() {
      return this.certificates.slice(0, 3)
    }
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return 'N/A'
      try {
        return new Date(dateString).toLocaleDateString()
      } catch {
        return dateString
      }
    },
    getRiskColor(riskLevel) {
      const colors = {
        'NONE': 'success',
        'LOW': 'info',
        'MEDIUM': 'warning',
        'HIGH': 'error',
        'CRITICAL': 'error'
      }
      return colors[riskLevel] || 'grey'
    }
  }
}
</script>

<style scoped>
.hero-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.hero-stats {
  margin-top: 40px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: white;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
  color: white;
}

.stat-icon {
  display: block;
  margin: 0 auto 8px;
}

.featured-products,
.certifications-section {
  padding: 60px 0;
  background: white;
}

.product-card {
  height: 100%;
  transition: transform 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-title {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-bottom: 1px solid #e0e0e0;
}

.product-details {
  margin-top: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.detail-item span {
  margin-left: 8px;
}

.cert-card {
  height: 100%;
  border: 2px solid #e8f5e8;
}

.cta-section {
  padding: 60px 0;
  background: #f8f9fa;
  text-align: center;
}

@media (max-width: 960px) {
  .hero-section {
    padding: 40px 0;
  }

  .stat-number {
    font-size: 2rem;
  }
}
</style>