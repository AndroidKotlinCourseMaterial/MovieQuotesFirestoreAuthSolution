package edu.rosehulman.boutell.moviequotes

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.dialog_add_edit_quote.view.*

class MovieQuoteAdapter(val context: Context, uid: String) : RecyclerView.Adapter<MovieQuoteViewHolder>() {
    private val movieQuotes = ArrayList<MovieQuote>()
    private val movieQuotesRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.USERS_COLLECTION)
        .document(uid)
        .collection(Constants.QUOTES_COLLECTION)
    private lateinit var listenerRegistration: ListenerRegistration

    fun addSnapshotListener() {
        listenerRegistration = movieQuotesRef
            .orderBy(MovieQuote.LAST_TOUCHED_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(Constants.TAG, "listen error", e)
                } else {
                    processSnapshotChanges(querySnapshot!!)
                }
            }
    }

    private fun processSnapshotChanges(querySnapshot: QuerySnapshot) {
        // Snapshots has documents and documentChanges which are flagged by type,
        // so we can handle C,U,D differently.
        for (documentChange in querySnapshot.documentChanges) {
            val movieQuote = MovieQuote.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    Log.d(Constants.TAG, "Adding $movieQuote")
                    movieQuotes.add(0, movieQuote)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
                    Log.d(Constants.TAG, "Removing $movieQuote")
                    val index = movieQuotes.indexOfFirst { it.id == movieQuote.id }
                    movieQuotes.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
                    Log.d(Constants.TAG, "Modifying $movieQuote")
                    val index = movieQuotes.indexOfFirst { it.id == movieQuote.id }
                    movieQuotes[index] = movieQuote
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, index: Int): MovieQuoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_view, parent, false)
        return MovieQuoteViewHolder(view, this)
    }

    override fun onBindViewHolder(
        viewHolder: MovieQuoteViewHolder,
        index: Int
    ) {
        viewHolder.bind(movieQuotes[index])
    }

    override fun getItemCount() = movieQuotes.size

    @SuppressLint("InflateParams")
    fun showAddEditDialog(position: Int = -1) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add a quote")
        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_add_edit_quote, null, false
        )
        builder.setView(view)
        builder.setIcon(android.R.drawable.ic_input_add)
        if (position >= 0) {
            view.dialog_edit_text_quote.setText(movieQuotes[position].quote)
            view.dialog_edit_text_movie.setText(movieQuotes[position].movie)
        }

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val quote = view.dialog_edit_text_quote.text.toString()
            val movie = view.dialog_edit_text_movie.text.toString()
            if (position < 0) {
                add(MovieQuote(quote, movie))
            } else {
                edit(position, quote, movie)
            }

        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setNeutralButton("Remove") { _, _ ->
            remove(position)
        }
        builder.show()
    }

    private fun add(movieQuote: MovieQuote) {
        movieQuotesRef.add(movieQuote)
    }

    private fun edit(position: Int, quote: String, movie: String) {
        movieQuotes[position].quote = quote
        movieQuotes[position].movie = movie
        movieQuotesRef.document(movieQuotes[position].id).set(movieQuotes[position])
    }

    private fun remove(position: Int) {
        movieQuotesRef.document(movieQuotes[position].id).delete()
    }

    fun selectMovieQuote(position: Int) {
        val mq =movieQuotes[position]
        mq.showDark = !mq.showDark
        movieQuotesRef.document(mq.id).set(mq)
    }
}