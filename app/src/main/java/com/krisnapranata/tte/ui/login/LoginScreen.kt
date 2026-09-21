package com.krisnapranata.tte.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(vm: AppViewModel) {
    val server by vm.serverInput.collectAsStateWithLifecycle()
    val cari by vm.cari.collectAsStateWithLifecycle()
    val hasil by vm.hasilCari.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    var nikManual by remember { mutableStateOf("") }
    var namaManual by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("TTE Persetujuan") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Alamat Server", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = server,
                onValueChange = vm::onServerInput,
                singleLine = true,
                placeholder = { Text("http://ip-server:8000/") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Cari petugas (nama, min. 3 huruf)",
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = cari,
                    onValueChange = vm::onCari,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { vm.cariPegawai() }, enabled = !loading) {
                    Text("Cari")
                }
            }
            hasil.forEach { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { vm.masuk(p.nik, p.nama) },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.nama, style = MaterialTheme.typography.titleMedium)
                        Text(p.nik, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Text("Atau isi manual", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = nikManual,
                onValueChange = { nikManual = it },
                label = { Text("NIK pegawai") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = namaManual,
                onValueChange = { namaManual = it },
                label = { Text("Nama") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.masuk(nikManual.trim(), namaManual.trim()) },
                enabled = nikManual.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Masuk")
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
