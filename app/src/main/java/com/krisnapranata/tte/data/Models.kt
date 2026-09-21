package com.krisnapranata.tte.data

import com.google.gson.annotations.SerializedName

data class PegawaiDto(
    val nik: String = "",
    val nama: String = "",
)

data class PasienDto(
    val no_rawat: String? = null,
    val no_rkm_medis: String? = null,
    val nm_pasien: String? = null,
    val jk: String? = null,
    val tgl_lahir: String? = null,
    val alamat: String? = null,
)

data class AntriResponse(
    val found: Boolean = false,
    val jenis: String? = null,
    val key: String? = null,
    val no_rawat: String? = null,
    val pasien: PasienDto? = null,
    val surat: Map<String, Any?>? = null,
    val photo: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    @SerializedName("ttd_ada") val ttdAda: Boolean = false,
    @SerializedName("sudah_dikonfirmasi") val sudahDikonfirmasi: Boolean = false,
    val webapp: String? = null,
)

data class KieBagian(
    val no_urut: Int = 0,
    val judul: String = "",
    val teks: String = "",
)

data class KieResponse(
    val jenis: String? = null,
    val bagian: List<KieBagian> = emptyList(),
)

data class FotoRequest(
    val jenis: String,
    val key: String,
    val image: String,
    val fields: Map<String, Any?>? = null,
)

data class FotoResponse(
    val ok: Boolean = false,
    val jenis: String? = null,
    val key: String? = null,
    val photo: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val detail: String? = null,
)

data class TtdRequest(
    val jenis: String,
    val key: String,
    @SerializedName("no_rawat") val noRawat: String,
    val ttd: String,
)

data class TtdResponse(
    val ok: Boolean = false,
    @SerializedName("signature_id") val signatureId: Long? = null,
    val detail: String? = null,
)
