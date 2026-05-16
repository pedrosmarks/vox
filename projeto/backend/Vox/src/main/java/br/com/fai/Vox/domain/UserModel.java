package br.com.fai.Vox.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserModel {

    private int id;
    private String name;
    private UserRole role;
    private String cpf;
    private String email;
    private String phone;
    private String password;
    private LocalDate birthDate;
    private int municipalityId;
    private Boolean acceptedTerms;
    private Boolean acceptedPrivacyPolicy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum UserRole {
        ADMINISTRATOR,
        MODERATOR,
        CITIZEN
    }

    public UserModel() {}

    public UserModel(String name, UserRole role, int id, String email, String cpf, String phone, String password, LocalDate birthDate, int municipalityId, Boolean acceptedTerms, Boolean acceptedPrivacyPolicy, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public UserModel(int id, String name, String email, String cpf, UserRole role, String phone, String password, LocalDate birthDate, int municipalityId, Boolean acceptedTerms, Boolean acceptedPrivacyPolicy) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getAcceptedTerms() {
        return acceptedTerms;
    }

    public void setAcceptedTerms(Boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }

    public Boolean getAcceptedPrivacyPolicy() {
        return acceptedPrivacyPolicy;
    }

    public void setAcceptedPrivacyPolicy(Boolean acceptedPrivacyPolicy) {
        this.acceptedPrivacyPolicy = acceptedPrivacyPolicy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public int getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(int municipalityId) {
        this.municipalityId = municipalityId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}