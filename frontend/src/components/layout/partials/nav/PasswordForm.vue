<template>
  <v-card class="tw-px-4 md:tw-px-8 tw-flex tw-justify-center tw-mt-3" flat>
    <div class="tw-w-full md:tw-w-64">
      <v-form v-model="isValid">
        <v-text-field
            class="my-3"
            id="password"
            label="Old Password"
            :type="oldPasswordField"
            v-model="form.oldPassword"
            :rules="[required('Old Password')]"
        >
          <v-icon slot="prepend" color="primary">mdi-lock</v-icon>
          <v-icon slot="append" color="primary" class="tw-cursor-pointer" @click="oldPasswordField = oldPasswordField === 'password' ? 'text' : 'password'">{{ oldPasswordField === 'password' ? 'mdi-eye' : 'mdi-eye-off' }}</v-icon>
        </v-text-field>
        <v-text-field
            class="my-3"
            id="newPassword"
            label="New Password"
            :type="passwordField"
            v-model="form.newPassword"
            :rules="[required('New Password'), minLength('password', 8), lowerCaseFormat(), upperCaseFormat(), digitFormat(), specialCharFormat()]"
        >
          <v-icon slot="prepend" color="primary">mdi-lock</v-icon>
          <v-icon slot="append" color="primary" class="tw-cursor-pointer" @click="passwordField = passwordField === 'password' ? 'text' : 'password'">{{ passwordField === 'password' ? 'mdi-eye' : 'mdi-eye-off' }}</v-icon>
        </v-text-field>

        <v-text-field
            class="tw-mb-5 my-3"
            id="ConfirmPassword"
            label="Confirm Password"
            :type="passwordConfirmField"
            v-model="form.confirmPassword"
            :rules="[required('Confirm Password'), confirmPassword('Confirm Password', form.newPassword)]"
        >
          <v-icon slot="prepend" color="primary">mdi-lock</v-icon>
          <v-icon slot="append" color="primary" class="tw-cursor-pointer" @click="passwordConfirmField = passwordConfirmField === 'password' ? 'text' : 'password'">{{ passwordConfirmField === 'password' ? 'mdi-eye' : 'mdi-eye-off' }}</v-icon>
        </v-text-field>

        <v-card-actions class="col-sm-6 offset-sm-3">
          <v-btn block small
                 id="passReset"
                 color="secondary"
                 @click="update"
                 :disabled="!isValid"
                 :loading="loading">
            Update Password
          </v-btn>
        </v-card-actions>
      </v-form>
    </div>
  </v-card>
</template>

<script>
import { mapGetters } from 'vuex';
import validations from '@/utils/validations.js';
import helpers from '@/utils/helpers.js';

export default {
  name: 'PasswordForm',
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      },
      isValid: false,
      ...validations,
      loading: false,
      passwordField: 'password',
      oldPasswordField: 'password',
      passwordConfirmField: 'password',
    };
  },
  computed: {
    ...mapGetters('auth', ['hasAuthenticationStatus', 'authenticationStatus']),
  },
  methods: {
    async update() {
      this.loading = true;
      await this.$store.dispatch('auth/passwordChange', this.form).then(() => {
        if (this.hasAuthenticationStatus) {
          this.loading = false;
          if (this.authenticationStatus.variant === 'error') {
            this.$toast.error(this.authenticationStatus.message, 'Error');
          } else {
            this.$toast.success(this.authenticationStatus.message, 'Success');
          }
          helpers.clearForm(this.form);
        }
      });
      if (this.hasAuthenticationStatus && this.authenticationStatus.variant === 'success') {
        this.$root.$emit('modal-close');
      }
    },
  },
};
</script>
