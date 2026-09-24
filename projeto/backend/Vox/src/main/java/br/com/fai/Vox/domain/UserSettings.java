package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.AccessibilityModeEnum;

@Getter
@Setter
public class UserSettings {

    private Integer id;
    private Integer userId;
    private Integer fontSize;
    private AccessibilityModeEnum accessibilityMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserSettings() {}
}
