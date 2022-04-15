package com.example.pokecards.collections.pkmn

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.getIntOrNull
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class PkmnAPIDatabase(private val context: Context) {

    private val dbFile = context.getDatabasePath(DATABASE_NAME)
    val db: SQLiteDatabase

    init {
        if (!dbFile.exists()) copyDatabaseFromAssets()
        val tempdb = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)

        val c = tempdb.rawQuery("SELECT version FROM meta", null)
        c.moveToFirst()
        val version = c.getIntOrNull(0)
        c.close()

        db = if (version == null || version < DATABASE_VERSION) {
            tempdb.close()
            copyDatabaseFromAssets()
            SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        } else {
            tempdb
        }
    }

    private fun copyDatabaseFromAssets() {
        Log.i(TAG, "Copying $DATABASE_NAME from assets to: ${dbFile.path}")

        var input: InputStream? = null
        var output: OutputStream? = null

        try {
            input = context.assets.open(DATABASE_ASSET)
            output = FileOutputStream(dbFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
        } finally {
            output?.close()
            input?.close()
        }
    }

    fun close() {
        db.close()
    }

    companion object {
        const val TAG = "PkmnCardDatabase"
        const val DATABASE_VERSION = 1
        const val DATABASE_ASSET = "pkmncards.sqlite3"
        const val DATABASE_NAME = "pkmncards.sqlite3"
    }

}