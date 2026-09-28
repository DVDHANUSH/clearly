(function () {
  'use strict';
  var form = document.getElementById('contact-form');
  if (!form) return;
  var apiBase = (location.protocol === 'file:' || location.port === '4173') ? 'http://localhost:8085/api/contact' : '/api/contact';
  var status = document.getElementById('contact-status');
  var phone = document.getElementById('contact-phone');
  var countryCode = document.getElementById('contact-country-code');
  var otpPanel = document.getElementById('contact-otp-panel');
  var otpInput = document.getElementById('contact-otp');
  var otpStatus = document.getElementById('contact-otp-status');
  var sendOtpButton = document.getElementById('contact-send-otp');
  var verifyOtpButton = document.getElementById('contact-verify-otp');
  var imagesInput = document.getElementById('contact-images');
  var fileSummary = document.getElementById('contact-file-summary');
  var verificationId = '';
  var verifiedPhone = '';

  function normalizedPhone() {
    var localNumber = phone.value.trim().replace(/[^\d]/g, '').replace(/^0+/, '');
    return countryCode.value + localNumber;
  }
  function setOtpStatus(message, success) {
    otpStatus.textContent = message;
    otpStatus.classList.toggle('contact-verified-text', !!success);
  }
  function requestJson(url, payload) {
    return fetch(url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(payload)})
      .then(function (response) { return response.json().catch(function () { return {}; }).then(function (body) {
        if (!response.ok) throw new Error(body.message || 'Something went wrong. Please try again.');
        return body;
      }); });
  }

  phone.addEventListener('input', function () {
    if (verifiedPhone && normalizedPhone() !== verifiedPhone) {
      verifiedPhone = ''; verificationId = ''; phone.classList.remove('contact-verified');
      setOtpStatus('Phone number changed. Please request a new OTP.', false); otpPanel.hidden = true;
    }
  });
  countryCode.addEventListener('change', function () {
    verifiedPhone = ''; verificationId = ''; phone.classList.remove('contact-verified');
    otpPanel.hidden = true; status.textContent = '';
  });
  sendOtpButton.addEventListener('click', function () {
    var value = normalizedPhone();
    if (!/^\+[1-9]\d{7,14}$/.test(value)) { status.textContent = 'Enter a valid phone number with country code, for example +919791046050.'; return; }
    sendOtpButton.disabled = true; status.textContent = '';
    requestJson(apiBase + '/otp/send', {phone: value}).then(function (body) {
      verificationId = body.verificationId; otpPanel.hidden = false;
      setOtpStatus(body.message || 'OTP sent. It is valid for 5 minutes.', false); otpInput.focus();
    }).catch(function (error) { status.textContent = error.message; })
      .finally(function () { sendOtpButton.disabled = false; });
  });
  verifyOtpButton.addEventListener('click', function () {
    var value = normalizedPhone();
    if (!verificationId || !/^\d{6}$/.test(otpInput.value.trim())) { setOtpStatus('Enter the 6-digit OTP sent to your phone.', false); return; }
    verifyOtpButton.disabled = true;
    requestJson(apiBase + '/otp/verify', {phone: value, verificationId: verificationId, otp: otpInput.value.trim()})
      .then(function (body) { verifiedPhone = value; phone.classList.add('contact-verified'); setOtpStatus(body.message || 'Phone number verified.', true); })
      .catch(function (error) { setOtpStatus(error.message, false); })
      .finally(function () { verifyOtpButton.disabled = false; });
  });
  imagesInput.addEventListener('change', function () {
    var files = Array.prototype.slice.call(imagesInput.files || []);
    if (files.length > 3) { imagesInput.value = ''; fileSummary.textContent = 'Select no more than 3 images.'; return; }
    var invalid = files.find(function (file) { return !/^image\/(jpeg|png|webp)$/.test(file.type) || file.size > 5 * 1024 * 1024; });
    if (invalid) { imagesInput.value = ''; fileSummary.textContent = 'Use JPG, PNG or WebP images under 5 MB each.'; return; }
    fileSummary.textContent = files.length ? files.length + (files.length === 1 ? ' image selected' : ' images selected') : 'No images selected';
  });
  form.addEventListener('submit', function (event) {
    event.preventDefault();
    var button = form.querySelector('button[type="submit"]');
    var details = {name: document.getElementById('contact-name').value.trim(), email: document.getElementById('contact-email').value.trim(), phone: normalizedPhone(), topic: document.getElementById('contact-topic').value, orderId: document.getElementById('contact-order').value.trim(), message: document.getElementById('contact-message').value.trim(), verificationId: verificationId};
    if (!details.name || !details.email || !details.message) { status.textContent = 'Please enter your name, email and message.'; return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(details.email)) { status.textContent = 'Please enter a valid email address.'; return; }
    if (!verifiedPhone || verifiedPhone !== details.phone) { status.textContent = 'Please verify your phone number before sending the enquiry.'; return; }
    var data = new FormData();
    data.append('details', new Blob([JSON.stringify(details)], {type: 'application/json'}));
    Array.prototype.forEach.call(imagesInput.files || [], function (file) { data.append('images', file, file.name); });
    status.textContent = 'Sending your enquiry…'; button.disabled = true;
    fetch(apiBase, {method: 'POST', body: data}).then(function (response) {
      return response.json().catch(function () { return {}; }).then(function (body) { if (!response.ok) throw new Error(body.message || 'The enquiry could not be sent.'); return body; });
    }).then(function (body) {
      status.textContent = body.message || 'Your enquiry has been sent successfully.'; status.style.color = '#16805b';
      form.reset(); verificationId = ''; verifiedPhone = ''; otpPanel.hidden = true; phone.classList.remove('contact-verified'); fileSummary.textContent = 'No images selected';
    }).catch(function (error) { status.textContent = error.message; status.style.color = '#c44735'; })
      .finally(function () { button.disabled = false; });
  });
})();
