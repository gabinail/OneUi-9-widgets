package com.vanya.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.time.temporal.IsoFields
import java.util.Locale

class DateWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val ru = Locale.forLanguageTag("ru")
        val monthYear = today.month.getDisplayName(DateTextStyle.SHORT, ru).uppercase(ru) +
            " " + today.year
        val weekday = today.dayOfWeek.getDisplayName(DateTextStyle.FULL, ru)
        val day = today.dayOfMonth.toString()
        val week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val info = "Неделя $week · день ${today.dayOfYear}"

        provideContent {
            val base = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.bg_purple))
                .padding(12.dp)

            when (formOf(LocalSize.current)) {
                Form.Small -> Column(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthYear,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = weekday,
                        style = TextStyle(
                            color = ColorProvider(AccentRed),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = day,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Form.Wide -> Row(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.width(16.dp))
                    Column {
                        Text(
                            text = weekday,
                            style = TextStyle(
                                color = ColorProvider(AccentRed),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = monthYear,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Form.Tall -> Column(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthYear,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = weekday,
                        style = TextStyle(
                            color = ColorProvider(AccentRed),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = day,
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = info,
                        style = TextStyle(
                            color = ColorProvider(SoftText),
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

class DateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DateWidget()
}
