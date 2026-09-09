import 'package:flutter/material.dart';
import '../services/settings_service.dart';
import '../theme/vox_app_bar.dart';

class ConfiguracoesScreen extends StatefulWidget {
  const ConfiguracoesScreen({super.key});

  @override
  State<ConfiguracoesScreen> createState() => _ConfiguracoesScreenState();
}

class _ConfiguracoesScreenState extends State<ConfiguracoesScreen> {
  final _controller = SettingsController.instance;

  late int _fontSize;
  late AccessibilityMode _mode;

  bool _isLoading = true;
  bool _isSaving = false;
  String? _message;
  bool _messageOk = false;

  static const _modes = <_ModeOption>[
    _ModeOption(
      AccessibilityMode.none,
      'Padrão',
      'Aparência normal do VOX.',
      Icons.palette_outlined,
    ),
    _ModeOption(
      AccessibilityMode.dark,
      'Modo escuro',
      'Fundo preto, menos brilho.',
      Icons.dark_mode_outlined,
    ),
    _ModeOption(
      AccessibilityMode.highContrast,
      'Alto contraste',
      'Cores fortes para melhor leitura.',
      Icons.contrast,
    ),
    _ModeOption(
      AccessibilityMode.protanopia,
      'Protanopia',
      'Ajuste para deficiência ao vermelho.',
      Icons.visibility_outlined,
    ),
    _ModeOption(
      AccessibilityMode.deuteranopia,
      'Deuteranopia',
      'Ajuste para deficiência ao verde.',
      Icons.visibility_outlined,
    ),
    _ModeOption(
      AccessibilityMode.tritanopia,
      'Tritanopia',
      'Ajuste para deficiência ao azul.',
      Icons.visibility_outlined,
    ),
  ];

  @override
  void initState() {
    super.initState();
    _fontSize = _controller.value.fontSize;
    _mode = _controller.value.mode;
    _load();
  }

  Future<void> _load() async {
    await _controller.load();
    if (!mounted) return;
    setState(() {
      _fontSize = _controller.value.fontSize;
      _mode = _controller.value.mode;
      _isLoading = false;
    });
  }

  /// Aplica na hora (preview), sem persistir ainda.
  void _previewFont(double value) {
    setState(() => _fontSize = value.round());
    _controller.value = _controller.value.copyWith(fontSize: _fontSize);
  }

  void _selectMode(AccessibilityMode mode) {
    setState(() => _mode = mode);
    _controller.value = _controller.value.copyWith(mode: mode);
    _save();
  }

  Future<void> _save() async {
    if (_isSaving) return;
    setState(() {
      _isSaving = true;
      _message = null;
    });
    final ok = await _controller.save(
      UserSettings(fontSize: _fontSize, mode: _mode),
    );
    if (!mounted) return;
    setState(() {
      _isSaving = false;
      _messageOk = ok;
      _message = ok
          ? 'Configurações salvas.'
          : 'Aplicado, mas não foi possível salvar no servidor.';
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: const VoxAppBar(title: 'Configurações'),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                // ── Tamanho da fonte ──
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Tamanho da fonte',
                          style: theme.textTheme.titleMedium,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Ajuste o tamanho do texto em todo o app.',
                          style: theme.textTheme.bodySmall,
                        ),
                        const SizedBox(height: 12),
                        Row(
                          children: [
                            const Text(
                              'A',
                              style: TextStyle(fontWeight: FontWeight.bold),
                            ),
                            Expanded(
                              child: Slider(
                                min: 15,
                                max: 30,
                                divisions: 15,
                                value: _fontSize.toDouble(),
                                label: '$_fontSize px',
                                onChanged: _previewFont,
                                onChangeEnd: (_) => _save(),
                              ),
                            ),
                            const Text(
                              'A',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 22,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Text('$_fontSize px'),
                          ],
                        ),
                        Container(
                          margin: const EdgeInsets.only(top: 8),
                          padding: const EdgeInsets.all(12),
                          width: double.infinity,
                          decoration: BoxDecoration(
                            color: theme.colorScheme.surface,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(color: theme.dividerColor),
                          ),
                          child: const Text(
                            'Exemplo: participe das decisões da sua cidade.',
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // ── Modo de acessibilidade ──
                Text(
                  'Modo de acessibilidade',
                  style: theme.textTheme.titleMedium,
                ),
                const SizedBox(height: 4),
                Text(
                  'Apenas um modo pode ficar ativo por vez.',
                  style: theme.textTheme.bodySmall,
                ),
                const SizedBox(height: 12),
                ..._modes.map((m) {
                  final selected = _mode == m.mode;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      leading: Icon(
                        m.icon,
                        color: selected
                            ? theme.colorScheme.primary
                            : theme.iconTheme.color,
                      ),
                      title: Text(m.label),
                      subtitle: Text(m.description),
                      trailing: selected
                          ? Icon(
                              Icons.check_circle,
                              color: theme.colorScheme.primary,
                            )
                          : const Icon(Icons.circle_outlined),
                      onTap: () => _selectMode(m.mode),
                    ),
                  );
                }),

                const SizedBox(height: 8),
                FilledButton(
                  onPressed: _isSaving ? null : _save,
                  child: _isSaving
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Salvar configurações'),
                ),
                if (_message != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 12),
                    child: Text(
                      _message!,
                      style: TextStyle(
                        color: _messageOk ? Colors.green : Colors.orange,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
              ],
            ),
    );
  }
}

class _ModeOption {
  final AccessibilityMode mode;
  final String label;
  final String description;
  final IconData icon;
  const _ModeOption(this.mode, this.label, this.description, this.icon);
}
