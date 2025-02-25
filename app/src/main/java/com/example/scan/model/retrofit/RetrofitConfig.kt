package com.example.scan.model.retrofit
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.scan.model.data.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream

interface ApiService {
    @Multipart
    @POST("/") // Cambia esta ruta según tu API
    fun uploadImage(
        @Header("Authorization") authToken: String,
        @Part img1: MultipartBody.Part,
        @Part img2: MultipartBody.Part
    ): Call<ApiResponse>
}
object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .retryOnConnectionFailure(true)
        .connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS) // Tiempo de espera para conectar
        .readTimeout(
            100,
            java.util.concurrent.TimeUnit.SECONDS
        )    // Tiempo de espera para leer datos
        .writeTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://lioness-artistic-monkfish.ngrok-free.app") // URL base
            //.baseUrl("http://192.168.2.3:8000") // URL base

            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
//TODO: BITMAP TO  FILE
fun sendImageToApi(
    bitmap1: Bitmap,
    bitmap2: Bitmap,
    context: Context,
    callback: (Boolean) -> Unit
) {
    val file1 = saveBitmapToFile(bitmap1, context, "img1.jpg")
    val file2 = saveBitmapToFile(bitmap2, context, "img2.jpg")
    if (file1 == null || file2 == null) {
        Log.e("FaceScanner", "Error al guardar archivos")
        return
    }
    val requestFile1 = file1.asRequestBody("image1/jpeg".toMediaTypeOrNull())
    val requestFile2 = file2.asRequestBody("image2/jpeg".toMediaTypeOrNull())
    val img1Part = MultipartBody.Part.createFormData("img1", file1.name, requestFile1)
    val img2Part = MultipartBody.Part.createFormData("img2", file2.name, requestFile2)
    val call = RetrofitClient.apiService.uploadImage(
        authToken = "Bearer 2tMm3KqV9+wkq+AHmFGXimwTV/y/qRRy9tOKFnwAg08=",
        img1 = img1Part,
        img2 = img2Part
    )
    call.enqueue(object : Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            if (response.isSuccessful) {
                try {
                    val rawJson = response.body()?.toString() ?: response.errorBody()?.string()
                    Log.d("FaceScanner", "Respuesta cruda de la API: $rawJson")
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        when (val result = apiResponse.result) {
                            is String -> {
                                Log.d("FaceScanner", "Resultado es una cadena: $result")
                                callback(false) // O maneja según tus necesidades
                            }
                            is Map<*, *> -> {
                                val verified = result["verified"] as? Boolean ?: false
                                val distance = result["distance"] as? Double ?: 0.0
                                val threshold = result["threshold"] as? Double ?: 0.0
                                Log.d("FaceScanner", "${if (verified) "✅" else "❌"} Verificado: $verified")
                                Log.d("FaceScanner", "Distancia: $distance")
                                Log.d("FaceScanner", "Threshold: $threshold")
                                callback(verified)
                            }
                            else -> {
                                Log.e("FaceScanner", "Formato de respuesta desconocido")
                                callback(false)
                            }
                        }
                    } else {
                        Log.e("FaceScanner", "Respuesta de la API es nula")
                        callback(false)
                    }
                } catch (e: Exception) {
                    Log.e("FaceScanner", "Error al parsear respuesta", e)
                    callback(false)
                }
            } else {
                Log.e("FaceScanner", "❌ Error en la API: ${response.errorBody()?.string()}")
            }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            Log.e("FaceScanner", "Error en la llamada API", t)
            callback(false)
        }
    })
}
fun saveBitmapToFile(bitmap: Bitmap, context: Context, filename: String): File? {
    val file = File(context.cacheDir, filename)
    return try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        Log.d("FaceScanner", "Imagen guardada temporalmente en: ${file.absolutePath}")
        file
    } catch (e: Exception) {
        Log.e("FaceScanner", "Error al guardar bitmap", e)
        null
    }
}
