package br.com.fai.Vox.implementation.dao.municipality;

import br.com.fai.Vox.domain.MunicipalityModel;
import br.com.fai.Vox.port.dao.municipality.MunicipalityDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public MunicipalityModel findByid(int id) {
        final String sql = "SELECT * FROM municipality WHERE id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                MunicipalityModel municipality = mapResultSetToMunicipalityModel(resultSet);

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
    public List<MunicipalityModel> findAll() {
        final List<MunicipalityModel> municipalities = new ArrayList<>();
        final String sql = "SELECT * FROM municipality";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                MunicipalityModel municipality = mapResultSetToMunicipalityModel(resultSet);
                municipalities.add(municipality);
            }

            resultSet.close();
            preparedStatement.close();
            return municipalities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MunicipalityModel mapResultSetToMunicipalityModel(ResultSet rs) throws SQLException {
        MunicipalityModel municipality = new MunicipalityModel();
        
        municipality.setId(rs.getInt("id"));
        municipality.setName(rs.getString("name"));
        municipality.setState(rs.getString("state"));
        
        return municipality;
    }
}
