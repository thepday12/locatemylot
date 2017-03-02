package neublick.locatemylot.djikstra;

// dai dien cho 1 vector
public class Edge {
	public final Vertex target;
	public final double weight;
	public String toString;
	
	public Edge(Vertex argSource, Vertex argTarget) {
		target = argTarget;
		weight = Math.sqrt(
			Math.pow(target.x - argSource.x, 2) + Math.pow(target.y - argSource.y, 2)			
		);
		toString = String.format("(%s,%s)", argSource.toString(), argTarget.toString());
	}

	@Override public String toString() {
		return toString;
	}
}