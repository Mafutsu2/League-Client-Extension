package org.mafutsu;

import com.formdev.flatlaf.FlatClientProperties;
import org.mafutsu.model.Champion;
import org.mafutsu.model.Participant;
import org.mafutsu.model.Roles;
import org.mafutsu.util.Constants;
import org.json.JSONArray;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.prefs.Preferences;

public class Window implements ActionListener, ICommands, ComboBoxMultiListener, KeyListener {
  private Commands commands;
  private JLabel textLabel, delayLabel, banLabel, hoverLabel, pickLabel, primaryLabel, secondaryLabel;
  private JTextArea textArea;
  private JTextField delay;
  private JButton connectBtn, createGame, dodge, pickRandomSkin, revealSummoners, openOPGG, openPoro, openUGG, openDeeplol;
  private ComboBoxMultiSelection<Champion> banChampionsPrimary, hoverChampionsPrimary, pickChampionsPrimary, banChampionsSecondary, hoverChampionsSecondary, pickChampionsSecondary;
  private Roles roles;
  private JToggleButton autoAccept, autoAcceptSwap, autoChampSelect;
  private JFrame frame;
  private String buttonStylesSelected, buttonStylesUnselected;
  private Preferences prefs;
  private ArrayList<Champion> champions;

  public Window() {
    commands = new Commands(this);
    buttonStylesSelected = "borderColor:#3a9c54;selectedBackground:#" + Integer.toHexString(Constants.COLOR_GREEN_10.getRGB()).substring(2);//10% opacity
    buttonStylesUnselected = "borderColor:#9c453a;";
    roles = new Roles();
    prefs = Preferences.userNodeForPackage(this.getClass());
  }

  public void savePrefs() {
    JSONArray banChampionsPrimaryJSON = new JSONArray();
    JSONArray hoverChampionsPrimaryJSON = new JSONArray();
    JSONArray pickChampionsPrimaryJSON = new JSONArray();
    for(Champion c : banChampionsPrimary.getSelectedItems())
      banChampionsPrimaryJSON.put(c.toJSON());
    for(Champion c : hoverChampionsPrimary.getSelectedItems())
      hoverChampionsPrimaryJSON.put(c.toJSON());
    for(Champion c : pickChampionsPrimary.getSelectedItems())
      pickChampionsPrimaryJSON.put(c.toJSON());
    prefs.put("banChampionsPrimary", banChampionsPrimaryJSON.toString());
    prefs.put("hoverChampionsPrimary", hoverChampionsPrimaryJSON.toString());
    prefs.put("pickChampionsPrimary", pickChampionsPrimaryJSON.toString());

    JSONArray banChampionsSecondaryJSON = new JSONArray();
    JSONArray hoverChampionsSecondaryJSON = new JSONArray();
    JSONArray pickChampionsSecondaryJSON = new JSONArray();
    for(Champion c : banChampionsSecondary.getSelectedItems())
      banChampionsSecondaryJSON.put(c.toJSON());
    for(Champion c : hoverChampionsSecondary.getSelectedItems())
      hoverChampionsSecondaryJSON.put(c.toJSON());
    for(Champion c : pickChampionsSecondary.getSelectedItems())
      pickChampionsSecondaryJSON.put(c.toJSON());
    prefs.put("banChampionsSecondary", banChampionsSecondaryJSON.toString());
    prefs.put("hoverChampionsSecondary", hoverChampionsSecondaryJSON.toString());
    prefs.put("pickChampionsSecondary", pickChampionsSecondaryJSON.toString());

    prefs.put("primaryRole", roles.getIdPref()[0].getId());
    prefs.put("secondaryRole", roles.getIdPref()[1].getId());
    prefs.put("delay", delay.getText());
  }

