package broesel;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Jan
 * 
 */
public class MainWindow extends JFrame implements ActionListener {

	private static final String COLOR_STRING_TARGET_ID_LOW_HP = "TargetID low HP";

	private static final String COLOR_STRING_TARGET_ID_BUFFED_HP = "TargetID buffed HP";

	private static final String COLOR_STRING_UBER_CHARGE_FULL1 = "Ubercharge Full 1";

	private static final String COLOR_STRING_UBER_CHARGE_FULL2 = "Ubercharge Full 2";

	private static final String COLOR_STRING_LOW_AMMO_WARNING1 = "Low-Ammo Warning 1";

	private static final String COLOR_STRING_LOW_AMMO_WARNING2 = "Low-Ammo Warning 2";

	private static final String COLOR_STRING_BUFFED_HP = "buffed HP";

	private static final String COLOR_STRING_LOW_HP = "low HP";

	private static final String COLOR_STRING_UBERCHARGE_BAR = "Ubercharge Bar";

	private static final String COLOR_STRING_DAMAGE_NUMBERS = "Damage Numbers";

	private static final String COLOR_STRING_AMMO_IN_RESERVE = "Ammo in Reserve";

	private static final String COLOR_STRING_AMMO_IN_CLIP = "Ammo in Clip";

	private static final String COLOR_STRING_HP = "HP";

	/**
	 * 
	 */
	private static final long serialVersionUID = -380227024232833244L;

	private static final String INSTALL_PATH_TXT = "installPath.txt";

	private static final String ACTION_COMMAND_ZIP_BUTTON = "zipbutton";

	private static final String ACTION_COMMAND_STEAM_BUTTON = "steambutton";

	private static final String ACTION_COMMAND_INSTALL_BUTTON = "installbutton";

	private static final String ACTION_COMMAND_MENU_CUSTOM = "menucustom";

	private static final String ACTION_COMMAND_MENU_STANDARD = "menustandard";

	private static final String ACTION_COMMAND_COLORED_BORDER_YES = "coloredborderyes";

	private static final String ACTION_COMMAND_COLORED_BORDER_NO = "coloredborderno";

	private static final String ACTION_COMMAND_INDICATOR_ON = "indicatoron";

	private static final String ACTION_COMMAND_INDICATOR_OFF = "indicatoroff";

	private static final String ACTION_COMMAND_AMMOFLASH_ON = "ammoflashon";

	private static final String ACTION_COMMAND_AMMOFLASH_OFF = "ammoflashoff";

	private static final String ACTION_COMMAND_NOTIFICATION_ON = "notificationon";

	private static final String ACTION_COMMAND_NOTIFICATION_OFF = "notificationoff";

	private static final String ACTION_COMMAND_ASPECT_DEFAULT = "aspectdefault";

	private static final String ACTION_COMMAND_ASPECT_43 = "aspect43";

	private static final String ACTION_COMMAND_ASPECT_54 = "aspect54";

	private static final String ACTION_COMMAND_OLDSCHOOL_ENABLED = "enableoldschool";

	private static final String ACTION_COMMAND_OLDSCHOOL_DISABLED = "disableoldschool";

	private static final String ACTION_COMMAND_HP_CROSS_ON = "hpcrosson";

	private static final String ACTION_COMMAND_HP_CROSS_OFF = "hpcrossoff";

	private static final List<String> OPTION_FILES = new ArrayList<String>();

	private static final String BACKUP_EXTENSION = ".bak";

	private File zipFile;
	private JTextField zipFileInput;
	private JTextField steamInput;
	private File installDir;

	private JPanel coloredBorderPanel;

	private ButtonGroup coloredBorderGroup;

	private Color colorLowHP;

	private Color colorBuffedHP;

	private ColorChooserPanel colorPanelLowHP;

	private ColorChooserPanel colorPanelBuffedHP;

	private ButtonGroup damageIndicatorGroup;

	private Color colorAmmoFlash1;

	private ColorChooserPanel colorPanelAmmoFlash1;

	private ColorChooserPanel colorPanelAmmoFlash2;

	private Color colorAmmoFlash2;

	private ButtonGroup notificationGroup;

	private Color colorHP;

	private ColorChooserPanel colorPanelHP;

	private Color colorAmmoInClip;

	private ColorChooserPanel colorPanelAmmoInClip;

	private Color colorAmmoInReserve;

	private ColorChooserPanel colorPanelAmmoInReserve;

	private Color colorDamageNumbers;

	private ColorChooserPanel colorPanelDamageNumbers;

	private Color colorUberChargeBar;

	private ColorChooserPanel colorPanelUberChargeBar;

	private Color colorUberChargeFull1;

	private ColorChooserPanel colorPanelUberChargeFull1;

	private Color colorUberChargeFull2;

	private ColorChooserPanel colorPanelUberChargeFull2;

	private Color colorTargetIDLow;

	private ColorChooserPanel colorPanelTargetIDLow;

	private Color colorTargetIDBuffed;

	private ColorChooserPanel colorPanelTargetIDBuffed;

	private boolean zipFileValid;

	private boolean installPathValid;

	private ButtonGroup aspectGroup;

	private ButtonGroup ammoFlashGroup;

	private ButtonGroup menuGroup;

	private ButtonGroup oldschoolTargetIDGroup;

