package slc;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
/**
 * @author luka
 * 
 * TODO: Napraviti unos za Triggere (triggere najbolje ispisati na kraju programa u outputu)
 * Mozda koristiti TextFieldove za ez unos.
 */
public class Slc extends Application {
    private Pane root;
    private Scene scene;
    private ArrayList<int[]> lvl;
    private boolean inMenu;
    // -------------------------------
    private Label angleSign;
    private Label coordsSign;
    private Label stiSign, solSign, sogSign, ssvSign, ssv2Sign, ssdSign;
    private Label reloadedSign;
    private Label selectedObjTypeSign;
    private Pane infoPane;
    private int sti; // selected texture id
    private int sol; // selected layer
    private int sog; // selected group
    private int ssv; // selected special value
    private int ssv2;// selected special2 value
    private boolean ssd;// selected solid value
    // --------------------------------
    private double cameraX=0.0, cameraY=0.0;
    // --------------------------------
    private Drawable so; // selected object
    private ImageView preview; // object preview
    // --------------------------------
    private ObjectPicker objPicker;
    
    // Triggers
    // [     action  		needsZ        specialValue  removeAfterInteraction  x  y     width           height  (special2) (special3) ]
    TextField trAction;CheckBox trNeedsZ;TextField trSpec; 	CheckBox trRmai; /*preko*/TextField trWidth,trHeight,  trSpec2,  trSpec3;
	Group triggerThings;												// misa i tastature
    
	// Commands (if you want to add custom commands like "box", "area"...)
	TextArea commandArea; // text will go directly from here to save file (most likely)
	Button closeCommandAreaButton;
	Group additionalCommands;
	
    private Pane nodeTreePane;
    private ArrayList<String> nodeTree;
    
    private int soIndex = -1;
    
    private Level objects;
    
    @Override
    public void start(Stage stage) {
        root = new Pane();
        root.setStyle("-fx-background-color: black");
        scene = new Scene(root, 1200, 700, Color.BLACK);
        
        
        AssetsManager.loadImages(4, 4);
        
        initElements();
        initEvents();
        
        drawLevel();
        //drawSheep();
        
        updateLabels();
        positionHUD();
        reposition();
        
        printHelpInfo();
        
        stage.setTitle("Sheep Level Creator v0.3");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((e) -> {
        	objPicker.close();
        });
    }
    
    private void drawLevel(){
        for (int[] data : lvl) {
        	// order:  textureid,  group,  layer,  solid,  angle,  scalex,  scaley
            Drawable drw = new Drawable(data);
            objects.add(drw);
        }
        root.getChildren().add(objects.lvl);
    }
    private void drawHUD(){
        root.getChildren().add(infoPane);
        root.getChildren().add(nodeTreePane);
        
        root.getChildren().add(preview);
    }
    private void initObjectRelatedLabels() {
    	angleSign = new Label("Rotate: 0");
        coordsSign = new Label("xy: 0, 0");
        stiSign = new Label(String.valueOf(sti));
        solSign = new Label(String.valueOf(sol));
        sogSign = new Label(String.valueOf(sog));
        ssvSign = new Label(String.valueOf(ssv));
        ssv2Sign = new Label(String.valueOf(ssv2));
        ssdSign = new Label(String.valueOf(ssv));
        
        infoPane = new Pane(angleSign, coordsSign, stiSign, solSign, sogSign, ssvSign, ssv2Sign, ssdSign);
        infoPane.setViewOrder(-1.0);
        for (Node n : infoPane.getChildren()) {
        	Label sign = (Label) n;
        	sign.setTextFill(Color.WHITE);
        	sign.setFont(Font.font("DejaVu Sans", 20));
        }
    }
    private void initAdditionalCommandNodes() {
    	commandArea = new TextArea();
    	commandArea.setPromptText("Add custom commands here:");
    	commandArea.setPrefHeight(300);
    	commandArea.relocate(50, 4);
    	commandArea.setOnKeyPressed((e) -> {
    		if (e.getCode()==KeyCode.TAB) {
    			hideAdditionalCommands();
    			commandArea.undo();
    		}
    	});
    	
    	additionalCommands = new Group(commandArea);
    }
    private void showAdditionalCommands() {
    	if (!root.getChildren().contains(additionalCommands))
    		root.getChildren().add(additionalCommands);
    	commandArea.requestFocus();
    }
    private void hideAdditionalCommands() {
    	if (root.getChildren().contains(additionalCommands))
    		root.getChildren().remove(additionalCommands);
    	root.requestFocus();
    }
    private void initElements(){
    	initAdditionalCommandNodes();
        LvlLoader lvlLoader = new LvlLoader();
        if (lvlLoader.successLoad()){
            lvl = lvlLoader.getLevel();
            commandArea.setText(lvlLoader.getAdditionalCommands());
        }
        initObjectRelatedLabels();
        
        
        nodeTree = new ArrayList<String>();
        nodeTreePane = new Pane();
        Rectangle rect = new Rectangle(200, 100000);
        rect.setFill(Color.rgb(0,0,0, 0.5));
        rect.relocate(0, 0);
        rect.setViewOrder(1);
        nodeTreePane.getChildren().add(rect);
        loadNodeTree();
        nodeTreePane.setViewOrder(-3);
        
        reloadedSign = new Label("RELOADED LEVEL!");
        selectedObjTypeSign = new Label("OBJECT TYPE CHANGED!");

        preview = new ImageView(AssetsManager.getImage(sti));
        preview.setViewOrder(-2);
        
        objPicker = new ObjectPicker();
        objPicker.resize(scene.getWidth(), scene.getHeight());
        
        objects = new Level();
        
        
        drawHUD();
    }
    
