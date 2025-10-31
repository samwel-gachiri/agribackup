<template>
  <div class="farmer-documents">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="blue" class="mr-3">mdi-file-document-multiple</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">My Documents</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Upload and manage your EUDR compliance documents
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="showUploadDialog = true"
              elevation="2"
            >
              <v-icon left>mdi-upload</v-icon>
              Upload Document
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Document Stats -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="success">mdi-check-circle</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ approvedDocs }}</div>
              <div class="text-caption">Approved</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="warning">mdi-clock-outline</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ pendingDocs }}</div>
              <div class="text-caption">Under Review</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="error">mdi-close-circle</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ rejectedDocs }}</div>
              <div class="text-caption">Rejected</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card" elevation="3">
            <v-card-text class="text-center">
              <v-icon large color="info">mdi-file-multiple</v-icon>
              <div class="text-h4 font-weight-bold mt-2">{{ totalDocs }}</div>
              <div class="text-caption">Total Documents</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Document Categories -->
      <v-row class="mb-4">
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-title>Required Document Categories</v-card-title>
            <v-card-text>
              <v-row>
                <v-col
                  cols="12" sm="6" md="4"
                  v-for="category in documentCategories"
                  :key="category.type"
                >
                  <div class="document-category">
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="d-flex align-center">
                        <v-icon :color="category.color" class="mr-2">{{ category.icon }}</v-icon>
                        <span class="font-weight-medium">{{ category.name }}</span>
                      </div>
                      <v-chip
                        :color="category.status === 'Complete' ? 'success' : 'warning'"
                        text-color="white"
                        x-small
                      >
                        {{ category.status }}
                      </v-chip>
                    </div>
                    <v-progress-linear
                      :value="category.progress"
                      :color="category.color"
                      height="6"
                      rounded
                    ></v-progress-linear>
                    <div class="text-caption text--secondary mt-1">
                      {{ category.uploaded }}/{{ category.required }} documents
                    </div>
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Documents List -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              My Documents
              <v-spacer></v-spacer>
              <v-select
                v-model="statusFilter"
                :items="statusOptions"
                label="Filter by Status"
                dense
                outlined
                hide-details
                class="filter-select mr-3"
              ></v-select>
              <v-text-field
                v-model="search"
                append-icon="mdi-magnify"
                label="Search documents..."
                single-line
                hide-details
                dense
                outlined
                class="search-field"
              ></v-text-field>
            </v-card-title>

            <v-data-table
              :headers="headers"
              :items="filteredDocuments"
              :search="search"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.documentType="{ item }">
                <div class="d-flex align-center">
                  <v-icon :color="getDocumentTypeColor(item.documentType)" class="mr-2">
                    {{ getDocumentTypeIcon(item.documentType) }}
                  </v-icon>
                  {{ formatDocumentType(item.documentType) }}
                </div>
              </template>

              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  text-color="white"
                  small
                >
                  {{ item.status }}
                </v-chip>
              </template>

              <template v-slot:item.fileSize="{ item }">
                {{ formatFileSize(item.fileSize) }}
              </template>

              <template v-slot:item.uploadedAt="{ item }">
                {{ formatDate(item.uploadedAt) }}
              </template>

              <template v-slot:item.verification="{ item }">
                <div class="d-flex align-center">
                  <v-icon
                    :color="item.isHederaVerified ? 'success' : 'grey'"
                    small
                    class="mr-1"
                  >
                    mdi-shield-check
                  </v-icon>
                  <v-icon
                    :color="item.isIpfsVerified ? 'success' : 'grey'"
                    small
                  >
                    mdi-cloud-check
                  </v-icon>
                </div>
              </template>

              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewDocument(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="success"
                  @click="downloadDocument(item)"
                >
                  <v-icon>mdi-download</v-icon>
                </v-btn>
                <v-btn
                  v-if="item.status === 'Rejected'"
                  icon
                  small
                  color="warning"
                  @click="reuploadDocument(item)"
                >
                  <v-icon>mdi-upload</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Upload Document Dialog -->
    <v-dialog v-model="showUploadDialog" max-width="600px" persistent>
      <v-card>
        <v-card-title class="text-h5">Upload Document</v-card-title>
        <v-card-text>
          <v-form ref="uploadForm" v-model="uploadFormValid">
            <v-select
              v-model="uploadData.documentType"
              :items="documentTypeOptions"
              label="Document Type"
              :rules="[v => !!v || 'Document type is required']"
              required
              outlined
            ></v-select>

            <v-file-input
              v-model="uploadData.file"
              label="Select File"
              :rules="fileRules"
              accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
              show-size
              outlined
              required
            ></v-file-input>

            <v-textarea
              v-model="uploadData.description"
              label="Description (Optional)"
              rows="3"
              outlined
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="cancelUpload">Cancel</v-btn>
          <v-btn
            color="primary"
            :disabled="!uploadFormValid"
            :loading="uploading"
            @click="uploadDocument"
          >
            Upload
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
/* eslint-disable */
export default {
  name: 'FarmerDocuments',

  data() {
    return {
      loading: false,
      search: '',
      statusFilter: 'All',
      showUploadDialog: false,
      uploadFormValid: false,
      uploading: false,

      statusOptions: ['All', 'Approved', 'Under Review', 'Rejected'],

      headers: [
        { text: 'Document Type', value: 'documentType', sortable: true },
        { text: 'File Name', value: 'fileName', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Size', value: 'fileSize', sortable: true },
        { text: 'Uploaded', value: 'uploadedAt', sortable: true },
        { text: 'Verification', value: 'verification', sortable: false },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '150px',
        },
      ],

      documents: [
        {
          id: 1,
          documentType: 'FARMING_LICENSE',
          fileName: 'farming-license-2024.pdf',
          status: 'Approved',
          fileSize: 2048576,
          uploadedAt: '2024-01-15T10:30:00Z',
          isHederaVerified: true,
          isIpfsVerified: true,
        },
        {
          id: 2,
          documentType: 'LAND_OWNERSHIP_CERTIFICATE',
          fileName: 'land-certificate.pdf',
          status: 'Under Review',
          fileSize: 1536000,
          uploadedAt: '2024-02-20T14:15:00Z',
          isHederaVerified: false,
          isIpfsVerified: true,
        },
        {
          id: 3,
          documentType: 'ENVIRONMENTAL_PERMIT',
          fileName: 'env-permit-scan.jpg',
          status: 'Rejected',
          fileSize: 3072000,
          uploadedAt: '2024-03-10T09:45:00Z',
          isHederaVerified: false,
          isIpfsVerified: true,
        },
      ],

      documentCategories: [
        {
          type: 'licenses',
          name: 'Licenses & Permits',
          icon: 'mdi-certificate',
          color: 'blue',
          uploaded: 2,
          required: 3,
          progress: 67,
          status: 'Incomplete',
        },
        {
          type: 'certificates',
          name: 'Certificates',
          icon: 'mdi-medal',
          color: 'green',
          uploaded: 3,
          required: 3,
          progress: 100,
          status: 'Complete',
        },
        {
          type: 'reports',
          name: 'Reports & Analysis',
          icon: 'mdi-file-chart',
          color: 'orange',
          uploaded: 1,
          required: 2,
          progress: 50,
          status: 'Incomplete',
        },
      ],

      uploadData: {
        documentType: '',
        file: null,
        description: '',
      },

      documentTypeOptions: [
        { text: 'Farming License', value: 'FARMING_LICENSE' },
        { text: 'Land Ownership Certificate', value: 'LAND_OWNERSHIP_CERTIFICATE' },
        { text: 'Environmental Permit', value: 'ENVIRONMENTAL_PERMIT' },
        { text: 'Organic Certificate', value: 'ORGANIC_CERTIFICATE' },
        { text: 'Quality Certificate', value: 'QUALITY_CERTIFICATE' },
        { text: 'Export License', value: 'EXPORT_LICENSE' },
        { text: 'Phytosanitary Certificate', value: 'PHYTOSANITARY_CERTIFICATE' },
        { text: 'Third Party Verification', value: 'THIRD_PARTY_VERIFICATION' },
        { text: 'Risk Assessment Report', value: 'RISK_ASSESSMENT_REPORT' },
        { text: 'Field Inspection Report', value: 'FIELD_INSPECTION_REPORT' },
        { text: 'Other', value: 'OTHER' },
      ],

      fileRules: [
        (v) => !!v || 'File is required',
        (v) => !v || v.size < 50000000 || 'File size should be less than 50MB',
      ],
    };
  },

  computed: {
    filteredDocuments() {
      if (this.statusFilter === 'All') {
        return this.documents;
      }
      return this.documents.filter((doc) => doc.status === this.statusFilter);
    },

    approvedDocs() {
      return this.documents.filter((doc) => doc.status === 'Approved').length;
    },

    pendingDocs() {
      return this.documents.filter((doc) => doc.status === 'Under Review').length;
    },

    rejectedDocs() {
      return this.documents.filter((doc) => doc.status === 'Rejected').length;
    },

    totalDocs() {
      return this.documents.length;
    },
  },

  methods: {
    getStatusColor(status) {
      const colors = {
        Approved: 'success',
        'Under Review': 'warning',
        Rejected: 'error',
      };
      return colors[status] || 'grey';
    },

    getDocumentTypeColor(type) {
      const colors = {
        FARMING_LICENSE: 'blue',
        LAND_OWNERSHIP_CERTIFICATE: 'green',
        ENVIRONMENTAL_PERMIT: 'orange',
        ORGANIC_CERTIFICATE: 'purple',
      };
      return colors[type] || 'grey';
    },

    getDocumentTypeIcon(type) {
      const icons = {
        FARMING_LICENSE: 'mdi-certificate',
        LAND_OWNERSHIP_CERTIFICATE: 'mdi-map-marker-check',
        ENVIRONMENTAL_PERMIT: 'mdi-leaf',
        ORGANIC_CERTIFICATE: 'mdi-medal',
      };
      return icons[type] || 'mdi-file-document';
    },

    formatDocumentType(type) {
      return type.replace(/_/g, ' ').toLowerCase()
        .replace(/\b\w/g, (l) => l.toUpperCase());
    },

    formatFileSize(bytes) {
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      if (bytes === 0) return '0 Bytes';
      const i = Math.floor(Math.log(bytes) / Math.log(1024));
      return `${Math.round(bytes / Math.pow(1024, i) * 100) / 100} ${sizes[i]}`;
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },

    viewDocument(document) {
    },

    downloadDocument(document) {
    },

    reuploadDocument(document) {
      this.uploadData.documentType = document.documentType;
      this.showUploadDialog = true;
    },

    cancelUpload() {
      this.showUploadDialog = false;
      this.resetUploadForm();
    },

    async uploadDocument() {
      if (!this.$refs.uploadForm.validate()) return;

      this.uploading = true;

      try {
        const formData = new FormData();
        formData.append('file', this.uploadData.file);
        formData.append('documentType', this.uploadData.documentType);
        formData.append('ownerEntityId', '111'); // Current farmer ID
        formData.append('ownerEntityType', 'FARMER');
        formData.append('uploaderId', 'current-user-id');
        formData.append('metadata', JSON.stringify({
          description: this.uploadData.description,
          uploadedBy: 'current-user-id',
          uploadedAt: new Date().toISOString(),
        }));

        // Make API call to upload document
        const response = await this.$http.post('/api/v1/documents/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        if (response.data.success) {
          // Add to documents list
          this.documents.unshift({
            id: response.data.documentId,
            documentType: this.uploadData.documentType,
            fileName: this.uploadData.file.name,
            status: 'Under Review',
            fileSize: this.uploadData.file.size,
            uploadedAt: new Date().toISOString(),
            isHederaVerified: !!response.data.hederaTransactionId,
            isIpfsVerified: !!response.data.ipfsHash,
          });

          this.showUploadDialog = false;
          this.resetUploadForm();
          this.$toast.success('Document uploaded successfully!');
        } else {
          throw new Error(response.data.error || 'Upload failed');
        }
      } catch (error) {
        this.$toast.error(`Failed to upload document: ${error.response?.data?.error || error.message}`);
      } finally {
        this.uploading = false;
      }
    },

    resetUploadForm() {
      this.uploadData = {
        documentType: '',
        file: null,
        description: '',
      };
      if (this.$refs.uploadForm) {
        this.$refs.uploadForm.resetValidation();
      }
    },
  },
};
</script>

<style scoped>
.farmer-documents {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.stat-card {
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.document-category {
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  margin-bottom: 16px;
}

.filter-select {
  max-width: 200px;
}

.search-field {
  max-width: 300px;
}
</style>
