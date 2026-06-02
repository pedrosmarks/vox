package br.com.fai.Vox.port.dao.crud;

public interface CreateDao<T> {

    int create(final T entity);

}
