package de.orchound.rendering


object Time {
	var currentTime: Long = 0
		private set
	var deltaTime: Long = 0
		private set

	private var timestamp = System.currentTimeMillis()

	fun update() {
		val previousTimestamp = timestamp
		timestamp = System.currentTimeMillis()
		deltaTime = timestamp - previousTimestamp
		currentTime += deltaTime
	}
}
