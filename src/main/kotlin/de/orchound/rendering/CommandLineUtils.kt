package de.orchound.rendering

import java.io.File


fun parseArguments(shortCutMap: Map<String, String>, args: List<String>): Map<String, String?> {
	return try {
		args.map(::argumentToTuple)
			.map {
				val key = shortCutMap[it.first] ?: throw IllegalArgumentException()
				Pair(key, it.second)
			}.toMap()
	} catch (ex: IllegalArgumentException) {
		mapOf(Pair("help", null))
	}
}

fun argumentToTuple(argument: String): Pair<String, String?> {
	val pattern = Regex("""-{1,2}(\w)[\w-]*(?:=([^\s]+))*""")
	val matchGroups = pattern.matchEntire(argument)!!.groups
	val key = matchGroups[1]?.value ?: throw IllegalArgumentException()

	return Pair(key, matchGroups[2]?.value)
}

fun parseFileList(fileString: String): List<File> {
	return if (fileString.isNotEmpty())
		fileString.split(";").map(::File)
	else
		emptyList()
}
