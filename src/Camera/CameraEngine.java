package Camera;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import nu.pattern.OpenCV;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;





/**
 *
 * @author 22cloteauxm
 */
public class CameraEngine extends JPanel{

	private BufferedImage cameraFrame;
    
	private final Rectangle qrBounds =  new Rectangle(75,50,490,320);
	
    private final int erodeRange = 1;
    private int colorThreshold;
    public double qualityLevel = 0.01;
    public double minDistance = 25;
    public int gradientSize = 3;
    public boolean useHarrisDetector = false;
    public boolean erode = false;
    public double areaThreshold = 1.5;

    private ArrayList<Point2D> intersectionPoints;
    
    private boolean mirrored = false;

    public final int width = 640;
    public final int height = 480;
    
    
    
    public boolean scanning = false;
    private ArrayList<String> codesDataBase;
    public ArrayList<String> validCodesRead = new ArrayList<>();
    public ArrayList<String> codesAlreadyRead = new ArrayList<>();
    private final QREngine qrEngine = new QREngine();
    
    private Image iWillDraw;
    private Polygon qrPolygon = new Polygon(), oldPolygon = new Polygon();
    private double qrArea;
    
    
    private final ArrayList<String> bitlyLinks = new ArrayList<>();

    public CameraEngine() {
        OpenCV.loadLocally();
        
        bitlyLinks.add("3BhLLgB");

        initDataBase();
        new MateiPolygon();
    }
    

    private void initDataBase(){
        codesDataBase = new ArrayList<>();
        codesDataBase.add("Matei C");
        codesDataBase.add("Ahdum L");
        codesDataBase.add("Naren H");
        codesDataBase.add("Ngaujal");
        codesDataBase.add("Bryce H");
        codesDataBase.add("Pranav");
    }

    public void setCameraFrame(BufferedImage c){cameraFrame = c;}
    public void setThreshold(int c){colorThreshold = c;}
    public void setMirrored(boolean c){mirrored = c;}
    
    public Dimension getSize(){return new Dimension(width,height);}
        
    
    
