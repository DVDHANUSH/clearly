const homeProfileLink = document.querySelector('#home-profile-link');
if (homeProfileLink) {
  const profileDestination = () => window.ClearlyShop?.signedIn() ? 'profile.html' : 'account.html';
  homeProfileLink.href = profileDestination();
  homeProfileLink.addEventListener('click', event => {
    event.preventDefault();
    window.location.assign(profileDestination());
  });
}

document.querySelectorAll('a[href^="#"]').forEach(link => {
  link.addEventListener('click', event => {
    const id = link.getAttribute('href').slice(1);
    const target = id && document.getElementById(id);
    if (target) { event.preventDefault(); target.scrollIntoView({behavior:'smooth'}); }
  });
});
document.querySelectorAll('.product p button').forEach(button => {
  button.addEventListener('click', () => {
    const id = button.dataset.homeCart;
    if (id) {
      let cart = {};
      try { cart = JSON.parse(sessionStorage.getItem('clearly_cart') || '{}') || {}; } catch (error) { cart = {}; }
      cart[id] = Number(cart[id] || 0) + 1;
      try { sessionStorage.setItem('clearly_cart', JSON.stringify(cart)); } catch (error) {}
      if (window.ClearlyShop) window.ClearlyShop.saveCart(cart).catch(() => {});
      updateHomeShoppingCounts();
    }
    button.textContent = '✓';
    button.classList.add('added');
    setTimeout(() => { button.textContent = '+'; button.classList.remove('added'); }, 1200);
  });
});

function updateHomeShoppingCounts() {
  const cartBadge = document.querySelector('#home-cart-count');
  const wishBadge = document.querySelector('#home-wish-count');
  if (!cartBadge && !wishBadge) return;
  let cart = {}, saved = [];
  try { cart = JSON.parse(sessionStorage.getItem('clearly_cart') || '{}') || {}; } catch (error) { cart = {}; }
  try { saved = JSON.parse(localStorage.getItem('clearly_saved') || '[]') || []; } catch (error) { saved = []; }
  const cartCount = Object.values(cart).reduce((sum, quantity) => sum + Math.max(0, Number(quantity) || 0), 0);
  const wishCount = Array.isArray(saved) ? saved.length : 0;
  if (cartBadge) { cartBadge.textContent = cartCount; cartBadge.dataset.empty = String(cartCount === 0); }
  if (wishBadge) { wishBadge.textContent = wishCount; wishBadge.dataset.empty = String(wishCount === 0); }
}
updateHomeShoppingCounts();
window.addEventListener('pageshow', updateHomeShoppingCounts);
window.addEventListener('clearly-shop-loaded', updateHomeShoppingCounts);
if (window.ClearlyShop) window.ClearlyShop.load().then(updateHomeShoppingCounts).catch(() => {});

// The home category cards must follow the same live catalog as the admin drawer.
// Keep the written card copy as a fallback, but replace it as soon as the API is available.
const originalCategories = document.querySelector('#categories');
if (originalCategories && !document.querySelector('#categories-continuous')) {
  const duplicate = originalCategories.cloneNode(true);
  duplicate.id = 'categories-continuous';
  duplicate.dataset.autoscroll = 'off';
  originalCategories.replaceWith(duplicate);
}

const categoryCarouselReady = (() => {
  const section = document.querySelector('#categories-continuous');
  const track = section?.querySelector('.category-grid');
  if (!track) return Promise.resolve();
  const api = (location.protocol === 'file:' || location.port === '4173')
    ? 'http://localhost:8080/api/categories' : '/api/categories';
  const homepageApi = (location.protocol === 'file:' || location.port === '4173')
    ? 'http://localhost:8080/api/homepage' : '/api/homepage';
  const copy = value => String(value == null ? '' : value).replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
  const cardFor = category => {
    const card = document.createElement('a');
    const cardVariant = category.slug === 'housekeeping-cleaning-supplies' ? ' housekeeping-card' : (category.slug === 'construction-chemicals' ? ' construction-card' : '');
    card.className = 'cat' + cardVariant;
    card.href = `./category.html?category=${encodeURIComponent(category.slug)}`;
    card.setAttribute('aria-label', `Open ${category.name} collection`);
    card.title = `View ${category.name} collection`;
    const sceneImage = category.slug === 'housekeeping-cleaning-supplies' ? 'assets/categories/floor-care-living-hall.png' : (category.imageUrl || 'assets/categories/floor-care-living-hall.png');
    card.innerHTML = `<div><h3>${copy(category.name)}</h3><p>${copy(category.description || 'Explore our collection.')}</p></div><img class="category-scene" src="${copy(sceneImage)}" alt="${copy(category.name)}"><span class="cat-arrow" aria-hidden="true">→</span>`;
    return card;
  };
  return fetch(api, {cache: 'no-store'}).then(response => {
    if (!response.ok) throw new Error('Categories unavailable');
    return response.json();
  }).then(rows => {
    const categories = (rows || []).filter(category => category.enabled !== false)
      .sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0) || String(a.name).localeCompare(String(b.name)));
    track.replaceChildren(...categories.map(cardFor));
    return fetch(homepageApi, {cache: 'no-store'}).then(response => response.ok ? response.json() : {});
  }).then(settings => {
    section.dataset.autoscroll = String(settings?.categoryAutoScroll).toLowerCase() === 'true' ? 'continuous' : 'off';
  }).catch(() => {});
})();

