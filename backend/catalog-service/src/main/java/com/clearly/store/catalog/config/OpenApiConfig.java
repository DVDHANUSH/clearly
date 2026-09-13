package com.clearly.store.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI clearlyStoreOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Clearly Store Catalog API")
                .version("1.0.0")
                .description("Interactive API for the Clearly Store product catalogue and admin data")
                .contact(new Contact().name("Clearly Store")))
            .tags(List.of(
                new Tag().name("Catalog").description("Combined catalog metadata and bulk catalog updates"),
                new Tag().name("Products").description("Product listing, details, creation and updates"),
                new Tag().name("Brands").description("Brand management"),
                new Tag().name("Categories").description("Main category management"),
                new Tag().name("Subcategories").description("Subcategory management and category filtering"),
                new Tag().name("Brand–Category Mapping").description("Relationships between brands and the categories they supply"),
                new Tag().name("Product Images").description("Product carousel image management"),
                new Tag().name("Media Uploads").description("Image and document uploads"),
                new Tag().name("Packages & Units").description("Product package sizes and measurement units"),
                new Tag().name("Discounts").description("Product and package bulk-discount tiers"),
                new Tag().name("Product Documents").description("Product documentation and attachments"),
                new Tag().name("Product Specifications").description("Structured product specifications"),
                new Tag().name("Delivery Estimates").description("PIN code validation and expected delivery windows")
            ));
    }
}
