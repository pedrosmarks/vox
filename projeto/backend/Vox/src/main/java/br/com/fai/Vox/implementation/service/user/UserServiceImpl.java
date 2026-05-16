package br.com.fai.Vox.implementation.service.user;

import br.com.fai.Vox.domain.UserModel;
import br.com.fai.Vox.port.dao.user.UserDao;
import br.com.fai.Vox.port.service.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
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
        return null;
    }

    @Override
    public Boolean resetPassword(String token, String newPassword) {
        return null;
    }
}
