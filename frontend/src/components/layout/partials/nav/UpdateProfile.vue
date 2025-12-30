<template>
  <div>
    <a href="#" @click.prevent="dialog = true"><v-icon color="primary">mdi-account</v-icon> My Profile</a>
    <v-dialog
        v-model="dialog"
        persistent
        max-width="700"
    >
      <v-card>
        <div class="tw-py-4 tw-flex tw-flex-col tw-justify-center">
          <div class="tw-w-full tw-flex tw-justify-center">
            <avatar-icon :username="user.fullName" :size="70" color="white" backgroundColor="#114cac" />
          </div>
          <div class="tw-text-center tw-mt-6">
            <p class="c-green-text">{{ user?.fullName || 'User' }}</p>
            <small class="c-gray-text">{{ user?.email || '—' }}</small>
          </div>
        </div>
        <div>
          <v-tabs
              v-model="tab"
              centered
              icons-and-text
          >
            <v-tabs-slider></v-tabs-slider>

            <v-tab class="tw-p-1" href="#tab-1">
              <v-icon left small>mdi-account</v-icon>
              Profile
            </v-tab>
            <v-tab href="#tab-2">
              <v-icon left small>mdi-account-group</v-icon>
              Roles
            </v-tab>
          </v-tabs>

          <v-tabs-items v-model="tab">
            <v-tab-item value="tab-1">
              <user-form />
            </v-tab-item>
            <v-tab-item value="tab-2">
              <roles-form />
            </v-tab-item>
          </v-tabs-items>
        </div>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              color="error"
              text
              @click="dialog = false"
          >
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import AvatarIcon from 'vue-avatar';
import UserForm from '@/components/layout/partials/nav/UserForm.vue';
import RolesForm from '@/components/layout/partials/nav/RolesForm.vue';

export default {
  name: 'UpdateProfile',
  components: {
    UserForm, AvatarIcon, RolesForm,
  },
  data() {
    return {
      dialog: false,
      tab: null,
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
  },
  mounted() {
    this.$root.$on('modal-close', () => {
      this.dialog = false;
    });
  },
};
</script>
