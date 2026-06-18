package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

/**
 * A model of the JSON the API returns. `@Serializable` is what the
 * kotlinx.serialization compiler plugin reads to generate the parsing code —
 * the same annotation-driven idea as Room's `@Entity`, just for JSON instead of
 * SQL. Field names here must match the JSON keys (extras are ignored thanks to
 * `ignoreUnknownKeys` in the HttpClient config).
 */
@Serializable
data class DemoPost(
    val id: Int,
    val title: String,
    val body: String,
)

/**
 * Demonstration API surface. Depending on this *interface* (not the Ktor class)
 * keeps callers testable and swappable — the same pattern as the Greeting/DI
 * example. A test can supply a fake; the app supplies [KtorExampleApi].
 */
interface ExampleApi {
    suspend fun firstPost(): DemoPost
}

/**
 * Real implementation, backed by Ktor. It's handed an [HttpClient] (Koin injects
 * the shared one) and never builds its own — so the client's config and lifetime
 * are owned in one place.
 *
 * `get(url).body()` performs the request and deserializes the JSON response into
 * [DemoPost] using the ContentNegotiation/json setup. It's a `suspend` function,
 * so call it from a coroutine (e.g. a ViewModel's `viewModelScope`).
 *
 * Uses the public, key-free JSONPlaceholder API purely as a stand-in so the
 * wiring is runnable; swap the URL/model for the real backend later.
 */
class KtorExampleApi(
    private val client: HttpClient,
) : ExampleApi {
    override suspend fun firstPost(): DemoPost =
        client.get("https://jsonplaceholder.typicode.com/posts/1").body()
}
