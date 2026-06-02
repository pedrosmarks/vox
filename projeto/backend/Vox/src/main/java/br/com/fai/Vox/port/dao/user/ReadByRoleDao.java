package br.com.fai.Vox.port.dao.user;

import br.com.fai.Vox.domain.UserModel;

import java.util.List;

public interface ReadByRoleDao {

    List<UserModel> findByRole(final String role);
}
