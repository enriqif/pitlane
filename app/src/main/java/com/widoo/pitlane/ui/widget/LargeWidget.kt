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
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import com.widoo.pitlane.MainActivity
import com.widoo.pitlane.R
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import com.widoo.pitlane.ui.theme.WarningOrange
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val vehicleRepo = VehicleRepository(db.vehicleDao())
        val serviceRepo = ServiceRepository(db.serviceRecordDao())
        val fuelRepo = FuelRepository(db.fuelLogDao())


        val vehicles = vehicleRepo.getAll().first()      // ← nuevo
        val vehicle = vehicles.firstOrNull { it.isActive }

        android.util.Log.d("LargeWidget", ">>> provideGlance - active vehicle: ${vehicle?.brand} ${vehicle?.model}")
        val vehicleCount = vehicles.size                 // ← nuevo

        val services = serviceRepo.getAll().first()
        val fuelLogs = fuelRepo.getAll().first()

        val latestService = services.firstOrNull()
        val latestFuel = fuelLogs.firstOrNull()
        val currentKm = vehicle?.currentKm ?: 0
        val nextServiceKm = latestService?.nextServiceKm ?: 0
        val kmToNext = if (nextServiceKm > currentKm) nextServiceKm - currentKm else 0

        // Gasto este mes
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        val startOfMonth = cal.timeInMillis
        val monthlyFuelCost = fuelLogs
            .filter { it.date >= startOfMonth }
            .sumOf { it.totalCost }

        // Consumo promedio — solo con las cargas que tienen km y litros cargados
        val fuelLogsWithData = fuelLogs.filter { it.km != null && it.liters != null }
        val avgConsumption = if (fuelLogsWithData.size >= 2) {
            val sorted = fuelLogsWithData.sortedBy { it.km }
            val kmDiff = (sorted.last().km!! - sorted.first().km!!).toDouble()
            val totalLiters = sorted.dropLast(1).sumOf { it.liters!! }
            if (kmDiff > 0) (totalLiters / kmDiff) * 100 else 0.0
        } else 0.0

        provideContent {
            LargeWidgetContent(
                context = context,
                vehicleName = vehicle?.let { "${it.brand} ${it.model}" } ?: "Sin vehículo",
                plate = vehicle?.plate ?: "",
                currentKm = currentKm,
                kmToNext = kmToNext,
                nextServiceKm = nextServiceKm,
                lastServiceType = latestService?.serviceType ?: "",
                lastServiceDate = latestService?.date ?: 0,
                monthlyFuelCost = monthlyFuelCost,
                avgConsumption = avgConsumption,
                totalServices = services.size,
                vehicleCount = vehicleCount
            )
        }
    }
}

@Composable
private fun LargeWidgetContent(
    context: Context,
    vehicleName: String,
    plate: String,
    currentKm: Int,
    kmToNext: Int,
    nextServiceKm: Int,
    lastServiceType: String,
    lastServiceDate: Long,
    monthlyFuelCost: Double,
    avgConsumption: Double,
    totalServices: Int,
    vehicleCount: Int
) {
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val isWarning = kmToNext in 1..500
    val cyanColor = Color(0xFF00E3FD)
    val bgColor = Color(0xFF1D201F)
    val surfaceColor = Color(0xFF282B29)
    val textPrimary = Color(0xFFE2E3E0)
    val textSecondary = Color(0xFF8A938E)

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(bgColor)
            .cornerRadius(16.dp)
            .padding(14.dp)
    ) {
        // ── Fila 1: Header con logo y patente ──
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "Pitlane",
                style = TextStyle(
                    color = ColorProvider(cyanColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = plate,
                style = TextStyle(
                    color = ColorProvider(textSecondary),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // ── Fila 2: Vehículo + km + botón cambiar ──
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(surfaceColor)
                .cornerRadius(12.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = vehicleName,
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "${numFormat.format(currentKm)} km",
                    style = TextStyle(
                        color = ColorProvider(cyanColor),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (nextServiceKm > 0) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = if (kmToNext > 0)
                            "Service en ${numFormat.format(kmToNext)} km"
                        else "¡Service vencido!",
                        style = TextStyle(
                            color = ColorProvider(
                                if (isWarning) WarningOrange else textSecondary
                            ),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Botón cambiar vehículo — grande y visible
            if (vehicleCount > 1) {
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column(
                    modifier = GlanceModifier
                        .background(Color(0xFF1D201F))
                        .cornerRadius(10.dp)
                        .padding(12.dp)
                        .clickable(actionRunCallback<SwitchVehicleAction>()),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "⇄",
                        style = TextStyle(
                            color = ColorProvider(cyanColor),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${vehicleCount} autos",
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── Fila 3: Stats ──
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            // Último service
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(surfaceColor)
                    .cornerRadius(10.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "ÚLTIMO SERVICE",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 8.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = if (lastServiceDate > 0)
                        dateFormat.format(Date(lastServiceDate))
                    else "Sin datos",
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (lastServiceType.isNotBlank()) {
                    Text(
                        text = lastServiceType,
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 9.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Nafta este mes
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(surfaceColor)
                    .cornerRadius(10.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "NAFTA MES",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 8.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "$${numFormat.format(monthlyFuelCost)}",
                    style = TextStyle(
                        color = ColorProvider(cyanColor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (avgConsumption > 0) {
                    Text(
                        text = "${String.format("%.1f", avgConsumption)} L/100",
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Servicios
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(surfaceColor)
                    .cornerRadius(10.dp)
                    .padding(10.dp)
            ) {
                Text(
                    text = "SERVICIOS",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 8.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "$totalServices",
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "registrados",
                    style = TextStyle(
                        color = ColorProvider(textSecondary),
                        fontSize = 9.sp
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── Fila 4: Botones de acción ──
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(cyanColor)
                    .cornerRadius(10.dp)
                    .padding(vertical = 12.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                action = "ACTION_OPEN_FUEL"
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⛽  Cargar nafta",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF000000)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .background(surfaceColor)
                    .cornerRadius(10.dp)
                    .padding(vertical = 12.dp)
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP or  // ← limpia el stack
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP    // ← reutiliza instancia existente
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📱  Abrir app",
                    style = TextStyle(
                        color = ColorProvider(cyanColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
    }
}

//class LargeWidgetReceiver : GlanceAppWidgetReceiver() {
//    override val glanceAppWidget: GlanceAppWidget = LargeWidget()
//}