package neublick.locatemylot.database;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import neublick.locatemylot.djikstra.Edge;
import neublick.locatemylot.djikstra.Vertex;
import android.content.ContentValues;
import android.database.Cursor;

/*
db.execSQL("CREATE TABLE IF NOT EXISTS CL_PATH(" +
		"ID INTEGER, " +
		"X REAL, "+
		"Y REAL, "+
		"LABEL VARCHAR, "+
		"ADJ VARCHAR, "+
		"CARPARK_ID INTEGER , PRIMARY KEY (ID, CARPARK_ID) )"
		);
*/

public class CLPath {

	public static long addEntry(Vertex item) {
		ContentValues values = new ContentValues();
		values.put("ID", 			item.id);
		values.put("X", 			item.x);
		values.put("Y", 			item.y);
		values.put("LABEL", 		item.toString());
		values.put("ADJ", 			item.adjacenciesString);
		values.put("CARPARK_ID", item.carparkId);
		return Database.getDatabase().insert(Database.TABLE_PATH, null, values);
	}

	public static Vertex getEntry(int id, int carparkId) {
		Vertex result = new Vertex();
		final String[] columns = {
			"ID",
			"X",
			"Y",
			"LABEL",
			"ADJ",
			"CARPARK_ID"
		};
		final String whereClause = "ID=? AND CARPARK_ID=?";
		final String[] whereArgs = {
			String.valueOf(id),
			String.valueOf(carparkId)
		};
		Cursor c = Database.getDatabase().query(Database.TABLE_PATH, columns, whereClause, whereArgs, null, null, null, null);
		if (c.moveToFirst()) {
			result.id 					= id;
			result.x 					= c.getFloat(1);
			result.y 					= c.getFloat(2);
			result.label				= c.getString(3);
			result.adjacenciesString 	= c.getString(4);
			result.carparkId 			= carparkId;
			result.adjacencies 			= new ArrayList<Edge>();
		}
		c.close();
		return result;
	}

	public static long deleteAll() {
		return Database.getDatabase().delete(Database.TABLE_PATH, null, null);
	}

	public static List<Vertex> getAllEntriesByCarparkId(int carparkId) {
		return getAllVertexByCarparkId(carparkId);
	}


	// lay toan bo Vertex theo carparkId va tra ve Map<Integer, Vertex>
	// Map<K, V>
	// K la vertex.id
	// V la instance cua vertex
	public Map<Integer, Vertex> vertexMap;

	public static Map<Integer, Vertex> getVertexMapByCarparkId(int carparkId) {
		Map<Integer, Vertex> result = new HashMap<Integer, Vertex>();
		final String[] columns = {
			"ID",
			"X",
			"Y",
			"LABEL",
			"ADJ",
			"CARPARK_ID"
		};
		final String whereClause = "CARPARK_ID=?";
		final String[] whereArgs = {
			String.valueOf(carparkId)
		};
		Cursor c = Database.getDatabase().query(Database.TABLE_PATH, columns, whereClause, whereArgs, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Vertex item 			= new Vertex();
				item.id 				= c.getInt(0);
				item.x 					= c.getFloat(1);
				item.y 					= c.getFloat(2);
				item.label 				= c.getString(3);
				item.adjacenciesString 	= c.getString(4);
				item.carparkId 			= c.getInt(5);
				item.adjacencies 		= new ArrayList<Edge>();
				result.put(item.id, item);
			} while(c.moveToNext());
		}
		c.close();
		for(Vertex item: result.values()) {
			StringTokenizer tokenizer = new StringTokenizer(item.adjacenciesString.trim(), ",");
			while(tokenizer.hasMoreTokens()) {
				Vertex matchItem = new Vertex();
				matchItem.carparkId = carparkId;
				matchItem.id = Integer.valueOf(tokenizer.nextToken());
				if (result.containsKey(matchItem.id)) {
					item.adjacencies.add(
						new Edge(item, result.get(matchItem.id))
					);
				}
			}
		}
		return result;
	}

	// lay toan bo Vertex theo carparkId
	public static List<Vertex> getAllVertexByCarparkId(int carparkId) {
		List<Vertex> result = new ArrayList<Vertex>();
		final String[] columns = {
			"ID",
			"X",
			"Y",
			"LABEL",
			"ADJ", // id1:id2:id3
			"CARPARK_ID"
		};
		final String whereClause = "CARPARK_ID=?";
		final String[] whereArgs = {
			String.valueOf(carparkId)
		};
		Cursor c = Database.getDatabase().query(Database.TABLE_PATH, columns, whereClause, whereArgs, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Vertex item 			= new Vertex();
				item.id 				= c.getInt(0);
				item.x 					= c.getFloat(1);
				item.y 					= c.getFloat(2);
				item.label 				= c.getString(3);
				item.adjacenciesString 	= c.getString(4);
				item.carparkId 			= c.getInt(5);
				item.adjacencies 		= new ArrayList<Edge>();
				result.add(item);
			} while(c.moveToNext());
		}
		c.close();
		if (result != null) {
			lw("TheKoCodeAndroidNua:" + result.toString());
		}
		for(Vertex item: result) {
			StringTokenizer tokenizer = new StringTokenizer(item.adjacenciesString.trim(), ",");
			while(tokenizer.hasMoreTokens()) {
				Vertex matchItem = new Vertex();
				matchItem.carparkId = carparkId;
				matchItem.id = Integer.valueOf(tokenizer.nextToken());
				if (result.contains(matchItem)) {
					item.adjacencies.add(
						new Edge(item, result.get(result.indexOf(matchItem)))
					);
				}
			}
		}
		return result;
	}


	static void lw(String s) {
		String TAG = CLPath.class.getSimpleName();
		android.util.Log.w(TAG, s);
	}
}