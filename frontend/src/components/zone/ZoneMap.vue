<template>
  <div :id="mapId" class="tw-h-96 tw-rounded-b-xl tw-overflow-hidden"></div>
</template>

<script>
export default {
  name: 'ZoneMap',
  props: {
    center: {
      type: Array,
      default: () => [36.8219, -1.2921],
    },
    radiusKm: {
      type: Number,
      default: null,
    },
    zones: {
      type: Array,
      default: () => [],
    },
    selectedZone: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      mapId: `map-${Math.random().toString(36).substr(2, 9)}`,
      mapView: null,
      Map: null,
      MapView: null,
      Graphic: null,
      Circle: null,
      Point: null,
    };
  },
  mounted() {
    this.loadArcGIS()
      .then(() => {
        this.initializeMap();
      })
      .catch(() => {
        // eslint-disable-next-line no-console
      });
  },
  watch: {
    center() {
      this.updatePreview();
    },
    radiusKm() {
      this.updatePreview();
    },
    zones() {
      this.displayZones();
    },
    selectedZone() {
      this.displayZones();
    },
  },
  methods: {
    async loadArcGIS() {
      if (this.Map && this.MapView) return; // already loaded
      if (!window.require) {
        await new Promise((resolve, reject) => {
          const script = document.createElement('script');
          script.src = 'https://js.arcgis.com/4.25/init.js';
          script.onload = resolve;
          script.onerror = reject;
          document.head.appendChild(script);
          const link = document.createElement('link');
          link.rel = 'stylesheet';
          link.href = 'https://js.arcgis.com/4.25/esri/themes/light/main.css';
          document.head.appendChild(link);
        });
      }
      await new Promise((resolve, reject) => {
        try {
          window.require([
            'esri/Map',
            'esri/views/MapView',
            'esri/Graphic',
            'esri/geometry/Circle',
            'esri/geometry/Point',
          ], (Map, MapView, Graphic, Circle, Point) => {
            this.Map = Map;
            this.MapView = MapView;
            this.Graphic = Graphic;
            this.Circle = Circle;
            this.Point = Point;
            resolve();
          });
        } catch (err) {
          reject(err);
        }
      });
    },
    initializeMap() {
      if (this.mapView) return; // already initialized
      if (!this.Map || !this.MapView) {
        // Modules not yet ready, retry shortly
        setTimeout(this.initializeMap, 60);
        return;
      }
      const fallbackCenter = Array.isArray(this.center) && this.center[0] && this.center[1]
        ? this.center
        : [36.8219, -1.2921]; // Nairobi default
      const map = new this.Map({ basemap: 'streets-navigation-vector' });
      this.mapView = new this.MapView({
        container: this.mapId,
        map,
        center: fallbackCenter,
        zoom: this.zones.length ? 8 : 10,
        popupEnabled: true,
        constraints: { snapToZoom: false },
      });
      this.mapView.on('click', (event) => {
        this.$emit('click', {
          latitude: event.mapPoint.latitude,
          longitude: event.mapPoint.longitude,
        });
      });
      if (this.zones.length) {
        this.displayZones();
      } else {
        this.updatePreview();
      }
    },
    updatePreview() {
      if (!this.mapView) return;
      const valid = Array.isArray(this.center) && this.center[0] && this.center[1];
      const coords = valid ? this.center : [36.8219, -1.2921];
      this.mapView.graphics.removeAll();
      const center = new this.Point({
        longitude: coords[0],
        latitude: coords[1],
      });
      const centerGraphic = new this.Graphic({
        geometry: center,
        symbol: {
          type: 'simple-marker',
          color: 'red',
          size: 8,
        },
      });
      this.mapView.graphics.add(centerGraphic);
      if (this.radiusKm) {
        const circle = new this.Circle({
          center,
          radius: this.radiusKm,
          radiusUnit: 'kilometers',
        });
        const circleGraphic = new this.Graphic({
          geometry: circle,
          symbol: {
            type: 'simple-fill',
            color: [0, 100, 255, 0.1],
            outline: { color: [0, 100, 255, 0.8], width: 2 },
          },
        });
        this.mapView.graphics.add(circleGraphic);
        this.mapView.goTo(circle.extent);
      } else {
        this.mapView.goTo({ center, zoom: 12 }).catch(() => {});
      }
    },
    displayZones() {
      if (!this.mapView) return;
      this.mapView.graphics.removeAll();
      this.zones.forEach((zone, index) => {
        const center = new this.Point({
          longitude: zone.centerLongitude,
          latitude: zone.centerLatitude,
        });
        const circle = new this.Circle({
          center,
          radius: zone.radiusKm,
          radiusUnit: 'kilometers',
        });
        const colors = [
          [255, 0, 0, 0.2], [0, 255, 0, 0.2], [0, 0, 255, 0.2],
          [255, 255, 0, 0.2], [255, 0, 255, 0.2], [0, 255, 255, 0.2],
        ];
        const isSelected = this.selectedZone && this.selectedZone.id === zone.id;
        const circleGraphic = new this.Graphic({
          geometry: circle,
          symbol: {
            type: 'simple-fill',
            color: isSelected ? [255, 165, 0, 0.3] : colors[index % colors.length],
            outline: {
              color: isSelected ? [255, 165, 0, 0.8] : colors[index % colors.length].map((c) => (c === 0.2 ? 0.8 : c)),
              width: 2,
            },
          },
          attributes: zone,
        });
        this.mapView.graphics.add(circleGraphic);
        const centerGraphic = new this.Graphic({
          geometry: center,
          symbol: {
            type: 'simple-marker',
            color: isSelected ? 'orange' : 'red',
            size: 6,
          },
        });
        this.mapView.graphics.add(centerGraphic);
      });
      if (this.zones.length > 0) {
        this.mapView.goTo(this.mapView.graphics.items).catch(() => {});
      }
    },
    centerOnZone(zone) {
      if (!this.mapView || !zone) return;
      const center = new this.Point({
        longitude: zone.centerLongitude,
        latitude: zone.centerLatitude,
      });
      this.mapView.goTo({ center, zoom: 12 });
    },
    resize() {
      if (this.mapView && typeof this.mapView.resize === 'function') {
        setTimeout(() => {
          this.mapView.resize();
        }, 100);
      }
    },
    clearGraphics() {
      if (this.mapView) {
        this.mapView.graphics.removeAll();
      }
    },
    destroy() {
      if (this.mapView) {
        this.mapView.destroy();
        this.mapView = null;
      }
    },
  },
  beforeDestroy() {
    this.destroy();
  },
};
</script>
