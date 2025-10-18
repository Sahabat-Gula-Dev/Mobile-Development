package com.pkm.sahabatgula.core.utils

import com.pkm.sahabatgula.data.remote.model.HistoryItem

fun List<HistoryItem>.filterForFood() =
    filter { it.foods?.isNotEmpty() == true }

fun List<HistoryItem>.filterForActivity() =
    filter { it.activities?.isNotEmpty() == true }
