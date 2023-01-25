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
	 * Resize the canvas. Called when the window has been resized.
	 * 
	 * @param width	Canvas width in pixels
	 * @param height Canvas height in pixels
	 */
	public void resize(int width, int height);
	
	/**
	 * Close the scene. Called when the window is closed.
	 */

	public void close();


}
