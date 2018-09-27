package com.codingwithmitch.applicationone.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"id"}, unique = true)})
public class NoteDataEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String note;

}
