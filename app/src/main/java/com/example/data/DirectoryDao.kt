package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.AdvocateEntity
import com.example.model.AdvocateWithClerk
import com.example.model.ClerkEntity
import com.example.model.ClerkWithAdvocates
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectoryDao {

    @Transaction
    @Query("SELECT * FROM advocates ORDER BY CASE WHEN initialSrNo IS NOT NULL THEN initialSrNo ELSE id END ASC")
    fun getAllAdvocatesWithClerks(): Flow<List<AdvocateWithClerk>>

    @Transaction
    @Query("SELECT * FROM advocates ORDER BY name ASC")
    fun getAllAdvocatesWithClerksByName(): Flow<List<AdvocateWithClerk>>

    @Transaction
    @Query("SELECT * FROM clerks ORDER BY name ASC")
    fun getAllClerksWithAdvocates(): Flow<List<ClerkWithAdvocates>>

    @Query("SELECT * FROM clerks ORDER BY name ASC")
    fun getAllClerks(): Flow<List<ClerkEntity>>

    @Query("SELECT * FROM clerks ORDER BY name ASC")
    suspend fun getAllClerksSnapshot(): List<ClerkEntity>

    @Query("SELECT * FROM advocates")
    suspend fun getAllAdvocatesSnapshot(): List<AdvocateEntity>

    @Transaction
    @Query("SELECT * FROM advocates WHERE id = :advocateId")
    suspend fun getAdvocateWithClerkById(advocateId: Long): AdvocateWithClerk?

    @Query("SELECT * FROM clerks WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findClerkByName(name: String): ClerkEntity?

    @Query("SELECT COUNT(*) FROM advocates")
    suspend fun getAdvocateCount(): Int

    @Query("SELECT COUNT(*) FROM clerks")
    suspend fun getClerkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvocate(advocate: AdvocateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvocates(advocates: List<AdvocateEntity>): List<Long>

    @Update
    suspend fun updateAdvocate(advocate: AdvocateEntity)

    @Delete
    suspend fun deleteAdvocate(advocate: AdvocateEntity)

    @Query("DELETE FROM advocates WHERE id = :id")
    suspend fun deleteAdvocateById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClerk(clerk: ClerkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClerks(clerks: List<ClerkEntity>): List<Long>

    @Update
    suspend fun updateClerk(clerk: ClerkEntity)

    @Delete
    suspend fun deleteClerk(clerk: ClerkEntity)

    @Query("UPDATE advocates SET clerkId = :newClerkId WHERE id = :advocateId")
    suspend fun updateAdvocateClerk(advocateId: Long, newClerkId: Long?)

    @Query("UPDATE advocates SET clerkId = :clerkId WHERE id IN (:advocateIds)")
    suspend fun assignAdvocatesToClerk(advocateIds: List<Long>, clerkId: Long)

    @Query("DELETE FROM advocates")
    suspend fun clearAdvocates()

    @Query("DELETE FROM clerks")
    suspend fun clearClerks()
}
