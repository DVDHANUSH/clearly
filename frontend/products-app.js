(function () {
  "use strict";

  var brandsEl = document.getElementById("brand-select");
  var categoryEl = document.getElementById("category-select");
  var sortEl = document.getElementById("sort-select");
  var stockEl = document.getElementById("in-stock-only");
  var searchEl = document.getElementById("search");
  var gridEl = document.getElementById("product-grid");
  var breadcrumbEl = document.getElementById("breadcrumb");
  var countEl = document.getElementById("result-count");
  var emptyEl = document.getElementById("empty-state");
  var clearEl = document.getElementById("clear-filters");
  var savedShelfEl = document.getElementById("saved-shelf");
  var savedGridEl = document.getElementById("saved-grid");
  var savedToggle = document.getElementById("saved-toggle");
  var savedCountEl = document.getElementById("saved-count");
  var savedClose = document.getElementById("saved-shelf-close");
  var bagEl = document.getElementById("bag");
  var bagCountEl = document.getElementById("bag-count");
  var cartSummaryEl = document.getElementById("cart-summary");
  var cartItemsEl = document.getElementById("cart-items");
  var cartTotalEl = document.getElementById("cart-total");

  var url = new URL(location.href, location.origin);

  // Read brand from hash fragment (#fosroc) so it survives the server intact
  function hashBrand() {
    var h = location.hash.replace(/^#/, "").toLowerCase();
    return (h && BRANDS[h]) ? h : "";
  }

  // ---------- Cart ----------
  var cart = {}; // id -> qty
  try { cart = JSON.parse(sessionStorage.getItem("clearly_cart") || "{}"); } catch (e) { cart = {}; }

  function saveCart() { try { sessionStorage.setItem("clearly_cart", JSON.stringify(cart)); } catch (e) {} }

  function cartTotal() { var total = 0; for (var id in cart) { var p = PRODUCT_BY_ID[id]; if (p) total += p.price * cart[id]; } return total; }
  function cartCount() { var c = 0; for (var id in cart) c += cart[id]; return c; }

  function renderCart() {
    var items = [];
    for (var id in cart) { items.push({ id: id, qty: cart[id] }); }
    bagCountEl.textContent = cartCount();
    bagEl.classList.toggle("has-items", cartCount() > 0);
    if (items.length === 0) { cartSummaryEl.hidden = true; return; }
    cartSummaryEl.hidden = false;
    var html = "";
    for (var i = 0; i < items.length; i++) { var it = items[i]; var p = PRODUCT_BY_ID[it.id];
      html += "<div class='cart-item'><span>" + escapeHtml(p.name) + "</span><b>₹" + p.price + "</b><span class='qty'>×" + it.qty + "</span></div>";
    }
    cartItemsEl.innerHTML = html;
    cartTotalEl.textContent = "₹" + cartTotal();
  }

  function addToCart(id) {
    if (!PRODUCT_BY_ID[id]) return;
    if (!cart[id]) cart[id] = 0;
    cart[id] += 1;
    saveCart();
    renderCart();
    flashCart();
  }

  function flashCart() {
    var node = bagEl;
    var orig = node.style.transform;
    node.style.transform = "scale(.86)";
    setTimeout(function () { node.style.transform = ""; }, 120);
  }

  // ---------- Saved shelf ----------
  var saved = [];
  try { saved = JSON.parse(localStorage.getItem("clearly_saved") || "[]"); } catch (e) { saved = []; }

  function saveSaved() { try { localStorage.setItem("clearly_saved", JSON.stringify(saved)); } catch (e) {} }

  function isSaved(id) { return saved.indexOf(id) !== -1; }
  function isSavedShelfVisible() { return savedShelfEl && !savedShelfEl.hidden; }

  function toggleSaved(id) {
    var idx = saved.indexOf(id);
    if (idx === -1) saved.push(id); else saved.splice(idx, 1);
    saveSaved();
    updateSavedCount();
    renderGrid();
    // if shelf is open, refresh it
    if (isSavedShelfVisible()) renderSavedShelf();
  }

  function updateSavedCount() {
    if (!savedCountEl) return;
    savedCountEl.textContent = saved.length;
    savedToggle.classList.toggle("has-items", saved.length > 0);
    savedToggle.innerHTML = (saved.length > 0 ? "♥" : "♡") + " <span id='saved-count'>" + saved.length + "</span>";
    var n = document.getElementById("saved-count");
    if (n) n.textContent = saved.length;
  }

  function renderSavedShelf() {
    if (!savedGridEl) return;
    if (saved.length === 0) { savedShelfEl.hidden = true; return; }
    savedShelfEl.hidden = false;
    var html = "";
    for (var i = 0; i < saved.length; i++) { var p = PRODUCT_BY_ID[saved[i]]; if (!p) continue;
      html += "<div class='saved-item'><span class='saved-pin'>saved</span>" + productCardHtml(p, true) + "</div>";
    }
    savedGridEl.innerHTML = html;
  }

  savedToggle.addEventListener("click", function () {
    if (!savedShelfEl) return;
    if (savedShelfEl.hidden) { savedShelfEl.hidden = false; renderSavedShelf();
      savedShelfEl.scrollIntoView({ behavior: "smooth", block: "start" }); }
    else { savedShelfEl.hidden = true; }
  });
  if (savedClose) savedClose.addEventListener("click", function () { savedShelfEl.hidden = true; });

  // ---------- URL state ----------
  function readParams() {
    return {
      brand: url.searchParams.get("brand") || "",
      category: url.searchParams.get("category") || "",
      inStock: url.searchParams.get("inStock") === "1",
      tags: url.searchParams.getAll("tag"),
      sort: url.searchParams.get("sort") || "relevance",
      search: url.searchParams.get("search") || ""
    };
  }

  function writeParams(p) {
    url.searchParams.delete("brand");
    url.searchParams.delete("category");
    url.searchParams.delete("inStock");
    url.searchParams.delete("sort");
    url.searchParams.delete("search");
    if (p.brand) url.searchParams.set("brand", p.brand);
    if (p.category) url.searchParams.set("category", p.category);
    if (p.inStock) url.searchParams.set("inStock", "1");
    if (p.tags && p.tags.length) { url.searchParams.delete("tag"); p.tags.forEach(function (t) { url.searchParams.append("tag", t); }); }
    if (p.sort && p.sort !== "relevance") url.searchParams.set("sort", p.sort);
    if (p.search) url.searchParams.set("search", p.search);
    else url.searchParams.delete("search");
    history.replaceState({}, "", url);
  }

  // ---------- Filtering / sort / search ----------
  function currentFilters() {
    var p = readParams();
    var hb = hashBrand();
    if (hb) p.brand = hb;
    return p;
  }

  function filterAndSort() {
    var p = currentFilters();
    var list = PRODUCTS.slice();
    if (p.brand) list = list.filter(function (x) { return x.brand === p.brand; });
    if (p.category) list = list.filter(function (x) { return x.category === p.category; });
    if (p.inStock) list = list.filter(function (x) { return x.inStock; });
    if (p.tags && p.tags.length) {
      var tl = p.tags.map(function (t) { return t.toLowerCase(); });
      list = list.filter(function (x) { return x.tags.some(function (t) { return tl.indexOf(t.toLowerCase()) !== -1; }); });
    }
    if (p.search) {
      var q = p.search.toLowerCase().trim();
      if (q) {
        list = list.filter(function (x) {
          return x.name.toLowerCase().indexOf(q) !== -1 ||
            x.brand.toLowerCase().indexOf(q) !== -1 ||
            x.category.toLowerCase().indexOf(q) !== -1 ||
            (x.tags && x.tags.some(function (t) { return t.toLowerCase().indexOf(q) !== -1; }));
        });
      }
    }
    switch (p.sort) {
      case "price-asc": list.sort(function (a, b) { return a.price - b.price; }); break;
      case "price-desc": list.sort(function (a, b) { return b.price - a.price; }); break;
      case "rating": list.sort(function (a, b) { return b.rating - a.rating || a.price - b.price; }); break;
      default: list.sort(function (a, b) { return a.name.localeCompare(b.name); });
    }
    return list;
  }

  function renderOptions() {
    var brands = [];
    for (var k in BRANDS) brands.push(k);
    brands.sort();

    var curBrand = hashBrand() || url.searchParams.get("brand");
    var curCat = url.searchParams.get("category");
    var curSort = url.searchParams.get("sort");

    // brand options
    var bh = "<option value=''>All brands</option>";
    for (var i = 0; i < brands.length; i++) { var b = brands[i];
      bh += "<option value='" + b + "'" + (b === curBrand ? " selected" : "") + ">" + BRANDS[b] + "</option>";
    }
    brandsEl.innerHTML = bh;

    // category options
    CATEGORIES.sort();
    var ch = "<option value=''>All categories</option>";
    for (var j = 0; j < CATEGORIES.length; j++) { var c = CATEGORIES[j];
      ch += "<option value='" + c + "'" + (c === curCat ? " selected" : "") + ">" + c + "</option>";
    }
    categoryEl.innerHTML = ch;

    sortEl.value = curSort || "relevance";
    stockEl.checked = url.searchParams.get("inStock") === "1";

    if (curBrand) {
      var bl = BRANDS[curBrand] || curBrand;
      breadcrumbEl.innerHTML = "<span class='crumb'><a href='products.html'>All products</a></span><span class='sep'>/</span><span class='crumb'>" + escapeHtml(bl) + "</span>";
    } else {
      breadcrumbEl.innerHTML = "<span class='crumb'><a href='products.html'>All products</a></span>";
    }

    // result count
    var list = filterAndSort();
    countEl.textContent = list.length === 0 ? "No products" :
      (list.length + " product" + (list.length === 1 ? "" : "s"));
  }

  function renderGrid() {
    var list = filterAndSort();
    if (list.length === 0) {
      gridEl.innerHTML = "";
      emptyEl.hidden = false;
      return;
    }
    emptyEl.hidden = true;
    var html = "";
    for (var i = 0; i < list.length; i++) html += productCardHtml(list[i], false);
    gridEl.innerHTML = html;
  }

  function productCardHtml(p, small) {
    var stockBadge = p.inStock ? "" : "<span class='sold-out'>Out of stock</span>";
    var btnClass = "cart-btn";
    if (!p.inStock) btnClass += " sold";
    var savedCls = "wishlist-btn" + (isSaved(p.id) ? " on" : "");
    var ico = isSaved(p.id) ? "♥" : "♡";
    var sizesHtml = "";
    if (p.sizes && p.sizes.length) {
      sizesHtml = "<div class='sizes'>" + p.sizes.map(function (s) { return "<span>" + escapeHtml(s) + "</span>"; }).join("") + "</div>";
    }
    return "<article class='product-card' data-id='" + p.id + "'>" +
      stockBadge +
      (p.image
        ? "<div class='product-img'><img src='" + escapeAttr(p.image) + "' alt='" + escapeAttr(p.name) + "' /></div>"
        : "<div class='product-placeholder'></div>") +
      "<span class='product-brand'>" + escapeHtml(BRANDS[p.brand] || p.brand) + "</span>" +
      "<div class='product-category'>" + escapeHtml(p.category) + "</div>" +
      "<h3 class='product-name'>" + escapeHtml(p.name) + "</h3>" +
      (p.rating ? "<span class='product-stars'>" + starsHtml(p.rating) + "</span>" : "") +
      sizesHtml +
      "<div class='product-price'>" +
        (p.originalPrice && p.originalPrice > p.price
          ? "<span class='old'>₹" + p.originalPrice + "</span>"
          : "") +
        "<span class='now'>₹" + p.price + "</span>" +
      "</div>" +
      "<div class='product-actions'>" +
        "<button class='" + btnClass + "' data-cart='" + p.id + "'>" + (p.inStock ? "Add to bag" : "Unavailable") + "</button>" +
        "<button class='" + savedCls + "' data-wish='" + p.id + "'><span class='ico'>" + ico + "</span> Save for later</button>" +
      "</div>" +
    "</article>";
  }

  function starsHtml(r) {
    var full = Math.floor(r);
    var half = (r - full) >= 0.5 ? 1 : 0;
    var empty = 5 - full - half;
    return "★".repeat(full) + (half ? "⯨" : "") + "<i>" + "★".repeat(empty) + "</i> <i>(" + r.toFixed(1) + ")</i>";
  }

  // ---------- Events ----------
  brandsEl.addEventListener("change", function () {
    var v = brandsEl.value;
    if (v) { url.searchParams.set("brand", v); url.searchParams.delete("category"); url.searchParams.delete("tag"); url.searchParams.delete("inStock");
      url.searchParams.delete("search"); }
    else url.searchParams.delete("brand");
    syncSortStock();
    apply();
  });
  categoryEl.addEventListener("change", function () {
    var v = categoryEl.value;
    if (v) url.searchParams.set("category", v); else url.searchParams.delete("category");
    syncSortStock();
    apply();
  });
  sortEl.addEventListener("change", function () {
    var v = sortEl.value;
    if (v === "relevance") url.searchParams.delete("sort"); else url.searchParams.set("sort", v);
    apply();
  });
  stockEl.addEventListener("change", function () {
    if (stockEl.checked) url.searchParams.set("inStock", "1"); else url.searchParams.delete("inStock");
    apply();
  });

  var searchTimer = null;
  searchEl.addEventListener("input", function () {
    var val = searchEl.value;
    if (val) url.searchParams.set("search", val); else url.searchParams.delete("search");
    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(function () { apply(); }, 250);
  });

  clearEl.addEventListener("click", function () {
    url.searchParams.delete("brand");
    url.searchParams.delete("category");
    url.searchParams.delete("tag");
    url.searchParams.delete("inStock");
    url.searchParams.delete("search");
    url.searchParams.delete("sort");
    syncSortStock();
    apply();
  });

  function syncSortStock() {
    sortEl.value = (url.searchParams.get("sort") || "relevance");
    stockEl.checked = url.searchParams.get("inStock") === "1";
  }

  // click delegation
  gridEl.addEventListener("click", function (e) {
    var btn = e.target.closest("button[data-cart]");
    if (btn) { e.preventDefault(); addToCart(btn.getAttribute("data-cart")); return; }
    var wish = e.target.closest("button[data-wish]");
    if (wish) { e.preventDefault(); toggleSaved(wish.getAttribute("data-wish")); return; }
  });

  function apply() { renderOptions(); renderGrid(); }
  updateSavedCount();
  renderCart();
  // initial render: populate options first so selected values are correct
  renderOptions();
  renderGrid();
  // re-render saved shelf (empty state if none)
  renderSavedShelf();

  // ---------- Helpers ----------
  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }
  function escapeAttr(s) { return escapeHtml(s); }

  // Build PRODUCT_BY_ID
  var PRODUCT_BY_ID = {};
  for (var i = 0; i < PRODUCTS.length; i++) PRODUCT_BY_ID[PRODUCTS[i].id] = PRODUCTS[i];

  // update saved count on saved count element changes
  var savedCountObserver = mutationSavedCount;
  function mutationSavedCount() {
    var n = document.getElementById("saved-count");
    if (n) n.textContent = saved.length;
  }

  // nav bag "has-items" style
  var style = document.createElement("style");
  style.textContent =
    ".bag.has-items b{background:var(--lime);} " +
    ".saved-toggle.has-items{color:var(--saved);} " +
    ".saved-shelf{display:none;} " +
    ".saved-shelf[hidden]{display:none;} " +
    ".cart-summary{display:none;} " +
    ".cart-summary[hidden]{display:none;}";
  document.head.appendChild(style);
})();
