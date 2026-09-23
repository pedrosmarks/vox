package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Análise por categoria: ranking de categorias com mais issues/projetos e
 * cruzamento categoria × status. Ajuda a direcionar política pública ("onde
 * o município mais precisa agir"). Escopado por município e intervalo de datas.
 */
@Getter
@Setter
public class CategoryAnalysisDto {

    private List<CategoryStatDto> categories;

    public CategoryAnalysisDto() {}

    public CategoryAnalysisDto(List<CategoryStatDto> categories) {
        this.categories = categories;
    }

    @Getter
    @Setter
    public static class CategoryStatDto {
        private int categoryId;
        private String categoryName;
        private long issueCount;
        private long projectCount;
        private long total;

        /** Contagem de issues por status na categoria (issue_status -> total). */
        private Map<String, Long> issuesByStatus;

        /** Contagem de projetos por status na categoria (project_status -> total). */
        private Map<String, Long> projectsByStatus;

        /** Issues abertas (status OPEN) na categoria — proxy de "onde agir". */
        private long openIssues;

        public CategoryStatDto() {}

        public CategoryStatDto(int categoryId, String categoryName,
                               long issueCount, long projectCount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.issueCount = issueCount;
            this.projectCount = projectCount;
            this.total = issueCount + projectCount;
        }
    }
}
