import 'package:flutter/material.dart';
import '../models/project.dart';
import '../models/user_profile.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
import '../services/subscription_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';
import 'moderacao_screen.dart';

class ProjetoDetalheScreen extends StatefulWidget {
  final int projectId;
  const ProjetoDetalheScreen({super.key, required this.projectId});

  @override
  State<ProjetoDetalheScreen> createState() => _ProjetoDetalheScreenState();
}

class _ProjetoDetalheScreenState extends State<ProjetoDetalheScreen> {
  final _projectService = ProjectService();
  final _authService = AuthService();
  final _subscriptionService = SubscriptionService();

  Project? _project;
  List<ProjectImage> _images = [];
  String? _selectedImage;
  String _categoryName = '';
  String _authorName = '';
  bool _isLoading = true;
  String? _error;

  String? _role;
  bool get _isModerator => _role == 'MODERATOR' || _role == 'ADMINISTRATOR';
  bool get _isCouncilor => _role == 'COUNCILOR';
  bool get _isCitizen => _role == 'CITIZEN';

  bool _signed = false;
  bool _isSigning = false;
  List<UserSummary> _councilors = [];
  bool _isCouncilorLinked = false;
  bool _isLinkingCouncilor = false;

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
      _role = await _authService.getUserRole();
      final project = await _projectService.getProjectById(widget.projectId);
      _project = project;
      await Future.wait([
        _loadImages(project.id),
        _loadCategory(project.categoryId),
        _loadAuthor(project.authorId),
        _loadSubscriptionState(project.id),
        if (_isCouncilor || _isModerator) _loadCouncilors(project.id),
      ]);
    } catch (_) {
      _error = 'Projeto não encontrado ou erro ao carregar.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _loadImages(int projectId) async {
    try {
      final images = await _projectService.getProjectImages(projectId);
      _images = images;
      _selectedImage = images.isNotEmpty ? images.first.url : null;
    } catch (_) {
      _images = [];
    }
  }

  Future<void> _loadCategory(int categoryId) async {
    try {
      final category = await _projectService.getCategoryById(categoryId);
      _categoryName = category.name;
    } catch (_) {
      _categoryName = '';
    }
  }

  Future<void> _loadAuthor(int authorId) async {
    try {
      final author = await _projectService.getUserById(authorId);
      _authorName = (author.fullname?.isNotEmpty ?? false)
          ? author.fullname!
          : author.name;
    } catch (_) {
      _authorName = '';
    }
  }

  Future<void> _loadSubscriptionState(int projectId) async {
    try {
      final subs = await _subscriptionService.getSubscriptions();
      _signed = subs.any((s) => s.type == 'PROJECT' && s.targetId == projectId);
    } catch (_) {
      _signed = false;
    }
  }

  Future<void> _loadCouncilors(int projectId) async {
    try {
      final councilors = await _projectService.getProjectCouncilors(projectId);
      final myId = await _authService.getUserId();
      if (mounted) {
        setState(() {
          _councilors = councilors;
          _isCouncilorLinked =
              myId != null && councilors.any((c) => c.id == myId);
        });
      }
    } catch (_) {
      _councilors = [];
    }
  }

  Future<void> _toggleSign() async {
    if (_project == null || _isSigning) return;
    setState(() => _isSigning = true);
    try {
      if (_signed) {
        await _subscriptionService.unsubscribeProject(_project!.id);
      } else {
        await _subscriptionService.subscribeProject(_project!.id);
      }
      if (mounted) setState(() => _signed = !_signed);
    } catch (_) {
      // ignora falha na ação
    } finally {
      if (mounted) setState(() => _isSigning = false);
    }
  }

  Future<void> _toggleCouncilorLink() async {
    if (_project == null || _isLinkingCouncilor) return;
    final myId = await _authService.getUserId();
    if (myId == null) return;
    setState(() => _isLinkingCouncilor = true);
    try {
      if (_isCouncilorLinked) {
        await _projectService.unlinkCouncilor(_project!.id, myId);
      } else {
        await _projectService.linkCouncilor(_project!.id, myId);
      }
      await _loadCouncilors(_project!.id);
    } catch (_) {
      // ignora falha na ação
    } finally {
      if (mounted) setState(() => _isLinkingCouncilor = false);
    }
  }

  void _promoteToOfficial() {
    if (_project == null) return;
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ModeracaoScreen(initialPromote: _project),
      ),
    );
  }

  String _typeLabel(Project p) => p.isOfficial || p.type == 'OFFICIAL'
      ? 'Projeto Oficial'
      : 'Projeto Sugerido';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(title: _project?.title ?? 'Projeto'),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : _buildContent(),
    );
  }

  Widget _buildContent() {
    final p = _project!;
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
                  color: Colors.grey.shade200,
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
                  itemBuilder: (_, i) {
                    final img = _images[i];
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
            children: [
              VoxBadgeColors.type(
                p.isOfficial || p.type == 'OFFICIAL',
                _typeLabel(p),
              ),
              VoxBadgeColors.projectStatus(
                p.status,
                StatusLabels.project(p.status),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(p.title, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(p.description),
          const SizedBox(height: 16),
          _buildActionButtons(p),
          const SizedBox(height: 16),
          if (_categoryName.isNotEmpty) _infoRow('Categoria', _categoryName),
          if (_authorName.isNotEmpty) _infoRow('Autor', _authorName),
          _infoRow('Endereço', '${p.street}, ${p.number} - ${p.neighborhood}'),
          if (p.startDate.isNotEmpty) _infoRow('Início', p.startDate),
          if (p.expectedEndDate.isNotEmpty)
            _infoRow('Previsão de término', p.expectedEndDate),
          if (p.endDate != null && p.endDate!.isNotEmpty)
            _infoRow('Concluído em', p.endDate!),
          _infoRow(
            'Custo estimado',
            'R\$ ${p.estimatedCost.toStringAsFixed(2)}',
          ),
          _infoRow(
            'Orçamento aprovado',
            'R\$ ${p.approvedBudget.toStringAsFixed(2)}',
          ),
          if (_councilors.isNotEmpty)
            _infoRow(
              'Vereadores responsáveis',
              _councilors.map((c) => c.fullname ?? c.name).join(', '),
            ),
        ],
      ),
    );
  }

  Widget _buildActionButtons(Project p) {
    if (_isModerator && !p.isOfficial) {
      return ElevatedButton.icon(
        onPressed: _promoteToOfficial,
        icon: const Icon(Icons.workspace_premium_outlined),
        label: const Text('Tornar Oficial'),
      );
    }
    if (_isCouncilor) {
      return OutlinedButton.icon(
        onPressed: _isLinkingCouncilor ? null : _toggleCouncilorLink,
        icon: Icon(
          _isCouncilorLinked ? Icons.check_circle : Icons.how_to_vote_outlined,
        ),
        label: Text(
          _isCouncilorLinked
              ? 'Vinculado como responsável'
              : 'Adotar este projeto',
        ),
      );
    }
    if (_isCitizen) {
      return OutlinedButton.icon(
        onPressed: _isSigning ? null : _toggleSign,
        icon: Icon(_signed ? Icons.check_circle : Icons.edit_outlined),
        label: Text(_signed ? 'Assinado' : 'Assinar'),
      );
    }
    return const SizedBox.shrink();
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 140,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
