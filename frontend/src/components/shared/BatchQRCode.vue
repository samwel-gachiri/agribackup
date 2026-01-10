<template>
  <v-dialog v-model="dialog" max-width="500">
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        color="primary"
        v-bind="attrs"
        v-on="on"
        :small="small"
        :outlined="outlined"
      >
        <v-icon left small>mdi-qrcode</v-icon>
        {{ buttonText }}
      </v-btn>
    </template>

    <v-card>
      <v-card-title class="headline">
        <v-icon left color="primary">mdi-qrcode</v-icon>
        Batch Verification QR Code
      </v-card-title>

      <v-card-text class="text-center py-6">
        <div class="mb-4">
          <v-chip color="primary" outlined>
            <v-icon left small>mdi-barcode</v-icon>
            {{ batchCode }}
          </v-chip>
        </div>

        <!-- QR Code Container -->
        <div class="qr-code-container" ref="qrCodeContainer">
          <div ref="qrcode" class="qr-code"></div>
        </div>

        <!-- Verification Link -->
        <div class="mt-4">
          <v-text-field
            :value="verificationUrl"
            readonly
            outlined
            dense
            label="Verification Link"
            append-icon="mdi-content-copy"
            @click:append="copyLink"
          ></v-text-field>
        </div>

        <!-- Instructions -->
        <v-alert type="info" text dense class="mt-4">
          <small>
            Scan this QR code or share the link to verify product authenticity
          </small>
        </v-alert>
      </v-card-text>

      <v-card-actions>
        <v-btn text @click="dialog = false">Close</v-btn>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="downloadQRCode">
          <v-icon left small>mdi-download</v-icon>
          Download PNG
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import QRCode from 'qrcodejs2';

export default {
  name: 'BatchQRCode',
  props: {
    batchCode: {
      type: String,
      required: true,
    },
    buttonText: {
      type: String,
      default: 'Generate QR Code',
    },
    small: {
      type: Boolean,
      default: false,
    },
    outlined: {
      type: Boolean,
      default: false,
    },
    size: {
      type: Number,
      default: 256,
    },
  },
  data() {
    return {
      dialog: false,
      qrCodeInstance: null,
    };
  },
  computed: {
    verificationUrl() {
      // Use production URL or current origin
      const baseUrl = process.env.VUE_APP_FRONTEND_URL || window.location.origin;
      return `${baseUrl}/verify/${this.batchCode}`;
    },
  },
  watch: {
    dialog(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.generateQRCode();
        });
      }
    },
  },
  methods: {
    generateQRCode() {
      // Clear existing QR code
      if (this.$refs.qrcode) {
        this.$refs.qrcode.innerHTML = '';
      }

      // Generate new QR code
      this.qrCodeInstance = new QRCode(this.$refs.qrcode, {
        text: this.verificationUrl,
        width: this.size,
        height: this.size,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H,
      });
    },

    async copyLink() {
      try {
        await navigator.clipboard.writeText(this.verificationUrl);
        this.$toast.success('Verification link copied to clipboard!');
      } catch (error) {
        this.$toast.error('Failed to copy link');
      }
    },

    downloadQRCode() {
      try {
        const canvas = this.$refs.qrcode.querySelector('canvas');
        if (!canvas) {
          this.$toast.error('QR code not generated yet');
          return;
        }

        // Convert canvas to blob
        canvas.toBlob((blob) => {
          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `batch-${this.batchCode}-qr-code.png`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(url);

          this.$toast.success('QR code downloaded successfully!');
        });
      } catch (error) {
        this.$toast.error('Failed to download QR code');
      }
    },
  },
};
</script>

<style scoped>
.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.qr-code {
  display: inline-block;
}

.qr-code >>> canvas {
  border-radius: 4px;
}
</style>
