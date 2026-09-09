import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService, UserRole } from '../../services/auth.service';
import { SettingsService } from '../../services/settings.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  isAdmin = false;
  isModerator = false;
  isCouncilor = false;
  isCitizen = false;
  menuOpen = false;

  constructor(
    private authService: AuthService,
    private settingsService: SettingsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role: UserRole | null = this.authService.getUserRole();
    this.isAdmin = role === 'ADMINISTRATOR';
    // Moderação é para MODERATOR (o admin tem telas próprias).
    this.isModerator = role === 'MODERATOR';
    this.isCouncilor = role === 'COUNCILOR';
    this.isCitizen = role === 'CITIZEN' || role == null;
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.menuOpen = false;
    }
  }

  logout(): void {
    this.authService.logout();
    this.settingsService.reset();
    this.router.navigate(['/login']);
  }
}
