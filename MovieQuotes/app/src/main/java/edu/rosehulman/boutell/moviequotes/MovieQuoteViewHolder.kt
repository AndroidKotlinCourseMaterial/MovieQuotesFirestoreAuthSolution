package edu.rosehulman.boutell.moviequotes

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.row_view.view.*

class MovieQuoteViewHolder(itemView: View, private val adapter: MovieQuoteAdapter): RecyclerView.ViewHolder(itemView) {
    private val quoteTextView: TextView = itemView.findViewById(R.id.quote_text_view)
    private val movieTextView: TextView = itemView.findViewById(R.id.movie_text_view)
    private var cardView: CardView

    init {
        itemView.setOnClickListener {
            adapter.showAddEditDialog(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.selectMovieQuote(adapterPosition)
            true
        }
        cardView = itemView.row_card_view
    }

    fun bind(movieQuote: MovieQuote) {
        quoteTextView.text = movieQuote.quote
        movieTextView.text = movieQuote.movie

        if (movieQuote.showDark) {
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(adapter.context, R.color.colorAccent)
            )
        } else {
            cardView.setCardBackgroundColor(Color.WHITE)
        }
    }
}