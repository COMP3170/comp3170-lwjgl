package comp3170;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

/**
 * A simple implementation of a scene graph.
 * 
 * To use:
 * 
 * 1) Create a 'root' object in your scene:
 * 
 *    root = new SceneObject();
 * 
 * 2) Extend this class to implement specific objects in the graph. 
 * 
 * 3) Subclasses should override the drawSelf() method to draw themselves. 
 *    Children will automatically be drawn using the draw() method below.
 *    
 * 4) Add new objects to the graph using the setParent method, e.g.:
 * 
 *    Ship ship = new Ship(); // a subclass of SceneObject
 *    ship.setParent(root);   // ship is now a child of root in the graph
 * 
 *    Pirate pirate = new Pirate(): // a subclass of SceneObject
 *    private.setParent(ship);		 // pirate is now a child of ship 	
 * 
 * 5) You can use this class without extending it to make 'pivot' objects in 
 *    the graph that are not drawn.
 * 
 * 6) Use the getMatrix() method to access the local coordinate frame matrix 
 *    from this object to its parent.
 * 
 *    Matrix4f pirateToShipMatrix = pirate.getMatrix();
 *    pirateToShipMatrix.translate(1,0,0);	// move the pirate 1 unit to the right 
 * 
 * 7) Use the draw() method to draw the entire scene graph with a given shader. 
 * 
 *    root.draw(); // recursively draws the ship and the pirate 
 * 
 * 8) If you are using a camera, you can pass in the view-projection matrix at the root
 *    and calculate the model-view-projection (MVP) matrix recursively as you descend
 *    the graph/
 * 
 *    projectionMatrix.mul(viewMatrix, mvpMatrix); // MVP = MP * MV
 *    root.draw(mvpMatrix);
 *    
 * 9) If you are implementing multipass rendering, you can also include a 'pass' value, e.g.
 *  
 *    // PASS 0: Render opaque geometry
 *    root.draw(mvpMatrix, 0);
 *    
 *    // PASS 1: Render transparent geometry
 *    root.draw(mvpMatrix, 1);
 * 
 *    Then override drawSelf(matrix, pass) to do different things on each pass, e.g.
 *    
 *    void drawSelf(Matrix4f matrix, int pass) {
 *    	if (pass == 0) {
 *      	// OPAQUE PASS
 *   	}
 *   	else if (pass == 1) {
 *   		// TRANSPARENT PASS
 *   	}
 *    }
 * 
 * 
 * @author malcolmryan
 *
 */
public class SceneObject {

	private static final Matrix4f IDENTITY = new Matrix4f().identity();
	
	private Matrix4f modelToParentMatrix;
	private List<SceneObject> children;
	private SceneObject parent;
	
	public SceneObject() {
		// Allocate model matrix and initialise to the identity matrix
		modelToParentMatrix = new Matrix4f();
		modelToParentMatrix.identity();
					
		// Initialise the scenegraph connections to parent and children
		parent = null;
		children = new ArrayList<SceneObject>();
	}
	
	/**
	 * Get the parent object in the scene graph 
	 * @return the parent object
	 */
	
	public SceneObject getParent() {
		return parent;
	}
	
	/**
	 * Set the parent of this object in the scenegraph.
	 * 
	 * @param newParent The new parent object
	 */
	public void setParent(SceneObject newParent) {
		// disconnect from the old part if necessary
		if (parent != null) {
			parent.children.remove(this);
		}
		
		parent = newParent;
		
		if (newParent != null) {
			newParent.children.add(this);
		}
	}
	
	/**
	 * Get the model->parent matrix
	 * @return	The model matrix
	 */
	public Matrix4f getMatrix()
	{
		return modelToParentMatrix;
	}	

	/**
	 * Write a copy of the model->parent matrix into the given matrix
	 * @param dest A preallocated destination matrix
	 * @return	The model matrix
	 */
	public Matrix4f getMatrix(Matrix4f dest)
	{
		return dest.set(modelToParentMatrix);
	}	
	
	/**
	 * Get the model->world matrix
	 * @param dest	 a matrix to write the result into
	 */
	public Matrix4f getModelToWorldMatrix(Matrix4f dest) {
		dest.identity();
		SceneObject o = this;
		
		// ascend the scene graph multiplying matrices on the left
		while (o.parent != null) {
			dest.mulLocal(o.modelToParentMatrix);
			o = o.parent;
		}		
		
		return dest;
	}
	
	/**
	 * Draw this object. Override this in subclasses to draw specific objects.
	 * 
	 * @param matrix	The matrix to use.
	 */
	protected void drawSelf(Matrix4f matrix) {
		// do nothing
	}

	/**
	 * Draw this object. Override this in subclasses to draw specific objects.
	 * Override this method to implement multipass rendering. 
	 * 
	 * By default it calls drawSelf on the pass 0 and does nothing on later passes.
	 * 
	 * @param matrix	The matrix to use.
	 */
	protected void drawSelf(Matrix4f matrix, int pass) {
		if (pass == 0) {
			drawSelf(matrix);			
		}
	}
	
	/**
	 * Draw this object and all its children in the subgraph.
	 * 
	 */
	public void draw() {
		draw(IDENTITY);
	}

	// Allocate a matrix for use in draw() to calculate the model->world Matrix.
	// (Note: this isn't guaranteed to hold an up-to-date value, don't use it in other methods)
	private Matrix4f tempModelMatrix = new Matrix4f();

	/**
	 * Draw this object and all its children in the subgraph.
	 * 
	 * @param parentMatrix	The model->world matrix for the parent. Set to the identity matrix when drawing the root.
	 */
	public void draw(Matrix4f parentMatrix) {
		// draw everything on pass 0
		draw(parentMatrix, 0);
	}

	/**
	 * Draw this object and all its children in the subgraph.
	 * Use this version for multipass rendering
	 * 
	 * @param parentMatrix	The model->world matrix for the parent. Set to the identity matrix when drawing the root.
	 * @param pass	The number of this draw pass (default 0)
	 */
	public void draw(Matrix4f parentMatrix, int pass) {
		// W = P * M
		tempModelMatrix.set(parentMatrix);			// W = P
		tempModelMatrix.mul(modelToParentMatrix);	// W = W * M
		
		// draw the object using the shader and calculated model matrix
		drawSelf(tempModelMatrix, pass);
	
		// recursively draw the children
		for (SceneObject child : children) {
			child.draw(tempModelMatrix, pass);
		}
		
	}

}
