package com.dung.ddmoney

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dung.ddmoney.local.AppDatabase
import com.dung.ddmoney.network.RetrofitClient
import com.dung.ddmoney.network.TokenManager
import com.dung.ddmoney.network.dto.*
import com.dung.ddmoney.network.dto.AuthRequest
import com.dung.ddmoney.network.dto.RegisterRequest
import com.dung.ddmoney.repository.AuthRepository
import com.dung.ddmoney.repository.BudgetDisplayModel
import com.dung.ddmoney.repository.BudgetPeriod
import com.dung.ddmoney.repository.BudgetRepository
import com.dung.ddmoney.repository.CategoryRepository
import com.dung.ddmoney.repository.TransactionRepository
import com.dung.ddmoney.repository.WalletRepository
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

// ─── App State ────────────────────────────────────────────────────────
data class UserInfo(val name: String = "", val email: String = "", val avatarUrl: String = "")

data class AppState(
        val transactions: List<Transaction> = emptyList(),
        val categories: List<Category> = emptyList(),
        val wallets: List<Wallet> = emptyList(),
        val allWallets: List<Wallet> = emptyList(),
        val budgets: List<BudgetDisplayModel> = emptyList(),
        val userInfo: UserInfo = UserInfo(),
        val isLoading: Boolean = false,
        val error: String? = null
)

