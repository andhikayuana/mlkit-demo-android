@file:OptIn(ExperimentalPermissionsApi::class)

package id.yuana.mlkit.demo.ui.components

import android.content.Context
import android.content.Intent
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import id.yuana.mlkit.demo.R

@Composable
fun CameraPermissionContent(
    modifier: Modifier = Modifier,
    permissionState: PermissionState
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textToShow = if (permissionState.status.shouldShowRationale) {
            R.string.camera_permission_rationale
        } else {
            R.string.camera_permission_request
        }
        Text(
            text = stringResource(textToShow),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (permissionState.status.shouldShowRationale) {
                    context.openAppSettings()
                } else {
                    permissionState.launchPermissionRequest()
                }
            }
        ) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

fun Context.openAppSettings() {
    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addCategory(CATEGORY_DEFAULT)
        addFlags(FLAG_ACTIVITY_NEW_TASK)
        addFlags(FLAG_ACTIVITY_NO_HISTORY)
        addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }.also { startActivity(it) }
}