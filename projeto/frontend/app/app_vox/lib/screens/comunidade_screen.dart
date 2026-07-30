import 'package:flutter/material.dart';
import '../models/notification.dart';
import '../services/notification_service.dart';
import '../theme/vox_app_bar.dart';

class ComunidadeScreen extends StatefulWidget {
  const ComunidadeScreen({super.key});

  @override
  State<ComunidadeScreen> createState() => _ComunidadeScreenState();
}

class _ComunidadeScreenState extends State<ComunidadeScreen> {
  final _notificationService = NotificationService();

  List<AppNotification> _notifications = [];
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
    } catch (_) {
      _error = 'Erro ao carregar notificações.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
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
              child: _notifications.isEmpty
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
                      itemCount: _notifications.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 8),
                      itemBuilder: (context, index) {
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
