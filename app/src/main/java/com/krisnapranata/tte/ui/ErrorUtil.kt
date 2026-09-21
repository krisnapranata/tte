package com.krisnapranata.tte.ui

import org.json.JSONObject
import retrofit2.HttpException

fun errorMessage(e: Throwable): String {
    if (e is HttpException) {
        val body = try {
            e.response()?.errorBody()?.string().orEmpty()
        } catch (ex: Exception) {
            ""
        }
        val detail = try {
            JSONObject(body).optString("detail")
        } catch (ex: Exception) {
            ""
        }
        return when {
            detail.isNotBlank() -> detail
            e.code() == 413 -> "Lampiran terlalu besar (HTTP 413) — server menolak ukuran file"
            else -> "HTTP ${e.code()}: ${e.response()?.message().orEmpty()}".trim()
        }
    }
    return e.message ?: e.javaClass.simpleName
}
