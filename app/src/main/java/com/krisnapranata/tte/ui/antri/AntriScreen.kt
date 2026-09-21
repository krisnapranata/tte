package com.krisnapranata.tte.ui.antri

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.JenisSurat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntriScreen(vm: AppViewModel) {
    val antri by vm.antri.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val jenis by vm.jenis.collectAsStateWithLifecycle()

    LaunchedEffect(jenis) { vm.mulaiPolling() }
    DisposableEffect(Unit) {
        onDispose { vm.stopPolling() }
    }
    BackHandler { vm.kembaliHome() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menunggu: ${JenisSurat.label(jenis)}") },
                navigationIcon = {
                    IconButton(onClick = { vm.kembaliHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    TextButton(onClick = { vm.mulaiPolling() }) { Text("Refresh") }
                },
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (antri?.found != true) {
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Menunggu pasien... (polling tiap 3 detik)")
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            antri?.pasien?.nm_pasien ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("No. RM: ${antri?.pasien?.no_rkm_medis ?: "-"}")
                        Text("No. Rawat: ${antri?.no_rawat ?: "-"}")
                        Text("No. Surat: ${antri?.key ?: "-"}")
                        val jk = antri?.pasien?.jk
                        val lahir = antri?.pasien?.tgl_lahir
                        if (!jk.isNullOrBlank() || !lahir.isNullOrBlank()) {
                            Text("${jk ?: "-"} / ${lahir ?: "-"}")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (!antri?.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = antri?.photoUrl,
                        contentDescription = "Foto surat",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    )
                }

                if (antri?.sudahDikonfirmasi == true) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Surat sudah pernah dikonfirmasi (Persetujuan/Penolakan)",
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { vm.mulaiKie() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Mulai KIE")
                }
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
