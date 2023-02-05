package comp3170;

public interface IWindowInputListener {

	/**
	 * Respond to a key event
	 * 
	 * @see https://javadoc.lwjgl.org/org/lwjgl/glfw/GLFW
	 * 
	 * @param key		The key code
	 * @param action	The action, one of PRESS, RELEASE or REPEAT
	 * @param mods		The modifier keys also pressed (as a bitfield)
	 */
	
	void keyEvent(int key, int action, int mods);

	/**
	 * Respons to a mouse button event
	 * 
	 * @see https://javadoc.lwjgl.org/org/lwjgl/glfw/GLFW
	 * 
	 * @param button	The button ID
	 * @param action	The action, one of PRESS or RELEASE
	 * @param mods		The modifier keys also pressed (as a bitfield)
	 */
	void mouseButtonEvent(int button, int action, int mods);

}
