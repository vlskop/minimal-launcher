package com.example.minimallauncher.launcher.drawer

import android.content.pm.ApplicationInfo
import com.example.minimallauncher.core.model.AppInfo
import java.util.Locale

enum class AppCategory(val label: String) {
    SOCIAL("Соцсети"),
    TRAVEL("Путешествия"),
    ENTERTAINMENT("Развлечения"),
    FINANCE("Финансы"),
    SHOPPING("Покупки"),
    WORK("Работа"),
    TOOLS("Инструменты")
}

fun categoryFor(app: AppInfo): AppCategory {
    val searchText = "${app.name} ${app.packageName}".lowercase(Locale.ROOT)

    return when {
        searchText.containsAny(
            "telegram", "whatsapp", "vkontakte", "vk.com", "instagram", "facebook",
            "messenger", "snapchat", "discord", "reddit", "tiktok", "соцсеть", "однокласс"
        ) -> AppCategory.SOCIAL

        searchText.containsAny(
            "maps", "map.", "карты", "2gis", "2гис", "uber", "taxi", "такси", "travel",
            "trip", "booking", "airbnb", "авиа", "поезд", "навигац"
        ) -> AppCategory.TRAVEL

        searchText.containsAny(
            "youtube", "spotify", "netflix", "kinopoisk", "кино", "музык", "видео",
            "игр", "game", "music", "media", "twitch", "развлеч"
        ) -> AppCategory.ENTERTAINMENT

        searchText.containsAny(
            "bank", "finance", "wallet", "pay", "сбер", "банк", "финанс", "т-банк",
            "тинькофф", "альфа", "втб", "мир"
        ) -> AppCategory.FINANCE

        searchText.containsAny(
            "ozon", "wildberries", "market", "shopping", "shop", "store", "amazon",
            "aliexpress", "покуп", "магазин", "маркет", "wb"
        ) -> AppCategory.SHOPPING

        searchText.containsAny(
            "gmail", "outlook", "mail", "почта", "office", "docs", "drive", "диск",
            "calendar", "календар", "zoom", "teams", "slack", "notion", "рабоч", "business"
        ) -> AppCategory.WORK

        app.category == ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
        app.category == ApplicationInfo.CATEGORY_MAPS -> AppCategory.TRAVEL
        app.category in setOf(
            ApplicationInfo.CATEGORY_GAME,
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE
        ) -> AppCategory.ENTERTAINMENT

        app.category == ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.WORK
        else -> AppCategory.TOOLS
    }
}

private fun String.containsAny(vararg values: String): Boolean = values.any(::contains)
