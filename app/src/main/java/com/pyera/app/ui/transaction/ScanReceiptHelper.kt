package com.pyera.app.ui.transaction

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.core.content.FileProvider

@Composable
fun rememberReceiptPicker(onImagePicked: (Uri) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }

    return { launcher.launch("image/*") }
}

// Note: For a real camera implementation, we would use a more complex setup with FileProvider.
// For now, we are sticking to the system picker/gallery which can also take photos on some devices.
