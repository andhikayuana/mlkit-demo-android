@file:OptIn(ExperimentalPermissionsApi::class)

package id.yuana.mlkit.demo.ui.components

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun CameraComponent(
    modifier: Modifier = Modifier,
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


    if (cameraPermissionState.status.isGranted) {
        CameraPreview(modifier = modifier)
    } else {
        CameraPermissionContent(
            modifier = modifier,
            permissionState = cameraPermissionState
        )
    }
}