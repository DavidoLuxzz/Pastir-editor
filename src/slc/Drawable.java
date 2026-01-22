package slc;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

// WARNING: this format is new, and thus do not rage quit
//	indices:	  0      1  2    3      4      5      6        7        8      9       10          11         12
// format:  [ textureID  x  y  solid  layer  group  angle˚  scalex%  scaley%  hue  saturation  brightness  special ]
public class Drawable extends ImageView {
    public static final int MAX_COMPONENTS = 13; // last component id (brightness) + 1
    public static final int COMP_TEXTURE_ID = 0;
    public static final int COMP_X = 1;
    public static final int COMP_Y = 2;
    public static final int COMP_SOLID = 3;
    public static final int COMP_LAYER = 4;
    public static final int COMP_GROUP = 5;
    public static final int COMP_ANGLE = 6;
    public static final int COMP_SCALEX = 7;
    public static final int COMP_SCALEY = 8;
    public static final int COMP_TINT_HUE = 9;
    public static final int COMP_TINT_SATURATION = 10;
    public static final int COMP_TINT_BRIGHTNESS = 11;
    public static final int COMP_SPECIAL = 12;



    public static final String[] TEXTURES = {
        "block","stone", "grass","water", "plank","plank_on_water", "cobweb","plum","fence_full",
        "door","door_open","room","shade","block_corner","block_top","block_topdown","block_end",
        "black","fence_left","fence_right","fence_no_conn","line_corner","line_top","line_sides",
        "line_end", "fasada", "grass0", "grass1","grass2", "grass3","grass4", "grass5", "grass6",
        "grass7","grass8","grass9", "grass10","grass11","grass12", "grass13","grass14","grass15",
        "dirt0","dirt1", "cobble0","cobble1","cobble2", "cobble3","cobble4", "cobble5","cobble6",
        "cobble7"
    };
    public static final int TEXTURE_BLOCK  	        = 0;
    public static final int TEXTURE_STONE  		    = 1;
    public static final int TEXTURE_GRASS  		    = 2;
    public static final int TEXTURE_WATER  		    = 3;
    public static final int TEXTURE_PLANK  		    = 4;
    public static final int TEXTURE_PLANKW 		    = 5;
    public static final int TEXTURE_COBWEB 		    = 6;
    public static final int TEXTURE_PLUM   		    = 7;
    public static final int TEXTURE_FENCE_FULL	    = 8;
    public static final int TEXTURE_DOOR   		    = 9;
    public static final int TEXTURE_DOOR_OPEN  	    = 10;
    public static final int TEXTURE_ROOM		    = 11;
    public static final int TEXTURE_SHADE		    = 12;
    public static final int TEXTURE_BLOCK_CORNER    = 13;
    public static final int TEXTURE_BLOCK_TOP	    = 14;
    public static final int TEXTURE_BLOCK_SIDES     = 15;
    public static final int TEXTURE_BLOCK_END       = 16;
    public static final int TEXTURE_BLACK           = 17;
    public static final int TEXTURE_FENCE_LEFT	    = 18;
    public static final int TEXTURE_FENCE_RIGHT	    = 19;
    public static final int TEXTURE_FENCE_NO_CONN   = 20;
    public static final int TEXTURE_LINE_CORNER     = 21;
    public static final int TEXTURE_LINE_TOP        = 22;
    public static final int TEXTURE_LINE_SIDES      = 23;
    public static final int TEXTURE_LINE_END        = 24;
    public static final int TEXTURE_FASADA          = 25;
    public static final int TEXTURE_GRASSX          = 26;
    public static final int GRASS_TEXTURE_COUNT     = 16;
    public static final int TEXTURE_DIRT0           = 42;
    public static final int TEXTURE_DIRT1           = 43;
    public static final int TEXTURE_COBBLEX         = 44;
    public static final int COBBLE_TEXTURE_COUNT    = 8;
    
    public static final int[] SOLID_IDs = {
            TEXTURE_BLOCK, TEXTURE_WATER, TEXTURE_PLUM, TEXTURE_FENCE_FULL,
            TEXTURE_DOOR, TEXTURE_BLOCK_CORNER, TEXTURE_BLOCK_TOP, TEXTURE_BLOCK_SIDES,
            TEXTURE_BLOCK_END, TEXTURE_BLACK, TEXTURE_FENCE_LEFT, TEXTURE_FENCE_RIGHT, 
            TEXTURE_FENCE_NO_CONN, TEXTURE_LINE_CORNER, TEXTURE_LINE_TOP, TEXTURE_LINE_SIDES,
            TEXTURE_LINE_END
    };
    public static boolean isSolid(int txtid) {
        for (int b : SOLID_IDs) {
            if (b == txtid)
                return true;
        }
        return false;
    }
    public static Bounds createHitbox(double minx, double miny, double w, double h) {
        return new BoundingBox(minx, miny, w, h);
    }
    private int texture;
    private int group; // group used to manage drawables easier
    private int layer; // layer (greater layer value objects will be in front of lower layer value
    // objects) try to keep value lower than 10
    private boolean solid; // drawable solid
    private int hue, saturation, brightness;

    private int spec;

    private ColorAdjust tint = null;

    public Drawable(int textureid, int angle, int scalex, int scaley) {
        super(AssetsManager.getImage(textureid));
        setAngle(angle);
        setScaleX(scalex / 100.0);
        setScaleY(scaley / 100.0);
    }

