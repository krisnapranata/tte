# TTE — Persetujuan Elektronik (KIE + Foto + TTD via Android)

Aplikasi Android (Kotlin) + API Django untuk alur persetujuan/penolakan tindakan medis
di SIMRS Khanza. Petugas tetap mengisi data surat di **SIMRS Khanza (Java desktop)**,
lalu pasien/keluarga melakukan KIE (Komunikasi Informasi Edukasi), foto bukti, dan
tanda tangan **di HP Android**. Java desktop dan webapp PHP **tidak diubah sama sekali**.

Repo: `https://github.com/krisnapranata/tte.git` (lokal: `/home/krisna/iDRG/tte`)
Backend: proyek Django `casemixiDRG` (`/home/krisna/iDRG/casemix/casemixiDRG`)

---

## 1. Alur bisnis

```
Java (PC)                          Android (HP)                    Penyimpanan
───────────                        ─────────────                   ─────────────
1. isi form surat → Simpan
   (no_surat dibuat: PSU/PM/APS/
    PPU/PRI/PPP/PAM/PPHIV/DPJP
    + yyyyMMdd + 3 digit)
2. klik "Ambil"
   → tulis baris tabel antri*
   → buka browser webapp PHP
     lama (dibiarkan apa adanya)
                                   3. app dibuka dulu di HP,
                                      tombol Refresh → polling
                                      GET /api/surat/antri/<jenis>/
                                      → baris antri* terbaca
                                   4. tampil KIE (teks persis
                                      webapp, perawat yang
                                      membacakan) + input:
                                      • umum: pengobatan_kepada,
                                        nilai_kepercayaan
                                      • tindakan: Setuju/Tolak
                                        + 11 checkbox konfirmasi
                                   5. foto (kamera HP) + TTD
                                      (signature pad)
                                   6. kirim:
                                      POST /api/surat/foto/
                                      POST /api/surat/ttd/
                                   7. selesai → status terkirim
8. klik Refresh foto di Java
   → foto HP tampil (path & tabel
   sama dengan webapp PHP)
9. cetak jasper tetap berfungsi
```

Klik "Ambil" di Java menulis satu baris ke tabel scratch `antri*` (MyISAM) yang
dibaca Android lewat API. Karena Java & PHP tidak diubah, alur lama (webcam PHP)
tetap bisa dipakai sebagai fallback.

---

## 2. Keputusan desain

| Aspek | Keputusan |
|---|---|
| Pengisian data surat | Tetap di Java desktop (tidak dibuat ulang di Android) |
| Trigger Android | Polling tabel `antri*` via API (aplikasi dibuka dulu di HP) |
| Teks KIE | Verbatim dari `kamera.php` webapp (`/var/www/html/webappsyatofaedit`), disimpan di customsik (tabel `surat_kie`), bisa diedit via Django admin |
| Pembaca KIE | Perawat (tidak pakai TTS) |
| Foto | Kamera HP → API Django → file JPEG ditulis ke `<WEBAPPS_ROOT>/<app>/pages/upload/<key>[PP].jpeg` (lokasi sama dengan webapp PHP) + path relatif `pages/upload/<file>` ditulis ke tabel anak legacy |
| TTD | Gambar di layar HP (base64 PNG) → tabel `surat_signature` di customsik |
| PDF | Server-side WeasyPrint (`GET /api/surat/pdf/<jenis>/<key>/`), foto dari path legacy + TTD dari customsik |
| Auth API | `X-API-KEY` + `X-USER` (nik pegawai), pola sama dengan SBAR (`pelayanan/api/sbar/sbar_auth.py`) |
| Build Android | GitHub Actions → `assembleRelease` → artifact APK |

---

## 3. Backend (casemixiDRG)

### 3.1 File yang ditambahkan

| File | Isi |
|---|---|
| `pelayanan/models/surat.py` | Model customsik `SuratKie`, `SuratSignature` (+ pilihan `JENIS_SURAT_CHOICES`) |
| `pelayanan/models/__init__.py` | Import `surat` |
| `pelayanan/services/surat_service.py` | Mapping `JENIS`, baca antri, data pasien/surat, KIE, simpan foto (replika `storeImage.php`), simpan TTD |
| `pelayanan/api/surat/views.py` | View DRF: antri, kie, foto, ttd, pdf |
| `pelayanan/api/surat/urls.py` | Routing API |
| `pelayanan/views/webapps_views.py` | `serve_webapps` — sajikan foto `/webapps/<app>/<path>` dari `WEBAPPS_ROOT`/SFTP |
| `idrg/urls.py` | Route `^webapps/` (pola sama `^pages/upload/`) |
| `pelayanan/urls.py` | Mount `api/surat/` |
| `idrg/settings.py` | Setting `WEBAPPS_*` (lihat 3.4) |
| `pelayanan/migrations/0015_suratkie_suratsignature.py` | Migrasi customsik |

