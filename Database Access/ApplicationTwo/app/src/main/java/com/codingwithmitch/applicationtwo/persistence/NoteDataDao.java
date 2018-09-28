package com.codingwithmitch.applicationtwo.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface NoteDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertNotes(NoteDataEntity... notes);

    @Update
    public void updateNotes(NoteDataEntity... notes);

    @Delete
    public void deleteNotes(NoteDataEntity... notes);

    @Query("SELECT * FROM NoteDataEntity")
    public List<NoteDataEntity> getNotes();

    @Query("SELECT * FROM NoteDataEntity LIMIT :row, 2 ")
    public List<NoteDataEntity> getSomeNotes(int row);

    @Query("SELECT COUNT(*) FROM NoteDataEntity")
    public Integer getNumRows();
}
