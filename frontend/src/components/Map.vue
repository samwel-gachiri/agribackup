<script>

import Default from '@/components/layout/Default.vue';
import googleMapsLoader from '@/components/GoogleMapsLoader.js';
// import axios from 'axios';

export default {
  components: { Default },
  data() {
    return {
      map: null,
      center: { lat: null, lng: null },
      markers: [
        {
          lat: 0,
          lng: 0,
          customName: 'string',
          iconUrl: null,
          clickEvent: () => {},
          mouseOverEvent: () => {},
          mouseExitEvent: () => {},
        },
      ],
      directionsService: null,
      directionsRenderer: null,
      distance: null,
    };
  },
  methods: {
    loadMap() {
      googleMapsLoader.load()
        .then((google) => {
          this.directionsService = new google.maps.DirectionsService();
          this.directionsRenderer = new google.maps.DirectionsRenderer();
          this.directionsRenderer.setMap(this.map); // Attach the renderer to the map

          navigator.geolocation.getCurrentPosition((position) => {
            this.map = new google.maps.Map(this.$refs.map, {
              center: { lat: position.coords.latitude, lng: position.coords.longitude },
              zoom: 16,
              heading: 320,
              tilt: 47.5,
              mapId: '8fafea89cad68455',
              rotateControl: true,
              disableDefaultUI: true,
            });
            this.center.lat = position.coords.latitude;
            this.center.lng = position.coords.longitude;
            const marker = new google.maps.Marker({
              position: { lat: position.coords.latitude, lng: position.coords.longitude },
              map: this.map,
              // icon: {
              //   url: '../assets/images/buyer_map_icon.png',
              //   scaledSize: new google.maps.Size(40, 40),
              // },
            });

            marker.addListener('click', () => {
              this.$toast.success('You clicked your location!');
            });
            this.markers.forEach((markerLocation) => {
              // eslint-disable-next-line no-new
              const newMarker = new google.maps.Marker({
                position: { lat: markerLocation.lat, lng: markerLocation.lng },
                map: this.map,
                icon: {
                  // eslint-disable-next-line global-require
                  url: markerLocation.url,
                  scaledSize: new google.maps.Size(50, 50),
                },
              });
              newMarker.addListener('click', () => {
                markerLocation.clickEvent();
              });
              // const tooltipContent = `
              //   <div>
              //     <p>${farmerLocation.farmer.name}</p>
              //     <v-btn>View me</v-btn>
              //   </div>
              // `;
              //
              // const infoWindow = new google.maps.InfoWindow({
              //   content: tooltipContent,
              // });
              //
              // Add mouseover event to show the tooltip
              newMarker.addListener('mouseover', () => {
                markerLocation.mouseOverEvent(newMarker);
                // newMarker.setTitle(`${farmerLocation.farmer.name}\n\n\bPRODUCES\n${farmerLocation.farmer.farmerProduces.length > 0 ? farmerLocation.farmer.farmerProduces.map((farmerProduce) => farmerProduce.farmProduce.name).join('\n') : 'none'}`);
              });
              //
              // // Add mouseout event to close the tooltip
              newMarker.addListener('mouseout', () => {
                markerLocation.mouseExitEvent();
                // setTimeout(() => {
                //   infoWindow.close();
                // }, 3000);
              });
            });
          },
          (error) => {
            this.$toast.error(error);
            this.$toast.error(error.message);
          },
          {
            enableHighAccuracy: true,
          });
        })
        .catch((err) => {
          this.$toast.error(err.message);
        });
    },

    getCurrentView() {
      const bounds = this.map.getBounds();
      const center = this.map.getCenter();

      if (bounds && center) {
        this.$toast.show('Current Bounds:', bounds.toJSON());
        this.$toast.show('Current Center:', center.toJSON());
        alert(`Current Center: ${center.lat()}, ${center.lng()}`);
      } else {
        alert('Map bounds are not available yet.');
      }
    },

    setFocusToLocation(lat, lng) {
      this.map.panTo({ lat, lng });
    },

    calculateRoute(start, end) {
      // Calculate distance between the two coordinates
      // eslint-disable-next-line no-undef,new-cap
      this.distance = new google.maps.geometry.spherical.computeDistanceBetween(
        // eslint-disable-next-line no-undef
        new google.maps.LatLng(start.lat, start.lng),
        // eslint-disable-next-line no-undef
        new google.maps.LatLng(end.lat, end.lng),
      );

      // Request directions from the Directions Service
      this.directionsService.route(
        {
          origin: start,
          destination: end,
          // eslint-disable-next-line no-undef
          travelMode: google.maps.TravelMode.DRIVING, // Can be DRIVING, WALKING, BICYCLING, or TRANSIT
        },
        (response, status) => {
          // eslint-disable-next-line no-undef
          if (status === google.maps.DirectionsStatus.OK) {
            // Display the route on the map
            this.directionsRenderer.setDirections(response);
          } else {
            alert(`Directions request failed due to ${status}`);
          }
        },
      );
    },

  },
};
</script>

<template>
  <Default>
    <div class="tw-w-full tw-h-screen tw-grid md:tw-grid-cols-2 tw-grid-cols-1 tw-pb-24">
      <v-tabs
          v-model="tab"
          centered
          icons-and-text
          class="tw-w-full tw-h-full tw-rounded-tl-lg tw-rounded-bl-lg tw-border tw-shadow-xl"
      >
        <v-tab href="#farmers">
          Farmers
        </v-tab>
        <v-tab href="#buyers">
          Buyers
        </v-tab>
        <v-tabs-items
            v-model="tab"
        >
          <v-tab-item value="farmers">
            <div>
              <div
                  class="produce-list tw-grid md:tw-grid-cols-2 tw-grid-cols-1 tw-w-full tw-gap-5 tw-bg-gray-100"
              >
                <div
                    v-for="(farmerLocation, i) in farmersLocation"
                    v-bind:key="i"
                    class="tw-m-4 tw-p-4 tw-shadow-lg tw-border-4 tw-rounded-lg tw-bg-white"
                >
                  <div
                      class="tw-my-3 tw-flex tw-flex-row"
                  >
                    <v-icon
                        size="100px"
                    >mdi mdi-image</v-icon>
                    <div class="tw-flex tw-flex-col tw-justify-center tw-items-start">
                      <h1>{{ farmerLocation.farmer.name }}</h1>
                      <h2>
                        <v-icon>mdi-google-maps</v-icon>
                        {{ farmerLocation.customName }}</h2>
                    </div>
                  </div>
                  <h2
                      class="tw-mb-3 tw-ml-4"
                  > ⭐⭐⭐ </h2>
                  <v-btn
                      class="tw-ml-4"
                      @click="()=>{
                        setFocusToLocation(farmerLocation.latitude, farmerLocation.longitude);
                      }"
                  >
                    View
                  </v-btn>
                </div>
              </div>
            </div>
          </v-tab-item>
          <v-tab-item value="buyers">
            <h2>Under development</h2>
          </v-tab-item>
        </v-tabs-items>
      </v-tabs>
      <div class="tw-w-full tw-h-full tw-shadow-xl">
        <div class="tw-rounded-tr-lg tw-rounded-br-lg tw-border-4" style="width: 100%; height: 100%;" ref="map"></div>
      </div>
    </div>
  </Default>
</template>

<style scoped>
.produce-list {
  max-height: 100vh;
  overflow: scroll;
}
.full-width {
  width: 90vw;
}

@media (min-width: 768px) {
  .full-width {
    width: 100vw;
  }
}

@media (min-width: 1300px) {
  .full-width {
    width: 80vw;
  }
}

</style>
