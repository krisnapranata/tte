package com.krisnapranata.tte.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.JenisSurat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel) {
    val session by vm.session.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pilih Surat")
                        Text(
                            "${session.nama} (${session.nik})",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { vm.logout() }) { Text("Keluar") }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
        ) {
            items(JenisSurat.daftar) { (kode, label) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { vm.pilihJenis(kode) },
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
