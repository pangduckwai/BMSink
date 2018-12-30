package org.sea9.android.bookmarks

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.app_main.*

class MainActivity : AppCompatActivity() {
	companion object {
		const val TAG = "bookmarks.main"
	}

	private lateinit var contentText: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.app_main)
		setSupportActionBar(toolbar)
		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}

		contentText = findViewById(R.id.content)

		handleIncomingIntent(intent)
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		Log.w(TAG, "onNewIntent!!!")
		handleIncomingIntent(intent)
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
			R.id.action_settings -> true
			else -> super.onOptionsItemSelected(item)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun handleIncomingIntent(intent: Intent?) {
		Log.w(TAG, "Handling incoming intent!")//getCategories() getDataString() getComponent().flattenToString() getType()
		var text =
				"Action\n${intent?.action}\n\n" +
				"Type\n${intent?.type}\n\n" +
//				"Component\n${intent?.component?.flattenToString()}\n\n" +
				"Data\n${intent?.dataString}\n\n"
		if ((intent != null) && (intent.categories != null)) {
			text += "Categories\n"
			for (cat in intent.categories) {
				text += (cat + "\n")
			}
		} else {
			text += "No category"
		}
		contentText.text = text
	}
}
