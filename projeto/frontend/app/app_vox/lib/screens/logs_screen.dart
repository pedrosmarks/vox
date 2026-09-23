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
  final _fromController = TextEditingController();
  final _toController = TextEditingController();
  int _page = 0;
  int _size = 20;
  int _totalElements = 0;
  List<Map<String, dynamic>> _logs = [];
  final Map<int, String> _actorNames = {};
  final Set<int> _resolvedActorIds = {};
  bool _isLoading = true;
  String? _error;

  bool get _isHighContrast =>
      Theme.of(context).colorScheme.primary.toARGB32() ==
      const Color(0xFFFFFF00).toARGB32();
  bool get _isDark => Theme.of(context).brightness == Brightness.dark;
  Color get _mutedColor => _isHighContrast
      ? Colors.white
      : _isDark
      ? const Color(0xFFC3C8D0)
      : VoxColors.textMuted;
  Color get _accentColor => Theme.of(context).colorScheme.primary;
  Color get _successColor => _isHighContrast
      ? const Color(0xFFFFFF00)
      : _isDark
      ? const Color(0xFF9BE2BB)
      : VoxColors.success;
  Color get _successBackground => _isHighContrast
      ? const Color(0xFFFFFF00)
      : _isDark
      ? const Color(0xFF18372B)
      : VoxColors.successBg;
  Color get _errorColor =>
      _isDark ? const Color(0xFFFFB4AB) : Theme.of(context).colorScheme.error;
  Color get _errorBackground => _isHighContrast
      ? const Color(0xFFFFFF00)
      : _isDark
      ? const Color(0xFF421F22)
      : VoxColors.errorBg;
  int get _totalPages =>
      _totalElements == 0 ? 0 : (_totalElements / _size).ceil();

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _fromController.dispose();
    _toController.dispose();
    super.dispose();
  }

  Future<void> _load({int page = 0}) async {
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
      await _resolveActorNames(_logs);
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
    _fromController.clear();
    _toController.clear();
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
              'ADMINISTRAÇÃO',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                color: _accentColor,
                fontWeight: FontWeight.w800,
                letterSpacing: 1,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Atividades recentes',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
            ),
            const SizedBox(height: 3),
            Text(
              'Acompanhe as ações realizadas no município',
              style: TextStyle(color: _mutedColor, fontSize: 11),
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
              Expanded(child: _dateFilter('Data inicial', _fromController)),
              const SizedBox(width: 10),
              Expanded(child: _dateFilter('Data final', _toController)),
            ],
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              IconButton(
                onPressed: _clearFilters,
                tooltip: 'Limpar período',
                icon: const Icon(Icons.restart_alt),
              ),
              const Spacer(),
              FilledButton.icon(
                onPressed: _isLoading ? null : () => _load(),
                icon: const Icon(Icons.filter_list, size: 18),
                label: const Text('Aplicar período'),
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
    final createdAt = log['createdAt']?.toString() ?? '';
    final parsedDate = DateTime.tryParse(createdAt);
    final dateLabel = parsedDate == null
        ? createdAt
        : DateFormat("dd/MM/yyyy 'às' HH:mm").format(parsedDate.toLocal());
    final actor = _actorLabel(log);
    final action = _actionLabel(log);
    final icon = _activityIcon(log);
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
        leading: CircleAvatar(
          backgroundColor: success ? _successBackground : _errorBackground,
          child: Icon(
            icon,
            color: success ? _successColor : _errorColor,
            size: 19,
          ),
        ),
        title: Text(
          '$actor $action',
          style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
        ),
        subtitle: Padding(
          padding: const EdgeInsets.only(top: 5),
          child: Text(
            '$dateLabel · ${success ? 'Concluído' : 'Não concluído'}',
          ),
        ),
      ),
    );
  }

  IconData _activityIcon(Map<String, dynamic> log) {
    final path = '${log['path'] ?? ''}'.toLowerCase();
    if (path.contains('issue')) return Icons.report_outlined;
    if (path.contains('project')) return Icons.account_balance_outlined;
    if (path.contains('user')) return Icons.person_outline;
    if (path.contains('settings') || path.contains('password')) {
      return Icons.settings_outlined;
    }
    return Icons.check_circle_outline;
  }

  Widget _pagination() => Row(
    children: [
      Expanded(
        child: Text(
          '$_totalElements atividades',
          style: TextStyle(fontSize: 10, color: _mutedColor),
        ),
      ),
      IconButton(
        onPressed: _page > 0 && !_isLoading
            ? () => _load(page: _page - 1)
            : null,
        tooltip: 'Atividades mais recentes',
        icon: const Icon(Icons.arrow_back),
      ),
      Text(
        '${_page + 1} de $_totalPages',
        style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600),
      ),
      IconButton(
        onPressed: _page + 1 < _totalPages && !_isLoading
            ? () => _load(page: _page + 1)
            : null,
        tooltip: 'Atividades anteriores',
        icon: const Icon(Icons.arrow_forward),
      ),
    ],
  );

  Widget _emptyState() => Padding(
    padding: const EdgeInsets.symmetric(vertical: 48),
    child: Column(
      children: [
        Icon(Icons.receipt_long_outlined, size: 34, color: _accentColor),
        const SizedBox(height: 8),
        const Text(
          'Nenhuma atividade encontrada',
          style: TextStyle(fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 3),
        Text(
          'Tente escolher outro período.',
          style: TextStyle(fontSize: 11, color: _mutedColor),
        ),
      ],
    ),
  );

  Widget _errorBanner(String text) => Container(
    width: double.infinity,
    padding: const EdgeInsets.all(11),
    decoration: BoxDecoration(
      color: _isHighContrast
          ? const Color(0xFFFFFF00)
          : Theme.of(context).colorScheme.errorContainer,
      borderRadius: BorderRadius.circular(5),
      border: _isHighContrast
          ? Border.all(color: Colors.white, width: 2)
          : null,
    ),
    child: Text(
      text,
      style: TextStyle(
        color: _isHighContrast
            ? Colors.black
            : Theme.of(context).colorScheme.onErrorContainer,
        fontSize: 12,
      ),
    ),
  );

  String _actorLabel(Map<String, dynamic> log) {
    final userId = log['userId'];
    if (userId == null) return 'Visitante';
    final role =
        const {
          'CITIZEN': 'Cidadão',
          'COUNCILOR': 'Vereador',
          'MODERATOR': 'Moderador',
          'ADMINISTRATOR': 'Administrador',
        }['${log['userRole']}'] ??
        'Usuário';
    final name = _actorNames[userId];
    return name == null || name.isEmpty ? role : name;
  }

  Future<void> _resolveActorNames(List<Map<String, dynamic>> logs) async {
    final ids = logs
        .map((log) => log['userId'])
        .whereType<num>()
        .map((id) => id.toInt())
        .toSet()
        .where((id) => !_resolvedActorIds.contains(id))
        .toList();
    _resolvedActorIds.addAll(ids);
    await Future.wait(
      ids.map((id) async {
        try {
          final user = await _authService.getUserById(id);
          if (mounted) setState(() => _actorNames[id] = user.name);
        } catch (_) {
          _actorNames[id] = '';
        }
      }),
    );
  }

  String _actionLabel(Map<String, dynamic> log) {
    final path = '${log['path'] ?? ''}'.toLowerCase().split('?').first;
    final method = '${log['httpMethod'] ?? ''}'.toUpperCase();
    String action(String description) => description;
    final resource = path.contains('issue')
        ? 'uma ocorrência'
        : path.contains('project')
        ? 'um projeto'
        : path.contains('user')
        ? 'um usuário'
        : path.contains('subscription')
        ? 'uma inscrição'
        : path.contains('settings')
        ? 'as configurações'
        : path.contains('category')
        ? 'uma categoria'
        : path.contains('municipality')
        ? 'um município'
        : path.contains('audience') || path.contains('meeting')
        ? 'uma audiência'
        : 'um registro';
    final target = resource;
    if (path.contains('/authenticate')) {
      return action('iniciou sessão');
    }
    if (path.contains('forgot-password')) {
      return action('solicitou redefinição de senha');
    }
    if (path.contains('reset-password') || path.contains('update-password')) {
      return action('alterou a senha');
    }
    if (path.contains('/subscriptions/')) {
      final verb = method == 'DELETE'
          ? 'cancelou inscrição em'
          : 'inscreveu-se para receber atualizações de';
      if (path.contains('all-projects')) {
        return action('$verb todos os projetos');
      }
      if (path.contains('all-issues')) {
        return action('$verb todas as ocorrências');
      }
      if (path.contains('/projects/')) {
        return action('$verb um projeto');
      }
      if (path.contains('/issues/')) {
        return action('$verb uma ocorrência');
      }
      if (path.contains('/categories/')) {
        return action('$verb uma categoria');
      }
      if (path.contains('/councilors/')) {
        return action('$verb um vereador');
      }
    }
    if (path.endsWith('/opinion')) {
      return action('registrou uma opinião sobre $target');
    }
    if (path.endsWith('/signature')) {
      return action(
        method == 'DELETE' ? 'removeu o apoio a $target' : 'apoiou $target',
      );
    }
    if (path.contains('/councilor/')) {
      return action(
        method == 'DELETE'
            ? 'removeu a associação de vereador a $target'
            : 'associou um vereador a $target',
      );
    }
    if (path.endsWith('/associar')) {
      return action('associou um vereador a $target');
    }
    if (path.endsWith('/approve')) {
      return action('aprovou $target');
    }
    if (path.endsWith('/reject')) {
      return action('rejeitou $target');
    }
    if (path.endsWith('/status') && method == 'PATCH') {
      return action('alterou o status de $target');
    }
    if (path.contains('/image')) {
      final owner = path.contains('/issues/') ? 'ocorrência' : 'projeto';
      if (path.contains('project-image')) {
        return action(
          method == 'DELETE'
              ? 'removeu uma imagem de projeto'
              : 'adicionou uma imagem de projeto',
        );
      }
      final from = owner == 'projeto' ? 'do' : 'da';
      final to = owner == 'projeto' ? 'ao' : 'à';
      return action(
        method == 'DELETE'
            ? 'removeu uma imagem $from $owner'
            : 'adicionou uma imagem $to $owner',
      );
    }
    final verb = method == 'POST'
        ? 'criou'
        : method == 'PUT' || method == 'PATCH'
        ? 'atualizou'
        : method == 'DELETE'
        ? 'removeu'
        : 'consultou';
    return action('$verb $target');
  }
}
