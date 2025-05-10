package org.example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(Constants.FRONTEND_PATH)
                        .allowedMethods(Constants.ALLOWED_HTTP_METHODS)
                        .allowedHeaders(Constants.ALLOWED_HTTP_HEADERS)
                        .allowCredentials(true);
            }
        };
    }
}
