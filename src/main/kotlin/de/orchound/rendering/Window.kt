package de.orchound.rendering

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*


class Window(title: String, width: Int, height: Int) {

	var width: Int = width
		private set

	var height: Int = height
		private set

	var title: String = title
		set(value) {
			glfwSetWindowTitle(handle, value)
			field = value
		}

	private var handle: Long = 0

	var mousePosX = 0.0
		private set
	var mousePosY = 0.0
		private set
	var mouseClickPosX = -1.0
		private set
	var mouseClickPosY = -1.0
		private set

	private var errorCallback = GLFWErrorCallback.createPrint(System.err)
	private var mouseButtonCallback = GLFWMouseButtonCallback.create { _, button, action, mods ->
		if (button == GLFW_MOUSE_BUTTON_LEFT) {
			if (action == GLFW_PRESS) {
				mouseClickPosX = mousePosX
				mouseClickPosY = mousePosY
			} else {
				mouseClickPosX = 0.1
				mouseClickPosY = 0.1
			}
		}
	}
	private var mousePositionCallback = GLFWCursorPosCallback.create { _, xPos, yPos ->
		mousePosX = xPos
		mousePosY = height - yPos
	}
	private var framebufferSizeCallback = GLFWFramebufferSizeCallback.create { window, newWidth, newHeight ->
		if (window == handle) {
			glViewport(0, 0, newWidth, newHeight)
			this.width = newWidth
			this.height = newHeight
		}
	}

	init {
		if (!glfwInit())
			throw RuntimeException("Failed to initialize GLFW")

		glfwSetErrorCallback(errorCallback)

		glfwDefaultWindowHints()
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

		handle = glfwCreateWindow(width, height, title, 0, 0)
		if (handle == 0L)
			throw Exception("Failed to create window")

		glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback)
		glfwSetMouseButtonCallback(handle, mouseButtonCallback)
		glfwSetCursorPosCallback(handle, mousePositionCallback)
		glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)

		glfwMakeContextCurrent(handle)
		GL.createCapabilities()
		glClearColor(0f, 0f, 0f, 1f)
	}

	fun shouldClose() = glfwWindowShouldClose(handle)

	fun prepareFrame() {
		glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
	}

	fun finishFrame() {
		glfwSwapBuffers(handle)
		glfwPollEvents()
	}

	fun destroy() {
		errorCallback.close()
		mouseButtonCallback.close()
		mousePositionCallback.close()
		framebufferSizeCallback.close()

		glfwDestroyWindow(handle)
		glfwTerminate()
	}
}
