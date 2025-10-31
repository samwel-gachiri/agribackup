<template>
  <v-menu offset-y transition="scale-transition" :close-on-content-click="false">
    <template v-slot:activator="{ on, attrs }">
      <v-badge
          color="error"
          :content="unreadCount"
          :value="unreadCount > 0"
          overlap
      >
        <v-btn
            icon
            v-bind="attrs"
            v-on="on"
            class="notification-bell"
        >
          <v-icon
              size="24"
              :class="{ 'pulse-animation': unreadCount > 0 }"
          >
            mdi-bell-outline
          </v-icon>
        </v-btn>
      </v-badge>
    </template>

    <v-card class="notification-card" width="350">
      <v-toolbar color="primary" dark flat>
        <v-toolbar-title>Notifications</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="markAllAsRead" v-if="unreadCount > 0">
          <v-icon>mdi-check-all</v-icon>
        </v-btn>
      </v-toolbar>

      <v-list three-line class="py-0">
        <template v-if="notifications.length > 0">
          <v-list-item
              v-for="(item, index) in notifications"
              :key="index"
              :class="{ 'unread-notification': !item.read }"
              @click="handleNotificationClick(item)"
          >
            <v-list-item-avatar>
              <v-icon
                  :color="getNotificationColor(item.type)"
                  class="notification-icon"
              >
                {{ getNotificationIcon(item.type) }}
              </v-icon>
            </v-list-item-avatar>

            <v-list-item-content>
              <v-list-item-title>{{ item.title }}</v-list-item-title>
              <v-list-item-subtitle class="text--primary">
                {{ item.message }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>
                <time-ago :datetime="item.timestamp" />
              </v-list-item-subtitle>
            </v-list-item-content>

            <v-list-item-action>
              <v-icon v-if="!item.read" color="primary">mdi-circle-small</v-icon>
            </v-list-item-action>
          </v-list-item>
        </template>

        <v-list-item v-else>
          <v-list-item-content>
            <v-list-item-title class="text-center">
              No new notifications
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>

      <v-divider></v-divider>

      <v-card-actions>
        <v-btn text color="primary" block @click="viewAllNotifications">
          View All
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script>
export default {
  name: 'NotificationBell',
  data() {
    return {
      notifications: [
        // {
        //   id: 1,
        //   title: 'Irrigation Complete',
        //   message: 'Field A irrigation cycle has finished',
        //   type: 'irrigation',
        //   read: true,
        //   timestamp: new Date(Date.now() - 1000 * 60 * 5),
        // },
        // {
        //   id: 2,
        //   title: 'Soil Alert',
        //   message: 'Low moisture detected in Field B',
        //   type: 'alert',
        //   read: true,
        //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
        // },
        // {
        //   id: 3,
        //   title: 'Harvest Ready',
        //   message: 'Wheat in Field C ready for harvest',
        //   type: 'harvest',
        //   read: true,
        //   timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
        // },
      ],
    };
  },
  computed: {
    unreadCount() {
      return this.notifications.filter((n) => !n.read).length;
    },
  },
  methods: {
    getNotificationIcon(type) {
      const icons = {
        irrigation: 'mdi-water',
        alert: 'mdi-alert',
        harvest: 'mdi-sprout',
        weather: 'mdi-weather-cloudy',
        system: 'mdi-cog',
      };
      return icons[type] || 'mdi-bell';
    },
    getNotificationColor(type) {
      const colors = {
        irrigation: 'blue',
        alert: 'red',
        harvest: 'green',
        weather: 'orange',
        system: 'purple',
      };
      return colors[type] || 'primary';
    },
    handleNotificationClick(notification) {
      if (!notification.read) {
        // eslint-disable-next-line no-param-reassign
        notification.read = true;
      }
      // Handle navigation based on notification type
      this.$emit('notification-click', notification);
    },
    markAllAsRead() {
      // eslint-disable-next-line no-return-assign,no-param-reassign
      this.notifications.forEach((n) => (n.read = true));
    },
    viewAllNotifications() {
      this.$router.push('/notifications');
    },
  },
};
</script>

<style scoped>
.notification-bell {
  position: relative;
  transition: all 0.3s ease;
}

.notification-bell:hover {
  transform: scale(1.1);
}

.notification-card {
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1) !important;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-icon {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  padding: 8px;
}

.unread-notification {
  background: rgba(100, 180, 255, 0.05);
  border-left: 3px solid var(--v-primary-base);
}

.pulse-animation {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}
</style>
