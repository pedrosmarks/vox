package br.com.fai.Vox.implementation.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.passwordresettoken.PasswordResetTokenDao;
import br.com.fai.Vox.port.dao.user.UserDao;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenDao passwordResetTokenDao;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder,
                            PasswordResetTokenDao passwordResetTokenDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenDao = passwordResetTokenDao;
    }

    @Override
    public int create(UserModel entity) {
        int invalidResponse = -1;

        if (entity == null) {
            return invalidResponse;
        }

        if (entity.getName().isEmpty() || entity.getEmail().isEmpty() || isPassWordInvalid(entity.getPassword())) {
            return invalidResponse;
        }

        final int id = userDao.create(entity);
        return id;
    }

    private boolean isPassWordInvalid(final String password) {

        if (password.isEmpty()) {
            return true;
        }

        if (password.length() < 2) {
            return true;
        }

        return false;
    }

    @Override
    public void delete(int id) {
        if (id < 0) {
            return;
        }

        userDao.delete(id);
    }

    @Override
    public UserModel findByid(int id) {
        if (id < 0) {
            return null;
        }

        UserModel entity = userDao.findByid(id);
        return entity;
    }

    @Override
    public List<UserModel> findAll() {
        final List<UserModel> entities = userDao.findAll();
        return entities;
    }

    @Override
    public void update(int id, UserModel entity) {
        if (id != entity.getId()) {
            return;
        }

        UserModel userModel = findByid(id);
        if (userModel == null) {
            return;
        }

        userDao.update(id, entity);
    }

    @Override
    public UserModel findByEmail(String email) {
        if (email.isEmpty()) {
            return null;
        }

        UserModel entity = userDao.findByEmail(email);
        return entity;
    }

    @Override
    public boolean updatePassword(int id, String oldPassword, String newPassword) {
        if (id < 0) {
            return false;
        }

        UserModel entity = userDao.findByid(id);

        if (!passwordEncoder.matches(oldPassword, entity.getPassword()) || isPassWordInvalid(newPassword)) {
            return false;
        }

        userDao.updatePassword(id, newPassword);
        return true;
    }

    @Override
    public List<UserModel> findByRole(String role) {
        if (role == null) {
            return new ArrayList<>();
        }

        List<UserModel> users = userDao.findByRole(role);

        return users;
    }

    // Criar o fluxo para poder resetar a senha do usuário

    @Override
    public Boolean forgotPassword(String email) {
        if (email == null || email.isEmpty()) return false;

        UserModel user = userDao.findByEmail(email);
        if (user == null) {
            // Não revelar se o e-mail existe por questões de segurança
            logger.log(Level.INFO, "Forgot password solicitado para e-mail não encontrado: " + email);
            return true;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        passwordResetTokenDao.save(user.getId(), token, expiresAt);

        // TODO: enviar e-mail com o token. Token gerado: token
        logger.log(Level.INFO, "Token de reset gerado para userId: " + user.getId() + " | Token: " + token);
        return true;
    }

    @Override
    public Boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isEmpty() || isPassWordInvalid(newPassword)) return false;

        if (!passwordResetTokenDao.isTokenValid(token)) return false;

        Integer userId = passwordResetTokenDao.findUserIdByToken(token);
        if (userId == null) return false;

        String encoded = passwordEncoder.encode(newPassword);
        userDao.updatePassword(userId, encoded);
        passwordResetTokenDao.markAsUsed(token);

        logger.log(Level.INFO, "Senha resetada com sucesso para userId: " + userId);
        return true;
    }
}