### 3.2 Tabel customsik (migrasi, DB `customsik`)

```sql
surat_kie(jenis_surat, no_urut, judul, teks)
surat_signature(jenis_surat, no_surat, no_rawat, ttd_pembuat, created_by, created_at)
```

### 3.3 Endpoint API

Auth: header `X-API-KEY` + `X-USER` (nik). Base: `/api/surat/`

| Method | URL | Keterangan |
|---|---|---|
| GET | `/api/surat/antri/<jenis>/` | Baca baris `antri*`. Balasan: `found`, `key`, `no_rawat`, `pasien`, `surat`, `photo`, `photo_url`, `ttd_ada`, `sudah_dikonfirmasi`, `webapp` |
| GET | `/api/surat/kie/<jenis>/` | Teks KIE per bagian (placeholder sudah ter-render) |
| POST | `/api/surat/foto/` | Body: `{jenis, key, image(base64/dataURI), fields{...}}` → tulis file + tabel anak + Ubah2 |
| POST | `/api/surat/ttd/` | Body: `{jenis, key, no_rawat, ttd(base64 PNG)}` → customsik |
| GET | `/api/surat/pdf/<jenis>/<key>/` | PDF surat (foto + TTD) |

`jenis` yang valid: `umum`, `tindakan`, `aps`, `pernyataanumum`, `rawatinap`,
`penundaan`, `penolakan`, `hiv`, `dpjp`.

`fields` per jenis pada POST foto:

```json
// umum
{"pengobatan_kepada": "Suami", "nilai_kepercayaan": "..."}
// tindakan
{"pilihan": "Persetujuan", "konfirmasi": {"diagnosa_konfirmasi": true, ...}}
```

### 3.4 Setting baru (`idrg/settings.py`)

```
WEBAPPS_ROOT          = /var/www/html/webapps   (default; env WEBAPPS_ROOT)
WEBAPPS_REMOTE_HOST   = ""                       (opsional SFTP bila beda server)
WEBAPPS_REMOTE_PORT   = 22
WEBAPPS_REMOTE_USER   = ""
WEBAPPS_REMOTE_PATH   = ""
WEBAPPS_REMOTE_KEY    = ""
WEBAPPS_REMOTE_PASSWORD= ""
WEBAPPS_PUBLIC_BASE   = ""                       (host publik untuk URL foto)
```

Catatan deploy: `WEBAPPS_ROOT` default `/var/www/html/webapps`. Foto disajikan Django di
`/webapps/<app>/<path>` lewat view `serve_webapps` (pola sama dengan `serve_berkas`), jadi tidak
perlu alias nginx. `WEBAPPS_PUBLIC_BASE` diisi prefix yang **sudah termasuk `/webapps`** (mis.
`http://192.168.1.78/webapps`); bila kosong, URL dibangun dari host request. Mode SFTP aktif bila
`WEBAPPS_REMOTE_HOST` diisi, memakai kredensial `WEBAPPS_REMOTE_*`.

Contoh `.env` production (pola sama dengan berkas digital):

```env
WEBAPPS_REMOTE_HOST=192.168.1.78
WEBAPPS_REMOTE_PORT=22
WEBAPPS_REMOTE_USER=root
WEBAPPS_REMOTE_PASSWORD=<sama dengan BERKAS_REMOTE_PASSWORD>
WEBAPPS_REMOTE_PATH=/opt/slemp/wwwroot/yatofa.net/webapps
WEBAPPS_PUBLIC_BASE=https://simrs.rsiyatofa.co.id/webapps
```

Untuk pemakaian **hanya di jaringan RS**, `WEBAPPS_PUBLIC_BASE` bisa diarahkan ke
`http://192.168.1.78/webapps` (foto langsung dari storage, bukan lewat Django).

---

## 4. Mapping jenis surat (hasil riset kode Java + webapp PHP)

