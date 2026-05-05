package br.com.fai.Vox.implementation.service.tools;

import br.com.fai.Vox.port.service.tools.ResourceFileService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class ResourceFileServiceImpl implements ResourceFileService {
    @Override
    public String read(String resourcePath) throws IOException {
        final ClassLoader classLoader = ResourceFileServiceImpl.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new RuntimeException("Arquivo nao encontrado.");
        }

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String content = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content += line;
        }

        return content;
    }
}
