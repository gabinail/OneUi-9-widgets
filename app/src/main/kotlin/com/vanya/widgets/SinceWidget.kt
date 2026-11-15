package com.vanya.widgets

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun configIntent(context: Context, cls: Class<*>, widgetId: Int): Intent =
    Intent(context, cls).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        data = Uri.parse("vanya://widget/$widgetId")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

private fun plural(n: Long, one: String, few: String, many: String): String {
    val a = (abs(n) % 100).toInt()
    val b = a % 10
    return when {
        a in 11..19 -> many
        b == 1 -> one
        b in 2..4 -> few
        else -> many
    }
}

class SinceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val epoch = context.getSharedPreferences("since", Context.MODE_PRIVATE)
            .getLong("w$widgetId", Long.MIN_VALUE)
        val start = if (epoch == Long.MIN_VALUE) null else LocalDate.ofEpochDay(epoch)
        provideContent {
            AnimBox(R.drawable.bg_rose, R.layout.anim_petals) {
                SinceContent(context, widgetId, start)
            }
        }
    }
}

@Composable
private fun SinceContent(context: Context, widgetId: Int, start: LocalDate?) {
    val form = formOf(LocalSize.current)
    val open = actionStartActivity(configIntent(context, SinceConfigActivity::class.java, widgetId))
    val today = LocalDate.now()
    val future = start != null && start.isAfter(today)

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(14.dp).clickable(open),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (start == null) {
            Text(
                text = "Нажмите и выберите дату события",
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 15.sp)
            )
        } else {
            val from = if (future) today else start
            val to = if (future) start else today
            val days = ChronoUnit.DAYS.between(from, to)
            val p = Period.between(from, to)
            val parts = listOfNotNull(
                if (p.years > 0) "${p.years} г." else null,
                if (p.months > 0) "${p.months} мес." else null,
                if (p.days > 0) "${p.days} дн." else null
            )
            Text(
                text = if (future) "До события" else "Прошло",
                style = TextStyle(color = ColorProvider(SoftText), fontSize = 14.sp)
            )
            Text(
                text = days.toString(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = when (form) {
                        Form.Small -> 52.sp
                        Form.Wide -> 60.sp
                        Form.Tall -> 80.sp
                    },
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = plural(days, "день", "дня", "дней"),
                style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp)
            )
            if (form != Form.Small) {
                Text(
                    text = if (parts.isEmpty()) "сегодня" else parts.joinToString(" "),
                    style = TextStyle(color = ColorProvider(SoftText), fontSize = 14.sp)
                )
            }
        }
    }
}

class SinceConfigActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val prefs = getSharedPreferences("since", MODE_PRIVATE)
        val saved = prefs.getLong("w$widgetId", Long.MIN_VALUE)
        val init = if (saved == Long.MIN_VALUE) LocalDate.now() else LocalDate.ofEpochDay(saved)
        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->
                prefs.edit().putLong("w$widgetId", LocalDate.of(y, m + 1, d).toEpochDay()).apply()
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                CoroutineScope(Dispatchers.Main).launch {
                    val gid = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
                    SinceWidget().update(applicationContext, gid)
                    finish()
                }
            },
            init.year, init.monthValue - 1, init.dayOfMonth
        )
        dialog.setOnCancelListener { finish() }
        dialog.show()
    }
}

class SinceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SinceWidget()
}
