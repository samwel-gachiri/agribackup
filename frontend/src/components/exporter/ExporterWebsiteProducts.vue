<template>
  <div class="exporter-products">
    <v-container>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <h2 class="text-center mb-4">Our Products</h2>
          <p class="text-center text--secondary mb-8">
            Discover our range of EUDR-compliant agricultural products with full traceability and blockchain verification.
          </p>
        </v-col>
      </v-row>

      <!-- Products Grid -->
      <v-row v-if="products.length > 0">
        <v-col
          v-for="product in products"
          :key="product.id"
          cols="12"
          sm="6"
          lg="4"
        >
          <v-card
            class="product-card"
            elevation="3"
            hover
          >
            <v-card-title class="product-header">
              <div class="product-icon">
                <v-icon color="primary" size="32">mdi-package-variant-closed</v-icon>
              </div>
              <div class="product-info">
                <h3 class="product-name">
                  {{ product.commodityDescription || product.commodity || 'Product' }}
                </h3>
                <div class="product-code">{{ product.batchCode || `Batch ${product.id}` }}</div>
              </div>
            </v-card-title>

            <v-card-text class="product-details">
              <v-row dense>
                <v-col cols="6">
                  <div class="detail-item">
                    <v-icon small color="secondary">mdi-weight</v-icon>
                    <span class="detail-label">Quantity:</span>
                    <span class="detail-value">{{ formatNumber(product.quantityKg) }} kg</span>
                  </div>
                </v-col>
                <v-col cols="6">
                  <div class="detail-item">
                    <v-icon small color="secondary">mdi-map-marker</v-icon>
                    <span class="detail-label">Origin:</span>
                    <span class="detail-value">{{ product.countryOfProduction || 'N/A' }}</span>
                  </div>
                </v-col>
                <v-col cols="6">
                  <div class="detail-item">
                    <v-icon small color="secondary">mdi-calendar</v-icon>
                    <span class="detail-label">Created:</span>
                    <span class="detail-value">{{ formatDate(product.createdAt) }}</span>
                  </div>
                </v-col>
                <v-col cols="6">
                  <div class="detail-item">
                    <v-icon small color="secondary">mdi-package-variant</v-icon>
                    <span class="detail-label">Status:</span>
                    <v-chip
                      :color="getStatusColor(product.status)"
                      x-small
                      class="status-chip"
                    >
                      {{ product.status || 'PENDING' }}
                    </v-chip>
                  </div>
                </v-col>
              </v-row>

              <!-- Risk Assessment -->
              <div v-if="product.riskLevel" class="risk-section mt-4">
                <div class="risk-header">
                  <v-icon small color="warning" class="mr-1">mdi-alert-circle</v-icon>
                  <span class="risk-label">Risk Assessment:</span>
                </div>
                <v-chip
                  :color="getRiskColor(product.riskLevel)"
                  small
                  class="risk-chip"
                >
                  {{ product.riskLevel }}
                </v-chip>
                <div v-if="product.riskRationale" class="risk-rationale mt-2">
                  <small class="text--secondary">{{ product.riskRationale }}</small>
                </div>
              </div>
            </v-card-text>

            <v-card-actions class="product-actions">
              <v-spacer></v-spacer>
              <v-btn
                text
                color="primary"
                small
                @click="showProductDetails(product)"
              >
                <v-icon left small>mdi-eye</v-icon>
                Details
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <!-- Empty State -->
      <v-row v-else>
        <v-col cols="12" class="text-center">
          <v-card elevation="2" class="empty-state">
            <v-card-text class="pa-8">
              <v-icon color="grey" size="64" class="mb-4">mdi-package-variant</v-icon>
              <h3>No Products Available</h3>
              <p class="text--secondary">
                This exporter hasn't published any products yet.
                Check back later for updates.
              </p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Certificates Section -->
      <v-row v-if="certificates.length > 0" class="mt-12">
        <v-col cols="12">
          <h3 class="text-center mb-8">Product Certificates</h3>
        </v-col>
      </v-row>

      <v-row v-if="certificates.length > 0">
        <v-col
          v-for="cert in certificates"
          :key="cert.id"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card class="certificate-card" elevation="2">
            <v-card-text class="text-center pa-6">
              <v-icon color="success" size="48" class="mb-3">mdi-certificate</v-icon>
              <h4>{{ cert.batchCode || `Certificate ${cert.id}` }}</h4>
              <p class="text--secondary mb-3">
                {{ cert.commodityDescription || 'Certified Product' }}
              </p>
              <div class="cert-details">
                <div class="cert-item">
                  <v-icon small color="success">mdi-shield-check</v-icon>
                  <span>EUDR Compliant</span>
                </div>
                <div class="cert-item">
                  <v-icon small color="primary">mdi-link</v-icon>
                  <span>Blockchain Verified</span>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Product Details Dialog -->
    <v-dialog v-model="productDialog" max-width="600px">
      <v-card v-if="selectedProduct">
        <v-card-title class="dialog-header">
          <v-icon left color="primary">mdi-package-variant-closed</v-icon>
          {{ selectedProduct.commodityDescription || 'Product Details' }}
          <v-spacer></v-spacer>
          <v-btn icon @click="productDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text class="pa-6">
          <v-row>
            <v-col cols="12" sm="6">
              <div class="detail-section">
                <h4>Product Information</h4>
                <div class="detail-row">
                  <strong>Batch Code:</strong> {{ selectedProduct.batchCode }}
                </div>
                <div class="detail-row">
                  <strong>Commodity:</strong> {{ selectedProduct.commodity }}
                </div>
                <div class="detail-row">
                  <strong>Quantity:</strong> {{ formatNumber(selectedProduct.quantityKg) }} kg
                </div>
                <div class="detail-row">
                  <strong>Origin:</strong> {{ selectedProduct.countryOfProduction }}
                </div>
                <div class="detail-row">
                  <strong>Created:</strong> {{ formatDate(selectedProduct.createdAt) }}
                </div>
              </div>
            </v-col>

            <v-col cols="12" sm="6">
              <div class="detail-section">
                <h4>Compliance Status</h4>
                <div class="detail-row">
                  <strong>Status:</strong>
                  <v-chip :color="getStatusColor(selectedProduct.status)" small>
                    {{ selectedProduct.status }}
                  </v-chip>
                </div>
                <div v-if="selectedProduct.riskLevel" class="detail-row">
                  <strong>Risk Level:</strong>
                  <v-chip :color="getRiskColor(selectedProduct.riskLevel)" small>
                    {{ selectedProduct.riskLevel }}
                  </v-chip>
                </div>
                <div v-if="selectedProduct.riskRationale" class="detail-row">
                  <strong>Risk Assessment:</strong>
                  <p class="text--secondary">{{ selectedProduct.riskRationale }}</p>
                </div>
              </div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: 'ExporterWebsiteProducts',
  props: {
    products: {
      type: Array,
      default: () => [],
    },
    certificates: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      productDialog: false,
      selectedProduct: null,
    };
  },
  methods: {
    formatNumber(num) {
      if (!num) return '0';
      return new Intl.NumberFormat().format(num);
    },
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      try {
        return new Date(dateString).toLocaleDateString();
      } catch {
        return dateString;
      }
    },
    getStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'error',
        PROCESSING: 'info',
      };
      return colors[status] || 'grey';
    },
    getRiskColor(riskLevel) {
      const colors = {
        NONE: 'success',
        LOW: 'info',
        MEDIUM: 'warning',
        HIGH: 'orange',
        CRITICAL: 'error',
      };
      return colors[riskLevel] || 'grey';
    },
    showProductDetails(product) {
      this.selectedProduct = product;
      this.productDialog = true;
    },
  },
};
</script>

