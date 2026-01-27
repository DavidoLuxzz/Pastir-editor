package slc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author luka
 * 
 *         TODO: Napraviti unos za Triggere (triggere najbolje ispisati na kraju
 *         programa u outputu) Mozda koristiti TextFieldove za ez unos.
 * 
 *         TODO: Napraviti interface za unos novih tekstura.
 * 
 *         TODO: Undo, Redo
 */
public class Slc extends Application {
    public static ArrayList<int[]> lvl;
    // -------------------------------
    public static int sti; // selected texture id
    public static int sol; // selected layer
    public static int sog; // selected group
    public static int ssv; // selected hue value
    public static int ssv2;// selected saturation value
    public static int sbv; // selected brightness value
    public static boolean ssd;// selected solid value
    public static HUD hud;
    // --------------------------------
    static Display root;
    // --------------------------------
    public static ArrayList<Drawable> sobjs; // selected objects
    public static ArrayList<Integer> soIndices; // so indices
    private ColorAdjust soEffect;
    // --------------------------------
    private ObjectPicker objPicker;
    // --------------------------------
    private FileChooser fileChooser;
    // --------------------------------
    private static final int MAX_EDITS = 3;
    private Edit[] changes;
    private int currentChangeIndex = 0;

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

    public static Level objects;
    public static LvlLoader lvlLoader;

    @Override
    public void start(Stage stage) {
        root = new Display();
        root.setStage(stage);

        AssetsManager.loadImages(4, 4);

        loadLevel();
        initElements();
        initEvents();

        drawLevel();

        hud.updateLabels();
        hud.positionHUD();
        reposition();

        // printHelpInfo();

        root.setTitle("Sheep Level Creator v0.4");
        root.show();
// contextMenu.show(stage, 500, 400);
        stage.setOnCloseRequest((e) -> {
            objPicker.close();
        });
        objPicker.initialPosition(stage.getX() + stage.getWidth(), stage.getY() + stage.getHeight());
        objects.lvl.getChildren().addListener((ListChangeListener<? super Node>)e -> {
            hud.updateNodeTree();
        });
    }

    private void drawLevel() {
        objects.clear();
        for (int[] data : lvl) {
            // order: textureid, group, layer, solid, angle, scalex, scaley
            Drawable drw = new Drawable(data);
            objects.add(drw);
        }
        if (!root.contains(objects.lvl)) root.add(objects.lvl);
    }

    

    

    private void initAdditionalCommandNodes() {
        commandArea = new TextArea(lvlLoader.getAdditionalCommands());
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
        if (!root.contains(additionalCommands))
            root.add(additionalCommands);
        commandArea.requestFocus();
    }

    private void hideAdditionalCommands() {
        if (root.contains(additionalCommands))
            root.remove(additionalCommands);
        root.getRoot().requestFocus();
    }

    private void loadLevel() {
        fileChooser = new FileChooser();
        lvlLoader = new LvlLoader(fileChooser.showOpenDialog(root.getStage()));
        if (lvlLoader.successLoad()) {
            lvl = lvlLoader.getLevel();
        }
    }

    

    private void initElements() {
        changes = new Edit[MAX_EDITS];
        sobjs = new ArrayList<>();
        soIndices = new ArrayList<>();
        initAdditionalCommandNodes();
        hud = new HUD();

        objPicker = new ObjectPicker();
        objPicker.setObjPickListener(() -> {
            sti = objPicker.getSelectedObject(); // where mouse currently is
            hud.setPreviewImage(AssetsManager.getImage(sti));
        });

        objects = new Level();
        objects.lvl.setViewOrder(2); // pozadi

        soEffect = new ColorAdjust();
        soEffect.setBrightness(+0.4);
        soEffect.setSaturation(+0.1);

        hud.drawHUD();
    }

    

    public static void updateSelectedObjects() {
        for (Drawable so : sobjs){
            int soIndex = objects.indexOf(so); // soIndices.get(index++);
            if (so != null && soIndex >= 0) {
                so.updateData(ssd, sol, sog, ssv, ssv2, sbv);
            }
        }
        hud.updateNodeTree();
    }
    
    public static void updateObjectsAndLabels() {
        updateSelectedObjects();
        hud.updateLabels();
    }

    public static int magnetize(double x) {
        int xx = (int) Math.round(x / 64);
        return xx * 64;
    }

    private void reloadLevel() {
        drawLevel();
        hud.getHelperSign().setText("Reloaded Level");
        hud.getHelperSign().setVisible(true);
    }
    private static final int MAX_SELECTION_COUNT = 100;
    private void addSelection(Drawable drw) {
        if (sobjs.size()>=MAX_SELECTION_COUNT) return;
        sobjs.add(drw);
        soIndices.add(objects.indexOf(drw));
        drw.setTemporaryTint(soEffect);
        sti = drw.getTexture();
        hud.setPreviewImage(AssetsManager.getImage(sti));
        sog = drw.getGroup();
        sol = drw.getLayer();
        ssd = drw.isSolid();
        ssv = drw.getTintHue();
        ssv2 = drw.getTintSaturation();
        hud.updateLabels();
        hud.ssvSlider.setValue(ssv);
        hud.ssv2Slider.setValue(ssv2);
        hud.sbvSlider.setValue(sbv);
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
        hud.setPreviewImage(AssetsManager.getImage(sti));
        sog = drw.getGroup();
        sol = drw.getLayer();
        ssd = drw.isSolid();
        ssv = drw.getTintHue();
        ssv2 = drw.getTintSaturation();
        hud.updateLabels();
        hud.ssvSlider.setValue(ssv);
        hud.ssv2Slider.setValue(ssv2);
        hud.sbvSlider.setValue(sbv);
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
        hud.updateLabels();
    }

