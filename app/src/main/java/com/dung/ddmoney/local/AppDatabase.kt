package com.dung.ddmoney.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dung.ddmoney.local.dao.*
import com.dung.ddmoney.local.entity.*

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ddmoney_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE categories ADD COLUMN parentId INTEGER")
                    db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 10000")
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE categories ADD COLUMN userId INTEGER")
                    db.execSQL("ALTER TABLE categories ADD COLUMN isEditable INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE categories ADD COLUMN isDeletable INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE categories ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("UPDATE categories SET isEditable = CASE WHEN isDefault = 1 THEN 0 ELSE 1 END")
                    db.execSQL("UPDATE categories SET isDeletable = CASE WHEN isDefault = 1 THEN 0 ELSE 1 END")
                }
            }

        /**
         * Wallet module refactor: adds new wallet columns for production-ready
         * wallet system with credit card support, default wallet logic, and archive.
         */
        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // New wallet metadata columns
                    db.execSQL("ALTER TABLE wallets ADD COLUMN userId INTEGER")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN icon TEXT NOT NULL DEFAULT 'wallet'")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN currency TEXT NOT NULL DEFAULT 'VND'")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                    // Credit card specific
                    db.execSQL("ALTER TABLE wallets ADD COLUMN creditLimit REAL")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN currentDebt REAL")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN billingDay INTEGER")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN paymentDueDay INTEGER")
                    // Timestamps
                    db.execSQL("ALTER TABLE wallets ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                    // Rename legacy type CREDIT → CREDIT_CARD
                    db.execSQL("UPDATE wallets SET type = 'CREDIT_CARD' WHERE type = 'CREDIT'")
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE wallets ADD COLUMN isIncludedInTotal INTEGER NOT NULL DEFAULT 1")
                }
            }

        private val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE wallets ADD COLUMN targetAmount REAL")
                    db.execSQL("ALTER TABLE wallets ADD COLUMN targetDate TEXT")
                }
            }

        private val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    if (!db.hasColumn("wallets", "updatedAt")) {
                        db.execSQL("ALTER TABLE wallets ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                    }
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `wallets_new` (
                            `id` TEXT NOT NULL,
                            `serverId` INTEGER,
                            `userId` INTEGER,
                            `name` TEXT NOT NULL,
                            `balance` REAL NOT NULL,
                            `type` TEXT NOT NULL,
                            `bankName` TEXT,
                            `cardNumber` TEXT,
                            `icon` TEXT NOT NULL,
                            `currency` TEXT NOT NULL,
                            `isDefault` INTEGER NOT NULL,
                            `isActive` INTEGER NOT NULL,
                            `isArchived` INTEGER NOT NULL,
                            `isIncludedInTotal` INTEGER NOT NULL,
                            `sortOrder` INTEGER NOT NULL,
                            `creditLimit` REAL,
                            `currentDebt` REAL,
                            `billingDay` INTEGER,
                            `paymentDueDay` INTEGER,
                            `targetAmount` REAL,
                            `targetDate` TEXT,
                            `syncStatus` TEXT NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO `wallets_new` (
                            `id`, `serverId`, `userId`, `name`, `balance`, `type`,
                            `bankName`, `cardNumber`, `icon`, `currency`, `isDefault`,
                            `isActive`, `isArchived`, `isIncludedInTotal`, `sortOrder`,
                            `creditLimit`, `currentDebt`, `billingDay`, `paymentDueDay`,
                            `targetAmount`, `targetDate`, `syncStatus`, `createdAt`, `updatedAt`
                        )
                        SELECT
                            `id`, `serverId`, `userId`, `name`, `balance`, `type`,
                            `bankName`, `cardNumber`,
                            CASE
                                WHEN `icon` IS NULL OR TRIM(`icon`) = '' OR `icon` = 'wallet'
                                    THEN 'wallet_icon1'
                                ELSE `icon`
                            END,
                            `currency`, `isDefault`, `isActive`, `isArchived`,
                            `isIncludedInTotal`, `sortOrder`, `creditLimit`, `currentDebt`,
                            `billingDay`, `paymentDueDay`, `targetAmount`, `targetDate`,
                            `syncStatus`, `createdAt`, `updatedAt`
                        FROM `wallets`
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE `wallets`")
                    db.execSQL("ALTER TABLE `wallets_new` RENAME TO `wallets`")
                }
            }

        private val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE budgets ADD COLUMN periodType TEXT NOT NULL DEFAULT 'MONTH'")
                    db.execSQL("ALTER TABLE budgets ADD COLUMN startDate TEXT")
                    db.execSQL("ALTER TABLE budgets ADD COLUMN endDate TEXT")
                    db.execSQL("ALTER TABLE budgets ADD COLUMN walletId INTEGER")
                }
            }

        private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
            val cursor = query("PRAGMA table_info(`$tableName`)")
            cursor.use {
                val nameIndex = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (it.getString(nameIndex) == columnName) return true
                }
            }
            return false
        }
    }
}
