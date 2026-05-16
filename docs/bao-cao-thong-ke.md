# Bao cao thong ke chi tieu DDMoney

Tai lieu nay tong hop cach chuc nang bao cao thong ke chi tieu dang duoc xay dung trong app DDMoney: nguon du lieu, cong thuc tinh, UI hien thi, animation va thu vien lien quan.

## 1. Tong quan chuc nang

Bao cao chi tieu giup nguoi dung xem nhanh:

- Tong tien da chi trong tuan nay hoac thang nay.
- Muc tang/giam so voi ky truoc.
- Bieu do cot so sanh ky truoc va ky hien tai.
- Danh muc chi tieu nhieu nhat.
- Bang phan ra chi tieu theo danh muc bang donut chart.

Chuc nang hien co hai diem hien thi chinh:

- Man hinh tong quan: `HomeReportSection`.
- Man hinh bao cao rieng: `AnalyticsScreen`.

Khi nguoi dung bam vao khu vuc "Chi tieu nhieu nhat", app mo `ExpenseBreakdownDialog` dang bottom sheet de hien thi donut chart va danh sach chi tiet tung danh muc.

## 2. Nguon du lieu

Bao cao duoc tinh truc tiep tu danh sach `Transaction` dang co trong state cua app. Phan bao cao chi lay cac giao dich co:

```kotlin
transaction.type == TransactionType.EXPENSE
```

Nhung giao dich thu nhap hoac loai khac khong duoc tinh vao bao cao chi tieu.

Moi giao dich chi tieu dung cac truong chinh:

- `amount`: so tien giao dich.
- `date`: ngay giao dich, dung de xep vao tuan/thang.
- `categoryId`: khoa nhom danh muc.
- `categoryName`: ten danh muc hien thi.
- `categoryIcon`: icon danh muc.
- `categoryColor`: mau dung cho chart va danh sach.
- `title`: ten du phong neu danh muc rong.

Neu `categoryId` rong, logic se fallback sang `categoryName`, sau do den `"unknown"`. Neu `categoryName` rong, ten hien thi fallback sang `title`, sau do den `"Khac"`.

## 3. Cach tinh ky bao cao

Ky bao cao duoc dinh nghia trong `ReportPeriod`:

```kotlin
enum class ReportPeriod(val label: String) {
    WEEK("Tuan"),
    MONTH("Thang")
}
```

### Tuan

- Ky hien tai: tu thu Hai den Chu nhat cua tuan hien tai.
- Ky truoc: tu thu Hai den Chu nhat cua tuan lien truoc.
- Cach tinh ngay bat dau tuan hien tai dung:

```kotlin
today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
```

### Thang

- Ky hien tai: tu ngay dau thang den ngay cuoi thang hien tai.
- Ky truoc: tu ngay dau thang den ngay cuoi thang lien truoc.
- Cach tinh:

```kotlin
val start = today.withDayOfMonth(1)
val end = start.plusMonths(1).minusDays(1)
```

## 4. Model va ham tinh chinh

Phan tinh toan nam trong `ReportModels.kt`.

### `buildExpenseReport`

Ham trung tam de tao bao cao:

```kotlin
fun buildExpenseReport(
    transactions: List<Transaction>,
    period: ReportPeriod,
    today: LocalDate = LocalDate.now()
): ExpenseReport
```

Ham nay thuc hien cac buoc:

1. Xac dinh khoang ngay cua ky hien tai va ky truoc.
2. Loc giao dich chi tieu bang `TransactionType.EXPENSE`.
3. Loc giao dich theo khoang ngay.
4. Tinh `currentTotal` va `previousTotal`.
5. Nhom giao dich ky hien tai theo danh muc.
6. Tinh tong tien va ti le tung danh muc.
7. Sap xep danh muc theo so tien giam dan.
8. Tra ve `ExpenseReport`.

### `ExpenseReport`

`ExpenseReport` gom cac thong tin:

- `currentTotal`: tong chi ky hien tai.
- `previousTotal`: tong chi ky truoc.
- `transactionCount`: so giao dich chi tieu trong ky hien tai.
- `currentLabel`: "Tuan nay" hoac "Thang nay".
- `previousLabel`: "Tuan truoc" hoac "Thang truoc".
- `rangeLabel`: nhan khoang thoi gian hien thi.
- `summaryLabel`: nhan mo ta tong chi.
- `topCategories`: danh sach danh muc da nhom va sap xep.

Hai gia tri phu:

```kotlin
val difference: Double
    get() = currentTotal - previousTotal
```

`difference` duoc dung de xac dinh tang/giam chi tieu:

- Lon hon `0`: chi nhieu hon ky truoc, hien mau do.
- Nho hon `0`: chi it hon ky truoc, hien mau xanh.
- Bang `0`: khong thay doi, hien mau trung tinh.

```kotlin
val differencePercentage: Float
```

Gia tri nay tinh phan tram thay doi khi `previousTotal > 0`.

### `formatVnd`

Dinh dang so tien day du theo tien Viet Nam:

```kotlin
fun formatVnd(amount: Double): String
```

