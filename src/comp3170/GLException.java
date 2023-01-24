package comp3170;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;


@SuppressWarnings("serial")
public class GLException extends Exception {

	private int glError;
	
	public GLException(int glError, GLException suppressed) {
		super(String.format("GLError: %d\n", glError));	// TODO: Show error message
		this.glError = glError;
		if (suppressed != null) {
			this.addSuppressed(suppressed);
		}
	}
	
	public GLException(String message) {
		super(message);
		this.glError = 0;
	}
	
	public int getErrorCode() {
		return this.glError;
	}

	public static void checkGLErrors() throws GLException  {
		
		GLException e = null;
		
		int error = glGetError();

		while (error != GL_NO_ERROR) {
			e = new GLException(error, e);
			error = glGetError();
		}
		
		if (e != null) {
			throw e;
		}
	}

}
