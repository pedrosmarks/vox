package br.com.fai.Vox.port.dao.user;

public interface ResetPasswordDao {

    Boolean resetPassword(final String token, final String newPassword);
}
