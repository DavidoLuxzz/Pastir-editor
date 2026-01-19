package slc;

import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
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
 *         TODO: Napraviti unos za Triggere (triggere najbolje ispisati na kraju
 *         programa u outputu) Mozda koristiti TextFieldove za ez unos.
 */
public class Slc extends Application {
    private Pane root;
    private Scene scene;
    private ArrayList<int[]> lvl;
    private boolean inMenu;
    // -------------------------------
    private Label angleSign;
    private Label coordsSign;
    private Label stiSign, solSign, sogSign, ssvSign, ssv2Sign, ssdSign, sbvSign;
    private Slider ssvSlider,ssv2Slider,sbvSlider;
    private Label helperSign;
    private Pane infoPane;
    public static int sti; // selected texture id
    public static int sol; // selected layer
    public static int sog; // selected group
    public static int ssv; // selected hue value
    public static int ssv2;// selected saturation value
    public static int sbv; // selected brightness value
    public static boolean ssd;// selected solid value
    // --------------------------------
    private double cameraX = 0.0, cameraY = 0.0;
    // --------------------------------
    private ArrayList<Drawable> sobjs; // selected objects
    private ArrayList<Integer> soIndices; // so indices
    private ImageView preview; // object preview
    private ColorAdjust soEffect;
    // --------------------------------
    private ObjectPicker objPicker;

    // Triggers
    // [ action needsZ specialValue removeAfterInteraction x y width height
    // (special2) (special3) ]
    TextField trAction;
    CheckBox trNeedsZ;
    TextField trSpec;
    CheckBox trRmai;
    /* preko */TextField trWidth, trHeight, trSpec2, trSpec3;
    Group triggerThings; // misa i tastature

    // Commands (if you want to add custom commands like "box", "area"...)
    TextArea commandArea; // text will go directly from here to save file (most likely)
    Button closeCommandAreaButton;
    Group additionalCommands;

    private Pane nodeTreePane;
    private ArrayList<String> nodeTree;

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
        // drawSheep();

        updateLabels();
        positionHUD();
        reposition();

        // printHelpInfo();

