package org.sea9.android.bookmarks.details

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView

import kotlinx.android.synthetic.main.details_main.*
import org.sea9.android.bookmarks.MainContext
import org.sea9.android.bookmarks.R

class BookmarkDetails : AppCompatActivity() {
	companion object {
		const val TAG = "bookmarks.details"
		const val PREFIX = "http"
	}

	private lateinit var cntxFrag: MainContext
	private lateinit var txtTitle: TextView
	private lateinit var txtUrl: TextView
	private lateinit var recycler: RecyclerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.details_main)
		setSupportActionBar(toolbar)

		cntxFrag = MainContext.getInstance(supportFragmentManager)

		fab.setOnClickListener { view ->
			if (!txtUrl.text.startsWith(PREFIX)) {
				Snackbar.make(view, getString(R.string.msg_not_http), Snackbar.LENGTH_LONG)
					.setAction("Action", null).show()
			} else {
				// TODO TEMP: the following should be code to save the bookmark
				Snackbar.make(view, "TEMP!!! ${recycler.adapter?.itemCount} tags retrieved", Snackbar.LENGTH_LONG)
					.setAction("Action", null).show()
			}
		}

		txtTitle = findViewById(R.id.title)
		txtUrl = findViewById(R.id.url)
		recycler = findViewById(R.id.tags)
		recycler.setHasFixedSize(true) // improve performance since content changes do not affect layout size of the RecyclerView
		recycler.layoutManager = LinearLayoutManager(this) // use a linear layout manager

		handleIncomingIntent(intent)
	}

	override fun onResume() {
		super.onResume()
		Log.w(TAG, "onResume()")

		recycler.adapter = cntxFrag.tagAdaptor

		Handler().postDelayed({
			cntxFrag.tagAdaptor.notifyDataSetChanged()
		}, 50)
	}

	@SuppressLint("SetTextI18n")
	private fun handleIncomingIntent(intent: Intent?) {
		Log.w(TAG, "handleIncomingIntent()")
		if ((intent == null) || (intent.extras == null) ||
			!intent.extras!!.containsKey(Intent.EXTRA_SUBJECT) ||
			!intent.extras!!.containsKey(Intent.EXTRA_TEXT)) {
			finish() // Nothing to record, close the activity
			return
		}

		txtTitle.text = intent.extras!!.get(Intent.EXTRA_SUBJECT)?.toString()
		txtUrl.text = intent.extras!!.get(Intent.EXTRA_TEXT)?.toString()
	}
}