| jenis | tabel utama | tabel anak (photo) | antri | file | Ubah2 saat foto |
|---|---|---|---|---|---|
| `umum` | `surat_persetujuan_umum` | `..._pembuat_pernyataan` | `antripersetujuanumum` | `<key>.jpeg` | `pengobatan_kepada`, `nilai_kepercayaan` |
| `tindakan` | `persetujuan_penolakan_tindakan` | `bukti_..._penerimainformasi` | `antripersetujuan` | `<key>PP.jpeg` | 11 kolom `*_konfirmasi` + `pernyataan` |
| `aps` | `surat_pulang_atas_permintaan_sendiri` | `..._pembuat_pernyataan` | `antriaps` | `<key>PP.jpeg` | — |
| `pernyataanumum` | `surat_pernyataan_pasien_umum` | `..._pembuat_pernyataan` | `antripernyataanumum` | `<key>.jpeg` | — |
| `rawatinap` | `surat_persetujuan_rawat_inap` | `..._pembuat_pernyataan` | `antripersetujuanrawatinap` | `<key>.jpeg` | — |
| `penundaan` | `persetujuan_penundaan_pelayanan` | `bukti_persetujuan_penundaan_pelayanan` | `antripenundaanpelayanan` | `<key>.jpeg` | — |
| `penolakan` | `surat_penolakan_anjuran_medis` | `..._pembuat_pernyataan` | `antripenolakananjuranmedis` | `<key>.jpeg` | — |
| `hiv` | `surat_persetujuan_pemeriksaan_hiv` | `..._pembuat_persetujuan` | `antripersetujuanpemeriksaanhiv` | `<key>.jpeg` | — |
| `dpjp` | `surat_pernyataan_memilih_dpjp` | `bukti_surat_pernyataan_memilih_dpjp` | `antripersetujuan` (shared dgn tindakan) | `<key>PP.jpeg` | — |

Folder webapp (untuk penulisan file): `persetujuanumum`, `persetujuantindakan`,
`pulangaps`, `pernyataanumum`, `persetujuanrawatinap`, `penundaanpelayanan`,
`penolakananjuranmedis`, `persetujuanpemeriksaanhiv`, `persetujuantindakan` (dpjp).

Catatan:
- `persetujuan_penolakan_tindakan.pernyataan` enum: `Belum Dikonfirmasi | Persetujuan | Penolakan`.
- Kolom `photo` tabel anak menyimpan path relatif (`pages/upload/<file>.jpeg`); file
  fisik ada di `<WEBAPPS_ROOT>/<app>/pages/upload/`.
- Kolom `photo` tabel `..._saksikeluarga` tidak diisi (mengikuti webapp — hanya 1 foto).

---

## 5. KIE

- Konten di-seed verbatim dari `/var/www/html/webappsyatofaedit/<app>/pages/kamera.php`
  ke `surat_kie` (per bagian: `judul` + `teks` dengan baris per item).
- Placeholder dalam teks: `{nama_instansi}`, `{no_surat}`, `{tanggal}`, `{nm_pasien}`,
  `{no_rkm_medis}`, `{jk}`, `{tgl_lahir}`, `{umur_pj}`, `{nama_pj}`, `{no_ktppj}`,
  `{jkpj}`, `{bertindak_atas}`, `{no_telp}`, `{jumlah_bed}`, `{kabupaten}`, dan
  field-field lain dari tabel utama (tindakan: `{diagnosa}`, `{tindakan}`, ...).
- Nilai dinamis di-render dari DB dengan SQL yang sama dengan PHP
  (`setting.nama_instansi`, `setting.kabupaten`, jumlah bed dari `kamar`).
- Struktur bagian per jenis (dari webapp):
  - `umum`: I. Ketentuan Rawat Inap (A–G) · II. Informasi Hak & Kewajiban Pasien
    (UU 44/2009 Ps. 32) · III. Persetujuan Umum (10 poin) · IV. Pernyataan
  - `tindakan`: pernyataan + 11 item medis + checkbox
  - `aps`, `pernyataanumum`, `rawatinap`, `penundaan`, `penolakan`, `hiv`: teks pernyataan

---

## 6. Android (`/home/krisna/iDRG/tte`)

> Status: **sudah di-scaffold** — proyek Gradle Kotlin/Compose ada di repo ini; build
> `assembleDebug` dan `assembleRelease` (unsigned) lolos dengan JDK 17 + Android SDK 36.
> Preview PDF (opsional) belum diimplementasikan.

### 6.1 Teknologi
Kotlin, Jetpack Compose, Retrofit/OkHttp, CameraX, DataStore (simpan nik petugas).

