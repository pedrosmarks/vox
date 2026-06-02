package br.com.fai.Vox.port.service.user;

public interface ResetPasswordService {

    Boolean resetPassword(final String token, final String newPassword);
}
