package comp3170;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

/**
 * Basic OpenGL window class.
 * 
 * To run on MacOS, set -XstartOnFirstThread as a VM flag
 * 
 * @author mq20145620
 */

public class Window {

	// This code assumes OpenGL 4.1
	private final static int MAJOR_VERSION = 4;
	private final static int MINOR_VERSION = 1;
	
	// The window handle
	private String title;
	private long window;
	private int preferredWidth;
	private int preferredHeight;
	private boolean resizable = false;
	private boolean fullScreen = false;

	private IWindowListener windowListener;

	/**
	 * Create a window.
	 * 
	 * @param title The window title.
	 * @param width The desired width of the window (in pixels)
	 * @param height The desired height of the window (in pixels)
	 * @param resizable Whether the window should be resizable.
	 */
	public Window(String title, int width, int height, boolean resizable, IWindowListener windowListener) {
		this.title = title;
		this.resizable = resizable;
		this.preferredWidth = width;
		this.preferredHeight = height;
		
		if (windowListener == null) {
			throw new NullPointerException("Window listener must be non-null");
		}
		
		this.windowListener = windowListener;

	}

	/**
	 * Create a non-resizable window.
	 * 
	 * @param title The window title.
	 * @param width The desired width of the window (in pixels)
	 * @param height The desired height of the window (in pixels)
	 */
	public Window(String title, int width, int height, IWindowListener windowListener) {
		this(title, width, height, false, windowListener);
	}
	
	/**
	 * Create a fullscreen window.
	 * 
	 * @param title The window title.
	 */
	public Window(String title, IWindowListener windowListener) {
		this.title = title;
		this.resizable = false;
		this.fullScreen = true;
		this.windowListener = windowListener;
	}
	
	public void run() throws GLException {
		init();
		loop();
		close();
	}

	private void init() throws GLException {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() ) {
			throw new IllegalStateException("Unable to initialize GLFW");			
		}

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		
		// 
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, MAJOR_VERSION);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, MINOR_VERSION);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);	// remove deprecated content
	
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE); // whether the window will be resizable

		// Get the monitor if full screen
		
		long monitor = NULL;
		if (fullScreen) {
			monitor = glfwGetPrimaryMonitor();
		}
		
		// Create the window
		window = glfwCreateWindow(preferredWidth, preferredHeight, title, monitor, NULL);
		if ( window == NULL ) {
			throw new RuntimeException("Failed to create the GLFW window");			
		}
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop				
			}
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

		GLCapabilities capabilities = GL.createCapabilities();
		
		if (!capabilities.OpenGL41) {
			throw new GLException("OpenGL 4.1 is not supported");
		}
		
	}

	private void loop()  {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GLCapabilities capabilities = GL.createCapabilities();

		windowListener.init(capabilities);
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			windowListener.draw();

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}


	private void close() {
		windowListener.close();
		
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	
}
