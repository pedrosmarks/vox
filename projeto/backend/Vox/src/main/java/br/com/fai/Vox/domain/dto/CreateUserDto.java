package br.com.fai.Vox.domain.dto;
import br.com.fai.Vox.domain.enums.UserRoleEnum;

import br.com.fai.Vox.domain.UserModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserDto {

    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String cpf;
    private String phone;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    private UserRoleEnum role;
    private LocalDate birthDate;
    private Integer municipalityId;
    private Boolean acceptedTerms;
    private Boolean acceptedPrivacyPolicy;

    private MultipartFile file;

    public CreateUserDto() {
    }

    public UserModel toUserModel() {
        UserModel user = new UserModel();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setCpf(cpf);
        user.setPhone(phone);
        user.setPassword(password);
        user.setRole(role);
        user.setBirthDate(birthDate);
        user.setMunicipalityId(municipalityId);
        user.setAcceptedTerms(acceptedTerms);
        user.setAcceptedPrivacyPolicy(acceptedPrivacyPolicy);
        return user;
    }
}
