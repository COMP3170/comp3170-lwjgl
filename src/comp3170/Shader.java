package comp3170;

import static org.lwjgl.BufferUtils.createFloatBuffer;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTES;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH;
import static org.lwjgl.opengl.GL20.GL_BOOL;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_MAT4;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC2;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC3;
import static org.lwjgl.opengl.GL20.GL_FLOAT_VEC4;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_INT_VEC2;
import static org.lwjgl.opengl.GL20.GL_INT_VEC3;
import static org.lwjgl.opengl.GL20.GL_INT_VEC4;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_SAMPLER_2D;
import static org.lwjgl.opengl.GL20.GL_SAMPLER_CUBE;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetActiveAttrib;
import static org.lwjgl.opengl.GL20.glGetActiveUniform;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform2fv;
import static org.lwjgl.opengl.GL20.glUniform2i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniform3i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniform4i;
import static org.lwjgl.opengl.GL20.glUniformMatrix2fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT_VEC2;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT_VEC3;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_INT_VEC4;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glUniform1ui;
import static org.lwjgl.opengl.GL30.glUniform2ui;
import static org.lwjgl.opengl.GL30.glUniform3ui;
import static org.lwjgl.opengl.GL30.glUniform4ui;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

/**
 * Version 2023.1
 * 
 * 2023.1: Rewrote code to work in LWJGL 3 2022.1: Factored into Shader,
 * GLBuffers, and GLTypes to allow shaders to share buffers
 * 
 * @author Malcolm Ryan
 */

public class Shader {

	private int program;
	private boolean strict = true;
	private HashMap<String, Integer> attributes;
	private HashMap<String, Integer> attributeTypes;
	private HashMap<String, Boolean> trackedAttributeErrors;
	private HashMap<String, Integer> uniforms;
	private HashMap<String, Integer> uniformTypes;
	private HashMap<String, Boolean> trackedUniformErrors;

	private FloatBuffer matrix2Buffer = createFloatBuffer(4);
	private FloatBuffer matrix3Buffer = createFloatBuffer(9);
	private FloatBuffer matrix4Buffer = createFloatBuffer(16);

	private FloatBuffer vector2Buffer = createFloatBuffer(2);
	private FloatBuffer vector3Buffer = createFloatBuffer(3);
	private FloatBuffer vector4Buffer = createFloatBuffer(4);

