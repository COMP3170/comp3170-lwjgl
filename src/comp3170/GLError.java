package comp3170;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

@SuppressWarnings("serial")
public class GLError extends Exception {

	private int glError;
	
	public GLError(int glError, GLError suppressed) {
		super(String.format("GLError: %d\n", glError));	// TODO: Show error message
		this.glError = glError;
		if (suppressed != null) {
			this.addSuppressed(suppressed);
		}
	}
	
	public GLError(String message) {
		super(message);
		this.glError = 0;
	}
	
	public int getErrorCode() {
		return this.glError;
	}

	public static void checkGLErrors() throws GLError  {
		
		GLError e = null;
		
		int error = glGetError();

		while (error != GL_NO_ERROR) {
			e = new GLError(error, e);
			error = glGetError();
		}
		
		if (e != null) {
			throw e;
		}
	}

}
