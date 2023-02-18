package Camera;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import nu.pattern.OpenCV;

public class CameraGame extends JPanel{
	
	
	
	private static BufferedImage cameraFrame;
    private static OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(0);    
	
	public CameraGame() {
		
		OpenCV.loadLocally();
		
		this.setPreferredSize(new Dimension(1200,700));
	}
	
	public void paintComponent(Graphics g) {

		Polygon qr = findRectangleV5(cameraFrame);
		
		g.setColor(Color.red);
		
		
		Point2D pt = centerPoint(qr);
		
		g.fillRect(1200-(int)pt.getX()*2,(int)pt.getY(),30,30);
		
		
	}
	
	
	
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

    	List<Point> myPoints = approximations.get(0).toList();
    	
    	Polygon qr = new Polygon();
    	
    	for(Point p: myPoints) {
    		qr.addPoint((int)p.x,(int)p.y);
    	}
    	
    	return qr;

    }
	public Mat bufferedImage2Mat(BufferedImage image) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	try {
    		ImageIO.write(image, "png", baos);
    		return Imgcodecs.imdecode(new MatOfByte(baos.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    	} catch(Exception e) {System.out.print(e);}
    	return null;
    }
	public Point2D centerPoint(Polygon p) {
		
		int minX = 9999; int minY = 9999; 
		int maxX = 0; int maxY = 0;
		
		for(int xpt: p.xpoints) {
			if(xpt>maxX)
				maxX = xpt;
			if(xpt<minX)
				minX = xpt;
		}
		
		for(int ypt: p.ypoints) {
			if(ypt>maxY)
				maxY = ypt;
			if(ypt<minY)
				minY = ypt;
		}
		
		return new Point2D.Double((maxX+minX)/2, (maxY+minY)/2);
		
		
	}
	
	
	
	
	public static void main(String[] args) throws FrameGrabber.Exception{
		
		try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch(Exception ex){}
		
	
		grabberCV.start();
        
		@SuppressWarnings("resource")
		Java2DFrameConverter frameToImg = new Java2DFrameConverter();
		
		
		JFrame myFrame = new JFrame("Game!");
		CameraGame gamePanel = new CameraGame();
		
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.setVisible(true);
		myFrame.getContentPane().add(gamePanel);

		Timer t = new Timer(1000/30, new ActionListener() {
	
			public void actionPerformed(ActionEvent e) {
				
				
				try {
					cameraFrame = (frameToImg.convert(grabberCV.grab()));
				} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
					e1.printStackTrace();
				}
				
				
				myFrame.setSize(gamePanel.getPreferredSize());
				myFrame.getComponent(0).repaint();
			}
			
		});	
		
		t.start();
		
		
	}

}
