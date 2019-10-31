package de.orchound.rendering.shadertoy

import de.orchound.rendering.Time
import de.orchound.rendering.Window
import de.orchound.rendering.opengl.Quad
import de.orchound.rendering.opengl.TextureLoader
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.time.LocalDateTime

class ShadertoyApplication(shaderFile: File, textureFiles: List<File>) {

	private val window = Window("Shadertoy Renderer", 1280, 720)
	private val shaderFileWatchService = FileSystems.getDefault().newWatchService()
	private val shaderFilePath = shaderFile.toPath()
	private val textures = textureFiles.map(TextureLoader::loadTexture)
	private val shader = ShadertoyShader(shaderFile, textures.size).apply {
		textures.withIndex().forEach {
			this.setChannelTexture(it.index, it.value.handle)
		}
	}
	private val quad = Quad()
	private var frame = 0
	private var dateTime = LocalDateTime.now()
	private var currentTime = 0f
	private var deltaTime = 0f
	private val dateUpdateInterval = 1f
	private var currentDateUpdateTime = 0f
	private val shaderReloadInterval = 0.5f
	private var currentShaderReloadTime = 0f

	init {
		shaderFilePath.toAbsolutePath().parent.register(shaderFileWatchService, ENTRY_MODIFY)
	}

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
		currentShaderReloadTime += deltaTime

		if (currentDateUpdateTime > dateUpdateInterval) {
			dateTime = LocalDateTime.now()
			currentDateUpdateTime = 0f
		}

		if (currentShaderReloadTime > shaderReloadInterval) {
			if (shaderChanged())
				shader.recompile()
			currentShaderReloadTime = 0f
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

	private fun shaderChanged(): Boolean {
		return shaderFileWatchService.poll()?.let { watchKey ->
			var shaderChanged = false
			for (event in watchKey.pollEvents()) {
				if (event.context() as Path == shaderFilePath)
					shaderChanged = true
			}

			watchKey.reset()
			shaderChanged
		} ?: false
	}
}
