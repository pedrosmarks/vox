package br.com.fai.Vox.port.dao.user;

public interface ForgotPasswordDao {

    Boolean forgotPassword(final String email);
}
