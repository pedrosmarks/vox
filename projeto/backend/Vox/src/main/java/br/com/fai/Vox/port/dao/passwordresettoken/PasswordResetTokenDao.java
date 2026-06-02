package br.com.fai.Vox.port.dao.passwordresettoken;

public interface PasswordResetTokenDao {
    void save(int userId, String token, java.time.LocalDateTime expiresAt);
    Integer findUserIdByToken(String token);
    boolean isTokenValid(String token);
    void markAsUsed(String token);
}
