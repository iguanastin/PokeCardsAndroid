package com.example.pokecards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pokecards.collections.Collections
import com.example.pokecards.databinding.ActivityViewCollectionBinding
import com.example.pokecards.databinding.CardsMenuFragmentBinding

class CardsMenuFragment(private val collection: Collections) : Fragment() {

    private lateinit var viewModel: CardsMenuViewModel

    private lateinit var viewBinding: CardsMenuFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = CardsMenuFragmentBinding.inflate(inflater, container, false)

        viewBinding.textView.text = collection.displayName
        viewBinding.root.setOnClickListener {
            startActivity(Intent(context, ViewCollectionActivity::class.java).putExtra(ViewCollectionActivity.COLLECTION_EXTRA_NAME, collection.id))
        }

        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[CardsMenuViewModel::class.java]
        // TODO: Use the ViewModel
    }

}