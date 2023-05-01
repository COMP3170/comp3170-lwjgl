package comp3170;

import java.util.Random;

import org.joml.Vector4f;

public class Math {

	public static float TAU = (float) (2 * java.lang.Math.PI);		// https://tauday.com/tau-manifesto

	private static Random rng = new Random();
	
	/**
	 * Generate a random float between the specified minumum and maximum bounds
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static float random(float min, float max) {
		return rng.nextFloat() * (max - min) + min;
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

	/**
	 * Calculate the cross product of homogeneous vectors a and b.
	 * Store the result in dest.
	 * 
	 * @param a The first vector
	 * @param b The second vector
	 * @param dest A pre-allocated vector in which to store the result
	 * @return
	 */
	public static Vector4f cross(Vector4f a, Vector4f b, Vector4f dest) {
		return dest.set(
				a.y * b.z - a.z * b.y,
				a.z * b.x - a.x * b.z,
				a.x * b.y - a.y * b.x,
				0);
	}
}
