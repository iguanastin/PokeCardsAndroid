package com.example.pokecards

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokecards.collections.CollectionDatabaseHelper
import com.example.pokecards.collections.pkmn.PkmnAPICard
import com.example.pokecards.databinding.ActivityPkmnCardImportBinding
import java.util.concurrent.Executors

class PkmnCardsImportActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPkmnCardImportBinding
    private val executor = Executors.newSingleThreadExecutor()
    private val dbHelper: CollectionDatabaseHelper by lazy { CollectionDatabaseHelper(this) }

    private lateinit var viewModel: PkmnCardsImportViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[PkmnCardsImportViewModel::class.java]
        viewBinding = ActivityPkmnCardImportBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModel.apply {
            if (cards.value == null) cards.value =
                intent.getParcelableArrayExtra("cards")?.map { it as PkmnAPICard }
            Log.i(TAG, "Displaying Cards: " + cards.value?.map { it.id })
            cards.observe(this@PkmnCardsImportActivity) { cards ->
                runOnUiThread {
                    (viewBinding.cardRecycler.adapter as PkmnCardsImportRecyclerAdapter).setCards(
                        cards
                    )
                }
            }
        }

        viewBinding.apply {
            cardRecycler.adapter =
                PkmnCardsImportRecyclerAdapter { card ->
                    executor.submit {
                        dbHelper.insertPkmnCard(card)
                        runOnUiThread { finish() }
                    }
                }.apply {
                    setCards(viewModel.cards.value)
                }
            cardRecycler.layoutManager = LinearLayoutManager(this@PkmnCardsImportActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        dbHelper.close()
    }


    companion object {
        private const val TAG = "APICardViewActivity"
    }

}