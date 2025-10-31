<template>
  <Default>
    <div class="tw-p-6 tw-space-y-8 tw-bg-gradient-to-br tw-from-slate-50 tw-via-white tw-to-slate-100 tw-min-h-screen">
      <!-- Header & Actions -->
      <div class="tw-flex tw-items-start tw-justify-between tw-flex-wrap tw-gap-6">
        <div class="tw-space-y-2">
<!--          <div class="tw-flex tw-items-center tw-gap-3">-->
<!--            <h1 class="tw-text-3xl tw-font-black tw-tracking-tight tw-bg-clip-text tw-text-transparent tw-bg-gradient-to-r tw-from-indigo-700 tw-to-teal-500">System Admins</h1>-->
<!--            <v-chip small color="indigo darken-2" text-color="white" class="tw-font-semibold tw-uppercase">Managed</v-chip>-->
<!--          </div>-->
<!--          <p class="tw-text-sm tw-text-slate-600 tw-max-w-xl">Create and manage system administrators. Update profiles and toggle active status.</p>-->
          <div class="tw-flex tw-gap-2 tw-flex-wrap">
            <v-btn color="primary" small :loading="loading" class="tw-rounded-xl tw-shadow-sm tw-font-medium" @click="refreshAdmins">
              <v-icon left small>mdi-refresh</v-icon>Refresh
            </v-btn>
            <v-btn color="success" small class="tw-rounded-xl tw-shadow-sm tw-font-medium" @click="openCreateDialog">
              <v-icon left small>mdi-account-plus</v-icon>New System Admin
            </v-btn>
            <v-btn small outlined class="tw-rounded-xl tw-font-medium" @click="clearSearch">
              <v-icon left small>mdi-filter-remove</v-icon>Reset
            </v-btn>
          </div>
        </div>
        <!-- Metrics -->
        <div class="tw-grid tw-grid-cols-2 md:tw-grid-cols-4 tw-gap-3 tw-w-full md:tw-w-auto">
          <div class="tw-bg-white tw-rounded-2xl tw-border tw-border-slate-200 tw-px-4 tw-py-3 tw-flex tw-flex-col tw-gap-1 tw-shadow-sm hover:tw-shadow transition">
            <div class="tw-text-[11px] tw-font-semibold tw-tracking-wide tw-text-slate-500 tw-uppercase">Admins</div>
            <div class="tw-flex tw-items-end tw-gap-2">
              <div class="tw-text-xl tw-font-bold tw-text-slate-800">{{ admins.length }}</div>
            </div>
            <div class="tw-text-[10px] tw-text-slate-400 tw-font-medium">Active only</div>
          </div>
          <div class="tw-bg-white tw-rounded-2xl tw-border tw-border-slate-200 tw-px-4 tw-py-3 tw-flex tw-flex-col tw-gap-1 tw-shadow-sm hover:tw-shadow transition">
            <div class="tw-text-[11px] tw-font-semibold tw-tracking-wide tw-text-slate-500 tw-uppercase">Search</div>
            <div class="tw-flex tw-items-end tw-gap-2">
              <div class="tw-text-xl tw-font-bold tw-text-slate-800">{{ search ? 'On' : 'Off' }}</div>
            </div>
            <div class="tw-text-[10px] tw-text-slate-400 tw-font-medium">Server filtered</div>
          </div>
        </div>
      </div>

      <!-- Search -->
      <div class="tw-bg-white/80 tw-backdrop-blur-sm tw-shadow-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-p-6 tw-space-y-3">
        <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Search</label>
        <div class="tw-grid md:tw-grid-cols-3 tw-gap-4">
          <input v-model.trim="search" @input="onSearchInput" type="text" placeholder="Search by name, email, phone or ID" class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-indigo-500 focus:tw-bg-white tw-transition" />
        </div>
        <div class="tw-text-xs tw-text-gray-500">Tip: Type at least 2 characters to search; clearing re-loads the full list.</div>
      </div>

      <!-- List -->
      <div class="tw-bg-white/90 tw-backdrop-blur-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-shadow-sm tw-overflow-hidden" style="min-height:420px;display:flex;flex-direction:column;">
        <div class="tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-4 tw-bg-gradient-to-r tw-from-indigo-600 tw-to-teal-600 tw-text-white">
          <div class="tw-font-semibold tw-text-sm md:tw-text-base tw-flex tw-items-center tw-gap-2">
            <v-icon small>mdi-account-tie</v-icon>
            System Admins <span class="tw-text-white/80 tw-text-xs">({{ admins.length }})</span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-btn small text class="tw-text-white tw-font-medium tw-rounded-lg" :disabled="!admins.length" @click="exportCsv"><v-icon left small>mdi-download</v-icon>Export CSV</v-btn>
          </div>
        </div>
        <div v-if="loading" class="tw-px-6 tw-pt-6 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-4 tw-w-full" style="flex:1;">
          <div v-for="n in 6" :key="n" class="tw-animate-pulse tw-rounded-xl tw-bg-slate-100 tw-h-32 tw-border tw-border-slate-200"></div>
        </div>
        <div v-else style="flex:1;display:flex;flex-direction:column;">
          <div v-if="!admins.length" class="tw-p-10 tw-text-center tw-text-slate-500 tw-text-sm">No system admins found.</div>
          <ul v-else class="tw-divide-y tw-bg-slate-50/60" style="flex:1;overflow:auto;">
            <li v-for="a in admins" :key="a.id" class="tw-p-4 tw-grid md:tw-grid-cols-12 tw-gap-4 hover:tw-bg-white tw-transition">
              <div class="md:tw-col-span-4">
                <div class="tw-font-semibold tw-text-gray-800 tw-flex tw-items-center tw-gap-2"><v-icon small color="indigo">mdi-account</v-icon>{{ a.fullName }}</div>
                <div class="tw-text-xs tw-text-gray-500 tw-truncate">{{ a.email || '—' }}</div>
              </div>
              <div class="md:tw-col-span-3 tw-text-sm">
                <div class="tw-text-gray-700">{{ a.phoneNumber || '—' }}</div>
                <div class="tw-text-xs tw-text-gray-500">Phone</div>
              </div>
              <div class="md:tw-col-span-2 tw-text-sm">
                <div class="tw-text-gray-700">{{ a.status }}</div>
                <div class="tw-text-xs tw-text-gray-500">Status</div>
              </div>
              <div class="md:tw-col-span-3 tw-flex tw-items-center tw-gap-2 tw-justify-end">
                <v-tooltip top>
                  <template #activator="{ on, attrs }">
                    <v-btn v-bind="attrs" v-on="on" small icon color="primary" @click="openEditDialog(a)">
                      <v-icon small>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                  <span>Edit</span>
                </v-tooltip>
                <v-tooltip top>
                  <template #activator="{ on, attrs }">
                    <v-btn v-if="a.status==='ACTIVE'" v-bind="attrs" v-on="on" small icon color="red" @click="deactivateAdmin(a)">
                      <v-icon small>mdi-cancel</v-icon>
                    </v-btn>
                    <v-btn v-else v-bind="attrs" v-on="on" small icon color="green" @click="reactivateAdmin(a)">
                      <v-icon small>mdi-check-circle</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ a.status==='ACTIVE' ? 'Deactivate' : 'Reactivate' }}</span>
                </v-tooltip>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- Create Admin Dialog -->
      <v-dialog v-model="showCreateDialog" max-width="520">
        <v-card>
          <v-card-title class="tw-font-semibold tw-text-slate-800">Create System Admin</v-card-title>
          <v-card-text class="tw-space-y-4">
            <v-text-field v-model="createForm.fullName" label="Full Name" dense outlined hide-details></v-text-field>
            <v-text-field v-model="createForm.email" label="Email" dense outlined hide-details></v-text-field>
            <v-text-field v-model="createForm.phoneNumber" label="Phone" dense outlined hide-details></v-text-field>
            <v-alert v-if="createTempPassword" type="info" dense outlined>Temp password will be: <strong>{{ createTempPassword }}</strong></v-alert>
          </v-card-text>
          <v-card-actions class="tw-justify-end tw-gap-2">
            <v-btn text small @click="showCreateDialog=false">Cancel</v-btn>
            <v-btn color="primary" small :loading="creating" @click="submitCreate">Create</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Edit Admin Dialog -->
      <v-dialog v-model="showEditDialog" max-width="520">
        <v-card>
          <v-card-title class="tw-font-semibold tw-text-slate-800">Edit System Admin</v-card-title>
          <v-card-text class="tw-space-y-4">
            <v-text-field v-model="editForm.fullName" label="Full Name" dense outlined hide-details></v-text-field>
            <v-text-field v-model="editForm.email" label="Email" dense outlined hide-details></v-text-field>
            <v-text-field v-model="editForm.phoneNumber" label="Phone" dense outlined hide-details></v-text-field>
          </v-card-text>
          <v-card-actions class="tw-justify-end tw-gap-2">
            <v-btn text small @click="showEditDialog=false">Close</v-btn>
            <v-btn color="primary" small :loading="saving" @click="submitEdit">Save</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Snackbar -->
      <v-snackbar v-model="snackbar.show" :timeout="4500" top right :color="snackbarColor(snackbar.color)" elevation="6">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-icon small v-if="snackbar.color==='success'">mdi-check-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='error'">mdi-alert-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='warning'">mdi-alert</v-icon>
          <v-icon small v-else>mdi-information</v-icon>
          <span class="tw-font-medium tw-text-sm">{{ snackbar.message }}</span>
          <v-spacer></v-spacer>
          <v-btn icon small text @click="snackbar.show=false"><v-icon small>mdi-close</v-icon></v-btn>
        </div>
      </v-snackbar>
    </div>
  </Default>

