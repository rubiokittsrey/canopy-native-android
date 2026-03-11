package app.practice.canopy_native_android.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import app.practice.canopy_native_android.database.dao.BiodiversityDao;
import app.practice.canopy_native_android.database.dao.HazardDao;
import app.practice.canopy_native_android.database.dao.InfrastructureDao;
import app.practice.canopy_native_android.database.dao.SoilDao;
import app.practice.canopy_native_android.database.dao.SurveyDao;
import app.practice.canopy_native_android.database.dao.TopographicDao;
import app.practice.canopy_native_android.database.dao.UserDao;
import app.practice.canopy_native_android.database.dao.VegetationDao;
import app.practice.canopy_native_android.database.dao.WaterDao;
import app.practice.canopy_native_android.database.entities.BiodiversityEntity;
import app.practice.canopy_native_android.database.entities.HazardEntity;
import app.practice.canopy_native_android.database.entities.InfrastructureEntity;
import app.practice.canopy_native_android.database.entities.SoilEntity;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.database.entities.UserEntity;
import app.practice.canopy_native_android.database.entities.VegetationEntity;
import app.practice.canopy_native_android.database.entities.WaterEntity;

@Database(
    entities = {
        UserEntity.class,
        SurveyEntity.class,
        TopographicEntity.class,
        VegetationEntity.class,
        SoilEntity.class,
        WaterEntity.class,
        BiodiversityEntity.class,
        HazardEntity.class,
        InfrastructureEntity.class
    },
    version = 1,
    exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "canopy_database";
    private static volatile AppDatabase instance;

    // abstract dao getters
    public abstract UserDao userDao();
    public abstract SurveyDao surveyDao();
    public abstract TopographicDao topographicDao();
    public abstract VegetationDao vegetationDao();
    public abstract SoilDao soilDao();
    public abstract WaterDao waterDao();
    public abstract BiodiversityDao biodiversityDao();
    public abstract HazardDao hazardDao();
    public abstract InfrastructureDao infrastructureDao();

    /**
     * Double-checked locking singleton pattern
     * **/
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).fallbackToDestructiveMigration(false).build();
                }
            }
        }
        return instance;
    }

    /**
     * Clear all tables - for logout
     * **/
    public void clearAllTables() {
        if (instance != null) {
            instance.clearAllTables();
        }
    }

}
