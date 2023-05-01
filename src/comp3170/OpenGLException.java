package comp3170;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL41.*;

import org.lwjgl.glfw.GLFWErrorCallback;

@SuppressWarnings("serial")
public class OpenGLException extends Exception {

	private int glError;
	
	public OpenGLException(int glError, long description) {
		super(String.format("OpenGLException: %s\n", GLFWErrorCallback.getDescription(description)));	
		this.glError = glError;
	}
	
	public OpenGLException(String message) {
		super(message);
		this.glError = GL_NO_ERROR;
	}
	
	public OpenGLException(int glError) {
		super(glErrorDescription(glError));
		this.glError = glError;
	}

	public int getErrorCode() {
		return this.glError;
	}
	
	public static String glErrorDescription(int error) {
		switch (error) {
			
		case GL_NO_ERROR:
			return "GL_NO_ERROR: No error has been recorded.";
		case GL_INVALID_ENUM:
			return "GL_INVALID_ENUM: An unacceptable value is specified for an enumerated argument.";
		case GL_INVALID_VALUE:
			return "GL_INVALID_VALUE: A numeric argument is out of range.";			
		case GL_INVALID_OPERATION:
			return "GL_INVALID_OPERATION: The specified operation is not allowed in the current state.";
		case GL_INVALID_FRAMEBUFFER_OPERATION:
			return "GL_INVALID_FRAMEBUFFER_OPERATION: The framebuffer object is not complete.";
		case GL_OUT_OF_MEMORY:
			return "GL_OUT_OF_MEMORY: There is not enough memory left to execute the command.";
		case GL_STACK_UNDERFLOW:
			return "GL_STACK_UNDERFLOW: An attempt has been made to perform an operation that would cause an internal stack to underflow.";
		case GL_STACK_OVERFLOW:
			return "GL_STACK_OVERFLOW: An attempt has been made to perform an operation that would cause an internal stack to overflow.";
		default:
			return String.format("An unknown error (%d) occured.", error);
		}
	}

	public static void checkError() throws OpenGLException {
		int error = glGetError();
		if (error != GL_NO_ERROR) {
			throw new OpenGLException(error);
		}
	}
}