</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'SystemAdminsManagement',
  components: { Default },
  data() {
    return {
      admins: [],
      loading: false,
      search: '',
      searchTimer: null,
      showCreateDialog: false,
      createForm: { fullName: '', email: '', phoneNumber: '' },
      createTempPassword: '12345',
      creating: false,
      showEditDialog: false,
      editingId: null,
      editForm: { fullName: '', email: '', phoneNumber: '' },
      saving: false,
      snackbar: { show: false, message: '', color: 'info' },
    };
  },
  async mounted() {
    this.refreshAdmins();
  },
  methods: {
    async refreshAdmins() {
      this.loading = true;
      try {
        const resp = await axios.get('/api/admin-service/system-admins');
        this.admins = resp.data?.data || [];
      } catch (e) { this.showSnackbar('Failed to load system admins', 'error'); } finally { this.loading = false; }
    },
    onSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(this.executeSearch, 350);
    },
    async executeSearch() {
      const q = (this.search || '').trim();
      if (!q) { this.refreshAdmins(); return; }
      if (q.length < 2) return; // avoid chatty network
      this.loading = true;
      try {
        const resp = await axios.get('/api/admin-service/system-admins/search', { params: { q } });
        this.admins = resp.data?.data || [];
      } catch (e) { this.showSnackbar('Search failed', 'error'); } finally { this.loading = false; }
    },
    clearSearch() { this.search = ''; this.executeSearch(); },
    openCreateDialog() { this.showCreateDialog = true; },
    async submitCreate() {
      if (!this.createForm.fullName || !this.createForm.email) { this.showSnackbar('Name & email required', 'warning'); return; }
      this.creating = true;
      try {
        const resp = await axios.post('/api/admin-service/system-admins', this.createForm);
        const admin = resp.data?.data;
        this.showSnackbar('System Admin created', 'success');
        this.showCreateDialog = false; this.createForm = { fullName: '', email: '', phoneNumber: '' };
        // If backend returns tempPassword, show it
        if (admin && admin.tempPassword) this.showSnackbar(`Temp password: ${admin.tempPassword}`, 'info');
        this.refreshAdmins();
      } catch (e) { this.showSnackbar(e?.response?.data?.message || 'Create failed', 'error'); } finally { this.creating = false; }
    },
    openEditDialog(a) {
      this.editingId = a.id;
      this.editForm = { fullName: a.fullName || '', email: a.email || '', phoneNumber: a.phoneNumber || '' };
      this.showEditDialog = true;
    },
    async submitEdit() {
      if (!this.editingId) return; this.saving = true;
      try {
        await axios.put(`/api/admin-service/system-admins/${this.editingId}`, this.editForm);
        this.showSnackbar('System Admin updated', 'success');
        this.showEditDialog = false; this.editingId = null;
        this.refreshAdmins();
      } catch (e) { this.showSnackbar(e?.response?.data?.message || 'Update failed', 'error'); } finally { this.saving = false; }
    },
    async deactivateAdmin(a) {
      if (!a?.id) return;
      try {
        await axios.delete(`/api/admin-service/system-admins/${a.id}`);
        this.showSnackbar('System Admin deactivated', 'success');
        this.refreshAdmins();
      } catch (e) { this.showSnackbar('Deactivation failed', 'error'); }
    },
    async reactivateAdmin(a) {
      if (!a?.id) return;
      try {
        await axios.post(`/api/admin-service/system-admins/${a.id}/reactivate`);
        this.showSnackbar('System Admin reactivated', 'success');
        this.refreshAdmins();
      } catch (e) { this.showSnackbar('Reactivation failed', 'error'); }
    },
    exportCsv() {
      const rows = [[
        'ID', 'UserID', 'Name', 'Email', 'Phone', 'Status', 'CreatedAt', 'UpdatedAt', 'TempPassword',
      ]];
      this.admins.forEach((a) => rows.push([
        a.id, a.userId, a.fullName, a.email || '', a.phoneNumber || '', a.status, a.createdAt || '', a.updatedAt || '', a.tempPassword || '',
      ]));
      const csv = rows.map((r) => r.map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = 'system-admins.csv'; a.click(); URL.revokeObjectURL(url);
      this.showSnackbar('Export complete', 'success');
    },
    showSnackbar(message, color = 'info') { this.snackbar = { show: true, message, color }; },
    snackbarColor(kind) {
      switch (kind) {
        case 'success': return 'green lighten-1';
        case 'error': return 'red lighten-1';
        case 'warning': return 'amber darken-2';
        case 'info': return 'blue lighten-1';
        default: return 'primary';
      }
    },
  },
};
</script>

<style scoped>
</style>
