
package ithaki;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.util.Vector;


/**
 * A class that provides methods for working with various types of data and telemetry in a drone or vehicle system.
 * This class includes methods for processing echo signals, images, sound, vehicle telemetry, and more.
 *
 * <ul>
 *   <li><code>echo(boolean, String)</code>: This method sends an echo signal to a specified port using UDP protocol.</li>
 *   <li><code>image(boolean, int, String, String, String)</code>: This method generates an image using specified
 *   	parameters and saves it to a file on the local machine. The image is created by sending UDP packets to a specified port.</li>
 *   <li><code>soundDPCM(int, String, String, String)</code>: This method generates a sound signal using differential
 *   	pulse-code modulation (DPCM) and saves it to a file on the local machine.</li>
 *   <li><code>soundAQDPCM(int, String, String)</code>: This method generates a sound signal using adaptive quantization
 *   	differential pulse-code modulation (AQ-DPCM) and saves it to a file on the local machine.</li>
 *   <li><code>copterTelemetryUDP(String)</code>: This method simulates a telemetry feed from a remote control
 *   	helicopter by sending UDP packets to a specified port.</li>
 *   <li><code>vehicleOBDII(int, String)</code>: This method simulates data output from a vehicle's on-board diagnostics
 *   	(OBD-II) system and sends the data to a specified port using TCP protocol.</li>
 *   <li><code>video(boolean, int, String, String)</code>: This method generates a video using specified parameters and
 *   	saves it to a file on the local machine. The video is created by converting a series of images to a video format using FFmpeg.</li>
 *   <li><code>copterTCPtest(String)</code>: This method tests the TCP connection to a remote control helicopter by
 *  	 sending a test message to a specified port.</li>
 * </ul>
 */
public class UserProgram {
	
	 public static int clientport=48019;
	 public static int serverport=38019;

	/**
	 * Sends 4 echo requests to the server and prints them.
	 * Used at the start of almost each program.
	 *
	 * @param echocode a String representing the code of the echo request
	 * @throws IOException if an I/O error occurs
	 */
	public static void initiate(String echocode) throws IOException {
		InetAddress ithakilab;  // to hold the IP address of the server
		DatagramSocket lab = new DatagramSocket();  // to send packets to the server
		ithakilab = InetAddress.getByName("155.207.18.208");  // gets the IP address of the server
		lab.connect(ithakilab, serverport);  // initiates a connection with the server
		DatagramSocket pc = new DatagramSocket(clientport);  // to receive packets from the server
		int count = 0;
		byte buffer[] = null;
		while (count != 4) {  // send packet 4 times
			buffer = ("echo_request_code=E" + echocode).getBytes();
			int bytesize = 32;
			DatagramPacket datasent = new DatagramPacket(buffer, buffer.length);
			lab.send(datasent);  // sends the packet to the server
			buffer = new byte[bytesize];
			DatagramPacket datareceived = new DatagramPacket(buffer, buffer.length);
			try {
				pc.receive(datareceived);  // receives the response packet, timeout is 4 sec
				pc.setSoTimeout(4000);
				System.out.println(new String(buffer));
			} catch (SocketTimeoutException e) {
				// do nothing
			}
			count++;
		}
		System.out.println("End Packets");
		lab.disconnect();
		lab.close();
		pc.close();
		return;
	}

