const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const querystring = require('querystring');

const root = __dirname;
const dataDir = path.join(root, 'data');
const uploadsDir = path.join(root, 'assets', 'uploads');
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, {recursive:true});
if (!fs.existsSync(uploadsDir)) fs.mkdirSync(uploadsDir, {recursive:true});

const types = { '.css': 'text/css', '.js': 'text/javascript', '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg', '.webp': 'image/webp', '.html': 'text/html', '.svg': 'image/svg+xml', '.json': 'application/json', '.pdf': 'application/pdf' };

function readBody(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => resolve(body));
  });
}

function parseProductData() {
  try {
    const raw = fs.readFileSync(path.join(dataDir, 'products.json'), 'utf8');
    return JSON.parse(raw);
  } catch { return {products: [], brands: {}, categories: [], nextId: 1}; }
}

function writeProductData(data) {
  fs.writeFileSync(path.join(dataDir, 'products.json'), JSON.stringify(data, null, 2));
}

function slugify(text) {
  return text.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/-+$/, '').replace(/^-+/, '');
}

http.createServer(async (request, response) => {
  const parsed = new URL(request.url, 'http://127.0.0.1');
  const pathname = parsed.pathname;
  const method = request.method;

  // Admin API routes
  if (pathname.startsWith('/api/')) {
    response.setHeader('Access-Control-Allow-Origin', '*');
    response.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
    response.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    if (method === 'OPTIONS') { response.writeHead(204); response.end(); return; }

    if (pathname === '/api/products' && method === 'GET') {
      const data = parseProductData();
      response.writeHead(200, {'Content-Type':'application/json'});
      response.end(JSON.stringify(data.products));
      return;
    }

    if (pathname === '/api/catalog' && method === 'GET') {
      const data = parseProductData();
      response.writeHead(200, {'Content-Type':'application/json'});
      response.end(JSON.stringify({brands:data.brands || {}, categories:data.categories || []}));
      return;
    }

    if (pathname === '/api/catalog' && method === 'PUT') {
      const body = await readBody(request);
      try {
        const updates = JSON.parse(body);
        const data = parseProductData();
        data.brands = updates.brands && typeof updates.brands === 'object' ? updates.brands : (data.brands || {});
        data.categories = Array.isArray(updates.categories) ? updates.categories : (data.categories || []);
        writeProductData(data);
        response.writeHead(200, {'Content-Type':'application/json'});
        response.end(JSON.stringify({brands:data.brands,categories:data.categories}));
      } catch (err) {
        response.writeHead(400, {'Content-Type':'application/json'});
        response.end(JSON.stringify({error:'Invalid catalog data'}));
      }
      return;
    }

    if (pathname === '/api/products' && method === 'POST') {
      const body = await readBody(request);
      const product = JSON.parse(body);
      const data = parseProductData();
      product.id = 'PROD-' + (data.nextId++);
      product.slug = slugify(product.name);
      product.image = product.image || '';
      product.bulkDiscounts = product.bulkDiscounts || [{minQty:1,unitPrice:product.price}];
      product.createdAt = new Date().toISOString();
      product.updatedAt = product.createdAt;
      data.products.push(product);
      writeProductData(data);
      response.writeHead(201, {'Content-Type':'application/json'});
      response.end(JSON.stringify(product));
      return;
    }

    const productMatch = pathname.match(/^\/api\/products\/([^/]+)$/);
    if (productMatch) {
      const pid = decodeURIComponent(productMatch[1]);
      const data = parseProductData();
      const idx = data.products.findIndex(p => p.id === pid);

      if (method === 'GET') {
        if (idx === -1) { response.writeHead(404); response.end('Not found'); return; }
        response.writeHead(200, {'Content-Type':'application/json'});
        response.end(JSON.stringify(data.products[idx]));
        return;
      }

      if (method === 'PUT') {
        if (idx === -1) { response.writeHead(404); response.end('Not found'); return; }
        const body = await readBody(request);
        const updates = JSON.parse(body);
        data.products[idx] = Object.assign(data.products[idx], updates, {updatedAt: new Date().toISOString()});
        if (updates.name) data.products[idx].slug = slugify(updates.name);
        writeProductData(data);
        response.writeHead(200, {'Content-Type':'application/json'});
        response.end(JSON.stringify(data.products[idx]));
        return;
      }

      if (method === 'DELETE') {
        if (idx === -1) { response.writeHead(404); response.end('Not found'); return; }
        data.products.splice(idx, 1);
        writeProductData(data);
        response.writeHead(200, {'Content-Type':'application/json'});
        response.end(JSON.stringify({success:true}));
        return;
      }
    }

    if (pathname === '/api/uploads' && method === 'POST') {
      try {
        if (!request.headers['content-type']?.includes('multipart/form-data')) {
          response.writeHead(400, {'Content-Type':'application/json'});
          response.end(JSON.stringify({error:'Expected multipart/form-data'}));
          return;
        }
        const contentType = request.headers['content-type'];
        const boundary = '--' + contentType.split('boundary=')[1].split(';')[0].trim();
        const chunks = [];
        for await (const chunk of request) chunks.push(chunk);
        const body = Buffer.concat(chunks).toString('binary');
        const parts = body.split(boundary);
        for (const part of parts) {
          if (!part.trim() || part.startsWith('--')) continue;
          const headerEnd = part.indexOf('\r\n\r\n');
          if (headerEnd === -1) continue;
          const headerSection = part.slice(0, headerEnd);
          const bodySection = part.slice(headerEnd + 4).replace(/--\r?\n$/, '');
          const filenameMatch = headerSection.match(/filename="([^"]+)"/);
          if (filenameMatch) {
            const ext = path.extname(filenameMatch[1]).toLowerCase();
            if (!['.png','.jpg','.jpeg','.webp','.gif','.svg','.pdf'].includes(ext)) {
              response.writeHead(400, {'Content-Type':'application/json'});
              response.end(JSON.stringify({error:'Unsupported file type. Use PNG, JPG, WebP, GIF or SVG.'}));
              return;
            }
            const filename = 'prod-' + Date.now() + '-' + Math.random().toString(36).slice(2,6) + ext;
            const filepath = path.join(uploadsDir, filename);
            fs.writeFileSync(filepath, Buffer.from(bodySection, 'binary'));
            const url = '/assets/uploads/' + filename;
            response.writeHead(201, {'Content-Type':'application/json'});
            response.end(JSON.stringify({filename, url, size: bodySection.length}));
            return;
          }
        }
        response.writeHead(400, {'Content-Type':'application/json'});
        response.end(JSON.stringify({error:'No file found in request'}));
      } catch (err) {
        console.error('Upload error:', err.message);
        response.writeHead(500, {'Content-Type':'application/json'});
        response.end(JSON.stringify({error:'Upload failed: ' + err.message}));
      }
      return;
    }

    response.writeHead(404);
    response.end('Not found');
    return;
  }

  // Static file serving
  const requestPath = pathname === '/' ? 'index.html' : (pathname.endsWith('/') ? decodeURIComponent(pathname + 'index.html') : decodeURIComponent(pathname));
  const filePath = path.join(root, requestPath);
  fs.readFile(filePath, (error, file) => {
    if (error) {
      response.writeHead(404, { 'Content-Type': 'text/plain' });
      response.end('Not found');
      return;
    }
    response.writeHead(200, { 'Content-Type': types[path.extname(filePath)] || 'application/octet-stream' });
    response.end(file);
  });
}).listen(4173, '127.0.0.1', () => console.log('Preview ready at http://127.0.0.1:4173\nAdmin panel: http://127.0.0.1:4173/admin.html'));
