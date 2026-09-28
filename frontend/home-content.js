(function(){
  'use strict';
  var api=(location.protocol==='file:'||location.port==='4173')?'http://localhost:8080/api/homepage':'/api/homepage';
  var defaultFaqs=[
    {question:'Do you offer discounts for bulk orders?',answer:'Yes. Eligible package sizes display a quantity-discount table on the product page, with the price per unit updating for each quantity tier.'},
    {question:'How do I choose the correct package size?',answer:'Open the size selector on the product card or product page. Each size shows its own image, MRP, selling price and available bulk savings.'},
    {question:'How long does delivery take?',answer:'Orders are normally dispatched within 24–48 hours. Enter your six-digit PIN code on a product page to see the estimated delivery window for your location.'},
    {question:'Is free delivery available?',answer:'Free delivery is available on eligible orders of ₹500 or more. Any applicable delivery charge is shown before payment.'},
    {question:'Can I return a product?',answer:'Eligible unopened products can be returned within 30 days. Contact support with your order details so the return can be reviewed and arranged.'},
    {question:'Will I receive a GST invoice?',answer:'Yes. Add your GSTIN during checkout when required. After successful payment, your invoice can be downloaded from your account.'},
    {question:'Which payment methods are supported?',answer:'Secure checkout is powered by Razorpay and supports the payment methods enabled for the store, including cards, UPI and other available options.'},
    {question:'How can I track my order?',answer:'Sign in to your Clearly account to view your order information. Confirmation and status messages may also be sent by email, SMS or WhatsApp.'},
    {question:'Can businesses request help choosing products?',answer:'Yes. Hotels, restaurants, institutions and commercial buyers can contact our team for help selecting products, pack sizes and quantities.'}
  ];
  function text(id,value){var element=document.getElementById(id);if(element&&value!=null&&value!=='')element.textContent=value;}
  function esc(value){return String(value||'').replace(/[&<>"']/g,function(character){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[character];});}
  function faqItems(value){try{var parsed=JSON.parse(value||'[]');return Array.isArray(parsed)?parsed.filter(function(item){return item&&item.question&&item.answer;}).slice(0,9):defaultFaqs;}catch(error){return defaultFaqs;}}
  function renderFaqs(value){var list=document.getElementById('home-faq-list');if(!list)return;var items=faqItems(value);if(!items.length)items=defaultFaqs;list.innerHTML=items.map(function(item,index){return '<details class="faq-item"><summary><span class="faq-number">'+String(index+1).padStart(2,'0')+'</span><span>'+esc(item.question)+'</span><i aria-hidden="true"></i></summary><div class="faq-answer"><p>'+esc(item.answer)+'</p></div></details>';}).join('');}
  function revealHero(image){var revealed=false;function done(){if(revealed)return;revealed=true;document.body.classList.remove('home-content-loading');}if(!image){done();return;}if(image.complete&&image.naturalWidth){done();return;}image.addEventListener('load',done,{once:true});image.addEventListener('error',done,{once:true});setTimeout(done,3000);}
  function apply(content){
    text('home-hero-eyebrow',content.heroEyebrow);text('home-hero-title-1',content.heroTitleLine1);text('home-hero-title-2',content.heroTitleLine2);text('home-hero-accent',content.heroTitleAccent);text('home-hero-description',content.heroDescription);
    var heroButton=document.getElementById('home-hero-button');if(heroButton){text('home-hero-button',null);heroButton.querySelector('span').textContent=content.heroButtonLabel||'Shop Now';heroButton.href=content.heroButtonUrl||'#shop';}
    var heroImage=document.getElementById('home-hero-image'),mobileImage=document.getElementById('home-hero-mobile-image');if(heroImage&&content.heroImage)heroImage.src=content.heroImage;if(mobileImage&&content.heroMobileImage)mobileImage.srcset=content.heroMobileImage;revealHero(heroImage);
    text('home-offer-eyebrow',content.offerEyebrow);text('home-offer-title',content.offerTitle);text('home-offer-accent',content.offerAccent);text('home-offer-description',content.offerDescription);var offerButton=document.getElementById('home-offer-button');if(offerButton){offerButton.querySelector('span').textContent=content.offerButtonLabel||'Shop Now';offerButton.href=content.offerButtonUrl||'#shop';}var discount=document.getElementById('home-offer-discount');if(discount&&content.offerDiscount)discount.innerHTML=String(content.offerDiscount).replace(/[^0-9.]/g,'')+'<sup>%</sup>';
    text('home-promise-eyebrow',content.promiseEyebrow);text('home-promise-title',content.promiseTitle);text('home-promise-accent',content.promiseAccent);text('home-promise-description',content.promiseDescription);
    renderFaqs(content.faqItems);
  }
  function load(){return fetch(api,{cache:'no-store'}).then(function(response){if(!response.ok)throw new Error();return response.json();}).then(apply).catch(function(){revealHero(document.getElementById('home-hero-image'));});}
  renderFaqs(JSON.stringify(defaultFaqs));load();window.addEventListener('storage',function(event){if(event.key==='clearly_homepage_revision')load();});
})();
