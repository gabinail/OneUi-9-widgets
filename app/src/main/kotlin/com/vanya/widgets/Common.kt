package com.vanya.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

internal val SoftText = Color(0xCCFFFFFF)
internal val ButtonBg = Color(0x33FFFFFF)
internal val AccentRed = Color(0xFFFF8A80)
internal val AccentGreen = Color(0xFF5CD16A)
internal val TrackColor = Color(0x33FFFFFF)

internal enum class Form { Small, Wide, Tall }

internal fun formOf(size: DpSize): Form = when {
    size.height >= 260.dp -> Form.Tall
    size.width >= 260.dp -> Form.Wide
    else -> Form.Small
}

@Composable
internal fun RoundButton(
    label: String,
    textColor: Color,
    action: Action,
    buttonSize: Dp = 40.dp
) {
    Box(
        modifier = GlanceModifier
            .size(buttonSize)
            .background(ColorProvider(ButtonBg))
            .cornerRadius(buttonSize / 2)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = (buttonSize.value / 2).sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
internal fun ProgressBar(width: Dp, fraction: Float) {
    Box(
        modifier = GlanceModifier
            .width(width)
            .height(8.dp)
            .background(ColorProvider(TrackColor))
            .cornerRadius(4.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .width(width * fraction.coerceIn(0.03f, 1f))
                .height(8.dp)
                .background(ColorProvider(AccentGreen))
                .cornerRadius(4.dp)
        ) {}
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
