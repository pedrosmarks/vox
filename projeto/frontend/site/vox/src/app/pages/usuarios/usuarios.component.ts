import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, CreateUserPayload, UserProfile, UserRole } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

type ManagedRole = 'MODERATOR' | 'COUNCILOR';

/** Tela do administrador para CRUD de moderadores e vereadores. */
@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.scss']
})
export class UsuariosComponent implements OnInit {
  activeTab: ManagedRole = 'MODERATOR';

  users: UserProfile[] = [];
  isLoading = true;
  loadError = '';

  showForm = false;
  editingId: number | null = null;
  isSubmitting = false;
  submitError = '';

  form = {
    name: '',
    email: '',
    cpf: '',
    phone: '',
    password: '',
    birthDate: ''
  };

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    if (!this.authService.isLoggedIn() || role !== 'ADMINISTRATOR') {
      this.router.navigate(['/projetos']);
      return;
    }
    this.loadUsers();
  }

  selectTab(tab: ManagedRole): void {
    this.activeTab = tab;
    this.showForm = false;
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.loadError = '';
    this.authService.getUsersByRole(this.activeTab as UserRole).subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'Erro ao carregar usuários.';
        this.isLoading = false;
      }
    });
  }

  openCreateForm(): void {
    this.editingId = null;
    this.submitError = '';
    this.form = { name: '', email: '', cpf: '', phone: '', password: '', birthDate: '' };
    this.showForm = true;
  }

  openEditForm(user: UserProfile): void {
    this.editingId = user.id;
    this.submitError = '';
    this.form = {
      name: user.name ?? '',
      email: user.email ?? '',
      cpf: user.cpf ?? '',
      phone: user.phone ?? '',
      password: '',
      birthDate: user.birthDate ?? ''
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.submitError = '';
  }

  saveUser(): void {
    if (!this.form.name.trim() || !this.form.email.trim() || !this.form.cpf.trim()) {
      this.submitError = 'Preencha os campos obrigatórios.';
      return;
    }
    if (!this.editingId && !this.form.password.trim()) {
      this.submitError = 'Informe uma senha para o novo usuário.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';

    if (this.editingId) {
      this.authService.updateUser(this.editingId, {
        name: this.form.name.trim(),
        email: this.form.email.trim(),
        cpf: this.form.cpf.trim(),
        phone: this.form.phone.trim() || undefined,
        birthDate: this.form.birthDate || undefined,
        role: this.activeTab,
        municipalityId: this.authService.getMunicipalityId()
      }).subscribe({
        next: () => {
          this.isSubmitting = false;
          this.showForm = false;
          this.loadUsers();
        },
        error: () => {
          this.isSubmitting = false;
          this.submitError = 'Erro ao atualizar usuário.';
        }
      });
      return;
    }

    const payload: CreateUserPayload = {
      name: this.form.name.trim(),
      email: this.form.email.trim(),
      cpf: this.form.cpf.trim(),
      phone: this.form.phone.trim() || undefined,
      password: this.form.password,
      birthDate: this.form.birthDate || undefined,
      role: this.activeTab,
      municipalityId: this.authService.getMunicipalityId(),
      acceptedTerms: true,
      acceptedPrivacyPolicy: true
    };

    this.authService.createUser(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showForm = false;
        this.loadUsers();
      },
      error: () => {
        this.isSubmitting = false;
        this.submitError = 'Erro ao criar usuário. Verifique os dados informados.';
      }
    });
  }

  deleteUser(user: UserProfile): void {
    if (!confirm(`Remover ${user.name}? Esta ação não pode ser desfeita.`)) return;
    this.authService.deleteUser(user.id).subscribe({
      next: () => { this.users = this.users.filter(u => u.id !== user.id); },
      error: () => { this.loadError = 'Erro ao remover usuário.'; }
    });
  }

  get roleLabel(): string {
    return this.activeTab === 'MODERATOR' ? 'Moderador' : 'Vereador';
  }
}
