import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

/** Tela de logs/auditoria do administrador — em construção. */
@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    if (!this.authService.isLoggedIn() || role !== 'ADMINISTRATOR') {
      this.router.navigate(['/projetos']);
    }
  }
}
