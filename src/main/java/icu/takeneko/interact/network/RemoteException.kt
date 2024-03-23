package icu.takeneko.interact.network

class RemoteException(val status:Int,message:String): RuntimeException(message) {
}