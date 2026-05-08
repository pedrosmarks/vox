package br.com.fai.Vox.port.service.authentication;

import br.com.fai.Vox.domain.UserModel;

public interface AuthenticationService {

    UserModel authenticate(final String email, final String password);

}
