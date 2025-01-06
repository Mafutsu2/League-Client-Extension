package org.mafutsu.model;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Image;

public class Role {
  private String id, urlPrimary, urlSecondary, urlDefault, shortcut;
  private JButton button;

  public Role(String id, String urlPrimary, String urlSecondary, String urlDefault, String shortcut) {
    this.id = id;
    this.urlPrimary = urlPrimary;
    this.urlSecondary = urlSecondary;
    this.urlDefault = urlDefault;
    this.shortcut = shortcut;
  }

  public String getId() {
    return id;
  }

  public String getUrlDefault() {
    return urlDefault;
  }

  public String getShortcut() {
    return shortcut;
  }

  public JButton getButton() {
    return button;
  }

  public void setButton(JButton button) {
    this.button = button;
  }

  public void setState(int state) {
    ImageIcon roleIcon = new ImageIcon(getClass().getResource(state == 1 ? urlPrimary : (state == 2 ? urlSecondary : urlDefault)));
    Image roleImage = roleIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
    ImageIcon roleIconScaled = new ImageIcon(roleImage);
    button.setIcon(roleIconScaled);
  }
}
