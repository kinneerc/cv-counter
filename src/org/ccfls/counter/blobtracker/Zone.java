package org.ccfls.counter.blobtracker;

import java.util.*;

public class Zone {

    private static int curId; 

    Location location;

    int id;

    boolean gateway;

    boolean outer;

    public Zone(Location l, boolean outer){
        this.location = l;
        this.outer = outer;
        this.gateway = true;
        this.id = curId++;

    }

    public Zone(String[] csvRow){

        this.id = Integer.parseInt(csvRow[0]);

        this.location = new Location(Integer.parseInt(csvRow[1]),
                Integer.parseInt(csvRow[2]),
                Integer.parseInt(csvRow[3]),
                Integer.parseInt(csvRow[4]));

        if (csvRow[5].equals("1"))
            gateway = true;

        if (csvRow[6].equals("1"))
            outer = true;
    }

    /**
     * Assists in serializing zone data
     * id,tlx,tly,brx,bry,gate,outer
     */
    public String toCSVRow(){
        String ans = Integer.toString(id)+",";
        ans += location.tlx+",";
        ans += location.tly+",";
        ans += location.brx+",";
        ans += location.bry+",";
        if (gateway)
            ans += "1,";
        else
            ans += "0,";
        if (outer)
            ans += "1";
        else
            ans += "0";

        return ans;
    }

    public static String toCSV(List<Zone> zones){
        String ans = "";
        for (Zone z : zones){
            ans += z.toCSVRow() + "\n";
        }
        return ans;
    }

}
