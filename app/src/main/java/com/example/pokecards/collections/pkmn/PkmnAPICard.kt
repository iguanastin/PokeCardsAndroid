package com.example.pokecards.collections.pkmn

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.contentValuesOf
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

data class PkmnAPICard(
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
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as Int?,
        parcel.readValue(Int::class.java.classLoader) as Int?,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as Int?,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString()
    )

    constructor(c: Cursor) : this(
        c.getString(0),
        c.getString(2),
        c.getString(1),
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(supertype)
        parcel.writeString(subtypes)
        parcel.writeValue(level)
        parcel.writeValue(hp)
        parcel.writeString(setId)
        parcel.writeString(setName)
        parcel.writeValue(setTotal)
        parcel.writeString(number)
        parcel.writeString(imageSmall)
        parcel.writeString(imageLarge)
        parcel.writeString(tcgpURL)
        parcel.writeString(cmURL)
        parcel.writeStringList(rules)
        parcel.writeStringList(attacks)
        parcel.writeString(flavorText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PkmnAPICard> {
        override fun createFromParcel(parcel: Parcel): PkmnAPICard {
            return PkmnAPICard(parcel)
        }

        override fun newArray(size: Int): Array<PkmnAPICard?> {
            return arrayOfNulls(size)
        }
    }

}