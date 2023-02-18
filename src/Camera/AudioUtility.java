package Camera;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * This class contains static methods to allow loading of Clip objects
 * @author spockm
 */
public class AudioUtility 
{
    //A utility to load audio clips!
    public static Clip loadClip(String fnm)
    {
        AudioInputStream audioInputStream; 
        Clip clip = null;
        try
        {
            audioInputStream =  
                AudioSystem.getAudioInputStream(new File(fnm).getAbsoluteFile());
            // create clip reference 
            clip = AudioSystem.getClip(); 
            clip.open(audioInputStream);
        }
        catch(Exception e)
        {
            System.out.println("Error building audio clip from file="+fnm);
        }
        return clip;
     }
    
}
