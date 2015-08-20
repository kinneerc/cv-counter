package org.ccfls.counter.blobtracker;

public class Location {

    public double tlx;
    public double tly;
    public double brx;
    public double bry;

    public Location(double tlx, double tly, double brx, double bry){
        this.tlx = tlx;
        this.tly = tly;
        this.brx = brx;
        this.bry = bry;
    }

    public double[] center(){
        double[] answer = new double[2];
        answer[0] = (tlx + brx)/2;
        answer[1] = (tly + bry)/2;
        return answer;
    }

    @Override
    public boolean equals(Object o){
        boolean same = false;

        if (o != null){
        if (o instanceof Location){
            Location lo = (Location) o;
            if(lo.tlx == tlx &&
               lo.tly == tly &&
               lo.brx == brx &&
               lo.bry == bry)
                same = true;
        }
        }

        return same;
    }

}
