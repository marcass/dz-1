package net.sf.dz3.runtime;

import net.sf.dz3.device.model.DamperController;
import net.sf.jukebox.util.Interval;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Entry point into DZ Core.
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org"> Vadim Tkachenko</a> 2009
 */
public class Container {

    /**
     * Logger to use.
     */
    private static Logger logger = Logger.getLogger(Container.class);

    /**
     * Name of the configuration file embedded into the jar file.
     */
    public static final String CF_EMBEDDED = "spring-config.xml";
    
    /**
     * Name of the file this class expects to find the configuration in.
     * 
     * Must be on the root of the classpath.
     */
    public static final String CF_LOCAL = "spring-config-local.xml";
    
    private ApplicationContext applicationContext;

    /**
     * @param args None expected.
     */
    public static void main(String[] args) {
        
        new Container().run(args);
    }

    /**
     * Run the system.
     */
    public void run(String[] args) {

        NDC.push("run");

        try {

            loadSampleConfiguration();
            
            if (args.length == 0) {
                loadConfiguration(CF_LOCAL);
            } else {
                for (int offset = 0; offset < args.length; offset++) {
                    loadConfiguration(args[offset]);
                }
            }
            
            logger.info("Sleeping until killed");
            
            while (true) {
                Thread.sleep(10000);
            }

        } catch (Throwable t) {
            logger.fatal("Unexpected exception: ", t);
        } finally {
            NDC.pop();
        }
    }

    /**
     * Load the configuration already present in the jar file.
     * 
     * With some luck, all objects instantiated here will be garbage collected
     * pretty soon and won't matter.
     * 
     * If this bothers you, remove the method invocation and recompile.
     * 
     * @see #CF_EMBEDDED
     */
    private void loadSampleConfiguration() {

        NDC.push("loadSampleConfiguration");
        
        try {
            
            // We don't need it for anything other than loading a sample,
            // hence local scope.
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { CF_EMBEDDED });

            // This class is the root of the instantiation hierarchy -
            // if it can be instantiated, then everything else is fine
            @SuppressWarnings("unused")
            DamperController dc = (DamperController) applicationContext.getBean("damper_controller-sample1");

        } catch (NoSuchBeanDefinitionException ex) {

            logger.debug("Oh, they found and deleted the sample configuration! Good.");
            
        } finally {
            NDC.pop();
        }
    }

    /**
     * Load the user supplied configuration.
     * 
     * This will be the configuration actually used to run the system.
     * 
     * @see #CF_LOCAL
     */
    private void loadConfiguration(String file) {

        NDC.push("loadConfiguration(" + file + ")");

        try {
            
            long start = System.currentTimeMillis();

            applicationContext = new ClassPathXmlApplicationContext(new String[] { file });

            ((AbstractApplicationContext) applicationContext).registerShutdownHook();
            
            logger.info("Loaded configuration in " + Interval.toTimeInterval(System.currentTimeMillis() - start));

        } catch (BeanDefinitionStoreException ex) {
            
            logger.error("Failed to load " + file, ex);
            
        } finally {
            NDC.pop();
        }
    }
}
