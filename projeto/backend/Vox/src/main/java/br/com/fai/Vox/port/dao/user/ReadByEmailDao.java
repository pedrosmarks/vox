package br.com.fai.Vox.port.dao.user;

import br.com.fai.Vox.domain.UserModel;

public interface ReadByEmailDao {

    UserModel findByEmail(final String email);

}
