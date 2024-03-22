package icu.takeneko.interact.network

interface Service {
    suspend fun handleRequest(request: Request):Response

    suspend operator fun invoke(request: Request) = handleRequest(request)
}