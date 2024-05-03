package comp3170;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

/**
 * This class provides a wrapper to the OpenGL and STBI library calls
 * necessary to load an image file for use as an OpenGL texture.
 *
 * This class implements a basic version of the Singleton pattern.
 * To use it, follow these steps:
 *
 * 1) Create a new copy of TextureLibrary with the path to the directory containing your
 * shaders when you initialise your scene:
 *
 *    public static final File TEXTURE_DIRECTORY = new File("comp3170/demos/week11/textures");
 *
 *    public void init() {
 *       // create an instance of the ShaderLibrary singleton
 * 	     new TextureLibrary(TEXTURE_DIRECTORY);
 *    }
 *
 * 2) When loading a texture, call TextureLibrary.instance.loadTexture().
 *
 * If the texture you specify has already been loaded, it will be returned.
 * Otherwise a new copy of the texture will be loaded and recorded in the library.
 *
 *    public static final String TEXTURE = "bricks.png";
 * 	  private int texture;
 *
 *    public Cylinder() {
 *        texture = TextureLibrary.instance.loadTexture(TEXTURE);
 *    }
 *
 * @author malcolmryan
 *
 */
public class TextureLibrary {

	public static TextureLibrary instance;
	private final Map<String, Integer> loadedTextures;
	private List<File> searchPath;

	public TextureLibrary() {
		if (instance != null) {
			throw new IllegalStateException("An instance of TextureLibrary already exists");
		}
		instance = this;

		searchPath = new ArrayList<>();
		loadedTextures = new HashMap<>();
	}

	public TextureLibrary(File path) {
		this();
		addPath(path);
	}

	public TextureLibrary(File[] paths) {
		this();
		addPaths(paths);
	}

	public TextureLibrary addPath(File path) {
		if (!path.isDirectory()) {
			throw new IllegalArgumentException(String.format("'%s' is not a directory", path.getName()));
		}
		searchPath.add(path);

		return this;
	}

	public TextureLibrary addPaths(File[] paths) {
		for (int i = 0; i < paths.length; i++) {
			addPath(paths[i]);
		}
		return this;
	}
	/**
	 * Load a texture from an image file.
	 *
	 * @param filename The image file
	 * @return The GL texture handle
	 * @throws IOException
	 * @throws OpenGLException
	 */
	public int loadTexture(String filename) throws IOException, OpenGLException {
		if (loadedTextures.containsKey(filename)) {
			return loadedTextures.get(filename);
		}

		// Load the image file using STBI library
		File textureFile = findFile(filename);

		IntBuffer x = BufferUtils.createIntBuffer(1);
		IntBuffer y = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);

		stbi_set_flip_vertically_on_load(true);
		ByteBuffer image = stbi_load(textureFile.getAbsolutePath(), x, y, channels, STBI_rgb_alpha);

		if (image == null) {
			throw new IOException(stbi_failure_reason());
		}

		// Create a new texture

		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);

		// Set the texture data to the loaded image data

		// stbi_load always returns 8 bits per channel
		// See: https://javadoc.lwjgl.org/org/lwjgl/stb/STBImage.html#stbi_load(java.lang.CharSequence,java.nio.IntBuffer,java.nio.IntBuffer,java.nio.IntBuffer,int)
		// regardless of external format, we'll store the texture internally as RGBA
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x.get(), y.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		OpenGLException.checkError();

		// Free the image file
		stbi_image_free(image);

		// Set default texture parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		loadedTextures.put(filename, textureID);
		return textureID;
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
	 * Create a render texture with the specified dimensions.
	 *
	 * @param width	Texture width in pixels
	 * @param height Texture height in pixels
	 * @param format Texture format, one of GL_RED, GL_RG, GL_RGB, GL_BGR, GL_RGBA, GL_BGRA, GL_DEPTH_COMPONENT, GL_DEPTH_STENCIL
	 * @return	The OpenGL handle to the render texture.
	 */

	public int createRenderTexture(int width, int height, int format) {
		int renderTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, renderTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		return renderTexture;
	}

	// Each of the sides of the cubemap
	private static final int[] SIDES = {
		GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
		GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
		GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};

	/**
	 * Create a cube map from six image files.
	 *
	 * @param filename	An arrange containing six filenames in the order [+X, -X, +Y, -Y, +Z, -Z]
	 * @return The OpenGL handle to the cubemap
	 * @throws IOException
	 * @throws OpenGLException
	 */
	public int loadCubemap(String[] filename) throws IOException, OpenGLException {
		return loadCubemap(filename, false);
	}
		
	/**
	 * Create a cube map from six image files.
	 *
	 * @param filename	An arrange containing six filenames in the order [+X, -X, +Y, -Y, +Z, -Z]
	 * @param flipY Flip the images around the Y axis
	 * @return The OpenGL handle to the cubemap
	 * @throws IOException
	 * @throws OpenGLException
	 */
	public int loadCubemap(String[] filename, boolean flipY) throws IOException, OpenGLException {
		// create a cubemap texture
		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);

		// load the images for the six sides of the cube

		stbi_set_flip_vertically_on_load(flipY);

		for (int i = 0; i < filename.length; i++) {
			File file = findFile(filename[i]);

			IntBuffer x = BufferUtils.createIntBuffer(1);
			IntBuffer y = BufferUtils.createIntBuffer(1);
			IntBuffer channels = BufferUtils.createIntBuffer(1);

			ByteBuffer image = stbi_load(file.getAbsolutePath(), x, y, channels, STBI_rgb_alpha);
			glTexImage2D(SIDES[i], 0, GL_RGBA, x.get(), y.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			OpenGLException.checkError();

			// Free the image file
			stbi_image_free(image);
		}

		// configure the default texture parameters

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

		return textureID;
	}

}