Vi du:

- `10442000.0` thanh `10.442.000 đ`.
- `1442000.0` thanh `1.442.000 đ`.

Ham dung `NumberFormat.getNumberInstance(Locale("vi", "VN"))`.

### `compactMoney`

Dinh dang so tien ngan gon cho chart va badge:

```kotlin
fun compactMoney(amount: Double): String
```

Vi du:

- `10_400_000` thanh `10.4M`.
- `3_000_000` thanh `3M`.
- `800_000` thanh `800K`.

### `comparisonChartAxisMax`

Tinh moc cao nhat cho bieu do cot:

```kotlin
fun comparisonChartAxisMax(vararg amounts: Double): Double
```

Hien tai bieu do chi so sanh hai cot dang hien thi. Vi du:

- Tab Tuan: so sanh `tuan truoc` va `tuan nay`.
- Tab Thang: so sanh `thang truoc` va `thang nay`.

Ham lay gia tri lon nhat trong cac so duoc truyen vao, sau do lam tron len theo buoc:

- Tu `100M`: buoc `10M`.
- Tu `1M`: buoc `1M`.
- Tu `100K`: buoc `100K`.
- Tu `10K`: buoc `10K`.
- Tu `1K`: buoc `1K`.
- Nho hon: buoc `100`.

### `comparisonChartVisualRatio`

Tinh ti le hien thi cho cot:

```kotlin
fun comparisonChartVisualRatio(amount: Double, axisMaxAmount: Double): Float
```

Bieu do khong dung ti le toan hoc tuyet doi vi cot nho co the bi thap qua, kho nhin tren mobile. Cong thuc hien tai:

```kotlin
val trueRatio = (amount / axisMaxAmount).toFloat().coerceIn(0f, 1f)
val readableFloor = 0.26f
return readableFloor + trueRatio * (1f - readableFloor)
```

Y nghia:

- Cot co gia tri `0` van cao `0`.
- Cot co gia tri lon nhat gan day vung ve.
- Cot nho co san hien thi 26% de van de nhin.
- Bieu do chi can doc de so sanh nhanh, khong phai chart tai chinh tuyet doi.

## 5. Cach tinh danh muc chi tieu nhieu nhat

Danh muc chi tieu duoc tinh trong `buildExpenseReport` bang cach nhom cac giao dich ky hien tai:

```kotlin
currentTransactions.groupBy { it.categoryId.ifBlank { it.categoryName.ifBlank { "unknown" } } }
```

Moi nhom tao ra mot `CategoryExpense`:

- `amount`: tong tien cua cac giao dich trong nhom.
- `percentage`: `amount / currentTotal`.
- `name`, `icon`, `color`: lay tu giao dich dau tien trong nhom.

Danh sach sau do duoc sap xep:

```kotlin
.sortedByDescending { it.amount }
```

Tai card trang chu, danh sach chi hien `topCategories.take(3)`. Trong bottom sheet, danh sach hien nhieu danh muc hon theo du lieu bao cao.

## 6. Bieu do cot so sanh

Bieu do cot duoc ve bang `Canvas` cua Jetpack Compose, khong dung thu vien chart ngoai.

### Du lieu vao

Moi lan chon tab:

- `previousAmount`: tong chi ky truoc.
- `currentAmount`: tong chi ky hien tai.
- `previousLabel`: "Tuan truoc" hoac "Thang truoc".
- `currentLabel`: "Tuan nay" hoac "Thang nay".

### Cach ve

- Hai cot duoc ve bang `drawRoundRect`.
- Cot ky truoc dung `OceanBlue400`.
- Cot ky hien tai dung `InvestAmber400`.
- Duong chan duoi dung `NeutralGray100`.
- Nhan gia tri nam canh cot:
  - Gia tri cot trai nam ben trai cot.
  - Gia tri cot phai nam ben phai cot.
- Text gia tri tren chart duoc ve bang Android native Canvas:

```kotlin
drawContext.canvas.nativeCanvas.drawText(...)
```

### Animation

Chieu cao cot duoc animate bang:

```kotlin
animateFloatAsState(
    targetValue = comparisonChartVisualRatio(...),
    animationSpec = tween(500)
)
```

Khi doi tab Tuan/Thang, cot thay doi mem hon thay vi nhay lap tuc.

## 7. Badge tang/giam chi tieu

Badge nam doi dien voi phan "Tong da chi".

Logic:

```kotlin
val difference = report.currentTotal - report.previousTotal
```

Quy tac mau:

- `difference > 0`: chi nhieu hon ky truoc, dung `ExpenseRed600`.
- `difference < 0`: chi it hon ky truoc, dung `SavingsTeal600`.
- `difference == 0`: dung mau trung tinh `NeutralGray600`.

Nen badge:

- Tang chi: `ExpenseRed50`.
- Giam chi: `SavingsTeal50`.
- Khong doi: `NeutralGray50`.

Nhan hien thi:

- `+1.2M so voi tuan truoc`.
- `-800K so voi thang truoc`.
- `0 so voi ky truoc`.

## 8. Bieu do donut va bottom sheet

