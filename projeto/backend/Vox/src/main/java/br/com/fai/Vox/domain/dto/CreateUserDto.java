package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.UserModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Payload de criação/atualização de usuário recebido como
 * {@code multipart/form-data}. Carrega os campos escalares do usuário e,
 * opcionalmente, o arquivo da foto de perfil ({@link #file}).
 *
 * <p>A foto é opcional: quando {@code file} for {@code null} ou vazio, nenhum
 * upload é feito e o usuário fica sem foto (na criação) ou mantém a atual (na
 * atualização).</p>
 */
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

    private UserModel.UserRole role;
    private LocalDate birthDate;
    private Integer municipalityId;
    private Boolean acceptedTerms;
    private Boolean acceptedPrivacyPolicy;

    /** Foto de perfil (opcional). */
    private MultipartFile file;

    public CreateUserDto() {
    }

    /**
     * Converte este DTO em um {@link UserModel} (sem a foto — o upload e a URL
     * são tratados na camada de serviço).
     */
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
