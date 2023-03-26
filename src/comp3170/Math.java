package comp3170;

public class Math {

	public static float TAU = (float) (2 * java.lang.Math.PI);		// https://tauday.com/tau-manifesto

	/**
	 * Generate a random float between the specified minumum and maximum bounds
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static float random(float min, float max) {
		return (float) (java.lang.Math.random() * (max - min) + min);
	}

	/**
	 * Linearly interpolate between the specified minumum and maximum bounds.
	 * 
	 * @param min
	 * @param max
	 * @param t 	 
	 * @return
	 */
	public static float lerp(float min, float max, float t) {
		return min + t * (max - min);
	}
	
}
