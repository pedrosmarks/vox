package br.com.fai.Vox.port.service.issueimage;

import br.com.fai.Vox.domain.IssueImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IssueImageService {
    int create(int issueId, MultipartFile file);
    void delete(int imageId);
    List<IssueImage> findByIssueId(int issueId);
}
