(function () {
  'use strict';
  var grid = document.getElementById('shop-subcategory-grid');
  if (!grid) return;
  var api = (location.protocol === 'file:' || location.port === '4173') ? 'http://localhost:8080/api' : '/api';
  var escapeHtml = function (value) { return String(value || '').replace(/[&<>"']/g, function (character) { return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[character]; }); };
  function leaves(tree) {
    var result = [], seen = {};
    (tree || []).forEach(function (category) {
      (category.subcategories || []).forEach(function (subcategory) {
        var items = subcategory.children && subcategory.children.length ? subcategory.children : [subcategory];
        items.forEach(function (item) {
          if (item.enabled === false || seen[String(item.name).toLowerCase()]) return;
          seen[String(item.name).toLowerCase()] = true;
          result.push(item);
        });
      });
    });
    return result.sort(function (a, b) { return Number(a.sortOrder || 0) - Number(b.sortOrder || 0) || String(a.name).localeCompare(String(b.name)); });
  }
  function render(catalog, products) {
    var items = leaves(catalog.categoryTree);
    if (!items.length) { grid.innerHTML = '<div class="shop-subcategory-loading">No subcategories are available yet.</div>'; return; }
    grid.innerHTML = items.map(function (item) {
      var href = 'products.html?category=' + encodeURIComponent(item.name);
      var matchingProduct = (products || []).find(function (product) { return String(product.subcategory || product.category || '').toLowerCase() === String(item.name).toLowerCase() && product.image; });
      var image = item.imageUrl || (matchingProduct && matchingProduct.image);
      var visual = image ? '<img src="' + escapeHtml(image) + '" alt="">' : '<span class="shop-subcategory-placeholder" aria-hidden="true">' + escapeHtml(String(item.name || '?').charAt(0)) + '</span>';
      return '<a class="shop-subcategory-card" href="' + href + '"><span class="shop-subcategory-visual">' + visual + '</span><strong>' + escapeHtml(item.name) + '</strong></a>';
    }).join('');
  }
  Promise.all([fetch(api + '/catalog', {cache:'no-store'}), fetch(api + '/products', {cache:'no-store'})]).then(function (responses) { if (!responses[0].ok || !responses[1].ok) throw new Error(); return Promise.all(responses.map(function (response) { return response.json(); })); }).then(function (data) { render(data[0], data[1]); }).catch(function () { grid.innerHTML = '<div class="shop-subcategory-loading">Subcategories could not be loaded.</div>'; });
  window.addEventListener('storage', function (event) { if (event.key === 'clearly_catalog_revision') location.reload(); });
})();
