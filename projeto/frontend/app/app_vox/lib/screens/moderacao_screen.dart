import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import '../models/issue.dart';
import '../models/project.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
import '../services/issue_service.dart';
import '../theme/vox_app_bar.dart';
import '../widgets/map_picker_field.dart';
import '../utils/fallback_categories.dart';
import '../utils/status_labels.dart';
import 'problema_detalhe_moderacao_screen.dart';
import 'projeto_detalhe_moderacao_screen.dart';

const _projectTypes = [
  {'value': 'CHAMBER', 'label': 'Câmara / Prefeitura'},
  {'value': 'CITIZEN', 'label': 'Cidadão'},
];

const _projectStatuses = [
  {'value': 'PUBLISHED', 'label': 'Publicado'},
  {'value': 'IN_VOTING', 'label': 'Em votação'},
  {'value': 'SELECTED_BY_COUNCIL', 'label': 'Selecionado pelo conselho'},
  {'value': 'APPROVED_BY_COUNCIL', 'label': 'Aprovado pelo conselho'},
  {'value': 'IN_EXECUTION', 'label': 'Em execução'},
  {'value': 'COMPLETED', 'label': 'Concluído'},
  {'value': 'ARCHIVED', 'label': 'Arquivado'},
  {'value': 'CANCELLED', 'label': 'Cancelado'},
];

/// Tela de moderação (aprovação de projetos pendentes + cadastro/oficialização
/// de projetos), equivalente a moderacao.component.ts. Acesso restrito a
/// MODERATOR/ADMINISTRATOR.
class ModeracaoScreen extends StatefulWidget {
  /// Projeto sugerido a ser pré-preenchido na aba "Novo Projeto", usado
  /// quando o moderador clica em "Tornar Oficial" na tela de detalhes.
  final Project? initialPromote;

  const ModeracaoScreen({super.key, this.initialPromote});

  @override
  State<ModeracaoScreen> createState() => _ModeracaoScreenState();
}

