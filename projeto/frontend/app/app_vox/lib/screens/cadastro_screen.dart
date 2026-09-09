import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/municipality_service.dart';
import '../services/settings_service.dart';
import '../theme/vox_colors.dart';
import 'home_shell.dart';

class CadastroScreen extends StatefulWidget {
  const CadastroScreen({super.key});

  @override
  State<CadastroScreen> createState() => _CadastroScreenState();
}

class _CadastroScreenState extends State<CadastroScreen> {
  final _authService = AuthService();
  final _municipalityService = MunicipalityService();

  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _cpfController = TextEditingController();
  final _phoneController = TextEditingController();
  final _birthDateController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmController = TextEditingController();

  List<Municipality> _municipalities = [];
  int? _municipalityId;
  bool _acceptedTerms = false;

  bool _isLoading = false;
  bool _obscure = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadMunicipalities();
  }

  Future<void> _loadMunicipalities() async {
    try {
      final list = await _municipalityService.getMunicipalities();
      if (!mounted) return;
      setState(() {
        _municipalities = list;
        if (list.length == 1) _municipalityId = list.first.id;
      });
    } catch (_) {
      // Sem token público a lista pode não vir; deixamos vazia e o usuário
      // digita o número do município no fallback.
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _cpfController.dispose();
    _phoneController.dispose();
    _birthDateController.dispose();
    _passwordController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  String? _validate() {
    if (_nameController.text.trim().isEmpty) return 'Informe seu nome.';
    if (_emailController.text.trim().isEmpty) return 'Informe seu e-mail.';
    if (_cpfController.text.trim().isEmpty) return 'Informe seu CPF.';
    if (_municipalityId == null) return 'Selecione seu município.';
    if (_passwordController.text.length < 6) {
      return 'A senha deve ter pelo menos 6 caracteres.';
    }
    if (_passwordController.text != _confirmController.text) {
      return 'As senhas não coincidem.';
    }
    if (!_acceptedTerms) {
      return 'É necessário aceitar os termos e a política de privacidade.';
    }
    return null;
  }

  Future<void> _onSubmit() async {
    final error = _validate();
    if (error != null) {
      setState(() => _errorMessage = error);
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      await _authService.register(
        name: _nameController.text.trim(),
        email: _emailController.text.trim(),
        cpf: _cpfController.text.trim(),
        phone: _phoneController.text.trim(),
        birthDate: _birthDateController.text.trim().isEmpty
            ? null
            : _birthDateController.text.trim(),
        password: _passwordController.text,
        municipalityId: _municipalityId!,
      );

      // Login automático após o cadastro.
      await _authService.login(
        _emailController.text.trim(),
        _passwordController.text,
      );
      try {
        await _authService.fetchCurrentUser();
      } catch (_) {}
      await SettingsController.instance.load();

      if (!mounted) return;
      Navigator.of(context).pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const HomeShell()),
        (route) => false,
      );
    } on AuthException catch (e) {
      setState(() => _errorMessage = e.message);
    } catch (_) {
      setState(
        () => _errorMessage = 'Erro ao conectar ao servidor. Tente novamente.',
      );
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _pickBirthDate() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime(now.year - 18, now.month, now.day),
      firstDate: DateTime(1900),
      lastDate: now,
    );
    if (picked != null) {
      _birthDateController.text =
          '${picked.year.toString().padLeft(4, '0')}-'
          '${picked.month.toString().padLeft(2, '0')}-'
          '${picked.day.toString().padLeft(2, '0')}';
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: VoxColors.gradient),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  const SizedBox(height: 8),
                  const Text(
                    'VOX CIDADÃO',
                    style: TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      letterSpacing: 2,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildCard(),
                  const SizedBox(height: 24),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildCard() {
    return Container(
      width: double.infinity,
      constraints: const BoxConstraints(maxWidth: 460),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.2),
            blurRadius: 50,
            offset: const Offset(0, 25),
          ),
        ],
      ),
      padding: const EdgeInsets.all(28),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'Criar sua conta',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              color: VoxColors.primary,
            ),
          ),
          const SizedBox(height: 24),

          _label('Nome completo'),
          _field(_nameController, 'Seu nome'),
          const SizedBox(height: 14),

          _label('E-mail'),
          _field(
            _emailController,
            'seu@email.com',
            keyboardType: TextInputType.emailAddress,
          ),
          const SizedBox(height: 14),

          _label('CPF'),
          _field(_cpfController, '000.000.000-00'),
          const SizedBox(height: 14),

          _label('Telefone'),
          _field(
            _phoneController,
            '(00) 00000-0000',
            keyboardType: TextInputType.phone,
          ),
          const SizedBox(height: 14),

          _label('Data de nascimento'),
          GestureDetector(
            onTap: _pickBirthDate,
            child: AbsorbPointer(
              child: _field(
                _birthDateController,
                'AAAA-MM-DD',
                suffix: const Icon(Icons.calendar_today, size: 18),
              ),
            ),
          ),
          const SizedBox(height: 14),

          _label('Município'),
          _buildMunicipalityField(),
          const SizedBox(height: 14),

          _label('Senha'),
          _field(
            _passwordController,
            '••••••••',
            obscure: _obscure,
            suffix: IconButton(
              icon: Icon(
                _obscure ? Icons.visibility_off : Icons.visibility,
                size: 20,
                color: const Color(0xFFA0A0B0),
              ),
              onPressed: () => setState(() => _obscure = !_obscure),
            ),
          ),
          const SizedBox(height: 14),

          _label('Confirmar senha'),
          _field(_confirmController, '••••••••', obscure: _obscure),
          const SizedBox(height: 8),

          CheckboxListTile(
            value: _acceptedTerms,
            onChanged: (v) => setState(() => _acceptedTerms = v ?? false),
            controlAffinity: ListTileControlAffinity.leading,
            contentPadding: EdgeInsets.zero,
            dense: true,
            title: const Text(
              'Li e aceito os termos de uso e a política de privacidade.',
              style: TextStyle(fontSize: 13, color: Color(0xFF555555)),
            ),
          ),

          if (_errorMessage != null) ...[
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: const Color(0xFFFFF1F2),
                border: Border.all(color: const Color(0xFFFECDD3)),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                _errorMessage!,
                style: const TextStyle(fontSize: 13, color: Color(0xFFBE123C)),
              ),
            ),
          ],

          const SizedBox(height: 16),
          FilledButton(
            onPressed: _isLoading ? null : _onSubmit,
            child: _isLoading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      color: Colors.white,
                      strokeWidth: 2.5,
                    ),
                  )
                : const Text('Cadastrar'),
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'Já tem uma conta? ',
                style: TextStyle(fontSize: 14, color: Color(0xFF717182)),
              ),
              GestureDetector(
                onTap: () => Navigator.of(context).pop(),
                child: const Text(
                  'Entrar',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: VoxColors.primary,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMunicipalityField() {
    // Se conseguimos a lista, mostramos um dropdown; senão, campo numérico.
    if (_municipalities.isNotEmpty) {
      return DropdownButtonFormField<int>(
        initialValue: _municipalityId,
        decoration: _inputDecoration('Selecione...'),
        items: _municipalities
            .map((m) => DropdownMenuItem(value: m.id, child: Text(m.label)))
            .toList(),
        onChanged: (v) => setState(() => _municipalityId = v),
      );
    }
    return TextField(
      keyboardType: TextInputType.number,
      style: const TextStyle(fontSize: 16, color: Color(0xFF1A1A2E)),
      decoration: _inputDecoration('Código do município (ex: 1)'),
      onChanged: (v) => _municipalityId = int.tryParse(v.trim()),
    );
  }

  Widget _label(String text) => Padding(
    padding: const EdgeInsets.only(bottom: 6),
    child: Text(
      text,
      style: const TextStyle(
        fontSize: 13,
        fontWeight: FontWeight.w600,
        color: Color(0xFF1A1A2E),
      ),
    ),
  );

  InputDecoration _inputDecoration(String hint, {Widget? suffix}) {
    return InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(color: Color(0xFFA0A0B0)),
      filled: true,
      fillColor: const Color(0xFFF3F3F5),
      suffixIcon: suffix,
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: BorderSide(color: Colors.black.withValues(alpha: 0.1)),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: BorderSide(color: Colors.black.withValues(alpha: 0.1)),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(10),
        borderSide: const BorderSide(color: VoxColors.primary, width: 1.5),
      ),
    );
  }

  Widget _field(
    TextEditingController controller,
    String hint, {
    TextInputType keyboardType = TextInputType.text,
    bool obscure = false,
    Widget? suffix,
  }) {
    return TextField(
      controller: controller,
      keyboardType: keyboardType,
      obscureText: obscure,
      style: const TextStyle(fontSize: 16, color: Color(0xFF1A1A2E)),
      decoration: _inputDecoration(hint, suffix: suffix),
    );
  }
}
