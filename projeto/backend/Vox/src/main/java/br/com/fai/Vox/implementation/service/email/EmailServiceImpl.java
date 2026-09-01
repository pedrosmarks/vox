package br.com.fai.Vox.implementation.service.email;

import br.com.fai.Vox.port.service.email.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String frontendResetUrl;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender, String frontendResetUrl, String fromEmail) {
        this.mailSender = mailSender;
        this.frontendResetUrl = frontendResetUrl;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendResetUrl + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Redefinição de senha - VOX");
        message.setText(
                "Olá,\n\n" +
                "Recebemos uma solicitação para redefinir a senha da sua conta no VOX.\n\n" +
                "Clique no link abaixo para criar uma nova senha (válido por 2 horas):\n\n" +
                resetLink + "\n\n" +
                "Se você não solicitou a redefinição, ignore este e-mail. Sua senha permanece a mesma.\n\n" +
                "Equipe VOX"
        );

        mailSender.send(message);
    }
}
