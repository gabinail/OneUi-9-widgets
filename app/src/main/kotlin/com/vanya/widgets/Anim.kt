package com.vanya.widgets

import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize

internal fun animLayout(month: Int): Int = when (month) {
    12, 1, 2 -> R.layout.anim_snow
    3, 7 -> R.layout.anim_birds
    4, 9 -> R.layout.anim_rain
    5 -> R.layout.anim_petals
    6, 8 -> R.layout.anim_fireflies
    else -> R.layout.anim_leaves
}

@Composable
internal fun AnimBox(bg: Int, layout: Int, content: @Composable () -> Unit) {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize().background(ImageProvider(bg))) {
        AndroidRemoteViews(
            remoteViews = RemoteViews(context.packageName, layout),
            modifier = GlanceModifier.fillMaxSize()
        )
        content()
    }
}
