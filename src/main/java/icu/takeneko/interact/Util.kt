package icu.takeneko.interact

import icu.takeneko.interact.network.Response
import kotlinx.serialization.json.Json

fun decodeJsonStringList(input:String):List<String>{
    return Json.decodeFromString<List<String>>(input)
}

fun ensureResponseValid(response:Response){

}