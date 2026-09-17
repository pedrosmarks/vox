import 'package:flutter/material.dart';
import '../models/issue.dart';
import '../services/auth_service.dart';
import '../services/issue_service.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';

class ProblemaDetalheScreen extends StatefulWidget {
  final int issueId;
  const ProblemaDetalheScreen({super.key, required this.issueId});

  @override
  State<ProblemaDetalheScreen> createState() => _ProblemaDetalheScreenState();
}

class _ProblemaDetalheScreenState extends State<ProblemaDetalheScreen> {
  final _issueService = IssueService();
  final _projectService = ProjectService();
  final _authService = AuthService();

  IssueReport? _issue;
  List<IssueImage> _images = [];
  String? _selectedImage;
  String _categoryName = '';
  String _authorName = '';
  String _councilorName = '';

  bool _isLoading = true;
  String? _error;

  String? _role;
  int? _myId;
  bool get _isModerator => _role == 'MODERATOR' || _role == 'ADMINISTRATOR';
  bool get _isCouncilor => _role == 'COUNCILOR';
  bool get _isMine => _myId != null && _issue?.councilorId == _myId;
  bool get _isOwnedByOther =>
      _issue?.councilorId != null && _issue?.councilorId != _myId;
  bool get _canAssume => _issue != null && _issue!.councilorId == null;

  bool _linking = false;
  bool _savingStatus = false;
  String? _selectedStatus;
  final _statusNoteController = TextEditingController();

