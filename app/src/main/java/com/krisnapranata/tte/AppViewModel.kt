package com.krisnapranata.tte

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krisnapranata.tte.data.ApiClient
import com.krisnapranata.tte.data.AntriResponse
import com.krisnapranata.tte.data.FotoRequest
import com.krisnapranata.tte.data.KieBagian
import com.krisnapranata.tte.data.PegawaiDto
import com.krisnapranata.tte.data.Session
import com.krisnapranata.tte.data.SessionStore
import com.krisnapranata.tte.data.TteApi
import com.krisnapranata.tte.data.TtdRequest
import com.krisnapranata.tte.ui.errorMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Step { LOGIN, HOME, ANTRI, KIE, FOTO, TTD, HASIL }

object JenisSurat {
    val daftar = listOf(
        "umum" to "Persetujuan Umum",
        "tindakan" to "Persetujuan/Penolakan Tindakan",
        "aps" to "Pulang Atas Permintaan Sendiri",
        "pernyataanumum" to "Pernyataan Pasien Umum",
        "rawatinap" to "Persetujuan Rawat Inap",
        "penundaan" to "Persetujuan Penundaan Pelayanan",
        "penolakan" to "Penolakan Anjuran Medis",
        "hiv" to "Persetujuan Pemeriksaan HIV",
        "dpjp" to "Pernyataan Memilih DPJP",
    )

    fun label(jenis: String): String =
        daftar.firstOrNull { it.first == jenis }?.second ?: jenis
}

val KONFIRMASI_TINDAKAN = listOf(
    "diagnosa_konfirmasi" to "Diagnosa",
    "tindakan_konfirmasi" to "Tindakan",
    "indikasi_tindakan_konfirmasi" to "Indikasi Tindakan",
    "tata_cara_konfirmasi" to "Tata Cara",
    "tujuan_konfirmasi" to "Tujuan",
    "risiko_konfirmasi" to "Risiko",
    "komplikasi_konfirmasi" to "Komplikasi",
    "prognosis_konfirmasi" to "Prognosis",
    "alternatif_konfirmasi" to "Alternatif dan Risikonya",
    "biaya_konfirmasi" to "Biaya",
    "lain_lain_konfirmasi" to "Lain-lain",
)

