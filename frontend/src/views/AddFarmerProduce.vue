<template>
  <default>
    <div class="page-wrap">
      <div class="bg-shape bg-1"></div>
      <div class="bg-shape bg-2"></div>
      <div class="bg-shape bg-3"></div>

      <div class="glass-container">
        <!-- Header -->
        <div class="tw-flex tw-items-center tw-justify-between tw-mb-6">
          <div class="tw-flex tw-items-center tw-gap-3">
            <v-btn
              icon
              class="btn-glass-outline"
              @click="$router.go(-1)"
            >
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            <div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-gray-800">Add Farmer Produce</h1>
              <p class="tw-text-sm tw-text-gray-600">Add a new produce to your farm</p>
            </div>
          </div>
        </div>

        <!-- Form -->
        <v-form v-model="newProduce.isValid" class="tw-max-w-2xl">
          <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 tw-gap-6">
            <!-- Produce Selection -->
            <div class="tw-col-span-full">
              <v-combobox
                dense
                class="data-input"
                v-model="selectedProduce"
                :items="produceList.map((produce)=>produce.name)"
                label="Select or Enter Produce"
                clearable
                hide-no-data
                :rules="[required('Produce Name')]"
                :filter="() => true"
                allow-overflow
                solo
                :allow-new="true"
              >
              </v-combobox>
            </div>

            <!-- Category selector: Crop vs Livestock -->
            <div class="tw-col-span-full">
              <div class="tw-text-xs tw-text-gray-600 tw-mb-2">Category</div>
              <v-btn-toggle v-model="category" mandatory dense class="tw-bg-white tw-rounded-lg tw-border tw-border-gray-200">
                <v-btn small value="CROP">Crop</v-btn>
                <v-btn small value="LIVESTOCK">Livestock</v-btn>
              </v-btn-toggle>
            </div>
          </div>

          <div class="tw-mt-6 tw-space-y-6">
            <!-- Generate Description -->
            <div class="tw-flex tw-justify-center tw-items-center">
              <v-btn
                small
                rounded
                color="success"
                class="tw-w-fit"
                :loading="descLoading"
                @click="generateProduceDescription"
                :disabled="!selectedProduce"
              >
                <v-icon left size="18px">mdi-robot</v-icon>
                Generate Description
              </v-btn>
            </div>

            <div v-if="descError" class="tw-text-red-600 tw-text-xs tw-text-center">{{ descError }}</div>

            <!-- Markdown preview -->
            <div
              v-if="newProduce.desc"
              ref="descPreview"
              class="tw-bg-white tw-border tw-border-gray-200 tw-rounded tw-p-4 tw-text-sm"
              v-html="descPreview"
              aria-live="polite"
            ></div>

            <!-- Edit Description -->
            <div v-if="newProduce.desc">
              <v-btn
                small
                color="primary"
                class="tw-mb-2"
                @click="editingDesc = true"
                v-if="!editingDesc"
              >
                <v-icon left size="18px">mdi-pencil</v-icon>
                Edit Description
              </v-btn>
              <div v-if="editingDesc" class="tw-mt-2">
                <v-textarea
                  v-model="editableDesc"
                  label="Edit Description"
                  auto-grow
                  rows="3"
                  dense
                  outlined
                  class="data-input"
                ></v-textarea>
                <div class="tw-flex tw-gap-2 tw-mt-2">
                  <v-btn
                    small
                    color="success"
                    @click="saveEditedDesc"
                  >
                    Save
                  </v-btn>
                  <v-btn
                    small
                    color="error"
                    @click="cancelEditedDesc"
                  >
                    Cancel
                  </v-btn>
                </div>
              </div>
            </div>

            <!-- Previous Yield -->
            <v-text-field
              v-model="newProduce.previousYield"
              label="Previous Yield Amount"
              type="number"
              min="0"
              class="data-input"
              :rules="[value => value === undefined || value === '' || value >= 0 || 'Yield must be positive']"
              prepend-inner-icon="mdi-scale"
              append-text="kg"
              :append-outer-icon="newProduce.previousYield ? 'mdi-check' : ''"
              dense
              clearable
            ></v-text-field>

            <!-- Crop: Planted date -->
            <div v-if="category === 'CROP'">
              <div class="tw-text-xs tw-text-gray-600 tw-mb-1">Planted on (for prediction)</div>
              <v-menu v-model="plantedMenu" :close-on-content-click="false" transition="scale-transition" offset-y min-width="auto">
                <template v-slot:activator="{ on, attrs }">
                  <v-text-field
                    v-model="plantedDate"
                    label="Planted date"
                    readonly
                    dense
                    outlined
                    class="data-input"
                    prepend-inner-icon="mdi-calendar"
                    v-bind="attrs"
                    v-on="on"
                  />
                </template>
                <v-date-picker v-model="plantedDate" no-title scrollable @input="plantedMenu = false" />
              </v-menu>
            </div>

            <!-- Livestock: Animal age -->
            <div v-if="category === 'LIVESTOCK'" class="tw-grid tw-grid-cols-2 tw-gap-3">
              <v-text-field
                v-model.number="animalAgeValue"
                label="Animal age"
                type="number"
                min="0"
                dense
                outlined
                class="data-input"
                prepend-inner-icon="mdi-cow"
              />
              <v-select
                v-model="animalAgeUnit"
                :items="ageUnits"
                label="Age unit"
                dense
                outlined
                class="data-input"
              />
            </div>

            <!-- Optional: user-specified time to harvest -->
            <div>
              <div class="tw-text-xs tw-text-gray-600 tw-mb-1">Optional: specify time to harvest</div>
              <div class="tw-grid tw-grid-cols-2 tw-gap-3">
                <v-text-field
                  v-model.number="harvestOverrideValue"
                  label="Time value"
                  type="number"
                  min="0"
                  dense
                  outlined
                  class="data-input"
                  prepend-inner-icon="mdi-timer-outline"
                />
                <v-select
                  v-model="harvestOverrideUnit"
                  :items="harvestUnits"
                  label="Unit"
                  dense
                  outlined
                  class="data-input"
                />
              </div>
              <div class="tw-text-xs tw-text-gray-500 tw-mt-1">If provided, we'll use this to predict/plan harvest instead of defaults.</div>
            </div>

            <!-- Images -->
            <div>
              <h6 class="tw-font-bold">{{newProduce.images.length > 1 ? 'Images': 'Image'}} Preview</h6>
              <div class="tw-mb-2">
                <button
                  type="button"
                  class="tw-bg-blue-600 tw-text-white tw-rounded-full tw-px-4 tw-py-2 tw-shadow hover:tw-bg-blue-700 tw-transition"
                  @click.prevent="openCamera"
                >
                  Capture Photos
                </button>
              </div>

              <!-- Show PhotoCapture modal/dialog -->
              <PhotoCapture
                v-if="showCamera"
                @captured-photos="onPhotosCaptured"
                @close="showCamera = false"
              />

              <!-- Show previews of captured photos -->
              <div v-if="newProduce.images.length > 0" class="tw-flex tw-flex-row tw-justify-center tw-align-center tw-gap-2">
                <img
                  v-for="(image, index) in imagePreviews"
                  :key="index"
                  :src="image"
                  class="tw-w-24 tw-h-24 tw-object-cover tw-rounded tw-border tw-border-gray-300"
                  alt="Produce photo"
                />
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="tw-flex tw-justify-center tw-items-center tw-gap-4 tw-mt-8">
            <v-btn
              :loading="loading"
              color="primary"
              @click="addFarmerProduce"
              style="color: white;"
              rounded
              large
            >
              <v-icon left>mdi-plus</v-icon>
              Add Produce
            </v-btn>
            <v-btn
              outlined
              color="grey"
              @click="$router.go(-1)"
              rounded
              large
            >
              <v-icon left>mdi-close</v-icon>
              Cancel
            </v-btn>
          </div>
        </v-form>
      </div>
    </div>
  </default>
