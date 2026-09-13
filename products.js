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
];

const PRODUCTS = [
  // Fosroc
  { id: "FOS-FC-001", brand: "fosroc", name: "Multi Surface Cleaner", category: "Surface Care",
    price: 199, originalPrice: 249, rating: 4.8, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["spray","multi-surface","disinfectant"], sizes: ["500 ml","1 L","5 L"] },
  { id: "FOS-FM-002", brand: "fosroc", name: "Floor Wash Concentrate", category: "Floor Care",
    price: 249, originalPrice: 299, rating: 4.8, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["liquid","concentrate","refill"], sizes: ["1 L","5 L"] },
  { id: "FOS-BC-003", brand: "fosroc", name: "Bathroom Foam Cleaner", category: "Bathroom Care",
    price: 179, originalPrice: 229, rating: 4.7, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["spray","degreaser","bathroom"], sizes: ["500 ml","1 L"] },
  { id: "FOS-KC-004", brand: "fosroc", name: "Kitchen Degreaser", category: "Kitchen Care",
    price: 189, originalPrice: 239, rating: 4.6, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["spray","degreaser","kitchen"], sizes: ["500 ml","1 L"] },
  { id: "FOS-LC-005", brand: "fosroc", name: "Laundry Liquid", category: "Laundry Care",
    price: 349, originalPrice: 429, rating: 4.7, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"] },
  { id: "FOS-SC-006", brand: "fosroc", name: "Anti-Microbial Surface Spray", category: "Surface Care",
    price: 219, originalPrice: 279, rating: 4.9, image: "assets/brands/fosroc.png",
    inStock: true, tags: ["spray","disinfectant","anti-microbial"], sizes: ["500 ml"] },

  // Diversey
  { id: "DIV-LM-101", brand: "diversey", name: "Neutral Floor Cleaner", category: "Floor Care",
    price: 279, originalPrice: 349, rating: 4.8, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","multi-surface","concentrate"], sizes: ["1 L","5 L"] },
  { id: "DIV-TC-102", brand: "diversey", name: "Toilet Bowl Cleaner", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml"] },
  { id: "DIV-CG-103", brand: "diversey", name: "Cream Gel Degreaser", category: "Kitchen Care",
    price: 199, originalPrice: 249, rating: 4.7, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","degreaser","kitchen"], sizes: ["500 ml","1 L"] },
  { id: "DIV-LN-104", brand: "diversey", name: "Laundry Powder", category: "Laundry Care",
    price: 299, originalPrice: 369, rating: 4.6, image: "assets/brands/diversey.png",
    inStock: true, tags: ["powder","laundry","fragrance"], sizes: ["1 kg","5 kg"] },
  { id: "DIV-HS-105", brand: "diversey", name: "Hand Wash Liquid", category: "Surface Care",
    price: 99, originalPrice: 129, rating: 4.7, image: "assets/brands/diversey.png",
    inStock: true, tags: ["liquid","anti-microbial","fragrance"], sizes: ["250 ml","500 ml"] },
  { id: "DIV-CL-106", brand: "diversey", name: "Cleanroom Disinfectant", category: "Surface Care",
    price: 499, originalPrice: 649, rating: 4.9, image: "assets/brands/diversey.png",
    inStock: false, tags: ["spray","disinfectant","anti-microbial"], sizes: ["1 L","5 L"] },

  // Godrej
  { id: "GOD-FC-201", brand: "godrej", name: "Nature's Carpet Shampoo", category: "Floor Care",
    price: 169, originalPrice: 219, rating: 4.6, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","carpet","fragrance"], sizes: ["500 ml","1 L"] },
  { id: "GOD-TC-202", brand: "godrej", name: "Toilet Cleaner", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml","1 L"] },
  { id: "GOD-DW-203", brand: "godrej", name: "Dish Wash Liquid", category: "Kitchen Care",
    price: 129, originalPrice: 169, rating: 4.6, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","kitchen","fragrance"], sizes: ["250 ml","500 ml"] },
  { id: "GOD-LC-204", brand: "godrej", name: "Laundry Liquid", category: "Laundry Care",
    price: 349, originalPrice: 449, rating: 4.7, image: "assets/brands/godrej.png",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"] },
  { id: "GOD-SC-205", brand: "godrej", name: "Surface Cleaner Spray", category: "Surface Care",
    price: 189, originalPrice: 239, rating: 4.7, image: "assets/brands/godrej.png",
    inStock: true, tags: ["spray","multi-surface","disinfectant"], sizes: ["500 ml"] },

  // Aditya Birla
  { id: "ABG-LM-301", brand: "aditya-birla", name: "Industrial Floor Wash", category: "Floor Care",
    price: 599, originalPrice: 799, rating: 4.7, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["liquid","concentrate","bulk"], sizes: ["5 L","20 L"] },
  { id: "ABG-KC-302", brand: "aditya-birla", name: "Kitchen Surface Cleaner", category: "Kitchen Care",
    price: 219, originalPrice: 279, rating: 4.6, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","kitchen","degreaser"], sizes: ["500 ml","1 L"] },
  { id: "ABG-LM-303", brand: "aditya-birla", name: "Delicate Wash Liquid", category: "Laundry Care",
    price: 249, originalPrice: 329, rating: 4.7, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"] },
  { id: "ABG-SC-304", brand: "aditya-birla", name: "All Purpose Cleaner", category: "Surface Care",
    price: 129, originalPrice: 169, rating: 4.5, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","multi-surface","fragrance"], sizes: ["500 ml"] },
  { id: "ABG-BC-305", brand: "aditya-birla", name: "Bathroom Freshener Spray", category: "Bathroom Care",
    price: 159, originalPrice: 209, rating: 4.5, image: "assets/brands/aditya-birla.png",
    inStock: true, tags: ["spray","bathroom","fragrance"], sizes: ["500 ml"] },

  // Shine All
  { id: "SHA-FM-401", brand: "shine-all", name: "Quick Shine Floor Cleaner", category: "Floor Care",
    price: 199, originalPrice: 249, rating: 4.8, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","multi-surface","fragrance"], sizes: ["500 ml","1 L"] },
  { id: "SHA-TC-402", brand: "shine-all", name: "Toilet Power Gel", category: "Bathroom Care",
    price: 149, originalPrice: 199, rating: 4.8, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","bathroom","disinfectant"], sizes: ["500 ml","1 L"] },
  { id: "SHA-KC-403", brand: "shine-all", name: "Kitchen Grease Buster", category: "Kitchen Care",
    price: 179, originalPrice: 229, rating: 4.6, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","kitchen","degreaser"], sizes: ["500 ml"] },
  { id: "SHA-LN-404", brand: "shine-all", name: "Laundry Liquid", category: "Laundry Care",
    price: 349, originalPrice: 449, rating: 4.7, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["liquid","laundry","fragrance"], sizes: ["1 L","5 L"] },
  { id: "SHA-SC-405", brand: "shine-all", name: "All Surface Spray", category: "Surface Care",
    price: 149, originalPrice: 199, rating: 4.7, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","multi-surface","anti-microbial"], sizes: ["500 ml"] },
  { id: "SHA-SC-406", brand: "shine-all", name: "Germ Shield Disinfectant", category: "Surface Care",
    price: 229, originalPrice: 299, rating: 4.9, image: "assets/brands/shine-all.svg",
    inStock: true, tags: ["spray","disinfectant","anti-microbial"], sizes: ["500 ml","1 L"] },
];

if (typeof module !== "undefined" && module.exports) {
  module.exports = { BRANDS, CATEGORIES, PRODUCT_TAGS, PRODUCTS };
}