package br.com.fai.Vox.implementation.service.category;

import br.com.fai.Vox.domain.Category;
import br.com.fai.Vox.port.dao.category.CategoryDao;
import br.com.fai.Vox.port.service.category.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public int create(Category entity) {
        int invalidResponse = -1;

        if (entity == null) {
            return invalidResponse;
        }

        if (entity.getName().isEmpty() || entity.getDescription().isEmpty()) {
            return invalidResponse;
        }

        final int id = categoryDao.create(entity);
        return id;
    }

    @Override
    public void delete(int id) {
        if (id < 0) {
            return;
        }

        categoryDao.delete(id);
    }

    @Override
    public Category findByid(int id) {
        if (id < 0) {
            return null;
        }

        Category entity = categoryDao.findByid(id);
        return entity;
    }

    @Override
    public List<Category> findAll() {
        final List<Category> entities = categoryDao.findAll();
        return entities;
    }

    @Override
    public void update(int id, Category entity) {
        if (id != entity.getId()) {
            return;
        }

        Category category = findByid(id);
        if (category == null) {
            return;
        }

        categoryDao.update(id, entity);
    }
}
