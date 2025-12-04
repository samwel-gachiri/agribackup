<template>
  <v-app>
    <v-container class="fill-height" fluid>
      <v-row align="center" justify="center">
        <v-col cols="12" sm="10" md="8" lg="6">
          <div class="text-center mb-8">
            <h2 class="text-h4 font-weight-bold text-green-700 mb-2">Select Your Role</h2>
            <p class="text-subtitle-1 text-gray-600">Choose how you want to use AgriBackup</p>
          </div>

          <v-row>
            <v-col cols="12" sm="6" md="4" v-for="role in roles" :key="role.value">
              <v-card
                class="role-card h-100"
                hover
                @click="selectRole(role.value)"
                elevation="2"
              >
                <v-card-text class="text-center pa-6">
                  <v-icon size="64" :color="role.color" class="mb-4">{{ role.icon }}</v-icon>
                  <h3 class="text-h6 font-weight-bold mb-2">{{ role.title }}</h3>
                  <p class="text-body-2 text-gray-500 mb-0">{{ role.description }}</p>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </v-container>
  </v-app>
</template>

<script>
export default {
  name: 'RoleSelection',
  data() {
    return {
      roles: [
        {
          title: 'Farmer',
          value: 'farmer',
          icon: 'mdi-barn',
          color: 'green',
          description: 'Register your farm and produce',
        },
        {
          title: 'Buyer',
          value: 'buyer',
          icon: 'mdi-cart',
          color: 'blue',
          description: 'Purchase produce directly from farmers',
        },
        {
          title: 'Exporter',
          value: 'exporter',
          icon: 'mdi-crown',
          color: 'purple',
          description: 'Export produce to international markets',
        },
        {
          title: 'Aggregator',
          value: 'aggregator',
          icon: 'mdi-warehouse',
          color: 'orange',
          description: 'Collect and store produce',
        },
        {
          title: 'Processor',
          value: 'processor',
          icon: 'mdi-factory',
          color: 'teal',
          description: 'Process raw produce into products',
        },
        {
          title: 'Importer',
          value: 'importer',
          icon: 'mdi-ship-wheel',
          color: 'indigo',
          description: 'Import produce from other regions',
        },
      ],
    };
  },
  methods: {
    selectRole(role) {
      // Redirect to the specific sign-up/profile completion page for the selected role
      // We can reuse the existing SignUp page with the portal query param
      // Or if we want to keep the user logged in and just add the role, we might need a different flow.
      // For now, let's assume we redirect to the SignUp page with the portal context,
      // but since the user is already logged in (conceptually), we might need to handle "upgrading" the profile.

      // However, the current SignUp.vue handles new registrations.
      // If the user is already logged in as a generic user, we should probably have a "Complete Profile" view.
      // But given the constraints and the current codebase, let's try to route them to the sign-up flow
      // which might need to be slightly adjusted to "attach" to the existing user if they are logged in.

      // Actually, the simplest way for now is to redirect to the SignUp page with the portal param.
      // But wait, SignUp creates a NEW user.
      // We need a way to "upgrade" the current generic user to a specific role.
      // The backend `registerFarmer` etc. checks for existing user by email/phone.
      // So if we pre-fill the SignUp form with the current user's details, it might work
      // IF the backend logic allows adding a role to an existing user.

      // I checked AuthService.registerFarmer:
      // val existingUser = ... findByEmail ...
      // if (existingUser != null) { ... Reuse existing user and add Farmer role ... }
      // So yes, the backend supports it!

      // So we just need to redirect to SignUp with the portal param.
      // Ideally, we should pre-fill the form data if possible, but the current SignUp flow might not support that easily without changes.
      // For now, let's redirect.

      this.$router.push({ name: 'SignUp', query: { portal: role } });
    },
  },
};
</script>

<style scoped>
.role-card {
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.role-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1) !important;
}
</style>
