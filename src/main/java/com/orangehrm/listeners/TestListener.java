package com.orangehrm.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import com.orangehrm.base.BaseClass;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.RetryAnalyzer;

public class TestListener implements ITestListener,IAnnotationTransformer {
	// Triggered when a test starts
	
	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
	
		annotation.setRetryAnalyzer(RetryAnalyzer.class);
		
	}

	@Override
	public void onTestStart(ITestResult result) {

		String testName = result.getMethod().getMethodName();
		// Initialize Extent Test here
		ExtentManager.startTest(testName);
		ExtentManager.logStep("Test Started: " + testName);
	}


	//Triggered when a test passes
	@Override
	public void onTestSuccess(ITestResult result) {
		String testName = result.getMethod().getMethodName();

		if (!result.getTestClass().getName().toLowerCase().contains("api")) {
			ExtentManager.logStepWithScreenshot(BaseClass.getDriver(), "Test Passed Successfully!",
					"Test End: " + testName + " - ✔ Test Passed");
		} else {
			ExtentManager.logStepValidationForAPI("Test End: " + testName + " - ✔ Test Passed");
		}

	}

	// Triggered when a test fails
	@Override
	public void onTestFailure(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String failureMessage = result.getThrowable().getMessage();
		ExtentManager.logStep(failureMessage);
		if(!result.getTestClass().getName().toLowerCase().contains("api")) {
			ExtentManager.logFailure(BaseClass.getDriver(), "Test Failed!", "Test End: " + testName + " - ❌ Test Failed");
		}
		else {
			ExtentManager.logFailureAPI("Test End: " + testName + " - ❌ Test Failed");
		}
	}

	// Triggered when a test is skipped
	@Override
	public void onTestSkipped(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentManager.logSkip("Test Skipped: " + testName);
	}

//Triggered when the suite starts
	@Override
	public void onStart(ITestContext context) {
		// Initialize Extent Reports here
		ExtentManager.getReporter();
	}

//Triggered when the suite ends
	@Override
	public void onFinish(ITestContext context) {
		// Flush Extent Reports here
		ExtentManager.endTest();
	}
	// Implement listener methods as needed

}
