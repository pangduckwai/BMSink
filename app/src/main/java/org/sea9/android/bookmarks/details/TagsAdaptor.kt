package org.sea9.android.bookmarks.details

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.sea9.android.bookmarks.R
import org.sea9.android.bookmarks.data.DbContract
import org.sea9.android.bookmarks.data.DbHelper
import org.sea9.android.bookmarks.data.TagRecord

class TagsAdaptor(ctx: Caller): RecyclerView.Adapter<TagsAdaptor.ViewHolder>() {
	companion object {
		const val TAG = "bookmarks.tags_adaptor"
	}

	private val caller: Caller = ctx
	private var cache: List<TagRecord> = mutableListOf()
	private lateinit var recyclerView: RecyclerView

	private val index = LongSparseArray<Int>()

	val selectedTags = mutableListOf<Long>()
	fun isSelected(position: Int): Boolean {
		return selectedTags.contains(cache[position].rid)
	}
	fun selectTags(list: List<Long>) {
		selectedTags.clear()
		selectedTags.addAll(list)
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
		val item = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false) as TextView
		item.setOnClickListener {
			val position = recyclerView.getChildLayoutPosition(it)
			val index = selectedTags.indexOf(cache[position].rid)
			if (index >= 0)
				selectedTags.removeAt(index)
			else
				selectedTags.add(cache[position].rid)
			caller.tagsUpdated()
			notifyDataSetChanged()
		}
		return ViewHolder(item)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.itemView.isSelected = isSelected(position)
		holder.tag.text = cache[position].tag
	}

	override fun getItemCount(): Int {
		return cache.size
	}
	//=====================================================

	fun populateCache() {
		cache = DbContract.Tags.select(caller.getDbHelper())
		cache.forEachIndexed { i, record ->
			index.put(record.rid, i)
		}
	}

	/**
	 * Called after a new tag is inserted.
	 * @param bid db ID of the new tag.
	 * @return position of the new tag in the detail dialog recycler view.
	 */
	fun onInserted(bid: Long): Int {
		val position = cache.indexOfFirst {
			it.rid == bid
		}
		if ((position >= 0) && !isSelected(position)) {
			selectedTags.add(bid)
			caller.tagsUpdated()
		}
		notifyDataSetChanged()
		return position
	}

	/*=============
	 * View holder
	 */
	class ViewHolder(view: TextView): RecyclerView.ViewHolder(view) {
		val tag: TextView = view
	}
	//=============

	/*==========================================
	 * Access interface to the ContextFragment
	 */
	interface Caller {
		fun tagsUpdated()
		fun getDbHelper(): DbHelper
	}
}