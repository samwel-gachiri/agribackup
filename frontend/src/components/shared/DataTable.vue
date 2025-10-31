<template>
  <div class="data-table tw-w-full">
    <!-- Table Header with Search and Actions -->
    <v-card class="tw-rounded-xl tw-shadow-md tw-mb-4" v-if="showHeader">
      <v-card-title class="tw-flex tw-flex-col sm:tw-flex-row tw-justify-between tw-items-start sm:tw-items-center tw-gap-4">
        <div class="tw-flex tw-items-center tw-gap-3">
          <v-icon v-if="icon" :color="iconColor">{{ icon }}</v-icon>
          <h3 class="tw-text-lg tw-font-semibold">{{ title }}</h3>
        </div>

        <div class="tw-flex tw-flex-col sm:tw-flex-row tw-gap-3 tw-w-full sm:tw-w-auto">
          <!-- Search -->
          <v-text-field
            v-if="searchable"
            v-model="searchQuery"
            placeholder="Search..."
            density="compact"
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            class="tw-min-w-64"
            clearable
          />

          <!-- Actions -->
          <div class="tw-flex tw-gap-2">
            <v-btn
              v-for="action in actions"
              :key="action.key"
              :color="action.color || 'primary'"
              :variant="action.variant || 'outlined'"
              :size="action.size || 'small'"
              @click="$emit('action', action.key)"
              :disabled="action.disabled"
            >
              <v-icon v-if="action.icon" left>{{ action.icon }}</v-icon>
              {{ action.label }}
            </v-btn>
          </div>
        </div>
      </v-card-title>
    </v-card>

    <!-- Data Table -->
    <v-card class="tw-rounded-xl tw-shadow-md tw-overflow-hidden">
      <v-data-table
        :headers="computedHeaders"
        :items="filteredItems"
        :loading="loading"
        :items-per-page="itemsPerPage"
        :sort-by="sortBy"
        :multi-sort="multiSort"
        class="elevation-0"
        :mobile-breakpoint="0"
      >
        <!-- Loading -->
        <template v-slot:loading>
          <v-skeleton-loader type="table-row@10" />
        </template>

        <!-- No Data -->
        <template v-slot:no-data>
          <div class="tw-text-center tw-py-8">
            <v-icon size="64" color="grey-lighten-1">{{ noDataIcon }}</v-icon>
            <p class="tw-text-gray-500 tw-mt-4">{{ noDataText }}</p>
            <v-btn
              v-if="showRefreshOnEmpty"
              color="primary"
              variant="outlined"
              @click="$emit('refresh')"
              class="tw-mt-4"
            >
              <v-icon left>mdi-refresh</v-icon>
              Refresh
            </v-btn>
          </div>
        </template>

        <!-- Custom column rendering -->
        <template v-for="header in headers" v-slot:[`item.${header.key}`]="{ item }">
          <span :key="header.key" class="tw-inline-block tw-w-full">
            <slot :name="`item.${header.key}`" :item="item" :value="item[header.key]">
              <!-- Default rendering based on column type -->
              <div v-if="header.type === 'avatar'" class="tw-flex tw-items-center tw-gap-3">
                <v-avatar :size="header.avatarSize || 32" :color="getAvatarColor(item[header.key])">
                  <v-img v-if="item[header.avatarSrc]" :src="item[header.avatarSrc]" />
                  <span v-else class="tw-text-white tw-font-medium">
                    {{ getInitials(item[header.key]) }}
                  </span>
                </v-avatar>
                <span class="tw-font-medium">{{ item[header.key] }}</span>
              </div>

              <v-chip
                v-else-if="header.type === 'status'"
                :color="getStatusColor(item[header.key], header.statusColors)"
                size="small"
                variant="flat"
              >
                {{ item[header.key] }}
              </v-chip>

              <div v-else-if="header.type === 'currency'" class="tw-font-mono">
                {{ formatCurrency(item[header.key], header.currency) }}
              </div>

              <div v-else-if="header.type === 'date'" class="tw-text-sm">
                {{ formatDate(item[header.key], header.dateFormat) }}
              </div>

              <div v-else-if="header.type === 'progress'" class="tw-w-full">
                <v-progress-linear
                  :model-value="item[header.key]"
                  :color="getProgressColor(item[header.key])"
                  height="8"
                  rounded
                />
                <span class="tw-text-xs tw-text-gray-600 tw-mt-1">{{ item[header.key] }}%</span>
              </div>

              <div v-else-if="header.type === 'actions'" class="tw-flex tw-gap-1">
                <v-btn
                  v-for="action in header.actions"
                  :key="action.key"
                  :color="action.color || 'primary'"
                  :variant="action.variant || 'text'"
                  :size="action.size || 'small'"
                  @click="$emit('row-action', { action: action.key, item })"
                  :disabled="action.disabled && action.disabled(item)"
                >
                  <v-icon>{{ action.icon }}</v-icon>
                </v-btn>
              </div>

              <span v-else>{{ item[header.key] }}</span>
            </slot>
          </span>
        </template>

        <!-- Expandable rows -->
        <template v-if="expandable" v-slot:expanded-row="{ item }">
          <td :colspan="headers.length" class="tw-p-0">
            <v-card flat class="tw-m-4 tw-rounded-lg">
              <v-card-text>
                <slot name="expanded-row" :item="item">
                  <pre class="tw-text-sm tw-bg-gray-50 tw-p-4 tw-rounded">{{ JSON.stringify(item, null, 2) }}</pre>
                </slot>
              </v-card-text>
            </v-card>
          </td>
        </template>

        <!-- Mobile view -->
        <template v-slot:item="{ item }" v-if="$vuetify.display.mobile">
          <tr>
            <td :colspan="headers.length" class="tw-p-0">
              <v-card flat class="tw-m-2 tw-rounded-lg tw-border">
                <v-card-text class="tw-p-4">
                  <div class="tw-space-y-3">
                    <div v-for="header in mobileHeaders" :key="header.key" class="tw-flex tw-justify-between tw-items-center">
                      <span class="tw-text-sm tw-font-medium tw-text-gray-600">{{ header.title }}</span>
                      <slot :name="`item.${header.key}`" :item="item" :value="item[header.key]">
                        <span class="tw-text-sm">{{ item[header.key] }}</span>
                      </slot>
                    </div>
                  </div>

                  <!-- Mobile actions -->
                  <div v-if="mobileActions.length > 0" class="tw-flex tw-gap-2 tw-mt-4 tw-pt-3 tw-border-t">
                    <v-btn
                      v-for="action in mobileActions"
                      :key="action.key"
                      :color="action.color || 'primary'"
                      :variant="action.variant || 'outlined'"
                      size="small"
                      @click="$emit('row-action', { action: action.key, item })"
                    >
                      <v-icon left>{{ action.icon }}</v-icon>
                      {{ action.label }}
                    </v-btn>
                  </div>
                </v-card-text>
              </v-card>
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
<script>
export default {
  name: 'DataTable',
  props: {
    title: {
      type: String,
      default: '',
    },
    icon: {
      type: String,
      default: null,
    },
    iconColor: {
      type: String,
      default: 'primary',
    },
    headers: {
      type: Array,
      required: true,
    },
    items: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
    searchable: {
      type: Boolean,
      default: true,
    },
    showHeader: {
      type: Boolean,
      default: true,
    },
    actions: {
      type: Array,
      default: () => [],
    },
    itemsPerPage: {
      type: Number,
      default: 10,
    },
    sortBy: {
      type: Array,
      default: () => [],
    },
    multiSort: {
      type: Boolean,
      default: false,
    },
    expandable: {
      type: Boolean,
      default: false,
    },
    noDataText: {
      type: String,
      default: 'No data available',
    },
    noDataIcon: {
      type: String,
      default: 'mdi-database-off',
    },
    showRefreshOnEmpty: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      searchQuery: '',
    };
  },
  computed: {
    computedHeaders() {
      return this.headers.map((header) => ({
        ...header,
        sortable: header.sortable !== false,
      }));
    },

    filteredItems() {
      if (!this.searchQuery) return this.items;

      const query = this.searchQuery.toLowerCase();
      return this.items.filter((item) => this.headers.some((header) => {
        const value = item[header.key];
        return value && value.toString().toLowerCase().includes(query);
      }));
    },

    mobileHeaders() {
      return this.headers.filter((header) => header.showOnMobile !== false);
    },

    mobileActions() {
      const actionsHeader = this.headers.find((header) => header.type === 'actions');
      return actionsHeader ? actionsHeader.actions || [] : [];
    },
  },
  methods: {
    getInitials(name) {
      if (!name) return '?';
      return name.split(' ').map((word) => word[0]).join('').toUpperCase()
        .slice(0, 2);
    },

    getAvatarColor(name) {
      if (!name) return 'grey';
      const colors = ['red', 'pink', 'purple', 'indigo', 'blue', 'cyan', 'teal', 'green', 'orange', 'brown'];
      const index = name.length % colors.length;
      return colors[index];
    },

    getStatusColor(status, statusColors = {}) {
      const defaultColors = {
        active: 'green',
        inactive: 'grey',
        pending: 'orange',
        completed: 'blue',
        cancelled: 'red',
        success: 'green',
        error: 'red',
        warning: 'orange',
        info: 'blue',
      };

      const colors = { ...defaultColors, ...statusColors };
      return colors[status?.toLowerCase()] || 'grey';
    },

    formatCurrency(value, currency = 'USD') {
      if (value == null) return '-';
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency,
      }).format(value);
    },

    formatDate(value, format = 'short') {
      if (!value) return '-';
      const date = new Date(value);

      switch (format) {
        case 'short':
          return date.toLocaleDateString();
        case 'long':
          return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
          });
        case 'datetime':
          return date.toLocaleString();
        case 'time':
          return date.toLocaleTimeString();
        default:
          return date.toLocaleDateString();
      }
    },

    getProgressColor(value) {
      if (value >= 80) return 'green';
      if (value >= 60) return 'orange';
      if (value >= 40) return 'yellow';
      return 'red';
    },
  },
};
</script>

<style scoped>
/* Custom table styling */
:deep(.v-data-table) {
  background: transparent;
}

:deep(.v-data-table__wrapper) {
  border-radius: 0;
}

:deep(.v-data-table-header) {
  background-color: #f8fafc;
}

:deep(.v-data-table-header th) {
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.v-data-table__td) {
  border-bottom: 1px solid #f3f4f6;
}

:deep(.v-data-table__tr:hover) {
  background-color: #f9fafb;
}

/* Mobile responsive adjustments */
@media (max-width: 768px) {
  :deep(.v-data-table) {
    display: none;
  }
}

/* Loading skeleton styling */
:deep(.v-skeleton-loader__table-row) {
  height: 48px;
  margin-bottom: 1px;
}

/* Smooth transitions */
.data-table {
  transition: all 0.3s ease;
}

/* Custom scrollbar for mobile cards */
:deep(.v-card-text) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

:deep(.v-card-text::-webkit-scrollbar) {
  width: 4px;
}

:deep(.v-card-text::-webkit-scrollbar-track) {
  background: #f7fafc;
}

:deep(.v-card-text::-webkit-scrollbar-thumb) {
  background-color: #cbd5e0;
  border-radius: 2px;
}
</style>
