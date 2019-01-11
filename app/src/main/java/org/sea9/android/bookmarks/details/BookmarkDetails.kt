package org.sea9.android.bookmarks.details

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.widget.TextView

import kotlinx.android.synthetic.main.details_main.*
import org.sea9.android.bookmarks.MainContext
import org.sea9.android.bookmarks.R

class BookmarkDetails : AppCompatActivity() {
	companion object {
		const val TAG = "bookmarks.details"
	}

	private lateinit var cntxFrag: MainContext
	private lateinit var tempText: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.details_main)
		setSupportActionBar(toolbar)

		cntxFrag = MainContext.getInstance(supportFragmentManager)

		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}

		tempText = findViewById(R.id.bookmark)

		handleIncomingIntent(intent)
	}

	@SuppressLint("SetTextI18n")
	private fun handleIncomingIntent(intent: Intent?) {
		Log.w(TAG, "handleIncomingIntent()")
		var text =
				"Action\n${intent?.action}\n\n" +
				"Type\n${intent?.type}\n\n" +
				"Component\n${intent?.component?.flattenToString()}\n\n" +
				"Data\n${intent?.dataString}\n\n"

		if (intent != null) {
			if (intent.categories != null) {
				text += "Categories\n"
				for (cat in intent.categories) {
					text += (cat + "\n")
				}
				text += "\n"
			} else {
				text += "No category\n\n"
			}

			if (intent.extras != null) {
				text != "Extras\n"
				for (xtr in intent.extras.keySet()) {
					text += (xtr + "\n" + intent.extras.get(xtr).toString() + "\n\n")
				}
			} else {
				text += "No extras\n\n"
			}
		}

		tempText.text = text
	}
}
