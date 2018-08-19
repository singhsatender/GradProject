package com.stressfreeroads.gradproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * DB Manager to manage user profile data. Its not used yet as we are using file system for easy access.
 * Created by singh on 4/21/2018.
 */

public class DBManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="gradproject.db";
    private static final int DATABASE_VERSION=1;

    public static final String USER_PROFILE_TABLE="user_profile";
    public static final String COLUMN_ID="_id";
    public static final String COLUMN_NAME="Name";
    public static final String COLUMN_EMAIL="email";
    public static final String COLUMN_MOB_NUM="mob_num";
    public static final String COLUMN_ANS1="ans1";
    public static final String COLUMN_ANS2="ans2";
    public static final String COLUMN_ANS3="ans3";
    public static final String COLUMN_ANS4="ans4";
    public static final String COLUMN_ANS5="ans5";
    public static final String COLUMN_ANS6="ans6";
    public static final String COLUMN_ANS7="ans7";
    public static final String COLUMN_ANS8="ans8";

    private String[] allColumns={COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_MOB_NUM, COLUMN_ANS1,
            COLUMN_ANS2, COLUMN_ANS3, COLUMN_ANS4, COLUMN_ANS5, COLUMN_ANS6, COLUMN_ANS7, COLUMN_ANS8 };

    private SQLiteDatabase sqlDB;

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String  CREATE_TABLE="create table "+USER_PROFILE_TABLE+" ( "+
            COLUMN_ID+ " integer primary key autoincrement ,"+
            COLUMN_NAME+ " text not null, "+
            COLUMN_EMAIL+ " text not null, " +
            COLUMN_MOB_NUM+ " text not null, "+
            COLUMN_ANS1+ " text not null, "+
            COLUMN_ANS2+ " text not null, "+
            COLUMN_ANS3+ " text not null, "+
            COLUMN_ANS4+ " text not null, "+
            COLUMN_ANS5+ " text not null, "+
            COLUMN_ANS6+ " text not null, "+
            COLUMN_ANS7+ " text not null, "+
            COLUMN_ANS8+ " text not null); "
            ;

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d("Creating Table", "Creating Table");
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.d("Upgrading DataBase", "Upgrading Database");
        db.execSQL("DROP TABLE IF EXISTS "+ USER_PROFILE_TABLE);
        onCreate(db);
    }

    public void open()  throws android.database.SQLException{
        sqlDB=this.getWritableDatabase();
    }

    public void close()
    {
        sqlDB.close();
    }

    // This function is used to insert the data in the DB as per the user activity
    public void insert_in_userProfile(String name,String email, String mobnum, String ans1,
                                      String ans2, String ans3, String ans4, String ans5,String ans6, String ans7, String ans8)
    {
        ContentValues values= new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_MOB_NUM, mobnum);
        values.put(COLUMN_ANS1, ans1);
        values.put(COLUMN_ANS2, ans2);
        values.put(COLUMN_ANS3, ans3);
        values.put(COLUMN_ANS4, ans4);
        values.put(COLUMN_ANS5, ans5);
        values.put(COLUMN_ANS6, ans6);
        values.put(COLUMN_ANS7, ans7);
        values.put(COLUMN_ANS8, ans8);

        long insertId=sqlDB.insert(USER_PROFILE_TABLE, null, values);
        Log.d("INSERTED","INSERTED => " + insertId);
    }

    public void truncate()
    {
        sqlDB.execSQL("DELETE FROM "+USER_PROFILE_TABLE);
    }

    //TODO:Useful to fect data if db is used.
    //This function is used to fetch all the user details from the database
//    public ArrayList<User> getAllDetails(){
//
//        ArrayList<User>  userlist=new ArrayList<User>();
//
//        Cursor cursor=sqlDB.query(USER_PROFILE_TABLE,allColumns, null, null, null, null, null);
//
//        for(cursor.moveToLast();!cursor.isBeforeFirst();cursor.moveToPrevious())
//        {
//            User userDetails=cursorToUser(cursor);
//            userlist.add(userDetails);
//        }
//
//        cursor.close();
//        return userlist;
//    }
//
//    private User cursorToUser(Cursor cursor)
//    {
//        User userDetails=new User(cursor.getString(1),cursor.getString(2));
//        return userDetails;
//    }

}

