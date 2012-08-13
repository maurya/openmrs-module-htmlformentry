package org.openmrs.module.htmlformentry;

import java.util.Date;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;


public class DynamicRepeatTest extends BaseModuleContextSensitiveTest {
	protected static final String XML_DATASET_PATH = "org/openmrs/module/htmlformentry/include/";
	protected static final String XML_REGRESSION_TEST_DATASET = "regressionTestDataSet";

	@Before
    public void setupDatabase() throws Exception {
		executeDataSet(XML_DATASET_PATH + new TestUtil().getTestDatasetFilename(XML_REGRESSION_TEST_DATASET));
    }

	@Test
	public void shouldTestDynamicRepeat() throws Exception {
		new RegressionTestHelper() {
			final Date date = new Date();

			@Override
			public String getFormName() {
				return "dynamicRepeatTestForm";
			}

			@Override
			public String[] widgetLabels() {
				return new String[] { "Date:", "Location:", "Provider:"};
			}

			@Override
			public void testBlankFormHtml(String html){
				System.out.println(html);
			}

			@Override
			public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
				request.addParameter(widgets.get("Date:"), dateAsString(date));
				request.addParameter(widgets.get("Location:"), "2");
				request.addParameter(widgets.get("Provider:"), "502");

				// w7 = number of repeats
				// w9-template is allergy, w11-template is allergy date
				request.addParameter("w7", "2");

				request.addParameter("w9-0", "Penicillin");
				request.addParameter("w11-0", dateAsString(date));

				request.addParameter("w9-1", "PAS");
				request.addParameter("w11-1", dateAsString(date));
			}

			@Override
			public void testResults(SubmissionResults results) {
				results.assertNoErrors();
				results.assertEncounterCreated();
				Encounter e = results.getEncounterCreated();
				Assert.assertTrue(e != null);

				// should have 6 obs (two groups, each with a grouper + two children)
				Assert.assertEquals(6, e.getAllObs().size());
			}

		}.run();
	}
}
