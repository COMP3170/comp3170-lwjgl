package comp3170;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL41.*;

public class Shader {

	private int program;

	public Shader(File vertexShaderFile, File fragmentShaderFile) throws IOException, GLException {

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

//		recordAttributes();
//		recordUniforms();
	}

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

	private static int compileShader(int type, File sourceFile) throws IOException, GLException {
		String[] source = readSource(sourceFile);

		int shader = glCreateShader(type);
		GLException.checkGLErrors();
		glShaderSource(shader, source);
		glCompileShader(shader);
		GLException.checkGLErrors();	

		// check compilation

		int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);

		if (compiled != 1) {
			int len = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
			String logString = glGetShaderInfoLog(shader, len);

			// delete the shader if the compilation failed
			glDeleteShader(shader);

			String message = String.format("%s: %s compilation error\n%s", sourceFile.getName(), shaderType(type),
					logString);
			throw new GLException(message);
		}

		return shader;
	}

	private static int linkShaders(int vertexShader, int fragmentShader) throws GLException {
		int program = glCreateProgram();
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);
		GLException.checkGLErrors();

		// check for linker errors

		int linked = glGetProgrami(program, GL_LINK_STATUS);

		if (linked != 1) {
			int len = glGetProgrami(program, GL_INFO_LOG_LENGTH);
			String err = glGetProgramInfoLog(program, len);
			String message = String.format("Link failed: %s\n", err);
			throw new GLException(message);
		}

		return program;
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
