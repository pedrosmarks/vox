/// Mapeia os status de backend para rótulos amigáveis em português,
/// espelhando o site Angular. Cobre todos os status do API.md.
class StatusLabels {
  static const Map<String, String> _project = {
    'PENDING_APPROVAL': 'Aguardando aprovação',
    'IN_ANALYSIS': 'Em análise',
    'REJECTED': 'Rejeitado',
    'PUBLISHED': 'Publicado',
    'IN_VOTING': 'Em votação',
    'SELECTED_BY_COUNCIL': 'Selecionado pelo conselho',
    'APPROVED_BY_COUNCIL': 'Aprovado pelo conselho',
    'APPROVED': 'Aprovado',
    'IN_EXECUTION': 'Em execução',
    'COMPLETED': 'Concluído',
    'ARCHIVED': 'Arquivado',
    'CANCELLED': 'Cancelado',
  };

  static const Map<String, String> _issue = {
    'OPEN': 'Aberta',
    'PENDING_APPROVAL': 'Aguardando aprovação',
    'IN_ANALYSIS': 'Em análise',
    'UNDER_REVIEW': 'Em revisão',
    'IN_PROGRESS': 'Em andamento',
    'FORWARDED': 'Encaminhada',
    'APPROVED': 'Aprovada',
    'RESOLVED': 'Resolvida',
    'REJECTED': 'Rejeitada',
    'CLOSED': 'Encerrada',
  };

  static String project(String status) => _project[status] ?? status;
  static String issue(String status) => _issue[status] ?? status;
}
