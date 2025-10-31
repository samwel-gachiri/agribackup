<template>
  <v-dialog v-model="dialog" max-width="800">
    <v-card>
      <v-card-title class="headline">Terms and Conditions</v-card-title>
      <v-card-text>
        <p>
          By using this application, you agree to the following terms and conditions:
        </p>
        <ul>
          <li>You must be at least 18 years old to use this service.</li>
          <li>You are responsible for maintaining the confidentiality of your account.</li>
          <li>We reserve the right to terminate your account for violations of these terms.</li>
          <li>Your data will be handled in accordance with our Privacy Policy.</li>
        </ul>
        <p>
          For any questions, please contact us at:
        </p>
        <ul>
          <li>Phone: <a href="tel:+254705644805">+254 705 644 805</a></li>
          <li>Email: <a href="mailto:agribackupltd@gmail.com">agribackupltd@gmail.com</a></li>
        </ul>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="dialog = false">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: 'terms-and-conditions',
  data() {
    return {
      dialog: false,
    };
  },
  methods: {
    openDialog() {
      this.dialog = true;
    },
  },
};
</script>

<style scoped>
</style>
