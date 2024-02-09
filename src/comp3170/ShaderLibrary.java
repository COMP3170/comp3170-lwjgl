package comp3170;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to keep track of a library of loaded shaders.
 * 
 * This class implements a basic version of the Singleton pattern. 
 * To use it, follow these steps:
 * 
 * 1) Create a new copy of ShaderLibrary with the path to the directory containing your
 * shaders when you initialise your scene:
 * 
 *    public static final File SHADER_DIRECTORY = new File("comp3170/demos/week11/shaders");
 * 
 *    public void init() {
 *       // create an instance of the ShaderLibrary singleton
 * 	     new ShaderLibrary(SHADER_DIRECTORY);
 *    }
 *
 * 2) When creating a new SceneObject, call ShaderLibrary.instance.compileShader(). 
 * If the shader you specify has already been compiled, it will be returned.
 * Otherwise a new copy of the shader will be compiled and recorded in the library.
 * 
 *    public static final String VERTEX_SHADER = "simpleVertex.glsl";
 *    public static final String FRAGMENT_SHADER = "simpleFragment.glsl";
 * 	  private Shader shader;
 * 
 *    public Cylinder() {
 *        shader = ShaderLibrary.instance.compileShader(VERTEX_SHADER, FRAGMENT_SHADER);
 *    }
 * 
 * @author malcolmryan
 *
 */

public class ShaderLibrary {
	
	public static ShaderLibrary instance = null;
	private List<File> searchPath; 	
	private Map<Pair<String, String>, Shader> loadedShaders;

	public ShaderLibrary() {
		if (instance != null) {
			throw new IllegalStateException("An instance of ShaderLibrary already exists.");
		}
		instance = this;

		searchPath = new ArrayList<File>();
		loadedShaders = new HashMap<Pair<String, String>, Shader>();
	}

	public ShaderLibrary(String path) {
		this();
		addPath(path);
	}
	
	public ShaderLibrary(File path) {
		this();
		addPath(path);
	}

	public ShaderLibrary addPath(String path) {
		return addPath(new File(path));
	}
	
	public ShaderLibrary addPath(File path) {
		if (!path.exists()) {
			throw new IllegalArgumentException(String.format("'%s' does not exist.", path.getAbsolutePath()));
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException(String.format("'%s' is not a directory", path.getAbsolutePath()));
		}
		searchPath.add(path);
		
		return this;
	}
	
	/**
	 * Load, compile and link a shader. 
	 * 
	 * All shader files are expected to be in the folder given by the DIRECTORY constant above.  
	 * 
	 * If the vertex/fragment pair have already been compiled and linked, 
	 * a reference to the existing shader will be returned, rather than recompiling.  
	 * 
	 * @param vertex	The name of the vertex shader file
	 * @param fragment	The name of the fragment shader file
	 * @return
	 */
	
	public Shader compileShader(String vertex, String fragment) {
		
		Pair<String, String> p = new Pair<String, String>(vertex, fragment);

		if (loadedShaders.containsKey(p)) {
			return loadedShaders.get(p);
		}		
		
		Shader shader = null;
		try {
			File vertexShader = findFile(vertex);
			File fragmentShader = findFile(fragment);
			shader = new Shader(vertexShader, fragmentShader);		
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (OpenGLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		loadedShaders.put(p, shader);
		
		return shader;

	}

	private File findFile(String filename) throws FileNotFoundException {
		for (File dir : searchPath) {
			for (File file : dir.listFiles()) {
				if (file.getName().equals(filename)) {
					return file;
				}
			}
		}

		throw new FileNotFoundException(filename);
	}

	/**
	 * Creates a pair of two objects
	 * 
	 * @param <T1> the type of object 1
	 * @param <T2> the type of object 2
	 */
	private static class Pair<T1, T2> {
	    
	    /**
	     * The first object
	     */
	    private final T1 obj1;
	    
	    /**
	     * The second object
	     */
	    private final T2 obj2;
	    
	    /**
	     * Creates a new pair of objects
	     * 
	     * @param first the first object
	     * @param second the second object
	     */
	    public Pair(T1 first, T2 second) {
	        this.obj1 = first;
	        this.obj2 = second;
	    }
	    
	    /**
	     * @return the first object
	     */
	    public T1 getFirst() {
	        return this.obj1;
	    }
	    
	    /**
	     * @return the second object
	     */
	    public T2 getSecond() {
	        return this.obj2;
	    }
	    
	    @Override
	    public boolean equals(Object other) {
	        if (!(other instanceof Pair<?,?>)) {
	            return false;
	        }
	        Pair<?,?> otherObj = (Pair<?,?>)other;
	        return otherObj.obj1.equals(obj1) && otherObj.obj2.equals(obj2);
	    }
	    
	    /**
	     * The number of bits per bytes
	     */
	    private static final int BITS_PER_BYTES = 8;
	    
	    /**
	     * The number of bytes in the hash
	     */
	    private static final int NUMBER_BITS = Integer.BYTES * BITS_PER_BYTES;
	    
	    /**
	     * The number of bytes in the hash
	     */
	    private static final int HALF_NUMBER_BITS = NUMBER_BITS / 2;
	    
	    /**
	     * 0's for the first half of the int, 1's for the second half of the int
	     */
	    private static final int EMPTY_FULL = (1 << (HALF_NUMBER_BITS + 1)) - 1;

	    /**
	     * 1's for the first half of the int, 0's for the second half of the int
	     */
	    private static final int FULL_EMPTY = EMPTY_FULL << HALF_NUMBER_BITS;
	    
	    @Override
	    public int hashCode() {
	        int firstHash = this.obj1.hashCode();
	        int first16Bits = (EMPTY_FULL & firstHash) ^ ((FULL_EMPTY & firstHash) >> HALF_NUMBER_BITS);
	        int secondHash = this.obj2.hashCode();
	        int second16Bits = (EMPTY_FULL & secondHash) ^ ((FULL_EMPTY & secondHash) >> HALF_NUMBER_BITS);
	        return (first16Bits << HALF_NUMBER_BITS) | second16Bits;
	    }
	    
	    @Override
	    public String toString() {
	        return String.format("Pair<%s, %s>", this.obj1.toString(),
	                this.obj2.toString());
	    }
	}

}
