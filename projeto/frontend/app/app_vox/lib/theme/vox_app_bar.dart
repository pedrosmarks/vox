import 'package:flutter/material.dart';
import 'vox_colors.dart';

/// AppBar com o mesmo gradiente do navbar do site (azul → azul claro → dourado).
class VoxAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final List<Widget>? actions;
  final Widget? bottom;
  final double bottomHeight;

  const VoxAppBar({
    super.key,
    required this.title,
    this.actions,
    this.bottom,
    this.bottomHeight = 0,
  });

  @override
  Widget build(BuildContext context) {
    return AppBar(
      title: Text(title),
      actions: actions,
      backgroundColor: Colors.transparent,
      flexibleSpace: Container(
        decoration: const BoxDecoration(gradient: VoxColors.gradient),
      ),
      bottom: bottom == null
          ? null
          : PreferredSize(
              preferredSize: Size.fromHeight(bottomHeight),
              child: bottom!,
            ),
    );
  }

  @override
  Size get preferredSize =>
      Size.fromHeight(kToolbarHeight + bottomHeight);
}