<style scoped>
.exporter-products {
  padding: 40px 0;
  background: #f8f9fa;
}

.product-card {
  height: 100%;
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15) !important;
}

.product-header {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  display: flex;
  align-items: center;
}

.product-icon {
  margin-right: 16px;
  background: white;
  border-radius: 50%;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
  color: #2c3e50;
}

.product-code {
  font-size: 0.85rem;
  color: #7f8c8d;
  margin-top: 4px;
}

.product-details {
  padding: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 0.9rem;
}

.detail-label {
  font-weight: 500;
  margin-right: 8px;
  color: #555;
}

.detail-value {
  color: #2c3e50;
}

.status-chip {
  font-size: 0.75rem;
}

.risk-section {
  border-top: 1px solid #e0e0e0;
  padding-top: 16px;
}

.risk-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.risk-label {
  font-weight: 500;
  color: #555;
}

.risk-chip {
  margin-left: 8px;
}

.risk-rationale {
  font-size: 0.8rem;
  line-height: 1.4;
}

.product-actions {
  padding: 16px 20px;
  background: #fafafa;
}

.certificate-card {
  height: 100%;
  border: 2px solid #e8f5e8;
  border-radius: 12px;
}

.cert-details {
  margin-top: 16px;
}

.cert-item {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 0.85rem;
}

.cert-item span {
  margin-left: 6px;
}

.empty-state {
  max-width: 500px;
  margin: 0 auto;
  border-radius: 12px;
}

.dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  color: #1976d2;
  margin-bottom: 16px;
  font-weight: 600;
}

.detail-row {
  margin-bottom: 12px;
  font-size: 0.95rem;
}

.detail-row strong {
  display: inline-block;
  width: 120px;
  color: #555;
}

@media (max-width: 960px) {
  .product-header {
    padding: 16px;
  }

  .product-details {
    padding: 16px;
  }

  .detail-row strong {
    width: 100px;
  }
}
</style>