	/**
	 * Sends and receives echo packets for 4 minutes, calculates throughput for 8, 16, 32 seconds as well as rtt, srtt,
	 * and rto and saves them to files.
	 * @param temp          whether to send packet for temperature
	 * @param echocode      code to append to output file names
	 * @throws IOException  if an I/O error occurs
	 */
	public static void echo(boolean temp, String echocode) throws IOException {

		// Initialize files and vDif (which stores rtt values)
		Vector<Integer> vDif = new Vector<Integer>();
		FileWriter th8 = new FileWriter("throutput8_E" + echocode + ".txt");
		FileWriter th16 = new FileWriter("throutput16_E" + echocode + ".txt");
		FileWriter th32 = new FileWriter("throutput32_E" + echocode + ".txt");
		FileWriter timesfile = new FileWriter("timesfile" + echocode + ".txt");
		FileWriter dif = new FileWriter("diffirencies" + echocode + ".txt");
		FileWriter Srtt = new FileWriter("srtt" + echocode + ".txt");
		FileWriter S = new FileWriter("s" + echocode + ".txt");
		FileWriter Rto = new FileWriter("rto" + echocode + ".txt");

		int bytesize = 0;
		InetAddress ithakilab;
		DatagramSocket lab = new DatagramSocket();  // initiate connection with server
		ithakilab = InetAddress.getByName("155.207.18.208");
		lab.connect(ithakilab, serverport);
		DatagramSocket pc = new DatagramSocket(clientport);

		long startTime = System.currentTimeMillis();  // initialize variables
		long start;
		long end;
		long endTime = startTime + (240000);
		byte buffer[] = null;
		int t8 = 0;
		int t16 = 0;
		int t32 = 0;
		long tstart8 = System.currentTimeMillis();
		long tstart16 = System.currentTimeMillis();
		long tstart32 = System.currentTimeMillis();

		while (System.currentTimeMillis() < endTime) {  // loop for 4 minutes
			if (temp == false) {
				// send packet for echo
				buffer = ("echo_request_code=E" + echocode).getBytes();
				bytesize = 32;
			} else {
				// send packet for temperature
				buffer = ("echo_request_codeT00=E" + echocode).getBytes();
				bytesize = 54;
			}

			// send packet
			DatagramPacket datasent = new DatagramPacket(buffer, buffer.length);
			start = System.currentTimeMillis();
			lab.send(datasent);

			// receive echo packet
			buffer = new byte[bytesize];
			DatagramPacket datareceived = new DatagramPacket(buffer, buffer.length);
			try {
				pc.receive(datareceived);
				pc.setSoTimeout(4000);  // set timeout: 4 sec
				end = System.currentTimeMillis();

				// calculate throughput every 8 seconds
				if (System.currentTimeMillis() <= tstart8 + 7600) {
					t8 += 32;  // t8 is the number of bytes received; after every packet it adds 32 bytes
				} else {
					long through8 = (t8 * 8 * 1000) / (end - tstart8);  // value of throughput is calculated and stored at file
					th8.write(String.valueOf(through8));
					th8.write("\n");
					tstart8 = System.currentTimeMillis();
					t8=0;                                                //reset number of received bytes
	    			}

	    			if (System.currentTimeMillis() <= tstart16+15500 ) {         //as above but for throughoutput 16sec
	    				t16+=32;
	    			}
	    			else {
	    				long through16= (t16*8*1000)/(end-tstart16);
	    				th16.write(String.valueOf(through16));
	    				th16.write("\n");
	    				tstart16=System.currentTimeMillis();
	    				t16=0;
	    			}
	    				if (System.currentTimeMillis() <= tstart32+31600 ) {        //as above but for throughoutput 32sec
		    				t32+=32;
		    			}
		    			else {
		    				long through32= (t32*8*1000)/(end-tstart32);
		    				th32.write(String.valueOf(through32));
		    				th32.write("\n");
		    				tstart32=System.currentTimeMillis();
		    				t32=0;
		    			}

	    			vDif.addElement((int)(end-start));                //add rtt to vector and write it to file
	    			dif.write(String.valueOf(end-start));
	    			dif.write("\n");
	    		    timesfile.write(new String(datareceived.getData()));   //write value of echo packets to file
	    		    timesfile.write("\n");
	    
	    		    System.out.println(new String(buffer));
	    			}
	    			catch(SocketTimeoutException e) {
	    			              ;
	    			}
	      }
	      
	      
	    double a=0.9;
      	double b=0.75;
      	double c=4;
      	int[] rtt= new int[vDif.size()];
		for(int i=0; i<vDif.size(); i++) {
			rtt[i]=vDif.elementAt(i);
		}
  			
      				double[] srtt= new double[rtt.length];
      		    	double[] s= new double[rtt.length];
      		    	double[] rto= new double[rtt.length];
      		    	
      				srtt[0]=rtt[0];
      		    	s[0]=rtt[0]/2;
      		    	rto[0]=srtt[0] +c*s[0];
      		    	
      		    	for(int t=1; t<rtt.length; t++) {
      		    		srtt[t]=a*srtt[t-1] + (1-a)*rtt[t];
      		    		s[t]=b*s[t-1] +(1-b)*Math.abs(srtt[t] - rtt[t]);
      		    		rto[t]=srtt[t] +c*s[t];
      		    		
      		    	}
      		    	for(int t=0; t<rtt.length; t++) {
      		    		Srtt.write(String.valueOf(srtt[t]));
      		    		S.write(String.valueOf(s[t]));
      		    		Rto.write(String.valueOf(rto[t]));
      		    		Srtt.write("\n");
      		    		S.write("\n");
      		    		Rto.write("\n");   		   
      		    	}
      		    Srtt.close();
      		   	S.close();                       //close files and disconnect
      		    Rto.close();
      		    
      		    
	      System.out.print("End");
	      timesfile.close();
	      th8.close();
	      th16.close();
	      th32.close();
	      dif.close();
	      lab.disconnect();
	      lab.close();
	      pc.close();
	      return;
    }

