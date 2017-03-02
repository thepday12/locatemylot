package neublick.locatemylot.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import neublick.locatemylot.model.Promotion;
import neublick.locatemylot.util.PromotionUtil;

public class CLPromotion {


	public static long addEntryWithUser(Promotion entry, String username) {
		le("addEntryWithUser(" + username + ")");
		ContentValues values = new ContentValues();
		values.put("ID", 				entry.id);
		values.put("NAME", 				entry.name);
		values.put("USER_NAME",			username);
		values.put("PROMOTION_THUMB", 	PromotionUtil.getImageFileNameFromLink(entry.promotionThumb));
		values.put("PROMOTION_IMAGE",	PromotionUtil.getImageFileNameFromLink(entry.promotionImage));
		values.put("TIME_GET", 			entry.timeGet);
		values.put("TIME_REDEEM",		entry.timeRedeem);
		values.put("PROMOTION_CONTENT", entry.promotionContent);
		new PromotionUtil.CopyFileFromInternetTask(PromotionUtil.getImageFileNameFromLink(entry.promotionThumb)).execute(entry.promotionThumb);
		new PromotionUtil.CopyFileFromInternetTask(PromotionUtil.getImageFileNameFromLink(entry.promotionImage)).execute(entry.promotionImage);
		return Database.getDatabase().insertWithOnConflict(Database.TABLE_PROMOTION, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}

	public static long deleteEntryWithUser(Promotion entry, String username) {
		return Database.getDatabase().delete(Database.TABLE_PROMOTION, "ID = ? AND USER_NAME = ?", new String[] {
			String.valueOf(entry.id),
			username
		});
	}

	public static List<Promotion> getAllByUserName(final String username) {
		le("*username="+username);
		Cursor c = Database.getDatabase().rawQuery(
			"SELECT ID, NAME, USER_NAME, PROMOTION_THUMB, PROMOTION_IMAGE, TIME_GET, TIME_REDEEM, PROMOTION_CONTENT "
			+ "FROM CL_PROMOTION WHERE USER_NAME = ?",
			new String[] {
				username
			}
		);
		List<Promotion> promotionList = new ArrayList<Promotion>();
		if (c.moveToFirst()) {
			do {
				Promotion item 			= new Promotion();
				item.id 				= c.getInt(0);
				item.name 				= c.getString(1);
				item.userName 			= c.getString(2);
				item.promotionThumb 	= c.getString(3);
				item.promotionImage		= c.getString(4);
				item.timeGet			= c.getLong(5);
				item.timeRedeem 		= c.getLong(6);
				item.promotionContent	= c.getString(7);
				promotionList.add(item);
			} while(c.moveToNext());
			c.close();
		}
		return promotionList;
	}

	public static void le(String fmt) {
		final String TAG = CLPromotion.class.getSimpleName();
		android.util.Log.e(TAG, fmt);
	}
}