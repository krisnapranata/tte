package com.krisnapranata.tte.ui.sign

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.ui.scaleBitmap
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureScreen(vm: AppViewModel) {
    val ttdB64 by vm.ttdB64.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var redraw by remember { mutableIntStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tanda Tangan Pembuat Pernyataan") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Minta pasien/keluarga menandatangani di area putih.")
            Spacer(Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        val p = Path().apply {
                                            moveTo(change.position.x, change.position.y)
                                        }
                                        paths.add(p)
                                        currentPath = p
                                    }

                                    PointerEventType.Move -> {
                                        currentPath?.lineTo(change.position.x, change.position.y)
                                        redraw++
                                    }

                                    PointerEventType.Release -> currentPath = null
                                }
                            }
                        }
                    },
            ) {
                redraw
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row {
                OutlinedButton(
                    onClick = {
                        paths.clear()
                        currentPath = null
                        redraw++
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Hapus")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val size = canvasSize
                        if (size.width == 0 || size.height == 0) return@Button
                        val bitmap = scaleBitmap(renderSignature(size, paths.toList()), 800)
                        val bos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                        vm.setTtd(Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP))
                    },
                    enabled = paths.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Simpan TTD")
                }
            }

            if (ttdB64 != null) {
                Spacer(Modifier.height(16.dp))
                Text("Tanda tangan tersimpan.", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.kirim() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (loading) "Mengirim..." else "Kirim Foto & TTD")
                }
            }
        }
    }
}

private fun renderSignature(size: IntSize, paths: List<Path>): Bitmap {
    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    paths.forEach { path ->
        canvas.drawPath(path.asAndroidPath() as AndroidPath, paint)
    }
    return bitmap
}
