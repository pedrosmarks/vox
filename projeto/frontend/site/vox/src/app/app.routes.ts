import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { ProjetosComponent } from './pages/projetos/projetos.component';
import { ProjetoDetalheComponent } from './pages/projeto-detalhe/projeto-detalhe.component';
import { ProjetoDetalheModeracaoComponent } from './pages/projeto-detalhe-moderacao/projeto-detalhe-moderacao.component';
import { SugestoesComponent } from './pages/sugestoes/sugestoes.component';
import { RelatarProblemaComponent } from './pages/relatar-problema/relatar-problema.component';
import { ModeracaoComponent } from './pages/moderacao/moderacao.component';
import { ComunidadeComponent } from './pages/comunidade/comunidade.component';
import { PerfilComponent } from './pages/perfil/perfil.component';
import { AudienciaComponent } from './pages/audiencia/audiencia.component';
import { AudienciaSalaComponent } from './pages/audiencia-sala/audiencia-sala.component';
import { ConfiguracoesComponent } from './pages/configuracoes/configuracoes.component';
import { CadastroComponent } from './pages/cadastro/cadastro.component';
import { RecuperarSenhaComponent } from './pages/recuperar-senha/recuperar-senha.component';
import { ProblemasComponent } from './pages/problemas/problemas.component';
import { ProblemaDetalheComponent } from './pages/problema-detalhe/problema-detalhe.component';
import { ProblemaDetalheModeracaoComponent } from './pages/problema-detalhe-moderacao/problema-detalhe-moderacao.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { LogsComponent } from './pages/logs/logs.component';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';
import { roleGuard } from './role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'projetos', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'cadastro', component: CadastroComponent },
  { path: 'recuperar-senha', component: RecuperarSenhaComponent },
  { path: 'projetos', component: ProjetosComponent },
  { path: 'projetos/:id', component: ProjetoDetalheComponent },
  { path: 'sugestoes', component: SugestoesComponent },
  { path: 'relatar-problema', component: RelatarProblemaComponent },
  { path: 'problemas', component: ProblemasComponent },
  { path: 'problemas/:id', component: ProblemaDetalheComponent },
  { path: 'moderacao', component: ModeracaoComponent },
  { path: 'moderacao/projetos/:id', component: ProjetoDetalheModeracaoComponent },
  { path: 'moderacao/problemas/:id', component: ProblemaDetalheModeracaoComponent },
  { path: 'comunidade', component: ComunidadeComponent },
  { path: 'perfil', component: PerfilComponent },
  { path: 'audiencia', component: AudienciaComponent },
  { path: 'audiencia/:id', component: AudienciaSalaComponent },
  { path: 'configuracoes', component: ConfiguracoesComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [roleGuard], data: { roles: ['ADMINISTRATOR'] } },
  { path: 'logs', component: LogsComponent },
  { path: 'usuarios', component: UsuariosComponent },
  { path: '**', redirectTo: 'projetos' }
];
