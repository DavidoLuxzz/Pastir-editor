package slc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LvlLoader {
	public File file;
    private ArrayList<int[]> l;
    private String additionalCommands = "";
    public LvlLoader(String filename){
        file = new File(filename);
        l = new ArrayList<>();
    }
    public LvlLoader(File file) {
        if (file!=null)
            this.file = file;
        else {
            this.file = new File("3.txt");
            System.out.println(this.file);
        }
        l = new ArrayList<>();
    }
    public boolean successLoad(){
        System.out.println(file);
        try {
        // System.out.println(myObj.getAbsolutePath());
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
            	String line = myReader.nextLine();
                String[] data = line.split(" ");
                if (data[0].equalsIgnoreCase("box")) continue; // automaticly added (no need for loading)
                if (line.length()<3) continue; // invalid
                if (!Character.isDigit(line.charAt(0))) {
                	additionalCommands += line+"\n";
                	continue;
                }
                // parse
                int[] obj = new int[Drawable.MAX_COMPONENTS];
                int index = 0;
                for (String comp : data) {
                	if (comp.length()<1) continue;
                	obj[index++] = Integer.parseInt(comp);
                }
                l.add(obj);
            }
            myReader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
        }
        return false;
    }
    public ArrayList<int[]> getLevel(){
        return l;
    }
    public String getAdditionalCommands() { // goes into commandArea [TextArea] (press TAB)
    	return additionalCommands;
    }
    public static String iats(int[] ar){ // int array to string
        String s = "";
        for (int i=0;i<ar.length-1;i++){
            s=s+String.valueOf(ar[i])+" ";
        }
        s=s+String.valueOf(ar[ar.length-1]);
        return s;
    }
    private String alts(ArrayList<int[]> list){ // array list to string
        String s = "";
        int maxw,maxh; // for box command
        maxw = list.get(0)[Drawable.COMP_X] + 64;
        maxh = list.get(0)[Drawable.COMP_Y] + 64;
        for (int[] il : list){
            s+=iats(il)+"\n";
            maxw = Math.max(maxw, il[Drawable.COMP_X]+64);
            maxh = Math.max(maxh, il[Drawable.COMP_Y]+64);
        }
        s+="box "+maxw+" "+maxh+"\n";
        return s;
    }
    public boolean save(File savefile, ArrayList<int[]> lvl, String additionalCommands){
        try {
            FileWriter myWriter = new FileWriter(savefile);
            String fullData = alts(lvl);
            if (additionalCommands.length()>0) {
	            if (additionalCommands.endsWith("\n"))
	            	fullData = additionalCommands+fullData;
	            else
	            	fullData = additionalCommands+"\n"+fullData;
            }
            myWriter.write(fullData);
            myWriter.close();
          // System.out.println("FILE "+filename.toUpperCase()+" SAVED!");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            return false;
        }
        return true;
    }
}