package de.orchound.rendering.shadertoy

import de.orchound.rendering.parseArguments
import de.orchound.rendering.parseFileList
import java.io.File


private val help = """
		|Usage:
		|java -jar ShadertoyRenderer.jar [OPTIONS] [SHADER]
		|
		|Options:
		|-t=<textures>  --textures=<textures>
		|		';'-separated list of texture files.
		|
		|Example:
		|java -jar ShadertoyRenderer.jar sample.shtoy
		|java -jar ShadertoyRenderer.jar -t=texture1;path/texture2 sample.shtoy
	""".trimMargin()

private val shortCutMap = mapOf(
	Pair("t", "textures"),
	Pair("h", "help")
)

fun main(args: Array<String>) {
	if (args.isEmpty() || args.contains("help")) {
		print(help)
	} else {
		val argsMap = parseArguments(shortCutMap, args.slice(0 until args.size - 1))
		val textures = parseFileList(argsMap["textures"].orEmpty())
		ShadertoyApplication(File(args.last()), textures).run()
	}
}
