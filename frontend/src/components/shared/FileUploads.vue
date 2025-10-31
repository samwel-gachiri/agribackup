<template>
  <div class="tw-flex tw-content-between tw-flex-wrap">
    <v-card class="tw-rounded-lg c-bg-gray tw-flex tw-justify-center tw-flex-wrap" :flat="true">
      <div class="image-preview tw-px-8 md:tw-px-12 tw-py-2" v-if="imageData.length > 0">
        <div v-if="allowDelete" class="tw-w-full tw-flex tw-justify-end tw-absolute tw--ml-20 tw--mt-3">
          <v-icon class="tw-bg-red-700 tw-rounded-full tw-p-1 tw-cursor-pointer" title="remove image" size="17" color="#fff" @click="deleteImage">mdi-close</v-icon>
        </div>
        <img class="preview" :src="imageData" alt="img" />
      </div>
      <label v-else class="tw-flex tw-items-center tw-px-8 md:tw-px-12 tw-py-4 md:tw-py-8 tw-cursor-pointer">
        <v-icon class="c-bg-green tw-rounded-full tw-w-6 tw-h-6" color="#fff">mdi-plus</v-icon>
        <p class="c-green-text tw-mt-3 tw-px-3">Attach Image</p>
        <input type="file" :id="name" class="tw-hidden" @change="previewImage" accept="image/*" />
        <v-text-field class="tw-hidden" v-model="image" :rules="rules"></v-text-field>
      </label>
    </v-card>
    <small class="tw-w-full tw-p-2">maximum of 5 MB</small>
  </div>
</template>

<script>
import helpers from '@/utils/helpers.js';

export default {
  name: 'FileUploads',
  props: {
    rules: {
      type: Array,
      required: false,
    },
    name: String,
    addedImage: {
      type: Object,
      required: false,
    },
  },
  data() {
    return {
      imageData: '',
      image: '',
      allowDelete: true,
      file: {},
      result: {},
      reader: {},
      scale: 100,
      quality: 20,
    };
  },
  watch: {
    image(val) {
      this.$emit('input', val);
    },
  },
  mounted() {
    if (!helpers.objIsEmpty(this.addedImage)) {
      this.image = this.addedImage;
      this.imageData = this.addedImage.base64String;
    }
  },
  methods: {
    previewImage(event) {
      const input = event.target;
      if (input?.files?.[0]) {
        // eslint-disable-next-line prefer-destructuring
        this.file = input.files[0];
        if (/\.(jpe?g|png|gif)$/i.test(this.file.name)) {
          const FileSize = this.file.size / 1024 / 1024;
          if (FileSize > 5) {
            this.$toast.error('Image greater than 2 MB', 'Image Error');
            return;
          }

          // Make new FileReader
          this.reader = new FileReader();
          // Convert the file to base64 text
          this.reader.readAsDataURL(this.file);
          // on reader load something...
          this.reader.onload = this.fileOnLoad;
        } else {
          this.$toast.error('Not Valid Image', 'Image Error');
        }
      }
    },
    drawImage(imgUrl, type) {
      // Recreate Canvas Element
      const canvas = document.createElement('canvas');
      // Set Canvas Context
      const ctx = canvas.getContext('2d');
      // Create New Image
      const img = new Image();
      img.src = imgUrl;
      img.onload = () => {
        // Image Size After Scaling
        const scale = this.scale / 100;
        const width = img.width * scale;
        const height = img.height * scale;
        // Set Canvas Height And Width According to Image Size And Scale
        canvas.setAttribute('width', width);
        canvas.setAttribute('height', height);
        ctx.drawImage(img, 0, 0, width, height);
        // Quality Of Image
        const quality = this.quality ? (this.quality / 100) : 1;
        // If all files have been proceed
        const base64 = canvas.toDataURL(type, quality);
        const fileName = this.result.file.name;

        const objToPass = {
          canvas,
          original: this.result,
          compressed: {
            base64,
            name: fileName,
            file: this.buildFile(base64, type),
          },
        };

        objToPass.compressed.size = `${Math.round(objToPass.compressed.file.size / 1000)} kB`;
        objToPass.compressed.type = type;
        this.imageData = objToPass.compressed.base64;
        this.image = {
          base64String: this.imageData,
          documentId: -1,
          documentName: this.name,
        };
      };
    },
    fileOnLoad() {
      // The File
      const { file } = this;
      // Make a fileInfo Object
      const fileInfo = {
        name: file.name,
        type: file.type,
        size: `${Math.round(file.size / 1000)} kB`,
        base64: this.reader.result,
        file,
      };
      // Push it to the state
      this.result = fileInfo;
      // DrawImage
      this.drawImage(this.result.base64, file.type);
    },
    // Convert Blob To File
    buildFile(blob, type) {
      return new Blob([blob], { type });
    },
    deleteImage() {
      this.imageData = '';
      this.image = '';
      this.file = {};
      this.result = {};
      this.reader = {};
    },
  },
};
</script>

<style scoped>
img.preview {
  width: 200px;
  background-color: white;
  border: 1px solid #DDD;
  padding: 5px;
}
</style>
