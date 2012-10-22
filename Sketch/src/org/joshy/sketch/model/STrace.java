package org.joshy.sketch.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 10/22/12
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class STrace extends SPoly {

    private Set<SlaveFunction> slaves;

    public STrace() {
        super();
        slaves = new HashSet<SlaveFunction>();
    }

    public void updateSlavePositions() {
        for(SlaveFunction slave : slaves) {
            slave.apply();
        }
    }

    public void addSlaveFunction(SlaveFunction func) {
        slaves.add(func);
    }

    public interface SlaveFunction {
        public void apply();
    }

    @Override
    public void setTranslateX(double translateX) {
        super.setTranslateX(translateX);
        updateSlavePositions();
    }

    @Override
    public void setTranslateY(double translateY) {
        super.setTranslateY(translateY);    //To change body of overridden methods use File | Settings | File Templates.
        updateSlavePositions();
    }
}
