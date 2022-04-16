package com.example.pokecards

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokecards.collections.pkmn.PkmnAPICard

class PkmnCardsImportViewModel: ViewModel() {

    val cards: MutableLiveData<List<PkmnAPICard>?> by lazy { MutableLiveData() }

}