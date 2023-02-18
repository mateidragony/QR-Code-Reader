package Camera;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 *
 * @author 22cloteauxm
 */
public class QREngine extends JPanel{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Character> bitlyChars = initAlphabet();
    	
    static String qrWord = "3BhLLgB";
    
    private BufferedImage myImage;
    
    public void setMyImage(BufferedImage c) {myImage = c;}
    public BufferedImage getImage() {return myImage;}
    
    public void paintComponent(Graphics g){    	
    	g.drawImage(myImage, 90,90, 390,390, this);
    	
    	/*
    	g.setColor(Color.RED);
    	
    	for(int i=0;i<14; i++) {
    		g.drawLine(90+i*35, 90, 90+i*35, 490+90);
    		g.drawLine(90,90+i*35,490+90, 90+i*35);
    	}
    	
    	g.setColor(Color.BLUE);
    	g.setFont(new Font("Sans Serif",Font.PLAIN, 20));
    	g.drawString("O",90+4*35+10,90+4*35+30);
    	g.drawString("O",90+4*35+10,90+8*35+30);
    	g.drawString("O",90+8*35+10,90+8*35+30);
    	g.drawString("O",90+8*35+10,90+4*35+30);
    
    	for(int i=0; i<7; i++) {
    		g.drawString("C",100+35*3+35*i,470);
    	}
    	
    	g.setColor(Color.BLACK);
    	for(int i=0; i<14; i++) {
    		g.drawString(""+i, 95+i*35,80);
    		g.drawString(""+i, 60,115+i*35);
    	}
    	*/
    	
    
    }
    
    
    private int bitsOn(ArrayList<Boolean> bits, int start, int end) {
    	
    	int count = 0;
    	
    	for(int i = start; i<end; i++) {
    		if(bits.get(i))
    			count++;
    	}
    	
    	return count;
    }
    
    private ArrayList<Boolean> intToBits(int bit){
        ArrayList<Boolean> bits = new ArrayList<>();
        
        for(int i=0;i<6;i++){
            if(bit % 2 == 1)
                bits.add(0,true);
            else 
                bits.add(0,false);
            
            bit/=2;
        }
        
        return bits;
    }
    private ArrayList<Integer> bitsToInts(ArrayList<Boolean> bits){
    	ArrayList<Integer> ints = new ArrayList<>();

    	//loop through each char in the bits list
    	for(int i=0; i<7; i++) {
    		
    		int bit = 0;
    		
    		//loop through every bit in the char
    		for(int j=0; j<6; j++) {
    			
    			if(bits.get(j+6*i)) {
    				bit += (int)Math.pow(2, 5-j);
    			}
    			
    		}
    		
    		ints.add(bit);
    		
    		
    	}
    	
    	return ints;
    }
    public String intsToString(ArrayList<Boolean> bits) {
    	
    	String name = "";
    	
    	ArrayList<Integer> ints = bitsToInts(bits);
        
        for(int chara: ints) {
        	name += bitlyChars.get(chara);
        }
        
        return name;
    }
    
