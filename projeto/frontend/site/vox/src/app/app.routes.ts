import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ProjetosComponent } from './pages/projetos/projetos.component';
import { SugestoesComponent } from './pages/sugestoes/sugestoes.component';

export const routes: Routes = [
  { path: '', redirectTo: 'projetos', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'projetos', component: ProjetosComponent },
  { path: 'sugestoes', component: SugestoesComponent },
  { path: '**', redirectTo: 'projetos' }
];
