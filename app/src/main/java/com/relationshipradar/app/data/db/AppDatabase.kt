package com.relationshipradar.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Person::class, ContactIdentifier::class, Interaction::class, ReminderState::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun personDao(): PersonDao
    abstract fun identifierDao(): IdentifierDao
    abstract fun interactionDao(): InteractionDao
    abstract fun reminderStateDao(): ReminderStateDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "relationship_radar.db")
                .build()
                .also { instance = it }
        }
    }
}
