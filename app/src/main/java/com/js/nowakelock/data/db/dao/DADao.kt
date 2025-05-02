package com.js.nowakelock.data.db.dao

import androidx.room.*
import com.js.nowakelock.data.db.Type
import com.js.nowakelock.data.db.entity.DA
import com.js.nowakelock.data.db.entity.Info
import com.js.nowakelock.data.db.entity.St
import kotlinx.coroutines.flow.Flow

@Dao
interface DADao : BaseDao<St> {

//    @Transaction
//    @Query("SELECT * FROM info where type_info = :type")
//    fun loadDAs(type: Type): Flow<List<DA>>

    // for sort
    @Transaction
    @Query(
        "SELECT * FROM info left outer join st on info.userid_info = st.userId_st and info.name_info = st.name_st " +
                "where type_info = :type"
    )
    fun loadISs(type: Type): Flow<Map<Info, St?>>

    @Transaction
    @Query(
        "SELECT * FROM info left outer join st on info.userid_info = st.userId_st and info.name_info = st.name_st " +
                "where type_info = :type and packageName_info = :packageName and userid_info = :userId"
    )
    fun loadISs(packageName: String, type: Type, userId: Int = 0): Flow<Map<Info, St?>>

    @Query("select * from info where name_info = :name and type_info = :type and userid_info = :userId")
    fun loadInfo(name: String, type: Type, userId: Int = 0): Flow<Info>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(infos: List<Info>)

    @Query("select * from st where name_st = :name and type_st = :type and userId_st = :userId")
    fun loadSt(name: String, type: Type, userId: Int = 0): Flow<St?>

    @Query("select * from st where type_st = :type")
    fun loadSts(type: Type): Flow<List<St>>

    @Query("select * from st where type_st = :type and userId_st = :userId")
    suspend fun loadStsDB(type: Type, userId: Int = 0): List<St>

    @Query("select * from st")
    suspend fun loadAllSts(): List<St>

    @Transaction
    @Query(
        "delete from info where not exists (select 1 from st " +
                "where name_info = name_st and type_info=type_st " +
                "and packageName_info = packageName_st and userId_info = userId_st)"
    )
    suspend fun clearNoActive()

    /**
     * Retrieves Info records for a specific package and type
     * Used for aggregating statistics for an application
     */
    @Query("SELECT * FROM info WHERE packageName_info = :packageName AND type_info = :type AND userid_info = :userId")
    suspend fun getInfosByPackageAndType(packageName: String, type: Type, userId: Int): List<Info>
}