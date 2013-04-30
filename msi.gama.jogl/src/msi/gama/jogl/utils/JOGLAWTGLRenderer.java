package msi.gama.jogl.utils;

import static javax.media.opengl.GL2.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import msi.gama.common.util.GuiUtils;
import msi.gama.jogl.JOGLAWTDisplaySurface;
import msi.gama.jogl.utils.Camera.Camera;
import msi.gama.jogl.utils.Camera.Arcball.*;
import msi.gama.jogl.utils.GraphicDataType.*;
import msi.gama.jogl.utils.JTSGeometryOpenGLDrawer.ShapeFileReader;
import msi.gama.jogl.utils.collada.ColladaReader;
import msi.gama.jogl.utils.dem.DigitalElevationModelDrawer;
import msi.gama.outputs.OutputSynchronizer;
import msi.gama.util.GamaColor;
import org.geotools.data.simple.SimpleFeatureCollection;
import utils.GLUtil;
import javax.media.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import com.jogamp.opengl.util.awt.Screenshot;
import com.jogamp.opengl.util.texture.*;

public class JOGLAWTGLRenderer implements GLEventListener {

	// ///OpenGL member//////
	private static final int REFRESH_FPS = 30;
	public GLU glu;
	public GL2 gl;
	public final FPSAnimator animator;
	private GLContext context;
	public GLCanvas canvas;

	public boolean opengl = true;

	public volatile boolean isInitialized = false;

	public boolean enableGlRenderAnimator = true;

	// Event Listener
	public MyListener myListener;

	private int width, height;
	// Camera
	public Camera camera;

	public MyGraphics graphicsGLUtils;

	// Use to test and display basic opengl shape and primitive
	public MyGLToyDrawer myGLDrawer;

	// Textures list to store all the texture.
	public Map<BufferedImage, MyTexture> myTextures = new LinkedHashMap();

	/** The earth texture. */
	private Texture earthTexture;

	public float textureTop;
	public float textureBottom;
	public float textureLeft;
	public float textureRight;
	public Texture[] textures = new Texture[3];
	public static int currTextureFilter = 2; // currently used filter

	// Lighting
	private static boolean isLightOn;
	public GamaColor ambientLightValue;

	// Blending
	private static boolean blendingEnabled; // blending on/off

	public JOGLAWTDisplaySurface displaySurface;

	// picking
	double angle = 0;

	private final boolean drawAxes = true;

	// Use multiple view port
	private final boolean multipleViewPort = false;

	// Display model a a 3D Cube
	private final boolean threeDCube = false;
	// Handle Shape file
	public ShapeFileReader myShapeFileReader;
	private boolean updateEnvDim = false;

	// Arcball
	private ArcBall arcBall;

	// use glut tesselation or JTS tesselation
	// (can be set in GAML with the boolean facet "tesselation")
	public boolean useTessellation = true;

	// Display or not the triangle when using triangulation (useTessellation = false)
	public boolean polygonmode = true;

	// Show JTS (GAMA) triangulation
	public boolean JTSTriangulation = false;

	// DEM
	public DigitalElevationModelDrawer dem;

	public JOGLAWTGLRenderer(JOGLAWTDisplaySurface d) {

		// Enabling the stencil buffer
		GLCapabilities cap = new GLCapabilities(null);
		cap.setStencilBits(8);
		// Initialize the user camera
		camera = new Camera();
		myGLDrawer = new MyGLToyDrawer();
		canvas = new GLCanvas(cap);

		myListener = new MyListener(camera, this);

		canvas.addGLEventListener(this);
		canvas.addKeyListener(myListener);
		canvas.addMouseListener(myListener);
		canvas.addMouseMotionListener(myListener);
		canvas.addMouseWheelListener(myListener);
		canvas.setFocusable(true); // To receive key event
		canvas.requestFocusInWindow();
		animator = new FPSAnimator(canvas, REFRESH_FPS, true);
		displaySurface = d;

		dem = new DigitalElevationModelDrawer(this);

	}

