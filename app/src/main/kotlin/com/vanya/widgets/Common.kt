package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

internal val SoftText = Color(0xCCFFFFFF)
internal val ButtonBg = Color(0x33FFFFFF)
internal val AccentRed = Color(0xFFFF8A80)

@Composable
internal fun RoundButton(label: String, textColor: Color, action: Action) {
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

internal suspend fun GlanceAppWidget.changeInt(
    context: Context,
    id: GlanceId,
    key: Preferences.Key<Int>,
    change: (Int) -> Int
) {
    updateAppWidgetState(context, id) { prefs ->
        prefs[key] = change(prefs[key] ?: 0)
    }
    update(context, id)
}
