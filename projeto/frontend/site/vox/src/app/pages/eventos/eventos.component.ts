import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CivicEvent, EventCategory, EventService } from '../../services/event.service';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { AddressResult, LatLng, MapPickerComponent } from '../../components/map-picker/map-picker.component';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Municipality, MunicipalityService } from '../../services/municipality.service';

@Component({
  selector: 'app-eventos', standalone: true, imports: [CommonModule, FormsModule, NavbarComponent, MapPickerComponent],
  templateUrl: './eventos.component.html', styleUrl: './eventos.component.scss'
})
export class EventosComponent implements OnInit {
  events: CivicEvent[] = []; categories: EventCategory[] = [];
  municipalities: Municipality[] = []; selectedMunicipalityId: number | null = null;
  selectedLocation: LatLng | null = null;
  search = ''; categoryId = ''; free = false; loading = false; error = '';
  page = 0; size = 12; total = 0;
  isModerator = false; formOpen = false; editingId: number | null = null; saving = false; formError = '';
  form = { title: '', categoryId: '', description: '', price: '', startDate: '', endDate: '', neighborhood: '', street: '', number: '', latitude: '', longitude: '' };
  constructor(private service: EventService, private router: Router, private auth: AuthService, private municipalityService: MunicipalityService) {}
  ngOnInit(): void {
    this.isModerator = this.auth.getUserRole() === 'MODERATOR';
    this.selectedMunicipalityId = this.auth.getMunicipalityId();
    this.municipalityService.getMunicipalities().subscribe({ next: list => this.municipalities = list });
    this.service.getCategories().subscribe({ next: c => this.categories = c }); this.load();
  }
  eventLocation(event: CivicEvent): string {
    const address = [event.street, event.number, event.neighborhood].filter(Boolean).join(', ');
    return address || event.location || 'Local a confirmar';
  }
  load(): void {
    this.loading = true; this.error = '';
    this.service.getEvents({ search: this.search.trim() || undefined, categoryId: this.categoryId ? +this.categoryId : undefined, municipalityId: this.selectedMunicipalityId ?? undefined, free: this.free || undefined, page: this.page, size: this.size }).subscribe({
      next: result => {
        this.events = result.content ?? [];
        this.total = result.totalElements ?? 0;
        this.loading = false;
        this.loadCardImages();
      },
      error: () => { this.error = 'Não foi possível carregar os eventos.'; this.loading = false; }
    });
  }
  open(event: CivicEvent): void { this.router.navigate(['/eventos', event.id]); }
  previous(): void { if (this.page > 0) { this.page--; this.load(); } }
  next(): void { if ((this.page + 1) * this.size < this.total) { this.page++; this.load(); } }
  trackById(_: number, event: CivicEvent): number { return event.id; }
  private loadCardImages(): void {
    if (!this.events.length) return;
    forkJoin(this.events.map(event => this.service.getEventImages(event.id).pipe(catchError(() => of([]))))).subscribe(images => {
      this.events = this.events.map((event, index) => ({ ...event, images: images[index] }));
    });
  }
  newEvent(): void { this.editingId = null; this.selectedLocation = null; this.form = { title: '', categoryId: '', description: '', price: '', startDate: '', endDate: '', neighborhood: '', street: '', number: '', latitude: '', longitude: '' }; this.formError = ''; this.formOpen = true; }
  editEvent(event: CivicEvent, click: MouseEvent): void { click.stopPropagation(); this.editingId = event.id; this.selectedLocation = event.latitude != null && event.longitude != null ? { latitude: event.latitude, longitude: event.longitude } : null; this.form = { title: event.title, categoryId: String(event.categoryId), description: event.description ?? '', price: event.price?.toString() ?? '', startDate: event.startDate?.slice(0, 16) ?? '', endDate: event.endDate?.slice(0, 16) ?? '', neighborhood: event.neighborhood ?? '', street: event.street ?? '', number: event.number ?? '', latitude: event.latitude?.toString() ?? '', longitude: event.longitude?.toString() ?? '' }; this.formError = ''; this.formOpen = true; }
  onLocationChange(location: LatLng): void {
    this.selectedLocation = location;
    this.form.latitude = String(location.latitude);
    this.form.longitude = String(location.longitude);
  }
  onAddressChange(address: AddressResult): void {
    if (address.street) this.form.street = address.street;
    if (address.number) this.form.number = address.number;
    if (address.neighborhood) this.form.neighborhood = address.neighborhood;
  }
  saveEvent(): void {
    if (!this.form.title.trim() || !this.form.categoryId || !this.selectedLocation) { this.formError = 'Informe título, categoria e selecione a localização no mapa.'; return; }
    this.saving = true; this.formError = '';
    const data: Record<string, string> = { ...this.form, categoryId: this.form.categoryId, price: this.form.price, startDate: this.form.startDate ? `${this.form.startDate}:00` : '', endDate: this.form.endDate ? `${this.form.endDate}:00` : '' };
    const request = this.editingId === null ? this.service.createEvent(data) : this.service.updateEvent(this.editingId, data);
    request.subscribe({ next: () => { this.formOpen = false; this.saving = false; this.load(); }, error: () => { this.formError = 'Não foi possível salvar o evento.'; this.saving = false; } });
  }
  deleteEvent(event: CivicEvent, click: MouseEvent): void { click.stopPropagation(); if (!confirm(`Excluir "${event.title}"?`)) return; this.service.deleteEvent(event.id).subscribe({ next: () => this.load(), error: () => this.error = 'Não foi possível excluir o evento.' }); }
}
