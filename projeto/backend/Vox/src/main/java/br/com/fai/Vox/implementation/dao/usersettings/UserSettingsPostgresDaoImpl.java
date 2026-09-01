package br.com.fai.Vox.implementation.dao.usersettings;

import br.com.fai.Vox.domain.UserSettings;
import br.com.fai.Vox.port.dao.usersettings.UserSettingsDao;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSettingsPostgresDaoImpl implements UserSettingsDao {

    private static final Logger logger = Logger.getLogger(UserSettingsPostgresDaoImpl.class.getName());

    private final Connection connection;

    public UserSettingsPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public UserSettings findByUserId(int userId) {
        final String sql = "SELECT * FROM user_settings WHERE user_id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserSettings settings = mapResultSet(rs);
                rs.close();
                ps.close();
                return settings;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void upsert(UserSettings settings) {
        final String sql =
                "INSERT INTO user_settings (user_id, font_size, accessibility_mode) " +
                "VALUES (?, ?, CAST(? AS accessibility_mode)) " +
                "ON CONFLICT (user_id) DO UPDATE SET " +
                "   font_size = EXCLUDED.font_size, " +
                "   accessibility_mode = EXCLUDED.accessibility_mode, " +
                "   updated_at = CURRENT_TIMESTAMP";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, settings.getUserId());
            ps.setInt(2, settings.getFontSize());
            ps.setString(3, settings.getAccessibilityMode().name());

            ps.executeUpdate();
            ps.close();
            connection.commit();

            logger.log(Level.INFO, "Configurações salvas para o userId=" + settings.getUserId());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    private UserSettings mapResultSet(ResultSet rs) throws SQLException {
        UserSettings settings = new UserSettings();
        settings.setId(rs.getInt("id"));
        settings.setUserId(rs.getInt("user_id"));
        settings.setFontSize(rs.getInt("font_size"));
        settings.setAccessibilityMode(
                UserSettings.AccessibilityMode.valueOf(rs.getString("accessibility_mode").toUpperCase())
        );
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) settings.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) settings.setUpdatedAt(updatedAt.toLocalDateTime());
        return settings;
    }
}
