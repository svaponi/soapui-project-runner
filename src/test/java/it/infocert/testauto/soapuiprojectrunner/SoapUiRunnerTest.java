package it.infocert.testauto.soapuiprojectrunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import it.infocert.soapui.SoapUiProjectRunner;
import it.infocert.soapui.TestCaseResult;

/**
 * Esegue dei progetti SoapUI di prova e controlla l'esito dei test.
 * <h2>Progetti:</h2>
 * <dl>
 * <dt>dummy-test-project.xml</dt>
 * <dd>progetto con 4 suites (SuiteA, SuiteB, SuiteC e SuiteD), ognuna con 3
 * cases (TestCase1, TestCase2 e TestCase3). Tutte le suites terminano senza
 * errori.</dd>
 * <dt>dummy-test-project_with-errors.xml</dt>
 * <dd>progetto con 4 suites (SuiteA, SuiteB, SuiteC e SuiteD), ognuna con 3
 * cases (TestCase1, TestCase2 e TestCase3). Le suite A e D generano degli
 * errori/eccezioni rispettivamente nel TestCase1 e TestCase3, questi errori
 * sono provocati dallo step "Groovy Script 3" (presente in ambo i cases)</dd>
 * </dl>
 * 
 * Per entrambi i progetti, gli elementi disabilitati (che dunque verranno
 * saltati dall'esecuzione) sono: SuiteB:TestCase1, SuiteB:TestCase2,
 * SuiteB:TestCase3, SuiteC, SuiteD:TestCase2. Se una suite è disabilitata
 * vengono disabilitati i suoi cases.
 * 
 * @author svaponi
 *
 */
public class SoapUiRunnerTest {

	static final File project;
	static final String projectName = "dummy-test-project.xml";
	static final File errorProject;
	static final String errorProjectName = "dummy-test-project_with-errors.xml";
	static {
		ClassLoader classLoader = SoapUiRunnerTest.class.getClassLoader();
		project = new File(classLoader.getResource(projectName).getPath());
		if (!project.exists())
			throw new RuntimeException("Missing test SoapUI project: " + projectName);
		errorProject = new File(classLoader.getResource(errorProjectName).getPath());
		if (!errorProject.exists())
			throw new RuntimeException("Missing test SoapUI project: " + errorProjectName);
	}

	/**
	 * Items (coppia Suite:TestCase) disabilitati, dunque non compaiono tra i risultati
	 */
	static final List<String> disabledItems = Arrays.asList("SuiteB:TestCase1", "SuiteB:TestCase2", "SuiteB:TestCase3",
			"SuiteC:TestCase1", "SuiteC:TestCase2", "SuiteC:TestCase3", "SuiteD:TestCase2");

	/**
	 * Items che generano errori in errorProject, il loro status sarà FAILED
	 */
	static final List<String> errorItems = Arrays.asList("SuiteA:TestCase1", "SuiteD:TestCase3");

	/**
	 * Status tornato da SoapUI dopo l'esecuzione di un item che è andato in errore
	 */
	private static final Object FAILED = "FAILED";
	
	/**
	 * Status tornato da SoapUI dopo l'esecuzione di un item terminato con successo
	 */
	private static final Object FINISHED = "FINISHED";

	public SoapUiRunnerTest() {
		super();
	}

	@Test
	public void run() {

		Map<String, TestCaseResult> result = new SoapUiProjectRunner(project).run();

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());

		for (String testId : result.keySet()) {

			// i testId non devono contenere gli items disabilitati
			Assert.assertFalse(disabledItems.contains(testId));

			TestCaseResult testResult = result.get(testId);
			// tutti i test finiscono senza errori
			Assert.assertEquals(testResult.getStatus(), FINISHED);
		}
	}

	@Test
	public void runWithErrors() {
		Map<String, TestCaseResult> result = new SoapUiProjectRunner(errorProject).run();

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());

		for (String testId : result.keySet()) {

			// i testId non devono contenere gli items disabilitati
			Assert.assertFalse(disabledItems.contains(testId));

			TestCaseResult testResult = result.get(testId);

			if (errorItems.contains(testId))
				// test che finiscono con errori
				Assert.assertEquals(testResult.getStatus(), FAILED);
			else
				// test che finiscono senza errori
				Assert.assertEquals(testResult.getStatus(), FINISHED);
		}
	}
}
