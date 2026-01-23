package slc;

import java.util.Collection;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Display {
    private Pane root;
    private Scene scene;
    private Stage stage;
    
    private ImageView staticBlur;
    private Rectangle opacityEffect;
    public static final int DEFAULT_WIDTH  = 1200;
    public static final int DEFAULT_HEIGHT = 700;
    
    private double camX, camY;

    public Display(){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Display(int width, int height){
        root = new Pane();
        root.setBackground(new Background(new BackgroundFill(null, null, null)));
        scene = new Scene(root, width, height, Color.BLACK);
    }
    public Scene getScene(){
        return scene;
    }
    public Pane getRoot(){
        return root;
    }
    public void setStage(Stage stage){
        this.stage = stage;
        stage.setScene(scene);
        stage.setRenderScaleX(2.0);
    }
    
    public boolean contains(Node n){
        return root.getChildren().contains(n);
    }
    public void addAll(Node... e){
        root.getChildren().addAll(e);
    }
    public void addAll(Collection<? extends Node> e){
        root.getChildren().addAll(e);
    }
    public void add(Node n){
        root.getChildren().add(n);
    }
    public void clear(){
        root.getChildren().clear();
        root.getChildren().addAll(staticBlur, opacityEffect);
    }
    public void remove(Node n){
        root.getChildren().remove(n);
    }
    public int indexOf(Node n){
        return root.getChildren().indexOf(n);
    }
    
    public double getWidth(){
        return scene.getWidth();
    }
    public double getHeight(){
        return scene.getHeight();
    }

    public void initDisplayEvents() {
        stage.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.floatValue()!=oldValue.floatValue()) {
                root.setLayoutX( (newValue.floatValue()-1024)/2 );
            }
        });
    }

    public Stage getStage(){
        return stage;
    }
    
    public void moveCamera(double dx, double dy){
    	// camera coordinates
        camX -= dx;
        camY -= dy;
    }
    public double getCameraX() {
    	return camX;
    }
    public double getCameraY() {
    	return camY;
    }
    public void setCameraX(double cx) {
        camX = cx;
    }
    public void setCameraY(double cy) {
        camY = cy;
    }

    public void setTitle(String title) {
        stage.setTitle(title);
    }
    public String getTitle() {
        return stage.getTitle();
    }

    public void show(){
        stage.show();
    }
    public void close(){
        stage.close();
    }
} 
