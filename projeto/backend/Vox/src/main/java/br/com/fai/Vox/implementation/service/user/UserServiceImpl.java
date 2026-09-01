package br.com.fai.Vox.implementation.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.passwordresettoken.PasswordResetTokenDao;
import br.com.fai.Vox.port.dao.user.UserDao;
import br.com.fai.Vox.port.service.email.EmailService;
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
    private final EmailService emailService;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder,
                            PasswordResetTokenDao passwordResetTokenDao,
                            EmailService emailService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.emailService = emailService;
    }

    @Override
    public int create(UserModel entity) {
        if (entity == null) return -1;
        if (entity.getName().isEmpty() || entity.getEmail().isEmpty() || isPassWordInvalid(entity.getPassword())) return -1;
        return userDao.create(entity);
    }

    private boolean isPassWordInvalid(final String password) {
        return password == null || password.isEmpty() || password.length() < 2;
    }

    @Override
    public void delete(int id) {
        if (id < 0) return;
        userDao.delete(id);
    }

    @Override
    public UserModel findByid(int id) {
        if (id < 0) return null;
        return userDao.findByid(id);
    }

    @Override
    public List<UserModel> findAll() {
        return userDao.findAll();
    }

    @Override
    public void update(int id, UserModel entity) {
        if (id != entity.getId()) return;
        if (findByid(id) == null) return;
        userDao.update(id, entity);
    }

    @Override
    public UserModel findByEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        return userDao.findByEmail(email);
    }

    @Override
    public boolean updatePassword(int id, String oldPassword, String newPassword) {
        if (id < 0) return false;

        UserModel entity = userDao.findByid(id);
        if (entity == null) return false;

        if (!passwordEncoder.matches(oldPassword, entity.getPassword()) || isPassWordInvalid(newPassword)) {
            return false;
        }

        // Encode antes de persistir
        userDao.updatePassword(id, passwordEncoder.encode(newPassword));
        return true;
    }

    @Override
    public List<UserModel> findByRole(String role) {
        if (role == null) return new ArrayList<>();
        return userDao.findByRole(role);
    }

    @Override
    public Boolean forgotPassword(String email) {
        if (email == null || email.isEmpty()) return false;

        UserModel user = userDao.findByEmail(email);
        if (user == null) {
            // Não revelar se o e-mail existe — retorna true mesmo assim
            logger.log(Level.INFO, "Forgot password solicitado para e-mail não encontrado: " + email);
            return true;
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        passwordResetTokenDao.save(user.getId(), token, expiresAt);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            logger.log(Level.INFO, "E-mail de reset enviado para userId: " + user.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao enviar e-mail de reset para userId: " + user.getId(), e);
            // Token já foi salvo — o usuário pode tentar novamente
        }

        return true;
    }

    @Override
    public Boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isEmpty() || isPassWordInvalid(newPassword)) return false;

        if (!passwordResetTokenDao.isTokenValid(token)) return false;

        Integer userId = passwordResetTokenDao.findUserIdByToken(token);
        if (userId == null) return false;

        userDao.updatePassword(userId, passwordEncoder.encode(newPassword));
        passwordResetTokenDao.markAsUsed(token);

        logger.log(Level.INFO, "Senha resetada com sucesso para userId: " + userId);
        return true;
    }
}
