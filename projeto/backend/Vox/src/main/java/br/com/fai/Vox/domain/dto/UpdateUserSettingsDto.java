package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.UserSettings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserSettingsDto {

    @NotNull(message = "O tamanho da fonte é obrigatório")
    @Min(value = 15, message = "O tamanho mínimo da fonte é 15")
    @Max(value = 30, message = "O tamanho máximo da fonte é 30")
    private Integer fontSize;

    @NotNull(message = "O modo de acessibilidade é obrigatório")
    private UserSettings.AccessibilityMode accessibilityMode;

    public UpdateUserSettingsDto() {}
}
