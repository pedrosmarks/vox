package br.com.fai.Vox.port.dao.usersettings;

import br.com.fai.Vox.domain.UserSettings;

public interface UserSettingsDao {
    UserSettings findByUserId(int userId);
    void upsert(UserSettings settings);
}
