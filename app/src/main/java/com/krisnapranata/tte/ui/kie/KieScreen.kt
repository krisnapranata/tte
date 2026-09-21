package com.krisnapranata.tte.ui.kie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.JenisSurat
import com.krisnapranata.tte.KONFIRMASI_TINDAKAN
import com.krisnapranata.tte.PENGOBATAN_KEPADA

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KieScreen(vm: AppViewModel) {
    val kie by vm.kie.collectAsStateWithLifecycle()
    val jenis by vm.jenis.collectAsStateWithLifecycle()
    val pengobatanKepada by vm.pengobatanKepada.collectAsStateWithLifecycle()
    val nilaiKepercayaan by vm.nilaiKepercayaan.collectAsStateWithLifecycle()
    val pilihan by vm.pilihan.collectAsStateWithLifecycle()
    val konfirmasi by vm.konfirmasi.collectAsStateWithLifecycle()

    var dropdownPengobatan by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("KIE: ${JenisSurat.label(jenis)}") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            kie.forEach { bagian ->
                if (bagian.judul.isNotBlank()) {
                    Text(bagian.judul, style = MaterialTheme.typography.titleMedium)
                }
                if (bagian.teks.isNotBlank()) {
                    Text(bagian.teks, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            when (jenis) {
                "umum" -> {
                    Text("Data Persetujuan", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Box {
                        OutlinedTextField(
                            value = pengobatanKepada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pengobatan kepada") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih",
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable { dropdownPengobatan = true }
                        )
                        DropdownMenu(
                            expanded = dropdownPengobatan,
                            onDismissRequest = { dropdownPengobatan = false },
                        ) {
                            PENGOBATAN_KEPADA.forEach { opsi ->
                                DropdownMenuItem(
                                    text = { Text(opsi) },
                                    onClick = {
                                        vm.onPengobatanKepada(opsi)
                                        dropdownPengobatan = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nilaiKepercayaan,
                        onValueChange = { teks ->
                            if (teks.length <= 50) vm.onNilaiKepercayaan(teks)
                        },
                        label = { Text("Nilai kepercayaan (maks. 50 huruf)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                "tindakan", "dpjp" -> {
                    Text("Pernyataan", style = MaterialTheme.typography.titleMedium)
                    listOf("Persetujuan", "Penolakan").forEach { opsi ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = pilihan == opsi,
                                onClick = { vm.onPilihan(opsi) },
                            )
                            Text(opsi)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    KONFIRMASI_TINDAKAN.forEach { (key, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = konfirmasi[key] == true,
                                onCheckedChange = { vm.onKonfirmasi(key, it) },
                            )
                            Text(label)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.lanjutFoto() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lanjut Foto")
            }
        }
    }
}
