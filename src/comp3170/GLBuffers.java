package comp3170;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT4;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC4;
import static org.lwjgl.opengl.GL41.*;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

/**
 * Version 2023.2
 * 
 * 2023.2: Added code for framebuffers
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
	 * @param data The data as a FloatBuffer
	 * @param type The type of data in this buffer
	 * @return	The OpenGL handle to the VBO
	 */
	
	static public int createBuffer(FloatBuffer data, int type) {
		int bufferID = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, bufferID);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

		bufferTypes.put(bufferID, type);

		if (data.capacity() % GLTypes.typeSize(type) != 0) {
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
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(2 * data.length);
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(i*2, floatBuffer);
		}
		
		return createBuffer(floatBuffer, GL_FLOAT_VEC2);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Vector3f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Vector3f[] data) {
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(3 * data.length);

		for (int i = 0; i < data.length; i++) {
			data[i].get(i*3, floatBuffer);
		}

		return createBuffer(floatBuffer, GL_FLOAT_VEC3);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it. 
	 * 
	 * @param data The data as an array of Vector4f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Vector4f[] data) {
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4 * data.length);

		for (int i = 0; i < data.length; i++) {
			data[i].get(i*4, floatBuffer);
		}

		return createBuffer(floatBuffer, GL_FLOAT_VEC4);
	}

	/**
	 * Create a new VBO (vertex buffer object) in graphics memory and copy data into
	 * it
	 * 
	 * @param data The data as an array of Matrix3f
	 * @return	The OpenGL handle to the VBO
	 */
	static public int createBuffer(Matrix3f[] data) {
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

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		bufferTypes.put(bufferID, GL_INT);

		return bufferID;
	}

	/**
	 * Update the contents of a VBO
	 * 
	 * @param buffer The OpenGL handle to the VBO
	 * @param type The type of data in this buffer
	 * @return	The OpenGL handle to the VBO
	 */
	
	static public void updateBuffer(int buffer, float[] data, int type) {
		GLBuffers.checkType(buffer, type);

		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);

		if (data.length % GLTypes.typeSize(type) != 0) {
			System.err.println(
					String.format("Warning: buffer of type %s has length which is not a mutliple of %d.",
					GLTypes.typeName(type), GLTypes.typeSize(type)));
		}

	}

	/**
	 * Update the contents of a VBO
	 * 
	 * @param buffer The OpenGL handle to the VBO
	 * @param type The type of data in this buffer
	 * @return	The OpenGL handle to the VBO
	 */
	
	static public void updateBuffer(int buffer, FloatBuffer data, int type) {
		GLBuffers.checkType(buffer, type);

		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);

		if (data.capacity() % GLTypes.typeSize(type) != 0) {
			System.err.println(
					String.format("Warning: buffer of type %s has length which is not a mutliple of %d.",
					GLTypes.typeName(type), GLTypes.typeSize(type)));
		}

	}
	
	/**
	 * Update the contents of a VBO
	 * 
	 * @param buffer The OpenGL handle to the VBO
	 * @param data The data as an array of Vector2f
	 */
	static public void updateBuffer(int buffer, Vector2f[] data) {
		int size = 2;
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(size * data.length);
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(i*size, floatBuffer);
		}
		
		updateBuffer(buffer, floatBuffer, GL_FLOAT_VEC2);
	}

	/**
	 * Update the contents of a VBO
	 * 
	 * @param buffer The OpenGL handle to the VBO
	 * @param data The data as an array of Vector3f
	 */
	static public void updateBuffer(int buffer, Vector3f[] data) {
		int size = 3;
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(size * data.length);
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(i*size, floatBuffer);
		}
		
		updateBuffer(buffer, floatBuffer, GL_FLOAT_VEC3);
	}

	/**
	 * Update the contents of a VBO
	 * 
	 * @param buffer The OpenGL handle to the VBO
	 * @param data The data as an array of Vector4f
	 */
	static public void updateBuffer(int buffer, Vector4f[] data) {
		int size = 4;
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(size * data.length);
		
		for (int i = 0; i < data.length; i++) {
			data[i].get(i*size, floatBuffer);
		}
		
		updateBuffer(buffer, floatBuffer, GL_FLOAT_VEC4);
	}
	

	/**
	 * Create a framebuffer that writes colours to the renderTexture given.
	 * 
	 * @param renderTexture	A render texture in which to store the colour buffer
	 * @return	The OpenGL handle to the frame buffer
	 * @throws OpenGLException 
	 * @throws GLException
	 */
	
	public static int createFrameBuffer(int renderTexture) throws OpenGLException {
		int[] width = new int[1];
		int[] height = new int[1];
		glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH, width);
		glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT, height);
		
		int framebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);		
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, renderTexture, 0);
		
		int depthrenderbuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width[0], height[0]);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthrenderbuffer);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			OpenGLException.checkError();
			throw new OpenGLException("Failed to create framebuffer");
		}

		return framebuffer;
	}

}
