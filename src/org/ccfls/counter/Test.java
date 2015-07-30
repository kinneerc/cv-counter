package org.ccfls.counter;

//imports
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.imgproc.Imgproc;

public class Test extends javax.swing.JFrame {

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;

    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;

    Mat frame = new Mat();
    Mat fgMask = new Mat();

    MatOfByte mem = new MatOfByte();

    BackgroundSubtractorMOG2 bsub =  Video.createBackgroundSubtractorMOG2(500,400,false); 

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
                            Mat show = processFrame(webSource);
			    Imgcodecs.imencode(".bmp", show, mem);
			    Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));

			    BufferedImage buff = (BufferedImage) im;
			    Graphics g=jPanel1.getGraphics();

			    if (g.drawImage(buff, 0, 0, getWidth(), getHeight() -150 , 0, 0, buff.getWidth(), buff.getHeight(), null))
			    
			    if(runnable == false)
                            {
			    	System.out.println("Going to wait()");
			    	this.wait();
			    }
			 }
			 catch(Exception ex)
                         {
			    System.out.println("Error");
                         }
                }
            }
        }
     }
   }
   /////////////////////////////////////////////////////////

    public Mat processFrame(VideoCapture vc){
        vc.retrieve(frame);
        bsub.apply(frame,fgMask);

        Imgproc.GaussianBlur(fgMask,fgMask,new Size(0,0),10);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        // and now do feature detection!
        Imgproc.findContours(fgMask,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint ct : contours){
            Rect bounds = Imgproc.boundingRect(ct);

            Imgproc.rectangle(frame,bounds.tl(),bounds.br(),new Scalar(0, 0, 255));


        }
        /* Mat out = new Mat(); */
        /* System.out.println(Imgproc.connectedComponents(fgMask,out)); */

        return frame;
    }

    public Test(){
        initComponents();
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
                new Test().setVisible(true);
            }
           });
}
}
