package de.orchound.rendering.shadertoy

import de.orchound.rendering.Quad
import de.orchound.rendering.Time
import de.orchound.rendering.Window
import java.io.File
import java.time.LocalDateTime

class ShadertoyApplication(shaderFile: File) {

	private val window = Window("Shadertoy Renderer", 1280, 720)
	private val shader = ShadertoyShader(shaderFile)
	private val quad = Quad()
	private var frame = 0
	private var dateTime = LocalDateTime.now()
	private var currentTime = 0f
	private var deltaTime = 0f
	private var currentDateUpdateTime = 0f
	private var dateUpdateInterval = 1f

	fun run() {
		while (!window.shouldClose()) {
			update()
			render()
			frame++
		}

		window.destroy()
	}

	private fun update() {
		Time.update()
		currentTime = Time.currentTime / 1000f
		deltaTime = Time.deltaTime / 1000f
		currentDateUpdateTime += deltaTime

		if (currentDateUpdateTime > dateUpdateInterval) {
			dateTime = LocalDateTime.now()
			currentDateUpdateTime = 0f
		}
	}

	private fun render() {
		window.prepareFrame()
		shader.bind()

		shader.setResolution(window.width.toFloat(), window.height.toFloat(), 0f)
		shader.setTime(currentTime)
		shader.setTimeDelta(deltaTime)
		shader.setFrame(frame)
		shader.setDate(
			dateTime.year.toFloat(), dateTime.monthValue.toFloat(), dateTime.dayOfMonth.toFloat(),
			dateTime.hour * 3600 + dateTime.second.toFloat() + currentDateUpdateTime
		)
		shader.setMouse(
			window.mousePosX.toFloat(), window.mousePosY.toFloat(),
			window.mouseClickPosX.toFloat(), window.mouseClickPosY.toFloat()
		)

		quad.draw()

		shader.unbind()
		window.finishFrame()
	}
}
