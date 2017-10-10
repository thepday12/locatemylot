package neublick.locatemylot.util.my_dijkstra;

import static android.R.attr.id;

/**
 * Created by theptokim on 10/9/17.
 */

public class Edge  {
    private final Vertex source;
    private final Vertex destination;
    private final double weight;

    public Edge(Vertex source, Vertex destination) {
        this.source = source;
        this.destination = destination;
        this.weight = Math.sqrt(Math.pow(source.getX()-destination.getX(),2)+Math.pow(source.getY()- destination.getY(),2));
    }


    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }


}
