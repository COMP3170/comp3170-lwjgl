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

public class Shader {

	private int program;
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
