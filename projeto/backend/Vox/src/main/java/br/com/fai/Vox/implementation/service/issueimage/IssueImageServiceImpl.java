package br.com.fai.Vox.implementation.service.issueimage;

import br.com.fai.Vox.domain.IssueImage;
import br.com.fai.Vox.port.dao.issueimage.IssueImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.issueimage.IssueImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class IssueImageServiceImpl implements IssueImageService {

    private static final Logger logger = Logger.getLogger(IssueImageServiceImpl.class.getName());

    private final IssueImageDao issueImageDao;
    private final CloudinaryService cloudinaryService;

    public IssueImageServiceImpl(IssueImageDao issueImageDao, CloudinaryService cloudinaryService) {
        this.issueImageDao = issueImageDao;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public int create(int issueId, MultipartFile file) {
        if (issueId <= 0 || file == null || file.isEmpty()) return -1;
        try {
            String fileName = "issue_" + issueId + "_" + file.getOriginalFilename();
            String url = cloudinaryService.uploadFile(file, fileName);
            IssueImage image = new IssueImage();
            image.setIssueId(issueId);
            image.setUrl(url);
            int id = issueImageDao.create(image);
            logger.log(Level.INFO, "IssueImage salva. ID: " + id);
            return id;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao fazer upload da imagem para issue ID: " + issueId, e);
            return -1;
        }
    }

    @Override
    public void delete(int imageId) {
        if (imageId <= 0) return;
        issueImageDao.delete(imageId);
    }

    @Override
    public List<IssueImage> findByIssueId(int issueId) {
        if (issueId <= 0) return List.of();
        return issueImageDao.findByIssueId(issueId);
    }
}
