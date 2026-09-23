package br.com.fai.Vox.implementation.service.eventimage;

import br.com.fai.Vox.domain.EventImage;
import br.com.fai.Vox.port.dao.eventimage.EventImageDao;
import br.com.fai.Vox.port.service.drive.CloudinaryService;
import br.com.fai.Vox.port.service.eventimage.EventImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EventImageServiceImpl implements EventImageService {

    private static final Logger logger = Logger.getLogger(EventImageServiceImpl.class.getName());

    private final EventImageDao eventImageDao;
    private final CloudinaryService cloudinaryService;

    public EventImageServiceImpl(EventImageDao eventImageDao, CloudinaryService cloudinaryService) {
        this.eventImageDao = eventImageDao;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public int create(int eventId, MultipartFile file) {
        if (eventId <= 0 || file == null || file.isEmpty()) return -1;
        try {
            String fileName = "event_" + eventId + "_" + file.getOriginalFilename();
            String url = cloudinaryService.uploadFile(file, fileName);

            EventImage image = new EventImage();
            image.setEventId(eventId);
            image.setUrl(url);
            int id = eventImageDao.create(image);
            logger.log(Level.INFO, "Imagem salva para o evento ID: " + eventId);
            return id;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro ao processar imagem do evento ID: " + eventId, e);
            return -1;
        }
    }

    @Override
    public List<EventImage> findByEventId(int eventId) {
        if (eventId <= 0) return List.of();
        return eventImageDao.findByEventId(eventId);
    }

    @Override
    public void delete(int imageId) {
        if (imageId <= 0) return;
        eventImageDao.delete(imageId);
    }
}
