package com.aizoid.unityadsdemo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class NetworkLocationInfo(
    val ip: String = "",
    val country: String = "",
    val countryCode: String = "",
    val flagEmoji: String = "🌐",
    val region: String = "",
    val city: String = "",
    val isp: String = "",
    val timezone: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class NetworkLocationService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLocationInfo(): NetworkLocationInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://ipwho.is/")
                .header("User-Agent", "UnityAdsDemo-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkLocationInfo(
                        isSuccess = false,
                        errorMessage = "HTTP Error ${response.code}: ${response.message}"
                    )
                }

                val bodyString = response.body?.string() ?: ""
                val json = JSONObject(bodyString)

                val success = json.optBoolean("success", true)
                if (!success) {
                    val message = json.optString("message", "Lookup failed")
                    return@withContext NetworkLocationInfo(
                        isSuccess = false,
                        errorMessage = message
                    )
                }

                val ip = json.optString("ip", "Unknown")
                val country = json.optString("country", "Unknown")
                val countryCode = json.optString("country_code", "")
                val region = json.optString("region", "Unknown")
                val city = json.optString("city", "Unknown")

                val connectionObj = json.optJSONObject("connection")
                val isp = connectionObj?.optString("isp") 
                    ?: json.optString("isp", "Unknown ISP")

                val timezoneObj = json.optJSONObject("timezone")
                val timezone = timezoneObj?.optString("id") 
                    ?: timezoneObj?.optString("utc", "UTC") 
                    ?: "UTC"

                val flagObj = json.optJSONObject("flag")
                val emoji = flagObj?.optString("emoji") 
                    ?: if (countryCode.isNotBlank()) countryCodeToEmoji(countryCode) else "🌐"

                NetworkLocationInfo(
                    ip = ip,
                    country = country,
                    countryCode = countryCode,
                    flagEmoji = emoji,
                    region = region,
                    city = city,
                    isp = isp,
                    timezone = timezone,
                    isSuccess = true,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            NetworkLocationInfo(
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Network connection failed"
            )
        }
    }

    private fun countryCodeToEmoji(code: String): String {
        val uppercase = code.uppercase()
        if (uppercase.length != 2) return "🌐"
        val firstChar = Character.codePointAt(uppercase, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(uppercase, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}