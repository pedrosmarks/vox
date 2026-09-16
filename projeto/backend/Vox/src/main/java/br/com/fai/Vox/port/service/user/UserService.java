package br.com.fai.Vox.port.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.domain.dto.CreateUserDto;
import br.com.fai.Vox.port.service.crud.CrudService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends CrudService<UserModel>, ReadByEmailService, UpdatePasswordService, ReadByRoleService, ForgotPasswordService, ResetPasswordService {

    /**
     * Cria um usuário a partir do payload multipart, fazendo o upload da foto
     * de perfil quando informada (opcional).
     *
     * @return id do usuário criado, ou valor negativo em caso de dados inválidos
     */
    int create(CreateUserDto dto);

    /**
     * Atualiza um usuário (update parcial) e, quando um novo arquivo for
     * informado, substitui a foto de perfil. Sem arquivo, a foto atual é
     * mantida.
     */
    void update(int id, UserModel entity, MultipartFile file);
}
