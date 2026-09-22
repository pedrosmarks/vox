import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../services/auth_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_colors.dart';

class LogsScreen extends StatefulWidget {
  const LogsScreen({super.key});

  @override
  State<LogsScreen> createState() => _LogsScreenState();
}

class _LogsScreenState extends State<LogsScreen> {
  final _authService = AuthService();
  final _userIdController = TextEditingController();
  final _fromController = TextEditingController();
  final _toController = TextEditingController();
  String _method = '';
  int _page = 0;
  int _size = 20;
  int _totalElements = 0;
  List<Map<String, dynamic>> _logs = [];
  bool _isLoading = true;
  String? _error;

  int get _totalPages =>
      _totalElements == 0 ? 0 : (_totalElements / _size).ceil();
  int get _firstResult => _totalElements == 0 ? 0 : _page * _size + 1;
  int get _lastResult => ((_page + 1) * _size).clamp(0, _totalElements);

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _userIdController.dispose();
    _fromController.dispose();
    _toController.dispose();
    super.dispose();
  }

  Future<void> _load({int page = 0}) async {
    final userText = _userIdController.text.trim();
    final userId = userText.isEmpty ? null : int.tryParse(userText);
    if (userText.isNotEmpty && (userId == null || userId < 0)) {
      setState(() => _error = 'Informe um ID de usuário válido.');
      return;
    }
    final from = _fromController.text.trim();
    final to = _toController.text.trim();
    if (from.isNotEmpty && to.isNotEmpty && from.compareTo(to) > 0) {
      setState(
        () => _error = 'A data inicial não pode ser posterior à data final.',
      );
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
      _page = page;
    });
    try {
      final result = await _authService.getLogs(
        page: page,
        size: _size,
        userId: userId,
        method: _method,
        from: from,
        to: to,
      );
      if (!mounted) return;
      setState(() {
        _logs = (result['content'] as List? ?? [])
            .whereType<Map>()
            .map((item) => Map<String, dynamic>.from(item))
            .toList();
        _page = (result['page'] as num?)?.toInt() ?? page;
        _size = (result['size'] as num?)?.toInt() ?? _size;
        _totalElements = (result['totalElements'] as num?)?.toInt() ?? 0;
      });
    } catch (_) {
      if (mounted) {
        setState(
          () => _error = 'Não foi possível carregar os logs de auditoria.',
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _pickDate(TextEditingController controller) async {
    final now = DateTime.now();
    final initialDate = DateTime.tryParse(controller.text) ?? now;
    final selected = await showDatePicker(
      context: context,
      initialDate: initialDate.isAfter(now) ? now : initialDate,
      firstDate: DateTime(2018),
      lastDate: now,
    );
    if (selected == null) return;
    setState(() => controller.text = DateFormat('yyyy-MM-dd').format(selected));
  }

  void _clearFilters() {
    _userIdController.clear();
    _fromController.clear();
    _toController.clear();
    setState(() => _method = '');
    _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(
        title: 'Logs do Sistema',
        actions: [
          IconButton(
            onPressed: _isLoading ? null : () => _load(page: _page),
            tooltip: 'Atualizar logs',
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => _load(page: _page),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(14, 16, 14, 28),
          children: [
            Text(
              'ADMINISTRAÇÃO · AUDITORIA',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                color: VoxColors.accent,
                fontWeight: FontWeight.w800,
                letterSpacing: 1,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Atividades do município',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 3),
            const Text(
              'Registros mantidos por 90 dias',
              style: TextStyle(color: VoxColors.textMuted, fontSize: 11),
            ),
            const SizedBox(height: 14),
            _filters(),
            if (_error != null) ...[
              const SizedBox(height: 10),
              _errorBanner(_error!),
            ],
            if (_isLoading) ...[
              const SizedBox(height: 10),
              const LinearProgressIndicator(minHeight: 2),
            ],
            const SizedBox(height: 10),
            if (!_isLoading && _logs.isEmpty && _error == null)
              _emptyState()
            else
              for (final log in _logs) _logCard(log),
            if (_totalElements > 0) ...[
              const SizedBox(height: 12),
              _pagination(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _filters() => Card(
    child: Padding(
      padding: const EdgeInsets.all(12),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _userIdController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'ID do usuário',
                    hintText: 'Todos',
                    prefixIcon: Icon(Icons.person_search),
                  ),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: DropdownButtonFormField<String>(
                  initialValue: _method,
                  decoration: const InputDecoration(labelText: 'Método'),
                  items: const [
                    DropdownMenuItem(value: '', child: Text('Todos')),
                    DropdownMenuItem(value: 'POST', child: Text('POST')),
                    DropdownMenuItem(value: 'PUT', child: Text('PUT')),
                    DropdownMenuItem(value: 'PATCH', child: Text('PATCH')),
                    DropdownMenuItem(value: 'DELETE', child: Text('DELETE')),
                  ],
                  onChanged: (value) => setState(() => _method = value ?? ''),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(child: _dateFilter('De', _fromController)),
              const SizedBox(width: 10),
              Expanded(child: _dateFilter('Até', _toController)),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              const Text(
                'Por página',
                style: TextStyle(color: VoxColors.textMuted, fontSize: 11),
              ),
              const SizedBox(width: 8),
              DropdownButton<int>(
                value: _size,
                items: const [20, 50, 100, 200]
                    .map(
                      (value) =>
                          DropdownMenuItem(value: value, child: Text('$value')),
                    )
                    .toList(),
                onChanged: (value) {
                  if (value == null) return;
                  setState(() => _size = value);
                  _load();
                },
              ),
              const Spacer(),
              IconButton(
                onPressed: _clearFilters,
                tooltip: 'Limpar filtros',
                icon: const Icon(Icons.backspace_outlined),
              ),
              FilledButton.icon(
                onPressed: _isLoading ? null : () => _load(),
                icon: const Icon(Icons.filter_list, size: 18),
                label: const Text('Filtrar'),
              ),
            ],
          ),
        ],
      ),
    ),
  );

  Widget _dateFilter(String label, TextEditingController controller) => InkWell(
    onTap: () => _pickDate(controller),
    borderRadius: BorderRadius.circular(8),
    child: InputDecorator(
      decoration: InputDecoration(
        labelText: label,
        suffixIcon: const Icon(Icons.calendar_today_outlined, size: 17),
      ),
      child: Text(
        controller.text.isEmpty ? 'Qualquer data' : controller.text,
        style: const TextStyle(fontSize: 12),
      ),
    ),
  );

  Widget _logCard(Map<String, dynamic> log) {
    final success = log['success'] == true;
    final method = '${log['httpMethod'] ?? 'HTTP'}';
    final userId = log['userId'];
    final createdAt = log['createdAt']?.toString() ?? '';
    final parsedDate = DateTime.tryParse(createdAt);
    final dateLabel = parsedDate == null
        ? createdAt
        : DateFormat('dd/MM/yyyy HH:mm:ss').format(parsedDate.toLocal());
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Container(
        decoration: BoxDecoration(
          border: Border(
            left: BorderSide(
              color: success ? VoxColors.success : VoxColors.error,
              width: 3,
            ),
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(12, 11, 12, 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 7,
                runSpacing: 6,
                children: [
                  _badge(method, _methodColor(method)),
                  SizedBox(
                    width: MediaQuery.sizeOf(context).width - 170,
                    child: Text(
                      '${log['path'] ?? '—'}${log['queryString'] == null ? '' : '?${log['queryString']}'}',
                      style: const TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                        fontFamily: 'monospace',
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  _badge(
                    '${log['statusCode'] ?? '—'} · ${success ? 'Sucesso' : 'Falha'}',
                    success ? VoxColors.successBg : VoxColors.errorBg,
                    textColor: success ? VoxColors.success : VoxColors.error,
                  ),
                ],
              ),
              const SizedBox(height: 9),
              Wrap(
                spacing: 13,
                runSpacing: 7,
                children: [
                  _meta(Icons.schedule, dateLabel),
                  _meta(
                    Icons.person_outline,
                    userId == null
                        ? 'Não autenticado'
                        : 'Usuário #$userId${log['userRole'] == null ? '' : ' · ${log['userRole']}'}',
                  ),
                  _meta(Icons.timer_outlined, '${log['durationMs'] ?? 0} ms'),
                  if (log['ipAddress'] != null)
                    _meta(Icons.language, '${log['ipAddress']}'),
                ],
              ),
              const Divider(height: 16),
              Theme(
                data: Theme.of(
                  context,
                ).copyWith(dividerColor: Colors.transparent),
                child: ExpansionTile(
                  tilePadding: EdgeInsets.zero,
                  childrenPadding: const EdgeInsets.only(bottom: 6),
                  dense: true,
                  title: const Text(
                    'Detalhes técnicos',
                    style: TextStyle(fontSize: 11, fontWeight: FontWeight.w700),
                  ),
                  children: [
                    _detail('ID do registro', '${log['id'] ?? '—'}'),
                    _detail('Município', '${log['municipalityId'] ?? '—'}'),
                    _detail(
                      'Agente do navegador',
                      '${log['userAgent'] ?? '—'}',
                    ),
                    if (log['errorMessage'] != null)
                      _detail(
                        'Erro',
                        '${log['errorMessage']}',
                        valueColor: VoxColors.error,
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _badge(String text, Color background, {Color? textColor}) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 4),
    decoration: BoxDecoration(
      color: background,
      borderRadius: BorderRadius.circular(4),
    ),
    child: Text(
      text,
      style: TextStyle(
        color: textColor ?? VoxColors.primary,
        fontSize: 9,
        fontWeight: FontWeight.w800,
      ),
    ),
  );

  Widget _meta(IconData icon, String text) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Icon(icon, size: 14, color: VoxColors.textMuted),
      const SizedBox(width: 4),
      Flexible(
        child: Text(
          text,
          style: const TextStyle(fontSize: 9, color: VoxColors.textMuted),
          overflow: TextOverflow.ellipsis,
        ),
      ),
    ],
  );

  Widget _detail(String label, String value, {Color? valueColor}) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 115,
          child: Text(
            label,
            style: const TextStyle(fontSize: 9, color: VoxColors.textMuted),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(
              fontSize: 9,
              color: valueColor,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ],
    ),
  );

  Widget _pagination() => Row(
    children: [
      Expanded(
        child: Text(
          '$_firstResult–$_lastResult de $_totalElements registros',
          style: const TextStyle(fontSize: 10, color: VoxColors.textMuted),
        ),
      ),
      IconButton(
        onPressed: _page > 0 && !_isLoading
            ? () => _load(page: _page - 1)
            : null,
        tooltip: 'Página anterior',
        icon: const Icon(Icons.chevron_left),
      ),
      Text(
        'Página ${_page + 1} de $_totalPages',
        style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600),
      ),
      IconButton(
        onPressed: _page + 1 < _totalPages && !_isLoading
            ? () => _load(page: _page + 1)
            : null,
        tooltip: 'Próxima página',
        icon: const Icon(Icons.chevron_right),
      ),
    ],
  );

  Widget _emptyState() => Padding(
    padding: const EdgeInsets.symmetric(vertical: 48),
    child: Column(
      children: [
        const Icon(
          Icons.receipt_long_outlined,
          size: 34,
          color: VoxColors.accent,
        ),
        const SizedBox(height: 8),
        const Text(
          'Nenhum registro encontrado',
          style: TextStyle(fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 3),
        const Text(
          'Altere os filtros ou limpe o período.',
          style: TextStyle(fontSize: 11, color: VoxColors.textMuted),
        ),
      ],
    ),
  );

  Widget _errorBanner(String text) => Container(
    width: double.infinity,
    padding: const EdgeInsets.all(11),
    decoration: BoxDecoration(
      color: Theme.of(context).colorScheme.errorContainer,
      borderRadius: BorderRadius.circular(5),
    ),
    child: Text(
      text,
      style: TextStyle(
        color: Theme.of(context).colorScheme.onErrorContainer,
        fontSize: 12,
      ),
    ),
  );

  Color _methodColor(String method) =>
      const {
        'POST': VoxColors.successBg,
        'PUT': Color(0xFFE8EEF9),
        'PATCH': Color(0xFFFFF2D9),
        'DELETE': VoxColors.errorBg,
      }[method] ??
      VoxColors.background;
}
