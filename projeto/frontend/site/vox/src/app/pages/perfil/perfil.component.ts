import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserProfile } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

type ActiveTab = 'dados' | 'senha';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.scss']
})
export class PerfilComponent implements OnInit {
  activeTab: ActiveTab = 'dados';
  user: UserProfile | null = null;
  isLoading = true;
  loadError = '';

  // Formulário de dados
  editForm = { name: '', phone: '' };
  isSavingProfile = false;
  profileSuccess = '';
  profileError = '';

  // Formulário de senha
  passwordForm = { current: '', newPass: '', confirm: '' };
  isSavingPassword = false;
  passwordSuccess = '';
  passwordError = '';
  showCurrent = false;
  showNew = false;
  showConfirm = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadUser();
  }

  loadUser(): void {
    this.isLoading = true;
    this.loadError = '';
    this.authService.fetchCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.editForm.name  = user.name ?? '';
        this.editForm.phone = user.phone ?? '';
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'Não foi possível carregar os dados do perfil.';
        this.isLoading = false;
      }
    });
  }

  getRoleLabel(role: string): string {
    const map: Record<string, string> = {
      ADMINISTRATOR: 'Administrador',
      MODERATOR:     'Moderador',
      CITIZEN:       'Cidadão'
    };
    return map[role] ?? role;
  }

  getRoleClass(role: string): string {
    const map: Record<string, string> = {
      ADMINISTRATOR: 'role-admin',
      MODERATOR:     'role-mod',
      CITIZEN:       'role-citizen'
    };
    return map[role] ?? 'role-citizen';
  }

  saveProfile(): void {
    if (!this.user) return;
    if (!this.editForm.name.trim()) {
      this.profileError = 'O nome não pode ficar em branco.';
      return;
    }
    this.isSavingProfile = true;
    this.profileError = '';
    this.profileSuccess = '';

    this.authService.updateProfile(this.user.id, {
      name:  this.editForm.name.trim(),
      phone: this.editForm.phone.trim() || undefined
    }).subscribe({
      next: (updated) => {
        this.user = { ...this.user!, ...updated };
        this.profileSuccess = 'Dados atualizados com sucesso!';
        this.isSavingProfile = false;
      },
      error: () => {
        this.profileError = 'Erro ao salvar. Tente novamente.';
        this.isSavingProfile = false;
      }
    });
  }

  savePassword(): void {
    this.passwordError = '';
    this.passwordSuccess = '';

    if (!this.passwordForm.current || !this.passwordForm.newPass || !this.passwordForm.confirm) {
      this.passwordError = 'Preencha todos os campos.';
      return;
    }
    if (this.passwordForm.newPass.length < 6) {
      this.passwordError = 'A nova senha deve ter pelo menos 6 caracteres.';
      return;
    }
    if (this.passwordForm.newPass !== this.passwordForm.confirm) {
      this.passwordError = 'As senhas não coincidem.';
      return;
    }

    this.isSavingPassword = true;
    this.authService.updatePassword(this.passwordForm.current, this.passwordForm.newPass).subscribe({
      next: () => {
        this.passwordSuccess = 'Senha alterada com sucesso!';
        this.passwordForm = { current: '', newPass: '', confirm: '' };
        this.isSavingPassword = false;
      },
      error: (err) => {
        this.passwordError = err.status === 401
          ? 'Senha atual incorreta.'
          : 'Erro ao alterar senha. Tente novamente.';
        this.isSavingPassword = false;
      }
    });
  }
}
