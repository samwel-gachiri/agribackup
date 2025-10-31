<template>
  <div class="loading-states">
    <!-- Skeleton Loaders -->
    <div v-if="type === 'skeleton'" class="skeleton-container">
      <!-- Card Skeleton -->
      <div v-if="variant === 'card'" class="tw-space-y-4">
        <v-skeleton-loader
          v-for="n in count"
          :key="n"
          type="card"
          class="tw-rounded-xl tw-shadow-md"
        />
      </div>

      <!-- List Skeleton -->
      <div v-else-if="variant === 'list'" class="tw-space-y-3">
        <v-skeleton-loader
          v-for="n in count"
          :key="n"
          type="list-item-avatar-two-line"
          class="tw-rounded-lg"
        />
      </div>

      <!-- Table Skeleton -->
      <div v-else-if="variant === 'table'">
        <v-skeleton-loader
          type="table-heading, table-row@5"
          class="tw-rounded-xl tw-shadow-md"
        />
      </div>

      <!-- Dashboard Skeleton -->
      <div v-else-if="variant === 'dashboard'" class="tw-space-y-6">
        <!-- Header -->
        <v-skeleton-loader type="heading" class="tw-mb-4" />

        <!-- Metrics Cards -->
        <div class="tw-grid tw-grid-cols-1 sm:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-4 tw-mb-6">
          <v-skeleton-loader
            v-for="n in 4"
            :key="n"
            type="card-heading"
            class="tw-rounded-xl tw-shadow-md tw-h-24"
          />
        </div>

        <!-- Main Content -->
        <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-3 tw-gap-6">
          <div class="lg:tw-col-span-2">
            <v-skeleton-loader type="image" class="tw-rounded-xl tw-shadow-md tw-h-96" />
          </div>
          <div class="tw-space-y-4">
            <v-skeleton-loader type="card" class="tw-rounded-xl tw-shadow-md" />
            <v-skeleton-loader type="list-item@3" class="tw-rounded-xl tw-shadow-md" />
          </div>
        </div>
      </div>
    </div>

    <!-- Spinner Loaders -->
    <div v-else-if="type === 'spinner'" class="spinner-container tw-flex tw-flex-col tw-items-center tw-justify-center tw-py-12">
      <!-- Circular Progress -->
      <div v-if="variant === 'circular'" class="tw-text-center">
        <v-progress-circular
          :size="size"
          :width="width"
          :color="color"
          indeterminate
          class="tw-mb-4"
        />
        <p v-if="message" class="tw-text-gray-600 tw-text-sm">{{ message }}</p>
      </div>

      <!-- Dots Loader -->
      <div v-else-if="variant === 'dots'" class="dots-loader">
        <div class="dot" :style="{ backgroundColor: color }"></div>
        <div class="dot" :style="{ backgroundColor: color }"></div>
        <div class="dot" :style="{ backgroundColor: color }"></div>
        <p v-if="message" class="tw-text-gray-600 tw-text-sm tw-mt-4">{{ message }}</p>
      </div>
    </div>

    <!-- Empty States -->
    <div v-else-if="type === 'empty'" class="empty-state tw-text-center tw-py-12">
      <div class="tw-mb-6">
        <v-icon :size="iconSize" :color="iconColor" class="tw-mb-4">
          {{ icon }}
        </v-icon>
        <h3 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-2">
          {{ title }}
        </h3>
        <p class="tw-text-gray-600 tw-max-w-md tw-mx-auto">
          {{ message }}
        </p>
      </div>

      <div v-if="actions.length > 0" class="tw-space-x-3">
        <v-btn
          v-for="action in actions"
          :key="action.key"
          :color="action.color || 'primary'"
          :variant="action.variant || 'outlined'"
          @click="$emit('action', action.key)"
        >
          <v-icon v-if="action.icon" left>{{ action.icon }}</v-icon>
          {{ action.label }}
        </v-btn>
      </div>
    </div>
  </div>
</template><s
cript>
export default {
  name: 'LoadingStates',
  props: {
    type: {
      type: String,
      required: true,
      validator: value => ['skeleton', 'spinner', 'empty', 'error', 'success'].includes(value)
    },
    variant: {
      type: String,
      default: 'card'
    },
    count: {
      type: Number,
      default: 3
    },
    size: {
      type: [String, Number],
      default: 64
    },
    width: {
      type: Number,
      default: 4
    },
    height: {
      type: Number,
      default: 4
    },
    color: {
      type: String,
      default: '#3b82f6'
    },
    message: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: 'mdi-database-off'
    },
    iconSize: {
      type: [String, Number],
      default: 64
    },
    iconColor: {
      type: String,
      default: 'grey-lighten-1'
    },
    actions: {
      type: Array,
      default: () => []
    },
    showSupport: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
/* Dots Loader Animation */
.dots-loader {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: dots-bounce 1.4s ease-in-out infinite both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
.dot:nth-child(3) { animation-delay: 0s; }

@keyframes dots-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Success Icon Animation */
.success-icon {
  animation: success-pulse 0.6s ease-in-out;
}

@keyframes success-pulse {
  0% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* Skeleton Loading Animation */
:deep(.v-skeleton-loader__bone) {
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    opacity: 1;
  }
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .tw-space-x-3 > * + * {
    margin-left: 0;
    margin-top: 0.75rem;
  }

  .tw-space-x-3 {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
  }
}

/* Smooth transitions */
.loading-states {
  transition: all 0.3s ease-in-out;
}

/* Custom skeleton colors */
:deep(.v-skeleton-loader__bone) {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s infinite;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>