`ExpenseBreakdownDialog` hien thi bang `Dialog` tuy bien, nhin nhu bottom sheet.

Tinh nang chinh:

- Mo tu duoi len bang `AnimatedVisibility`.
- Co the dong bang nut `X`.
- Co the dong khi bam ben ngoai sheet.
- Co the vuot sheet de dong.
- Chon danh muc bang cach bam vao segment donut hoac dong danh sach.
- Khi chon segment, segment do duoc phong/to noi bat.
- Danh sach tu dong cuon den danh muc dang chon.

### Donut chart

Donut chart duoc ve bang `Canvas`:

- Moi danh muc la mot segment.
- Ti le segment dua tren `category.percentage`.
- Cac danh muc hien thi duoc normalize de tong donut bang 100% trong phan du lieu dang ve.
- Segment duoc lift/scale khi duoc chon.
- Cac segment khong duoc chon giam alpha de tap trung vao segment dang chon.

### Tuong tac

Hit test segment donut dung vi tri tap:

```kotlin
detectTapGestures { offset -> ... }
```

Ham `findTappedDonutSegment` xac dinh nguoi dung bam vao segment nao dua tren goc va ban kinh.

## 9. UI lien quan

| File | Vai tro |
| --- | --- |
| `app/src/main/java/com/dung/ddmoney/ui/analytics/ReportModels.kt` | Chua model va logic tinh bao cao. |
| `app/src/main/java/com/dung/ddmoney/ui/home/components/HomeReportSection.kt` | Card bao cao tren man hinh tong quan. |
| `app/src/main/java/com/dung/ddmoney/ui/analytics/AnalyticsScreen.kt` | Man hinh bao cao rieng. |
| `app/src/main/java/com/dung/ddmoney/ui/analytics/ExpenseBreakdownDialog.kt` | Bottom sheet donut chart va danh sach danh muc. |
| `app/src/main/java/com/dung/ddmoney/ui/navigation/NavGraph.kt` | Dieu huong va truyen transaction state vao cac man hinh. |

## 10. Thu vien va cong nghe da dung

| Thu vien/Cong nghe | Muc dich |
| --- | --- |
| Kotlin | Ngon ngu lap trinh chinh. |
| Jetpack Compose | Xay dung UI bang Composable. |
| Compose Material3 | `Surface`, `Text`, `Icon`, `LinearProgressIndicator` va cac primitive UI. |
| Compose Canvas | Ve bieu do cot va donut chart. |
| Android native Canvas text | Ve nhan gia tri tren bieu do cot va text trong donut. |
| Compose Animation | `animateFloatAsState`, `animateDpAsState`, `AnimatedVisibility`, `tween`. |
| Navigation Compose | Dieu huong giua Home, Bao cao va cac man hinh khac. |
| Room | Luu cache SQLite local cho du lieu app. |
| KSP | Xu ly annotation compiler cho Room. |
| Kotlin Date/Time API | `LocalDate`, `DayOfWeek`, `TemporalAdjusters` de tinh tuan/thang. |
| Java `NumberFormat` | Dinh dang tien VND theo locale Viet Nam. |
| Vico Charts | Co trong dependencies, nhung chuc nang bao cao hien tai dang ve chart bang Compose Canvas. |

Ngoai ra app co Retrofit, OkHttp, Gson, Firebase, WorkManager, Coil va Security Crypto trong dependencies, nhung cac thu vien nay khong phai phan chinh cua logic bao cao thong ke hien tai.

## 11. Mau sac va theme

Chuc nang bao cao dung he mau Ocean Blueprint cua app:

- `OceanBlue600`: mau chu dao, active tab, nhan cot ky truoc.
- `OceanBlue400`: cot ky truoc.
- `InvestAmber400`: cot ky hien tai.
- `InvestAmber600`: nhan gia tri cot ky hien tai.
- `ExpenseRed600`: chi nhieu hon ky truoc.
- `ExpenseRed50`: nen badge tang chi.
- `SavingsTeal600`: chi it hon ky truoc.
- `SavingsTeal50`: nen badge giam chi va icon category badge.
- `NeutralGray600`: text phu.
- `NeutralGray100`: duong chan bieu do.

## 12. Kiem tra tinh nang

Cac truong hop can kiem tra:

- Khong co giao dich chi tieu trong ky hien tai.
- Ky truoc co tien, ky hien tai khong co tien.
- Ky hien tai co tien, ky truoc khong co tien.
- Ky hien tai chi nhieu hon ky truoc, badge phai mau do.
- Ky hien tai chi it hon ky truoc, badge phai mau xanh.
- Chuyen tab Tuan/Thang, pill selector truot mem.
- Bieu do cot animate lai theo 2 gia tri cua tab dang chon.
- Bam vao "Chi tieu nhieu nhat" mo bottom sheet.
- Bam ngoai bottom sheet dong sheet.
- Bam donut segment hoac dong danh sach thi chon dung danh muc.
- Donut segment phong/to va danh sach cuon toi danh muc dang chon.

Lenh build co the dung de kiem tra Kotlin:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon
```

