package icu.takeneko.interact.mcdr

import icu.takeneko.interact.os.walkProcessGroup

fun checkIsMCDReforgedPresent():Boolean{
    return walkProcessGroup().values.stream().anyMatch {
        it.contains("python") || it.contains("mcdreforged")
    }
}

fun getMCDReforgedProcessPid():Int{
    for ((pid, cmdLine) in walkProcessGroup()) {
        if(cmdLine.contains("python") || cmdLine.contains("mcdreforged")){
            return pid
        }
    }
    return -1
}