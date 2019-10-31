package de.orchound.rendering.opengl

import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.channels.FileChannel


object TextureLoader {

	fun loadTexture(file: File): Texture {
		return MemoryStack.stackPush().use { frame ->
			val byteBuffer = file.inputStream().use { inputStream ->
				inputStream.channel.use { fileChannel ->
					fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
				}
			}

			val width = frame.mallocInt(1)
			val height = frame.mallocInt(1)
			val components = frame.mallocInt(1)

			val data = stbi_load_from_memory(byteBuffer, width, height, components, 4) ?:
			throw Exception("Failed to load image data for file: $file")

			val texture = Texture(width.get(), height.get(), data)
			stbi_image_free(data)
			texture
		}
	}
}
