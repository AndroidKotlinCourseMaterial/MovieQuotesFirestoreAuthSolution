package edu.rosehulman.boutell.moviequotes

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.dialog_add_edit_quote.view.*

class MovieQuoteAdapter(val context: Context, val uid: String) : RecyclerView.Adapter<MovieQuoteViewHolder>() {
    private val movieQuotes = ArrayList<MovieQuote>()
    private val movieQuotesRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.USERS_COLLECTION)
        .document(uid)
        .collection(Constants.QUOTES_COLLECTION)
    private lateinit var listenerRegistration: ListenerRegistration

    fun addSnapshotListener() {
        listenerRegistration = movieQuotesRef

            .orderBy(MovieQuote.LAST_TOUCHED_KEY)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(Constants.TAG, "listen error", e)
                    return@addSnapshotListener
                }
//                populateLocalQuotes(querySnapshot!!)
                processSnapshotChanges(querySnapshot!!)
            }
    }

    fun removeSnapshotListener() {
        Log.d(Constants.TAG, "Removing listener")
        listenerRegistration.remove()
        //movieQuotes.clear()
    }

    fun populateLocalQuotes(querySnapshot: QuerySnapshot) {
        // First attempt: just get them all.
        Log.d(Constants.TAG, "Populating")
        movieQuotes.clear()
        for (document in querySnapshot.documents) {
            // This is a very convenient helper method.
            Log.d(Constants.TAG, "document: $document")
            movieQuotes.add(MovieQuote.fromSnapshot(document))
        }
        notifyDataSetChanged()
        if (movieQuotes.isNotEmpty()) {
            Log.d(Constants.TAG, "ID of first: " + movieQuotes[0].id)
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
//                    movieQuotes.remove(movieQuote)
//                    notifyDataSetChanged()
                    for ((k, mq) in movieQuotes.withIndex()) {
                        if (mq.id == movieQuote.id) {
                            movieQuotes.removeAt(k)
                            notifyItemRemoved(k)
                            break
                        }
                    }
                }
                DocumentChange.Type.MODIFIED -> {
                    Log.d(Constants.TAG, "Modifying $movieQuote")
                    for ((k, mq) in movieQuotes.withIndex()) {
                        if (mq.id == movieQuote.id) {
                            movieQuotes[k] = movieQuote
                            notifyItemChanged(k)
                            break
                        }
                    }
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