package br.com.fai.Vox.domain.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HotspotDetailDto {

    private List<HotspotItemDto> issues;
    private List<HotspotItemDto> projects;

    public HotspotDetailDto() {}

    public HotspotDetailDto(List<HotspotItemDto> issues, List<HotspotItemDto> projects) {
        this.issues = issues;
        this.projects = projects;
    }

    @Getter
    @Setter
    public static class HotspotItemDto {
        private int id;
        private String title;
        private String status;
        private String category;
        private String neighborhood;

        public HotspotItemDto() {}

        public HotspotItemDto(int id, String title, String status,
                              String category, String neighborhood) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.category = category;
            this.neighborhood = neighborhood;
        }
    }
}