    /**
     * No change on list size
     */
    private void updateNodeTreeLabelsNoNew() {
    	int index = 1;
    	for (String s : nodeTree) {
    		((Label)nodeTreePane.getChildren().get(index++)).setText(s);
    	}
    }
    private void loadNodeTree() {
    	int ycoord = 0;
    	clearNodeTree();
    	for (int[] e : lvl) {
    		String s = LvlLoader.iats(e);
    		
    		Label lab = new Label(s);
    		lab.setFont(new Font("Helvetica Bold", 12));
    		lab.setTextFill(Color.WHITE);
    		lab.relocate(0, ycoord);
    		
    		nodeTree.add(s);
    		nodeTreePane.getChildren().add(lab);
    		ycoord += 14;
    	}
    }
    private void clearNodeTree() {
    	nodeTree.clear();
    	if (nodeTreePane.getChildren().size()>1)
    		nodeTreePane.getChildren().remove(1, nodeTreePane.getChildren().size()); // all except background
    }
    
    private void updateSelectedObject() {
    	if (so!=null && soIndex>=0) {
    		so.updateData(ssd, sol, sog, ssv, ssv2);
    		int[] soData = so.createData();
    		lvl.set(soIndex, soData);
    		nodeTree.set(soIndex, LvlLoader.iats(soData));
    		updateNodeTreeLabelsNoNew();
    	}
    }
    
    private void updateLabels(){
        if (so!=null) {
            coordsSign.setText("x,y: ("+(int)so.getLayoutX()+", "+(int)so.getLayoutY()+")");
            angleSign.setText("Rotate: "+so.getRotate());
        } else {
            coordsSign.setText("x,y: ( , )");
            angleSign.setText("Rotate: 0.0");
        }
        stiSign.setText("txID: "+sti);
        solSign.setText("layer: "+sol);
        sogSign.setText("group: "+sog);
        ssdSign.setText("solid: "+ssd);
        ssvSign.setText("spec: "+ssv);
        ssv2Sign.setText("spec2: "+ssv2);
    }
    
    private int magnetize(double x){
        int xx = (int)Math.round(x/64);
        return xx*64;
    }
    
    private void reloadLevel(){
        drawLevel();
        root.getChildren().add(reloadedSign);
    }
    
