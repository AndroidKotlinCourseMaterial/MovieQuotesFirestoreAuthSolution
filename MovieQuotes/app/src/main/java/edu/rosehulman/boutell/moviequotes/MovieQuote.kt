package edu.rosehulman.boutell.moviequotes

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class MovieQuote(
    var quote: String = "",
    var movie: String = "",
    var showDark: Boolean = false) {
    @get:Exclude var id = ""
    @ServerTimestamp var lastTouched: Timestamp? = null

    companion object {
        const val LAST_TOUCHED_KEY = "lastTouched"

        fun fromSnapshot(snapshot: DocumentSnapshot): MovieQuote {
            val movieQuote = snapshot.toObject(MovieQuote::class.java)!!
            movieQuote.id = snapshot.id
            return movieQuote
        }
    }
}
