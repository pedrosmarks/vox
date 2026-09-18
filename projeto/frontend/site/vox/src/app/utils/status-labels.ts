/**
 * Rótulos e classes de status em português, centralizados para todo o site.
 * Cobre todos os status de projeto e ocorrência definidos no API.md.
 */

const PROJECT_LABELS: Record<string, string> = {
  PENDING_APPROVAL: 'Aguardando aprovação',
  IN_ANALYSIS: 'Em análise',
  REJECTED: 'Rejeitado',
  PUBLISHED: 'Publicado',
  IN_VOTING: 'Em votação',
  SELECTED_BY_COUNCIL: 'Selecionado pelo conselho',
  APPROVED_BY_COUNCIL: 'Aprovado pelo conselho',
  APPROVED: 'Aprovado',
  IN_EXECUTION: 'Em execução',
  COMPLETED: 'Concluído',
  ARCHIVED: 'Arquivado',
  CANCELLED: 'Cancelado'
};

const ISSUE_LABELS: Record<string, string> = {
  OPEN: 'Aberta',
  PENDING_APPROVAL: 'Aguardando aprovação',
  IN_ANALYSIS: 'Em análise',
  UNDER_REVIEW: 'Em revisão',
  IN_PROGRESS: 'Em andamento',
  FORWARDED: 'Encaminhada',
  APPROVED: 'Aprovada',
  RESOLVED: 'Resolvida',
  REJECTED: 'Rejeitada',
  CLOSED: 'Encerrada'
};

/** Classe CSS de cor por status (usa as classes já existentes nos SCSS). */
const STATUS_CLASSES: Record<string, string> = {
  PENDING_APPROVAL: 'status-analise',
  IN_ANALYSIS: 'status-analise',
  OPEN: 'status-analise',
  UNDER_REVIEW: 'status-analise',
  IN_VOTING: 'status-votacao',
  IN_PROGRESS: 'status-votacao',
  FORWARDED: 'status-votacao',
  PUBLISHED: 'status-aprovado',
  SELECTED_BY_COUNCIL: 'status-aprovado',
  APPROVED_BY_COUNCIL: 'status-aprovado',
  APPROVED: 'status-aprovado',
  IN_EXECUTION: 'status-votacao',
  RESOLVED: 'status-aprovado',
  COMPLETED: 'status-concluido',
  ARCHIVED: 'status-concluido',
  CLOSED: 'status-concluido',
  REJECTED: 'status-rejeitado',
  CANCELLED: 'status-rejeitado'
};

export function projectStatusLabel(status: string): string {
  return PROJECT_LABELS[status] ?? status;
}

export function issueStatusLabel(status: string): string {
  return ISSUE_LABELS[status] ?? status;
}

export function statusClass(status: string): string {
  return STATUS_CLASSES[status] ?? 'status-analise';
}
