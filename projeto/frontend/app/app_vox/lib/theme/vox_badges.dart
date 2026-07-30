import 'package:flutter/material.dart';

/// Badge pill com cores fixas (bg/texto), igual às classes .badge-status/
/// .badge-type/.role-badge do site.
class VoxBadge extends StatelessWidget {
  final String label;
  final Color background;
  final Color foreground;

  const VoxBadge({
    super.key,
    required this.label,
    required this.background,
    required this.foreground,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: foreground,
          fontSize: 12,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

class _Palette {
  final Color bg;
  final Color fg;
  const _Palette(this.bg, this.fg);
}

/// Cores extraídas de projetos.component.scss (.status-*, .type-*) e
/// perfil.component.scss (.role-*).
class VoxBadgeColors {
  static const Map<String, _Palette> _projectStatus = {
    'IN_VOTING': _Palette(Color(0xFFEDE9FE), Color(0xFF4F46E5)),
    'PENDING_APPROVAL': _Palette(Color(0xFFFFF7ED), Color(0xFFC2410C)),
    'IN_ANALYSIS': _Palette(Color(0xFFFFF7ED), Color(0xFFC2410C)),
    'APPROVED': _Palette(Color(0xFFDCFCE7), Color(0xFF15803D)),
    'REJECTED': _Palette(Color(0xFFFEE2E2), Color(0xFFDC2626)),
    'COMPLETED': _Palette(Color(0xFFF1F5F9), Color(0xFF475569)),
  };

  static const Map<String, _Palette> _issueStatus = {
    'PENDING_APPROVAL': _Palette(Color(0xFFFFF7ED), Color(0xFFC2410C)),
    'IN_ANALYSIS': _Palette(Color(0xFFFFF7ED), Color(0xFFC2410C)),
    'APPROVED': _Palette(Color(0xFFDCFCE7), Color(0xFF15803D)),
    'REJECTED': _Palette(Color(0xFFFEE2E2), Color(0xFFDC2626)),
    'RESOLVED': _Palette(Color(0xFFF1F5F9), Color(0xFF475569)),
  };

  static const _Palette _typeOfficial = _Palette(Color(0xFFF1F5F9), Color(0xFF475569));
  static const _Palette _typeSuggested = _Palette(Color(0xFFF0F9FF), Color(0xFF0369A1));

  static const Map<String, _Palette> _role = {
    'ADMINISTRATOR': _Palette(Color(0xFFFEF3C7), Color(0xFF92400E)),
    'MODERATOR': _Palette(Color(0xFFEDE9FE), Color(0xFF4F46E5)),
    'CITIZEN': _Palette(Color(0xFFDCFCE7), Color(0xFF15803D)),
  };

  static const _Palette _fallback = _Palette(Color(0xFFF1F5F9), Color(0xFF475569));

  static Widget projectStatus(String status, String label) {
    final p = _projectStatus[status] ?? _fallback;
    return VoxBadge(label: label, background: p.bg, foreground: p.fg);
  }

  static Widget issueStatus(String status, String label) {
    final p = _issueStatus[status] ?? _fallback;
    return VoxBadge(label: label, background: p.bg, foreground: p.fg);
  }

  static Widget type(bool isOfficial, String label) {
    final p = isOfficial ? _typeOfficial : _typeSuggested;
    return VoxBadge(label: label, background: p.bg, foreground: p.fg);
  }

  static Widget role(String role, String label) {
    final p = _role[role] ?? _fallback;
    return VoxBadge(label: label, background: p.bg, foreground: p.fg);
  }
}
