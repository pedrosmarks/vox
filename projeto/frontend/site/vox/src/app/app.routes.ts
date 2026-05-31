import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ProjetosComponent } from './pages/projetos/projetos.component';
import { ProjetoDetalheComponent } from './pages/projeto-detalhe/projeto-detalhe.component';
import { SugestoesComponent } from './pages/sugestoes/sugestoes.component';
import { ModeracaoComponent } from './pages/moderacao/moderacao.component';
import { ComunidadeComponent } from './pages/comunidade/comunidade.component';
import { PerfilComponent } from './pages/perfil/perfil.component';

export const routes: Routes = [
  { path: '', redirectTo: 'projetos', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'projetos', component: ProjetosComponent },
  { path: 'projetos/:id', component: ProjetoDetalheComponent },
  { path: 'sugestoes', component: SugestoesComponent },
  { path: 'moderacao', component: ModeracaoComponent },
  { path: 'comunidade', component: ComunidadeComponent },
  { path: 'perfil', component: PerfilComponent },
  { path: '**', redirectTo: 'projetos' }
];
