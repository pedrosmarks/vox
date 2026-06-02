package br.com.fai.Vox.port.dao.user;

public interface UpdatePasswordDao {

    boolean updatePassword(final int id, final String newPassword);

}
