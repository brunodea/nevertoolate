package br.brunodea.nevertoolate.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

@Dao
public interface EntityDao<T> {
    @Insert
    long insert(T t);
    @Update
    void update(T t);
    @Delete
    void delete(T t);
}
