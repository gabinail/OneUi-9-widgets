package com.vanya.widgets

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

class CalendarWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val ru = Locale.forLanguageTag("ru")
        val offset = today.withDayOfMonth(1).dayOfWeek.value - 1
        val daysInMonth = today.lengthOfMonth()
        val weeks = (offset + daysInMonth + 6) / 7
        val dayNames = listOf("П", "В", "С", "Ч", "П", "С", "В")

        provideContent {
            val size = LocalSize.current
            val big = size.width >= 260.dp || size.height >= 260.dp
            val pad = 12.dp
            val titleH = if (big) 32.dp else 22.dp
            val cellW = (size.width - pad * 2) / 7
            val rowH = minOf((size.height - pad * 2 - titleH) / (weeks + 1), 36.dp)
            val fontSize = if (cellW >= 36.dp) 16.sp else 12.sp
            val title = if (big) {
                today.month.getDisplayName(DateTextStyle.FULL_STANDALONE, ru)
                    .uppercase(ru) + " " + today.year
            } else {
                today.month.getDisplayName(DateTextStyle.SHORT_STANDALONE, ru).uppercase(ru)
            }

            Box(modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.bg_slate))) {
            AndroidRemoteViews(
                remoteViews = RemoteViews(context.packageName, animLayout(today.monthValue)),
                modifier = GlanceModifier.fillMaxSize()
            )
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = if (big) 20.sp else 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row {
                    dayNames.forEachIndexed { i, name ->
                        CalendarCell(
                            text = name,
                            width = cellW,
                            height = rowH,
                            fontSize = fontSize,
                            color = if (i == 6) AccentRed else SoftText,
                            highlight = false
                        )
                    }
                }
                for (w in 0 until weeks) {
                    Row {
                        for (d in 0 until 7) {
                            val dayNum = w * 7 + d - offset + 1
                            if (dayNum in 1..daysInMonth) {
                                CalendarCell(
                                    text = dayNum.toString(),
                                    width = cellW,
                                    height = rowH,
                                    fontSize = fontSize,
                                    color = if (d == 6) AccentRed else Color.White,
                                    highlight = dayNum == today.dayOfMonth
                                )
                            } else {
                                Spacer(GlanceModifier.size(cellW, rowH))
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    text: String,
    width: Dp,
    height: Dp,
    fontSize: TextUnit,
    color: Color,
    highlight: Boolean
) {
    val inner = if (highlight) {
        GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFE5575C)))
            .cornerRadius(6.dp)
    } else {
        GlanceModifier.fillMaxSize()
    }
    Box(modifier = GlanceModifier.size(width, height).padding(1.dp)) {
        Box(modifier = inner, contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    color = ColorProvider(if (highlight) Color(0xFF1B1B1F) else color),
                    fontSize = fontSize,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}
