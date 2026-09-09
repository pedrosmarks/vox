import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';
import { MunicipalityService, Municipality } from '../../services/municipality.service';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cadastro.component.html',
  styleUrls: ['./cadastro.component.scss']
})
export class CadastroComponent implements OnInit {
  name = '';
  email = '';
  cpf = '';
  phone = '';
  birthDate = '';
  password = '';
  confirmPassword = '';
  municipalityId: number | null = null;
  acceptedTerms = false;

  municipalities: Municipality[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private settingsService: SettingsService,
    private municipalityService: MunicipalityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.municipalityService.getMunicipalities().subscribe({
      next: list => {
        this.municipalities = list;
        if (list.length === 1) this.municipalityId = list[0].id;
      },
      error: () => {
        // Sem lista de municípios não dá pra cadastrar; avisa o usuário.
        this.errorMessage = 'Não foi possível carregar a lista de municípios.';
      }
    });
  }

  private validate(): string | null {
    if (!this.name.trim()) return 'Informe seu nome.';
    if (!this.email.trim()) return 'Informe seu e-mail.';
    if (!this.cpf.trim()) return 'Informe seu CPF.';
    if (this.municipalityId == null) return 'Selecione seu município.';
    if (this.password.length < 6) return 'A senha deve ter pelo menos 6 caracteres.';
    if (this.password !== this.confirmPassword) return 'As senhas não coincidem.';
    if (!this.acceptedTerms) return 'É necessário aceitar os termos e a política de privacidade.';
    return null;
  }

  onSubmit(): void {
    const error = this.validate();
    if (error) {
      this.errorMessage = error;
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService
      .register({
        name: this.name.trim(),
        email: this.email.trim(),
        cpf: this.cpf.trim(),
        phone: this.phone.trim(),
        birthDate: this.birthDate || undefined,
        password: this.password,
        municipalityId: this.municipalityId!,
        acceptedTerms: true,
        acceptedPrivacyPolicy: true
      })
      .subscribe({
        next: () => this.autoLogin(),
        error: err => {
          this.isLoading = false;
          if (err.status === 409 || err.status === 400) {
            this.errorMessage =
              err?.error?.message || 'Não foi possível cadastrar. Verifique os dados (e-mail/CPF podem já existir).';
          } else {
            this.errorMessage = 'Erro ao conectar ao servidor. Tente novamente.';
          }
        }
      });
  }

  /** Após cadastrar, já entra na conta e vai para os projetos. */
  private autoLogin(): void {
    this.authService.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.authService.fetchCurrentUser().subscribe({
          next: () => {
            this.settingsService.load().subscribe({ error: () => {} });
            this.isLoading = false;
            this.router.navigate(['/projetos']);
          },
          error: () => {
            this.isLoading = false;
            this.router.navigate(['/projetos']);
          }
        });
      },
      error: () => {
        // Cadastro deu certo mas login automático falhou — manda pro login.
        this.isLoading = false;
        this.router.navigate(['/login']);
      }
    });
  }
}