    private void addObject(double x, double y) {
    	ssd = Drawable.isSolid(sti);
        Drawable drw = new Drawable(sti, sog, sol, ssd, 0, 100, 100);
        drw.relocate(x,y);
        //drw.setImage(AssetsManager.getImage(0)); debug (removal)
        root.getChildren().add(drw);
        objects.add(drw);
        lvl.add(drw.createData());
        so = drw;
        soIndex = objects.indexOf(drw);
        loadNodeTree();
    }
    private void removeObject(Drawable n) {
    	root.getChildren().remove(n);
        int index = objects.indexOf(n);
        lvl.remove(index);
        objects.remove(index);
        loadNodeTree();
        
        so = null;
        soIndex = -1;
    }

    
    private void initEvents(){
        scene.setOnMousePressed(evt -> {
        	mouseEvent(evt);
        	
            if (root.getChildren().contains(reloadedSign))
                root.getChildren().remove(reloadedSign);
            if (root.getChildren().contains(selectedObjTypeSign))
                root.getChildren().remove(selectedObjTypeSign);
            
            updateLabels();
        });
        scene.setOnScroll(evt -> {
        	if (evt.getX()<nodeTreePane.getLayoutX()) { 
	            if (evt.isShiftDown()){
	                cameraX -= evt.getDeltaX();
	            } else
	                cameraY -= evt.getDeltaY();
	            reposition();
        	} else { // cursor is on the node tree pane
        		nodeTreePane.setLayoutY(nodeTreePane.getLayoutY()+evt.getDeltaY());
        		if (nodeTreePane.getLayoutY()>0) nodeTreePane.setLayoutY(0);
        	}
        });
        scene.setOnKeyPressed(evt -> {
            keyPressEvent(evt);
            updateLabels();
            updateSelectedObject();
        });
        scene.widthProperty().addListener((ce) -> {
        	positionHUD();
        });
        scene.heightProperty().addListener((ce) -> {
        	positionHUD();
        });
    }
    
    private static final int AREA_LEVEL = 0;
    private static final int AREA_NODE_TREE = 1;
    private static final int AREA_PREVIEW_IMAGE = 2;
    private int getClickArea(double x, double y) {
    	if (preview.getBoundsInParent().contains(x,y)) return AREA_PREVIEW_IMAGE;
    	if (nodeTreePane.getBoundsInParent().contains(x,y)) return AREA_NODE_TREE;
    	return AREA_LEVEL;
    }
    
    private void mouseEvent(MouseEvent evt) {
    	switch(getClickArea(evt.getX(),evt.getY())) {
    		case AREA_LEVEL:
    			levelMouseEvent(evt); return;
    		case AREA_PREVIEW_IMAGE:
    			previewImageMouseEvent(evt); return;
    		default:return;
    	}
    }
    
    private void previewImageMouseEvent(MouseEvent evt) {
    	System.out.println("preview clicked");
    	// TODO: block picker u posebnom ekranu
    	objPicker.show();
    }
    
    private void levelMouseEvent(MouseEvent evt) {
    	if (evt.getButton()==MouseButton.PRIMARY && !inMenu){
    		addObject(magnetize(evt.getX()+cameraX-30),magnetize(evt.getY()+cameraY-30));
        } else if (evt.getButton()==MouseButton.SECONDARY){
            ArrayList<Node> toKill = new ArrayList<>();
            for (Node n : objects){
                if (magnetize(n.getLayoutX())==magnetize(evt.getX()+cameraX-30))
                    if (magnetize(n.getLayoutY())==magnetize(evt.getY()+cameraY-30))
                        toKill.add(n);
            }
            so = null;
            soIndex = -1;
            for (Node n : toKill){
                removeObject((Drawable)n);
            }
            if (!toKill.isEmpty()) loadNodeTree(); // ?!
            
        }
    }
    
