import 'package:flutter/material.dart';
import '../models/project.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';

class ProjetoDetalheScreen extends StatefulWidget {
  final int projectId;
  const ProjetoDetalheScreen({super.key, required this.projectId});

  @override
  State<ProjetoDetalheScreen> createState() => _ProjetoDetalheScreenState();
}

class _ProjetoDetalheScreenState extends State<ProjetoDetalheScreen> {
  final _projectService = ProjectService();

  Project? _project;
  List<ProjectImage> _images = [];
  String? _selectedImage;
  String _categoryName = '';
  String _authorName = '';
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
      final project = await _projectService.getProjectById(widget.projectId);
      _project = project;
      await Future.wait([
        _loadImages(project.id),
        _loadCategory(project.categoryId),
        _loadAuthor(project.authorId),
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
        ],
      ),
    );
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
