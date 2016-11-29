package it.infocert.soapui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.eviware.soapui.SoapUI;

/**
 * Classe principale per l'invocazione del {@link SoapUiProjectRunner}.
 * 
 * @author svaponi
 * @email s.vaponi@miriade.it
 */
public class Runner {

	public static final String VERSION = "1.0.3";
	public static final String HELP = "h";
	public static final String PROJECT_PATH = "p";
	public static final String OUTPUT_LEVEL = "l";
	public static final String TEST_SUITEs = "s";
	public static final String TEST_CASEs = "c";
	public static final String OUTPUT_DIR = "output";
	public static final String RESULT_FILE_TMPL = "%s.%s.log";
	public static final String LOG_FILE_TMPL = "%s.stdout.log";
	public static final String FILE_SEPARATOR;
	public static final String LINE_SEPARATOR;
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
	// private static final String PATTERN = "%d %-5p [%c{3}] %m%n";
	public static final String PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%c{2}] %m%n";
	public static final Logger log = Logger.getLogger(Runner.class);
	public static final Level level = Level.INFO;

	private static Date startTime;

	static {

		startTime = new Date();

		// Configuro il logger del core di SoapUI
		SoapUI.setSoapUICore(new CustomSoapUICore());

		String separator = System.getProperty("file.separator");

		if (separator == null || separator.isEmpty())
			separator = System.getProperty("path.separator");

		if (separator == null || separator.isEmpty())
			separator = "/";

		FILE_SEPARATOR = separator;

		String newLine = System.getProperty("line.separator");
		if (newLine == null || newLine.isEmpty())
			newLine = "\r\n";

		LINE_SEPARATOR = newLine;

		FileAppender fa = new FileAppender();
		fa.setName("RootLogger");
		String logFileName = String.format(LOG_FILE_TMPL, DATE_FORMATTER.format(new Date()));
		fa.setFile(OUTPUT_DIR + FILE_SEPARATOR + logFileName);
		fa.setLayout(new PatternLayout(PATTERN));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();

		Logger.getRootLogger().addAppender(fa);
		Logger.getRootLogger().setLevel(level);
	}

	public static void main(String[] args) {

		int status = 1;
		boolean launched = false;
		try {

			CommandLineParser parser = new GnuParser();
			List<Option> optionList = new ArrayList<Option>();

			/*
			 * Definisco gli argomenti in input
			 */
			OptionBuilder.withArgName("project");
			OptionBuilder.withLongOpt("project");
			OptionBuilder.hasArg();
			OptionBuilder.isRequired();
			OptionBuilder.withDescription("SoapUI project XML file");
			optionList.add(OptionBuilder.create(PROJECT_PATH));

			OptionBuilder.withArgName("help");
			OptionBuilder.withLongOpt("help");
			OptionBuilder.hasArgs(0);
			OptionBuilder.withDescription("Print usage");
			optionList.add(OptionBuilder.create(HELP));

			OptionBuilder.withArgName("output");
			OptionBuilder.withLongOpt("output");
			OptionBuilder.hasArg();
			OptionBuilder.withDescription("Output level");
			optionList.add(OptionBuilder.create(OUTPUT_LEVEL));

			OptionBuilder.withArgName("suites");
			OptionBuilder.withLongOpt("suites");
			OptionBuilder.hasArgs();
			OptionBuilder.withDescription("TestSuite names");
			optionList.add(OptionBuilder.create(TEST_SUITEs));

			OptionBuilder.withArgName("cases");
			OptionBuilder.withLongOpt("cases");
			OptionBuilder.hasArgs();
			OptionBuilder.withDescription("TestCase names");
			optionList.add(OptionBuilder.create(TEST_CASEs));

			Options options = new Options();
			for (Option op : optionList)
				options.addOption(op);

			/*
			 * Leggo gli argomenti in input
			 */
			CommandLine line = parser.parse(options, args);

			log.info(String.format("%-14s %s ", "Version", VERSION));

			String file = line.getOptionValue(PROJECT_PATH);
			// controllo subito se il project file è valido
			if (!new File(file).exists())
				throw new IllegalArgumentException("Project file not found");
			if (!file.substring(file.length() - 4).equalsIgnoreCase(".xml"))
				throw new IllegalArgumentException("Invalid project file");
			log.info(String.format("%-14s %s ", "Project", file));

			String levelStr = null;
			if (line.hasOption(OUTPUT_LEVEL)) {
				levelStr = line.getOptionValue(OUTPUT_LEVEL).toUpperCase();
				log.info(String.format("%-14s %s ", "Output", levelStr));
				initLog(levelStr);
			} else {
				log.info(String.format("%-14s %s ", "Output", level));
			}

			Collection<String> suites = null;
			if (line.hasOption(TEST_SUITEs)) {
				suites = Arrays.asList(line.getOptionValues(TEST_SUITEs));
				log.info(String.format("%-14s %s ", "TestSuites", suites));
			}

			Collection<String> cases = null;
			if (line.hasOption(TEST_CASEs)) {
				cases = Arrays.asList(line.getOptionValues(TEST_CASEs));
				log.info(String.format("%-14s %s ", "TestCases", cases));
			}

			log.info(String.format("Letti parametri in input - Elapsed: %s ", computeDiff(startTime, new Date())));

			// inizializzo il TestRunner
			SoapUiProjectRunner runner = new SoapUiProjectRunner(file);
			log.info(String.format("Progetto caricato correttamente - Elapsed: %s ",
					computeDiff(startTime, new Date())));

			// Lancio il TestRunner per invocare i test
			Map<String, TestCaseResult> results = runner.run(suites, cases);
			launched = true;

			String basename;
			int x = file.lastIndexOf(FILE_SEPARATOR);
			if (x >= 0)
				basename = file.substring(x + 1, file.length() - 4);
			else
				basename = file.substring(0, file.length() - 4);

			String outputFileName = String.format(RESULT_FILE_TMPL, DATE_FORMATTER.format(new Date()), basename);
			String outputFile = OUTPUT_DIR + FILE_SEPARATOR + outputFileName;
			log.info("Printing results...");

			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			Writer writer = null;

			try {
				// Inizializzo la cartella di output
				new File(OUTPUT_DIR).mkdirs();

				fos = new FileOutputStream(outputFile);
				osw = new OutputStreamWriter(fos, "utf-8");
				writer = new BufferedWriter(osw);

				String tmp;
				for (String key : results.keySet()) {
					tmp = String.format("%s=%s ", key, results.get(key).toString());
					writer.write(tmp);
					writer.write(LINE_SEPARATOR);
				}
				log.info("See " + new File(outputFile).getAbsolutePath() + "");
			} catch (Exception e) {
				log.error("Error writing output: " + e.getMessage());
			} finally {
				if (writer != null)
					writer.close();
				if (osw != null)
					osw.close();
				if (fos != null)
					fos.close();
			}

			log.info(String.format("Finished! Total elapsed: %s ", computeDiff(startTime, new Date())));

		} catch (Exception e) {
			System.err.println(e.getMessage());

			if (!launched)
				printUsage();
		}

		System.exit(status);
	}

	/**
	 * Stampa le info per il corretto utilizzo del tool in stdout
	 */
	private static void printUsage() {
		System.out.println("");
		double version = getVersion();
		if (version == 0.0)
			System.out.println("Java 1.6 required! Current version is UNKNOWN \n");
		else if (version < 1.6)
			System.out.println("Java 1.6 required! Current version is Java " + version + " \n");
		System.out.printf(
				"java -jar {name}.jar -%s /path/to/soapui-project.xml [-l level] [-%s suite1 suite2 ...] [-%s case1 suite2:case2 ...] \n",
				PROJECT_PATH, TEST_SUITEs, TEST_CASEs);
		System.out.println("");
		System.out.printf("  -%s %-24s %-12s %s \n", PROJECT_PATH, "soapui-project-path", "REQUIRED",
				"file xml del progetto (path relativo o assoluto)");
		System.out.printf("  -%s %-24s %-12s %s \n", OUTPUT_LEVEL, "level", "OPTIONAL",
				"livello di output, default è INFO. Altri: DEBUG (stampa tutto, inclusi i test disabilitati o ignorati); INFO e WARN (stampa solo i test eseguiti); ERROR (stanpa solo i test falliti)");
		System.out.printf("  -%s %-24s %-12s %s \n", TEST_SUITEs, "suite1 suite2", "OPTIONAL",
				"lista di TestSuite da eseguire");
		System.out.printf("  -%s %-24s %-12s %s \n", TEST_CASEs, "case1 suite2:case2", "OPTIONAL",
				"lista di TestCase da eseguire (in caso di omonimia è possibile disambiguare i TestCase specificando la TestSuite come prefisso separata da \":\")");
	}

	/**
	 * Ritorna la versione Java corrente in double, es. 1.6
	 * 
	 * @return
	 */
	private static double getVersion() {
		try {
			String version = System.getProperty("java.version");
			int pos = version.indexOf('.');
			pos = version.indexOf('.', pos + 1);
			return Double.parseDouble(version.substring(0, pos));
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Ritorna una mappo con tutti i TimeUnit e i relativi valori numerici, es.
	 * {HOURS=0, MINUTES=0, SECONDS=1, MILLISECONDS=768}
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	private static Map<TimeUnit, Long> computeDiff(Date date1, Date date2) {
		long diffInMillies = date2.getTime() - date1.getTime();
		List<TimeUnit> units = Arrays.asList(TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
		Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
		long milliesRest = diffInMillies;
		for (TimeUnit unit : units) {
			long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
			long diffInMilliesForUnit = unit.toMillis(diff);
			milliesRest = milliesRest - diffInMilliesForUnit;
			result.put(unit, diff);
		}
		return result;
	}

	private static void initLog(String levelStr) {
		Level level;
		try {
			level = Level.toLevel(levelStr);
		} catch (Exception e) {
			level = Level.INFO;
		}
		Logger.getRootLogger().setLevel(level);
	}
}
