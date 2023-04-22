package comp3170;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
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

	// This code assumes OpenGL 4.1 (later versions are not supported by Apple)
	private final static int MAJOR_VERSION = 4;
	private final static int MINOR_VERSION = 1;

	// The window handle
	private String title;
	private long window;
	private int preferredWidth;
	private int preferredHeight;

	private IWindowListener windowListener;

	private boolean isResizable = false;
	private boolean isFullScreen = false;
	private boolean isDoubleBuffered = true;
	private int samples = 0; 

	/**
	 * Create a window.
	 * 
	 * @param title     The window title.
	 * @param width     The desired width of the window (in pixels)
	 * @param height    The desired height of the window (in pixels)
	 * @param resizable Whether the window should be resizable.
	 */
	public Window(String title, int width, int height, IWindowListener windowListener) {
		this.title = title;
		this.preferredWidth = width;
		this.preferredHeight = height;

		if (windowListener == null) {
			throw new NullPointerException("Window listener must be non-null");
		}

		this.windowListener = windowListener;

	}

	/**
	 * Set whether the window is full screen (default false)
	 * 
	 * This must be called before running the window. 
	 */
	
	public void setFullScreen(boolean fullScreen) {
		isFullScreen = fullScreen;
	}

	/**
	 * Set whether the window is resizable (default false)
	 * 
	 * This must be called before running the window. 
	 */
	
	public void setResizable(boolean resizable) {
		isResizable = resizable;
	}

	/**
	 * Set whether the window is double-buffered (default true)
	 * 
	 * This must be called before running the window. 
	 */
	
	public void setDoubleBuffered(boolean doubleBuffered) {
		isDoubleBuffered = doubleBuffered;
	}

	/**
	 * Set multisampling rate.
	 * samples = 0 disables multisampling (default).
	 * 
	 * This must be called before running the window. 
	 */
	
	public void setSamples(int samples) {
		this.samples = samples;
	}

	
	public void run() throws OpenGLException {
		init();
		loop();
		close();
	}

	private void init() throws OpenGLException {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// set a callback to print any GL errors to the console
		GLFWErrorCallback.createPrint(System.err);
		
		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default

		// Request desired OpenGL version
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, MAJOR_VERSION);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, MINOR_VERSION);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // remove deprecated content

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, isResizable ? GLFW_TRUE : GLFW_FALSE); // whether the window will be resizable

		// configure the framebuffer
		glfwWindowHint(GLFW_SAMPLES, samples);
		glfwWindowHint(GLFW_DOUBLEBUFFER, isDoubleBuffered ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);

		// Get the monitor if full screen

		long monitor = NULL;
		if (isFullScreen) {
			monitor = glfwGetPrimaryMonitor();
		}

		// Create the window
		window = glfwCreateWindow(preferredWidth, preferredHeight, title, monitor, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// send a resize event to the WindowListener when the window is resized
		
		glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				windowListener.resize(width, height);
			}
		});

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);

	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GLCapabilities capabilities = GL.createCapabilities();

		windowListener.init();

		// send an initial resize event, to report the window size
		
		try (MemoryStack stack = stackPush()) {
			IntBuffer width = stack.mallocInt(1); // int*
			IntBuffer height = stack.mallocInt(1); // int*

			glfwGetWindowSize(window, width, height);
			windowListener.resize(width.get(), height.get());
		}
		

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(window)) {
			windowListener.draw();		// redraw
			glfwSwapBuffers(window);	// swap the color buffers
			glfwPollEvents();			// poll for window events

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

	/**
	 * Add a listener to listen to key and mouse events in the window.
	 * 
	 * @param listener
	 */
	
	public void setInputListener(IWindowInputListener listener) {
		glfwSetKeyCallback(window, new GLFWKeyCallbackI() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				listener.keyEvent(key, action, mods);			
			}			
		});

		glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				listener.mouseButtonEvent(button, action, mods);			
			}
		});
	}
	
	/**
	 * Returns the position of the cursor, in screen coordinates, 
	 * relative to the upper-left corner of the content area of the window.
	 * 
	 * @param dest	A pre-allocated Vector2f into which to write the result 
	 * @return
	 */
	
	public Vector2f getCursorPos(Vector2f dest) {
		try (MemoryStack stack = stackPush()) {
			DoubleBuffer x = stack.mallocDouble(1); // int*
			DoubleBuffer y = stack.mallocDouble(1); // int*

			glfwGetCursorPos(window, x, y);
			dest.set(x.get(), y.get());
		}
		
		return dest;
	}
	
}
