package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserSettings {

    private Integer id;
    private Integer userId;
    private Integer fontSize;
    private AccessibilityMode accessibilityMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Modos de acessibilidade disponíveis.
     * Regra: apenas um modo pode estar ativo por vez.
     * Quando qualquer modo estiver ativo, somente o fontSize pode ser alterado junto.
     */
    public enum AccessibilityMode {
        NONE,
        DARK,
        HIGH_CONTRAST,
        PROTANOPIA,
        DEUTERANOPIA,
        TRITANOPIA
    }

    public UserSettings() {}
}
