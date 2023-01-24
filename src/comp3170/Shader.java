package comp3170;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.system.MemoryStack;

/**
 * Version 2023.1
 * 
 * 2023.1: Rewrote code to work in LWJGL 3
 * 2022.1: Factored into Shader, GLBuffers, and GLTypes to allow shaders to share buffers
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

	public Shader(File vertexShaderFile, File fragmentShaderFile) throws IOException, GLError {

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
	 * If true, incorrect uniform and attribute names will throw exceptions.
	 * If false, incorrect uniform and attribute names will print a message to the console and continue.
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

	private static int compileShader(int type, File sourceFile) throws IOException, GLError {
		String[] source = readSource(sourceFile);

		int shader = glCreateShader(type);
		GLError.checkGLErrors();
		glShaderSource(shader, source);
		glCompileShader(shader);
		GLError.checkGLErrors();	

		// check compilation

		int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);

		if (compiled != 1) {
			int len = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
			String logString = glGetShaderInfoLog(shader, len);

			// delete the shader if the compilation failed
			glDeleteShader(shader);

			String message = String.format("%s: %s compilation error\n%s", sourceFile.getName(), shaderType(type),
					logString);
			throw new GLError(message);
		}

		return shader;
	}

	private static int linkShaders(int vertexShader, int fragmentShader) throws GLError {
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		GLError.checkGLErrors();

		// check for linker errors

		int linked = glGetProgrami(program, GL_LINK_STATUS);

		if (linked != 1) {
			int len = glGetProgrami(program, GL_INFO_LOG_LENGTH);
			String err = glGetProgramInfoLog(program, len);
			String message = String.format("Link failed: %s\n", err);
			throw new GLError(message);
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

		// Use stack buffers for "any small buffer/struct allocation that is shortly-lived"
		// https://blog.lwjgl.org/memory-management-in-lwjgl-3/
		MemoryStack stack = stackPush();
		IntBuffer sizeBuffer = stack.mallocInt(1);
		IntBuffer typeBuffer = stack.mallocInt(1);

		for (int i = 0; i < activeAttributes; ++i) {
			String name = glGetActiveAttrib(program, i, maxNameSize, sizeBuffer, typeBuffer);

			attributes.put(name, glGetAttribLocation(program, name));
			attributeTypes.put(name, typeBuffer.get(0));
		}
	}

	/**
	 * Establish the mapping from uniform names to IDs
	 * @param trackedUniformErrors 
	 */

	private void recordUniforms() {
		uniforms = new HashMap<String, Integer>();
		uniformTypes = new HashMap<String, Integer>();
		trackedUniformErrors = new HashMap<String, Boolean>();

		int activeUniforms = glGetProgrami(program, GL_ACTIVE_UNIFORMS);
		int maxNameSize = glGetProgrami(program, GL_ACTIVE_UNIFORM_MAX_LENGTH);

		// Use stack buffers for "any small buffer/struct allocation that is shortly-lived"
		// https://blog.lwjgl.org/memory-management-in-lwjgl-3/
		MemoryStack stack = stackPush();
		IntBuffer sizeBuffer = stack.mallocInt(1);
		IntBuffer typeBuffer = stack.mallocInt(1);

		for (int i = 0; i < activeUniforms; ++i) {
			String name = glGetActiveUniform(program, i, maxNameSize, sizeBuffer, typeBuffer);

			uniforms.put(name, glGetUniformLocation(program, name));
			uniformTypes.put(name, typeBuffer.get(0));
		}
	}

	/**
	 * Check if the shader has a particular attribute
	 * 
	 * @param name	The name of the attribute
	 * @return true if the shader has an attribute with the name provided
	 */

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	/**
	 * Check if the shader has a particular uniform
	 * 
	 * @param name	The name of the uniform
	 * @return true if the shader has a uniform with the name provided
	 */

	public boolean hasUniform(String name) {
		return uniforms.containsKey(name);
	}

	/**
	 * Get the handle for an attribute
	 * 
	 * @param name	The name of the attribute
	 * @return	The OpenGL handle for the attribute
	 */

	public int getAttribute(String name) {
		if (!attributes.containsKey(name)) {
			String message = String.format("Unknown attribute: '%s'", name);
			if(strict) {
				throw new IllegalArgumentException(message);
			} else if(!this.trackedAttributeErrors.containsKey(name)) {
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
	 * @param name	The name of the uniform
	 * @return	The OpenGL handle for the uniform 
	 */

	public int getUniform(String name) {
		if (!uniforms.containsKey(name)) {
			String message = String.format("Unknown uniform: '%s'", name);
			if(strict) {
				throw new IllegalArgumentException(message);
			} else if(!trackedUniformErrors.containsKey(name)) {
				System.err.println(message);
				this.trackedUniformErrors.put(name, true);
			}
			return -1;
		}

		return uniforms.get(name);
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
