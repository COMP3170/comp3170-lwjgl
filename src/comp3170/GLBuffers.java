package comp3170;

import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT4;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC4;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Version 2023.1
 * 
 * 2023.1: Rewrote code to work using LWJGL
 * 2022.1: Factored buffer code out of Shader to allow shaders to share buffers
 * 
 * @author Malcolm Ryan
 */

public class GLBuffers {
	static private Map<Integer, Integer> bufferTypes = new HashMap<Integer,Integer>();

	/**
	 * Get the GL type of a buffer.
	 * @param buffer	An allocated buffer
	 * @return the type value
	 * @throws IllegalArgumentException if the buffer has not been allocated.
	 */
	
	static public int getType(int buffer) {
		if (bufferTypes.containsKey(buffer)) {
			return bufferTypes.get(buffer);
		}
		
		throw new IllegalArgumentException(String.format("Buffer %d has not been allocated.", buffer));
	}
	
	/**
	 * Check the type of a buffer matches the expected type
	 * @param buffer	An allocated buffer
	 * @param type		The expected type
	 * @return true if the buffer type matches the expected type
	 * @throws IllegalArgumentException if the types do not match.
	 */
	static public boolean checkType(int buffer, int type) {
		if (GLBuffers.getType(buffer) != type) {
			throw new IllegalArgumentException(String.format("Expected buffer of type %s, got %s.", 
					GLTypes.typeName(type),
					GLTypes.typeName(GLBuffers.getType(buffer))));
		}

		return true;
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of floats
	 * @param type The type of data in this buffer
	 * @return	The OpenGL handle to the VBO
	 */
	
	static public int createBuffer(float[] data, int type) {
		int bufferID = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, bufferID);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

		bufferTypes.put(bufferID, type);

		if (data.length % GLTypes.typeSize(type) != 0) {
			System.err.println(
					String.format("Warning: buffer of type %s has length which is not a mutliple of %d.",
					GLTypes.typeName(type), GLTypes.typeSize(type)));
		}

		return bufferID;
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Vector2f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Vector2f[] data) {
		// this is a hack, but I can't get it to work otherwise
		float[] array = new float[2 * data.length];
		int j = 0;
		for (int i = 0; i < data.length; i++) {
			array[j++] = data[i].x;
			array[j++] = data[i].y;
		}

		return createBuffer(array, GL_FLOAT_VEC2);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Vector3f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Vector3f[] data) {
		// this is a hack, but I can't get it to work otherwise
		float[] array = new float[3 * data.length];
		int j = 0;
		for (int i = 0; i < data.length; i++) {
			array[j++] = data[i].x;
			array[j++] = data[i].y;
			array[j++] = data[i].z;
		}

		return createBuffer(array, GL_FLOAT_VEC3);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it. 
	 * 
	 * @param data The data as an array of Vector4f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Vector4f[] data) {
		// this is a hack, but I can't get it to work otherwise
		float[] array = new float[4 * data.length];
		int j = 0;
		for (int i = 0; i < data.length; i++) {
			array[j++] = data[i].x;
			array[j++] = data[i].y;
			array[j++] = data[i].z;
			array[j++] = data[i].w;
		}

		return createBuffer(array, GL_FLOAT_VEC4);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Matrix3f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Matrix3f[] data) {
		// this is a hack, but I can't get it to work otherwise
		float[] array = new float[9 * data.length];
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(array, 9 * i);
		}

		return createBuffer(array, GL_FLOAT_MAT3);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Matrix4f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Matrix4f[] data) {
		// this is a hack, but I can't get it to work otherwise
		float[] array = new float[16 * data.length];
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(array, 16 * i);
		}

		return createBuffer(array, GL_FLOAT_MAT4);
	}
	
	/**
	 * Create a new index buffer and initialise it
	 * 
	 * @param indices The indices as an array of ints
	 * @return	The OpenGL handle to the index buffer
	 */
	static public int createIndexBuffer(int[] indices) {
		int bufferID = glGenBuffers();

		IntBuffer intBuffer = createIntBuffer(indices.length);
		intBuffer.put(indices);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL_STATIC_DRAW);

		bufferTypes.put(bufferID, GL_INT);

		return bufferID;
	}
	
}
