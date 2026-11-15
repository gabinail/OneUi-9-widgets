package com.vanya.widgets

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val text = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
            .getString("w$widgetId", "") ?: ""
        provideContent {
            AnimBox(R.drawable.bg_slate, R.layout.anim_drift) {
                NoteContent(context, widgetId, text)
            }
        }
    }
}

@Composable
private fun NoteContent(context: Context, widgetId: Int, text: String) {
    val size = LocalSize.current
    val fontSize = if (size.width.value >= 260) 18 else 16
    val lines = ((size.height.value - 32) / (fontSize * 1.4f)).toInt().coerceAtLeast(1)
    val open = actionStartActivity(configIntent(context, NoteActivity::class.java, widgetId))

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp).clickable(open),
        verticalAlignment = Alignment.Top
    ) {
        if (text.isBlank()) {
            Text(
                text = "Нажмите, чтобы написать заметку",
                style = TextStyle(color = ColorProvider(SoftText), fontSize = 15.sp)
            )
        } else {
            Text(
                text = text,
                maxLines = lines,
                style = TextStyle(color = ColorProvider(Color.White), fontSize = fontSize.sp)
            )
        }
    }
}

class NoteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val prefs = getSharedPreferences("notes", MODE_PRIVATE)
        val input = EditText(this).apply {
            setText(prefs.getString("w$widgetId", ""))
            hint = "Текст заметки"
            minLines = 4
            gravity = Gravity.TOP or Gravity.START
        }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val box = FrameLayout(this).apply {
            setPadding(pad, pad / 2, pad, 0)
            addView(input)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Заметка")
            .setView(box)
            .setPositiveButton("Сохранить") { _, _ ->
                prefs.edit().putString("w$widgetId", input.text.toString()).apply()
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                CoroutineScope(Dispatchers.Main).launch {
                    val gid = GlanceAppWidgetManager(applicationContext).getGlanceIdBy(widgetId)
                    NoteWidget().update(applicationContext, gid)
                    finish()
                }
            }
            .setNegativeButton("Отмена") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }
}

class NoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NoteWidget()
}
