package be.hogent.tarsos.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.hogent.tarsos.Tarsos;

public final class Execute {
	private static final Logger LOG = Logger.getLogger(Execute.class.getName());

	// disable default constructor
	private Execute() {
	}

	/**
	 * Executes a command and returns the exit value of the process.
	 * 
	 * @param command
	 *            the command to execute
	 * @param redirectOutputToFile
	 *            redirects the output to this file. Leave empty or set to null
	 *            when no output file is wanted.
	 * @return the exit value of the process. 0 means everything went OK. Other
	 *         values depend on the operating system and process.
	 */
	public static int command(final String command,
			final String redirectOutputToFile) {
		int exitValue = -1;

		final String[] cmd = buildCommand(command, redirectOutputToFile);
		LOG.info(String.format("Executing %s %s %s", cmd[0], cmd[1], cmd[2]));
		final Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(cmd);
			final BufferedReader stdOut = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			final BufferedReader stdErr = new BufferedReader(
					new InputStreamReader(proc.getErrorStream()));
			new Thread(new OutputProcessor(stdOut, false), "Proc stdout data")
					.start();
			new Thread(new OutputProcessor(stdErr, false), "Proc stderr data")
					.start();
			exitValue = proc.waitFor();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error while communicating with process", e);
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, "Error while communicating with process", e);
		}
		if (exitValue != 0) {
			LOG.warning("Abnormal process termination, exit status: "
					+ exitValue);
		}
		LOG.info(String.format("Finished executing (exit status %s) '%s'",
				exitValue, cmd[2]));

		return exitValue;
	}

	/**
	 * Reads and optionally prints data from BufferedReader.
	 */
	private static class OutputProcessor implements Runnable {
		private final BufferedReader outputReader;
		private final boolean printToStandardOut;

		public OutputProcessor(final BufferedReader reader, final boolean print) {
			outputReader = reader;
			printToStandardOut = print;
		}

		public void run() {
			String line;
			try {
				line = outputReader.readLine();
				while (line != null) {
					LOG.finer(line);
					line = outputReader.readLine();
					if (printToStandardOut) {
						Tarsos.println(line);
					}
				}
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Error while communicating with process",
						e);
			} finally {
				try {
					outputReader.close();
				} catch (final IOException e) {
					LOG.log(Level.SEVERE,
							"Could not close communication channel with proc.",
							e);
				} catch (final NullPointerException e) {
					LOG.log(Level.SEVERE,
							"Failed to initialize communication channel with proc.",
							e);
				}
			}
		}
	}

	/**
	 * Executes a command and returns the exit value of the process. Redirect
	 * STDOUT the process to STDOUT of this process.
	 * 
	 * @param command
	 *            the command to execute
	 * @return the exit value of the process. 0 means everything went OK. Other
	 *         values depend on the operating system and process.
	 */
	public static int command(final String command) {
		return command(command, null);
	}

	/**
	 * Checks if the external command is available in the path.
	 * 
	 * @param command
	 *            the command to check
	 * @return <code>true</code> if the command is available, false otherwise.
	 */
	public static boolean executableAvailable(final String command) {
		boolean executableIsInPath = false;
		final int exitValue = Execute.command(command);
		int commandNotFoundExitCode;
		if (System.getProperty("os.name").contains("indows")) {
			commandNotFoundExitCode = Configuration
					.getInt(ConfKey.win_shell_executable_not_found_exit_code);
		} else {
			commandNotFoundExitCode = Configuration
					.getInt(ConfKey.unix_shell_executable_not_found_exit_code);
		}
		if (exitValue != commandNotFoundExitCode) {
			executableIsInPath = true;
		}
		return executableIsInPath;
	}

	private static String[] buildCommand(final String command,
			final String redirectOutputToFile) {
		String[] cmd;
		if (System.getProperty("os.name").contains("indows")) {
			cmd = buildWindowsCommand(command, redirectOutputToFile);
		} else {
			cmd = buildUinxCommand(command, redirectOutputToFile);
		}
		return cmd;
	}

	private static String[] buildWindowsCommand(final String command,
			final String redirectOutputToFile) {
		final String redirectPostfix;
		final String[] cmd = new String[3];
		if (redirectOutputToFile == null) {
			redirectPostfix = "";
		} else {
			redirectPostfix = " > " + redirectOutputToFile;
		}
		cmd[0] = Configuration.get(ConfKey.win_shell_executable);
		cmd[1] = Configuration.get(ConfKey.win_shell_executable_option);
		cmd[2] = command + redirectPostfix;
		return cmd;
	}

	private static String[] buildUinxCommand(final String command,
			final String redirectOutputToFile) {
		final String redirectPostfix;
		final String[] cmd = new String[3];
		if (redirectOutputToFile == null) {
			redirectPostfix = "";
		} else {
			redirectPostfix = "| cat > " + redirectOutputToFile;
		}
		cmd[0] = Configuration.get(ConfKey.unix_shell_executable);
		cmd[1] = Configuration.get(ConfKey.unix_shell_executable_option);
		cmd[2] = command + " 2>&1 " + redirectPostfix;
		return cmd;
	}
}
