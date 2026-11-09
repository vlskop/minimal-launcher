package com.example.minimallauncher.data.repository

import android.content.Context
import android.content.Intent
import com.example.minimallauncher.core.model.AppInfo

class AppRepository(
    private val context: Context
) {
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0)

        return apps.map { resolveInfo ->
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                name = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager),
                category = resolveInfo.activityInfo.applicationInfo.category
            )
        }.sortedBy { it.name.lowercase() }
    }
}
