package com.example.pokecards.collections

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.example.pokecards.collections.pkmn.PkmnAPICard
import java.time.Instant

class CollectionDatabaseHelper(context: Context?) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    DATABASE_VERSION,
    SQLiteDatabase.OpenParams.Builder().build()
) {

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("CREATE TABLE pkmncards(id TEXT PRIMARY KEY NOT NULL, name TEXT, supertype TEXT, subtypes TEXT, setid TEXT, thumburl TEXT, largeurl TEXT, tcgpurl TEXT, cmurl TEXT)")
        p0?.execSQL("CREATE TABLE pkmnsets(id TEXT PRIMARY KEY NOT NULL, name TEXT, series TEXT, printedtotal INT, total INT, symbolurl TEXT, logourl TEXT)")
        p0?.execSQL("CREATE TABLE ownedpkmncards(id INTEGER PRIMARY KEY, cardid TEXT, condition TEXT, variant TEXT, time INT)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    private fun hasPkmnCard(card: PkmnAPICard): Boolean {
        var result: Boolean

        readableDatabase.apply {
            rawQuery("SELECT id FROM pkmncards WHERE id=?", arrayOf(card.id)).apply {
                result = count > 0
            }.close()
        }.close()

        return result
    }

    fun insertPkmnCard(card: PkmnAPICard, condition: String? = null, variant: String? = null) {
        writableDatabase.apply {
            if (!hasPkmnCard(card)) {
                insert(
                    "pkmncards", null, contentValuesOf(
                        Pair("id", card.id),
                        Pair("name", card.name),
                        Pair("supertype", card.supertype),
                        Pair("subtypes", card.subtypes),
                        Pair("setid", card.setId),
                        Pair("thumburl", card.imageSmall),
                        Pair("largeurl", card.imageLarge),
                        Pair("tcgpurl", card.tcgpURL),
                        Pair("cmurl", card.cmURL)
                    )
                )
            }

            insert(
                "pkmncards", null, contentValuesOf(
                    Pair("cardid", card.id),
                    Pair("condition", condition),
                    Pair("variant", variant),
                    Pair("time", Instant.now().toEpochMilli())
                )
            )
        }.close()
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "collection.sqlite3"
    }

}