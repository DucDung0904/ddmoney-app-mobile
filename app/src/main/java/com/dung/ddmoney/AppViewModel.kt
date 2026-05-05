package com.dung.ddmoney

import com.dung.ddmoney.network.TokenManager
import com.dung.ddmoney.network.dto.AuthRequest
import com.dung.ddmoney.network.dto.RegisterRequest
import com.dung.ddmoney.repository.AuthRepository
import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dung.ddmoney.local.AppDatabase
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.dto.*
import com.dung.ddmoney.repository.CategoryRepository
import com.dung.ddmoney.repository.TransactionRepository
import com.dung.ddmoney.repository.WalletRepository
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── App State ────────────────────────────────────────────────────────
data class UserInfo(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = ""
)

data class AppState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val userInfo: UserInfo = UserInfo(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val totalBalance: Double
        get() = wallets.sumOf { it.balance }

    val currentMonthIncome: Double
        get() {
            val now = LocalDate.now()
            return transactions
                .filter {
                    it.type == TransactionType.INCOME &&
                            it.date.month == now.month &&
                            it.date.year == now.year
                }
                .sumOf { it.amount }
        }

    val currentMonthExpense: Double
        get() {
            val now = LocalDate.now()
            return transactions
                .filter {
                    it.type == TransactionType.EXPENSE &&
                            it.date.month == now.month &&
                            it.date.year == now.year
                }
                .sumOf { it.amount }
        }

    fun expenseCategories(): List<Category> =
        categories.filter { it.type == CategoryType.EXPENSE || it.type == CategoryType.BOTH }

    fun incomeCategories(): List<Category> =
        categories.filter { it.type == CategoryType.INCOME || it.type == CategoryType.BOTH }

    fun debtCategories(): List<Category> =
        categories.filter { it.type == CategoryType.DEBT }

    fun categorySpending(): List<Category> {
        val now = LocalDate.now()
        val grouped = transactions
            .filter {
                it.type == TransactionType.EXPENSE &&
                        it.date.month == now.month &&
                        it.date.year == now.year
            }
            .groupBy { it.categoryId }

        val total = grouped.values.flatten().sumOf { it.amount }
        if (total == 0.0) return emptyList()

        return categories
            .filter { it.type == CategoryType.EXPENSE }
            .mapNotNull { cat ->
                val amount = grouped[cat.id]?.sumOf { it.amount } ?: return@mapNotNull null
                cat.copy(
                    totalAmount = amount,
                    percentage = if (total > 0) (amount / total).toFloat() else 0f
                )
            }
            .sortedByDescending { it.totalAmount }
            .take(6)
    }
}

