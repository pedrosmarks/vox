package br.com.fai.Vox.port.dao.projectimage;

import br.com.fai.Vox.domain.ProjectImage;
import br.com.fai.Vox.port.dao.crud.CrudDao;

public interface ProjectImageDao extends CrudDao<ProjectImage>, ReadByProjectIdDao {
}
