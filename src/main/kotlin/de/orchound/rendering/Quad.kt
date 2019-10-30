package de.orchound.rendering

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays


class Quad {
	private val vao = glGenVertexArrays()

	init {
		val vertices = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)

		val vbo = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vbo)
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
		glBindBuffer(GL_ARRAY_BUFFER, 0)

		glBindVertexArray(vao)
		glEnableVertexAttribArray(0)
		glBindBuffer(GL_ARRAY_BUFFER, vbo)
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
		glBindVertexArray(0)
	}

	fun draw() {
		glBindVertexArray(vao)
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
		glBindVertexArray(0)
	}
}
