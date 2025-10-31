<template>
  <v-app>
    <div class="tw-flex tw-flex-col tw-items-center tw-p-6">
      <logo-title></logo-title>
      <v-card class="tw-w-96 tw-p-6">
        <v-card-title class="tw-text-center tw-text-xl tw-font-bold">Reset Password</v-card-title>
        <v-form v-model="isValid">
          <v-card-text>
            <v-text-field v-model="phoneNumber" label="Phone Number" type="tel" variant="outlined" class="tw-mb-4" disabled />
            <div>
              <h2>Enter Verification Code</h2>
              <v-otp-input
                  v-model="code"
                  type="text"
                  variant="outlined"
                  length="6"
                  class="tw-mb-4"/>
            </div>
            <v-text-field
                class="tw-my-3"
                dense
                id="password"
                label="Password"
                :type="passwordField"
                v-model="newPassword"
                :rules="[required('Password'), minLength('password', 8), lowerCaseFormat(), upperCaseFormat(), digitFormat(), specialCharFormat()]"
            >
              <v-icon slot="prepend" color="primary">mdi-lock</v-icon>
              <v-icon slot="append" color="primary" class="tw-cursor-pointer" @click="passwordField = passwordField === 'password' ? 'text' : 'password'">{{ passwordField === 'password' ? 'mdi-eye' : 'mdi-eye-off' }}</v-icon>
            </v-text-field>
            <div class="tw-w-full tw-flex tw-flex-row-reverse">
              <router-link :to="{name: 'SignIn' }" class="tw-my-3">Back to Log In</router-link>
            </div>
            <v-btn color="primary" @click="resetPassword" block :disabled="!isValid || code == null || code.length < 6">Reset Password</v-btn>
          </v-card-text>
        </v-form>
      </v-card>
    </div>
  </v-app>
</template>

<script>
import Auth from '@aws-amplify/auth';
import Validations from '@/utils/validations.js';
import LogoTitle from '@/components/shared/LogoText.vue';

export default {
  components: { LogoTitle },
  props: ['phoneNumber'],
  data() {
    return {
      code: '',
      newPassword: '',
      passwordField: 'password',
      isValid: false,
    };
  },
  methods: {
    ...Validations,
    async resetPassword() {
      try {
        await Auth.forgotPasswordSubmit(this.phoneNumber, this.code, this.newPassword);
        this.$toast.success('Password reset successful!');
        this.$router.push({ name: 'SignIn' });
      } catch (error) {
        this.$toast.error('Error resetting password:', error.message);
      }
    },
  },
};
</script>
