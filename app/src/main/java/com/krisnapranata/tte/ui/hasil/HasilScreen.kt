package com.krisnapranata.tte.ui.hasil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HasilScreen(vm: AppViewModel) {
    val status by vm.status.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Status") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Mengirim data...")
            } else {
                Text(
                    status,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = if (status.startsWith("Berhasil")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Spacer(Modifier.height(24.dp))
                if (status.startsWith("Berhasil")) {
                    Button(
                        onClick = { vm.kembaliHome() },
                        modifier = Modifier.fillMaxSize(0.6f),
                    ) {
                        Text("Selesai")
                    }
                } else {
                    Button(
                        onClick = { vm.ulangi() },
                        modifier = Modifier.fillMaxSize(0.6f),
                    ) {
                        Text("Ulangi")
                    }
                    TextButton(onClick = { vm.kembaliHome() }) {
                        Text("Ke Beranda")
                    }
                }
            }
        }
    }
}
