# 06c · SQL 100 Practice Questions, Part 2

Part 2 contains P51-P100 of exactly 100 SQL coding problems, continuing the schemas defined in `06b_SQL_100_Practice_Questions.md`. Syntax is PostgreSQL by default; MySQL differences are noted.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Beginner guide to this file](#beginner-guide-to-this-file)
- [Top-N, Nth Highest, and Duplicates](#top-n-nth-highest-and-duplicates)
- [Running Totals, Moving Averages, Gaps and Islands](#running-totals-moving-averages-gaps-and-islands)
- [Date and Time Analytics](#date-and-time-analytics)
- [Recursive CTE, Pivot, and Advanced Analytics](#recursive-cte-pivot-and-advanced-analytics)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Beginner guide to this file

### What this file teaches

This file teaches the second half of the SQL practice set: ranking, duplicate cleanup, gaps and islands,
date analytics, recursive Common Table Expressions (CTEs), pivots, funnels, retention, and revenue analytics.
Interviewers use these problems to test whether you can reason about time, ties, dirty data, and multi-step
analysis without losing the correct output grain. These patterns appear in backend reporting jobs, product
analytics, fraud checks, data repair scripts, and customer lifecycle dashboards.

### Mental model

Think of this file as moving from "find rows" to "tell a story over rows": order events, compare neighbors,
collapse repeated behavior into islands, and summarize user or revenue journeys.

- Ranking: choose ROW_NUMBER, RANK, DENSE_RANK, CUME_DIST, or NTILE based on tie behavior.
- Time windows: define whether the window means rows, calendar days, months, or inactivity gaps.
- Islands: convert consecutive rows into stable group keys, then aggregate each island.
- Recursive traversal: anchor the first level, then repeatedly join to the next level.
- Pivoting: use conditional aggregation to turn row values into columns, or UNION ALL to unpivot columns.
- Analytics grain: decide whether the result is per user, per session, per cohort, per date, or one summary row.

### How to think through any question here

1. Name the event or entity grain first: salary value, login day, session, customer-month, or hierarchy node.
2. Decide whether ties should be kept, collapsed, ranked with gaps, or forced into exactly N rows.
3. Put row-level restrictions such as paid status, weekend date, or event type in WHERE before aggregation.
4. Use GROUP BY and HAVING when the condition depends on counts, sums, medians, or distinct categories.
5. Use window functions when the query needs previous rows, running totals, rolling averages, or top-N per group.
6. Use CTEs to separate stages: aggregate first, rank second, filter last.
7. Check edge cases: nulls, duplicate dirty rows, missing dates, empty groups, ties, cycles, and boundary days.
8. Explain how the query would change for production scale, such as indexes, calendar tables, and deduplication.

### Pattern recognition cues

| If the question mentions... | Think about... | Why it fits |
| --- | --- | --- |
| "Nth", "top N", or "latest two" | DENSE_RANK or ROW_NUMBER | The query orders rows inside a group and filters by rank. |
| "duplicate" or "dirty import" | GROUP BY HAVING count or ROW_NUMBER delete | You must detect or keep one row per duplicate key. |
| "consecutive" or "streak" | Gaps-and-islands | Consecutive dates or ids become island groups. |
| "running", "rolling", or "previous" | Window frame or LAG | The output keeps row detail while looking around the row. |
| "retention" or "cohort" | Date offsets and COUNT DISTINCT | Users are counted by signup or activity age. |
| "hierarchy" or "manager chain" | Recursive CTE | Each result level depends on rows found in the previous level. |
| "pivot" | Conditional aggregation | Values such as status or category become columns. |
| "unpivot" | UNION ALL | Several columns are stacked into one row set. |
| "funnel" or "attribution" | First event per user and order checks | Event order matters more than raw event count. |
| "stored total differs" | Aggregate detail rows and HAVING | The header row is validated against child-row totals. |

### Abbreviations and key terms

| Short form | Expanded form | Plain meaning |
| --- | --- | --- |
| CTE | Common Table Expression | A named temporary result introduced with WITH for one query. |
| D1 | Day 1 | The day after signup or first activity, often used in retention. |
| D7 | Day 7 | Seven days after signup or first activity, often used in retention. |
| DESC | Descending | Sort from high to low or newest to oldest. |
| FAANG | Facebook, Apple, Amazon, Netflix, Google | Shorthand for large product-company interview style. |
| ID | Identifier | A value that uniquely identifies an entity row or event row. |
| MAU | Monthly Active Users | Distinct users active during a calendar month. |
| MySQL | MySQL database system | A relational database that uses SQL with some syntax differences from PostgreSQL. |
| N | Number parameter | A placeholder for how many ranks or rows the query should keep. |
| NULL | Null marker | A missing or unknown value, not the same as zero or an empty string. |
| P51-P100 | Practice problems 51 through 100 | The second half of this 100-problem SQL practice set. |
| Q1-Q4 | Quarter 1 through Quarter 4 | Four three-month periods in a year, often stored as quarterly columns. |
| SQL | Structured Query Language | The language used to query and change relational database data. |
| UTM | Urchin Tracking Module | Marketing source tags used to attribute acquisition events. |
| WAU | Weekly Active Users | Distinct users active during a seven-day window. |

### Common beginner traps

- Using ROW_NUMBER when the requirement says include ties, or DENSE_RANK when exactly N rows are required.
- Treating a rolling row window as a rolling calendar-day window.
- Forgetting to deduplicate dirty daily events before streak, retention, or funnel calculations.
- Filtering a window-function alias in the same SELECT level instead of using a CTE or subquery.
- Counting joined rows after multiple left joins and accidentally multiplying users.
- Writing recursive CTEs without a clear anchor, stop condition, or cycle strategy.
- Assuming SUM returns zero for missing groups; it usually returns null unless COALESCE is used.

### How to study this file

1. For each problem, state whether it is ranking, deduplication, islanding, date analytics, recursion, or pivoting.
2. Before reading the SQL, choose the expected grain and the stage where filtering should happen.
3. Use the short query to identify the core pattern, then read the notes for tie, null, and duplicate behavior.
4. Turn each advanced query into a spoken walkthrough: CTE one does this, CTE two does that, final filter does this.
5. Re-solve selected problems with a twist, such as including zero days, requiring exact top N, or handling ties.
6. Keep flashcards for the function choice: LAG versus LEAD, ROW_NUMBER versus DENSE_RANK, GROUP BY versus window.

### Scalability and production lens

Advanced analytics queries often scan many rows, sort within partitions, and count distinct users. Production
systems usually need indexes on join keys and event dates, pre-aggregated daily tables, calendar tables for
missing dates, and clear deduplication rules before retention or funnel logic. Recursive CTEs need cycle guards,
maximum depth expectations, and tests on malformed hierarchies. Cleanup queries should be reviewed in a
transaction with backups or rollback plans.

## Top-N, Nth Highest, and Duplicates

### P51. Nth highest distinct salary
`🟡 Intermediate` `🔥`

**Statement:** Return the Nth highest distinct salary. Assume `:n` is a positive parameter.

**How to think:** Grain is one salary value for the requested Nth distinct rank. The employees table is
enough, and DISTINCT removes duplicate salaries before DENSE_RANK orders them descending. The outer WHERE
filters the computed rank, so no GROUP BY is needed. If :n is larger than the number of distinct salaries,
the query returns no row. Consider filtering null salaries if the interview expects only real salaries.

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

**How to think:** Grain is exactly one output row containing the second highest distinct salary or null. The
inner query keeps the top two distinct salaries, the next ORDER BY returns the smaller of those two, and the
scalar subquery makes a missing result become NULL. No join or HAVING is involved. Duplicate salaries are
removed. If nullable salaries exist, clarify whether they should be excluded before ordering.

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

**How to think:** Grain is one product per category and revenue rank that falls within top N. Paid orders,
order_items, and products are all required because status, quantity, price, and category live in different
tables. WHERE keeps paid rows before the revenue GROUP BY. DENSE_RANK in the second CTE includes ties, so a
category can return more than N products. Unsold products are excluded.

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

**How to think:** Grain is one login row, up to two rows per user. The logins table has the needed user_id
and login_date, and ROW_NUMBER partitions by user while ordering newest first. The outer WHERE keeps rn less
than or equal to two. No aggregation is needed. Duplicate login dates would need a tie-breaker, but the
sample schema's primary key prevents same-user duplicate days.

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

**How to think:** Grain is one user_id with exactly one login record. GROUP BY user_id creates the user
grain, and HAVING COUNT star equals one filters groups after counting. The users table is not joined, so
users with zero logins are omitted rather than returned. If a dirty table can contain duplicate same-day
rows, this counts records, not distinct login days.

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

**How to think:** Grain is one duplicate user_id and login_date pair. The logins table is grouped by both
columns, and HAVING COUNT star greater than one keeps pairs that appear multiple times. This is aggregate
duplicate detection, not a row cleanup query. Null user or date values, if allowed in a dirty import, would
group together. Add row ids when you need to inspect individual duplicates.

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

**How to think:** Grain is the dirty login rows to delete. The CTE assigns ROW_NUMBER within each user_id
and login_date partition ordered by id, so rn equals one is the kept row and rn greater than one is
duplicate. DELETE USING joins those ranked ids back to the table. Null keys partition together. In
production, preview first and run in a transaction.

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

**How to think:** Grain is one possible duplicate order signature: customer, date, and total amount. The
orders table is grouped by those three values, and HAVING COUNT star greater than one reports repeated
signatures. This flags suspicious duplicates but does not prove they are bad business records. Null
customer, date, or amount values can be grouped together, so inspect order ids before cleanup.

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

**How to think:** Grain is one missing employee_id range between two existing adjacent ids. LEAD looks at
the next id in sorted order, and WHERE keeps pairs where the next id is more than one greater. No join or
GROUP BY is required. This finds internal gaps only; it does not invent gaps before the first id or after
the last id. Primary keys prevent duplicate ids.

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

**How to think:** Grain is one employee whose non-null manager_id points nowhere. This is a self LEFT JOIN
from employee to manager, then WHERE keeps rows where the manager id was provided but no manager row
matched. That is an orphan-reference anti-join. Top-level employees with null manager_id are valid and
excluded. This finds missing managers, not cycles or invalid hierarchy depth.

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

**How to think:** Grain is one user who has at least one streak of three or more consecutive login days. The
row_number date-island trick creates the same grp value for consecutive dates, then GROUP BY user_id and grp
counts streak length. HAVING days greater than or equal to three filters islands. This assumes one login row
per user per day; duplicate dirty rows can inflate or break streaks.

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

**How to think:** Grain is one number value that appears across at least three consecutive ids. The numbers
table self-joins three roles at id, id plus one, and id plus two with equal num values. DISTINCT removes
repeated windows for longer runs. This checks id adjacency, not merely neighboring rows after sorting. Null
num values will not match with equals, and missing ids break the chain.

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

**How to think:** Grain is one stadium row that belongs to a high-traffic island of at least three
consecutive ids. WHERE first filters rows with people at least 100, then id minus row_number groups
consecutive retained ids. HAVING count at least three finds qualifying islands, and the join returns all
rows in them. Missing ids break islands, and rows below 100 reset the streak.

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

**How to think:** Grain is one weather row id where today's temperature is higher than the previous calendar
day's. A self join connects each row w to row p whose record_date is one day earlier, and WHERE compares
temperatures. The inner join means first days or gaps with no prior date are omitted. Equal temperatures do
not qualify. Date arithmetic syntax differs across databases.

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

**How to think:** Grain is one login streak island per user with start, end, and length. The logins table is
transformed by row_number into groups where consecutive dates share the same date-minus-row-number value.
GROUP BY user_id and grp then produces island summaries. This needs daily grain; duplicate dates or
timestamps should be deduplicated to one login day before applying the island logic.

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

**How to think:** Grain is one user with the length of their longest consecutive login streak. The first CTE
builds date islands per user, the second counts days per island, and the outer GROUP BY takes MAX
streak_days per user. Users with no logins are absent. Because only the length is returned, ties between
multiple longest streaks do not matter. Dirty duplicate dates can distort counts.

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

**How to think:** Grain is one first transaction per account where running balance reaches at least 10000.
The balances CTE uses a signed running SUM by account ordered by transaction date and id. The ranked CTE
filters balances above the threshold, then ROW_NUMBER chooses the earliest crossing row. Accounts that never
cross are absent. The CASE treats every non-DEPOSIT as negative, so unknown transaction types need care.

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

**How to think:** Grain is one transaction with the rolling average amount over that transaction and the
previous two transactions for the same account. AVG is a window function with a ROWS frame, so it counts
rows, not calendar days. No GROUP BY is used because detail rows remain. The first two transactions average
fewer than three rows, and null amounts are ignored by AVG.

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

**How to think:** Grain is one login date with active users, retained users, and retention percentage. The
daily CTE provides login user rows, retained joins each day to the next day's login for the same user, and
the final LEFT JOIN keeps days with zero retention. GROUP BY is by login date. Duplicate login rows can
overcount retained_users because retained uses COUNT star instead of COUNT DISTINCT.

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

**How to think:** Grain is one signup cohort month and month_age pair. Users join logins because the output
counts active users from each cohort at each age, and COUNT DISTINCT prevents multiple logins by the same
user from inflating activity. GROUP BY the computed cohort and age. Never-active users are absent due to the
inner join. Logins before signup would create negative ages unless filtered.

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

**How to think:** Grain is a single row with two revenue columns. The orders table is filtered to PAID rows
in WHERE, and conditional SUM FILTER expressions split those rows into current-month and previous-month
buckets. No GROUP BY is needed. If a month has no paid orders, the SUM returns null rather than zero.
CURRENT_DATE and date_trunc follow the database session's calendar and timezone rules.

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

**How to think:** Grain is one weekend order. The orders table has all requested columns, and WHERE uses
EXTRACT ISODOW to keep Saturday and Sunday values. There is no join, aggregation, or window function. Null
order_date values fail the filter. Be ready to translate the date function for MySQL or other systems,
because weekday numbering is vendor-specific.

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

**How to think:** Grain is one customer whose first order is within seven days after account creation. The
first_order CTE groups orders by customer_id to find MIN order_date, then joins customers and filters by
created_at plus interval. Customers without orders are omitted. The query uses a strict less-than seven
days, so an order exactly seven days later does not qualify unless the requirement says inclusive.

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

**How to think:** Grain is one month with a distinct count of active login users. The logins table is
grouped by date_trunc month, and COUNT DISTINCT user_id prevents multiple login days from inflating monthly
active users. Missing months are omitted, not shown as zero. If login_date is a timestamp, timezone and
month boundary choices matter. HAVING would only be needed for a threshold on active users.

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

**How to think:** Grain is one hour-of-day bucket with event count. The events table is grouped by EXTRACT
hour from event_time, and COUNT star counts events in that bucket. No join is required. Hours with no events
do not appear unless you join to a generated 0-through-23 table. Null event_time values can form a null hour
group depending on database behavior.

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

**How to think:** Grain is one user who signed up but has no login row. Start from users and LEFT JOIN
logins, then keep rows where the login side is null. That null check in WHERE creates the anti-join.
Duplicate login rows do not matter because any match removes the user. A login row with null login_date
still counts as activity because the row exists.

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

**How to think:** Grain is one calendar date between the minimum and maximum order dates. Bounds finds the
range, generate_series creates all dates, daily aggregates paid order revenue, and the LEFT JOIN fills
missing paid-revenue days with zero via COALESCE. The paid filter belongs in the daily CTE's WHERE. If the
orders table is empty, min and max are null and no date rows are generated.

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

**How to think:** Grain is one session that has both a view and a purchase with purchase not before view.
The CTE groups events by session_id and uses filtered MIN values to find first_view and first_purchase. The
final WHERE removes sessions missing either event and enforces order. Multiple views or purchases are
reduced to first occurrences, so clarify if the question wants the purchase after the first view or after
any view.

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

**How to think:** Grain is one employee_tree row reachable from a root, with a hierarchy level. The
recursive CTE anchor selects roots where manager_id is null, then repeatedly joins children whose manager_id
equals the current id. UNION ALL preserves the traversal rows. No aggregation is needed. Orphan nodes are
not reached, and real recursive queries should guard against cycles and excessive depth.

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

**How to think:** Grain is one node in the manager chain from the requested employee up to the root. The
recursive CTE starts at :employee_id, then joins from the current row's manager_id to the manager row.
distance counts hops upward. If the employee id is missing, no rows return; if a manager reference is
broken, the chain stops early. Cycles require a visited-node guard.

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

**How to think:** Grain is one manager_id with the count of all direct and indirect subordinates. The
recursive CTE starts with direct manager-subordinate edges, then repeatedly follows subordinates downward.
GROUP BY the original manager_id and COUNT DISTINCT subordinate_id avoid double-counting if paths repeat.
Managers with no subordinates are absent. Cycles in employee_tree can recurse indefinitely unless the
production query prevents revisits.

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

**How to think:** Grain is one month with separate revenue columns for selected order statuses. The orders
table is grouped by month, and FILTER inside SUM performs the pivot without changing row grain. There is no
join. Months with no rows for a status produce null for that column, not zero. Status values outside PAID,
REFUNDED, and PENDING are intentionally ignored.

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

**How to think:** Grain is one order_id with category-specific quantity columns. order_items joins products
because category lives on products while quantities live on items. GROUP BY order_id defines the grain, and
conditional SUM FILTER expressions pivot categories into columns. Orders with no items are absent.
Categories outside Books, Electronics, and Clothing are ignored; missing selected categories return null
quantities.

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

**How to think:** Grain is one product_id and quarter row from a wide quarterly_sales record. UNION ALL
stacks q1, q2, q3, and q4 columns into rows without de-duplicating equal revenues. No join, WHERE, or GROUP
BY is needed. Null quarter values are still returned unless filtered. This is unpivoting: making columns
into rows so later grouping and filtering are easier.

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

**How to think:** Grain is a single median salary value. percentile_cont is an ordered-set aggregate over
employees ordered by salary, so no GROUP BY or join is needed. The continuous median interpolates for an
even number of salaries, often averaging the two middle values. Null salaries are ignored by the aggregate.
Some databases need a window-function workaround instead of percentile_cont.

```sql
SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY salary) AS median_salary
FROM employees;
```

**Alternative:** MySQL 8 lacks `percentile_cont` in many setups; use row numbers and average middle rows.

**Notes:** Ordered-set aggregate sorts salaries; index may help but DB may still sort.

### P86. Median salary per department
`🔴 Advanced`

**Statement:** Return median salary for each department.

**How to think:** Grain is one department_id with its median salary. GROUP BY department_id creates the
department grain, and percentile_cont computes the median within each group. No join is needed because
department names are not requested. Null department_id becomes its own group, and null salaries are ignored.
Departments with no employees are absent because the query starts from employees.

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

**How to think:** Grain is a single funnel summary row across users. The per_user CTE reduces events to each
user's first view, add_to_cart, and purchase timestamps, then the outer SELECT conditionally counts users at
each step. The ordering checks are row-level comparisons after aggregation. Same-timestamp events fail
strict greater-than, users without earlier steps are excluded from later counts, and duplicate events
collapse to first timestamps.

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

**How to think:** Grain is one user with their first acquisition source. Filter events to event_type values
beginning with utm:, strip the prefix, then ROW_NUMBER by event_time and event_id chooses the first source
per user. WHERE rn equals one filters after the window. Users with no UTM event are absent. event_id is
important because simultaneous events need deterministic tie-breaking.

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

**How to think:** Grain is one signup_date cohort with D1 and D7 retained counts. Users are left joined to
logins exactly one and seven days after signup, preserving users who did not return. GROUP BY signup_date
counts cohort size and retained subsets. Duplicate login rows can multiply joined rows and inflate counts,
so dirty data should use COUNT DISTINCT user_id or pre-deduplicate logins.

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

**How to think:** Grain is one customer whose latest order is older than 90 days. Customers inner join
orders, GROUP BY customer, and HAVING MAX order_date before the threshold filters after aggregation.
Customers with no orders are omitted; decide separately whether they count as churned. Null order_date
values are ignored by MAX, and CURRENT_DATE makes the result change over time.

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

**How to think:** Grain is one spending customer in the top ten percent by paid spend. The spend CTE groups
paid orders by customer, then CUME_DIST orders those spend rows descending and measures relative position
including ties. The outer WHERE keeps cd up to 0.10. Non-spenders are excluded. Boundary behavior with ties
can include more or fewer rows than a simple LIMIT.

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

**How to think:** Grain is one spending customer with their cumulative share of paid revenue. The CTE
aggregates paid spend per customer, then window SUM over descending spend creates the running numerator and
SUM over all rows creates the denominator. No join is needed. Ties should use a deterministic tiebreaker if
ordering matters. If total revenue is zero, protect against division by zero.

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

**How to think:** Grain is one event with an assigned session number for that user. The marked CTE uses LAG
to compare each event_time to the previous event and flags a new session after a 30-minute gap or at the
first event. The numbered CTE uses a running SUM of flags. event_time and event_id provide deterministic
order; null timestamps or clock skew need production rules.

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

**How to think:** Grain is one session_id where a purchase happened after a search. The CTE groups events by
session and finds first_search and first_purchase via filtered MIN. The final WHERE requires a search and a
purchase time greater than it. Sessions with purchase before search, no search, or no purchase are excluded.
Multiple searches or purchases are simplified to first occurrences.

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

**How to think:** Grain is one customer with their average day gap between consecutive orders. LAG finds the
previous order date per customer ordered by date and order_id, WHERE removes each customer's first order,
and GROUP BY customer averages the date differences. Customers with one order are absent. Same-day orders
contribute zero days, and order_id makes same-date ordering deterministic.

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

**How to think:** Grain is one unordered product pair with the count of orders containing both products.
order_items self-joins within the same order, and product_id less-than removes self-pairs and reverse
duplicates. GROUP BY the pair counts co-purchases. Quantities are ignored, which is correct for basket
co-occurrence. Dirty duplicate item rows can inflate together_count unless order-product pairs are
deduplicated first.

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

**How to think:** Grain is one login date with distinct users active in that date's trailing seven-day
window. The dates CTE provides anchor dates from observed logins, then the join includes login rows between
anchor minus six days and anchor. COUNT DISTINCT handles repeated users. Missing calendar dates are omitted.
On large tables, this range join can be expensive without date indexes or pre-aggregated daily activity.

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

**How to think:** Grain is one order whose stored total differs from its item total. Orders join
order_items, GROUP BY order, and HAVING compares orders.total_amount to SUM quantity times unit_price. The
aggregate comparison belongs in HAVING. Orders with no items are omitted by the inner join. Real money
systems need rounding tolerance and careful handling of null quantities or prices.

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

**How to think:** Grain is one customer_id whose existing orders are all paid and who has at least one
order. Group orders by customer_id, then HAVING verifies there is at least one row and zero rows with status
not PAID or null. The null check matters because SQL status not equal PAID would otherwise ignore null.
Customers with no orders are absent because the query starts from orders.

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

**How to think:** Grain is one customer and month where paid purchases span at least three distinct product
categories. Orders join order_items and products because customer/month, item product, and category live in
different places. WHERE keeps paid orders before grouping. HAVING COUNT DISTINCT category at least three
applies after aggregation. Duplicate items in the same category do not inflate the count; null categories
are ignored by COUNT DISTINCT.

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
