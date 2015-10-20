package org.ccfls.counter;

//imports
import java.sql.SQLException;
import java.io.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.imgproc.Imgproc;

import org.ccfls.counter.blobtracker.*;

public class CVFrame extends javax.swing.JFrame {

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;

    private String zoneFile = "zones.csv";

    // this class handles reporting to database
    // as well as raspberry pi interactions
    private PeopleCounter pc;

    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;

    Mat frame = new Mat();
    Mat fgMask = new Mat();

    MatOfByte mem = new MatOfByte();

    BackgroundSubtractorMOG2 bsub =  Video.createBackgroundSubtractorMOG2(500,400,false); 

    BlobTracker blobTracker = new BlobTracker();

    private ArrayList<Zone> readZones(File zoneFile){

        ArrayList<Zone> ans = new ArrayList<Zone>();

        try (BufferedReader br = new BufferedReader(new FileReader(zoneFile))){
            String line = "";
		while ((line = br.readLine()) != null) {

		        // use comma as separator
			String[] data = line.split(",");

			ans.add(new Zone(data));

		}
        }catch(IOException e){
            e.printStackTrace();
        }
        
        return ans;

    }

    private void jButton1ActionPerformed(ActionEvent evt){
////////////////////////////////////////////////////////////
 /// start button 
 webSource =new VideoCapture(0);
  myThread = new DaemonThread();
            Thread t = new Thread(myThread);
            t.setDaemon(true);
            myThread.runnable = true;
            t.start();
			 jButton1.setEnabled(false);  //start button
            jButton2.setEnabled(true);  // stop button
}

private void jButton2ActionPerformed(ActionEvent evt){
//////////////////////////////////////////////////////
/// stop button 
myThread.runnable = false;
            jButton2.setEnabled(false);   
            jButton1.setEnabled(true);
            
            webSource.release();			
}

/////////////////////////////////////////////////////////////////////
  class DaemonThread implements Runnable
    {
    protected volatile boolean runnable = false;

    // given a single frame, allow the user to select gateway zones
    private ArrayList<Zone> pickZones(Mat frame) throws IOException {

        // first we'll try to read zones from the file
        File zones = new File(zoneFile);

        // if the file exists, simply read in the zones
        if (zones.exists()){
            return readZones(zones);
        }

        // otherwise, we'll have to get this from the user
        // we'll then save it to a file for next time

        ArrayList<Zone> zoneList = new ArrayList<Zone>();

        Scanner scan = new Scanner(System.in);

        System.out.println("Entering zone control mode.\nThe user will select gateway zones.");

        showMat(frame);

        boolean happy = false;

        while (!happy){
            System.out.print("Enter top left x:");
            double tlx = scan.nextDouble();
            System.out.print("Enter top left y:");
            double tly = scan.nextDouble();
            System.out.print("Enter bottom right x:");
            double brx = scan.nextDouble();
            System.out.print("Enter bottom right y:");
            double bry = scan.nextDouble();

            System.out.print("Confirm zone location (y/n):");
            // 
            Mat preview = new Mat();
            frame.copyTo(preview);
            Point tl = new Point(tlx,tly);
            Point br = new Point(brx,bry);

            Imgproc.rectangle(preview,tl,br,new Scalar(0, 0, 255));

            showMat(preview);

            if(scan.next().equals("y")){
                // create the zone
                System.out.print("Outer gateway? (incoming? y/n):");
                boolean outer = scan.next().equals("y");

                zoneList.add(new Zone(new Location(tl.x,tl.y,br.x,br.y),outer));
                preview.copyTo(frame);

            }

            System.out.print("Done entering zones? (y/n):");
            if(scan.next().equals("y")){
                happy = true;
            }


        }

        // write the zoneList to a file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(zones))){
            writer.write(Zone.toCSV(zoneList));
            writer.flush();
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        return zoneList;

    }

    private boolean showMat(Mat frame) throws IOException {
        Imgcodecs.imencode(".bmp", frame, mem);
		Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));

			    BufferedImage buff = (BufferedImage) im;
			    Graphics g=jPanel1.getGraphics();

			    return g.drawImage(buff, 0, 0, getWidth(), getHeight() -150 , 0, 0, buff.getWidth(), buff.getHeight(), null);

    }

    @Override
    public  void run()
    {
        synchronized(this)
        {
            while(runnable)
            {
                if(webSource.grab())
                {
		    	try
                        {

                if(!blobTracker.zoned()){
                    webSource.retrieve(frame);
                    blobTracker.setZones(pickZones(frame));
                }

                Mat show = processFrame(webSource);

			    if (showMat(show))
			    
			    if(runnable == false)
                            {
			    	System.out.println("Going to wait()");
			    	this.wait();
			    }
			 }
			 catch(Exception ex)
                         {
                             ex.printStackTrace();
			    System.out.println("Error");
                         }
                }
            }
        }
     }
   }
   /////////////////////////////////////////////////////////

    public Mat processFrame(VideoCapture vc){

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

        // label the blobs on the image
        for (Blob b : blobs){

            // label direction
            String dir = "-";
            if (b.incoming())
                dir += "I";
            if(b.outgoing())
                dir += "O";

            Imgproc.putText(frame,b.getId()+dir,new Point(b.getCurrent().brx,b.getCurrent().bry),Core.FONT_HERSHEY_SIMPLEX,1,new Scalar(0,0,255));
        }
        
        /* Mat out = new Mat(); */
        /* System.out.println(Imgproc.connectedComponents(fgMask,out)); */

        return frame;
    }

    public CVFrame(String place){
        initComponents();
        try{
        pc = new PeopleCounter(place);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


     private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Pause");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(255, 255, 255)
                .addComponent(jButton1)
                .addGap(86, 86, 86)
                .addComponent(jButton2)
                .addContainerGap(258, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   // add this line to main method
  public static void main(String[] args){ 
           System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // load native library of opencv
           java.awt.EventQueue.invokeLater(new Runnable(){
            public void run(){
                new CVFrame(args[0]).setVisible(true);
            }
           });
}
}
