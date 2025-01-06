package org.mafutsu;

import org.mafutsu.model.Champion;

import java.util.ArrayList;

public interface ComboBoxMultiListener {
  void onComboBoxChange(String key, ArrayList<Champion> champions);
}
