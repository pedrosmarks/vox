package br.com.fai.Vox.implementation.dao.category;

import br.com.fai.Vox.domain.Category;
import br.com.fai.Vox.port.dao.category.CategoryDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryPostgresDaoImpl implements CategoryDao {

    private static final Logger logger = Logger.getLogger(CategoryPostgresDaoImpl.class.getName());

    private final Connection connection;

    public CategoryPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int create(Category entity) {
        final String sql = "INSERT INTO category (name, description) VALUES (?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            ps.close();

            logger.log(Level.INFO, "Categoria criada com sucesso. ID: " + id);
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        final String sql = "DELETE FROM category WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            ps.close();
            logger.log(Level.INFO, "Categoria removida com sucesso. ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Category findByid(int id) {
        final String sql = "SELECT * FROM category WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Category category = mapResultSetToCategory(rs);
                rs.close();
                ps.close();
                return category;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Category> findAll() {
        final List<Category> categories = new ArrayList<>();
        final String sql = "SELECT * FROM category ORDER BY name ASC";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            rs.close();
            ps.close();
            return categories;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int id, Category entity) {
        final String sql = "UPDATE category SET name = ?, description = ? WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) category.setCreatedAt(createdAt.toLocalDateTime());

        return category;
    }
}
