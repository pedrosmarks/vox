package br.com.fai.Vox.domain.enums;

/**
 * Modos de acessibilidade disponíveis.
 * Regra: apenas um modo pode estar ativo por vez.
 * Quando qualquer modo estiver ativo, somente o fontSize pode ser alterado junto.
 */
public enum AccessibilityModeEnum {
    NONE,
    DARK,
    HIGH_CONTRAST,
    PROTANOPIA,
    DEUTERANOPIA,
    TRITANOPIA
}
