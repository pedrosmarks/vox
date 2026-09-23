import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-recuperar-senha',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recuperar-senha.component.html',
  styleUrls: ['./recuperar-senha.component.scss']
})
export class RecuperarSenhaComponent {
  // Etapa 1: pedir token por e-mail. Etapa 2: redefinir com token + nova senha.
  step: 1 | 2 = 1;

  email = '';
  token = '';
  newPassword = '';
  confirmPassword = '';

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /** Etapa 1 — envia o pedido de recuperação para o e-mail. */
  requestReset(): void {
    if (!this.email.trim()) {
      this.errorMessage = 'Informe seu e-mail.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.forgotPassword(this.email.trim()).subscribe({
      next: () => {
        this.isLoading = false;
        this.step = 2;
        this.successMessage =
          'Se o e-mail estiver cadastrado, você receberá um token de recuperação. Informe-o abaixo com a nova senha.';
      },
      error: () => {
        this.isLoading = false;
        // Não revela se o e-mail existe; segue para a etapa 2 mesmo assim.
        this.step = 2;
        this.successMessage =
          'Se o e-mail estiver cadastrado, você receberá um token de recuperação. Informe-o abaixo com a nova senha.';
      }
    });
  }

  /** Etapa 2 — redefine a senha usando o token recebido. */
  resetPassword(): void {
    if (!this.token.trim()) {
      this.errorMessage = 'Informe o token de recuperação.';
      return;
    }
    if (this.newPassword.length < 6) {
      this.errorMessage = 'A nova senha deve ter pelo menos 6 caracteres.';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'As senhas não coincidem.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resetPassword(this.token.trim(), this.newPassword).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Senha redefinida com sucesso! Redirecionando para o login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 400 || err.status === 404) {
          this.errorMessage = 'Token inválido ou expirado. Solicite um novo.';
        } else {
          this.errorMessage = 'Erro ao redefinir a senha. Tente novamente.';
        }
      }
    });
  }
}
