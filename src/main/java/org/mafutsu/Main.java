package org.mafutsu;

import com.formdev.flatlaf.FlatDarkLaf;
import org.mafutsu.util.Constants;

import javax.swing.UIManager;
import java.awt.Color;

public class Main {
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
      UIManager.put("Button.innerFocusWidth", 0);
      UIManager.put("Button.focusedBorderColor", Color.TRANSLUCENT);
      UIManager.put("ToggleButton.background", Constants.COLOR_RED_10);//10% opacity
      UIManager.put("Component.focusedBorderColor", Color.TRANSLUCENT);
    } catch(Exception e) {
      e.printStackTrace();
    }
    Window window = new Window();
    window.createWindow();
  }
}
