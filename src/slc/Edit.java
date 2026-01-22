package slc;

public class Edit {
    Runnable action=null, counterAction=null;

    public static int id = 0;
    private int thisID;

    public int[][] targetDrawableStatesOld;
    public int[][] targetDrawableStates;
    public int[] drawableIDs;

    public Edit(){}

    /**
     * Initializes the drawableIDs list
     * @param numberOfEditedDrawables size of the list
     */
    public Edit(int numberOfEditedDrawables) {
        drawableIDs = new int[numberOfEditedDrawables];
        thisID = id++;
    }

    public int[][] getTargetDrawableStatesOld() {
        return targetDrawableStatesOld;
    }

    public void setTargetDrawableStatesOld(int[][] targetDrawableStatesOld) {
        this.targetDrawableStatesOld = targetDrawableStatesOld;
    }

    public int[][] getTargetDrawableStates() {
        return targetDrawableStates;
    }

    public void setTargetDrawableStates(int[][] targetDrawableStates) {
        this.targetDrawableStates = targetDrawableStates;
    }

    public Edit(Runnable action) {
        this.action = action;
    }
    public Edit(Runnable action, Runnable counterAction){
        this.action = action;
        this.counterAction = counterAction;
        thisID = id++;
    }

    public Edit(int[] targetIDs, int[][] oldstates, int[][] newstates) {
        this.drawableIDs = targetIDs;
        this.targetDrawableStates = newstates;
        this.targetDrawableStatesOld = oldstates;
    }

    public String toString(){
        return "Edit #"+thisID;
    }

    public int[] getDrawableIDs() {
        return drawableIDs;
    }
    public void setDrawableIDs(int[] dis) {
        this.drawableIDs = dis;
    }
    /**
     * 
     * @param index index in drawableIDs array
     * @param drawableID
     */
    public void setDrawableID(int index, int drawableID) {
        this.drawableIDs[index] = drawableID;
    }
    /**
     * 
     * @param index index in drawableIDs array
     * @return
     */
    public int getDrawableID(int index) {
        return this.drawableIDs[index];
    }

    /**
     * action.run();
     */
    public void redo(){
        if (action!=null) action.run();
        if (drawableIDs != null  &&  targetDrawableStates != null) {
            for (int i = 0; i < drawableIDs.length; i++) {
                int drawableID = drawableIDs[i];
                if (i>=targetDrawableStates.length) break; // continue treba al ajde
                if (targetDrawableStates[i] != null)
                    Slc.objects.get(drawableID).updateData(targetDrawableStates[i]);
            }
        }
        //if (targetDrawableState != null)
         //   Slc.objects.get(drawableID).updateData(targetDrawableState);
    }
    /**
     * counterAction.run();
     */
    public void undo(){
        if (counterAction!=null) counterAction.run();
        if (drawableIDs != null  &&  targetDrawableStatesOld != null){
            for (int i = 0; i < drawableIDs.length; i++) {
                int drawableID = drawableIDs[i];
                if (i>=targetDrawableStatesOld.length) break; // continue treba al ajde
                if (targetDrawableStatesOld[i] != null)
                    Slc.objects.get(drawableID).updateData(targetDrawableStatesOld[i]);
            }
        }
        
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
