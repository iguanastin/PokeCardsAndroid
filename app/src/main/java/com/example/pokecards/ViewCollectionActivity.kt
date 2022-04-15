package com.example.pokecards

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pokecards.collections.CollectionDatabaseHelper
import com.example.pokecards.collections.Collections
import com.example.pokecards.databinding.ActivityViewCollectionBinding

class ViewCollectionActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityViewCollectionBinding

    private var collectionDB: CollectionDatabaseHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityViewCollectionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        when (intent.getStringExtra(COLLECTION_EXTRA_NAME)) {
            Collections.POKEMON.id -> {
                // TODO init for pokemon collection
            }
        }
    }

    companion object {
        const val COLLECTION_EXTRA_NAME = "collection"
    }

}