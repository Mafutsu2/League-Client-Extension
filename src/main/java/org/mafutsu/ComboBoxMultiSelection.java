package org.mafutsu;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatCheckBoxIcon;
import com.formdev.flatlaf.ui.FlatComboBoxUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import org.mafutsu.model.Champion;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ComboBoxMultiSelection<E> extends JComboBox<E> {

  private final ArrayList<Champion> selectedItems = new ArrayList<>();
  private final ComboBoxMultiCellEditor comboBoxMultiCellEditor;
  private Component comboList;
  private org.mafutsu.ComboBoxMultiListener comboBoxMultiListener;
  private String key;

  public ComboBoxMultiSelection(String key, ArrayList<E> champions, org.mafutsu.ComboBoxMultiListener comboBoxMultiListener) {
    this(champions);
    this.key = key;
    this.comboBoxMultiListener = comboBoxMultiListener;
  }

  public ComboBoxMultiSelection(ArrayList<E> champions) {
    setUI(new ComboBoxMultiUI());
    comboBoxMultiCellEditor = new ComboBoxMultiCellEditor();
    for(E c : champions) {
      this.addItem(c);
    }

    setRenderer(new ComboBoxMultiCellRenderer());
    setEditor(this.comboBoxMultiCellEditor);
    setEditable(true);
    addActionListener((e) -> {
      if(e.getModifiers() == ActionEvent.MOUSE_EVENT_MASK) {
        JComboBox combo = (JComboBox) e.getSource();
        Object obj = combo.getSelectedItem();
        if(selectedItems.contains(obj)) {
          removeItemObject(obj);
        } else {
          addItemObject(obj);
        }
      }
    });
  }

  public List<Champion> getSelectedItems() {
    return selectedItems;
  }

  public void setSelectedItems(List<Champion> selectedItems) {
    List<Object> comboItem = new ArrayList<>();
    int count = getItemCount();
    for(int i = 0; i < count; i++) {
      comboItem.add(getItemAt(i));
    }
    for(Object obj : selectedItems) {
      if(comboItem.contains(obj)) {
        addItemObject(obj);
      }
    }
    comboItem.clear();
  }

  public void clearSelectedItems() {
    selectedItems.clear();
    Component editorCom = getEditor().getEditorComponent();
    if(editorCom instanceof JScrollPane) {
      JScrollPane scroll = (JScrollPane) editorCom;
      JPanel panel = (JPanel) scroll.getViewport().getComponent(0);
      panel.removeAll();
      revalidate();
      repaint();
      comboList.repaint();
    }
  }

  private void removeItemObject(Object obj) {
    selectedItems.remove(obj);
    comboBoxMultiCellEditor.removeItem(obj);
    if(comboList != null) {
      comboList.repaint();
    }
    comboBoxMultiListener.onComboBoxChange(key, selectedItems);
  }

  private void addItemObject(Object obj) {
    selectedItems.add((Champion) obj);
    comboBoxMultiCellEditor.addItem(obj);
    if(comboList != null) {
      comboList.repaint();
    }
    comboBoxMultiListener.onComboBoxChange(key, selectedItems);
  }

  public void setButtonsEnabled(boolean isEnabled) {
    comboBoxMultiCellEditor.setItemsEnabled(isEnabled);
  }

  @Override
  public void setPopupVisible(boolean v) {
  }

  private class ComboBoxMultiUI extends FlatComboBoxUI {

    @Override
    protected ComboPopup createPopup() {
      return new MultiComboPopup(comboBox);
    }

    @Override
    protected Dimension getDisplaySize() {
      Dimension size = super.getDefaultSize();
      return new Dimension(0, size.height);
    }

    private class MultiComboPopup extends FlatComboPopup {

      public MultiComboPopup(JComboBox combo) {
        super(combo);
      }
    }
  }

  private class ComboBoxMultiCellRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, ((Champion) value).getName(), index, isSelected, cellHasFocus);
      if(comboList != list) {
        comboList = list;
      }
      setIcon(new CheckBoxIcon(selectedItems.contains(value)));
      return this;
    }
  }

  private class ComboBoxMultiCellEditor extends BasicComboBoxEditor {
    protected final JScrollPane scroll;
    protected final JPanel panel;

    public ComboBoxMultiCellEditor() {
      panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
      scroll = new JScrollPane(panel);
      scroll.putClientProperty(FlatClientProperties.STYLE, "border:2,2,2,2;" + "background:$ComboBox.editableBackground");
      panel.putClientProperty(FlatClientProperties.STYLE, "border:0,-4,0,-4;" + "background:$ComboBox.editableBackground");
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      JScrollBar scrollBar = scroll.getHorizontalScrollBar();
      scrollBar.putClientProperty(FlatClientProperties.STYLE, "width:3;" + "thumbInsets:0,0,0,1;" + "hoverTrackColor:null");
      scrollBar.setUnitIncrement(10);
    }

    protected void addItem(Object obj) {
      Item item = new Item(obj);
      panel.add(item);
      panel.repaint();
      panel.revalidate();
    }

    protected void removeItem(Object obj) {
      int count = panel.getComponentCount();
      for(int i = 0; i < count; i++) {
        Item item = (Item) panel.getComponent(i);
        if(item.getItem() == obj) {
          panel.remove(i);
          panel.revalidate();
          panel.repaint();
          break;
        }
      }
    }

    protected void setItemsEnabled(boolean isEnabled) {
      int count = panel.getComponentCount();
      for(int i = 0; i < count; i++) {
        Item item = (Item) panel.getComponent(i);
        item.setEnabled(isEnabled);
      }
    }

    @Override
    public Component getEditorComponent() {
      return scroll;
    }

  }

  private class CheckBoxIcon extends FlatCheckBoxIcon {

    private final boolean selected;

    public CheckBoxIcon(boolean selected) {
      this.selected = selected;
    }

    @Override
    protected boolean isSelected(Component c) {
      return selected;
    }
  }

  private class Item extends JButton {

    private final Object item;

    public Item(Object item) {
      super(((Champion) item).getName());
      this.item = item;
      init();
    }

    public Object getItem() {
      return item;
    }

    private void init() {
      putClientProperty(FlatClientProperties.STYLE, "border:0,5,0,5;");
      addActionListener((e) -> removeItemObject(item));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      FlatUIUtils.setRenderingHints(g2);
      int arc = UIScale.scale(10);
      g2.setColor(getBackground());
      FlatUIUtils.paintComponentBackground(g2, 0, 0, getWidth(), getHeight(), 0, arc);
      g2.dispose();
      super.paintComponent(g);
    }
  }
}
