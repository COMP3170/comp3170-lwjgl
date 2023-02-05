package comp3170;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector2f;

/**
 * Input manager class for COMP3170 projects.
 * 
 * Usage:
 * 
 * In the init() method of the WindowListener create an InputManager and tell it the window it is listening to.
 *  
 * Every frame, call the accessor methods to check for key and mouse:
 * 
 * isMouseDown() - mouse is currently held down 
 * wasMouseClicked() - mouse has been clicked since the last frame 
 * getMousePosition() - mouse position within the window 
 * isKeyDown() - the specified key is currently down 
 * wasKeyPressed() - the specified key has been pressed since the last frame
 * 
 * At the end of the frame:
 * 
 * clear() - clear the wasPressed and wasClicked flags
 * 
 * Version History 
 * - 2022.1 Copied from lecture demos
 * - 2022.2 Made mouse position into a Vector4f
 * - 2023.1 Changes to work in LWJGL 
 * 
 * @author malcolmryan
 *
 */


public class InputManager implements IWindowInputListener {

	private Window window;
	
	private boolean mouseDown;
	private boolean mouseClicked;

	private Set<Integer> keysDown;
	private Set<Integer> keysPressed;

	public InputManager(Window window) {
		
		this.window = window;
		window.setInputListener(this);
		
		mouseDown = false;
		mouseClicked = false;

		keysDown = new HashSet<Integer>();
		keysPressed = new HashSet<Integer>();
	}
	
	// IWindowInputListener methods
	
	@Override
	public void keyEvent(int key, int action, int mods) {
		if (action == GLFW_PRESS) {
			keysDown.add(key);
			keysPressed.add(key);
		}
		else if (action == GLFW_RELEASE) {
			keysDown.remove((Integer)key);
		}
		
	}

	@Override
	public void mouseButtonEvent(int button, int action, int mods) {
		if (button == GLFW_MOUSE_BUTTON_1) {
			if (action == GLFW_PRESS) {
				mouseDown = true;
				mouseClicked = true;				
			}
			else if (action == GLFW_RELEASE) {
				mouseDown = false;
			}
		}		
	}

	
	/**
	 * Test if the mouse button is currently pressed
	 * 
	 * @return true if the mouse button is pressed
	 */
	public boolean isMouseDown() {
		return mouseDown;
	}

	/**
	 * Test if the mouse button was clicked this frame
	 * 
	 * @return true if the mouse button is pressed
	 */
	public boolean wasMouseClicked() {
		return mouseClicked;
	}

	/**
	 * Test if the specified key is currently pressed. Note: the input is a keycode
	 * value, as specified on the GLFW class.
	 * 
	 * https://javadoc.lwjgl.org/org/lwjgl/glfw/GLFW.html 
	 * 
	 * So, for instance, to test if the up arrow is pressed call:
	 * 
	 * input.isKeyDown(GLFW_KEY_UP)
	 * 
	 * @param keyCode The integer keycode for the key
	 * @return true if the key is pressed
	 */

	public boolean isKeyDown(int keyCode) {
		return keysDown.contains(keyCode);
	}

	/**
	 * Test if the specified key has been pressed since the last call to clear().
	 * 
	 * Note: the input is a keycode value, as specified on the KeyEvent class.
	 * https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html
	 * 
	 * So, for instance, to test if the up arrow has been pressed since the last
	 * frame:
	 * 
	 * input.wasKeyPressed(KeyEvent.VK_UP)
	 *
	 * @param keyCode The integer keycode for the key
	 * @return true if the key has been pressed
	 */

	public boolean wasKeyPressed(int keyCode) {
		return keysPressed.contains(keyCode);
	}

	/**
	 * Call this at the end of each frame to clear the mouseClicked and keysPressed flags.
	 */
	
	public void clear() {
		keysPressed.clear();
		mouseClicked = false;
	}
	
	/**
	 * Returns the position of the cursor, in screen coordinates, 
	 * relative to the upper-left corner of the content area of the window.
	 * 
	 * @param dest	A pre-allocated Vector2f into which to write the result 
	 * @return
	 */
	
	public Vector2f getCursorPos(Vector2f dest) {
		return window.getCursorPos(dest);
	}

}
