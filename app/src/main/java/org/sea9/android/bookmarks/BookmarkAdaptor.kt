package org.sea9.android.bookmarks

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.sea9.android.bookmarks.data.BookmarkRecord
import org.sea9.android.bookmarks.data.DbContract
import org.sea9.android.bookmarks.data.DbHelper

class BookmarkAdaptor(ctx: Caller): RecyclerView.Adapter<BookmarkAdaptor.ViewHolder>() {
	companion object {
		const val TAG = "bookmarks.adaptor"
	}

	private val caller: Caller = ctx
	private var cache: List<BookmarkRecord> = mutableListOf()
	private lateinit var recyclerView: RecyclerView

	private var selectedPos = -1
	private fun isSelected(position: Int): Boolean {
		return (selectedPos == position)
	}

	/*=====================================================
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	override fun onAttachedToRecyclerView(recycler: RecyclerView) {
		super.onAttachedToRecyclerView(recycler)
		Log.d(TAG, "onAttachedToRecyclerView")
		recyclerView = recycler
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val item = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
		item.setOnClickListener {
			val position = recyclerView.getChildLayoutPosition(it)
			if (position == selectedPos) {
				selectedPos = -1 //Un-select
			} else if (cache.size > position) {
				selectedPos = position
			}
			notifyDataSetChanged()
		}
		return ViewHolder(item)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.itemView.isSelected = isSelected(position)
		val selected = cache[position]
		holder.ttl.text = selected.title
		holder.url.text = selected.url
	}

	override fun getItemCount(): Int {
		return cache.size
	}
	//=====================================================

	fun populateCache() {
		if (caller.isDbReady()) {
			cache = DbContract.Bookmarks.select(caller.getDbHelper()!!) as MutableList<BookmarkRecord>
			notifyDataSetChanged()
		}
		Log.d(TAG, "populateCache ${cache.size}")
	}

	/*=============
	 * View holder
	 */
	class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
		val ttl: TextView = view.findViewById(R.id.title)
		val url: TextView = view.findViewById(R.id.url)
	}

	/*=========================================
	 * Access interface to the ContextFragment
	 */
	interface Caller {
		fun isDbReady(): Boolean
		fun getDbHelper(): DbHelper?
	}
}