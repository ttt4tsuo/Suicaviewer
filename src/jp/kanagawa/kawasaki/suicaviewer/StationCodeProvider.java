package jp.kanagawa.kawasaki.suicaviewer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public final class StationCodeProvider extends ContentProvider {
	
    private static final String AUTHORITY = "jp.kanagawa.kawasaki.suicaviewer.provider";
    private static final String PATH = "sqlite.stationcode";
   	public final Uri contentUri = Uri.parse("content://" + AUTHORITY + "/" + PATH);
   	public final String mimeTypeForItem = "vnd.android.cursor.item/vnd.suicaviewer." + PATH;
    public final String mimeTypeForDir = "vnd.android.cursor.dir/vnd.kojion." + PATH;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, PATH, 1);
        sUriMatcher.addURI(AUTHORITY, PATH + "/#", 2);
    }

    private StationCodeSqliteOpenHelper mSqliteOpenHelper;
    
    @Override
    public boolean onCreate() {
        final int version = 1;
        mSqliteOpenHelper = new StationCodeSqliteOpenHelper(getContext(), PATH, null, version);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    	if (-1 == sUriMatcher.match(uri)){
    		throw new IllegalArgumentException("unknown uri : " + uri);
    	}
        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(uri.getPathSegments().get(0), projection,
        		selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
	
    @Override
	public String getType(Uri uri) {
		return null;
	}
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	if (-1 == sUriMatcher.match(uri)){
    		throw new IllegalArgumentException("unknown uri : " + uri);
    	}
        SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
        final int count = db.update(uri.getPathSegments().get(0), values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
	
    @Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	
    @Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

    private static class StationCodeSqliteOpenHelper extends SQLiteOpenHelper {
        public StationCodeSqliteOpenHelper(Context context, String dbname, CursorFactory cursorfactory, int ver) {
            super(context, dbname, cursorfactory, ver);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE Table1");
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
    }
}