package neublick.locatemylot.djikstra;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

import neublick.locatemylot.app.Config;

public class Djikstra {

    public  void computePaths(Vertex source) {
        source.minDistance = 0.;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>(Config.COLLECTION_INITIAL_CAPACITY);
      	vertexQueue.add(source);
		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();
			
            // Visit each edge exiting u
            for (Edge e : u.adjacencies) {
                Vertex v = e.target;
                double weight = e.weight;
                double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					vertexQueue.remove(v);
					v.minDistance = distanceThroughU ;
					v.previous = u;
					vertexQueue.add(v);
				}
            }
        }

    }

    // ham nay co the tra ve emptyList, neu khong co Vertex nao?!
    public  List<Vertex> getShortestPathTo(Vertex target) {
        List<Vertex> path = new ArrayList<Vertex>(Config.COLLECTION_INITIAL_CAPACITY);
        int count = 0;
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous) {
            ++count;
            le("memory_count = " + count);
            path.add(vertex);
        }
        return path;
    }

    static void le(String s) {
        final String TAG = Djikstra.class.getSimpleName();
        android.util.Log.e(TAG, s);
    }
}