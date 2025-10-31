<template>
  <Default ref="default">
    <FarmerDashboard v-if="user && getCurrentUserRole() === 'farmer'"/>
    <BuyerDashboard v-if="user && getCurrentUserRole() === 'buyer'"/>
    <AdminDashboard v-if="user && getCurrentUserRole() === 'admin'"/>
    <ExporterDashboardLayout v-if="user && getCurrentUserRole() === 'exporter'"/>
    <SystemAdminDashboard v-if="user && getCurrentUserRole() === 'system_admin'"/>
    <ZoneSupervisorDashboard v-if="user && getCurrentUserRole() === 'zone_supervisor'"/>
  </Default>
</template>

<script>
import { mapState } from 'vuex';
import Default from '@/components/layout/Default.vue';
import FarmerDashboard from '@/components/layout/dashboard/FarmerDashboard.vue';
import AdminDashboard from '@/components/layout/dashboard/AdminDashboard.vue';
import BuyerDashboard from '@/views/buyers/BuyerDashboard.vue';
import ExporterDashboardLayout from '@/components/layout/dashboard/ExporterDashboard.vue';
import SystemAdminDashboard from '@/components/layout/dashboard/SystemAdminDashboard.vue';
import ZoneSupervisorDashboard from '@/components/layout/dashboard/ZoneSupervisorDashboard.vue';
import { getCurrentUserRole } from '@/utils/roles.js';

export default {
  methods: { getCurrentUserRole },
  components: {
    BuyerDashboard,
    AdminDashboard,
    FarmerDashboard,
    ExporterDashboardLayout,
    SystemAdminDashboard,
    ZoneSupervisorDashboard,
    Default,
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
  },
  mounted() {
    if (this.user == null || this.user === '') {
      this.$router.push({ name: 'Landing' });
      this.$refs.default.$refs.avatar.logout();
    }
  },
};
</script>

<style scoped>
/* Tailwind-like utility classes */
.bg-gray-50 {
  background-color: #f9fafb;
}
.min-h-screen {
  min-height: 100vh;
}
.p-8 {
  padding: 2rem;
}
.mb-8 {
  margin-bottom: 2rem;
}
.text-3xl {
  font-size: 1.875rem;
}
.font-bold {
  font-weight: 700;
}
.text-gray-800 {
  color: #1f2937;
}
.text-gray-600 {
  color: #4b5563;
}
.text-green-600 {
  color: #16a34a;
}
.text-blue-600 {
  color: #2563eb;
}
.text-purple-600 {
  color: #9333ea;
}
.rounded-lg {
  border-radius: 0.5rem;
}
.shadow-md {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
.hover\:shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
.transition-shadow {
  transition: box-shadow 0.3s ease-in-out;
}
.flex {
  display: flex;
}
.space-x-4 > * + * {
  margin-left: 1rem;
}

</style>
