(function(){
  const products = typeof PRODUCTS !== 'undefined' ? PRODUCTS : [];
  const grid = document.querySelector('#category-products');
  const count = document.querySelector('#category-count');
  const resultCount = document.querySelector('#category-result-count');
  const filterCopy = document.querySelector('#category-filter-copy');
  const sort = document.querySelector('#category-sort');
  const rail = document.querySelector('#subcategory-rail');
  const brandRail = document.querySelector('#brand-rail');
  const clearButton = document.querySelector('#category-clear');
  const params = new URLSearchParams(window.location.search);
  const brandNames = {
    'diversey':'Diversey',
    'shine-all':'Shine All',
    'godrej':'Godrej',
    'aditya-birla':'Aditya Birla',
    'fosroc':'Fosroc'
  };
  const brandLogos = {
    'diversey':'assets/brands/diversey.png',
    'shine-all':'assets/brands/shine-all.svg',
    'godrej':'assets/brands/godrej.png',
    'aditya-birla':'assets/brands/aditya-birla.png',
    'fosroc':'assets/brands/fosroc.png'
  };
  const collections = {
    'restaurant-food-service': ['Restaurant & Food Service', 'restaurant-food-service', ['Restaurant & Food Service', 'Kitchen Care']],
    'healthcare-institutions': ['Healthcare & Institutions', 'healthcare-institutions', ['Healthcare & Institutions']],
    'laundry-chemicals': ['Laundry Chemicals', 'laundry-chemicals', ['Laundry Chemicals', 'Laundry Care']],
    'swimming-pool-chemicals': ['Swimming Pool Chemicals', 'swimming-pool-chemicals', ['Swimming Pool Chemicals']],
    'hotel-hospitality': ['Hotel & Hospitality', 'hotel-hospitality', ['Hotel & Hospitality']],
    'specialty-chemicals': ['Specialty Chemicals', 'specialty-chemicals', ['Specialty Chemicals']],
    'construction-chemicals': ['Construction Chemicals', 'construction-chemicals', ['Construction Chemicals']]
  };
  const collectionSlug = params.get('category');
  const collection = collections[collectionSlug];
  const categories = collection ? collection[2] : ['Floor Care', 'Bathroom Care', 'Kitchen Care', 'Laundry Care', 'Surface Care'];
  let activeCategory = params.get('subcategory') || 'all';
  let activeBrand = params.get('brand') || 'all';
  let cart = {};
  let saved = [];
  try { cart = JSON.parse(sessionStorage.getItem('clearly_cart') || '{}') || {}; } catch (error) { cart = {}; }
  const cartKey = (productId, size) => `${productId}@@${encodeURIComponent(size || 'Standard pack')}`;
  try { saved = JSON.parse(localStorage.getItem('clearly_saved') || '[]') || []; } catch (error) { saved = []; }
  if (!Array.isArray(saved)) saved = [];

  function updateBagCount(){
    const bagCount = document.querySelector('#category-cart b');
    if (bagCount) bagCount.textContent = Object.values(cart).reduce((total, quantity) => total + Number(quantity || 0), 0);
  }

  function stars(rating){
    const rounded = Math.round(Number(rating) || 0);
    return `${'★'.repeat(rounded)}<i>${'★'.repeat(5-rounded)}</i> <small>(${Number(rating).toFixed(1)})</small>`;
  }
  function packageOptions(product){
    const enabled=(product.packages||[]).filter(item=>item.enabled!==false);
    return enabled.length?enabled:(product.sizes||[product.packaging||'Standard pack']).map((label,index)=>({label,price:Number(product.price||0),originalPrice:Number(product.originalPrice||product.price||0),isDefault:index===0}));
  }

  if (collection) {
    const [name, image] = collection;
    document.title = `${name} — Clearly.`;
    document.querySelector('.category-hero-copy .category-eyebrow').textContent = name;
    document.querySelector('.category-hero-copy h1').textContent = name;
    document.querySelector('.category-hero-copy > p').textContent = `Explore our ${name.toLowerCase()} collection.`;
    const art = document.querySelector('.category-hero-art img');
    art.src = `assets/categories/${image}.png`;
    art.alt = name;
    document.querySelector('.hero-art-note').textContent = name;
    document.querySelector('.category-stats span:nth-child(2)').hidden = true;
    document.querySelector('.category-section-head h2').textContent = 'Explore the collection.';
    rail.querySelectorAll('[data-category]').forEach(button => {
      button.hidden = button.dataset.category !== 'all' && !categories.includes(button.dataset.category);
    });
  }

  const collectionProducts = products.filter(product =>
    collectionSlug === 'construction-chemicals'
      ? product.brand === 'fosroc'
      : categories.includes(product.category)
  );
  const availableBrands = [...new Set(collectionProducts.map(product => product.brand))];
  if (activeBrand !== 'all' && !availableBrands.includes(activeBrand)) activeBrand = 'all';
  if (activeCategory !== 'all' && !categories.includes(activeCategory)) activeCategory = 'all';
  brandRail.innerHTML = [
    `<button class="brand-option ${activeBrand === 'all' ? 'active' : ''}" data-brand="all" aria-pressed="${activeBrand === 'all'}"><span class="brand-all-mark"><i></i><i></i><i></i></span><span><b>All brands</b><small>${collectionProducts.length} products</small></span><em>View all</em></button>`,
    ...availableBrands.map(brand => {
      const total = collectionProducts.filter(product => product.brand === brand).length;
      return `<button class="brand-option ${activeBrand === brand ? 'active' : ''}" data-brand="${brand}" aria-pressed="${activeBrand === brand}"><span class="brand-logo-shell"><img src="${brandLogos[brand] || ''}" alt="" /></span><span><b>${brandNames[brand] || brand}</b><small>${total} product${total === 1 ? '' : 's'}</small></span><em>Choose</em></button>`;
    })
  ].join('');

  function visibleProducts(){
    const list = collectionProducts.filter(product =>
      (activeCategory === 'all' || product.category === activeCategory) &&
      (activeBrand === 'all' || product.brand === activeBrand)
    );
    return list.sort((a,b) =>
      sort.value === 'price-low' ? a.price-b.price :
      sort.value === 'price-high' ? b.price-a.price :
      sort.value === 'rating' ? b.rating-a.rating : 0
    );
  }

  function render(){
    const list = visibleProducts();
    count.textContent = collectionProducts.length;
    resultCount.textContent = list.length;
    const labels = [];
    if (activeBrand !== 'all') labels.push(brandNames[activeBrand] || activeBrand);
    if (activeCategory !== 'all') labels.push(activeCategory);
    filterCopy.textContent = labels.length ? `in ${labels.join(' · ')}` : 'across all brands and care areas';
    clearButton.hidden = activeBrand === 'all' && activeCategory === 'all';

    brandRail.querySelectorAll('.brand-option').forEach(button => {
      const chosen = button.dataset.brand === activeBrand;
      button.classList.toggle('active', chosen);
      button.setAttribute('aria-pressed', String(chosen));
    });
    rail.querySelectorAll('.subcategory').forEach(button => {
      const chosen = button.dataset.category === activeCategory;
      button.classList.toggle('active', chosen);
      button.setAttribute('aria-pressed', String(chosen));
    });

    grid.innerHTML = list.map(product => {
      const href = `product.html?id=${encodeURIComponent(product.id)}`;
      const packages=packageOptions(product),displayPackage=packages.find(item=>item.isDefault)||packages[0]||product;
      const sizeList = packages.length ? `<div class="sizes"><select class="package-select" data-package-select aria-label="Choose package size"><option value="">Select package size</option>${packages.map((pkg,index) => `<option value="${index}" data-size="${pkg.label}" data-price="${Number(pkg.price||0)}" data-mrp="${Number(pkg.originalPrice||pkg.price||0)}">${pkg.label}</option>`).join('')}</select></div><small class="size-prompt" aria-live="polite">Select a package size</small>` : '';
      const originalPrice = displayPackage.originalPrice > displayPackage.price ? `<span class="old">₹${Number(displayPackage.originalPrice).toLocaleString('en-IN')}</span>` : '';
      const isSaved = saved.includes(product.id);
      return `<article class="product-card" data-id="${product.id}"><a class="product-img" href="${href}"><img src="${product.image}" alt="${product.name}" /></a><div class="product-category">${product.category}</div><h3 class="product-name"><a href="${href}">${product.name}</a></h3>${product.rating ? `<span class="product-stars">${stars(product.rating)}</span>` : ''}${sizeList}<div class="product-price">${originalPrice}<span class="now">₹${Number(displayPackage.price).toLocaleString('en-IN')}</span></div><div class="product-actions"><button class="cart-btn" data-cart="${product.id}">Add to bag</button><button class="wishlist-btn${isSaved ? ' on' : ''}" data-wish="${product.id}" aria-label="Save ${product.name} for later" title="Save for later"><span class="ico">${isSaved ? '♥' : '♡'}</span></button></div></article>`;
    }).join('');
    if (!list.length) grid.innerHTML = '<div class="category-empty"><span>⌁</span><h3>No matching products yet</h3><p>Try another brand or care area.</p><button type="button">Show all products</button></div>';
    grid.querySelector('.category-empty button')?.addEventListener('click', clearFilters);
  }

  function clearFilters(){
    activeBrand = 'all';
    activeCategory = 'all';
    render();
  }

  brandRail.addEventListener('click', event => {
    const button = event.target.closest('.brand-option');
    if (!button) return;
    activeBrand = button.dataset.brand;
    render();
  });
  rail.addEventListener('click', event => {
    const button = event.target.closest('.subcategory');
    if (!button) return;
    activeCategory = button.dataset.category;
    render();
  });
  clearButton.addEventListener('click', clearFilters);
  sort.addEventListener('change', render);
  grid.addEventListener('change', event => {
    const select = event.target.closest('[data-package-select]');
    if (!select) return;
    const card = select.closest('.product-card'), selected = select.options[select.selectedIndex];
    if (!select.value) { card.querySelector('.size-prompt').textContent='Select a package size'; return; }
    card.classList.remove('needs-size');
    card.querySelector('.size-prompt').textContent=`Selected: ${selected.dataset.size}`;
    const price=Number(selected.dataset.price||0),mrp=Number(selected.dataset.mrp||price);
    card.querySelector('.product-price').innerHTML=`${mrp>price?`<span class="old">₹${mrp.toLocaleString('en-IN')}</span>`:''}<span class="now">₹${price.toLocaleString('en-IN')}</span>`;
  });
  grid.addEventListener('click', event => {
    const cartButton = event.target.closest('[data-cart]');
    if (cartButton) {
      const id = cartButton.dataset.cart;
      const card=cartButton.closest('.product-card'), select=card.querySelector('[data-package-select]'), selected=select&&select.options[select.selectedIndex];
      if(!select||!select.value){card.classList.add('needs-size');card.querySelector('.size-prompt').textContent='Please select a package size first';setTimeout(()=>card.classList.remove('needs-size'),1400);return;}
      const key=cartKey(id,selected.dataset.size);
      cart[key] = Number(cart[key] || 0) + 1;
      try { sessionStorage.setItem('clearly_cart', JSON.stringify(cart)); } catch (error) {}
      if (window.ClearlyShop) window.ClearlyShop.saveCart(cart).catch(() => {});
      updateBagCount();
      cartButton.textContent = 'Added ✓';
      cartButton.classList.add('added');
      setTimeout(() => { cartButton.textContent = 'Add to bag'; cartButton.classList.remove('added'); }, 1200);
      return;
    }
    const wishButton = event.target.closest('[data-wish]');
    if (wishButton) {
      if (window.ClearlyShop && !window.ClearlyShop.requireSignIn('wishlist', 'category.html')) return;
      const id = wishButton.dataset.wish;
      saved = saved.includes(id) ? saved.filter(savedId => savedId !== id) : [...saved, id];
      try { localStorage.setItem('clearly_saved', JSON.stringify(saved)); } catch (error) {}
      if (window.ClearlyShop) window.ClearlyShop.saveWishlist(saved).catch(() => {});
      wishButton.classList.toggle('on', saved.includes(id));
      wishButton.querySelector('.ico').textContent = saved.includes(id) ? '♥' : '♡';
    }
  });
  document.querySelector('#category-sub-prev')?.addEventListener('click', () => rail.scrollBy({left:-310,behavior:'smooth'}));
  document.querySelector('#category-sub-next')?.addEventListener('click', () => rail.scrollBy({left:310,behavior:'smooth'}));
  updateBagCount();
  render();
  if (window.ClearlyShop) window.ClearlyShop.load().then(state => {
    cart = state.cart || {};
    saved = state.wishlist || [];
    updateBagCount();
    render();
  }).catch(() => {});
  window.addEventListener('clearly-catalog-loaded',()=>{
    const fresh=products.filter(product=>collectionSlug==='construction-chemicals'?product.brand==='fosroc':categories.includes(product.category));
    collectionProducts.splice(0,collectionProducts.length,...fresh);
    render();
  });
})();
