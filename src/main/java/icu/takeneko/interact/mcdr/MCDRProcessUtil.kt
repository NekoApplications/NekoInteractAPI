package icu.takeneko.interact.mcdr

import icu.takeneko.interact.os.walkProcessGroup
import oshi.SystemInfo
import oshi.software.os.OSProcess

fun checkIsMCDReforgedPresent(): Boolean {
    return walkProcessGroup().values.stream().anyMatch {
        it.contains("python") || it.contains("mcdreforged")
    }
}

fun getMCDReforgedProcess(): OSProcess? {
    val os = SystemInfo().operatingSystem
    val processes = os.processes.associateBy { it.processID }
    var currentProcess = os.currentProcess
    while (true) {
        if (currentProcess.commandLine.contains("python") || currentProcess.commandLine.contains("mcdreforged")) {
            return currentProcess
        }
        currentProcess = processes[currentProcess.parentProcessID] ?: break
    }
    return null
}

fun getMCDReforgedProcessPid(): Int {
    for ((pid, cmdLine) in walkProcessGroup()) {
        if (cmdLine.contains("python") || cmdLine.contains("mcdreforged")) {
            return pid
        }
    }
    return -1
}

fun getMCDReforgedProcessWorkingDir():String? {
    val proc = getMCDReforgedProcess() ?: return null
    return proc.currentWorkingDirectory
}