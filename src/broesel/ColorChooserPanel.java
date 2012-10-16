/**
 * 
 */
package broesel;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Jan
 * 
 */
public class ColorChooserPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7000138248661269321L;
	private static final String AC_CHANGE = "change";
	private static final String AC_RESET = "reset";
	private Color color;
	private final Component parent;
	private final String text;
	private final JLabel colorField;
	private Color initColor;

	/**
	 * @param text
	 * @param initialColor
	 * @param dialogParent
	 */
	public ColorChooserPanel(final String text, final Color initialColor, final Component dialogParent) {
		// TODO: [X] 1.1 Reset to standard button
		// TODO: [X] 1.1 Button und Farbvorschau rechtsb�ndig (damit alles
		// untereinander ist)
		color = initialColor;
		initColor = initialColor;
		parent = dialogParent;
		this.text = text;
		// setLayout(new FlowLayout(FlowLayout.LEFT));
		setLayout(new GridLayout(1, 2));
		add(new JLabel(text));
		final JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		colorField = new JLabel("                ");
		colorField.setBackground(color);
		colorField.setOpaque(true);
		rightPanel.add(colorField);
		final JButton changeButton = new JButton("Change...");
		changeButton.setActionCommand(AC_CHANGE);
		changeButton.addActionListener(this);
		rightPanel.add(changeButton);
		final JButton resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset this color to standard");
		resetButton.setActionCommand(AC_RESET);
		resetButton.addActionListener(this);
		rightPanel.add(resetButton);
		add(rightPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent )
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals(AC_CHANGE)) {
			final Color tmpColor = JColorChooser.showDialog(parent, "Change Color: " + text, color);
			if (tmpColor != null) {
				color = tmpColor;
				colorField.setBackground(color);
			}
		}
		if (e.getActionCommand().equals(AC_RESET)) {
			if (initColor != null) {
				color = initColor;
				colorField.setBackground(color);
			}
		}
	}

	/**
	 * @param newColor
	 */
	public void setColor(final Color newColor) {
		this.color = newColor;
		colorField.setBackground(color);
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param newInitialColor
	 */
	public void setInitialColor(final Color newInitialColor) {
		initColor = newInitialColor;
	}

}
