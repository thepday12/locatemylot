package neublick.locatemylot.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by theptokim on 11/9/17.
 */

public class ShareReceiveObject {
    private int id;
    private long time;
    private int type;
    private String userFromName;
    private String imageUrl;

    /***
     * {"type":"1","user_from":"178", "user_to":"178", "user_from_name":"Thép Tô Kim", "x":"", "y":"", "floor":"",
     * "zone":"", "carpark_id":"", "check_in_time":"",
     * "image_url":"http://neublick.com/demo/carlocation/cms/upload_files/screen_share20171107143015.jpg"}
     */
    public ShareReceiveObject(int id, String data,int type,long time) {
        this.id = id;
        this.type = type;
        this.time = time;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
            this.userFromName = jsonObject.getString("user_from_name");
            this.imageUrl = jsonObject.getString("image_url");
        } catch (JSONException e) {
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserFromName() {
        return userFromName;
    }

    public void setUserFromName(String userFromName) {
        this.userFromName = userFromName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