    public BufferedImage createQR(String c, int pWidth){
        
        int packWidth = pWidth;
        
        BufferedImage qr = new BufferedImage(8+packWidth*2,8+packWidth*2,BufferedImage.TYPE_INT_ARGB);
        
        ArrayList<Boolean> bits = new ArrayList<>();
        
        String newC = packOrCut(c);
        
        for(char chara : newC.toCharArray()){
            bits.addAll(intToBits(bitlyChars.indexOf(chara)));
        }
        
        //Outer Black
        for(int x=0; x<qr.getWidth(); x++){
            for(int y=0; y<qr.getHeight(); y++){
                qr.setRGB(x, y, 0xFF000000);
            }
        }
        
        //InnerWhite
        for(int x=(int)Math.ceil(packWidth/2.0); x<qr.getWidth()-(int)Math.ceil(packWidth/2.0); x++){
            for(int y=(int)Math.ceil(packWidth/2.0); y<qr.getHeight()-(int)Math.ceil(packWidth/2.0); y++){
                qr.setRGB(x, y, 0xFFFFFFFF);
            }
        }
        
        //Top Left Orientation Corner (W)
        qr.setRGB(1+packWidth,1+packWidth,0xFFFFFFFF);
        //Bottom Left Orientation Corner (B)
        qr.setRGB(1+packWidth,5+packWidth, 0xFF000000);
        //Top Right Orientation Corner (B)
        qr.setRGB(5+packWidth,1+packWidth, 0xFF000000);
        //Bottom Right Orientation Corner (B)
        qr.setRGB(5+packWidth,5+packWidth, 0xFF000000);
        

        //First Char
        for(int i=0; i<6;i++){
            if(bits.get(i))
                qr.setRGB(0+packWidth,i+packWidth,0xFF000000);
        }
        //Second Char
        for(int i=6; i<12;i++){
        	if(i== 6){ 
        		if(bits.get(i))
        			qr.setRGB(1+packWidth,0+packWidth,0xFF000000);
        	}
        	else if(i==10){
        		if(bits.get(i))
        			qr.setRGB(1+packWidth,6+packWidth,0xFF000000);
        	}
        	else if(i==11) {
        		if(bits.get(i))
        			qr.setRGB(0+packWidth,6+packWidth,0xFF000000);
        	}
        	else {
        		if(bits.get(i))
        			qr.setRGB(1+packWidth,1+packWidth+i-6,0xFF000000);
        	}
        }
        //3rd-5th char
        for(int chara=2; chara<=4; chara++){
            for(int i=0; i<6; i++){
                if(bits.get(i+6*chara))
                    qr.setRGB(chara+packWidth,i+packWidth,0xFF000000);
                else
                    qr.setRGB(chara+packWidth,i+packWidth,0xFFFFFFFF);
            }
        }
        //6th Char
        for(int i=30; i<36;i++){

        	if(i == 30) { 
        		if(bits.get(i))
        			qr.setRGB(5+packWidth,0+packWidth,0xFF000000);
        	}
        	else if(i == 34){
        		if(bits.get(i))
        			qr.setRGB(5+packWidth,6+packWidth,0xFF000000);
        	}
        	else if(i == 35){
        		if(bits.get(i))
        			qr.setRGB(6+packWidth,6+packWidth,0xFF000000);
        	}
        	else{
        		if(bits.get(i))
        			qr.setRGB(5+packWidth,1+packWidth+i-6*5,0xFF000000);
        	}
                        
            
        }
        //7th Char
        for(int i=36; i<42;i++){
            if(bits.get(i))
                qr.setRGB(6+packWidth,packWidth+i-36,0xFF000000);
        }
        
        
        //Check Sum
        for(int i=0; i<42;i+= 6){
            if(bitsOn(bits,i,i+6) % 2 != 0)
                qr.setRGB(i/6+packWidth,7+packWidth,0xFF000000);
        }
        
        
        //far right border
        for(int i=0; i<qr.getHeight(); i++) {
        	qr.setRGB(qr.getWidth()-packWidth,i,0xFF000000);
        }
        
        return qr;
    }
    
