package br.com.fai.Vox.implementation.service.authentication;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.service.authentication.AuthenticationService;
import br.com.fai.Vox.port.service.user.UserService;

//@Service
public class BasicAuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;

    public BasicAuthenticationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserModel authenticate(String email, String password) {
        if (email == null || email.isEmpty()
                || password == null || password.isEmpty()) {
            return null;
        }

        UserModel user = userService.findByEmail(email);
        if (user == null) {
            return null;
        }

        if (user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }
}
