package com.simplechat.client.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.simplechat.client.R;
import com.simplechat.client.domain.ChatMessage;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * Created by Rufim on 25.01.2015.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper implements Serializable {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    //имя файла базы данных который будет храниться в /data/data/APPNAME/database_name.db
    private static String database_name;

    //с каждым увеличением версии, при нахождении в устройстве БД с предыдущей версией будет выполнен метод onUpgrade();
    private static final int DATABASE_VERSION = 1;

    //ссылки на DAO соответсвующие сущностям, хранимым в БД
    private HistoryDAO HistoryDao = null;

    public DatabaseHelper(Context context) {
        super(context, database_name = context.getResources().getString(R.string.database_name_key) + ".db", null, DATABASE_VERSION);
    }

    //Выполняется, когда файл с БД не найден на устройстве
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ChatMessage.class);
        } catch (SQLException e) {
            Log.e(TAG, "error creating DB " + database_name);
            throw new RuntimeException(e);
        }
    }

    //Выполняется, когда БД имеет версию отличную от текущей
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer,
                          int newVer) {
        try {
            //Так делают ленивые, гораздо предпочтительнее не удаляя БД аккуратно вносить изменения
            TableUtils.dropTable(connectionSource, ChatMessage.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "error upgrading db " + database_name + "from ver " + oldVer);
            throw new RuntimeException(e);
        }
    }


    // синглтон для HistoryDAO
    public HistoryDAO getHistoryDAO() throws SQLException {
        if (HistoryDao == null) {
            HistoryDao = new HistoryDAO(getConnectionSource(), ChatMessage.class);
        }
        return HistoryDao;
    }

    //выполняется при закрытии приложения
    @Override
    public void close() {
        super.close();
        HistoryDao = null;
    }
}

