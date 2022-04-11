package com.example.pokecards

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

class PkmnCardInfo(val id: String, val name: String, val supertype: String, val subtypes: String?, val level: Int?, val hp: Int?, val setId: String?, val setName: String?, val setTotal: Int?, val number: String?, val imageSmall: String?, val imageLarge: String?, val tcgpURL: String?, val cmURL: String?, val rules: List<String>?, val attacks: List<String>?, val flavorText: String?) {

    companion object {
        fun fromDatabaseCursor(c: Cursor): PkmnCardInfo {
            return PkmnCardInfo(c.getString(0), c.getString(1), c.getString(2), c.getStringOrNull(3), c.getIntOrNull(4), c.getIntOrNull(5), c.getStringOrNull(6), c.getStringOrNull(7), c.getIntOrNull(8), c.getStringOrNull(9), c.getStringOrNull(10), c.getStringOrNull(11), c.getStringOrNull(12), c.getStringOrNull(13), c.getStringOrNull(14)?.split("\\"), c.getStringOrNull(15)?.split("\\"), c.getStringOrNull(16))
        }
    }

}