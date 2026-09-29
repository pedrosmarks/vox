package br.com.fai.Vox.port.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateUserDto;
import br.com.fai.Vox.port.service.crud.CrudService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends CrudService<UserModel>, ReadByEmailService, UpdatePasswordService, ReadByRoleService, ForgotPasswordService, ResetPasswordService {

    int create(CreateUserDto dto);

    void update(int id, UserModel entity, MultipartFile file);
}
