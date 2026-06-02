package br.com.fai.Vox.implementation.dao.passwordresettoken;

import br.com.fai.Vox.port.dao.passwordresettoken.PasswordResetTokenDao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordResetTokenPostgresDaoImpl implements PasswordResetTokenDao {

    private static final Logger logger = Logger.getLogger(PasswordResetTokenPostgresDaoImpl.class.getName());

    private final Connection connection;

    public PasswordResetTokenPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(int userId, String token, LocalDateTime expiresAt) {
        final String sql = "INSERT INTO password_reset_token (user_id, token, expires_at, used) VALUES (?, ?, ?, false)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
            ps.close();
            logger.log(Level.INFO, "PasswordResetToken salvo para userId: " + userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer findUserIdByToken(String token) {
        final String sql = "SELECT user_id FROM password_reset_token WHERE token = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                rs.close();
                ps.close();
                return userId;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean isTokenValid(String token) {
        final String sql = "SELECT 1 FROM password_reset_token WHERE token = ? AND used = false AND expires_at > CURRENT_TIMESTAMP";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            boolean valid = rs.next();
            rs.close();
            ps.close();
            return valid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markAsUsed(String token) {
        final String sql = "UPDATE password_reset_token SET used = true WHERE token = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, token);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