    private Drawable addObject(int[] data) {
        Drawable drw = new Drawable(data);
        // drw.setImage(AssetsManager.getImage(0)); debug (removal)
        // root.add(drw);
        objects.add(drw);
        // select(drw);
        hud.updateNodeTree();
        return drw;
    }

    private Drawable addObject(double x, double y) {
        ssd = Drawable.isSolid(sti);
        Drawable drw = new Drawable(sti, sog, sol, ssd, 0, 100, 100, ssv,ssv2,sbv);
        drw.relocate(x, y);
        // drw.setImage(AssetsManager.getImage(0)); debug (removal)
        // root.add(drw);
        objects.add(drw);
        // select(drw);
        hud.updateNodeTree();
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
        // root.remove(n);
        int index = objects.indexOf(n);
        objects.remove(index);
        hud.updateNodeTree();
    }

    private void removeAll(Collection<? extends Drawable> objs) {
        objects.lvl.getChildren().removeAll(objs);
    }


    private void undoLastEdit(){
        int index = currentChangeIndex-1;
        if (index<0) return; // no edits
        changes[index].undo();
        currentChangeIndex--;
    }

    private void redoLastUndo(){
        if (currentChangeIndex>=MAX_EDITS) return; // was no undo before redo (no avaiable redo)
        if (changes[currentChangeIndex]!=null){
            changes[currentChangeIndex].redo();
            currentChangeIndex++;
        }
    }





    private double mouseLastX, mouseLastY;
    private boolean mouseMoveFlag = false;
    private double mouseDeltaX, mouseDeltaY;

    private void initEvents() {
        root.getScene().setOnMouseReleased(evt -> {
            if (!mouseMoveFlag) {
                mouseEvent(evt);
    
                hud.updateLabels();
            }
            mouseDeltaX = 0;
            mouseDeltaY = 0;
            mouseMoveFlag = false;
            hud.getHelperSign().setVisible(false);
        });
        root.getScene().setOnMouseDragged(evt -> {
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
                        root.moveCamera(dx, dy);
                        reposition();
                    }
                }
            } else {
                if (evt.isPrimaryButtonDown()){
                    for (Node drw : objects){
                        if (drw.getBoundsInParent().contains(evt.getX()+root.getCameraX(), evt.getY()+root.getCameraY())) {
                            if (!sobjs.contains(drw))
                                addSelection((Drawable)drw);
                        }
                    }
                }
            }
            
