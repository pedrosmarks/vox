import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { SettingsService } from './services/settings.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: []
})
export class App implements OnInit {
  constructor(
    private authService: AuthService,
    private settingsService: SettingsService
  ) {}

  ngOnInit(): void {
    // Ao iniciar, se já estiver logado, carrega e aplica as preferências
    // de acessibilidade/fonte salvas no backend.
    if (this.authService.isLoggedIn()) {
      this.settingsService.load().subscribe({ error: () => {} });
    }
  }
}
