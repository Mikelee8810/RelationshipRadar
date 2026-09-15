package com.relationshipradar.app.data.db

/** Seeded on first launch. Users can edit intervals and add custom categories. */
object BuiltInCategories {
    val all: List<Category> = listOf(
        Category(name = "Partner / Spouse", defaultIntervalDays = null, reminderBehavior = ReminderBehavior.INDIVIDUAL, builtIn = true, sortOrder = 0),
        Category(name = "Immediate Family", defaultIntervalDays = 7, reminderBehavior = ReminderBehavior.INDIVIDUAL, builtIn = true, sortOrder = 1),
        Category(name = "Extended Family", defaultIntervalDays = 30, builtIn = true, sortOrder = 2),
        Category(name = "Best Friend", defaultIntervalDays = 7, reminderBehavior = ReminderBehavior.INDIVIDUAL, builtIn = true, sortOrder = 3),
        Category(name = "Close Friend", defaultIntervalDays = 14, builtIn = true, sortOrder = 4),
        Category(name = "Friend", defaultIntervalDays = 30, builtIn = true, sortOrder = 5),
        Category(name = "Work / Coworker", defaultIntervalDays = 30, builtIn = true, sortOrder = 6),
        Category(name = "Professional Contact", defaultIntervalDays = 60, builtIn = true, sortOrder = 7),
        Category(name = "Neighbor", defaultIntervalDays = 60, builtIn = true, sortOrder = 8),
        Category(name = "Online Friend", defaultIntervalDays = 30, builtIn = true, sortOrder = 9),
        Category(name = "Acquaintance", defaultIntervalDays = 90, builtIn = true, sortOrder = 10),
        Category(name = "Person I Should Check On", defaultIntervalDays = 14, reminderBehavior = ReminderBehavior.INDIVIDUAL, builtIn = true, sortOrder = 11),
    )
}
