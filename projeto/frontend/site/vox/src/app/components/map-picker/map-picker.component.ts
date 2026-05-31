import {
  Component,
  AfterViewInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  ElementRef,
  ViewChild,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import * as maplibregl from 'maplibre-gl';

export interface LatLng {
  latitude: number;
  longitude: number;
}

@Component({
  selector: 'app-map-picker',
  standalone: true,
  template: `
    <div class="map-picker-wrapper">
      <div #mapContainer class="map-container"></div>
      @if (!selectedLocation) {
        <p class="map-hint">Clique no mapa para selecionar a localização do projeto</p>
      }
      @if (selectedLocation) {
        <p class="map-coords">
          📍 Lat: {{ selectedLocation.latitude | number:'1.6-6' }} &nbsp;|&nbsp;
          Lng: {{ selectedLocation.longitude | number:'1.6-6' }}
        </p>
      }
    </div>
  `,
  styles: [`
    .map-picker-wrapper {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .map-container {
      width: 100%;
      height: 360px;
      border-radius: 12px;
      overflow: hidden;
      border: 2px solid #e5e7eb;
    }
    .map-hint {
      font-size: 13px;
      color: #6b7280;
      margin: 0;
      text-align: center;
    }
    .map-coords {
      font-size: 13px;
      color: #2563eb;
      margin: 0;
      text-align: center;
      font-weight: 600;
    }
  `],
  imports: [
    DecimalPipe
  ]
})
export class MapPickerComponent implements AfterViewInit, OnDestroy, OnChanges {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  /** Coordenadas iniciais do centro do mapa (padrão: Brasil) */
  @Input() initialLatitude = -15.7801;
  @Input() initialLongitude = -47.9292;
  @Input() initialZoom = 4;

  /** Permite definir o valor via binding (ex: edição de projeto existente) */
  @Input() value: LatLng | null = null;

  @Output() locationChange = new EventEmitter<LatLng>();

  selectedLocation: LatLng | null = null;

  private map!: maplibregl.Map;
  private marker: maplibregl.Marker | null = null;

  ngAfterViewInit(): void {
    this.map = new maplibregl.Map({
      container: this.mapContainer.nativeElement,
      style: 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json',
      center: [this.initialLongitude, this.initialLatitude],
      zoom: this.initialZoom
    });

    this.map.addControl(new maplibregl.NavigationControl(), 'top-right');

    this.map.on('click', (e) => {
      const { lng, lat } = e.lngLat;
      this.setMarker(lng, lat);
    });

    // Se já veio com valor preenchido, usa ele
    if (this.value) {
      this.setMarker(this.value.longitude, this.value.latitude);
      this.map.setCenter([this.value.longitude, this.value.latitude]);
      this.map.setZoom(13);
      return;
    }

    // Caso contrário, tenta centralizar na localização atual do usuário
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          this.map.flyTo({
            center: [pos.coords.longitude, pos.coords.latitude],
            zoom: 13,
            speed: 1.5
          });
        },
        () => { /* permissão negada — mantém Brasil como fallback */ }
      );
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['value'] && this.map && this.value) {
      this.setMarker(this.value.longitude, this.value.latitude);
      this.map.flyTo({ center: [this.value.longitude, this.value.latitude], zoom: 12 });
    }
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private setMarker(lng: number, lat: number): void {
    if (this.marker) {
      this.marker.remove();
    }

    this.marker = new maplibregl.Marker({ color: '#2563eb', draggable: true })
      .setLngLat([lng, lat])
      .addTo(this.map);

    this.marker.on('dragend', () => {
      const pos = this.marker!.getLngLat();
      this.emitLocation(pos.lng, pos.lat);
    });

    this.emitLocation(lng, lat);
  }

  private emitLocation(lng: number, lat: number): void {
    this.selectedLocation = { latitude: lat, longitude: lng };
    this.locationChange.emit(this.selectedLocation);
  }
}
