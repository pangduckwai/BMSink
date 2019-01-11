package org.sea9.android.bookmarks.data

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.toTagRecords(): MutableSet<TagRecord> {
	val result = mutableSetOf<TagRecord>()
	for (i in 0 until length()) {
		result.add(TagRecord(getJSONObject(i)))
	}
	return result
}

data class BookmarkRecord(
	  var rid: Long
	, var url: String
	, var title: String
	, var category: MutableSet<TagRecord>?
	, var modified: Long?
) {
	companion object {
		const val RID = "id"
		const val URL = "url"
		const val TTL = "title"
		const val CAT = "category"
		const val MOD = "modified"
	}

	constructor(json: JSONObject) : this(
		json.getLong(RID),
		json.getString(URL),
		json.getString(TTL),
		json.optJSONArray(CAT)?.toTagRecords(),
		json.optLong(MOD)
	)
	constructor(json: String) : this(JSONObject(json))

	override fun toString(): String {
		val result = JSONObject()
		result.put(RID, rid)
		result.put(URL, url)
		result.put(TTL, title)
		if (category != null) result.put(CAT, category)
		if (modified != null) result.put(MOD, modified!!)
		return result.toString()
	}

	override fun equals(other: Any?): Boolean {
		val value = other as BookmarkRecord
		return (url == value.url)
	}

	override fun hashCode(): Int {
		return url.hashCode()
	}
}