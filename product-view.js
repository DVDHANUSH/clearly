(function(){
  var id=new URLSearchParams(location.search).get('id'), fallback=(typeof PRODUCTS!=='undefined'&&PRODUCTS.find(function(x){return x.id===id}))||PRODUCTS[0];
  function normalize(api, original){
    var p=Object.assign({},original||{},api||{});
    p.id=api.product_code||api.productCode||String(api.id||((original||{}).id||''));
    p.brand=api.brand_slug||api.brand||(original||{}).brand;
    p.category=api.category||(original||{}).category;
    p.image=api.image||api.image_url||(original||{}).image;
    p.images=api.images&&api.images.length?api.images.map(function(image){return typeof image==='string'?image:(image.imageUrl||image.image||'');}).filter(Boolean):(original||{}).images;
    p.packages=(api.packages||[]).filter(function(item){return item.enabled!==false;}).map(function(item){return Object.assign({},item,{price:Number(item.price||0),originalPrice:Number(item.originalPrice||item.price||0)});});
    var defaultPackage=p.packages.find(function(item){return item.isDefault;})||p.packages[0];
    p.price=defaultPackage?defaultPackage.price:Number(api.price||p.price||0);
    p.originalPrice=defaultPackage?defaultPackage.originalPrice:(api.originalPrice!=null?api.originalPrice:api.original_price);
    p.use=api.description||api.use||(original||{}).use;
    p.howToUse=api.howToUse||api.how_to_use||(original||{}).howToUse;
    p.sizes=p.packages.length?p.packages.map(function(item){return item.label;}):(api.sizes&&api.sizes.length?api.sizes:(original||{}).sizes||[]);
    p.bulkDiscountEnabled=api.bulk_discount_enabled!==false;
    p.bulkDiscounts=api.bulkDiscounts||(original||{}).bulkDiscounts||[];
    return p;
  }
  function renderProduct(p){
  var brand=BRANDS[p.brand]||p.brand;
  document.title=p.name+' — Clearly.';
  document.getElementById('brand').textContent=brand; document.getElementById('name').textContent=p.name;
  var heroImages={'FOS-RG-012':'assets/brushbond-roofguard-studio.png','FOS-AQ-019':'assets/brushbond-aquaprotect-studio.png'};
  document.getElementById('category').textContent=p.category; document.getElementById('product-image').src=heroImages[p.id]||p.image; document.getElementById('product-image').alt=p.name;
  document.getElementById('description').textContent='A dependable '+p.category.toLowerCase()+' solution from '+brand+', designed for powerful everyday cleaning with a fresh, clean finish.';
  if(p.use){var packContent=p.packRows?'<table class="pack-table"><thead><tr><th>Pack size</th><th>Powder</th><th>Liquid</th><th>Water</th></tr></thead><tbody>'+p.packRows.map(function(r){return '<tr>'+r.map(function(c){return '<td>'+c+'</td>';}).join('')+'</tr>';}).join('')+'</tbody></table>':('<p>'+(p.packInfo||('Available in '+p.sizes.join(', ')+'. Add '+p.packWater+' clean potable water per 1 kg pack.'))+'</p>');var useContent=p.howToItems?'<div class="use-grid">'+p.howToItems.map(function(x,i){return '<div class="use-item"><span class="use-number">'+(i+1)+'</span><span class="use-icon">'+x[0]+'</span><div><b>'+x[1]+'</b><small>'+x[2]+'</small></div></div>';}).join('')+'</div>':'<p>'+p.howToUse+'</p>';var careContent=p.careItems?'<div class="care-grid">'+p.careItems.map(function(x){return '<div class="care-item"><span>'+x[0]+'</span><div><b>'+x[1]+'</b><small>'+x[2]+'</small></div></div>';}).join('')+'</div>':'<p>'+p.care+'</p>';document.getElementById('description').textContent=p.use;document.getElementById('buyer-info').innerHTML='<h3>How to use</h3>'+useContent+'<h3>Pack & mixing</h3>'+packContent+'<h3>Safety & care</h3>'+careContent;if(p.brand==='diversey'){var visualSteps=['measure-dilute','apply-clean','rinse-dry'];Array.from(document.querySelectorAll('#buyer-info .use-item')).slice(0,3).forEach(function(item,i){var icon=item.querySelector('.use-icon');if(icon){icon.textContent='';var image=document.createElement('img');image.className='use-illustration';image.src='assets/usage-icons/'+visualSteps[i]+'.png';image.alt=item.querySelector('b').textContent+' illustration';icon.appendChild(image);}});var packHeading=Array.from(document.querySelectorAll('#buyer-info h3')).find(function(h){return h.textContent==='Pack & mixing';});if(packHeading){var packImage=document.createElement('img');packImage.className='pack-illustration';packImage.src='assets/usage-icons/pack-mixing.png';packImage.alt='Pack and mixing illustration';packHeading.insertAdjacentElement('afterend',packImage);}}}
  if(p.brand==='diversey'){var packHeadingToRemove=Array.from(document.querySelectorAll('#buyer-info h3')).find(function(h){return h.textContent==='Pack & mixing';});if(packHeadingToRemove){var removeNode=packHeadingToRemove;while(removeNode&&!(removeNode!==packHeadingToRemove&&removeNode.tagName==='H3')){var nextNode=removeNode.nextElementSibling;removeNode.remove();removeNode=nextNode;}}}
  document.getElementById('crumb').innerHTML='<a href="products.html">Products</a><span>/</span><a href="products.html#'+p.brand+'">'+brand+'</a><span>/</span><strong>'+p.name+'</strong>';
  document.getElementById('rating').textContent='★ '+(p.rating||'—')+'  ·  Highly rated';
  if(!p.rating) document.getElementById('rating').hidden=true;

  function cleanText(value){return String(value==null?'':value).replace(/<[^>]*>/g,'').replace(/\s+/g,' ').trim();}
  var sizeText=(p.sizes||[]).join(', ')||'Refer to product label';
  var packText=p.packRows?p.packRows.map(function(row){return row.join(' · ');}).join('; '):(p.packaging||sizeText);
  var specRows=[
    ['Item group',p.category||'General care'],
    ['Brand',brand],
    ['Product purpose',p.purpose||p.use||'Product-specific cleaning or construction care'],
    ['Available sizes',sizeText],
  ];
  document.getElementById('product-spec-table').innerHTML='<table class="spec-table"><thead><tr><th>Specification</th><th>Value</th></tr></thead><tbody>'+specRows.map(function(row){return '<tr><th>'+row[0]+'</th><td>'+row[1]+'</td></tr>';}).join('')+'</tbody></table>';
  var documentLink=document.getElementById('download-document');
  var documentText=[
    p.name+' — Product Information Sheet',
    'Brand: '+brand,
    'Category: '+(p.category||'General care'),
    'Available sizes: '+sizeText,
    '',
    'PURPOSE',
    cleanText(p.use||p.purpose||'Please refer to the product label.'),
    '',
    'HOW TO USE',
    cleanText(p.howToUse||((p.howToItems||[]).map(function(x){return x[1]+': '+x[2];}).join(' | '))||'Follow the instructions on the product label.'),
    '',
    'PACK & MIXING',
    cleanText(p.packInfo||packText),
    '',
    'SAFETY & CARE',
    cleanText(p.care||((p.careItems||[]).map(function(x){return x[1]+': '+x[2];}).join(' | '))||'Follow the current product label and safety instructions.'),
    '',
    'SHIPPING & RETURNS',
    'Orders placed before 2 PM IST on business days are processed and dispatched within 24-48 hours, excluding weekends and public holidays.',
    'Products can be returned within 30 days of receipt. Packed products must be unopened and all returned items must be original and unused.'
  ].join('\n');
  var documentUrl=URL.createObjectURL(new Blob([documentText],{type:'text/plain;charset=utf-8'}));
  documentLink.href=documentUrl;
  documentLink.download=p.name.replace(/[^a-z0-9]+/gi,'-').replace(/^-|-$/g,'').toLowerCase()+'-product-information.txt';

  var packages=p.packages&&p.packages.length?p.packages:(p.sizes||[]).map(function(label,index){return {label:label,price:Number(p.price||0),originalPrice:Number(p.originalPrice||p.price||0),isDefault:index===0};});
  var selectedPackage=packages.find(function(item){return item.isDefault;})||packages[0]||{label:p.packaging||'Standard pack',price:Number(p.price||0),originalPrice:Number(p.originalPrice||p.price||0)};
  function updatePrice(){var price=Number(selectedPackage.price||0),mrp=Number(selectedPackage.originalPrice||price);document.getElementById('price').textContent='₹'+price.toLocaleString('en-IN');document.getElementById('original-price').textContent=mrp>price?'₹'+mrp.toLocaleString('en-IN'):'';document.getElementById('discount').textContent=mrp>price?Math.round((1-price/mrp)*100)+'% OFF':'';}
  updatePrice();
  var sizeBox=document.getElementById('sizes'); sizeBox.innerHTML=packages.map(function(pkg){return '<span tabindex="0" role="button" data-package-code="'+(pkg.packageCode||'')+'">'+pkg.label+'</span>';}).join('');
  var floorCleanerFragrances={
    'PROD-21':{label:'Lavender'},
    'PROD-24':{label:'Lemon'},
    'PROD-25':{label:'Rose'}
  };
  if(floorCleanerFragrances[p.id]){
    var fragranceBlock=document.createElement('div');fragranceBlock.className='detail-block fragrance-block';
    fragranceBlock.innerHTML='<h3>Choose fragrance</h3><div class="fragrance-options" role="group" aria-label="Choose fragrance">'+Object.keys(floorCleanerFragrances).map(function(code){var fragrance=floorCleanerFragrances[code];return '<button type="button" class="fragrance-option'+(code===p.id?' selected':'')+'" data-fragrance-product="'+code+'" aria-pressed="'+(code===p.id)+'">'+fragrance.label+'</button>';}).join('')+'</div>';
    var sizeBlock=sizeBox.closest('.detail-block');sizeBlock.parentNode.insertBefore(fragranceBlock,sizeBlock);
    fragranceBlock.addEventListener('click',function(event){var choice=event.target.closest('[data-fragrance-product]');if(!choice||choice.dataset.fragranceProduct===p.id)return;location.href='product.html?id='+encodeURIComponent(choice.dataset.fragranceProduct);});
  }
  var bulkQty=1, bulkDiscounts=[0,3.5,7,10.5], bulkPack=selectedPackage.label;
  var bulkSection=document.getElementById('bulk-section');
  if(p.brand!=='diversey'){bulkSection.hidden=true;} else {document.getElementById('bulk-discounts').innerHTML='<table class="bulk-table"><thead><tr><th class="bulk-check-col"></th><th>Quantity</th><th>Savings</th><th>Price per unit</th></tr></thead><tbody>'+[1,2,4,8].map(function(q,i){var unit=Math.round(p.price*(1-bulkDiscounts[i]/100));var saving=Math.round(p.price*q-unit*q);return '<tr data-bulk-qty="'+q+'" class="'+(q===1?'selected':'')+'"><td class="bulk-check-cell"><span>✓</span></td><td><b>'+q+' × '+bulkPack+'</b></td><td>'+(i?'Save '+bulkDiscounts[i]+'%':'-')+(i?'<small>₹'+saving.toLocaleString('en-IN')+'</small>':'')+'</td><td><strong>₹'+unit.toLocaleString('en-IN')+'</strong></td></tr>';}).join('')+'</tbody></table>';}
  Array.from(document.querySelectorAll('[data-bulk-qty]')).forEach(function(row){row.onclick=function(){bulkQty=Number(row.getAttribute('data-bulk-qty'));var i=[1,2,4,8].indexOf(bulkQty);document.getElementById('price').textContent='₹'+Math.round(p.price*(1-bulkDiscounts[i]/100)).toLocaleString('en-IN');Array.from(document.querySelectorAll('[data-bulk-qty]')).forEach(function(x){x.classList.remove('selected');});row.classList.add('selected');};});
  var related=PRODUCTS.filter(function(x){return x.id!==p.id&&x.category===p.category;}).slice(0,5); document.getElementById('related-grid').innerHTML=related.map(function(x){return '<a class="related-card" href="product.html?id='+x.id+'"><div><img src="'+x.image+'" alt="'+x.name+'"></div><b>'+x.name+'</b><strong>₹'+x.price+'</strong><small>★ '+(x.rating||'—')+'</small></a>';}).join('');
  var selectedSize=''; Array.from(sizeBox.children).forEach(function(el,index){el.onclick=function(){selectedSize=el.textContent;selectedPackage=packages[index]||selectedPackage;p.price=selectedPackage.price;p.originalPrice=selectedPackage.originalPrice;bulkPack=selectedPackage.label;bulkQty=1;updatePrice();Array.from(document.querySelectorAll('[data-bulk-qty]')).forEach(function(row,rowIndex){var q=[1,2,4,8][rowIndex],unit=Math.round(p.price*(1-bulkDiscounts[rowIndex]/100)),saving=Math.round(p.price*q-unit*q);row.classList.toggle('selected',rowIndex===0);row.querySelector('td:nth-child(2) b').textContent=q+' × '+bulkPack;row.querySelector('td:nth-child(3)').innerHTML=(rowIndex?'Save '+bulkDiscounts[rowIndex]+'%<small>₹'+saving.toLocaleString('en-IN')+'</small>':'-');row.querySelector('td:nth-child(4) strong').textContent='₹'+unit.toLocaleString('en-IN');});Array.from(sizeBox.children).forEach(function(x){x.classList.remove('selected');});el.classList.add('selected');if(p.sizeImages&&p.sizeImages[selectedSize]){document.getElementById('product-image').src=p.sizeImages[selectedSize];document.getElementById('product-image').alt=p.name+' '+selectedSize;var firstThumb=document.querySelector('.gallery-thumb img');if(firstThumb)firstThumb.src=p.sizeImages[selectedSize];}};});
  var groutIllustration='assets/nitotile-grout-purpose.png';
  var nitobondPackaging='assets/nitobond-ep-packaging-change.png', nitobondHowTo='assets/nitobond-ep-how-to-use.png';
  var brushbondHowTo='assets/brushbond-rfx-how-to-use.png', brushbondEnhanced='assets/brushbond-rfx-pack-enhanced.png';
  var isGrout=p.id==='FOS-NG-007';
  var applicationSteps=p.howToItems||({
    'FOS-NG-007':[['🧹','Prepare the joints','Clean the tile joints, lightly wet them and remove excess surface water.'],['🪣','Mix the grout','Combine the powder with clean water until smooth and lump-free.'],['🧽','Fill and finish','Press grout into the joints, then wipe diagonally with a damp sponge.']],
    'FOS-NB-008':[['🧹','Prepare the concrete','Clean the existing surface and remove dust, oil and loose material.'],['🪣','Mix both parts','Combine Part A and Part B as directed until the bonding coat is uniform.'],['🧱','Bond fresh concrete','Apply the coat and place fresh concrete while it is ready for overcoating.']],
    'FOS-BR-009':[['🧹','Clean and dampen','Remove loose material, oil and dirt, then dampen the surface.'],['🪣','Mix the coating','Combine the powder and liquid components with the required water.'],['🖌️','Apply two coats','Brush, roll or trowel two coats, allowing the first to become touch-dry.']],
    'FOS-CG-010':[['🧹','Prepare the base','Clean the substrate and dampen it without leaving standing water.'],['🪣','Mix the grout','Add the measured water, then slowly mix in the 25 kg powder.'],['⬇️','Place continuously','Pour from one side to avoid trapped air and protect the grout as it gains strength.']]
  })[p.id]||[['🧹','Prepare the surface','Clean the area and remove loose material, dust, oil and residue.'],['🪣','Apply the product','Use the recommended amount and apply evenly with the suitable tool.'],['✨','Finish the job','Allow the product to work, then wipe, rinse or cure as appropriate.']];
  var infoImages={'FOS-NG-007':'assets/nitotile-grout-purpose.png','FOS-NB-008':'assets/nitobond-ep-how-to-use.png','FOS-BR-009':'assets/brushbond-rfx-how-to-use.png','FOS-CG-010':'assets/conbextra-gp2-how-to.png','FOS-AM-011':'assets/auramix-500-how-to.png','FOS-RG-012':'assets/brushbond-roofguard-how-to-use.png','FOS-AQ-019':'assets/brushbond-aquaprotect-how-to.png'};
  infoImages['FOS-CP-014']='assets/conplast-sp430-how-to.png';
  infoImages['FOS-LK-015']='assets/lokfix-p-how-to.png';
  infoImages['FOS-NL-016']='assets/nitobond-sbr-how-to.png';
  var infoImage=document.getElementById('product-info-image');
  infoImages['FOS-RG-012']='assets/brushbond-roofguard-how-to.png';
  var poster=p.infoImage||infoImages[p.id];
  if(poster){infoImage.src=poster;infoImage.alt=p.name+' three-step application procedure';}
  else {infoImage.closest('.product-poster').hidden=true;infoImage.removeAttribute('src');}
  var isNitobond=p.id==='FOS-NB-008';
  var isBrushbond=p.id==='FOS-BR-009';
  var isRoofguard=p.id==='FOS-RG-012', isAquaProtect=p.id==='FOS-AQ-019', isConplast=p.id==='FOS-CP-014', isLokfix=p.id==='FOS-LK-015', isSbr=p.id==='FOS-NL-016';
  var current=0, images=isGrout?[p.image,groutIllustration]:isNitobond?[p.image,nitobondPackaging,nitobondHowTo]:isBrushbond?[p.image,brushbondEnhanced,brushbondHowTo]:isRoofguard?[heroImages[p.id],'assets/brushbond-roofguard-how-to.png','assets/brushbond-roofguard-benefits.png']:isAquaProtect?[p.image,'assets/brushbond-aquaprotect-info.png','assets/brushbond-aquaprotect-how-to.png','assets/brushbond-aquaprotect-benefits.png']:isConplast?[p.image,'assets/conplast-sp430-benefits.png','assets/conplast-sp430-site.png']:isLokfix?[p.image,'assets/lokfix-p-benefits.png','assets/lokfix-p-how-to.png']:isSbr?[p.image,'assets/nitobond-sbr-benefits.png','assets/nitobond-sbr-how-to.png']:[p.image], main=document.getElementById('product-image'), thumbs=document.getElementById('gallery-thumbs');
  if(p.images && p.images.length) images=p.images.filter(function(src,index,list){return src&&list.indexOf(src)===index;});
  if(p.brand==='diversey') images.push('assets/usage-icons/pack-mixing.png');
  main.src=images[0];
  var progress=document.getElementById('gallery-progress');
  var visibleThumbnailCount=Math.min(images.length,5);
  images.slice(0,visibleThumbnailCount).forEach(function(src,i){var b=document.createElement('button');b.className='gallery-thumb'+(i===0?' active':'');b.dataset.galleryIndex=i;b.innerHTML='<img src="'+src+'" alt="Product image '+(i+1)+'">';b.setAttribute('aria-label',p.galleryAlts?.[i]||p.name+' image '+(i+1));b.onclick=function(){show(i);};thumbs.appendChild(b);});
  if(images.length>visibleThumbnailCount){var more=document.createElement('button');more.className='gallery-thumb gallery-more';more.dataset.galleryMore='true';more.textContent='+'+(images.length-visibleThumbnailCount);more.setAttribute('aria-label','View '+(images.length-visibleThumbnailCount)+' more product images');more.onclick=function(){show(visibleThumbnailCount);};thumbs.appendChild(more);}
  function restartProgress(){if(!progress)return;progress.classList.remove('is-running');void progress.offsetWidth;progress.classList.add('is-running');}
  function show(i){var direction=i<current?'carousel-prev':'carousel-next';current=(i+images.length)%images.length;main.classList.remove('carousel-next','carousel-prev');void main.offsetWidth;main.classList.add(direction);main.src=images[current];Array.from(thumbs.children).forEach(function(x){var index=Number(x.dataset.galleryIndex);x.classList.toggle('active',x.dataset.galleryMore==='true'?current>=visibleThumbnailCount:index===current);});restartProgress();}
  document.getElementById('prev-image').onclick=function(){show(current-1);}; document.getElementById('next-image').onclick=function(){show(current+1);};
  var gallery=document.querySelector('.detail-gallery'),autoCarousel=null;
  function stopAutoCarousel(){if(autoCarousel){clearInterval(autoCarousel);autoCarousel=null;}progress.classList.remove('is-running');}
  function startAutoCarousel(){if(images.length<2||document.hidden||autoCarousel)return;restartProgress();autoCarousel=setInterval(function(){show(current+1);},7000);}
  document.addEventListener('visibilitychange',function(){if(document.hidden)stopAutoCarousel();else startAutoCarousel();});startAutoCarousel();
  Array.from(sizeBox.children).forEach(function(el){el.onkeydown=function(e){if(e.key==='Enter'||e.key===' '){e.preventDefault();el.click();}};});
  if(sizeBox.children.length===1)sizeBox.children[0].click();
  function readCart(){try{var c=JSON.parse(sessionStorage.getItem('clearly_cart')||'{}');return c&&typeof c==='object'&&!Array.isArray(c)?c:{};}catch(e){return {};}}
  function updateCartCount(){var c=readCart(),count=Object.keys(c).reduce(function(sum,key){var quantity=Number(c[key]);return sum+(Number.isInteger(quantity)&&quantity>0?quantity:0);},0),badge=document.getElementById('cart-count');if(badge)badge.textContent=count;document.querySelectorAll('[data-site-cart]').forEach(function(item){item.textContent=count;});}
  updateCartCount();
  document.getElementById('add').onclick=function(){if((p.sizes||[]).length&&!selectedSize){sizeBox.style.outline='2px solid #ef5260';setTimeout(function(){sizeBox.style.outline='';},900);return;}var c=readCart(),key=p.id+'@@'+encodeURIComponent(selectedSize||p.packaging||'Standard pack');c[key]=(Number.isInteger(c[key])&&c[key]>0?c[key]:0)+bulkQty;sessionStorage.setItem('clearly_cart',JSON.stringify(c));if(window.ClearlyShop)window.ClearlyShop.saveCart(c).catch(function(){});updateCartCount();this.textContent='Added to bag';};
  function readSaved(){try{var s=JSON.parse(localStorage.getItem('clearly_saved')||'[]');return Array.isArray(s)?s:[];}catch(e){return [];}}
  function updateSave(){var on=readSaved().includes(p.id),button=document.getElementById('save');button.textContent=on?'♥':'♡';button.title=on?'Remove from wishlist':'Save to wishlist';button.setAttribute('aria-label',button.title);button.setAttribute('aria-pressed',String(on));}
  updateSave();
  document.getElementById('save').onclick=function(){if(window.ClearlyShop&&!window.ClearlyShop.requireSignIn('wishlist','product.html?id='+encodeURIComponent(p.id)))return;var s=readSaved();s=s.includes(p.id)?s.filter(function(id){return id!==p.id;}):s.concat(p.id);localStorage.setItem('clearly_saved',JSON.stringify(s));if(window.ClearlyShop)window.ClearlyShop.saveWishlist(s).catch(function(){});updateSave();};
  window.addEventListener('clearly-shop-loaded',function(){updateCartCount();updateSave();},{once:true});window.addEventListener('clearly-cart-updated',updateCartCount);
  if(window.ClearlyShop)window.ClearlyShop.load().then(function(){updateCartCount();updateSave();}).catch(function(){});
  var deliveryForm=document.getElementById('delivery-checker'), deliveryInput=document.getElementById('delivery-pincode'), deliveryResult=document.getElementById('delivery-result'), deliveryButton=document.getElementById('delivery-check-button');
  function deliveryEndpoint(pin){var base=(location.protocol==='file:'||location.port==='4173')?'http://localhost:8081/api/delivery-estimate':'/api/delivery-estimate';return base+'?pincode='+encodeURIComponent(pin);}
  function showDeliveryError(message){deliveryResult.className='delivery-result show error';deliveryResult.innerHTML='<strong>We could not check that PIN</strong><small>'+message+'</small>';}
  function checkDelivery(pin){
    if(!/^[1-9][0-9]{5}$/.test(pin)){showDeliveryError('Enter a valid 6-digit Indian PIN code.');deliveryInput.focus();return;}
    deliveryButton.disabled=true;deliveryButton.textContent='Checking…';deliveryResult.className='delivery-result show';deliveryResult.innerHTML='<small>Checking your delivery location…</small>';
    fetch(deliveryEndpoint(pin)).then(function(response){return response.json().catch(function(){return {};}).then(function(body){if(!response.ok)throw new Error(body.detail||body.message||'Please try again shortly.');return body;});}).then(function(data){
      var place=[data.location&&data.location.district,data.location&&data.location.state].filter(Boolean).join(', ');
      deliveryResult.className='delivery-result show';deliveryResult.innerHTML='<strong>Expected by '+data.estimatedDelivery.label+'</strong><small>Delivery to '+place+' · Estimated, subject to courier serviceability.</small>';
    }).catch(function(error){showDeliveryError(error.message||'Please try again shortly.');}).finally(function(){deliveryButton.disabled=false;deliveryButton.textContent='Check';});
  }
  deliveryInput.oninput=function(){this.value=this.value.replace(/\D/g,'').slice(0,6);};
  deliveryForm.onsubmit=function(event){event.preventDefault();checkDelivery(deliveryInput.value.trim());};
  var sharedPin=new URLSearchParams(location.search).get('pincode');if(sharedPin){deliveryInput.value=sharedPin.replace(/\D/g,'').slice(0,6);if(deliveryInput.value.length===6)checkDelivery(deliveryInput.value);}
  }
  Promise.resolve(window.ClearlyCatalogReady).then(function(){var current=PRODUCTS.find(function(x){return String(x.id)===id||String(x.productRef)===id||String(x.databaseId)===id;})||fallback;renderProduct(normalize(current,current));}).catch(function(){renderProduct(fallback);});
})();
