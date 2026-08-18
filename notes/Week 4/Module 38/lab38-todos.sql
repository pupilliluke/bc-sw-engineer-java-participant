-- baseline (avoid)
SELECT * FROM customer
WHERE lower(full_name) = 'amina khan';

-- optimized lookup
SELECT customer_id, full_name, status
FROM customer
WHERE customer_id = 'CUS-1001';

-- supporting index ideas
CREATE INDEX idx_customer_status ON customer (status);
CREATE INDEX idx_account_customer ON account (customer_id);

-- paging sketch
SELECT customer_id, full_name
FROM customer
ORDER BY customer_id
LIMIT 20 OFFSET 0;

-- TODO: prefer keyset pagination (WHERE customer_id > :last) for deep pages
