package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Rate(val code: String, val value: Double, val prev: Double)

private val RATE_CODES = listOf("USD", "EUR", "CNY", "GBP", "TRY")

private fun parseRates(text: String?): List<Rate> {
    if (text == null) return emptyList()
    return try {
        val all = JSONObject(text).getJSONObject("Valute")
        RATE_CODES.mapNotNull { code ->
            val o = all.optJSONObject(code) ?: return@mapNotNull null
            val nominal = o.optDouble("Nominal", 1.0)
            Rate(code, o.getDouble("Value") / nominal, o.getDouble("Previous") / nominal)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun loadRates(context: Context): List<Rate> = withContext(Dispatchers.IO) {
    val prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)
    try {
        val conn = URL("https://www.cbr-xml-daily.ru/daily_json.js").openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        if (parseRates(text).isNotEmpty()) prefs.edit().putString("json", text).apply()
    } catch (e: Exception) {
    }
    parseRates(prefs.getString("json", null))
}

private fun fmt(v: Double): String = String.format(Locale.forLanguageTag("ru"), "%.2f", v)

private fun symbolOf(code: String): String = when (code) {
    "USD" -> "$"
    "EUR" -> "€"
    "CNY" -> "¥"
    "GBP" -> "£"
    "TRY" -> "₺"
    else -> code
}

class CurrencyWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rates = loadRates(context)
        provideContent {
            AnimBox(R.drawable.bg_gold, R.layout.anim_coins) { CurrencyContent(rates) }
        }
    }
}

@Composable
private fun CurrencyContent(rates: List<Rate>) {
    val size = LocalSize.current
    val count = ((size.height.value - 50) / 28).toInt().coerceIn(1, 5)
    val showDiff = size.width.value >= 170
    val fontSize = if (size.width.value >= 170) 18 else 15

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(14.dp)
            .clickable(actionRunCallback<RefreshCurrencyAction>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Курс ЦБ РФ",
            style = TextStyle(color = ColorProvider(SoftText), fontSize = 13.sp)
        )
        if (rates.isEmpty()) {
            Text(
                text = "Нет данных. Нажмите, чтобы обновить",
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp)
            )
        } else {
            rates.take(count).forEach { RateRow(it, showDiff, fontSize) }
        }
    }
}

@Composable
private fun RateRow(r: Rate, showDiff: Boolean, fontSize: Int) {
    val diff = r.value - r.prev
    val up = diff >= 0
    val arrowText = (if (up) "▲" else "▼") + (if (showDiff) " " + fmt(abs(diff)) else "")
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbolOf(r.code),
            modifier = GlanceModifier.width(26.dp),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = (fontSize + 1).sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            text = fmt(r.value),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = arrowText,
            style = TextStyle(
                color = ColorProvider(if (up) AccentGreen else AccentRed),
                fontSize = 12.sp
            )
        )
    }
}

class RefreshCurrencyAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CurrencyWidget().update(context, glanceId)
    }
}

class CurrencyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrencyWidget()
}
