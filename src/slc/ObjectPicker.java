package slc;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ObjectPicker {

	public Pane fullView;
	public Pane objects;
	private Stage stage;
	private Scene scene;
	private Text text;
	
	private static final double WINDOW_SCALE = 1.0;
	private static final double OFFSET = 8.0;
	
	public Pane getView() { return fullView; }
	
	public ObjectPicker() {
		fullView = new Pane();
		
		objects = new Pane();
		objects.setViewOrder(1);
		initObjectGrid();
		
		text = new Text("block");
		text.setFill(Color.WHITE);
		text.setFont(Font.font("SF Pro", 36));
		text.setStroke(Color.BLACK);
		text.relocate(4, 4);
		
		fullView.getChildren().addAll(objects, text);
		fullView.setBackground(new Background(new BackgroundFill(null,null,null)));
		
		Scale scale = new Scale(WINDOW_SCALE, WINDOW_SCALE, 0, 0);
		
		objects.getTransforms().add(scale);
		
		// objects.getBoundsInLocal().getWidth()*scale.getX()+OFFSET, objects.getBoundsInLocal().getHeight()*scale.getY()+OFFSET
		scene = new Scene(fullView, fullView.getBoundsInLocal().getWidth()+scale.getX()*OFFSET, fullView.getBoundsInLocal().getHeight()+scale.getY()*OFFSET, Color.DARKCYAN);
		stage = new Stage(StageStyle.UTILITY);
		stage.setScene(scene);
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		initEvents();
		stage.setTitle("block");
		stage.show();
	}
	/** @params x,y Screen coords */
	public void relocate(double x, double y) {
		stage.setX(x);
		stage.setY(y);
	}
	
	public void initialPosition(double mainStageMaxX, double mainStageMaxY) {
		relocate(mainStageMaxX-scene.getWidth()+50, mainStageMaxY-scene.getHeight()-100);
	}
	
	public void close() {
		if (stage!=null)
			stage.close();
	}
	public void show() {
		if (stage!=null)
			stage.show();
	}
	
	// ########## OBJECT SELECTION ###########
	
	private Bounds[] grid = null;
	
	private void initObjectGrid() {
		int col=0,row=0;
		grid = new Bounds[Drawable.TEXTURES.length];
		for (int i=0; i<Drawable.TEXTURES.length; i++) {
			Drawable d = new Drawable(i, 0, 100,100);
			d.relocate(OFFSET+col*70, OFFSET+row*70);
			grid[i] = Drawable.createHitbox(d.getLayoutX()+objects.getLayoutX(), d.getLayoutY()+objects.getLayoutY(), 64, 64);
			if (d.getImage().getWidth()>100) {
				d.setImage(AssetsManager.INVALID_TEXTURE);
				objects.getChildren().add(d);
			} else {
				objects.getChildren().add(d);
			}
			
			if (++col>=7) {
				row++;
				col = 0;
			}
		}
	}
	
	private int sti = 0;
	private void initEvents() {
		scene.setOnMouseMoved((e)->{
			double mx = e.getX();
			double my = e.getY();
			
			for (int i=0; i<grid.length; i++) {
				if (grid[i].contains(mx, my)) {
					sti = i;
					text.setText(Drawable.TEXTURES[i]+"("+i+")");
					stage.setTitle(Drawable.TEXTURES[i]);
					break;
				}
			}
		});
		stage.focusedProperty().addListener((li) -> {
			stage.setOpacity(stage.isFocused()?0.98:0.75);
		});
		scene.setOnMouseClicked((e)->{
			if (objPickAction!=null && objects.getBoundsInParent().contains(e.getX(),e.getY())) // objects
				objPickAction.run();
		});
	}
	
	public int getObjectAt(double x, double y) {
		for (int i=0; i<grid.length; i++) {
			if (grid[i].contains(x, y))
				return i;
		}
		return 0;
	}
	public int getSelectedObject() {
		return sti;
	}
	
	public Scene getScene() {
		return scene;
	}

	private Runnable objPickAction = null;
	
	public void setObjPickListener(Runnable run) {
		objPickAction = run;
	}
	
}
