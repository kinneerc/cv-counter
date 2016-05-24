package org.ccfls.counter;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;
import java.sql.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

public class PeopleCounter {
    private GpioController gpio;
    private GpioPinDigitalInput pc;
    protected Indicators idc;
    private Connection conn;

    private String dbServIp, dbServPw, dbServPort, dbServUser, dbServName;

    private long lastTrigger;

    private final long interval = 5000;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String place, library;

    public PeopleCounter(String place, String library) throws SQLException {

        this.place = place;
	this.library = library;

	// read in the database info from config file
	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    		String line;
    		while ((line = br.readLine()) != null) {
      			String[] ct = line.split("=");
			String value = ct[1].replace("\"","");
			switch (ct[0]) {
				case "dbServIp": dbServIp = value; break;
				case "dbServPw": dbServPw = value; break;
				case "dbServPort": dbServPort = value; break;
				case "dbServUser": dbServUser = value; break;
				case "dbServName": dbServName = value; break;
				default: System.out.println("Unrecognized config option: "+ct[0]);
    			}
		}

        setupDatabase();
        idc = new Indicators();

        setupGPIO();

        idc.heartbeat();
        idc.start();

    }

    private void setupDatabase() throws SQLException {
        try{
        Class.forName("com.mysql.jdbc.Driver");
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

	String connectAddr = "jdbc:mysql://"+dbServIp+":"+dbServPort+"/"+dbServName+"?autoReconnect=true";
        conn = DriverManager.getConnection(connectAddr,dbServUser,dbServPw);


    }


    private void setupGPIO(){
        // set up GPIO
        gpio = GpioFactory.getInstance();
        /* pc = gpio.provisionDigitalInputPin( */
        /*         RaspiPin.GPIO_07, PinPullResistance.PULL_DOWN); */
        /*  */
        /* // add listener for interrupts */
        /* pc.addListener(new GpioPinListenerDigital(){ */
        /*     @Override */
        /*     public void handleGpioPinDigitalStateChangeEvent( */
        /*         GpioPinDigitalStateChangeEvent event){ */
        /*         trigger(); */
        /*         } */
        /* }); */


    }

    public static void main(String[] args) throws InterruptedException, SQLException {
        if(args.length != 1){
            System.out.println("Need to give Place as an arg");
            return;
        }

        PeopleCounter pc = new PeopleCounter(args[0]);
        while(true){
            pc.idc.heartbeat();
            Thread.sleep(500);
        }
    }

    public boolean dbServReachable(){
        try{
        int timeout = 2000;
        InetAddress[] addresses = InetAddress.getAllByName(dbServIp);
        for (InetAddress address : addresses) {
            if (address.isReachable(timeout))
                return true;
            else
                return false;
        }
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public long timeSince(){
        return System.currentTimeMillis() - lastTrigger;
    }

    // TODO add type to database
    public void trigger(String type){

        if (timeSince() > interval){
            try{
                lastTrigger = System.currentTimeMillis();
                idc.count();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO AllCounts VALUES("+"NOW()"+",\""+place+"\",\""+library+"\",\""+type+"\")");
                // if all of that worked, then all is well
                idc.error.allIsWell();
            }catch(SQLException e){
                e.printStackTrace();
                // houston, we have a problem
                // first, we'll check network accessability
                if(!dbServReachable()){
                    // if the database is not reachable, it seems we 
                    // have a network problem
                    idc.error.connectivity();
                }else{
                    // if the database server is reachable, then I guess it has
                    // to be a database problem
                    idc.error.database();
                }
            }
            
        }

    }
}