Struktur: `app/src/main/java/com/krisnapranata/tte/` — `data/` (DTO, Retrofit, DataStore),
`ui/<layar>/`, `AppViewModel.kt` (state machine `Step`).

### 6.2 Layar
1. Pilih petugas (login) — cari via `GET /master/api/pegawai/autocomplete/?q=` (min. 3 huruf)
   atau isi NIK manual; alamat server bisa diubah dan disimpan di DataStore
2. Home — daftar 9 jenis surat
3. Mode "Menunggu Ambi" — polling `GET /api/surat/antri/<jenis>/` tiap ±3 detik
   + tombol Refresh (perilaku sama seperti tombol Refresh di webapp)
4. Layar KIE — teks per bagian (dibacakan perawat) + input: `umum` (`pengobatan_kepada`,
   `nilai_kepercayaan`), `tindakan`/`dpjp` (radio Persetujuan/Penolakan + 11 checkbox)
5. Kamera — CameraX → JPEG (dikompres maksimal 1600 px, kualitas 80) → base64
6. TTD — signature pad (Canvas) → PNG (maksimal 800 px) → base64
7. Kirim (foto + ttd) → status; POST `/api/surat/foto/` lalu `/api/surat/ttd/`
8. Preview PDF (opsional) — belum ada

### 6.3 Konfigurasi
- `gradle.properties`: `tteApiBaseUrl` (default `https://simrs.rsiyatofa.co.id/` untuk produksi;
  emulator: `http://10.0.2.2:8000/`) dan `tteApiKey` → menjadi `BuildConfig.API_BASE_URL` / `API_KEY`.
- Di HP, alamat server bisa diubah dari layar login (mis. `http://192.168.1.176:8000/`) — tersimpan.
- **Release HTTPS-only**: `usesCleartextTraffic` hanya aktif di build debug (`src/debug/AndroidManifest.xml`).
  Di APK release, alamat server wajib `https://...`; HTTP hanya untuk debug/uji lokal.
- Manifest mengizinkan cleartext HTTP karena server internal umumnya tanpa TLS.

### 6.4 Build
- Lokal (JDK 17): `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`;
  `./gradlew assembleRelease` → `app-release-unsigned.apk` (tanpa keystore) atau
  `app-release.apk` (signed, bila env keystore diisi).
- GitHub Actions: `.github/workflows/build.yml` → `./gradlew assembleRelease`, artifact
  `tte-release-apk`.

### 6.5 Signing release
- Keystore ada di luar repo (`~/keystores/tte-release.jks`); `*.jks`/`*.keystore` di-gitignore.
- GitHub Secrets yang dipakai workflow: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD` — workflow men-decode keystore ke `$RUNNER_TEMP` sebelum build.
- Build lokal signed:
  ```bash
  KEYSTORE_FILE=~/keystores/tte-release.jks \
  KEYSTORE_PASSWORD=<pass> KEY_ALIAS=tte KEY_PASSWORD=<pass> \
  ./gradlew assembleRelease
  ```
- Verifikasi: `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`.

---

## 7. Verifikasi backend

```bash
cd /home/krisna/iDRG/casemix/casemixiDRG
# venv ada di luar repo: /home/krisna/iDRG/casemix/venv
/home/krisna/iDRG/casemix/venv/bin/python3 manage.py shell -c "
from pelayanan.services import surat_service as s
print(s.baca_antri('umum'))
print(s.kie_teks('umum')[:1])
"
# uji endpoint
curl -H 'X-API-KEY: rahasia123' -H 'X-USER: <nik>' \
  http://127.0.0.1:8000/api/surat/antri/umum/
```

## 8. Catatan penting

- Router DB hanya memblokir migrasi di DB `default` — tulis/baca raw SQL aman.
- `antripersetujuan` dipakai bersama oleh `tindakan` dan `dpjp` (sama seperti Java).
- File foto ditimpa bila surat yang sama difoto ulang (perilaku lama dipertahankan).
- API key hardcode (`rahasia123`) mengikuti pola SBAR; pindah ke env bila perlu.
- Kolom `photo` milik webapp PHP TIDAK ditulis dari jalur lain selain `simpan_foto`
  agar path tetap konsisten `pages/upload/<file>`.
- Foto surat disajikan `/webapps/<app>/<path>` oleh `serve_webapps` (bukan alias nginx);
  PDF meng-embed foto sebagai data URI, jadi tidak bergantung jangkauan HTTP.
