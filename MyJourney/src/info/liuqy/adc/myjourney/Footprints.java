package info.liuqy.adc.myjourney;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Footprints {
	public static final int DB_VERSION = 2;
	public static final String DB_NAME = "data";
	public static final String TBL_NAME = "footprints";
	public static final String FIELD_LATITUDE = "latitude";
	public static final String FIELD_LONGITUDE = "longitude";
	public static final String FIELD_FLAG = "flag";
	public static final String FIELD_RESOURCE = "resource";
	public static final String FIELD_ID = "_id";

	public static enum FLAG {
		F // flag only
		, P // photo
		, V // video
	};

	static final String CREATE_SQL = "create table "
			+ "footprints(_id integer primary key autoincrement, "
			+ "latitude double not null, longitude double not null,"
			+ "flag char(1) not null, resource text)";
	Context ctx;
	SQLiteOpenHelper dbHelper;
	SQLiteDatabase db;

	public Footprints(Context ctx) {
		this.ctx = ctx;
	}

	public Footprints open() {
		dbHelper = new SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(CREATE_SQL);
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				// TODO on db upgrade
			}
		};
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getFootprintsIn(double startLatitude, double startLongitude,
			double endLatitude, double endLongitude) {
		Cursor cur = db.query(true, TBL_NAME, new String[] { FIELD_LATITUDE,
				FIELD_LONGITUDE, FIELD_FLAG, FIELD_RESOURCE }, FIELD_LATITUDE
				+ ">" + startLatitude + " AND " + FIELD_LATITUDE + "<"
				+ endLatitude + " AND " + FIELD_LONGITUDE + ">"
				+ startLongitude + " AND " + FIELD_LONGITUDE + "<"
				+ endLongitude, null, null, null, null, null);
		return cur;
	}

	public long saveFootprintAt(double latitude, double longitude, FLAG flag,
			String resource) {
		// TODO
		return 0L;
	}

}