function initialiseCategoryCarousels() { document.querySelectorAll('.categories').forEach(section => {
  const viewport = section.querySelector('.category-viewport');
  const track = section.querySelector('.category-grid');
  if (!viewport || !track) return;
  const prev = section.querySelector('.category-prev');
  const next = section.querySelector('.category-next');
  const continuous = section.dataset.autoscroll === 'continuous';
  const autoScroll = continuous;
  let loopWidth = 0;
  let originalCards = [];
  let repeatedCards = [];
  function measureLoop() {
    loopWidth = repeatedCards[0] && originalCards[0]
      ? repeatedCards[0].offsetLeft - originalCards[0].offsetLeft
      : track.scrollWidth / 2;
  }
  if (continuous) {
    originalCards = [...track.children];
    repeatedCards = originalCards.map(card => {
      const copy = card.cloneNode(true);
      copy.dataset.loopCopy = 'true';
      copy.setAttribute('aria-hidden', 'true');
      copy.tabIndex = -1;
      track.append(copy);
      return copy;
    });
    measureLoop();
  }
  let autoFrame = 0;
  let offset = 0, direction = 1, animation = 0, timer = 0;
  let gesture = null, suppressClick = false;
  const maximum = () => Math.max(0, track.scrollWidth - viewport.clientWidth);
  const clamp = value => Math.max(0, Math.min(value, maximum()));
  const wrap = value => loopWidth ? ((value % loopWidth) + loopWidth) % loopWidth : 0;
  // Animation and drag share one position. The repeated card set makes wrapping invisible.
  function render(value) {
    offset = continuous ? wrap(value) : clamp(value);
    track.style.transform = `translate3d(${-offset}px, 0, 0)`;
    if (prev) prev.disabled = !continuous && offset <= 0;
    if (next) next.disabled = !continuous && offset >= maximum();
  }
  function stop() {
    cancelAnimationFrame(autoFrame);
    autoFrame = 0;
    cancelAnimationFrame(animation);
    animation = 0;
    clearTimeout(timer);
  }
  function moveTo(value) {
    stop();
    const from = offset, to = continuous ? value : clamp(value), start = performance.now();
    function frame(now) {
      const progress = Math.min(1, (now - start) / 1500);
      render(from + (to - from) * (1 - Math.cos(Math.PI * progress)) / 2);
      animation = progress < 1 ? requestAnimationFrame(frame) : 0;
      if (!animation && continuous) schedule();
    }
    animation = requestAnimationFrame(frame);
  }
  function schedule() {
    clearTimeout(timer);
    cancelAnimationFrame(autoFrame);
    if (!autoScroll || gesture || document.hidden) return;
    if (continuous) {
      if (animation) return;
      let last = performance.now();
      function drift(now) {
        const elapsed = Math.min(50, now - last) / 1000;
        last = now;
        render(offset + 22 * elapsed);
        autoFrame = requestAnimationFrame(drift);
      }
      autoFrame = requestAnimationFrame(drift);
      return;
    }
    timer = setTimeout(() => {
      if (offset >= maximum() - 1) direction = -1;
      if (offset <= 1) direction = 1;
      moveTo(offset + viewport.clientWidth * direction);
      schedule();
    }, 7000);
  }
  prev?.addEventListener('click', () => { moveTo(offset - viewport.clientWidth); schedule(); });
  next?.addEventListener('click', () => { moveTo(offset + viewport.clientWidth); schedule(); });
  viewport.addEventListener('pointerdown', event => {
    if (gesture || event.isPrimary === false || event.button !== 0 || event.target.closest('button')) return;
    stop();
    suppressClick = false;
    gesture = {id:event.pointerId, x:event.clientX, y:event.clientY, offset, moved:false};
    // Keep normal clicks on their anchor; capture only after the drag threshold.
  });
  window.addEventListener('pointermove', event => {
    if (!gesture || event.pointerId !== gesture.id) return;
    const dx = event.clientX - gesture.x, dy = event.clientY - gesture.y;
    if (!gesture.moved) {
      if (Math.abs(dx) <= 6) return;
      if (event.pointerType === 'touch' && Math.abs(dy) > Math.abs(dx)) { finish(); return; }
      gesture.moved = true;
      viewport.classList.add('is-dragging');
      viewport.setPointerCapture(event.pointerId);
    }
    event.preventDefault();
    render(gesture.offset - dx);
  }, {passive:false});
  function finish(event) {
    if (!gesture || (event?.pointerId != null && event.pointerId !== gesture.id)) return;
    const ended = gesture;
    gesture = null;
    suppressClick = ended.moved;
    viewport.classList.remove('is-dragging');
    if (viewport.hasPointerCapture(ended.id)) viewport.releasePointerCapture(ended.id);
    schedule();
  }
  window.addEventListener('pointerup', finish);
  window.addEventListener('pointercancel', finish);
  viewport.addEventListener('lostpointercapture', finish);
  window.addEventListener('blur', () => { finish(); stop(); });
  window.addEventListener('focus', schedule);
  viewport.addEventListener('dragstart', event => event.preventDefault());
  viewport.addEventListener('click', event => {
    if (suppressClick && event.detail !== 0) { event.preventDefault(); event.stopPropagation(); }
    suppressClick = false;
  }, true);
  window.addEventListener('resize', () => {
    finish();
    stop();
    if (continuous) measureLoop();
    render(offset);
    schedule();
  });
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) { finish(); stop(); } else schedule();
  });
  track.style.transition = 'none';
  render(0);
  schedule();
}); }
categoryCarouselReady.then(initialiseCategoryCarousels);
