# 06b · SQL 100 Practice Questions, Part 1

Part 1 contains P1-P50 of exactly 100 SQL coding problems. Syntax is PostgreSQL by default; MySQL differences are called out in notes.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Beginner guide to this file](#beginner-guide-to-this-file)
- [Sample Schemas](#sample-schemas)
- [Basics and Aggregation](#basics-and-aggregation)
- [Joins](#joins)
- [Subqueries and Set Logic](#subqueries-and-set-logic)
- [Window Functions](#window-functions)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Beginner guide to this file

### What this file teaches

This file teaches how to turn small backend data questions into SQL queries: first simple filtering,
then grouping, joins, subqueries, and window functions. Interviews ask these because Java services
constantly read relational data for reports, dashboards, audits, cleanup jobs, and product decisions.
The goal is not to memorize answers, but to learn how to identify the output row shape, pick the right
tables, and place filters at the correct stage of the query.

### Mental model

Think of each SQL query as an assembly line: choose the source tables, connect related rows, remove
rows that do not qualify, fold rows into groups when needed, then label or order the result.

- Output grain: decide what one result row represents before writing SELECT.
- Table relationships: primary keys identify rows; foreign keys point from one table to another.
- Join choice: inner joins require a match; left joins keep the left-side row even without a match.
- Filter stage: WHERE filters rows before grouping; HAVING filters groups after aggregation.
- Aggregation: GROUP BY changes many input rows into fewer output rows.
- Window functions: calculate rankings, running totals, and comparisons while keeping row detail.

### How to think through any question here

1. Restate the requested output as "one row per ..." before choosing syntax.
2. Mark which columns come from which table and which keys connect those tables.
3. Decide whether unmatched rows must remain; that usually chooses inner join versus left join.
4. Put row-level conditions such as status, date range, or active flag in WHERE.
5. If the question asks for counts, sums, averages, or group thresholds, define GROUP BY and HAVING.
6. If the question compares a row to another row in the same group, consider a subquery or window function.
7. Say edge cases aloud: ties, duplicate rows, null values, empty groups, and missing related records.
8. Read the final SELECT list and confirm it matches the grain, not an accidental lower-level detail.

### Pattern recognition cues

| If the question mentions... | Think about... | Why it fits |
| --- | --- | --- |
| "per department", "per month", or "by status" | GROUP BY | The output grain is a category or time bucket. |
| "with no orders" or "never sold" | LEFT JOIN with null check or NOT EXISTS | You need rows with no matching child row. |
| "more than three", "above average", or "revenue over" | HAVING or aggregate subquery | The filter depends on a count, sum, or average. |
| "same city pairs" or "manager versus employee" | Self join | The same table plays two roles. |
| "all products" | COUNT DISTINCT compared to total count | Universal conditions often become aggregate comparisons. |
| "latest", "first", or "highest including ties" | MAX or MIN joined back | The aggregate finds the value, and the join returns full rows. |
| "running", "previous", or "rank" | Window function | The answer needs neighboring rows while preserving row detail. |
| "paid only" | WHERE before aggregation | Status is a row-level condition that should shrink input rows first. |

### Abbreviations and key terms

| Short form | Expanded form | Plain meaning |
| --- | --- | --- |
| AS | Alias keyword | Gives a readable name to an output column or table reference. |
| CTE | Common Table Expression | A named temporary result introduced with WITH for one query. |
| DESC | Descending | Sort from high to low or newest to oldest. |
| FAANG | Facebook, Apple, Amazon, Netflix, Google | Shorthand for large product-company interview style. |
| ID | Identifier | A column value that uniquely identifies an entity row. |
| MySQL | MySQL database system | A relational database that uses SQL with some syntax differences from PostgreSQL. |
| NULL | Null marker | A missing or unknown value, not the same as zero or an empty string. |
| P1-P50 | Practice problems 1 through 50 | The first half of this 100-problem SQL practice set. |
| SQL | Structured Query Language | The language used to query and change relational database data. |
| TINYINT | Tiny integer | A small integer type often used by MySQL for boolean-like values. |
| Window function | Analytic function | A calculation across related rows that still returns one row per input row. |

### Common beginner traps

- Starting with SELECT columns before deciding the output grain.
- Using WHERE for aggregate conditions that must be written in HAVING.
- Accidentally turning a LEFT JOIN into an inner join by filtering the right table in WHERE.
- Counting star after an outer join when COUNT(child_id) is needed for zero-child groups.
- Forgetting that COUNT DISTINCT, RANK, DENSE_RANK, and ROW_NUMBER handle duplicates differently.
- Returning only the aggregate value when the question asks for the full row tied to that value.
- Ignoring null behavior in equality comparisons, averages, and anti-join checks.

### How to study this file

1. Read the statement and write the grain in your own words before looking at the SQL.
2. Predict the tables, joins, and WHERE versus HAVING placement, then compare with the short query.
3. Use the detailed notes to explain why the query keeps or removes unmatched rows.
4. Convert each SQL block into a flashcard prompt: "What pattern is this? What edge case breaks it?"
5. Re-solve the same problem with one variation, such as including zero-count rows or handling ties differently.
6. Practice speaking the reasoning out loud, because interviews grade your approach as much as the final query.

### Scalability and production lens

Toy interview tables are small, but production queries must respect indexes, data volume, and correctness under
messy data. Filter early on selective columns such as status and date, join on indexed keys, aggregate at the
lowest useful grain, and be careful with COUNT DISTINCT on very large tables. For cleanup queries such as deleting
duplicates, preview affected rows, run in a transaction, and understand locks, constraints, and rollback strategy.

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

**How to think:** Grain is one row per active employee. The only table needed is employees because the
requested columns all live there, so no join or grouping is useful. Apply active = true in WHERE because it
filters individual rows before any result shaping; ORDER BY only controls display order. Edge cases to say
aloud are boolean storage differences in MySQL, employees with the same name, and whether inactive or
unknown active values should be excluded.

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

**How to think:** Grain is one row per department. Start from departments and LEFT JOIN employees so
departments with zero employees still appear; count e.employee_id rather than star so the null side of the
join counts as zero. The department columns define the GROUP BY, and there is no HAVING because no aggregate
condition is being filtered. Watch for accidentally putting employee filters in WHERE, which would turn the
outer join into an inner join.

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

**How to think:** Grain is one row per department_id after grouping employees. The salary threshold is about
the aggregate AVG salary, so it belongs in HAVING, not WHERE. No join is needed because the query returns
department_id rather than department name. Salary is declared not null in the sample schema, but in real
tables AVG ignores nulls. Departments with no employees are absent because the query starts from employees.

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

**How to think:** Grain is one row per order status. The orders table already has status and total_amount,
so grouping by status is enough. COUNT star counts every order row, while SUM total_amount adds known
amounts and ignores null totals. Use WHERE only if a row-level date or customer filter is added; use HAVING
only for aggregate filters such as statuses with more than 10 orders. A null status becomes its own group.

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

**How to think:** Grain is one row per customer created during calendar year 2026. Use customers only, and
put the date range in WHERE because created_at is a row value. The half-open range from 2026-01-01 to before
2027-01-01 is safer than extracting the year because it can use an index and handles all dates in the year.
Edge cases are boundary dates and whether created_at is a timestamp in another schema.

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

**How to think:** Grain is a single revenue value. Orders must join order_items because the paid status
lives on orders but the quantity and unit price live on line items. The PAID filter belongs in WHERE before
the SUM, and there is no GROUP BY because the whole result is one total. If no paid line items exist, SUM
returns null, not zero; use COALESCE only if the requirement asks for zero.

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

**How to think:** Grain is one row per product whose price beats the global product average. A scalar
subquery computes the one average value, then WHERE compares each product row to it. No join, GROUP BY, or
window function is required. Duplicates do not matter because the average is over product rows. Null prices
are ignored by AVG and fail the greater-than comparison, so clarify whether null-priced products should be
excluded explicitly.

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

**How to think:** Grain is one row per month that has at least one order. The date_trunc expression creates
the month bucket, then GROUP BY counts order rows in that bucket. Filtering, if requested, would be WHERE
before grouping; HAVING is unnecessary here. Missing months are not generated, so the output is not a
complete calendar. If order_date were a timestamp, timezone rules could change the month bucket.

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

**How to think:** Grain is one row per order month. The orders table has both order_date and customer_id,
and COUNT DISTINCT customer_id gives unique purchasing customers rather than order count. GROUP BY the same
month expression used in SELECT. WHERE would be used for paid-only or date-range restrictions; HAVING is not
needed. Null customer_id values are not counted by COUNT DISTINCT, and repeat orders by the same customer in
a month count once.

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

**How to think:** Grain is one row per product category whose paid revenue passes the threshold. Use inner
joins from paid orders to order_items to products because revenue requires all three pieces. The PAID status
filter is row-level, so it belongs in WHERE before grouping. The revenue threshold is aggregate-level, so it
belongs in HAVING. Null categories form their own group, and repeated order lines add to revenue.

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

**How to think:** Grain is one row per employee tied for the highest salary. The scalar subquery finds the
global MAX salary, and the outer WHERE returns every employee at that value, which preserves ties. No join
or GROUP BY is needed. If the table is empty, the subquery returns null and the outer query returns no rows.
Salary is not null in the sample schema, but nullable salaries would not match the max.

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

**How to think:** Grain is a single salary value. The inner subquery finds the global maximum, then WHERE
salary less than that removes the top distinct value; MAX of the remaining salaries gives the second
distinct salary. This is aggregation rather than a window solution. Duplicate top salaries do not matter.
With only one distinct salary or no employees, the aggregate still returns one row with null, which is often
what interviews expect.

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

**How to think:** Grain is one duplicate email value. The WHERE email IS NOT NULL filter is row-level and
avoids grouping unknown email values together. GROUP BY email creates one group per known email, and HAVING
COUNT star greater than one keeps only duplicates. No join is needed. Edge cases include case-insensitive
email matching, whitespace normalization, and whether two null emails should be treated as duplicates by
business rules.

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

**How to think:** Grain is the customer rows to delete, not rows to display. This is a self-join delete: c
is the candidate duplicate and keep is an earlier row with the same email and smaller customer_id. The
comparison belongs in the join condition, and no aggregation is needed. SQL null equality means null emails
are not matched. In production, preview the rows first, run in a transaction, and confirm no unique
constraint already prevents duplicates.

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

**How to think:** Grain is one customer with no matching orders. Start from customers and LEFT JOIN orders,
then keep rows where the order side is null; that is the anti-join pattern. The null check belongs in WHERE
after the outer join. No GROUP BY is required because a customer with any order is removed. Duplicate orders
do not matter, and a primary key order_id makes the null test reliable.

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

**How to think:** Grain is one employee enriched with department name. Employees and departments are
connected by department_id, and an inner join is correct because the statement asks for employees with their
department. There is no filtering, grouping, or window logic. If the business wanted employees even when
department_id is null or broken, switch to a LEFT JOIN and expect null department names.

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

**How to think:** Grain is one employee-manager pair where the employee earns more. This is a self-join: e
is the employee and m is the manager row reached through manager_id. The salary comparison is row-level, so
it belongs in WHERE. No grouping is needed. Top-level employees with null manager_id are naturally excluded
by the inner join, and equal salaries are not returned because the comparison is strictly greater than.

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

**How to think:** Grain is one employee whose salary equals the maximum in that employee's department. The
subquery groups employees by department_id to compute each max salary, then joins back to employees to keep
all ties. A window DENSE_RANK would be an alternative. The max condition is aggregate logic in the subquery,
not a WHERE predicate. Null department_id can be tricky because normal equality does not match nulls.

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

**How to think:** Grain is one order item line with descriptive customer and product data. Inner joins fit
because the output needs a valid order, customer, item, and product. There is no GROUP BY because
line_amount is row-level quantity times unit_price. Filtering would go in WHERE if the question added paid
orders or dates. Edge cases are missing dimension rows, null prices, and duplicate dirty item rows.

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

**How to think:** Grain is one order_id that has more than three distinct products. The order_items table is
enough because the answer depends on products inside each order. GROUP BY order_id creates the order grain,
and HAVING COUNT DISTINCT product_id greater than three filters groups after aggregation. Distinct prevents
duplicate line rows for the same product from inflating the count. Orders with no items are absent.

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

**How to think:** Grain is one customer who has purchased every product. Join customers to orders to
order_items, group by customer, and compare COUNT DISTINCT purchased product_id with the product catalog
count from a scalar subquery. The universal condition belongs in HAVING. Inner joins mean customers with no
purchases never qualify. If the products table is empty, discuss expected behavior because this query will
not return every customer.

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

**How to think:** Grain is one department with zero employees. Start from departments, LEFT JOIN employees,
and keep rows where the employee side is null. That null test belongs in WHERE after preserving unmatched
departments. No aggregation is needed. This is the same anti-join pattern as customers with no orders.
Employee rows with invalid department_id do not affect the result because the question is anchored on
departments.

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

**How to think:** Grain is one manager who has at least one direct report. The employees table joins to
itself: m is the manager and e is each employee whose manager_id points to m. GROUP BY the manager columns
and count report employee_ids. HAVING is unnecessary because the inner join already removes managers with
zero reports. Use LEFT JOIN if the requirement says include managers with zero direct reports.

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

**How to think:** Grain is one order whose total_amount is above that customer's own average. The subquery
groups orders by customer_id to compute avg_amount, then the outer query joins each order to its customer's
average and filters in WHERE. A window AVG over customer is an alternative. Single-order customers do not
qualify because their order equals the average. Null total_amount values are ignored by AVG and fail
comparison.

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

**How to think:** Grain is one product that has no order_items row. NOT EXISTS is an anti-semi-join: for
each product, ask whether any matching item exists. No GROUP BY is needed, and it avoids duplicate item rows
changing the answer. Because the query ignores order status, a product appearing only in refunded or pending
orders is still considered sold. Null product_id values in order_items do not match a product primary key.

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

**How to think:** Grain is one latest order row per customer, including ties on date. The subquery groups
orders by customer_id to find MAX order_date, then joins back to the full orders rows on customer and date.
This preserves multiple orders on the same latest date. Customers with no orders are absent. If the
interviewer wants exactly one row per customer, switch to ROW_NUMBER with an order_id tiebreaker.

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

**How to think:** Grain is one customer with their first order date. Customers inner join orders because
only customers who ordered can have a first order in this query. MIN order_date is the aggregate, so GROUP
BY customer_id and name defines the output grain. HAVING is not needed. Customers without orders are
omitted; use LEFT JOIN if they should appear with a null first_order_date.

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

**How to think:** Grain is one unique pair of customers in the same city. This is a self-join on customers,
and the customer_id less-than condition removes self-pairs and reverse duplicates. No aggregation is needed.
Null city values do not match with equals in standard SQL, so customers with unknown cities are not paired.
Many customers in one city produce many combinations, so result size can grow quickly.

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

**How to think:** Grain is one employee in Alice's department, excluding Alice. A scalar subquery finds
Alice's department_id, and WHERE compares each employee's department_id to it. No join or grouping is
needed. LIMIT 1 means multiple employees named Alice make the chosen department arbitrary, so real systems
should identify Alice by employee_id. The name exclusion removes any employee named Alice, not just the
original row.

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

**How to think:** Grain is one customer satisfying two existence tests. EXISTS checks at least one PAID
order, and NOT EXISTS checks there is no REFUNDED order for the same customer. These correlated subqueries
avoid duplicate rows without GROUP BY. The status filters belong inside the subqueries because they describe
matching order rows. Customers with only pending orders fail, and null statuses do not count as refunded.

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

**How to think:** Grain is one employee above their department average salary. The subquery groups employees
by department_id to calculate avg_salary, then the outer query joins each employee to that department
average and filters in WHERE. This is aggregate-then-compare logic. A one-person department does not qualify
because salary equals the average. Employees with null department_id would not join under normal equality
rules.

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

**How to think:** Grain is one spending customer whose paid spend is above the average among spending
customers. The CTE first groups paid orders by customer_id, then the outer query joins customer names and
compares total_spend to AVG total_spend over the CTE. Filtering PAID belongs in WHERE inside the CTE.
Customers with no paid orders are excluded from both the output and the average, which is an important
business choice.

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

**How to think:** Grain is one product priced above its category average. The subquery groups products by
category to compute avg_price, then joins back to products and filters row prices in WHERE. This is the
category version of above-global-average. A category with one product does not return that product. Null
categories can be missed by the equality join, and null prices are ignored by AVG and fail comparison.

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

**How to think:** Grain is one user who lacks a recent login. NOT EXISTS checks for no login row for that
user with login_date on or after current date minus 30 days. The date condition belongs inside the
correlated subquery because it defines disqualifying logins. Users who never logged in are included. The
boundary includes exactly 30 days ago; clarify if the business means strictly after 30 days.

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

**How to think:** Grain is one account_id with deposits greater than withdrawals. Transactions are grouped
by account_id, and conditional SUM expressions separately total deposit and withdrawal amounts. The
comparison is aggregate-level, so it belongs in HAVING. No join to accounts is needed unless account
metadata is required. Accounts with no transactions are absent, and unknown transaction types contribute
zero in both conditional sums.

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

**How to think:** Grain is one city tied for the highest customer count. The CTE groups customers by city,
then the outer query compares each city count to the maximum count from the CTE. This keeps ties. The filter
is on an already aggregated result, so WHERE in the outer query is fine. Null city can become a group, and
an empty customers table returns no rows.

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

**How to think:** Grain is one customer_id with at least one January 2026 order and at least one February
2026 order. WHERE narrows rows to the two-month window, then GROUP BY customer_id. The two month-presence
checks are aggregate conditions, so they belong in HAVING with filtered counts. Multiple orders per month
are fine. Half-open date boundaries avoid time-of-day bugs if order_date becomes a timestamp.

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

**How to think:** Grain is one sold product whose total units exceed the average units among sold products.
The CTE groups order_items by product_id and sums quantity, then the outer query joins product details and
compares to AVG units_sold over that CTE. Unsold products are excluded from both the average and output.
Duplicate dirty item rows or null quantities can affect totals, so validate item grain.

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

**How to think:** Grain is one manager who earns less than at least one direct report. The employees table
self-joins manager rows to report rows, and WHERE compares salaries. DISTINCT removes duplicate manager rows
when several reports earn more. No aggregation is required. Equal salaries are not returned, and top-level
managers can still qualify if another employee points to them as manager.

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

**How to think:** Grain is one latest order for a customer, but only when its total is greater than the
previous order total. The CTE uses LAG to see the previous amount in chronological order and ROW_NUMBER
descending to identify the latest row. Filtering happens after window calculation. Customers with fewer than
two orders have null previous amount and fail. The order_date, order_id ordering handles same-day ties
deterministically.

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

**How to think:** Grain is one employee with an ordinal salary_row inside the employee's department.
ROW_NUMBER is a window function, not aggregation, so it preserves every employee row while numbering within
each department partition. Salary descending defines the ranking order, and employee_id breaks ties
deterministically. Use RANK or DENSE_RANK instead when tied salaries should share the same number. Null
department_id forms its own partition.

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

**How to think:** Grain is one employee with a global salary_rank. RANK is a window function over all
employees because there is no PARTITION BY. Tied salaries receive the same rank and the next rank has a gap,
which is exactly what the statement asks. No WHERE, GROUP BY, or join is needed. If the interviewer wants no
gaps, use DENSE_RANK; if they want arbitrary unique positions, use ROW_NUMBER.

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

**How to think:** Grain is one employee whose salary falls in the top three distinct salary levels of their
department. The CTE uses DENSE_RANK partitioned by department_id so ties share a rank and ranks have no
gaps. The outer WHERE keeps ranks up to three. This can return more than three employees per department when
there are ties. Use ROW_NUMBER for exactly three rows.

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

**How to think:** Grain is one transaction with the account balance after that transaction. The signed CASE
expression turns deposits positive and withdrawals negative, and the window SUM accumulates within each
account ordered by txn_date and txn_id. ROWS up to current row makes it a true running total. Be careful
that this query treats every non-DEPOSIT type as negative; production code should handle unknown transaction
types explicitly.

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

**How to think:** Grain is one login row with its previous login and day gap. LAG looks backward within each
user_id partition ordered by login_date, so no self-join is necessary. The first login per user has null
previous login and null gap. Because the sample logins table is keyed by user and date, duplicate same-day
logins are impossible; with timestamps, add a stable tiebreaker.

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

**How to think:** Grain is one paid revenue month with its previous-month delta and percentage growth. The
CTE groups paid orders by month using WHERE status = PAID, then LAG reads the prior result row. NULLIF
protects division by zero. Missing revenue months are skipped, so the previous row may not be the previous
calendar month unless you first fill a calendar table.

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

**How to think:** Grain is one paid-revenue day that exists in the daily CTE. First aggregate orders to
daily revenue, then use a window AVG over the current row and six preceding rows. This is a seven-row moving
average, not a guaranteed seven-calendar-day average when dates are missing. WHERE filters paid orders
before aggregation. For zero-revenue days, generate a date series first.

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

**How to think:** Grain is one paid order with the count of paid orders that customer has made so far. The
WHERE status = PAID filter runs before the window, so unpaid orders do not affect the count. COUNT star over
the customer partition ordered by order_date and order_id gives the running ordinal. Same-day orders are
deterministic because order_id is part of the order.

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

**How to think:** Grain is one account_id with first and last transaction dates. FIRST_VALUE windows keep
row detail, then DISTINCT collapses repeated account rows. The partition is account_id, with opposite
orderings for first and last. Because only dates are needed, GROUP BY with MIN and MAX would be simpler;
this version demonstrates window functions. Same-day transactions are ordered by txn_id for determinism.

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

**How to think:** Grain is one spending customer assigned to a paid-spend quartile. The CTE groups paid
orders by customer_id, then NTILE(4) divides those customer rows into four roughly equal buckets ordered by
spend descending. Non-spending customers are excluded. Ties can be split across quartiles because NTILE
balances row counts, so add a customer_id tiebreaker if stable repeatable buckets matter.

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
