(function () {
  'use strict';
  const page = document.body.dataset.shopPage;
  const productList = typeof PRODUCTS !== 'undefined' ? PRODUCTS : [];
  const brandMap = typeof BRANDS !== 'undefined' ? BRANDS : {};
  const productById = Object.fromEntries(productList.map(product => [product.id, product]));
  function rebuildProductIndex(){Object.keys(productById).forEach(id=>delete productById[id]);productList.forEach(product=>productById[product.id]=product);}
  const CART_KEY_SEPARATOR = '@@';
  function cartPart(key) { const parts=String(key).split(CART_KEY_SEPARATOR);return {id:parts[0],size:parts.length>1?decodeURIComponent(parts.slice(1).join(CART_KEY_SEPARATOR)):''}; }

  function readObject(storage, key) {
    try {
      const value = JSON.parse(storage.getItem(key) || '{}');
      return value && typeof value === 'object' && !Array.isArray(value) ? value : {};
    } catch (error) { return {}; }
  }
  function readArray(storage, key) {
    try {
      const value = JSON.parse(storage.getItem(key) || '[]');
      return Array.isArray(value) ? value : [];
    } catch (error) { return []; }
  }
  function readCart() {
    const cart = readObject(sessionStorage, 'clearly_cart');
    Object.keys(cart).forEach(key => {
      const quantity = Math.floor(Number(cart[key]));
      if (!productById[cartPart(key).id] || quantity < 1) delete cart[key]; else cart[key] = quantity;
    });
    return cart;
  }
  function readSaved() { return readArray(localStorage, 'clearly_saved').filter(id => productById[id]); }
  function saveCart(cart) { try { sessionStorage.setItem('clearly_cart', JSON.stringify(cart)); } catch (error) {} if(window.ClearlyShop)window.ClearlyShop.saveCart(cart).catch(function(error){toast(error.message==='AUTH_REQUIRED'?'Please sign in to use your cart':'Cart could not be saved');}); }
  function saveSaved(saved) { const unique=[...new Set(saved)];try { localStorage.setItem('clearly_saved', JSON.stringify(unique)); } catch (error) {} if(window.ClearlyShop)window.ClearlyShop.saveWishlist(unique).catch(function(error){toast(error.message==='AUTH_REQUIRED'?'Please sign in to use your wishlist':'Wishlist could not be saved');}); }
  function money(value) { return `₹${Number(value || 0).toLocaleString('en-IN')}`; }
  function packageFor(product,size){const packages=(product.packages||[]).filter(item=>item.enabled!==false);return packages.find(item=>item.label===size)||packages.find(item=>item.isDefault)||packages[0]||{price:Number(product.price||0),originalPrice:Number(product.originalPrice||product.price||0)};}
  function quantityPrice(product,pkg,quantity){const all=(product.bulkDiscountEnabled===false?[]:(product.bulkDiscounts||[])).filter(tier=>tier.enabled!==false),selected=all.filter(tier=>String(tier.packageId)===String(pkg.id||pkg.packageId||'')),packages=(product.packages||[]).filter(item=>item.enabled!==false),defaultPackage=packages.find(item=>item.isDefault||item.is_default)||packages[0],defaultId=defaultPackage&&(defaultPackage.id||defaultPackage.packageId),fallback=all.filter(tier=>String(tier.packageId)===String(defaultId)),tiers=(selected.length?selected:(fallback.length?fallback:all)).filter(tier=>Number(tier.minQuantity||tier.minQty||1)<=quantity).sort((a,b)=>Number(a.minQuantity||a.minQty||1)-Number(b.minQuantity||b.minQty||1)),tier=tiers[tiers.length-1],percent=Number(tier&&(tier.discountPercent??tier.savePct)||0);return Number((Number(pkg.price||0)*(1-percent/100)).toFixed(2));}
  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));
  }
  function countCart(cart) { return Object.values(cart).reduce((sum, quantity) => sum + Number(quantity || 0), 0); }
  function toast(message) {
    const element = document.querySelector('#shop-toast');
    if (!element) return;
    element.textContent = message;
    element.classList.add('show');
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => element.classList.remove('show'), 1800);
  }
  function updateCounts() {
    const cartCount = countCart(readCart());
    const wishCount = readSaved().length;
    document.querySelectorAll('[data-cart-count]').forEach(element => {
      element.textContent = cartCount;
      element.dataset.empty = String(cartCount === 0);
    });
    document.querySelectorAll('[data-wish-count]').forEach(element => {
      element.textContent = wishCount;
      element.dataset.empty = String(wishCount === 0);
    });
    document.querySelectorAll('[data-profile-cart]').forEach(element => element.textContent = cartCount);
    document.querySelectorAll('[data-profile-wish]').forEach(element => element.textContent = wishCount);
  }

  function emptyMarkup(kind) {
    const isCart = kind === 'cart';
    return `<div class="empty-icon"><svg viewBox="0 0 24 24">${isCart ? '<path d="M3.8 5h2.3l1.5 9.5h10.2l2.1-6.8H7"/><circle cx="10" cy="18.5" r="1.3"/><circle cx="17" cy="18.5" r="1.3"/>' : '<path d="M20.5 8.6c0 5.1-8.5 10-8.5 10s-8.5-4.9-8.5-10A4.6 4.6 0 0 1 12 6.1a4.6 4.6 0 0 1 8.5 2.5Z"/>'}</svg></div><h2>${isCart ? 'Your cart is ready for something good.' : 'Your wishlist is waiting.'}</h2><p>${isCart ? 'Browse the collection and add the products your space needs.' : 'Tap “Save for later” on any product to keep it here.'}</p><a class="primary-link" href="products.html">Explore all products</a>`;
  }

  function renderCart() {
    const list = document.querySelector('#cart-list');
    const summary = document.querySelector('#cart-summary');
    const content = document.querySelector('#cart-content');
    if (!list || !summary || !content) return;
    const cart = readCart();
    const entries = Object.entries(cart).map(([key, quantity]) => {const part=cartPart(key);return {key,part,product:productById[part.id],quantity};}).filter(item => item.product);
    if (!entries.length) {
      content.className = 'panel empty-shop';
      content.innerHTML = emptyMarkup('cart');
      updateCounts();
      return;
    }
    content.className = 'cart-layout';
    list.innerHTML = entries.map(({key,part,product,quantity}) => {const pkg=packageFor(product,part.size),unitPrice=quantityPrice(product,pkg,quantity);return `<article class="cart-row" data-cart-id="${escapeHtml(key)}"><a class="cart-photo" href="product.html?id=${encodeURIComponent(product.id)}"><img src="${escapeHtml(product.image)}" alt="${escapeHtml(product.name)}" /></a><div class="cart-copy"><span class="cart-brand">${escapeHtml(brandMap[product.brand] || product.brand)}</span><h2>${escapeHtml(product.name)}</h2><p>${escapeHtml(product.category)} · <strong>${escapeHtml(part.size || product.packaging || product.sizes?.[0] || 'Standard pack')}</strong></p><div class="cart-price">${money(unitPrice * quantity)}</div></div><div class="cart-controls"><div class="qty-control" aria-label="Quantity"><button data-qty="-1" aria-label="Decrease quantity">−</button><span>${quantity}</span><button data-qty="1" aria-label="Increase quantity">+</button></div><button class="remove-item" data-remove>Remove</button></div></article>`;}).join('');
    const subtotal = entries.reduce((sum, item) => {const pkg=packageFor(item.product,item.part.size);return sum+quantityPrice(item.product,pkg,item.quantity)*item.quantity;}, 0);
    const shipping = subtotal >= 499 ? 0 : 59;
    const progress = Math.min(100, subtotal / 499 * 100);
    summary.innerHTML = `<h2>Order summary</h2><div class="summary-line"><span>Subtotal</span><strong>${money(subtotal)}</strong></div><div class="summary-line"><span>Delivery</span><strong>${shipping ? money(shipping) : 'Free'}</strong></div><div class="shipping-box">${shipping ? `Add ${money(499 - subtotal)} more for free delivery` : 'You unlocked free delivery'}<div class="shipping-track"><i style="width:${progress}%"></i></div></div><hr /><div class="summary-total"><span>Total</span><strong>${money(subtotal + shipping)}</strong></div><button class="checkout" id="checkout-button">Proceed to checkout</button><p class="secure-note">Secure checkout · Easy returns</p>`;
    updateCounts();
  }

  function initCart() {
    renderCart();
    document.querySelector('#cart-content')?.addEventListener('click', event => {
      const row = event.target.closest('[data-cart-id]');
      if (!row) return;
      const cart = readCart();
      const id = row.dataset.cartId;
      const quantityButton = event.target.closest('[data-qty]');
      const confirmRemoval = () => {
        const part = cartPart(id);
        const product = productById[part.id];
        const label = product.name + (part.size ? ` (${part.size})` : '');
        return window.confirm(`Remove ${label} from your cart?`);
      };
      if (quantityButton) {
        const nextQuantity = Number(cart[id] || 0) + Number(quantityButton.dataset.qty);
        if (nextQuantity < 1 && !confirmRemoval()) return;
        cart[id] = nextQuantity;
        if (cart[id] < 1) delete cart[id];
        saveCart(cart); renderCart(); return;
      }
      if (event.target.closest('[data-remove]')) {
        if (!confirmRemoval()) return;
        delete cart[id]; saveCart(cart); renderCart(); toast('Removed from cart');
      }
    });
    document.addEventListener('click', event => {
      if (event.target.closest('#checkout-button')) toast('Your order is ready for checkout');
    });
  }

  function renderWishlist() {
    const grid = document.querySelector('#wish-grid');
    const empty = document.querySelector('#wish-empty');
    if (!grid || !empty) return;
    const saved = readSaved();
    if (!saved.length) {
      grid.innerHTML = ''; empty.hidden = false; empty.innerHTML = emptyMarkup('wishlist'); updateCounts(); return;
    }
    empty.hidden = true;
    grid.innerHTML = saved.map(id => productById[id]).filter(Boolean).map(product => {const packages=(product.packages||[]).filter(item=>item.enabled!==false);const choices=packages.length?packages:[{label:product.packaging||product.sizes?.[0]||'Standard pack',price:product.price,originalPrice:product.originalPrice||product.price}];const defaultPackage=choices.find(item=>item.isDefault)||choices[0];const old=Number(defaultPackage.originalPrice||0)>Number(defaultPackage.price||0)?`<span class="old">${money(defaultPackage.originalPrice)}</span>`:'';return `<article class="product-card" data-wish-id="${escapeHtml(product.id)}"><a class="product-img" href="product.html?id=${encodeURIComponent(product.id)}"><img src="${escapeHtml(product.image)}" alt="${escapeHtml(product.name)}" /></a><h3 class="product-name"><a href="product.html?id=${encodeURIComponent(product.id)}">${escapeHtml(product.name)}</a></h3><div class="sizes"><select class="package-select" data-wish-package aria-label="Choose package size"><option value="">Select package size</option>${choices.map((pkg,index)=>`<option value="${index}" data-size="${escapeHtml(pkg.label)}" data-price="${Number(pkg.price||0)}" data-mrp="${Number(pkg.originalPrice||pkg.price||0)}">${escapeHtml(pkg.label)}</option>`).join('')}</select></div><small class="size-prompt">Select a package size</small><div class="product-price">${old}<span class="now">${money(defaultPackage.price)}</span></div><div class="product-actions"><button class="cart-btn" data-wish-add>Add to bag</button><button class="wishlist-btn on" data-wish-remove aria-label="Remove ${escapeHtml(product.name)} from wishlist" title="Remove from wishlist"><span class="ico">♥</span></button></div></article>`;}).join('');
    updateCounts();
  }

  function initWishlist() {
    renderWishlist();
    document.querySelector('#wish-grid')?.addEventListener('change', event => {
      const select=event.target.closest('[data-wish-package]'); if(!select)return;
      const card=select.closest('[data-wish-id]'),option=select.options[select.selectedIndex];
      card.querySelector('.size-prompt').textContent=select.value?`Selected: ${option.dataset.size}`:'Select a package size';
      if(select.value){const price=Number(option.dataset.price||0),mrp=Number(option.dataset.mrp||price);card.querySelector('.product-price').innerHTML=`${mrp>price?`<span class="old">${money(mrp)}</span>`:''}<span class="now">${money(price)}</span>`;}
    });
    document.querySelector('#wish-grid')?.addEventListener('click', event => {
      const card = event.target.closest('[data-wish-id]');
      if (!card) return;
      const id = card.dataset.wishId;
      if (event.target.closest('[data-wish-add]')) {
        const select=card.querySelector('[data-wish-package]'),option=select&&select.options[select.selectedIndex];
        if(!select||!select.value){card.classList.add('needs-size');card.querySelector('.size-prompt').textContent='Please select a package size first';setTimeout(()=>card.classList.remove('needs-size'),1400);return;}
        const cart=readCart(),key=`${id}${CART_KEY_SEPARATOR}${encodeURIComponent(option.dataset.size)}`;
        cart[key]=Number(cart[key]||0)+1;saveCart(cart);updateCounts();toast('Added to bag');
        return;
      }
      if (event.target.closest('[data-wish-remove]')) {
        saveSaved(readSaved().filter(savedId => savedId !== id)); renderWishlist(); toast('Removed from wishlist');
      }
    });
  }

  function renderSignedOut(kind) {
    const isCart = kind === 'cart';
    const destination = `account.html?required=${kind}&return=${kind}.html`;
    const markup = `<div class="signin-empty-icon" aria-hidden="true"><svg viewBox="0 0 64 64"><rect x="17" y="10" width="30" height="44" rx="7"/><circle cx="32" cy="26" r="7"/><path d="M22 47c2-7 18-7 20 0"/></svg></div><h2>Please sign in</h2><p>Sign in to view the products saved in your ${isCart ? 'cart' : 'wishlist'}.</p><a class="primary-link" href="${destination}">Sign in</a>`;
    if (isCart) {
      const content = document.querySelector('#cart-content');
      content.className = 'panel empty-shop signed-out-shop';
      content.innerHTML = markup;
    } else {
      document.querySelector('#wish-grid').innerHTML = '';
      const empty = document.querySelector('#wish-empty');
      empty.hidden = false;
      empty.classList.add('signed-out-shop');
      empty.innerHTML = markup;
    }
    updateCounts();
  }

  function initAccount() {
    const auth = document.querySelector('#account-auth'), form = document.querySelector('#account-form'), otpPanel = document.querySelector('#otp-panel');
    const apiBase = (location.protocol === 'file:' || location.port === '4173') ? 'http://localhost:8085/api/auth' : '/api/auth';
    let mode = 'signin', signupChannel = 'PHONE', pendingIdentifier = '';
    const accountParams = new URLSearchParams(location.search), requestedPage = accountParams.get('return'), requiredArea = accountParams.get('required');
    const safeReturnPage = requestedPage && /^(?:index|category|products|product|cart|wishlist)\.html(?:\?[^#]*)?$/.test(requestedPage) ? requestedPage : 'index.html';
    const readToken = () => sessionStorage.getItem('clearly_access_token') || localStorage.getItem('clearly_access_token') || '';
    function clearSession() { sessionStorage.removeItem('clearly_access_token'); localStorage.removeItem('clearly_access_token'); localStorage.removeItem('clearly_profile'); }
    function saveSession(data) { clearSession(); (document.querySelector('#remember-account')?.checked ? localStorage : sessionStorage).setItem('clearly_access_token', data.accessToken); localStorage.setItem('clearly_profile', JSON.stringify(data.user)); }
    async function api(path, options = {}) {
      const headers = {'Content-Type':'application/json', ...(options.headers || {})}, token = readToken(); if (token) headers.Authorization = `Bearer ${token}`;
      const response = await fetch(apiBase + path, {...options, headers}), body = await response.json().catch(() => ({}));
      if (!response.ok) throw new Error(body.detail || body.message || 'Something went wrong. Please try again.'); return body;
    }
    function message(target, text) { target.textContent = text || ''; target.className = `form-message${text ? ' show' : ''}`; }
    function loading(button, busy, copy) { if (!button.dataset.label) button.dataset.label = button.textContent; button.disabled = busy; button.textContent = busy ? copy : button.dataset.label; }
    function updateSignupChannel() {
      const phone = signupChannel === 'PHONE', input = document.querySelector('#account-identifier');
      document.querySelectorAll('[data-signup-channel]').forEach(button => button.classList.toggle('active', button.dataset.signupChannel === signupChannel));
      document.querySelector('#identifier-label').textContent = phone ? 'Phone number' : 'Email address';
      input.placeholder = phone ? '+91 98765 43210' : 'you@example.com'; input.autocomplete = phone ? 'tel' : 'email'; input.inputMode = phone ? 'tel' : 'email';
    }
    function setMode(next) {
      mode = next; const registering = mode === 'register'; otpPanel.hidden = true; form.hidden = false;
      document.querySelector('.account-divider').hidden = false; document.querySelector('#google-signin').hidden = false;
      document.querySelectorAll('[data-account-mode]').forEach(tab => tab.classList.toggle('active', tab.dataset.accountMode === mode));
      document.querySelector('#name-field').hidden = !registering; document.querySelector('#account-name').required = registering; document.querySelector('#signup-methods').hidden = !registering;
      document.querySelector('#password-hint').hidden = !registering; document.querySelector('#account-heading').textContent = registering ? 'Create your account' : 'Welcome back';
      document.querySelector('#account-intro').textContent = registering ? 'Verify by email or phone. Your account is created only after the correct 4-digit code.' : 'Sign in using your verified email address or phone number.';
      const submit = document.querySelector('#account-submit'); submit.textContent = registering ? 'Send verification code' : 'Sign in'; submit.dataset.label = submit.textContent;
      if (registering) { signupChannel = 'PHONE'; document.querySelector('#account-identifier').value = ''; updateSignupChannel(); } else { document.querySelector('#identifier-label').textContent = 'Email address or phone number'; document.querySelector('#account-identifier').placeholder = 'Email or +91 phone number'; }
      message(document.querySelector('#account-message'), '');
    }
    document.querySelectorAll('[data-account-mode]').forEach(tab => tab.addEventListener('click', () => setMode(tab.dataset.accountMode)));
    document.querySelectorAll('[data-signup-channel]').forEach(button => button.addEventListener('click', () => { signupChannel = button.dataset.signupChannel; document.querySelector('#account-identifier').value = ''; updateSignupChannel(); }));
    form.addEventListener('submit', async event => {
      event.preventDefault(); const identifier = document.querySelector('#account-identifier').value.trim(), button = document.querySelector('#account-submit'); message(document.querySelector('#account-message'), ''); loading(button, true, mode === 'register' ? 'Sending code…' : 'Signing in…');
      try {
        if (mode === 'register') {
          const destination = signupChannel === 'PHONE' ? {phone:identifier} : {email:identifier};
          const result = await api('/register', {method:'POST', body:JSON.stringify({name:document.querySelector('#account-name').value.trim(), password:document.querySelector('#account-password').value, channel:signupChannel, ...destination})});
          pendingIdentifier = identifier; form.hidden = true; document.querySelector('.account-divider').hidden = true; document.querySelector('#google-signin').hidden = true; otpPanel.hidden = false;
          document.querySelector('#otp-heading').textContent = signupChannel === 'PHONE' ? 'Verify your phone' : 'Verify your email';
          document.querySelector('#otp-copy').textContent = result.developmentOtp ? `Local preview code: ${result.developmentOtp}. Enter it to create your account.` : `Enter the 4-digit code sent to ${result.identifier}.`;
          document.querySelector('#account-otp').focus();
        } else {
          const result = await api('/login', {method:'POST', body:JSON.stringify({identifier, password:document.querySelector('#account-password').value})}); saveSession(result); window.location.assign(safeReturnPage);
        }
      } catch (error) { message(document.querySelector('#account-message'), error.message); } finally { loading(button, false); }
    });
    document.querySelector('#account-otp').addEventListener('input', event => { event.target.value = event.target.value.replace(/\D/g, '').slice(0, 4); });
    document.querySelector('#otp-form').addEventListener('submit', async event => {
      event.preventDefault(); const button = document.querySelector('#verify-otp'); message(document.querySelector('#otp-message'), ''); loading(button, true, 'Verifying…');
      try { const result = await api('/verify-otp', {method:'POST', body:JSON.stringify({identifier:pendingIdentifier, channel:signupChannel, otp:document.querySelector('#account-otp').value})}); saveSession(result); window.location.assign('index.html'); }
      catch (error) { message(document.querySelector('#otp-message'), error.message); } finally { loading(button, false); }
    });
    document.querySelector('#resend-otp').addEventListener('click', async event => {
      const button = event.currentTarget; loading(button, true, 'Sending…');
      try { const result = await api('/resend-otp', {method:'POST', body:JSON.stringify({identifier:pendingIdentifier, channel:signupChannel})}); document.querySelector('#otp-copy').textContent = result.developmentOtp ? `New local preview code: ${result.developmentOtp}` : `A new code was sent to ${result.identifier}.`; }
      catch (error) { message(document.querySelector('#otp-message'), error.message); } finally { loading(button, false); }
    });
    document.querySelector('#otp-back').addEventListener('click', () => setMode('register'));
    async function completeGoogle(response) { try { const result = await api('/google', {method:'POST', body:JSON.stringify({credential:response.credential})}); saveSession(result); window.location.assign(safeReturnPage); } catch (error) { message(document.querySelector('#account-message'), error.message); } }
    async function setupGoogle() {
      const placeholder = document.querySelector('#google-placeholder'), note = document.querySelector('#google-note');
      try { const config = await api('/config'); if (!config.googleEnabled) { note.textContent = 'Google sign-in is not configured.'; return; }
        const script = document.createElement('script'); script.src = 'https://accounts.google.com/gsi/client'; script.async = true; script.onload = () => { placeholder.hidden = true; google.accounts.id.initialize({client_id:config.googleClientId, callback:completeGoogle}); google.accounts.id.renderButton(document.querySelector('#google-button'), {theme:'outline', size:'large', shape:'pill', width:320}); }; document.head.appendChild(script);
      } catch (error) { note.textContent = 'Google sign-in is temporarily unavailable.'; }
    }
    if(requiredArea)message(document.querySelector('#account-message'), `Please sign in to access your ${requiredArea}.`);
    setupGoogle(); const token = readToken();
    if (token) api('/me').then(profile => { localStorage.setItem('clearly_profile', JSON.stringify(profile)); window.location.replace('profile.html'); }).catch(() => clearSession());
  }

  if(page==='cart'||page==='wishlist'){
    const start=()=>{updateCounts();if(page==='cart')initCart();else initWishlist();};
    if(window.ClearlyShop&&!window.ClearlyShop.signedIn()&&page==='wishlist')renderSignedOut(page);
    else if(window.ClearlyShop)window.ClearlyShop.load().then(start).catch(()=>{});else start();
  }else updateCounts();
  if (page === 'account') initAccount();
  window.addEventListener('pageshow', updateCounts);
  window.addEventListener('clearly-catalog-loaded',()=>{rebuildProductIndex();if(page==='cart')renderCart();if(page==='wishlist')renderWishlist();updateCounts();});
})();
