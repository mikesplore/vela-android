package com.template.app.core.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Before Room opens a destructive upgrade from v25, capture the single
 * connection from the legacy settings table into SharedPreferences so we can
 * restore it into paired_devices after the new schema is created.
 */
object LegacyConnectionMigrator {
    private const val TAG = "LegacyConnectionMigrator"
    private const val PREFS = "vela_legacy_migration"
    private const val KEY_PENDING = "pending"
    private const val KEY_BASE_URL = "baseUrl"
    private const val KEY_API_TOKEN = "apiToken"
    private const val KEY_THEME = "themeMode"
    private const val KEY_HOSTNAME = "hostname"

    data class CapturedConnection(
        val baseUrl: String,
        val apiToken: String,
        val themeMode: String,
        val hostname: String?
    )

    fun captureFromLegacyDb(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_PENDING, false)) return

        val dbFile = context.getDatabasePath("app_database")
        if (!dbFile.exists()) return

        try {
            SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { db ->
                val version = db.version
                if (version >= 26) return

                var baseUrl = ""
                var apiToken = ""
                var themeMode = "SYSTEM"
                db.rawQuery(
                    "SELECT baseUrl, apiToken, themeMode FROM settings WHERE id = 0 LIMIT 1",
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        baseUrl = cursor.getString(0).orEmpty()
                        apiToken = cursor.getString(1).orEmpty()
                        themeMode = cursor.getString(2) ?: "SYSTEM"
                    }
                }

                var hostname: String? = null
                try {
                    db.rawQuery(
                        "SELECT prettyHostname, hostname FROM vela_device WHERE id = 0 LIMIT 1",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            hostname = cursor.getString(0) ?: cursor.getString(1)
                        }
                    }
                } catch (_: Exception) {
                    // table may not exist
                }

                if (baseUrl.isNotBlank() && apiToken.isNotBlank()) {
                    prefs.edit()
                        .putBoolean(KEY_PENDING, true)
                        .putString(KEY_BASE_URL, baseUrl)
                        .putString(KEY_API_TOKEN, apiToken)
                        .putString(KEY_THEME, themeMode)
                        .putString(KEY_HOSTNAME, hostname)
                        .apply()
                    Log.i(TAG, "Captured legacy connection for migration")
                } else if (themeMode.isNotBlank()) {
                    prefs.edit()
                        .putBoolean(KEY_PENDING, true)
                        .putString(KEY_THEME, themeMode)
                        .apply()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture legacy settings", e)
        }
    }

    fun consumePending(context: Context): CapturedConnection? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PENDING, false)) return null

        val captured = CapturedConnection(
            baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty(),
            apiToken = prefs.getString(KEY_API_TOKEN, "").orEmpty(),
            themeMode = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM",
            hostname = prefs.getString(KEY_HOSTNAME, null)
        )
        prefs.edit().clear().apply()
        return captured
    }
}
