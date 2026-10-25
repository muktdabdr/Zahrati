package com.zahrati.drawing

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val BackgroundColor = Color(0xFF101116)
private val CardColor = Color(0xFF1D2029)
private val AccentColor = Color(0xFFB99AFF)
private val MainTextColor = Color(0xFFF5F1FF)
private val SecondaryTextColor = Color(0xFFAAA6B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AccentColor,
                    background = BackgroundColor,
                    surface = CardColor
                )
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    ZahratiApp()
                }
            }
        }
    }
}

@Composable
fun ZahratiApp() {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var currentScreen by remember { mutableStateOf("home") }
    var imageOpacity by remember { mutableFloatStateOf(0.55f) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            currentScreen = "trace"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        when (currentScreen) {
            "trace" -> {
                TraceScreen(
                    image = selectedImage,
                    opacity = imageOpacity,
                    onOpacityChange = { imageOpacity = it },
                    onChooseImage = {
                        imagePicker.launch("image/*")
                    },
                    onBack = { currentScreen = "home" }
                )
            }

            "library" -> {
                LibraryScreen(
                    onBack = { currentScreen = "home" },
                    onChooseImage = {
                        imagePicker.launch("image/*")
                    }
                )
            }

            else -> {
                HomeScreen(
                    onChooseImage = {
                        imagePicker.launch("image/*")
                    },
                    onOpenLibrary = {
                        currentScreen = "library"
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onChooseImage: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "رسمي",
            color = AccentColor,
            fontSize = 17.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "زهرتي",
            color = MainTextColor,
            fontSize = 36.sp
        )

        Text(
            text = "تعلّم الرسم خطوة بخطوة",
            color = SecondaryTextColor,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    CardColor,
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✦", fontSize = 42.sp, color = AccentColor)
                Spacer(Modifier.height(8.dp))
                Text(
                    "حوّل فكرتك إلى رسمة",
                    color = MainTextColor,
                    fontSize = 21.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "اختر صورة وابدأ التدريب",
                    color = SecondaryTextColor,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "ابدأ الآن",
            color = MainTextColor,
            fontSize = 22.sp
        )

        Spacer(Modifier.height(12.dp))

        FeatureCard(
            title = "ارسم من صورة",
            description = "اختر صورة من هاتفك وتدرّب على تتبّع خطوطها",
            symbol = "✎",
            onClick = onChooseImage
        )

        Spacer(Modifier.height(12.dp))

        FeatureCard(
            title = "مكتبة رسوماتي",
            description = "افتح صورة جديدة من جهازك للتدريب",
            symbol = "▧",
            onClick = onOpenLibrary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "نصيحة: خفّض شفافية الصورة لتتمكن من رؤية خطوطك على الورق.",
            color = SecondaryTextColor,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    symbol: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = MainTextColor,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    description,
                    color = SecondaryTextColor,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                symbol,
                color = AccentColor,
                fontSize = 30.sp
            )
        }
    }
}

@Composable
fun TraceScreen(
    image: Uri?,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    onChooseImage: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("رجوع", color = AccentColor)
            }

            Spacer(Modifier.width(12.dp))

            Text(
                "وضع التدريب",
                color = MainTextColor,
                fontSize = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Color(0xFF292B33),
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    factory = { context ->
                        ImageView(context).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            adjustViewBounds = true
                        }
                    },
                    update = { view ->
                        if (view.tag != image.toString()) {
                            view.setImageURI(image)
                            view.tag = image.toString()
                        }
                        view.alpha = opacity
                    }
                )
            } else {
                Text(
                    "اختر صورة للبدء",
                    color = SecondaryTextColor
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "شفافية الصورة: ${(opacity * 100).toInt()}%",
            color = MainTextColor,
            fontSize = 16.sp
        )

        Slider(
            value = opacity,
            onValueChange = onOpacityChange,
            valueRange = 0.15f..1f,
            colors = SliderDefaults.colors(
                thumbColor = AccentColor,
                activeTrackColor = AccentColor
            )
        )

        Button(
            onClick = onChooseImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color(0xFF17121F)
            )
        ) {
            Text("اختيار صورة", fontSize = 17.sp)
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onChooseImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("رجوع", color = AccentColor)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "مكتبة الرسومات",
            color = MainTextColor,
            fontSize = 27.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "اختر رسمة من صور هاتفك لتبدأ التدريب عليها.",
            color = SecondaryTextColor,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onChooseImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color(0xFF17121F)
            )
        ) {
            Text("اختيار صورة من الهاتف")
        }
    }
}
