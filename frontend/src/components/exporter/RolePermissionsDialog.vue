<template>
  <v-dialog v-model="model" max-width="700">
    <v-card>
      <v-card-title class="tw-text-base tw-font-semibold">
        <v-icon left class="tw-mr-1">mdi-shield-account</v-icon>
        Role Permissions (Preview)
      </v-card-title>
      <v-card-text>
        <v-alert type="info" dense text class="tw-mb-4">
          This is a placeholder. Integrate real permissions mapping later.
        </v-alert>
        <v-simple-table dense>
          <thead>
            <tr>
              <th class="tw-text-left">Role</th>
              <th class="tw-text-left">Permissions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="role in roles" :key="role.name">
              <td class="tw-font-medium">{{ role.name }}</td>
              <td>
                <v-chip
                  v-for="p in role.permissions"
                  :key="p"
                  small
                  class="tw-m-1"
                  color="primary"
                  text-color="white"
                >{{ p }}</v-chip>
              </td>
            </tr>
          </tbody>
        </v-simple-table>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn text @click="close">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script>
export default {
  name: 'RolePermissionsDialog',
  props: {
    value: { type: Boolean, default: false },
  },
  data() {
    return {
      roles: [
        { name: 'Exporter', permissions: ['READ_ANALYTICS', 'MANAGE_USERS', 'MANAGE_ZONES'] },
        { name: 'System Admin', permissions: ['ASSIGN_SUPERVISORS', 'VIEW_FARMERS'] },
        { name: 'Zone Supervisor', permissions: ['VIEW_ZONES', 'VIEW_FARMERS', 'CREATE_PICKUPS'] },
      ],
    };
  },
  computed: {
    model: {
      get() { return this.value; },
      set(v) { this.$emit('input', v); },
    },
  },
  methods: {
    close() { this.$emit('input', false); },
  },
};
</script>
