import Vue from 'vue';
import Vuex from 'vuex';

// import rfq from '@/store/modules/rfq.vue';
import axios from 'axios';
// import renewal from '@/store/modules/renewal.vue';
import auth from './modules/auth.js';
// import variables from './modules/variables';

Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    client: [],
    clientLoading: true,
    notification: {},
    policy: '',
  },
  getters: {
    clientData: (state) => (state.client.length === 0 ? null : state.client[0]),
    notificationCount: (state) => ('MOTOR' in state.notification ? state.notification.MOTOR : 0) || 0,
  },
  mutations: {
    setClient(state, payload) {
      state.clientLoading = false;
      state.client = payload;
    },
    updateNotification(state, payload) {
      state.notification = payload;
    },
    updatePolicy(state, payload) {
      state.policy = payload;
    },
  },
  actions: {
    getNotificationCount: async (context) => {
      const user = context.state.auth.user;
      if (!user || !user.email) return; // guard against undefined user/email
      await axios.get(`/customer/notification/app/unread/count/${user.email}?pin=${user['custom:kra-pin'] || ''}`).then((response) => {
        let data = null;
        if (response.data) {
          data = response.data.data;
        } else {
          data = { success: true, data: { MOTOR: 19, HEALTH: 0, LIFE: 0 } };
        }
        context.commit('updateNotification', data);
      });
    },
    getPolicy: async (context, param) => {
      await axios.get(`/customer/gis/policies/details?policyNo=${param}`)
        .then((response) => {
          context.commit('updatePolicy', response?.data?.object);
        });
    },
  },
  modules: {
    auth,
    // rfq,
    // renewal,
    // variables,
  },
  strict: process.env.NODE_ENV !== 'production',
});

export default store;
