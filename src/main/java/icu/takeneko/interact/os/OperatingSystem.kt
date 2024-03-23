package icu.takeneko.interact.os

import oshi.SystemInfo

fun walkProcessGroup(): Map<Int, String>{
    val resultMap = mutableMapOf<Int,String>()
    val os = SystemInfo().operatingSystem
    val processes = os.processes.associateBy { it.processID }
    var currentProcess = os.currentProcess
    while (true) {
        resultMap[currentProcess.processID] = currentProcess.commandLine
        currentProcess = processes[currentProcess.parentProcessID] ?: break
    }
    return resultMap
}