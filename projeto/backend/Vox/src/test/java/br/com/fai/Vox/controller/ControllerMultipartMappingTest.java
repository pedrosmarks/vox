package br.com.fai.Vox.controller;

import br.com.fai.Vox.domain.IssueReport;
import br.com.fai.Vox.domain.Project;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControllerMultipartMappingTest {

    @Test
    void projectUpdateEndpoint_acceptsMultipartFormData() throws NoSuchMethodException {
        Method method = ProjectRestController.class.getDeclaredMethod("update", int.class, Project.class, HttpServletRequest.class);
        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/{id}"}, mapping.value());
        assertArrayEquals(new String[]{"multipart/form-data"}, mapping.consumes());
    }

    @Test
    void issueReportUpdateEndpoint_acceptsMultipartFormData() throws NoSuchMethodException {
        Method method = IssueReportRestController.class.getDeclaredMethod("update", int.class, IssueReport.class, HttpServletRequest.class);
        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[]{"/{id}"}, mapping.value());
        assertArrayEquals(new String[]{"multipart/form-data"}, mapping.consumes());
    }
}
