package com.marblevhs.clairsavedimages.room

import androidx.room.*
import com.marblevhs.clairsavedimages.data.LocalImage

@Dao
interface ImageDao {
    @Insert suspend fun insert(image: LocalImage)
    @Update suspend fun update(image: LocalImage)
    @Delete suspend fun delete(image: LocalImage)

    @get:Query("SELECT * FROM LocalImage")
    val allImage: List<LocalImage>

    @Query("SELECT * FROM LocalImage where id = :imageId")
    fun getImageByImageId(imageId: String): LocalImage?
}

@Database(entities = [LocalImage::class], version = 1)
abstract class DatabaseStorage : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}