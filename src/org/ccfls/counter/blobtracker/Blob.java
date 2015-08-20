package org.ccfls.counter.blobtracker;

import java.util.List;

public class Blob {

    static int nextId = 0;

    Location initial;
    Location current;

    int id;

    public Blob(Location l){
        initial = l;
        current = l;

        id = nextId++;
    }

    public Location getCurrent(){
        return current;
    }

    public Location getInitial(){
        return initial;
    }

    public int getId(){
        return id;
    }

    public boolean collidesWith(Location l){
        return true;
    }

    public double diff(Location l){
        return distance(l.center()[0],current.center()[0],l.center()[1],current.center()[1]);
    }

    private static double distance(double x1, double x2, double y1, double y2){
        return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

}
