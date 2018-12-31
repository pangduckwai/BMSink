package org.sea9.android.bookmarks

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.app_main.*

class MainActivity : AppCompatActivity() {
	companion object {
		const val TAG = "bookmarks.main"
	}

	private lateinit var cntxFrag: MainContext
	private lateinit var recycler: RecyclerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "onCreate()")
		setContentView(R.layout.app_main)
		setSupportActionBar(toolbar)

		cntxFrag = MainContext.getInstance(supportFragmentManager)

		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}

		recycler = findViewById(R.id.recycler_list)
		recycler.setHasFixedSize(true) // improve performance since content changes do not affect layout size of the RecyclerView
		recycler.layoutManager = LinearLayoutManager(this) // use a linear layout manager
	}

	override fun onResume() {
		super.onResume()
		Log.d(TAG, "onResume()")

		recycler.adapter = cntxFrag.adaptor
	}

//	override fun onNewIntent(intent: Intent?) {
//		super.onNewIntent(intent)
//		Log.w(TAG, "onNewIntent()")
//		handleIncomingIntent(intent)
//	}

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

//	private fun initRetainedFragment() {
//		cntxFrag = supportFragmentManager.findFragmentByTag(MainContext.TAG) as MainContext?
//		if (cntxFrag == null) {
//			cntxFrag = MainContext()
//			supportFragmentManager.beginTransaction().add(cntxFrag!!, MainContext.TAG).commit()
//		}
//	}
}
