package com.example.minimallauncher.launcher.drawer

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.minimallauncher.core.model.AppInfo
import java.util.Locale

data class AppCategory(val label: String)

private data class CategoryRule(
    val label: String,
    val terms: List<String>
)

private val fallbackCategoryRules = listOf(
    CategoryRule(
        label = "Соцсети",
        terms = listOf(
            "telegram", "whatsapp", "vkontakte", "vk.com", "instagram", "facebook",
            "messenger", "snapchat", "discord", "reddit", "tiktok", "соцсеть", "однокласс"
        )
    ),
    CategoryRule(
        label = "Путешествия",
        terms = listOf(
            "maps", "map.", "карты", "2gis", "2гис", "uber", "taxi", "такси", "travel",
            "trip", "booking", "airbnb", "авиа", "поезд", "навигац"
        )
    ),
    CategoryRule(
        label = "Развлечения",
        terms = listOf(
            "youtube", "spotify", "netflix", "kinopoisk", "кино", "музык", "видео",
            "игр", "game", "music", "media", "twitch", "развлеч"
        )
    ),
    CategoryRule(
        label = "Финансы",
        terms = listOf(
            "bank", "finance", "wallet", "pay", "сбер", "банк", "финанс", "т-банк",
            "тинькофф", "альфа", "втб", "мир"
        )
    ),
    CategoryRule(
        label = "Покупки",
        terms = listOf(
            "ozon", "wildberries", "market", "shopping", "shop", "store", "amazon",
            "aliexpress", "покуп", "магазин", "маркет", "wb"
        )
    ),
    CategoryRule(
        label = "Работа",
        terms = listOf(
            "gmail", "outlook", "mail", "почта", "office", "docs", "drive", "диск",
            "calendar", "календар", "zoom", "teams", "slack", "notion", "рабоч", "business"
        )
    )
)

fun categoryFor(context: Context, app: AppInfo): AppCategory {
    if (app.category != ApplicationInfo.CATEGORY_UNDEFINED) {
        ApplicationInfo.getCategoryTitle(context, app.category)
            ?.toString()
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { return AppCategory(it) }
    }

    val appIdentity = "${app.name} ${app.packageName}".lowercase(Locale.ROOT)
    val inferredLabel = fallbackCategoryRules.firstOrNull { rule ->
        rule.terms.any(appIdentity::contains)
    }?.label

    return AppCategory(inferredLabel ?: "Без категории")
}
