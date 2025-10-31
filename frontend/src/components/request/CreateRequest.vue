<template>
  <v-card>
    <v-form v-model="isValid" class="tw-py-4 tw-pr-3 tw-flex tw-flex-col tw-justify-center">
      <div class="tw-w-full tw-flex tw-justify-center tw-flex-col">
        <logo-title>
          <h2>Request for farm produce</h2>
        </logo-title>
        <div class="tw-grid md:tw-grid-cols-2 tw-grid-cols-1 tw-gap-5 tw-m-5">
          <v-combobox
              dense
              v-model="selectedProduce"
              :items="produces"
              title="name"
              item-value="id"
              item-text="name"
              label="Select farm produce"
              @change="farmProduceRequestChanged"
              return-object
          ></v-combobox>
          <v-combobox
              v-model="request.unit"
              dense
              label="Give in measurement"
              :items="units"
          ></v-combobox>
          <number-input
              :label="`Quantity in ${request.unit}`"
              v-model="request.quantity"
              :min="1"
              :initial-value="request.quantity"
          ></number-input>
<!--              :rules="[required('produce')]"-->
          <vuetify-money
              v-model="request.price.price"
              :label="`Input the price per (${request.unit})`"
              :options="{
                locale: 'en-US',
                prefix: 'Ksh. ',
                suffix: `/${request.unit}`,
                length: 11,
                precision: 2,
              }"
              :rules="[required('price')]"
          ></vuetify-money>
          <vuetify-money
              label="Total"
              readonly
              :value="request.price.price * request.quantity"
              :options="vuetifyMoneyOptions"
          ></vuetify-money>
          <!-- Image Upload Section -->
          <div v-if="false">
            <label class="tw-font-medium">Upload Images (Min: 1, Max: 3)</label>
          </div>
          <v-btn
              style="background-color: darkgreen; color: white;"
              :disabled="!isValid || request.buyerProduceId === '' || request.price.price <= 0"
              :loading="loading"
              @click="postRequest "
          >Request<v-icon>mdi-gesture-two-tap</v-icon>
          </v-btn>
        </div>
      </div>
      <div class="tw-text-center tw-mt-6">
      </div>
    </v-form>
  </v-card>
</template>

<script >

import axios from 'axios';
import LogoTitle from '@/components/shared/LogoText.vue';
import HelperMixins from '@/mixins/helperMixins.js';
import { getCurrentUserId } from '@/utils/roles.js';
import NumberInput from '@/components/shared/NumberInput.vue';
import validations from '@/utils/validations.js';

export default {
  name: 'CreateRequest',
  components: { NumberInput, LogoTitle },
  mixins: [HelperMixins],
  data() {
    return {
      units: ['KG', 'L'],
      selectedProduce: null,
      buyer: null,
      request: {
        produceSource: '',
        buyerProduceId: '',
        quantity: 1,
        price: {
          price: 0,
          currency: 'KSH',
        },
        unit: '',
        images: [],
      },
      produces: [],
      imagePreviews: [],
      isValid: false,
      ...validations,
      loading: false,
    };
  },
  mounted() {
    axios.get(`/buyers-service/buyer?buyerId=${getCurrentUserId()}`).then((response) => {
      if (response.data.data == null) throw Error('User not found');
      this.buyer = response.data.data;
    }).catch((e) => {
      this.$toast.error(e.message);
    });
    // eslint-disable-next-line sonarjs/no-duplicate-string
    axios.get('/farmers-service/produce').then((response) => {
      if (response.data.success === false) this.$toast.error(response.data.msg);
      this.produces = response.data.data;
    }).catch((e) => {
      this.$toast.error(e.message);
    });
  },
  computed: {
    getCurrentUserId,
  },
  methods: {
    farmProduceRequestChanged(farmProduce) {
      const farmProduceTense = farmProduce.name;
      this.units = [farmProduceTense, 'KG', 'L'];
      this.request.unit = 'KG';
      this.request.buyerProduceId = farmProduce.id;
      this.request.produceSource = farmProduce.source;
    },
    previewImages() {
      this.imagePreviews = []; // Clear previous previews
      if (this.request.images) {
        this.request.images.forEach((file) => {
          this.imagePreviews.push(URL.createObjectURL(file));
        });
      }
    },
    validateImages(value) {
      // eslint-disable-next-line no-mixed-operators
      return value.length >= 1 && value.length <= 3 || 'You must upload between 1 and 3 images';
    },
    async postRequest() {
      this.loading = true;
      try {
        let produceId = this.selectedProduce.id;
        if (this.request.produceSource.toUpperCase() === 'FARMER') {
          // adds to buyers table
          produceId = (await axios.post('/buyers-service/produce', this.selectedProduce)).data.data.id;
        }

        const buyerHasProduce = this.buyer.preferredProduces.some((p) => p.bsfarmProduce.id === produceId);
        if (buyerHasProduce === false) {
          const res = axios.post('/buyers-service/buyer/add-buyer-produce', {
            buyerId: getCurrentUserId(),
            buyerProducesId: [
              produceId,
            ],
          });
          // adds to buyers preferred produce
          if (res.data.data.success === true) {
            this.buyer.prefferedProduces.push(res.data.data);
          }
        }
        await axios.post('/buyers-service/request', {
          buyerProduceId: this.buyer.preferredProduces.find((p) => p.bsfarmProduce.id === produceId).id,
          price: this.request.price,
          quantity: this.request.quantity,
          unit: this.request.unit,
        }).then((response) => {
          if (response.data.success === true) {
            this.$toast.success('Request posted successfully');
          } else {
            this.$toast.error(response.data.msg, 'Error creating request!');
          }
        }).catch((error) => {
          this.$toast.error(error);
        }).finally(() => this.loading === false);
      } catch (e) {
        this.$toast.error(e.message);
      } finally {
        this.loading = false;
      }
      // this.loading = true;
      // const formData = new FormData();
      // formData.append('buyerProduceId', this.request.buyerProduceId);
      // formData.append('price', JSON.stringify(this.request.price));
      // formData.append('quantity', this.request.quantity);
      // formData.append('unit', this.request.unit);
      // this.request.images.forEach((file, index) => {
      //   formData.append(`images[${index}]`, file);
      // });
      // axios.post('/buyers-service/request', formData, {
      //   headers: { 'Content-Type': 'multipart/form-data' },
      // })
      //   .then((response) => {
      //     if (response.data.success === true) {
      //       this.$toast.success('Request posted successfully');
      //     } else {
      //       this.$toast.error(response.data.msg, 'Error creating request!');
      //     }
      //   })
      //   .catch((error) => {
      //     this.$toast.error('Error creating request!', error.message);
      //   })
      //   .finally(() => {
      //     this.loading = false;
      //   });
    },
  },
};
</script>

<style scoped>

</style>
