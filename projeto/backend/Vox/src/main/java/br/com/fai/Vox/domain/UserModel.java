package br.com.fai.Vox.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.fai.Vox.domain.enums.UserRoleEnum;

@Getter
@Setter
public class UserModel {

    private Integer id;
    private String name;
    private UserRoleEnum role;
    private String cpf;
    private String email;
    private String phone;
    private String password;
    private LocalDate birthDate;
    private Integer municipalityId;
    private Boolean acceptedTerms;
    private Boolean acceptedPrivacyPolicy;
    private String profilePhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserModel() {}

    public UserModel(String name, UserRoleEnum role, Integer id, String email, String cpf, String phone, String password, LocalDate birthDate, Integer municipalityId, Boolean acceptedTerms, Boolean acceptedPrivacyPolicy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.name = name;
        this.role = role;
        this.id = id;
        this.email = email;
        this.cpf = cpf;
        this.phone = phone;
        this.password = password;
        this.birthDate = birthDate;
        this.municipalityId = municipalityId;
        this.acceptedTerms = acceptedTerms;
        this.acceptedPrivacyPolicy = acceptedPrivacyPolicy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserModel(Integer id, String name, String email, String cpf, UserRoleEnum role, String phone, String password, LocalDate birthDate, Integer municipalityId, Boolean acceptedTerms, Boolean acceptedPrivacyPolicy) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.role = role;
        this.phone = phone;
        this.password = password;
        this.birthDate = birthDate;
        this.municipalityId = municipalityId;
        this.acceptedTerms = acceptedTerms;
        this.acceptedPrivacyPolicy = acceptedPrivacyPolicy;
    }

}
