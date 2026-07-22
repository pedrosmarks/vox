package br.com.fai.Vox.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDto {

    private int id;

    @NotBlank(message = "Senha atual é obrigatória")
    private String oldPassword;

    @NotBlank(message = "Nova senha é obrigatória")
    private String newPassword;

    public UpdatePasswordDto() {}
}
