package com.widoo.pitlane.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import com.widoo.pitlane.MainActivity
import com.widoo.pitlane.R
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

class SmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val vehicleRepo = VehicleRepository(db.vehicleDao())
        val serviceRepo = ServiceRepository(db.serviceRecordDao())

        val vehicle = vehicleRepo.getActive().first()
        val latestService = serviceRepo.getAll().first().firstOrNull()

        val currentKm = vehicle?.currentKm ?: 0
        val nextServiceKm = latestService?.nextServiceKm ?: 0
        val kmToNext = if (nextServiceKm > currentKm) nextServiceKm - currentKm else 0
        val vehicleName = vehicle?.let { "${it.brand} ${it.model}" } ?: "Sin vehículo"

        provideContent {
            SmallWidgetContent(
                context = context,
                vehicleName = vehicleName,
                currentKm = currentKm,
                kmToNext = kmToNext,
                nextServiceKm = nextServiceKm
            )
        }
    }
}

@OptIn(ExperimentalGlanceApi::class)
@Composable
private fun SmallWidgetContent(
    context: Context,
    vehicleName: String,
    currentKm: Int,
    kmToNext: Int,
    nextServiceKm: Int
) {
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val isWarning = kmToNext in 1..500
    val accentColor = if (isWarning) Color(0xFFFF7043) else Color(0xFF00E3FD)

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0xFF1D201F))
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // App name
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = GlanceModifier.size(16.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "Pitlane",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF00E3FD)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Vehículo
            Text(
                text = vehicleName,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFE2E3E0)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Km actual
            Text(
                text = "${numFormat.format(currentKm)} km",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF00E3FD)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Próximo service
            if (nextServiceKm > 0) {
                Text(
                    text = if (kmToNext > 0)
                        "Service en ${numFormat.format(kmToNext)} km"
                    else
                        "¡Service vencido!",
                    style = TextStyle(
                        color = ColorProvider(accentColor),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Botón cargar combustible
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(Color(0xFF00E3FD))
                    .cornerRadius(8.dp)
                    .padding(vertical = 6.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⛽ Cargar",
                    style = TextStyle(
                        color = ColorProvider(Color.Black),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class SmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}