	/**
	 * Receives an image from the server in packets of size L, from camera cam, and optionally uses a flow mechanism.
	 *
	 * @param flow A boolean indicating whether or not to use the flow mechanism.
	 * @param L The packet size.
	 * @param cam The camera to receive the image from.
	 * @param imagecode A string identifier for the image.
	 * @param echocode A string identifier for the client.
	 * @throws IOException If there is an I/O error.
	 */
	public static void image(boolean flow, int L, String cam, String imagecode, String echocode) throws IOException {
		initiate(echocode); // initialize the client with echocode

		// Create a file to write the image to.
		FileOutputStream image = new FileOutputStream("image" + imagecode + ".jpg");

		// Connect to the server.
		InetAddress ithakilab;
		DatagramSocket lab = new DatagramSocket();
		ithakilab = InetAddress.getByName("155.207.18.208");
		lab.connect(ithakilab, serverport);
		DatagramSocket pc = new DatagramSocket(clientport);

		byte buffer[] = null;
		int flag = 0;

		if (flow == false) {
			// If the flow mechanism is not used, create a packet with the user's preferences.
			buffer = ("image_request_code=M" + imagecode + "CAM=" + cam + "UDP=" + L).getBytes();
		} else {
			// If the flow mechanism is used, create a packet with the user's preferences and turn on the flow mechanism.
			buffer = ("image_request_code=M" + imagecode + "FLOW=ONCAM=" + cam + "UDP=" + L).getBytes();
		}

		// Send the packet to the server.
		DatagramPacket datasent = new DatagramPacket(buffer, buffer.length);
		lab.send(datasent);

		while (flag == 0) {
			// Receive sequential response packets from the server and write them to the image file until a packet of size not equal to L is received.
			if (flow == true) {
				// If the flow mechanism is used, send a packet with the word NEXT to the server.
				buffer = ("NEXT").getBytes();
				datasent = new DatagramPacket(buffer, buffer.length);
				lab.send(datasent);
			}

			buffer = new byte[L];
			DatagramPacket datareceived = new DatagramPacket(buffer, buffer.length);

			try {
				pc.receive(datareceived);
				image.write(datareceived.getData());
			} catch (SocketTimeoutException e) {
				// If a timeout occurs, just continue.
			}

			if (datareceived.getLength() != L) {
				flag = 1;
			}
		}

		// Close the image file and disconnect from the server.
		image.close();
		System.out.print("End");
		lab.disconnect();
		lab.close();
		pc.close();
		return;
	}

	/**
	 * Test application that sends a packet with the preferred direction to the PTZ camera so that it moves.
	 *
	 * @param direction: a string containing the direction in which the camera should move
	 * @throws IOException: If an I/O error occurs
	 */
	public static void imageMove(String direction) throws IOException{

		InetAddress ithakilab;   // address of the Ithaki lab server
		DatagramSocket lab = new DatagramSocket();  // socket for connecting to the server
		ithakilab = InetAddress.getByName("155.207.18.208");  // IP address of the Ithaki lab server
		lab.connect(ithakilab, serverport);  // connect the socket to the server
		DatagramSocket pc = new DatagramSocket(clientport); // create a new socket for receiving data from the server
		byte[] buffer = ("").getBytes();  // initialize the buffer for sending the packet
		buffer = ("image_request_code=M2586CAM=PTZDIR=" + direction).getBytes(); // create and send the packet containing the direction to the server
		DatagramPacket datasent = new DatagramPacket(buffer, buffer.length);
		lab.send(datasent);
		lab.disconnect();   // disconnect from the server
		lab.close();  // close the socket
		pc.close();   // close the socket
	}


	/**
	 * Downloads images for 1 minute and saves them in sequential files so they can be turned into a video using ffmpeg library and command:
	 * ffmpeg -start_number 1 -framerate 0.6 -i image%d.jpg -vcodec mpeg4 video.mp4
	 *
	 * @param flow: a boolean that determines whether or not to use flow control
	 * @param L: the size of the datagram packet
	 * @param cam: the camera ID
	 * @param imagecode: the image code for the request
	 *
	 * @throws IOException
	 */
	public static void video(boolean flow, int L, String cam, String imagecode) throws IOException{

		// Set up variables
		int i=1;
		InetAddress ithakilab;
		DatagramSocket lab= new DatagramSocket();
		ithakilab = InetAddress.getByName("155.207.18.208");
		lab.connect(ithakilab, serverport);
		DatagramSocket pc= new DatagramSocket(clientport);
		long startTime2 = System.currentTimeMillis();
		long endTime2 = startTime2+(60000);  //1min

		// Loop until 1 minute has passed
		while(System.currentTimeMillis()<endTime2){
			// Open file to write image to
			FileOutputStream image = new FileOutputStream("image"+i+".jpg");

			int flag=0;
			while(flag==0) {
				byte[] buffer=null;
				// Create byte array based on whether flow control is on or off
				if(flow==false) {
					buffer=("image_request_code=M"+ imagecode+ "CAM="+ cam +"UDP="+L).getBytes();
				}
				else {
					buffer=("image_request_code=M"+ imagecode + "FLOW=ONCAM="+ cam +"UDP="+L).getBytes();
				}
				// Send packet to server
				DatagramPacket datasent= new DatagramPacket(buffer, buffer.length);
				lab.send(datasent);

				// If flow control is on, send "NEXT" packet
				if(flow==true) {
					buffer=("NEXT").getBytes();
					datasent= new DatagramPacket(buffer, buffer.length);
					lab.send(datasent);
				}

				// Receive packet from server
				buffer= new byte[L];
				DatagramPacket datareceived= new DatagramPacket(buffer, buffer.length);

				try {
					pc.receive(datareceived);
					image.write(buffer);
					//pc.setSoTimeout(3000);
				}
				catch(SocketTimeoutException e) {
					// Do nothing
				}
				// Check if the received packet is smaller than the datagram packet size
				if(datareceived.getLength() != L) {
					flag=1;
				}
			}

			// Close file and print end message
			image.close();
			System.out.println("End"+i);
			i++;
		}

		// Close sockets
		lab.disconnect();
		lab.close();
		pc.close();
		return;
	}

