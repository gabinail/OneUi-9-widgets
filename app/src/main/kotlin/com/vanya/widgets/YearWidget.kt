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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

class YearWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val total = today.lengthOfYear()
        val passed = today.dayOfYear
        val left = total - passed
        val pct = passed * 100 / total
        val fraction = passed.toFloat() / total
        val year = today.year.toString()

        provideContent {
            val size = LocalSize.current
            val barW = size.width - 24.dp
            val base = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.bg_forest))
                .padding(12.dp)

            when (formOf(size)) {
                Form.Small -> Column(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = year,
                        style = TextStyle(color = ColorProvider(SoftText), fontSize = 14.sp)
                    )
                    Text(
                        text = "$pct%",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    ProgressBar(barW, fraction)
                }

                Form.Wide -> Column(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$pct%",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(GlanceModifier.width(16.dp))
                        Column {
                            Text(
                                text = "$year год",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "осталось $left дн.",
                                style = TextStyle(
                                    color = ColorProvider(SoftText),
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.height(10.dp))
                    ProgressBar(barW, fraction)
                }

                Form.Tall -> Column(
                    modifier = base,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$year год",
                        style = TextStyle(
                            color = ColorProvider(SoftText),
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "$pct%",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "осталось $left дн.",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "до Нового года",
                        style = TextStyle(
                            color = ColorProvider(SoftText),
                            fontSize = 14.sp
                        )
                    )
                    Spacer(GlanceModifier.height(14.dp))
                    ProgressBar(barW, fraction)
                }
            }
        }
    }
}

class YearWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearWidget()
}
