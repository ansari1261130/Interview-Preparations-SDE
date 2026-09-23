# 06b · SQL 100 Practice Questions, Part 1

Part 1 contains P1-P50 of exactly 100 SQL coding problems. Syntax is PostgreSQL by default; MySQL differences are called out in notes.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Sample Schemas](#sample-schemas)
- [Basics and Aggregation](#basics-and-aggregation)
- [Joins](#joins)
- [Subqueries and Set Logic](#subqueries-and-set-logic)
- [Window Functions](#window-functions)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Sample Schemas

```sql
CREATE TABLE departments (department_id bigint PRIMARY KEY, name text NOT NULL);
CREATE TABLE employees (
  employee_id bigint PRIMARY KEY, name text NOT NULL, email text UNIQUE,
  department_id bigint REFERENCES departments(department_id), manager_id bigint REFERENCES employees(employee_id),
  salary numeric(12,2) NOT NULL, hire_date date NOT NULL, active boolean NOT NULL DEFAULT true
);
CREATE TABLE customers (customer_id bigint PRIMARY KEY, name text, email text UNIQUE, city text, created_at date NOT NULL);
CREATE TABLE products (product_id bigint PRIMARY KEY, name text, category text, price numeric(12,2));
CREATE TABLE orders (order_id bigint PRIMARY KEY, customer_id bigint REFERENCES customers(customer_id), order_date date, status text, total_amount numeric(12,2));
CREATE TABLE order_items (order_id bigint REFERENCES orders(order_id), product_id bigint REFERENCES products(product_id), quantity int, unit_price numeric(12,2), PRIMARY KEY(order_id, product_id));
CREATE TABLE users (user_id bigint PRIMARY KEY, email text UNIQUE, signup_date date, country text);
CREATE TABLE logins (user_id bigint REFERENCES users(user_id), login_date date, PRIMARY KEY(user_id, login_date));
CREATE TABLE events (event_id bigint PRIMARY KEY, user_id bigint, event_time timestamp, event_type text, session_id text);
CREATE TABLE accounts (account_id bigint PRIMARY KEY, user_id bigint, opened_at date);
CREATE TABLE transactions (txn_id bigint PRIMARY KEY, account_id bigint REFERENCES accounts(account_id), txn_date date, amount numeric(12,2), txn_type text);
CREATE TABLE weather (id bigint PRIMARY KEY, record_date date UNIQUE, temperature int);
CREATE TABLE stadium (id bigint PRIMARY KEY, visit_date date, people int);
CREATE TABLE numbers (id bigint PRIMARY KEY, num int);
CREATE TABLE employee_tree (id bigint PRIMARY KEY, name text, manager_id bigint REFERENCES employee_tree(id));
```

## Basics and Aggregation

### P1. List active employees
`🟢 Beginner` `🔥`

**Statement:** Return `employee_id`, `name`, and `salary` for active employees ordered by name.

```sql
SELECT employee_id, name, salary
FROM employees
WHERE active = true
ORDER BY name;
```

**Alternative:** MySQL uses `active = 1` if stored as `TINYINT`.

**Notes:** Simple filter plus order; index `(active, name)` helps if active is selective enough.

### P2. Count employees by department
`🟢 Beginner` `🔥`

**Statement:** Return each department and number of employees.

```sql
SELECT d.department_id, d.name, COUNT(e.employee_id) AS employee_count
FROM departments d
LEFT JOIN employees e ON e.department_id = d.department_id
GROUP BY d.department_id, d.name
ORDER BY d.name;
```

**Alternative:** Use inner join if departments with zero employees are not needed.

**Notes:** Index `employees(department_id)` supports the join.

### P3. Departments with average salary above 80000
`🟢 Beginner` `🔥`

**Statement:** Return departments whose average employee salary is greater than 80000.

```sql
SELECT department_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY department_id
HAVING AVG(salary) > 80000;
```

**Alternative:** Join departments for names.

**Notes:** Demonstrates `HAVING`; index rarely helps the aggregate unless filtered first.

### P4. Count orders by status
`🟢 Beginner` `🔥`

**Statement:** Show order count and total amount by status.

```sql
SELECT status, COUNT(*) AS order_count, SUM(total_amount) AS total_amount
FROM orders
GROUP BY status
ORDER BY order_count DESC;
```

**Alternative:** Add `WHERE order_date >= CURRENT_DATE - INTERVAL '30 days'` for recent orders.

**Notes:** Index `(status)` can help selective status filters; grouping all rows may scan.

### P5. Customers created in 2026
`🟢 Beginner`

**Statement:** Return customers whose account was created in calendar year 2026.

```sql
SELECT customer_id, name, created_at
FROM customers
WHERE created_at >= DATE '2026-01-01'
  AND created_at <  DATE '2027-01-01';
```

**Alternative:** Avoid `EXTRACT(YEAR FROM created_at) = 2026` if you want an index range seek.

**Notes:** Index `customers(created_at)` is usable because predicate is sargable.

### P6. Total paid revenue from order items
`🟢 Beginner` `🔥`

**Statement:** Compute revenue from paid orders using item price times quantity.

```sql
SELECT SUM(oi.quantity * oi.unit_price) AS paid_revenue
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
WHERE o.status = 'PAID';
```

**Alternative:** Use `SUM(o.total_amount)` only if it is trusted and already includes all adjustments.

**Notes:** Index `orders(status, order_id)` and `order_items(order_id)` help.

### P7. Products priced above global average
`🟢 Beginner`

**Statement:** Return products whose price is above the average product price.

```sql
SELECT product_id, name, price
FROM products
WHERE price > (SELECT AVG(price) FROM products);
```

**Alternative:** Use `CROSS JOIN (SELECT AVG(price) avg_price FROM products) a`.

**Notes:** Subquery is scalar; database computes it once. Index on `price` may help filtering.

### P8. Monthly order count
`🟢 Beginner` `🔥`

**Statement:** Count orders by month.

```sql
SELECT date_trunc('month', order_date)::date AS month_start,
       COUNT(*) AS order_count
FROM orders
GROUP BY date_trunc('month', order_date)::date
ORDER BY month_start;
```

**Alternative:** MySQL: `DATE_FORMAT(order_date, '%Y-%m-01')` or `EXTRACT(YEAR_MONTH FROM order_date)`.

**Notes:** For filtering, use date ranges; expression indexes can help grouping by month.

### P9. Distinct customers per month
`🟢 Beginner`

**Statement:** Count unique purchasing customers by order month.

```sql
SELECT date_trunc('month', order_date)::date AS month_start,
       COUNT(DISTINCT customer_id) AS unique_customers
FROM orders
GROUP BY 1
ORDER BY 1;
```

**Alternative:** Pre-aggregate `(month, customer_id)` in a CTE, then count rows.

**Notes:** Index `(order_date, customer_id)` helps date filtering and distinct grouping.

### P10. Categories with revenue over 100000
`🟡 Intermediate` `🔥`

**Statement:** Return product categories whose paid revenue exceeds 100000.

```sql
SELECT p.category, SUM(oi.quantity * oi.unit_price) AS revenue
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
JOIN products p ON p.product_id = oi.product_id
WHERE o.status = 'PAID'
GROUP BY p.category
HAVING SUM(oi.quantity * oi.unit_price) > 100000
ORDER BY revenue DESC;
```

**Alternative:** Pre-filter paid orders in a CTE for readability.

**Notes:** Joins are `O(order_items)` after filtering paid orders; index foreign keys.

### P11. Highest salary overall
`🟢 Beginner` `🔥`

**Statement:** Return employee or employees with the highest salary.

```sql
SELECT employee_id, name, salary
FROM employees
WHERE salary = (SELECT MAX(salary) FROM employees);
```

**Alternative:** `ORDER BY salary DESC FETCH FIRST 1 ROW WITH TIES` in PostgreSQL 13+.

**Notes:** Index `(salary DESC)` helps top salary lookup.

### P12. Second highest distinct salary
`🟡 Intermediate` `🔥`

**Statement:** Return the second highest distinct salary.

```sql
SELECT MAX(salary) AS second_highest_salary
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```

**Alternative:**
```sql
SELECT salary
FROM (SELECT DISTINCT salary FROM employees ORDER BY salary DESC LIMIT 2) s
ORDER BY salary
LIMIT 1;
```

**Notes:** Distinct matters when top salary has ties. MySQL supports `LIMIT`; standard SQL uses `FETCH`.

### P13. Duplicate customer emails
`🟢 Beginner` `🔥`

**Statement:** Find emails appearing more than once in `customers`.

```sql
SELECT email, COUNT(*) AS cnt
FROM customers
WHERE email IS NOT NULL
GROUP BY email
HAVING COUNT(*) > 1;
```

**Alternative:** Use window count if you need full duplicate rows.

**Notes:** Unique constraint normally prevents this; useful for dirty imported data.

### P14. Delete duplicate customer emails keeping smallest id
`🟡 Intermediate` `🔥`

**Statement:** Delete duplicate customer rows, keeping the lowest `customer_id` for each email.

```sql
DELETE FROM customers c
USING customers keep
WHERE c.email = keep.email
  AND c.customer_id > keep.customer_id;
```

**Alternative:**
```sql
WITH ranked AS (
  SELECT customer_id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY customer_id) AS rn
  FROM customers
  WHERE email IS NOT NULL
)
DELETE FROM customers c
USING ranked r
WHERE c.customer_id = r.customer_id AND r.rn > 1;
```

**Notes:** MySQL syntax uses `DELETE c FROM customers c JOIN customers keep ...`.

### P15. Customers with no orders
`🟢 Beginner` `🔥`

**Statement:** Return customers who have never placed an order.

```sql
SELECT c.customer_id, c.name
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
WHERE o.order_id IS NULL;
```

**Alternative:**
```sql
SELECT c.customer_id, c.name
FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id);
```

**Notes:** `NOT EXISTS` avoids NULL traps; index `orders(customer_id)` helps.

## Joins

### P16. Employees with department name
`🟢 Beginner` `🔥`

**Statement:** Return employee name, department name, and salary.

```sql
SELECT e.name AS employee_name, d.name AS department_name, e.salary
FROM employees e
JOIN departments d ON d.department_id = e.department_id;
```

**Alternative:** Use `LEFT JOIN` to include employees with no department.

**Notes:** Standard foreign-key join; index child FK `employees(department_id)`.

### P17. Employees earning more than their managers
`🟡 Intermediate` `🔥`

**Statement:** Return employees whose salary is greater than their manager's salary.

```sql
SELECT e.employee_id, e.name, e.salary, m.name AS manager_name, m.salary AS manager_salary
FROM employees e
JOIN employees m ON m.employee_id = e.manager_id
WHERE e.salary > m.salary;
```

**Alternative:** Use a CTE to name employee-manager pairs first.

**Notes:** Famous self-join problem; index `employees(manager_id)` helps some traversals.

### P18. Department highest salary employees
`🟡 Intermediate` `🔥`

**Statement:** Return employees who have the highest salary in their department, including ties.

```sql
SELECT e.department_id, e.employee_id, e.name, e.salary
FROM employees e
JOIN (
  SELECT department_id, MAX(salary) AS max_salary
  FROM employees
  GROUP BY department_id
) m ON m.department_id = e.department_id AND m.max_salary = e.salary;
```

**Alternative:** Use `DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) = 1`.

**Notes:** Index `(department_id, salary DESC)` helps both approaches.

### P19. Order line details with customer and product
`🟢 Beginner`

**Statement:** Return order id, customer name, product name, quantity, and line amount.

```sql
SELECT o.order_id, c.name AS customer_name, p.name AS product_name,
       oi.quantity, oi.quantity * oi.unit_price AS line_amount
FROM orders o
JOIN customers c ON c.customer_id = o.customer_id
JOIN order_items oi ON oi.order_id = o.order_id
JOIN products p ON p.product_id = oi.product_id;
```

**Alternative:** Filter paid orders in `WHERE o.status = 'PAID'` for revenue reports.

**Notes:** Join order is optimized by DB; make sure FK columns are indexed.

### P20. Orders with more than three distinct products
`🟡 Intermediate`

**Statement:** Return orders containing more than three distinct products.

```sql
SELECT order_id, COUNT(DISTINCT product_id) AS product_count
FROM order_items
GROUP BY order_id
HAVING COUNT(DISTINCT product_id) > 3;
```

**Alternative:** Join `orders` to include order metadata.

**Notes:** Primary key `(order_id, product_id)` makes this efficient.

### P21. Customers who bought all products
`🔴 Advanced` `🔥`

**Statement:** Return customers who have purchased every product at least once.

```sql
SELECT c.customer_id, c.name
FROM customers c
JOIN orders o ON o.customer_id = c.customer_id
JOIN order_items oi ON oi.order_id = o.order_id
GROUP BY c.customer_id, c.name
HAVING COUNT(DISTINCT oi.product_id) = (SELECT COUNT(*) FROM products);
```

**Alternative:** Relational division with `NOT EXISTS` twice.

**Notes:** Index `order_items(product_id, order_id)` and `orders(customer_id)` help. Handle empty product catalog explicitly if needed.

### P22. Departments with no employees
`🟢 Beginner`

**Statement:** Return departments that currently have zero employees.

```sql
SELECT d.department_id, d.name
FROM departments d
LEFT JOIN employees e ON e.department_id = d.department_id
WHERE e.employee_id IS NULL;
```

**Alternative:** Use `NOT EXISTS`.

**Notes:** Same anti-join pattern as customers without orders.

### P23. Managers with direct report counts
`🟡 Intermediate`

**Statement:** Return each manager and the number of direct reports.

```sql
SELECT m.employee_id, m.name, COUNT(e.employee_id) AS direct_reports
FROM employees m
JOIN employees e ON e.manager_id = m.employee_id
GROUP BY m.employee_id, m.name
ORDER BY direct_reports DESC;
```

**Alternative:** `LEFT JOIN` to include managers with zero reports if you have a manager list.

**Notes:** Index `employees(manager_id)` supports the self-join.

### P24. Orders above the customer's average order amount
`🟡 Intermediate`

**Statement:** Return orders whose `total_amount` is above that customer's average order amount.

```sql
SELECT o.*
FROM orders o
JOIN (
  SELECT customer_id, AVG(total_amount) AS avg_amount
  FROM orders
  GROUP BY customer_id
) a ON a.customer_id = o.customer_id
WHERE o.total_amount > a.avg_amount;
```

**Alternative:** Use `AVG(total_amount) OVER (PARTITION BY customer_id)` in a subquery.

**Notes:** Window approach scans once logically; index `(customer_id)` helps grouping.

### P25. Products never sold
`🟢 Beginner` `🔥`

**Statement:** Return products that do not appear in any order item.

```sql
SELECT p.product_id, p.name
FROM products p
WHERE NOT EXISTS (
  SELECT 1 FROM order_items oi WHERE oi.product_id = p.product_id
);
```

**Alternative:** `LEFT JOIN order_items ... WHERE oi.product_id IS NULL`.

**Notes:** Index `order_items(product_id)` is important.

### P26. Latest order per customer
`🟡 Intermediate` `🔥`

**Statement:** Return each customer's latest order, including ties on date.

```sql
SELECT o.*
FROM orders o
JOIN (
  SELECT customer_id, MAX(order_date) AS latest_date
  FROM orders
  GROUP BY customer_id
) x ON x.customer_id = o.customer_id AND x.latest_date = o.order_date;
```

**Alternative:** Use `ROW_NUMBER()` and choose one deterministic latest order.

**Notes:** Index `(customer_id, order_date DESC)` helps.

### P27. First order date per customer
`🟢 Beginner`

**Statement:** Return each customer and their first order date.

```sql
SELECT c.customer_id, c.name, MIN(o.order_date) AS first_order_date
FROM customers c
JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name;
```

**Alternative:** Use `LEFT JOIN` to include customers with NULL first order date.

**Notes:** Good for cohort analysis; index `orders(customer_id, order_date)`.

### P28. Customers in the same city pairs
`🟡 Intermediate`

**Statement:** Return unique customer pairs living in the same city.

```sql
SELECT c1.customer_id AS customer1, c2.customer_id AS customer2, c1.city
FROM customers c1
JOIN customers c2
  ON c1.city = c2.city
 AND c1.customer_id < c2.customer_id;
```

**Alternative:** Aggregate by city if only counts are needed.

**Notes:** Self-join can be expensive for large cities; index `(city, customer_id)` helps.

### P29. Employees in the same department as Alice
`🟢 Beginner`

**Statement:** Return employees who work in the same department as employee named Alice, excluding Alice.

```sql
SELECT e.employee_id, e.name
FROM employees e
WHERE e.department_id = (SELECT department_id FROM employees WHERE name = 'Alice' LIMIT 1)
  AND e.name <> 'Alice';
```

**Alternative:** Self-join employees to Alice by department.

**Notes:** If names are not unique, filter by employee id instead.

### P30. Customers with paid orders but no refunded orders
`🟡 Intermediate`

**Statement:** Return customers who have at least one PAID order and no REFUNDED order.

```sql
SELECT c.customer_id, c.name
FROM customers c
WHERE EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id AND o.status = 'PAID')
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id AND o.status = 'REFUNDED');
```

**Alternative:** Group orders by customer and use conditional aggregation.

**Notes:** Index `(customer_id, status)` is ideal.

## Subqueries and Set Logic

### P31. Employees earning above department average
`🟡 Intermediate` `🔥`

**Statement:** Return employees whose salary is above their department's average salary.

```sql
SELECT e.employee_id, e.name, e.department_id, e.salary
FROM employees e
JOIN (
  SELECT department_id, AVG(salary) AS avg_salary
  FROM employees
  GROUP BY department_id
) a ON a.department_id = e.department_id
WHERE e.salary > a.avg_salary;
```

**Alternative:** Window average in a subquery.

**Notes:** Index `(department_id, salary)` helps grouping and filtering.

### P32. Customers whose spend is above average customer spend
`🟡 Intermediate`

**Statement:** Return customers whose total paid spend is above the average spend across spending customers.

```sql
WITH spend AS (
  SELECT customer_id, SUM(total_amount) AS total_spend
  FROM orders
  WHERE status = 'PAID'
  GROUP BY customer_id
)
SELECT c.customer_id, c.name, s.total_spend
FROM spend s
JOIN customers c ON c.customer_id = s.customer_id
WHERE s.total_spend > (SELECT AVG(total_spend) FROM spend);
```

**Alternative:** Use `AVG(total_spend) OVER ()` in an outer subquery.

**Notes:** PostgreSQL can reuse the CTE logically; index `(status, customer_id)` helps.

### P33. Products priced above category average
`🟡 Intermediate`

**Statement:** Return products whose price is above the average price of their category.

```sql
SELECT p.product_id, p.name, p.category, p.price
FROM products p
JOIN (
  SELECT category, AVG(price) AS avg_price
  FROM products
  GROUP BY category
) a ON a.category = p.category
WHERE p.price > a.avg_price;
```

**Alternative:** Window average by category.

**Notes:** Index `(category, price)` helps if product table is large.

### P34. Users with no login in the last 30 days
`🟢 Beginner`

**Statement:** Return users who have not logged in in the last 30 days.

```sql
SELECT u.user_id, u.email
FROM users u
WHERE NOT EXISTS (
  SELECT 1 FROM logins l
  WHERE l.user_id = u.user_id
    AND l.login_date >= CURRENT_DATE - INTERVAL '30 days'
);
```

**Alternative:** Left join recent logins and filter NULL.

**Notes:** Index `logins(user_id, login_date)` supports the anti-join. MySQL interval: `CURRENT_DATE - INTERVAL 30 DAY`.

### P35. Accounts with deposits greater than withdrawals
`🟢 Beginner`

**Statement:** Return accounts where total deposit amount exceeds total withdrawal amount.

```sql
SELECT account_id,
       SUM(CASE WHEN txn_type = 'DEPOSIT' THEN amount ELSE 0 END) AS deposits,
       SUM(CASE WHEN txn_type = 'WITHDRAWAL' THEN amount ELSE 0 END) AS withdrawals
FROM transactions
GROUP BY account_id
HAVING SUM(CASE WHEN txn_type = 'DEPOSIT' THEN amount ELSE 0 END) >
       SUM(CASE WHEN txn_type = 'WITHDRAWAL' THEN amount ELSE 0 END);
```

**Alternative:** PostgreSQL `SUM(amount) FILTER (WHERE txn_type = 'DEPOSIT')`.

**Notes:** Index `(account_id, txn_type)` helps grouped scans by account.

### P36. City with the most customers
`🟢 Beginner`

**Statement:** Return the city or cities with the highest customer count.

```sql
WITH city_counts AS (
  SELECT city, COUNT(*) AS cnt
  FROM customers
  GROUP BY city
)
SELECT city, cnt
FROM city_counts
WHERE cnt = (SELECT MAX(cnt) FROM city_counts);
```

**Alternative:** `ORDER BY cnt DESC FETCH FIRST 1 ROW WITH TIES`.

**Notes:** Handles ties correctly; index `(city)` can help grouping.

### P37. Customers with orders in both January and February 2026
`🟡 Intermediate`

**Statement:** Return customers who placed at least one order in Jan 2026 and at least one in Feb 2026.

```sql
SELECT customer_id
FROM orders
WHERE order_date >= DATE '2026-01-01' AND order_date < DATE '2026-03-01'
GROUP BY customer_id
HAVING COUNT(*) FILTER (WHERE order_date < DATE '2026-02-01') > 0
   AND COUNT(*) FILTER (WHERE order_date >= DATE '2026-02-01') > 0;
```

**Alternative:** MySQL uses `SUM(CASE WHEN ... THEN 1 ELSE 0 END) > 0`.

**Notes:** Index `(order_date, customer_id)` or `(customer_id, order_date)` depending workload.

### P38. Products sold more than average units per product
`🟡 Intermediate`

**Statement:** Return products whose sold quantity is above the average sold quantity among sold products.

```sql
WITH units AS (
  SELECT product_id, SUM(quantity) AS units_sold
  FROM order_items
  GROUP BY product_id
)
SELECT p.product_id, p.name, u.units_sold
FROM units u
JOIN products p ON p.product_id = u.product_id
WHERE u.units_sold > (SELECT AVG(units_sold) FROM units);
```

**Alternative:** Window average over `units`.

**Notes:** Uses aggregate CTE; index `order_items(product_id)` helps.

### P39. Managers earning less than a direct report
`🟡 Intermediate`

**Statement:** Return managers who earn less than at least one direct report.

```sql
SELECT DISTINCT m.employee_id, m.name, m.salary
FROM employees m
JOIN employees e ON e.manager_id = m.employee_id
WHERE m.salary < e.salary;
```

**Alternative:** Group by manager and compare `m.salary < MAX(e.salary)`.

**Notes:** Self-join; index `employees(manager_id)` helps.

### P40. Customers with increasing order totals across their last two orders
`🟡 Intermediate`

**Statement:** Return customers whose latest order total is greater than their previous order total.

```sql
WITH ranked AS (
  SELECT customer_id, order_id, order_date, total_amount,
         LAG(total_amount) OVER (PARTITION BY customer_id ORDER BY order_date, order_id) AS prev_amount,
         ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY order_date DESC, order_id DESC) AS rn_desc
  FROM orders
)
SELECT customer_id, order_id, total_amount, prev_amount
FROM ranked
WHERE rn_desc = 1 AND total_amount > prev_amount;
```

**Alternative:** Self-join latest and previous rows using row numbers.

**Notes:** Index `(customer_id, order_date, order_id)` supports ordering.

## Window Functions

### P41. Number employees by salary within department
`🟡 Intermediate` `🔥`

**Statement:** Assign row numbers to employees in each department ordered by salary descending.

```sql
SELECT employee_id, name, department_id, salary,
       ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC, employee_id) AS salary_row
FROM employees;
```

**Alternative:** Use `RANK` or `DENSE_RANK` if ties should share rank.

**Notes:** Index `(department_id, salary DESC)` can avoid extra sorting for large data.

### P42. Rank all employees by salary with ties
`🟡 Intermediate` `🔥`

**Statement:** Rank employees globally by salary, giving ties the same rank and leaving gaps.

```sql
SELECT employee_id, name, salary,
       RANK() OVER (ORDER BY salary DESC) AS salary_rank
FROM employees;
```

**Alternative:** Use `DENSE_RANK()` to avoid gaps.

**Notes:** Sorting is `O(n log n)`; index on salary can help ordered access.

### P43. Top 3 salaries per department
`🟡 Intermediate` `🔥`

**Statement:** Return employees whose salary is in the top 3 distinct salaries in their department.

```sql
WITH ranked AS (
  SELECT e.*,
         DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) AS dr
  FROM employees e
)
SELECT department_id, employee_id, name, salary
FROM ranked
WHERE dr <= 3
ORDER BY department_id, salary DESC;
```

**Alternative:** Use `ROW_NUMBER()` if exactly 3 employees per department are required.

**Notes:** Famous top-N-per-group pattern; index `(department_id, salary DESC)` helps.

### P44. Running account balance
`🟡 Intermediate` `🔥`

**Statement:** Compute running balance per account. Deposits add and withdrawals subtract.

```sql
SELECT account_id, txn_id, txn_date,
       SUM(CASE WHEN txn_type = 'DEPOSIT' THEN amount ELSE -amount END)
       OVER (PARTITION BY account_id ORDER BY txn_date, txn_id
             ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_balance
FROM transactions;
```

**Alternative:** Store signed amounts in a CTE first.

**Notes:** Index `(account_id, txn_date, txn_id)` supports window order.

### P45. Days between user logins
`🟡 Intermediate`

**Statement:** For each login, show previous login date and days since previous login.

```sql
SELECT user_id, login_date,
       LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS prev_login,
       login_date - LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS days_since_prev
FROM logins;
```

**Alternative:** Use a CTE to compute `prev_login` once.

**Notes:** MySQL uses `DATEDIFF(login_date, prev_login)` in an outer query.

### P46. Month-over-month revenue growth
`🟡 Intermediate` `🔥`

**Statement:** Compute monthly paid revenue and percentage growth from previous month.

```sql
WITH monthly AS (
  SELECT date_trunc('month', order_date)::date AS month_start,
         SUM(total_amount) AS revenue
  FROM orders
  WHERE status = 'PAID'
  GROUP BY 1
)
SELECT month_start, revenue,
       revenue - LAG(revenue) OVER (ORDER BY month_start) AS revenue_delta,
       ROUND(100.0 * (revenue - LAG(revenue) OVER (ORDER BY month_start)) /
             NULLIF(LAG(revenue) OVER (ORDER BY month_start), 0), 2) AS pct_growth
FROM monthly;
```

**Alternative:** Self-join each month to previous month.

**Notes:** MySQL uses `DATE_FORMAT`/`TIMESTAMPDIFF`; index `(status, order_date)` helps.

### P47. Seven-day moving average of daily revenue
`🔴 Advanced`

**Statement:** Compute 7-day moving average revenue from paid orders.

```sql
WITH daily AS (
  SELECT order_date, SUM(total_amount) AS revenue
  FROM orders
  WHERE status = 'PAID'
  GROUP BY order_date
)
SELECT order_date, revenue,
       AVG(revenue) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma7
FROM daily
ORDER BY order_date;
```

**Alternative:** Use a generated date spine to include missing zero-revenue days.

**Notes:** `ROWS` uses last 7 rows, not necessarily calendar days if dates are missing.

### P48. Cumulative paid orders per customer
`🟡 Intermediate`

**Statement:** For each paid order, show how many paid orders the customer has made so far.

```sql
SELECT customer_id, order_id, order_date,
       COUNT(*) OVER (PARTITION BY customer_id ORDER BY order_date, order_id) AS paid_order_number
FROM orders
WHERE status = 'PAID';
```

**Alternative:** Use `ROW_NUMBER()` because each output row is one paid order.

**Notes:** Index `(customer_id, status, order_date)` helps.

### P49. First and last transaction per account
`🟡 Intermediate`

**Statement:** Return each account's first and latest transaction dates.

```sql
SELECT DISTINCT account_id,
       FIRST_VALUE(txn_date) OVER (PARTITION BY account_id ORDER BY txn_date, txn_id) AS first_txn_date,
       FIRST_VALUE(txn_date) OVER (PARTITION BY account_id ORDER BY txn_date DESC, txn_id DESC) AS last_txn_date
FROM transactions;
```

**Alternative:** `GROUP BY account_id` with `MIN(txn_date), MAX(txn_date)` if only dates are needed.

**Notes:** Window version can also return full first/last rows.

### P50. Split users into revenue quartiles
`🔴 Advanced`

**Statement:** Put customers into four buckets by total paid spend.

```sql
WITH spend AS (
  SELECT customer_id, SUM(total_amount) AS total_spend
  FROM orders
  WHERE status = 'PAID'
  GROUP BY customer_id
)
SELECT customer_id, total_spend,
       NTILE(4) OVER (ORDER BY total_spend DESC) AS spend_quartile
FROM spend;
```

**Alternative:** Use `PERCENT_RANK()` or percentile functions for threshold-based segmentation.

**Notes:** Sorting dominates. Bucket sizes are balanced, not equal revenue ranges.

## Revision Cheat Sheet

- Use `WHERE` before grouping and `HAVING` after grouping.
- Use `NOT EXISTS` for anti-joins when NULLs may appear.
- Famous patterns: second highest salary, duplicates, employees above managers, department max salary, bought all products.
- Window functions solve top-N, running totals, previous-row comparison, and ranking without collapsing rows.
- Use `DENSE_RANK` for top N distinct values with ties; `ROW_NUMBER` for exactly N rows.
- PostgreSQL date tools include `date_trunc`, `FILTER`, and interval literals; MySQL syntax differs.
- Index joins on foreign keys and analytics windows on partition/order columns.

## Flashcards

- Q → Customers with no orders pattern? A → `NOT EXISTS` or `LEFT JOIN ... IS NULL`.
- Q → Include ties for department max salary? A → Join to grouped `MAX` or use `DENSE_RANK = 1`.
- Q → Delete duplicates safely? A → Rank duplicates and delete rows where rank > 1.
- Q → Running balance function? A → `SUM(signed_amount) OVER (PARTITION BY account ORDER BY date)`.
- Q → Top 3 salaries per dept with ties? A → `DENSE_RANK() <= 3`.
- Q → MySQL replacement for PostgreSQL `FILTER`? A → `SUM(CASE WHEN condition THEN 1 ELSE 0 END)`.
- Q → Why `NOT IN` risky? A → NULL in subquery can make predicate UNKNOWN.
- Q → Index for latest order per customer? A → `(customer_id, order_date DESC)`.
- Q → Difference between `RANK` and `DENSE_RANK`? A → `RANK` leaves gaps; `DENSE_RANK` does not.
- Q → Bought all products pattern? A → Relational division via count distinct or double `NOT EXISTS`.
