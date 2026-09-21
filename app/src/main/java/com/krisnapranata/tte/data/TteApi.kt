package com.krisnapranata.tte.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TteApi {

    @GET("master/api/pegawai/autocomplete/")
    suspend fun cariPegawai(@Query("q") q: String): List<PegawaiDto>

    @GET("api/surat/antri/{jenis}/")
    suspend fun antri(@Path("jenis") jenis: String): AntriResponse

    @GET("api/surat/kie/{jenis}/")
    suspend fun kie(
        @Path("jenis") jenis: String,
        @Query("key") key: String?,
    ): KieResponse

    @POST("api/surat/foto/")
    suspend fun kirimFoto(@Body body: FotoRequest): FotoResponse

    @POST("api/surat/ttd/")
    suspend fun kirimTtd(@Body body: TtdRequest): TtdResponse
}