	public Shader(File vertexShaderFile, File fragmentShaderFile) throws IOException, OpenGLException {

		if (vertexShaderFile == null || fragmentShaderFile == null) {
			throw new NullPointerException("Shader files must be non-null.");
		}

		// compile the shaders

		int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderFile);
		int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderFile);

		// link the shaders

		program = linkShaders(vertexShader, fragmentShader);

		// delete the shaders after linking

		glDetachShader(this.program, vertexShader);
		glDetachShader(this.program, fragmentShader);
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		
		// generate VAO (not used)
		// this is needed to make anything show on screen
	 	
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);

		// record attribute and uniforms

		recordAttributes();
		recordUniforms();
	}

	/**
	 * Check whether uniform & attribute names are being enforced.
	 * 
	 * @return true if names are being enforced
	 */
	public boolean isStrict() {
		return strict;
	}

	/**
	 * Set whether uniform & attribute names are being enforced.
	 * 
	 * If true, incorrect uniform and attribute names will throw exceptions. If
	 * false, incorrect uniform and attribute names will print a message to the
	 * console and continue.
	 * 
	 * @param strict
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * Enable the shader
	 */

	public void enable() {
		glUseProgram(program);
//		glBindVertexArray(this.vao);
	}

	//
	// Shader compilation
	//

	/**
	 * Read source code from a shader file.
	 * 
	 * @param shaderFile
	 * @return
	 * @throws IOException
	 */
	private static String[] readSource(File shaderFile) throws IOException {
		ArrayList<String> source = new ArrayList<String>();
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(shaderFile));

			for (String line = in.readLine(); line != null; line = in.readLine()) {
				source.add(line + "\n");
			}

		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}

		String[] lines = new String[source.size()];
		return source.toArray(lines);
	}

	private static int compileShader(int type, File sourceFile) throws IOException, OpenGLException {
		String[] source = readSource(sourceFile);

		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);

		// check compilation

		int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);

		if (compiled != 1) {
			int len = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
			String logString = glGetShaderInfoLog(shader, len);

			// delete the shader if the compilation failed
			glDeleteShader(shader);

			String message = String.format("%s: %s compilation error\n%s", sourceFile.getName(), shaderType(type),
					logString);
			throw new OpenGLException(message);
		}

		return shader;
	}

	private static int linkShaders(int vertexShader, int fragmentShader) throws OpenGLException {
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);

		// check for linker errors

		int linked = glGetProgrami(program, GL_LINK_STATUS);

		if (linked != 1) {
			int len = glGetProgrami(program, GL_INFO_LOG_LENGTH);
			String err = glGetProgramInfoLog(program, len);
			String message = String.format("Link failed: %s\n", err);
			throw new OpenGLException(message);
		}

		return program;
	}

	//
	// Uniforms and Attributes
	//

	/**
	 * Establish the mapping from attribute names to IDs
	 */

	private void recordAttributes() {
		attributes = new HashMap<String, Integer>();
		attributeTypes = new HashMap<String, Integer>();
		trackedAttributeErrors = new HashMap<String, Boolean>();

		int activeAttributes = glGetProgrami(program, GL_ACTIVE_ATTRIBUTES);
		int maxNameSize = glGetProgrami(program, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH);

		// Use stack buffers for "any small buffer/struct allocation that is
		// shortly-lived"
		// https://blog.lwjorg/memory-management-in-lwjgl-3/

		try (MemoryStack stack = stackPush()) {
			IntBuffer sizeBuffer = stack.mallocInt(1);
			IntBuffer typeBuffer = stack.mallocInt(1);

			for (int i = 0; i < activeAttributes; ++i) {
				String name = glGetActiveAttrib(program, i, maxNameSize, sizeBuffer, typeBuffer);

				attributes.put(name, glGetAttribLocation(program, name));
				attributeTypes.put(name, typeBuffer.get(0));
			}
		}
	}

	/**
	 * Establish the mapping from uniform names to IDs
	 * 
	 * @param trackedUniformErrors
	 */

	private void recordUniforms() {
		uniforms = new HashMap<String, Integer>();
		uniformTypes = new HashMap<String, Integer>();
		trackedUniformErrors = new HashMap<String, Boolean>();

		int activeUniforms = glGetProgrami(program, GL_ACTIVE_UNIFORMS);
		int maxNameSize = glGetProgrami(program, GL_ACTIVE_UNIFORM_MAX_LENGTH);

		// Use stack buffers for "any small buffer/struct allocation that is
		// shortly-lived"
		// https://blog.lwjorg/memory-management-in-lwjgl-3/
		try (MemoryStack stack = stackPush()) {
			IntBuffer sizeBuffer = stack.mallocInt(1);
			IntBuffer typeBuffer = stack.mallocInt(1);

			for (int i = 0; i < activeUniforms; ++i) {
				String name = glGetActiveUniform(program, i, maxNameSize, sizeBuffer, typeBuffer);

				uniforms.put(name, glGetUniformLocation(program, name));
				uniformTypes.put(name, typeBuffer.get(0));
			}
		}
	}

	/**
	 * Check if the shader has a particular attribute
	 * 
	 * @param name The name of the attribute
	 * @return true if the shader has an attribute with the name provided
	 */

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	/**
	 * Check if the shader has a particular uniform
	 * 
	 * @param name The name of the uniform
	 * @return true if the shader has a uniform with the name provided
	 */

	public boolean hasUniform(String name) {
		return uniforms.containsKey(name);
	}

	/**
	 * Get the handle for an attribute
	 * 
	 * @param name The name of the attribute
	 * @return The OpenGL handle for the attribute
	 */

	public int getAttribute(String name) {
		if (!attributes.containsKey(name)) {
			String message = String.format("Unknown attribute: '%s'", name);
			if (strict) {
				throw new IllegalArgumentException(message);
			} else if (!this.trackedAttributeErrors.containsKey(name)) {
				System.err.println(message);
				this.trackedAttributeErrors.put(name, true);
			}
			return -1;
		}

		return attributes.get(name);
	}

	/**
	 * Get the handle for a uniform
	 * 
	 * @param name The name of the uniform
	 * @return The OpenGL handle for the uniform
	 */

	public int getUniform(String name) {
		if (!uniforms.containsKey(name)) {
			String message = String.format("Unknown uniform: '%s'", name);
			if (strict) {
				throw new IllegalArgumentException(message);
			} else if (!trackedUniformErrors.containsKey(name)) {
				System.err.println(message);
				this.trackedUniformErrors.put(name, true);
			}
			return -1;
		}

		return uniforms.get(name);
	}

	/**
	 * Connect a buffer to a shader attribute
	 * 
	 * @param attributeName The name of the shader attribute
	 * @param buffer        The buffer
	 */
	public void setAttribute(String attributeName, int buffer) {
		int attribute = getAttribute(attributeName);
		if (attribute < 0)
			return;

		int type = attributeTypes.get(attributeName);
		GLBuffers.checkType(buffer, type);

		int size = GLTypes.typeSize(type);
		int elementType = GLTypes.elementType(type);

		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		if (size <= 4) {
			glVertexAttribPointer(attribute, size, elementType, false, 0, 0);			
		}
		else {
			// mat3 = 3 x vec3, mat4 = 4 x vec4
			int stride = GLTypes.stride(type);
			int n = (size-1) / stride;
			for (int i = 0; i < n; i++) {
				glVertexAttribPointer(attribute+i, stride, elementType, false, stride, 0);							
			}
		}
		glEnableVertexAttribArray(attribute);
	}

	
	/**
	 * Set the value of a uniform to a boolean
	 * 
	 * @param uniformName The GLSL uniform
	 * @param value       The int value
	 */
	public void setUniform(String uniformName, boolean value) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		switch (type) {
		case GL_BOOL:
			glUniform1ui(uniform, value ? 1 : 0);
			break;
		default:
			throw new IllegalArgumentException(String.format("Expected %s got boolean", GLTypes.typeName(type)));
		}
	}

	/**
	 * Set the value of a uniform to an int
	 * 
	 * @param uniformName The GLSL uniform
	 * @param value       The int value
	 */
	public void setUniform(String uniformName, int value) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		switch (type) {
		case GL_UNSIGNED_INT:
			glUniform1ui(uniform, value);
			break;
		case GL_INT:
		case GL_SAMPLER_2D:
		case GL_SAMPLER_CUBE:
			glUniform1i(uniform, value);
			break;
		default:
			throw new IllegalArgumentException(String.format("Expected %s got int", GLTypes.typeName(type)));
		}
	}

	/**
	 * Set the value of a uniform to a float
	 * 
	 * @param uniformName The GLSL uniform
	 * @param value       The float value
	 */
	public void setUniform(String uniformName, float value) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT) {
			throw new IllegalArgumentException(String.format("Expected %s got float", GLTypes.typeName(type)));
		}

		glUniform1f(uniform, value);
	}

	/**
	 * Set the value of a uniform to an array of int
	 * 
	 * This works for GLSL types float, vec2, vec3, vec4, mat2, mat3 and mat4.
	 * 
	 * Note that for matrix types, the elements should be specified in column order
	 * 
	 * @param uniformName
	 * @param value
	 */
	public void setUniform(String uniformName, int[] value) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);
		int expectedArgs = GLTypes.typeSize(type);

		if (value.length != expectedArgs) {
			throw new IllegalArgumentException(
					String.format("Expected %s got int[%d]", GLTypes.typeName(type), value.length));
		}

		switch (type) {
		case GL_INT:
			glUniform1i(uniform, value[0]);
			break;
		case GL_INT_VEC2:
			glUniform2i(uniform, value[0], value[1]);
			break;
		case GL_INT_VEC3:
			glUniform3i(uniform, value[0], value[1], value[2]);
			break;
		case GL_INT_VEC4:
			glUniform4i(uniform, value[0], value[1], value[2], value[4]);
			break;
		case GL_UNSIGNED_INT:
			glUniform1ui(uniform, value[0]);
			break;
		case GL_UNSIGNED_INT_VEC2:
			glUniform2ui(uniform, value[0], value[1]);
			break;
		case GL_UNSIGNED_INT_VEC3:
			glUniform3ui(uniform, value[0], value[1], value[2]);
			break;
		case GL_UNSIGNED_INT_VEC4:
			glUniform4ui(uniform, value[0], value[1], value[2], value[4]);
			break;
		default:
			throw new IllegalArgumentException(String.format("Cannot convert int array to %s", GLTypes.typeName(type)));
		}
	}

	/**
	 * Set the value of a uniform to an array of floats
	 * 
	 * This works for GLSL types float, vec2, vec3, vec4, mat2, mat3 and mat4.
	 * 
	 * Note that for matrix types, the elements should be specified in column order
	 * 
	 * @param uniformName
	 * @param value
	 */
	public void setUniform(String uniformName, float[] value) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);
		int expectedArgs = GLTypes.typeSize(type);

		if (value.length != expectedArgs) {
			throw new IllegalArgumentException(
					String.format("Expected %s got float[%d]", GLTypes.typeName(type), value.length));
		}

		switch (type) {
		case GL_FLOAT:
			glUniform1f(uniform, value[0]);
			break;
		case GL_FLOAT_VEC2:
			glUniform2f(uniform, value[0], value[1]);
			break;
		case GL_FLOAT_VEC3:
			glUniform3f(uniform, value[0], value[1], value[2]);
			break;
		case GL_FLOAT_VEC4:
			glUniform4f(uniform, value[0], value[1], value[2], value[3]);
			break;
		case GL_FLOAT_MAT2:
			glUniformMatrix2fv(uniform, false, value);
			break;
		case GL_FLOAT_MAT3:
			glUniformMatrix3fv(uniform, false, value);
			break;
		case GL_FLOAT_MAT4:
			glUniformMatrix4fv(uniform, false, value);
			break;
		default:
			throw new IllegalArgumentException(
					String.format("Cannot convert float array to %s", GLTypes.typeName(type)));

		}

	}

	/**
	 * Set a uniform of type vec2 to a Vector2f value
	 * 
	 * @param uniformName the uniform to set
	 * @param vector      the vector value to send
	 */

	public void setUniform(String uniformName, Vector2f vector) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_VEC2) {
			throw new IllegalArgumentException(String.format("Expected %s got Vector2f", GLTypes.typeName(type)));
		}

		glUniform2fv(uniform, vector.get(vector2Buffer));
	}

	/**
	 * Set a uniform of type vec3 to a Vector3f value
	 * 
	 * @param uniformName the uniform to set
	 * @param vector      the vector value to send
	 */

	public void setUniform(String uniformName, Vector3f vector) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_VEC3) {
			throw new IllegalArgumentException(String.format("Expected %s got Vector3f", GLTypes.typeName(type)));
		}

		glUniform3fv(uniform, vector.get(vector3Buffer));
	}

	/**
	 * Set a uniform of type vec4 to a Vector4f value
	 * 
	 * @param uniformName the uniform to set
	 * @param vector      the vector value to send
	 */

	public void setUniform(String uniformName, Vector4f vector) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_VEC4) {
			throw new IllegalArgumentException(String.format("Expected %s got Vector4f", GLTypes.typeName(type)));
		}

		glUniform4fv(uniform, vector.get(vector4Buffer));
	}

	/**
	 * Set a uniform of type mat2 to a Matrix2f value
	 * 
	 * @param uniformName the uniform to set
	 * @param matrix      the matrix value to send
	 */

	public void setUniform(String uniformName, Matrix2f matrix) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_MAT2) {
			throw new IllegalArgumentException(String.format("Expected %s got Matrix2f", GLTypes.typeName(type)));
		}

		glUniformMatrix2fv(uniform, false, matrix.get(matrix2Buffer));
	}

	/**
	 * Set a uniform of type mat3 to a Matrix3f value
	 * 
	 * @param uniformName the uniform to set
	 * @param matrix      the matrix value to send
	 */

	public void setUniform(String uniformName, Matrix3f matrix) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_MAT3) {
			throw new IllegalArgumentException(String.format("Expected %s got Matrix3f", GLTypes.typeName(type)));
		}

		glUniformMatrix3fv(uniform, false, matrix.get(matrix3Buffer));
	}

	/**
	 * Set a uniform of type mat4 to a Matrix4 value
	 * 
	 * @param uniformName the uniform to set
	 * @param matrix      the matrix value to send
	 */

	public void setUniform(String uniformName, Matrix4f matrix) {
		int uniform = getUniform(uniformName);
		if (uniform < 0)
			return;

		int type = uniformTypes.get(uniformName);

		if (type != GL_FLOAT_MAT4) {
			throw new IllegalArgumentException(String.format("Expected %s got Matrix4f", GLTypes.typeName(type)));
		}

		glUniformMatrix4fv(uniform, false, matrix.get(matrix4Buffer));
	}

	
	/**
	 * Turn a shader type constant into a descriptive string.
	 * 
	 * @param type
	 * @return
	 */
	private static String shaderType(int type) {
		switch (type) {
		case GL_VERTEX_SHADER:
			return "Vertex shader";
		case GL_FRAGMENT_SHADER:
			return "Fragment shader";
		}
		return "Unknown shader";
	}

}
