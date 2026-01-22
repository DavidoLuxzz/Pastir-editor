package slc;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Button extends ImageView {

    final ContextMenu contextMenu;

    public Button(){
        super(AssetsManager.getSizedImage("logo.png", 100, 50));
        contextMenu = new ContextMenu();
        contextMenu.setOnShowing((e) -> {
            System.out.println("showing");
        });
        contextMenu.setOnShown((e) -> {
            System.out.println("shown");
        });

        MenuItem item1 = new MenuItem("About");
        item1.setOnAction((e) -> {
            System.out.println("About");
        });
        MenuItem item2 = new MenuItem("Preferences");
        item2.setOnAction((e) -> {
            System.out.println("Preferences");
        });
        contextMenu.getItems().addAll(item1, item2);
    }

    public void handleEvent(MouseEvent evt){
        // if (getBoundsInParent().contains(evt.getX(),evt.getY())){
        System.out.println("Clicked on logo!");
        contextMenu.show(getParent().getScene().getWindow(), evt.getScreenX(), evt.getScreenY());
    }

}
