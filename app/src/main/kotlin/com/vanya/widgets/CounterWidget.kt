package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Button
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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

val CountKey = intPreferencesKey("count")

class CounterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val count = currentState<Preferences>()[CountKey] ?: 0

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Счётчик",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            )
            Text(
                text = count.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(text = "−", onClick = actionRunCallback<DecrementAction>())
                Spacer(GlanceModifier.width(8.dp))
                Button(text = "+", onClick = actionRunCallback<IncrementAction>())
                Spacer(GlanceModifier.width(8.dp))
                Button(text = "0", onClick = actionRunCallback<ResetAction>())
            }
        }
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
