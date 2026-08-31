import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ProjetosComponent } from './pages/projetos/projetos.component';
import { ProjetoDetalheComponent } from './pages/projeto-detalhe/projeto-detalhe.component';
import { SugestoesComponent } from './pages/sugestoes/sugestoes.component';
import { RelatarProblemaComponent } from './pages/relatar-problema/relatar-problema.component';
import { ModeracaoComponent } from './pages/moderacao/moderacao.component';
import { ComunidadeComponent } from './pages/comunidade/comunidade.component';
import { PerfilComponent } from './pages/perfil/perfil.component';
import { AudienciaComponent } from './pages/audiencia/audiencia.component';
import { AudienciaSalaComponent } from './pages/audiencia-sala/audiencia-sala.component';
import { ProblemasComponent } from './pages/problemas/problemas.component';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';
import { LogsComponent } from './pages/logs/logs.component';
import { roleGuard } from './role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'projetos', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'projetos',
    component: ProjetosComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN', 'COUNCILOR', 'MODERATOR'] }
  },
  {
    path: 'projetos/:id',
    component: ProjetoDetalheComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN', 'COUNCILOR', 'MODERATOR'] }
  },
  {
    path: 'sugestoes',
    component: SugestoesComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN'] }
  },
  {
    path: 'relatar-problema',
    component: RelatarProblemaComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN'] }
  },
  {
    path: 'problemas',
    component: ProblemasComponent,
    canActivate: [roleGuard],
    data: { roles: ['COUNCILOR'] }
  },
  {
    path: 'moderacao',
    component: ModeracaoComponent,
    canActivate: [roleGuard],
    data: { roles: ['MODERATOR'] }
  },
  {
    path: 'usuarios',
    component: UsuariosComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMINISTRATOR'] }
  },
  {
    path: 'logs',
    component: LogsComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMINISTRATOR'] }
  },
  {
    path: 'comunidade',
    component: ComunidadeComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN', 'COUNCILOR', 'MODERATOR'] }
  },
  { path: 'perfil', component: PerfilComponent, canActivate: [roleGuard] },
  {
    path: 'audiencia',
    component: AudienciaComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN', 'COUNCILOR', 'MODERATOR'] }
  },
  {
    path: 'audiencia/:id',
    component: AudienciaSalaComponent,
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN', 'COUNCILOR', 'MODERATOR'] }
  },
  { path: '**', redirectTo: 'projetos' }
];

