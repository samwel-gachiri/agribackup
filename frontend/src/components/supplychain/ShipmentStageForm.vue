<template>
  <div class="tw-space-y-4">
    <div class="tw-bg-indigo-50 tw-rounded-lg tw-p-4 tw-border tw-border-indigo-200">
      <div class="tw-flex tw-items-center tw-gap-2 tw-text-indigo-800">
        <v-icon color="indigo darken-2">mdi-information</v-icon>
        <span class="tw-font-semibold">Record shipment details to importer</span>
      </div>
    </div>

    <v-select
      v-model="formData.importerId"
      :items="importers"
      item-text="name"
      item-value="id"
      label="Select Importer"
      outlined
      prepend-inner-icon="mdi-domain"
      :rules="[rules.required]"
    >
      <template #selection="{ item }">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-avatar size="24" color="indigo lighten-4">
            <span class="tw-text-xs tw-font-bold tw-text-indigo-800">{{ getInitials(item.name) }}</span>
          </v-avatar>
          <span>{{ item.name }}</span>
        </div>
      </template>
      <template #item="{ item }">
        <div class="tw-py-2">
          <div class="tw-font-semibold">{{ item.name }}</div>
          <div class="tw-text-xs tw-text-gray-500">{{ item.country || 'Country not set' }}</div>
          <v-chip x-small color="indigo" text-color="white" class="tw-mt-1">
            <v-icon x-small left>mdi-truck</v-icon>
            {{ item.importVolume || 'N/A' }} kg/month
          </v-chip>
        </div>
      </template>
    </v-select>

    <v-text-field
      v-model="formData.shipmentDate"
      label="Shipment Date"
      type="date"
      outlined
      prepend-inner-icon="mdi-calendar"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.expectedDeliveryDate"
      label="Expected Delivery Date"
      type="date"
      outlined
      prepend-inner-icon="mdi-calendar-check"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.shippedQuantity"
      label="Shipped Quantity (kg)"
      type="number"
      outlined
      prepend-inner-icon="mdi-weight-kilogram"
      :rules="[rules.required, rules.positive]"
      suffix="kg"
    ></v-text-field>

    <v-text-field
      v-model="formData.containerNumber"
      label="Container Number"
      outlined
      prepend-inner-icon="mdi-alpha-c-box"
      :rules="[rules.required]"
      hint="Container or Vehicle ID"
    ></v-text-field>

    <v-text-field
      v-model="formData.sealNumber"
      label="Seal Number"
      outlined
      prepend-inner-icon="mdi-lock"
      :rules="[rules.required]"
    ></v-text-field>

    <v-select
      v-model="formData.transportMode"
      :items="transportModes"
      label="Transport Mode"
      outlined
      prepend-inner-icon="mdi-truck-fast"
      :rules="[rules.required]"
    ></v-select>

    <v-text-field
      v-model="formData.carrier"
      label="Carrier Company"
      outlined
      prepend-inner-icon="mdi-truck-delivery"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.trackingNumber"
      label="Tracking Number"
      outlined
      prepend-inner-icon="mdi-map-marker-path"
      hint="Optional tracking reference"
    ></v-text-field>

    <v-text-field
      v-model="formData.originPort"
      label="Origin Port/Location"
      outlined
      prepend-inner-icon="mdi-airport"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.destinationPort"
      label="Destination Port/Location"
      outlined
      prepend-inner-icon="mdi-map-marker-check"
      :rules="[rules.required]"
    ></v-text-field>

    <div class="tw-bg-amber-50 tw-rounded-lg tw-p-4 tw-border tw-border-amber-200">
      <div class="tw-flex tw-items-start tw-gap-3">
        <v-icon color="amber darken-2" class="tw-mt-1">mdi-file-certificate</v-icon>
        <div class="tw-flex-1">
          <h4 class="tw-font-bold tw-text-amber-900 tw-mb-2">Documentation Checklist</h4>
          <div class="tw-space-y-2">
            <v-checkbox
              v-model="formData.hasCommercialInvoice"
              label="Commercial Invoice"
              dense
              hide-details
              color="amber darken-2"
            ></v-checkbox>
            <v-checkbox
              v-model="formData.hasPackingList"
              label="Packing List"
              dense
              hide-details
              color="amber darken-2"
            ></v-checkbox>
            <v-checkbox
              v-model="formData.hasCertificateOfOrigin"
              label="Certificate of Origin"
              dense
              hide-details
              color="amber darken-2"
            ></v-checkbox>
            <v-checkbox
              v-model="formData.hasPhytosanitaryCertificate"
              label="Phytosanitary Certificate"
              dense
              hide-details
              color="amber darken-2"
            ></v-checkbox>
            <v-checkbox
              v-model="formData.hasBillOfLading"
              label="Bill of Lading / Airway Bill"
              dense
              hide-details
              color="amber darken-2"
            ></v-checkbox>
          </div>
        </div>
      </div>
    </div>

    <v-textarea
      v-model="formData.notes"
      label="Shipment Notes"
      outlined
      rows="3"
      prepend-inner-icon="mdi-note-text"
    ></v-textarea>
  </div>
</template>

<script>
export default {
  name: 'ShipmentStageForm',
  props: {
    stage: {
      type: Object,
      required: true,
    },
    importers: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      formData: {
        importerId: '',
        shipmentDate: '',
        expectedDeliveryDate: '',
        shippedQuantity: '',
        containerNumber: '',
        sealNumber: '',
        transportMode: '',
        carrier: '',
        trackingNumber: '',
        originPort: '',
        destinationPort: '',
        hasCommercialInvoice: false,
        hasPackingList: false,
        hasCertificateOfOrigin: false,
        hasPhytosanitaryCertificate: false,
        hasBillOfLading: false,
        notes: '',
        ...this.stage.data,
      },
      transportModes: ['Sea Freight', 'Air Freight', 'Road Transport', 'Rail Transport', 'Multimodal'],
      rules: {
        required: (v) => !!v || 'This field is required',
        positive: (v) => (v && parseFloat(v) > 0) || 'Must be greater than 0',
      },
    };
  },
  watch: {
    formData: {
      deep: true,
      handler(newVal) {
        this.$emit('update', newVal);
      },
    },
  },
  methods: {
    getInitials(name) {
      if (!name) return '?';
      return name.split(' ')
        .map((n) => n[0])
        .join('')
        .substring(0, 2)
        .toUpperCase();
    },
  },
};
</script>
