package br.com.fai.Vox.implementation.dao.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.user.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserPostgresDaoImpl implements UserDao {

    private static final Logger logger = Logger.getLogger(UserPostgresDaoImpl.class.getName());

    private final Connection connection;

    public UserPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(UserModel entity) {
        logger.log(Level.INFO, "Inserindo usuário no banco de dados.");

        final String sql = "INSERT INTO user_model (name, email, cpf, phone, password, role, municipality_id) " +
                "VALUES (?, ?, ?, ?, crypt(?, gen_salt('bf')), CAST(? AS user_role), ?)";

        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getCpf());
            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getPassword());
            ps.setString(6, entity.getRole().name().toLowerCase());
            ps.setInt(7, entity.getMunicipalityId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            connection.commit();
            rs.close();
            ps.close();

            logger.log(Level.INFO, "Usuário adicionado com sucesso. ID: " + userId);
            return userId;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao inserir usuário. Realizando rollback.");
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM user_model WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            logger.log(Level.INFO, "Usuário removido com sucesso.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserModel findByid(int id) {
        final String sql = "SELECT * FROM user_model WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserModel user = mapResultSetToUserModel(rs);
                rs.close();
                ps.close();
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<UserModel> findAll() {
        final List<UserModel> users = new ArrayList<>();
        final String sql = "SELECT * FROM user_model";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUserModel(rs));
            }
            rs.close();
            ps.close();
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int id, UserModel entity) {
        final String sql = "UPDATE user_model SET name = ?, cpf = ?, email = ?, phone = ?, municipality_id = ? WHERE id = ?";
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getCpf());
            ps.setString(3, entity.getEmail());
            ps.setString(4, entity.getPhone());
            ps.setInt(5, entity.getMunicipalityId());
            ps.setInt(6, id);

            ps.executeUpdate();
            ps.close();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserModel findByEmail(String email) {
        final String sql = "SELECT * FROM user_model WHERE email = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserModel user = mapResultSetToUserModel(rs);
                rs.close();
                ps.close();
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean updatePassword(int id, String newPassword) {
        final String sql = "UPDATE user_model SET password = crypt(?, gen_salt('bf')) WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            ps.execute();
            ps.close();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserModel> findByRole(String role) {
        final List<UserModel> users = new ArrayList<>();
        final String sql = "SELECT * FROM user_model WHERE role = CAST(? AS user_role)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, role.toLowerCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUserModel(rs));
            }
            rs.close();
            ps.close();
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserModel mapResultSetToUserModel(ResultSet rs) throws SQLException {
        UserModel user = new UserModel();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setCpf(rs.getString("cpf"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(UserModel.UserRole.valueOf(rs.getString("role").toUpperCase()));
        user.setMunicipalityId(rs.getInt("municipality_id"));
        return user;
    }
}