    public Drawable(int textureid, int group, int layer, boolean solid, 
                    int angle, int scalex, int scaley,
                    int hue, int saturation, int brightness) {
        super(AssetsManager.getImage(textureid));
        init(textureid, group, layer, solid, angle, scalex, scaley, hue, saturation, brightness, false);
    }

    public Drawable(int[] data) {
        super(AssetsManager.getImage(data[COMP_TEXTURE_ID]));
        init(data[COMP_TEXTURE_ID], data[COMP_GROUP], data[COMP_LAYER], data[COMP_SOLID] > 0, data[COMP_ANGLE],
                data[COMP_SCALEX], data[COMP_SCALEY],
                data[COMP_TINT_HUE], data[COMP_TINT_SATURATION], data[COMP_TINT_BRIGHTNESS], false);
        relocate(data[COMP_X], data[COMP_Y]);
        // setTintHue(data[COMP_SPECIAL]);
        // setTintSaturation(data[COMP_SPECIAL2]);
    }

    public void updateData(boolean solid, int layer, int group, int hue, int satur, int bright) {
        updateData(solid, layer, group);
        setTint(hue, satur, bright);
    }

    public void updateData(boolean solid, int layer, int group) {
        setSolid(solid);
        setLayer(layer);
        setGroup(group);
    }

    public void updateData(int[] data){
        setImage(AssetsManager.getImage(data[COMP_TEXTURE_ID]));
        init(data[COMP_TEXTURE_ID], data[COMP_GROUP], data[COMP_LAYER], data[COMP_SOLID] > 0, data[COMP_ANGLE],
                data[COMP_SCALEX], data[COMP_SCALEY],
                data[COMP_TINT_HUE], data[COMP_TINT_SATURATION], data[COMP_TINT_BRIGHTNESS],  true);
        relocate(data[COMP_X], data[COMP_Y]);
    }

    public int[] createData() {
        return new int[] {
                texture,
                (int) getLayoutX(), (int) getLayoutY(),
                solid ? 1 : 0, layer, group, getAngle(),
                (int) (getScaleX() * 100), (int) (getScaleY() * 100),
                hue, saturation, brightness,
                spec
        };
    }

    /**
     * Sets temporary tint.
     * 
     * @param ca default = null
     */
    public void setTemporaryTint(ColorAdjust ca) {
        if (ca != null) {
            tint.setInput(ca);
            // setEffect(ca);
            updateTint();
        } else {
            tint.setInput(null);
            updateTint();
        }
    }

    // ## tint components ## //

    public void setTint(int hue, int saturation, int brightness) { // hex rgb
        setTintHue(hue);
        setTintSaturation(saturation);
        setTintBrightness(brightness);
        updateTint();
    }

    public void updateTint() {
        // tint.setInput(blend);
        setEffect(tint);
    }

    public int getTintSaturation() {
        return saturation;
    }

    public void setTintSaturation(int saturation) {
        this.saturation = saturation;
        tint.setSaturation(saturation / 100.0);
    }

    public void setTintHue(int hue) { // Math.sin
        this.hue = hue % 360; // 0 -- 180 -- 360 ==> 0 -- 180 -- -180
        tint.setHue(hue / 180.0); // 0=0, 180=180, 181=-179, 350=-10, 360 = 0
        // tint.setHue(0.0);
        // System.out.println(hue);
    }

    public int getTintHue() {
        return hue;
    }

    public void setTintBrightness(int b) {
        this.brightness = b;
        tint.setBrightness(b / 100.0);
    }

    public int getTintBrightness() {
        return brightness;
    }

    public void init(int textureid, int group, int layer, boolean solid, int angle, int scalex, int scaley, int hue,
            int saturation, int brightness,
            boolean update
        ) {
        setSmooth(false);
        this.texture = textureid;
        setLayer(layer);
        setGroup(group);
        setSolid(solid);
        setRotate(angle);

        setScaleX(scalex / 100.0);
        setScaleY(scaley / 100.0);

        if (!update)
            tint = new ColorAdjust();
        // blend = new Blend(BlendMode.SRC_OVER);
        // blend.setBottomInput(tint);
        setTint(hue, saturation, brightness);
    }

    @Override
    public void relocate(double x, double y) {
        setLayoutX(x);
        setLayoutY(y);
    }

    public double getMiniX() {
        return getLayoutX() / 64;
    }

    public double getMiniY() {
        return getLayoutY() / 64;
    }

    public void setAngle(int angle) {
        setRotate(angle);
    }

    public int getAngle() {
        return (int) getRotate();
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public void setLayer(int layer) {
        this.layer = layer;
        setViewOrder(-layer);
    }

    public int getLayer() {
        return layer;
    }

    public int getGroup() {
        return group;
    }

    public int getTexture() {
        return texture;
    }

    public void setTexture(int texture) {
        this.texture = texture;
        setImage(AssetsManager.getImage(TEXTURES[texture] + ".png", 4, 4));
    }

    public void setSpecial(int spec) {
        this.spec = spec;
    }

    public int getSpecial() {
        return spec;
    }

    public void setSolid(boolean s) {
        solid = s;
    }

    public boolean isSolid() {
        return solid;
    }
}