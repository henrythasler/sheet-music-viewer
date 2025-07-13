package com.henrythasler.sheetmusic

import kotlinx.serialization.Serializable

@Serializable
data class TimemapItem(
	val tstamp: Double,
	val qstamp: Double,
	val tempo: Double? = null,
	val on: List<String>? = null,
	val off: List<String>? = null
)
