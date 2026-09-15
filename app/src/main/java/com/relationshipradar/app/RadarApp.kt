package com.relationshipradar.app

import android.app.Application
import com.relationshipradar.app.connectors.ConnectorRegistry
import com.relationshipradar.app.connectors.ContactsImporter
import com.relationshipradar.app.data.db.AppDatabase
import com.relationshipradar.app.data.repo.Repository
import com.relationshipradar.app.data.repo.Settings
import com.relationshipradar.app.work.ReminderScheduler
import com.relationshipradar.app.work.ScanScheduler
import com.relationshipradar.app.work.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RadarApp : Application() {
    val db by lazy { AppDatabase.get(this) }
    val repo by lazy { Repository(db) }
    val settings by lazy { Settings(this) }
    val contacts by lazy { ContactsImporter(this, repo) }
    val connectors by lazy { ConnectorRegistry(this, repo) }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        scope.launch { repo.seedCategoriesIfEmpty() }
        ReminderScheduler.ensureScheduled(this)
        ScanScheduler.ensureScheduled(this)
    }

    companion object {
        fun from(context: android.content.Context) = context.applicationContext as RadarApp
    }
}
