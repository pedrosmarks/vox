package br.com.fai.Vox.port.service.crud;

import java.util.List;

public interface ReadService<T> {

    T findByid(final int id);

    List<T> findAll();

}
