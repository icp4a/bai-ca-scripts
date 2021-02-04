package com.ibm.decisions.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Permission;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.automation.AutomationService;
import com.ibm.decisions.DecisionServiceExporter;

class ServiceExporterTest {
	@SuppressWarnings("serial")
	protected static class ExitException extends SecurityException {
		public final int status;

		public ExitException(int status) {
			super("There is no escape!");
			this.status = status;
		}
	}

	private static class NoExitSecurityManager extends SecurityManager {
		@Override
		public void checkPermission(Permission perm) {
			// allow anything.
		}

		@Override
		public void checkPermission(Permission perm, Object context) {
			// allow anything.
		}

		@Override
		public void checkExit(int status) {
			super.checkExit(status);
			throw new ExitException(status);
		}
	}

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;

	@Test
	void testLoanValidation() throws JsonParseException, JsonMappingException, IOException {
		TemporaryFolder folder = new TemporaryFolder();
		String output = "";
		folder.create();
		File f = folder.newFile("output.json");
		output = f.getAbsolutePath();
		String args = "-p ../../Decision Service sample projects/Loan Validation Service/Loan Validation Service -d production deployment -o loan validation production   -r /mydeployment/1.0/Miniloan_ServiceRuleset/1.0  -output ";
		args += output;
		DecisionServiceExporter.main(args.split(" "));

		ObjectMapper mapper = new ObjectMapper();
		AutomationService srv = mapper.readValue(new File(output), AutomationService.class);
		assertEquals(28, srv.concepts.size());
		assertEquals("1.0.0", srv.getVersion());
		assertEquals("/mydeployment/1.0/Miniloan_ServiceRuleset/1.0", srv.getAssetPath());
		assertEquals(3, srv.getSignature().getParameters().size());
		assertEquals("borrower", srv.getSignature().getParameters().get(0).getName());
		assertEquals("loan.Borrower", srv.getSignature().getParameters().get(0).getType());
		assertEquals("the borrower", srv.getSignature().getParameters().get(0).getVerbalization());
		folder.delete();
	}

	@Test
	void testSegmentation() throws JsonParseException, JsonMappingException, IOException {
		TemporaryFolder folder = new TemporaryFolder();
		String output = "";
		folder.create();
		File f = folder.newFile("output.json");
		output = f.getAbsolutePath();
		String args = "-p ../../Decision Service sample projects/Segmentation300Rules -d test    -r /mydeployment/1.0/a/1.0  -output ";
		args += output;
		DecisionServiceExporter.main(args.split(" "));

		ObjectMapper mapper = new ObjectMapper();
		AutomationService srv = mapper.readValue(new File(output), AutomationService.class);
		assertEquals(27, srv.concepts.size());
		assertEquals(27, srv.concepts.size());

		assertEquals("1.0.0", srv.getVersion());
		assertEquals("/mydeployment/1.0/a/1.0", srv.getAssetPath());
		assertEquals(3, srv.getSignature().getParameters().size());
		assertEquals("consumer", srv.getSignature().getParameters().get(0).getName());
		assertEquals("poc.ilog.webperso.Consumer", srv.getSignature().getParameters().get(0).getType());
		assertEquals("the consumer", srv.getSignature().getParameters().get(0).getVerbalization());
		folder.delete();
	}

	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	void testBadProject() throws JsonParseException, JsonMappingException, IOException {

		System.setSecurityManager(new NoExitSecurityManager());
		setUpStreams();
		try {
			String args = "-p ./doesnotexist -d production deployment -o loan validation production   -r /mydeployment/1.0/Miniloan_ServiceRuleset/1.0 -verbose -output t.json";
			DecisionServiceExporter.main(args.split(" "));
			Assert.fail();
		} catch (ExitException e) {
			assertEquals(0, outContent.toString().indexOf("Project directory does not exist"));
		} finally {
			System.setSecurityManager(null); // or save and restore original
			restoreStreams();
		}

	}

	@Test
	void testMissingArg() throws JsonParseException, JsonMappingException, IOException {

		System.setSecurityManager(new NoExitSecurityManager());
		setUpStreams();
		try {
			String args = " -d production deployment -o loan validation production   -r /mydeployment/1.0/Miniloan_ServiceRuleset/1.0 -verbose -output t.json";
			DecisionServiceExporter.main(args.split(" "));
			Assert.fail();
		} catch (ExitException e) {
			assertEquals(0, outContent.toString().indexOf("usage"));
		} finally {
			System.setSecurityManager(null); // or save and restore original
			restoreStreams();
		}

	}

}
