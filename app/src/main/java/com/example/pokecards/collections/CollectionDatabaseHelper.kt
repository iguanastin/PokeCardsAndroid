package com.example.pokecards.collections

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CollectionDatabaseHelper(context: Context?): SQLiteOpenHelper(context, DATABASE_NAME, DATABASE_VERSION, SQLiteDatabase.OpenParams.Builder().build()) {

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE pkmncards(id TEXT PRIMARY KEY NOT NULL, name TEXT, supertype TEXT, subtypes TEXT, setid TEXT, thumburl TEXT, largeurl TEXT, tcgpurl TEXT, cmurl TEXT)", arrayOf())
        p0?.execSQL("CREATE TABLE pkmnsets(id TEXT PRIMARY KEY NOT NULL, name TEXT, series TEXT, printedtotal INT, total INT, symbolurl TEXT, logourl TEXT)", arrayOf())
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "collection.sqlite3"
    }

}