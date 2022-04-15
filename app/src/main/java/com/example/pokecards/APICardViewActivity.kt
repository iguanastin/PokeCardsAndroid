package com.example.pokecards

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.contentValuesOf
import com.example.pokecards.collections.CollectionDatabaseHelper
import com.example.pokecards.collections.pkmn.PkmnAPICard
import com.example.pokecards.databinding.ActivityApiCardViewBinding
import java.net.URL
import java.util.concurrent.Executors

class APICardViewActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityApiCardViewBinding
    private val executor = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityApiCardViewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val card = intent.getParcelableExtra<PkmnAPICard>("card")
        if (card == null) {
            finish()
            Log.e(TAG, "Tried to start activity with no card")
            return
        }
        Log.i(TAG, card.toString())

        viewBinding.titleText.text = card.name

        viewBinding.addButton.setOnClickListener {
            viewBinding.addButton.isEnabled = false
            viewBinding.cancelButton.isEnabled = false
            executor.submit {
                val db = CollectionDatabaseHelper(this)
                db.insertPkmnCard(card)
                db.close()
                runOnUiThread { finish() }
            }
        }
        viewBinding.cancelButton.setOnClickListener {
            finish()
        }

        executor.submit {
            val input = URL(card.imageLarge).openStream()
            val bmp = BitmapFactory.decodeStream(input)
            input.close()
            runOnUiThread { viewBinding.imageView.setImageBitmap(bmp) }
        }
    }


    companion object {
        private const val TAG = "APICardViewActivity"
    }

}