	/**
	 * Plays a track from the server of the Ithaki Lab or from the frequency generator with DPCM coding
	 * and saves it to file, as well as the differences and the samples.
	 *
	 * @param xxx: the number of packets to receive and decode.
	 * @param audiocode: the code of the audio file to be played.
	 * @param TorF: a flag indicating if the audio is to be played by the server or the frequency generator.
	 * @param echocode: the echocode to be used.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void soundDPCM(int xxx, String audiocode, String TorF, String echocode) throws IOException {

		// Initialize
		initiate(echocode);

		// Connect to server
		InetAddress ithakilab;
		DatagramSocket lab = new DatagramSocket();
		ithakilab = InetAddress.getByName("155.207.18.208");
		lab.connect(ithakilab, serverport);
		DatagramSocket pc = new DatagramSocket(clientport);
		byte[][] pinakas = new byte[xxx][128];

		// Create files for differences and samples
		FileWriter Dif = new FileWriter("difDPCM" + audiocode + ".txt");
		FileWriter sample = new FileWriter("samplesDPCM" + audiocode + ".txt");

		// Create and send a packet to the server
		byte[] buffer = ("sound_request_code=A" + audiocode + TorF + xxx).getBytes();
		DatagramPacket datasent = new DatagramPacket(buffer, buffer.length);
		lab.send(datasent);

		int i = 0; // Receive the requested number of packets
		while(i < xxx) {
			buffer = new byte[128];
			DatagramPacket datareceived = new DatagramPacket(buffer, buffer.length); // Receive a packet with length 128 bytes

			try {
				pc.receive(datareceived);
				pinakas[i] = datareceived.getData(); // Save all the packets in a matrix so it can be used for decoding. Set timeout 0,5sec
				pc.setSoTimeout(500);
			}
			catch(SocketTimeoutException e) {
				// Continue waiting for packets if timeout occurs
			}
			i++;
		}

		byte[] data = new byte[i * 256]; // Create a matrix "data" where decoded samples will be stored
		int counter = 0;

		for(int k = 0; k < i; k++) { // Loop for the number of requested packets
			int[] dif = new int[256]; // Create a matrix where differences will be stored
			int countdif = 0;

			for(int n = 0; n < 128; n++) { // Loop for the bytes of every packet (128 bytes)
				int add1 = 15; // Create nibbles and differences
				int add2 = 240;
				int a = pinakas[k][n];
				int Nibble1 = (add1 & a);
				int Nibble2 = ((add2 & a) >> 4);
				int b = 1;
				dif[countdif++] = (Nibble1 - 8) * b;
				dif[countdif++] = (Nibble2 - 8) * b;
			}
	  	    	  
	  	    	 if(k==0) {                         //create samples. first sample of first packet is 0. other first samples = 
              	   data[counter++]=0;	            //diffirence calculated + last sample of previous packet. all other packets are calculated as: 	   
                 }                                  //diffirence calculated + sample of previous packet. if sample is out of range, it sets as the upper or lower limit
                 else {
              	   data[counter]=(byte) (dif[0]+data[counter-1]);
              	   if(data[counter]>127) {
              		   data[counter]=127;
              	   }
              	 if(data[counter]<-128) {
            		   data[counter]=-128;
            	   }
              	   counter++;
                 }
	  	    	                                        
	  	    	for(int p=1; p<256; p++){
	    			data[counter] = (byte) (dif[p] + data[counter-1]);
			    	
			    	if (data[counter]>127) {data[counter]=127;}
			    	if (data[counter]<-128) {data[counter]=-128;}
			    	counter++;
	    		}
	  	    	
	  	    	for(int d=0; d<256; d++) {                       //write diffirencies to file
	  	    		Dif.write(String.valueOf(dif[d]));
	  	    		Dif.write("\n");
	  	    	}

	  	 }
  	    	for(int kl=0; kl<2000; kl++) {                   //write first 2000 samples to file
	  	    		sample.write(String.valueOf(data[kl]));
	  	    		sample.write("\n");
	  	           }

	  	    ByteArrayInputStream bis = new ByteArrayInputStream(data);           //save audio to file
	  	  AudioFormat audioFormat= new AudioFormat(8000,8,1,true,false);
	  	  AudioInputStream audioInputStream=new AudioInputStream(bis, audioFormat, data.length);
	  	  
	  	  
	  		try {                
		  	    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("DPCM"+audiocode+".wav"));
		  	}
		  	catch(Exception e) {
		  	    
		  	}
	  	  
	  	  
	  		 try{                                    //play audio
	 	            AudioFormat linearPCM = new AudioFormat( 8000, 8, 1, true, false );
	 	            SourceDataLine lineOut = AudioSystem.getSourceDataLine( linearPCM );
	 	            lineOut.open( linearPCM, 32000 );
	 	            lineOut.start();
	 	            lineOut.write( data, 0, data.length );
	 	            lineOut.stop();
	 	            lineOut.close();
	 	        }
	 	        catch( Exception x ){
	 	           
	 	        }
	  	  
	     System.out.print("End");         //close files and disconnect
	     Dif.close();
	     sample.close();
	      lab.disconnect();
	      lab.close();
	      pc.close();
	      return;
    }


	/**
	 * Plays audio from ithaki using AQDPCM coding, saves it as well as samples,
	 * differences, mean, and step. Receives the audio code, echoes code, and the
	 * number of packets to receive. The difference, mean, and step are used to decode
	 * the audio signal, and the result is saved to text files.
	 *
	 * @param numOfPackets The number of packets to receive
	 * @param audioCode The audio code to send to the server
	 * @param echoCode The echo code to send to the server
	 *
	 * @throws IOException If an I/O error occurs
	 */
    public static void soundAQDPCM(int xxx, String audiocode, String echocode) throws IOException{ //play audio from ithaki using AQDPCM coding, 
    																			//save it as well as samples,diffirences, mean and step
    	initiate(echocode);
    	
     	InetAddress ithakilab;                                             //connect to server
		DatagramSocket lab= new DatagramSocket();
		ithakilab = InetAddress.getByName("155.207.18.208");
		lab.connect(ithakilab, serverport);
		DatagramSocket pc= new DatagramSocket(clientport);	
		
	    byte[] buffer=("sound_request_code=A" +audiocode+ "AQF"+ xxx).getBytes(); //create and send packet 
	    
	    DatagramPacket datasent= new DatagramPacket(buffer, buffer.length);
	   	lab.send(datasent);
	   	buffer= new byte[128];
	   	
	   	DatagramPacket datareceived= new DatagramPacket(buffer, buffer.length);  //create vectors and files
	   	Vector<Integer> vSamples= new Vector<Integer>();
	    Vector<Integer> vDif= new Vector<Integer>(); 
	    Vector<Integer> vMean= new Vector<Integer>();
	    Vector<Integer> vStep= new Vector<Integer>(); 
	    FileWriter Dif = new FileWriter("difAQDPCM"+audiocode+".txt");
	    FileWriter Sample = new FileWriter("samplesAQDPCM"+audiocode+".txt");
	    FileWriter Mean = new FileWriter("mean"+audiocode+".txt");
	    FileWriter Step = new FileWriter("step"+audiocode+".txt");    
	    
	    		    int numOfSamplesPerPack = 2*128;                  //initialize variables and matrixes
	    		    int dif[] = new int[numOfSamplesPerPack];
	    		    int packSample[] = new int[numOfSamplesPerPack];
	    	        int a=0;
	    		    //INITIALIZATIONS

	    		   int mean;
	    		   int step;                  
	    		   
	    		    for(int it=1; it<=xxx; it++){  //loop for the number of received packets
	    		    	try {
	    		    		
	    		    		pc.receive(datareceived);       //receive audio packet. timeout: 0,5 sec
	    		    		pc.setSoTimeout(500);
	    		    	//	System.out.println(a);
	    		    		a++;
	    		    		mean = (buffer[1]<<8) + (buffer[0]&0xFF);  //calc mean and step from the first 4 bytes of packet and add them to vectors
	    			   		vMean.addElement(mean);
	    			   			
	    			   		step = (buffer[3]<<8) + (buffer[2]&0xFF);
	    			   		vStep.addElement(step);
	    			   		
	    			   		
	    			   		//store 4bit diffirencies in array dif [256]
	    			   		
	    		    		int j=0;
	    			   		for(int i=4; i<buffer.length; i++){   //create nibbles and diffirencies 
	    			   			int nibble1= buffer[i] >> 4 & 0x0F ;
	    			   			int nibble2 = buffer[i] & 0x0F;
	    			   			nibble2 += -8 ;
	    			   			nibble1 += -8 ;
	    			   			dif[j++] = nibble1 * step;
	    			   			dif[j++] = nibble2 * step;
	    			   			
	    			   		}
	    			   		
	    			   											//save diffirencies in vector
	    			   		for(int i=0; i<numOfSamplesPerPack; i++){
	    			   			vDif.addElement(dif[i]);
	    			   		}
	    			   		
	    			 
	    			   			
	    			   											//calc first sample
	    					    if (it==1){
	    					    								//for the first packet
	    							packSample[0] = 0;
	    					    }else{
	    					    								//for the rest packets
	    					    	packSample[0] = dif[0] + vSamples.lastElement();	    					    	
	    					    	if (packSample[0]>32000) {packSample[0]=32000;}
	    					    	if (packSample[0]<-32000) {packSample[0]=-32000;}
	    					    }
	    					    
	    			    										//calc sample for the rest packets
	    			    		for(int i=1; i<numOfSamplesPerPack; i++){
	    			    			packSample[i] = dif[i] + packSample[i-1];	    					    	
	    					    	if (packSample[i]>32000) {packSample[i]=32000;}
	    					    	if (packSample[i]<-32000) {packSample[i]=-32000;}
	    			    		}
  		    		
	    			    										//add to sample the mean value and add them to vector
	    		
	    		    		for(int i=0; i<numOfSamplesPerPack; i++){
	    		    			vSamples.addElement(packSample[i]+mean);
	    		    		}
	    		    		
	    		    	} catch (Exception x) {
	    	    		
	    		    		
	    		    	}
	    		    }
	    		                                   //write to file the first 2000 samples, all the diffirencies, all the means and all the steps
	    		    for(int i=0;i<2000 ;i++){
	    				Sample.write(String.valueOf(vSamples.elementAt(i)));
	    				Sample.write("\n");
	    			}
	    		    
	    		    for(int i=0;i<2000 ;i++){
	    				Dif.write(String.valueOf(vDif.elementAt(i)));
	    				Dif.write("\n");
	    			}
	    		    
	    		    for(int i=0;i<300 ;i++){
	    				Mean.write(String.valueOf(vMean.elementAt(i)));
	    				Mean.write("\n");
	    			}
	    		    
	    		    for(int i=0;i<300 ;i++){
	    				Step.write(String.valueOf(vStep.elementAt(i)));
	    				Step.write("\n");
	    			}
	    		    
	    			
	    																	//copy to int array to be able to cast with byte
	    			int[] temp = new int[vSamples.size()]; 
	    			for(int i=0;i<temp.length ;i++){
	    				temp[i] = vSamples.elementAt(i);
	    			}

	    													                  //create byte array of samples
	    				byte[] audioBufferOut = new byte[2*vSamples.size()]; //2 bytes per sample (integer)
	    				for(int i=0;i<vSamples.size() ;i++){ 
	    					audioBufferOut[2*i] = (byte) (temp[i] & 0xFF) ; //LSB
	    					audioBufferOut[2*i+1] = (byte) ((temp[i]>>8) & 0xFF) ; //MSB
	    				}
	    				

	  	      
	    																		//play the audio
	  	    	 try{
	 	            AudioFormat linearPCM = new AudioFormat( 8000, 16, 1, true, false );
	 	            SourceDataLine lineOut = AudioSystem.getSourceDataLine( linearPCM );
	 	            lineOut.open( linearPCM, 32000 );
	 	            lineOut.start();
	 	            lineOut.write( audioBufferOut, 0, audioBufferOut.length );
	 	            lineOut.stop();
	 	            lineOut.close();
	 	        }
	 	        catch( Exception x ){
	 	           
	 	        }
	  	      
	  	                                                               //save the audio
	  	  
	  		 ByteArrayInputStream bis = new ByteArrayInputStream(audioBufferOut); 
		  	  AudioFormat audioFormat= new AudioFormat(8000,16,1,true,false);
		  	  AudioInputStream audioInputStream=new AudioInputStream(bis, audioFormat, audioBufferOut.length);
		  	  
		  	 
		  		try {                 
			  	    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("AQDPCM"+audiocode+".wav"));
			  	}
			  	catch(Exception e) {
			  	    
			  	}
		  	  
