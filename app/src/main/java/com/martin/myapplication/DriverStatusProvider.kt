package com.martin.myapplication

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
class DriverStatusProvider : ContentProvider() {

    companion object {
        private const val PROVIDER_NAME = "com.martin.myapplication.provider"
        private const val URL = "content://$PROVIDER_NAME/driver_status"
        val CONTENT_URI: Uri = Uri.parse(URL)

        private const val DRIVER_STATUS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PROVIDER_NAME, "driver_status", DRIVER_STATUS)
        }
    }

    private lateinit var database: SQLiteDatabase

    override fun onCreate(): Boolean {
        val dbHelper = object : SQLiteOpenHelper(context, "DriverDB", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE driver_status (id INTEGER PRIMARY KEY, status TEXT)")
                db.execSQL("INSERT INTO driver_status (id, status) VALUES (1, 'offline')")
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }
        database = dbHelper.writableDatabase
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return if (uriMatcher.match(uri) == DRIVER_STATUS) {
            database.query("driver_status", arrayOf("status"), null, null, null, null, null)
        } else {
            throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return if (uriMatcher.match(uri) == DRIVER_STATUS) {
            database.update("driver_status", values, selection, selectionArgs)
        } else {
            throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Delete not supported")
    }

    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.$PROVIDER_NAME.driver_status"
    }
}
