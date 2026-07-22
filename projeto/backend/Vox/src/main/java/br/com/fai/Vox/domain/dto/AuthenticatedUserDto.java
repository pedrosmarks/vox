package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.UserModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AuthenticatedUserDto {

    private Integer id;
    private String name;
    private String email;
    private String cpf;
    private String phone;
    private LocalDate birthDate;
    private UserModel.UserRole role;
    private Integer municipalityId;
    private Boolean acceptedTerms;
    private Boolean acceptedPrivacyPolicy;

    public AuthenticatedUserDto() {}

    public static AuthenticatedUserDto from(UserModel user) {
        AuthenticatedUserDto dto = new AuthenticatedUserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCpf(user.getCpf());
        dto.setPhone(user.getPhone());
        dto.setBirthDate(user.getBirthDate());
        dto.setRole(user.getRole());
        dto.setMunicipalityId(user.getMunicipalityId());
        dto.setAcceptedTerms(user.getAcceptedTerms());
        dto.setAcceptedPrivacyPolicy(user.getAcceptedPrivacyPolicy());
        return dto;
    }
}
