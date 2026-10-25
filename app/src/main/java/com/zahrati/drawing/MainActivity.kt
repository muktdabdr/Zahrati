
package com.zahrati.drawing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF101116)
private val Panel = Color(0xFF1D2029)
private val Purple = Color(0xFFB99AFF)
private val WhiteText = Color(0xFFF5F1FF)
private val Muted = Color(0xFFAAA6B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Purple,
                    background = Bg,
                    surface = Panel
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
    var stage by remember { mutableIntStateOf(1) }
    var showTrace by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            stage = 1
            showTrace = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Bg
    ) {
        if (showTrace) {
            DrawingScreen(
                uri = selectedImage,
                stage = stage,
                onStageChange = { stage = it },
                onChooseImage = { picker.launch("image/*") },
                onBack = { showTrace = false }
            )
        } else {
            HomeScreen(
                onChooseImage = { picker.launch("image/*") }
            )
        }
    }
}

@Composable
fun HomeScreen(onChooseImage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(28.dp))

        Text(
            "زهرتي",
            color = Purple,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "مختبر الرسم والتعلّم",
            color = WhiteText,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "حوّل صورتك إلى خطوط تساعدك على تعلّم الرسم.",
            color = Muted,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Panel, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✎", color = Purple, fontSize = 56.sp)
                Text(
                    "ابدأ من صورتك",
                    color = WhiteText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "استخرج الخطوط وبدّل مستوى التفاصيل",
                    color = Muted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onChooseImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                contentColor = Color(0xFF17121F)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("اختيار صورة والبدء بالرسم", fontSize = 17.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text("طريقة الاستخدام", color = WhiteText, fontSize = 20.sp)

        Spacer(Modifier.height(10.dp))

        InfoCard("١", "اختر صورة واضحة من هاتفك.")
        InfoCard("٢", "شاهد الخطوط التي يستخرجها التطبيق.")
        InfoCard("٣", "بدّل مراحل التفاصيل واختر النتيجة المناسبة للتدريب.")

        Spacer(Modifier.height(16.dp))

        Text(
            "ملاحظة: المعالجة الحالية تستخرج الحواف، ولا تفهم عناصر الصورة أو تقسّمها تلقائيًا إلى خطوات رسم احترافية.",
            color = Muted,
            fontSize = 13.sp
        )
    }
}

@Composable
fun InfoCard(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(Panel, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, color = Purple, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, color = WhiteText, fontSize = 14.sp)
    }
}

@Composable
fun DrawingScreen(
    uri: Uri?,
    stage: Int,
    onStageChange: (Int) -> Unit,
    onChooseImage: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var processedBitmap by remember(uri, stage) {
        mutableStateOf<Bitmap?>(null)
    }
    var loading by remember(uri, stage) {
        mutableStateOf(false)
    }
    var error by remember(uri, stage) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(uri, stage) {
        if (uri == null) {
            processedBitmap = null
            return@LaunchedEffect
        }

        loading = true
        error = null

        try {
            val result = withContext(Dispatchers.IO) {
                val source = context.contentResolver
                    .openInputStream(uri)
                    ?.use { BitmapFactory.decodeStream(it) }
                    ?: throw IllegalStateException("تعذّر فتح الصورة")

                DrawingProcessor.makeLineArt(source, stage)
            }

            processedBitmap = result
        } catch (e: Exception) {
            processedBitmap = null
            error = "تعذّرت معالجة الصورة. جرّب صورة أخرى."
        } finally {
            loading = false
        }
    }

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
                Text("رجوع", color = Purple)
            }

            Spacer(Modifier.width(8.dp))

            Text(
                "تحويل الصورة إلى خطوط",
                color = WhiteText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator(color = Purple)

                error != null -> Text(
                    error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(20.dp)
                )

                processedBitmap != null -> Image(
                    bitmap = processedBitmap!!.asImageBitmap(),
                    contentDescription = "الصورة بعد استخراج الخطوط",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )

                else -> Text("اختر صورة للبدء", color = Color.DarkGray)
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "مرحلة التفاصيل: $stage من 4",
            color = WhiteText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            when (stage) {
                1 -> "المرحلة الأولى: خطوط الحواف الأوضح"
                2 -> "المرحلة الثانية: تفاصيل إضافية"
                3 -> "المرحلة الثالثة: تفاصيل أكثر"
                else -> "المرحلة الرابعة: استخراج حواف أكثر"
            },
            color = Muted,
            fontSize = 13.sp
        )

        Slider(
            value = stage.toFloat(),
            onValueChange = { onStageChange(it.toInt().coerceIn(1, 4)) },
            valueRange = 1f..4f,
            steps = 2,
            colors = SliderDefaults.colors(
                thumbColor = Purple,
                activeTrackColor = Purple
            )
        )

        Button(
            onClick = onChooseImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                contentColor = Color(0xFF17121F)
            )
        ) {
            Text("اختيار صورة أخرى", fontSize = 16.sp)
        }
    }
}
