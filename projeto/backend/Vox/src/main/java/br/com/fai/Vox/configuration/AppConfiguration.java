package br.com.fai.Vox.configuration;

import br.com.fai.Vox.implementation.service.drive.GoogleDriveServiceImpl;
import br.com.fai.Vox.port.service.drive.GoogleDriveService;
import br.com.fai.Vox.implementation.dao.category.CategoryPostgresDaoImpl;
import br.com.fai.Vox.implementation.dao.municipality.MunicipalityPostgresDaoImpl;
import br.com.fai.Vox.implementation.dao.project.ProjectPostgresDaoImpl;
import br.com.fai.Vox.implementation.dao.projectimage.ProjectImagePostgresDaoImpl;
import br.com.fai.Vox.implementation.dao.user.UserPostgresDaoImpl;
import br.com.fai.Vox.implementation.service.authentication.BasicAuthenticationServiceImpl;
import br.com.fai.Vox.implementation.service.authentication.JwtAuthenticationServiceImpl;
import br.com.fai.Vox.port.dao.category.CategoryDao;
import br.com.fai.Vox.port.dao.municipality.MunicipalityDao;
import br.com.fai.Vox.port.dao.project.ProjectDao;
import br.com.fai.Vox.port.dao.projectimage.ProjectImageDao;
import br.com.fai.Vox.port.dao.user.UserDao;
import br.com.fai.Vox.port.service.authentication.AuthenticationService;
import br.com.fai.Vox.port.service.user.UserService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.sql.Connection;

@Configuration
public class AppConfiguration {

    private final Environment environment;

    public AppConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    //    http://localhost:8080/swagger-ui/index.html
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI().info(new Info().title("LIFE BANK").version("0.0.1").description("API - LIFE BANK"));
    }

    @Bean
    public GoogleDriveService getGoogleDriveService() {
        return new GoogleDriveServiceImpl();
    }

    @Bean
    public UserDao getUserFakeDao(final Connection connection) {
        return new UserPostgresDaoImpl(connection);
    }

    @Bean
    public MunicipalityDao getMunicipalityFakeDao(final Connection connection) {
        return new MunicipalityPostgresDaoImpl(connection);
    }

    @Bean
    public ProjectDao getProjectFakeDao(final Connection connection) {
        return new ProjectPostgresDaoImpl(connection);
    }

    @Bean
    public CategoryDao getCategoryFakeDao(final Connection connection) {
        return new CategoryPostgresDaoImpl(connection);
    }

    @Bean
    public ProjectImageDao getProjectImageFakeDao(final Connection connection) {
        return new ProjectImagePostgresDaoImpl(connection);
    }

    @Bean
    @Profile("basic")
    public AuthenticationService basicAutheticationService(final UserService userService) {
        return new BasicAuthenticationServiceImpl(userService);
    }

    @Bean
    @Profile("jwt")
    public AuthenticationService jwtAutheticationService(final UserService userService, final PasswordEncoder passwordEncoder) {
        return new JwtAuthenticationServiceImpl(userService, passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
