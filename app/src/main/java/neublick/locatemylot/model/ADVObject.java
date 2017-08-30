package neublick.locatemylot.model;

import org.json.JSONException;
import org.json.JSONObject;

import neublick.locatemylot.app.Config;

/**
 * Created by theptokim on 7/21/17.
 */

public class ADVObject {
    private String id;
    private String image;

    public ADVObject() {
    }

    public ADVObject(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("adv_id");
        } catch (JSONException e) {

        }
        try {
            this.image = jsonObject.getString("adv_img");
        } catch (JSONException e) {

        }
    }

    public ADVObject(String id, String image) {
        this.id = id;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public String getImageFullLink() {
        return Config.CMS_URL + "/cms/upload_files/" +image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
