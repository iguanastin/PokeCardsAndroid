package com.example.pokecards

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pokecards.databinding.ActivityMainBinding
import com.example.pokecards.databinding.ActivityTempCardViewBinding
import java.net.URL
import java.util.concurrent.Executors

class TempCardViewActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityTempCardViewBinding
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTempCardViewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val url = intent.getStringExtra("url")

        executor.submit {
            val input = URL(url).openStream()
            val bmp = BitmapFactory.decodeStream(input)
            runOnUiThread { viewBinding.imageView.setImageBitmap(bmp) }
        }
    }
}