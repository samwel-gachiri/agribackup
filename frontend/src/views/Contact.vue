<template>
  <v-app>
    <v-main>
      <div class="contact-background">
        <v-container class="fill-height" fluid>
          <v-row align="center" justify="center">
            <v-col cols="12" sm="8" md="6" lg="5">
              <v-card
                class="pa-6 contact-card"
                elevation="12"
                rounded="xl"
              >
                <v-card-title class="justify-center text-center">
                  <v-avatar size="64" color="primary">
                    <v-icon size="32" color="white">mdi-phone</v-icon>
                  </v-avatar>
                </v-card-title>

                <v-card-subtitle class="text-center text-h5 font-weight-bold mb-4">
                  Contact Us
                </v-card-subtitle>

                <v-card-text class="text-center">
                  <p class="text-body-1 mb-6">
                    Need assistance with sign in, sign up, or any other issues?
                    Our support team is here to help.
                  </p>

                  <div class="contact-info mb-6">
                    <v-btn
                      block
                      large
                      color="primary"
                      href="tel:+254705644805"
                      class="mb-4"
                    >
                      <v-icon left>mdi-phone</v-icon>
                      Call Support: +254 705 644 805
                    </v-btn>

                    <v-btn
                      block
                      large
                      color="secondary"
                      href="mailto:agribackupltd@gmail.com"
                      class="mb-4"
                    >
                      <v-icon left>mdi-email</v-icon>
                      Email Support
                    </v-btn>
                  </div>

                  <div class="back-button">
                    <v-btn
                      text
                      color="primary"
                      @click="$router.go(-1)"
                    >
                      <v-icon left>mdi-arrow-left</v-icon>
                      Go Back
                    </v-btn>
                  </div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </div>
    </v-main>
  </v-app>
</template>

<script>
export default {
  name: 'ContactView',
  metaInfo: {
    title: 'Contact Us | AgriBackup - Get Help and Support',
    meta: [
      { name: 'description', content: 'Contact AgriBackup support for help with sign in, sign up, and any other issues. Our team is ready to assist you.' },
      { name: 'keywords', content: 'AgriBackup contact, support, help, customer service, phone support, email support' },
      { name: 'robots', content: 'index,follow' },
      { property: 'og:title', content: 'Contact Us | AgriBackup' },
      { property: 'og:description', content: 'Contact AgriBackup support for help with sign in, sign up, and any other issues.' },
      { property: 'og:type', content: 'website' },
      { property: 'og:url', content: 'https://agribackup.com/contact' },
      { property: 'og:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { name: 'twitter:card', content: 'summary_large_image' },
      { name: 'twitter:title', content: 'Contact Us | AgriBackup' },
      { name: 'twitter:description', content: 'Contact AgriBackup support for help with sign in, sign up, and any other issues.' },
      { name: 'twitter:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { rel: 'canonical', href: 'https://agribackup.com/contact' },
    ],
  },
};
</script>

<style scoped>
.contact-background {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: 100vh;
}

.contact-card {
  border-radius: 16px;
  background: white;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.contact-info {
  margin: 2rem 0;
}

.back-button {
  margin-top: 2rem;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .contact-card {
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
</style>
