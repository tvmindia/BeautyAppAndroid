package com.tech.thrithvam.beautyapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "BeautyApp.db";
    private SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Creating Tables
    // IMPORTANT: if you are changing anything in the below function onCreate(), DO DELETE THE DATABASE file in
    // the emulator or uninstall the application in the phone, to run the application
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VARIABLES_TABLE = "CREATE TABLE IF NOT EXISTS Variables (variable TEXT PRIMARY KEY, value TEXT);";//store the variables in the program
        db.execSQL(CREATE_VARIABLES_TABLE);
       // db.execSQL("insert into variables values ('notIDs','');");//store notifications IDs
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE IF NOT EXISTS Notifications (NotIDs TEXT, ExDate DATE);";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        String CREATE_USERACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS UserAccount (UserName TEXT, Email TEXT, Password TEXT, MobNo TEXT, Gender TEXT);";
        db.execSQL(CREATE_USERACCOUNTS_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME );
        // Create tables again
        onCreate(db);
    }

    //---------------------Variables Table------------------
    public String getVarValue(String var)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select (value) from Variables where variable='"+var+"';",null);
        if (cursor != null)
            {cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("value"));}
        else
            return "";
    }

    public void updateVarValue(String var,String val)
    {
        db=this.getWritableDatabase();
        db.execSQL("UPDATE Variables SET value='"+val+"' WHERE variable='"+var+"';");
        db.close();
    }

    //----------------Notifications table-----------------------
    public void insertNotIDs(String notIds, String date)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO Notifications (NotIDs,ExDate) VALUES ('"+notIds+"','"+date+"');");
        db.close();
    }
    public String getNotIDs()
    {db=this.getReadableDatabase();
        String nIDs="";
        Cursor cursor = db.rawQuery("SELECT (NotIDs) FROM Notifications;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                nIDs=nIDs+","+cursor.getString(cursor.getColumnIndex("NotIDs"));
            }while (cursor.moveToNext());
            return nIDs;
        }
        else return "";
    }
    public void flushNotifications()
    {db=this.getWritableDatabase();
        long time= System.currentTimeMillis();
        db.execSQL("DELETE FROM Notifications WHERE ExDate<"+time+";");
        db.close();
    }
    //--------------------------User Accounts-----------------------------
    public void UserLogin(String UserName, String Email, String Password, String MobNo, String Gender)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO UserAccount VALUES ('"+UserName+"','"+Email+"','"+Password+"','"+MobNo+"','"+Gender+"');");
        db.close();
    }
    public void UserLogout()
    {db=this.getWritableDatabase();
     db.execSQL("DELETE FROM UserAccount;");
     db.close();
    }
    public String GetUserDetail(String detail)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM UserAccount;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(detail));
        }
        else return "";
    }
}

