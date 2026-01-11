package slc;

import java.util.Iterator;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class Level implements Iterable<Node> {

	public Pane lvl;
	
	public Level() {
		lvl = new Pane();
	}
	
	public void add(Node n) {
		lvl.getChildren().add(n);
	}
	public void remove(Node n) {
		lvl.getChildren().remove(n);
	}
	public void remove(Drawable d) {
		lvl.getChildren().remove(d);
	}
	public void remove(int index) {
		lvl.getChildren().remove(index);
	}
	public void clear() {
		lvl.getChildren().clear();
	}
	public int indexOf(Node n) {
		return lvl.getChildren().indexOf(n);
	}
	public Drawable get(int index) {
		return (Drawable)lvl.getChildren().get(index);
	}

	@Override
	public Iterator<Node> iterator() {
		return lvl.getChildren().iterator();
	}

}
