(function(){
  'use strict';
  var api=(location.protocol==='file:'||location.port==='4173')?'http://localhost:8080/api/homepage':'/api/homepage';
  function text(id,value){var element=document.getElementById(id);if(element&&value!=null&&value!=='')element.textContent=value;}
  function revealHero(image){var revealed=false;function done(){if(revealed)return;revealed=true;document.body.classList.remove('home-content-loading');}if(!image){done();return;}if(image.complete&&image.naturalWidth){done();return;}image.addEventListener('load',done,{once:true});image.addEventListener('error',done,{once:true});setTimeout(done,3000);}
  function apply(content){
    text('home-hero-eyebrow',content.heroEyebrow);text('home-hero-title-1',content.heroTitleLine1);text('home-hero-title-2',content.heroTitleLine2);text('home-hero-accent',content.heroTitleAccent);text('home-hero-description',content.heroDescription);
    var heroButton=document.getElementById('home-hero-button');if(heroButton){text('home-hero-button',null);heroButton.querySelector('span').textContent=content.heroButtonLabel||'Shop Now';heroButton.href=content.heroButtonUrl||'#shop';}
    var heroImage=document.getElementById('home-hero-image'),mobileImage=document.getElementById('home-hero-mobile-image');if(heroImage&&content.heroImage)heroImage.src=content.heroImage;if(mobileImage&&content.heroMobileImage)mobileImage.srcset=content.heroMobileImage;revealHero(heroImage);
    text('home-offer-eyebrow',content.offerEyebrow);text('home-offer-title',content.offerTitle);text('home-offer-accent',content.offerAccent);text('home-offer-description',content.offerDescription);var offerButton=document.getElementById('home-offer-button');if(offerButton){offerButton.querySelector('span').textContent=content.offerButtonLabel||'Shop Now';offerButton.href=content.offerButtonUrl||'#shop';}var discount=document.getElementById('home-offer-discount');if(discount&&content.offerDiscount)discount.innerHTML=String(content.offerDiscount).replace(/[^0-9.]/g,'')+'<sup>%</sup>';
    text('home-promise-eyebrow',content.promiseEyebrow);text('home-promise-title',content.promiseTitle);text('home-promise-accent',content.promiseAccent);text('home-promise-description',content.promiseDescription);
  }
  function load(){return fetch(api,{cache:'no-store'}).then(function(response){if(!response.ok)throw new Error();return response.json();}).then(apply).catch(function(){revealHero(document.getElementById('home-hero-image'));});}
  load();window.addEventListener('storage',function(event){if(event.key==='clearly_homepage_revision')load();});
})();
