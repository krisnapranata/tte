package com.krisnapranata.tte.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krisnapranata.tte.AppViewModel
import com.krisnapranata.tte.Step
import com.krisnapranata.tte.ui.antri.AntriScreen
import com.krisnapranata.tte.ui.camera.CameraScreen
import com.krisnapranata.tte.ui.hasil.HasilScreen
import com.krisnapranata.tte.ui.home.HomeScreen
import com.krisnapranata.tte.ui.kie.KieScreen
import com.krisnapranata.tte.ui.login.LoginScreen
import com.krisnapranata.tte.ui.sign.SignatureScreen

@Composable
fun AppRoot(vm: AppViewModel) {
    val step by vm.step.collectAsStateWithLifecycle()
    when (step) {
        Step.LOGIN -> LoginScreen(vm)
        Step.HOME -> HomeScreen(vm)
        Step.ANTRI -> AntriScreen(vm)
        Step.KIE -> KieScreen(vm)
        Step.FOTO -> CameraScreen(vm)
        Step.TTD -> SignatureScreen(vm)
        Step.HASIL -> HasilScreen(vm)
    }
}
