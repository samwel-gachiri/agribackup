<template>
  <div class="text-center">
    <v-dialog
        v-model="dialog"
        width="500"
    >
      <v-card class="tw-px-2">
        <card-title>Session Expired</card-title>

        <v-card-text class="tw-text-xl tw-mt-4">
          <p>You have left this browser idle for sometime <br>
            You will need to login again.
          </p>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              color="primary"
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
// import { NOTIFICATIONS } from '@/utils/const.js';

import CardTitle from '@/components/shared/CardTitle.vue';

export default {
  name: 'ScreenIdle',
  components: { CardTitle },
  props: ['dialog'],
  watch: {
    dialog(val) {
      if (!val) {
        this.$store.dispatch('auth/signOut').then(() => {
          // localStorage.removeItem(NOTIFICATIONS);
          if (caches) {
            caches.keys().then((arr) => {
              arr.forEach((key) => {
                caches.delete(key);
              });
            });
          }
          this.$store.commit('setClient', []);
          window.location.href = `/signin?r=${btoa(window.location.href)}`;
        });
      }
    },
  },
};
</script>
