package net.asim.inventory.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by asimaltwijry on 5/5/17.
 */


public class InventoryDBHelper extends SQLiteOpenHelper {

    public static final String TAG = InventoryDBHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;


    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY = "CREATE TABLE " + InventoryContract.ProductEntry.TABLE_NAME + " ("
                + InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.ProductEntry.COL_NAME + " TEXT NOT NULL, "
                + InventoryContract.ProductEntry.COL_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COL_PRICE + " REAL NOT NULL DEFAULT 0.0, "
                + InventoryContract.ProductEntry.COL_PICTURE + " TEXT NOT NULL DEFAULT 'No images', "
                + InventoryContract.ProductEntry.COL_ITEMS_SOLD + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COL_SUPPLIER + " TEXT NOT NULL DEFAULT '05xxxxxxxx' "
                + ");";

        db.execSQL(SQL_CREATE_INVENTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InventoryContract.ProductEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
