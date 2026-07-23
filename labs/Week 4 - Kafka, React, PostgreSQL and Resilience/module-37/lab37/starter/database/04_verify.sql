SELECT public_id, full_name, status FROM customer ORDER BY public_id;
SELECT a.account_number, c.public_id
FROM account a
JOIN customer c ON c.customer_id = a.customer_id;
