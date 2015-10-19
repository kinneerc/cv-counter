package org.ccfls.counter.blobtracker;

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

}
