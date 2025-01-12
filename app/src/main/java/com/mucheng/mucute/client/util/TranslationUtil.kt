package com.mucheng.mucute.client.util

import com.mucheng.mucute.client.R
import com.mucheng.mucute.client.application.AppContext
import com.mucheng.mucute.client.game.TranslationManager

inline val String.translatedSelf: String
    get() = TranslationManager.getTranslationMap(AppContext.instance.getString(R.string.current_language))[this]
        ?: this