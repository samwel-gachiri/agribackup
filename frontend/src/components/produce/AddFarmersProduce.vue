<template>
  <v-dialog
      v-model="dialog"
      max-width="500px"
  >
    <v-card
      rounded="xl"
      class="tw-flex tw-flex-col tw-justify-center tw-mt-4 action-panel"
    >
      <v-toolbar flat>
        <v-avatar size="40" class="tw-mr-2">
          <v-icon>mdi-corn</v-icon>
        </v-avatar>
        <v-toolbar-title>Add produce</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-toolbar>
      <v-form v-model="newProduce.isValid" class="tw-gap-0">
        <div class="tw-flex tw-flex-col tw-gap-5">
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
          <!-- Category selector: Crop vs Livestock -->
          <div>
            <div class="tw-text-xs tw-text-gray-600 tw-mb-1">Category</div>
            <v-btn-toggle v-model="category" mandatory dense class="tw-bg-white tw-rounded-lg tw-border tw-border-gray-200">
              <v-btn small value="CROP">Crop</v-btn>
              <v-btn small value="LIVESTOCK">Livestock</v-btn>
            </v-btn-toggle>
          </div>
        </div>
        <div class="tw-flex tw-my-5 tw-flex-col tw-gap-4">
          <!-- <v-textarea
              v-model="newProduce.desc"
              dense
              rounded
              class="data-input"
              label="Input produce description"
              :auto-grow="true"
              rows="2"
          ></v-textarea> -->
          <div class="tw-flex tw-justify-center tw-items-center">
            <v-btn
              small
              rounded
              color="success"
              class="tw-mb-2 tw-mt-[-8px] tw-w-fit"
              :loading="descLoading"
              @click="generateProduceDescription"
              :disabled="!selectedProduce"
            >
              <v-icon left size="18px">mdi-robot</v-icon>
              Generate Description
            </v-btn>
          </div>
          <div v-if="descError" class="tw-text-red-600 tw-text-xs tw-mt-1">{{ descError }}</div>
          <!-- Markdown preview below the textarea -->
          <div
            v-if="newProduce.desc"
            ref="descPreview"
            class="tw-bg-white tw-border tw-border-gray-200 tw-rounded tw-p-3 tw-mb-2 tw-text-sm"
            v-html="descPreview"
            aria-live="polite"
          ></div>
          <!-- Edit Description Button and Editable Description -->
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
          <!-- New input for previous yield amount -->
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
          <h6 class="tw-font-bold">{{newProduce.images.length > 1 ? 'Images': 'Image'}} Preview</h6>
          <!-- Camera PhotoCapture integration -->
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
        <div class="tw-flex tw-justify-center tw-items-center">
          <v-btn :loading="loading" color="primary" @click="addFarmerProduce" style="color: white;" rounded>
            Add Produce
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </v-dialog>
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

export default {
  name: 'add-farmer-produce',
  data() {
    return {
      dialog: false,
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
  props: {
    farmerProduces: {
      type: Array,
      default: () => [
        {
          id: '80a20596-056e-4e45-a010-2e6ef56e4757',
          farmProduce: {
            id: 'e1c0a0f8-307d-435b-a337-421c88feb62e',
            name: 'banana',
            description: '',
            farmingType: '',
            status: 'ACTIVE',
          },
          status: 'ON_FARM',
        },
      ],
    },
    // New prop to control save/emit behavior
    shouldSave: {
      type: Boolean,
      default: true,
    },
  },
  created() {
    this.fetchProduces(); // Fetch produces when component is created
    this.gemini = new GoogleGenerativeAI(this.geminiApiKey);
  },
  computed: {
    getCurrentUserId,
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
        if (this.shouldSave) {
          // Save via API as before
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
            this.closeDialog(); // Close dialog after successful save
            this.fetchProduces(); // Refresh produce list
          } else {
            this.$toast.error('Failed to add produce:', response.data.message);
          }
        } else {
          // Emit the new produce data to parent component
          this.$emit('add-produce', {
            name: this.selectedProduce,
            desc: this.newProduce.desc,
            farmingType: this.newProduce.farmingType,
            images: this.newProduce.images,
            previousYield: this.newProduce.previousYield,
          });
          this.closeDialog(); // Close dialog after emitting
          this.$toast.success('Produce added successfully!');
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
    openDialog() {
      this.dialog = true;
      // Reset editable description state when dialog opens
      this.editingDesc = false;
      this.editableDesc = this.newProduce.desc;
    },
    closeDialog() {
      this.dialog = false;
      this.fetchProduces(); // Refresh produce list when dialog is closed
    },
  },
  components: {
    PhotoCapture,
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

<style lang="scss">
.data-input {
  background-color: #FEEED5;
  border-radius: 10px;
  margin: 5px;
}
.action-panel {
  background-color: #FFF8F0;
  padding: 5px;
  border-radius: 10px;
}
</style>