	@Override
	public void init(GLAutoDrawable drawable) {

		width = drawable.getWidth();
		height = drawable.getHeight();
		// Get the OpenGL graphics context
		gl = drawable.getGL().getGL2();
		// GL Utilities
		glu = new GLU();

		setContext(drawable.getContext());

		arcBall = new ArcBall(width, height);

		// Set background color
		gl.glClearColor(displaySurface.getBgColor().getRed(), displaySurface.getBgColor().getGreen(), displaySurface
			.getBgColor().getBlue(), 1.0f);

		// Enable smooth shading, which blends colors nicely, and smoothes out
		// lighting.
		GLUtil.enableSmooth(gl);

		// Perspective correction
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		GLUtil.enableDepthTest(gl);

		// Set up the lighting for Light-1
		GLUtil.InitializeLighting(gl, glu, width, ambientLightValue);

		// PolygonMode (Solid or lines)
		if ( polygonmode ) {
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		} else {
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		}

		// Blending control
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		gl.glEnable(GL_BLEND);
		// gl.glDisable(GL_DEPTH_TEST);
		// FIXME : should be turn on only if need (if we draw image)
		// problem when true with glutBitmapString
		blendingEnabled = true;
		isLightOn = true;

		camera.UpdateCamera(gl, glu, width, height);

		graphicsGLUtils = new MyGraphics(this);

		// hdviet added 28j/05/2012
		// Start Of User Initialization
		LastRot.setIdentity(); // Reset Rotation
		ThisRot.setIdentity(); // Reset Rotation
		ThisRot.get(matrix);

		// FIXME: Need to be place somewhere (triggered by a button in Gama)
		/*
		 * if(dem !=null){
		 * dem.InitDEM(gl);
		 * }
		 */

		isInitialized = true;
		GuiUtils.debug("JOGLAWTGLRenderer.init: " + this.displaySurface.getOutputName());
		OutputSynchronizer.decInitializingViews(this.displaySurface.getOutputName());
	}

