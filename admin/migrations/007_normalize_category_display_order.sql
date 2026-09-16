-- Keep the category display order aligned with the eight storefront categories.
USE clearly_store;

UPDATE categories
SET sort_order = CASE slug
  WHEN 'home-care-cleaning' THEN 1
  WHEN 'restaurant-food-service' THEN 2
  WHEN 'hotel-hospitality' THEN 3
  WHEN 'healthcare-institutions' THEN 4
  WHEN 'laundry-chemicals' THEN 5
  WHEN 'swimming-pool-chemicals' THEN 6
  WHEN 'specialty-chemicals' THEN 7
  WHEN 'construction-chemicals' THEN 8
  ELSE sort_order
END;
