package org.mafutsu.model;

import org.mafutsu.util.Constants;

import javax.swing.JButton;
import java.util.Timer;
import java.util.TimerTask;

public class Roles {
  public final String iconTopPrimary = "/primary/rankposition_challenger-top.png";
  public final String iconTopSecondary = "/secondary/rankposition_grandmaster-top.png";
  public final String iconTopDefault = "/default/icon-position-top-hover.png";

  public final String iconJunglePrimary = "/primary/rankposition_challenger-jungle.png";
  public final String iconJungleSecondary = "/secondary/rankposition_grandmaster-jungle.png";
  public final String iconJungleDefault = "/default/icon-position-jungle-hover.png";

  public final String iconMiddlePrimary = "/primary/rankposition_challenger-mid.png";
  public final String iconMiddleSecondary = "/secondary/rankposition_grandmaster-mid.png";
  public final String iconMiddleDefault = "/default/icon-position-middle-hover.png";

  public final String iconBottomPrimary = "/primary/rankposition_challenger-bot.png";
  public final String iconBottomSecondary = "/secondary/rankposition_grandmaster-bot.png";
  public final String iconBottomDefault = "/default/icon-position-bottom-hover.png";

  public final String iconSupportPrimary = "/primary/rankposition_challenger-support.png";
  public final String iconSupportSecondary = "/secondary/rankposition_grandmaster-support.png";
  public final String iconSupportDefault = "/default/icon-position-utility-hover.png";

  public final String iconAutofillPrimary = "/primary/rankposition_challenger-fill.png";
  public final String iconAutofillSecondary = "/secondary/rankposition_grandmaster-fill.png";
  public final String iconAutofillDefault = "/default/icon-position-fill-hover.png";
  private Role[] list, idPref;
  //private String[] idPref;
  private int pointer;
  private Timer timer;

  public Roles() {
    list = new Role[] {
      new Role(Constants.POSITION_TOP, iconTopPrimary, iconTopSecondary, iconTopDefault, "top"),
      new Role(Constants.POSITION_JUNGLE, iconJunglePrimary, iconJungleSecondary, iconJungleDefault, "jg"),
      new Role(Constants.POSITION_MIDDLE, iconMiddlePrimary, iconMiddleSecondary, iconMiddleDefault, "mid"),
      new Role(Constants.POSITION_BOTTOM, iconBottomPrimary, iconBottomSecondary, iconBottomDefault, "adc"),
      new Role(Constants.POSITION_UTILITY, iconSupportPrimary, iconSupportSecondary, iconSupportDefault, "sup"),
      new Role(Constants.POSITION_FILL, iconAutofillPrimary, iconAutofillSecondary, iconAutofillDefault, "fill")
    };
    idPref = new Role[] {list[2], list[0]};
    pointer = 0;
  }

  public Role[] getIdPref() {
    return idPref;
  }

  public void setIdPref(String[] id) {
    this.idPref = new Role[] {getRole(id[0]), getRole(id[1])};
  }

  public Role getRole(String id) {
    for(Role r : list) {
      if(r.getId().equals(id))
        return r;
    }
    return null;
  }

  public void setButton(String id, JButton button) {
    for(Role r : list) {
      if(r.getId().equals(id))
        r.setButton(button);
    }
  }

  ;

  public void toggleRole(String id) {
    if(timer != null) {
      timer.cancel();
      timer = null;
    }
    if(pointer == 0) {
      timer = new Timer();
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          pointer = 0;
        }
      }, 2000);
    }
    idPref[pointer] = getRole(id);
    pointer = pointer == 0 ? 1 : 0;
    refreshVisual();
  }

  public void refreshVisual() {
    for(Role r : list) {
      if(r.getId().equals(idPref[0].getId()))
        r.setState(1);
      else if(r.getId().equals(idPref[1].getId()))
        r.setState(2);
      else
        r.setState(0);
    }
  }
}
