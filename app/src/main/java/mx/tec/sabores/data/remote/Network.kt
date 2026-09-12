package mx.tec.sabores.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object Network {
    private const val BASE_URL = "https://startdroid.com/api/"
    const val alumno = "a01562825"
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val identity = Interceptor { chain ->
        chain.proceed(chain.request().newBuilder().header("X-Alumno", alumno).build())
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        // Una escritura sin respuesta no se repite automáticamente.
        .retryOnConnectionFailure(false)
        .addInterceptor(identity)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()
    val api: SaboresApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build().create(SaboresApi::class.java)
}
