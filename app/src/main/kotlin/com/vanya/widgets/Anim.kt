package com.vanya.widgets

internal fun animLayout(month: Int): Int = when (month) {
    12, 1, 2 -> R.layout.anim_snow
    3, 7 -> R.layout.anim_birds
    4, 9 -> R.layout.anim_rain
    5 -> R.layout.anim_petals
    6, 8 -> R.layout.anim_fireflies
    else -> R.layout.anim_leaves
}
