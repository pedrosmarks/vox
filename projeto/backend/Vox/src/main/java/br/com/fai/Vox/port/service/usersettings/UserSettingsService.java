package br.com.fai.Vox.port.service.usersettings;

import br.com.fai.Vox.domain.UserSettings;
import br.com.fai.Vox.domain.dto.UpdateUserSettingsDto;

public interface UserSettingsService {
    UserSettings findByUserId(int userId);
    void update(int userId, UpdateUserSettingsDto dto);
}
