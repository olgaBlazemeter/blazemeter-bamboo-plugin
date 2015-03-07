package com.blazemeter.bamboo.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blazemeter.bamboo.plugin.api.BzmServiceManager;
import com.blazemeter.bamboo.plugin.api.TestInfo;
import com.blazemeter.bamboo.plugin.configuration.BlazeMeterConstants;
import com.blazemeter.bamboo.plugin.servlet.AdminServlet.Config;
import org.apache.commons.lang3.StringUtils;

public class BlazeMeterTaskType implements TaskType{
	private static final int CHECK_INTERVAL = 60000;
    private static final int INIT_TEST_TIMEOUT = 600000;

    private int testDuration;
	int errorUnstableThreshold;
	int errorFailedThreshold;
	int responseTimeUnstableThreshold;
	int responseTimeFailedThreshold;
	String dataFolder;
	String mainJMX;
	
	boolean needTestUpload;
	File rootDirectory;

	ProcessService processService;
	private final PluginSettingsFactory pluginSettingsFactory;
	
	public BlazeMeterTaskType(final ProcessService processService, PluginSettingsFactory pluginSettingsFactory){
		this.processService = processService;
		this.pluginSettingsFactory = pluginSettingsFactory;
	}

 	@Override
	public TaskResult execute(TaskContext context) throws TaskException {
        final BuildLogger logger = context.getBuildLogger();
        final CurrentBuildResult currentBuildResult = context.getBuildContext().getBuildResult();
        TaskResultBuilder resultBuilder = TaskResultBuilder.create(context);
        ConfigurationMap configMap = context.getConfigurationMap();
        logger.addBuildLogEntry("BlazeMeter execute task");
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        String config = (String) pluginSettings.get(Config.class.getName() + BlazeMeterConstants.PROXY_USER_KEY);
        String proxyserver = (String) pluginSettings.get(Config.class.getName() + BlazeMeterConstants.PROXY_SERVER);
        String proxyport = (String) pluginSettings.get(Config.class.getName() + BlazeMeterConstants.PROXY_PORT);
        String proxyuser = (String) pluginSettings.get(Config.class.getName() + BlazeMeterConstants.PROXY_USER);
        String proxypass = (String) pluginSettings.get(Config.class.getName() + BlazeMeterConstants.PROXY_PASS);
        String testId = configMap.get(BlazeMeterConstants.SETTINGS_SELECTED_TEST_ID);


            if (StringUtils.isBlank(config)) {
                logger.addErrorLogEntry("BlazeMeter user key not defined!");
                return resultBuilder.failed().build();
            }
        BzmServiceManager bzmServiceManager= bzmServiceManager = BzmServiceManager.getBzmServiceManager(
                proxyserver,
                proxyport,
                proxyuser,
                proxypass,
                "v3");
        if(!bzmServiceManager.verifyUserKey(config)){
            return resultBuilder.failedWithError().build();
        }
        bzmServiceManager.setTestId(testId);
        bzmServiceManager.setUserKey(config);

        rootDirectory = context.getRootDirectory();

        logger.addBuildLogEntry("Attempting to start test with id:" + testId);
        boolean started = bzmServiceManager.startTest(testId, logger, currentBuildResult);
        long testInitStart=System.currentTimeMillis();

        if (!started) {
                return resultBuilder.failed().build();
            } else {
                if (bzmServiceManager.getSession() != null) {//save the session id to the build custom data map
                    context.getBuildContext().getBuildResult().getCustomBuildData().put("session_id", bzmServiceManager.getSession().toString());
                } else {
                    logger.addErrorLogEntry("Failed to retrieve test session id! Report will not be available for this test!");
                }
//                logger.addBuildLogEntry("Test started. Waiting for finishing...");
            }

            long totalWaitTime = (testDuration + 2) * 60 * 1000;//the duration is in minutes so we multiply to get the value in ms
            long nrOfCheckInterval = totalWaitTime / CHECK_INTERVAL;//
            long currentCheck = 0;

            TestInfo testInfo;
        boolean initTimeOutPassed=false;

        do{
            Utils.sleep(CHECK_INTERVAL);
            testInfo = bzmServiceManager.getTestStatus();
            logger.addBuildLogEntry("Check if the test is initialized...");
            initTimeOutPassed=System.currentTimeMillis()>testInitStart+INIT_TEST_TIMEOUT;
        }while (!(initTimeOutPassed|testInfo.getStatus().equals(TestStatus.Running.toString())));

        if(testInfo.getStatus().equals(TestStatus.NotRunning.toString())){
            logger.addErrorLogEntry("Test was not initialized, marking build as failed.");
            return resultBuilder.failedWithError().build();
        }

        while (currentCheck++ < nrOfCheckInterval) {
                try {
                    Thread.currentThread().sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    logger.addErrorLogEntry("BlazeMeter Interrupted Exception: " + e.getMessage());
                    break;
                }

                logger.addBuildLogEntry("Check if the test is still running. Time passed since start:" + ((currentCheck * CHECK_INTERVAL) / 1000 / 60) + " minutes.");
                testInfo = bzmServiceManager.getTestStatus();
                if (testInfo.getStatus().equals(TestStatus.NotRunning.toString())) {
                    logger.addBuildLogEntry("Test is finished earlier then estimated! Time passed since start:" + ((currentCheck * CHECK_INTERVAL) / 1000 / 60) + " minutes.");
                    break;
                } else if (testInfo.getStatus().equals(TestStatus.NotFound.toString())) {
                    logger.addErrorLogEntry("BlazeMeter test not found!");
                    return resultBuilder.failed().build();
                }
            }

            //BlazeMeter test stopped due to user test duration setup reached
            logger.addBuildLogEntry("Stopping test...");
            bzmServiceManager.stopTest(testId, logger, currentBuildResult);

            logger.addBuildLogEntry("Test finished. Checking for test report...");
            bzmServiceManager.getReport(logger);
            return resultBuilder.success().build();
        }

