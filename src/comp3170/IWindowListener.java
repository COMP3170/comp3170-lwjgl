package comp3170;

import org.lwjgl.opengl.GLCapabilities;

public interface IWindowListener {

	/**
	 * Initialise the scene. Called once when the Window is first created
	 * @param capabilities The capabilities for the current GL implementation
	 */
	
	public void init(GLCapabilities capabilities);

	/**
	 * Draw the scene. Called once every frame.
	 */

	public void draw();

	/**
	 * Reshape the canvas. Called when the window has been resized.
	 */

	public void reshape();
	
	/**
	 * Close the scene. Called when the window is closed.
	 */

	public void close();

}
