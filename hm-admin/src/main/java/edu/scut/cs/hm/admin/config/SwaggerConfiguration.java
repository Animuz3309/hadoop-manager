package edu.scut.cs.hm.admin.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import edu.scut.cs.hm.common.utils.AppInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Date;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket newsApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        return docket
                .groupName("DockMaster")
                .apiInfo(apiInfo())
                //it need for correct samples of date
                .directModelSubstitute(Date.class, Long.class)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(makePathRegexp())
                .build();
    }

    private Predicate<String> makePathRegexp() {
        return Predicates.not(PathSelectors.regex("/error"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("hm API")
                .description("A Docker based hadoop manager tool ")
                .contact(new Contact("SCUT LWW336", "#", "#"))
                .version("server version: " + AppInfo.getApplicationVersion() + ", revision: " + AppInfo.getBuildRevision())
                .build();
    }
}
