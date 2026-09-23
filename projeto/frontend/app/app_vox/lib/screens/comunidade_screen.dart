import 'package:flutter/material.dart';
import '../models/project.dart';
import '../models/notification.dart';
import '../services/notification_service.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';

class ComunidadeScreen extends StatefulWidget {
  const ComunidadeScreen({super.key});

  @override
  State<ComunidadeScreen> createState() => _ComunidadeScreenState();
}

class _ComunidadeScreenState extends State<ComunidadeScreen> {
  final _notificationService = NotificationService();
  final _projectService = ProjectService();

  List<AppNotification> _notifications = [];
  List<_CommunityEvent> _projectEvents = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      _notifications = await _notificationService.getNotifications();
      final projects = await _projectService.getProjects();
      _projectEvents = projects
          .map(_eventFromProject)
          .whereType<_CommunityEvent>()
          .toList();
    } catch (_) {
      _error = 'Erro ao carregar notificações.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  _CommunityEvent? _eventFromProject(Project project) {
    final isOfficial =
        project.isOfficial ||
        project.type == 'OFFICIAL' ||
        project.type == 'CHAMBER';
    final description = switch (project.status) {
      'IN_VOTING' => '"${project.title}" entrou em votação. Sua voz importa!',
      'COMPLETED' => 'O projeto "${project.title}" foi marcado como concluído.',
      'PUBLISHED' when isOfficial =>
        'Foi publicado o projeto "${project.title}".',
      'PUBLISHED' =>
        'A sugestão "${project.title}" foi aceita e está disponível para votação.',
      'SELECTED_BY_COUNCIL' =>
        '"${project.title}" foi selecionado pelo conselho.',
      'APPROVED_BY_COUNCIL' => '"${project.title}" foi aprovado pelo conselho.',
      'IN_EXECUTION' => '"${project.title}" está em execução.',
      _ => null,
    };
    if (description == null) return null;
    final title = switch (project.status) {
      'IN_VOTING' => 'Votação aberta',
      'COMPLETED' => 'Projeto concluído',
      'PUBLISHED' when isOfficial => 'Novo projeto oficial publicado',
      'PUBLISHED' => 'Nova sugestão aprovada pela moderação',
      _ => 'Projeto mudou de status',
    };
    return _CommunityEvent(title: title, description: description);
  }

  Future<void> _markAsRead(AppNotification n) async {
    if (n.read) return;
    try {
      await _notificationService.markAsRead(n.id);
      _load();
    } catch (_) {
      // ignora falha ao marcar como lida
    }
  }

  Future<void> _markAllAsRead() async {
    try {
      await _notificationService.markAllAsRead();
      _load();
    } catch (_) {
      // ignora falha ao marcar todas como lidas
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(
        title: 'Comunidade',
        actions: [
          IconButton(
            onPressed: _markAllAsRead,
            icon: const Icon(Icons.done_all),
            tooltip: 'Marcar todas como lidas',
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : RefreshIndicator(
              onRefresh: _load,
              child: (_notifications.isEmpty && _projectEvents.isEmpty)
                  ? ListView(
                      children: const [
                        Padding(
                          padding: EdgeInsets.only(top: 80),
                          child: Center(
                            child: Text('Nenhuma notificação por aqui.'),
                          ),
                        ),
                      ],
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(12),
                      itemCount: _notifications.length + _projectEvents.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 8),
                      itemBuilder: (context, index) {
                        if (index >= _notifications.length) {
                          final event =
                              _projectEvents[index - _notifications.length];
                          return Card(
                            child: ListTile(
                              leading: const Icon(Icons.campaign_outlined),
                              title: Text(event.title),
                              subtitle: Text(event.description),
                            ),
                          );
                        }
                        final n = _notifications[index];
                        return Card(
                          color: n.read ? null : Colors.blue.shade50,
                          child: ListTile(
                            leading: Icon(
                              n.read
                                  ? Icons.notifications_none
                                  : Icons.notifications_active,
                              color: n.read ? Colors.grey : Colors.blue,
                            ),
                            title: Text(n.message),
                            subtitle: Text(n.createdAt),
                            onTap: () => _markAsRead(n),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}

class _CommunityEvent {
  final String title;
  final String description;

  const _CommunityEvent({required this.title, required this.description});
}
