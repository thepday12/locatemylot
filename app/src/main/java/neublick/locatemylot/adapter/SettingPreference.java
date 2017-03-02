package neublick.locatemylot.adapter;

public class SettingPreference {
	// dung de phan biet xem item nen hien thi nhu the nao
	public String type;

	// day la nhan hien thi
	public String title;

	// day la gia tri hien thi cua value
	public String summary;

	// day moi la gia tri thuc su
	public Object value;

	public SettingPreference(String type, String title, String summary, Object value) {
		this.type 		= type;
		this.title		= title;
		this.summary	= summary;
		this.value 		= value;
	}
}