  static const _statuses = [
    'OPEN',
    'UNDER_REVIEW',
    'IN_PROGRESS',
    'FORWARDED',
    'RESOLVED',
    'REJECTED',
    'CLOSED',
  ];

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _statusNoteController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      _role = await _authService.getUserRole();
      _myId = await _authService.getUserId();
      final issue = await _issueService.getIssueById(widget.issueId);
      _issue = issue;
      _selectedStatus = issue.status;
      await Future.wait([
        _loadImages(issue.id),
        _loadCategory(issue.categoryId),
        _loadAuthor(issue.authorId),
        _loadCouncilor(issue.councilorId),
      ]);
    } catch (_) {
      _error = 'Ocorrência não encontrada ou erro ao carregar.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _loadImages(int id) async {
    try {
      final imgs = await _issueService.getIssueImages(id);
      _images = imgs;
      _selectedImage = imgs.isNotEmpty ? imgs.first.url : null;
    } catch (_) {
      _images = [];
    }
  }

  Future<void> _loadCategory(int id) async {
    try {
      final c = await _projectService.getCategoryById(id);
      _categoryName = c.name;
    } catch (_) {
      _categoryName = '';
    }
  }

  Future<void> _loadAuthor(int id) async {
    try {
      final u = await _projectService.getUserById(id);
      _authorName = (u.fullname?.isNotEmpty ?? false) ? u.fullname! : u.name;
    } catch (_) {
      _authorName = '';
    }
  }

  Future<void> _loadCouncilor(int? id) async {
    if (id == null) {
      _councilorName = '';
      return;
    }
    try {
      final u = await _authService.getCouncilorById(id);
      _councilorName = (u.fullname?.isNotEmpty ?? false) ? u.fullname! : u.name;
    } catch (_) {
      _councilorName = '';
    }
  }

  Future<void> _toggleLink() async {
    final issue = _issue;
    if (issue == null || _myId == null || _linking) return;
    // Ocorrência de outro vereador não pode ser assumida.
    if (_isOwnedByOther) return;
    setState(() => _linking = true);
    try {
      if (_isMine) {
        await _issueService.unassignCouncilor(issue);
      } else {
        await _issueService.associate(issue.id);
      }
      await _load();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Não foi possível atualizar o vínculo.'),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _linking = false);
    }
  }

  Future<void> _saveStatus() async {
    final issue = _issue;
    if (issue == null || _savingStatus || _selectedStatus == null) return;
    if (_selectedStatus == issue.status) return;
    if ((_selectedStatus == 'REJECTED' || _selectedStatus == 'CLOSED') &&
        _statusNoteController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Informe o motivo da decisão.')),
      );
      return;
    }
    setState(() => _savingStatus = true);
    try {
      await _issueService.updateIssueStatus(
        issue.id,
        _selectedStatus!,
        note: _statusNoteController.text.trim(),
      );
      await _load();
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Status atualizado.')));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Não foi possível atualizar o status.')),
        );
      }
    } finally {
      if (mounted) setState(() => _savingStatus = false);
    }
  }

  Future<void> _moderateIssue(String status) async {
    final issue = _issue;
    if (issue == null) return;
    var note = '';
    if (status == 'REJECTED') {
      final controller = TextEditingController();
      note =
          await showDialog<String>(
            context: context,
            builder: (dialogContext) => AlertDialog(
              title: const Text('Negar ocorrência'),
              content: TextField(
                controller: controller,
                maxLines: 4,
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
                    if (controller.text.trim().isEmpty) return;
                    Navigator.pop(dialogContext, controller.text.trim());
                  },
                  child: const Text('Confirmar'),
                ),
              ],
            ),
          ) ??
          '';
      controller.dispose();
      if (note.isEmpty) return;
    }
    try {
      await _issueService.updateIssueStatus(issue.id, status, note: note);
      if (mounted) Navigator.of(context).pop();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Não foi possível salvar a decisão.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(title: _issue?.title ?? 'Ocorrência'),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : _buildContent(),
    );
  }

  Widget _buildContent() {
    final i = _issue!;
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (_selectedImage != null)
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.network(
                _selectedImage!,
                height: 200,
                width: double.infinity,
                fit: BoxFit.cover,
                errorBuilder: (_, _, _) => Container(
                  height: 200,
                  color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  child: const Icon(Icons.image_not_supported),
                ),
              ),
            ),
          if (_images.length > 1)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: SizedBox(
                height: 60,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: _images.length,
                  separatorBuilder: (_, _) => const SizedBox(width: 8),
                  itemBuilder: (_, idx) {
                    final img = _images[idx];
                    return GestureDetector(
                      onTap: () => setState(() => _selectedImage = img.url),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.network(
                          img.url,
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              VoxBadgeColors.issueStatus(
                i.status,
                StatusLabels.issue(i.status),
              ),
              if (_isMine)
                VoxBadge(
                  label: 'Minha ocorrência',
                  background: const Color(0xFFDCFCE7),
                  foreground: const Color(0xFF15803D),
                ),
            ],
          ),
          const SizedBox(height: 12),
          Text(i.title, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(i.description),
          const SizedBox(height: 16),

          // Ação do vereador: assumir / desvincular (só se minha ou disponível)
          if (_isCouncilor && (_isMine || _canAssume))
            OutlinedButton.icon(
              onPressed: _linking ? null : _toggleLink,
              icon: Icon(_isMine ? Icons.check_circle : Icons.how_to_reg),
              label: Text(
                _isMine
                    ? 'Vinculada a mim (desvincular)'
                    : 'Assumir ocorrência',
              ),
            ),
          // De outro vereador: apenas indicação, sem ação.
          if (_isCouncilor && _isOwnedByOther)
            Row(
              children: [
                Icon(
                  Icons.person,
                  size: 18,
                  color: Theme.of(context).hintColor,
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    'Já atribuída a outro vereador',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ),
              ],
            ),
          const SizedBox(height: 16),

          if (_categoryName.isNotEmpty) _infoRow('Categoria', _categoryName),
          if (_authorName.isNotEmpty) _infoRow('Relatado por', _authorName),
          _infoRow(
            'Vereador responsável',
            _councilorName.isNotEmpty ? _councilorName : 'Nenhum',
          ),
          _infoRow(
            'Endereço',
            [
              if (i.street.isNotEmpty) i.street,
              if (i.number.isNotEmpty) i.number,
              if (i.neighborhood.isNotEmpty) i.neighborhood,
            ].join(', '),
          ),
          if (i.createdAt.isNotEmpty)
            _infoRow('Relatado em', _fmtDate(i.createdAt)),

          // Atualização de status (vereador/moderador)
          if (_isModerator) ...[
            const Divider(height: 32),
            Row(
              children: [
                Expanded(
                  child: FilledButton.icon(
                    onPressed: () => _moderateIssue('OPEN'),
                    icon: const Icon(Icons.check),
                    label: const Text('Aceitar'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton.icon(
                    style: FilledButton.styleFrom(
                      backgroundColor: Colors.amber,
                      foregroundColor: Colors.black87,
                    ),
                    onPressed: () => _moderateIssue('REJECTED'),
                    icon: const Icon(Icons.close),
                    label: const Text('Negar'),
                  ),
                ),
              ],
            ),
          ] else if (_isCouncilor) ...[
            const Divider(height: 32),
            Text(
              'Atualizar status',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              initialValue: _selectedStatus,
              decoration: const InputDecoration(labelText: 'Status'),
              items: _statuses
                  .map(
                    (s) => DropdownMenuItem(
                      value: s,
                      child: Text(StatusLabels.issue(s)),
                    ),
                  )
                  .toList(),
              onChanged: (v) => setState(() => _selectedStatus = v),
            ),
            const SizedBox(height: 12),
            if (_selectedStatus == 'REJECTED' || _selectedStatus == 'CLOSED')
              TextField(
                controller: _statusNoteController,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Motivo da decisão *',
                  border: OutlineInputBorder(),
                ),
              ),
            if (_selectedStatus == 'REJECTED' || _selectedStatus == 'CLOSED')
              const SizedBox(height: 12),
            FilledButton(
              onPressed: (_savingStatus || _selectedStatus == i.status)
                  ? null
                  : _saveStatus,
              child: _savingStatus
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Salvar status'),
            ),
          ],
        ],
      ),
    );
  }

  String _fmtDate(String d) {
    final parsed = DateTime.tryParse(d);
    if (parsed == null) return d;
    final dd = parsed.day.toString().padLeft(2, '0');
    final mm = parsed.month.toString().padLeft(2, '0');
    return '$dd/$mm/${parsed.year}';
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 150,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          Expanded(child: Text(value.isEmpty ? '—' : value)),
        ],
      ),
    );
  }
}