</template>

<script>
import axios from 'axios';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getCurrentUserId } from '@/utils/roles.js';
import { farmingTypes } from '@/assets/data/farmingTypes.js';
import validations from '@/utils/validations.js';
import PhotoCapture from '@/components/pictures/PhotoCapture.vue';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'AddFarmerProducePage',
  components: {
    Default,
    PhotoCapture,
  },
  data() {
    return {
      produceList: [], // To store list of produces fetched from '/produce'
      selectedProduce: null, // To store the selected produce by user
      newProduce: {
        isValid: false,
        name: '',
        desc: '',
        farmingType: '',
        images: [],
        previousYield: '', // Added field for previous yield
      },
      // New predictive fields
      category: 'CROP',
      plantedDate: '',
      plantedMenu: false,
      animalAgeValue: null,
      animalAgeUnit: 'months',
      ageUnits: ['days', 'weeks', 'months', 'years'],
      harvestOverrideValue: null,
      harvestOverrideUnit: 'days',
      harvestUnits: ['days', 'weeks', 'months'],
      newProduceId: '', // Used to store the id of a newly created produce
      showCamera: false,
      imagePreviews: [],
      farmingTypes,
      ...validations,
      loading: false,
      descLoading: false,
      descError: '',
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
      editingDesc: false,
      editableDesc: '',
    };
  },
  created() {
    this.fetchProduces(); // Fetch produces when component is created
    this.gemini = new GoogleGenerativeAI(this.geminiApiKey);
  },
  computed: {
    descPreview() {
      // Render Markdown preview from plain text
      return DOMPurify.sanitize(this.md.render(this.newProduce.desc || ''));
    },
  },
  methods: {
    /**
     * Fetches the list of available produces.
     *
     * Endpoint: GET /farmers-service/produce
     * Purpose: Retrieve all produce items for selection or reference.
     * Response: { data: [ { id, name, ... }, ... ] }
     */
    async fetchProduces() {
      this.loading = true;
      try {
        const response = await axios.get('/farmers-service/produce');
        this.produceList = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching produces:', error.message);
      } finally {
        this.loading = false;
      }
    },
    openCamera(e) {
      // Prevent default to avoid form submit/page reload
      if (e) {
        if (typeof e.preventDefault === 'function') e.preventDefault();
        if (typeof e.stopPropagation === 'function') e.stopPropagation();
        // If this is a native <button> inside a <form>, also set type="button" in the template
      }
      this.showCamera = true;
    },
    onPhotosCaptured(photos) {
      this.newProduce.images = photos.map((photo) => photo.blob);
      this.imagePreviews = photos.map((photo) => photo.url);
      this.showCamera = false;
    },
    validateImages(value) {
      // eslint-disable-next-line no-mixed-operators
      return value.length <= 3 || 'Maximum images is 3';
    },
    async addFarmerProduce() {
      if (!this.newProduce.isValid) {
        this.$toast.error('Give enough information');
        return;
      }
      this.loading = true;
      try {
        const formData = new FormData();
        formData.append('farmerId', getCurrentUserId());
        formData.append('produceName', this.selectedProduce);
        formData.append('description', this.newProduce.desc);
        formData.append('farmingType', this.newProduce.farmingType);
        formData.append('category', this.category);
        if (this.category === 'CROP' && this.plantedDate) {
          formData.append('plantedDate', this.plantedDate);
        }
        if (this.category === 'LIVESTOCK' && this.animalAgeValue !== null && this.animalAgeValue !== '') {
          formData.append('animalAgeValue', String(this.animalAgeValue));
          formData.append('animalAgeUnit', this.animalAgeUnit);
        }
        if (this.harvestOverrideValue !== null && this.harvestOverrideValue !== '') {
          formData.append('customHarvestValue', String(this.harvestOverrideValue));
          formData.append('customHarvestUnit', this.harvestOverrideUnit);
        }
        // Add previous yield if provided
        if (this.newProduce.previousYield !== '' && this.newProduce.previousYield !== undefined) {
          formData.append('yieldAmount', this.newProduce.previousYield);
        }
        // Use blobs from PhotoCapture as files
        if (this.newProduce.images.length) {
          this.newProduce.images.forEach((blob, idx) => {
            formData.append('images', blob, `produce-photo-${idx + 1}.jpg`);
          });
        }
        const response = await axios.post('/farmers-service/farmer/add-smart-produce', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        if (response.data.success) {
          this.$toast.success('Produce added successfully!');
          this.newProduceId = response.data.produceId; // Store the new produce ID
          // Redirect back to produces page
          this.$router.push({ name: 'Produces', params: { farmerId: getCurrentUserId() } });
        } else {
          this.$toast.error('Failed to add produce:', response.data.message);
        }
      } catch (error) {
        this.$toast.error('Error adding produce to farmer!', `${error}`);
      } finally {
        this.loading = false;
      }
    },
    saveEditedDesc() {
      this.newProduce.desc = this.editableDesc;
      this.editingDesc = false;
    },
    cancelEditedDesc() {
      this.editableDesc = this.newProduce.desc;
      this.editingDesc = false;
    },
    async generateProduceDescription() {
      this.descError = '';
      if (!this.selectedProduce) {
        this.descError = 'Please select or enter a produce name first.';
        return;
      }
      this.descLoading = true;
      const systemPrompt = 'You are an expert agricultural assistant. Given a produce name, generate a short, simple Markdown description for a farmer. If it is a crop, include: typical spacing, growth time, fertilizer, and any key info. If not a crop, give relevant farming details. Keep it concise and practical. Do not hallucinate';
      const userPrompt = `Produce: ${this.selectedProduce}${this.newProduce.farmingType ? `\nType: ${this.newProduce.farmingType}` : ''}`;
      try {
        const model = this.gemini.getGenerativeModel({ model: 'gemini-2.5-flash' });
        const result = await model.generateContent([systemPrompt, userPrompt]);
        const response = await result.response;
        const text = response.text();
        // Store plain text (Markdown source) in textarea
        this.newProduce.desc = text;
        this.editableDesc = text; // sync editable with generated
      } catch (err) {
        this.descError = 'Failed to generate description. Try again.';
      } finally {
        this.descLoading = false;
      }
    },
  },
  watch: {
    // Watch for changes in selectedProduce to update newProduce.name
    selectedProduce(newValue) {
      this.newProduce.name = newValue || '';
      this.generateProduceDescription();
    },
  },
};
</script>

