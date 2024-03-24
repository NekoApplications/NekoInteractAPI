package icu.takeneko.interact

import kotlinx.serialization.json.Json

fun decodeJsonStringList(input:String):List<String>{
    return Json.decodeFromString<List<String>>(input)
}

fun decodeJsonString2StringListMap(input: String):Map<String,List<String>>{
    return Json.decodeFromString<Map<String,List<String>>>(input)
}