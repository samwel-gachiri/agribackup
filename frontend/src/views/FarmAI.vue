<template>
  <Default>
  <div class="tw-min-h-screen tw-bg-gradient-to-br tw-from-green-100 tw-via-white tw-to-green-200 tw-pb-12 tw-relative tw-flex tw-flex-col tw-items-center tw-justify-start tw-pt-24 md:tw-pt-28">
      <!-- Glassmorphic Card -->
      <div class="tw-absolute tw-inset-0 tw-z-0">
        <div class="tw-absolute tw-inset-0 tw-bg-gradient-to-br tw-from-green-200/60 tw-via-white/40 tw-to-green-300/60 tw-blur-2xl"></div>
      </div>
      <div class="tw-relative tw-z-10 tw-w-full tw-max-w-2xl tw-mx-auto">
        <div class="tw-flex tw-items-center tw-space-x-4 tw-mb-6 tw-justify-center">
          <div class="tw-bg-white/60 tw-backdrop-blur-lg tw-rounded-full tw-shadow-lg tw-p-2">
            <v-icon class="tw-text-green-600" size="40px">mdi-robot-excited-outline</v-icon>
          </div>
          <div>
            <div class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-drop-shadow">FarmAI Assistant</div>
            <div class="tw-text-base tw-text-gray-500 tw-mt-1">
              {{ roleWelcomeText }}
            </div>
          </div>
        </div>
        <!-- Quick Actions -->
        <div class="tw-flex tw-flex-wrap tw-gap-2 tw-justify-center tw-mb-4">
          <v-chip
            v-for="action in filteredQuickActions"
            :key="action.label"
            class="tw-bg-white/70 tw-backdrop-blur tw-shadow tw-text-green-700 tw-font-semibold hover:tw-bg-green-100 tw-cursor-pointer"
            @click="handleQuickAction(action)"
            outlined
            pill
          >
            <v-icon left small>{{ action.icon }}</v-icon>
            {{ action.label }}
          </v-chip>
        </div>
        <!-- Chat Card -->
        <div class="tw-bg-white/70 tw-backdrop-blur-lg tw-rounded-2xl tw-shadow-xl tw-p-0 tw-flex tw-flex-col tw-h-[520px] tw-overflow-hidden">
          <div id="chat-messages" class="tw-flex-1 tw-p-5 tw-overflow-y-auto tw-space-y-4 tw-text-base tw-text-gray-800 tw-bg-transparent">
            <!-- Messages will appear here -->
          </div>
          <div class="tw-border-t tw-border-green-100 tw-p-4 tw-bg-white/80">
            <div class="tw-flex tw-items-end tw-space-x-2">
              <v-textarea
                v-model="aiQuestion"
                outlined
                rounded
                hide-details
                placeholder="Ask anything about farming, or type a command..."
                rows="1"
                auto-grow
                class="tw-flex-1 tw-bg-white/60 tw-rounded-lg tw-border-none focus:tw-ring-2 focus:tw-ring-green-400"
                style="min-height:40px;"
                @keyup.enter="askAI"
              ></v-textarea>
              <v-btn
                color="success"
                class="tw-rounded-full tw-shadow-md"
                :loading="loading"
                :disabled="!aiQuestion.trim()"
                @click="askAI"
                elevation="2"
                min-width="48"
                height="48"
              >
                <v-icon size="26px">mdi-send</v-icon>
              </v-btn>
            </div>
          </div>
        </div>
        <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000" class="tw-rounded-lg">
          {{ snackbarText }}
          <template v-slot:action="{ attrs }">
            <v-btn text v-bind="attrs" @click="snackbar = false" class="tw-text-green-700">Close</v-btn>
          </template>
        </v-snackbar>
      </div>
    </div>
  </Default>
</template>

<script>
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getCurrentUserRole } from '@/utils/roles.js';
import Default from '@/components/layout/Default.vue';

