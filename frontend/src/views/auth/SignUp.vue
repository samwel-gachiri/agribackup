<template>
  <v-app>
    <v-main>
      <div class="signup-background tw-flex tw-border-2 tw-justify-center tw-items-center tw-pt-4">
          <div>
            <v-card
              class="pa-6 signup-card"
              elevation="12"
              rounded="xl"
            >
              <!-- Header with Portal Context -->
              <v-card-title class="justify-center">
                <v-avatar size="64" :color="getPortalColor()">
                  <v-icon size="32" color="white">{{ getPortalIcon() }}</v-icon>
                </v-avatar>
              </v-card-title>

              <v-card-subtitle class="text-center text-h5 font-weight-bold mb-2">
                {{ getSignUpTitle() }}
              </v-card-subtitle>

              <!-- Portal Context Display -->
              <div v-if="portalContext" class="text-center mb-4">
                <v-chip
                  :color="getPortalColor()"
                  text-color="white"
                  class="ma-1"
                >
                  <v-icon left small>{{ getPortalIcon() }}</v-icon>
                  {{ getPortalDisplayName() }}
                </v-chip>
              </div>

              <v-card-text>
                <!-- Render appropriate signup component based on portal context -->
                <FarmerSignUp
                  v-if="portalContext === 'farmer'"
                  @farmer-registered="handleRegistrationSuccess"
                />
                <BuyerSignUp
                  v-else-if="portalContext === 'buyer'"
                  @buyer-registered="handleRegistrationSuccess"
                />
                <ExporterSignUp
                  v-else-if="portalContext === 'exporter'"
                  :role-context="roleContext"
                  @exporter-registered="handleRegistrationSuccess"
                />
                <AggregatorSignUp
                  v-else-if="portalContext === 'aggregator'"
                  @aggregator-registered="handleRegistrationSuccess"
                />
                <ProcessorSignUp
                  v-else-if="portalContext === 'processor'"
                  @processor-registered="handleRegistrationSuccess"
                />
                <ImporterSignUp
                  v-else-if="portalContext === 'importer'"
                  @importer-registered="handleRegistrationSuccess"
                />

                <!-- Fallback: Portal Selection -->
                <div v-else class="text-center">
                  <v-icon size="64" color="grey lighten-2" class="mb-4">mdi-account-plus</v-icon>
                  <h3 class="text-h6 mb-4">Select Portal to Register</h3>
                  <p class="text--secondary mb-6">Choose which portal you'd like to create an account for:</p>

                  <v-row>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="green"
                        @click="selectPortal('farmer')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-barn</v-icon>
                        Farmer Portal
                      </v-btn>
                    </v-col>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="blue"
                        @click="selectPortal('buyer')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-cart</v-icon>
                        Buyer Portal
                      </v-btn>
                    </v-col>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="purple"
                        @click="selectPortal('exporter')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-crown</v-icon>
                        Exporter Portal
                      </v-btn>
                    </v-col>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="orange"
                        @click="selectPortal('aggregator')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-warehouse</v-icon>
                        Aggregator Portal
                      </v-btn>
                    </v-col>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="teal"
                        @click="selectPortal('processor')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-factory</v-icon>
                        Processor Portal
                      </v-btn>
                    </v-col>
                    <v-col cols="12" sm="4">
                      <v-btn
                        block
                        large
                        color="indigo"
                        @click="selectPortal('importer')"
                        class="mb-3"
                      >
                        <v-icon left>mdi-ship-wheel</v-icon>
                        Importer Portal
                      </v-btn>
                    </v-col>
                  </v-row>
                </div>

                <!-- Navigation Links -->
                <div class="mt-6 text-center">
                  <span class="text-caption">Already have an account?</span>
                  <v-btn
                    text
                    color="primary"
                    class="ml-1"
                    small
                    @click="goToSignIn"
                  >
                    Sign In
                  </v-btn>
                </div>

                <!-- Contact Information -->
                <div class="mt-4 text-center">
                  <span class="text-caption text--secondary">Need help? Contact us:</span>
                  <br>
                  <v-btn
                    text
                    color="primary"
                    small
                    href="tel:+254705644805"
                    class="mt-1"
                  >
                    <v-icon left small>mdi-phone</v-icon>
                    +254 705 644 805
                  </v-btn>
                </div>
              </v-card-text>
            </v-card>
          </div>
      </div>
    </v-main>
  </v-app>
</template>

<script>
/* eslint-disable */
// import { getCurrentUserRole } from '@/utils/roles.js';
import FarmerSignUp from '@/components/auth/FarmerSignUp.vue';
import BuyerSignUp from '@/components/auth/BuyerSignUp.vue';
import ExporterSignUp from '@/components/auth/ExporterSignUp.vue';
import AggregatorSignUp from '@/components/auth/AggregatorSignUp.vue';
import ProcessorSignUp from '@/components/auth/ProcessorSignUp.vue';
import ImporterSignUp from '@/components/auth/ImporterSignUp.vue';
// import CardTitle from '@/components/shared/CardTitle.vue';

