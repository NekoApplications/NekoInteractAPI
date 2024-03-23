package icu.takeneko.interact

import kotlinx.serialization.json.Json

fun decodeJsonStringList(input:String):List<String>{
    return Json.decodeFromString<List<String>>(input)
}