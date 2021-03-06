package org.sea9.android.bookmarks

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import org.sea9.android.bookmarks.data.DbContract
import org.sea9.android.bookmarks.data.DbHelper
import org.sea9.android.bookmarks.details.TagsAdaptor

class MainContext : Fragment(), DbHelper.Caller, BookmarkAdaptor.Caller, TagsAdaptor.Caller {
	companion object {
		const val TAG = "bookmarks.context"

		fun getInstance(sfm: FragmentManager): MainContext {
			var instance = sfm.findFragmentByTag(TAG) as MainContext?
			if (instance == null) {
				instance = MainContext()
				sfm.beginTransaction().add(instance, TAG).commit()
			}
			return instance
		}
	}

	private var dbHelper: DbHelper? = null
	private fun setDbHelper(helper: DbHelper) {
		dbHelper = helper
	}
	override fun getDbHelper(): DbHelper? {
		return dbHelper
	}
	override fun isDbReady(): Boolean {
		return ((dbHelper != null) && dbHelper!!.ready)
	}

	lateinit var adaptor: BookmarkAdaptor
		private set

	lateinit var tagAdaptor: TagsAdaptor
		private set

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "onCreate()")
		retainInstance = true

		adaptor = BookmarkAdaptor(this)
		tagAdaptor = TagsAdaptor(this)
	}

	override fun onResume() {
		super.onResume()
		Log.d(TAG, "onResume()")
		if (!isDbReady()) {
			initDb()
		} else {
			adaptor.populateCache()
			tagAdaptor.populateCache()
		}
	}

	/*===================================================
	 * @see org.sea9.android.secret.data.DbHelper.Caller
	 */
	override fun onReady() {
		Log.d(TAG, "db ready")
		activity?.runOnUiThread {
			adaptor.populateCache()
			tagAdaptor.populateCache()
		}
	}

	private fun initDb() {
		Log.d(TAG, "init db")
		AsyncDbInitTask(this).execute()
	}
	class AsyncDbInitTask (private val caller: MainContext): AsyncTask<Void, Void, Void>() {
		override fun doInBackground(vararg params: Void?): Void? {
			val helper = DbHelper(caller)
			caller.setDbHelper(helper)
			helper.writableDatabase.execSQL(DbContract.SQL_CONFIG)
			return null
		}
	}
}