    private void keyPressEvent(KeyEvent evt) {
    	if (null!=evt.getCode())switch (evt.getCode()) {
        case ESCAPE:
            System.exit(0);
            break;
        case ENTER: // save
            LvlLoader lvlL = new LvlLoader();
            lvlL.save(lvl, commandArea.getText());
            break;
        case BACK_SPACE:
        	if (soIndex > 0) {
            	removeObject(so);
        	}
        	break;
        case R:
            reloadLevel();
            break;
        case Q: // clear level
        	if (!evt.isControlDown()) {
        		if (so!=null) so.setScaleX(-so.getScaleX());
        		break;
        	}
            lvl.clear();
            objects.clear();
            clearNodeTree();
            so = null;
            soIndex = -1;
            break;
        case E:
        	if (so!=null) so.setScaleY(-so.getScaleY());
        	break;
        case RIGHT: // layer
            sol++;
            break;
        case LEFT: // layer
        	sol--;
            break;
        case UP:
        	sog++;
        	break;
        case DOWN:
        	sog--;
        	break;
        case SPACE:
        	ssd = !ssd;
        	break;
        case A: // angle
            if (so!=null){
                if (!evt.isAltDown())
                    so.setRotate(so.getRotate()-1);
                else
                    so.setRotate(so.getRotate()-45);
                lvl.get(lvl.size()-1)[Drawable.COMP_ANGLE]=(int)so.getRotate();
            }
            break;
        case D:
            if (so!=null){
                if (!evt.isAltDown())
                    so.setRotate(so.getRotate()+1);
                else
                    so.setRotate(so.getRotate()+45);
                lvl.get(lvl.size()-1)[Drawable.COMP_ANGLE]=(int)so.getRotate();
            }
            break;
        case Z:
        	if (evt.isMetaDown()) {
        		// TODO: undo
        	}
            if (so!=null) so.setLayoutX(so.getLayoutX()-1);
            break;
        case X:
            if (so!=null) so.setLayoutX(so.getLayoutX()+1);
            break;
        case C:
        	if (so!=null) so.setLayoutY(so.getLayoutY()-1);
        	break;
        case V:
        	if (so!=null) so.setLayoutY(so.getLayoutY()+1);
        	break;
        case TAB:
        	if (root.getChildren().contains(additionalCommands))
        		hideAdditionalCommands();
        	else
        		showAdditionalCommands();
        	break;
        case ADD:
        case PLUS:
        	if (sti+1>=Drawable.TEXTURES.length) break;
        	preview.setImage(AssetsManager.getImage(Drawable.TEXTURES[++sti]+".png", 4, 4));
        	break;
        case SUBTRACT:
        case MINUS:
        	if (sti<=0) break;
        	preview.setImage(AssetsManager.getImage(Drawable.TEXTURES[--sti]+".png", 4, 4));
        	break;
        default:
            // 0 1 2 3 4 5 6 7 8 9  - selected texture id
            for (int i=0;i<10;i++){
                if (evt.getText().equals(String.valueOf(i))){
                    sti=Integer.parseInt(evt.getText());
                    preview.setImage(AssetsManager.getImage(Drawable.TEXTURES[sti]+".png", 4, 4));
                    break;
                }
            }
            break;
    }
    }
    
    private void printHelpInfo() {
    	System.out.println("Controls:");
        System.out.println("ESCAPE: quit program");
        System.out.println("ENTER: save level");
        System.out.println("BACKSPACE: remove selected object");
        System.out.println("R: reload level");
        System.out.println("CTRL+Q: erase everything");
        System.out.println("Q: flip horizontaly (scalex = -scalex)");
        System.out.println("E: flip verticaly (scaley = -scaley)");
        System.out.println("RIGHT: layer++");
        System.out.println("LEFT: layer--");
        System.out.println("UP: group++");
        System.out.println("DOWN: group--");
        System.out.println("SPACE: solid = !solid");
        System.out.println("A: angle-=1 degree");
        System.out.println("ALT+A: angle-=45 degrees");
        System.out.println("D: angle+=1 degree");
        System.out.println("ALT+D: angle+=45 degrees");
        System.out.println("Z: move left 1px");
        System.out.println("X: move right 1px");
        System.out.println("C: move up 1px");
        System.out.println("V: move down 1px");
        System.out.println("TAB: show additional commands area (will go directly into save file)");
        System.out.println("+: texture++");
        System.out.println("-: texture--");
        System.out.println("ANY_DIGIT: texture = n");
        
        System.out.println("note that only the image is flipped. Try flip + rotate and you'll see what I mean");
        System.out.println("note that layer can be max 10");
        
        System.out.println("Trigger format: [room, action, needsZ, specialValue, removeAfterInteraction, x, y, width, height, (special2), (special3)]");
        System.out.println("But type \"tr\" at the start");
    }
    
    private void positionHUD() {
    	stiSign.   relocate(10, 10);
        coordsSign.relocate(10, 30);
        ssdSign.   relocate(10, 50);
        solSign.   relocate(10, 70);
        sogSign.   relocate(10, 90);
        ssvSign.   relocate(10, 110);
        ssv2Sign.  relocate(10, 130);
        angleSign. relocate(10, 150);
        
        preview.   relocate(16, scene.getHeight()-80);
        nodeTreePane. relocate(scene.getWidth()-200, 5);
    }
    
    private void reposition() {
    	objects.lvl.relocate(-cameraX, -cameraY);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
