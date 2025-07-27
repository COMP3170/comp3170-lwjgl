package comp3170;

import java.util.Random;

import org.joml.Vector4f;

public class Math {

	public static float TAU = (float) (2 * java.lang.Math.PI);		// https://tauday.com/tau-manifesto

	private static Random rng = new Random();

	/**
	 * Generate a random float between the specified minimum and maximum bounds
	 *
	 * @param min The minimum value (inclusive)
	 * @param max The maximum value (exclusive)
	 * @return A random float value in the range [min ... max)
	 */
	public static float random(float min, float max) {
		return rng.nextFloat() * (max - min) + min;
	}

	/**
	 * Generate a random int between the specified minimum and maximum bounds
	 *
	 * @param min The minimum value (inclusive)
	 * @param max The maximum value (exclusive)
	 * @return A random integer value in the range [min ... max)
	 */
	public static int randomInt(int min, int max) {
		return rng.nextInt(max - min) + min;
	}

	/**
	 * Linearly interpolate between the specified minimum and maximum bounds.
	 *
	 * @param min	The lower bound (returned when t = 0)
	 * @param max	The upper bound (returned when t = 1)
	 * @param t		The interpolation offset
	 * @return	The interpolated value between min and max
	 */
	public static float lerp(float min, float max, float t) {
		return min + t * (max - min);
	}

	/**
	 * Calculate the cross product of homogeneous 3D vectors a and b.
	 * Store the result in dest.
	 *
	 * @param a The first vector
	 * @param b The second vector
	 * @param dest A pre-allocated vector in which to store the result
	 * @return the destination vector
	 */
	public static Vector4f cross(Vector4f a, Vector4f b, Vector4f dest) {
		return dest.set(
				a.y * b.z - a.z * b.y,
				a.z * b.x - a.x * b.z,
				a.x * b.y - a.y * b.x,
				0);
	}
}
