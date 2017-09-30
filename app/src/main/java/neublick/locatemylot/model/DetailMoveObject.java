package neublick.locatemylot.model;

/**
 * Created by theptokim on 9/4/17.
 */

public class DetailMoveObject {
    private String mapName;
    private float startX;
    private float startY;
    private float destinationX;
    private float destinationY;
    private String text;
    private boolean destinationIsCar;



    public DetailMoveObject(String mapName, float startX, float startY, float destinationX, float destinationY, String text, boolean destinationIsCar) {
        this.mapName = mapName;
        this.startX = startX;
        this.startY = startY;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.text = text;
        this.destinationIsCar = destinationIsCar;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getDestinationX() {
        return destinationX;
    }

    public void setDestinationX(float destinationX) {
        this.destinationX = destinationX;
    }

    public float getDestinationY() {
        return destinationY;
    }

    public void setDestinationY(float destinationY) {
        this.destinationY = destinationY;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDestinationIsCar() {
        return destinationIsCar;
    }

    public void setDestinationIsCar(boolean destinationIsCar) {
        this.destinationIsCar = destinationIsCar;
    }
}
