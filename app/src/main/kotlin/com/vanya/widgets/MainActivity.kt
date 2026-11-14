package com.vanya.widgets

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this).apply {
            text = "Виджеты установлены.\n\n" +
                "Долгое нажатие на домашнем экране → Виджеты → Vanya Widgets → «Счётчик»."
            textSize = 18f
            setPadding(64, 200, 64, 64)
        }
        setContentView(view)
    }
}
