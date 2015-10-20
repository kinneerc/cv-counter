package org.ccfls.counter.blobtracker;

public class Match implements Comparable<Match> {

    public Blob blob;
    public Location location;
    public Double score;

    protected Match(Blob b, Location l, double s){
        blob = b;
        location = l;
        score = s;
    }

    public Match(Blob b, Location l){
        this(b,l,b.diff(l));
    }

    public int compareTo(Match input){
        return score.compareTo(input.score);
    }

    // update this blobs location
    public Blob take(){
        blob.current = location;
        return blob;
    }

    // change state of the blob based on zone
    protected void take(Zone z){
        if (z.outer){
            if (blob.outgoing){
                blob.outgoing = false;
                blob.incoming = true;
                BlobTracker.exits += 1;
                System.out.println("EXIT");
                BlobTracker.trigger("EXIT");
            }
        }else{
            if (blob.incoming){
                blob.incoming = false;
                blob.outgoing = true;
                BlobTracker.enters += 1;
                System.out.println("ENTER");
                BlobTracker.trigger("ENTER");
            }
        }
    }

    @Override
    public boolean equals(Object o){
        boolean same = false;

        if (o != null){
        if (o instanceof Match){
            Match mo = (Match) o;
            same = mo.location.equals(location);
        }
        }

        return same;

    }

}