class _ModeracaoScreenState extends State<ModeracaoScreen>
    with SingleTickerProviderStateMixin {
  late final TabController _tabController;
  final _authService = AuthService();
  final _projectService = ProjectService();
  final _issueService = IssueService();

  List<Project> _pending = [];
  List<IssueReport> _pendingIssues = [];
  bool _isLoadingIssues = true;
  final Map<int, String> _authorNames = {};
  bool _isLoadingPending = true;
  String? _pendingError;
  int? _actionInProgress;
  String? _actionMessage;

  int? _editingProjectId;
  List<Category> _categories = fallbackCategories;

  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _neighborhoodController = TextEditingController();
  final _streetController = TextEditingController();
  final _numberController = TextEditingController();
  final _startDateController = TextEditingController();
  final _expectedEndDateController = TextEditingController();
  final _endDateController = TextEditingController();
  final _financialAnalysisController = TextEditingController();
  final _estimatedCostController = TextEditingController();
  final _approvedBudgetController = TextEditingController();

  int? _categoryId;
  String _type = 'CHAMBER';
  String _status = 'PUBLISHED';
  bool _highlighted = false;
  double? _latitude;
  double? _longitude;
  XFile? _pickedImage;
  bool _isSubmitting = false;
  String? _submitError;
  bool _submitSuccess = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadPending();
    _loadPendingIssues();
    _loadCategories();
    if (widget.initialPromote != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) _promoteToOfficial(widget.initialPromote!);
      });
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    _titleController.dispose();
    _descriptionController.dispose();
    _neighborhoodController.dispose();
    _streetController.dispose();
    _numberController.dispose();
    _startDateController.dispose();
    _expectedEndDateController.dispose();
    _endDateController.dispose();
    _financialAnalysisController.dispose();
    _estimatedCostController.dispose();
    _approvedBudgetController.dispose();
    super.dispose();
  }

  // ── Aba Pendentes ─────────────────────────────────────────

  Future<void> _loadPending() async {
    setState(() {
      _isLoadingPending = true;
      _pendingError = null;
    });
    try {
      _pending = await _projectService.getPendingProjects();
      _loadAuthorNames();
    } catch (_) {
      _pendingError = 'Erro ao carregar projetos pendentes.';
    } finally {
      if (mounted) setState(() => _isLoadingPending = false);
    }
  }

  Future<void> _loadPendingIssues() async {
    setState(() => _isLoadingIssues = true);
    try {
      _pendingIssues = await _issueService.getPendingIssues();
    } catch (_) {
      _pendingIssues = [];
    } finally {
      if (mounted) setState(() => _isLoadingIssues = false);
    }
  }

  Future<void> _loadAuthorNames() async {
    final ids = {for (final p in _pending) p.authorId};
    for (final id in ids) {
      try {
        final user = await _projectService.getUserById(id);
        final name = (user.fullname?.isNotEmpty ?? false)
            ? user.fullname!
            : user.name;
        if (mounted) setState(() => _authorNames[id] = name);
      } catch (_) {
        // ignora falha ao buscar autor individual
      }
    }
  }

  String _authorName(int id) => _authorNames[id] ?? 'Usuário #$id';

  void _promoteToOfficial(Project p) {
    setState(() {
      _editingProjectId = p.id;
      _submitSuccess = false;
      _submitError = null;
      _titleController.text = p.title;
      _descriptionController.text = p.description;
      _categoryId = p.categoryId;
      _type = 'CHAMBER';
      _status = 'PUBLISHED';
      _highlighted = false;
      _neighborhoodController.text = p.neighborhood;
      _streetController.text = p.street;
      _numberController.text = p.number;
      _latitude = p.latitude;
      _longitude = p.longitude;
      _startDateController.clear();
      _expectedEndDateController.clear();
      _endDateController.clear();
      _financialAnalysisController.clear();
      _estimatedCostController.clear();
      _approvedBudgetController.clear();
      _pickedImage = null;
    });
    _tabController.animateTo(1);
  }

  // ── Aba Novo Projeto ──────────────────────────────────────

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

  void _resetForm() {
    _editingProjectId = null;
    _titleController.clear();
    _descriptionController.clear();
    _categoryId = null;
    _type = 'CHAMBER';
    _status = 'PUBLISHED';
    _highlighted = false;
    _neighborhoodController.clear();
    _streetController.clear();
    _numberController.clear();
    _latitude = null;
    _longitude = null;
    _startDateController.clear();
    _expectedEndDateController.clear();
    _endDateController.clear();
    _financialAnalysisController.clear();
    _estimatedCostController.clear();
    _approvedBudgetController.clear();
    _pickedImage = null;
  }

  Future<void> _submit() async {
    if (_titleController.text.trim().isEmpty ||
        _descriptionController.text.trim().isEmpty ||
        _categoryId == null ||
        _startDateController.text.trim().isEmpty ||
        _expectedEndDateController.text.trim().isEmpty ||
        _latitude == null ||
        _longitude == null) {
      setState(
        () => _submitError =
            'Preencha os campos obrigatórios e selecione a localização no mapa.',
      );
      return;
    }

    final userId = await _authService.getUserId();
    if (userId == null) {
      setState(() => _submitError = 'Sessão inválida. Faça login novamente.');
      return;
    }

    setState(() {
      _isSubmitting = true;
      _submitError = null;
    });

    try {
      final municipalityId = await _authService.getMunicipalityId();
      final fields = <String, String>{
        'title': _titleController.text.trim(),
        'description': _descriptionController.text.trim(),
        'municipalityId': municipalityId.toString(),
        'authorId': userId.toString(),
        'categoryId': _categoryId.toString(),
        'type': _type,
        'status': _editingProjectId == null ? 'PUBLISHED' : _status,
        'highlighted': _highlighted.toString(),
        'isOfficial': 'true',
        'neighborhood': _neighborhoodController.text.trim(),
        'street': _streetController.text.trim(),
        'number': _numberController.text.trim(),
        'latitude': _latitude?.toString() ?? '',
        'longitude': _longitude?.toString() ?? '',
        'startDate': _apiDate(_startDateController.text),
        'expectedEndDate': _apiDate(_expectedEndDateController.text),
        if (_endDateController.text.trim().isNotEmpty)
          'endDate': _apiDate(_endDateController.text),
        if (_financialAnalysisController.text.trim().isNotEmpty)
          'financialAnalysis': _financialAnalysisController.text.trim(),
        if (_estimatedCostController.text.trim().isNotEmpty)
          'estimatedCost': _estimatedCostController.text.trim(),
        if (_approvedBudgetController.text.trim().isNotEmpty)
          'approvedBudget': _approvedBudgetController.text.trim(),
      };

      final files = <http.MultipartFile>[];
      if (_pickedImage != null) {
        files.add(
          await http.MultipartFile.fromPath('file', _pickedImage!.path),
        );
      }

      if (_editingProjectId != null) {
        await _projectService.updateProject(
          _editingProjectId!,
          fields,
          files: files,
        );
        _pending = _pending.where((p) => p.id != _editingProjectId).toList();
      } else {
        await _projectService.createProject(fields, files: files);
      }

      setState(() => _submitSuccess = true);
      _resetForm();
    } catch (_) {
      setState(() => _submitError = 'Erro ao salvar projeto. Tente novamente.');
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(
        title: 'Moderação',
        bottomHeight: 48,
        bottom: TabBar(
          controller: _tabController,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          indicatorColor: Colors.white,
          tabs: const [
            Tab(text: 'Pendentes'),
            Tab(text: 'Ocorrências'),
            Tab(text: 'Novo Projeto'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildPendingTab(),
          _buildPendingIssuesTab(),
          _buildFormTab(),
        ],
      ),
    );
  }

  Widget _buildPendingTab() {
    if (_isLoadingPending) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_pendingError != null) {
      return Center(child: Text(_pendingError!));
    }
    return RefreshIndicator(
      onRefresh: _loadPending,
      child: Column(
        children: [
          if (_actionMessage != null)
            Container(
              width: double.infinity,
              color: Colors.green.shade50,
              padding: const EdgeInsets.all(12),
              child: Text(
                _actionMessage!,
                style: const TextStyle(color: Colors.green),
              ),
            ),
          Expanded(
            child: _pending.isEmpty
                ? ListView(
                    children: const [
                      Padding(
                        padding: EdgeInsets.only(top: 80),
                        child: Center(child: Text('Nenhum projeto pendente.')),
                      ),
                    ],
                  )
                : ListView.builder(
                    padding: const EdgeInsets.all(12),
                    itemCount: _pending.length,
                    itemBuilder: (context, index) {
                      final p = _pending[index];
                      final busy = _actionInProgress == p.id;
                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        child: Padding(
                          padding: const EdgeInsets.all(12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                p.title,
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                p.description,
                                maxLines: 3,
                                overflow: TextOverflow.ellipsis,
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'Por ${_authorName(p.authorId)}',
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              Text(
                                StatusLabels.project(p.status),
                                style: Theme.of(context).textTheme.bodySmall,
                              ),
                              const SizedBox(height: 8),
                              Wrap(
                                spacing: 8,
                                children: [
                                  OutlinedButton(
                                    onPressed: busy
                                        ? null
                                        : () async {
                                            final changed =
                                                await Navigator.of(
                                                  context,
                                                ).push<bool>(
                                                  MaterialPageRoute(
                                                    builder: (_) =>
                                                        ProjetoDetalheModeracaoScreen(
                                                          projectId: p.id,
                                                        ),
                                                  ),
                                                );
                                            if (changed == true && mounted) {
                                              setState(() {
                                                _pending.removeWhere(
                                                  (item) => item.id == p.id,
                                                );
                                              });
                                            }
                                          },
                                    child: const Text('Abrir e analisar'),
                                  ),
                                  TextButton.icon(
                                    onPressed: busy
                                        ? null
                                        : () => _promoteToOfficial(p),
                                    icon: const Icon(
                                      Icons.workspace_premium_outlined,
                                    ),
                                    label: const Text('Oficializar'),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildPendingIssuesTab() {
    if (_isLoadingIssues) {
      return const Center(child: CircularProgressIndicator());
    }
    return RefreshIndicator(
      onRefresh: _loadPendingIssues,
      child: _pendingIssues.isEmpty
          ? ListView(
              children: const [
                Padding(
                  padding: EdgeInsets.only(top: 80),
                  child: Center(child: Text('Nenhuma ocorrência pendente.')),
                ),
              ],
            )
          : ListView.builder(
              padding: const EdgeInsets.all(12),
              itemCount: _pendingIssues.length,
              itemBuilder: (context, index) {
                final issue = _pendingIssues[index];
                return Card(
                  child: ListTile(
                    title: Text(issue.title),
                    subtitle: Text(
                      issue.description,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () async {
                      final changed = await Navigator.of(context).push<bool>(
                        MaterialPageRoute(
                          builder: (_) =>
                              ProblemaDetalheModeracaoScreen(issueId: issue.id),
                        ),
                      );
                      if (changed == true && mounted) {
                        await _loadPendingIssues();
                      }
                    },
                  ),
                );
              },
            ),
    );
  }

  Widget _dateField(TextEditingController controller, String label) {
    return TextField(
      controller: controller,
      readOnly: true,
      onTap: () async {
        final initial = _displayDateParser(controller.text) ?? DateTime.now();
        final selected = await showDatePicker(
          context: context,
          initialDate: initial,
          firstDate: DateTime(2000),
          lastDate: DateTime(2100),
          locale: const Locale('pt', 'BR'),
          helpText: label,
          cancelText: 'Cancelar',
          confirmText: 'Selecionar',
        );
        if (selected != null) {
          controller.text = DateFormat('dd/MM/yyyy').format(selected);
        }
      },
      decoration: InputDecoration(
        labelText: label,
        hintText: 'Selecione uma data',
        suffixIcon: const Icon(Icons.calendar_month_outlined),
      ),
    );
  }

  DateTime? _displayDateParser(String value) {
    try {
      return DateFormat('dd/MM/yyyy').parseStrict(value);
    } catch (_) {
      return DateTime.tryParse(value);
    }
  }

  String _apiDate(String value) {
    final date = _displayDateParser(value);
    return date == null ? value.trim() : DateFormat('yyyy-MM-dd').format(date);
  }

  Widget _buildFormTab() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (_editingProjectId != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Text(
              'Oficializando projeto sugerido #$_editingProjectId',
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
        TextField(
          controller: _titleController,
          decoration: const InputDecoration(labelText: 'Título *'),
        ),
        const SizedBox(height: 12),
        DropdownButtonFormField<int>(
          initialValue: _categoryId,
          decoration: const InputDecoration(labelText: 'Categoria *'),
          items: _categories
              .map((c) => DropdownMenuItem(value: c.id, child: Text(c.name)))
              .toList(),
          onChanged: (v) => setState(() => _categoryId = v),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: _descriptionController,
          decoration: const InputDecoration(labelText: 'Descrição *'),
          maxLines: 4,
        ),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _type,
          decoration: const InputDecoration(labelText: 'Tipo'),
          items: _projectTypes
              .map(
                (t) => DropdownMenuItem(
                  value: t['value'],
                  child: Text(t['label']!),
                ),
              )
              .toList(),
          onChanged: (v) => setState(() => _type = v ?? _type),
        ),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          initialValue: _status,
          decoration: const InputDecoration(labelText: 'Status'),
          items: _projectStatuses
              .map(
                (s) => DropdownMenuItem(
                  value: s['value'],
                  child: Text(s['label']!),
                ),
              )
              .toList(),
          onChanged: (v) => setState(() => _status = v ?? _status),
        ),
        const SizedBox(height: 4),
        Row(
          children: [
            Expanded(
              flex: 2,
              child: TextField(
                controller: _streetController,
                decoration: const InputDecoration(labelText: 'Rua'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: TextField(
                controller: _numberController,
                decoration: const InputDecoration(labelText: 'Número'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        TextField(
          controller: _neighborhoodController,
          decoration: const InputDecoration(labelText: 'Bairro'),
        ),
        const SizedBox(height: 12),
        MapPickerField(
          initialLatitude: _latitude,
          initialLongitude: _longitude,
          onLocationChanged: (point) => setState(() {
            _latitude = point.latitude;
            _longitude = point.longitude;
          }),
          onAddressChanged: (address) => setState(() {
            if (address.street.isNotEmpty) {
              _streetController.text = address.street;
            }
            if (address.number.isNotEmpty) {
              _numberController.text = address.number;
            }
            if (address.neighborhood.isNotEmpty) {
              _neighborhoodController.text = address.neighborhood;
            }
          }),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(child: _dateField(_startDateController, 'Data início *')),
            const SizedBox(width: 8),
            Expanded(
              child: _dateField(_expectedEndDateController, 'Previsão fim *'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        _dateField(_endDateController, 'Data de conclusão'),
        const SizedBox(height: 12),
        TextField(
          controller: _financialAnalysisController,
          decoration: const InputDecoration(labelText: 'Análise financeira'),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _estimatedCostController,
                decoration: const InputDecoration(labelText: 'Custo estimado'),
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: TextField(
                controller: _approvedBudgetController,
                decoration: const InputDecoration(
                  labelText: 'Orçamento aprovado',
                ),
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: _pickImage,
          icon: const Icon(Icons.image_outlined),
          label: Text(
            _pickedImage == null ? 'Anexar imagem' : 'Imagem selecionada',
          ),
        ),
        if (_submitSuccess)
          const Padding(
            padding: EdgeInsets.only(top: 12),
            child: Text(
              'Projeto salvo com sucesso!',
              style: TextStyle(color: Colors.green),
            ),
          ),
        if (_submitError != null)
          Padding(
            padding: const EdgeInsets.only(top: 12),
            child: Text(
              _submitError!,
              style: const TextStyle(color: Colors.red),
            ),
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
              : Text(
                  _editingProjectId != null
                      ? 'Oficializar projeto'
                      : 'Criar projeto',
                ),
        ),
      ],
    );
  }
}
