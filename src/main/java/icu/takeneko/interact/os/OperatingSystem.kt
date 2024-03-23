package icu.takeneko.interact.os

import icu.takeneko.interact.Mod
import oshi.SystemInfo
import java.io.StringReader
import java.nio.charset.Charset
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import kotlin.io.path.*

enum class OperatingSystem {
    WINDOWS {
        fun walkProcessGroup0(): Map<Int, String> {
            val result = mutableMapOf<Int, String>()
            val detectionExe = Mod.privateDir.resolve("GetParentProcess_windows.exe")
            detectionExe.deleteIfExists()
            if (detectionExe.notExists()) {
                detectionExe.createFile()
                val stream = this.javaClass.classLoader.getResourceAsStream("bin/GetParentProcess_windows.exe")
                if (stream != null) {
                    detectionExe.outputStream().use {
                        stream.copyTo(it)
                    }
                }
            }
            if (detectionExe.exists()) {
                val process = Runtime.getRuntime().exec(detectionExe.absolutePathString())
                val output = StringBuilder()
                val th = thread {
                    val reader = process.inputStream.bufferedReader(Charset.forName("utf-8"))
                    try {
                        while (true) {
                            val line = reader.readLine() ?: continue
                            output.append(line).append("\n")
                        }
                    } catch (_: InterruptedException) {
                    } finally {
                        reader.close()
                    }
                }
                while (process.isAlive) {
                    LockSupport.parkNanos(1000)
                }
                th.interrupt()
                val out = output.toString()
                try{
                    if (out.isNotEmpty()) {
                        val reader = StringReader(out).buffered()
                        val countPid = reader.readLine().toInt()
                        repeat(countPid){
                            result[reader.readLine().toInt()] = ""
                        }
                        val countAvail = reader.readLine().toInt()
                        repeat(countAvail){
                            val pid = reader.readLine().toInt()
                            val cmd = reader.readLine()
                            result[pid] = cmd
                        }
                    }
                }catch (e:Exception){

                }
            }
            return result
        }
    },
    MACOS,
    LINUX,
    UNKNOWN;

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

    companion object {
        var current: OperatingSystem

        init {
            val string = System.getProperty("os.name").lowercase()
            current = if (string.contains("win")) {
                WINDOWS
            } else if (string.contains("mac")) {
                MACOS
            } else if (string.contains("linux")) {
                LINUX
            } else {
                if (string.contains("unix")) LINUX else UNKNOWN
            }
        }
    }
}
