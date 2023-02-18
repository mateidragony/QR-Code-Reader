package Camera;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import nu.pattern.OpenCV;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;




/**
 *
 * @author 22cloteauxm
 */
public class CameraRunner{
    
    private static OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(0);    
        
    
    public static void saveImage(BufferedImage b)
    {
        try 
        {  
            File savedImage = new File("src/selfie.png");
            ImageIO.write(b, "png", savedImage);              
        } catch (IOException e) { 
            System.out.println("WRITE IMAGE FAILED!! ");
            e.printStackTrace();  
        }  
    }
    
    
    
    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException{
        
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch(Exception ex){}
    	
    	
    	OpenCV.loadLocally()
;        
        grabberCV.start();
        
		@SuppressWarnings("resource")
		Java2DFrameConverter frameToImg = new Java2DFrameConverter();
        
        CameraEngine ce = new CameraEngine();
        
        //Jframe with the actual camera images
        JFrame myFrame = new JFrame("My Frame");  
        
        myFrame.getContentPane().add(ce);
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //JFrame that displays the selfie
        JFrame selfieFrame = new JFrame("Yet another frame");
        SelfiePanel mySelfiePanel = new SelfiePanel();
        selfieFrame.getContentPane().add(mySelfiePanel);
        selfieFrame.setVisible(false);
        
        //JFrame with bunch of buttons and sliders
        JFrame sliderFrame = new JFrame("My other frame");
        
        sliderFrame.setVisible(true);
        sliderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sliderFrame.setLayout(new BoxLayout(sliderFrame.getContentPane(), BoxLayout.PAGE_AXIS));
        sliderFrame.add(new JLabel(" "));  
        
        
        //------------------------------------------------------
        //JFrame sliders for image variables
        //------------------------------------------------------
        
        JSlider threshold = new JSlider(0,16777215);
        sliderFrame.add(threshold);
        JLabel thresholdText = new JLabel("Threshold");
        sliderFrame.add(thresholdText);
        sliderFrame.add(new JLabel(" "));    

        JSlider minDistance = new JSlider(0,50);
        sliderFrame.add(minDistance);
        JLabel erodeText = new JLabel("Distance Threshold");
        sliderFrame.add(erodeText);
        sliderFrame.add(new JLabel(" "));    
        
        JSlider blockSize = new JSlider(0,6);
        sliderFrame.add(blockSize);
        JLabel blockText = new JLabel("Block Size");
        sliderFrame.add(blockText);
        sliderFrame.add(new JLabel(" "));    
        
        JSlider areaThresh = new JSlider(10000,10100);
        sliderFrame.add(areaThresh);
        JLabel areaText = new JLabel("Arae value");
        sliderFrame.add(areaText);
        sliderFrame.add(new JLabel(" "));    
        
        
        //------------------------------------------------------
        //JFrame check boxes
        //------------------------------------------------------
        
        JCheckBox mirrorBox = new JCheckBox("Mirror");
        sliderFrame.add(mirrorBox);
        sliderFrame.add(new JLabel(" "));    

        JCheckBox erodeBox = new JCheckBox("Erode/Dilate?");
        sliderFrame.add(erodeBox);
        sliderFrame.add(new JLabel(" "));   
        
        JButton selfie = new JButton("Selfie");
        selfie.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                System.out.println("Selfie has been taken");
                
                BufferedImage theSelfie;
                
                try{
                    theSelfie = frameToImg.convert(grabberCV.grab());
                }catch(FrameGrabber.Exception ex){
                    theSelfie = new BufferedImage(640,480,BufferedImage.TYPE_INT_ARGB);
                    System.out.println(ex);
                }
                
                saveImage(theSelfie);
                
                selfieFrame.setSize(theSelfie.getWidth(),theSelfie.getHeight());
                selfieFrame.getComponent(0).setSize(theSelfie.getWidth(),theSelfie.getHeight());     
                mySelfiePanel.setSelfie(theSelfie);
                mySelfiePanel.repaint();
                selfieFrame.setVisible(true);
            }
        });
        sliderFrame.add(selfie);
        
        
        sliderFrame.add(new JLabel(" "));  
        sliderFrame.add(new JLabel(" "));  
       
        
        JButton nextButt = new JButton("Scan1");
        nextButt.addActionListener(new ActionListener() {
        	
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		ce.doTheDew();
        	}
        	
        });
        sliderFrame.add(nextButt);   
        

        sliderFrame.add(new JLabel(" "));  
        sliderFrame.add(new JLabel(" "));  
        
        JButton bigManButt = new JButton("Reset");
        bigManButt.addActionListener(new ActionListener() {
        	
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		ce.codesAlreadyRead = new ArrayList<>();
        	}
        	
        });
        sliderFrame.add(bigManButt);   
        

        sliderFrame.add(new JLabel(" "));  
        sliderFrame.add(new JLabel(" "));
       
        
        Timer t = new Timer(1000/30, new ActionListener() {
        	
        	public void actionPerformed(ActionEvent e) {
        		
        		try {
	        		BufferedImage tempFrame = (frameToImg.convert(grabberCV.grab()));
	                ce.setCameraFrame(tempFrame);
	                
	                
	                ce.setThreshold(threshold.getValue());
	                ce.setMirrored(mirrorBox.isSelected());
	                ce.erode = erodeBox.isSelected();
	                ce.areaThreshold = areaThresh.getValue()/10000.0;
	                ce.minDistance = minDistance.getValue();
	                
	                erodeText.setText("Distance Threshold - " + minDistance.getValue());
	                blockText.setText("Block Size - "+ blockSize.getValue());
	                areaText.setText("Area Ratio - "+areaThresh.getValue()/10000.0);
	                thresholdText.setText("Threshold - " + threshold.getValue());
	                
	                myFrame.getComponent(0).repaint();
	                myFrame.setSize(ce.getSize());
	                myFrame.getComponent(0).setSize(ce.getSize());
	                
	                sliderFrame.setLocation(myFrame.getX()+myFrame.getWidth(),myFrame.getY());
	                sliderFrame.setSize(250,500);
        		} catch(Exception except) {}
        	}
        });
        
        t.start();
        
        /*
        while(true){
            BufferedImage tempFrame = (frameToImg.convert(grabberCV.grab()));
            ce.setCameraFrame(tempFrame);
            
            
            ce.setThreshold(threshold.getValue());
            ce.setMirrored(mirrorBox.isSelected());
            ce.erode = erodeBox.isSelected();
            ce.areaThreshold = areaThresh.getValue()/10000.0;
            ce.minDistance = minDistance.getValue();
            
            erodeText.setText("Distance Threshold - " + minDistance.getValue());
            blockText.setText("Block Size - "+ blockSize.getValue());
            areaText.setText("Area Ratio - "+areaThresh.getValue()/10000.0);
            thresholdText.setText("Threshold - " + threshold.getValue());
            
            myFrame.getComponent(0).repaint();
            myFrame.setSize(ce.getSize());
            myFrame.getComponent(0).setSize(ce.getSize());
            
            sliderFrame.setLocation(myFrame.getX()+myFrame.getWidth(),myFrame.getY());
            sliderFrame.setSize(250,500);
            
            Thread.sleep(30);
        }
        */
    }
}

