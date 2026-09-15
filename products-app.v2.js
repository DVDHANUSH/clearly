/**
 * Shared product catalog.
 * Read by: products.html (catalog + saved shelf) and index.html (featured list, via the
 * page's own script inclusion order below).
 *
 * Brands (slug -> label used for filter chips):
 *   fosroc         -> Fosroc
 *   diversey       -> Diversey
 *   godrej         -> Godrej
 *   aditya-birla   -> Aditya Birla
 *   shine-all      -> Shine All
 */
const BRANDS = {
  "fosroc": "Fosroc",
  "diversey": "Diversey",
  "godrej": "Godrej",
  "aditya-birla": "Aditya Birla",
  "shine-all": "Shine All",
};

const CATEGORIES = [
  "Floor Care",
  "Bathroom Care",
  "Kitchen Care",
  "Laundry Care",
  "Tile Care",
  "Concrete Repair",
  "Waterproofing",
  "Precision Grouting",
  "Concrete Admixture",
  "Surface Care",
];

const PRODUCT_TAGS = [
  "spray",
  "liquid",
  "disinfectant",
  "degreaser",
  "multi-surface",
  "anti-microbial",
  "concentrate",
  "refill",
  "bulk",
  "cementitious",
  "precision-grout",
  "non-shrink",
  "high-strength",
];

