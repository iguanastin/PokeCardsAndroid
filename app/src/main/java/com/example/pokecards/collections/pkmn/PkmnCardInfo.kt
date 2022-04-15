package com.example.pokecards.collections.pkmn

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

data class PkmnCardInfo(
    val id: String,
    val name: String,
    val supertype: String,
    val subtypes: String?,
    val level: Int?,
    val hp: Int?,
    val setId: String?,
    val setName: String?,
    val setTotal: Int?,
    val number: String?,
    val imageSmall: String?,
    val imageLarge: String?,
    val tcgpURL: String?,
    val cmURL: String?,
    val rules: List<String>?,
    val attacks: List<String>?,
    val flavorText: String?
) {

    fun insertIntoDatabase(db: SQLiteDatabase) {
        db.insert(
            "cards", null, contentValuesOf(
                Pair("name", name),
                Pair("supertype", supertype),
                Pair("subtypes", subtypes),
                Pair("level", level),
                Pair("hp", hp),
                Pair("setid", setId),
                Pair("setname", setName),
                Pair("settotal", setTotal),
                Pair("number", number),
                Pair("imagesmall", imageSmall),
                Pair("imagelarge", imageLarge),
                Pair("tcgpurl", tcgpURL),
                Pair("number", number),
                Pair("cmurl", cmURL),
                Pair("rules", rules?.joinToString("\\")),
                Pair("attacks", attacks?.joinToString("\\")),
                Pair("flavortext", flavorText)
            )
        )
    }

    fun updateInDatabase(db: SQLiteDatabase) {
        db.update(
            "cards",
            contentValuesOf(
                Pair("name", name),
                Pair("supertype", supertype),
                Pair("subtypes", subtypes),
                Pair("level", level),
                Pair("hp", hp),
                Pair("setid", setId),
                Pair("setname", setName),
                Pair("settotal", setTotal),
                Pair("number", number),
                Pair("imagesmall", imageSmall),
                Pair("imagelarge", imageLarge),
                Pair("tcgpurl", tcgpURL),
                Pair("number", number),
                Pair("cmurl", cmURL),
                Pair("rules", rules?.joinToString("\\")),
                Pair("attacks", attacks?.joinToString("\\")),
                Pair("flavortext", flavorText)
            ),
            "id=?",
            arrayOf(id)
        )
    }

    companion object {
        fun fromDatabaseCursor(c: Cursor): PkmnCardInfo {
            return PkmnCardInfo(
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getStringOrNull(3),
                c.getIntOrNull(4),
                c.getIntOrNull(5),
                c.getStringOrNull(6),
                c.getStringOrNull(7),
                c.getIntOrNull(8),
                c.getStringOrNull(9),
                c.getStringOrNull(10),
                c.getStringOrNull(11),
                c.getStringOrNull(12),
                c.getStringOrNull(13),
                c.getStringOrNull(14)?.split("\\"),
                c.getStringOrNull(15)?.split("\\"),
                c.getStringOrNull(16)
            )
        }
    }

}