		     System.out.print("End");                        //close files and disconnect
		     Sample.close();
		     Dif.close();
		     Mean.close();
		     Step.close();
		      lab.disconnect();
		      lab.close();
		      pc.close();
		      return;
    }

	/**
	 * Receive values from copter while the copter app is open and save values to files.
	 *
	 * @param echocode the echocode of the copter
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public static void copterTelemetryUDP(String echocode) throws IOException {
		// initialize communication with the copter using the given echocode
		initiate(echocode);

		// create output files for telemetry values
		FileOutputStream Tel = new FileOutputStream("telemetry.txt");
		FileWriter LLL = new FileWriter("LLL.txt");
		FileWriter RRR = new FileWriter("RRR.txt");
		FileWriter Alt = new FileWriter("Altitude.txt");
		FileWriter Temp = new FileWriter("Temperature.txt");
		FileWriter Press = new FileWriter("Pressure.txt");

		// create a socket to receive datagrams from the copter
		DatagramSocket copter = new DatagramSocket(48078);
		byte[] buffer = new byte[256];
		DatagramPacket q = new DatagramPacket(buffer, buffer.length);

		// receive telemetry values from the copter for 2 minutes (1 packet per second estimate)
		for (int i = 0; i < 120; i++) {
			try {
				copter.receive(q); // receive datagram packet from the copter
				// write the received message to the telemetry file
				Tel.write(buffer, 0, q.getLength());
				Tel.write("\r\n".getBytes());

				String line = new String(buffer, 0, q.getLength());
				System.out.println(line);

				// split the message so each value can be written to its own file
				String[] splitLine = null;
				splitLine = line.split(" ");

				// write LLL motor value to file
				String[] str = null;
				str = splitLine[3].split("=");
				LLL.write(str[1]);
				LLL.write("\n");

				// write RRR motor value to file
				str = null;
				str = splitLine[4].split("=");
				RRR.write(str[1]);
				RRR.write("\n");

				// write altitude value to file
				str = null;
				str = splitLine[5].split("=");
				Alt.write(str[1]);
				Alt.write("\n");

				// write temperature value to file
				str = null;
				str = splitLine[6].split("=");
				Temp.write(str[1]);
				Temp.write("\n");

				// write pressure value to file
				str = null;
				str = splitLine[7].split("=");
				Press.write(str[1]);
				Press.write("\n");

			} catch (Exception e) {
				// ignore any exceptions that occur during processing of the datagram packet
				;
			}
		}

		// close all the files and the socket
		LLL.close();
		RRR.close();
		Alt.close();
		Temp.close();
		Press.close();
		copter.close();
		Tel.close();

		return;
	}

	/**
	 * Connect to a copter through TCP and send packets to fly the copter with specific engine values.
	 *
	 *
	 * @param wantedlevel  the altitude at which the copter should fly.
	 *
	 * @throws IOException
	 */
	public static void copterTCPtest(String wantedlevel) throws IOException {

		byte[] host = { (byte)155, (byte)207, 18, (byte)208 }; // IP address of the copter.
		InetAddress hostAddress = InetAddress.getByAddress(host);
		Socket ithaki = new Socket(hostAddress, 38048); // Creates a stream socket and connects it to the specified port number at the specified IP address.
		InputStream in = ithaki.getInputStream(); // Returns an input stream for this socket.
		OutputStream out = ithaki.getOutputStream(); // Returns an output stream for this socket.

		String motor = "190"; // Engine values.
		for (int i = 0; i < 20; i++) { // Send packets to copter.
			out.write(("AUTO FLIGHTLEVEL=" + wantedlevel + " LMOTOR=" + motor + " RMOTOR=" + motor + " PILOT \r\n").getBytes());

			byte[] b = new byte[113]; // Buffer to store received packets.
			in.read(b, 0, 113); // Read received packets into the buffer.

			String line = new String(b, 0, b.length); // Convert received packets to string.
			System.out.println(line); // Print received packets to console.
		}

		ithaki.close(); // Close the connection to the copter.
		return;
	}


	/**
	 * This method receives diagnostics from a vehicle through TCP and saves them to a file.
	 *
	 * @param choice an integer value indicating which value to receive.
	 * @param echocode a string value of a unique code for the vehicle.
	 *
	 * @throws IOException if there is an issue with the file writing or the connection to the server.
	 */
	public static void vehicleOBDII(int choice, String echocode) throws IOException {

    /*
    The following are the valid choices:
        1 - Engine run time
        2 - Intake air temperature
        3 - Throttle position
        4 - Engine RPM
        5 - Vehicle speed
        6 - Coolant temperature
    */

		// Initiate the connection with the vehicle.
		initiate(echocode);

		// Determine the pID and filename according to the choice.
		String mode = "01", pID = null, filename;
		if (choice == 1) {
			pID = "1F";
			filename = "Engine_run_time.txt";
		} else if (choice == 2) {
			pID = "0F";
			filename = "Intake_air_temperature.txt";
		} else if (choice == 3) {
			pID = "11";
			filename = "Throttle_position.txt";
		} else if (choice == 4) {
			pID = "0C";
			filename = "Engine_RPM.txt";
		} else if (choice == 5) {
			pID = "0D";
			filename = "Vehicle_speed.txt";
		} else if (choice == 6) {
			pID = "05";
			filename = "Coolant_temperature.txt";
		} else {
			System.out.println("Invalid input!");
			return;
		}

		// Create the file and connect to the server through TCP.
		FileWriter fop = new FileWriter(filename);
		byte[] host = {(byte) 155, (byte) 207, 18, (byte) 208};
		InetAddress hostAddress = InetAddress.getByAddress(host);
		Socket ithaki = new Socket(hostAddress, 29078);
		InputStream in = ithaki.getInputStream();
		OutputStream out = ithaki.getOutputStream();

		byte[] buffer = (mode + " " + pID + "\r").getBytes();

		int counter = 0;
		String str = null;
		int a;

		// Receive data, split it to strings, process it, and save it to file.
	     
	     if(pID == "1F") {            //recieve data, split it to strings, process it and save it to file
	    	 int XX,YY,data;
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 	
	    			//	System.out.println("--"+str+ "--");
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);
		    			YY = Integer.parseInt(splitLine[3], 16);
		    			data = 256 * XX + YY;
		    			System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    	 }
	    	 fop.close();
	     }
	     
	     else if(pID == "0F") {               //same as above
	    	 int XX, data;
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 		    			
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);
		    			
		    			data = XX - 40;
		    		//	System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    	 }
	    	 
	    	 fop.close();
	     }
	                                                          //same as above
	     else if(pID == "11") {
	    	 int XX;
	    	 float data;
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 		    			
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);		    			
		    			data =(float)XX*100/255;
		    		//	System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    		 
	    		 
	    	 }
	    	 
	    	 fop.close();
	     }
	     													//same as above
	     else if(pID == "0C") {
	    	 int XX, YY, data;
	    	 
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 		    			
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);
		    			YY = Integer.parseInt(splitLine[3], 16);
		    			data =( XX * 256 + YY) /4;
		    		//	System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    		 
	    		 
	    	 }
	    	 
	    	 fop.close();
	     }
	     																//same as above
	     else if(pID == "0D") {
	    	 int XX, data;
	    	 
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 		    			
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);		    			
		    			data =XX;
		    		//	System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    		 
	    		 
	    	 }
	    	 
	    	 fop.close();
	     }
	     													//	same as above
	     else if(pID == "05") {
	    	 int XX, data;
	    	 
	    	 
	    	 while(counter != 178) {
	    		 try {
	    			 out.write(buffer);
	    			 a =in.read();
	    			 
	    			 if (a != 13) {
		    				str += (char)a; //Add k to String
		    			}
	    			 else if (a==13) {	
	    				String[] splitLine = null; 		    			
		    			splitLine = str.split(" ");
		    			XX = Integer.parseInt(splitLine[2], 16);
		    			data =XX - 40;
		    		//	System.out.println(data);
		    			fop.write(String.valueOf(data));
		    			fop.write("\n");
		    		    str="";
		    			counter++;  //Count num of total data 
	    		      }
	    		 }
	    		 catch(Exception e) {
	    			 ;
	    		 }
	    		 
	    		 
	    	 }
	    	 
	    	 fop.close();
	     }
	     
	     System.out.println("End vehicle");       //close file and disconnect
	     ithaki.close();
	     return;
    }

	/**
	 * Runs the necessary programs with appropriate values to create diagrams, images, audio, and diagnostics.
	 *
	 * @param args the command-line arguments
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException{
		// Set the values for Echo, Image, and Sound
		String Echo="8070";
		String Image="5050";
		String Sound="3736";

		// Run the echo() function with various parameters
		echo(false, Echo);
		echo(false,"0000");

		// Run the image() function with various parameters
		image(false , 256, "FIX", Image, Echo);
		image(false , 256, "PTZ", Image, Echo);

		// Run the echo() function again, but with different parameters
		echo(true, Echo);

		// Run the soundDPCM() function with various parameters
		soundDPCM(999, Sound, "F", Echo);
		soundDPCM(300, Sound, "T", Echo);

		// Run the soundAQDPCM() function with various parameters
		soundAQDPCM( 999, Sound, Echo);

		// Run the copterTelemetryUDP() function with the Echo value
		copterTelemetryUDP(Echo);

		// Run the vehicleOBDII() function with various parameters
		vehicleOBDII(1, Echo);
		vehicleOBDII(2, "0000");
		vehicleOBDII(3, "0000");
		vehicleOBDII(4, "0000");
		vehicleOBDII(5, "0000");
		vehicleOBDII(6, "0000");

		//creating a video
		 video(false, 1024, "FIX", "6431");
		 copterTCPtest("250");

		// create a video using the images created earlier
		// ffmpeg -start_number 1 -framerate 0.6 -i image%d.jpg -vcodec mpeg4 video.mp4
	}



}
