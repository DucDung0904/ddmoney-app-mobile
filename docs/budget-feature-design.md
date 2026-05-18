# Thiet ke tinh nang Ngan sach / Budget

## 1. Tong quan nghiep vu

Ngan sach la gioi han chi tieu user dat ra trong mot khoang thoi gian. Ngan sach khong tru tien trong vi, khong tao giao dich va khong lam thay doi balance. He thong chi dung ngan sach de so sanh voi cac giao dich EXPENSE thuc te.

Cong thuc chung:

```text
budgetAmount = so tien gioi han
spentAmount = tong tien giao dich EXPENSE phu hop
remainingAmount = budgetAmount - spentAmount
percentUsed = spentAmount / budgetAmount * 100
```

Trang thai:

```text
SAFE: percentUsed < 80
WARNING: 80 <= percentUsed < 100
EXCEEDED: percentUsed >= 100
```

## 2. Flow hoat dong

1. User vao tab Budget.
2. User bam Add Budget.
3. User nhap ten, amount, category/all categories, wallet/all wallets, period, repeat.
4. Backend validate so tien, category, wallet, ngay, duplicate.
5. Backend luu budget.
6. Backend tinh `spentAmount` tu transactions hien co trong khoang ngay.
7. Mobile hien card budget voi progress/status.
8. Khi them transaction EXPENSE moi, danh sach budget se tinh lai theo query tong chi.
9. Backend co the tra them `budgetWarnings` trong response transaction neu giao dich lam budget dat WARNING/EXCEEDED.

## 3. Rule nghiep vu

- Amount phai lon hon 0.
- Khong cho tao budget voi category INCOME.
- Chi category EXPENSE moi duoc gan vao budget.
- `scope = ALL_CATEGORIES` thi `category_id = NULL`.
- `scope = CATEGORY` thi `category_id` bat buoc co.
- `wallet_scope = ALL_WALLETS` thi `wallet_id = NULL`.
- `wallet_scope = ONE_WALLET` thi `wallet_id` bat buoc co.
- Wallet phai thuoc user hien tai.
- Wallet phai active va khong archive.
- `start_date <= end_date`.
- `period_type = CUSTOM` khong nen repeat.
- Repeat chi ap dung cho WEEK, MONTH, QUARTER, YEAR.
- Khong tao trung budget cung user, category, wallet va khoang thoi gian.
- User chi duoc xem/sua/xoa budget cua minh.

## 4. Database design

Bang `budgets` moi:

```sql
id, user_id, name, amount,
category_id, wallet_id,
scope, wallet_scope,
period_type, repeat_type,
start_date, end_date,
active, created_at, updated_at
```

Y nghia:

- `category_id = NULL`: budget cho tat ca danh muc chi tieu.
- `wallet_id = NULL`: budget cho tat ca vi dang hoat dong.
- `scope`: `CATEGORY` hoac `ALL_CATEGORIES`.
- `wallet_scope`: `ONE_WALLET` hoac `ALL_WALLETS`.
- `active = false`: soft delete budget.

Index quan trong:

```sql
idx_budgets_user_active(user_id, active)
idx_budgets_period(user_id, start_date, end_date)
idx_transactions_budget_calc(user_id, type, date, category_id, wallet_id)
uk_budget_identity(user_id, category_key, wallet_key, start_date, end_date, active)
```

## 5. API design

### POST /api/budgets

Request:

```json
{
  "name": "An uong thang 5",
  "amount": 2000000,
  "categoryId": 1,
  "walletId": 2,
  "scope": "CATEGORY",
  "walletScope": "ONE_WALLET",
  "periodType": "MONTH",
  "repeatType": "MONTHLY",
  "startDate": "2026-05-01",
  "endDate": "2026-05-31"
}
```

Response: `BudgetResponse`.

Loi thuong gap:

- `So tien ngan sach phai lon hon 0`
- `Chi duoc tao ngan sach cho danh muc chi tieu`
- `Vi phai dang hoat dong va chua bi luu tru`
- `Ngan sach cung danh muc, vi va khoang thoi gian da ton tai`

### GET /api/budgets

Query optional: `month`, `year`. Tra ve danh sach budget overlap voi thang duoc chon.

### GET /api/budgets/current

Tra ve cac budget dang active va ngay hien tai nam trong `startDate/endDate`.

### GET /api/budgets/{id}

Tra ve `BudgetDetailResponse`: thong tin budget, chi tieu theo category, transactions va thong ke ngay.

### PUT /api/budgets/{id}

Request giong create. Backend validate ownership va duplicate voi `excludeId`.

### DELETE /api/budgets/{id}

Soft delete bang cach set `active = false`.

## 6. DTO design

`CreateBudgetRequest` va `UpdateBudgetRequest` co cac truong:

