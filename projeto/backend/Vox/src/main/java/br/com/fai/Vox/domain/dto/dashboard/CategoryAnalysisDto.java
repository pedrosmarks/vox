package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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

        private Map<String, Long> issuesByStatus;

        private Map<String, Long> projectsByStatus;

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