  public void loadPrefs() {
    String banChampionsPrimaryPref = prefs.get("banChampionsPrimary", null);
    String hoverChampionsPrimaryPref = prefs.get("hoverChampionsPrimary", null);
    String pickChampionsPrimaryPref = prefs.get("pickChampionsPrimary", null);
    banChampionsPrimary.setSelectedItems(loadPref(banChampionsPrimaryPref));
    hoverChampionsPrimary.setSelectedItems(loadPref(hoverChampionsPrimaryPref));
    pickChampionsPrimary.setSelectedItems(loadPref(pickChampionsPrimaryPref));

    String banChampionsSecondaryPref = prefs.get("banChampionsSecondary", null);
    String hoverChampionsSecondaryPref = prefs.get("hoverChampionsSecondary", null);
    String pickChampionsSecondaryPref = prefs.get("pickChampionsSecondary", null);
    banChampionsSecondary.setSelectedItems(loadPref(banChampionsSecondaryPref));
    hoverChampionsSecondary.setSelectedItems(loadPref(hoverChampionsSecondaryPref));
    pickChampionsSecondary.setSelectedItems(loadPref(pickChampionsSecondaryPref));

    roles.setIdPref(new String[] {prefs.get("primaryRole", Constants.POSITION_MIDDLE), prefs.get("secondaryRole", Constants.POSITION_TOP)});
    roles.refreshVisual();
    primaryLabel.setText(roles.getIdPref()[0].getShortcut());
    secondaryLabel.setText(roles.getIdPref()[1].getShortcut());
    commands.setPrimaryRole(roles.getIdPref()[0].getId());
    commands.setSecondaryRole(roles.getIdPref()[1].getId());
    delay.setText(prefs.get("delay", "500"));
    refreshDelay();
  }

  private ArrayList<Champion> loadPref(String championsPref) {
    ArrayList<Champion> championsList = new ArrayList<>();
    if(championsPref != null) {
      JSONArray championsJSON = new JSONArray(championsPref);
      for(int i = 0; i < championsJSON.length(); i++) {
        Champion prefChampion = new Champion(championsJSON.getJSONObject(i));
        champions.stream().filter(c -> c.getId() == prefChampion.getId()).findFirst().ifPresent(championsList::add);
      }
    }
    return championsList;
  }

