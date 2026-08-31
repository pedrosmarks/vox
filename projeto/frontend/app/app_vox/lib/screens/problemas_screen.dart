import 'package:flutter/material.dart';
import '../models/issue.dart';
import '../services/issue_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';

/// Tela do vereador para acompanhar problemas relatados pelos cidadãos do
/// município, equivalente a problemas.component.ts do site.
class ProblemasScreen extends StatefulWidget {
  const ProblemasScreen({super.key});

  @override
  State<ProblemasScreen> createState() => _ProblemasScreenState();
}

class _ProblemasScreenState extends State<ProblemasScreen> {
  final _issueService = IssueService();
  List<IssueReport> _issues = [];
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
      final issues = await _issueService.getIssues();
      if (mounted) setState(() => _issues = issues);
    } catch (_) {
      if (mounted)
        setState(() => _error = 'Erro ao carregar problemas relatados.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Problemas Relatados'),
      body: RefreshIndicator(
        onRefresh: _load,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
            ? Center(child: Text(_error!))
            : _issues.isEmpty
            ? ListView(
                children: const [
                  SizedBox(height: 120),
                  Center(
                    child: Text('Nenhum problema relatado até o momento.'),
                  ),
                ],
              )
            : ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: _issues.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (_, i) {
                  final issue = _issues[i];
                  return Card(
                    child: ListTile(
                      title: Text(issue.title),
                      subtitle: Text(issue.description),
                      trailing: VoxBadgeColors.issueStatus(
                        issue.status,
                        StatusLabels.issue(issue.status),
                      ),
                    ),
                  );
                },
              ),
      ),
    );
  }
}
