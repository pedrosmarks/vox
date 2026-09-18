import 'package:flutter/material.dart';
import '../models/project.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';

class ProjetoDetalheModeracaoScreen extends StatefulWidget {
  final int projectId;
  const ProjetoDetalheModeracaoScreen({super.key, required this.projectId});

  @override
  State<ProjetoDetalheModeracaoScreen> createState() =>
      _ProjetoDetalheModeracaoScreenState();
}

class _ProjetoDetalheModeracaoScreenState
    extends State<ProjetoDetalheModeracaoScreen> {
  final _service = ProjectService();
  Project? _project;
  List<ProjectImage> _images = [];
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
      final project = await _service.getProjectById(widget.projectId);
      final images = await _service.getProjectImages(project.id);
      if (mounted) {
        setState(() {
          _project = project;
          _images = images;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _error = 'Projeto não encontrado.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _approve() async {
    final project = _project;
    if (project == null || _saving) return;
    setState(() => _saving = true);
    try {
      await _service.approveProject(project.id);
      if (mounted) Navigator.pop(context, true);
    } catch (_) {
      if (mounted) {
        setState(() => _saving = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Não foi possível aceitar o projeto.')),
        );
      }
    }
  }

  Future<void> _reject() async {
    final project = _project;
    if (project == null || _saving) return;
    final controller = TextEditingController();
    final note = await showDialog<String>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Negar projeto'),
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
      await _service.updateProjectStatus(project.id, 'REJECTED', note: note);
      if (mounted) Navigator.pop(context, true);
    } catch (_) {
      if (mounted) {
        setState(() => _saving = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Não foi possível negar o projeto.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Revisão de projeto'),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : _buildContent(context),
    );
  }

  Widget _buildContent(BuildContext context) {
    final project = _project!;
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
        Text(project.title, style: Theme.of(context).textTheme.headlineSmall),
        const SizedBox(height: 12),
        Text(project.description),
        const SizedBox(height: 24),
        _fact(context, 'Autor', '#${project.authorId}'),
        _fact(
          context,
          'Bairro',
          project.neighborhood.isEmpty ? '—' : project.neighborhood,
        ),
        _fact(
          context,
          'Custo estimado',
          'R\$ ${project.estimatedCost.toStringAsFixed(2)}',
        ),
        _fact(
          context,
          'Endereço',
          '${project.street}, ${project.number} - ${project.neighborhood}',
        ),
        _fact(
          context,
          'Período',
          '${project.startDate} até ${project.expectedEndDate}',
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
