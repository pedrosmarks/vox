package br.com.fai.Vox.port.service.tools;

import java.io.IOException;

public interface ResourceFileService {
    String read(final String resourcePath) throws IOException;
}