    public void paintComponent(Graphics g) {		

        qrPolygon = findRectangleV5(cameraFrame);
        
        if(mirrored){
            g.drawImage(iWillDraw, width,0,-width,height, this);
        } else {
            g.drawImage(iWillDraw, 0,0,width,height, this);
        }

        
        Graphics2D g2d = (Graphics2D)g;
        ArrayList<Line2D.Double> gridLines = this.drawQRGrid(qrPolygon, g2d);
        intersectionPoints = findIntersectionPoints(gridLines);
        

       	doTheDew2(qrEngine);
        
        if(validCodesRead.size() >= 5) {
        	scanning = false;
            String finalCode = mostCommonString(validCodesRead);
        	codesAlreadyRead.add(finalCode);
        	
        	validCodesRead = new ArrayList<>();

        	Clip audio = AudioUtility.loadClip("QR Code Reader/src/Camera/Audio/"+ finalCode.trim()+".wav");
        	
        	if(audio!= null) {
        		audio.start();
        	}
        	
        	if(bitlyLinks.contains(finalCode)) {
        		try {
        			Runtime.getRuntime().exec(new String[]{"cmd", "/c","start chrome http://bit.ly/"+ finalCode});
        		} catch (IOException ex) {ex.printStackTrace();}
        	}
        	
        	
        	JOptionPane.showMessageDialog(null, finalCode);
        }
        
        drawCoolThing2(cameraFrame, intersectionPoints, g2d);
    }    
    
    
    //------------------------------------------
    //Methods which will do the dew
    //------------------------------------------
    public void doTheDew(){
    	JFrame myFrame = new JFrame("Look! I found this qr code!");
    	
    	QREngine qr = new QREngine();
        qr.setPreferredSize(new Dimension(700,700));
        
        myFrame.setVisible(true);
        myFrame.getContentPane().add(qr);
        
        myFrame.setSize(qr.getPreferredSize());
        
        BufferedImage drawnQR = drawQRCode(cameraFrame,intersectionPoints);
        drawnQR = qr.rotateUntilGood(drawnQR, 3);
 
        
        File errorFile = new File("src/error.png");
        BufferedImage errorImg = null;
        try {
        	errorImg = ImageIO.read(errorFile);
        } catch (IOException ex) {ex.printStackTrace();}
        
        
        if(drawnQR == null) {
        	qr.setMyImage(errorImg);
        	myFrame.setVisible(false);
        	JOptionPane.showMessageDialog(null, """
                    You're actually such an idiot. How do you expect me to read this qr code.
                     Do you not know how to hold something straight? Actual Idiot.\s
                     Get tf off this program""", "You're actually dumb", JOptionPane.WARNING_MESSAGE);
        }
        else
        	qr.setMyImage(drawnQR);
        
        myFrame.getContentPane().getComponent(0).repaint();
        
        myFrame.addMouseListener(new MouseAdapter() {
			
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		System.out.println((qr.readQR(qr.getImage(),3)));
        	}

        });
    }
    public void doTheDew2(QREngine qr) {
    	try {
	    	BufferedImage drawnQR = drawQRCode(cameraFrame,intersectionPoints);
	        drawnQR = qr.rotateUntilGood(drawnQR, 3);
        
      
	        if(drawnQR != null) {

	        	qr.setMyImage(drawnQR);
	        	
	        	String readWord = qr.readQR(qr.getImage(),3);
	        	
	        	if(readWord != null && !codesAlreadyRead.contains(readWord) && codesDataBase.contains(readWord))
	        		validCodesRead.add(readWord);
	        }
        } catch(NullPointerException | ArrayIndexOutOfBoundsException ex) {
        	System.out.println("Something went a tad bit wrong... Hopefully program still works lol");
        }
    		
    }
    public void drawCoolThing2(BufferedImage src, ArrayList<Point2D> intersections, Graphics2D g) {
    	
    	int xCount = 0;
    	int yCount = 0;
    	for(Point2D pt : intersections) {
    		if(pt!= null && pt.getX() < src.getWidth() && pt.getY() < src.getHeight()
    				&& isDark(src.getRGB((int)pt.getX(),(int)pt.getY()))) {

    			g.setColor(Color.BLACK);

            } else {

    			g.setColor(Color.WHITE);
            }
            g.fillRect(20+xCount*3,20+yCount*3,3,3);

            xCount++;
    		
    		if(xCount > 13) {
    			xCount = 0;
    			yCount ++;
    			
    		}
    		
    		
    		if(yCount > 13) {
    			yCount = 0;
    			
    		}
    		
    	}
    	
    }
    
    public String mostCommonString(ArrayList<String> strings) {
    	String bestString = "";
    	int bestFreq = 0;
    	
    	for(String s: strings) {
    		if(Collections.frequency(strings, s) > bestFreq)
    			bestString = s;
    	}
    	
    	return bestString;
    }
    
    
    //------------------------------------------
    //Methods to edit the camera footage
    //------------------------------------------
    public BufferedImage makeSmaller(BufferedImage oldImage){
        BufferedImage smaller = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_ARGB);
        
        for(int x=0; x<oldImage.getWidth(); x+=2){
            for(int y=0; y<oldImage.getHeight(); y+=2){
                smaller.setRGB(x/2, y/2, oldImage.getRGB(x, y));
            }
        }
        
        return smaller;
    }    
    public BufferedImage noColor(BufferedImage oldImage){
        BufferedImage recolored = 
                new BufferedImage(oldImage.getWidth(),oldImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        
        for(int x=0; x<oldImage.getWidth(); x++){
            for(int y=0; y<oldImage.getHeight(); y++){
                if((oldImage.getRGB(x, y) & 0x00FFFFFF) > colorThreshold)
                    recolored.setRGB(x, y, 0xFFFFFFFF);
                else
                    recolored.setRGB(x, y, 0xFF000000);
            }
        }
        
        return recolored;
    }
    public BufferedImage grayScale(BufferedImage img) {
    	BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    	
    	for (int x = 0; x < img.getWidth(); ++x)
    	    for (int y = 0; y < img.getHeight(); ++y)
    	    {
    	        int rgb = img.getRGB(x, y);
    	        int r = (rgb >> 16) & 0xFF;
    	        int g = (rgb >> 8) & 0xFF;
    	        int b = (rgb & 0xFF);
    	        

    	        // Normalize and gamma correct:
    	        float rr = (float)Math.pow(r / 255.0, 2.2);
    	        float gg = (float)Math.pow(g / 255.0, 2.2);
    	        float bb = (float)Math.pow(b / 255.0, 2.2);

    	        
    	        // Calculate luminance:
    	        float lum = (float)(0.2126 * rr + 0.7152 * gg + 0.0722 * bb);

    	        // Gamma compand and rescale to byte range:
    	        int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
    	        int graynum = (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
    	        gray.setRGB(x, y, graynum);
    	    }
    	
    	return gray;
    }
    public BufferedImage erode(BufferedImage oldImage){
        int range = erodeRange;
        BufferedImage erodedImage = 
                new BufferedImage(oldImage.getWidth(),oldImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        
        for(int x=0; x<oldImage.getWidth(); x++){
            for(int y=0; y<oldImage.getHeight(); y++){
                
                boolean surroundedByBlack = true;
                
                for(int i = x-range; i<=x+range; i++){
                    for(int j = y-range; j<=y+range; j++){
                        if(i>0 && i<oldImage.getWidth()
                                && j>0 && j<oldImage.getHeight()
                                && i!= x && j!=y){
                            if(oldImage.getRGB(i,j) != 0xFF000000)
                                surroundedByBlack = false;
                        }
                    }
                }
                
                if(surroundedByBlack)
                    erodedImage.setRGB(x, y, 0xFF000000);
                else
                    erodedImage.setRGB(x, y, 0xFFFFFFFF);
                
            }
        }
        return erodedImage;
    }
    public BufferedImage dilate(BufferedImage oldImage){
        int range = erodeRange;
        
        BufferedImage dilatedImage = 
                new BufferedImage(oldImage.getWidth(),oldImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
        
        for(int x=0; x<oldImage.getWidth(); x++){
            for(int y=0; y<oldImage.getHeight(); y++){
                
                boolean bordersBlack = false;
                
                for(int i = x-range; i<=x+range; i++){
                    for(int j = y-range; j<=y+range; j++){
                        if(i>0 && i<oldImage.getWidth()
                                && j>0 && j<oldImage.getHeight()){
                            if(oldImage.getRGB(i,j) == 0xFF000000){
                                bordersBlack = true;
                                break;
                            }
                        }
                    }
                    if(bordersBlack)
                        break;
                }
                
                if(bordersBlack)
                    dilatedImage.setRGB(x, y, 0xFF000000);
                else
                    dilatedImage.setRGB(x, y, 0xFFFFFFFF);
                
            }
        }
        
        return dilatedImage;
    }
    public BufferedImage onlyLines(BufferedImage oldImage) {
    	
    	int range = erodeRange;
    	
    	BufferedImage newImage = new BufferedImage(oldImage.getWidth(),oldImage.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
    	
    	for(int x=0; x<newImage.getWidth(); x++) {
    		for(int y=0; y<newImage.getHeight(); y++){
    			
    			
    			boolean bordersSomethingElse = false;
                
                for(int i = x-range; i<=x+range; i++){
                    for(int j = y-range; j<=y+range; j++){
                        if(i>0 && i<oldImage.getWidth()
                                && j>0 && j<oldImage.getHeight()){
                            if(oldImage.getRGB(i,j) != oldImage.getRGB(x,y)){
                                bordersSomethingElse = true;
                                break;
                            }
                        }
                    }
                    if(bordersSomethingElse)
                        break;
                }
                
                if(bordersSomethingElse)
                    newImage.setRGB(x, y, 0xFF000000);
                else
                    newImage.setRGB(x, y, 0xFFFFFFFF);
    			
    			
    		}
    	}
    	
    	return newImage;
    }
    
    
    
    //------------------------------------------
    //Methods are broken 
    //------------------------------------------
    /*
    public boolean[][] detectCorners(BufferedImage oldImage){
        
        boolean[][] corners = new boolean[oldImage.getWidth()][oldImage.getHeight()];
        
        Size size = new Size(oldImage.getWidth(),oldImage.getHeight());
        
        Mat initialImage = new Mat(size, CvType.CV_32F);
                
        for (int r = 0; r < oldImage.getWidth(); r++) {
            for (int c = 0; c < oldImage.getHeight(); c++) {               
                initialImage.put(r, c, (int)oldImage.getRGB(r, c));
            }
        }        
        
        Mat harrisScene = Mat.zeros(initialImage.size(), CvType.CV_32F);
                
        Imgproc.cornerHarris(initialImage, harrisScene, blockSize, apertureSize, k);
        
        for( int j = 0; j < harrisScene.rows() ; j++){
            for( int i = 0; i < harrisScene.cols(); i++){
                if ((int) harrisScene.get(j,i)[0] > 180){
                    corners[i][j] = true;
                }
            }
        }
        
        return corners;
    }
    */
    public int[] betterCorners(BufferedImage oldImage) {
    	    	
    	Size size = new Size(oldImage.getWidth(),oldImage.getHeight());
        
        Mat initialImage = new Mat(size, CvType.CV_32F);      
        
        
        for (int r = 0; r < oldImage.getWidth(); r++) {
            for (int c = 0; c < oldImage.getHeight(); c++) {               
                initialImage.put(c, r, (int)oldImage.getRGB(r, c));
            }
        }     
        
        Mat copy = new Mat();		
        initialImage.copyTo(copy);
        
        MatOfPoint corners = new MatOfPoint();


        double k = 0.236;
        int blockSize = 3;
        Imgproc.goodFeaturesToTrack(initialImage, corners, 70, qualityLevel, minDistance, new Mat(),
                blockSize, gradientSize, useHarrisDetector, k);
        
        
        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        
        corners.get(0, 0, cornersData);
        
        
        
        int radius = 4;
        
        for (int i = 0; i < corners.rows(); i++) {
            Imgproc.circle(copy, new Point(cornersData[i * 2], cornersData[i * 2 + 1]), radius,
                    new Scalar(0,0,0), 1);
        }

        copy.convertTo(copy, CvType.CV_8S);
        
        iWillDraw = HighGui.toBufferedImage(copy);
        
        
        return cornersData;
    }
   

    //------------------------------------------
    //Methods to find Rectangle 
    //------------------------------------------
    
    //Uses Side Lengths to find the QR Square
    public Polygon findRectangleV1(ArrayList<Polygon> polys){

    	//ArrayList<Polygon> rects =  new ArrayList<>();
    	
    	Polygon bestPoly = new Polygon();
    	double bestRatio = 99999;
    	
    	for(Polygon rect: polys) {
			
            int[] x = rect.xpoints;
            int x1 = x[0]; int x2 = x[1]; int x3 = x[2]; int x4 = x[3];
            
            int[] y = rect.ypoints;
            int y1 = y[0]; int y2 = y[1]; int y3 = y[2]; int y4 = y[3];       
            
            double tempAvgDist = avgDistance(x1,y1,x2,y2,x3,y3,x4,y4);

            double ratio1 = dist(x1,y1,x2,y2)/tempAvgDist;
            double ratio2 = dist(x2,y2,x3,y3)/tempAvgDist;
            double ratio3 = dist(x3,y3,x4,y4)/tempAvgDist;
            double ratio4 = dist(x1,y1,x4,y4)/tempAvgDist;


            if(ratio1 < 0) ratio1 = 1/ratio1;
            if(ratio2 < 0) ratio2 = 1/ratio2;
            if(ratio3 < 0) ratio3 = 1/ratio3;
            if(ratio4 < 0) ratio4 = 1/ratio4;

            double avgRatio = (ratio1+ratio2+ratio3+ratio4) / 4;

            Line2D side1 = new Line2D.Double(x1,y1,x2,y2);
            Line2D side2 = new Line2D.Double(x2,y2,x3,y3);
            Line2D side3 = new Line2D.Double(x3,y3,x4,y4);
            Line2D side4 = new Line2D.Double(x1,y1,x4,y4);

            if(avgRatio < bestRatio
            		&& !side1.intersectsLine(side3)
            		&& !side2.intersectsLine(side4)) {
            	bestPoly = new Polygon(new int[] {x1,x2,x3,x4}, 
            			new int[] {y1,y2,y3,y4}, 4);
            	bestRatio = avgRatio;
            		
            }
            
        }
    	
    	
    	return bestPoly;
    	
    }
    
    //Uses Area to try and find the QR Square
    public ArrayList<Polygon> findRectangleV2(int[] cornersDataInitial) {
    	
    	ArrayList<Polygon> bestPolys = new ArrayList<>();
    	
    	Integer[] cornersData = noOutlyingPoints(cornersDataInitial);
    	
    	for(int i1 = 0; i1 < cornersData.length/2 - 3; i1++) {
            int x1 = cornersData[i1 * 2];
            int y1 = cornersData[i1 * 2 + 1];
            
            for(int i2 = i1+1; i2< cornersData.length/2 - 2; i2++) {
            	int x2 = cornersData[i2 * 2];
            	int y2 = cornersData[i2 * 2 + 1];
            	
            	for(int i3 = i2+1; i3< cornersData.length/2 - 1; i3++) {
            		int x3 = cornersData[i3 * 2];
            		int y3 = cornersData[i3 * 2 + 1];
            		
            		for(int i4 = i3+1; i4<cornersData.length/2; i4++){
            			int x4 = cornersData[i4 * 2];
            			int y4 = cornersData[i4 * 2 + 1];
            			
            			
            			Line2D side1 = new Line2D.Double(x1,y1,x2,y2);
            			Line2D side2 = new Line2D.Double(x2,y2,x3,y3);
            			Line2D side3 = new Line2D.Double(x3,y3,x4,y4);
            			Line2D side4 = new Line2D.Double(x1,y1,x4,y4);
            			
            			double AvgAreaSq = (Math.pow(dist(x1,y1,x2,y2), 2)
            					            + Math.pow(dist(x2,y2,x3,y3), 2)
            					            + Math.pow(dist(x3,y3,x4,y4), 2)
            					            + Math.pow(dist(x1,y1,x4,y4), 2)) / 4;
            			double AvgAreaMult = (dist(x1,y1,x2,y2)*dist(x2,y2,x3,y3)
            								  + dist(x2,y2,x3,y3)*dist(x3,y3,x4,y4)
            								  + dist(x3,y3,x4,y4)*dist(x1,y1,x4,y4)
            								  + dist(x1,y1,x2,y2)*dist(x1,y1,x4,y4)) / 4;
            			
      
            			
            			double avgRatio = AvgAreaSq/AvgAreaMult;
            			if(avgRatio < 1)
            				avgRatio = 1/avgRatio;
            			
            			
            			if(avgRatio < areaThreshold
            					&& !side1.intersectsLine(side3)
            					&& !side2.intersectsLine(side4)) {
	            			bestPolys.add( new Polygon(new int[] {x1*2,x2*2,x3*2,x4*2}, 
	            					new int[] {y1*2,y2*2,y3*2,y4*2}, 4)  );
            			}
            		}
            	}
            }
            
        }
    	
    	
    	return bestPolys;
    	
    }

    //This finds the contours and draws them on the image
    public Image findRectangleV3(BufferedImage oldImage) {

        Mat srcGray = new Mat();
        int threshold = 100;
        
        Mat src = bufferedImage2Mat(oldImage);

        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new Size(3, 3));

        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        
        
        Rect rect = null;
    	double maxArea = 300;
    	ArrayList<Rect> arr = new ArrayList<Rect>();
    	for (int i = 0; i < contours.size(); i++) {
    		Mat contour = contours.get(i);
    		double contourArea = Imgproc.contourArea(contour);
    		if (contourArea > maxArea) {
    			rect = Imgproc.boundingRect(contours.get(i));
    			arr.add(rect);
    		}
    	}
    	
        
        
        
        Mat drawing = new Mat();
        
        src.copyTo(drawing);
        
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0,0,255);
            Imgproc.drawContours(drawing, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }
        
        return HighGui.toBufferedImage(drawing);        
    }
    
    //Use Angles to try and find the QR Square
    public Polygon findRectangleV4(ArrayList<Polygon> rects) {
    	
    	Polygon bestPoly = new Polygon();
    	double bestRatio = 0;
    	
    	for(Polygon rect: rects) {
            			
            int[] x = rect.xpoints;
            int x1 = x[0]; int x2 = x[1]; int x3 = x[2]; int x4 = x[3];
            
            int[] y = rect.ypoints;
            int y1 = y[0]; int y2 = y[1]; int y3 = y[2]; int y4 = y[3];
    		
    		Line2D side1 = new Line2D.Double(x1,y1,x2,y2);
	        Line2D side2 = new Line2D.Double(x2,y2,x3,y3);
	        Line2D side3 = new Line2D.Double(x3,y3,x4,y4);
	        Line2D side4 = new Line2D.Double(x1,y1,x4,y4);

	        double avgAngle = (angleBetween2Lines(side1,side2)
	        				  + angleBetween2Lines(side2,side3)
	        				  + angleBetween2Lines(side3,side4)
	        				  + angleBetween2Lines(side4,side1))/4;
	          		
	        if(avgAngle < 0) {
	        	avgAngle = Math.abs(avgAngle);
	        }
	        
	        if(avgAngle > (Math.PI/2)) {
	        	avgAngle = Math.PI - avgAngle;
	        }
	          			
	        if(avgAngle/(Math.PI/2) > bestRatio
	       			&& !side1.intersectsLine(side3)
	       			&& !side2.intersectsLine(side4)) {
		   			
	        	bestPoly = new Polygon(new int[] {x1,x2,x3,x4}, 
		   					new int[] {y1,y2,y3,y4}, 4);
		          			bestRatio = avgAngle/(Math.PI/2);
            			
            		
            }
        }
            
        
    	
    	
    	return bestPoly;
    	
    }
    
    //This finds the contours and finds the best looking rectangles
    public Polygon findRectangleV5(BufferedImage oldImage) {
    	Mat srcGray = new Mat();
        int threshold = 100;
        
        Mat src = bufferedImage2Mat(oldImage);

        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new Size(3, 3));

        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        
        
        
        
    	double biggestArea = 0;
    	int bestIndex = 0;
    	
    	
    	//This loop finds the contour with greatest area
    	//That will be the qr code
    	for (int i = 0; i < contours.size(); i++) {
    		
    		//converts MatOfPoint to MatOfPoint2f
    		MatOfPoint2f contour = new MatOfPoint2f();
    		contours.get(i).convertTo(contour, CvType.CV_32FC2);
    		
    		//finds area
    		double contourArea = Imgproc.contourArea(contour);
    		
    		
    		if (contourArea > biggestArea) {
    			biggestArea = contourArea;
    			bestIndex = i;
    		}
    	}

        qrArea = biggestArea;

    	
    	//Mats of points
    	MatOfPoint2f contour = new MatOfPoint2f();
    	MatOfPoint2f approx = new MatOfPoint2f();
		MatOfPoint approx1f = new MatOfPoint();
    
    	contours.get(bestIndex).convertTo(contour, CvType.CV_32FC2);
    	double epsilon = .04 * Imgproc.arcLength(contour, true);
		Imgproc.approxPolyDP(contour,approx,epsilon,true);
		approx.convertTo(approx1f, CvType.CV_32S);
    	
		
		ArrayList<MatOfPoint> approximations = new ArrayList<>();
		approximations.add(approx1f);
    	
    	
    	Mat drawing = new Mat();
    	src.copyTo(drawing);

       	Imgproc.drawContours(drawing,approximations,0, new Scalar(0,255,0),3);

    	
    	iWillDraw = HighGui.toBufferedImage(drawing);

    	List<Point> myPoints = approximations.get(0).toList();
    	
    	Polygon qr = new Polygon();
    	
    	for(Point p: myPoints) {
    		qr.addPoint((int)p.x,(int)p.y);
    	}
    	
    	return qr;

    }
    
    
    //------------------------------------------
    //Methods to draw the lines of the QR Rect
    //------------------------------------------
    public ArrayList<Line2D.Double> drawQRGrid(Polygon qr, Graphics2D g) {
    	
    	ArrayList<Line2D.Double> gridLines = new ArrayList<>();
    	
    	int[] x = qr.xpoints;
    	int[] y = qr.ypoints;
    	
    	double deltaY1 = y[1]-y[0]; double deltaX1 = x[1]-x[0];
    	if(deltaX1 == 0)
    		deltaX1 = .01;
    	
    	double deltaY2 = y[2]-y[1]; double deltaX2 = x[2]-x[1];
    	if(deltaX1 == 0)
    		deltaX1 = .01;
    	
    	double deltaY3 = y[3]-y[2]; double deltaX3 = x[3]-x[2];
    	if(deltaX1 == 0)
    		deltaX1 = .01;
    	
    	double deltaY4 = y[0]-y[3]; double deltaX4 = x[0]-x[3];
    	if(deltaX1 == 0)
    		deltaX1 = .01;
    	
	
//    	double length1 = dist(x[0],y[0],x[1],y[1]);
//    	double length2 = dist(x[1],y[1],x[2],y[2]);
//    	double length3 = dist(x[2],y[2],x[3],y[3]);
//    	double length4 = dist(x[3],y[3],x[0],y[0]);
    	
    	
    	//Adding the 14 row lines
    	for(double i=0.5; i<14.5; i++) {
    		
    		double xPos1 = x[0]+(i*deltaX1)/14;
    		double yPos1 = y[0]+(i*deltaY1)/14;
    		double xPos2 = x[3]-(i*deltaX3)/14;
    		double yPos2 = y[3]-(i*deltaY3)/14;
    		
    		gridLines.add(new Line2D.Double(xPos1,yPos1,xPos2,yPos2));
    		
    	}
    	
    	//Adding the 14 column lines
    	for(double i=0.5; i<14.5; i++) {
    		
    		double xPos3 = x[0]-(i*deltaX4)/14;
    		double yPos3 = y[0]-(i*deltaY4)/14;
    		double xPos4 = x[1]+(i*deltaX2)/14;
    		double yPos4 = y[1]+(i*deltaY2)/14;
    		
    		gridLines.add(new Line2D.Double(xPos3,yPos3,xPos4,yPos4));
    	}

    	
    	
    	g.setColor(Color.RED);
    	
    	for(Line2D.Double line : gridLines) {
    		g.draw(line);
    	}
    	
    	
    	return gridLines;
    }
    
    
    //------------------------------------------
    //Helper Methods for rectangle finding
    //------------------------------------------
    public ArrayList<Point2D> findIntersectionPoints(ArrayList<Line2D.Double> lines){
    	
    	ArrayList<Point2D> intersections = new ArrayList<>();
    	
    	for(int i=0; i<lines.size()-1; i++) {
    		
    		for(int j=i+1; j<lines.size(); j++) {
    			
    			if(lines.get(i).intersectsLine(lines.get(j))){
    				
    				Point2D intersection = intersection(lines.get(i),lines.get(j));
    				intersections.add(intersection);
    			}
    			
    		}
    		
    	}
    	
    	return intersections;
    }
    public Point2D intersection(Line2D a, Line2D b) {
        double x1 = a.getX1(), y1 = a.getY1(), x2 = a.getX2(), y2 = a.getY2(), x3 = b.getX1(), y3 = b.getY1(),
                x4 = b.getX2(), y4 = b.getY2();
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) {
            return null;
        }

        double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

        return new Point2D.Double(xi, yi);
    }
    public BufferedImage drawQRCode(BufferedImage src, ArrayList<Point2D> intersectionPts) {
    	
    	int packWidth = 3;
    	BufferedImage qrImg = new BufferedImage(8+packWidth*2,8+packWidth*2,BufferedImage.TYPE_INT_ARGB);

    	int xCount = 0;
    	int yCount = 0;
    	for(Point2D pt : intersectionPts) {
    		if(pt!= null && pt.getX() < src.getWidth() && pt.getY() < src.getHeight() 
    				&& isDark(src.getRGB((int)pt.getX(),(int)pt.getY()))) {
    			qrImg.setRGB(xCount,yCount,0xFF000000);
    		} else {
    			qrImg.setRGB(xCount,yCount,0xFFFFFFFF);
    		}
    		
    		xCount++;
    		
    		if(xCount > 13) {
    			xCount = 0;
    			yCount ++;
    			
    		}
    		
    		
    		if(yCount > 13) {
    			yCount = 0;
    			
    		}
    		
    	}
    	
    	
    	return qrImg;
    }
    public boolean isDark(int rgb) {
    	Color color = new Color(rgb);
    	
    	return color.getRed() <= 100 && color.getBlue() <= 100 && color.getGreen() <= 100; 
    }
    
    
   
    public double dist(int x1, int y1, int x2, int y2) {
    	return Math.pow(   Math.pow(x2-x1, 2) + Math.pow(y2-y1,2)   , 0.5);
    }
    public double avgDistance(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
    	double dist1to2 = Math.pow(   Math.pow(x2-x1, 2) + Math.pow(y2-y1,2)   , 0.5);
    	double dist1to4 = Math.pow(   Math.pow(x4-x1, 2) + Math.pow(y4-y1,2)   , 0.5);
    	double dist2to3 = Math.pow(   Math.pow(x3-x2, 2) + Math.pow(y3-y2,2)   , 0.5);
    	double dist3to4 = Math.pow(   Math.pow(x4-x3, 2) + Math.pow(y4-y3,2)   , 0.5);
    	
    	return (dist1to2+dist1to4+dist2to3+dist3to4)/4;
    }
    public double angleBetween2Lines(Line2D line1, Line2D line2) {
        double angle1 = Math.atan2(line1.getY1() - line1.getY2(),
                                   line1.getX1() - line1.getX2());
        double angle2 = Math.atan2(line2.getY1() - line2.getY2(),
                                   line2.getX1() - line2.getX2());
        return Math.abs(angle1) - Math.abs(angle2);
    }
    public Integer[] noOutlyingPoints(int[] cornersData) {
    	
    	ArrayList<Integer> pts = new ArrayList<>();
    	
    	for(int i1 = 0; i1 < cornersData.length/2 - 3; i1++) {
            int x1 = cornersData[i1 * 2];
            int y1 = cornersData[i1 * 2 + 1];
            
            if(qrBounds.contains(x1*2,y1*2)) {
            	pts.add(x1);
            	pts.add(y1);
            }
    	}
    	
    	Integer[] ptsArray = new Integer[pts.size()];
    	
    	return pts.toArray(ptsArray);
    	
    }
    public Mat bufferedImage2Mat(BufferedImage image) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	try {
    		ImageIO.write(image, "png", baos);
    		return Imgcodecs.imdecode(new MatOfByte(baos.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    	} catch(Exception e) {System.out.print(e);}
    	return null;
    }
    
    //------------------------------------------
    //Old Methods. Dont work anymore
    //------------------------------------------
    public ArrayList<Point> onlyPoints(boolean[][] points){
        ArrayList<Point> truePoints = new ArrayList<>();
        
        for(int x=0; x<points.length; x++){
            for(int y=0; y<points[0].length; y++){
                if(points[x][y])
                    truePoints.add(new Point(x,y));
            }
        }
        
        return truePoints;
    }
    public ArrayList<Point> noClosePoints(ArrayList<Point> oldPoints){
        ArrayList<Point> newPoints = new ArrayList<>();
        
        for(int i=0; i<oldPoints.size()-1; i++){
            
            boolean isClose = false;
            
            Point p1 = oldPoints.get(i);
            
            for(int j=i+1;j<oldPoints.size();j++){
                //Point p2 = oldPoints.get(j);
                
                //if(Point < distThreshold)
                    isClose = true;

            }
            
            if(!isClose)
                newPoints.add(p1);
            
        }
        
        return newPoints;
    }
    public BufferedImage cool(BufferedImage oldImage){
        BufferedImage newImage = new BufferedImage(oldImage.getWidth(),oldImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
        
        for(int x=0; x<newImage.getWidth();x++){
            for(int y=0; y<newImage.getHeight(); y++){
                Color c = new Color(oldImage.getRGB(x,y),true);
                
                Color newC = new Color(255-c.getRed(),255-c.getBlue(), 255-c.getGreen());
                
                newImage.setRGB(x,y, newC.getRGB());
            }
        }
        
        return newImage;
    }


    private class MateiPolygon extends JPanel {

        private static final int width = 640, height = 480;
        private double newArea, oldArea;

        public MateiPolygon(){
            this.setPreferredSize(new Dimension(width,height));

            JFrame myFrame = new JFrame("Look! A polygon!");
            myFrame.setLocation(100,50);
            myFrame.add(this);
            myFrame.setSize(this.getPreferredSize());
            myFrame.setResizable(false);
            myFrame.setVisible(true);
            myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Timer t = new Timer(1000/60, e -> myFrame.getComponent(0).repaint());
            t.start();
        }

        @Override
        public void paintComponent(Graphics g){
            Polygon drawnPoly = decideDrawnPoly(qrPolygon);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(Color.RED);
            g2d.fill(drawnPoly);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Serif", Font.PLAIN, 20));
            g2d.drawString("Give me all your JMoney!", 30,30);

        }

        public Polygon decideDrawnPoly(Polygon newQR){
            Polygon returnedPoly = oldPolygon;

            newArea = qrArea;

            //first check the area is big enough
            //This remove wacky looking shapes
            if(newArea > 1000) {

                //Then checks if I actually read a qr code
                //if(isValidQRCode) {
                    returnedPoly = newQR;
                    oldPolygon = newQR;
                //}
            }

            return returnedPoly;
        }
    }


}
