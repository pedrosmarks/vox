package br.com.fai.Vox.port.service.email;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String token);
}
