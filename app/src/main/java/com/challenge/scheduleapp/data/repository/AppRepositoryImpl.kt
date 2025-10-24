package com.challenge.scheduleapp.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    override suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            packages
                .asSequence()
                .filter { app ->
                    try {
                        packageManager.getLaunchIntentForPackage(app.packageName) != null
                    } catch (e: Exception) {
                       Log.d(TAG, "Error getting launch intent for package")
                       false
                    }
                }
                .map { app ->
                    InstalledApp(
                        packageName = app.packageName,
                        appName = try {
                            app.loadLabel(packageManager).toString()
                        } catch (e: Exception) {
                            app.packageName
                        },
                        icon = try {
                            app.loadIcon(packageManager)
                        } catch (e: Exception) {
                            null
                        }
                    )
                }
                .sortedBy { it.appName }
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val TAG = "AppRepositoryImpl"
    }
}