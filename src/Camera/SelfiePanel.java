package Camera;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author 22cloteauxm
 */
public class SelfiePanel extends JPanel{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage mySelfie;
    
    public void setSelfie(BufferedImage c){mySelfie = c;}
    
    public SelfiePanel(){
        mySelfie = new BufferedImage(640,480,BufferedImage.TYPE_INT_ARGB);
    }
    
    public void paintComponent(Graphics g){
        g.drawImage(mySelfie, 0,0,getWidth(),getHeight(), this);
    }
    
}