export default {
  components: { Default },
  data() {
    return {
      loading: false,
      aiQuestion: '',
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
      md: new MarkdownIt({
        html: true,
        xhtmlOut: true,
        breaks: true,
        linkify: true,
        typographer: true,
        strict: true,
      }),
      geminiApiKey: process.env.VUE_APP_GOOGLE_API_KEY,
      gemini: null,
      quickActions: [
        // Farmer
        {
          label: 'Add Produce',
          icon: 'mdi-plus-box',
          type: 'navigate',
          route: { name: 'AddProduce' },
          roles: ['farmer'],
        },
        {
          label: 'My Listings',
          icon: 'mdi-format-list-bulleted',
          type: 'navigate',
          route: { name: 'MyProduces' },
          roles: ['farmer'],
        },
        // Buyer
        {
          label: 'Browse Listings',
          icon: 'mdi-magnify',
          type: 'navigate',
          route: { name: 'BrowseListings' },
          roles: ['buyer'],
        },
        {
          label: 'My Orders',
          icon: 'mdi-cart',
          type: 'navigate',
          route: { name: 'Orders' },
          roles: ['buyer'],
        },
        // Shared
        {
          label: 'Market Prices',
          icon: 'mdi-currency-usd',
          type: 'navigate',
          route: { name: 'MarketPrices' },
          roles: ['farmer', 'buyer'],
        },
        {
          label: 'Community',
          icon: 'mdi-account-group',
          type: 'navigate',
          route: { name: 'Community' },
          roles: ['farmer', 'buyer', 'admin', 'system_admin', 'zone_supervisor'],
        },
        {
          label: 'Farm Advice',
          icon: 'mdi-leaf',
          type: 'advice',
          question: 'Give me farm advice',
          roles: ['farmer', 'buyer', 'admin', 'system_admin', 'zone_supervisor'],
        },
        {
          label: 'Help',
          icon: 'mdi-help-circle',
          type: 'advice',
          question: 'What can you do?',
          roles: ['farmer', 'buyer', 'admin', 'system_admin', 'zone_supervisor'],
        },
      ],
      computed: {
        userRole() {
          // Use store or utility to get current user role, fallback to 'guest'
          let role = (this.getCurrentUserRole && this.getCurrentUserRole()) || '';
          if (!role) role = 'guest';
          return role.toLowerCase();
        },
        filteredQuickActions() {
          // Only show actions for the current role, and only those with valid routes
          return this.quickActions
            .filter((a) => {
              if (!a.roles || a.roles.length === 0) return true;
              return a.roles.includes(this.userRole) || a.roles.includes('anybody');
            })
            .filter((a) => a.type !== 'navigate' || (a.route && a.route.name));
        },
        roleWelcomeText() {
          switch (this.userRole) {
            case 'farmer':
              return 'Welcome, Farmer! Get advice, manage your produce, and more.';
            case 'buyer':
              return 'Welcome, Buyer! Ask for market info, browse listings, or get help.';
            case 'admin':
            case 'system_admin':
              return 'Welcome, Admin! Use FarmAI for insights, navigation, and support.';
            case 'zone_supervisor':
              return 'Welcome, Zone Supervisor! Optimize routes, manage pickups, and more.';
            default:
              return 'Your smart farming companion.';
          }
        },
      },
    };
  },
  mounted() {
    this.gemini = new GoogleGenerativeAI(this.geminiApiKey);
    this.addMessage('FarmAI', '👋 Welcome! I am your personal farm assistant. Ask me anything about farming, or use the quick actions below. I can also guide you to any part of the app.');
  },
  methods: {
    getCurrentUserRole,
    async askAI() {
      try {
        if (!this.aiQuestion.trim()) {
          this.showSnackbar('Please enter a question', 'error');
          return;
        }
        this.addMessage('You', this.aiQuestion);
        this.loading = true;
        const question = this.aiQuestion;
        this.aiQuestion = '';

        // Create bot message element first
        const botMessageElement = this.addMessage('FarmAI', '...');
        const contentElement = botMessageElement.querySelector('.message-content');

        try {
          const model = this.gemini.getGenerativeModel({ model: 'gemini-2.5-flash' });
          const result = await model.generateContent(question);
          const response = await result.response;
          const text = response.text();

          // Check for navigation intent
          const navMatch = text.match(/go to ([a-zA-Z ]+)/i);
          if (navMatch) {
            const page = navMatch[1].trim();
            const route = this.findRouteByPageName(page);
            if (route) {
              // eslint-disable-next-line no-useless-escape
              contentElement.innerHTML = DOMPurify.sanitize(this.md.render(`${text}\n\n<button class='tw-mt-3 tw-bg-green-500 tw-text-white tw-px-4 tw-py-2 tw-rounded-lg tw-shadow hover:tw-bg-green-600' onclick=\"window.__farmai_navigate('${route.name}')\">Go to ${route.name.replace(/([A-Z])/g, ' $1').trim()}</button>`));
              // eslint-disable-next-line no-underscore-dangle
              window.__farmai_navigate = (routeName) => {
                this.$router.push({ name: routeName });
              };
            } else {
              contentElement.innerHTML = DOMPurify.sanitize(this.md.render(text));
            }
          } else {
            contentElement.innerHTML = DOMPurify.sanitize(this.md.render(text));
          }
          this.showSnackbar('Answer provided', 'success');
        } catch (error) {
          contentElement.textContent = 'An error occurred. Please try again.';
          this.showSnackbar('Error getting answer', 'error');
        }
      } catch (e) {
        this.showSnackbar(e.message, 'error');
      } finally {
        this.loading = false;
      }
    },
    addMessage(sender, content) {
      const chatMessages = document.getElementById('chat-messages');
      const messageElement = document.createElement('div');
      const mdContent = DOMPurify.sanitize(this.md.render(content));
      messageElement.className = sender === 'You'
        ? 'tw-bg-green-50 tw-text-green-900 tw-p-3 tw-rounded-lg tw-self-end tw-max-w-[80%] tw-ml-auto'
        : 'tw-bg-white/90 tw-text-gray-800 tw-p-3 tw-rounded-lg tw-shadow-sm tw-self-start tw-max-w-[80%]';

      messageElement.innerHTML = `
        <div class="tw-text-xs tw-font-semibold tw-mb-1 ${sender === 'You' ? 'tw-text-green-700' : 'tw-text-gray-500'}">${sender}</div>
        <div class="message-content">${mdContent}</div>
      `;
      chatMessages.appendChild(messageElement);
      chatMessages.scrollTop = chatMessages.scrollHeight;
      return messageElement;
    },
    showSnackbar(text, color) {
      this.snackbarText = text;
      this.snackbarColor = color;
      this.snackbar = true;
    },
    handleQuickAction(action) {
      if (action.type === 'navigate') {
        this.$router.push(action.route);
      } else if (action.type === 'advice') {
        this.aiQuestion = action.question;
        this.askAI();
      }
    },
    findRouteByPageName(page) {
      // Map page names to route names (customize as needed)
      const map = {
        'add produce': { name: 'AddProduce' },
        'my listings': { name: 'MyProduces' },
        'market prices': { name: 'MarketPrices' },
        community: { name: 'Community' },
        dashboard: { name: 'Dashboard' },
        orders: { name: 'Orders' },
        profile: { name: 'Profile' },
      };
      const key = page.toLowerCase().trim();
      return map[key] || null;
    },
  },
};
</script>

