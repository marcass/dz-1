package net.sf.dz3.device.sensor;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import net.sf.dz3.device.sensor.impl.AnalogConverterLM34;
import junit.framework.TestCase;

public class AnalogConverterLM34Test extends TestCase {
    
    protected final Logger logger = Logger.getLogger(getClass());

    private final AnalogConverter c = new AnalogConverterLM34();

    public void testLM34High() {
        
        
        assertEquals("High boundary conversion failed", 148.889, c.convert(3000d), 0.001);
    }

    public void testLM34Middle() {
        
        
        assertEquals("Midrange conversion failed", 22.222, c.convert(720d), 0.001);
    }

    public void testLM34Low() {
        
        assertEquals("Low boundary conversion failed", -45.556, c.convert(-500d), 0.001);
    }

    public void testLM34AnalogReference() {
        
        NDC.push("LM34");
        
        try {
        
            printC(1100d);
            printC(2560d);
            printC(5000d);

        } finally {
            NDC.pop();
        }
    }
    
    private void printC(double millivolts) {
        logger.info("Top measurable temperature at " + millivolts + "mV analog reference: " + c.convert(millivolts) + "°C");
    }
}