	private ButtonGroup hpCrossGroup;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (final InstantiationException e) {
			e.printStackTrace();
		}
		catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (final UnsupportedLookAndFeelException e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
			catch (final ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			catch (final InstantiationException e1) {
				e1.printStackTrace();
			}
			catch (final IllegalAccessException e1) {
				e1.printStackTrace();
			}
			catch (final UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		new MainWindow();
	}

	/**
	 * 
	 */
	public MainWindow() {
		// Dateien, die beim Entpacken besonders behandelt werden m�ssen
		OPTION_FILES.add("resource/GameMenu.res");
		OPTION_FILES.add("resource/ui/MainMenuOverride.res");
		OPTION_FILES.add("resource/ui/SpectatorTournament.res");
		OPTION_FILES.add("resource/ui/SpectatorTournament_4x3.res");
		OPTION_FILES.add("resource/ui/SpectatorTournament_5x4.res");
		OPTION_FILES.add("resource/ui/HudPlayerHealth.res");
		OPTION_FILES.add("resource/ui/HudPlayerHealth_CROSS.res");
		OPTION_FILES.add("resource/ui/HudDamageAccount.res");
		OPTION_FILES.add("resource/ui/HudDamageAccount_CROSS.res");
		OPTION_FILES.add("resource/ui/HudDamageAccount_WITHOUT_LAST.res");
		OPTION_FILES.add("resource/ui/TargetID.res");
		OPTION_FILES.add("resource/ui/TargetID_OLDSCHOOL.res");
		OPTION_FILES.add("resource/ui/SpectatorGUIHealth.res");
		OPTION_FILES.add("resource/ui/SpectatorGUIHealth_OLDSCHOOL.res");
		OPTION_FILES.add("scripts/HudAnimations_tf.txt");
		OPTION_FILES.add("resource/ui/HudHealthAccount.res");
		OPTION_FILES.add("resource/ui/HudHealthAccount_WITH_NOTIFICATION.res");
		OPTION_FILES.add("resource/ClientScheme.res");
		OPTION_FILES.add("CLICK HERE FOR FREE HAT.txt");
		OPTION_FILES.add("PLEASE, FOR THE LOVE OF GOD, READ THIS.txt");
		OPTION_FILES.add("PLEASE, FOR THE LOVE OF GOD, READ THIS.html");
		OPTION_FILES.add("ReadMe.html");

		zipFileValid = false;
		installPathValid = false;

		// TODO: [X] 1.1 Dialoge zentrieren
		// TODO: [X] 1.1 Men�leiste mit "about" und "about broeselhud"
		setLayout(new BorderLayout());
		initMenuBar();
		initTopPanel();
		initCenterPanel();
		initBottomPanel();

		JOptionPane.showMessageDialog(this, "Please select the broeselhud zip-file.", "Welcome to the broeselhud installer", JOptionPane.INFORMATION_MESSAGE);
		selectZipFile();

		// Fenster vorbereiten und anzeigen
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("broeselhud Installer");
		pack();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// TODO: [X] 2.0 Fenster ist nicht h�her als die Aufl�sung
		if (getHeight() > (screenSize.height - 50)) {
			setSize(getWidth(), screenSize.height - 50);
		}
		setLocation(screenSize.width / 2 - getSize().width / 2, screenSize.height / 2 - getSize().height / 2);
		setVisible(true);
	}

	/**
	 * 
	 */
	private void initMenuBar() {
		final JMenuBar menuBar = new JMenuBar();
		final JMenu aboutMenu = new JMenu("About...");
		final JMenuItem instItem = new JMenuItem("...this installer");
		final JMenuItem hudItem = new JMenuItem("...broeselhud");
		aboutMenu.add(instItem);
		aboutMenu.add(hudItem);

		menuBar.add(aboutMenu);
		setJMenuBar(menuBar);

		instItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				String aboutText = "<html><h2>This is the broeselhud installer.</h2>";
				aboutText += "<p>You can easily install and customize the custom TF2 HUD <a href=\"http://code.google.com/p/broeselhud/\">broeselhud</a> (version 2.x) with it!<br>";
				aboutText += "Just select the correct zipFile (download it from the broeselhud website), choose your preferred options and click install!</p><br>";
				aboutText += "<p>It was written by <a href=\"http://steamcommunity.com/profiles/76561198010745013\">Baret</a></p>";
				aboutText += "<p>Please give feedback or suggestions on my Steam profile or via mail at <a href=\"mailto:Beret14@web.de\">Beret14@web.de</a></p>";
				aboutText += "</html>";
				final JEditorPane panel = new JEditorPane("text/html", aboutText);
				panel.setEditable(false);
				panel.setOpaque(false);
				panel.addHyperlinkListener(new MyHyperlinkListener());
				JOptionPane.showMessageDialog(menuBar.getParent(), panel, "About broeselhud installer", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		hudItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				String aboutText = "<html>";
				aboutText += "<p>broeselhud is a custom TF2 HUD made by <a href=\"http://steamcommunity.com/id/brsl\">broesel</a>, download it here: <a href=\"http://code.google.com/p/broeselhud/\">http://code.google.com/p/broeselhud/</a></p>";
				aboutText += "<p>...and install it with this installer :D</p>";
				aboutText += "</html>";
				final JEditorPane panel = new JEditorPane("text/html", aboutText);
				panel.setEditable(false);
				panel.setOpaque(false);
				panel.addHyperlinkListener(new MyHyperlinkListener());
				JOptionPane.showMessageDialog(menuBar.getParent(), panel, "About broeselhud", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	/**
	 * 
	 */
	private void initBottomPanel() {
		// TODO: [X] 1.1 Installbutton auff�lliger
		final JButton instButton = new JButton("<html><h3 color=#990000>Install broeselhud!</h3></html>");
		instButton.setActionCommand(ACTION_COMMAND_INSTALL_BUTTON);
		instButton.addActionListener(this);
		this.add(instButton, BorderLayout.SOUTH);
	}

	/**
	 * 
	 */
	private void initCenterPanel() {
		final JPanel centerPanel = new JPanel();
		final BoxLayout layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
		centerPanel.setLayout(layout);

		// TODO: [X] 1.1 Tooltips
		final FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);

		// Panel f�r Custom Main Menu
		final JPanel menuPanel = getBorderedPanel("Custom main menu");
		menuPanel.setLayout(flowLayout);
		menuPanel.setToolTipText("You can disable the custom main menu if you don't like it");
		menuGroup = new ButtonGroup();
		final JRadioButton rbMenuCustom = new JRadioButton("Enable custom menu", true);
		rbMenuCustom.setActionCommand(ACTION_COMMAND_MENU_CUSTOM);
		final JRadioButton rbMenuStandard = new JRadioButton("Disable custom menu");
		rbMenuStandard.setActionCommand(ACTION_COMMAND_MENU_STANDARD);
		menuGroup.add(rbMenuCustom);
		menuGroup.add(rbMenuStandard);
		menuPanel.add(rbMenuCustom);
		menuPanel.add(rbMenuStandard);

		// Panel f�r Screen aspect ratio
		final JPanel aspectPanel = getBorderedPanel("Aspect ratio of your screen");
		aspectPanel.setLayout(flowLayout);
		aspectPanel.setToolTipText("Select your screen aspect ratio");
		aspectGroup = new ButtonGroup();
		final JRadioButton rbAspecta1610 = new JRadioButton("Widescreen", true);
		rbAspecta1610.setActionCommand(ACTION_COMMAND_ASPECT_DEFAULT);
		final JRadioButton rbAspectb43 = new JRadioButton("4:3");
		rbAspectb43.setActionCommand(ACTION_COMMAND_ASPECT_43);
		final JRadioButton rbAspectc54 = new JRadioButton("5:4");
		rbAspectc54.setActionCommand(ACTION_COMMAND_ASPECT_54);
		aspectGroup.add(rbAspecta1610);
		aspectGroup.add(rbAspectb43);
		aspectGroup.add(rbAspectc54);
		aspectPanel.add(rbAspecta1610);
		aspectPanel.add(rbAspectb43);
		aspectPanel.add(rbAspectc54);

		// Panel f�r Oldschool TargetIDs
		final JPanel oldschoolTargetIDPanel = getBorderedPanel("Oldschool TargetIDs");
		oldschoolTargetIDPanel.setLayout(flowLayout);
		oldschoolTargetIDPanel.setToolTipText("Back to the roots? Enable the \"oldschool TargetIDs\" from previous broeselhud versions!");
		oldschoolTargetIDGroup = new ButtonGroup();
		final JRadioButton rbOldschoolEnabled = new JRadioButton("Enable oldschool TargetIDs");
		rbOldschoolEnabled.setActionCommand(ACTION_COMMAND_OLDSCHOOL_ENABLED);
		final JRadioButton rbOldschoolDisabled = new JRadioButton("Disable oldschool TargetIDs", true);
		rbOldschoolDisabled.setActionCommand(ACTION_COMMAND_OLDSCHOOL_DISABLED);
		oldschoolTargetIDGroup.add(rbOldschoolEnabled);
		oldschoolTargetIDGroup.add(rbOldschoolDisabled);
		oldschoolTargetIDPanel.add(rbOldschoolEnabled);
		oldschoolTargetIDPanel.add(rbOldschoolDisabled);

		// Panel f�r HP Cross
		final JPanel hpCrossPanel = getBorderedPanel("HP Cross");
		hpCrossPanel.setLayout(flowLayout);
		hpCrossPanel.setToolTipText("You can enable or disable the HP Cross for \"normal\" health (not buffed and not low)");
		hpCrossGroup = new ButtonGroup();
		final JRadioButton rbHPCrossOn = new JRadioButton("Enable HP cross");
		rbHPCrossOn.setActionCommand(ACTION_COMMAND_HP_CROSS_ON);
		rbHPCrossOn.addActionListener(this);
		final JRadioButton rbHPCrossOff = new JRadioButton("Disable HP cross", true);
		rbHPCrossOff.setActionCommand(ACTION_COMMAND_HP_CROSS_OFF);
		rbHPCrossOff.addActionListener(this);
		hpCrossGroup.add(rbHPCrossOn);
		hpCrossGroup.add(rbHPCrossOff);
		hpCrossPanel.add(rbHPCrossOn);
		hpCrossPanel.add(rbHPCrossOff);

		// Panel f�r Colored Border um HP [HP Cross ON]
		coloredBorderPanel = getBorderedPanel("Do you want your HP-display to have a team-colored border?");
		coloredBorderPanel.setLayout(flowLayout);
		coloredBorderPanel
				.setToolTipText("If you choose this option you will have a colored border around your HP-cross. The color is depending on your team-color");
		coloredBorderGroup = new ButtonGroup();
		final JRadioButton rbColoredBorderYes = new JRadioButton("Yes!");
		rbColoredBorderYes.setActionCommand(ACTION_COMMAND_COLORED_BORDER_YES);
		final JRadioButton rbColoredBorderNo = new JRadioButton("No!", true);
		rbColoredBorderNo.setActionCommand(ACTION_COMMAND_COLORED_BORDER_NO);
		coloredBorderGroup.add(rbColoredBorderYes);
		coloredBorderGroup.add(rbColoredBorderNo);
		coloredBorderPanel.add(rbColoredBorderYes);
		coloredBorderPanel.add(rbColoredBorderNo);
		coloredBorderPanel.setVisible(false);

		// Panel f�r Last Damage Indicator
		final JPanel damageIndicatorPanel = getBorderedPanel("Last damage indicator");
		damageIndicatorPanel.setLayout(flowLayout);
		damageIndicatorPanel.setToolTipText("The damage indicator shows the amount of damage done by the last hit");
		damageIndicatorGroup = new ButtonGroup();
		final JRadioButton rbIndicatorOn = new JRadioButton("Turn the damage indicator ON", true);
		rbIndicatorOn.setActionCommand(ACTION_COMMAND_INDICATOR_ON);
		final JRadioButton rbIndicatorOff = new JRadioButton("Turn the damage indicator OFF");
		rbIndicatorOff.setActionCommand(ACTION_COMMAND_INDICATOR_OFF);
		damageIndicatorGroup.add(rbIndicatorOn);
		damageIndicatorGroup.add(rbIndicatorOff);
		damageIndicatorPanel.add(rbIndicatorOn);
		damageIndicatorPanel.add(rbIndicatorOff);

		// Panel f�r Medpack/Sandvich PickUp-notification
		final JPanel notificationPanel = getBorderedPanel("Medpack/Sandvich Pickup-Notification");
		notificationPanel.setLayout(flowLayout);
		notificationPanel.setToolTipText("The notification shows the amount of health gained from a healthpack or sandvich you picked up");
		notificationGroup = new ButtonGroup();
		final JRadioButton rbnotificationOn = new JRadioButton("Enable");
		rbnotificationOn.setActionCommand(ACTION_COMMAND_NOTIFICATION_ON);
		final JRadioButton rbnotificationOff = new JRadioButton("Disable", true);
		rbnotificationOff.setActionCommand(ACTION_COMMAND_NOTIFICATION_OFF);
		notificationGroup.add(rbnotificationOn);
		notificationGroup.add(rbnotificationOff);
		notificationPanel.add(rbnotificationOn);
		notificationPanel.add(rbnotificationOff);

		// Panel f�r Low Ammo Flash
		final JPanel ammoFlashPanel = getBorderedPanel("Low ammo flash");
		ammoFlashPanel.setLayout(new GridLayout(3, 1));
		ammoFlashPanel.setToolTipText("If enabled, your ammo will begin flashing in these two colors when you're running low on ammo");
		final JPanel ammoFlashRadioPanel = new JPanel(flowLayout);
		ammoFlashGroup = new ButtonGroup();
		final JRadioButton rbAmmoFlashOn = new JRadioButton("Enable blinking ammo at low ammo");
		rbAmmoFlashOn.setActionCommand(ACTION_COMMAND_AMMOFLASH_ON);
		final JRadioButton rbAmmoFlashOff = new JRadioButton("Disable blinking ammo at low ammo", true);
		rbAmmoFlashOff.setActionCommand(ACTION_COMMAND_AMMOFLASH_OFF);
		ammoFlashGroup.add(rbAmmoFlashOn);
		ammoFlashGroup.add(rbAmmoFlashOff);
		ammoFlashRadioPanel.add(rbAmmoFlashOn);
		ammoFlashRadioPanel.add(rbAmmoFlashOff);
		colorPanelAmmoFlash1 = new ColorChooserPanel("Low ammo flash color 1", colorAmmoFlash1, this);
		colorPanelAmmoFlash2 = new ColorChooserPanel("Low ammo flash color 2", colorAmmoFlash2, this);
		ammoFlashPanel.add(ammoFlashRadioPanel);
		ammoFlashPanel.add(colorPanelAmmoFlash1);
		ammoFlashPanel.add(colorPanelAmmoFlash2);
		// Panel f�r Colors
		final JPanel colorPanel = getBorderedPanel("Color customization");
		colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
		colorPanelHP = new ColorChooserPanel("HP display", colorHP, this);
		colorPanelHP.setToolTipText("The color of the value representing your current health (not the cross)");
		colorPanelLowHP = new ColorChooserPanel("Low HP color", colorLowHP, this);
		colorPanelLowHP.setToolTipText("The color of your HP display when low on health");
		colorPanelBuffedHP = new ColorChooserPanel("Buffed HP color", colorBuffedHP, this);
		colorPanelBuffedHP.setToolTipText("The color of your HP display when your health is buffed");
		colorPanelAmmoInClip = new ColorChooserPanel("Ammo in clip", colorAmmoInClip, this);
		colorPanelAmmoInClip.setToolTipText("The color of your ammo left in the clip");
		colorPanelAmmoInReserve = new ColorChooserPanel("Ammo in reserve", colorAmmoInReserve, this);
		colorPanelAmmoInReserve.setToolTipText("The color of your ammo you have in reserve");
		colorPanelDamageNumbers = new ColorChooserPanel("Damage numbers", colorDamageNumbers, this);
		colorPanelDamageNumbers
				.setToolTipText("The color of the numbers shown by the damage indicator. Only takes effect if you enable the last damage indicator");
		colorPanelUberChargeBar = new ColorChooserPanel("Ubercharge bar", colorUberChargeBar, this);
		colorPanelUberChargeBar.setToolTipText("Your ubercharge bar will be filled with this color");
		colorPanelUberChargeFull1 = new ColorChooserPanel("Ubercharge bar full 1", colorUberChargeFull1, this);
		colorPanelUberChargeFull1.setToolTipText("When you are charged as medic your ubercharge bar will pulsate between these two colors");
		colorPanelUberChargeFull2 = new ColorChooserPanel("Ubercharge bar full 2", colorUberChargeFull2, this);
		colorPanelUberChargeFull2.setToolTipText("When you are charged as medic your ubercharge bar will pulsate between these two colors");
		colorPanelTargetIDLow = new ColorChooserPanel("TargetID low HP", colorTargetIDLow, this);
		colorPanelTargetIDLow.setToolTipText("The color of the health display in target IDs when the target has low HP");
		colorPanelTargetIDBuffed = new ColorChooserPanel("TargetID buffed HP", colorTargetIDBuffed, this);
		colorPanelTargetIDBuffed.setToolTipText("The color of the health display in target IDs when the target has buffed HP");
		colorPanel.add(colorPanelHP);
		colorPanel.add(colorPanelLowHP);
		colorPanel.add(colorPanelBuffedHP);
		colorPanel.add(colorPanelAmmoInClip);
		colorPanel.add(colorPanelAmmoInReserve);
		colorPanel.add(colorPanelDamageNumbers);
		colorPanel.add(colorPanelUberChargeBar);
		colorPanel.add(colorPanelUberChargeFull1);
		colorPanel.add(colorPanelUberChargeFull2);
		colorPanel.add(colorPanelTargetIDLow);
		colorPanel.add(colorPanelTargetIDBuffed);

		// Panels zu centerPanel hinuzf�gen
		centerPanel.add(menuPanel);
		centerPanel.add(aspectPanel);
		centerPanel.add(oldschoolTargetIDPanel);
		centerPanel.add(hpCrossPanel);
		centerPanel.add(coloredBorderPanel);
		centerPanel.add(damageIndicatorPanel);
		centerPanel.add(notificationPanel);
		centerPanel.add(ammoFlashPanel);
		centerPanel.add(colorPanel);

		// centerPanel zur GUI hinzuf�gen
		this.add(new JScrollPane(centerPanel), BorderLayout.CENTER);
	}

	/**
	 * 
	 */
	private void initTopPanel() {
		final JPanel topPanel = new JPanel(new GridLayout(2, 1));
		final JPanel zipInputPanel = getBorderedPanel("Path to broeselhud .zip-File");
		final BoxLayout zipInputLayout = new BoxLayout(zipInputPanel, BoxLayout.LINE_AXIS);
		zipInputPanel.setLayout(zipInputLayout);

		final JPanel steamInputPanel = getBorderedPanel("Path to Steam\\-Folder");
		final BoxLayout steamInputLayout = new BoxLayout(steamInputPanel, BoxLayout.LINE_AXIS);
		steamInputPanel.setLayout(steamInputLayout);

		zipInputPanel
				.setToolTipText("Enter the path to the .zip-file of the broeselhud you want to install. You can download it from http://code.google.com/p/broeselhud/");
		steamInputPanel.setToolTipText("Enter the path to your Steam-folder here. You will be asked for witch user the HUD should be installed.");

		// zipInputPanel f�llen
		zipFileInput = new JTextField();
		fillInputPanel(zipInputPanel, ACTION_COMMAND_ZIP_BUTTON, zipFileInput);

		// steamInputPanel f�llen
		steamInput = new JTextField();
		fillInputPanel(steamInputPanel, ACTION_COMMAND_STEAM_BUTTON, steamInput);
		try {
			String loadedInstallPath = loadInstallPath();
			if (loadedInstallPath != null) {
				steamInput.setText(loadedInstallPath);
				installDir = new File(loadedInstallPath);
				if (installDir.isDirectory() && installDir.exists()) {
					installPathValid = true;
				}
			}
			loadedInstallPath = loadZipFilePath();
			if (loadedInstallPath != null) {
				zipFileInput.setText(loadedInstallPath);
				zipFile = new File(loadedInstallPath);
				if (zipFile.exists()) {
					initColors(zipFile);
					zipFileValid = true;
				}
			}
		}
		catch (final IOException e) {
			showErrorDialog(e.getMessage(), "Could not load installpath");
			e.printStackTrace();
		}

		// topPanel f�llen
		topPanel.add(zipInputPanel);
		topPanel.add(steamInputPanel);

		// topPanel in GUI aufnehmen
		this.add(topPanel, BorderLayout.NORTH);
	}

	/**
	 * @param inputPanel
	 * @param actionCommand
	 * @param textField
	 */
	private void fillInputPanel(final JPanel inputPanel, final String actionCommand, final JTextField textField) {
		textField.setEditable(false);
		final JButton steamButton = new JButton("Search...");
		steamButton.setActionCommand(actionCommand);
		steamButton.addActionListener(this);
		inputPanel.add(textField);
		inputPanel.add(steamButton);
	}

	/**
	 * @param string
	 * @return
	 */
	private JPanel getBorderedPanel(final String title) {
		final JPanel ret = new JPanel();
		ret.setBorder(BorderFactory.createTitledBorder(title));
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent arg0) {
		// Install-Button wurde geklickt
		if (arg0.getActionCommand().equals(ACTION_COMMAND_INSTALL_BUTTON)) {
			installHUD();
		}

		// Buttton zur Auswahl des Steamordners wurde geklickt
		if (arg0.getActionCommand().equals(ACTION_COMMAND_STEAM_BUTTON)) {
			selectSteamLocation();
		}

		// Buttton zur Auswahl des zip-Files wurde geklickt
		if (arg0.getActionCommand().equals(ACTION_COMMAND_ZIP_BUTTON)) {
			selectZipFile();
		}

		// HP Cross enabled wurde ausgew�hlt
		if (arg0.getActionCommand().equals(ACTION_COMMAND_HP_CROSS_ON)) {
			coloredBorderPanel.setVisible(true);
		}

		// HP Cross disabled wurde ausgew�hlt
		if (arg0.getActionCommand().equals(ACTION_COMMAND_HP_CROSS_OFF)) {
			coloredBorderPanel.setVisible(false);
		}
	}

	/**
	 * 
	 */
	private void installHUD() {
		// TODO: [X] 1.1 "Exit after install?"
		if (zipFileValid && installPathValid) {
			final Set<String> backuppedFiles = new TreeSet<String>();
			// Zipfile durchgehen (entries)
			int unzippedFiles = 0;
			try {
				final ZipFile zip = new ZipFile(zipFile);
				for (final Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
					// ist Entry in optionsListe?
					final ZipEntry entry = e.nextElement();
					if (OPTION_FILES.contains(entry.getName())) {
						// ja => schauen welche Option gew�hlt ist, entsprechend behandeln

						// Custom Main Menu
						// (Die 2 Dateien werden bei Custom menu entpackt, sonst nicht)
						if (entry.getName().equalsIgnoreCase("resource/GameMenu.res")
								&& menuGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_MENU_CUSTOM)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/MainMenuOverride.res")
								&& menuGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_MENU_CUSTOM)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/GameMenu.res")
								&& menuGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_MENU_STANDARD)) {
							final File resFile = new File(installDir, entry.getName());
							if (resFile.exists()) {
								if (!resFile.delete()) {
									System.out.println(resFile.toString() + " could not be deleted!");
								}
							}
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/MainMenuOverride.res")
								&& menuGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_MENU_STANDARD)) {
							final File resFile = new File(installDir, entry.getName());
							if (resFile.exists()) {
								if (!resFile.delete()) {
									System.out.println(resFile.toString() + " could not be deleted!");
								}
							}
						}
						// Aspect Ratio
						if (entry.getName().equalsIgnoreCase("resource/ui/SpectatorTournament.res")
								&& aspectGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_ASPECT_DEFAULT)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/SpectatorTournament_4x3.res")
								&& aspectGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_ASPECT_43)) {
							unzip(zip, entry, "resource/ui/SpectatorTournament.res", backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/SpectatorTournament_5x4.res")
								&& aspectGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_ASPECT_54)) {
							unzip(zip, entry, "resource/ui/SpectatorTournament.res", backuppedFiles);
						}
						// Oldschool TargetIDs
						if (entry.getName().equalsIgnoreCase("resource/ui/TargetID.res")
								&& oldschoolTargetIDGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_OLDSCHOOL_DISABLED)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/SpectatorGUIHealth.res")
								&& oldschoolTargetIDGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_OLDSCHOOL_DISABLED)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/TargetID_OLDSCHOOL.res")
								&& oldschoolTargetIDGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_OLDSCHOOL_ENABLED)) {
							unzip(zip, entry, "resource/ui/TargetID.res", backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/SpectatorGUIHealth_OLDSCHOOL.res")
								&& oldschoolTargetIDGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_OLDSCHOOL_ENABLED)) {
							unzip(zip, entry, "resource/ui/SpectatorGUIHealth.res", backuppedFiles);
						}
						// HP Cross
						if (entry.getName().equalsIgnoreCase("resource/ui/HudPlayerHealth.res")
								&& hpCrossGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_HP_CROSS_OFF)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/HudDamageAccount.res")
								&& hpCrossGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_HP_CROSS_OFF)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/HudPlayerHealth_CROSS.res")
								&& hpCrossGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_HP_CROSS_ON)) {
							unzip(zip, entry, "resource/ui/HudPlayerHealth.res", backuppedFiles);
							// Teamcolored Border setzen, wenn enabled
							if (coloredBorderGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_COLORED_BORDER_YES)) {
								enableColoredBorder(zip, entry);
							}
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/HudDamageAccount_CROSS.res")
								&& hpCrossGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_HP_CROSS_ON)) {
							unzip(zip, entry, "resource/ui/HudDamageAccount.res", backuppedFiles);
						}
						// Damage indicator
						if (entry.getName().equalsIgnoreCase("resource/ui/HudDamageAccount.res")
								&& damageIndicatorGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_INDICATOR_ON)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/HudDamageAccount_WITHOUT_LAST.res")
								&& damageIndicatorGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_INDICATOR_OFF)) {
							unzip(zip, entry, "resource/ui/HudDamageAccount.res", backuppedFiles);
						}
						// Medpack pickup notification
						if (entry.getName().equalsIgnoreCase("resource/ui/HudHealthAccount.res")
								&& notificationGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_NOTIFICATION_OFF)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("resource/ui/HudHealthAccount_WITH_NOTIFICATION.res")
								&& notificationGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_NOTIFICATION_ON)) {
							unzip(zip, entry, "resource/ui/HudHealthAccount.res", backuppedFiles);
						}
						// Blinking ammo
						if (entry.getName().equalsIgnoreCase("scripts/HudAnimations_tf.txt")
								&& ammoFlashGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_AMMOFLASH_OFF)) {
							unzip(zip, entry, entry.getName(), backuppedFiles);
						}
						if (entry.getName().equalsIgnoreCase("scripts/HudAnimations_tf.txt")
								&& ammoFlashGroup.getSelection().getActionCommand().equals(ACTION_COMMAND_AMMOFLASH_ON)) {
							unzipHundAnimations(zip, entry, backuppedFiles);
						}
						// Colors
						if (entry.getName().equalsIgnoreCase("resource/ClientScheme.res")) {
							unzipClientScheme(zip, entry, backuppedFiles);
						}
					}
					else {
						// keine sonderbehandlung => entpacken
						unzip(zip, entry, entry.getName(), backuppedFiles);
					}
					if (!entry.isDirectory()) {
						unzippedFiles++;
					}
				}
				System.out.println("\n\nBackupped Files:");
				for (final String string : backuppedFiles) {
					System.out.println(string);
				}
				System.out.println("\nUnzipped files total: " + unzippedFiles);
				System.out.println("Backupped files total: " + backuppedFiles.size());
				System.out.println("\n- - - - SUCCESSFULLY INSTALLED! - - - -");
				final String msg = "<html>broeselhud <b>scuccessfully installed!</b><br>" + unzippedFiles + " files were extracted<br>" + backuppedFiles.size()
						+ " files were backupped (renamed to " + BACKUP_EXTENSION + "<br><br><b>Exit installer now?</b></html>";
				final int awnser = JOptionPane.showConfirmDialog(this, msg, "Success", JOptionPane.YES_NO_OPTION);
				if (awnser == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
			catch (final ZipException e) {
				System.out.println("Unzipped Files so far: " + unzippedFiles);
				showErrorDialog("Could not install broeselhud! Something went wrong, sorry :(", "Error");
				e.printStackTrace();
			}
			catch (final IOException e) {
				System.out.println("Unzipped Files so far: " + unzippedFiles);
				showErrorDialog("Could not install broeselhud! Something went wrong, sorry :(", "Error");
				e.printStackTrace();
			}
		}
		else {
			showErrorDialog("Can't install broeselhud! Installpath or selected zip-file not valid!", "Can't install");
		}
	}

	private void enableColoredBorder(final ZipFile zip, final ZipEntry entry) {
		// Diese Zeile muss ge�ndert werden:
		// "visible" "0"
		try {
			boolean visibleIsSet = false;
			final BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));

			final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(installDir, "resource/ui/HudPlayerHealth.res"), false));

			while (reader.ready()) {
				String line = reader.readLine();

				if (!visibleIsSet && line.length() > 0) {
					final StringTokenizer tokenizer = new StringTokenizer(line, "\"", false);
					if (tokenizer.nextToken().equalsIgnoreCase("visible")) {
						line = line.replace('0', '1');
						visibleIsSet = true;
					}
				}

				writer.write(line);
				writer.newLine();
				writer.flush();
			}
			reader.close();
			writer.close();
		}
		catch (final FileNotFoundException e) {
			System.out.println("Setting team colored border failed!");
			e.printStackTrace();
		}
		catch (final IOException e) {
			System.out.println("Setting team colored border failed!");
			e.printStackTrace();
		}
	}

	/**
	 * @param zip
	 * @param entry
	 * @param backuppedFiles
	 * @throws IOException
	 */
	private void unzipClientScheme(final ZipFile zip, final ZipEntry entry, final Set<String> backuppedFiles) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
		File outputFile = new File(installDir, entry.getName());
		if (outputFile.exists()) {
			// .bak erstellen
			backuppedFiles.add(backup(outputFile));

			// Weil outputfile umbenannt wurde, neu instanziieren
			outputFile = new File(installDir, entry.getName());
		}
		else {
			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
		}
		final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));
		while (reader.ready()) {
			String line = reader.readLine();

			if (line.matches(getLineRegex(COLOR_STRING_HP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelHP));
			}

			if (line.matches(getLineRegex(COLOR_STRING_AMMO_IN_CLIP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelAmmoInClip));
			}

			if (line.matches(getLineRegex(COLOR_STRING_AMMO_IN_RESERVE))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelAmmoInReserve));
			}

			if (line.matches(getLineRegex(COLOR_STRING_DAMAGE_NUMBERS))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelDamageNumbers));
			}

			if (line.matches(getLineRegex(COLOR_STRING_UBERCHARGE_BAR))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelUberChargeBar));
			}

			if (line.matches(getLineRegex(COLOR_STRING_LOW_HP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelLowHP));
			}

			if (line.matches(getLineRegex(COLOR_STRING_BUFFED_HP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelBuffedHP));
			}

			if (line.matches(getLineRegex(COLOR_STRING_LOW_AMMO_WARNING1))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelAmmoFlash1));
			}

			if (line.matches(getLineRegex(COLOR_STRING_LOW_AMMO_WARNING2))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelAmmoFlash2));
			}

			if (line.matches(getLineRegex(COLOR_STRING_UBER_CHARGE_FULL1))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelUberChargeFull1));
			}

			if (line.matches(getLineRegex(COLOR_STRING_UBER_CHARGE_FULL2))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelUberChargeFull2));
			}

			if (line.matches(getLineRegex(COLOR_STRING_TARGET_ID_LOW_HP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelTargetIDLow));
			}

			if (line.matches(getLineRegex(COLOR_STRING_TARGET_ID_BUFFED_HP))) {
				line = line.replaceFirst(getColorStringRegex(), getColorString(colorPanelTargetIDBuffed));
			}

			writer.write(line);
			writer.newLine();
			writer.flush();
		}
		reader.close();
		writer.close();
	}

	/**
	 * @return
	 */
	private String getColorStringRegex() {
		return "\"(\\d{1,3} ){3}\\d{1,3}\"";
	}

	/**
	 * @param colorName
	 * @return
	 */
	private String getLineRegex(final String colorName) {
		return "\\s*\"" + colorName + "\"\\s*\"(\\d{1,3} ){3}\\d{1,3}\".*";
	}

	/**
	 * @return
	 */
	private String getColorString(final ColorChooserPanel colorPanel) {
		return "\"" + colorPanel.getColor().getRed() + " " + colorPanel.getColor().getGreen() + " " + colorPanel.getColor().getBlue() + " "
				+ colorPanel.getColor().getAlpha() + "\"";
	}

	/**
	 * @param zip
	 * @param entry
	 * @param backuppedFiles
	 * @throws IOException
	 */
	private void unzipHundAnimations(final ZipFile zip, final ZipEntry entry, final Set<String> backuppedFiles) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
		boolean lineChanged = false;
		File outputFile = new File(installDir, entry.getName());
		if (outputFile.exists()) {
			// .bak erstellen
			backuppedFiles.add(backup(outputFile));

			// Weil outputfile umbenannt wurde, neu instanziieren
			outputFile = new File(installDir, entry.getName());
		}
		else {
			if (!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
		}
		final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		while (reader.ready()) {
			String line = reader.readLine();
			if (!lineChanged) {
				if (line.matches("^/{2}[^/]*")) {
					line = line.replace("//", "");
					lineChanged = true;
				}
			}

			writer.write(line);
			writer.newLine();
			writer.flush();
		}
		reader.close();
		writer.close();
	}

	/**
	 * @param zip
	 * @param entry
	 * @param backuppedFiles
	 * @param name
	 * @throws IOException
	 */
	private void unzip(final ZipFile zip, final ZipEntry entry, final String targetFilename, final Set<String> backuppedFiles) throws IOException {
		String backupFileName = null;
		final BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
		File outputFile = new File(installDir, targetFilename);
		if (!entry.isDirectory()) {
			if (outputFile.exists()) {
				// .bak erstellen
				backupFileName = backup(outputFile);

				// Weil outputfile umbenannt wurde, neu instanziieren
				outputFile = new File(installDir, targetFilename);
			}
			else {
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
			}
			// entpacken
			writeZippedFile(is, outputFile);
		}
		else {
			// Ordner, einfach erstellen
			if (!outputFile.exists()) {
				if (!outputFile.mkdirs()) {
					showErrorDialog("Could not create directory: " + outputFile.getAbsolutePath(), "Can't create directory");
				}
			}
		}
		is.close();
		if (backupFileName != null) {
			backuppedFiles.add(backupFileName);
		}
	}

	/**
	 * @param outputFile
	 * @return
	 */
	private String backup(final File outputFile) {
		String backupFileName;
		backupFileName = "";
		final File backupFile = new File(outputFile.getAbsolutePath() + BACKUP_EXTENSION);
		if (backupFile.exists()) {
			if (!backupFile.delete()) {
				backupFileName = "FAILED: ";
			}
			else {
				if (!outputFile.renameTo(backupFile)) {
					backupFileName = "FAILED: ";
				}
			}
		}
		else {
			if (!outputFile.renameTo(backupFile)) {
				backupFileName = "FAILED: ";
			}
		}
		backupFileName += outputFile.getAbsolutePath();
		if (!backupFileName.contains(BACKUP_EXTENSION)) {
			backupFileName += BACKUP_EXTENSION;
		}
		return backupFileName;
	}

	/**
	 * @param is
	 * @param file
	 * @throws IOException
	 */
	private void writeZippedFile(final BufferedInputStream is, final File targetFile) throws IOException {
		final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile, false));
		int len = 0;
		final byte[] buffer = new byte[1024];
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		os.close();
	}

	/**
	 * 
	 */
	private void selectSteamLocation() {
		installPathValid = false;
		File steamFolder = new File("");
		if (installDir != null && installDir.exists()) {
			steamFolder = installDir.getParentFile().getParentFile().getParentFile().getParentFile();
		}
		final JFileChooser chooser = new JFileChooser(steamFolder);
		chooser.setDialogTitle("Select Steam\\ folder");
		chooser.setToolTipText("Please select you Steam\\ folder! Not any subfolders of it.");
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		final int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File steamappsFolder = new File(chooser.getSelectedFile(), "SteamApps");
			if (!steamappsFolder.exists()) {
				showErrorDialog("Invalid path to ...\\Steam\\SteamApps\\: " + steamappsFolder.getAbsolutePath(), "No SteamApps\\ Folder");
			}
			else if (!steamappsFolder.isDirectory()) {
				showErrorDialog("The entered path is not a folder: " + steamappsFolder.getAbsolutePath(), "This is not a Folder");
			}
			else {
				// Steam-User ausw�hlen lassen
				// DropDown erstellen
				final JComboBox dropDown = new JComboBox();
				final File[] userFolders = steamappsFolder.listFiles();
				for (int i = 0; i < userFolders.length; i++) {
					if (userFolders[i].isDirectory() && !userFolders[i].getName().equalsIgnoreCase("common")
							&& !userFolders[i].getName().equalsIgnoreCase("sourcemods")) {
						// �berpr�fen, ob in dem User-Ordner ein tf2 Ordner
						// vorhanden ist
						final Collection<String> gameFolders = Arrays.asList(userFolders[i].list());
						if (gameFolders.contains("team fortress 2")) {
							dropDown.addItem(userFolders[i].getName());
						}
					}
				}

				// �berpr�fen ob dropdown elemente hat und dialog anzeigen
				if (dropDown.getItemCount() > 0) {
					final JPanel dialogPanel = new JPanel();
					dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
					dialogPanel.add(new JLabel("Please choose for which user you want to install the HUD"));
					dialogPanel.add(dropDown);
					JOptionPane.showMessageDialog(this, dialogPanel, "Select user", JOptionPane.QUESTION_MESSAGE);
				}
				else {
					showErrorDialog("No users have TF2 installed!", "No TF2 found");
					return;
				}

				installDir = new File(steamappsFolder, dropDown.getSelectedItem() + File.separator + "team fortress 2" + File.separator + "tf");
				if (installDir.isDirectory() && installDir.exists()) {
					installPathValid = true;
					steamInput.setText(installDir.getAbsolutePath());
					try {
						String zipFilePath = "";
						if (zipFile != null && zipFileValid) {
							zipFilePath = zipFile.getAbsolutePath();
						}
						saveInstallPath(installDir.getAbsolutePath(), zipFilePath);
					}
					catch (final IOException e1) {
						showErrorDialog(e1.getMessage(), "Could not save installpath");
						e1.printStackTrace();
					}
				}
				else {
					showErrorDialog("This is not a valid install location for broeselhud", "No valid installpath");
				}
			}
		}
	}

	/**
	 * @param message
	 * @param title
	 */
	private void showErrorDialog(final String message, final String title) {
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private String loadInstallPath() throws IOException {
		final File installPathFile = new File(INSTALL_PATH_TXT);
		String ret = "";
		if (installPathFile.exists()) {
			final BufferedReader br = new BufferedReader(new FileReader(installPathFile));
			ret = br.readLine();
			br.close();
		}
		return ret;

	}

	private String loadZipFilePath() throws IOException {
		final File installPathFile = new File(INSTALL_PATH_TXT);
		String ret = "";
		if (installPathFile.exists()) {
			final BufferedReader br = new BufferedReader(new FileReader(installPathFile));
			br.readLine();
			ret = br.readLine();
			br.close();
		}
		return ret;

	}

	/**
	 * @param absolutePath
	 * @throws IOException
	 */
	private void saveInstallPath(final String installPath, final String zipFilePath) throws IOException {
		final FileWriter fw = new FileWriter(INSTALL_PATH_TXT, false);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(installPath);
		bw.newLine();
		bw.write(zipFilePath);
		bw.flush();
		bw.close();
	}

	/**
	 * 
	 */
	private void selectZipFile() {
		// TODO: [X] 1.1 Auch letzten zipfile ordner merken
		// TODO: [X] bei Ordnerselektion aktuellen Pfad w�hlen
		zipFileValid = false;
		final JFileChooser chooser = new JFileChooser(zipFile);
		chooser.setDialogTitle("Select valid .zip file");
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final FileNameExtensionFilter filter = new FileNameExtensionFilter(".zip Files", "zip");
		chooser.setFileFilter(filter);

		final int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Farben initialisieren
			try {
				zipFileInput.setText("");
				zipFile = chooser.getSelectedFile();
				initColors(zipFile);
				zipFileInput.setText(zipFile.getAbsolutePath());
				zipFileValid = true;
				// Installpath speichern
				String installPath = "";
				if (installDir != null && installPathValid) {
					installPath = installDir.getAbsolutePath();
				}
				saveInstallPath(installPath, zipFile.getAbsolutePath());
			}
			catch (final ZipException e) {
				showErrorDialog("Invalid ZipFile! Please select the broeselhud zip-File!", "Invalid zipFile");
				e.printStackTrace();
			}
			catch (final IOException e) {
				showErrorDialog("Invalid ZipFile! Please select the broeselhud zip-File!", "Invalid zipFile");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param selectedFile
	 * @throws IOException
	 * @throws ZipException
	 */
	private void initColors(final File selectedFile) throws ZipException, IOException {
		// TODO: [x] 1.2 Farben aus vorheriger Installation auslesen, wenn
		// vorhanden
		final InputStream colorsInputstream = getColorsInputstream(selectedFile);
		if (colorsInputstream != null) {
			colorHP = readColor(colorsInputstream, COLOR_STRING_HP);
			if (colorPanelHP != null) {
				colorPanelHP.setColor(colorHP);
				colorPanelHP.setInitialColor(colorHP);
			}
			// Da der Inputstream in readColor() geschlossen wird und auch nicht
			// resettet werden kann, muss bei jedem Aufruf ein neuer Stream
			// erzeugt werden
			colorAmmoInClip = readColor(getColorsInputstream(selectedFile), COLOR_STRING_AMMO_IN_CLIP);
			if (colorPanelAmmoInClip != null) {
				colorPanelAmmoInClip.setColor(colorAmmoInClip);
				colorPanelAmmoInClip.setInitialColor(colorAmmoInClip);
			}
			colorAmmoInReserve = readColor(getColorsInputstream(selectedFile), COLOR_STRING_AMMO_IN_RESERVE);
			if (colorPanelAmmoInReserve != null) {
				colorPanelAmmoInReserve.setColor(colorAmmoInReserve);
				colorPanelAmmoInReserve.setInitialColor(colorAmmoInReserve);
			}
			colorDamageNumbers = readColor(getColorsInputstream(selectedFile), COLOR_STRING_DAMAGE_NUMBERS);
			if (colorPanelDamageNumbers != null) {
				colorPanelDamageNumbers.setColor(colorDamageNumbers);
				colorPanelDamageNumbers.setInitialColor(colorDamageNumbers);
			}
			colorUberChargeBar = readColor(getColorsInputstream(selectedFile), COLOR_STRING_UBERCHARGE_BAR);
			if (colorPanelUberChargeBar != null) {
				colorPanelUberChargeBar.setColor(colorUberChargeBar);
				colorPanelUberChargeBar.setInitialColor(colorUberChargeBar);
			}
			colorLowHP = readColor(getColorsInputstream(selectedFile), COLOR_STRING_LOW_HP);
			if (colorPanelLowHP != null) {
				colorPanelLowHP.setColor(colorLowHP);
				colorPanelLowHP.setInitialColor(colorLowHP);
			}
			colorBuffedHP = readColor(getColorsInputstream(selectedFile), COLOR_STRING_BUFFED_HP);
			if (colorPanelBuffedHP != null) {
				colorPanelBuffedHP.setColor(colorBuffedHP);
				colorPanelBuffedHP.setInitialColor(colorBuffedHP);
			}
			colorAmmoFlash1 = readColor(getColorsInputstream(selectedFile), COLOR_STRING_LOW_AMMO_WARNING1);
			if (colorPanelAmmoFlash1 != null) {
				colorPanelAmmoFlash1.setColor(colorAmmoFlash1);
				colorPanelAmmoFlash1.setInitialColor(colorAmmoFlash1);
			}
			colorAmmoFlash2 = readColor(getColorsInputstream(selectedFile), COLOR_STRING_LOW_AMMO_WARNING2);
			if (colorPanelAmmoFlash2 != null) {
				colorPanelAmmoFlash2.setColor(colorAmmoFlash2);
				colorPanelAmmoFlash2.setInitialColor(colorAmmoFlash2);
			}
			colorUberChargeFull1 = readColor(getColorsInputstream(selectedFile), COLOR_STRING_UBER_CHARGE_FULL1);
			if (colorPanelUberChargeFull1 != null) {
				colorPanelUberChargeFull1.setColor(colorUberChargeFull1);
				colorPanelUberChargeFull1.setInitialColor(colorUberChargeFull1);
			}
			colorUberChargeFull2 = readColor(getColorsInputstream(selectedFile), COLOR_STRING_UBER_CHARGE_FULL2);
			if (colorPanelUberChargeFull2 != null) {
				colorPanelUberChargeFull2.setColor(colorUberChargeFull2);
				colorPanelUberChargeFull2.setInitialColor(colorUberChargeFull2);
			}
			colorTargetIDLow = readColor(getColorsInputstream(selectedFile), COLOR_STRING_TARGET_ID_LOW_HP);
			if (colorPanelTargetIDLow != null) {
				colorPanelTargetIDLow.setColor(colorTargetIDLow);
				colorPanelTargetIDLow.setInitialColor(colorTargetIDLow);
			}
			colorTargetIDBuffed = readColor(getColorsInputstream(selectedFile), COLOR_STRING_TARGET_ID_BUFFED_HP);
			if (colorPanelTargetIDBuffed != null) {
				colorPanelTargetIDBuffed.setColor(colorTargetIDBuffed);
				colorPanelTargetIDBuffed.setInitialColor(colorTargetIDBuffed);
			}
		}
		else {
			System.out.println("Invalid zipfile selected");
			throw new FileNotFoundException("Could not find file ClientScheme.res!");
		}
	}

	private InputStream getColorsInputstream(final File selectedFile) throws FileNotFoundException, ZipException, IOException {
		InputStream inputStream = null;
		final String colorsConfigSubPath = "resource/ClientScheme.res";
		// Pr�fen, ob ClientScheme.res schon aus vorheriger Installation
		// vorhanden ist....
		if (installPathValid) {
			final File resFile = new File(installDir.getAbsolutePath() + File.separator + colorsConfigSubPath.replace('/', File.separatorChar));
			if (resFile.exists()) {
				inputStream = new BufferedInputStream(new FileInputStream(resFile));
			}
		}
		if (inputStream == null) {
			// ...sonst default werte aus zip file lesen
			final ZipFile zipFile = new ZipFile(selectedFile);
			final ZipEntry colorsEntry = zipFile.getEntry(colorsConfigSubPath);
			inputStream = zipFile.getInputStream(colorsEntry);
		}
		return inputStream;
	}

	/**
	 * @param inputStream
	 * @param colorString
	 * @return
	 * @throws IOException
	 */
	private Color readColor(final InputStream inputStream, final String colorString) throws IOException {
		Color readColor = null;
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		while (br.ready()) {
			final String line = br.readLine().trim();
			if (line.matches(getLineRegex(colorString))) {
				final StringTokenizer tokenizer = new StringTokenizer(line, "\"", false);
				tokenizer.nextToken();
				tokenizer.nextToken();
				final String colorValues = tokenizer.nextToken();
				final StringTokenizer colorTokenzier = new StringTokenizer(colorValues, " ", false);
				readColor = new Color(Integer.parseInt(colorTokenzier.nextToken()), Integer.parseInt(colorTokenzier.nextToken()),
						Integer.parseInt(colorTokenzier.nextToken()), Integer.parseInt(colorTokenzier.nextToken()));
				break;
			}
		}
		br.close();
		return readColor;
	}

	/**
	 * @author Baret
	 * 
	 */
	public class ColorButtonListener implements ActionListener {

		private final Component parent;
		private final String text;
		private Color color;
		private Canvas colorLabel;

		/**
		 * @param parent
		 * @param text
		 * @param color
		 */
		public ColorButtonListener(final Component parent, final String text, final Color color) {
			this.parent = parent;
			this.text = text;
			this.color = color;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent )
		 */
		@Override
		public void actionPerformed(final ActionEvent e) {
			color = JColorChooser.showDialog(parent, "Change Color: " + text, color);
			colorLabel.getGraphics().setColor(color);
			colorLabel.getGraphics().drawRect(0, 0, colorLabel.getWidth(), colorLabel.getHeight());
		}

		/**
		 * @param newColorField
		 */
		public void setColorField(final Canvas newColorField) {
			colorLabel = newColorField;
		}
	}

}
