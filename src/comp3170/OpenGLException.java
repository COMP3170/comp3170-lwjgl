package comp3170;

import org.lwjgl.glfw.GLFWErrorCallback;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;

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
	
	public int getErrorCode() {
		return this.glError;
	}
}