const PRODUCTS = [
  // Fosroc
  { id: "FOS-NG-007", brand: "fosroc", name: "Nitotile Grout", category: "Tile Care",
    price: 399, originalPrice: 449, rating: 4.8, image: "assets/nitotile-grout-pack.png",
    inStock: true, tags: ["cementitious","tile","joint-filler"], sizes: ["1 kg"], packWater: "300 ml", use: "For filling 1-6 mm joints in glazed wall tiles, porcelain, mosaic, vitrified and fully vitrified tiles, natural stone and engineered stone.", howToUse: "Clean the joints, wet them with clean water and let the surface become dry. Mix 1 kg powder with 300 ml clean potable water until lump-free. Apply with a squeegee, spatula or putty blade, then clean diagonally with a damp sponge after about 15 minutes.", care: "Work in small areas. Avoid staining unglazed or textured tiles by testing a small area first. Do not use below 5°C or in moving water. Wear eye protection and avoid breathing cement dust.",
    bulkDiscounts: [{minQty:1,unitPrice:399},{minQty:2,unitPrice:387,savePct:3,saveAmt:24},{minQty:5,unitPrice:375,savePct:6,saveAmt:120},{minQty:10,unitPrice:360,savePct:10,saveAmt:390}] },
  { id: "FOS-NB-008", brand: "fosroc", name: "Nitobond EP", category: "Concrete Repair",
    price: 1299, originalPrice: 1499, rating: 4.8, image: "assets/nitobond-ep-new.png", inStock: true, tags: ["epoxy","concrete","bonding-agent"], sizes: ["2 L", "4 L"], use: "A two-part epoxy bonding agent that helps bond fresh concrete to an existing concrete surface.", howToUse: "Clean and prepare the existing concrete surface. Mix Part A and Part B as directed on the pack, apply the mixed bonding coat, then place the fresh concrete while the coat is ready for overcoating.", care: "Use suitable gloves and eye protection. Follow the pack instructions for mixing, working time and surface preparation.",
    bulkDiscounts: [{minQty:1,unitPrice:1299},{minQty:2,unitPrice:1250,savePct:3.8,saveAmt:98},{minQty:4,unitPrice:1200,savePct:7.6,saveAmt:396},{minQty:8,unitPrice:1140,savePct:12.2,saveAmt:1272}] },
  { id: "FOS-BR-009", brand: "fosroc", name: "Brushbond RFX", category: "Waterproofing",
    price: 899, originalPrice: 999, rating: 4.8, image: "assets/brushbond-rfx-pack.png", inStock: true, tags: ["waterproofing","concrete","coating"], sizes: ["6.71 kg", "20.088 kg", "30 kg"], use: "A two-component cementitious waterproof coating for concrete, brick and blockwork, including roofs, water tanks, wet areas and other exposed structures.", howToUse: "Clean the surface so it is free from oil, grease, dirt and loose material. Dampen the surface, mix the powder and liquid components with the required clean water, then apply with a stiff brush, roller or trowel. Apply two coats and let the first coat reach a touch-dry state before the second.", howToItems: [["🧹", "Clean & dampen", "Remove oil, grease, dirt and loose material, then dampen the surface."], ["🪣", "Mix the components", "Combine powder, liquid and the required clean water."], ["🖌️", "Apply two coats", "Brush, roll or trowel on the coating. Let coat one become touch-dry before coat two."]], packRows: [["6.71 kg", "5 kg", "1.71 kg", "1 litre"], ["20.088 kg", "15 kg", "5.088 kg", "3 litres"], ["30 kg", "22.5 kg", "7.5 kg", "4.5 litres"]], careItems: [["🌡️", "10°C or warmer", "Do not apply below 10°C"], ["🧤", "Protect your hands", "Wear gloves while handling"], ["🥽", "Protect your eyes", "Wear safety goggles"], ["😷", "Avoid dust", "Use a dust mask with the powder"], ["⏱️", "30-minute pot life", "Use the mixed product within this time at 27°C"]],
    bulkDiscounts: [{minQty:1,unitPrice:899},{minQty:2,unitPrice:868,savePct:3.4,saveAmt:62},{minQty:4,unitPrice:836,savePct:7,saveAmt:252},{minQty:8,unitPrice:800,savePct:11,saveAmt:792}] },
  { id: "FOS-CG-010", brand: "fosroc", name: "Conbextra GP2", category: "Precision Grouting",
    price: 2499, originalPrice: 2799, rating: 4.8, image: "assets/conbextra-gp2-pack.png", inStock: true, tags: ["cementitious","precision-grout","non-shrink","high-strength"], sizes: ["25 kg"], use: "A free-flow, high-strength, non-shrink cementitious precision grout for machine bases, bridge bearings, precast construction and structural load transfer.", howToItems: [["🧹", "Prepare the base", "Clean the substrate, remove loose material and dampen the surface without leaving standing water."], ["🪣", "Mix the grout", "Measure 4.125 litres for pourable or 4.5 litres for flowable consistency, then slowly add the 25 kg powder while mixing."], ["⬇️", "Place continuously", "Pour from one side to avoid trapped air and maintain a continuous grout front beneath the supported element."]], packRows: [["25 kg bag", "4.125 litres", "Pourable", "Approx. 12.5 litres"], ["25 kg bag", "4.5 litres", "Flowable", "Approx. 13.3 litres"]], careItems: [["🧤", "Protect your hands", "Wear suitable gloves while handling the dry powder and mixed grout."], ["🥽", "Protect your eyes", "Wear eye protection during mixing and placement."], ["😷", "Avoid dust", "Use a dust mask when opening and handling the powder."], ["⏱️", "Work continuously", "Plan labour and mixing capacity so the grout can be placed without interruption."], ["💧", "Cure the grout", "Protect the placed grout and follow the TDS curing guidance as it gains strength."]],
    bulkDiscounts: [{minQty:1,unitPrice:2499},{minQty:2,unitPrice:2424,savePct:3,saveAmt:148},{minQty:4,unitPrice:2324,savePct:7,saveAmt:696},{minQty:8,unitPrice:2200,savePct:12,saveAmt:2392}] },

  { id: "FOS-AM-011", brand: "fosroc", name: "Auramix 500", category: "Concrete Admixture",
    price: 12499, originalPrice: 13999, rating: 4.9, image: "assets/auramix-500-barrel.png", inStock: true, tags: ["superplasticiser","polycarboxylic","high-strength","workability-retention"], sizes: ["200 L barrel"], use: "A high-performance polycarboxylic superplasticiser for ultra-high-strength, self-compacting and pumpable concrete where very low water-cement ratio and long workability retention are required.", howToItems: [["🧪", "Dose accurately", "Measure the recommended admixture dose for the concrete mix and confirm the mix design before batching."], ["🌀", "Blend into concrete", "Add Auramix 500 with the mixing water and disperse it thoroughly through the concrete."], ["🏗️", "Place with confidence", "Pump or place the cohesive concrete while retaining workability and avoiding segregation."]], packInfo: "Supplied in a 200 L barrel. Dosage should be established by trial mixes and the approved concrete mix design.", careItems: [["🧤", "Protect your hands", "Wear suitable gloves when handling the liquid admixture."], ["🥽", "Protect your eyes", "Use eye protection during transfer and batching."], ["🧪", "Trial before use", "Confirm dosage, compatibility and workability retention with trial mixes."], ["🌡️", "Store correctly", "Keep the barrel sealed and store as recommended in the product data sheet."], ["📋", "Follow the mix design", "Use only the approved dosage and concrete production procedure."]],
    bulkDiscounts: [{minQty:1,unitPrice:12499},{minQty:2,unitPrice:12124,savePct:3,saveAmt:750},{minQty:4,unitPrice:11624,savePct:7,saveAmt:3496},{minQty:6,unitPrice:11199,savePct:10.4,saveAmt:7800}] },

  { id: "FOS-RG-012", brand: "fosroc", name: "Brushbond Roofguard", category: "Waterproofing",
    price: 1299, originalPrice: 1499, rating: 4.8, image: "assets/brushbond-roofguard-pack.png", inStock: true, tags: ["waterproofing","roofing","acrylic","fibre-reinforced"], sizes: ["20 L bucket"], use: "A high-build fibre-reinforced acrylic elastomeric waterproofing coating for old and new roofs, applied in multiple coats to form a seamless, weather-resistant protective layer.", howToItems: [["🧹", "Prepare the roof", "Remove oil, grease, wax, dirt and loose material. Repair weak areas and dampen the sound surface."], ["🪣", "Apply the primer", "Dilute one part Roofguard with one part water and brush it over the prepared roof."], ["🖌️", "Build the waterproof layer", "Apply three coats with a short stiff brush, allowing each coat to become touch-dry."]], packInfo: "Supplied in a 20 L bucket. Apply multiple coats to achieve the specified total dry film thickness.", careItems: [["🌡️", "Check the temperature", "Do not apply when the substrate temperature is below 10°C."], ["🧹", "Use a stiff brush", "Apply the coating like paint with a short stiff brush about 120–150 mm wide."], ["☀️", "Avoid hot surfaces", "For very hot roofs, saturate the surface with water before application."], ["⏱️", "Respect coat intervals", "Allow each coat to reach a touch-dry state before applying the next."], ["🧤", "Protect yourself", "Wear suitable gloves and eye protection while applying the coating."]],
    bulkDiscounts: [{minQty:1,unitPrice:1299},{minQty:2,unitPrice:1256,savePct:3.3,saveAmt:86},{minQty:4,unitPrice:1200,savePct:7.6,saveAmt:396},{minQty:8,unitPrice:1130,savePct:11.5,saveAmt:1352}] },

  { id: "FOS-AQ-019", brand: "fosroc", name: "Brushbond AquaProtect", category: "Waterproofing",
    price: 1899, originalPrice: 2199, rating: 4.9, image: "assets/brushbond-aquaprotect-pack.png", inStock: true, tags: ["waterproofing","potable-water","water-tanks","swimming-pools"], sizes: ["5 kg", "15 kg"], use: "A two-component cementitious waterproof coating for water-retaining structures, suitable for potable-water tanks, swimming pools and concrete or masonry surfaces.", howToItems: [["🧹", "Prepare the surface", "Clean sound concrete or masonry and dampen the substrate before coating."], ["🪣", "Mix the coating", "Combine the powder and liquid components until the mix is smooth and uniform."], ["🖌️", "Apply two coats", "Brush on two even coats, allowing the first coat to become touch-dry before the second."]], packInfo: "Available in 5 kg and 15 kg kits. Follow the TDS for component ratios, coverage and curing requirements.", careItems: [["💧", "Potable-water safe", "Use only after the coating has fully cured as specified for water-retaining applications."], ["🧱", "Sound substrate", "Repair weak or spalled concrete before starting the waterproofing work."], ["🧤", "Protect yourself", "Wear gloves, eye protection and suitable dust protection while mixing."], ["⏱️", "Respect cure time", "Allow the coating to cure fully before filling the tank or pool."], ["🔧", "Use the right tool", "Apply evenly with a stiff brush or suitable approved application tool."]],
    bulkDiscounts: [{minQty:1,unitPrice:1899},{minQty:2,unitPrice:1832,savePct:3.5,saveAmt:134},{minQty:4,unitPrice:1767,savePct:7,saveAmt:528},{minQty:8,unitPrice:1695,savePct:10.7,saveAmt:1632}] },

  { id: "FOS-CP-014", brand: "fosroc", name: "Conplast SP430", category: "Concrete Admixture",
    price: 799, originalPrice: 899, rating: 4.8, image: "assets/conplast-sp430-studio.png", sizeImages: { "5 L": "assets/conplast-sp430-5l-enhanced.png", "20 L": "assets/conplast-sp430-20l.png" }, inStock: true, tags: ["superplasticiser","concrete","chloride-free","precast"], sizes: ["5 L", "20 L"], use: "A chloride-free superplasticising admixture for improving concrete workability, increasing strength and reducing water demand in site-mixed and precast concrete.", howToItems: [["🧪", "Measure the dose", "Follow the approved mix design and measure the required admixture quantity accurately."], ["🌀", "Disperse thoroughly", "Add the brown liquid to the concrete mix with the mixing water and blend evenly."], ["🏗️", "Place the concrete", "Use the improved workability for faster placing and compaction with less vibration."]], packInfo: "Available in 5 L and 20 L packs. Confirm dosage and compatibility with trial mixes before production.", careItems: [["🧤", "Protect your hands", "Wear suitable gloves during handling and batching."], ["🥽", "Protect your eyes", "Use eye protection when transferring the liquid."], ["📋", "Follow the mix design", "Use the approved dosage and concrete production procedure."], ["🧱", "Precast ready", "Suitable for precast and other high early-strength requirements."], ["🚫", "Chloride free", "Suitable for reinforced and prestressed concrete applications."]],
    bulkDiscounts: [{minQty:1,unitPrice:799},{minQty:2,unitPrice:773,savePct:3.3,saveAmt:52},{minQty:5,unitPrice:746,savePct:6.7,saveAmt:265},{minQty:10,unitPrice:712,savePct:10.9,saveAmt:870}] },

  { id: "FOS-LK-015", brand: "fosroc", name: "Lokfix P", category: "Concrete Repair",
    price: 899, originalPrice: 999, rating: 4.8, image: "assets/lokfix-p-enhanced.png", inStock: true, tags: ["anchor-grout","rapid-strength","non-shrink","chemical-anchor"], sizes: ["500 ml"],
    use: "A rapid-strength polyester resin anchor grout for horizontal, vertical and overhead anchoring applications, including anchoring into concrete and masonry.",
    howToItems: [["🧹", "Prepare the hole", "Drill to the specified diameter and depth, then remove dust, debris and standing water."],["🌀", "Mix and place", "Mix the resin and filler components until uniform, then place the grout from the back of the hole."],["⚓", "Position and cure", "Insert the clean anchor with a twisting motion, align it and allow the grout to cure before loading."]],
    packInfo: "Supplied as a 500 ml Lokfix P anchor-grout pack with resin and filler components. Follow the product data sheet for hole size, mixing, working time and cure time.",
    careItems: [["🧤", "Protect your hands", "Wear suitable chemical-resistant gloves during mixing and placing."],["🥽", "Protect your eyes", "Use eye protection when handling resin and filler."],["🧹", "Clean the hole", "Dust and debris can reduce the anchor bond; brush and blow the hole thoroughly."],["⏱️", "Respect cure time", "Do not load or disturb the anchor until the specified cure time has passed."],["📋", "Follow the TDS", "Use the approved anchor size, hole dimensions and installation procedure."]],
    bulkDiscounts: [{minQty:1,unitPrice:899},{minQty:3,unitPrice:872,savePct:3,saveAmt:81},{minQty:6,unitPrice:844,savePct:6.1,saveAmt:330},{minQty:12,unitPrice:1780,savePct:7.7,saveAmt:648}] },

  { id: "FOS-NL-016", brand: "fosroc", name: "Nitobond SBR Latex", category: "Concrete Repair",
    price: 899, originalPrice: 999, rating: 4.8, image: "assets/nitobond-sbr-studio.png", inStock: true, tags: ["polymer-latex","concrete-repair","bonding-agent","screed"], sizes: ["5 L"],
    use: "A single-component polymer latex for improved screed and plaster, helping improve bond, tensile and flexural strength, durability and crack resistance.",
    howToItems: [["🧹", "Prepare the surface", "Clean sound concrete or masonry and remove dust, oil, loose material and laitance."],["🧪", "Mix the latex system", "Measure Nitobond SBR Latex as specified and blend it into the bonding slurry or repair mix."],["🧱", "Apply and cure", "Apply the polymer-modified screed, plaster or repair mortar, finish it properly and protect it while curing."]],
    packInfo: "Supplied in a 5 L pack. Follow the product data sheet for mix proportions, coverage, application thickness and curing requirements.",
    careItems: [["🧤", "Protect your hands", "Wear suitable gloves during mixing and application."],["🥽", "Protect your eyes", "Use eye protection when handling the liquid and wet mix."],["🧹", "Use a sound base", "Remove weak or contaminated material before applying the repair system."],["⏱️", "Cure properly", "Protect the applied mortar or plaster and follow the specified curing procedure."],["📋", "Follow the mix design", "Use the approved latex proportion for the intended repair or bonding application."]],
    bulkDiscounts: [{minQty:1,unitPrice:899},{minQty:2,unitPrice:872,savePct:3,saveAmt:54},{minQty:4,unitPrice:836,savePct:7,saveAmt:252},{minQty:8,unitPrice:792,savePct:12,saveAmt:856}] },

  // Diversey
  { id: "DIV-LM-101", brand: "diversey", subBrand: "TASKI", name: "Neutral Floor Cleaner", category: "Industrial",
    price: 279, originalPrice: 349, rating: 4.8, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","multi-surface","concentrate"], sizes: ["1 L","5 L"], packaging: "5 L",
    bulkDiscounts: [{minQty:1,unitPrice:279},{minQty:2,unitPrice:270,savePct:3.2,saveAmt:18},{minQty:5,unitPrice:260,savePct:6.8,saveAmt:95},{minQty:10,unitPrice:248,savePct:11.1,saveAmt:310}] },
  { id: "DIV-TC-102", brand: "diversey", subBrand: "Room Care", name: "Toilet Bowl Cleaner", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:149},{minQty:2,unitPrice:145,savePct:2.7,saveAmt:8},{minQty:5,unitPrice:139,savePct:6.7,saveAmt:50},{minQty:10,unitPrice:131,savePct:12,saveAmt:180}] },
  { id: "DIV-CG-103", brand: "diversey", subBrand: "SUMA / Suma Brite", name: "Cream Gel Degreaser", category: "Kitchen Care",
    price: 199, originalPrice: 249, rating: 4.7, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","degreaser","kitchen"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:199},{minQty:2,unitPrice:193,savePct:3,saveAmt:12},{minQty:5,unitPrice:185,savePct:7,saveAmt:70},{minQty:10,unitPrice:176,savePct:11.6,saveAmt:230}] },
  { id: "DIV-LN-104", brand: "diversey", subBrand: "CLAX", name: "Laundry Powder", category: "Laundry Care",
    price: 299, originalPrice: 369, rating: 4.6, image: "assets/brands/diversey.png",
    inStock: true, tags: ["powder","laundry","fragrance"], sizes: ["1 kg","5 kg"], packaging: "5 kg",
    bulkDiscounts: [{minQty:1,unitPrice:299},{minQty:2,unitPrice:289,savePct:3.3,saveAmt:20},{minQty:5,unitPrice:277,savePct:7.4,saveAmt:110},{minQty:10,unitPrice:264,savePct:11.7,saveAmt:350}] },
  { id: "DIV-HS-105", brand: "diversey", subBrand: "Soft Care", name: "Hand Wash Liquid", category: "Bathroom Care",
    price: 99, originalPrice: 129, rating: 4.7, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","anti-microbial","fragrance"], sizes: ["250 ml","500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:99},{minQty:2,unitPrice:96,savePct:3,saveAmt:6},{minQty:5,unitPrice:91,savePct:8.1,saveAmt:40},{minQty:10,unitPrice:86,savePct:13.1,saveAmt:130}] },
  { id: "DIV-CL-106", brand: "diversey", subBrand: "Oxivir / Virex", name: "Cleanroom Disinfectant", category: "Industrial",
    price: 499, originalPrice: 649, rating: 4.9, image: "assets/brands/diversey.png",
    tags: ["spray","disinfectant","anti-microbial"], sizes: ["1 L","5 L"], packaging: "5 L",
    bulkDiscounts: [{minQty:1,unitPrice:499},{minQty:2,unitPrice:484,savePct:3,saveAmt:30},{minQty:5,unitPrice:467,savePct:6.4,saveAmt:160},{minQty:10,unitPrice:445,savePct:10.8,saveAmt:540}] },

  // Godrej
  { id: "GOD-FC-201", brand: "godrej", name: "Nature's Carpet Shampoo", category: "Floor Care",
    price: 169, originalPrice: 219, rating: 4.6, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","carpet","fragrance"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:169},{minQty:2,unitPrice:164,savePct:3,saveAmt:10},{minQty:5,unitPrice:157,savePct:7.1,saveAmt:60},{minQty:10,unitPrice:150,savePct:11.2,saveAmt:190}] },
  { id: "GOD-TC-202", brand: "godrej", name: "Toilet Cleaner", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:149},{minQty:2,unitPrice:145,savePct:2.7,saveAmt:8},{minQty:5,unitPrice:138,savePct:7.4,saveAmt:55},{minQty:10,unitPrice:131,savePct:12.1,saveAmt:180}] },
  { id: "GOD-DW-203", brand: "godrej", name: "Dish Wash Liquid", category: "Kitchen Care",
    price: 129, originalPrice: 169, rating: 4.6, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","kitchen","fragrance"], sizes: ["250 ml","500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:129},{minQty:2,unitPrice:125,savePct:3.1,saveAmt:8},{minQty:5,unitPrice:120,savePct:7,saveAmt:45},{minQty:10,unitPrice:113,savePct:12.4,saveAmt:160}] },
  { id: "GOD-LC-204", brand: "godrej", name: "Laundry Liquid", category: "Laundry Care",
    price: 349, originalPrice: 449, rating: 4.7, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"], packaging: "5 L",
    bulkDiscounts: [{minQty:1,unitPrice:349},{minQty:2,unitPrice:338,savePct:3.1,saveAmt:22},{minQty:5,unitPrice:323,savePct:7.4,saveAmt:130},{minQty:10,unitPrice:308,savePct:11.7,saveAmt:410}] },
  { id: "GOD-SC-205", brand: "godrej", name: "Surface Cleaner Spray", category: "Surface Care",
    price: 189, originalPrice: 239, rating: 4.7, image: "assets/brands/godrej.png",
    inStock: true, tags: ["spray","multi-surface","disinfectant"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:189},{minQty:2,unitPrice:184,savePct:2.6,saveAmt:10},{minQty:5,unitPrice:176,savePct:6.9,saveAmt:65},{minQty:10,unitPrice:168,savePct:11.1,saveAmt:210}] },

  // Aditya Birla
  { id: "ABG-LM-301", brand: "aditya-birla", name: "Industrial Floor Wash", category: "Floor Care",
    price: 599, originalPrice: 799, rating: 4.7, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["liquid","concentrate","bulk"], sizes: ["5 L","20 L"], packaging: "20 L",
    bulkDiscounts: [{minQty:1,unitPrice:599},{minQty:2,unitPrice:575,savePct:4,saveAmt:48},{minQty:4,unitPrice:549,savePct:8.3,saveAmt:200},{minQty:8,unitPrice:520,savePct:13.2,saveAmt:632}] },
  { id: "ABG-KC-302", brand: "aditya-birla", name: "Kitchen Surface Cleaner", category: "Kitchen Care",
    price: 219, originalPrice: 279, rating: 4.6, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","kitchen","degreaser"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:219},{minQty:2,unitPrice:212,savePct:3.2,saveAmt:14},{minQty:5,unitPrice:202,savePct:7.8,saveAmt:85},{minQty:10,unitPrice:192,savePct:12.3,saveAmt:270}] },
  { id: "ABG-LM-303", brand: "aditya-birla", name: "Delicate Wash Liquid", category: "Laundry Care",
    price: 249, originalPrice: 329, rating: 4.7, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"], packaging: "5 L",
    bulkDiscounts: [{minQty:1,unitPrice:249},{minQty:2,unitPrice:241,savePct:3.2,saveAmt:16},{minQty:5,unitPrice:229,savePct:8,saveAmt:100},{minQty:10,unitPrice:217,savePct:12.9,saveAmt:320}] },
  { id: "ABG-SC-304", brand: "aditya-birla", name: "All Purpose Cleaner", category: "Surface Care",
    price: 129, originalPrice: 169, rating: 4.5, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","multi-surface","fragrance"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:129},{minQty:2,unitPrice:125,savePct:3.1,saveAmt:8},{minQty:5,unitPrice:120,savePct:7,saveAmt:45},{minQty:10,unitPrice:113,savePct:12.4,saveAmt:160}] },
  { id: "ABG-BC-305", brand: "aditya-birla", name: "Bathroom Freshener Spray", category: "Bathroom Care",
    price: 159, originalPrice: 209, rating: 4.5, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","bathroom","fragrance"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:159},{minQty:2,unitPrice:154,savePct:3.1,saveAmt:10},{minQty:5,unitPrice:147,savePct:7.5,saveAmt:60},{minQty:10,unitPrice:140,savePct:11.9,saveAmt:190}] },

  // Shine All
  { id: "SHA-FM-401", brand: "shine-all", name: "Quick Shine Floor Cleaner", category: "Floor Care",
    price: 199, originalPrice: 249, rating: 4.8, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","multi-surface","fragrance"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:199},{minQty:2,unitPrice:193,savePct:3,saveAmt:12},{minQty:5,unitPrice:184,savePct:7.5,saveAmt:75},{minQty:10,unitPrice:175,savePct:12,saveAmt:240}] },
  { id: "SHA-TC-402", brand: "shine-all", name: "Toilet Power Gel", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:149},{minQty:2,unitPrice:145,savePct:2.7,saveAmt:8},{minQty:5,unitPrice:136,savePct:8.7,saveAmt:65},{minQty:10,unitPrice:129,savePct:13.4,saveAmt:200}] },
  { id: "SHA-KC-403", brand: "shine-all", name: "Kitchen Grease Buster", category: "Kitchen Care",
    price: 179, originalPrice: 229, rating: 4.6, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","kitchen","degreaser"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:179},{minQty:2,unitPrice:174,savePct:2.8,saveAmt:10},{minQty:5,unitPrice:165,savePct:7.8,saveAmt:70},{minQty:10,unitPrice:157,savePct:12.3,saveAmt:220}] },
  { id: "SHA-LN-404", brand: "shine-all", name: "Laundry Liquid", category: "Laundry Care",
    price: 349, originalPrice: 449, rating: 4.7, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"], packaging: "5 L",
    bulkDiscounts: [{minQty:1,unitPrice:349},{minQty:2,unitPrice:338,savePct:3.1,saveAmt:22},{minQty:5,unitPrice:324,savePct:7.2,saveAmt:125},{minQty:10,unitPrice:310,savePct:11.2,saveAmt:390}] },
  { id: "SHA-SC-405", brand: "shine-all", name: "All Surface Spray", category: "Surface Care",
    price: 149, originalPrice: 199, rating: 4.7, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","multi-surface","anti-microbial"], sizes: ["500 ml"], packaging: "500 ml",
    bulkDiscounts: [{minQty:1,unitPrice:149},{minQty:2,unitPrice:145,savePct:2.7,saveAmt:8},{minQty:5,unitPrice:139,savePct:6.7,saveAmt:50},{minQty:10,unitPrice:131,savePct:12,saveAmt:180}] },
  { id: "SHA-SC-406", brand: "shine-all", name: "Germ Shield Disinfectant", category: "Surface Care",
    price: 229, originalPrice: 299, rating: 4.9, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","disinfectant","anti-microbial"], sizes: ["500 ml","1 L"], packaging: "1 L",
    bulkDiscounts: [{minQty:1,unitPrice:229},{minQty:2,unitPrice:222,savePct:3.1,saveAmt:14},{minQty:5,unitPrice:211,savePct:7.9,saveAmt:90},{minQty:10,unitPrice:201,savePct:12.2,saveAmt:280}] }
];

// Replace unverified Diversey demo entries only when the researched catalogue loads.
var taskiProducts = typeof TASKI_PRODUCTS !== 'undefined' ? TASKI_PRODUCTS :
  (typeof module !== 'undefined' && module.exports ? require('./taski-catalog.js') : []);
if (taskiProducts.length) {
  for (var catalogIndex=PRODUCTS.length-1; catalogIndex>=0; catalogIndex--) {
    if (PRODUCTS[catalogIndex].brand === 'diversey') PRODUCTS.splice(catalogIndex,1);
  }
  PRODUCTS.push.apply(PRODUCTS,taskiProducts);
}
var roofguardProduct = PRODUCTS.find(function(x){ return x.id === "FOS-RG-012"; });
if (roofguardProduct) roofguardProduct.image = "assets/brushbond-roofguard-studio.png";
var aquaProtectProduct = PRODUCTS.find(function(x){ return x.id === "FOS-AQ-019"; });
if (aquaProtectProduct) aquaProtectProduct.image = "assets/brushbond-aquaprotect-studio.png";
var conplastProduct = PRODUCTS.find(function(x){ return x.id === "FOS-CP-014"; });
if (conplastProduct) { conplastProduct.image = "assets/conplast-sp430-studio.png"; conplastProduct.sizeImages["5 L"] = "assets/conplast-sp430-5l-enhanced.png"; }

if (typeof module !== "undefined" && module.exports) {
  module.exports = { BRANDS, CATEGORIES, PRODUCT_TAGS, PRODUCTS };
}
(function () {
  "use strict";
  if (typeof document === 'undefined' || !document.getElementById("brand-select")) return;

  // Must exist before rendering restored cart/saved data.
  var PRODUCT_BY_ID = {};
  PRODUCTS.forEach(function(p){ PRODUCT_BY_ID[p.id]=p; });

  var brandsEl = document.getElementById("brand-select");
  var categoryEl = document.getElementById("category-select");
  var sortEl = document.getElementById("sort-select");
  var searchEl = document.getElementById("search");
  var gridEl = document.getElementById("product-grid");
  var breadcrumbEl = document.getElementById("breadcrumb");
  var brandMarkEl = document.getElementById("brand-mark");
  var countEl = document.getElementById("result-count");
  var emptyEl = document.getElementById("empty-state");
  var clearEl = document.getElementById("clear-filters");
  var savedToggle = document.getElementById("saved-toggle");
  var savedCountEl = document.getElementById("saved-count");
  var bagEl = document.getElementById("bag");
  var bagCountEl = document.getElementById("bag-count");
  var cartSummaryEl = document.getElementById("cart-summary");
  var cartItemsEl = document.getElementById("cart-items");
  var cartTotalEl = document.getElementById("cart-total");

  var url = new URL(location.href, location.origin);
  var CART_KEY_SEPARATOR = "@@";
  function cartKey(productId, size) { return productId + CART_KEY_SEPARATOR + encodeURIComponent(size || "Standard pack"); }
  function cartPart(key) { var parts=String(key).split(CART_KEY_SEPARATOR); return { id:parts[0], size:parts.length>1?decodeURIComponent(parts.slice(1).join(CART_KEY_SEPARATOR)):"" }; }
  function packageOptions(product) {
    if (product.packages && product.packages.length) return product.packages.filter(function(item){ return item.enabled !== false; });
    return (product.sizes || [product.packaging || "Standard pack"]).map(function(label,index){ return { label:label, price:Number(product.price||0), originalPrice:Number(product.originalPrice||product.price||0), isDefault:index===0 }; });
  }
  function selectedPackage(product,size) { var options=packageOptions(product);return options.find(function(item){return item.label===size;})||options.find(function(item){return item.isDefault;})||options[0]||{price:Number(product.price||0),originalPrice:Number(product.originalPrice||product.price||0)}; }
  function rebuildProductIndex(){PRODUCT_BY_ID={};PRODUCTS.forEach(function(p){PRODUCT_BY_ID[p.id]=p;});}

  // Read brand from hash fragment (#fosroc) so it survives the server intact
  function hashBrand() {
    var h = url.hash.replace(/^#/, "").toLowerCase();
    return (h && BRANDS[h]) ? h : "";
  }

  // ---------- Cart ----------
  var cart = {}; // id -> qty
  try { cart = JSON.parse(sessionStorage.getItem("clearly_cart") || "{}"); } catch (e) { cart = {}; }
  if (!cart || Array.isArray(cart) || typeof cart !== 'object') cart={};
  Object.keys(cart).forEach(function(key){ var part=cartPart(key);if (!PRODUCT_BY_ID[part.id] || !Number.isInteger(cart[key]) || cart[key]<1) delete cart[key]; });

  function saveCart() { try { sessionStorage.setItem("clearly_cart", JSON.stringify(cart)); } catch (e) {} }

  function cartTotal() { var total = 0; for (var key in cart) { var part=cartPart(key),p = PRODUCT_BY_ID[part.id]; if (p) total += selectedPackage(p,part.size).price * cart[key]; } return total; }
  function cartCount() { var c = 0; for (var id in cart) c += cart[id]; return c; }

  function renderCart() {
    var items = [];
    for (var key in cart) { items.push({ key:key, part:cartPart(key), qty: cart[key] }); }
    if (bagCountEl) bagCountEl.textContent = cartCount();
    if (bagEl) bagEl.classList.toggle("has-items", cartCount() > 0);
    if (!cartSummaryEl || !cartItemsEl || !cartTotalEl) return;
    if (items.length === 0) { cartSummaryEl.hidden = true; return; }
    cartSummaryEl.hidden = false;
    var html = "";
    for (var i = 0; i < items.length; i++) { var it = items[i]; var p = PRODUCT_BY_ID[it.part.id];
      var pkg=selectedPackage(p,it.part.size);html += "<div class='cart-item'><span>" + escapeHtml(p.name) + " · " + escapeHtml(it.part.size || p.packaging || "Standard pack") + "</span><b>₹" + Number(pkg.price).toLocaleString('en-IN') + "</b><span class='qty'>×" + it.qty + "</span></div>";
    }
    cartItemsEl.innerHTML = html;
    cartTotalEl.textContent = "₹" + cartTotal();
  }

  function addToCart(id, size) {
    if (!PRODUCT_BY_ID[id] || !size) return;
    var key=cartKey(id,size);
    if (!cart[key]) cart[key] = 0;
    cart[key] += 1;
    saveCart();
    if(window.ClearlyShop)window.ClearlyShop.saveCart(cart).catch(function(){});
    renderCart();
    flashCart();
  }

  function flashCart() {
    var node = bagEl;
    var orig = node.style.transform;
    node.style.transform = "scale(.86)";
    setTimeout(function () { node.style.transform = ""; }, 120);
  }

  // ---------- Wishlist ----------
  var saved = [];
  try { saved = JSON.parse(localStorage.getItem("clearly_saved") || "[]"); } catch (e) { saved = []; }
  saved=Array.isArray(saved) ? saved.filter(function(id,i,a){return PRODUCT_BY_ID[id] && a.indexOf(id)===i;}) : [];

  function saveSaved() { try { localStorage.setItem("clearly_saved", JSON.stringify(saved)); } catch (e) {} }

  function isSaved(id) { return saved.indexOf(id) !== -1; }
  function toggleSaved(id) {
    if (window.ClearlyShop && !window.ClearlyShop.requireSignIn('wishlist', 'products.html')) return;
    var idx = saved.indexOf(id);
    if (idx === -1) saved.push(id); else saved.splice(idx, 1);
    saveSaved();
    if(window.ClearlyShop)window.ClearlyShop.saveWishlist(saved).catch(function(){});
    updateSavedCount();
    renderGrid();
  }

  function updateSavedCount() {
    if (!savedCountEl) return;
    savedCountEl.textContent = saved.length;
    savedToggle.classList.toggle("has-items", saved.length > 0);
    savedToggle.innerHTML = (saved.length > 0 ? "♥" : "♡") + " <span id='saved-count'>" + saved.length + "</span>";
    var n = document.getElementById("saved-count");
    if (n) n.textContent = saved.length;
  }

  // ---------- URL state ----------
  function readParams() {
    return {
      brand: url.searchParams.getAll("brand"),
      category: url.searchParams.getAll("category"),
      tags: url.searchParams.getAll("tag"),
      sort: url.searchParams.get("sort") || "relevance",
      search: url.searchParams.get("search") || ""
    };
  }

  function writeParams(p) {
    url.searchParams.delete("brand");
    url.searchParams.delete("category");
    url.searchParams.delete("sort");
    url.searchParams.delete("search");
    if (p.brand && p.brand.length) p.brand.forEach(function (b) { url.searchParams.append("brand", b); });
    if (p.category) p.category.forEach(function(c){url.searchParams.append("category",c);});
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
    if (hb) p.brand = [hb];
    return p;
  }

  function filterAndSort() {
    var p = currentFilters();
    var list = PRODUCTS.slice();
    if (p.brand && p.brand.length) list = list.filter(function (x) { return p.brand.indexOf(x.brand) !== -1; });
    if (p.category && p.category.length) list = list.filter(function (x) { return p.category.indexOf(x.category) !== -1; });
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
      case "rating": list.sort(function (a, b) { return (b.rating||0) - (a.rating||0) || a.price - b.price; }); break;
      default: list.sort(function (a, b) { return a.name.localeCompare(b.name); });
    }
    return list;
  }

  function renderOptions() {
    var brands = [];
    for (var k in BRANDS) brands.push(k);
    brands.sort();

    var hashSelectedBrand = hashBrand();
    var selectedBrands = hashSelectedBrand ? [hashSelectedBrand] : url.searchParams.getAll("brand");
    var curCats = url.searchParams.getAll("category");
    var curSort = url.searchParams.get("sort");

    // brand options
    var bh = "<button class='brand-option brand-all" + (selectedBrands.length === 0 ? " selected" : "") + "' type='button' data-brand-all='true'><span class='brand-check' aria-hidden='true'>✓</span><span>All brands</span></button>";
    for (var i = 0; i < brands.length; i++) { var b = brands[i];
      bh += "<label class='brand-option'><input type='checkbox' value='" + escapeAttr(b) + "'" + (selectedBrands.indexOf(b) !== -1 ? " checked" : "") + "><span class='brand-check' aria-hidden='true'>✓</span><span>" + escapeHtml(BRANDS[b]) + "</span></label>";
    }
    if (brandsEl.innerHTML !== bh) brandsEl.innerHTML = bh;
    var brandSummary = document.querySelector("#brand-filter summary");
    if (brandSummary) brandSummary.textContent = selectedBrands.length ? (selectedBrands.length + " brand" + (selectedBrands.length === 1 ? "" : "s")) : "Brands";

    // Show only categories represented by the currently selected brands.
    var categorySource = selectedBrands.length ? PRODUCTS.filter(function (x) { return selectedBrands.indexOf(x.brand) !== -1; }) : PRODUCTS;
    var availableCategories = [];
    categorySource.forEach(function (x) { if (availableCategories.indexOf(x.category) === -1) availableCategories.push(x.category); });
    availableCategories.sort();
    var ch = "";
    for (var j = 0; j < availableCategories.length; j++) { var c = availableCategories[j];
      ch += "<label><input type='checkbox' value='" + escapeAttr(c) + "'" + (curCats.indexOf(c) !== -1 ? " checked" : "") + "> <span>" + escapeHtml(c) + "</span></label>";
    }
    if (categoryEl.innerHTML !== ch) {
      var focusedValue=categoryEl.contains(document.activeElement) ? document.activeElement.value : null;
      categoryEl.innerHTML = ch;
      if(focusedValue) Array.from(categoryEl.querySelectorAll('input')).find(function(x){return x.value===focusedValue;})?.focus();
    }
    var allCategoryButton = document.getElementById("category-all");
    if (allCategoryButton) {
      allCategoryButton.classList.toggle("active", curCats.length === 0);
      allCategoryButton.setAttribute("aria-pressed", curCats.length === 0 ? "true" : "false");
    }

    sortEl.value = curSort || "relevance";
    searchEl.value = url.searchParams.get('search') || '';
    searchEl.disabled = false;

    if (selectedBrands.length === 1) {
      var selectedBrand = selectedBrands[0];
      var bl = BRANDS[selectedBrand] || selectedBrand;
      var logoPath = selectedBrand === "shine-all" ? "assets/shine-all-products/ShineAll Home Care Logo.png" : "assets/brands/" + selectedBrand + ".png";
      brandMarkEl.innerHTML = "<img src='" + logoPath + "' alt='" + escapeAttr(bl) + "' />";
      brandMarkEl.href = "products.html#" + selectedBrand;
      breadcrumbEl.innerHTML = "<span class='crumb'><a href='products.html'>All products</a></span><span class='sep'>/</span><span class='crumb'>" + escapeHtml(bl) + "</span>";
    } else if (selectedBrands.length > 1) {
      brandMarkEl.innerHTML = "";
      brandMarkEl.removeAttribute("href");
      breadcrumbEl.innerHTML = "<span class='crumb'><a href='products.html'>All products</a></span><span class='sep'>/</span><span class='crumb'>" + selectedBrands.length + " brands</span>";
    } else {
      brandMarkEl.innerHTML = "";
      brandMarkEl.removeAttribute("href");
      breadcrumbEl.innerHTML = "<span class='crumb'><a href='products.html'>All products</a></span>";
    }

    // result count
    var list = filterAndSort();
    countEl.textContent = list.length === 0 ? "No products" :
      (list.length + " product" + (list.length === 1 ? "" : "s"));
  }

  var gridTransitionTimer = null;
  function renderGrid() {
    var list = filterAndSort();
    if (gridTransitionTimer) clearTimeout(gridTransitionTimer);
    gridEl.classList.add("is-filtering");
    gridTransitionTimer = setTimeout(function () {
      if (list.length === 0) {
        gridEl.innerHTML = "";
        emptyEl.hidden = false;
      } else {
        emptyEl.hidden = true;
        var html = "";
        for (var i = 0; i < list.length; i++) html += productCardHtml(list[i], false);
        gridEl.innerHTML = html;
      }
      requestAnimationFrame(function () { gridEl.classList.remove("is-filtering"); });
    }, 170);
  }

  function productCardHtml(p, small) {
    var btnClass = "cart-btn";
    var savedCls = "wishlist-btn" + (isSaved(p.id) ? " on" : "");
    var ico = isSaved(p.id) ? "♥" : "♡";
    var sizesHtml = "";
    var packages=packageOptions(p),displayPackage=packages.find(function(item){return item.isDefault;})||packages[0]||p;
    if (packages.length) {
      sizesHtml = "<div class='sizes' role='group' aria-label='Choose package size'>" + packages.map(function (pkg) { return "<button type='button' class='size-option' data-size='" + escapeAttr(pkg.label) + "' data-price='"+Number(pkg.price||0)+"' data-mrp='"+Number(pkg.originalPrice||pkg.price||0)+"' data-package-code='"+escapeAttr(pkg.packageCode||"")+"' aria-pressed='false'>" + escapeHtml(pkg.label) + "</button>"; }).join("") + "</div><small class='size-prompt' aria-live='polite'>Select a package size</small>";
    }
    return "<article class='product-card' data-id='" + p.id + "'>" +
      (p.image
        ? "<div class='product-img'><img src='" + escapeAttr(p.image) + "' alt='" + escapeAttr(p.name) + "' /></div>"
        : "<div class='product-placeholder'></div>") +
      "<div class='product-category'>" + escapeHtml(p.category) + "</div>" +
      "<h3 class='product-name'><a href='product.html?id=" + encodeURIComponent(p.id) + "'>" + escapeHtml(p.name) + "</a></h3>" +
      (p.purpose ? "<p class='product-purpose'>" + escapeHtml(p.purpose) + "</p>" : "") +
      (p.rating ? "<span class='product-stars'>" + starsHtml(p.rating) + "</span>" : "") +
      sizesHtml +
      "<div class='product-price'>" +
        (displayPackage.originalPrice && displayPackage.originalPrice > displayPackage.price
          ? "<span class='old'>₹" + Number(displayPackage.originalPrice).toLocaleString('en-IN') + "</span>"
          : "") +
        "<span class='now'>₹" + Number(displayPackage.price).toLocaleString('en-IN') + "</span>" +
      "</div>" +

      "<div class='product-actions'>" +
        "<button class='" + btnClass + "' data-cart='" + p.id + "'>Add to bag</button>" +
        "<button class='" + savedCls + "' data-wish='" + p.id + "' aria-label='Save " + escapeAttr(p.name) + " for later' title='Save for later'><span class='ico'>" + ico + "</span></button>" +
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
  brandsEl.addEventListener("change", function (e) {
    if (!e.target.matches("input[type='checkbox']")) return;
    url.hash = "";
    url.searchParams.delete("brand");
    brandsEl.querySelectorAll("input[type='checkbox']:checked").forEach(function (input) { url.searchParams.append("brand", input.value); });
    url.searchParams.delete("category");
    syncSortStock();
    apply();
  });
  brandsEl.addEventListener("click", function (e) {
    var allBrands = e.target.closest("[data-brand-all]");
    if (!allBrands) return;
    url.hash = "";
    url.searchParams.delete("brand");
    url.searchParams.delete("category");
    syncSortStock();
    apply();
  });
  categoryEl.addEventListener("change", function (e) {
    if (!e.target.matches("input[type='checkbox']")) return;
    url.searchParams.delete("category");
    categoryEl.querySelectorAll("input[type='checkbox']:checked").forEach(function (input) { url.searchParams.append("category", input.value); });
    syncSortStock();
    apply();
  });
  var categoryAllButton = document.getElementById("category-all");
  if (categoryAllButton) categoryAllButton.addEventListener("click", function () {
    url.searchParams.delete("category");
    syncSortStock();
    apply();
  });
  document.addEventListener("click", function (e) {
    var categoryFilter = document.getElementById("category-filter");
    if (categoryFilter && categoryFilter.open && !categoryFilter.contains(e.target)) categoryFilter.open = false;
    var brandFilter = document.getElementById("brand-filter");
    if (brandFilter && brandFilter.open && !brandFilter.contains(e.target)) brandFilter.open = false;
  });
  document.addEventListener("pointerdown", function (e) {
    var brandFilter = document.getElementById("brand-filter");
    if (brandFilter && brandFilter.open && !brandFilter.contains(e.target)) brandFilter.open = false;
  });
  sortEl.addEventListener("change", function () {
    var v = sortEl.value;
    if (v === "relevance") url.searchParams.delete("sort"); else url.searchParams.set("sort", v);
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
    url.searchParams.delete("search");
    url.searchParams.delete("sort");
    syncSortStock();
    apply();
  });

  function syncSortStock() {
    sortEl.value = (url.searchParams.get("sort") || "relevance");
  }

  // click delegation
  function handleProductClick(e) {
    var sizeButton=e.target.closest("button[data-size]");
    if(sizeButton){e.preventDefault();var sizeGroup=sizeButton.closest('.sizes');sizeGroup.querySelectorAll('[data-size]').forEach(function(x){var on=x===sizeButton;x.classList.toggle('selected',on);x.setAttribute('aria-pressed',String(on));});var cardForSize=sizeButton.closest('.product-card');cardForSize.classList.remove('needs-size');var prompt=cardForSize.querySelector('.size-prompt');if(prompt)prompt.textContent='Selected: '+sizeButton.dataset.size;var priceBox=cardForSize.querySelector('.product-price'),price=Number(sizeButton.dataset.price||0),mrp=Number(sizeButton.dataset.mrp||price);priceBox.innerHTML=(mrp>price?"<span class='old'>₹"+mrp.toLocaleString('en-IN')+"</span>":"")+"<span class='now'>₹"+price.toLocaleString('en-IN')+"</span>";return;}
    var btn = e.target.closest("button[data-cart]");
    if (btn) { e.preventDefault();var cardForCart=btn.closest('.product-card'),selected=cardForCart.querySelector('[data-size].selected');if(!selected){cardForCart.classList.add('needs-size');var prompt=cardForCart.querySelector('.size-prompt');if(prompt)prompt.textContent='Please select a package size first';setTimeout(function(){cardForCart.classList.remove('needs-size');},1400);return;}addToCart(btn.getAttribute("data-cart"),selected.dataset.size);btn.textContent='Added ✓';setTimeout(function(){btn.textContent='Add to bag';},1100);return; }
    var wish = e.target.closest("button[data-wish]");
    if (wish) { e.preventDefault(); toggleSaved(wish.getAttribute("data-wish")); return; }
    var card = e.target.closest(".product-card");
    if (card && !e.target.closest('a')) { location.href = "product.html?id=" + encodeURIComponent(card.getAttribute("data-id")); }
  }
  gridEl.addEventListener('click',handleProductClick);

  function apply() { history.replaceState({},'',url); renderOptions(); renderGrid(); }
  function restoreLocation() { url=new URL(location.href); renderOptions(); renderGrid(); }
  window.addEventListener('hashchange',restoreLocation);
  window.addEventListener('popstate',restoreLocation);
  document.addEventListener('keydown',function(e){if(e.key==='Escape'){document.getElementById('category-filter').open=false;var bf=document.getElementById('brand-filter');if(bf)bf.open=false;}});
  updateSavedCount();
  renderCart();
  // initial render: populate options first so selected values are correct
  renderOptions();
  renderGrid();
  function applyShoppingState(state){cart=state.cart||{};saved=state.wishlist||[];renderCart();updateSavedCount();renderGrid();}
  window.addEventListener('clearly-shop-loaded',function(event){applyShoppingState(event.detail);});
  if(window.ClearlyShop)window.ClearlyShop.load().then(applyShoppingState).catch(function(){});
  window.addEventListener('clearly-catalog-loaded',function(){rebuildProductIndex();Object.keys(cart).forEach(function(key){if(!PRODUCT_BY_ID[cartPart(key).id])delete cart[key];});renderOptions();renderGrid();renderCart();});

  // ---------- Helpers ----------
  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }
  function escapeAttr(s) { return escapeHtml(s); }

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
    ".cart-summary[hidden]{display:none;}";
  document.head.appendChild(style);
})();
