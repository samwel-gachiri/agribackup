<template>
  <div class="tw-flex tw-flex-col tw-gap-5 tw-mt-2">
    <v-form v-model="updatedProduce.isValid" class="tw-gap-0">
      <!-- Section: Produce Info -->
      <div class="section-title">
        <v-icon color="primary" small class="tw-mr-1">mdi-information-outline</v-icon>
        <span>Produce Info</span>
      </div>
      <div class="form-grid">
        <div>
          <v-text-field
            v-model="updatedProduce.name"
            label="Produce Name"
            outlined
            disabled
          />
        </div>
        <div>
          <v-combobox
            v-model="updatedProduce.farmingType"
            label="Farming Type"
            :items="farmingTypes"
            outlined
          />
        </div>
      </div>

      <!-- Section: Description with AI generation -->
      <div class="section-title tw-mt-1">
        <v-icon color="primary" small class="tw-mr-1">mdi-text-box-edit-outline</v-icon>
        <span>Description</span>
      </div>
      <div class="tw-flex tw-justify-center tw-items-center">
        <v-btn
          small
          rounded
          color="success"
          class="tw-mb-1 tw-w-fit"
          :loading="descLoading"
          @click="generateProduceDescription"
          :disabled="!updatedProduce.name"
        >
          <v-icon left size="18px">mdi-robot</v-icon>
          Generate Description
        </v-btn>
      </div>
      <div v-if="descError" class="tw-text-red-600 tw-text-xs tw-mt-1 tw-text-center">{{ descError }}</div>

      <v-textarea
        v-if="editingDesc"
        v-model="editableDesc"
        label="Edit Description"
        auto-grow
        rows="3"
        dense
        outlined
      />
      <div v-else-if="updatedProduce.desc" ref="descPreview" class="desc-preview" v-html="descPreview" aria-live="polite"></div>
      <div class="tw-flex tw-gap-2 tw-mt-1" v-if="updatedProduce.desc">
        <v-btn small color="primary" @click="editingDesc = true" v-if="!editingDesc">
          <v-icon left size="18px">mdi-pencil</v-icon>
          Edit Description
        </v-btn>
        <v-btn small color="success" v-if="editingDesc" @click="saveEditedDesc">Save</v-btn>
        <v-btn small color="error" v-if="editingDesc" @click="cancelEditedDesc">Cancel</v-btn>
      </div>
      <v-textarea
        v-if="!updatedProduce.desc && !editingDesc"
        v-model="updatedProduce.desc"
        dense
        outlined
        auto-grow
        label="Produce Description"
      />

      <!-- Section: Images -->
      <div class="section-title tw-mt-4">
        <v-icon color="primary" small class="tw-mr-1">mdi-image-multiple</v-icon>
        <span>Images</span>
        <span class="tw-text-[11px] tw-text-gray-500 tw-ml-2">Up to {{ maxImages }} images. First image is used as cover.</span>
      </div>
      <input
        ref="fileInput"
        type="file"
        multiple
        accept="image/*"
        class="tw-hidden"
        @change="onFilesSelected"
      />
      <div class="tile-grid">
        <!-- Existing images -->
        <div v-for="(image, idx) in updatedProduce.imageUrls" :key="`existing-${idx}`" class="tile tile-image">
          <img :src="image" :alt="`Image ${idx+1}`" />
          <button type="button" class="tile-remove" @click.prevent="removeImage(image)">
            <v-icon small>mdi-close</v-icon>
          </button>
        </div>
        <!-- New image previews -->
        <div v-for="(image, idx) in imagePreviews" :key="`preview-${idx}`" class="tile tile-image">
          <img :src="image" :alt="`Preview ${idx+1}`" />
          <button type="button" class="tile-remove" @click.prevent="removeNewImage(idx)">
            <v-icon small>mdi-close</v-icon>
          </button>
        </div>
        <!-- Add tile -->
        <button
          v-if="canAddMore"
          type="button"
          class="tile tile-add"
          @click.prevent="clickAddTile"
          aria-label="Add image"
        >
          <div class="tile-add-inner">
            <v-icon large color="primary">mdi-plus</v-icon>
            <div class="tw-text-[11px] tw-text-gray-500 tw-mt-1">Add image</div>
          </div>
        </button>
      </div>

      <div class="tw-flex tw-justify-end tw-items-center tw-mt-4">
        <v-btn :loading="loading" :disabled="!updatedProduce.isValid" color="primary" class="btn-gradient" @click="updateProduce">
          Update Produce
        </v-btn>
      </div>
    </v-form>
  </div>
  </template>

<script>
/* disable-eslint */
import axios from 'axios';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { farmingTypes } from '@/assets/data/farmingTypes.js';
import validations from '@/utils/validations.js';

