// Create/replace: app/src/main/java/com/example/sportify/model/WeatherService.kt

package com.example.sportify.model

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class WeatherService {
    private val client = OkHttpClient()
    private val apiKey = "a46af2e1199b957262ac5e39d755155d"
    private val baseUrl = "https://api.openweathermap.org/data/2.5/weather"

    // Add a cache to store weather results
    private val weatherCache = mutableMapOf<String, Pair<WeatherInfo, Long>>()
    private val CACHE_DURATION_MS = 30 * 60 * 1000 // 30 minutes

    companion object {
        private const val TAG = "WeatherService"
    }

    // Data class to represent weather information
    data class WeatherInfo(
        val temperature: Double,
        val description: String,
        val icon: String
    ) {
        // Format temperature as "XX°C"
        fun formattedTemperature(): String {
            return "${temperature.toInt()}°C"
        }

        // Get emoji for weather condition
        fun getWeatherEmoji(): String {
            return when {
                icon.contains("01") -> "☀️" // clear sky
                icon.contains("02") -> "⛅" // few clouds
                icon.contains("03") || icon.contains("04") -> "☁️" // clouds
                icon.contains("09") || icon.contains("10") -> "🌧️" // rain
                icon.contains("11") -> "⛈️" // thunderstorm
                icon.contains("13") -> "❄️" // snow
                icon.contains("50") -> "🌫️" // mist
                else -> "🌤️" // default
            }
        }

        // Format as "XX°C emoji"
        fun formatForDisplay(): String {
            return "${formattedTemperature()} ${getWeatherEmoji()}"
        }
    }

    // Get weather by city name
    fun getWeatherByCity(city: String, callback: (WeatherInfo?, String?) -> Unit) {
        if (city.isEmpty()) {
            Log.w(TAG, "Empty city name provided")
            callback(null, "Location is empty")
            return
        }

        val formattedCity = city.trim()

        // Check cache first
        val cachedWeather = weatherCache[formattedCity]
        if (cachedWeather != null) {
            val (weatherInfo, timestamp) = cachedWeather
            val currentTime = System.currentTimeMillis()

            // If cache is still valid (less than 30 minutes old)
            if (currentTime - timestamp < CACHE_DURATION_MS) {
                Log.d(TAG, "Using cached weather for: $formattedCity")
                // Ensure callback runs on main thread
                Handler(Looper.getMainLooper()).post {
                    callback(weatherInfo, null)
                }
                return
            }
        }

        // Proceed with API request if no cache or cache expired
        Log.d(TAG, "Getting weather for city: '$formattedCity'")

        try {
            val encodedCity = URLEncoder.encode(formattedCity, "UTF-8")
            val url = "$baseUrl?q=$encodedCity&units=metric&appid=$apiKey"

            fetchWeather(url) { weatherInfo, error ->
                if (weatherInfo != null) {
                    // Update cache with new weather data
                    weatherCache[formattedCity] = Pair(weatherInfo, System.currentTimeMillis())
                }
                // Ensure callback runs on main thread
                Handler(Looper.getMainLooper()).post {
                    callback(weatherInfo, error)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating weather request", e)
            Handler(Looper.getMainLooper()).post {
                callback(null, "Error: ${e.message}")
            }
        }
    }

    // Get weather by coordinates
    fun getWeatherByCoordinates(lat: Double, lon: Double, callback: (WeatherInfo?, String?) -> Unit) {
        val cacheKey = "lat${lat}_lon${lon}"

        // Check cache first
        val cachedWeather = weatherCache[cacheKey]
        if (cachedWeather != null) {
            val (weatherInfo, timestamp) = cachedWeather
            val currentTime = System.currentTimeMillis()

            // If cache is still valid (less than 30 minutes old)
            if (currentTime - timestamp < CACHE_DURATION_MS) {
                Log.d(TAG, "Using cached weather for coordinates: $lat, $lon")
                Handler(Looper.getMainLooper()).post {
                    callback(weatherInfo, null)
                }
                return
            }
        }

        val url = "$baseUrl?lat=$lat&lon=$lon&units=metric&appid=$apiKey"
        Log.d(TAG, "Getting weather for coordinates: $lat, $lon")

        fetchWeather(url) { weatherInfo, error ->
            if (weatherInfo != null) {
                // Update cache with new weather data
                weatherCache[cacheKey] = Pair(weatherInfo, System.currentTimeMillis())
            }
            Handler(Looper.getMainLooper()).post {
                callback(weatherInfo, error)
            }
        }
    }

    private fun fetchWeather(url: String, callback: (WeatherInfo?, String?) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Weather API request failed", e)
                callback(null, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Weather API response code: ${response.code}")

                // Use try-with-resources pattern to ensure response is closed
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Weather API error: ${response.code}")
                        callback(null, "API error: ${response.code}")
                        return@use
                    }

                    try {
                        val responseBody = response.body
                        if (responseBody == null) {
                            Log.e(TAG, "Empty response body")
                            callback(null, "Empty response from API")
                            return@use
                        }

                        val responseString = responseBody.string() // Consumes and closes the body
                        Log.d(TAG, "Weather API response: $responseString")

                        val jsonObject = JSONObject(responseString)

                        // Parse weather data
                        val main = jsonObject.getJSONObject("main")
                        val temp = main.getDouble("temp")

                        val weatherArray = jsonObject.getJSONArray("weather")
                        val weather = weatherArray.getJSONObject(0)
                        val description = weather.getString("description")
                        val icon = weather.getString("icon")

                        val weatherInfo = WeatherInfo(temp, description, icon)
                        Log.d(TAG, "Weather parsed successfully: ${weatherInfo.formatForDisplay()}")
                        callback(weatherInfo, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing weather data", e)
                        callback(null, "Error parsing weather data: ${e.message}")
                    }
                }
            }
        })
    }

    // Helper method to test if the weather service is working
    fun testWeatherService(callback: (Boolean, String?) -> Unit) {
        getWeatherByCity("London,UK") { weatherInfo, error ->
            if (weatherInfo != null) {
                Log.d(TAG, "Weather test successful: ${weatherInfo.formatForDisplay()}")
                callback(true, weatherInfo.formatForDisplay())
            } else {
                Log.e(TAG, "Weather test failed: $error")
                callback(false, error)
            }
        }
    }
}