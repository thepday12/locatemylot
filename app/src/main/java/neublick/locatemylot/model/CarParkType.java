package neublick.locatemylot.model;

/**
 * Created by theptokim on 1/5/17.
 */

public class CarParkType {
    private int id;
    private int buttonId;
    private String name;

    public CarParkType(int id, int buttonId, String name) {
        this.id = id;
        this.buttonId = buttonId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getButtonId() {
        return buttonId;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