export default {
  name: 'update-farmer-produce',
  props: {
    selectedFarmerProduce: Object,
  },
  data() {
    return {
      updatedProduce: {
        isValid: false,
        id: this.selectedFarmerProduce.id,
        name: this.selectedFarmerProduce.farmProduce.name,
        desc: this.selectedFarmerProduce.description,
        farmingType: this.selectedFarmerProduce.farmingType,
        imageUrls: this.selectedFarmerProduce.imageUrls || [],
        removedImages: [],
        newImages: [],
      },
      imagePreviews: [],
      farmingTypes,
      ...validations,
      loading: false,
      maxImages: 3,
      // AI description state
      descLoading: false,
      descError: '',
      md: new MarkdownIt({
        html: true, xhtmlOut: true, breaks: true, linkify: true, typographer: true, strict: true,
      }),
      geminiApiKey: process.env.VUE_APP_GOOGLE_API_KEY,
      gemini: null,
      editingDesc: false,
      editableDesc: '',
    };
  },
  created() {
    try { this.gemini = new GoogleGenerativeAI(this.geminiApiKey); } catch (e) { /* ignore */ }
    this.editableDesc = this.updatedProduce.desc;
  },
  methods: {
    removeImage(image) {
      this.updatedProduce.imageUrls = this.updatedProduce.imageUrls.filter((item) => item !== image);
      this.updatedProduce.removedImages.push(image);
    },
    removeNewImage(idx) {
      // eslint-disable-next-line no-unused-vars
      const file = this.updatedProduce.newImages[idx];
      const url = this.imagePreviews[idx];
      if (url) URL.revokeObjectURL(url);
      this.updatedProduce.newImages.splice(idx, 1);
      this.imagePreviews.splice(idx, 1);
    },
    clickAddTile() {
      this.$refs.fileInput.click();
    },
    onFilesSelected(e) {
      const files = Array.from(e.target.files || []);
      if (!files.length) return;
      const currentCount = this.updatedProduce.imageUrls.length + this.imagePreviews.length;
      const remaining = this.maxImages - currentCount;
      if (remaining <= 0) {
        // eslint-disable-next-line no-unused-expressions
        this.$toast && this.$toast.error(`You can upload up to ${this.maxImages} images only`);
        e.target.value = '';
        return;
      }
      const toAdd = files.slice(0, remaining);
      toAdd.forEach((file) => {
        this.updatedProduce.newImages.push(file);
        this.imagePreviews.push(URL.createObjectURL(file));
      });
      if (files.length > toAdd.length) {
        // eslint-disable-next-line no-unused-expressions
        this.$toast && this.$toast.error(`Only ${remaining} more image(s) allowed`);
      }
      e.target.value = '';
    },
    async updateProduce() {
      this.loading = true;
      const formData = new FormData();
      formData.append('id', this.updatedProduce.id);
      formData.append('description', this.updatedProduce.desc);
      formData.append('farmingType', this.updatedProduce.farmingType == null ? '' : this.updatedProduce.farmingType);
      formData.append('removeImageUrls', this.updatedProduce.removedImages);
      this.updatedProduce.newImages.forEach((file) => {
        formData.append('newImages', file);
      });
      try {
        const response = await axios.put(`/farmers-service/farmer/update-farmer-produce/${this.updatedProduce.id}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        if (response.data.success) {
          this.$toast.success('Produce updated successfully!');
        } else {
          this.$toast.error('Update failed:', response.data.msg);
        }
      } catch (error) {
        this.$toast.error('Error updating produce:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async generateProduceDescription() {
      this.descError = '';
      if (!this.updatedProduce.name) {
        this.descError = 'Missing produce name.';
        return;
      }
      this.descLoading = true;
      const systemPrompt = 'You are an expert agricultural assistant. Given a produce name, generate a short, simple Markdown description for a farmer. If it is a crop, include: typical spacing, growth time, fertilizer, and any key info. If not a crop, give relevant farming details. Keep it concise and practical. Do not hallucinate';
      const userPrompt = `Produce: ${this.updatedProduce.name}${this.updatedProduce.farmingType ? `\nType: ${this.updatedProduce.farmingType}` : ''}`;
      try {
        if (!this.gemini) throw new Error('AI not initialized');
        const model = this.gemini.getGenerativeModel({ model: 'gemini-2.5-flash' });
        const result = await model.generateContent([systemPrompt, userPrompt]);
        const response = await result.response;
        const text = response.text();
        this.updatedProduce.desc = text;
        this.editableDesc = text;
        this.editingDesc = false;
      } catch (err) {
        this.descError = 'Failed to generate description. Try again.';
      } finally {
        this.descLoading = false;
      }
    },
    saveEditedDesc() {
      this.updatedProduce.desc = this.editableDesc;
      this.editingDesc = false;
    },
    cancelEditedDesc() {
      this.editableDesc = this.updatedProduce.desc;
      this.editingDesc = false;
    },
  },
  computed: {
    canAddMore() {
      return this.updatedProduce.imageUrls.length + this.imagePreviews.length < this.maxImages;
    },
    descPreview() {
      return DOMPurify.sanitize(this.md.render(this.updatedProduce.desc || ''));
    },
  },
  beforeDestroy() {
    try {
      this.imagePreviews.forEach((u) => URL.revokeObjectURL(u));
    // eslint-disable-next-line no-empty
    } catch (_) {}
  },
};
</script>

<style lang="scss" scoped>
.section-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}
@media (min-width: 768px) {
  .form-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

.desc-preview {
  background: rgba(255,255,255,0.85);
  border: 1px solid rgba(0,0,0,0.06);
  border-radius: 12px;
  padding: 12px;
  font-size: 0.9rem;
}

.tile-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.tile {
  position: relative;
  width: 100%;
  padding-top: 100%; // square
  border-radius: 14px;
  border: 1px solid transparent;
  background-image:
    linear-gradient(180deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7)),
    linear-gradient(135deg, #d1fae5, #bfdbfe, #f5d0fe);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
  overflow: hidden;
}

.tile-image img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.tile-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  background: rgba(255,255,255,0.85);
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}
.tile-remove:hover { filter: brightness(0.95); }

.tile-add {
  display: grid;
  place-items: center;
  color: #0f766e;
}
.tile-add-inner { text-align: center; }

.btn-gradient {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 35%, #0ea5e9 100%) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 8px 18px rgba(34,197,94,0.25) !important;
}
</style>
