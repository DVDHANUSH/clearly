(function(){
  'use strict';
  var base=(location.protocol==='file:'||location.port==='4173')?'http://localhost:8085/api/checkout':'/api/checkout';
  function token(){return sessionStorage.getItem('clearly_access_token')||localStorage.getItem('clearly_access_token')||'';}
  var checkoutToken='';
  function api(path, options) {
    var authToken=checkoutToken||token();
    if(!authToken)return Promise.reject(new Error('Please sign in before continuing to checkout.'));
    var requestOptions = Object.assign({}, options || {});
    requestOptions.headers = Object.assign(
      {
        'Content-Type': 'application/json',
        Authorization: 'Bearer '+authToken
      },
      options && options.headers || {}
    );

    return fetch(base+path,requestOptions)
      .then(function(response){
        return response.json().catch(function(){
          return {};
        }).then(function(body){
          if(response.status===401){
            checkoutToken='';
            sessionStorage.removeItem('clearly_access_token');
            localStorage.removeItem('clearly_access_token');
            throw new Error('Your sign-in session has expired. Please sign in again, then retry checkout.');
          }
          if(!response.ok){
            throw new Error(body.detail||body.message||body.error||('Checkout failed (HTTP '+response.status+')'));
          }
          return body;
        });
      });
  }
  function loadRazorpay(){if(window.Razorpay)return Promise.resolve();return new Promise(function(resolve,reject){var script=document.createElement('script');script.src='https://checkout.razorpay.com/v1/checkout.js';script.onload=resolve;script.onerror=function(){reject(new Error('Could not load secure payment window'));};document.head.appendChild(script);});}
  function cartItems(){var cart={},products=typeof PRODUCTS!=='undefined'?PRODUCTS:[];try{cart=JSON.parse(sessionStorage.getItem('clearly_cart')||'{}')||{};}catch(error){}return Object.keys(cart).map(function(key){var parts=String(key).split('@@'),id=parts[0],size=parts.length>1?decodeURIComponent(parts.slice(1).join('@@')):'';var product=products.find(function(item){return item.id===id;});if(!product)return null;var packages=(product.packages||[]).filter(function(item){return item.enabled!==false;}),pkg=packages.find(function(item){return item.label===size;})||packages.find(function(item){return item.isDefault;})||packages[0];return {productRef:product.productRef||id,name:product.name,packageSize:size||pkg&&pkg.label||product.packaging||product.sizes&&product.sizes[0]||'Standard pack',packageCode:pkg&&pkg.packageCode||'',quantity:Number(cart[key])||1};}).filter(Boolean);}
  function shell(){var node=document.getElementById('checkout-modal');if(node)return node;node=document.createElement('div');node.id='checkout-modal';node.className='checkout-modal';node.hidden=true;node.innerHTML='<div class="checkout-backdrop" data-close-checkout></div><section class="checkout-dialog" role="dialog" aria-modal="true" aria-labelledby="checkout-title"><button class="checkout-close" data-close-checkout aria-label="Close">×</button><span class="page-kicker">SECURE CHECKOUT</span><h2 id="checkout-title">Billing details</h2><div id="seller-billing" class="seller-billing"></div><form id="checkout-form"><div class="checkout-fields"><label>Billing name<input name="name" required></label><label>Phone<input name="phone" required pattern="[+0-9 ]{10,16}"></label><label>Email<input name="email" type="email"></label><label>GSTIN (optional)<input name="gstin"></label><label class="wide">Billing address<input name="address" required></label><label>City<input name="city" required></label><label>State<input name="state" required></label><label>PIN code<input name="postalCode" required pattern="[1-9][0-9]{5}" maxlength="6"></label></div><p class="checkout-message" id="checkout-message"></p><button class="checkout" type="submit">Pay securely with Razorpay</button></form><div id="checkout-success" class="checkout-success" hidden></div></section>';document.body.appendChild(node);node.querySelectorAll('[data-close-checkout]').forEach(function(button){button.onclick=function(){node.hidden=true;};});node.querySelector('#checkout-form').onsubmit=submit;node.addEventListener('click',function(event){var button=event.target.closest('[data-document]');if(button)downloadDocument(button.dataset.document,button.dataset.filename);});return node;}
  function open(){checkoutToken=token();var modal=shell(),message=modal.querySelector('#checkout-message');message.textContent='';modal.querySelector('#checkout-success').hidden=true;modal.querySelector('#checkout-form').hidden=false;modal.hidden=false;api('/config').then(function(config){var seller=config.seller||{};modal.querySelector('#seller-billing').innerHTML='<b>Sold and billed by '+(seller.displayName||'Adhithya Chemicals')+'</b><span>'+[seller.addressLine1,seller.addressLine2,seller.city,seller.state,seller.postalCode].filter(Boolean).join(', ')+'</span>';if(!config.configured)message.textContent='Razorpay is ready, but test keys must be added to the server before payment can start.';}).catch(function(error){message.textContent=error.message;if(error.message.indexOf('sign-in session')===0||error.message.indexOf('Please sign in')===0)setTimeout(function(){location.href='account.html?return=cart.html&required=signin';},900);});}
  function submit(event){event.preventDefault();var form=event.currentTarget,button=form.querySelector('[type=submit]'),message=document.querySelector('#checkout-message'),address=Object.fromEntries(new FormData(form).entries());button.disabled=true;button.textContent='Creating secure payment…';Promise.all([api('/orders',{method:'POST',body:JSON.stringify({items:cartItems(),billingAddress:address})}),loadRazorpay()]).then(function(results){var order=results[0],profile={};try{profile=JSON.parse(localStorage.getItem('clearly_profile')||'{}')||{};}catch(error){}var checkout=new Razorpay({key:order.keyId,amount:order.amount,currency:order.currency,name:order.seller.displayName||'Adhithya Chemicals',description:'Order '+order.orderNo,order_id:order.razorpayOrderId,prefill:{name:address.name,email:address.email,contact:address.phone},theme:{color:'#2d68f6'},handler:function(response){message.textContent='Verifying your payment…';api('/verify',{method:'POST',body:JSON.stringify({localOrderId:order.localOrderId,razorpayOrderId:response.razorpay_order_id,razorpayPaymentId:response.razorpay_payment_id,razorpaySignature:response.razorpay_signature})}).then(success);}});checkout.on('payment.failed',function(){message.textContent='Payment was not completed. Your cart is unchanged.';});checkout.open();}).catch(function(error){message.textContent=error.message;if(error.message.indexOf('sign-in session')===0)setTimeout(function(){location.href='account.html?return=cart.html&required=signin';},900);}).finally(function(){button.disabled=false;button.textContent='Pay securely with Razorpay';});}
  function success(result){sessionStorage.setItem('clearly_cart','{}');var form=document.querySelector('#checkout-form'),panel=document.querySelector('#checkout-success');form.hidden=true;panel.hidden=false;panel.innerHTML='<span class="success-check">✓</span><h2>Order confirmed</h2><p>'+result.orderNo+' has been paid and saved.</p><div><button class="secondary" data-document="'+result.invoiceUrl+'" data-filename="invoice.pdf">Download invoice</button><button class="secondary" data-document="'+result.slipUrl+'" data-filename="order-slip.pdf">Download order slip</button></div><a class="primary-link" href="profile.html">View my orders</a>';}
  function downloadDocument(path,filename){fetch((location.protocol==='file:'||location.port==='4173'?'http://localhost:8085':'')+path,{headers:{Authorization:'Bearer '+token()}}).then(function(response){if(!response.ok)throw new Error('Document unavailable');return response.blob();}).then(function(blob){var link=document.createElement('a');link.href=URL.createObjectURL(blob);link.download=filename;link.click();setTimeout(function(){URL.revokeObjectURL(link.href);},1000);});}
  document.addEventListener('click',function(event){if(event.target.closest('#checkout-button')){event.preventDefault();open();}});
})();
