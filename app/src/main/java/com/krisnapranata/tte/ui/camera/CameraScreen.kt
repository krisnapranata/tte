package com.krisnapranata.tte.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.ui.base64ToBitmap
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fotoB64 by vm.fotoB64.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return@LaunchedEffect
        }
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    val provider = future.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        capture,
                    )
                    imageCapture = capture
                } catch (e: Exception) {
                    vm.setError("Kamera gagal: ${e.message}")
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Foto Bukti") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            if (hasPermission) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
            } else {
                Text("Izin kamera diperlukan untuk mengambil foto bukti.")
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    val file = File(context.cacheDir, "tte_foto.jpg")
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    capture.takePicture(
                        options,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(
                                outputFileResults: ImageCapture.OutputFileResults,
                            ) {
                                val b64 = Base64.encodeToString(
                                    file.readBytes(),
                                    Base64.NO_WRAP,
                                )
                                vm.setFoto(b64)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                vm.setError("Gagal ambil foto: ${exception.message}")
                            }
                        },
                    )
                },
                enabled = hasPermission && imageCapture != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (fotoB64 == null) "Ambil Foto" else "Foto Ulang")
            }

            val foto = fotoB64
            if (foto != null) {
                val bitmap = remember(foto) { base64ToBitmap(foto) }
                if (bitmap != null) {
                    Spacer(Modifier.height(12.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Preview foto",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.lanjutTtd() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Lanjut Tanda Tangan")
                    }
                }
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
