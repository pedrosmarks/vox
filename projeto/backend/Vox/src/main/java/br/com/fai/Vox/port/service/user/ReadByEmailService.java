package br.com.fai.Vox.port.service.user;

import br.com.fai.Vox.domain.UserModel;

public interface ReadByEmailService {

    UserModel findByEmail(final String email);

}
