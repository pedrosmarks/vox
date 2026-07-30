import 'dart:async';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import '../models/project.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../theme/vox_colors.dart';
import '../utils/fallback_categories.dart';
import '../utils/status_labels.dart';

class SugestoesScreen extends StatefulWidget {
  const SugestoesScreen({super.key});

  @override
  State<SugestoesScreen> createState() => _SugestoesScreenState();
}

class _SugestoesScreenState extends State<SugestoesScreen> {
  final _authService = AuthService();
  final _projectService = ProjectService();

  List<Project> _mine = [];
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
      final userId = await _authService.getUserId();
      final projects = await _projectService.getProjects();
      _mine = userId != null
          ? projects.where((p) => p.authorId == userId).toList()
          : projects;
    } catch (_) {
      _error = 'Erro ao carregar suas sugestões.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _openForm() async {
    final created = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (_) => const _SugestaoFormScreen()),
    );
    if (created == true) _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Sugestões de projetos'),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openForm,
        backgroundColor: VoxColors.purple,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add),
        label: const Text('Nova sugestão'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? Center(child: Text(_error!))
          : RefreshIndicator(
              onRefresh: _load,
              child: _mine.isEmpty
                  ? ListView(
                      children: const [
                        Padding(
                          padding: EdgeInsets.only(top: 80),
                          child: Center(
                            child: Text('Você ainda não enviou sugestões.'),
                          ),
                        ),
                      ],
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(12),
                      itemCount: _mine.length,
                      itemBuilder: (context, index) {
                        final p = _mine[index];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: ListTile(
                            title: Text(
                              p.title,
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            subtitle: Text(
                              p.description,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: VoxBadgeColors.projectStatus(
                              p.status,
                              StatusLabels.project(p.status),
                            ),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}

class _SugestaoFormScreen extends StatefulWidget {
  const _SugestaoFormScreen();

  @override
  State<_SugestaoFormScreen> createState() => _SugestaoFormScreenState();
}

class _SugestaoFormScreenState extends State<_SugestaoFormScreen> {
  final _authService = AuthService();
  final _projectService = ProjectService();
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _streetController = TextEditingController();
  final _numberController = TextEditingController();
  final _neighborhoodController = TextEditingController();

  List<Category> _categories = fallbackCategories;
  int? _categoryId;
  XFile? _pickedImage;
  bool _isSubmitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    try {
      final categories = await _projectService.getCategories();
      if (mounted && categories.isNotEmpty) {
        setState(() => _categories = categories);
      }
    } catch (_) {
      // mantém fallbackCategories
    }
  }

  Future<void> _pickImage() async {
    final image = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (image != null) setState(() => _pickedImage = image);
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate() || _categoryId == null) {
      setState(() => _error = 'Preencha todos os campos obrigatórios.');
      return;
    }
    setState(() {
      _isSubmitting = true;
      _error = null;
    });

    try {
      final userId = await _authService.getUserId();
      final municipalityId = await _authService.getMunicipalityId();
      final fields = <String, String>{
        'municipalityId': municipalityId.toString(),
        'categoryId': _categoryId.toString(),
        'type': 'CITIZEN',
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'status': 'PENDING_APPROVAL',
        'highlighted': 'false',
        'isOfficial': 'false',
        if (userId != null) 'authorId': userId.toString(),
        if (_streetController.text.isNotEmpty)
          'street': _streetController.text.trim(),
        if (_numberController.text.isNotEmpty)
          'number': _numberController.text.trim(),
        if (_neighborhoodController.text.isNotEmpty)
          'neighborhood': _neighborhoodController.text.trim(),
      };

      final files = <http.MultipartFile>[];
      if (_pickedImage != null) {
        files.add(
          await http.MultipartFile.fromPath('file', _pickedImage!.path),
        );
      }

      await _projectService.createProject(fields, files: files);
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (_) {
      setState(() => _error = 'Erro ao enviar sugestão. Tente novamente.');
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _streetController.dispose();
    _numberController.dispose();
    _neighborhoodController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Nova sugestão'),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            TextFormField(
              controller: _titleController,
              decoration: const InputDecoration(labelText: 'Título *'),
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Obrigatório' : null,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<int>(
              initialValue: _categoryId,
              decoration: const InputDecoration(labelText: 'Categoria *'),
              items: _categories
                  .map(
                    (c) => DropdownMenuItem(value: c.id, child: Text(c.name)),
                  )
                  .toList(),
              onChanged: (v) => setState(() => _categoryId = v),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(labelText: 'Descrição *'),
              maxLines: 4,
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Obrigatório' : null,
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  flex: 2,
                  child: TextFormField(
                    controller: _streetController,
                    decoration: const InputDecoration(labelText: 'Rua'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextFormField(
                    controller: _numberController,
                    decoration: const InputDecoration(labelText: 'Número'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _neighborhoodController,
              decoration: const InputDecoration(labelText: 'Bairro'),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: _pickImage,
              icon: const Icon(Icons.image_outlined),
              label: Text(
                _pickedImage == null ? 'Anexar imagem' : 'Imagem selecionada',
              ),
            ),
            if (_error != null)
              Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Text(_error!, style: const TextStyle(color: Colors.red)),
              ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: _isSubmitting ? null : _submit,
              child: _isSubmitting
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Enviar sugestão'),
            ),
          ],
        ),
      ),
    );
  }
}