#chat-messages {
  min-height: 180px;
  scrollbar-width: thin;
  scrollbar-color: #bbf7d0 #f0fdf4;
}
#chat-messages::-webkit-scrollbar {
  width: 6px;
}
#chat-messages::-webkit-scrollbar-thumb {
  background: #bbf7d0;
  border-radius: 6px;
}
#chat-messages::-webkit-scrollbar-track {
  background: #f0fdf4;
}

.tw-bg-white\/70 {
  background-color: rgba(255,255,255,0.7) !important;
}
.tw-bg-white\/80 {
  background-color: rgba(255,255,255,0.8) !important;
}
.tw-bg-white\/90 {
  background-color: rgba(255,255,255,0.9) !important;
}
.tw-blur-2xl {
  filter: blur(40px);
}
.tw-backdrop-blur-lg {
  backdrop-filter: blur(16px);
}
.tw-backdrop-blur {
  backdrop-filter: blur(8px);
}
.tw-shadow-xl {
  box-shadow: 0 8px 32px 0 rgba(16, 185, 129, 0.18), 0 1.5px 4px 0 rgba(0,0,0,0.04);
}
.tw-shadow-md {
  box-shadow: 0 2px 8px 0 rgba(16, 185, 129, 0.10), 0 1.5px 4px 0 rgba(0,0,0,0.04);
}
.tw-shadow {
  box-shadow: 0 1.5px 4px 0 rgba(16, 185, 129, 0.08);
}
.tw-drop-shadow {
  filter: drop-shadow(0 2px 8px rgba(16,185,129,0.10));
}
.tw-max-w-2xl {
  max-width: 42rem;
}
.tw-max-w-\[80%\] {
  max-width: 80%;
}
.tw-ml-auto {
  margin-left: auto;
}
