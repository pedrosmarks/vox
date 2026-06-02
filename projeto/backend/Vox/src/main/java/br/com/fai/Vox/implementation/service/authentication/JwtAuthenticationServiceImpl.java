package br.com.fai.Vox.implementation.service.authentication;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.service.authentication.AuthenticationService;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class JwtAuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserModel authenticate(String email, String password) {
        try {
            final UserModel userModel = userService.findByEmail(email);
            if (userModel == null) {
                return null;
            }

            if (!passwordEncoder.matches(password, userModel.getPassword())) {
                return null;
            }

            return userModel;
        } catch (Exception e) {
            return null;
        }
    }
}