<style scoped>
/* Page background with subtle gradient */
.page-wrap {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% -10%, #ebfff3 0%, rgba(235,255,243,0) 60%),
              radial-gradient(1000px 600px at 110% 10%, #e9f3ff 0%, rgba(233,243,255,0) 60%),
              linear-gradient(135deg, #f5fdf7 0%, #f2fbff 100%);
}

/* Decorative blurred blobs */
.bg-shape {
  position: absolute;
  filter: blur(40px);
  opacity: 0.55;
  z-index: 0;
  animation: float 14s ease-in-out infinite;
}
.bg-1 { width: 320px; height: 320px; top: -40px; left: -40px; background: radial-gradient(circle at 30% 30%, #86efac, transparent 60%); animation-delay: 0s; }
.bg-2 { width: 360px; height: 360px; bottom: -60px; right: -60px; background: radial-gradient(circle at 70% 70%, #93c5fd, transparent 60%); animation-delay: 1.2s; }
.bg-3 { width: 280px; height: 280px; top: 40%; left: 60%; background: radial-gradient(circle at 50% 50%, #f0abfc, transparent 60%); animation-delay: 2.1s; }

/* Glassmorphic container */
.glass-container {
  position: relative;
  z-index: 1;
  margin: 0 auto;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 32px;
  backdrop-filter: saturate(180%) blur(14px);
}

.data-input {
  background-color: #FEEED5;
  border-radius: 10px;
  margin: 5px 0;
}

/* Buttons */
.btn-gradient {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 35%, #0ea5e9 100%) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 8px 18px rgba(34,197,94,0.25) !important;
}
.btn-gradient:hover { filter: brightness(1.05); transform: translateY(-1px); }
.btn-glass-outline {
  background: rgba(255,255,255,0.55) !important;
  color: #0f766e !important;
  border: 1px solid rgba(15,118,110,0.25) !important;
  backdrop-filter: blur(8px);
}

/* Keyframes */
@keyframes float {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(0, 8px, 0) scale(1.02); }
}

@media (max-width: 768px) {
  .glass-container { padding: 20px; }
  .tw-grid-cols-2 { grid-template-columns: 1fr; }
}
</style>
