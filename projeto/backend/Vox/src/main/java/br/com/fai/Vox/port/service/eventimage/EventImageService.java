package br.com.fai.Vox.port.service.eventimage;

import br.com.fai.Vox.domain.EventImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventImageService {
    int create(int eventId, MultipartFile file);
    List<EventImage> findByEventId(int eventId);
    void delete(int imageId);
}
