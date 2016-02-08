package org.ccfls.counter;

import java.sql.SQLException;

import java.util.*;
import java.io.File;

import org.ccfls.counter.blobtracker.*;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.imgproc.Imgproc;

public class CVCounter {

    @Parameter(names = {"-v","--visual","-g","--gui"}, description = "Show visual window for debugging")
    protected boolean visual = false;

    @Parameter(names = {"-p","--place","--site"}, description = "The location of this unit.", required = true)
    protected String site;

    @Parameter(names = {"-z","--zone"}, description = "Redefine entry and exit zones by visual window")    
    protected boolean zone = false;

    @Parameter(names = {"-f","--file"}, description = "Path to csv defining the entry and exit zones")    
    protected String zoneFile = "zones.csv";

    @Parameter(names = {"-h","--help"}, description = "Show command line arguments", help = true)
    protected boolean help;

    private JCommander jc;

    private BlobTracker blobTracker;

    // this class handles reporting to database
    // as well as raspberry pi interactions
    private PeopleCounter pc;

    // This is the camera
    private VideoCapture webSource = null;

    public static void main(String[] args){
        CVCounter counter = new CVCounter(args);
        counter.jc = new JCommander(counter,args);
        
        counter.run();
    }

    public CVCounter(String[] args){
    }

    private void run(){

        if (help){
            jc.usage();
            System.exit(0);
        }

        // is this a run to set the zones?
        if (zone){
            // if so, use the CVFrame to do this
            CVFrame.main(new String[] {site, "true"});
            System.exit(0);
        }

        // next, do we want headless run?
        if (visual){
            CVFrame.main(new String[] {site,"false"});
            System.exit(0);
        }else{
            try{
                runHeadless();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }        
    
    }

    public void runHeadless() throws SQLException {

        // and the peoplecounter
        pc = new PeopleCounter(site);
        // instantiate tracker
        blobTracker = new BlobTracker(pc);
 


         // first step, do we have zone information?
        ArrayList<Zone> zones = null;
        // lets take a look at the file first
        File zoneFile = new File(this.zoneFile);
        // any luck?
        if (zoneFile.exists()){
            // if the file is there, then lets try reading it in
            zones = CVFrame.readZones(zoneFile);
        }else{
            // if there is no file, then they have to provide one or create one
            System.out.println("No Zone File found. Must provide one or create a new one with the -z argument");
            
            System.exit(1);
        
        }

        // now we configure the blobtracker
        blobTracker.setZones(zones);

        if(!blobTracker.zoned()){
                        System.out.println("No Zone File found. Must provide one or create a new one with the -z argument");
                        System.exit(1);
                    }

        // turn the webcam on

        VideoCapture webSource = new VideoCapture(0);

        BackgroundSubtractorMOG2 bsub =  Video.createBackgroundSubtractorMOG2(500,400,false);

        // enter main loop

        while(true){
            if (webSource.grab()){
                try{
                    
                    processFrame(webSource,bsub);

                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    System.out.println("Error");
                }

            }
        }
    }
    
    public Mat processFrame(VideoCapture vc,BackgroundSubtractorMOG2 bsub){

        Mat frame = new Mat();
        Mat fgMask = new Mat();

        pc.idc.heartbeat();

        vc.retrieve(frame);
        bsub.apply(frame,fgMask);

        //TODO look into improving preprocessing using the cinema example
        Imgproc.GaussianBlur(fgMask,fgMask,new Size(0,0),10);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        // and now do feature detection!
        Imgproc.findContours(fgMask,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        // store locations of rects for blobtracker
        ArrayList<Location> locks = new ArrayList<Location>();

        for (MatOfPoint ct : contours){
            Rect bounds = Imgproc.boundingRect(ct);


            locks.add(new Location(bounds.tl().x,bounds.tl().y,bounds.br().x,bounds.br().y));

            Imgproc.rectangle(frame,bounds.tl(),bounds.br(),new Scalar(0, 0, 255));


        }

        // run blobtracker on the locations
        ArrayList<Blob> blobs = blobTracker.track(locks);

        // headless mode, we don't need to modify the frame for output / debugging

        /* // label the blobs on the image */
        /* for (Blob b : blobs){ */
        /*  */
        /*     // label direction */
        /*     String dir = "-"; */
        /*     if (b.incoming()) */
        /*         dir += "I"; */
        /*     if(b.outgoing()) */
        /*         dir += "O"; */
        /*  */
        /*     Imgproc.putText(frame,b.getId()+dir,new Point(b.getCurrent().brx,b.getCurrent().bry),Core.FONT_HERSHEY_SIMPLEX,1,new Scalar(0,0,255)); */
        /* } */
        
        /* Mat out = new Mat(); */
        /* System.out.println(Imgproc.connectedComponents(fgMask,out)); */

        return frame;
    }


}
