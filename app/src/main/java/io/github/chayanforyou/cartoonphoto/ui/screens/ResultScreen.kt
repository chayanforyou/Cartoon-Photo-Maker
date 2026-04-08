package io.github.chayanforyou.cartoonphoto.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chayanforyou.cartoonphoto.ui.components.BeforeAfterSlider
import io.github.chayanforyou.cartoonphoto.utils.ImageSaver
import io.github.chayanforyou.cartoonphoto.utils.PermissionHelper

@Composable
fun ResultScreen(
    originalBitmap: Bitmap,
    cartoonBitmap: Bitmap,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            saveImage(context, cartoonBitmap)
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleSaveClick() {
        if (PermissionHelper.isStoragePermissionNeeded() &&
            !PermissionHelper.hasStoragePermission(context)
        ) {
            permissionLauncher.launch(PermissionHelper.getStoragePermission())
        } else {
            saveImage(context, cartoonBitmap)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Result Image
        BeforeAfterSlider(
            before = BitmapPainter(originalBitmap.asImageBitmap()),
            after = BitmapPainter(cartoonBitmap.asImageBitmap()),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .align(Alignment.Center)
        )

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(12.dp)
                .background(Color.Black, CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Save button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { handleSaveClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(
                    text = "Save to gallery",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun saveImage(context: Context, bitmap: Bitmap) {
    val result = ImageSaver.saveToGallery(
        context = context,
        bitmap = bitmap
    )
    when (result) {
        is ImageSaver.SaveResult.Success -> {
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }

        is ImageSaver.SaveResult.Error -> {
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
        }
    }
}
