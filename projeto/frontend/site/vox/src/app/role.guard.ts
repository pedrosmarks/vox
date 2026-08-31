import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, UserRole } from './services/auth.service';

/** Guarda de rota: exige login e, opcionalmente, uma das roles informadas em route.data['roles']. */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.parseUrl('/login');
  }

  const allowedRoles = route.data?.['roles'] as UserRole[] | undefined;
  if (allowedRoles?.length) {
    const role = authService.getUserRole();
    if (!role || !allowedRoles.includes(role)) {
      // Cada papel tem sua "home": ADMINISTRATOR não acessa Projetos/Audiência/
      // Comunidade/Moderação, então cai em Usuários em vez de causar loop.
      return router.parseUrl(role === 'ADMINISTRATOR' ? '/usuarios' : '/projetos');
    }
  }

  return true;
};