            mouseLastX = evt.getX();
            mouseLastY = evt.getY();
        });
        root.getScene().setOnMouseMoved(evt -> {
            mouseLastX = evt.getX();
            mouseLastY = evt.getY();
        });
        root.getScene().setOnScroll(evt -> {
            if (evt.getX() < hud.nodeTreePane.getLayoutX()) {
                if (evt.isShiftDown()) {
                    root.moveCamera(evt.getDeltaX(), 0);
                } else
                    root.moveCamera(0, evt.getDeltaY());
                reposition();
            } else { // cursor is on the node tree pane
                hud.scrollNodeTree(evt);
            }
        });
        root.getScene().setOnKeyPressed(evt -> {
            keyPressEvent(evt);
        });
        root.getScene().widthProperty().addListener((ce) -> {
            hud.positionHUD();
        });
        root.getScene().heightProperty().addListener((ce) -> {
            hud.positionHUD();
        });
    }

    /**
     * Adds edit to change list.
     * Will not run edit action nor counter action.
     */
    private void addEdit(Edit edit) {
        if (currentChangeIndex>=MAX_EDITS) {
            currentChangeIndex = MAX_EDITS-1;
            System.arraycopy(changes, 1, changes, 0, MAX_EDITS-1);
            changes[currentChangeIndex++] = edit;
        } else {
            changes[currentChangeIndex++] = edit;
        }
        System.out.println(Arrays.toString(changes) + ", index="+currentChangeIndex);
    }

    private static final int AREA_LEVEL = 0;
    private static final int AREA_NODE_TREE = 1;
    private static final int AREA_PREVIEW_IMAGE = 2;
    private static final int AREA_HUD = 3;

    private int getClickArea(double x, double y) {
        if (hud.preview.getBoundsInParent().contains(x, y))
            return AREA_PREVIEW_IMAGE;
        if (hud.nodeTreePane.getBoundsInParent().contains(x, y))
            return AREA_NODE_TREE;
        if (hud.infoPane.getBoundsInParent().contains(x, y))
            return AREA_HUD;
        return AREA_LEVEL;
    }

    private int getSmartClickArea(double x, double y) {
        if (!hud.infoPane.isVisible()) return AREA_LEVEL;
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
        case AREA_HUD:
            hud.mouseEvent(evt);
            return;
        default:
            return;
        }
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
        if (evt.getButton() == MouseButton.PRIMARY) {
            if (!evt.isShiftDown()) {
                //Drawable d = addObject(magnetize(evt.getX() + cameraX - 30), magnetize(evt.getY() + cameraY - 30));
                // select(d);
                Edit edit = new Edit(1);
                edit.setAction(()->{
                    Drawable drw = addObject(magnetize(evt.getX() + root.getCameraX() - 30), magnetize(evt.getY() + root.getCameraY() - 30));
                    select(drw);
                    edit.setDrawableID(0, objects.indexOf(drw));
                });
                edit.setCounterAction(()->{   removeObject(objects.get(edit.getDrawableID(0)));   });

                edit.action.run();

                addEdit(edit);
            } else {
                int firstID=-1;
                int i=0;
                for (Node n : objects) {
                    if (n.getBoundsInParent().contains(evt.getX()+root.getCameraX(), evt.getY()+root.getCameraY())) {
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
            int[] toKill = new int[MAX_SELECTION_COUNT];
            int[][] oldstates = new int[Drawable.MAX_COMPONENTS][MAX_SELECTION_COUNT];
            int index = 0;
            for (Node n : objects) {
                if (n.getBoundsInParent().contains(evt.getX()+root.getCameraX(), evt.getY()+root.getCameraY())) {
                    toKill[index] = objects.indexOf(n);
                    oldstates[index] = ((Drawable) n).createData();
                    index++;
                }
            }
            deselect();

            if (index==0) return;
            
            Edit edit = new Edit(index);
            edit.setDrawableIDs(Arrays.copyOf(toKill, index));
            edit.setTargetDrawableStates(null);
            edit.setTargetDrawableStatesOld(Arrays.copyOf(oldstates, index));

            toKill = null;
            oldstates = null;

            edit.setAction(()->{
                Arrays.sort(edit.getDrawableIDs());
                for (int i=edit.getDrawableIDs().length-1; i>=0; i--) {
                    int noerrorDrawableID = edit.getDrawableID(i);
                    // System.err.println("to remove: "+noerrorDrawableID+", "+i);
                    removeObject(objects.get(noerrorDrawableID));
                }
            });
            edit.setCounterAction(()->{
                deselect();
                for (int i = 0; i < edit.getDrawableIDs().length; i++) {
                    Drawable drw = addObject(edit.getTargetDrawableStatesOld()[i]);
                    addSelection(drw);
                    edit.setDrawableID(i, objects.indexOf(drw));
                }
            });

            edit.action.run();

            addEdit(edit);
            //if (!toKill.isEmpty())
             //   loadNodeTree(); // ?!
            hud.updateNodeTree();
        }
    }

    private void keyPressEvent(KeyEvent evt) {
        if (null != evt.getCode())
            switch (evt.getCode()) {
            case ESCAPE:
                System.exit(0);
                break;
            case F1:
                hud.toggleHUD();
                break;
            case F2: // debug dugme
                System.out.println(sobjs.size());
                break;
            case ENTER: // save
                saveFile();
                break;
            case BACK_SPACE:
                removeAll(sobjs);
                sobjs.clear();
                soIndices.clear();
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
                hud.clearNodeTree();
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
                if (!evt.isMetaDown()){
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
                } else {
                    if (evt.isShiftDown())
                        redoLastUndo();
                    else
                        undoLastEdit();
                }
                hud.updateNodeTree();
                break;
            case X:
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
                hud.updateNodeTree();
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
                if (root.contains(additionalCommands))
                    hideAdditionalCommands();
                else
                    showAdditionalCommands();
                break;
            case ADD:
            case PLUS:
                if (sti + 1 >= Drawable.TEXTURES.length)
                    break;
                hud.setPreviewImage(AssetsManager.getImage(++sti));
                break;
            case SUBTRACT:
            case MINUS:
                if (sti <= 0)
                    break;
                hud.setPreviewImage(AssetsManager.getImage(--sti));
                break;
            default:
                // 0 1 2 3 4 5 6 7 8 9 - selected texture id
                for (int i = 0; i < 10; i++) {
                    if (evt.getText().equals(String.valueOf(i))) {
                        sti = Integer.parseInt(evt.getText());
                        hud.setPreviewImage(AssetsManager.getImage(sti));
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

    private void saveFile() {
        File f = fileChooser.showSaveDialog(root.getStage());
        if (f==null) return;
        hud.getHelperSign().setText(lvlLoader.save(f, lvl, commandArea.getText())?(lvlLoader.file.getAbsolutePath()+" saved!"):"Problem saving file!");
        hud.getHelperSign().setVisible(true);
    }

    


    private void reposition() {
        objects.lvl.relocate(-root.getCameraX(), -root.getCameraY());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
