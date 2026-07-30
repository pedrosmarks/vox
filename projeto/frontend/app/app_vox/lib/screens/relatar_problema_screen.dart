import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import '../models/issue.dart';
import '../models/project.dart';
import '../models/user_profile.dart';
import '../services/auth_service.dart';
import '../services/issue_service.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/fallback_categories.dart';
import '../utils/status_labels.dart';

class RelatarProblemaScreen extends StatefulWidget {
  const RelatarProblemaScreen({super.key});

  @override
  State<RelatarProblemaScreen> createState() => _RelatarProblemaScreenState();
}

class _RelatarProblemaScreenState extends State<RelatarProblemaScreen> {
  final _issueService = IssueService();

  List<IssueReport> _mine = [];
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
      _mine = await _issueService.getMyIssues();
    } catch (_) {
      _error = 'Erro ao carregar suas ocorrências.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _openForm() async {
    final created = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (_) => const _ProblemaFormScreen()),
    );
    if (created == true) _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Relatar problema'),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openForm,
        icon: const Icon(Icons.add),
        label: const Text('Nova ocorrência'),
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
                            child: Text('Você ainda não relatou problemas.'),
                          ),
                        ),
                      ],
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(12),
                      itemCount: _mine.length,
                      itemBuilder: (context, index) {
                        final i = _mine[index];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 12),
                          child: ListTile(
                            title: Text(
                              i.title,
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            subtitle: Text(
                              i.description,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: VoxBadgeColors.issueStatus(
                              i.status,
                              StatusLabels.issue(i.status),
                            ),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}

class _ProblemaFormScreen extends StatefulWidget {
  const _ProblemaFormScreen();

  @override
  State<_ProblemaFormScreen> createState() => _ProblemaFormScreenState();
}

class _ProblemaFormScreenState extends State<_ProblemaFormScreen> {
  final _authService = AuthService();
  final _projectService = ProjectService();
  final _issueService = IssueService();
  final _formKey = GlobalKey<FormState>();

  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _streetController = TextEditingController();
  final _numberController = TextEditingController();
  final _neighborhoodController = TextEditingController();

  List<Category> _categories = fallbackCategories;
  List<UserProfile> _councilors = [];
  int? _categoryId;
  int? _councilorId;
  XFile? _pickedImage;
  bool _isSubmitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadCategories();
    _loadCouncilors();
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

  Future<void> _loadCouncilors() async {
    try {
      final councilors = await _authService.getCouncilors();
      if (mounted) setState(() => _councilors = councilors);
    } catch (_) {
      if (mounted) setState(() => _councilors = []);
    }
  }

  Future<void> _pickImage() async {
    final image = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (image != null) setState(() => _pickedImage = image);
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate() ||
        _categoryId == null ||
        _councilorId == null) {
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
        'councilorId': _councilorId.toString(),
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'status': 'PENDING_APPROVAL',
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

      await _issueService.createIssue(fields, files: files);
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (_) {
      setState(() => _error = 'Erro ao enviar ocorrência. Tente novamente.');
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
      appBar: const VoxAppBar(title: 'Nova ocorrência'),
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
            DropdownButtonFormField<int>(
              initialValue: _councilorId,
              decoration: const InputDecoration(
                labelText: 'Vereador responsável *',
              ),
              items: _councilors
                  .map(
                    (c) => DropdownMenuItem(
                      value: c.id,
                      child: Text(
                        c.fullname?.isNotEmpty == true ? c.fullname! : c.name,
                      ),
                    ),
                  )
                  .toList(),
              onChanged: (v) => setState(() => _councilorId = v),
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
                  : const Text('Enviar ocorrência'),
            ),
          ],
        ),
      ),
    );
  }
}
