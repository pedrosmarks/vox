package br.com.fai.Vox.implementation.service.usersettings;

import br.com.fai.Vox.domain.UserSettings;
import br.com.fai.Vox.domain.UserSettings.AccessibilityMode;
import br.com.fai.Vox.domain.dto.UpdateUserSettingsDto;
import br.com.fai.Vox.port.dao.usersettings.UserSettingsDao;
import br.com.fai.Vox.port.service.usersettings.UserSettingsService;
import org.springframework.stereotype.Service;

@Service
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final int DEFAULT_FONT_SIZE = 16;

    private final UserSettingsDao userSettingsDao;

    public UserSettingsServiceImpl(UserSettingsDao userSettingsDao) {
        this.userSettingsDao = userSettingsDao;
    }

    @Override
    public UserSettings findByUserId(int userId) {
        if (userId <= 0) return null;

        UserSettings settings = userSettingsDao.findByUserId(userId);

        // Se o usuário ainda não tem configurações, retorna o padrão sem persistir
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setFontSize(DEFAULT_FONT_SIZE);
            settings.setAccessibilityMode(AccessibilityMode.NONE);
        }
        return settings;
    }

    @Override
    public void update(int userId, UpdateUserSettingsDto dto) {
        if (userId <= 0 || dto == null) return;

        // Regra de negócio: apenas um modo pode estar ativo por vez.
        // O fontSize pode ser alterado junto com qualquer modo.
        // A validação de conflito de modos é responsabilidade do front,
        // mas garantimos que o valor recebido seja um dos modos válidos via Bean Validation no DTO.

        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setFontSize(dto.getFontSize());
        settings.setAccessibilityMode(dto.getAccessibilityMode());

        userSettingsDao.upsert(settings);
    }
}