// ─── AppViewModel ─────────────────────────────────────────────────────
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    
    // Khởi tạo TokenManager và RetrofitClient
    private val tokenManager = TokenManager(application)
    init {
        RetrofitClient.init(tokenManager)
    }
    
    private val api = RetrofitClient.instance

    private val authRepo = AuthRepository(api, tokenManager)
    private val walletRepo = WalletRepository(api, db.walletDao())
    private val categoryRepo = CategoryRepository(api, db.categoryDao())
    private val transactionRepo = TransactionRepository(api, db.transactionDao(), application)

    // Loading & error state (tách riêng để không làm mất reactive state)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _userInfo = MutableStateFlow(
        UserInfo(
            name = tokenManager.getUserName(), 
            email = tokenManager.getUserEmail(),
            avatarUrl = tokenManager.getUserAvatar()
        )
    )

    private val _isDarkMode = MutableStateFlow(tokenManager.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        tokenManager.setDarkMode(isDark)
        _isDarkMode.value = isDark
    }

    /**
     * Kết hợp 3 Flow từ Room — tự động cập nhật khi DB thay đổi.
     * Khi offline, Room vẫn phát dữ liệu cached.
     */
    val state: StateFlow<AppState> = combine(
        walletRepo.observeAll(),
        categoryRepo.observeAll(),
        transactionRepo.observeAll(),
        _userInfo,
        _isLoading,
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val wallets = args[0] as List<Wallet>
        @Suppress("UNCHECKED_CAST")
        val categories = args[1] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val transactions = args[2] as List<Transaction>
        val userInfo = args[3] as UserInfo
        val isLoading = args[4] as Boolean
        val error = args[5] as String?

        AppState(
            wallets = wallets,
            categories = categories,
            transactions = transactions,
            userInfo = userInfo,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppState(
            isLoading = true, 
            userInfo = UserInfo(
                name = tokenManager.getUserName(), 
                email = tokenManager.getUserEmail(),
                avatarUrl = tokenManager.getUserAvatar()
            )
        )
    )

    // Auth states
    private val _isLoggedIn = MutableStateFlow(authRepo.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    init {
        if (_isLoggedIn.value) {
            syncAll()
        }
    }

    // ── Auth Actions ───────────────────────────────────────────────────
    fun login(req: AuthRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.login(req)
                .onSuccess {
                    _isLoggedIn.value = true
                    _userInfo.value = UserInfo(
                        name = tokenManager.getUserName(), 
                        email = tokenManager.getUserEmail(),
                        avatarUrl = tokenManager.getUserAvatar()
                    )
                    syncAll() // Tải dữ liệu sau khi đăng nhập
                    // Sau khi đăng nhập, cập nhật user profile từ api/users/me
                    viewModelScope.launch {
                        authRepo.fetchCurrentUser().onSuccess {
                            _userInfo.value = UserInfo(
                                name = tokenManager.getUserName(), 
                                email = tokenManager.getUserEmail(),
                                avatarUrl = tokenManager.getUserAvatar()
                            )
                        }
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _error.value = "Đăng nhập thất bại: ${e.message}"
                }
            _authLoading.value = false
        }
    }

    fun register(req: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.register(req)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    _error.value = "Đăng ký thất bại: ${e.message}"
                }
            _authLoading.value = false
        }
    }

    fun logout() {
        authRepo.logout()
        _isLoggedIn.value = false
        _userInfo.value = UserInfo()
        // Xóa data local
        viewModelScope.launch {
            db.transactionDao().deleteAll()
            db.walletDao().deleteAll()
            db.categoryDao().deleteAll()
        }
    }

    // ── Sync từ API → Room ─────────────────────────────────────────────
    fun syncAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                listOf(
                    async { walletRepo.sync() },
                    async { categoryRepo.sync() },
                    async { transactionRepo.sync() }
                ).awaitAll()
            } catch (e: Exception) {
                _error.value = "Không thể kết nối tới server: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Alias để giữ tương thích với code UI cũ
    fun loadAll() = syncAll()

    fun clearError() {
        _error.value = null
    }

    // ── User / Avatar ──────────────────────────────────────────────────
    fun updateAvatar(url: String) {
        viewModelScope.launch {
            authRepo.updateAvatar(url).onSuccess {
                _userInfo.value = UserInfo(
                    name = tokenManager.getUserName(), 
                    email = tokenManager.getUserEmail(),
                    avatarUrl = tokenManager.getUserAvatar()
                )
            }.onFailure {
                _error.value = "Cập nhật avatar thất bại"
            }
        }
    }

    // ── Transaction ───────────────────────────────────────────────────
    fun addTransaction(
        title: String,
        categoryId: String,
        amount: Double,
        type: TransactionType,
        walletId: String,
        date: LocalDate,
        note: String = ""
    ) {
        viewModelScope.launch {
            val walletIdLong = walletId.toLongOrNull() ?: return@launch
            val categoryIdLong = categoryId.toLongOrNull() ?: return@launch

            val req = TransactionRequest(
                title = title.ifBlank { null },
                amount = amount,
                type = type.name,
                date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                walletId = walletIdLong,
                categoryId = categoryIdLong,
                note = note.ifBlank { null }
            )

            transactionRepo.create(req)
                .onSuccess {
                    // Update local balance immediately instead of syncing from server.
                    // Server sync would fetch the old balance because SyncWorker is async.
                    val entity = db.walletDao().getByServerId(walletIdLong)
                    if (entity != null) {
                        val delta = if (type == TransactionType.INCOME) amount else -amount
                        db.walletDao().upsert(entity.copy(balance = entity.balance + delta))
                    }
                }
                .onFailure { e ->
                    _error.value = "Lỗi tạo giao dịch: ${e.message}"
                }
        }
    }

    // ── Transfer ──────────────────────────────────────────────────────
    fun transfer(
        fromWalletId: String,
        toWalletId: String,
        amount: Double,
        date: LocalDate,
        note: String = ""
    ) {
        viewModelScope.launch {
            val fromId = fromWalletId.toLongOrNull() ?: return@launch
            val toId = toWalletId.toLongOrNull() ?: return@launch

            walletRepo.transfer(fromId, toId, amount, note.ifBlank { null })
                .onFailure { e ->
                    _error.value = "Lỗi chuyển tiền: ${e.message}"
                }
        }
    }

    // ── Category CRUD ─────────────────────────────────────────────────
    fun addCategory(name: String, icon: String, color: Color, type: CategoryType) {
        viewModelScope.launch {
            val req = CategoryRequest(
                name = name,
                icon = icon,
                colorHex = colorToHex(color),
                type = type.name
            )
            categoryRepo.create(req)
                .onFailure { e ->
                    _error.value = "Lỗi tạo danh mục: ${e.message}"
                }
        }
    }

    fun editCategory(category: Category) {
        viewModelScope.launch {
            val id = category.id.toLongOrNull() ?: return@launch
            val req = CategoryRequest(
                name = category.name,
                icon = category.icon,
                colorHex = colorToHex(category.color),
                type = category.type.name
            )
            categoryRepo.update(id, req)
                .onFailure { e ->
                    _error.value = "Lỗi cập nhật danh mục: ${e.message}"
                }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val id = categoryId.toLongOrNull() ?: return@launch
            categoryRepo.delete(id)
                .onFailure { e ->
                    _error.value = "Lỗi xóa danh mục: ${e.message}"
                }
        }
    }

    // ── Wallet CRUD ───────────────────────────────────────────────────
    fun addWallet(
        name: String,
        balance: Double,
        type: WalletType,
        bank: String = "",
        color: Color
    ) {
        viewModelScope.launch {
            val req = WalletRequest(
                name = name,
                balance = balance,
                type = type.name,
                bankName = bank.ifBlank { null },
                colorHex = colorToHex(color)
            )
            walletRepo.create(req)
                .onFailure { e ->
                    _error.value = "Lỗi tạo ví: ${e.message}"
                }
        }
    }

    fun editWallet(wallet: Wallet) {
        viewModelScope.launch {
            val id = wallet.id.toLongOrNull() ?: return@launch
            val req = WalletRequest(
                name = wallet.name,
                balance = wallet.balance,
                type = wallet.type.name,
                bankName = wallet.bank.ifBlank { null },
                colorHex = colorToHex(wallet.color)
            )
            walletRepo.update(id, req)
                .onFailure { e ->
                    _error.value = "Lỗi cập nhật ví: ${e.message}"
                }
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            val id = walletId.toLongOrNull() ?: return@launch
            walletRepo.delete(id)
                .onFailure { e ->
                    _error.value = "Lỗi xóa ví: ${e.message}"
                }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────
    private fun colorToHex(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return "#%02X%02X%02X".format(r, g, b)
    }
}

// ─── Extension: Map API DTOs → Domain Models (kept for backward compat) ──

fun WalletResponse.toModel(): Wallet = Wallet(
    id = id.toString(),
    name = name,
    balance = balance,
    type = runCatching { WalletType.valueOf(type) }.getOrDefault(WalletType.CASH),
    bank = bankName ?: "",
    cardNumber = cardNumber ?: "",
    color = parseColor(colorHex)
)

fun CategoryResponse.toModel(): Category = Category(
    id = id.toString(),
    name = name,
    icon = icon,
    color = parseColor(colorHex),
    type = runCatching { CategoryType.valueOf(type) }.getOrDefault(CategoryType.EXPENSE),
    isDefault = isDefault ?: false
)

fun TransactionResponse.toModel(): Transaction = Transaction(
    id = id.toString(),
    title = title,
    categoryId = categoryId.toString(),
    categoryName = categoryName ?: "",
    categoryIcon = categoryIcon ?: "📦",
    categoryColor = parseColor(categoryColor),
    amount = amount,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    walletId = walletId.toString(),
    walletName = walletName ?: "",
    date = runCatching {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrDefault(LocalDate.now()),
    note = note ?: ""
)

// Parse "#RRGGBB" hex string to Compose Color, with sensible fallback
fun parseColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF4659A6)
    return try {
        val clean = hex.trimStart('#')
        Color(android.graphics.Color.parseColor("#$clean"))
    } catch (e: Exception) {
        Color(0xFF4659A6)
    }
}
