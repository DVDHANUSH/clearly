const packs = document.querySelectorAll('.pack');
const choiceTitle = document.querySelector('#choice-title');
const choiceCopy = document.querySelector('#choice-copy');
const choicePrice = document.querySelector('#choice-price');
const cartButton = document.querySelector('#cart-button');
const exploreButton = document.querySelector('.primary[href="#packs"]');

exploreButton.addEventListener('click', (event) => {
  event.preventDefault();
  document.querySelector('#packs').scrollIntoView({ behavior: 'smooth', block: 'start' });
});

packs.forEach((pack) => pack.addEventListener('click', () => {
  packs.forEach((item) => item.classList.remove('selected'));
  pack.classList.add('selected');
  choiceTitle.textContent = pack.dataset.title;
  choiceCopy.textContent = pack.dataset.copy;
  choicePrice.textContent = pack.dataset.price;
  cartButton.textContent = pack.dataset.price === 'Request a quote' ? 'Request a quote →' : 'Add to bag +';
}));

cartButton.addEventListener('click', () => {
  cartButton.textContent = 'Added to bag ✓';
  cartButton.classList.add('added');
  setTimeout(() => { cartButton.textContent = 'Add to bag +'; cartButton.classList.remove('added'); }, 1400);
});