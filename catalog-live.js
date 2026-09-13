(function(){
  'use strict';
  if(typeof PRODUCTS==='undefined')return;
  var base=(location.protocol==='file:'||location.port==='4173')?'http://localhost:8080/api':'/api';
  function normalize(api){
    var fallback=PRODUCTS.find(function(item){return String(item.name).toLowerCase()===String(api.name).toLowerCase();})||{};
    var packages=(api.packages||[]).filter(function(item){return item.enabled!==false;}).map(function(item){var mrp=Number(item.originalPrice||item.price||0),price=Number(item.price||0);return Object.assign({},item,{label:item.label||((item.quantity||'')+' '+(item.symbol||item.unit||'')),price:price,originalPrice:mrp,discountPercent:mrp>price?Math.round((1-price/mrp)*10000)/100:0});});
    var defaultPackage=packages.find(function(item){return item.isDefault;})||packages[0];
    return Object.assign({},fallback,api,{id:fallback.id||api.productCode||String(api.id),productRef:api.productCode||fallback.id||String(api.id),databaseId:String(api.id||''),brand:api.brand||fallback.brand,category:api.subcategory||api.category||fallback.category,image:api.image||fallback.image,packages:packages,sizes:packages.length?packages.map(function(item){return item.label;}):(fallback.sizes||[]),price:defaultPackage?defaultPackage.price:Number(api.price||fallback.price||0),originalPrice:defaultPackage?defaultPackage.originalPrice:Number(api.originalPrice||fallback.originalPrice||0)});
  }
  function load(){return fetch(base+'/products',{cache:'no-store'}).then(function(response){if(!response.ok)throw new Error('Catalogue unavailable');return response.json();}).then(function(rows){var normalized=(rows||[]).map(normalize);PRODUCTS.splice.apply(PRODUCTS,[0,PRODUCTS.length].concat(normalized));normalized.forEach(function(product){if(product.brandName&&product.brand)BRANDS[product.brand]=product.brandName;});window.dispatchEvent(new CustomEvent('clearly-catalog-loaded',{detail:{products:PRODUCTS}}));return PRODUCTS;});}
  window.ClearlyRefreshCatalog=load;
  window.ClearlyCatalogReady=load().catch(function(){return PRODUCTS;});
  window.addEventListener('storage',function(event){if(event.key==='clearly_catalog_revision')load().catch(function(){});});
})();