	private String validateLocalTresholds(Map<String, String> params, BuildLogger logger) {

		String errorUnstable = params.get(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		String errorFail = params.get(BlazeMeterConstants.SETTINGS_ERROR_THRESHOLD_FAIL);
		String timeUnstable = params.get(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		String timeFail = params.get(BlazeMeterConstants.SETTINGS_RESPONSE_TIME_FAIL);

		try{
			errorFailedThreshold = errorFail.isEmpty()?-1:Integer.valueOf(errorFail);
		} catch (NumberFormatException nfe){
			return "Error threshold failed is not a number.";
		}
		try{
			errorUnstableThreshold = errorUnstable.isEmpty()?-1:Integer.valueOf(errorUnstable);
		} catch (NumberFormatException nfe){
			return "Error threshold unstable is not a number.";
		}
		try{
			responseTimeFailedThreshold = timeFail.isEmpty()?-1:Integer.valueOf(timeFail);
		} catch (NumberFormatException nfe){
			return "Response time failed is not a number.";
		}
		try{
			responseTimeUnstableThreshold = timeUnstable.isEmpty()?-1:Integer.valueOf(timeUnstable);
		} catch (NumberFormatException nfe){
			return "Response time unstable is not a number.";
		}


		return null;
	}
	
	/**
	 * Upload main JMX file and all the files from the data folder
	 */
    private void uploadDataFolderFiles(BuildLogger logger, CurrentBuildResult currentBuildResult) {
        BzmServiceManager bzmServiceManager=BzmServiceManager.getBzmServiceManager();
        logger.addBuildLogEntry("Uploading data files");
        if (dataFolder == null || dataFolder.isEmpty()){
            logger.addErrorLogEntry("Empty data folder. Please enter the path to your data folder or '.' for main folder where the files are checked out.");
            return;
        }
        
        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.addErrorLogEntry("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.");
            return ;
        } else {
        	logger.addBuildLogEntry("DataFolder "+dataFolder+" exists.");
        }

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String file;
            if (listOfFiles[i].isFile()) {
                file = listOfFiles[i].getName();
                if (file.endsWith(mainJMX)){
                	logger.addBuildLogEntry("Uploading main JMX "+mainJMX);
                    bzmServiceManager.uploadJMX(bzmServiceManager.getTestId(), mainJMX, dataFolder + File.separator + mainJMX);
                }
                else {
                	logger.addBuildLogEntry("Uploading data files "+file);
                	bzmServiceManager.uploadFile(bzmServiceManager.getTestId(), dataFolder, file, logger, currentBuildResult);
                }
            }
        }
    }
	}