```text
name, amount, categoryId, walletId,
scope, walletScope, periodType, repeatType,
startDate, endDate
```

`BudgetResponse` tra ve:

```text
id, name, amount, spentAmount, remainingAmount,
percentUsed, status, categoryName, walletName,
startDate, endDate, repeatType
```

`BudgetDetailResponse` bo sung:

```text
dailySuggestedAmount, projectedSpending,
actualDailySpending, spentByCategory, transactions
```

`BudgetSummaryResponse` dung cho man hinh tong hop:

```text
totalBudgetAmount, totalSpentAmount, totalRemainingAmount,
percentUsed, status, count, startDate, endDate
```

`TransactionResponse` co the bo sung:

```text
budgetWarnings: List<BudgetResponse>
```

Field nay dung de mobile hien canh bao ngay sau khi user them/sua giao dich chi tieu.

## 7. Service logic

Logic da duoc map trong `BudgetService`:

- `createBudget()`: validate request, resolve category/wallet/period, check duplicate, save, return response co spent.
- `updateBudget()`: chi cho sua budget cua user, validate lai toan bo, check duplicate tru budget hien tai.
- `deleteBudget()`: soft delete.
- `getBudgetDetail()`: lay budget, tinh spent, tinh daily suggestion, group spending, list transaction.
- `calculateSpentAmount()`: tong EXPENSE theo user, date range, category scope va wallet scope.
- `determineBudgetStatus()`: SAFE/WARNING/EXCEEDED theo percent.
- `findImpactedBudgetWarnings()`: loc cac budget bi anh huong boi transaction EXPENSE va tra ve budget da cham/vuot nguong.

## 8. Query mau

Kiem tra trung budget:

```sql
SELECT *
FROM budgets
WHERE user_id = :userId
  AND active = true
  AND COALESCE(category_id, -1) = COALESCE(:categoryId, -1)
  AND COALESCE(wallet_id, -1) = COALESCE(:walletId, -1)
  AND start_date <= :endDate
  AND end_date >= :startDate;
```

Tinh spent:

```sql
SELECT COALESCE(SUM(amount), 0)
FROM transactions
WHERE user_id = :userId
  AND type = 'EXPENSE'
  AND date BETWEEN :startDate AND :endDate
  AND (:categoryId IS NULL OR category_id = :categoryId)
  AND (:walletId IS NULL OR wallet_id = :walletId);
```

Group theo category:

```sql
SELECT category_id, SUM(amount)
FROM transactions
WHERE user_id = :userId
  AND type = 'EXPENSE'
  AND date BETWEEN :startDate AND :endDate
GROUP BY category_id;
```

## 9. Repeat budget

Hien tai co the luu repeat ngay trong `budgets` va tinh ky hien tai tu `period_type/repeat_type/start_date`.

Khi can mo rong chuyen nghiep hon, tach:

- `budget_templates`: cau hinh lap lai, category, wallet, amount.
- `budget_periods`: tung ky cu the, start/end, amount snapshot.

Cach tach nay tot hon khi can lich su budget tung thang, report theo ky va thay doi amount tu thang sau ma khong anh huong thang cu.

## 10. UI/UX goi y

Danh sach budget:

- Empty state khi chua co ngan sach.
- Card gom ten, amount, da chi, con lai, progress bar, status chip.
- Tap card mo detail.
- CTA Add Budget ro rang.

Them/sua budget:

- Chon category hoac all categories.
- Chon wallet hoac all wallets.
- Nhap amount.
- Chon period: week/month/quarter/year/custom.
- Repeat chi enable neu period khong phai custom.
- Save disabled neu thieu amount/category/wallet/ngay.

Detail budget:

- Header co back/edit.
- Summary card: amount, spent, remaining, progress.
- Chart nho theo khoang ngay.
- Spent by category.
- Transaction list.
- Delete button o cuoi man hinh.

## 11. Vi du thuc te

Budget:

```text
Name: An uong thang 5
Amount: 2,000,000
Category: An uong
Wallet: Tien mat
Period: 2026-05-01 -> 2026-05-31
```

Transactions:

```text
100,000 an sang
500,000 di sieu thi
1,600,000 an nha hang
```

Ket qua:

```text
spentAmount = 2,200,000
remainingAmount = -200,000
percentUsed = 110%
status = EXCEEDED
```

## 12. Loi can tranh

- Tru tien truc tiep vao wallet khi tao budget.
- Tinh ca transaction INCOME vao budget.
- Cho category INCOME tao budget.
- Bo qua wallet archived trong all wallets.
- Duplicate budget cung scope/time lam progress bi nhan doi.
- Dung month/year co dinh cho moi loai period.
- Delete hard neu can giu lich su report.
- Frontend tu tinh spent khac backend, dan den lech so lieu.
