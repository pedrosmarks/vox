package br.com.fai.Vox.port.service.drive;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String fileName) throws IOException;
}
