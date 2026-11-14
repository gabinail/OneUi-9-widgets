package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val CountKey = intPreferencesKey("count")

private val PanelColor = Color(0xB3363E52)
private val SoftText = Color(0xCCFFFFFF)
private val ButtonBg = Color(0x33FFFFFF)
private val AccentRed = Color(0xFFFF7A70)

class CounterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val count = currentState<Preferences>()[CountKey] ?: 0

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(PanelColor))
                .cornerRadius(28.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Счётчик",
                style = TextStyle(
                    color = ColorProvider(SoftText),
                    fontSize = 14.sp
                )
            )
            Text(
                text = count.toString(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundButton("−", Color.White, actionRunCallback<DecrementAction>())
                Spacer(GlanceModifier.width(10.dp))
                RoundButton("+", Color.White, actionRunCallback<IncrementAction>())
                Spacer(GlanceModifier.width(10.dp))
                RoundButton("0", AccentRed, actionRunCallback<ResetAction>())
            }
        }
    }
}

@Composable
private fun RoundButton(label: String, textColor: Color, action: Action) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .background(ColorProvider(ButtonBg))
            .cornerRadius(20.dp)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private suspend fun changeCount(context: Context, id: GlanceId, change: (Int) -> Int) {
    updateAppWidgetState(context, id) { prefs ->
        prefs[CountKey] = change(prefs[CountKey] ?: 0)
    }
    CounterWidget().update(context, id)
}

class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = changeCount(context, glanceId) { it + 1 }
}

class DecrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = changeCount(context, glanceId) { it - 1 }
}

class ResetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) = changeCount(context, glanceId) { 0 }
}

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterWidget()
}
