# 06c · SQL 100 Practice Questions, Part 2

Part 2 contains P51-P100 of exactly 100 SQL coding problems, continuing the schemas defined in `06b_SQL_100_Practice_Questions.md`. Syntax is PostgreSQL by default; MySQL differences are noted.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Top-N, Nth Highest, and Duplicates](#top-n-nth-highest-and-duplicates)
- [Running Totals, Moving Averages, Gaps and Islands](#running-totals-moving-averages-gaps-and-islands)
- [Date and Time Analytics](#date-and-time-analytics)
- [Recursive CTE, Pivot, and Advanced Analytics](#recursive-cte-pivot-and-advanced-analytics)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Top-N, Nth Highest, and Duplicates

### P51. Nth highest distinct salary
`🟡 Intermediate` `🔥`

**Statement:** Return the Nth highest distinct salary. Assume `:n` is a positive parameter.

```sql
SELECT salary
FROM (
  SELECT DISTINCT salary,
         DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
  FROM employees
) s
WHERE rnk = :n;
```

**Alternative:** PostgreSQL offset: `SELECT DISTINCT salary FROM employees ORDER BY salary DESC OFFSET :n - 1 LIMIT 1`.

**Notes:** Window approach is portable conceptually. Index `(salary DESC)` helps ordering.

### P52. Second highest salary returning NULL if missing
`🟡 Intermediate` `🔥`

**Statement:** Return one row with the second highest distinct salary, or NULL if it does not exist.

```sql
SELECT (
  SELECT salary
  FROM (SELECT DISTINCT salary FROM employees ORDER BY salary DESC LIMIT 2) x
  ORDER BY salary
  LIMIT 1
) AS second_highest_salary;
```

**Alternative:** Use `MAX(salary) WHERE salary < global max`, but it also returns NULL when missing.

**Notes:** Mirrors common LeetCode style requirement of exactly one output row.

### P53. Top N products per category by revenue
`🔴 Advanced` `🔥`

**Statement:** Return top `:n` products per category by paid revenue.

```sql
WITH revenue AS (
  SELECT p.category, p.product_id, p.name,
         SUM(oi.quantity * oi.unit_price) AS revenue
  FROM products p
  JOIN order_items oi ON oi.product_id = p.product_id
  JOIN orders o ON o.order_id = oi.order_id
  WHERE o.status = 'PAID'
  GROUP BY p.category, p.product_id, p.name
), ranked AS (
  SELECT revenue.*, DENSE_RANK() OVER (PARTITION BY category ORDER BY revenue DESC) AS rnk
  FROM revenue
)
SELECT * FROM ranked WHERE rnk <= :n;
```

**Alternative:** Use `ROW_NUMBER` for exactly N products per category.

**Notes:** Index `orders(status, order_id)` and `order_items(product_id, order_id)` help.

### P54. Latest two logins per user
`🟡 Intermediate`

**Statement:** Return each user's latest two login dates.

```sql
WITH ranked AS (
  SELECT user_id, login_date,
         ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date DESC) AS rn
  FROM logins
)
SELECT user_id, login_date
FROM ranked
WHERE rn <= 2
ORDER BY user_id, login_date DESC;
```

**Alternative:** PostgreSQL lateral join per user with `ORDER BY login_date DESC LIMIT 2`.

**Notes:** Index `(user_id, login_date DESC)` is ideal.

### P55. Users with exactly one login
`🟢 Beginner`

**Statement:** Return users who have exactly one login record.

```sql
SELECT user_id
FROM logins
GROUP BY user_id
HAVING COUNT(*) = 1;
```

**Alternative:** Join to `users` to include email.

**Notes:** Primary key `(user_id, login_date)` makes counting grouped logins efficient.

### P56. Duplicate login days after dirty import
`🟢 Beginner`

**Statement:** If `logins` allowed duplicates, find `(user_id, login_date)` pairs appearing more than once.

```sql
SELECT user_id, login_date, COUNT(*) AS cnt
FROM logins
GROUP BY user_id, login_date
HAVING COUNT(*) > 1;
```

**Alternative:** Use window count to return every duplicate row.

**Notes:** In the provided schema, the primary key prevents duplicates; this tests the pattern.

### P57. Delete duplicate logins keeping one row
`🟡 Intermediate`

**Statement:** Delete duplicate login rows, keeping one row per `(user_id, login_date)`. Assume a dirty table `logins_dirty(id, user_id, login_date)`.

```sql
WITH ranked AS (
  SELECT id,
         ROW_NUMBER() OVER (PARTITION BY user_id, login_date ORDER BY id) AS rn
  FROM logins_dirty
)
DELETE FROM logins_dirty d
USING ranked r
WHERE d.id = r.id AND r.rn > 1;
```

**Alternative:** MySQL 8 supports a CTE with `DELETE` differently; often use join to a derived table.

**Notes:** Always preview ranked rows before deleting in production.

### P58. Duplicate orders with same customer, date, and amount
`🟡 Intermediate`

**Statement:** Find possible duplicate orders for same customer, order date, and total amount.

```sql
SELECT customer_id, order_date, total_amount, COUNT(*) AS cnt
FROM orders
GROUP BY customer_id, order_date, total_amount
HAVING COUNT(*) > 1;
```

**Alternative:** Use `ARRAY_AGG(order_id)` in PostgreSQL to list duplicate ids.

**Notes:** Add a uniqueness/idempotency key in real payment/order systems.

### P59. Find gaps in employee ids
`🟡 Intermediate`

**Statement:** Show employee id ranges where an id is missing between existing adjacent ids.

```sql
WITH x AS (
  SELECT employee_id,
         LEAD(employee_id) OVER (ORDER BY employee_id) AS next_id
  FROM employees
)
SELECT employee_id + 1 AS gap_start, next_id - 1 AS gap_end
FROM x
WHERE next_id > employee_id + 1;
```

**Alternative:** Generate a full series and anti-join missing ids in PostgreSQL.

**Notes:** Gaps in surrogate keys are normal; do not treat them as data loss by default.

### P60. Employees with invalid manager references
`🟢 Beginner`

**Statement:** Return employees whose `manager_id` does not exist in employees.

```sql
SELECT e.employee_id, e.name, e.manager_id
FROM employees e
LEFT JOIN employees m ON m.employee_id = e.manager_id
WHERE e.manager_id IS NOT NULL
  AND m.employee_id IS NULL;
```

**Alternative:** `WHERE manager_id IS NOT NULL AND NOT EXISTS (...)`.

**Notes:** A foreign key prevents this; useful for validating imported data.

## Running Totals, Moving Averages, Gaps and Islands

### P61. Active users for 3 consecutive days
`🔴 Advanced` `🔥`

**Statement:** Return users who logged in on at least three consecutive days.

```sql
WITH x AS (
  SELECT user_id, login_date,
         login_date - (ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date))::int AS grp
  FROM logins
), streaks AS (
  SELECT user_id, MIN(login_date) AS start_date, MAX(login_date) AS end_date, COUNT(*) AS days
  FROM x
  GROUP BY user_id, grp
)
SELECT DISTINCT user_id
FROM streaks
WHERE days >= 3;
```

**Alternative:** Self-join `l1`, `l2`, `l3` on `+ INTERVAL '1 day'` and `+ INTERVAL '2 days'`.

**Notes:** MySQL date arithmetic uses `DATE_SUB(login_date, INTERVAL rn DAY)` in an outer query.

### P62. Consecutive numbers appearing at least three times
`🟡 Intermediate` `🔥`

**Statement:** From `numbers(id, num)`, return numbers that appear in at least three consecutive ids.

```sql
SELECT DISTINCT n1.num AS consecutive_num
FROM numbers n1
JOIN numbers n2 ON n2.id = n1.id + 1 AND n2.num = n1.num
JOIN numbers n3 ON n3.id = n1.id + 2 AND n3.num = n1.num;
```

**Alternative:** Use `LAG(num, 1)` and `LAG(num, 2)` over `ORDER BY id`.

**Notes:** Famous consecutive numbers pattern; index on `id` is primary key.

### P63. Stadium high traffic for 3 consecutive rows
`🔴 Advanced` `🔥`

**Statement:** Return stadium rows where people >= 100 for at least three consecutive ids.

```sql
WITH high AS (
  SELECT *, id - ROW_NUMBER() OVER (ORDER BY id) AS grp
  FROM stadium
  WHERE people >= 100
), streaks AS (
  SELECT grp
  FROM high
  GROUP BY grp
  HAVING COUNT(*) >= 3
)
SELECT h.id, h.visit_date, h.people
FROM high h
JOIN streaks s ON s.grp = h.grp
ORDER BY h.id;
```

**Alternative:** Use self-joins on `id + 1` and `id + 2`, but returning all rows is clunkier.

**Notes:** This assumes consecutive ids represent consecutive records as in the famous problem.

### P64. Rising temperature
`🟡 Intermediate` `🔥`

**Statement:** Return weather rows where temperature is higher than the previous calendar day.

```sql
SELECT w.id
FROM weather w
JOIN weather p ON p.record_date = w.record_date - INTERVAL '1 day'
WHERE w.temperature > p.temperature;
```

**Alternative:** Use `LAG(temperature) OVER (ORDER BY record_date)` and compare dates.

**Notes:** MySQL: `DATE_SUB(w.record_date, INTERVAL 1 DAY)`.

### P65. Login streak islands per user
`🔴 Advanced`

**Statement:** Return all consecutive login streaks per user with start date, end date, and length.

```sql
WITH x AS (
  SELECT user_id, login_date,
         login_date - (ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date))::int AS grp
  FROM logins
)
SELECT user_id, MIN(login_date) AS start_date, MAX(login_date) AS end_date, COUNT(*) AS streak_days
FROM x
GROUP BY user_id, grp
ORDER BY user_id, start_date;
```

**Alternative:** Use recursive CTE to walk dates, but row-number grouping is simpler.

**Notes:** Classic gaps-and-islands technique; input should be one row per day.

### P66. Longest login streak per user
`🔴 Advanced`

**Statement:** Return each user's longest consecutive login streak length.

```sql
WITH islands AS (
  SELECT user_id,
         login_date - (ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date))::int AS grp
  FROM logins
), streaks AS (
  SELECT user_id, COUNT(*) AS streak_days
  FROM islands
  GROUP BY user_id, grp
)
SELECT user_id, MAX(streak_days) AS longest_streak
FROM streaks
GROUP BY user_id;
```

**Alternative:** Rank streaks by length and return full start/end dates too.

**Notes:** Index `(user_id, login_date)` is essential.

### P67. Running sum until account crosses threshold
`🔴 Advanced`

**Statement:** Return the first transaction where each account's running balance reaches at least 10000.

```sql
WITH balances AS (
  SELECT account_id, txn_id, txn_date,
         SUM(CASE WHEN txn_type = 'DEPOSIT' THEN amount ELSE -amount END)
         OVER (PARTITION BY account_id ORDER BY txn_date, txn_id) AS balance
  FROM transactions
), ranked AS (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY account_id ORDER BY txn_date, txn_id) AS rn
  FROM balances
  WHERE balance >= 10000
)
SELECT account_id, txn_id, txn_date, balance
FROM ranked
WHERE rn = 1;
```

**Alternative:** Use `MIN(txn_date)` after filtering, then join back, but tie handling needs care.

**Notes:** Requires sorted window per account; index `(account_id, txn_date, txn_id)`.

### P68. Rolling 3-transaction average per account
`🟡 Intermediate`

**Statement:** Compute average amount over current and previous two transactions per account.

```sql
SELECT account_id, txn_id, txn_date, amount,
       AVG(amount) OVER (PARTITION BY account_id ORDER BY txn_date, txn_id ROWS BETWEEN 2 PRECEDING AND CURRENT ROW) AS avg_last_3
FROM transactions;
```

**Alternative:** Self-join on row numbers for databases without window frames.

**Notes:** `ROWS` means previous two rows, not previous two days.

### P69. Next-day retention
`🟡 Intermediate` `🔥`

**Statement:** For each login date, compute what percentage of users returned the next day.

```sql
WITH daily AS (
  SELECT login_date, user_id FROM logins
), retained AS (
  SELECT d.login_date, COUNT(*) AS retained_users
  FROM daily d
  JOIN daily n ON n.user_id = d.user_id AND n.login_date = d.login_date + INTERVAL '1 day'
  GROUP BY d.login_date
)
SELECT d.login_date,
       COUNT(DISTINCT d.user_id) AS active_users,
       COALESCE(r.retained_users, 0) AS retained_users,
       ROUND(100.0 * COALESCE(r.retained_users, 0) / COUNT(DISTINCT d.user_id), 2) AS retention_pct
FROM daily d
LEFT JOIN retained r ON r.login_date = d.login_date
GROUP BY d.login_date, r.retained_users
ORDER BY d.login_date;
```

**Alternative:** Use `LEAD(login_date)` per user and count next-day matches.

**Notes:** MySQL date addition uses `DATE_ADD(d.login_date, INTERVAL 1 DAY)`.

### P70. Cohort retention by signup month and age month
`🔴 Advanced` `🔥`

**Statement:** Count active users by signup cohort month and months since signup.

```sql
SELECT date_trunc('month', u.signup_date)::date AS cohort_month,
       ((EXTRACT(YEAR FROM l.login_date) - EXTRACT(YEAR FROM u.signup_date)) * 12
        + EXTRACT(MONTH FROM l.login_date) - EXTRACT(MONTH FROM u.signup_date))::int AS month_age,
       COUNT(DISTINCT u.user_id) AS active_users
FROM users u
JOIN logins l ON l.user_id = u.user_id
GROUP BY 1, 2
ORDER BY 1, 2;
```

**Alternative:** MySQL uses `TIMESTAMPDIFF(MONTH, signup_date, login_date)`.

**Notes:** Index `logins(user_id, login_date)` and `users(signup_date)` help.

## Date and Time Analytics

### P71. This month revenue vs previous month
`🟡 Intermediate`

**Statement:** Return paid revenue for current month and previous month.

```sql
SELECT
  SUM(total_amount) FILTER (WHERE order_date >= date_trunc('month', CURRENT_DATE)) AS this_month_revenue,
  SUM(total_amount) FILTER (
    WHERE order_date >= date_trunc('month', CURRENT_DATE) - INTERVAL '1 month'
      AND order_date <  date_trunc('month', CURRENT_DATE)
  ) AS previous_month_revenue
FROM orders
WHERE status = 'PAID';
```

**Alternative:** MySQL uses `SUM(CASE WHEN ... THEN total_amount ELSE 0 END)`.

**Notes:** Keep predicates sargable if filtering with date ranges.

### P72. Orders placed on weekends
`🟢 Beginner`

**Statement:** Return orders placed on Saturday or Sunday.

```sql
SELECT order_id, customer_id, order_date
FROM orders
WHERE EXTRACT(ISODOW FROM order_date) IN (6, 7);
```

**Alternative:** MySQL: `DAYOFWEEK(order_date) IN (1, 7)` where Sunday is 1.

**Notes:** Function on column prevents normal date index seek; use only after a date range filter for large tables.

### P73. First order within 7 days of signup
`🟡 Intermediate`

**Statement:** Return users/customers whose first order happened within 7 days of customer creation.

```sql
WITH first_order AS (
  SELECT customer_id, MIN(order_date) AS first_order_date
  FROM orders
  GROUP BY customer_id
)
SELECT c.customer_id, c.name, c.created_at, f.first_order_date
FROM customers c
JOIN first_order f ON f.customer_id = c.customer_id
WHERE f.first_order_date < c.created_at + INTERVAL '7 days';
```

**Alternative:** MySQL uses `DATE_ADD(c.created_at, INTERVAL 7 DAY)`.

**Notes:** Useful activation metric; index `orders(customer_id, order_date)`.

### P74. Monthly active users
`🟢 Beginner` `🔥`

**Statement:** Count distinct login users per month.

```sql
SELECT date_trunc('month', login_date)::date AS month_start,
       COUNT(DISTINCT user_id) AS mau
FROM logins
GROUP BY 1
ORDER BY 1;
```

**Alternative:** MySQL: group by `DATE_FORMAT(login_date, '%Y-%m-01')`.

**Notes:** For exact large-scale distinct counts, this can be expensive; approximate counts are system-specific.

### P75. Event counts by hour of day
`🟢 Beginner`

**Statement:** Return event count for each hour of day.

```sql
SELECT EXTRACT(HOUR FROM event_time)::int AS hour_of_day,
       COUNT(*) AS event_count
FROM events
GROUP BY 1
ORDER BY 1;
```

**Alternative:** MySQL uses `HOUR(event_time)`.

**Notes:** Consider time zone before grouping product analytics by hour.

### P76. Users inactive after signup
`🟢 Beginner`

**Statement:** Return users who signed up but never logged in.

```sql
SELECT u.user_id, u.email
FROM users u
LEFT JOIN logins l ON l.user_id = u.user_id
WHERE l.user_id IS NULL;
```

**Alternative:** `NOT EXISTS` with `logins`.

**Notes:** Index `logins(user_id)` makes this anti-join fast.

### P77. Fill missing daily revenue dates
`🔴 Advanced`

**Statement:** Return every date between min and max order date with paid revenue, using 0 for missing days.

```sql
WITH bounds AS (
  SELECT MIN(order_date) AS min_date, MAX(order_date) AS max_date FROM orders
), dates AS (
  SELECT generate_series(min_date, max_date, INTERVAL '1 day')::date AS order_date
  FROM bounds
), daily AS (
  SELECT order_date, SUM(total_amount) AS revenue
  FROM orders
  WHERE status = 'PAID'
  GROUP BY order_date
)
SELECT d.order_date, COALESCE(daily.revenue, 0) AS revenue
FROM dates d
LEFT JOIN daily ON daily.order_date = d.order_date
ORDER BY d.order_date;
```

**Alternative:** MySQL 8 uses a recursive CTE to generate dates.

**Notes:** Date spines are important for correct moving averages.

### P78. Time from view to purchase in same session
`🔴 Advanced`

**Statement:** For sessions with a view and purchase, return seconds from first view to first purchase.

```sql
WITH session_events AS (
  SELECT session_id,
         MIN(event_time) FILTER (WHERE event_type = 'view') AS first_view,
         MIN(event_time) FILTER (WHERE event_type = 'purchase') AS first_purchase
  FROM events
  GROUP BY session_id
)
SELECT session_id,
       EXTRACT(EPOCH FROM (first_purchase - first_view)) AS seconds_to_purchase
FROM session_events
WHERE first_view IS NOT NULL
  AND first_purchase IS NOT NULL
  AND first_purchase >= first_view;
```

**Alternative:** MySQL uses conditional `MIN(CASE WHEN ...)` and `TIMESTAMPDIFF(SECOND, first_view, first_purchase)`.

**Notes:** Index `(session_id, event_type, event_time)` helps.

## Recursive CTE, Pivot, and Advanced Analytics

### P79. Employee hierarchy with levels
`🔴 Advanced` `🔥`

**Statement:** Return every employee in `employee_tree` with hierarchy level starting from roots.

```sql
WITH RECURSIVE org AS (
  SELECT id, name, manager_id, 1 AS level
  FROM employee_tree
  WHERE manager_id IS NULL
  UNION ALL
  SELECT e.id, e.name, e.manager_id, org.level + 1
  FROM employee_tree e
  JOIN org ON e.manager_id = org.id
)
SELECT * FROM org ORDER BY level, id;
```

**Alternative:** Store materialized path for faster reads if hierarchy is read-heavy.

**Notes:** Add cycle protection in dirty data; index `employee_tree(manager_id)`.

### P80. Manager chain for a given employee
`🔴 Advanced`

**Statement:** Return the chain from employee `:employee_id` up to the root manager.

```sql
WITH RECURSIVE chain AS (
  SELECT id, name, manager_id, 0 AS distance
  FROM employee_tree
  WHERE id = :employee_id
  UNION ALL
  SELECT m.id, m.name, m.manager_id, chain.distance + 1
  FROM employee_tree m
  JOIN chain ON chain.manager_id = m.id
)
SELECT * FROM chain ORDER BY distance;
```

**Alternative:** Use closure table if ancestry queries are frequent.

**Notes:** Recursive depth should be bounded or cycle-checked in production.

### P81. Count all subordinates per manager
`🔴 Advanced`

**Statement:** Return every manager id and total number of direct plus indirect subordinates.

```sql
WITH RECURSIVE subordinates AS (
  SELECT manager_id, id AS subordinate_id
  FROM employee_tree
  WHERE manager_id IS NOT NULL
  UNION ALL
  SELECT s.manager_id, e.id
  FROM subordinates s
  JOIN employee_tree e ON e.manager_id = s.subordinate_id
)
SELECT manager_id, COUNT(DISTINCT subordinate_id) AS total_subordinates
FROM subordinates
GROUP BY manager_id
ORDER BY total_subordinates DESC;
```

**Alternative:** Maintain a closure table `(ancestor, descendant, depth)`.

**Notes:** Recursive traversal can be expensive on deep trees.

### P82. Pivot revenue by order status
`🟡 Intermediate`

**Statement:** Return monthly revenue columns for PAID, REFUNDED, and PENDING orders.

```sql
SELECT date_trunc('month', order_date)::date AS month_start,
       SUM(total_amount) FILTER (WHERE status = 'PAID') AS paid_revenue,
       SUM(total_amount) FILTER (WHERE status = 'REFUNDED') AS refunded_revenue,
       SUM(total_amount) FILTER (WHERE status = 'PENDING') AS pending_revenue
FROM orders
GROUP BY 1
ORDER BY 1;
```

**Alternative:** MySQL uses `SUM(CASE WHEN status = 'PAID' THEN total_amount ELSE 0 END)`.

**Notes:** Conditional aggregation is the most portable pivot pattern.

### P83. Pivot product counts by category for each order
`🟡 Intermediate`

**Statement:** Return each order with counts of books, electronics, and clothing items.

```sql
SELECT oi.order_id,
       SUM(oi.quantity) FILTER (WHERE p.category = 'Books') AS books_qty,
       SUM(oi.quantity) FILTER (WHERE p.category = 'Electronics') AS electronics_qty,
       SUM(oi.quantity) FILTER (WHERE p.category = 'Clothing') AS clothing_qty
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
GROUP BY oi.order_id;
```

**Alternative:** Use CASE expressions for MySQL or dynamic SQL for dynamic categories.

**Notes:** Static pivots are easy; dynamic pivots are database-specific.

### P84. Unpivot quarterly revenue columns
`🟡 Intermediate`

**Statement:** Given a wide table `quarterly_sales(product_id, q1, q2, q3, q4)`, return rows `(product_id, quarter, revenue)`.

```sql
SELECT product_id, 'Q1' AS quarter, q1 AS revenue FROM quarterly_sales
UNION ALL
SELECT product_id, 'Q2', q2 FROM quarterly_sales
UNION ALL
SELECT product_id, 'Q3', q3 FROM quarterly_sales
UNION ALL
SELECT product_id, 'Q4', q4 FROM quarterly_sales;
```

**Alternative:** PostgreSQL can use `CROSS JOIN LATERAL (VALUES ('Q1', q1), ...)`.

**Notes:** Use `UNION ALL`; `UNION` would incorrectly remove duplicate revenue rows.

### P85. Median salary overall
`🔴 Advanced` `🔥`

**Statement:** Return the median employee salary.

```sql
SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM employees;
```

**Alternative:** MySQL 8 lacks `percentile_cont` in many setups; use row numbers and average middle rows.

**Notes:** Ordered-set aggregate sorts salaries; index may help but DB may still sort.

### P86. Median salary per department
`🔴 Advanced`

**Statement:** Return median salary for each department.

```sql
SELECT department_id,
       percentile_cont(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM employees
GROUP BY department_id;
```

**Alternative:** Use row-number middle-row method for portability.

**Notes:** Index `(department_id, salary)` helps ordered grouping.

### P87. Conversion funnel counts
`🔴 Advanced` `🔥`

**Statement:** Count users who performed view, add_to_cart, and purchase events in that order.

```sql
WITH per_user AS (
  SELECT user_id,
         MIN(event_time) FILTER (WHERE event_type = 'view') AS viewed_at,
         MIN(event_time) FILTER (WHERE event_type = 'add_to_cart') AS cart_at,
         MIN(event_time) FILTER (WHERE event_type = 'purchase') AS purchased_at
  FROM events
  GROUP BY user_id
)
SELECT COUNT(*) FILTER (WHERE viewed_at IS NOT NULL) AS viewed_users,
       COUNT(*) FILTER (WHERE cart_at > viewed_at) AS cart_after_view_users,
       COUNT(*) FILTER (WHERE purchased_at > cart_at AND cart_at > viewed_at) AS purchase_after_cart_users
FROM per_user;
```

**Alternative:** Use step-by-step joins where each later event must occur after prior event.

**Notes:** Simple first-event funnel; real funnels may need session constraints.

### P88. First-touch attribution source
`🔴 Advanced`

**Statement:** Given `events.event_type = 'utm:<source>'` for acquisition events, return first source per user.

```sql
WITH sources AS (
  SELECT user_id, REPLACE(event_type, 'utm:', '') AS source, event_time,
         ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY event_time, event_id) AS rn
  FROM events
  WHERE event_type LIKE 'utm:%'
)
SELECT user_id, source
FROM sources
WHERE rn = 1;
```

**Alternative:** PostgreSQL `DISTINCT ON (user_id) ... ORDER BY user_id, event_time`.

**Notes:** Index `(user_id, event_time)` helps; source parsing is usually cleaner as a separate column.

### P89. D1 and D7 retention by signup date
`🔴 Advanced` `🔥`

**Statement:** For each signup date, compute users who logged in exactly day 1 and day 7 after signup.

```sql
SELECT u.signup_date,
       COUNT(*) AS cohort_size,
       COUNT(*) FILTER (WHERE l1.user_id IS NOT NULL) AS d1_retained,
       COUNT(*) FILTER (WHERE l7.user_id IS NOT NULL) AS d7_retained
FROM users u
LEFT JOIN logins l1 ON l1.user_id = u.user_id AND l1.login_date = u.signup_date + INTERVAL '1 day'
LEFT JOIN logins l7 ON l7.user_id = u.user_id AND l7.login_date = u.signup_date + INTERVAL '7 days'
GROUP BY u.signup_date
ORDER BY u.signup_date;
```

**Alternative:** MySQL uses `DATE_ADD(u.signup_date, INTERVAL 1 DAY)` and `SUM(CASE WHEN ...)`.

**Notes:** Index `logins(user_id, login_date)` is essential.

### P90. Churned customers with no order in 90 days
`🟡 Intermediate`

**Statement:** Return customers whose latest order is older than 90 days.

```sql
SELECT c.customer_id, c.name, MAX(o.order_date) AS last_order_date
FROM customers c
JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name
HAVING MAX(o.order_date) < CURRENT_DATE - INTERVAL '90 days';
```

**Alternative:** Include never-ordered customers with `LEFT JOIN` and `MAX(order_date) IS NULL OR ...`.

**Notes:** Index `(customer_id, order_date)` helps latest-order aggregation.

### P91. Top 10 percent customers by spend
`🔴 Advanced`

**Statement:** Return customers in the top 10 percent by paid spend.

```sql
WITH spend AS (
  SELECT customer_id, SUM(total_amount) AS total_spend
  FROM orders
  WHERE status = 'PAID'
  GROUP BY customer_id
), ranked AS (
  SELECT *, CUME_DIST() OVER (ORDER BY total_spend DESC) AS cd
  FROM spend
)
SELECT customer_id, total_spend
FROM ranked
WHERE cd <= 0.10;
```

**Alternative:** Use `NTILE(10) = 1`, but buckets are equal row counts and handle ties differently.

**Notes:** Sorting spend dominates; materialize spend for large datasets.

### P92. Pareto revenue share by customer
`🔴 Advanced`

**Statement:** Show each customer's cumulative share of paid revenue ordered by spend descending.

```sql
WITH spend AS (
  SELECT customer_id, SUM(total_amount) AS total_spend
  FROM orders
  WHERE status = 'PAID'
  GROUP BY customer_id
)
SELECT customer_id, total_spend,
       SUM(total_spend) OVER (ORDER BY total_spend DESC) /
       SUM(total_spend) OVER () AS cumulative_revenue_share
FROM spend
ORDER BY total_spend DESC;
```

**Alternative:** Add `ROW_NUMBER()` to identify how many customers reach 80 percent revenue.

**Notes:** Useful for whale/customer concentration analysis.

### P93. Sessionize events using 30-minute inactivity gap
`⚫ FAANG` `Stretch`

**Statement:** Assign a session number per user whenever the gap from previous event exceeds 30 minutes.

```sql
WITH marked AS (
  SELECT user_id, event_id, event_time,
         CASE WHEN event_time - LAG(event_time) OVER (PARTITION BY user_id ORDER BY event_time, event_id) > INTERVAL '30 minutes'
                   OR LAG(event_time) OVER (PARTITION BY user_id ORDER BY event_time, event_id) IS NULL
              THEN 1 ELSE 0 END AS new_session
  FROM events
), numbered AS (
  SELECT *, SUM(new_session) OVER (PARTITION BY user_id ORDER BY event_time, event_id) AS session_num
  FROM marked
)
SELECT user_id, event_id, event_time, session_num
FROM numbered;
```

**Alternative:** Compute `prev_event_time` in a CTE to avoid repeating `LAG`.

**Notes:** Index `(user_id, event_time, event_id)` is critical; MySQL uses `TIMESTAMPDIFF(MINUTE, prev, event_time)`.

### P94. Sessions with purchase after search
`🟡 Intermediate`

**Statement:** Return sessions where a `purchase` event occurred after a `search` event.

```sql
WITH s AS (
  SELECT session_id,
         MIN(event_time) FILTER (WHERE event_type = 'search') AS first_search,
         MIN(event_time) FILTER (WHERE event_type = 'purchase') AS first_purchase
  FROM events
  GROUP BY session_id
)
SELECT session_id
FROM s
WHERE first_search IS NOT NULL
  AND first_purchase > first_search;
```

**Alternative:** Self-join events by session and compare timestamps.

**Notes:** Conditional aggregation is compact; index `(session_id, event_type, event_time)` helps.

### P95. Customer average days between orders
`🟡 Intermediate`

**Statement:** Return average number of days between consecutive orders per customer.

```sql
WITH x AS (
  SELECT customer_id, order_date,
         LAG(order_date) OVER (PARTITION BY customer_id ORDER BY order_date, order_id) AS prev_order_date
  FROM orders
)
SELECT customer_id, AVG(order_date - prev_order_date) AS avg_days_between_orders
FROM x
WHERE prev_order_date IS NOT NULL
GROUP BY customer_id;
```

**Alternative:** MySQL uses `AVG(DATEDIFF(order_date, prev_order_date))`.

**Notes:** Index `(customer_id, order_date, order_id)` supports ordering.

### P96. Products frequently bought together
`🔴 Advanced` `🔥`

**Statement:** Return product pairs bought together in the same order and their co-purchase count.

```sql
SELECT oi1.product_id AS product_a,
       oi2.product_id AS product_b,
       COUNT(*) AS together_count
FROM order_items oi1
JOIN order_items oi2
  ON oi1.order_id = oi2.order_id
 AND oi1.product_id < oi2.product_id
GROUP BY oi1.product_id, oi2.product_id
ORDER BY together_count DESC;
```

**Alternative:** Join product names after computing pairs.

**Notes:** Pair generation can explode for large baskets; primary key `(order_id, product_id)` helps.

### P97. Rolling 7-day active users
`🔴 Advanced`

**Statement:** For each login date, count distinct users who logged in during that date and previous 6 days.

```sql
WITH dates AS (SELECT DISTINCT login_date FROM logins)
SELECT d.login_date,
       COUNT(DISTINCT l.user_id) AS wau_7d
FROM dates d
JOIN logins l
  ON l.login_date BETWEEN d.login_date - INTERVAL '6 days' AND d.login_date
GROUP BY d.login_date
ORDER BY d.login_date;
```

**Alternative:** For very large data, pre-aggregate or use approximate distinct structures outside standard SQL.

**Notes:** Index `(login_date, user_id)` helps range joins; exact rolling distinct is expensive.

### P98. Orders whose stored total differs from item total
`🟡 Intermediate` `🔥`

**Statement:** Return orders where `orders.total_amount` does not equal the sum of line items.

```sql
SELECT o.order_id, o.total_amount, SUM(oi.quantity * oi.unit_price) AS item_total
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
GROUP BY o.order_id, o.total_amount
HAVING o.total_amount <> SUM(oi.quantity * oi.unit_price);
```

**Alternative:** Use tolerance for decimals if calculations involve taxes/rounding.

**Notes:** Useful data integrity check matching the candidate's validation experience.

### P99. Customers whose every order is paid
`🟡 Intermediate`

**Statement:** Return customers with at least one order and no non-PAID orders.

```sql
SELECT customer_id
FROM orders
GROUP BY customer_id
HAVING COUNT(*) > 0
   AND COUNT(*) FILTER (WHERE status <> 'PAID' OR status IS NULL) = 0;
```

**Alternative:** `EXISTS` paid order plus `NOT EXISTS` non-paid order.

**Notes:** MySQL uses `SUM(CASE WHEN status <> 'PAID' OR status IS NULL THEN 1 ELSE 0 END) = 0`.

### P100. Customers who bought products from at least three categories in one month
`🔴 Advanced`

**Statement:** Return customers and months where they bought products from at least three distinct categories.

```sql
SELECT o.customer_id,
       date_trunc('month', o.order_date)::date AS month_start,
       COUNT(DISTINCT p.category) AS category_count
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
JOIN products p ON p.product_id = oi.product_id
WHERE o.status = 'PAID'
GROUP BY o.customer_id, date_trunc('month', o.order_date)::date
HAVING COUNT(DISTINCT p.category) >= 3
ORDER BY o.customer_id, month_start;
```

**Alternative:** Pre-aggregate distinct `(customer_id, month, category)` first, then count categories.

**Notes:** Index `orders(status, order_date, customer_id)` plus FK indexes helps. MySQL uses `DATE_FORMAT` for month.

## Revision Cheat Sheet

- P51-P60 cover Nth salary, top-N, duplicate deletion, dirty data checks, and anti-corruption queries.
- Gaps-and-islands key trick: `date - row_number()` creates a stable group for consecutive dates.
- Consecutive rows can be solved by self-join for fixed length or window/islands for variable length.
- Date analytics often need a date spine to include missing days.
- Recursive CTEs need anchor, recursive member, and cycle/depth protection.
- Pivot with conditional aggregation; unpivot with `UNION ALL` or `VALUES`.
- Median in PostgreSQL uses `percentile_cont`; portable median uses row numbers.
- Funnel and retention queries are conditional aggregation plus time-order constraints.
- Rolling distinct counts are expensive; index date/user columns and consider pre-aggregation.
- Reconciliation queries compare stored summaries with detail-table aggregates.

## Flashcards

- Q → Nth highest salary robustly? A → `DENSE_RANK() OVER (ORDER BY salary DESC)` and filter rank.
- Q → Three consecutive login days trick? A → Group by `login_date - row_number()` per user.
- Q → Rising temperature pattern? A → Join current row to previous date and compare temperature.
- Q → Date spine in PostgreSQL? A → `generate_series(min_date, max_date, interval '1 day')`.
- Q → Hierarchy query tool? A → Recursive CTE with anchor plus recursive join.
- Q → Static pivot pattern? A → Conditional aggregation with `FILTER` or `CASE`.
- Q → Unpivot pattern? A → `UNION ALL` each column into rows.
- Q → PostgreSQL median function? A → `percentile_cont(0.5) WITHIN GROUP`.
- Q → Sessionization boundary? A → New session when gap from previous event exceeds threshold.
- Q → Bought-together pairs? A → Self-join order items on same order with `product_id < product_id`.
- Q → Rolling WAU exact query cost? A → Expensive range join plus distinct count.
- Q → Integrity check for order totals? A → Compare header total with `SUM(quantity * unit_price)`.
