package br.com.fai.Vox.domain;

public class UserModel {

    private int id;
    private String name;
    private UserRole role;
    private String cpf;
    private String email;
    private String phone;
    private String password;
    private int municipalityId;

    public enum UserRole {
        ADMINISTRATOR,
        MODERATOR,
        CITIZEN
    }

    public UserModel() {}

    public UserModel(int id, String password, String name, UserRole role, String cpf, String email, String phone) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.role = role;
        this.cpf = cpf;
        this.email = email;
        this.phone = phone;
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
}