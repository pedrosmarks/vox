package br.com.fai.Vox.implementation.dao.municipality;

import br.com.fai.Vox.domain.Municipality;
import br.com.fai.Vox.port.dao.municipality.MunicipalityDao;

import java.sql.*;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MunicipalityPostgresDaoImpl implements MunicipalityDao {

    private static final Logger logger = Logger.getLogger(MunicipalityPostgresDaoImpl.class.getName());

    private final Connection connection;

    public MunicipalityPostgresDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Municipality findByid(int id) {
        final String sql = "SELECT * FROM municipality WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Municipality municipality = mapResultSetToMunicipalityModel(resultSet);

                resultSet.close();
                preparedStatement.close();
                return municipality;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Municipality> findAll() {
        final List<Municipality> municipalities = new ArrayList<>();
        final String sql = "SELECT * FROM municipality";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Municipality municipality = mapResultSetToMunicipalityModel(resultSet);
                municipalities.add(municipality);
            }

            resultSet.close();
            preparedStatement.close();
            return municipalities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Municipality mapResultSetToMunicipalityModel(ResultSet rs) throws SQLException {
        Municipality municipality = new Municipality();
        
        municipality.setId(rs.getInt("id"));
        municipality.setName(rs.getString("name"));
        municipality.setState(rs.getString("state"));
        
        return municipality;
    }

    @Override
    public int create(Municipality entity) {
        final String sql = "INSERT INTO municipality (name, state) VALUES (?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getState());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            rs.close();
            ps.close();

            logger.log(Level.INFO, "Município criado com sucesso. ID: " + id);
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
