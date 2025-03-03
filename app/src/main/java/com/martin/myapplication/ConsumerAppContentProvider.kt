package com.martin.myapplication

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class ConsumerAppStatusProvider : ContentProvider() {
    companion object {
        const val PROVIDER_NAME = "com.lmev.rider.dev.statusprovider"
        private const val DRIVER_STATUS = 1
        const val TABLE_NAME = "consumer_status"
        private const val DB_NAME = "ConsumerDB"
        const val COLUMN = "status"

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PROVIDER_NAME, TABLE_NAME, DRIVER_STATUS)
        }
    }

    private lateinit var database: SQLiteDatabase

    override fun onCreate(): Boolean {
        val appContext = requireNotNull(context) { "ContentProvider context is null" }
        val dbHelper = object : SQLiteOpenHelper(appContext, DB_NAME, null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY, $COLUMN INTEGER DEFAULT -1)")
                db.execSQL("INSERT INTO $TABLE_NAME (id) VALUES (1)")
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }
        database = dbHelper.writableDatabase
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return if (uriMatcher.match(uri) == DRIVER_STATUS) {
            database.query(TABLE_NAME, projection ?: arrayOf(COLUMN), null, null, null, null, null)
        } else {
            throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return if (uriMatcher.match(uri) == DRIVER_STATUS) {
            synchronized(database) {
                database.update(TABLE_NAME, values, selection, selectionArgs)
            }
        } else {
            throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Delete not supported")
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.$PROVIDER_NAME.$TABLE_NAME"
    }
}

fun updateConsumerAppStatus(context: Context, status: Int) {
    val authority = "com.lmev.rider.dev.statusprovider"
    val uri = Uri.parse("content://$authority/${ConsumerAppStatusProvider.TABLE_NAME}")

    val values = ContentValues().apply {
        put(ConsumerAppStatusProvider.COLUMN, status)
    }

    context.contentResolver.update(uri, values, "id = ?", arrayOf("1"))
}

fun getConsumerAppStatus(context: Context): Int {
    val authority = "com.lmev.rider.dev.statusprovider"
    val uri = Uri.parse("content://$authority/${ConsumerAppStatusProvider.TABLE_NAME}")

    val cursor: Cursor? = context.contentResolver.query(uri, arrayOf(ConsumerAppStatusProvider.COLUMN), "id = ?", arrayOf("1"), null)

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getInt(it.getColumnIndexOrThrow(ConsumerAppStatusProvider.COLUMN))
        }
    }
    return -1
}


