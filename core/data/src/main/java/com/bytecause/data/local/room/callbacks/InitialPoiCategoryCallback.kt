package com.bytecause.data.local.room.callbacks

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bytecause.core.resources.R
import com.bytecause.data.di.DatabaseModule.provideDatabase
import com.bytecause.data.local.room.dao.CustomPoiDao
import com.bytecause.data.local.room.tables.CustomPoiCategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class InitialPoiCategoryCallback(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                populateDatabase(context, provideDatabase(context).customPoiDao())
            } finally {
                cancel()
            }
        }
    }
}

private suspend fun populateDatabase(context: Context, customPoiDao: CustomPoiDao) {
    // Use context to access localized strings
    val categories = listOf(
        CustomPoiCategoryEntity(
            categoryName = context.getString(R.string.favorite),
            drawableResourceName = context.resources.getResourceEntryName(R.drawable.baseline_favorite_24)
        ),
        CustomPoiCategoryEntity(
            categoryName = context.getString(R.string.travel_plans),
            drawableResourceName = context.resources.getResourceEntryName(R.drawable.baseline_mode_of_travel_24)
        ),
        CustomPoiCategoryEntity(
            categoryName = context.getString(R.string.nautical_poi),
            drawableResourceName = context.resources.getResourceEntryName(R.drawable.nautical_poi_icon)
        )
    )
    categories.forEach { customPoiDao.insertCategory(it) }
}