// ─── AppViewModel ─────────────────────────────────────────────────────
@OptIn(ExperimentalCoroutinesApi::class)
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
    private val transactionRepo =
            TransactionRepository(
                    api,
                    db.transactionDao(),
                    db.walletDao(),
                    db.categoryDao(),
                    application
            )
    private val budgetRepo =
            BudgetRepository(api, db.budgetDao(), db.transactionDao(), db.categoryDao())

    // Loading & error state (tách riêng để không làm mất reactive state)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _userInfo =
            MutableStateFlow(
                    UserInfo(
                            name = tokenManager.getUserName(),
                            email = tokenManager.getUserEmail(),
                            avatarUrl = tokenManager.getUserAvatar()
                    )
            )
    private val _currentUserId = MutableStateFlow(tokenManager.getUserId())

    private val _isDarkMode = MutableStateFlow(tokenManager.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isOnboardingDone = MutableStateFlow(tokenManager.isOnboardingDone())
    val isOnboardingDone: StateFlow<Boolean> = _isOnboardingDone.asStateFlow()

    private val _currency = MutableStateFlow(tokenManager.getCurrency())
    val currency: StateFlow<String> = _currency.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        tokenManager.setDarkMode(isDark)
        _isDarkMode.value = isDark
    }

    /**
     * Xác định màn hình bắt đầu khi app khởi động dựa vào trạng thái token & onboarding. Được gọi 1
     * lần duy nhất ở MainActivity trước khi NavGraph render.
     * - Không có token → WELCOME
     * - Có token nhưng chưa onboarding → ONBOARDING
     * - Có token & đã onboarding → MAIN
     */
    fun resolveStartDestination(): String {
        val hasToken = tokenManager.hasValidToken()
        return when {
            !hasToken -> "welcome"
            !tokenManager.isOnboardingDone() -> "onboarding"
            else -> "main"
        }
    }

    fun completeOnboarding(
            currency: String,
            walletName: String,
            walletBalance: Double,
            walletIcon: String = "",
            walletType: WalletType = WalletType.CASH
    ) {
        tokenManager.setCurrency(currency)
        tokenManager.setOnboardingDone(true)
        _currency.value = currency
        _isOnboardingDone.value = true
        // Create the first wallet via API
        viewModelScope.launch {
            val req =
                    WalletRequest(
                            name = walletName,
                            balance = walletBalance,
                            type = walletType.name,
                            bankName = null,
                            icon = walletIcon,
                            currency = currency,
                            isDefault = true
                    )
            walletRepo.create(req).onFailure { e ->
                _error.value = "Không thể tạo ví: ${e.message}"
            }
        }
    }

    /**
     * Kết hợp 3 Flow từ Room — tự động cập nhật khi DB thay đổi. Khi offline, Room vẫn phát dữ liệu
     * cached.
     */
    val state: StateFlow<AppState> =
            combine(
                            walletRepo.observeActive(),
                            walletRepo.observeAll(),
                            _currentUserId.flatMapLatest { userId ->
                                categoryRepo.observeAll(userId)
                            },
                            transactionRepo.observeAll(),
                            budgetRepo.getBudgetsWithCalculatedSpent(),
                            _userInfo,
                            _isLoading,
                            _error
                    ) { args ->
                        @Suppress("UNCHECKED_CAST") val wallets = args[0] as List<Wallet>
                        @Suppress("UNCHECKED_CAST") val allWallets = args[1] as List<Wallet>
                        @Suppress("UNCHECKED_CAST") val categories = args[2] as List<Category>
                        @Suppress("UNCHECKED_CAST") val transactions = args[3] as List<Transaction>
                        @Suppress("UNCHECKED_CAST")
                        val budgets = args[4] as List<BudgetDisplayModel>
                        val userInfo = args[5] as UserInfo
                        val isLoading = args[6] as Boolean
                        val error = args[7] as String?

                        AppState(
                                wallets = wallets,
                                allWallets = allWallets,
                                categories = categories,
                                transactions = transactions,
                                budgets = budgets,
                                userInfo = userInfo,
                                isLoading = isLoading,
                                error = error
                        )
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5_000),
                            initialValue =
                                    AppState(
                                            isLoading = true,
                                            userInfo =
                                                    UserInfo(
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
        RetrofitClient.setUnauthorizedHandler { expireSession() }
        if (_isLoggedIn.value) {
            syncAll()
        }
    }

    // ── Auth Actions ───────────────────────────────────────────────────
    fun login(req: AuthRequest, onSuccess: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.login(req)
                    .onSuccess {
                        _isLoggedIn.value = true
                        _currentUserId.value = tokenManager.getUserId()
                        _userInfo.value =
                                UserInfo(
                                        name = tokenManager.getUserName(),
                                        email = tokenManager.getUserEmail(),
                                        avatarUrl = tokenManager.getUserAvatar()
                                )

                        val isNewUser = tokenManager.isNewUserFromToken()

                        if (!isNewUser) {
                            tokenManager.setOnboardingDone(true)
                            _isOnboardingDone.value = true
                        }

                        syncAll() // Tải dữ liệu sau khi đăng nhập
                        // Sau khi đăng nhập, cập nhật user profile từ api/users/me
                        viewModelScope.launch {
                            authRepo.fetchCurrentUser().onSuccess {
                                _userInfo.value =
                                        UserInfo(
                                                name = tokenManager.getUserName(),
                                                email = tokenManager.getUserEmail(),
                                                avatarUrl = tokenManager.getUserAvatar()
                                        )
                            }
                        }
                        onSuccess(isNewUser)
                    }
                    .onFailure { e ->
                        val msg = e.message ?: ""
                        if (msg.contains("HTTP 400") || msg.contains("HTTP 401")) {
                            if (msg.contains("invalid", ignoreCase = true) &&
                                            msg.contains("email", ignoreCase = true)
                            ) {
                                _error.value = "Email không hợp lệ."
                            } else {
                                _error.value = "Email hoặc mật khẩu không đúng."
                            }
                        } else {
                            _error.value = "Đăng nhập thất bại: ${e.message}"
                        }
                    }
            _authLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.loginWithGoogle(idToken)
                    .onSuccess {
                        _isLoggedIn.value = true
                        _currentUserId.value = tokenManager.getUserId()
                        _userInfo.value =
                                UserInfo(
                                        name = tokenManager.getUserName(),
                                        email = tokenManager.getUserEmail(),
                                        avatarUrl = tokenManager.getUserAvatar()
                                )

                        val isNewUser = tokenManager.isNewUserFromToken()

                        if (!isNewUser) {
                            tokenManager.setOnboardingDone(true)
                            _isOnboardingDone.value = true
                        }

                        syncAll()

                        viewModelScope.launch {
                            authRepo.fetchCurrentUser().onSuccess {
                                _userInfo.value =
                                        UserInfo(
                                                name = tokenManager.getUserName(),
                                                email = tokenManager.getUserEmail(),
                                                avatarUrl = tokenManager.getUserAvatar()
                                        )
                            }
                        }

                        onSuccess(isNewUser)
                    }
                    .onFailure { e ->
                        var serverMessage = ""
                        if (e is retrofit2.HttpException) {
                            try {
                                val errorBody = e.response()?.errorBody()?.string()
                                if (errorBody != null) {
                                    serverMessage = " - Server: $errorBody"
                                }
                            } catch (ignore: Exception) {}
                        }
                        val msg = e.message ?: ""
                        if (msg.contains("hủy")) {
                            _error.value = "Đăng nhập Google bị hủy."
                        } else if (msg.contains("HTTP 400") || msg.contains("HTTP 401")) {
                            _error.value = "Xác thực Google thất bại.$serverMessage"
                        } else if (msg.contains("Network") || msg.contains("Connect")) {
                            _error.value = "Không thể kết nối máy chủ."
                        } else {
                            _error.value = "Không thể đăng nhập Google: ${e.message}$serverMessage"
                        }
                    }
            _authLoading.value = false
        }
    }

    fun register(req: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.register(req).onSuccess { onSuccess() }.onFailure { e ->
                val msg = e.message ?: ""
                if (msg.contains("HTTP 400") || msg.contains("HTTP 409")) {
                    if (msg.contains("email", ignoreCase = true)) {
                        _error.value = "Email đã tồn tại hoặc không hợp lệ."
                    } else {
                        _error.value = "Thông tin đăng ký không hợp lệ."
                    }
                } else {
                    _error.value = "Đăng ký thất bại: ${e.message}"
                }
            }
            _authLoading.value = false
        }
    }

    fun logout() {
        clearSession(showExpiredMessage = false)
    }

    private fun expireSession() {
        clearSession(showExpiredMessage = true)
    }

    private fun clearSession(showExpiredMessage: Boolean) {
        authRepo.logout()
        _isLoggedIn.value = false
        _currentUserId.value = -1L
        _userInfo.value = UserInfo()
        if (showExpiredMessage) {
            _error.value = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        }
        // Xóa data local
        viewModelScope.launch {
            db.transactionDao().deleteAll()
            db.walletDao().deleteAll()
            db.categoryDao().deleteAll()
            db.budgetDao().deleteAll()
            db.budgetDao().deleteAllCategories()
        }
    }

    // ── Budget Actions ──────────────────────────────────────────────────
    fun createBudget(
            name: String,
            amount: Double,
            period: BudgetPeriod,
            walletId: Long?,
            categoryIds: List<Long>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                budgetRepo.createBudget(0L, name, amount, period, walletId, categoryIds)
                syncAll()
            } catch (e: retrofit2.HttpException) {
                _error.value = readServerError(e, "Không thể tạo ngân sách")
            } catch (e: Exception) {
                _error.value = "Không thể tạo ngân sách: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBudget(
            budgetId: String,
            name: String,
            amount: Double,
            period: BudgetPeriod,
            walletId: Long?,
            categoryIds: List<Long>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                budgetRepo.updateBudget(
                        budgetId,
                        0L,
                        name,
                        amount,
                        period,
                        walletId,
                        categoryIds
                )
                syncAll()
            } catch (e: retrofit2.HttpException) {
                _error.value = readServerError(e, "Không thể cập nhật ngân sách")
            } catch (e: Exception) {
                _error.value = "Không thể cập nhật ngân sách: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                budgetRepo.deleteBudget(budgetId)
                syncAll()
            } catch (e: Exception) {
                _error.value = "Không thể xóa ngân sách: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Sync từ API → Room ─────────────────────────────────────────────
    fun syncAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val syncResults =
                        listOf(
                                walletRepo.sync(),
                                transactionRepo.sync(),
                                categoryRepo.sync(),
                                budgetRepo.sync()
                        )
                syncResults.firstOrNull { it.isFailure }?.exceptionOrNull()?.let { error ->
                    _error.value = "Không thể đồng bộ toàn bộ dữ liệu: ${error.message}"
                }
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

    fun setError(message: String) {
        _error.value = message
    }

    private fun readServerError(
            error: retrofit2.HttpException,
            fallback: String
    ): String {
        val body = error.response()?.errorBody()?.string().orEmpty()
        val message =
                runCatching { JSONObject(body).optString("error") }
                        .getOrNull()
                        ?.takeIf { it.isNotBlank() }
        return message ?: "$fallback (HTTP ${error.code()})"
    }

    // ── User / Avatar ──────────────────────────────────────────────────
    fun updateAvatar(url: String) {
        viewModelScope.launch {
            authRepo.updateAvatar(url)
                    .onSuccess {
                        _userInfo.value =
                                UserInfo(
                                        name = tokenManager.getUserName(),
                                        email = tokenManager.getUserEmail(),
                                        avatarUrl = tokenManager.getUserAvatar()
                                )
                    }
                    .onFailure { _error.value = "Cập nhật avatar thất bại" }
        }
    }

    // ── Transaction Actions ─────────────────────────────────────────────
    fun addTransaction(req: TransactionRequest, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            transactionRepo
                    .create(req)
                    .onSuccess {
                        onComplete()
                    }
                    .onFailure { e -> _error.value = "Không thể lưu giao dịch: ${e.message}" }
            _isLoading.value = false
        }
    }

    // ── Wallet Actions ──────────────────────────────────────────────────
    fun createWallet(req: WalletRequest, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            walletRepo
                    .create(req)
                    .onSuccess {
                        onComplete()
                        syncAll()
                    }
                    .onFailure { e -> _error.value = "Không thể tạo ví: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun updateWallet(walletId: String, req: WalletRequest, onComplete: () -> Unit = {}) {
        val serverId = walletId.toLongOrNull()
        if (serverId == null) {
            _error.value = "Không thể cập nhật ví chưa đồng bộ"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            walletRepo
                    .update(serverId, req)
                    .onSuccess {
                        onComplete()
                        syncAll()
                    }
                    .onFailure { e -> _error.value = "Không thể cập nhật ví: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun setDefaultWallet(walletId: String) {
        viewModelScope.launch {
            walletRepo.setDefaultWallet(walletId).onFailure { e ->
                _error.value = "Không thể đặt ví mặc định: ${e.message}"
            }
        }
    }


    fun archiveWallet(walletId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            walletRepo
                    .archiveWallet(walletId)
                    .onSuccess {
                        onComplete()
                        syncAll()
                    }
                    .onFailure { e -> _error.value = "Không thể lưu trữ ví: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun unarchiveWallet(walletId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            walletRepo
                    .unarchiveWallet(walletId)
                    .onSuccess {
                        onComplete()
                        syncAll()
                    }
                    .onFailure { e -> _error.value = "Không thể bỏ lưu trữ ví: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun transferWallet(
            fromWalletId: String,
            toWalletId: String,
            amount: Double,
            onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            walletRepo
                    .transfer(fromWalletId, toWalletId, amount)
                    .onSuccess {
                        onComplete()
                        syncAll()
                    }
                    .onFailure { e -> _error.value = "Không thể chuyển tiền: ${e.message}" }
            _isLoading.value = false
        }
    }
}

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
