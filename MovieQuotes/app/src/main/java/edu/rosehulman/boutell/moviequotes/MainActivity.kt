package edu.rosehulman.boutell.moviequotes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    SplashFragment.OnLoginButtonPressedListener {

    override fun onLoginButtonPressed() {
        launchLoginUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, SplashFragment())
        ft.commit()



        fab.setOnClickListener {
            // adapter.showAddEditDialog()
        }

    }

    private val RC_SIGN_IN = 1

    private fun launchLoginUI() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

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
                val user = FirebaseAuth.getInstance().currentUser!!
                Log.d(Constants.TAG, "UID: ${user.uid}")
                Log.d(Constants.TAG, "Name: ${user.displayName}")
                Log.d(Constants.TAG, "Email: ${user.email}")
                Log.d(Constants.TAG, "Photo: ${user.photoUrl}")
                Log.d(Constants.TAG, "Phone: ${user.phoneNumber}")
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
                getWhichSettings()
                true
            }
            R.id.action_clear -> {
                confirmClear()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
