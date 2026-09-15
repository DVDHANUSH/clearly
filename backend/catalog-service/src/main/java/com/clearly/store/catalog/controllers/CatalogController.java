package com.clearly.store.catalog.controllers;

import com.clearly.store.catalog.services.CatalogService;
import com.clearly.store.catalog.services.DeliveryEstimateService;
import com.clearly.store.catalog.services.ProductImageNormalizer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;
    private final DeliveryEstimateService deliveryEstimateService;
    public CatalogController(CatalogService catalogService, DeliveryEstimateService deliveryEstimateService) {
        this.catalogService = catalogService;
        this.deliveryEstimateService = deliveryEstimateService;
    }
    @Operation(summary = "Estimate delivery by PIN code", description = "Validates an Indian PIN code and returns the store's estimated delivery window", tags = {"Delivery Estimates"})
    @CrossOrigin(origins = "*")
    @GetMapping("/delivery-estimate") public Object deliveryEstimate(@RequestParam String pincode) { return deliveryEstimateService.estimate(pincode); }
    @Operation(summary = "List products", tags = {"Products"})
    @GetMapping("/products") public Object products() { return catalogService.findAll(); }
    @Operation(summary = "Get one product", description = "Accepts either a numeric database id or a stable product code such as PROD-2", tags = {"Products"})
    @GetMapping("/products/{id}") public Object product(@PathVariable String id) { return catalogService.findOne(id); }
    @Operation(summary = "Get catalog metadata and products", tags = {"Catalog"})
    @GetMapping("/catalog") public Object catalog() { return catalogService.catalog(); }
    @Operation(summary = "Get public homepage content", tags = {"Homepage"})
    @GetMapping("/homepage") public Object homepage() { return catalogService.homepage(); }
    @Operation(summary = "Update homepage content", tags = {"Homepage"})
    @PutMapping("/homepage") public Object saveHomepage(@RequestBody Map<String,Object> payload) { return catalogService.saveHomepage(payload); }
    @Operation(summary = "Save brands and categories", tags = {"Catalog"})
    @PutMapping("/catalog") public Object saveCatalog(@RequestBody Map<String,Object> payload) { return catalogService.saveCatalog(payload); }
    @Operation(summary = "Create a brand", tags = {"Brands"})
    @PostMapping("/brands") public Object createBrand(@RequestBody Map<String,Object> payload) { return catalogService.createBrand(payload); }
    @Operation(summary = "List brands", tags = {"Brands"})
    @GetMapping("/brands") public Object brands() { return catalogService.brands(); }
    @Operation(summary = "Update a brand", tags = {"Brands"})
    @PutMapping("/brands/{id}") public Object updateBrand(@PathVariable Integer id,@RequestBody Map<String,Object> payload) { return catalogService.updateBrand(id,payload); }
    @Operation(summary = "Delete a brand", tags = {"Brands"})
    @DeleteMapping("/brands/{id}") public Object deleteBrand(@PathVariable Integer id) { return catalogService.deleteBrand(id); }
    @Operation(summary = "Create a main category", tags = {"Categories"})
    @PostMapping("/categories") public Object createCategory(@RequestBody Map<String,Object> payload) { return catalogService.createCategory(payload); }
    @Operation(summary = "List main categories", tags = {"Categories"})
    @GetMapping("/categories") public Object categories() { return catalogService.categories(); }
    @Operation(summary = "Update a main category", tags = {"Categories"})
    @PutMapping("/categories/{id}") public Object updateCategory(@PathVariable Integer id,@RequestBody Map<String,Object> payload) { return catalogService.updateCategory(id,payload); }
    @Operation(summary = "Delete a main category", tags = {"Categories"})
    @DeleteMapping("/categories/{id}") public Object deleteCategory(@PathVariable Integer id) { return catalogService.deleteCategory(id); }
    @Operation(summary = "List subcategories", description = "Optionally filter by parent main category", tags = {"Subcategories"})
    @GetMapping("/subcategories") public Object subcategories(@RequestParam(required=false) Integer categoryId) { return catalogService.subcategories(categoryId); }
    @Operation(summary = "Create a subcategory", tags = {"Subcategories"})
    @PostMapping("/subcategories") public Object createSubcategory(@RequestBody Map<String,Object> payload) { return catalogService.createSubcategory(payload); }
    @Operation(summary = "Update a subcategory", tags = {"Subcategories"})
    @PutMapping("/subcategories/{id}") public Object updateSubcategory(@PathVariable Integer id,@RequestBody Map<String,Object> payload) { return catalogService.updateSubcategory(id,payload); }
    @Operation(summary = "Delete a subcategory", tags = {"Subcategories"})
    @DeleteMapping("/subcategories/{id}") public Object deleteSubcategory(@PathVariable Integer id) { return catalogService.deleteSubcategory(id); }
    @Operation(summary = "List brand/category relationships", tags = {"Brand–Category Mapping"})
    @GetMapping("/brand-categories") public Object brandCategories(@RequestParam(required=false) Integer brandId) { return catalogService.brandCategories(brandId); }
    @Operation(summary = "Create or update a brand/category relationship", tags = {"Brand–Category Mapping"})
    @PutMapping("/brand-categories") public Object saveBrandCategory(@RequestBody Map<String,Object> payload) { return catalogService.saveBrandCategory(payload); }
    @Operation(summary = "Delete a brand/category relationship", tags = {"Brand–Category Mapping"})
    @DeleteMapping("/brand-categories/{brandId}/{categoryId}") public Object deleteBrandCategory(@PathVariable Integer brandId,@PathVariable Integer categoryId) { return catalogService.deleteBrandCategory(brandId,categoryId); }
    @Operation(summary = "Create a product", tags = {"Products"})
    @PostMapping("/products") public Object create(@RequestBody Map<String,Object> payload) { return catalogService.saveProduct(payload, null); }
    @Operation(summary = "Update a product", tags = {"Products"})
    @PutMapping("/products/{id}") public Object update(@PathVariable String id, @RequestBody Map<String,Object> payload) { return catalogService.saveProduct(payload, id); }
    @Operation(summary = "Upload a product image or document", tags = {"Media Uploads"})
    @PostMapping("/uploads") public Map<String,String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String originalName = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()).getFileName().toString();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean image = contentType.startsWith("image/");
        String safeName = image ? UUID.randomUUID() + ".jpg" : UUID.randomUUID() + "-" + originalName;
        Path dir = Paths.get("uploads"); Files.createDirectories(dir);
        try {
            if (image) ProductImageNormalizer.write(file.getBytes(), dir.resolve(safeName));
            else Files.write(dir.resolve(safeName), file.getBytes());
        } catch (java.io.IOException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a valid PNG, JPG, or WebP image.");
        }
        return Map.of("url", "uploads/" + safeName, "name", safeName);
    }
    @Operation(summary = "Serve an uploaded image or document", tags = {"Media Uploads"})
    @CrossOrigin(origins = "*")
    @GetMapping("/uploads/{filename}") public org.springframework.http.ResponseEntity<byte[]> uploadedFile(@PathVariable String filename) throws Exception {
        Path directory = Paths.get("uploads").toAbsolutePath().normalize();
        Path file = directory.resolve(Path.of(filename).getFileName()).normalize();
        if (!file.startsWith(directory) || !Files.exists(file)) return org.springframework.http.ResponseEntity.notFound().build();
        String contentType = Files.probeContentType(file);
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType == null ? "application/octet-stream" : contentType))
                .body(Files.readAllBytes(file));
    }
    @Operation(summary = "List product images", tags = {"Product Images"})
    @GetMapping("/products/{productId}/images") public Object images(@PathVariable String productId) { return catalogService.images(productId); }
    @Operation(summary = "Add a carousel image", tags = {"Product Images"})
    @PostMapping("/products/{productId}/images") public Object addImage(@PathVariable String productId, @RequestBody Map<String,Object> payload) { return catalogService.addImage(productId, payload); }
    @Operation(summary = "Update image order or alt text", tags = {"Product Images"})
    @PutMapping("/images/{imageId}") public Object updateImage(@PathVariable Integer imageId, @RequestBody Map<String,Object> payload) { return catalogService.updateImage(imageId, payload); }
    @Operation(summary = "Delete a carousel image", tags = {"Product Images"})
    @DeleteMapping("/images/{imageId}") public Object deleteImage(@PathVariable Integer imageId) { return catalogService.deleteImage(imageId); }
    @Operation(summary = "List available package sizes", tags = {"Packages & Units"})
    @GetMapping("/products/{productId}/packages") public Object packages(@PathVariable String productId) { return catalogService.packages(productId); }
    @Operation(summary = "List measurement units", tags = {"Packages & Units"})
    @GetMapping("/measurement-units") public Object units() { return catalogService.units(); }
    @Operation(summary = "Add a package size", tags = {"Packages & Units"})
    @PostMapping("/products/{productId}/packages") public Object addPackage(@PathVariable String productId, @RequestBody Map<String,Object> payload) { return catalogService.addPackage(productId, payload); }
    @Operation(summary = "Update a package size", tags = {"Packages & Units"})
    @PutMapping("/packages/{packageId}") public Object updatePackage(@PathVariable Integer packageId, @RequestBody Map<String,Object> payload) { return catalogService.updatePackage(packageId, payload); }
    @Operation(summary = "Delete a package size", tags = {"Packages & Units"})
    @DeleteMapping("/packages/{packageId}") public Object deletePackage(@PathVariable Integer packageId) { return catalogService.deletePackage(packageId); }
    @Operation(summary = "List bulk discount tiers", tags = {"Discounts"})
    @GetMapping("/products/{productId}/discounts") public Object discounts(@PathVariable String productId) { return catalogService.discounts(productId); }
    @Operation(summary = "Add a bulk discount tier", tags = {"Discounts"})
    @PostMapping("/products/{productId}/discounts") public Object addDiscount(@PathVariable String productId, @RequestBody Map<String,Object> payload) { return catalogService.addDiscount(productId, payload); }
    @Operation(summary = "Update or toggle a bulk discount tier", tags = {"Discounts"})
    @PutMapping("/discounts/{discountId}") public Object updateDiscount(@PathVariable Integer discountId, @RequestBody Map<String,Object> payload) { return catalogService.updateDiscount(discountId, payload); }
    @Operation(summary = "Delete a bulk discount tier", tags = {"Discounts"})
    @DeleteMapping("/discounts/{discountId}") public Object deleteDiscount(@PathVariable Integer discountId) { return catalogService.deleteDiscount(discountId); }
    @Operation(summary = "List discounts for one package", tags = {"Discounts"})
    @GetMapping("/packages/{packageId}/discounts") public Object packageDiscounts(@PathVariable Integer packageId) { return catalogService.packageDiscounts(packageId); }
    @Operation(summary = "Add a discount to one package", tags = {"Discounts"})
    @PostMapping("/packages/{packageId}/discounts") public Object addPackageDiscount(@PathVariable Integer packageId,@RequestBody Map<String,Object> payload) { return catalogService.addPackageDiscount(packageId,payload); }
    @Operation(summary = "List product documents", tags = {"Product Documents"})
    @GetMapping("/products/{productId}/documents") public Object documents(@PathVariable String productId) { return catalogService.documents(productId); }
    @Operation(summary = "Attach a product document", tags = {"Product Documents"})
    @PostMapping("/products/{productId}/documents") public Object addDocument(@PathVariable String productId, @RequestBody Map<String,Object> payload) { return catalogService.addDocument(productId, payload); }
    @Operation(summary = "Update a product document", tags = {"Product Documents"})
    @PutMapping("/documents/{documentId}") public Object updateDocument(@PathVariable Integer documentId,@RequestBody Map<String,Object> payload) { return catalogService.updateDocument(documentId,payload); }
    @Operation(summary = "Delete a product document", tags = {"Product Documents"})
    @DeleteMapping("/documents/{documentId}") public Object deleteDocument(@PathVariable Integer documentId) { return catalogService.deleteDocument(documentId); }
    @Operation(summary = "List product specifications", tags = {"Product Specifications"})
    @GetMapping("/products/{productId}/specifications") public Object specifications(@PathVariable String productId) { return catalogService.specifications(productId); }
    @Operation(summary = "Add a product specification", tags = {"Product Specifications"})
    @PostMapping("/products/{productId}/specifications") public Object addSpecification(@PathVariable String productId,@RequestBody Map<String,Object> payload) { return catalogService.addSpecification(productId,payload); }
    @Operation(summary = "Update a product specification", tags = {"Product Specifications"})
    @PutMapping("/specifications/{specificationId}") public Object updateSpecification(@PathVariable Integer specificationId,@RequestBody Map<String,Object> payload) { return catalogService.updateSpecification(specificationId,payload); }
    @Operation(summary = "Delete a product specification", tags = {"Product Specifications"})
    @DeleteMapping("/specifications/{specificationId}") public Object deleteSpecification(@PathVariable Integer specificationId) { return catalogService.deleteSpecification(specificationId); }
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<Map<String,String>> invalidInput(IllegalArgumentException error) {
        return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }
}
