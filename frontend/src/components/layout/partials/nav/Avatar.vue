<template>
  <v-menu
      v-model="menu"
      :close-on-content-click="false"
      offset-x
      offset-y
  >
    <template v-slot:activator="{ on }">
      <div
          v-if="isAuthenticated"
          class="tw-flex tw-px-2 tw-items-center tw-cursor-pointer"
          v-on="on"
      >
        <div :class="['tw-px-2 tw-hidden md:tw-block', $route.name === 'Landing' ? 'tw-text-white' : 'c-gray-text']">{{ user.fullName }} <v-icon :color="$route.name === 'Landing' ? '#fff' : 'gray'">mdi-chevron-down</v-icon></div>
        <avatar-icon :username="user.fullName" :size="32" :color="$route.name === 'Landing' ? '#4aac11' : 'white'" :backgroundColor="$route.name === 'Landing' ? 'white' : '#2dac11'" />
        <div class="tw-absolute tw-block md:tw-hidden tw-mt-6 tw-ml-6">
          <v-icon :color="$route.name === 'Landing' ? '#fff' : 'gray'">mdi-chevron-down</v-icon>
        </div>
      </div>
    </template>

    <v-card>
      <v-list>
        <v-list-item>
          <v-list-item-action>
            <router-link :to="{name: 'Dashboard'}"><v-icon color="primary">mdi-apps</v-icon> Dashboard </router-link>
          </v-list-item-action>
        </v-list-item>
        <v-list-item>
          <v-list-item-action>
            <update-profile v-if="isAuthenticated" />
          </v-list-item-action>
        </v-list-item>
        <v-list-item>
          <v-list-item-action>
            <a href="#" @click.prevent="logout"><v-icon color="primary">mdi-login</v-icon> Logout</a>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <v-divider></v-divider>
    </v-card>
  </v-menu>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import AvatarIcon from 'vue-avatar';
import UpdateProfile from '@/components/layout/partials/nav/UpdateProfile.vue';
// import { NOTIFICATIONS } from '@/utils/const.js';

export default {
  name: 'Avatar',
  data: () => ({
    menu: null,
  }),
  components: { UpdateProfile, AvatarIcon },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
    ...mapGetters('auth', ['isAuthenticated']),
  },
  methods: {
    async logout() {
      await this.$store.dispatch('auth/signOut').then(() => {
        // localStorage.removeItem(NOTIFICATIONS);
        if (caches) {
          caches.keys().then((arr) => {
            arr.forEach((key) => {
              caches.delete(key);
            });
          });
        }
        this.$store.commit('setClient', []);
        this.$router.push({ name: 'Home' });
      });
    },
  },
};
</script>
