package rocks.vinu.demo.es.config;


import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String INFO_URL = "http://api.vinu.rocks/";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.ant("/")))
                .paths(Predicates.or(PathSelectors.ant("/**")))
                .paths(Predicates.not(PathSelectors.ant("/error")))
                .build()
                .apiInfo(apiInfo());
    }
    private ApiInfo apiInfo() {

        Contact contact = new Contact("Admin", INFO_URL, "vinu@xminds.com");

        ApiInfo apiInfo = new ApiInfo(
                "ES Demo Api",
                "ES Demo Api with percolator index",
                "Unversioned",
                INFO_URL,
                contact,
                "VERY STRICT",
                INFO_URL,
                new ArrayList<>());
        return apiInfo;
    }
}
