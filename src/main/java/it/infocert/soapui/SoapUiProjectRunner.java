package it.infocert.soapui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.SoapUIException;

/**
 * Il ProjetRunner si occupa di invocare i test, per fare ciò cicla sulle
 * TestSuite selezionate e invoca i singoli TestCase e ne ritorna il risultato
 * in una mappa
 * 
 * @author svaponi
 *
 */
public class SoapUiProjectRunner {

	public static final String SEPARATOR = ":";
	private WsdlProject project;

	public static final Logger log = Logger.getLogger(SoapUiProjectRunner.class);

	public SoapUiProjectRunner(File project) {
		this(project.getAbsolutePath());
	}

	public SoapUiProjectRunner(String projectPath) {
		super();
		try {
			project = new WsdlProject(projectPath);
		} catch (SoapUIException e) {
			log.error("Impossibile aprire il progetto! " + e.getMessage());
		} catch (XmlException e) {
			log.error("Impossibile aprire il progetto! " + e.getMessage());
		} catch (IOException e) {
			log.error("Impossibile aprire il progetto! " + e.getMessage());
		}

	}

	public Map<String, TestCaseResult> run() {
		return runLowLevel(null, null);
	}

	public Map<String, TestCaseResult> run(Collection<String> testSuites, Collection<String> testCases) {
		return runLowLevel(testSuites, testCases);
	}

	public Map<String, TestCaseResult> runSuites(Collection<String> testSuites) {
		return runLowLevel(testSuites, null);
	}

	public Map<String, TestCaseResult> runCases(Collection<String> testCases) {
		return runLowLevel(null, testCases);
	}

	private Map<String, TestCaseResult> runLowLevel(Collection<String> testSuites, Collection<String> testCases) {

		log.info("Running...");
		Map<String, TestCaseResult> result = new TreeMap<String, TestCaseResult>();
		String tSuiteName , tCaseName , key;

		// ciclo sulle test suite
		tSuiteLoop: for (final TestSuite tSuite : project.getTestSuiteList()) {

			tSuiteName = tSuite.getName();
			key = tSuiteName;
			log.debug(l(0) + "Analysing TestSuite \"" + tSuiteName + "\"...");

			if (testSuites != null && !testSuites.isEmpty() && !testSuites.contains(tSuiteName)) {

				if (log.isDebugEnabled())
					// salvo il risultato
					result.put(tSuiteName, new TestCaseResult(tSuiteName, "IGNORED"));

				log.debug(l(0) + "TestSuite \"" + tSuiteName + "\" IGNORED");
				continue tSuiteLoop;

			} else if (tSuite.isDisabled()) {

				if (log.isDebugEnabled())
					// salvo il risultato
					result.put(tSuiteName, new TestCaseResult(tSuiteName, "DISABLED"));

				log.debug(l(0) + "TestSuite \"" + tSuiteName + "\" is DISABLED");
				continue tSuiteLoop;
			}

			log.info(l(0) + "Running TestSuite \"" + tSuiteName + "\"...");

			// ciclo sui test case
			tCaseLoop: for (final TestCase tCase : tSuite.getTestCaseList()) {

				tCaseName = tCase.getName();
				key = tSuiteName + SEPARATOR + tCaseName;
				log.debug(l(1) + "Analysing TestCase \"" + tCaseName + "\"...");

				if (!runTestCase(tSuiteName, tCaseName, testCases)) {

					if (log.isDebugEnabled())
						// salvo il risultato
						result.put(tSuiteName + SEPARATOR + tCaseName,
								new TestCaseResult(tSuiteName + SEPARATOR + tCaseName, "IGNORED"));

					log.debug(l(1) + "TestCase \"" + tCaseName + "\" IGNORED");
					continue tCaseLoop;

				} else if (tCase.isDisabled()) {

					if (log.isDebugEnabled())
						// salvo il risultato
						result.put(tSuiteName + SEPARATOR + tCaseName,
								new TestCaseResult(tSuiteName + SEPARATOR + tCaseName, "DISABLED"));

					log.info(l(1) + "TestCase \"" + tCaseName + "\" is DISABLED");
					continue tCaseLoop;
				}

				log.info(l(1) + "Running TestCase \"" + tCaseName + "\"...");

				try {
					// Eseguo il test case tCase
					final TestRunner runner = tCase.run(new PropertiesMap(), false);
					String status = runner.getStatus().toString();

					// salvo il risultato se c'è ERRORE o se level è maggiore o
					// pari a INFO
					if (Status.FAILED.toString().equals(status) || log.isInfoEnabled()) {
						TestCaseResult testResult = new TestCaseResult(key, runner);
						result.put(key, testResult);

						// Stampo il risultato
						log.info(l(2) + "Status => " + testResult.getFormattedStatus());
						log.info(l(2) + "Time start => " + testResult.getFormattedStartTime());
						log.info(l(2) + "Time elapsed => " + testResult.getFormattedElapsed());
					}

					// List test-steps
					if (log.isDebugEnabled()) {
						log.debug(l(2) + "Steps: ");
						for (final ModelItem child : runner.getTestRunnable().getChildren())
							log.debug(l(3) + child.getName());
					}

				} catch (Exception e) {
					// salvo l'errore
					result.put(key, new TestCaseResult(key, "ERROR", e.getMessage()));
					log.error(l(2) + "Error => " + e.getMessage());
				}

			}
		}
		return result;
	}

	/**
	 * Verifica se il test case è da eseguire o meno (con la possibilità di
	 * disambiguare i test case preappendendo il nome della suite separato dal
	 * {@link SoapUIRunner#SEPARATOR}).
	 * 
	 * @param suiteName
	 * @param caseName
	 * @param testCases
	 * @return
	 */
	private boolean runTestCase(String tSuiteName, String tCaseName, Collection<String> inputTestCases) {

		if (inputTestCases == null || inputTestCases.isEmpty())
			return true;

		String[] arr;
		boolean found;
		/*
		 * Controlla se il test case in input specifica anche la suite (prefisso
		 * separato da SEPARATOR)
		 */
		for (String inputTestCase : inputTestCases) {
			if (inputTestCase.indexOf(SEPARATOR) > 0) {
				arr = inputTestCase.split(SEPARATOR);
				found = tSuiteName.equals(arr[0]) && tCaseName.equals(arr[1]);
			} else {
				found = tCaseName.equals(inputTestCase);
			}
			if (found)
				return true;
		}
		return false;
	}

	/**
	 * Utility per formattare lo standard output
	 * 
	 * @param i
	 * @return
	 */
	private static String l(int i) {
		if (i < 0)
			return "";
		int n = 2 * (i + 1);
		return String.format("%0$" + n + "s", "> ");
	}

}
