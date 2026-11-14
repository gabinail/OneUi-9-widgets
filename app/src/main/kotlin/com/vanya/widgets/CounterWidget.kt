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

val CountKey = intPreferencesKey("count")

class CounterWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val count = currentState<Preferences>()[CountKey] ?: 0
        val size = LocalSize.current
        val base = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.bg_blue))
            .padding(12.dp)

        when (formOf(size)) {
            Form.Small -> Column(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CounterLabel(14.sp)
                CounterNumber(count, 48.sp)
                CounterButtons(40.dp, 10.dp)
            }

            Form.Wide -> Row(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.Start
                ) {
                    CounterLabel(16.sp)
                    CounterNumber(count, 64.sp)
                }
                CounterButtons(48.dp, 10.dp)
            }

            Form.Tall -> Column(
                modifier = base,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CounterLabel(16.sp)
                CounterNumber(count, 72.sp)
                Spacer(GlanceModifier.height(12.dp))
                CounterButtons(if (size.width >= 260.dp) 56.dp else 40.dp, 8.dp)
            }
        }
    }
}

@Composable
private fun CounterLabel(fontSize: TextUnit) {
    Text(
        text = "Счётчик",
        style = TextStyle(color = ColorProvider(SoftText), fontSize = fontSize)
    )
}

@Composable
private fun CounterNumber(count: Int, fontSize: TextUnit) {
    Text(
        text = count.toString(),
        style = TextStyle(
            color = ColorProvider(Color.White),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun CounterButtons(buttonSize: Dp, gap: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RoundButton("−", Color.White, actionRunCallback<DecrementAction>(), buttonSize)
        Spacer(GlanceModifier.width(gap))
        RoundButton("+", Color.White, actionRunCallback<IncrementAction>(), buttonSize)
        Spacer(GlanceModifier.width(gap))
        RoundButton("0", AccentRed, actionRunCallback<ResetAction>(), buttonSize)
    }
}

class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = CounterWidget().changeInt(context, glanceId, CountKey) { it + 1 }
}

class DecrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = CounterWidget().changeInt(context, glanceId, CountKey) { it - 1 }
}

class ResetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = CounterWidget().changeInt(context, glanceId, CountKey) { 0 }
}

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterWidget()
}
