import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../theme/vox_app_bar.dart';

/// Tela do administrador para auditoria de ações do sistema, equivalente a
/// logs.component.ts do site. Endpoint /api/logs ainda não documentado no
/// backend — tela trata erro graciosamente.
class LogsScreen extends StatefulWidget {
  const LogsScreen({super.key});

  @override
  State<LogsScreen> createState() => _LogsScreenState();
}

class _LogsScreenState extends State<LogsScreen> {
  final _authService = AuthService();
  List<Map<String, dynamic>> _logs = [];
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
      final logs = await _authService.getLogs();
      if (mounted) setState(() => _logs = logs);
    } catch (_) {
      if (mounted) {
        setState(
          () => _error =
              'Não foi possível carregar os logs. Verifique se o endpoint /api/logs está disponível no backend.',
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Logs do Sistema'),
      body: RefreshIndicator(
        onRefresh: _load,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
            ? ListView(
                children: [
                  const SizedBox(height: 80),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 24),
                    child: Text(_error!, textAlign: TextAlign.center),
                  ),
                ],
              )
            : _logs.isEmpty
            ? ListView(
                children: const [
                  SizedBox(height: 120),
                  Center(child: Text('Nenhum log registrado.')),
                ],
              )
            : ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: _logs.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (_, i) {
                  final log = _logs[i];
                  return Card(
                    child: ListTile(
                      title: Text(log['action']?.toString() ?? '—'),
                      subtitle: Text(
                        [
                          if (log['userName'] != null) '👤 ${log['userName']}',
                          if (log['entity'] != null)
                            '📄 ${log['entity']}${log['entityId'] != null ? ' #${log['entityId']}' : ''}',
                          if (log['createdAt'] != null) '${log['createdAt']}',
                        ].join(' · '),
                      ),
                    ),
                  );
                },
              ),
      ),
    );
  }
}