val PENGOBATAN_KEPADA = listOf(
    "Suami",
    "Istri",
    "Anak",
    "Ayah",
    "Ibu",
    "Saudara",
    "Keponakan",
    "Adik",
    "Kakak",
    "Orang Tua",
    "Diri Sendiri",
    "-",
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SessionStore(app)
    private var api: TteApi? = null
    private var pollingJob: Job? = null

    private val _step = MutableStateFlow(Step.LOGIN)
    val step: StateFlow<Step> = _step.asStateFlow()

    private val _session = MutableStateFlow(Session(BuildConfig.API_BASE_URL, "", ""))
    val session: StateFlow<Session> = _session.asStateFlow()

    private val _serverInput = MutableStateFlow(BuildConfig.API_BASE_URL)
    val serverInput: StateFlow<String> = _serverInput.asStateFlow()

    private val _cari = MutableStateFlow("")
    val cari: StateFlow<String> = _cari.asStateFlow()

    private val _hasilCari = MutableStateFlow<List<PegawaiDto>>(emptyList())
    val hasilCari: StateFlow<List<PegawaiDto>> = _hasilCari.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _jenis = MutableStateFlow("umum")
    val jenis: StateFlow<String> = _jenis.asStateFlow()

    private val _antri = MutableStateFlow<AntriResponse?>(null)
    val antri: StateFlow<AntriResponse?> = _antri.asStateFlow()

    private val _kie = MutableStateFlow<List<KieBagian>>(emptyList())
    val kie: StateFlow<List<KieBagian>> = _kie.asStateFlow()

    private val _pengobatanKepada = MutableStateFlow("Diri Sendiri")
    val pengobatanKepada: StateFlow<String> = _pengobatanKepada.asStateFlow()

    private val _nilaiKepercayaan = MutableStateFlow("")
    val nilaiKepercayaan: StateFlow<String> = _nilaiKepercayaan.asStateFlow()

    private val _pilihan = MutableStateFlow("Persetujuan")
    val pilihan: StateFlow<String> = _pilihan.asStateFlow()

    private val _konfirmasi = MutableStateFlow(
        KONFIRMASI_TINDAKAN.associate { it.first to false }
    )
    val konfirmasi: StateFlow<Map<String, Boolean>> = _konfirmasi.asStateFlow()

    private val _fotoB64 = MutableStateFlow<String?>(null)
    val fotoB64: StateFlow<String?> = _fotoB64.asStateFlow()

    private val _ttdB64 = MutableStateFlow<String?>(null)
    val ttdB64: StateFlow<String?> = _ttdB64.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    init {
        viewModelScope.launch {
            store.session.collect { s ->
                _session.value = s
                _serverInput.value = s.serverUrl
                if (s.nik.isNotBlank()) {
                    api = ApiClient.create(s.serverUrl, s.nik)
                }
            }
        }
    }

    fun onServerInput(value: String) {
        _serverInput.value = value
    }

    fun onCari(value: String) {
        _cari.value = value
    }

    fun onPengobatanKepada(value: String) {
        _pengobatanKepada.value = value
    }

    fun onNilaiKepercayaan(value: String) {
        _nilaiKepercayaan.value = value
    }

    fun onPilihan(value: String) {
        _pilihan.value = value
    }

    fun onKonfirmasi(key: String, checked: Boolean) {
        _konfirmasi.value = _konfirmasi.value + (key to checked)
    }

    fun setFoto(base64: String) {
        _fotoB64.value = base64
    }

    fun setTtd(base64: String) {
        _ttdB64.value = base64
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun masuk(nik: String, nama: String) {
        if (nik.isBlank()) {
            _error.value = "NIK wajib diisi"
            return
        }
        viewModelScope.launch {
            val url = _serverInput.value.trim()
            store.saveServer(url)
            store.saveUser(nik, nama)
            api = ApiClient.create(url, nik)
            _error.value = ""
            _step.value = Step.HOME
        }
    }

    fun logout() {
        pollingJob?.cancel()
        viewModelScope.launch {
            store.clearUser()
            api = null
            _antri.value = null
            _hasilCari.value = emptyList()
            _step.value = Step.LOGIN
        }
    }

    fun cariPegawai() {
        val q = _cari.value.trim()
        if (q.length < 3) {
            _error.value = "Minimal 3 huruf untuk mencari"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                val tmp = ApiClient.create(_serverInput.value.trim(), "x")
                _hasilCari.value = tmp.cariPegawai(q)
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "Gagal mencari: ${errorMessage(e)}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun pilihJenis(value: String) {
        _jenis.value = value
        _antri.value = null
        _kie.value = emptyList()
        _fotoB64.value = null
        _ttdB64.value = null
        _status.value = ""
        _step.value = Step.ANTRI
    }

    fun kembaliHome() {
        pollingJob?.cancel()
        _step.value = Step.HOME
    }

    fun mulaiPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    _antri.value = api?.antri(_jenis.value)
                    _error.value = ""
                } catch (e: Exception) {
                    _error.value = "Gagal polling: ${errorMessage(e)}"
                }
                delay(3000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    fun mulaiKie() {
        val key = _antri.value?.key ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = api?.kie(_jenis.value, key)
                _kie.value = res?.bagian ?: emptyList()
                _step.value = Step.KIE
            } catch (e: Exception) {
                _error.value = "Gagal ambil KIE: ${errorMessage(e)}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun lanjutFoto() {
        _step.value = Step.FOTO
    }

    fun lanjutTtd() {
        _step.value = Step.TTD
    }

    fun kirim() {
        val key = _antri.value?.key ?: return
        val noRawat = _antri.value?.no_rawat ?: ""
        val foto = _fotoB64.value
        val ttd = _ttdB64.value
        if (foto == null || ttd == null) {
            _status.value = "Foto dan tanda tangan wajib diisi"
            _step.value = Step.HASIL
            return
        }

        viewModelScope.launch {
            _loading.value = true
            var fotoTersimpan = false
            try {
                val fields: Map<String, Any?>? = when (_jenis.value) {
                    "umum" -> mapOf(
                        "pengobatan_kepada" to _pengobatanKepada.value,
                        "nilai_kepercayaan" to _nilaiKepercayaan.value,
                    )

                    "tindakan" -> mapOf(
                        "pilihan" to _pilihan.value,
                        "konfirmasi" to _konfirmasi.value,
                    )

                    else -> null
                }
                val client = api ?: throw IllegalStateException("Sesi belum siap")
                val r1 = client.kirimFoto(FotoRequest(_jenis.value, key, foto, fields))
                if (!r1.ok) throw IllegalStateException(r1.detail ?: "Gagal simpan foto")
                fotoTersimpan = true
                val r2 = client.kirimTtd(TtdRequest(_jenis.value, key, noRawat, ttd))
                if (!r2.ok) throw IllegalStateException(r2.detail ?: "Gagal simpan TTD")
                _status.value = "Berhasil dikirim"
            } catch (e: Exception) {
                _status.value = if (fotoTersimpan) {
                    "Foto tersimpan, TTD gagal: ${errorMessage(e)}"
                } else {
                    "Gagal: ${errorMessage(e)}"
                }
            } finally {
                _loading.value = false
                _step.value = Step.HASIL
            }
        }
    }

    fun ulangi() {
        _fotoB64.value = null
        _ttdB64.value = null
        _status.value = ""
        _step.value = Step.ANTRI
    }
}