  public void createWindow() {
    frame = new JFrame("League Client Extension");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        commands.disconnectWebsocket();
        savePrefs();
        System.exit(0);
      }
    });

    Container container = frame.getContentPane();

    SpringLayout springLayout = new SpringLayout();
    frame.setLayout(springLayout);

    textLabel = new JLabel("Not connected to client!", SwingConstants.CENTER);
    textLabel.setForeground(Constants.COLOR_RED);
    frame.add(textLabel);
    setConstraints(springLayout, container, null, textLabel);

    connectBtn = new JButton("Connect to Client");
    connectBtn.setEnabled(true);
    connectBtn.setActionCommand("connect");
    connectBtn.addActionListener(this);
    frame.add(connectBtn);
    setConstraints(springLayout, container, textLabel, connectBtn);

    GridLayout gridLayoutRoles = new GridLayout(1, 5);
    gridLayoutRoles.setHgap(4);
    JPanel panelRoles = new JPanel();
    panelRoles.setLayout(gridLayoutRoles);
    frame.add(panelRoles);
    setConstraints(springLayout, container, connectBtn, panelRoles);

    for(String r : Constants.ROLES_STR) {
      ImageIcon roleIcon = new ImageIcon(roles.getRole(r).getUrlDefault());
      Image roleImage = roleIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
      ImageIcon roleIconScaled = new ImageIcon(roleImage);
      JButton roleButton = new JButton(roleIconScaled);
      roleButton.setMargin(new Insets(2, 4, 2, 4));
      roleButton.setEnabled(false);
      roleButton.setActionCommand(r);
      roleButton.addActionListener(this);
      panelRoles.add(roleButton);
      roles.setButton(r, roleButton);
    }
    roles.refreshVisual();

    createGame = new JButton();
    changeCreateGameAnimation(getClass().getResource("/playButtonDefault.gif"));
    createGame.setEnabled(false);
    createGame.setMargin(new Insets(0, 0, 0, 0));
    createGame.setActionCommand("createGame");
    createGame.addActionListener(this);
    createGame.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        changeCreateGameAnimation(getClass().getResource("/playButtonHovered.gif"));
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        changeCreateGameAnimation(getClass().getResource("/playButtonDefault.gif"));
      }
    });
    panelRoles.add(createGame);
    //h= 24 b= 1
    //w= 32 b= 1

    GridBagLayout acceptGridBagLayout = new GridBagLayout();
    GridBagConstraints gbc2 = new GridBagConstraints();
    JPanel acceptPanel = new JPanel();
    acceptPanel.setLayout(acceptGridBagLayout);
    frame.add(acceptPanel);
    setConstraints(springLayout, container, panelRoles, acceptPanel);

    autoAccept = new JToggleButton("Auto Accept");
    autoAccept.putClientProperty(FlatClientProperties.STYLE, buttonStylesUnselected);
    autoAccept.setSelected(false);
    autoAccept.setEnabled(false);
    autoAccept.setActionCommand("autoAccept");
    autoAccept.addActionListener(this);
    gbc2.weightx = 6;
    addGridBagConstraints(autoAccept, acceptGridBagLayout, gbc2, 0, 0, 6, 1, true);
    acceptPanel.add(autoAccept);

    delay = new JTextField(3);
    delay.setEnabled(false);
    delay.addKeyListener(this);
    gbc2.weightx = 0;
    addGridBagConstraints(delay, acceptGridBagLayout, gbc2, 6, 0, 1, 1, true);
    acceptPanel.add(delay);

    delayLabel = new JLabel("ms");
    delayLabel.setEnabled(false);
    addGridBagConstraints(delayLabel, acceptGridBagLayout, gbc2, 7, 0, 1, 1);
    acceptPanel.add(delayLabel);

    dodge = new JButton("Dodge");
    dodge.setEnabled(false);
    dodge.setActionCommand("dodge");
    dodge.addActionListener(this);
    frame.add(dodge);
    setConstraints(springLayout, container, acceptPanel, dodge);

    autoAcceptSwap = new JToggleButton("Auto Accept Swap");
    autoAcceptSwap.putClientProperty(FlatClientProperties.STYLE, buttonStylesUnselected);
    autoAcceptSwap.setSelected(false);
    autoAcceptSwap.setEnabled(false);
    autoAcceptSwap.setActionCommand("autoAcceptSwap");
    autoAcceptSwap.addActionListener(this);
    frame.add(autoAcceptSwap);
    setConstraints(springLayout, container, dodge, autoAcceptSwap);

    GridBagLayout championsGridBagLayout = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel championsPanel = new JPanel();
    championsPanel.setLayout(championsGridBagLayout);
    frame.add(championsPanel);
    setConstraints(springLayout, container, autoAcceptSwap, championsPanel, 4);

    autoChampSelect = new JToggleButton();
    autoChampSelect.setMargin(new Insets(0, 0, 0, 0));
    autoChampSelect.putClientProperty(FlatClientProperties.STYLE, buttonStylesUnselected);
    autoChampSelect.setSelected(false);
    autoChampSelect.setEnabled(false);
    autoChampSelect.setActionCommand("autoChampSelect");
    autoChampSelect.addActionListener(this);
    autoChampSelect.setPreferredSize(new Dimension(15, 15));
    gbc.weightx = 0;
    addGridBagConstraints(autoChampSelect, championsGridBagLayout, gbc, 0, 0, 1, 1, true, GridBagConstraints.NONE, GridBagConstraints.LINE_START);
    championsPanel.add(autoChampSelect);
    banLabel = new JLabel("Auto Ban");
    banLabel.setHorizontalAlignment(SwingConstants.LEFT);
    gbc.weightx = 1;
    addGridBagConstraints(banLabel, championsGridBagLayout, gbc, 1, 0, 2, 1, true);
    championsPanel.add(banLabel);
    hoverLabel = new JLabel("Auto Hover");
    hoverLabel.setHorizontalAlignment(SwingConstants.CENTER);
    addGridBagConstraints(hoverLabel, championsGridBagLayout, gbc, 3, 0, 3, 1, true);
    championsPanel.add(hoverLabel);
    pickLabel = new JLabel("Auto Pick");
    pickLabel.setHorizontalAlignment(SwingConstants.CENTER);
    addGridBagConstraints(pickLabel, championsGridBagLayout, gbc, 6, 0, 3, 1);
    championsPanel.add(pickLabel);

    primaryLabel = addSeparator(championsPanel, championsGridBagLayout, gbc, 1, 0);

    champions = commands.getChampionsList();
    champions.sort(Comparator.comparing(Champion::getName));
    banChampionsPrimary = new ComboBoxMultiSelection<>("primaryBan", champions, this);
    addGridBagConstraints(banChampionsPrimary, championsGridBagLayout, gbc, 0, 2, 3, 1, 2, true);
    championsPanel.add(banChampionsPrimary);
    hoverChampionsPrimary = new ComboBoxMultiSelection<>("primaryHover", champions, this);
    addGridBagConstraints(hoverChampionsPrimary, championsGridBagLayout, gbc, 3, 2, 3, 1, 2, true);
    championsPanel.add(hoverChampionsPrimary);
    pickChampionsPrimary = new ComboBoxMultiSelection<>("primaryPick", champions, this);
    addGridBagConstraints(pickChampionsPrimary, championsGridBagLayout, gbc, 6, 2, 3, 1, 2);
    championsPanel.add(pickChampionsPrimary);

    secondaryLabel = addSeparator(championsPanel, championsGridBagLayout, gbc, 3, 4);

    banChampionsSecondary = new ComboBoxMultiSelection<>("secondaryBan", champions, this);
    addGridBagConstraints(banChampionsSecondary, championsGridBagLayout, gbc, 0, 4, 3, 1, 2, true);
    championsPanel.add(banChampionsSecondary);
    hoverChampionsSecondary = new ComboBoxMultiSelection<>("secondaryHover", champions, this);
    addGridBagConstraints(hoverChampionsSecondary, championsGridBagLayout, gbc, 3, 4, 3, 1, 2, true);
    championsPanel.add(hoverChampionsSecondary);
    pickChampionsSecondary = new ComboBoxMultiSelection<>("secondaryPick", champions, this);
    addGridBagConstraints(pickChampionsSecondary, championsGridBagLayout, gbc, 6, 4, 3, 1, 2);
    championsPanel.add(pickChampionsSecondary);

    pickRandomSkin = new JButton("Pick Random Skin");
    pickRandomSkin.setEnabled(false);
    pickRandomSkin.setActionCommand("pickRandomSkin");
    pickRandomSkin.addActionListener(this);
    frame.add(pickRandomSkin);
    setConstraints(springLayout, container, championsPanel, pickRandomSkin);

    revealSummoners = new JButton("Reveal Summoner Names");
    revealSummoners.setEnabled(false);
    revealSummoners.setActionCommand("revealSummoners");
    revealSummoners.addActionListener(this);
    frame.add(revealSummoners);
    setConstraints(springLayout, container, pickRandomSkin, revealSummoners);

    GridLayout gridLayout = new GridLayout(1, 4);
    gridLayout.setHgap(4);
    JPanel panel = new JPanel();
    panel.setLayout(gridLayout);
    frame.add(panel);
    setConstraints(springLayout, container, revealSummoners, panel);

    openOPGG = addWebsiteButton(panel, "OPGG");
    openPoro = addWebsiteButton(panel, "Poro");
    openUGG = addWebsiteButton(panel, "UGG");
    openDeeplol = addWebsiteButton(panel, "Deeplol");

    textArea = new JTextArea();
    textArea.setEditable(true);
    JScrollPane scrollPane = new JScrollPane(textArea);
    frame.add(scrollPane);
    springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, container);
    springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, panel);
    springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, container);
    springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, container);

    frame.setSize(300, 560);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    loadPrefs();
    enableAutoChampSelect(false);
  }

  private JButton addWebsiteButton(JPanel panel, String name) {
    JButton button = new JButton(name);
    button.setMargin(new Insets(2, 4, 2, 4));
    button.setEnabled(false);
    button.setActionCommand(name);
    button.addActionListener(this);
    panel.add(button);
    return button;
  }

  private JLabel addSeparator(JPanel panel, GridBagLayout layout, GridBagConstraints gbc, int gridY, int marginTop) {
    GridBagLayout gridLayoutSeparator = new GridBagLayout();
    GridBagConstraints gbc3 = new GridBagConstraints();
    JPanel separatorPanel = new JPanel();
    separatorPanel.setLayout(gridLayoutSeparator);
    addGridBagConstraints(separatorPanel, layout, gbc, 0, gridY, 9, 1, 0);
    panel.add(separatorPanel);

    JSeparator separator1 = new JSeparator();
    gbc3.weightx = 1;
    addGridBagConstraints(separator1, gridLayoutSeparator, gbc3, 0, 0, 1, 1, marginTop, true, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
    separatorPanel.add(separator1);
    JLabel label = new JLabel("", SwingConstants.CENTER);
    label.putClientProperty(FlatClientProperties.STYLE, "foreground:#828282;font:+italic -1");
    gbc3.weightx = 0;
    addGridBagConstraints(label, gridLayoutSeparator, gbc3, 1, 0, 1, 1, marginTop, true, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
    separatorPanel.add(label);
    JSeparator separator2 = new JSeparator();
    gbc3.weightx = 1;
    addGridBagConstraints(separator2, gridLayoutSeparator, gbc3, 2, 0, 1, 1, marginTop, false, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
    separatorPanel.add(separator2);
    return label;
  }

  private void setConstraints(SpringLayout springLayout, Container container, JComponent previousComponent, JComponent component) {
    setConstraints(springLayout, container, previousComponent, component, 10);
  }

  private void setConstraints(SpringLayout springLayout, Container container, JComponent previousComponent, JComponent component, int paddingNorth) {
    springLayout.putConstraint(SpringLayout.WEST, component, 10, SpringLayout.WEST, container);
    if(previousComponent == null)
      springLayout.putConstraint(SpringLayout.NORTH, component, paddingNorth, SpringLayout.NORTH, container);
    else
      springLayout.putConstraint(SpringLayout.NORTH, component, paddingNorth, SpringLayout.SOUTH, previousComponent);
    springLayout.putConstraint(SpringLayout.EAST, component, -10, SpringLayout.EAST, container);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth) {
    addGridBagConstraints(componente, layout, gbc, gridX, gridY, gridWidth, gridHeigth, 4);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth, int marginTop) {
    addGridBagConstraints(componente, layout, gbc, gridX, gridY, gridWidth, gridHeigth, marginTop, false, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth, boolean hasGapAfter) {
    addGridBagConstraints(componente, layout, gbc, gridX, gridY, gridWidth, gridHeigth, 4, hasGapAfter, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth, int marginTop, boolean hasGapAfter) {
    addGridBagConstraints(componente, layout, gbc, gridX, gridY, gridWidth, gridHeigth, marginTop, hasGapAfter, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth, boolean hasGapAfter, int fill, int anchor) {
    addGridBagConstraints(componente, layout, gbc, gridX, gridY, gridWidth, gridHeigth, 4, hasGapAfter, fill, anchor);
  }

  public void addGridBagConstraints(Component componente, GridBagLayout layout, GridBagConstraints gbc, int gridX, int gridY, int gridWidth, int gridHeigth, int marginTop, boolean hasGapAfter, int fill, int anchor) {
    if(hasGapAfter)
      gbc.insets = new Insets(marginTop, 0, 0, 4);
    else
      gbc.insets = new Insets(marginTop, 0, 0, 0);
    gbc.gridx = gridX;
    gbc.gridy = gridY;
    gbc.gridwidth = gridWidth;
    gbc.gridheight = gridHeigth;

    gbc.weighty = 1;
    gbc.fill = fill;
    gbc.anchor = anchor;
    layout.setConstraints(componente, gbc);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals("connect")) {
      boolean isConnected = commands.connect();
      if(isConnected) {
        textLabel.setText("Connected to client!");
        textLabel.setForeground(Constants.COLOR_GREEN);
        connectBtn.setText("Connect again to Client");
      } else {
        textLabel.setText("Not connected to client!");
        textLabel.setForeground(Constants.COLOR_RED);
        connectBtn.setText("Connected to client!");
      }
      enableButtons(isConnected);
    } else if(e.getActionCommand().equals("createGame")) {
      if(!roles.getIdPref()[0].equals(roles.getIdPref()[1]))
        commands.createGame(roles.getIdPref()[0].getId(), roles.getIdPref()[1].getId());
    } else if(e.getActionCommand().equals("autoAccept")) {
      AbstractButton abstractButton = (AbstractButton) e.getSource();
      setAutoAccept(abstractButton.getModel().isSelected());
    } else if(e.getActionCommand().equals("dodge")) {
      commands.dodge();
    } else if(e.getActionCommand().equals("autoAcceptSwap")) {
      AbstractButton abstractButton = (AbstractButton) e.getSource();
      setAutoAcceptSwap(abstractButton.getModel().isSelected());
    } else if(e.getActionCommand().equals("autoChampSelect")) {
      AbstractButton abstractButton = (AbstractButton) e.getSource();
      setAutoChampSelect(abstractButton.getModel().isSelected());
    } else if(e.getActionCommand().equals("pickRandomSkin")) {
      commands.pickRandomSkin();
    } else if(e.getActionCommand().equals("revealSummoners")) {
      ArrayList<Participant> summoners = commands.getParticipants();
      String summonersStr = "";
      for(Participant summoner : summoners)
        summonersStr += summoner.getCompleteNameHash() + " joined the lobby\n";
      textArea.setText(summonersStr);
      System.out.println(openOPGG);
      openOPGG.setEnabled(true);
      openPoro.setEnabled(true);
      openUGG.setEnabled(true);
      openDeeplol.setEnabled(true);
    } else if(e.getActionCommand().equals("OPGG") || e.getActionCommand().equals("Poro") || e.getActionCommand().equals("UGG") || e.getActionCommand().equals("Deeplol")) {
      commands.openInBrowser(e.getActionCommand());
    } else {
      for(String r : Constants.ROLES_STR) {
        if(e.getActionCommand().equals(r)) {
          roles.toggleRole(r);
          primaryLabel.setText(roles.getIdPref()[0].getShortcut());
          secondaryLabel.setText(roles.getIdPref()[1].getShortcut());
          commands.setPrimaryRole(roles.getIdPref()[0].getId());
          commands.setSecondaryRole(roles.getIdPref()[0].getId());
        }
      }
    }
  }

  private void enableButtons(boolean isEnabled) {
    for(String r : Constants.ROLES_STR)
      roles.getRole(r).getButton().setEnabled(isEnabled);
    createGame.setEnabled(isEnabled);
    autoAccept.setEnabled(isEnabled);
    delay.setEnabled(isEnabled);
    dodge.setEnabled(isEnabled);
    autoChampSelect.setEnabled(isEnabled);
    autoAcceptSwap.setEnabled(isEnabled);
    pickRandomSkin.setEnabled(isEnabled);
    revealSummoners.setEnabled(isEnabled);
  }

  private void enableAutoChampSelect(boolean isEnabled) {
    banLabel.setEnabled(isEnabled);
    hoverLabel.setEnabled(isEnabled);
    pickLabel.setEnabled(isEnabled);

    banChampionsPrimary.setEnabled(isEnabled);
    banChampionsPrimary.setButtonsEnabled(isEnabled);
    hoverChampionsPrimary.setEnabled(isEnabled);
    hoverChampionsPrimary.setButtonsEnabled(isEnabled);
    pickChampionsPrimary.setEnabled(isEnabled);
    pickChampionsPrimary.setButtonsEnabled(isEnabled);

    banChampionsSecondary.setEnabled(isEnabled);
    banChampionsSecondary.setButtonsEnabled(isEnabled);
    hoverChampionsSecondary.setEnabled(isEnabled);
    hoverChampionsSecondary.setButtonsEnabled(isEnabled);
    pickChampionsSecondary.setEnabled(isEnabled);
    pickChampionsSecondary.setButtonsEnabled(isEnabled);
  }

  private void changeCreateGameAnimation(URL url) {
    ImageIcon icon = new ImageIcon(url);
    icon.setImage(icon.getImage().getScaledInstance(32, 24, Image.SCALE_DEFAULT));
    createGame.setIcon(icon);
  }

  private void setAutoAccept(boolean isSelected) {
    commands.setAutoAccept(isSelected);
    autoAccept.setSelected(isSelected);
    autoAccept.putClientProperty(FlatClientProperties.STYLE, isSelected ? buttonStylesSelected : buttonStylesUnselected);
  }

  private void setAutoAcceptSwap(boolean isSelected) {
    commands.setAutoAcceptSwap(isSelected);
    autoAcceptSwap.setSelected(isSelected);
    autoAcceptSwap.putClientProperty(FlatClientProperties.STYLE, isSelected ? buttonStylesSelected : buttonStylesUnselected);
  }

  private void setAutoChampSelect(boolean isSelected) {
    commands.setAutoChampSelect(isSelected);
    autoChampSelect.setSelected(isSelected);
    autoChampSelect.putClientProperty(FlatClientProperties.STYLE, isSelected ? buttonStylesSelected : buttonStylesUnselected);
    enableAutoChampSelect(isSelected);
  }

  @Override
  public void onCloseConnection() {
    setAutoAccept(false);
    setAutoAcceptSwap(false);
    setAutoChampSelect(false);
  }

  @Override
  public void onComboBoxChange(String key, ArrayList<Champion> champions) {
    switch(key) {
      case "primaryBan":
        commands.setBanChampionsPrimary(champions);
        break;
      case "primaryHover":
        commands.setHoverChampionsPrimary(champions);
        break;
      case "primaryPick":
        commands.setPickChampionsPrimary(champions);
        break;
      case "secondaryBan":
        commands.setBanChampionsSecondary(champions);
        break;
      case "secondaryHover":
        commands.setHoverChampionsSecondary(champions);
        break;
      case "secondaryPick":
        commands.setPickChampionsSecondary(champions);
        break;
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  @Override
  public void keyPressed(KeyEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {
    refreshDelay();
  }

  public void refreshDelay() {
    String delayStr = delay.getText();
    if(isNumberBetween(delayStr, 0, 10000)) {
      commands.setDelay(Integer.parseInt(delayStr));
      delay.setForeground(null);
    } else {
      delay.setForeground(Constants.COLOR_RED);
    }
  }

  public boolean isNumberBetween(String str, int min, int max) {
    try {
      int value = Integer.parseInt(str);
      return value >= min && value <= max;
    } catch(NumberFormatException e) {
      System.out.println(e);
      return false;
    }
  }
}
