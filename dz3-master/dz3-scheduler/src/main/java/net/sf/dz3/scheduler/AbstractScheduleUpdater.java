package net.sf.dz3.scheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.dz3.device.model.Thermostat;

/**
 * Back end independent base for the schedule updater.
 * 
 * @author Copyright &copy; <a href="mailto:vt@freehold.crocodile.org">Vadim Tkachenko</a> 2001-2010
 */
public abstract class AbstractScheduleUpdater implements ScheduleUpdater {
    
    /**
     * Set of thermostats to retrieve updated schedule for.
     */
    private final Map<String, Set<Thermostat>> targetMap = new TreeMap<String, Set<Thermostat>>();
    
    /**
     * Create an instance.
     * 
     * @param ts2source Keys are thermostats to update the schedule for,
     * values are source names to pull schedules from.
     */
    public AbstractScheduleUpdater(Map<Thermostat, String> ts2source) {
    
        if (ts2source == null || ts2source.isEmpty()) {
            
            throw new IllegalStateException("No thermostats to control given, why bother?");
        }
        
        for (Iterator<Thermostat> i = ts2source.keySet().iterator(); i.hasNext(); ) {
            
            Thermostat ts = i.next();
            String source = ts2source.get(ts);
            
            if (source == null || "".equals(source)) {
                
                throw new IllegalArgumentException("Source name can't be null or empty (check entry for thermostat '" + ts.getName() + "')");
            }
            
            Set<Thermostat> tSet = targetMap.get(source);
            
            if (tSet == null) {
                
                tSet = new TreeSet<Thermostat>();
                targetMap.put(source, tSet);
            }
            
            tSet.add(ts);
        }
    }
    
    /**
     * @return Thermostat names to retrieve schedules for.
     */
    protected final Set<String> getTargetNames() {
        
        return new TreeSet<String>(targetMap.keySet());
    }
    
    /**
     * Get a thermostat instance by name.
     * 
     * @param name Name to match.
     * 
     * @return Thermostat instance, or {@code null} if not found.
     */
    protected final Set<Thermostat> getByName(String name) {
        
        return targetMap.get(name);
    }
}
