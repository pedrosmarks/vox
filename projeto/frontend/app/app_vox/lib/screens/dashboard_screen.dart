import 'package:flutter/material.dart';

import '../services/dashboard_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_colors.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final _service = DashboardService();
  late DateTime _from;
  late DateTime _to;
  Map<String, dynamic>? _data;
  bool _loading = true;
  String? _error;
  int _statusView = 0;
  int _timelineView = 0;
  int _categoryMetric = 0;
  int _moderationView = 0;

  @override
  void initState() {
    super.initState();
    _to = DateTime.now();
    _from = _to.subtract(const Duration(days: 29));
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await _service.load(from: _date(_from), to: _date(_to));
      if (!mounted) return;
      setState(() {
        _data = data;
        if (data['overview'] == null) {
          _error = 'Não foi possível carregar os indicadores principais.';
        }
      });
    } catch (_) {
      if (mounted) {
        setState(() => _error = 'Não foi possível conectar ao dashboard.');
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _pickDate({required bool start}) async {
    final picked = await showDatePicker(
      context: context,
      initialDate: start ? _from : _to,
      firstDate: DateTime(2018),
      lastDate: DateTime.now(),
      helpText: start ? 'Início do período' : 'Fim do período',
    );
    if (picked == null) return;
    setState(() {
      if (start) {
        _from = picked;
        if (_from.isAfter(_to)) _to = _from;
      } else {
        _to = picked;
        if (_to.isBefore(_from)) _from = _to;
      }
    });
    await _load();
  }

  Map<String, dynamic> get _overview => _map(_data?['overview']);
  Map<String, dynamic> get _moderation => _map(_data?['moderation']);
  Map<String, dynamic> get _engagement => _map(_data?['engagement']);
  Map<String, dynamic> get _categories => _map(_data?['categories']);
  Map<String, dynamic> get _timeline => _map(_data?['timeline']);
  Map<String, dynamic> get _lifecycle => _map(_data?['projectLifecycle']);
  List<Map<String, dynamic>> get _issueStatuses =>
      _map(_overview['issuesByStatus']).entries
          .map(
            (entry) => {
              'label': _label(entry.key),
              'value': _number(entry.value),
              'color': _statusColor(entry.key),
            },
          )
          .toList();
  List<Map<String, dynamic>> get _points => _list(_timeline['points']);
  List<Map<String, dynamic>> get _categoryItems =>
      _list(_categories['categories'])
          .take(6)
          .map(
            (item) => {
              'name': item['categoryName'] ?? 'Sem categoria',
              'value': _number(
                item[_categoryMetric == 0 ? 'issueCount' : 'projectCount'],
              ),
            },
          )
          .toList();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Dashboard'),
      body: RefreshIndicator(
        onRefresh: _load,
        child: LayoutBuilder(
          builder: (context, constraints) {
            final compact = constraints.maxWidth < 650;
            final columns = compact
                ? 1
                : constraints.maxWidth < 1050
                ? 2
                : 3;
            final cardWidth =
                (constraints.maxWidth - (columns - 1) * 12) / columns;
            return ListView(
              padding: const EdgeInsets.fromLTRB(16, 20, 16, 32),
              children: [
                _heading(compact),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  _errorBanner(_error!),
                ],
                if (_loading) ...[
                  const SizedBox(height: 12),
                  const LinearProgressIndicator(minHeight: 2),
                ],
                const SizedBox(height: 14),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    _kpi(
                      'Ocorrências',
                      _overview['totalIssues'],
                      '${_number(_overview['pendingModerationIssues'])} aguardando moderação',
                      VoxColors.accent,
                      cardWidth,
                    ),
                    _kpi(
                      'Projetos publicados',
                      _overview['publishedProjects'],
                      '${_number(_overview['totalProjects'])} no total',
                      VoxColors.success,
                      cardWidth,
                    ),
                    _kpi(
                      'Usuários',
                      _overview['totalUsers'],
                      'Contas no município',
                      VoxColors.secondary,
                      cardWidth,
                    ),
                    _kpi(
                      'Taxa de resolução',
                      _overview['issueResolutionRate'] == null
                          ? null
                          : '${_overview['issueResolutionRate']}%',
                      'Ocorrências aprovadas resolvidas',
                      VoxColors.error,
                      cardWidth,
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: [
                    _panel(
                      'Situação das ocorrências',
                      'Distribuição por status',
                      cardWidth,
                      _statusPanel(),
                    ),
                    _panel(
                      'Atividade ao longo do tempo',
                      'Novas ocorrências e projetos',
                      cardWidth,
                      _timelinePanel(),
                    ),
                    _panel(
                      'Temas em destaque',
                      'Volume por categoria',
                      cardWidth,
                      _categoryPanel(),
                    ),
                    _panel(
                      'Moderação',
                      'Fila de análise e decisões',
                      cardWidth,
                      _moderationPanel(),
                    ),
                    _panel(
                      'Regiões com mais registros',
                      'Ocorrências e projetos aprovados',
                      cardWidth,
                      _rankPanel(
                        _list(_data?['neighborhoods']).take(6).toList(),
                        nameKey: 'neighborhood',
                        valueKey: 'total',
                        empty: 'Sem registros geográficos neste período.',
                      ),
                    ),
                    _panel(
                      'Participação cidadã',
                      'Contas mais ativas no período',
                      cardWidth,
                      _rankPanel(
                        _list(_engagement['topActiveUsers']).take(5).toList(),
                        nameKey: 'userName',
                        valueKey: 'total',
                        empty: 'Sem atividade cidadã neste período.',
                      ),
                    ),
                    _panel(
                      'Ciclo de vida dos projetos',
                      'Orçamento e etapas',
                      cardWidth,
                      _lifecyclePanel(),
                    ),
                  ],
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _heading(bool compact) {
    final dates = Row(
      children: [
        Expanded(child: _dateButton('De', _from, () => _pickDate(start: true))),
        const SizedBox(width: 8),
        Expanded(child: _dateButton('Até', _to, () => _pickDate(start: false))),
        IconButton(
          onPressed: _loading ? null : _load,
          tooltip: 'Atualizar indicadores',
          icon: const Icon(Icons.refresh),
          style: IconButton.styleFrom(
            backgroundColor: VoxColors.primary,
            foregroundColor: Colors.white,
          ),
        ),
      ],
    );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'GESTÃO MUNICIPAL',
          style: Theme.of(context).textTheme.labelSmall?.copyWith(
            color: VoxColors.accent,
            fontWeight: FontWeight.w800,
            letterSpacing: 1,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          'Dashboard',
          style: Theme.of(
            context,
          ).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800),
        ),
        const SizedBox(height: 3),
        Text(
          'Indicadores de participação, projetos e moderação',
          style: Theme.of(
            context,
          ).textTheme.bodySmall?.copyWith(color: VoxColors.textMuted),
        ),
        const SizedBox(height: 14),
        if (compact)
          dates
        else
          ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: dates,
          ),
      ],
    );
  }

  Widget _dateButton(
    String label,
    DateTime date,
    VoidCallback onTap,
  ) => InkWell(
    onTap: onTap,
    borderRadius: BorderRadius.circular(6),
    child: Container(
      height: 44,
      padding: const EdgeInsets.symmetric(horizontal: 10),
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        border: Border.all(color: VoxColors.border),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        children: [
          Text(
            '$label ',
            style: const TextStyle(color: VoxColors.textMuted, fontSize: 11),
          ),
          Expanded(
            child: Text(
              _date(date),
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 12),
              overflow: TextOverflow.ellipsis,
            ),
          ),
          const Icon(Icons.calendar_today_outlined, size: 15),
        ],
      ),
    ),
  );

  Widget _kpi(
    String title,
    dynamic value,
    String note,
    Color color,
    double width,
  ) => SizedBox(
    width: width,
    child: Container(
      constraints: const BoxConstraints(minHeight: 111),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        border: Border(
          top: BorderSide(color: color, width: 3),
          left: const BorderSide(color: VoxColors.border),
          right: const BorderSide(color: VoxColors.border),
          bottom: const BorderSide(color: VoxColors.border),
        ),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              color: VoxColors.textMuted,
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 7),
          Text(
            value?.toString() ?? '—',
            style: const TextStyle(
              fontSize: 26,
              height: 1,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 5),
          Text(
            note,
            style: const TextStyle(color: VoxColors.textMuted, fontSize: 10),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    ),
  );

  Widget _panel(
    String title,
    String subtitle,
    double width,
    Widget child,
  ) => SizedBox(
    width: width,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 3),
            Text(
              subtitle,
              style: const TextStyle(color: VoxColors.textMuted, fontSize: 10),
            ),
            const SizedBox(height: 14),
            child,
          ],
        ),
      ),
    ),
  );

  Widget _switcher(
    List<String> labels,
    int selected,
    ValueChanged<int> onChanged,
  ) => SegmentedButton<int>(
    showSelectedIcon: false,
    segments: [
      for (var i = 0; i < labels.length; i++)
        ButtonSegment(value: i, label: Text(labels[i])),
    ],
    selected: {selected},
    onSelectionChanged: (selection) => onChanged(selection.first),
    style: ButtonStyle(
      textStyle: const WidgetStatePropertyAll(
        TextStyle(fontSize: 10, fontWeight: FontWeight.w600),
      ),
      visualDensity: VisualDensity.compact,
      padding: const WidgetStatePropertyAll(
        EdgeInsets.symmetric(horizontal: 7, vertical: 4),
      ),
    ),
  );

  Widget _statusPanel() => Column(
    children: [
      Align(
        alignment: Alignment.centerRight,
        child: _switcher(
          const ['Barras', 'Rosca'],
          _statusView,
          (value) => setState(() => _statusView = value),
        ),
      ),
      const SizedBox(height: 14),
      if (_issueStatuses.isEmpty)
        _empty('Sem ocorrências no intervalo selecionado.')
      else if (_statusView == 0)
        ..._issueStatuses.map(
          (item) => _barRow(
            item['label'] as String,
            item['value'] as int,
            _issueStatuses.fold<int>(
              0,
              (sum, entry) => sum + (entry['value'] as int),
            ),
            item['color'] as Color,
          ),
        )
      else
        _donut(_issueStatuses),
    ],
  );

  Widget _timelinePanel() => Column(
    children: [
      Align(
        alignment: Alignment.centerRight,
        child: _switcher(
          const ['Barras', 'Linha'],
          _timelineView,
          (value) => setState(() => _timelineView = value),
        ),
      ),
      const SizedBox(height: 10),
      SizedBox(
        height: 150,
        child: _points.isEmpty
            ? _empty('Sem atividade no intervalo selecionado.')
            : _timelineView == 0
            ? _timelineBars()
            : _timelineLine(),
      ),
      const SizedBox(height: 10),
      _legend(),
    ],
  );

  Widget _timelineBars() {
    final points = _points.length > 12
        ? _points.sublist(_points.length - 12)
        : _points;
    final maximum = points.fold<int>(
      1,
      (max, item) =>
          _number(item['total']) > max ? _number(item['total']) : max,
    );
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: [
        for (final point in points)
          Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 2),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  Expanded(
                    child: Align(
                      alignment: Alignment.bottomCenter,
                      child: SizedBox(
                        height: 112,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            Container(
                              height:
                                  112 *
                                  _number(point['projectCount']) /
                                  maximum,
                              decoration: const BoxDecoration(
                                color: VoxColors.secondary,
                                borderRadius: BorderRadius.vertical(
                                  top: Radius.circular(2),
                                ),
                              ),
                            ),
                            Container(
                              height:
                                  112 * _number(point['issueCount']) / maximum,
                              color: VoxColors.accent,
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 5),
                  Text(
                    '${point['period'] ?? ''}'.substring(
                      5.clamp(0, '${point['period'] ?? ''}'.length),
                    ),
                    style: const TextStyle(
                      fontSize: 8,
                      color: VoxColors.textMuted,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.clip,
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }

  Widget _timelineLine() {
    final points = _points.length > 12
        ? _points.sublist(_points.length - 12)
        : _points;
    final max = points.fold<int>(
      1,
      (current, item) =>
          _number(item['total']) > current ? _number(item['total']) : current,
    );
    return CustomPaint(
      size: Size.infinite,
      painter: _LinePainter(
        points.map((point) => _number(point['total']) / max).toList(),
      ),
    );
  }

  Widget _categoryPanel() => Column(
    children: [
      Align(
        alignment: Alignment.centerRight,
        child: _switcher(
          const ['Ocorrências', 'Projetos'],
          _categoryMetric,
          (value) => setState(() => _categoryMetric = value),
        ),
      ),
      const SizedBox(height: 12),
      if (_categoryItems.isEmpty)
        _empty('Sem dados de categoria neste período.')
      else
        ...() {
          final max = _categoryItems.fold<int>(
            1,
            (current, item) => (item['value'] as int) > current
                ? item['value'] as int
                : current,
          );
          return _categoryItems.map(
            (item) => _simpleBar(
              item['name'].toString(),
              item['value'] as int,
              max,
              VoxColors.accent,
            ),
          );
        }(),
    ],
  );

  Widget _moderationPanel() {
    final values = _moderationView == 0
        ? [
            {
              'label': 'Pendentes',
              'value':
                  _number(_moderation['pendingIssues']) +
                  _number(_moderation['pendingProjects']),
              'color': VoxColors.secondary,
            },
            {
              'label': 'Aprovados',
              'value':
                  _number(_moderation['approvedIssues']) +
                  _number(_moderation['approvedProjects']),
              'color': VoxColors.success,
            },
            {
              'label': 'Rejeitados',
              'value':
                  _number(_moderation['rejectedIssues']) +
                  _number(_moderation['rejectedProjects']),
              'color': VoxColors.textMuted,
            },
          ]
        : [
            {
              'label': 'Aprovação de ocorrências',
              'value': _number(_moderation['issueApprovalRate']),
              'color': VoxColors.accent,
            },
            {
              'label': 'Aprovação de projetos',
              'value': _number(_moderation['projectApprovalRate']),
              'color': VoxColors.success,
            },
          ];
    final max = _moderationView == 1
        ? 100
        : values.fold<int>(
            1,
            (current, item) => (item['value'] as int) > current
                ? item['value'] as int
                : current,
          );
    return Column(
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: _switcher(
            const ['Volume', 'Taxas'],
            _moderationView,
            (value) => setState(() => _moderationView = value),
          ),
        ),
        const SizedBox(height: 12),
        ...values.map(
          (item) => _barRow(
            item['label'] as String,
            item['value'] as int,
            max,
            item['color'] as Color,
            suffix: _moderationView == 1 ? '%' : '',
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Tempo médio: ocorrências ${_moderation['avgIssueDecisionHours'] ?? '—'}h · projetos ${_moderation['avgProjectDecisionHours'] ?? '—'}h',
          style: const TextStyle(fontSize: 9, color: VoxColors.textMuted),
        ),
      ],
    );
  }

  Widget _rankPanel(
    List<Map<String, dynamic>> items, {
    required String nameKey,
    required String valueKey,
    required String empty,
  }) {
    if (items.isEmpty) return _empty(empty);
    return Column(
      children: [
        for (var i = 0; i < items.length; i++)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 7),
            child: Row(
              children: [
                SizedBox(
                  width: 28,
                  child: Text(
                    '${i + 1}'.padLeft(2, '0'),
                    style: const TextStyle(
                      color: VoxColors.accent,
                      fontWeight: FontWeight.w800,
                      fontSize: 11,
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    (items[i][nameKey] ?? 'Não informado').toString(),
                    style: const TextStyle(fontSize: 11),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                Text(
                  '${items[i][valueKey] ?? 0}',
                  style: const TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 11,
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _lifecyclePanel() {
    final statuses = _map(_lifecycle['projectsByStatus']);
    final rate = _lifecycle['budgetExecutionRate'];
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Execução orçamentária',
          style: const TextStyle(color: VoxColors.textMuted, fontSize: 10),
        ),
        const SizedBox(height: 5),
        Text(
          rate == null ? '—' : '$rate%',
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800),
        ),
        const SizedBox(height: 10),
        _simpleBar(
          'Orçamento aprovado / estimado',
          _number(rate),
          100,
          VoxColors.success,
          suffix: '%',
        ),
        const Divider(height: 18),
        for (final entry in statuses.entries.take(4))
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 3),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    _label(entry.key),
                    style: const TextStyle(
                      fontSize: 10,
                      color: VoxColors.textMuted,
                    ),
                  ),
                ),
                Text(
                  '${entry.value}',
                  style: const TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _barRow(
    String label,
    int value,
    int max,
    Color color, {
    String suffix = '',
  }) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 7),
    child: Row(
      children: [
        Expanded(
          flex: 4,
          child: Text(
            label,
            style: const TextStyle(fontSize: 10, color: VoxColors.textMuted),
            overflow: TextOverflow.ellipsis,
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          flex: 5,
          child: ClipRRect(
            borderRadius: BorderRadius.circular(2),
            child: LinearProgressIndicator(
              value: max == 0 ? 0 : (value / max).clamp(0, 1),
              minHeight: 8,
              backgroundColor: VoxColors.border,
              color: color,
            ),
          ),
        ),
        SizedBox(
          width: 42,
          child: Text(
            '$value$suffix',
            textAlign: TextAlign.right,
            style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w700),
          ),
        ),
      ],
    ),
  );

  Widget _simpleBar(
    String label,
    int value,
    int max,
    Color color, {
    String suffix = '',
  }) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Row(
        children: [
          Expanded(
            child: Text(
              label,
              style: const TextStyle(fontSize: 10, color: VoxColors.textMuted),
              overflow: TextOverflow.ellipsis,
            ),
          ),
          Text(
            '$value$suffix',
            style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w800),
          ),
        ],
      ),
      const SizedBox(height: 5),
      ClipRRect(
        borderRadius: BorderRadius.circular(2),
        child: LinearProgressIndicator(
          value: max == 0 ? 0 : (value / max).clamp(0, 1),
          minHeight: 7,
          backgroundColor: VoxColors.border,
          color: color,
        ),
      ),
      const SizedBox(height: 10),
    ],
  );

  Widget _donut(List<Map<String, dynamic>> items) {
    final total = items.fold<int>(
      0,
      (sum, item) => sum + (item['value'] as int),
    );
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        SizedBox(
          width: 120,
          height: 120,
          child: CustomPaint(
            painter: _DonutPainter(items, total),
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    '$total',
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const Text(
                    'total',
                    style: TextStyle(fontSize: 9, color: VoxColors.textMuted),
                  ),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 18),
        Flexible(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              for (final item in items)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 5),
                  child: Row(
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: item['color'] as Color,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          item['label'] as String,
                          style: const TextStyle(fontSize: 10),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Text(
                        '${item['value']}',
                        style: const TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _legend() => Row(
    mainAxisAlignment: MainAxisAlignment.center,
    children: [
      _legendItem('Ocorrências', VoxColors.accent),
      const SizedBox(width: 16),
      _legendItem('Projetos', VoxColors.secondary),
    ],
  );

  Widget _legendItem(String label, Color color) => Row(
    children: [
      Container(
        width: 8,
        height: 8,
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(2),
        ),
      ),
      const SizedBox(width: 5),
      Text(
        label,
        style: const TextStyle(color: VoxColors.textMuted, fontSize: 9),
      ),
    ],
  );

  Widget _empty(String text) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 24),
    child: Center(
      child: Text(
        text,
        style: const TextStyle(color: VoxColors.textMuted, fontSize: 11),
        textAlign: TextAlign.center,
      ),
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

  Map<String, dynamic> _map(dynamic value) =>
      value is Map ? Map<String, dynamic>.from(value) : {};
  List<Map<String, dynamic>> _list(dynamic value) => value is List
      ? value
            .whereType<Map>()
            .map((item) => Map<String, dynamic>.from(item))
            .toList()
      : [];
  int _number(dynamic value) =>
      value is num ? value.round() : int.tryParse('$value') ?? 0;
  String _date(DateTime date) =>
      '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  String _label(String value) =>
      const {
        'OPEN': 'Aberta',
        'IN_PROGRESS': 'Em andamento',
        'RESOLVED': 'Resolvida',
        'PENDING': 'Pendente',
        'APPROVED': 'Aprovada',
        'REJECTED': 'Rejeitada',
        'PENDING_APPROVAL': 'Aguardando aprovação',
        'PUBLISHED': 'Publicado',
        'IN_EXECUTION': 'Em execução',
        'COMPLETED': 'Concluído',
      }[value] ??
      value.replaceAll('_', ' ').toLowerCase();
  Color _statusColor(String key) =>
      const {
        'OPEN': Color(0xFFD76535),
        'IN_PROGRESS': Color(0xFF237A8A),
        'RESOLVED': Color(0xFF438653),
      }[key] ??
      VoxColors.textMuted;
}

class _DonutPainter extends CustomPainter {
  final List<Map<String, dynamic>> items;
  final int total;
  _DonutPainter(this.items, this.total);

  @override
  void paint(Canvas canvas, Size size) {
    var start = -1.5708;
    final rect = Offset.zero & size;
    for (final item in items) {
      final value = item['value'] as int;
      final sweep = total == 0 ? 0.0 : value / total * 6.2832;
      final paint = Paint()
        ..color = item['color'] as Color
        ..style = PaintingStyle.stroke
        ..strokeWidth = 15
        ..strokeCap = StrokeCap.butt;
      canvas.drawArc(rect.deflate(9), start, sweep, false, paint);
      start += sweep;
    }
  }

  @override
  bool shouldRepaint(covariant _DonutPainter oldDelegate) =>
      oldDelegate.items != items || oldDelegate.total != total;
}

class _LinePainter extends CustomPainter {
  final List<double> values;
  _LinePainter(this.values);

  @override
  void paint(Canvas canvas, Size size) {
    final grid = Paint()
      ..color = VoxColors.border
      ..strokeWidth = 1;
    for (final y in [12.0, size.height / 2, size.height - 8]) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), grid);
    }
    if (values.isEmpty) return;
    final points = <Offset>[];
    for (var index = 0; index < values.length; index++) {
      final x = values.length == 1
          ? size.width / 2
          : index / (values.length - 1) * size.width;
      final y =
          size.height - 10 - values[index].clamp(0, 1) * (size.height - 24);
      points.add(Offset(x, y));
    }
    final line = Paint()
      ..color = VoxColors.accent
      ..strokeWidth = 2.5
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    final path = Path()..moveTo(points.first.dx, points.first.dy);
    for (final point in points.skip(1)) {
      path.lineTo(point.dx, point.dy);
    }
    canvas.drawPath(path, line);
    final dot = Paint()..color = VoxColors.secondary;
    for (final point in points) {
      canvas.drawCircle(point, 3, dot);
    }
  }

  @override
  bool shouldRepaint(covariant _LinePainter oldDelegate) =>
      oldDelegate.values != values;
}
