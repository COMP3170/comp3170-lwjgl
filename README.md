# How to convert 2022 JOGL code to 2023 LWJGL

## General 

*Note:* Running LWJGL code on a Mac requires you to add the command line argument -XstartOnFirstThread to the JVM. 
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
    import static org.lwjgl.opengl.GL20.*;

The new library classes have slightly diiferent names:

**Old:**

    import comp3170.GLBuffers;
    import comp3170.GLException;
    import comp3170.Shader;

**New:**

    import comp3170.GLBuffers;
    import comp3170.IWindowListener;
    import comp3170.Shader;
    import comp3170.Window;

## Main class 

* Windowing is now handled by JWGL rather than Java Swing, so you don't need to use the JFrame anymore
* The JOGL GLEventListener has been replaced with the interface IWindowListener

**Old:**

    public class Week2 extends JFrame implements GLEventListener {

**New**

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
* There is no need to have a GL object any more

**Old:**

      @Override
      public void display(GLAutoDrawable arg0) {
          GL4 gl = (GL4) GLContext.getCurrentGL();
          gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
          gl.glClear(GL.GL_COLOR_BUFFER_BIT);

**New:**

      @Override
      public void draw(GLAutoDrawable arg0) {
          glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
          glClear(GL.GL_COLOR_BUFFER_BIT);

### Resize 

* The method signature for resize has changed. 
* Resize is always called once when the window is first created

**Note:** on Macs with retina display, the window size does not match the canvas size, so you should always implement a basic resize method.

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




