# How to convert 2022 JOGL code to 2023 LWJGL

## General 

**Notes:**
* All code in this unit should be written in OpenGL 4.1. Apple does not support newer versions of OpenGL for Macs.
* Running LWJGL code on a Mac requires you to add the command line argument -XstartOnFirstThread to the JVM. 
In Eclipse, this can be done in Run > Run configurations > Arguments > VM arguments


## Imports 

* Delete any JOGL imports.
* Most GL methods and constants can be found on the org.lwjgl.opengl.GL41 class
* You will also need the GLCapabilites class

**Old:**

    import com.jogamp.opengl.GL;
    import com.jogamp.opengl.GL4;

**New:**

    import org.lwjgl.opengl.GLCapabilities;
    import static org.lwjgl.opengl.GL41.*;

The new library classes have slightly diiferent names:

**Old:**

    import comp3170.GLBuffers;
    import comp3170.GLException;
    import comp3170.Shader;

**New:**

    import comp3170.GLBuffers;
    import comp3170.OpenGLException;
    import comp3170.IWindowListener;
    import comp3170.Shader;
    import comp3170.Window;

## Main class 

* Windowing is now handled by JWGL rather than Java Swing, so you don't need to use the JFrame anymore
* The JOGL GLEventListener has been replaced with the interface IWindowListener

**Old:**

    public class Week2 extends JFrame implements GLEventListener {

**New**

    public class Week2 implements IWindowListener {

## Constructor

The new constructor is much simpler, just create a window and run it.

**Old:**

      public Week2() {
          super("Week 2 example");

          // set up a GL canvas
          GLProfile profile = GLProfile.get(GLProfile.GL4);		 
          GLCapabilities capabilities = new GLCapabilities(profile);
          
          canvas = new GLCanvas(capabilities);
          canvas.addGLEventListener(this);
          add(canvas);

          // set up the JFrame

          setSize(width,height);
          setVisible(true);
          addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              System.exit(0);
            }
          });
      }

**New:**

      public Week2() {
          Window window = new Window("Week 2 example", width, height, this);
          window.run();
      }
    
## Event methods

* The event methods are now called init(), draw(), resize() and close()
* The event methods no longer take a GLAutoDrawable parameter
* There is no need to have a GL object to access methods or constants

**Old:**

      @Override
      public void display(GLAutoDrawable arg0) {
          GL4 gl = (GL4) GLContext.getCurrentGL();
          gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
          gl.glClear(GL.GL_COLOR_BUFFER_BIT);

**New:**

      @Override
      public void draw() {
          glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
          glClear(GL_COLOR_BUFFER_BIT);

### Resize 

* The method signature for resize has changed. 
* Resize is always called once when the window is first created

**Note:** on Macs with retina display, the window size does not match the canvas size, so you should always implement a basic resize method, even if the window isn't resizable.

**Old:**

  	@Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
    }

**New:**

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

### Input

The new InputManager class has the same interface as the old one. The only difference is that you need to create the InputManager in init() rather than in the main constructor, as the window has to be initialised before the InputManager can be attached.

**Old:**

	private GLCanvas canvas;
	private InputManager input;

    public Demo() {
        GLProfile profile = GLProfile.get(GLProfile.GL4);		 
        GLCapabilities capabilities = new GLCapabilities(profile);
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        add(canvas);
		
        // set up Input manager
        input = new InputManager(canvas);
    }

**New:**

    private Window window;
    private InputManager input;

    public Demo() {
        window = new Window("Demo", width, height, true, this);
        window.run();		
    }

    public void init() {
        // set up Input manager
        input = new InputManager(canvas);        
    }

The new InputSystem uses [GLFW keycodes](https://www.glfw.org/docs/3.3/group__keys.html) rather than Java KeyEvent keycodes.

**Old:**

    if (input.isKeyDown(KeyEvent.VK_LEFT)) {
        cameraAngle = (cameraAngle + CAMERA_ROTATION_SPEED * deltaTime) % TAU;			
    }

**New:**

    if (input.isKeyDown(GLFW_KEY_LEFT)) {
        cameraAngle = (cameraAngle + CAMERA_ROTATION_SPEED * deltaTime) % TAU;			
    }

### Animation

There is no longer any need to add an Animator or otherwise 'turn-on' animation. When you call Window.run() it will repeatedly call the draw() method on the listener until the window closes. Double buffering is enabled by default (and is tricky to disable without editing the Window class).

Calculating deltaTime is done much the same as before, however I recommend initialising oldTime in init() rather than in the constructor.

**Old:**

    private Animator animator;
    private long oldTime;

    public Week3() {
        // set up a GL canvas
        GLProfile profile = GLProfile.get(GLProfile.GL4);		 
        GLCapabilities capabilities = new GLCapabilities(profile);
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        add(canvas);
		
        // set up Animator		
        animator = new Animator(canvas);
        animator.start();
        oldTime = System.currentTimeMillis();				
    }
    
    private void update() {
        long time = System.currentTimeMillis();
        float deltaTime = (time - oldTime) / 1000f;
        oldTime = time;
        System.out.println("update: dt = " + deltaTime + "s");
    }
    
    public void display(GLAutoDrawable arg0) {
        // update the scene
	update();	
        // ...
    }

**New:**

    private long oldTime;

    public Week3() throws OpenGLException {
        window = new Window("Week 3", screenWidth, screenHeight, this);
        window.run();
	// no need for an Animator
    }
    
    public void init() {
        oldTime = System.currentTimeMillis();				
    }
    
    private void update() {
    	// same code as before
        long time = System.currentTimeMillis();
        float deltaTime = (time - oldTime) / 1000f;
        oldTime = time;
        System.out.println("update: dt = " + deltaTime + "s");
    }

    public void draw() {
        // update the scene
        update();
	// ...
    }
    
    
