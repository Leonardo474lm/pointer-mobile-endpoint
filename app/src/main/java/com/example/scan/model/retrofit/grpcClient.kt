package com.example.scan.model.retrofit
import android.util.Log
import com.example.scan.model.data.entiti.Empresa
import com.example.scan.model.data.entiti.Sucursal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface Service {
    @GET("/utils/lista-all/empresa") // Ajusta la ruta según tu servicio en Quarkus
    fun listarEmpresas(): Call<List<Empresa>>
    @GET("/utils/list-by-empresa/sucursal")
    fun listaSucursal(@Query("idEmpresa") idEmpresa: String):Call<List<Sucursal>>
}


object RetrofitApiClient {
    private const val BASE_URL = "http://192.168.2.3:8080" // Cambia por la URL de tu Quarkus API
    val instance: Service by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())  //todo: solo es para json
           // .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create()) // todo:string
            .build()
            .create(Service::class.java)
    }

}

fun fetchListEmpresa(lista:(List<Empresa>)->Unit={}) {
    RetrofitApiClient.instance.listarEmpresas().enqueue(object : Callback<List<Empresa>> {
        override fun onResponse(call: Call<List<Empresa>>, response: Response<List<Empresa>>) {
            if (response.isSuccessful) {
                val empresas = response.body()
                Log.d("Retrofit", "Empresas recibidas: $empresas")
                lista(empresas?.map { it }?: emptyList())
            } else {
                Log.d("Retrofit", "Error en la respuesta: ${response.code()}")
            }
        }
        override fun onFailure(call: Call<List<Empresa>>, t: Throwable) {
            Log.e("Retrofit", "Fallo en la petición: ${t.message}")
        }
    })
}
fun fetchListSucursal(idEmpresa: String,lista:(List<String>)->Unit={}) {
    RetrofitApiClient.instance.listaSucursal(idEmpresa =idEmpresa ).enqueue(object : Callback<List<Sucursal>> {
        override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
            if (response.isSuccessful) {
                val sucursal = response.body()

                Log.d("Retrofit", "Sucursal: $sucursal")
                lista(sucursal?.map{it.id_empresa}?: emptyList())
                Log.d("Retrofit", "Sucursal: $lista")

            } else {
                Log.d("Retrofit", "Error en la respuesta: ${response.code()}")
            }
        }
        override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
            Log.e("Retrofit", "Fallo en la petición: ${t.message}")
        }
    })
}