package org.joshy.sketch.modes.powerup;

import org.joshy.sketch.Main;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.modes.DocContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 3/31/12
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PowerupManager {
    private static PowerupManager _self;
    public static PowerupManager get() {
        if(_self == null) {
            _self = new PowerupManager();
        }
        return _self;
    }


    private List<Powerup> powerups = new ArrayList<Powerup>();

    private PowerupManager() {
        powerups.add(new RokuPowerup());
        powerups.add(new FXMLPowerup());
    }

    public Iterable<? extends Powerup> getPowerups() {
        return powerups;
    }


    public static class EnablePowerup extends SAction {
        private Powerup powerup;
        private DocContext context;
        private Main main;

        public EnablePowerup(Powerup powerup, DocContext context, Main main) {
            super();
            this.powerup = powerup;
            this.context = context;
            this.main = main;
        }

        @Override
        public CharSequence getDisplayName() {
            return powerup.getMenuName();
        }

        @Override
        public void execute() throws Exception {
            powerup.enable(context, main);
        }
    }

}
