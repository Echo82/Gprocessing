package Gprocessing.graphics.renderer;

import Gprocessing.ecs.SpriteRenderer;
import Gprocessing.graphics.Shader;
import Gprocessing.graphics.Window;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import org.joml.Vector4f;
import Gprocessing.physics.Transform;



public class RenderBatch {
	/**
	 * Vertex layout
	 * 
	 * position color float, float, float, float, float, float
	 */
	private final int POSITION_SIZE = 2;
	private final int COLOR_SIZE = 4;

	private final int POSITION_OFFSET = 0;
	private final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;

	private final int VERTEX_SIZE = 6;
	private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

	private SpriteRenderer[] sprites;
	private int numberOfSprites;
	private boolean hasRoomLeft;

	private float[] vertices;

	private int vaoID, vboID;
	private int maxBatchSize;
	private Shader shader;

	RenderBatch(int maxBatchSize) {
		shader = new Shader("src/assets/shaders/default.glsl");
		shader.compile();

		this.sprites = new SpriteRenderer[maxBatchSize];
		this.maxBatchSize = maxBatchSize;

		// 4 vertices quads
		vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

		this.numberOfSprites = 0;
		this.hasRoomLeft = true;
	}

	public void start() {
		// Generate and bind a Vertex Array Object
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);

		// Allocate space for vertices
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

		// Create and upload indices buffer
		int eboID = glGenBuffers();
		int[] indices = generateIndices();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		// Enable Buffer attribute pointers (tell openGL what a vertex layout looks
		// like)
		glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);
	}
	


	public void addSprite(SpriteRenderer sprite) {
		// Get the index and add the renderObject
		int index = this.numberOfSprites;
		this.sprites[index] = sprite;
		this.numberOfSprites++;

		// Add properties to local vertices array
		loadVertexProperties(index);

		if (this.numberOfSprites >= this.maxBatchSize) {
			this.hasRoomLeft = false;
		}
	}

	public void render() {
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

		// Use shader
		shader.use();
		shader.uploadMat4f("uProjection", Window.currentScene.camera().getProjectionMatrix());
		shader.uploadMat4f("uView", Window.currentScene.camera().getViewMatrix());

		// bind the VAO
		glBindVertexArray(vaoID);

		// enable vertex attribute pointers
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glDrawElements(GL_TRIANGLES, this.numberOfSprites * 6, GL_UNSIGNED_INT, 0);

		// unbind everything
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		shader.detach();
	}

	private void loadVertexProperties (int index) {
		// NOTE: this function figures out how to add vertices with an origin at the bottom left
		SpriteRenderer sprite = this.sprites[index];
		
		// FInd offset within array (4 vertices per sprite)
		int offset = index * 4 * VERTEX_SIZE;
		
		Vector4f color = sprite.getColor();
		
		// Add vertex with the appropriate properties
		float xAdd = 1;
		float yAdd = 1;
		for (int  i = 0; i < 4; i ++) {
			switch (i) {
			case 1:
				yAdd = 0;
				break;
			case 2:
				xAdd = 0;
				break;
			case 3:
				yAdd = 1;
				break;
			}
			
			// Load position
			Transform spr = sprite.gameObject.transform;
			vertices[offset] 	 = spr.position.x + (xAdd * spr.scale.x);
			vertices[offset + 1] = spr.position.y + (yAdd * spr.scale.y);
			
			// Load color
			vertices[offset + 2] = color.x;
			vertices[offset + 3] = color.y;
			vertices[offset + 4] = color.z;
			vertices[offset + 5] = color.w;
			
			offset += VERTEX_SIZE;
		}
	}

	private int[] generateIndices() {
		// 6 indices/quad (3/triangle)
		int[] elements = new int[6 * maxBatchSize];
		for (int i = 0; i < maxBatchSize; i++) {
			loadElementIndices(elements, i);
		}

		return elements;
	}

	private void loadElementIndices(int[] elements, int i) {
		int offsetArrayIndex = 6 * i;
		int offset = 4 * i;

		// 3, 2, 0, 0, 2, 1, 	7, 6, 4, 4, 6, 5

		// Triangle 1
		elements[offsetArrayIndex] = offset + 3;
		elements[offsetArrayIndex + 1] = offset + 2;
		elements[offsetArrayIndex + 2] = offset + 0;

		// Triangle 2
		elements[offsetArrayIndex + 3] = offset + 0;
		elements[offsetArrayIndex + 4] = offset + 2;
		elements[offsetArrayIndex + 5] = offset + 1;

	}

	public boolean hasRoomLeft() {
		return hasRoomLeft;
	}
}
