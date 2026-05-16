package br.com.fai.Vox.implementation.service.drive;

import br.com.fai.Vox.port.service.drive.GoogleDriveService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleDriveServiceImpl implements GoogleDriveService {

    private static final String APPLICATION_NAME = "PFC-Vox";
    private static final String CREDENTIALS_FILE = "/credentials.json";
    private static final String FOLDER_ID = "1gfvTwFOiPrEuu9siK6Y1jZCHF30rYAfk";

    private Drive buildDriveService() throws IOException, GeneralSecurityException {
        InputStream credentialsStream = getClass().getResourceAsStream(CREDENTIALS_FILE);
        if (credentialsStream == null) {
            throw new RuntimeException("Arquivo credentials.json não encontrado em resources.");
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        try {
            Drive drive = buildDriveService();

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream()
            );

            File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");
            drive.permissions().create(uploadedFile.getId(), permission).execute();

            return "https://drive.google.com/uc?id=" + uploadedFile.getId();
        } catch (GeneralSecurityException e) {
            throw new IOException("Erro ao autenticar com o Google Drive.", e);
        }
    }
}
