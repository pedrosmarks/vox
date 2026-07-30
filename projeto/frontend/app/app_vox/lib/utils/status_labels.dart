/// Mapeia os status de backend para rótulos amigáveis, espelhando o site Angular.
class StatusLabels {
  static const Map<String, String> _project = {
    'PENDING_APPROVAL': 'Em análise',
    'IN_VOTING': 'Em votação',
    'APPROVED': 'Aprovado',
    'REJECTED': 'Rejeitado',
    'IN_ANALYSIS': 'Em análise',
    'COMPLETED': 'Concluído',
  };

  static const Map<String, String> _issue = {
    'PENDING_APPROVAL': 'Em análise',
    'APPROVED': 'Aprovada',
    'REJECTED': 'Rejeitada',
    'IN_ANALYSIS': 'Em análise',
    'RESOLVED': 'Resolvida',
  };

  static String project(String status) => _project[status] ?? status;
  static String issue(String status) => _issue[status] ?? status;
}
