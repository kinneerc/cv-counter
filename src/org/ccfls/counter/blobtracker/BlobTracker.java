package org.ccfls.counter.blobtracker;

import org.ccfls.counter.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class BlobTracker {

    private boolean debug = false;

    public int exits = 0;

    public int enters = 0;

    // people counter object that we will trigger
    private PeopleCounter pc;

    // stores the history for the requested amount of time
    ArrayList<Blob> history;

    ArrayList<Zone> zones;

    public BlobTracker(PeopleCounter pc){
        this.pc = pc;
    }

    public void trigger(String type){
        pc.trigger(type);
    }

    /**
     * Given a list of locations, get list of labeled blobs
     */
    public ArrayList<Blob> track(ArrayList<Location> locks){

        ArrayList<Blob> bloblist = new ArrayList<Blob>();

        if(locks==null){
            history = null;
            return bloblist;
        }

        // check for initialization case
        // simply make all locks blobs
        if (history == null){
            history = new ArrayList<Blob>();
            for (Location l : locks){
              Zone z = null;
              if((z = inZone(l)) != null){
                  Blob blob = new Blob(l);
                  if (z.outer)
                      blob.incoming = true;
                  else
                      blob.outgoing = true;

                history.add(blob);
            }
            }

            return (ArrayList<Blob>) history.clone();
        }

        // otherwise, consider each past blob and try to identify it in this frame
       
        // first, get priority queue for each blob
        ArrayList<PriorityQueue<Match>> qlist = new ArrayList<PriorityQueue<Match>>();

        for (Blob b : history){

            PriorityQueue<Match> matchQueue = new PriorityQueue();

            for (Location l : locks){
                // for each location, score the match
                matchQueue.add(new Match(b,l));
            }

            qlist.add(matchQueue);
        
        }

        ArrayList<Match> taken = new ArrayList<Match>();

        ArrayList<Blob> leftoverBlobs = (ArrayList<Blob>) history.clone();
        ArrayList<Location> leftoverLocks = (ArrayList<Location>) locks.clone();

        // now, take the best match
        while(!qlist.isEmpty()){
            Match best = null;
            int bestindex = 0;
            for (int count = 0; count < qlist.size(); count++){

                PriorityQueue<Match> pq = qlist.get(count);

                while(taken.contains(pq.peek())){
                    pq.poll();
                }
                if (best == null || best.score < pq.peek().score){
                    best = pq.poll();
                    bestindex = count;
                }

            }

            if (best!=null){
            leftoverBlobs.remove(best.blob);
            leftoverLocks.remove(best.location);
            taken.add(best);
            qlist.remove(bestindex);
            }else{
            break;
            }

        }

        // check for split or merge
        if (history.size() > locks.size()){
          // merge
          // don't worry about it right now
          // later, we will find what blob was removed,
          // and find what blob it merged to
          // we can then transfer it properties if desired
        }else if (history.size() < locks.size()){
          // split
          // for now, just make it a new blob
          // later, we can find what lock was not matched, and find the 
          // closest blob.  We can then transfer properties if desired
          if(debug)
          System.out.println("Looking to add blobs");
          for (Location l : leftoverLocks){
              // only add new blob if inside of a zone
              if(debug)
              System.out.println("Considering a leftover lock");
              Zone z = null;
              if((z = inZone(l)) != null){
                  Blob blob = new Blob(l);
                  if (z.outer)
                      blob.incoming = true;
                  else
                      blob.outgoing = true;
                if(debug)
                System.out.println("Got lock in zone");
                bloblist.add(blob);
              }else{
              }
          }

        }

        // now add in matches to bloblist
        for (Match m : taken){
            bloblist.add(m.take());
            // now check to see if we have entered a zone
            Zone z = inZone(m.location);

        if(z != null){
            // deal with the zone
            take(m,z);
        }


        }

        // and override history
        history = (ArrayList<Blob>) bloblist.clone();

        return bloblist;
    }

        // TODO refactor this to blobtracker
    // change state of the blob based on zone
    protected void take(Match m, Zone z){

        Blob blob = m.blob;

        if (z.outer){
            if (blob.outgoing){
                blob.outgoing = false;
                blob.incoming = true;

                exits += 1;

                System.out.println("EXIT");

                trigger("EXIT");
            }
        }else{
            if (blob.incoming){
                blob.incoming = false;
                blob.outgoing = true;

                enters += 1;

                System.out.println("ENTER");

                trigger("ENTER");
            }
        }
    }

    protected Zone inZone(Location l){

        for (Zone z : zones){
            if(debug)
            System.out.println("Considering zone "+z.id);
            if (z.location.contains(l.center()))
                return z;
        } 
        return null;

    
    }

    public void setZones(ArrayList<Zone> input){
        zones = input;
    }

    public boolean zoned(){
        return zones != null;
    }

    /* protected List<Blob> checkCollisions(Location l){ */
    /*     ArrayList<Blob> bloblist = new ArrayList<Blob>(); */
    /*     for (Blob b : history){ */
    /*         if (b.current.collidesWith(l)) */
    /*             bloblist.add(b); */
    /*     } */
    /*     return bloblist; */
    /* } */

}
