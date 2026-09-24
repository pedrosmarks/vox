import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CivicEvent, EventService } from '../../services/event.service';

@Component({ selector: 'app-evento-detalhe', standalone: true, imports: [CommonModule], templateUrl: './evento-detalhe.component.html', styleUrl: './evento-detalhe.component.scss' })
export class EventoDetalheComponent implements OnInit {
  event?: CivicEvent; loading = true; error = '';
  constructor(private route: ActivatedRoute, private service: EventService, private router: Router) {}
  ngOnInit(): void { const id = Number(this.route.snapshot.paramMap.get('id')); this.service.getEvent(id).subscribe({ next: event => { this.event = event; this.loading = false; }, error: () => { this.error = 'Evento não encontrado.'; this.loading = false; } }); }
  back(): void { this.router.navigate(['/eventos']); }
}