export default {
  name: 'SignUpView',
  metaInfo: {
    title: 'Sign Up | AgriBackup - Register for All Supply Chain Roles',
    meta: [
      { name: 'description', content: 'Create your AgriBackup account and join the global agricultural marketplace. Register as a farmer, buyer, exporter, aggregator, processor, or importer. Sell and buy farm produce directly, eliminate middlemen, and access AI-powered insights.' },
      { name: 'keywords', content: 'AgriBackup sign up, register, create account, farmer registration, buyer registration, exporter registration, aggregator registration, processor registration, importer registration, agriculture, global marketplace, supply chain, direct selling, eliminate middlemen' },
      { name: 'robots', content: 'index,follow' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:title', content: 'Sign Up | AgriBackup' },
      { property: 'og:description', content: 'Create your AgriBackup account and join the global agricultural marketplace. Register for any role in the supply chain.' },
      { property: 'og:type', content: 'website' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:url', content: 'https://agribackup.com/signup' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { name: 'twitter:card', content: 'summary_large_image' },
      { name: 'twitter:title', content: 'Sign Up | AgriBackup' },
      { name: 'twitter:description', content: 'Create your AgriBackup account and join the global agricultural marketplace.' },
      { name: 'twitter:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { rel: 'canonical', href: 'https://agribackup.com/signup' },
    ],
    script: [
      {
        type: 'application/ld+json',
        json: {
          '@context': 'https://schema.org',
          '@type': 'WebPage',
          name: 'Sign Up | AgriBackup',
          url: 'https://agribackup.com/signup',
          description: 'Create your AgriBackup account and join the global agricultural marketplace. Register as a farmer, buyer, exporter, aggregator, processor, or importer. Sell and buy farm produce directly, eliminate middlemen, and access AI-powered insights.',
          image: 'https://agribackup.com/assets/images/happy-farmer.jpg',
        },
      },
    ],
  },
  components: {
    // CardTitle,
    FarmerSignUp,
    BuyerSignUp,
    ExporterSignUp,
    AggregatorSignUp,
    ProcessorSignUp,
    ImporterSignUp,
  },
  computed: {
    portalContext() {
      return this.$route.query.portal || this.$store.getters['auth/portalContext']?.portal;
    },
    roleContext() {
      return this.$route.query.role || this.$store.getters['auth/portalContext']?.role;
    },
  },
  mounted() {
    // If no portal context, show portal selection
    if (!this.portalContext) {
      this.$toast.info('Select a portal to create your account');
    } else {
      this.$toast.info(`Creating account for ${this.getPortalDisplayName()}`);
    }
  },
  methods: {
    selectPortal(portal) {
      if (portal === 'exporter') {
        // For exporter, redirect to Home to select role first
        this.$router.push({ name: 'Home' });
      } else {
        // Update route with portal context
        this.$router.push({
          name: 'SignUp',
          query: { portal },
        });
      }
    },

    handleRegistrationSuccess() {
      this.$toast.success('Account created successfully! Please sign in');

      // Redirect to sign in with portal context
      const query = {};
      if (this.portalContext) query.portal = this.portalContext;
      if (this.roleContext) query.role = this.roleContext;

      this.$router.push({
        name: 'SignIn',
        query,
      });
    },

    goToSignIn() {
      const query = {};
      if (this.portalContext) query.portal = this.portalContext;
      if (this.roleContext) query.role = this.roleContext;

      this.$router.push({
        name: 'SignIn',
        query,
      });
    },

    getSignUpTitle() {
      if (!this.portalContext) return 'Create Account';

      const titles = {
        farmer: 'Create Farmer Account',
        buyer: 'Create Buyer Account',
        exporter: 'Create Exporter Account',
        aggregator: 'Create Aggregator Account',
        processor: 'Create Processor Account',
        importer: 'Create Importer Account',
      };

      return titles[this.portalContext] || 'Create Account';
    },

    getPortalDisplayName() {
      if (!this.portalContext) return '';

      const names = {
        farmer: 'Farmer Portal',
        buyer: 'Buyer Portal',
        exporter: this.getRoleDisplayName(),
        aggregator: 'Aggregator Portal',
        processor: 'Processor Portal',
        importer: 'Importer Portal',
      };

      return names[this.portalContext] || '';
    },

    getRoleDisplayName() {
      if (this.portalContext !== 'exporter' || !this.roleContext) return 'Exporter Portal';

      const roleNames = {
        exporter: 'Super Admin Portal',
        system_admin: 'System Admin Portal',
        zone_supervisor: 'Zone Supervisor Portal',
      };

      return roleNames[this.roleContext] || 'Exporter Portal';
    },

    getPortalColor() {
      const colors = {
        farmer: 'green',
        buyer: 'blue',
        exporter: 'purple',
        aggregator: 'orange',
        processor: 'teal',
        importer: 'indigo',
      };

      return colors[this.portalContext] || 'primary';
    },

    getPortalIcon() {
      const icons = {
        farmer: 'mdi-barn',
        buyer: 'mdi-cart',
        exporter: 'mdi-crown',
        aggregator: 'mdi-warehouse',
        processor: 'mdi-factory',
        importer: 'mdi-ship-wheel',
      };

      return icons[this.portalContext] || 'mdi-account-plus';
    },
  },
};
</script>

<style scoped>
.signup-background {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.signup-card {
  border-radius: 16px;
  background: white;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .signup-card {
    margin: 1rem;
    padding: 1rem !important;
  }

  .v-card-title {
    padding: 1rem 0 !important;
  }

  .v-card-subtitle {
    font-size: 1.3rem !important;
  }
}

@media (max-width: 480px) {
  .v-card-subtitle {
    font-size: 1.1rem !important;
  }

  .v-btn {
    font-size: 0.875rem;
  }
}

/* Clean professional styling */
.v-card {
  border-radius: 16px;
}

.v-card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .v-btn {
    min-height: 44px;
    min-width: 44px;
  }
}
</style>
