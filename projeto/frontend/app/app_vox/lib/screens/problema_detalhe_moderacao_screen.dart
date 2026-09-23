import 'package:flutter/material.dart';
import '../models/issue.dart';
import '../services/issue_service.dart';
import '../theme/vox_app_bar.dart';

class ProblemaDetalheModeracaoScreen extends StatefulWidget {
  final int issueId;
  const ProblemaDetalheModeracaoScreen({super.key, required this.issueId});

  @override
  State<ProblemaDetalheModeracaoScreen> createState() =>
      _ProblemaDetalheModeracaoScreenState();
}

class _ProblemaDetalheModeracaoScreenState
    extends State<ProblemaDetalheModeracaoScreen> {
  final _service = IssueService();
  IssueReport? _issue;
  List<IssueImage> _images = [];
  bool _loading = true;
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final issue = await _service.getIssueById(widget.issueId);
      final images = await _service.getIssueImages(issue.id);
      if (mounted) {
        setState(() {
          _issue = issue;
          _images = images;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _error = 'Ocorrência não encontrada.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _approve() async {
    final issue = _issue;
    if (issue == null || _saving) return;
    setState(() => _saving = true);
    try {
      await _service.approveIssue(issue.id);
      if (mounted) Navigator.pop(context, true);
    } catch (_) {
      _showError('Não foi possível aceitar a ocorrência.');
    }
  }

  Future<void> _reject() async {
    final issue = _issue;
    if (issue == null || _saving) return;
    final controller = TextEditingController();
    final note = await showDialog<String>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Negar ocorrência'),
        content: TextField(
          controller: controller,
          maxLines: 5,
          decoration: const InputDecoration(
            labelText: 'Motivo da negativa *',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                Navigator.pop(dialogContext, controller.text.trim());
              }
            },
            child: const Text('Confirmar'),
          ),
        ],
      ),
    );
    controller.dispose();
    if (note == null) return;
    setState(() => _saving = true);
    try {
      await _service.updateIssueStatus(issue.id, 'REJECTED', note: note);
      if (mounted) Navigator.pop(context, true);
    } catch (_) {
      _showError('Não foi possível negar a ocorrência.');
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    setState(() => _saving = false);
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Revisão de ocorrência'),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : _buildContent(context),
    );
  }

  Widget _buildContent(BuildContext context) {
    final issue = _issue!;
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        if (_images.isNotEmpty) ...[
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: Image.network(
              _images.first.url,
              height: 240,
              fit: BoxFit.cover,
              errorBuilder: (_, _, _) =>
                  const SizedBox(height: 240, child: Icon(Icons.broken_image)),
            ),
          ),
          const SizedBox(height: 16),
        ],
        Text(issue.title, style: Theme.of(context).textTheme.headlineSmall),
        const SizedBox(height: 12),
        Text(issue.description),
        const SizedBox(height: 24),
        _fact(context, 'Relatado por', '#${issue.authorId}'),
        _fact(
          context,
          'Bairro',
          issue.neighborhood.isEmpty ? '—' : issue.neighborhood,
        ),
        _fact(context, 'Status recebido', issue.status),
        _fact(
          context,
          'Endereço',
          '${issue.street}, ${issue.number} - ${issue.neighborhood}',
        ),
        _fact(
          context,
          'Vereador responsável',
          issue.councilorId?.toString() ?? 'Nenhum',
        ),
        const SizedBox(height: 28),
        Row(
          children: [
            Expanded(
              child: FilledButton.icon(
                onPressed: _saving ? null : _approve,
                icon: const Icon(Icons.check),
                label: const Text('Aceitar'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: _saving ? null : _reject,
                icon: const Icon(Icons.close),
                label: const Text('Negar'),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _fact(BuildContext context, String label, String value) => Container(
    margin: const EdgeInsets.only(bottom: 12),
    padding: const EdgeInsets.all(14),
    decoration: BoxDecoration(
      color: Theme.of(context).colorScheme.surface,
      border: Border.all(color: Theme.of(context).colorScheme.outline),
      borderRadius: BorderRadius.circular(10),
    ),
    child: Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Expanded(child: Text(label)),
        Flexible(child: Text(value, textAlign: TextAlign.end)),
      ],
    ),
  );
}
