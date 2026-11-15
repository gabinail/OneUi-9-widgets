package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
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

val WaterKey = intPreferencesKey("water")
const val WATER_GOAL = 8
const val GLASS_ML = 250

class WaterWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val glasses = currentState<Preferences>()[WaterKey] ?: 0
            val anim = if (glasses >= WATER_GOAL) R.layout.anim_confetti else R.layout.anim_water
            AnimBox(R.drawable.bg_teal, anim) { Content() }
        }
    }

    @Composable
    private fun Content() {
        val count = currentState<Preferences>()[WaterKey] ?: 0
        val size = LocalSize.current
        val title = if (count >= WATER_GOAL) "Норма выполнена" else "Вода"
        val dots = "●".repeat(count) + "○".repeat(WATER_GOAL - count)
        val base = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)

        when (formOf(size)) {
            Form.Small -> Column(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WaterText(title, 14.sp, SoftText)
                WaterNumber(count, 40.sp)
                WaterButtons(40.dp, 14.dp)
            }

            Form.Wide -> Row(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.Start
                ) {
                    WaterText(title, 16.sp, SoftText)
                    WaterNumber(count, 48.sp)
                    WaterText(dots, 16.sp, Color.White)
                }
                WaterButtons(48.dp, 12.dp)
            }

            Form.Tall -> Column(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WaterText(title, 16.sp, SoftText)
                WaterNumber(count, 56.sp)
                WaterText(dots, 20.sp, Color.White)
                WaterText("${count * GLASS_ML} мл", 16.sp, SoftText)
                Spacer(GlanceModifier.height(12.dp))
                WaterButtons(if (size.width >= 260.dp) 56.dp else 44.dp, 14.dp)
            }
        }
    }
}

@Composable
private fun WaterText(text: String, fontSize: TextUnit, color: Color) {
    Text(
        text = text,
        style = TextStyle(color = ColorProvider(color), fontSize = fontSize)
    )
}

@Composable
private fun WaterNumber(count: Int, fontSize: TextUnit) {
    Text(
        text = "$count / $WATER_GOAL",
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun WaterButtons(buttonSize: Dp, gap: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RoundButton("−", Color.White, actionRunCallback<WaterMinusAction>(), buttonSize)
        Spacer(GlanceModifier.width(gap))
        RoundButton("+", Color.White, actionRunCallback<WaterPlusAction>(), buttonSize)
    }
}

class WaterPlusAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = WaterWidget().changeInt(context, glanceId, WaterKey) {
        (it + 1).coerceAtMost(WATER_GOAL)
    }
}

class WaterMinusAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = WaterWidget().changeInt(context, glanceId, WaterKey) {
        (it - 1).coerceAtLeast(0)
    }
}

class WaterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterWidget()
}
