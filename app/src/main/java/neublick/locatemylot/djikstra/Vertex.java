package neublick.locatemylot.djikstra;

import java.util.List;

import neublick.locatemylot.ui.MapView;

public class Vertex implements Comparable<Vertex> {

    // location
    public Float x;
    public Float y;

    // identifier
    public Integer id = Integer.MIN_VALUE;
    public Integer carparkId = Integer.MIN_VALUE;

    // id:carparkId, for instance 10:0
    public String label;

    // cac dinh gan ke no
    public List<Edge> adjacencies;

    public String adjacenciesString = "";
    public String floor = "";

    public double minDistance = Double.POSITIVE_INFINITY;

    public int hashCode = Integer.MIN_VALUE;

    public Vertex previous;

    public double distanceToCarObject = -1;
    public double distanceToUserObject = -1;

    public Vertex() {
    }

    public Vertex(String argLabel, float argX, float argY) {
        label = argLabel;
        x = argX;
        y = argY;
    }

    public Vertex(String argLabel, float argX, float argY, int argId, int argCarparkId) {
        label = argLabel;
        x = argX;
        y = argY;
        id = argId;
        carparkId = argCarparkId;
    }

    @Override
    public String toString() {
        if (label == null) {
            label = String.format("[id:%d,carparkId:%d]", id, carparkId);
        }
        return label;
    }

    @Override
    public int compareTo(Vertex other) {
        return Double.compare(minDistance, other.minDistance);
    }

    @Override
    public boolean equals(Object o) {
        // Return true if the objects are identical.
        // (This is just an optimization, not required for correctness.)
        if (this == o) {
            return true;
        }

        // Return false if the other object has the wrong type.
        // This type may be an interface depending on the interface's specification.
        if (!(o instanceof Vertex)) {
            return false;
        }

        Vertex obj = (Vertex) o;
        return (id == obj.id && carparkId == obj.carparkId);
    }

    @Override
    public int hashCode() {
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = 17;
            hashCode = 31 * hashCode + id;
            hashCode = 31 * hashCode + carparkId;
        }
        return hashCode;
    }

    // =====================================================================
    // calculate DISTANCE from this to the CAR object
    public void calculateDistanceToCarObject(Vertex carObject) {
        distanceToCarObject = Math.sqrt(Math.pow(x - carObject.x, 2) + Math.pow(y - carObject.y, 2));
    }

    // calculate DISTANCE from this to the USER object
    public void calculateDistanceToUserObject(Vertex userObject) {
        distanceToUserObject = Math.sqrt(Math.pow(x - userObject.x, 2) + Math.pow(y - userObject.y, 2));
    }

    // =====================================================================
    public void calculateDistanceToCarObject(MapView.ObjectOverlap carObject) {
        distanceToCarObject = Math.sqrt(
                Math.pow(x - carObject.location.originalX, 2) +
                        Math.pow(y - carObject.location.originalY, 2)
        );
    }

    public void calculateDistanceDestination(float destinationX, float destinationY) {
        distanceToCarObject = Math.sqrt(
                Math.pow(x - destinationX, 2) +
                        Math.pow(y - destinationY, 2)
        );
    }

    public void calculateDistanceToUserObject(MapView.ObjectOverlap userObject) {
        distanceToUserObject = Math.sqrt(
                Math.pow(x - userObject.location.originalX, 2) +
                        Math.pow(y - userObject.location.originalY, 2)
        );
    }

    public void calculateDistanceToUserObject(float destinationX, float destinationY) {
        distanceToUserObject = Math.sqrt(
                Math.pow(x - destinationX, 2) +
                        Math.pow(y - destinationY, 2)
        );
    }
    // ======================================================================
}