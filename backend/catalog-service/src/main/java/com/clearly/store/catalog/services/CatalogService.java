package com.clearly.store.catalog.services;

import com.clearly.store.catalog.repositories.CatalogRepository;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CatalogService {
    private final CatalogRepository catalogRepository;
    public CatalogService(CatalogRepository catalogRepository) { this.catalogRepository = catalogRepository; }
    public Object findAll() { return catalogRepository.findAll(); }
    public Object findOne(String id) { return catalogRepository.findOne(id); }
    public Object catalog() { return catalogRepository.catalog(); }
    public Object homepage() { return catalogRepository.homepage(); }
    public Object saveHomepage(Map<String,Object> payload) { return catalogRepository.saveHomepage(payload); }
    public Object saveCatalog(Map<String,Object> payload) { return catalogRepository.saveCatalog(payload); }
    public Object brands() { return catalogRepository.brands(); }
    public Object createBrand(Map<String,Object> payload) { return catalogRepository.createBrand(payload); }
    public Object updateBrand(Integer id, Map<String,Object> payload) { return catalogRepository.updateBrand(id, payload); }
    public Object deleteBrand(Integer id) { return catalogRepository.deleteBrand(id); }
    public Object categories() { return catalogRepository.categories(); }
    public Object createCategory(Map<String,Object> payload) { return catalogRepository.createCategory(payload); }
    public Object updateCategory(Integer id, Map<String,Object> payload) { return catalogRepository.updateCategory(id, payload); }
    public Object deleteCategory(Integer id) { return catalogRepository.deleteCategory(id); }
    public Object subcategories(Integer categoryId) { return catalogRepository.subcategories(categoryId); }
    public Object createSubcategory(Map<String,Object> payload) { return catalogRepository.createSubcategory(payload); }
    public Object updateSubcategory(Integer id, Map<String,Object> payload) { return catalogRepository.updateSubcategory(id, payload); }
    public Object deleteSubcategory(Integer id) { return catalogRepository.deleteSubcategory(id); }
    public Object brandCategories(Integer brandId) { return catalogRepository.brandCategories(brandId); }
    public Object saveBrandCategory(Map<String,Object> payload) { return catalogRepository.saveBrandCategory(payload); }
    public Object deleteBrandCategory(Integer brandId,Integer categoryId) { return catalogRepository.deleteBrandCategory(brandId,categoryId); }
    public Object saveProduct(Map<String,Object> payload, String id) { return catalogRepository.saveProduct(payload, id); }
    public Object images(String id){return catalogRepository.images(id);} public Object addImage(String id,Map<String,Object> p){return catalogRepository.addImage(id,p);} public Object updateImage(Integer id,Map<String,Object> p){return catalogRepository.updateImage(id,p);} public Object deleteImage(Integer id){return catalogRepository.deleteImage(id);}
    public Object packages(String id){return catalogRepository.packages(id);} public Object addPackage(String id,Map<String,Object> p){return catalogRepository.addPackage(id,p);} public Object updatePackage(Integer id,Map<String,Object> p){return catalogRepository.updatePackage(id,p);} public Object deletePackage(Integer id){return catalogRepository.deletePackage(id);}
    public Object units(){return catalogRepository.units();}
    public Object discounts(String id){return catalogRepository.discounts(id);} public Object addDiscount(String id,Map<String,Object> p){return catalogRepository.addDiscount(id,p);} public Object updateDiscount(Integer id,Map<String,Object> p){return catalogRepository.updateDiscount(id,p);} public Object deleteDiscount(Integer id){return catalogRepository.deleteDiscount(id);}
    public Object packageDiscounts(Integer id){return catalogRepository.packageDiscounts(id);} public Object addPackageDiscount(Integer id,Map<String,Object> p){return catalogRepository.addPackageDiscount(id,p);}
    public Object documents(String id){return catalogRepository.documents(id);} public Object addDocument(String id,Map<String,Object> p){return catalogRepository.addDocument(id,p);} public Object updateDocument(Integer id,Map<String,Object> p){return catalogRepository.updateDocument(id,p);} public Object deleteDocument(Integer id){return catalogRepository.deleteDocument(id);}
    public Object specifications(String id){return catalogRepository.specifications(id);} public Object addSpecification(String id,Map<String,Object> p){return catalogRepository.addSpecification(id,p);} public Object updateSpecification(Integer id,Map<String,Object> p){return catalogRepository.updateSpecification(id,p);} public Object deleteSpecification(Integer id){return catalogRepository.deleteSpecification(id);}
}
