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

    public boolean collidesWith(Location l){
        return true;
    }

    public double diff(Location l){
        return -1;
    }

}
