package com.example.pokecards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
import com.example.pokecards.collections.pkmn.PkmnAPICard
import com.example.pokecards.databinding.PkmnCardsImportRecyclerBinding
import com.squareup.picasso.Picasso

class PkmnCardsImportRecyclerAdapter(private val onAdd: (card: PkmnAPICard) -> Unit) :
    RecyclerView.Adapter<PkmnCardsImportRecyclerAdapter.PkmnCardsImportRecyclerViewHolder>() {

    private var cards: List<PkmnAPICard> = listOf()

    class PkmnCardsImportRecyclerViewHolder(val binding: PkmnCardsImportRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)


    fun setCards(cards: List<PkmnAPICard>?) {
        this.cards = cards ?: listOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PkmnCardsImportRecyclerViewHolder {
        return PkmnCardsImportRecyclerViewHolder(
            PkmnCardsImportRecyclerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PkmnCardsImportRecyclerViewHolder, position: Int) {
        val card = cards[position]

        holder.binding.apply {
            imageButton.setOnClickListener { onAdd(card) }
            Picasso.get().load(card.imageLarge).into(imageView2)
        }
    }

    override fun getItemCount(): Int {
        return cards.size
    }

}