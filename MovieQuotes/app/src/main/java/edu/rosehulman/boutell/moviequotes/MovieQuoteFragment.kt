package edu.rosehulman.boutell.moviequotes


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

private const val ARG_UID = "UID"

class MovieQuoteFragment : Fragment() {
    private var uid: String? = null
    private lateinit var adapter: MovieQuoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
   }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(R.layout.fragment_movie_quote, container, false) as RecyclerView
        adapter = MovieQuoteAdapter(context!!, uid!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        adapter.addSnapshotListener()
        (context as MainActivity).getFab().setOnClickListener {
            adapter.showAddEditDialog()
        }
        return recyclerView
    }

    companion object {
        @JvmStatic
        fun newInstance(uid: String) =
            MovieQuoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
    }
}
