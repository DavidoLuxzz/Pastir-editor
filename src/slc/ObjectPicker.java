package slc;

import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ObjectPicker {

	public Pane fullView;
	public Pane objects;
	private Stage stage;
	private Scene scene;
	
	private static final double WINDOW_SCALE = 1.0;
	private static final double OFFSET = 8.0;
	
	public Pane getView() { return fullView; }
	
	public ObjectPicker() {
		fullView = new Pane();
		objects = new Pane();
		objects.setViewOrder(1);
		initObjectGrid();
		
		fullView.getChildren().addAll(objects);
		fullView.setViewOrder(-4); // basicly on top of everything
		fullView.setBackground(new Background(new BackgroundFill(null,null,null)));
		
		Scale scale = new Scale(WINDOW_SCALE, WINDOW_SCALE, 0, 0);
		
		objects.getTransforms().add(scale);
		
		scene = new Scene(fullView, objects.getBoundsInLocal().getWidth()*scale.getX()+OFFSET, objects.getBoundsInLocal().getHeight()*scale.getY()+OFFSET, Color.DARKCYAN);
		stage = new Stage(StageStyle.UTILITY);
		stage.setScene(scene);
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.setOpacity(0.75);
		stage.show();
	}
	
	public void close() {
		if (stage!=null)
			stage.close();
	}
	public void show() {
		if (stage!=null)
			stage.show();
	}
	
	private void initObjectGrid() {
		int col=0,row=0;
		for (int i=0; i<Drawable.TEXTURES.length; i++) {
			Drawable d = new Drawable(i, 0, 0, false, 0, 100, 100);
			d.relocate(OFFSET+col*70, OFFSET+row*70);
			if (d.getImage().getWidth()>100) {
				d.setImage(AssetsManager.getImage(Drawable.TEXTURE_BLACK));
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
	
	public void resize(double sceneWidth, double sceneHeight) {
		fullView.relocate(0, 0);
		// fullView.relocate(0, 100);
		
	}
	
	public void scroll(ScrollEvent evt) {
		
	}
	
	public int getObjectAt(double x, double y) {
		return 0;
	}

}
