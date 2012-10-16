/**
 * 
 */
package broesel;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author Jan
 *
 */
public class MyHyperlinkListener implements HyperlinkListener {

	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	@Override
	public void hyperlinkUpdate(final HyperlinkEvent e) {
		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			try {
				Desktop.getDesktop().browse(e.getURL().toURI());
			} catch (final IOException e1) {
				e1.printStackTrace();
			} catch (final URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}

}
