package neublick.locatemylot.model;

/**
 * Created by theptokim on 10/24/16.
 */
public class ShareLocationHint {
    private String hint;
    private int count;

    public ShareLocationHint(String hint, int count) {
        this.hint = hint;
        this.count = count;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
