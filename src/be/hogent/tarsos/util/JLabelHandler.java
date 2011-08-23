package be.hogent.tarsos.util;

import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Utility class to handle logging with a JLabel (status bar)
 * 
 * @author Joren Six
 */
public final class JLabelHandler extends Handler {

	public static final class JLabelFormatter extends Formatter {

		@Override
		public String format(final LogRecord record) {
			return String.format(" %s: %s ", record.getLevel().getName(),
					record.getMessage());
		}

	}

	private static JLabel label = null;

	/**
	 * Set the JTextArea to log to.
	 * 
	 * @param textArea
	 *            The JTextArea to log to.
	 */
	private static void setTextArea(final JLabel jLabel) {
		JLabelHandler.label = jLabel;
	}

	/**
	 * 
	 * @param logJTextArea
	 *            setup logging for a JTextarea
	 */
	public static void setupLoggerHandler(final JLabel jLabel) {
		// This code attaches the handler to the text area
		setTextArea(jLabel);
	}

	private Level level = Level.INFO; // The logging level for this handler,
										// which is configurable.

	/**
	 * Include filtering mechanism as it is not included in the (lame) Abstract
	 * Handler class.
	 */
	public JLabelHandler() {
		Filter filter = new Filter() {
			public boolean isLoggable(final LogRecord record) {
				return record.getLevel().intValue() >= level.intValue();
			}
		};
		this.setFilter(filter);
		this.setFormatter(new JLabelFormatter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */

	@Override
	public void publish(final LogRecord logRecord) {
		// Must filter our own logRecords, (lame) Abstract Handler does not do
		// it for us.
		if (!getFilter().isLoggable(logRecord)) {
			return;
		}

		final String message = getFormatter().format(logRecord);

		//
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (label != null) {
					label.setText(message);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Handler#close()
	 */

	@Override
	public void close() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Handler#flush()
	 */

	@Override
	public void flush() {
	}

	/**
	 * Must capture level to use in our custom filter, because this is not done
	 * in the abstract class.
	 */

	@Override
	public void setLevel(final Level newLevel) {
		super.setLevel(newLevel);
		level = newLevel;
	}
}
