import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:share_plus/share_plus.dart';
import '../models/project.dart';
import '../models/user_profile.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
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
  int _signatureCount = 0;
  List<UserSummary> _councilors = [];
  bool _isCouncilorLinked = false;
  bool _isLinkingCouncilor = false;
  bool _isExporting = false;

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
        _loadSignatureState(project.id),
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

  Future<void> _loadSignatureState(int projectId) async {
    try {
      _signed = await _projectService.hasSignedProject(projectId);
    } catch (_) {
      _signed = false;
    }
    try {
      _signatureCount = await _projectService.getSignatureCount(projectId);
    } catch (_) {
      _signatureCount = 0;
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
        await _projectService.unsignProject(_project!.id);
        if (mounted) {
          setState(() {
            _signed = false;
            if (_signatureCount > 0) _signatureCount--;
          });
        }
      } else {
        await _projectService.signProject(_project!.id);
        if (mounted) {
          setState(() {
            _signed = true;
            _signatureCount++;
          });
        }
      }
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

  // ── Exportação ─────────────────────────────────────────────

  String _fmtDate(String? d) {
    if (d == null || d.isEmpty) return '—';
    final parsed = DateTime.tryParse(d);
    if (parsed == null) return d;
    final dd = parsed.day.toString().padLeft(2, '0');
    final mm = parsed.month.toString().padLeft(2, '0');
    return '$dd/$mm/${parsed.year}';
  }

  String _fmtMoney(double v) => v > 0 ? 'R\$ ${v.toStringAsFixed(2)}' : '—';

  /// Pares rótulo/valor usados nos dois formatos de exportação.
  List<MapEntry<String, String>> _buildExportRows() {
    final p = _project;
    if (p == null) return [];
    final endereco = [
      if (p.street.isNotEmpty) p.street,
      if (p.number.isNotEmpty) p.number,
      if (p.neighborhood.isNotEmpty) p.neighborhood,
    ].join(', ');

    return [
      MapEntry('ID', p.id.toString()),
      MapEntry('Título', p.title),
      MapEntry('Descrição', p.description),
      MapEntry('Status', StatusLabels.project(p.status)),
      MapEntry('Tipo', _typeLabel(p)),
      MapEntry('Categoria', _categoryName.isNotEmpty ? _categoryName : '—'),
      MapEntry(
        'Autor',
        _authorName.isNotEmpty ? _authorName : 'Usuário #${p.authorId}',
      ),
      MapEntry('Endereço', endereco.isNotEmpty ? endereco : '—'),
      MapEntry('Data de início', _fmtDate(p.startDate)),
      MapEntry('Previsão de término', _fmtDate(p.expectedEndDate)),
      MapEntry('Data de conclusão', _fmtDate(p.endDate)),
      MapEntry('Custo estimado', _fmtMoney(p.estimatedCost)),
      MapEntry('Orçamento aprovado', _fmtMoney(p.approvedBudget)),
      MapEntry('Assinaturas de apoio', _signatureCount.toString()),
      if (_councilors.isNotEmpty)
        MapEntry(
          'Vereadores responsáveis',
          _councilors.map((c) => c.fullname ?? c.name).join(', '),
        ),
    ];
  }

  String _safeFileName() {
    final p = _project;
    final base = (p?.title ?? 'projeto')
        .toLowerCase()
        .replaceAll(RegExp(r'[^a-z0-9]+'), '_')
        .replaceAll(RegExp(r'^_+|_+$'), '');
    return 'projeto_${p?.id ?? ''}_$base'.replaceAll(RegExp(r'_+$'), '');
  }

  Future<void> _exportCsv() async {
    if (_project == null || _isExporting) return;
    setState(() => _isExporting = true);
    try {
      final rows = _buildExportRows();
      String escape(String v) => '"${v.replaceAll('"', '""')}"';
      final buffer = StringBuffer();
      buffer.writeln('${escape('Campo')},${escape('Valor')}');
      for (final row in rows) {
        buffer.writeln('${escape(row.key)},${escape(row.value)}');
      }
      // BOM UTF-8 para acentuação correta no Excel.
      final csv = '\uFEFF${buffer.toString()}';

      final dir = await getTemporaryDirectory();
      final file = File('${dir.path}/${_safeFileName()}.csv');
      await file.writeAsString(csv);

      await Share.shareXFiles([
        XFile(file.path, mimeType: 'text/csv'),
      ], subject: _project!.title);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Falha ao exportar CSV.')));
      }
    } finally {
      if (mounted) setState(() => _isExporting = false);
    }
  }

  Future<void> _exportPdf() async {
    final p = _project;
    if (p == null || _isExporting) return;
    setState(() => _isExporting = true);
    try {
      final rows = _buildExportRows();
      final doc = pw.Document();

      doc.addPage(
        pw.MultiPage(
          pageFormat: PdfPageFormat.a4,
          margin: const pw.EdgeInsets.all(32),
          build: (context) => [
            pw.Text(
              p.title,
              style: pw.TextStyle(
                fontSize: 20,
                fontWeight: pw.FontWeight.bold,
                color: PdfColor.fromInt(0xFF1B3F8B),
              ),
            ),
            pw.SizedBox(height: 4),
            pw.Text(
              'VOX — Detalhes do Projeto',
              style: pw.TextStyle(
                fontSize: 11,
                color: PdfColor.fromInt(0xFF6B7280),
              ),
            ),
            pw.SizedBox(height: 20),
            pw.Table(
              border: pw.TableBorder.symmetric(
                inside: pw.BorderSide(
                  color: PdfColor.fromInt(0xFFE5E7EB),
                  width: 0.5,
                ),
              ),
              columnWidths: const {
                0: pw.FixedColumnWidth(150),
                1: pw.FlexColumnWidth(),
              },
              children: rows.map((row) {
                return pw.TableRow(
                  children: [
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(6),
                      child: pw.Text(
                        row.key,
                        style: pw.TextStyle(
                          fontWeight: pw.FontWeight.bold,
                          fontSize: 11,
                          color: PdfColor.fromInt(0xFF1B3F8B),
                        ),
                      ),
                    ),
                    pw.Padding(
                      padding: const pw.EdgeInsets.all(6),
                      child: pw.Text(
                        row.value,
                        style: const pw.TextStyle(fontSize: 11),
                      ),
                    ),
                  ],
                );
              }).toList(),
            ),
            pw.SizedBox(height: 24),
            pw.Text(
              'Exportado em ${_fmtDate(DateTime.now().toIso8601String())}',
              style: pw.TextStyle(
                fontSize: 9,
                color: PdfColor.fromInt(0xFF9CA3AF),
              ),
            ),
          ],
        ),
      );

      await Printing.sharePdf(
        bytes: await doc.save(),
        filename: '${_safeFileName()}.pdf',
      );
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Falha ao exportar PDF.')));
      }
    } finally {
      if (mounted) setState(() => _isExporting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(
        title: _project?.title ?? 'Projeto',
        actions: [
          if (_project != null && !_isExporting)
            PopupMenuButton<String>(
              icon: const Icon(Icons.ios_share),
              tooltip: 'Exportar para',
              onSelected: (value) {
                if (value == 'csv') {
                  _exportCsv();
                } else if (value == 'pdf') {
                  _exportPdf();
                }
              },
              itemBuilder: (_) => const [
                PopupMenuItem(
                  value: 'csv',
                  child: ListTile(
                    leading: Icon(Icons.description_outlined),
                    title: Text('Exportar CSV'),
                    contentPadding: EdgeInsets.zero,
                  ),
                ),
                PopupMenuItem(
                  value: 'pdf',
                  child: ListTile(
                    leading: Icon(Icons.picture_as_pdf_outlined),
                    title: Text('Exportar PDF'),
                    contentPadding: EdgeInsets.zero,
                  ),
                ),
              ],
            ),
          if (_isExporting)
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 16),
              child: Center(
                child: SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
        ],
      ),
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
      final countSuffix = _signatureCount > 0 ? ' ($_signatureCount)' : '';
      return OutlinedButton.icon(
        onPressed: _isSigning ? null : _toggleSign,
        icon: Icon(_signed ? Icons.check_circle : Icons.edit_outlined),
        label: Text((_signed ? 'Assinado' : 'Assinar') + countSuffix),
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
