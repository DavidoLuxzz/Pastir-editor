package slc;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class HUD {
    public Label angleSign;
    public Label coordsSign;
    public Label stiSign, solSign, sogSign, ssvSign, ssv2Sign, ssdSign, sbvSign;
    public Slider ssvSlider,ssv2Slider,sbvSlider;
    public Label helperSign;
    public Pane infoPane;

    public ImageView preview; // object preview

    public Button mainButton;

    public HUD() {
        initObjectRelatedLabels();
        initNodeTree();
        initMainButton();
    }

    private void initNodeTree(){
        nodeTree = new ArrayList<String>();
        nodeTreePane = new Pane();
        Rectangle rect = new Rectangle(200, 100000);
        rect.setFill(Color.rgb(0, 0, 0, 0.5));
        rect.relocate(0, 0);
        rect.setViewOrder(1);
        nodeTreePane.getChildren().add(rect);
        loadNodeTree();
        nodeTreePane.setViewOrder(-3);
    }

    private void initObjectRelatedLabels() {
        angleSign = new Label("Rotate: 0");
        coordsSign = new Label("xy: 0, 0");
        stiSign = new Label();
        solSign = new Label();
        sogSign = new Label();
        ssvSign = new Label();
        ssv2Sign = new Label();
        sbvSign = new Label();
        ssdSign = new Label();
        
        
        ssvSlider = new Slider(-180, 180, 0); // hue
        ssvSlider.setBlockIncrement(1);
        ssvSlider.valueProperty().addListener(e -> {
            if (!ssvSlider.isValueChanging()) return;
            Slc.ssv = (int) ssvSlider.getValue();
            updateLabels();
            Slc.updateSelectedObjects();
        });
        ssv2Slider = new Slider(-100, 100, 0); // saturation
        ssv2Slider.setBlockIncrement(1);
        ssv2Slider.valueProperty().addListener(e -> {
            if (!ssv2Slider.isValueChanging()) return;
            Slc.ssv2 = (int) ssv2Slider.getValue();
            updateLabels();
            Slc.updateSelectedObjects();
        });
        sbvSlider = new Slider(-100, 100, 0); // brightness
        sbvSlider.setBlockIncrement(1);
        sbvSlider.valueProperty().addListener(e -> {
            if (!sbvSlider.isValueChanging()) return;
            Slc.sbv = (int) sbvSlider.getValue();
            updateLabels();
            Slc.updateSelectedObjects();
        });

        infoPane = new Pane(angleSign, coordsSign, stiSign, solSign, sogSign, ssvSign, ssv2Sign, sbvSign, ssdSign);
        infoPane.setViewOrder(-1.0);
        for (Node n : infoPane.getChildren()) {
            Label sign = (Label) n;
            sign.setTextFill(Color.WHITE);
            sign.setFont(Font.font("DejaVu Sans", 20));
        }
        infoPane.getChildren().addAll(ssvSlider,ssv2Slider,sbvSlider);

        helperSign = new Label("Reloaded level");
        helperSign.setFont(Font.font(36));
        helperSign.setTextFill(Color.WHITE);
        helperSign.setVisible(false);

        preview = new ImageView(AssetsManager.getImage(Slc.sti));
        preview.setViewOrder(-2);
    }

    public void updateLabels() {
        if (!Slc.sobjs.isEmpty()) {
            if (Slc.sobjs.size()>1)
                coordsSign.setText("count: "+Slc.sobjs.size());
            else {
                Drawable so = Slc.sobjs.getFirst();
                coordsSign.setText("x,y: (" + (int) so.getLayoutX() + ", " + (int) so.getLayoutY() + ")");
                angleSign.setText("Rotate: " + so.getRotate());
            }
        } else {
            coordsSign.setText("x,y: ( , )");
            angleSign.setText("Rotate: 0.0");
        }
        stiSign.setText("txID: " + Slc.sti);
        solSign.setText("layer: " + Slc.sol);
        sogSign.setText("group: " + Slc.sog);
        ssdSign.setText("solid: " + Slc.ssd);
        ssvSign.setText("Hue: " + Slc.ssv);
        ssv2Sign.setText("Saturation: " + Slc.ssv2);
        sbvSign.setText("Brightness: "+Slc.sbv);
    }

    private void initMainButton(){
        mainButton = new Button();
    }

    public void drawHUD() {
        Slc.root.add(mainButton);

        Slc.root.add(infoPane);
        Slc.root.add(nodeTreePane);

        Slc.root.add(preview);
    }

    public Label getHelperSign(){
        return helperSign;
    }

    public void setPreviewImage(Image img){
        preview.setImage(img);
    }

    public void mouseEvent(MouseEvent evt) {
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
            Slc.ssd = !Slc.ssd;
        else if (solSign.getBoundsInParent().contains(x, y))
            Slc.sol += dv;
        else if (sogSign.getBoundsInParent().contains(x, y))
            Slc.sog += dv;
        else if (ssvSign.getBoundsInParent().contains(x, y)) {
            Slc.ssv += dv;
            ssvSlider.setValue(Slc.ssv);
        } else if (ssv2Sign.getBoundsInParent().contains(x, y)) {
            Slc.ssv2 += dv;
            ssv2Slider.setValue(Slc.ssv2);
        } else if (sbvSign.getBoundsInParent().contains(x, y)) {
            Slc.sbv += dv;
            sbvSlider.setValue(Slc.sbv);
        } else if (mainButton.getBoundsInParent().contains(x, y)) {
            mainButton.handleEvent(evt);
        } else
            return;
        Slc.updateObjectsAndLabels();
    }

    public void positionHUD() {

        mainButton.relocate(4, 4);

        // ### INFO PANE ### //
        double isx = 10, isy = 50; // info pane start coords
        stiSign.relocate(isx, isy);
        coordsSign.relocate(isx, isy+=20);
        ssdSign.relocate(isx, isy+=20);
        solSign.relocate(isx, isy+=20);
        sogSign.relocate(isx, isy+=20);
        // hsb
        ssvSign.relocate(isx, isy+=20);
        ssvSlider.relocate(isx+150, isy+5);
        ssv2Sign.relocate(isx, isy+=20);
        ssv2Slider.relocate(isx+150, isy+5);
        sbvSign.relocate(isx, isy+=20);
        sbvSlider.relocate(isx+150, isy+5);
        // angle
        angleSign.relocate(isx, isy+=20);

        // other shit
        preview.relocate(16, Slc.root.getHeight() - 80);
        nodeTreePane.relocate(Slc.root.getWidth() - 200, 5);
        
        helperSign.relocate(100, Slc.root.getHeight()-60);
    }

    public void hideHUD() {
        infoPane.setVisible(false);
        nodeTreePane.setVisible(false);
        preview.setVisible(false);
    }
    public void showHUD() {
        infoPane.setVisible(true);
        nodeTreePane.setVisible(true);
        preview.setVisible(true);
    }
    public void toggleHUD() {
        if (infoPane.isVisible())
            hideHUD();
        else
            showHUD();
    }





    //////// #### NODE TREE #### /////////
    /// // /**
    //  * No change on list size
    //  */
    // private void updateNodeTreeLabelsNoNew() {
    //     int index = 1;
    //     for (String s : nodeTree) {
    //         ((Label) nodeTreePane.getChildren().get(index++)).setText(s);
    //     }
    // }

    public Pane nodeTreePane;
    public ArrayList<String> nodeTree;

    public static final int NODE_TREE_Y_RAZMAK = 14;
    /**
     * Updates full node tree.
     * Steps:
     *  - check if object list has GROWN (doesn't remove any labels, only creates them)
     *  - updates text of all labels
     *  - hides all other labels
     */
    public void updateNodeTree(){
        // new responsibility: lvl (ArrayList<int[]>)
        int ycoord = nodeTree.size()*NODE_TREE_Y_RAZMAK;
        while (Slc.objects.lvl.getChildren().size()>nodeTree.size()){
            Label lab = new Label("fix me");
            lab.setFont(new Font("Helvetica Bold", 12));
            lab.setTextFill(Color.WHITE);
            lab.relocate(0, ycoord);

            nodeTree.add("fix me");
            nodeTreePane.getChildren().add(lab);
            ycoord += NODE_TREE_Y_RAZMAK;
        }
        for (int i=0; i<Slc.objects.lvl.getChildren().size(); i++){
            int[] data = ((Drawable)Slc.objects.lvl.getChildren().get(i)).createData();
            
            if (i>=Slc.lvl.size())
                Slc.lvl.add(data);
            else
                Slc.lvl.set(i, data);
            
            
            String s = LvlLoader.iats(data);
            ((Label) nodeTreePane.getChildren().get(i+1)).setText(s);
            ((Label) nodeTreePane.getChildren().get(i+1)).setVisible(true);
        }
        for (int i=Slc.objects.lvl.getChildren().size(); i<nodeTree.size(); i++){
            nodeTreePane.getChildren().get(i+1).setVisible(false);
        }
        while (Slc.lvl.size() > Slc.objects.lvl.getChildren().size()){
            Slc.lvl.removeLast();
        }
    }
    /**
     * Creates full node tree
     */
    public void loadNodeTree() {
        int ycoord = 0;
        clearNodeTree();
        for (int[] e : Slc.lvl) {
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

    public void clearNodeTree() {
        nodeTree.clear();
        if (nodeTreePane.getChildren().size() > 1)
            nodeTreePane.getChildren().remove(1, nodeTreePane.getChildren().size()); // all except background
    }

    public void scrollNodeTree(ScrollEvent evt){
        nodeTreePane.setLayoutY(nodeTreePane.getLayoutY() + evt.getDeltaY());
        if (nodeTreePane.getLayoutY() > 0)
            nodeTreePane.setLayoutY(0);
    }
}
