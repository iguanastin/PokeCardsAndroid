package com.example.pokecards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.pokecards.collections.Collections
import com.example.pokecards.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        supportFragmentManager.beginTransaction().apply {
            add(viewBinding.pkmnFrameLayout.id, CardsMenuFragment(Collections.POKEMON))
            add(viewBinding.yugiohFrameLayout.id, CardsMenuFragment(Collections.YUGIOH))
            commit()
        }

        viewBinding.button.setOnClickListener {
            startActivity(Intent(this, PhotoActivity::class.java))
        }
    }

}

// https://docs.pokemontcg.io/api-reference/cards/get-card
// https://github.com/PokemonTCG/pokemon-tcg-sdk-kotlin
// https://developers.google.com/ml-kit/custom-models
// https://www.tensorflow.org/lite/tutorials/model_maker_object_detection
// https://www.reddit.com/r/PokemonTCG/
// https://developers.google.com/ml-kit/vision/text-recognition/android