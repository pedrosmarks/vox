package br.com.fai.Vox.port.service.user;

import br.com.fai.Vox.domain.UserModel;

import java.util.List;

public interface ReadByRoleService {

   List<UserModel> findByRole(final String role);
}
