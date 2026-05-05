package br.com.fai.Vox.port.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.service.crud.CrudService;

public interface UserService extends CrudService<UserModel>, ReadByEmailService, UpdatePasswordService, ReadByRoleService {


}
