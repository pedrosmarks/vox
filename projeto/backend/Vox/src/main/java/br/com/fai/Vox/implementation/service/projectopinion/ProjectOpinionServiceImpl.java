package br.com.fai.Vox.implementation.service.projectopinion;

import br.com.fai.Vox.domain.ProjectOpinion;
import br.com.fai.Vox.domain.dto.ProjectOpinionStatsDto;
import br.com.fai.Vox.port.dao.projectopinion.ProjectOpinionDao;
import br.com.fai.Vox.port.service.projectopinion.ProjectOpinionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProjectOpinionServiceImpl implements ProjectOpinionService {

    private final ProjectOpinionDao projectOpinionDao;

    public ProjectOpinionServiceImpl(ProjectOpinionDao projectOpinionDao) {
        this.projectOpinionDao = projectOpinionDao;
    }

    @Override
    public void submitOpinion(int projectId, int userId, ProjectOpinion.OpinionType opinion) {
        if (projectId <= 0 || userId <= 0 || opinion == null) return;

        ProjectOpinion entity = new ProjectOpinion();
        entity.setProjectId(projectId);
        entity.setUserId(userId);
        entity.setOpinion(opinion);
        projectOpinionDao.upsert(entity);
    }

    @Override
    public List<ProjectOpinion> findByProjectId(int projectId) {
        if (projectId <= 0) return List.of();
        return projectOpinionDao.findByProjectId(projectId);
    }

    @Override
    public Map<String, Integer> countByProjectId(int projectId) {
        if (projectId <= 0) return Map.of("APPROVE", 0, "DISAPPROVE", 0, "NEUTRAL", 0, "TOTAL", 0);
        return projectOpinionDao.countByProjectId(projectId);
    }

    @Override
    public ProjectOpinionStatsDto getStats(int projectId) {
        if (projectId <= 0) return new ProjectOpinionStatsDto(0, 0, 0);
        Map<String, Integer> counts = projectOpinionDao.countByProjectId(projectId);
        return new ProjectOpinionStatsDto(
                counts.getOrDefault("APPROVE", 0),
                counts.getOrDefault("DISAPPROVE", 0),
                counts.getOrDefault("NEUTRAL", 0)
        );
    }

    @Override
    public ProjectOpinion findByProjectIdAndUserId(int projectId, int userId) {
        if (projectId <= 0 || userId <= 0) return null;
        return projectOpinionDao.findByProjectIdAndUserId(projectId, userId);
    }
}
