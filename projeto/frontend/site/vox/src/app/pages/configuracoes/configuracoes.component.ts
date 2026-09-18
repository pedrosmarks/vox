import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {
  SettingsService,
  UserSettings,
  AccessibilityMode
} from '../../services/settings.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

interface ModeOption {
  value: AccessibilityMode;
  label: string;
  description: string;
  icon: string;
}

@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './configuracoes.component.html',
  styleUrls: ['./configuracoes.component.scss']
})
export class ConfiguracoesComponent implements OnInit {
  fontSize = 16;
  accessibilityMode: AccessibilityMode = 'NONE';

  isLoading = true;
  saving = false;
  message = '';
  messageOk = false;

  readonly minFont = 15;
  readonly maxFont = 30;

  readonly modes: ModeOption[] = [
    { value: 'NONE', label: 'Padrão', description: 'Aparência normal do VOX.', icon: '🎨' },
    { value: 'DARK', label: 'Modo escuro', description: 'Fundo escuro, menos brilho.', icon: '🌙' },
    { value: 'HIGH_CONTRAST', label: 'Alto contraste', description: 'Cores fortes para melhor leitura.', icon: '⬛' },
    { value: 'PROTANOPIA', label: 'Protanopia', description: 'Ajuste para deficiência ao vermelho.', icon: '🔴' },
    { value: 'DEUTERANOPIA', label: 'Deuteranopia', description: 'Ajuste para deficiência ao verde.', icon: '🟢' },
    { value: 'TRITANOPIA', label: 'Tritanopia', description: 'Ajuste para deficiência ao azul.', icon: '🔵' }
  ];

  constructor(
    private authService: AuthService,
    private settingsService: SettingsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.settingsService.load().subscribe({
      next: settings => {
        this.applyToForm(settings);
        this.isLoading = false;
      },
      error: () => {
        this.applyToForm(this.settingsService.current);
        this.isLoading = false;
      }
    });
  }

  private applyToForm(settings: UserSettings): void {
    this.fontSize = settings.fontSize;
    this.accessibilityMode = settings.accessibilityMode;
  }

  /** Pré-visualização em tempo real ao mexer no slider. */
  onFontChange(): void {
    this.settingsService.save({
      fontSize: this.fontSize,
      accessibilityMode: this.accessibilityMode
    }).subscribe({ error: () => {} });
  }

  /** Seleciona um modo — aplica na hora e persiste. */
  selectMode(mode: AccessibilityMode): void {
    this.accessibilityMode = mode;
    this.save();
  }

  save(): void {
    if (this.saving) return;
    this.saving = true;
    this.message = '';
    this.settingsService
      .save({ fontSize: this.fontSize, accessibilityMode: this.accessibilityMode })
      .subscribe({
        next: () => {
          this.saving = false;
          this.messageOk = true;
          this.message = 'Configurações salvas.';
        },
        error: () => {
          this.saving = false;
          this.messageOk = false;
          this.message = 'As mudanças foram aplicadas, mas não foi possível salvar no servidor.';
        }
      });
  }
}
