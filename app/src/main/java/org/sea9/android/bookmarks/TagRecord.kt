package org.sea9.android.bookmarks

import org.json.JSONObject

data class TagRecord(
	  var rid: Long
	, var tag: String
	, var modified: Long?
) {
	companion object {
		const val RID = "id"
		const val TAG = "tag"
		const val MOD = "modified"
	}

	constructor(json: JSONObject) : this(
		json.getLong(RID),
		json.getString(TAG),
		json.optLong(MOD)
	)
	constructor(json: String) : this(JSONObject(json))

	override fun toString(): String {
		val result = JSONObject()
		result.put(RID, rid)
		result.put(TAG, tag)
		if (modified != null) result.put(MOD, modified!!)
		return result.toString()
	}

	override fun equals(other: Any?): Boolean {
		val value = other as TagRecord
		return (tag == value.tag)
	}

	override fun hashCode(): Int {
		return tag.hashCode()
	}
}