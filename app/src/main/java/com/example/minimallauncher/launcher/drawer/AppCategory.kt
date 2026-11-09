package com.example.minimallauncher.launcher.drawer

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.minimallauncher.core.model.AppInfo
import java.util.Locale

data class AppCategory(val label: String)

private data class CategoryTerm(
    val value: String,
    val weight: Int = 4,
    val words: List<String> = value.lowercase(Locale.ROOT)
        .replace('ё', 'е')
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter(String::isNotBlank)
)

private data class CategoryRule(
    val label: String,
    val terms: List<CategoryTerm>
)

private fun terms(vararg values: String) = values.map { CategoryTerm(it) }

private val categoryRules = listOf(
    CategoryRule("Соцсети", terms(
        "telegram", "whatsapp", "vkontakte", "vk", "instagram", "facebook", "messenger",
        "snapchat", "discord", "reddit", "tiktok", "threads", "wechat", "signal",
        "social", "chat", "community", "dating", "forum", "соцсет", "мессендж",
        "переписк", "однокласс", "знакомств", "общени"
    )),
    CategoryRule("Путешествия", terms(
        "google maps", "yandex maps", "maps", "navigation", "navigator", "travel", "trip",
        "booking", "airbnb", "uber", "taxi", "flight", "transit", "railway", "maps",
        "карты", "навигац", "путеше", "авиа", "отел", "поезд", "такси", "транспорт",
        "билет", "метро", "вокзал", "аэропорт", "каршеринг"
    )),
    CategoryRule("Развлечения", terms(
        "youtube", "spotify", "netflix", "twitch", "kinopoisk", "ivi", "okko", "music",
        "video", "movie", "cinema", "streaming", "podcast", "radio", "game", "игр",
        "кино", "музык", "видео", "стрим", "подкаст", "радио", "игров", "тв"
    )),
    CategoryRule("Финансы", terms(
        "banking", "bank", "finance", "fintech", "wallet", "payment", "pay", "credit",
        "loan", "investment", "trading", "tax", "insurance", "crypto", "sber", "tinkoff",
        "alfabank", "vtb", "сбер", "банк", "финанс", "кошелек", "оплат", "платеж",
        "кредит", "налог", "инвест", "страхован", "перевод", "брокер", "акци", "валют"
    )),
    CategoryRule("Покупки", terms(
        "shopping", "shop", "store", "marketplace", "commerce", "retail", "ozon",
        "wildberries", "aliexpress", "amazon", "ebay", "market", "покуп", "магазин",
        "маркет", "торгов", "заказ", "скидк", "купон", "каталог"
    )),
    CategoryRule("Работа", terms(
        "microsoft teams", "google docs", "google drive", "office", "productivity", "work",
        "business", "enterprise", "gmail", "outlook", "mail", "email", "calendar", "docs",
        "drive", "slack", "zoom", "notion", "asana", "trello", "jira", "crm", "meeting",
        "task", "cloud", "file manager", "почта", "календар", "документ", "рабоч",
        "офис", "задач", "облак", "файл", "встреч", "заметк", "таблиц", "презентац"
    )),
    CategoryRule("Здоровье и спорт", terms(
        "health", "healthcare", "medical", "medicine", "doctor", "clinic", "pharmacy",
        "fitness", "workout", "gym", "exercise", "running", "health connect", "fitbit",
        "strava", "health", "здоров", "медицин", "врач", "клиник", "аптек", "лекарств",
        "фитнес", "трениров", "спорт", "бег"
    )),
    CategoryRule("Образование", terms(
        "education", "educational", "learning", "learn", "study", "school", "university",
        "student", "course", "language", "dictionary", "translate", "duolingo", "образован",
        "обучен", "учеб", "школ", "студент", "курс", "урок", "язык", "словар", "перевод"
    )),
    CategoryRule("Новости", terms(
        "news", "newspaper", "journalism", "magazine", "rss", "newsstand", "новост",
        "газет", "журнал", "сми", "лента новостей"
    )),
    CategoryRule("Фото и видео", terms(
        "camera", "gallery", "photos", "photo", "photography", "image editor", "image",
        "picture", "scanner", "screenshot", "camera", "камера", "галере", "фото", "снимок",
        "изображ", "редактор", "сканер"
    )),
    CategoryRule("Еда и доставка", terms(
        "food delivery", "food", "restaurant", "grocery", "recipe", "cafe", "doordash",
        "ubereats", "delivery", "restaurant", "еда", "ресторан", "продукт", "рецепт",
        "кафе", "доставк", "меню"
    )),
    CategoryRule("Книги и чтение", terms(
        "ebook", "e-book", "reader", "audiobook", "kindle", "literature", "book",
        "книг", "читал", "чтение", "аудиокниг", "библиотек", "литератур"
    )),
    CategoryRule("Инструменты", terms(
        "settings", "calculator", "converter", "flashlight", "utility", "utilities", "toolbox",
        "launcher", "file manager", "device care", "cleaner", "weather", "clock", "alarm",
        "recorder", "system", "backup", "bluetooth", "files", "фонар", "калькулятор",
        "конвертер", "настройк", "погода", "часы", "будиль", "диктофон", "система",
        "резервн", "очистк", "инструмент", "память", "qr", "скриншот", "vpn",
        "безопасност", "аккумулятор", "батаре", "подключен"
    ))
)

fun categoryFor(context: Context, app: AppInfo): AppCategory {
    if (app.category != ApplicationInfo.CATEGORY_UNDEFINED) {
        ApplicationInfo.getCategoryTitle(context, app.category)
            ?.toString()
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { return AppCategory(it) }
    }

    val words = appIdentityWords(app)
    val ranked = categoryRules
        .map { rule -> rule to rule.score(words) }
        .filter { (_, score) -> score > 0 }
        .sortedByDescending { it.second }
    val best = ranked.firstOrNull()
    val runnerUpScore = ranked.getOrNull(1)?.second ?: 0

    return if (best != null && best.second >= 3 && best.second - runnerUpScore >= 2) {
        AppCategory(best.first.label)
    } else {
        AppCategory("Без категории")
    }
}

private fun appIdentityWords(app: AppInfo): Set<String> {
    val original = "${app.name} ${app.packageName}"
    val camelSeparated = original.replace(Regex("([\\p{Ll}])([\\p{Lu}])"), "$1 $2")
    val normalized = "$original $camelSeparated"
        .lowercase(Locale.ROOT)
        .replace('ё', 'е')
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()

    return normalized.split(Regex("\\s+")).filter(String::isNotBlank).toSet()
}

private fun CategoryRule.score(words: Set<String>): Int {
    val matchedWeights = terms.mapNotNull { term ->
        val matched = when {
            term.words.size > 1 && term.words.all(words::contains) -> term.weight + 2
            term.words.size == 1 && term.words.first() in words -> term.weight
            term.words.size == 1 && term.words.first().length >= 5 &&
                words.any { it.startsWith(term.words.first()) } -> term.weight - 1
            else -> 0
        }
        matched.takeIf { it > 0 }
    }.distinct().sortedDescending()

    return matchedWeights.firstOrNull()?.plus(matchedWeights.drop(1).take(2).sumOf { 1 }) ?: 0
}
