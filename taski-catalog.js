/* TASKI R-series: mapped to the supplied Adhithya Chemicals PDF and Zarnik,
 * accessed 2026-09-07. Retailer prices are references, not confirmed store prices.
 * PDF page 7 gives product added to 1 litre of water, not final solution volume.
 */
const TASKI_PRODUCTS = [
  { code: 'R1', name: 'TASKI R1 Super', purpose: 'Bathroom cleaner concentrate', price: 1950, category: 'Bathroom Care', slug: 'taski-r1-super-bathroom-cleaner-sanitiser-concentrate-5l-dv1001', pages: [1,7], dilution: ['20 ml','25 ml'],
    use: 'Concentrated cleaner for routine cleaning of suitable bathroom sinks, tubs, tiles and fittings. Designed for professional housekeeping.',
    benefits: ['Bathroom surfaces','Concentrated formula','Routine housekeeping'],
    steps: [['Measure & dilute','Distributor PDF reference per 1 litre of water: normal soiling 20 ml; heavy soiling 25 ml. Follow the current product label for the exact application.'],['Apply & clean','Wipe suitable bathroom surfaces with a cloth or sponge.'],['Rinse & dry','Rinse thoroughly and dry fittings.']],
    bulkDiscounts: [{minQty:1,unitPrice:1950},{minQty:2,unitPrice:1890,savePct:3.1,saveAmt:120},{minQty:4,unitPrice:1810,savePct:7.2,saveAmt:560},{minQty:8,unitPrice:1740,savePct:10.8,saveAmt:1680}] },
  { code: 'R2', name: 'TASKI R2', purpose: 'Hard-surface cleaner concentrate', price: 2050, category: 'Industrial', slug: 'taski-r2-hard-surface-cleaner-sanitiser-concentrate-5l-dv1002', pages: [2,7], dilution: ['20 ml','40 ml'],
    use: 'A concentrated general-purpose cleaner for suitable washable hard surfaces, including cabinets, tables and other housekeeping surfaces. Apply to a cloth rather than directly to electrical equipment.',
    benefits: ['Washable hard surfaces','Concentrated formula','Everyday housekeeping'],
    steps: [['Measure & dilute','Follow the pack dilution for the intended task.'],['Apply to a cloth','Wipe suitable hard surfaces with the diluted solution.'],['Wipe & finish','Remove residue with a clean, dry cloth.']] },
  { code: 'R3', name: 'TASKI R3', purpose: 'Glass & mirror cleaner concentrate', price: 2450, category: 'Industrial', slug: 'taski-r3-professional-glass-mirror-cleaner-concentrate-5l-dv1003', pages: [3,7], dilution: ['20 ml','50 ml'],
    use: 'A concentrated cleaner for windows, mirrors and other suitable glass surfaces. Use with a clean, lint-free cloth for a clear finish.',
    benefits: ['Glass & mirrors','Concentrated formula','Lint-free finishing'],
    steps: [['Measure & dilute','Use the dilution stated on the product label.'],['Apply & wipe','Apply to a clean cloth and wipe the glass.'],['Buff dry','Finish with a dry, lint-free cloth.']],
    bulkDiscounts: [{minQty:1,unitPrice:2450},{minQty:2,unitPrice:2376,savePct:3,saveAmt:148},{minQty:4,unitPrice:2279,savePct:7,saveAmt:724},{minQty:8,unitPrice:2180,savePct:11,saveAmt:2160}] },
  { code: 'R4', name: 'TASKI R4 Shine-Up', purpose: 'Wooden furniture maintainer', price: 2600, category: 'Industrial', slug: 'taski-r4-shine-up-wooden-furniture-polish-maintainer-5l-dv1004', pages: [3,4,7],
    use: 'Ready-to-use furniture maintainer for suitable sealed wooden furnishings. Apply sparingly to a soft cloth, then wipe and buff.',
    benefits: ['Wooden furniture care','Ready to use','Buffed finish'],
    steps: [['Ready to use','Shake well. Do not dilute.'],['Apply to a cloth','Use a small amount on a soft cloth, not directly on furniture.'],['Wipe & buff','Wipe suitable sealed wood and buff to a sheen.']], extraCare: 'Test an inconspicuous area first. Do not apply to floors or surfaces where slipperiness would be hazardous.',
    bulkDiscounts: [{minQty:1,unitPrice:2600},{minQty:2,unitPrice:2520,savePct:3.1,saveAmt:160},{minQty:4,unitPrice:2410,savePct:7.3,saveAmt:760},{minQty:8,unitPrice:2310,savePct:11.2,saveAmt:2320}] },
  { code: 'R5', name: 'TASKI R5', purpose: 'Room air freshener', price: 1850, category: 'Industrial', slug: 'taski-r5-professional-air-freshener-room-deodorizer-5l-dv1005', pages: [4,7],
    use: 'A ready-to-use, water-based air freshener for guest rooms, offices and shared indoor spaces. Use after cleaning and dealing with the source of unwanted odours.',
    benefits: ['Room air freshening','Ready to use','Water-based formula'],
    steps: [['Clean first','Finish cleaning and remove the source of odours.'],['Ready to use','Use an appropriately labelled trigger bottle. Do not dilute.'],['Mist into the air','Spray upward, away from people, fabrics and furniture.']], extraCare: 'Do not spray towards people, food, fabrics, furniture or plastic surfaces. Follow label ventilation guidance.',
    bulkDiscounts: [{minQty:1,unitPrice:1850},{minQty:2,unitPrice:1793,savePct:3.1,saveAmt:114},{minQty:4,unitPrice:1718,savePct:7.1,saveAmt:528},{minQty:8,unitPrice:1646,savePct:11,saveAmt:1632}] },
  { code: 'R6', name: 'TASKI R6', purpose: 'Toilet bowl & urinal cleaner', price: 1250, category: 'Bathroom Care', slug: 'taski-r6-professional-toilet-bowl-urinal-cleaner-5l-dv1006', pages: [4,7],
    use: 'Ready-to-use cleaner for toilet bowls and urinals. Apply with a suitable dispensing bottle, allow the label-specified contact time, brush and flush.',
    benefits: ['Toilet bowls & urinals','Ready to use','Targeted bathroom cleaning'],
    steps: [['Ready to use','Wet the bowl. Apply using a suitable dispensing bottle.'],['Allow & brush','Follow label contact time, then scrub with a toilet brush.'],['Flush thoroughly','Flush away all cleaner residue.']], extraCare: 'Acidic cleaner. Never combine with bleach or other cleaning chemicals. Use only on suitable toilet bowls and urinals.',
    bulkDiscounts: [{minQty:1,unitPrice:1250},{minQty:2,unitPrice:1210,savePct:3.2,saveAmt:80},{minQty:4,unitPrice:1160,savePct:7.2,saveAmt:360},{minQty:8,unitPrice:1110,savePct:11.2,saveAmt:1120}] },
  { code: 'R7', name: 'TASKI R7', purpose: 'Floor cleaner concentrate', price: 910, category: 'Industrial', slug: 'taski-r7-neutral-floor-cleaner-concentrate-5l-dv1007', pages: [5,7], dilution: ['20 ml','50 ml'],
    use: 'Concentrated cleaner for routine wet mopping of suitable water-resistant floors. Also suitable for compatible floor-cleaning machines when used according to their instructions.',
    benefits: ['Routine floor cleaning','Concentrated formula','Manual or machine use'],
    steps: [['Measure & dilute','Follow the pack dilution for the soil level.'],['Mop the floor','Wring the mop and work toward the exit.'],['Let it dry','Keep the area closed until the floor is dry.']], extraCare: 'Use wet-floor signs and prevent access until the surface is dry. Check floor and machine compatibility before use.',
    bulkDiscounts: [{minQty:1,unitPrice:910},{minQty:2,unitPrice:882,savePct:3.1,saveAmt:56},{minQty:4,unitPrice:846,savePct:7,saveAmt:256},{minQty:8,unitPrice:810,savePct:11,saveAmt:800}] },
  { code: 'R9', name: 'TASKI R9', purpose: 'Hard-water bathroom cleaner concentrate', price: 1450, category: 'Bathroom Care', slug: 'taski-r9-hard-water-bathroom-cleaner-descaler-conc-5l-dv1016', pages: [5,7], dilution: ['50 ml','100 ml'],
    use: 'A concentrated bathroom cleaner for hard-water areas, designed to address deposits and routine soiling on compatible bathroom surfaces. Rinse thoroughly after cleaning.',
    benefits: ['Hard-water bathroom care','Concentrated formula','Rinse-clean finish'],
    steps: [['Check & dilute','Avoid acid-sensitive surfaces. Follow label dilution.'],['Apply & clean','Apply with a cloth or sponge to suitable bathroom surfaces.'],['Rinse & dry','Rinse thoroughly and dry the fittings.']], extraCare: 'Do not use on acid-sensitive surfaces. The retailer gives conflicting natural-stone advice; confirm compatibility using the current manufacturer label/TDS before use.',
    bulkDiscounts: [{minQty:1,unitPrice:1450},{minQty:2,unitPrice:1405,savePct:3.1,saveAmt:90},{minQty:4,unitPrice:1340,savePct:7.6,saveAmt:440},{minQty:8,unitPrice:1280,savePct:11.7,saveAmt:1360}] }
].map(function (p) {
  const code=p.code.toLowerCase();
  return Object.assign(p, {
    id:'DIV-TASKI-'+p.code, brand:'diversey', subBrand:'TASKI', sizes:['5 L'],
    image:'assets/taski/'+code+'-studio.png',
    images:['assets/taski/'+code+'-studio.png','assets/taski/'+code+'-benefits.png','assets/taski/'+code+'-howto.png','assets/taski/'+code+'-source.webp'],
    galleryAlts:[p.name+' enhanced 5 L pack',p.name+' uses and features',p.name+' three-step cleaning guide',p.name+' original supplier packaging photo'],
    infoImage:'assets/taski/'+code+'-howto.png',
    originalPrice: 2399,
    tags:['taski',p.code.toLowerCase(),p.purpose.toLowerCase(),p.category==='Industrial'?'housekeeping':'bathroom'],
    howToItems:p.steps.map(function(s){return ['✓',s[0],s[1]];}),
    packInfo:p.dilution ? '5 L pack. Distributor PDF reference per 1 litre of water: normal soiling '+p.dilution[0]+'; heavy soiling '+p.dilution[1]+'. Follow the current product label for the exact application. These are cleaning dilutions, not a verified disinfection protocol.' : '5 L pack. Ready to use; do not dilute. Follow the current product label and use a suitable, clearly labelled dispenser.',
    careItems:[['🧤','Handle safely','Wear gloves and eye protection.'],['🚫','Do not mix','Never mix chemicals. Keep away from children and food.'],['📋','Check the label',p.extraCare||'Follow the label and test an inconspicuous area first.']]
  });
});
if (typeof module !== 'undefined' && module.exports) module.exports=TASKI_PRODUCTS;
