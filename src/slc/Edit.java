package slc;

public class Edit {
    Runnable action=null, counterAction=null;

    public static int id = 0;
    private int thisID;

    private int[] targetDrawableStateOld;
    private int[] targetDrawableState;
    private int drawableID;

    public Edit(){}

    public Edit(Runnable action) {
        this.action = action;
    }
    public Edit(Runnable action, Runnable counterAction){
        this.action = action;
        this.counterAction = counterAction;
        thisID = id++;
    }

    public Edit(int targetID, int[] oldstate, int[] newstate) {
        this.drawableID = targetID;
        this.targetDrawableState = newstate;
        this.targetDrawableStateOld = oldstate;
    }

    public String toString(){
        return "Edit #"+thisID;
    }

    public int getDrawableID() {
        return drawableID;
    }
    public void setDrawableID(int di) {
        this.drawableID = di;
    }

    /**
     * action.run();
     */
    public void redo(){
        if (action!=null) action.run();
        if (targetDrawableState != null)
            Slc.objects.get(drawableID).updateData(targetDrawableState);
    }
    /**
     * counterAction.run();
     */
    public void undo(){
        if (counterAction!=null) counterAction.run();
        if (targetDrawableStateOld != null)
            Slc.objects.get(drawableID).updateData(targetDrawableStateOld);
    }

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    public Runnable getCounterAction() {
        return counterAction;
    }

    public void setCounterAction(Runnable counterAction) {
        this.counterAction = counterAction;
    }
}
