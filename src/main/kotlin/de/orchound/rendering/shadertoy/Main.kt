package de.orchound.rendering.shadertoy

import java.io.File


private val help = """
		|Usage:
		|java -jar ShadertoyRenderer.jar [SHADER]
		|
		|Example:
		|java -jar ShadertoyRenderer.jar sample.shtoy
	""".trimMargin()

fun main(args: Array<String>) {
	if (args.isEmpty())
		print(help)
	else
		ShadertoyApplication(File(args[0])).run()
}
