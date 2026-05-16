package br.com.fai.Vox.port.dao.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.crud.CrudDao;

public interface UserDao extends CrudDao<UserModel>, ReadByEmailDao, UpdatePasswordDao, ReadByRoleDao, ForgotPasswordDao, ResetPasswordDao {
}
