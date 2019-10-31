package de.orchound.rendering.shadertoy

import org.lwjgl.opengl.GL20.*
import java.io.File

class ShadertoyShader(private val shaderFile: File, private val textureChannelsCount: Int) {

	private var handle: Int = 0

	private var resolutionLocation = -1
	private var timeLocation = -1
	private var timeDeltaLocation = -1
	private var frameLocation = -1
	private val channelTimeLocation = IntArray(4) { -1 }
	private val channelResolutionLocation = IntArray(4) { -1 }
	private var mouseLocation = -1
	private val textureChannelLocations = IntArray(textureChannelsCount) { -1 }
	private var dateLocation = -1
	private var sampleRateLocation = -1

	init {
		require(textureChannelsCount in 0 .. 3)
		initShader()
	}

	fun setResolution(x: Float, y: Float, z: Float) = glUniform3f(resolutionLocation, x, y, z)
	fun setTime(value: Float) = glUniform1f(timeLocation, value)
	fun setTimeDelta(value: Float) = glUniform1f(timeDeltaLocation, value)
	fun setFrame(value: Int) = glUniform1i(timeDeltaLocation, value)
	fun setMouse(mouseX: Float, mouseY: Float, mouseClickX: Float, mouseClickY: Float) =
		glUniform4f(mouseLocation, mouseX, mouseY, mouseClickX, mouseClickY)
	fun setDate(year: Float, month: Float, day: Float, second: Float) =
		glUniform4f(dateLocation, year, month, day, second)
	fun setSampleRate(value: Float) = glUniform1f(sampleRateLocation, value)

	fun setChannelTime(value1: Float, value2: Float, value3: Float, value4: Float) {
		glUniform1f(channelTimeLocation[0], value1)
		glUniform1f(channelTimeLocation[1], value2)
		glUniform1f(channelTimeLocation[2], value3)
		glUniform1f(channelTimeLocation[3], value4)
	}

	fun setChannelResolution(values: FloatArray) {
		for (i in 0 .. 3) {
			val valueIndex = i * 3
			glUniform3f(channelResolutionLocation[i],
				values[valueIndex], values[valueIndex + 1], values[valueIndex + 2]
			)
		}
	}

	fun setChannelTexture(channelId: Int, textureHandle: Int) {
		glActiveTexture(GL_TEXTURE0 + channelId)
		glBindTexture(GL_TEXTURE_2D, textureHandle)
	}

	fun bind() = glUseProgram(handle)
	fun unbind() = glUseProgram(0)
	fun recompile() = initShader()

	private fun initShader() {
		if (handle > 0)
			glDeleteProgram(handle)
		handle = createShaderProgram()

		initUniformLocations()

		bind()
		initTextureUniforms()
		unbind()
	}

	private fun createShaderProgram(): Int {
		val fragmentShaderSource = fragmentShaderSourceTemplate
			.replace("#SAMPLER2D_UNIFORMS#", generateSampler2dUniformDeclaration())
			.replace("#SHADERTOY_SOURCE#", loadShaderSource(shaderFile))

		val vertexShaderHandle = compileShader(vertexShaderSource, GL_VERTEX_SHADER)
		val fragmentShaderHandle = compileShader(fragmentShaderSource, GL_FRAGMENT_SHADER)

		val programHandle = glCreateProgram()
		glAttachShader(programHandle, vertexShaderHandle)
		glAttachShader(programHandle, fragmentShaderHandle)

		glBindAttribLocation(programHandle, 0, "in_position")

		glLinkProgram(programHandle)

		glDetachShader(programHandle, vertexShaderHandle)
		glDetachShader(programHandle, fragmentShaderHandle)
		glDeleteShader(vertexShaderHandle)
		glDeleteShader(fragmentShaderHandle)

		validateShaderLinking(programHandle)
		validateShaderProgram(programHandle)

		return programHandle
	}

	private fun compileShader(shaderSource: String, type: Int): Int {
		val shaderId = glCreateShader(type)

		glShaderSource(shaderId, shaderSource)
		glCompileShader(shaderId)

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
			val info = getShaderInfoLog(shaderId)
			val shaderType = if (type == GL_VERTEX_SHADER) "Vertex" else "Fragment"
			throw Exception("$shaderType shader compilation failed: $info")
		}

		return shaderId
	}

	private fun getProgramInfoLog(programId:Int):String {
		return glGetProgramInfoLog(programId, GL_INFO_LOG_LENGTH)
	}

	private fun getShaderInfoLog(shaderId:Int):String {
		return glGetShaderInfoLog(shaderId, GL_INFO_LOG_LENGTH)
	}

	private fun validateShaderProgram(programId: Int) {
		glValidateProgram(programId)

		val error = glGetError()
		if (error != 0)
			throw Exception("OpenGL shader creation failed")
	}

	private fun validateShaderLinking(programId: Int) {
		if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
			val info = getProgramInfoLog(programId)
			throw Exception("OpenGL shader linking failed: $info")
		}
	}

	private fun generateSampler2dUniformDeclaration(): String {
		return StringBuilder().apply {
			(0 until textureChannelsCount).forEach {
				this.appendln("uniform sampler2D iChannel$it;")
			}
		}.toString()
	}

	private fun initTextureUniforms() {
		(0 until textureChannelsCount).forEach { index ->
			glUniform1i(textureChannelLocations[index], index)
		}
	}

	private fun initUniformLocations() {
		resolutionLocation = getUniformLocation("iResolution")
		timeLocation = getUniformLocation("iTime")
		timeDeltaLocation = getUniformLocation("iTimeDelta")
		frameLocation = getUniformLocation("iFrame")
		for (index in channelTimeLocation.indices)
			channelTimeLocation[index] = getUniformLocation("iChannelTime[$index]")
		for (index in channelResolutionLocation.indices)
			channelResolutionLocation[index] = getUniformLocation("iChannelResolution[$index]")
		mouseLocation = getUniformLocation("iMouse")
		dateLocation = getUniformLocation("iDate")
		sampleRateLocation = getUniformLocation("iSampleRate")

		for (index in textureChannelLocations.indices)
			textureChannelLocations[index] = getUniformLocation("iChannel$index")
	}

	private fun getUniformLocation(name: String) = glGetUniformLocation(handle, name)

	private fun loadShaderSource(shader: File): String {
		return shader.inputStream().use { inputStream ->
			inputStream.bufferedReader().readLines()
		}.joinToString("\n") { it }
	}

	companion object {
		private val vertexShaderSource = """
			|#version 330
			|layout(location = 0) in vec2 in_position;
			|
			|void main(void) {
			|	gl_Position = vec4(in_position, 0.0, 1.0);
			|}
		""".trimMargin()

		private val fragmentShaderSourceTemplate = """
			|#version 330
			|
			|out vec4 color;
			|
			|uniform vec3 iResolution;
			|uniform float iTime;
			|uniform float iTimeDelta;
			|uniform int iFrame;
			|uniform float iChannelTime[4];
			|uniform vec3 iChannelResolution[4];
			|uniform vec4 iMouse;
			|#SAMPLER2D_UNIFORMS#
			|uniform vec4 iDate;
			|uniform float iSampleRate;
			|
			|#SHADERTOY_SOURCE#
			|
			|void main(void) {
			|	mainImage(color, gl_FragCoord.xy);
			|}
		""".trimMargin()
	}
}
