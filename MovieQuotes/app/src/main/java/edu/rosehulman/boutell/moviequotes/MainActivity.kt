package edu.rosehulman.boutell.moviequotes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MovieQuoteAdapter
    private lateinit var settingsRef: DocumentReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        launchLoginUI()

        settingsRef = FirebaseFirestore
            .getInstance()
            .collection("settings").document("settings")

        fab.setOnClickListener {
            // For testing
            // adapter.add(MovieQuote("Quote", "Movie"))
            adapter.showAddEditDialog()
        }

        settingsRef.addSnapshotListener { document, exception ->
            if (exception != null) {
                Log.w(Constants.TAG, "listen error", exception)
                return@addSnapshotListener
            }
            author_text_view.text = (document?.get("author") ?: "") as String
        }

        adapter = MovieQuoteAdapter(this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = adapter
        adapter.addSnapshotListener()
    }

    private val RC_SIGN_IN = 1

    private fun launchLoginUI() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        val loginIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_custom)
            .build()

        startActivityForResult(loginIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {
                val response = IdpResponse.fromResultIntent(data)
                if (response == null) {
                    Log.e(Constants.TAG, "User pressed back: $response")
                }
                Log.e(Constants.TAG, "Login error: ${response?.error?.errorCode}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_increase_font_size -> {
                changeFontSize(4)
                true
            }
            R.id.action_decrease_font_size -> {
                changeFontSize(-4)
                true
            }
            R.id.action_settings -> {
                updateAppTitle()
//                getWhichSettings()
                true
            }
            R.id.action_clear -> {
                confirmClear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateAppTitle() {
        settingsRef.get()
            .addOnSuccessListener { document ->
                var author = (document["author"] ?: "") as String
                val builder = AlertDialog.Builder(this)
                builder.setTitle("App Author")
                val authorEditText = EditText(this)
                authorEditText.setText(author)
                authorEditText.hint = "App author's name"
                builder.setView(authorEditText)
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    author = authorEditText.text.toString()
                    Log.d(Constants.TAG, "Author: $author")
                    val map = mapOf<String, Any>(Pair("author", author))
                    settingsRef.set(map, SetOptions.merge())
                }
                builder.create().show()
            } .addOnFailureListener {exception ->
                Log.e(Constants.TAG, "Get error: $exception")
            }
    }

    private fun changeFontSize(delta: Int) {
        // Increase the font size by delta sp
//        var currentSize = quote_text_view.textSize / resources.displayMetrics.scaledDensity
//        currentSize += delta
//        quote_text_view.textSize = currentSize
//        movie_text_view.textSize = currentSize
    }

    private fun confirmClear() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.confirm_delete_title))
        builder.setMessage(getString(R.string.confirm_delete_message))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            // updateQuote(defaultMovieQuote)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    private fun getWhichSettings() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_which_settings_title))
        // For others, see https://developer.android.com/reference/android/provider/Settings
        builder.setItems(R.array.settings_types) { _, index ->
            var actionConstant = when (index) {
                0 -> Settings.ACTION_SOUND_SETTINGS
                1 -> Settings.ACTION_SEARCH_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            startActivity(Intent(actionConstant))
        }
        builder.create().show()
    }

}
