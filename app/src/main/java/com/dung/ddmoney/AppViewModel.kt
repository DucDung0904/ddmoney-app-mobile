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
import com.dung.ddmoney.repository.CategoryRepository
import com.dung.ddmoney.repository.TransactionRepository
import com.dung.ddmoney.repository.WalletRepository
import com.dung.ddmoney.ui.dashboard.model.*
import com.dung.ddmoney.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── App State ────────────────────────────────────────────────────────
data class UserInfo(val name: String = "", val email: String = "", val avatarUrl: String = "")

data class AppState(
        val transactions: List<Transaction> = emptyList(),
        val categories: List<Category> = emptyList(),
        val wallets: List<Wallet> = emptyList(),
        val userInfo: UserInfo = UserInfo(),
        val isLoading: Boolean = false,
        val error: String? = null
)

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

    private val _userInfo =
            MutableStateFlow(
                    UserInfo(
                            name = tokenManager.getUserName(),
                            email = tokenManager.getUserEmail(),
                            avatarUrl = tokenManager.getUserAvatar()
                    )
            )

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
     * Xác định màn hình bắt đầu khi app khởi động dựa vào trạng thái token & onboarding.
     * Được gọi 1 lần duy nhất ở MainActivity trước khi NavGraph render.
     * - Không có token  → WELCOME
     * - Có token nhưng chưa onboarding → ONBOARDING
     * - Có token & đã onboarding → MAIN
     */
    fun resolveStartDestination(): String {
        val hasToken = !tokenManager.getToken().isNullOrBlank()
        return when {
            !hasToken -> "welcome"
            !tokenManager.isOnboardingDone() -> "onboarding"
            else -> "main"
        }
    }

    fun completeOnboarding(currency: String, walletName: String, walletBalance: Double, walletIcon: String = "wallet", walletType: WalletType = WalletType.CASH) {
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
                            colorHex = "#003CC7",
                            icon = walletIcon
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
                            walletRepo.observeAll(),
                            categoryRepo.observeAll(),
                            transactionRepo.observeAll(),
                            _userInfo,
                            _isLoading,
                            _error
                    ) { args ->
                        @Suppress("UNCHECKED_CAST") val wallets = args[0] as List<Wallet>
                        @Suppress("UNCHECKED_CAST") val categories = args[1] as List<Category>
                        @Suppress("UNCHECKED_CAST") val transactions = args[2] as List<Transaction>
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
                        _userInfo.value =
                                UserInfo(
                                        name = tokenManager.getUserName(),
                                        email = tokenManager.getUserEmail(),
                                        avatarUrl = tokenManager.getUserAvatar()
                                )

                        var isNewUser = true
                        try {
                            val token = tokenManager.getToken()
                            if (token != null) {
                                val split = token.split(".")
                                if (split.size >= 2) {
                                    val payload =
                                            String(
                                                    android.util.Base64.decode(
                                                            split[1],
                                                            android.util.Base64.URL_SAFE
                                                    )
                                            )
                                    val jsonObject = org.json.JSONObject(payload)
                                    if (jsonObject.has("new")) {
                                        isNewUser = jsonObject.getBoolean("new")
                                    } else if (jsonObject.has("isNew")) {
                                        isNewUser = jsonObject.getBoolean("isNew")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

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
                    .onFailure { e -> _error.value = "Đăng nhập thất bại: ${e.message}" }
            _authLoading.value = false
        }
    }

    fun register(req: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _error.value = null
            authRepo.register(req).onSuccess { onSuccess() }.onFailure { e ->
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
                        )
                        .awaitAll()
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

}

// ─── Extension: Map API DTOs → Domain Models (kept for backward compat) ──

fun WalletResponse.toModel(): Wallet =
        Wallet(
                id = id.toString(),
                name = name,
                balance = balance,
                type = runCatching { WalletType.valueOf(type) }.getOrDefault(WalletType.CASH),
                bank = bankName ?: "",
                cardNumber = cardNumber ?: "",
                color = parseColor(colorHex)
        )

fun CategoryResponse.toModel(): Category =
        Category(
                id = id.toString(),
                name = name,
                icon = icon,
                color = parseColor(colorHex),
                type =
                        runCatching { CategoryType.valueOf(type) }
                                .getOrDefault(CategoryType.EXPENSE),
                isDefault = isDefault ?: false
        )

fun TransactionResponse.toModel(): Transaction =
        Transaction(
                id = id.toString(),
                title = title,
                categoryId = categoryId.toString(),
                categoryName = categoryName ?: "",
                categoryIcon = categoryIcon ?: "📦",
                categoryColor = parseColor(categoryColor),
                amount = amount,
                type =
                        runCatching { TransactionType.valueOf(type) }
                                .getOrDefault(TransactionType.EXPENSE),
                walletId = walletId.toString(),
                walletName = walletName ?: "",
                date =
                        runCatching { LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) }
                                .getOrDefault(LocalDate.now()),
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
