import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../theme/vox_colors.dart';

class RecuperarSenhaScreen extends StatefulWidget {
  const RecuperarSenhaScreen({super.key});

  @override
  State<RecuperarSenhaScreen> createState() => _RecuperarSenhaScreenState();
}

class _RecuperarSenhaScreenState extends State<RecuperarSenhaScreen> {
  final _authService = AuthService();

  // Etapa 1: pedir token por e-mail. Etapa 2: redefinir com token + nova senha.
  int _step = 1;

  final _emailController = TextEditingController();
  final _tokenController = TextEditingController();
  final _newPasswordController = TextEditingController();
  final _confirmController = TextEditingController();

  bool _isLoading = false;
  bool _obscure = true;
  String? _errorMessage;
  String? _successMessage;

  @override
  void dispose() {
    _emailController.dispose();
    _tokenController.dispose();
    _newPasswordController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  Future<void> _requestReset() async {
    if (_emailController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'Informe seu e-mail.');
      return;
    }
    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _successMessage = null;
    });
    try {
      await _authService.forgotPassword(_emailController.text.trim());
    } catch (_) {
      // Não revela se o e-mail existe — segue para a etapa 2 do mesmo jeito.
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _step = 2;
          _successMessage =
              'Se o e-mail estiver cadastrado, você receberá um token de recuperação. Informe-o abaixo com a nova senha.';
        });
      }
    }
  }

  Future<void> _resetPassword() async {
    if (_tokenController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'Informe o token de recuperação.');
      return;
    }
    if (_newPasswordController.text.length < 6) {
      setState(
        () => _errorMessage = 'A nova senha deve ter pelo menos 6 caracteres.',
      );
      return;
    }
    if (_newPasswordController.text != _confirmController.text) {
      setState(() => _errorMessage = 'As senhas não coincidem.');
      return;
    }
    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _successMessage = null;
    });
    try {
      await _authService.resetPassword(
        _tokenController.text.trim(),
        _newPasswordController.text,
      );
      if (!mounted) return;
      setState(() => _successMessage = 'Senha redefinida com sucesso!');
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Senha redefinida com sucesso!')),
      );
      await Future.delayed(const Duration(milliseconds: 900));
      if (mounted) Navigator.of(context).pop();
    } catch (_) {
      if (mounted) {
        setState(
          () => _errorMessage = 'Token inválido/expirado ou erro ao redefinir.',
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
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
            'Recuperar senha',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              color: VoxColors.primary,
            ),
          ),
          const SizedBox(height: 20),

          if (_successMessage != null) ...[
            _banner(
              _successMessage!,
              const Color(0xFFDCFCE7),
              const Color(0xFF15803D),
            ),
            const SizedBox(height: 14),
          ],
          if (_errorMessage != null) ...[
            _banner(
              _errorMessage!,
              const Color(0xFFFFF1F2),
              const Color(0xFFBE123C),
            ),
            const SizedBox(height: 14),
          ],

          if (_step == 1) ..._buildStep1() else ..._buildStep2(),

          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'Lembrou a senha? ',
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

  List<Widget> _buildStep1() {
    return [
      const Padding(
        padding: EdgeInsets.only(bottom: 12),
        child: Text(
          'Informe seu e-mail para receber um token de recuperação.',
          style: TextStyle(fontSize: 13, color: Color(0xFF717182)),
        ),
      ),
      _label('E-mail'),
      _field(
        _emailController,
        'seu@email.com',
        keyboardType: TextInputType.emailAddress,
      ),
      const SizedBox(height: 16),
      _submitButton('Enviar', _requestReset),
    ];
  }

  List<Widget> _buildStep2() {
    return [
      _label('Token de recuperação'),
      _field(_tokenController, 'Cole o token recebido'),
      const SizedBox(height: 14),
      _label('Nova senha'),
      _field(
        _newPasswordController,
        'Mínimo 6 caracteres',
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
      _label('Confirmar nova senha'),
      _field(_confirmController, 'Repita a nova senha', obscure: _obscure),
      const SizedBox(height: 16),
      _submitButton('Redefinir senha', _resetPassword),
      const SizedBox(height: 8),
      TextButton(
        onPressed: _isLoading
            ? null
            : () => setState(() {
                _step = 1;
                _errorMessage = null;
              }),
        child: const Text('← Reenviar para outro e-mail'),
      ),
    ];
  }

  Widget _submitButton(String label, VoidCallback onPressed) {
    return FilledButton(
      onPressed: _isLoading ? null : onPressed,
      child: _isLoading
          ? const SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(
                color: Colors.white,
                strokeWidth: 2.5,
              ),
            )
          : Text(label),
    );
  }

  Widget _banner(String text, Color bg, Color fg) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(text, style: TextStyle(fontSize: 13, color: fg)),
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