	@Override
	public void display(GLAutoDrawable drawable) {

		if ( enableGlRenderAnimator ) {

			// hdviet added 28/05/2012
			synchronized (matrixLock) {
				ThisRot.get(matrix);
			}

			// Get the OpenGL graphics context
			gl = drawable.getGL().getGL2();
			setContext(drawable.getContext());

			width = drawable.getWidth();
			height = drawable.getHeight();

			// Clear the screen and the depth buffer
			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL2.GL_PROJECTION);
			// Reset the view (x, y, z axes back to normal)
			gl.glLoadIdentity();

			camera.UpdateCamera(gl, glu, width, height);

			if ( isLightOn ) {
				gl.glEnable(GL_LIGHTING);
			} else {
				gl.glDisable(GL_LIGHTING);
			}

			// FIXME: Now the background is not updated but it should to have a night effect.
			// Set background color
			// gl.glClearColor(ambiantLightValue.floatValue(), ambiantLightValue.floatValue(),
			// ambiantLightValue.floatValue(), 1.0f);
			// The ambiant_light is always reset in case of dynamic lighting.
			GLUtil.UpdateAmbiantLight(gl, glu, ambientLightValue);

			// Show triangulated polygon or not (trigger by GAMA)
			if ( !displaySurface.triangulation ) {
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
			} else {
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			}

			// Blending control
			if ( blendingEnabled ) {
				gl.glEnable(GL_BLEND); // Turn blending on
				// FIXME: This has been comment (09/12 r4989) to have the depth testing when image
				// are drawn but need to know why it was initially disabled?
				// Imply strange rendering when using picture (e.g boids)
				// gl.glDisable(GL_DEPTH_TEST); // Turn depth testing off
			} else {
				gl.glDisable(GL_BLEND); // Turn blending off
				gl.glEnable(GL_DEPTH_TEST); // Turn depth testing on
			}

			// hdviet added 02/06/2012
			gl.glPushMatrix();
			gl.glMultMatrixf(matrix, 0);

			// Use polygon offset for a better edges rendering
			// (http://www.glprogramming.com/red/chapter06.html#name4)
			gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
			gl.glPolygonOffset(1, 1);

			if ( dem.isInitialized() == true ) {
				dem.DisplayDEM(gl);
			} else {
				this.DrawScene();
				if ( drawAxes ) {
					double envMaxDim = displaySurface.getIGraphics().getMaxEnvDim();
					this.graphicsGLUtils.DrawXYZAxis(envMaxDim / 10);
					this.graphicsGLUtils.DrawZValue(-envMaxDim / 10, (float) camera.zPos);
				}
			}

			// this.DrawShapeFile();
			// this.DrawCollada();
			gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

			gl.glPopMatrix();

			// ROI drawer
			if ( this.displaySurface.selectRectangle ) {
				DrawROI();
			}

		} else {
			// System.out.println("I stop the display");
		}
	}

	public Point GetRealWorldPointFromWindowPoint(Point windowPoint) {

		int viewport[] = new int[4];
		double mvmatrix[] = new double[16];
		double projmatrix[] = new double[16];
		int realy = 0;// GL y coord pos
		double wcoord[] = new double[4];// wx, wy, wz;// returned xyz coords

		int x = windowPoint.x, y = windowPoint.y;

		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		/* note viewport[3] is height of window in pixels */
		realy = viewport[3] - y - 1;

		FloatBuffer floatBuffer = FloatBuffer.allocate(1);
		gl.glReadPixels(x, realy, 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, floatBuffer);
		float z = floatBuffer.get(0);

		glu.gluUnProject(x, realy, z, //
			mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
		/*
		 * System.out.println("World coords at z=" + z + "are (" //
		 * + wcoord[0] + ", " + wcoord[1] + ", " + wcoord[2]
		 * + ")");
		 */

		gl.glFlush();

		Point realWorldPoint = new Point((int) wcoord[0], (int) wcoord[1]);
		return realWorldPoint;
	}

	public void DrawROI() {

		if ( myListener.enableROIDrawing ) {
			Point windowPressedPoint = new Point(myListener.lastxPressed, myListener.lastyPressed);
			Point realPressedPoint = GetRealWorldPointFromWindowPoint(windowPressedPoint);

			Point windowmousePositionPoint = new Point(myListener.mousePosition.x, myListener.mousePosition.y);
			Point realmousePositionPoint = GetRealWorldPointFromWindowPoint(windowmousePositionPoint);

			System.out.println("From" + realPressedPoint.x + "," + realPressedPoint.y);
			System.out.println("To" + realmousePositionPoint.x + "," + realmousePositionPoint.y);

			// System.out.println("World coords are (" //+ realPoint.x + ", " + realPoint.y);

			if ( camera.isModelCentered ) {
				gl.glTranslated(-displaySurface.getIGraphics().getEnvWidth() / 2, displaySurface.getIGraphics()
					.getEnvHeight() / 2, 0.0f); // translate
				// right
				// and
				// into
				// the
				// screen
			}

			myGLDrawer.DrawROI(gl, realPressedPoint.x - displaySurface.getIGraphics().getEnvWidth() / 2,
				-(realPressedPoint.y - displaySurface.getIGraphics().getEnvHeight() / 2), realmousePositionPoint.x -
					displaySurface.getIGraphics().getEnvWidth() / 2, -(realmousePositionPoint.y - displaySurface
					.getIGraphics().getEnvHeight() / 2));

		}

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {

		// Get the OpenGL graphics context
		gl = drawable.getGL().getGL2();

		if ( height == 0 ) {
			height = 1; // prevent divide by zero
		}
		float aspect = (float) width / height;

		// Set the viewport (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Enable the model view - any new transformations will affect the
		// model-view matrix
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset

		// perspective view
		gl.glViewport(10, 10, width - 20, height - 20);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, aspect, 0.1f, 100.0f);
		glu.gluLookAt(camera.getXPos(), camera.getYPos(), camera.getZPos(), camera.getXLPos(), camera.getYLPos(),
			camera.getZLPos(), 0.0, 1.0, 0.0);
		arcBall.setBounds(width, height);

	}

	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}

	public void DrawScene() {
		if ( displaySurface.picking ) {
			// Display the model center on 0,0,0
			if ( camera.isModelCentered ) {
				gl.glTranslated(-displaySurface.getIGraphics().getEnvWidth() / 2, displaySurface.getIGraphics()
					.getEnvHeight() / 2, 0.0f); // translate
				// right
				// and
				// into
				// the
				// screen
			}
			this.DrawPickableObject();
		} else {
			// Display the model center on 0,0,0
			if ( camera.isModelCentered ) {
				gl.glTranslated(-displaySurface.getIGraphics().getEnvWidth() / 2, displaySurface.getIGraphics()
					.getEnvHeight() / 2, 0.0f); // translate
				// right
				// and
				// into
				// the
				// screen
			}
			// FIXME: Need to simplify , give a boolean to DrawModel to know
			// if it's in Picking mode.

			if ( threeDCube ) {
				// float envMaxDim = (
				// displaySurface.openGLGraphics).maxEnvDim;
				float envMaxDim = (float) displaySurface.getIGraphics().getEnvWidth();

				this.drawModel(false);
				gl.glTranslatef(envMaxDim, 0, 0);
				gl.glRotatef(90, 0, 1, 0);
				this.drawModel(false);
				gl.glTranslatef(envMaxDim, 0, 0);
				gl.glRotatef(90, 0, 1, 0);
				this.drawModel(false);
				gl.glTranslatef(envMaxDim, 0, 0);
				gl.glRotatef(90, 0, 1, 0);
				this.drawModel(false);
				gl.glTranslatef(envMaxDim, 0, 0);
				gl.glRotatef(90, 0, 1, 0);

				gl.glRotatef(-90, 1, 0, 0);
				gl.glTranslatef(0, envMaxDim, 0);
				this.drawModel(false);
				gl.glTranslatef(0, -envMaxDim, 0);
				gl.glRotatef(90, 1, 0, 0);

				gl.glRotatef(90, 1, 0, 0);
				gl.glTranslatef(0, 0, envMaxDim);
				this.drawModel(false);

				gl.glTranslatef(0, 0, -envMaxDim);
				gl.glRotatef(-90, 1, 0, 0);
				/*
				 * gl.glTranslatef(0,(
				 * displaySurface.openGLGraphics).envWidth,0);
				 * this.DrawModel(false);
				 * 
				 * gl.glTranslatef(0,-(
				 * displaySurface.openGLGraphics).envWidth,0);
				 * gl.glRotatef(90, 1, 0, 0);
				 * 
				 * gl.glTranslatef(0,-(
				 * displaySurface.openGLGraphics).envWidth,0);
				 * gl.glRotatef(90, 1, 0, 0);
				 * this.DrawModel(false);
				 */

			} else {
				if ( !multipleViewPort ) {
					gl.glViewport(0, 0, width, height); // Reset The Current Viewport
					this.drawModel(false);
				} else {
					// Set The Viewport To The Top Left
					gl.glViewport(0, height / 2, width / 2, height / 2);
					this.drawModel(false);

					// Set The Viewport To The Top Right. It Will Take Up Half The
					// Screen Width And Height
					gl.glViewport(width / 2, height / 2, width / 2, height / 2);
					this.drawModel(false);

					// Set The Viewport To The Bottom Right
					gl.glViewport(width / 2, 0, width / 2, height / 2);
					this.drawModel(false);

					// Set The Viewport To The Bottom Left
					gl.glViewport(0, 0, width / 2, height / 2);
					this.drawModel(false);
				}
			}

		}
	}

	public void drawModel(boolean picking) {

		// (displaySurface.openGLGraphics).DrawEnvironmentBounds(false);

		// Draw Geometry
		if ( !displaySurface.getIGraphics().getJTSGeometries().isEmpty() ) {
			displaySurface.getIGraphics().drawMyJTSGeometries(picking);
		}

		// Draw Static Geometry
		if ( !displaySurface.getIGraphics().getMyJTSStaticGeometries().isEmpty() ) {
			displaySurface.getIGraphics().drawMyJTSStaticGeometries(picking);
		}

		// Draw Image
		if ( !displaySurface.getIGraphics().getImages().isEmpty() ) {
			blendingEnabled = true;
			displaySurface.getIGraphics().drawMyImages(picking);
		}

		// FIXME: When picking = true produes a glitch when clicking on obejt
		if ( !picking ) {
			// Draw String
			if ( !displaySurface.getIGraphics().getStrings().isEmpty() ) {
				displaySurface.getIGraphics().drawMyStrings();
			}

		}
	}

	/**
	 * Draw a given shapefile
	 **/
	public void drawShapeFile() {

		if ( !displaySurface.getIGraphics().getCollections().isEmpty() ) {
			SimpleFeatureCollection myCollection =
				myShapeFileReader.getFeatureCollectionFromShapeFile(myShapeFileReader.store);
			displaySurface.getIGraphics().drawCollection();
			// Adjust the size of the display surface according to the bound of the shapefile.
			displaySurface.setEnvHeight((float) myCollection.getBounds().getHeight());
			displaySurface.setEnvWidth((float) myCollection.getBounds().getWidth());
			if ( !updateEnvDim ) {
				displaySurface.zoomFit();
				updateEnvDim = true;
			}
		}
		return;
	}

	public void DrawCollada() {

		ColladaReader myColReader = new ColladaReader();
		return;
	}

	public void DrawTexture(MyImage img) {
		gl.glTranslated(img.offSet.x, -img.offSet.y, img.offSet.z);
		MyTexture curTexture = myTextures.get(img.image);
		if ( curTexture == null ) { return; }
		// Enable the texture
		gl.glEnable(GL_TEXTURE_2D);
		Texture t = curTexture.texture;
		t.enable(gl);
		t.bind(gl);
		// Reset opengl color. Set the transparency of the image to
		// 1 (opaque).
		gl.glColor4f(1.0f, 1.0f, 1.0f, img.alpha);
		TextureCoords textureCoords;
		textureCoords = t.getImageTexCoords();
		textureTop = textureCoords.top();
		textureBottom = textureCoords.bottom();
		textureLeft = textureCoords.left();
		textureRight = textureCoords.right();
		if ( img.angle != 0 ) {
			gl.glTranslated(img.x + img.width / 2, -(img.y + img.height / 2), 0.0f);
			// FIXME:Check counterwise or not, and do we rotate
			// around the center or around a point.
			gl.glRotatef(-img.angle, 0.0f, 0.0f, 1.0f);
			gl.glTranslated(-(img.x + img.width / 2), +(img.y + img.height / 2), 0.0f);

			gl.glBegin(GL_QUADS);
			// bottom-left of the texture and quad
			gl.glTexCoord2f(textureLeft, textureBottom);
			gl.glVertex3d(img.x, -(img.y + img.height), img.z);
			// bottom-right of the texture and quad
			gl.glTexCoord2f(textureRight, textureBottom);
			gl.glVertex3d(img.x + img.width, -(img.y + img.height), img.z);
			// top-right of the texture and quad
			gl.glTexCoord2f(textureRight, textureTop);
			gl.glVertex3d(img.x + img.width, -img.y, img.z);
			// top-left of the texture and quad
			gl.glTexCoord2f(textureLeft, textureTop);
			gl.glVertex3d(img.x, -img.y, img.z);
			gl.glEnd();
			gl.glTranslated(img.x + img.width / 2, -(img.y + img.height / 2), 0.0f);
			gl.glRotatef(img.angle, 0.0f, 0.0f, 1.0f);
			gl.glTranslated(-(img.x + img.width / 2), +(img.y + img.height / 2), 0.0f);
		} else {
			gl.glBegin(GL_QUADS);
			// bottom-left of the texture and quad
			gl.glTexCoord2f(textureLeft, textureBottom);
			gl.glVertex3d(img.x, -(img.y + img.height), img.z);
			// bottom-right of the texture and quad
			gl.glTexCoord2f(textureRight, textureBottom);
			gl.glVertex3d(img.x + img.width, -(img.y + img.height), img.z);
			// top-right of the texture and quad
			gl.glTexCoord2f(textureRight, textureTop);
			gl.glVertex3d(img.x + img.width, -img.y, img.z);
			// top-left of the texture and quad
			gl.glTexCoord2f(textureLeft, textureTop);
			gl.glVertex3d(img.x, -img.y, img.z);
			gl.glEnd();
		}
		gl.glDisable(GL_TEXTURE_2D);
		gl.glTranslated(-img.offSet.x, img.offSet.y, -img.offSet.z);
	}

	public void InitTexture(BufferedImage image, boolean isDynamic) {

		// Create a OpenGL Texture object from (URL, mipmap, file suffix)
		// need to have an opengl context valide
		// if ( this.context != null ) {
		this.getContext().makeCurrent();
		Texture texture = com.jogamp.opengl.util.texture.awt.AWTTextureIO.newTexture(GLProfile.getDefault(),image, false);
		MyTexture curTexture = new MyTexture();
		curTexture.texture = texture;
		curTexture.isDynamic = isDynamic;
		this.myTextures.put(image, curTexture);
		// }
		// else {
		// // FIXME: See issue 310
		// throw new GamaRuntimeException("JOGLRenderer context is null");
		// }

	}

	// hdviet 27/05/2012
	// add new listener for ArcBall
	// public InputHandler arcBallListener;
	// private GLUquadric quadratic; // Used For Our Quadric
	// hdviet 27/05/2012
	// add attribute to ArcBall model
	private final Matrix4f LastRot = new Matrix4f();
	private final Matrix4f ThisRot = new Matrix4f();
	private final Object matrixLock = new Object();
	private final float[] matrix = new float[16];

	// add function to capture mouse event of ArcBall model
	public void drag(Point mousePoint) {

		Quat4f ThisQuat = new Quat4f();

		arcBall.drag(mousePoint, ThisQuat); // Update End Vector And Get
											// Rotation As Quaternion
		synchronized (matrixLock) {
			ThisRot.setRotation(ThisQuat); // Convert Quaternion Into Matrix3fT
			ThisRot.mul(ThisRot, LastRot); // Accumulate Last Rotation Into This
											// One
		}
	}

	public void startDrag(Point mousePoint) {
		// ArcBall
		synchronized (matrixLock) {
			LastRot.set(ThisRot); // Set Last Static Rotation To Last Dynamic
									// One
		}
		arcBall.click(mousePoint); // Update Start Vector And Prepare For
									// Dragging

	}

	public void reset() {
		synchronized (matrixLock) {
			LastRot.setIdentity(); // Reset Rotation
			ThisRot.setIdentity(); // Reset Rotation
		}
	}

	public void DrawPickableObject() {
		if ( myListener.beginPicking(gl) ) {
			// Need to to do a translation before to draw object and retranslate
			// after.
			// FIXME: need also to apply the arcball matrix to make it work in
			// 3D
			if ( camera.isModelCentered ) {
				gl.glTranslated(-displaySurface.getIGraphics().getEnvWidth() / 2, displaySurface.getIGraphics()
					.getEnvHeight() / 2, 0.0f);
				drawModel(true);

				gl.glTranslated(displaySurface.getIGraphics().getEnvWidth() / 2, -displaySurface.getIGraphics()
					.getEnvHeight() / 2, 0.0f);
			} else {
				drawModel(true);
			}
			displaySurface.getIGraphics().setPickedObjectIndex(myListener.endPicking(gl));
		}

		drawModel(true);

	}

	public BufferedImage getScreenShot() {
		BufferedImage img = null;
		if ( getContext() != null ) {
			this.getContext().makeCurrent();
			img = Screenshot.readToBufferedImage(width, height);
			this.getContext().release();
		} else {}
		return img;

	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public GLContext getContext() {
		// while (!isInitialized) {
		// GuiUtils.debug("JOGLAWTGLRenderer.getContext: waiting");
		// Thread.dumpStack();
		// try {
		// Thread.sleep(10);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		return context;
	}

	public void setContext(GLContext context) {
		this.context = context;
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}
}
