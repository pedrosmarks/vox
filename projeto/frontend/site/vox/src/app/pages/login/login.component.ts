import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private settingsService: SettingsService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.email.trim() || !this.password.trim()) {
      this.errorMessage = 'Por favor, preencha todos os campos.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        // Fetch user profile to store numeric ID (needed for API calls)
        this.authService.fetchCurrentUser().subscribe({
          next: () => {
            // Carrega as preferências de acessibilidade do usuário.
            this.settingsService.load().subscribe({ error: () => {} });
            this.isLoading = false;
            this.router.navigate(['/projetos']);
          },
          error: () => {
            // Proceed even if profile fetch fails; authorId will be unavailable
            this.isLoading = false;
            this.router.navigate(['/projetos']);
          }
        });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'E-mail ou senha inválidos.';
        } else {
          this.errorMessage = 'Erro ao conectar ao servidor. Tente novamente.';
        }
      }
    });
  }
}
