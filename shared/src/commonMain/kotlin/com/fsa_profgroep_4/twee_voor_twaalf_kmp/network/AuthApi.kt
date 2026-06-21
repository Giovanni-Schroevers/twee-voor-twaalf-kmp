package com.fsa_profgroep_4.twee_voor_twaalf_kmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/* -------------------------------------------------------------------------- */
/*  DTOs — these mirror the backend's `model/User.kt` 1:1 so kotlinx.serialization
    can (de)serialize the JSON directly. `id` is a UInt on the server, so we keep
    it a UInt here. `avatar` is nullable and currently always passed through
    untouched (avatar upload is out of scope).                                   */
/* -------------------------------------------------------------------------- */

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class UpdateUserRequest(
    val username: String,
    val email: String,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class UserDto(
    val id: UInt,
    val username: String,
    val email: String,
    val avatar: String? = null,
)

/** Response of `POST /api/login`: the JWT plus the user it belongs to. */
@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

/**
 * The backend's user endpoints. Depending on this interface (not the Ktor class)
 * keeps callers testable — the same pattern as [ExampleApi].
 *
 * All calls are `suspend`; invoke them from a coroutine (e.g. a ViewModel's
 * `viewModelScope`). Paths are relative to the base URL configured on the
 * [HttpClient] (see [createHttpClient]).
 */
interface AuthApi {
    /** Creates an account. Returns the new user id. Throws on failure (e.g. a
     *  duplicate username surfaces as a non-2xx response). Note the backend does
     *  NOT return a token here — log in afterwards to obtain one. */
    suspend fun register(request: RegisterRequest): UInt

    /** Returns the auth payload, or `null` when credentials are rejected (401). */
    suspend fun login(request: LoginRequest): AuthResponse?

    /** Updates the signed-in user's profile (requires the bearer token). */
    suspend fun updateUser(request: UpdateUserRequest): UserDto

    /** Changes the signed-in user's password (requires the bearer token). Throws
     *  on failure — notably 400 when the current password is wrong. */
    suspend fun changePassword(request: ChangePasswordRequest)

    /**
     * Uploads a new avatar image for the signed-in user (requires the bearer
     * token) and returns the updated user. Sent as `multipart/form-data` with a
     * single `avatar` file part — the backend validates the content type
     * (png/jpeg/webp) and stores the file, serving it at `/avatars/<file>`.
     */
    suspend fun uploadAvatar(bytes: ByteArray, filename: String, mimeType: String): UserDto

    /** Deletes the signed-in user's account (requires the bearer token). */
    suspend fun deleteUser()
}

/**
 * Ktor-backed implementation. The shared [HttpClient] already carries the base
 * URL, JSON negotiation and the bearer token (added automatically once login has
 * written it to the [AuthTokenStore]), so these methods stay thin.
 */
class KtorAuthApi(
    private val client: HttpClient,
) : AuthApi {

    override suspend fun register(request: RegisterRequest): UInt {
        val response = client.post("api/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.requireSuccess()
        return response.body()
    }

    override suspend fun login(request: LoginRequest): AuthResponse? {
        val response = client.post("api/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        // The endpoint replies 401 with a plain-text body on bad credentials;
        // treat that as a normal "no" rather than an error.
        if (response.status == HttpStatusCode.Unauthorized) return null
        response.requireSuccess()
        return response.body()
    }

    override suspend fun updateUser(request: UpdateUserRequest): UserDto {
        val response = client.put("api/user/update") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.requireSuccess()
        return response.body()
    }

    override suspend fun changePassword(request: ChangePasswordRequest) {
        client.put("api/user/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccess()
    }

    override suspend fun uploadAvatar(bytes: ByteArray, filename: String, mimeType: String): UserDto {
        val response = client.post("api/user/avatar") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        // Part name must be "avatar"; the Content-Type header is how
                        // the backend decides the image type/extension.
                        append(
                            key = "avatar",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                            },
                        )
                    },
                ),
            )
        }
        response.requireSuccess()
        return response.body()
    }

    override suspend fun deleteUser() {
        client.delete("api/user/delete").requireSuccess()
    }

    /**
     * Throws a [BackendException] carrying the status and the raw response body
     * when the server returns a non-2xx status, so callers can surface what the
     * backend actually said instead of a generic message.
     */
    private suspend fun HttpResponse.requireSuccess() {
        if (!status.isSuccess()) {
            val body = runCatching { bodyAsText() }.getOrDefault("")
            throw BackendException(status.value, body)
        }
    }
}

/** A non-2xx response from the backend. [message] is `HTTP <status>: <body>`. */
class BackendException(
    val status: Int,
    val body: String,
) : Exception("HTTP $status" + if (body.isBlank()) "" else ": $body")