        stage.setTitle("Sheep Level Creator v0.4");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((e) -> {
            objPicker.close();
        });
        objPicker.initialPosition(stage.getX() + stage.getWidth(), stage.getY() + stage.getHeight());
        objects.lvl.getChildren().addListener((ListChangeListener<? super Node>)e -> {
            updateNodeTree();
        });
    }

    private void drawLevel() {
        objects.clear();
        for (int[] data : lvl) {
            // order: textureid, group, layer, solid, angle, scalex, scaley
            Drawable drw = new Drawable(data);
            objects.add(drw);
        }
        if (!root.getChildren().contains(objects.lvl)) root.getChildren().add(objects.lvl);
    }

    private void drawHUD() {
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
        sbvSign = new Label(String.valueOf(sbv));
        ssdSign = new Label(String.valueOf(ssd));
        
        
        ssvSlider = new Slider(-180, 180, 0); // hue
        ssvSlider.setBlockIncrement(1);
        ssvSlider.valueProperty().addListener(e -> {
            if (!ssvSlider.isValueChanging()) return;
            ssv = (int) ssvSlider.getValue();
            updateLabels();
            updateSelectedObjects();
        });
        ssv2Slider = new Slider(-100, 100, 0); // saturation
        ssv2Slider.setBlockIncrement(1);
        ssv2Slider.valueProperty().addListener(e -> {
            if (!ssv2Slider.isValueChanging()) return;
            ssv2 = (int) ssv2Slider.getValue();
            updateLabels();
            updateSelectedObjects();
        });
        sbvSlider = new Slider(-100, 100, 0); // brightness
        sbvSlider.setBlockIncrement(1);
        sbvSlider.valueProperty().addListener(e -> {
            if (!sbvSlider.isValueChanging()) return;
            sbv = (int) sbvSlider.getValue();
            updateLabels();
            updateSelectedObjects();
        });

        infoPane = new Pane(angleSign, coordsSign, stiSign, solSign, sogSign, ssvSign, ssv2Sign, sbvSign, ssdSign);
        infoPane.setViewOrder(-1.0);
        for (Node n : infoPane.getChildren()) {
            Label sign = (Label) n;
            sign.setTextFill(Color.WHITE);
            sign.setFont(Font.font("DejaVu Sans", 20));
        }
        infoPane.getChildren().addAll(ssvSlider,ssv2Slider,sbvSlider);
    }

    private void initAdditionalCommandNodes() {
        commandArea = new TextArea();
        commandArea.setPromptText("Add custom commands here:");
        commandArea.setPrefHeight(300);
        commandArea.relocate(300, 40);
        commandArea.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.TAB) {
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

    private void initElements() {
        sobjs = new ArrayList<>();
        soIndices = new ArrayList<>();
        initAdditionalCommandNodes();
        LvlLoader lvlLoader = new LvlLoader();
        if (lvlLoader.successLoad()) {
            lvl = lvlLoader.getLevel();
            commandArea.setText(lvlLoader.getAdditionalCommands());
        }
        initObjectRelatedLabels();

        nodeTree = new ArrayList<String>();
        nodeTreePane = new Pane();
        Rectangle rect = new Rectangle(200, 100000);
        rect.setFill(Color.rgb(0, 0, 0, 0.5));
        rect.relocate(0, 0);
        rect.setViewOrder(1);
        nodeTreePane.getChildren().add(rect);
        loadNodeTree();
        nodeTreePane.setViewOrder(-3);

        helperSign = new Label("Reloaded level");
        helperSign.setFont(Font.font(36));
        helperSign.setTextFill(Color.WHITE);
        helperSign.setVisible(false);
        root.getChildren().add(helperSign);

        preview = new ImageView(AssetsManager.getImage(sti));
        preview.setViewOrder(-2);

        objPicker = new ObjectPicker();
        objPicker.setObjPickListener(() -> {
            sti = objPicker.getSelectedObject(); // where mouse currently is
            preview.setImage(AssetsManager.getImage(sti));
        });

        objects = new Level();
        objects.lvl.setViewOrder(2); // pozadi

        soEffect = new ColorAdjust();
        soEffect.setBrightness(+0.4);
        soEffect.setSaturation(+0.1);

        drawHUD();
    }

    // /**
    //  * No change on list size
    //  */
    // private void updateNodeTreeLabelsNoNew() {
    //     int index = 1;
    //     for (String s : nodeTree) {
    //         ((Label) nodeTreePane.getChildren().get(index++)).setText(s);
    //     }
    // }

    private static final int NODE_TREE_Y_RAZMAK = 14;
    /**
     * Updates full node tree.
     * Steps:
     *  - check if object list has GROWN (doesn't remove any labels, only creates them)
     *  - updates text of all labels
     *  - hides all other labels
     */
    private void updateNodeTree(){
        // new responsibility: lvl (ArrayList<int[]>)
        int ycoord = nodeTree.size()*NODE_TREE_Y_RAZMAK;
        while (objects.lvl.getChildren().size()>nodeTree.size()){
            Label lab = new Label("fix me");
            lab.setFont(new Font("Helvetica Bold", 12));
            lab.setTextFill(Color.WHITE);
            lab.relocate(0, ycoord);

            nodeTree.add("fix me");
            nodeTreePane.getChildren().add(lab);
            ycoord += NODE_TREE_Y_RAZMAK;
        }
        for (int i=0; i<objects.lvl.getChildren().size(); i++){
            int[] data = ((Drawable)objects.lvl.getChildren().get(i)).createData();
            
            if (i>=lvl.size())
                lvl.add(data);
            else
                lvl.set(i, data);
            
            
            String s = LvlLoader.iats(data);
            ((Label) nodeTreePane.getChildren().get(i+1)).setText(s);
            ((Label) nodeTreePane.getChildren().get(i+1)).setVisible(true);
        }
        for (int i=objects.lvl.getChildren().size(); i<nodeTree.size(); i++){
            nodeTreePane.getChildren().get(i+1).setVisible(false);
        }
        while (lvl.size() > objects.lvl.getChildren().size()){
            lvl.removeLast();
        }
    }
    /**
     * Creates full node tree
     */
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
            ycoord += NODE_TREE_Y_RAZMAK;
        }
    }

    private void clearNodeTree() {
        nodeTree.clear();
        if (nodeTreePane.getChildren().size() > 1)
            nodeTreePane.getChildren().remove(1, nodeTreePane.getChildren().size()); // all except background
    }

    private void updateSelectedObjects() {
        for (Drawable so : sobjs){
            int soIndex = objects.indexOf(so); // soIndices.get(index++);
            if (so != null && soIndex >= 0) {
                so.updateData(ssd, sol, sog, ssv, ssv2, sbv);
            }
        }
        updateNodeTree();
    }
    
    private void updateObjectsAndLabels() {
        updateSelectedObjects();
        updateLabels();
    }

    private void updateLabels() {
        if (!sobjs.isEmpty()) {
            if (sobjs.size()>1)
                coordsSign.setText("count: "+sobjs.size());
            else {
                Drawable so = sobjs.getFirst();
                coordsSign.setText("x,y: (" + (int) so.getLayoutX() + ", " + (int) so.getLayoutY() + ")");
                angleSign.setText("Rotate: " + so.getRotate());
            }
        } else {
            coordsSign.setText("x,y: ( , )");
            angleSign.setText("Rotate: 0.0");
        }
        stiSign.setText("txID: " + sti);
        solSign.setText("layer: " + sol);
        sogSign.setText("group: " + sog);
        ssdSign.setText("solid: " + ssd);
        ssvSign.setText("Hue: " + ssv);
        ssv2Sign.setText("Saturation: " + ssv2);
        sbvSign.setText("Brightness: "+sbv);
    }

    private int magnetize(double x) {
        int xx = (int) Math.round(x / 64);
        return xx * 64;
    }

    private void reloadLevel() {
        drawLevel();
        helperSign.setText("Reloaded Level");
        helperSign.setVisible(true);
    }
    private static final int MAX_SELECTION_COUNT = 100;
    private void addSelection(Drawable drw) {
        if (sobjs.size()>=MAX_SELECTION_COUNT) return;
        sobjs.add(drw);
        soIndices.add(objects.indexOf(drw));
        drw.setTemporaryTint(soEffect);
        sti = drw.getTexture();
        preview.setImage(AssetsManager.getImage(sti));
        sog = drw.getGroup();
        sol = drw.getLayer();
        ssd = drw.isSolid();
        ssv = drw.getTintHue();
        ssv2 = drw.getTintSaturation();
        updateLabels();
        ssvSlider.setValue(ssv);
        ssv2Slider.setValue(ssv2);
        sbvSlider.setValue(sbv);
    }
    /**
     * Single select (one drawable)
     * @param drw
     */
    private void select(Drawable drw) {
        deselectForNextSelect();
        if (sobjs.isEmpty())
            sobjs.add(drw);
        else
            sobjs.set(0, drw);
        if (soIndices.isEmpty())
            soIndices.add(objects.indexOf(drw));
        else
            soIndices.set(0, objects.indexOf(drw));
        drw.setTemporaryTint(soEffect);
        sti = drw.getTexture();
        preview.setImage(AssetsManager.getImage(sti));
        sog = drw.getGroup();
        sol = drw.getLayer();
        ssd = drw.isSolid();
        ssv = drw.getTintHue();
        ssv2 = drw.getTintSaturation();
        updateLabels();
        ssvSlider.setValue(ssv);
        ssv2Slider.setValue(ssv2);
        sbvSlider.setValue(sbv);
    }
    /**
     * Deselect for single select
     */
    private void deselectForNextSelect() {
        for (Drawable so : sobjs){
            if (so == null)
                continue;
            so.setTemporaryTint(null);
        }
        sobjs.clear();
        soIndices.clear();
    }
    
    private void deselect() { // full deselect
        deselectForNextSelect();
        lastSelectedID = -1;
        sog = 0; sol = 0; ssd = false; ssv = 0; ssv2 = 0;
        updateLabels();
    }

    private Drawable addObject(int[] data) {
        Drawable drw = new Drawable(data);
        // drw.setImage(AssetsManager.getImage(0)); debug (removal)
        // root.getChildren().add(drw);
        objects.add(drw);
        // select(drw);
        updateNodeTree();
        return drw;
    }

    private Drawable addObject(double x, double y) {
        ssd = Drawable.isSolid(sti);
        Drawable drw = new Drawable(sti, sog, sol, ssd, 0, 100, 100, ssv,ssv2,sbv);
        drw.relocate(x, y);
        // drw.setImage(AssetsManager.getImage(0)); debug (removal)
        // root.getChildren().add(drw);
        objects.add(drw);
        // select(drw);
        updateNodeTree();
        return drw;
    }

    private void duplicateObjects(Collection<? extends Drawable> n) {
        ArrayList<Drawable> next = new ArrayList<>();
        for (Drawable so : n) {
            Drawable drw = addObject(so.createData());
            // objects.add(drw);
            // lvl.add(drw.createData());
            // select(drw);
            next.add(drw);
        }
        deselect();
        for (Drawable nd : next){
            addSelection(nd);
        }
    }

    private void removeObject(Drawable n) {
        // root.getChildren().remove(n);
        int index = objects.indexOf(n);
        objects.remove(index);
        updateNodeTree();
    }

    private void removeAll(Collection<? extends Drawable> objs) {
        objects.lvl.getChildren().removeAll(objs);
    }

    private double mouseLastX, mouseLastY;
    private boolean mouseMoveFlag = false;
    private double mouseDeltaX, mouseDeltaY;

    private void initEvents() {
        scene.setOnMouseReleased(evt -> {
            if (!mouseMoveFlag) {
                mouseEvent(evt);
    
                updateLabels();
            }
            mouseDeltaX = 0;
            mouseDeltaY = 0;
            mouseMoveFlag = false;
            helperSign.setVisible(false);
        });
        scene.setOnMouseDragged(evt -> {
            double dx = evt.getX() - mouseLastX;
            double dy = evt.getY() - mouseLastY;
            mouseDeltaX += dx;
            mouseDeltaY += dy;
            if (Math.abs(mouseDeltaX) > 20 || Math.abs(mouseDeltaY) > 20) {
                mouseMoveFlag = true;
            }
            if (!evt.isShiftDown()) {
                if (evt.isPrimaryButtonDown()) {
                    if (mouseMoveFlag) {
                        cameraX -= dx;
                        cameraY -= dy;
                        reposition();
                    }
                }
            } else {
                if (evt.isPrimaryButtonDown()){
                    for (Node drw : objects){
                        if (drw.getBoundsInParent().contains(evt.getX()+cameraX, evt.getY()+cameraY)) {
                            if (!sobjs.contains(drw))
                                addSelection((Drawable)drw);
                        }
                    }
                }
            }
            
            mouseLastX = evt.getX();
            mouseLastY = evt.getY();
        });
        scene.setOnMouseMoved(evt -> {
            mouseLastX = evt.getX();
            mouseLastY = evt.getY();
        });
        scene.setOnScroll(evt -> {
            if (evt.getX() < nodeTreePane.getLayoutX()) {
                if (evt.isShiftDown()) {
                    cameraX -= evt.getDeltaX();
                } else
                    cameraY -= evt.getDeltaY();
                reposition();
            } else { // cursor is on the node tree pane
                nodeTreePane.setLayoutY(nodeTreePane.getLayoutY() + evt.getDeltaY());
                if (nodeTreePane.getLayoutY() > 0)
                    nodeTreePane.setLayoutY(0);
            }
        });
        scene.setOnKeyPressed(evt -> {
            keyPressEvent(evt);
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
    private static final int AREA_OBJECT_INFO = 3;

    private int getClickArea(double x, double y) {
        if (preview.getBoundsInParent().contains(x, y))
            return AREA_PREVIEW_IMAGE;
        if (nodeTreePane.getBoundsInParent().contains(x, y))
            return AREA_NODE_TREE;
        if (infoPane.getBoundsInParent().contains(x, y))
            return AREA_OBJECT_INFO;
        return AREA_LEVEL;
    }

    private int getSmartClickArea(double x, double y) {
        if (!infoPane.isVisible()) return AREA_LEVEL;
        else return getClickArea(x, y);
    }

    private void mouseEvent(MouseEvent evt) {
        switch (getSmartClickArea(evt.getX(), evt.getY())) {
        case AREA_LEVEL:
            levelMouseEvent(evt);
            return;
        case AREA_PREVIEW_IMAGE:
            previewImageMouseEvent(evt);
            return;
        case AREA_OBJECT_INFO:
            objInfoMouseEvent(evt);
            return;
        default:
            return;
        }
    }

    private void objInfoMouseEvent(MouseEvent evt) {
        int dv = 0;
        if (evt.getButton() == MouseButton.PRIMARY)
            dv = 1;
        else if (evt.getButton() == MouseButton.SECONDARY)
            dv = -1;
        else
            return;
        double x = evt.getX();
        double y = evt.getY();
        if (ssdSign.getBoundsInParent().contains(x, y))
            ssd = !ssd;
        else if (solSign.getBoundsInParent().contains(x, y))
            sol += dv;
        else if (sogSign.getBoundsInParent().contains(x, y))
            sog += dv;
        else if (ssvSign.getBoundsInParent().contains(x, y)) {
            ssv += dv;
            ssvSlider.setValue(ssv);
        } else if (ssv2Sign.getBoundsInParent().contains(x, y)) {
            ssv2 += dv;
            ssv2Slider.setValue(ssv2);
        } else if (sbvSign.getBoundsInParent().contains(x, y)) {
            sbv += dv;
            sbvSlider.setValue(sbv);
        } else
            return;
        updateObjectsAndLabels();
    }

    private void previewImageMouseEvent(MouseEvent evt) {
        objPicker.show();
    }
    private int lastSelectedID = -1;
    /**
     * Called when genuine click or release (not drag and move)
     * @param evt
     */
    private void levelMouseEvent(MouseEvent evt) {
        if (evt.getButton() == MouseButton.PRIMARY && !inMenu) {
            if (!evt.isShiftDown()) {
                select(addObject(magnetize(evt.getX() + cameraX - 30), magnetize(evt.getY() + cameraY - 30)));
            } else {
                int firstID=-1;
                int i=0;
                for (Node n : objects) {
                    if (n.getBoundsInParent().contains(evt.getX()+cameraX, evt.getY()+cameraY)) {
                        if (i>lastSelectedID) {
                            select((Drawable)n);
                            lastSelectedID = i;
                            return; // return, a ne break
                        }
                        if (firstID<0) {
                            firstID = i;
                        }
                    }
                    i++;
                }
                if (firstID>0) {
                    select(objects.get(firstID));
                    lastSelectedID = firstID;
                }
            }
        } else if (evt.getButton() == MouseButton.SECONDARY) {
            ArrayList<Node> toKill = new ArrayList<>();
            for (Node n : objects) {
                if (n.getBoundsInParent().contains(evt.getX()+cameraX, evt.getY()+cameraY))
                    toKill.add(n);
            }
            deselect();
            for (Node n : toKill) {
                removeObject((Drawable) n);
            }
            if (!toKill.isEmpty())
                loadNodeTree(); // ?!

        }
    }

    private void keyPressEvent(KeyEvent evt) {
        if (null != evt.getCode())
            switch (evt.getCode()) {
            case ESCAPE:
                System.exit(0);
                break;
            case F1:
                toggleHUD();
                break;
            case F2: // debug dugme
                System.out.println(sobjs.size());
                break;
            case ENTER: // save
                LvlLoader lvlL = new LvlLoader();
                helperSign.setText(lvlL.save(lvl, commandArea.getText())?("File "+LvlLoader.filename+" saved!"):"Problem saving file!");
                helperSign.setVisible(true);
                break;
            case BACK_SPACE:
                removeAll(sobjs);
                break;
            case R:
                deselect();
                reloadLevel();
                break;
            case Q: // clear level
                if (!evt.isControlDown()) {
                    for (Drawable so : sobjs)
                        if (so != null)
                            so.setScaleX(-so.getScaleX());
                    break;
                }
                deselect();
                objects.clear();
                clearNodeTree();
                break;
            case E:
                for (Drawable so : sobjs){
                    if (so != null) {
                        so.setScaleY(-so.getScaleY());
                        updateObjectsAndLabels();
                    }
                }
                break;
            case RIGHT: // layer
                sol++;
                updateObjectsAndLabels();
                break;
            case LEFT: // layer
                sol--;
                updateObjectsAndLabels();
                break;
            case UP:
                sog++;
                updateObjectsAndLabels();
                break;
            case DOWN:
                sog--;
                updateObjectsAndLabels();
                break;
            case SPACE:
                ssd = !ssd;
                updateObjectsAndLabels();
                break;
            case Z: // angle
                int index = 0;
                for (Drawable so : sobjs) {
                    if (so != null) {
                        if (evt.isAltDown())
                            so.setRotate(so.getRotate() - 1);
                        else if (evt.isShiftDown())
                            so.setRotate(so.getRotate() - 45);
                        else
                            so.setRotate(so.getRotate() - 90);
                        updateObjectsAndLabels();
                    }
                }
                updateNodeTree();
                break;
            case X:
                index = 0;
                for (Drawable so : sobjs) {
                    if (so != null) {
                        if (evt.isAltDown())
                            so.setRotate(so.getRotate() + 1);
                        else if (evt.isShiftDown())
                            so.setRotate(so.getRotate() + 45);
                        else
                            so.setRotate(so.getRotate() + 90);
                        updateObjectsAndLabels();
                    }
                }
                updateNodeTree();
                break;
            case W: // move
                for (Drawable so : sobjs) {
                    if (so != null) {
                        so.setLayoutY(so.getLayoutY() - (evt.isAltDown() ? 1 : (evt.isShiftDown() ? 8 : 64)));
                        updateObjectsAndLabels();
                    }
                }
                break;
            case S:
                for (Drawable so : sobjs) {
                    if (so != null) {
                        so.setLayoutY(so.getLayoutY() + (evt.isAltDown() ? 1 : (evt.isShiftDown() ? 8 : 64)));
                        updateObjectsAndLabels();
                    }
                }
                break;
            case A:
                for (Drawable so : sobjs) {
                    if (so != null) {
                        so.setLayoutX(so.getLayoutX() - (evt.isAltDown() ? 1 : (evt.isShiftDown() ? 8 : 64)));
                        updateObjectsAndLabels();
                    }
                }
                break;
            case D:
                if (evt.isControlDown()) {
                    deselect();
                } else if (evt.isMetaDown()) {
                    // duplicate
                    duplicateObjects(sobjs);
                } else {
                    for (Drawable so : sobjs) {
                        if (so != null) {
                            so.setLayoutX(so.getLayoutX() + (evt.isAltDown() ? 1 : (evt.isShiftDown() ? 8 : 64)));
                            updateObjectsAndLabels();
                        }
                    }
                }
                break;
            case TAB:
                if (root.getChildren().contains(additionalCommands))
                    hideAdditionalCommands();
                else
                    showAdditionalCommands();
                break;
            case ADD:
            case PLUS:
                if (sti + 1 >= Drawable.TEXTURES.length)
                    break;
                preview.setImage(AssetsManager.getImage(++sti));
                break;
            case SUBTRACT:
            case MINUS:
                if (sti <= 0)
                    break;
                preview.setImage(AssetsManager.getImage(--sti));
                break;
            default:
                // 0 1 2 3 4 5 6 7 8 9 - selected texture id
                for (int i = 0; i < 10; i++) {
                    if (evt.getText().equals(String.valueOf(i))) {
                        sti = Integer.parseInt(evt.getText());
                        preview.setImage(AssetsManager.getImage(sti));
                        break;
                    }
                }
                break;
            }
    }

    public static void printHelpInfo() {
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

        System.out.println(
                "Trigger format: [room, action, needsZ, specialValue, removeAfterInteraction, x, y, width, height, (special2), (special3)]");
        System.out.println("But type \"tr\" at the start");
    }

    private void positionHUD() {
        stiSign.relocate(10, 10);
        coordsSign.relocate(10, 30);
        ssdSign.relocate(10, 50);
        solSign.relocate(10, 70);
        sogSign.relocate(10, 90);
        // hsb
        ssvSign.relocate(10, 110);
        ssvSlider.relocate(160, 115);
        ssv2Sign.relocate(10, 130);
        ssv2Slider.relocate(160, 135);
        sbvSign.relocate(10, 150);
        sbvSlider.relocate(160, 155);
        
        angleSign.relocate(10, 170);

        preview.relocate(16, scene.getHeight() - 80);
        nodeTreePane.relocate(scene.getWidth() - 200, 5);
        
        helperSign.relocate(100, scene.getHeight()-60);
    }

    private void hideHUD() {
        infoPane.setVisible(false);
        nodeTreePane.setVisible(false);
        preview.setVisible(false);
    }
    private void showHUD() {
        infoPane.setVisible(true);
        nodeTreePane.setVisible(true);
        preview.setVisible(true);
    }
    private void toggleHUD() {
        if (infoPane.isVisible())
            hideHUD();
        else
            showHUD();
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
