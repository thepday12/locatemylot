package neublick.locatemylot.app;

// static class
public class Config {

	// ================================ //
	// DON VI THOI GIAN LA MILLISECONDS //
	// ================================ //

	// neu welcome dialog hien thi thi phai sau
	// it nhat khoang thoi gian nay moi hien thi lai
	public static int THRESHOLD_ENTRY_CARPARK = 5*60*1000;

	// sau khoang thoi gian nay neu ko the phat hien ra duoc beacon nao
	// thi => da ra khoi carpark
	public static int THRESHOLD_NO_BEACON_FOUND = 10*60*1000;

	// thoi gian giua 2 lan bao thuc
	// phai dat thoi gian bao thuc > THRESHOLD_SNOOZE_TIME
	public static int THRESHOLD_SNOOZE_TIME = 2*60*1000;

	// thoi gian giua 2 lan toast message USER dang o rat gan voi CAR
	public static int THRESHOLD_TOAST_MESSAGE_NEARBY = 10*1000;

	// chon loc ra 4 Beacon de tinh toan
	public static int BEACON_FOUND_TRIM_FOR_CALCULATING = 4;

	// de o external storage
	public static String PHOTO_SAVE_DIR = "locateMyLot";

	public static int TOOLBAR_GIAM_HEIGHT = 16; // PIXELS

	// thoi gian de Handler kiem tra co set location theo shared location hay ko?
	public static int TIME_TO_CHECK_LOCATION_SHARED = 10*1000;

	// initialCapacity cua ArrayList va PriorityQueue danh cho thuat toan djikstra
	public static int COLLECTION_INITIAL_CAPACITY = 1000;

	// thoi gian tu dong check out la 100 gio
	public static int CHECKOUT_TIMEOUT = 100*60*60*1000;

	// version
	public static String VERSION = "LML 2015-07-22:22:20";

	public static int PROMOTION_UPDATE_INTERVAL = 10*1000;//260*60*60*1000;
	public static int GET_SHARED_UPDATE_INTERVAL = 5*1000;

	public static String PROMOTION_IMAGE_DIR = "lmlPromotion";
	public static String CMS_URL = "http://neublick.com/demo/carlocation";
}