    public String readQR(BufferedImage qr, int packWidth){
        
        ArrayList<Boolean> bits = new ArrayList<>();
        
        String name = "";
        
        //First Char
        for(int i=0; i<6;i++){
            if(qr.getRGB(0+packWidth,i+packWidth) == 0xFF000000)
                bits.add(true);
            else
            	bits.add(false);
        }
        //Second Char
        for(int i=6; i<12;i++){
        	if(i==6) { 
                    if(qr.getRGB(1+packWidth,packWidth) == 0xFF000000)
                        bits.add(true);
                    else
                    	bits.add(false);
                }
        	else if(i == 10){
                    if(qr.getRGB(1+packWidth,6+packWidth) == 0xFF000000)
                        bits.add(true);
                    else
                    	bits.add(false);
                }
        	else if (i == 11) {
                    if(qr.getRGB(packWidth,6+packWidth) == 0xFF000000)
                        bits.add(true);
                    else
                    	bits.add(false);
                }
        	else {
                    if(qr.getRGB(1+packWidth,1+packWidth+i-6) == 0xFF000000)
                        bits.add(true);
                    else
                    	bits.add(false);
                }
                        
            
        }
        //3rd-5th char
        for(int chara=2; chara<=4; chara++){
            for(int i=0; i<6; i++){
                if(qr.getRGB(chara+packWidth,i+packWidth) == 0xFF000000)
                    bits.add(true);
                else
                   bits.add(false);
            }
        }
        //6th Char
        for(int i=30; i<36;i++){

        	if(i==30){ 
        		bits.add(qr.getRGB(5+packWidth,0+packWidth) == 0xFF000000);
        	}
        	else if(i == 34){
        		bits.add(qr.getRGB(5+packWidth,6+packWidth) == 0xFF000000);
        	}
        	else if(i== 35){
        		bits.add(qr.getRGB(6+packWidth,6+packWidth) == 0xFF000000);
        	}
        	else{
        		bits.add(qr.getRGB(5+packWidth,1+packWidth+i-6*5) == 0xFF000000);
        	}
                              
        }
        //7th Char
        for(int i=36; i<42;i++){
            bits.add(qr.getRGB(6+packWidth,packWidth+i-36) == 0xFF000000);
        }

        if(!bits.isEmpty())
            name = intsToString(bits);
        
        
        
        //Check Sum

        	
        	
        for(int i=0; i<42; i+=6) {
        	//If the bits are odd but check sum is not on
        	if(name != (null) && bitsOn(bits,i,i+6) % 2 != 0
        			&& qr.getRGB(i/6+packWidth,7+packWidth) != 0xFF000000) {

        		name = null;
        		
        	//Or if the bits are even and the check sum is on	
        	} else if(name != (null) && bitsOn(bits,i,i+6) % 2 == 0
        			&& qr.getRGB(i/6+packWidth,7+packWidth) != 0xFFFFFFFF) {

        		name = null;

        	}
        }
	     
	        
        
        
        
        return name;
    }

    
    public BufferedImage rotateUntilGood(BufferedImage src, int packWidth) {
    	BufferedImage rotated = src;
    	
    	int rotateCount = 0;
    	
    	while(!(rotated.getRGB(1+packWidth,1+packWidth) == 0xFFFFFFFF && rotated.getRGB(1+packWidth,5+packWidth) == 0xFF000000
    			&& rotated.getRGB(5+packWidth,1+packWidth) == 0xFF000000 && rotated.getRGB(5+packWidth,5+packWidth) == 0xFF000000)) {
    		
    		rotated = rotate(rotated);
    		
    		rotateCount++;
    		
    		if(rotateCount > 5)
    			break;
    	}
    	
    	if(rotateCount > 5)
    		return null;
    	
    	return rotated;
    	
    }
    public BufferedImage rotate(BufferedImage bimg) {

        int w = bimg.getWidth();    
        int h = bimg.getHeight();

        BufferedImage rotated = new BufferedImage(w, h, bimg.getType());  
        Graphics2D graphic = rotated.createGraphics();
        graphic.rotate(Math.PI/2, w/2, h/2);
        graphic.drawImage(bimg, null, 0, 0);
        graphic.dispose();
        return rotated;
    }
    private String packOrCut(String c){
        
        String packedOrCut = c;
        
        if(c.length() > 7){
            packedOrCut = c.substring(0,7);
        }else if(c.length() < 7){
            for(int i=7-c.length(); i>0; i--){
                packedOrCut += " ";
            }
        }
        
        return packedOrCut;
    }
    
    
    public ArrayList<Character> initAlphabet(){
        ArrayList<Character> alpha = new ArrayList<>();
        
        File alphaFile = new File("QR Code Reader/src/Camera/Alphabet.txt");
        
        try{
            @SuppressWarnings("resource")
			Scanner fileReader = new Scanner(alphaFile);

            while(fileReader.hasNextLine()){
                alpha.add(fileReader.nextLine().toCharArray()[0]);
            }
        } catch(FileNotFoundException ex){
            System.out.println("Oopsie Poopsie!");
        }
        
        return alpha;
    }
    
    
    
    public void printStuff(ArrayList<Boolean> bits){
        
        int count = 0;
        for(Boolean bit:bits){
           
            System.out.print(bit ? 1: 0);
            
            count++;
            
            if(count % 6 == 0)
                System.out.println();
        }
        
    }
    
    
    
    public static void main(String args[]) throws InterruptedException, IOException{
        
        QREngine qr = new QREngine();
        qr.setPreferredSize(new Dimension(700,700));
        
        JFrame myFrame = new JFrame("Hello");
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.getContentPane().add(qr);
        
        myFrame.setSize(qr.getPreferredSize());
        
        
        BufferedImage theImage = qr.createQR(qrWord, 3);
        
        qr.setMyImage(theImage);
        myFrame.getComponent(0).repaint();
        
        String word = qr.readQR(theImage,3);
        
        System.out.println((word));
        
        
        
        myFrame.addMouseListener(new MouseAdapter() {
			
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		//qr.printStuff(qr.readQR(theImage,3));
        	}

        });
        
    }
}
