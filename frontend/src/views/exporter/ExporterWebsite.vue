<template>
  <div class="exporter-website">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <v-progress-circular
        indeterminate
        color="primary"
        size="64"
      ></v-progress-circular>
      <p class="mt-4">Loading exporter website...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <v-alert type="error" class="mb-4">
        {{ error }}
      </v-alert>
      <v-btn color="primary" @click="loadExporterData">
        Try Again
      </v-btn>
    </div>

    <!-- Main Website Content -->
    <div v-else class="website-content">
      <!-- Navigation Header -->
      <v-app-bar
        color="primary"
        dark
        elevation="2"
        class="website-header"
      >
        <v-toolbar-title class="company-title">
          <v-icon left>mdi-office-building</v-icon>
          {{ exporterData.profile?.companyName || 'Exporter Company' }}
        </v-toolbar-title>

        <v-spacer></v-spacer>

        <!-- Navigation Menu -->
        <v-tabs
          v-model="activeTab"
          background-color="transparent"
          class="hidden-sm-and-down"
        >
          <v-tab href="#home">Home</v-tab>
          <v-tab href="#about">About</v-tab>
          <v-tab href="#products">Products</v-tab>
        </v-tabs>

        <!-- Mobile Menu -->
        <v-menu offset-y class="hidden-md-and-up">
          <template v-slot:activator="{ on, attrs }">
            <v-btn icon v-bind="attrs" v-on="on">
              <v-icon>mdi-menu</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @click="activeTab = 'home'">
              <v-list-item-title>Home</v-list-item-title>
            </v-list-item>
            <v-list-item @click="activeTab = 'about'">
              <v-list-item-title>About</v-list-item-title>
            </v-list-item>
            <v-list-item @click="activeTab = 'products'">
              <v-list-item-title>Products</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-app-bar>

      <!-- Tab Content -->
      <v-tabs-items v-model="activeTab" class="tab-content">
        <!-- Home Tab -->
        <v-tab-item value="home">
          <ExporterWebsiteHome
            :exporter="exporterData.profile"
            :products="exporterData.products"
            :certificates="exporterData.certificates"
          />
        </v-tab-item>

        <!-- About Tab -->
        <v-tab-item value="about">
          <ExporterWebsiteAbout
            :exporter="exporterData.profile"
          />
        </v-tab-item>

        <!-- Products Tab -->
        <v-tab-item value="products">
          <ExporterWebsiteProducts
            :products="exporterData.products"
            :certificates="exporterData.certificates"
          />
        </v-tab-item>
      </v-tabs-items>

      <!-- Footer -->
      <v-footer
        color="grey lighten-4"
        class="mt-8"
      >
        <v-container>
          <v-row>
            <v-col cols="12" md="6">
              <h4>{{ exporterData.profile?.companyName || 'Exporter Company' }}</h4>
              <p class="text--secondary">
                EUDR Compliant Agricultural Products
              </p>
            </v-col>
            <v-col cols="12" md="6" class="text-right">
              <p class="text--secondary">
                Powered by AgriBackup Platform
              </p>
              <v-chip
                small
                color="success"
                class="mt-2"
              >
                <v-icon left small>mdi-shield-check</v-icon>
                EUDR Verified
              </v-chip>
            </v-col>
          </v-row>
        </v-container>
      </v-footer>
    </div>
  </div>
</template>

<script>
import ExporterWebsiteHome from '@/components/exporter/ExporterWebsiteHome.vue';
import ExporterWebsiteAbout from '@/components/exporter/ExporterWebsiteAbout.vue';
import ExporterWebsiteProducts from '@/components/exporter/ExporterWebsiteProducts.vue';
import exporterWebsiteService from '@/services/exporterWebsite.service';

export default {
  name: 'ExporterWebsite',
  components: {
    ExporterWebsiteHome,
    ExporterWebsiteAbout,
    ExporterWebsiteProducts,
  },
  data() {
    return {
      loading: true,
      error: null,
      activeTab: 'home',
      exporterData: {
        profile: null,
        products: [],
        certificates: [],
      },
    };
  },
  computed: {
    exporterId() {
      return this.$route.params.exporterId;
    },
  },
  async created() {
    await this.loadExporterData();
  },
  methods: {
    async loadExporterData() {
      try {
        this.loading = true;
        this.error = null;

        const data = await exporterWebsiteService.getExporterWebsiteData(this.exporterId);
        this.exporterData = data;
      } catch (error) {
        this.$toast.error('Error loading exporter data:', error);
        this.error = 'Failed to load exporter information. Please try again later.';
      } finally {
        this.loading = false;
      }
    },
  },
  watch: {
    '$route.params.exporterId': {
      handler: 'loadExporterData',
      immediate: false,
    },
  },
};
</script>

<style scoped>
.exporter-website {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
}

.website-header {
  position: sticky;
  top: 0;
  z-index: 100;
}

.company-title {
  font-weight: 600;
  font-size: 1.2rem;
}

.tab-content {
  background-color: transparent;
}

.v-tab {
  text-transform: none;
  font-weight: 500;
}

@media (max-width: 960px) {
  .company-title {
    font-size: 1rem;
  }
}
</style>
