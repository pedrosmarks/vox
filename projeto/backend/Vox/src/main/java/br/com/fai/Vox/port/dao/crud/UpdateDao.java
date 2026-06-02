package br.com.fai.Vox.port.dao.crud;

public interface UpdateDao<T> {

    void update(final int id, final T entity);

}
