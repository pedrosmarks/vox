package br.com.fai.Vox.domain.dto;

import br.com.fai.Vox.domain.IssueReport;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateIssueReportDto {

    private Integer municipalityId;   // preenchido pelo backend via token
    private Integer authorId;         // preenchido pelo backend via token
    private Integer councilorId;
    private String title;
    private String description;
    private String neighborhood;
    private String street;
    private String number;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private MultipartFile file;
}
