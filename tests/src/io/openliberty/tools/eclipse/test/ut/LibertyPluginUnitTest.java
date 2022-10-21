package io.openliberty.tools.eclipse.test.ut;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.openliberty.tools.eclipse.ui.launch.LaunchConfigurationDelegateLauncher;
import io.openliberty.tools.eclipse.ui.launch.LaunchConfigurationDelegateLauncher.RuntimeEnv;
import io.openliberty.tools.eclipse.ui.launch.MainTab;

public class LibertyPluginUnitTest {

    /**
     * Runs before each test.
     */
    @BeforeEach
    public void beforeEach(TestInfo info) {
        System.out.println("INFO: Test " + info.getDisplayName() + " entry: " + java.time.LocalDateTime.now());
    }

    /**
     * Runs after each test.
     */
    @AfterEach
    public void afterEach(TestInfo info) {
        System.out.println("INFO: Test " + info.getDisplayName() + " exit: " + java.time.LocalDateTime.now());
    }

    /**
     * Tests that run configurations are filtered correctly based on the project, run environment.
     * 
     * @throws Exception
     */
    @Test
    public void testConfigFiltering() throws Exception {
        List<ILaunchConfiguration> rawCfgList = getDefaultConfigurationList();

        // Test 1. Normal run.
        List<ILaunchConfiguration> filteredListDev = LaunchConfigurationDelegateLauncher
                .filterLaunchConfigurations(rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project1", RuntimeEnv.LOCAL);
        Assertions.assertTrue(filteredListDev.size() == 3,
                "The resulting list should have contained 3 entries. List size: " + filteredListDev.size());
        Assertions.assertTrue(filteredListDev.get(0).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, true) == false,
                "The run in container value associated with config entry[0] was not false.");
        Assertions.assertTrue(filteredListDev.get(1).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, true) == false,
                "The run in container value associated with config entry[1] was not false.");
        Assertions.assertTrue(filteredListDev.get(2).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, true) == false,
                "The run in container value associated with config entry[2] was not false.");

        // test 2. Container run.
        List<ILaunchConfiguration> filteredListDevc = LaunchConfigurationDelegateLauncher.filterLaunchConfigurations(
                rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project1", RuntimeEnv.CONTAINER);
        Assertions.assertTrue(filteredListDevc.size() == 3,
                "The resulting list should have contained 3 entries. Found: " + filteredListDevc.size() + ". List: " + filteredListDevc);
        Assertions.assertTrue(filteredListDevc.get(0).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, false) == true,
                "The run in container value associated with config entry[0] was not true.");
        Assertions.assertTrue(filteredListDevc.get(1).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, false) == true,
                "The run in container value associated with config entry[1] was not true.");
        Assertions.assertTrue(filteredListDevc.get(2).getAttribute(MainTab.PROJECT_RUN_IN_CONTAINER, false) == true,
                "The run in container value associated with config entry[2] was not true.");
    }

    /**
     * Tests that the run configuration that ran last is returned.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveLastRunConfig() throws Exception {
        List<ILaunchConfiguration> rawCfgList = getDefaultConfigurationList();
        List<ILaunchConfiguration> filteredListDev = LaunchConfigurationDelegateLauncher
                .filterLaunchConfigurations(rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project1", RuntimeEnv.LOCAL);
        Assertions.assertTrue(filteredListDev.size() == 3,
                "The resulting list should have contained 3 entries. List size: " + filteredListDev.size());

        // Test 1. Normal run.
        ILaunchConfiguration lastRunConfigDev = LaunchConfigurationDelegateLauncher.getLastRunConfiguration(filteredListDev);

        String cfgNameFoundDev = lastRunConfigDev.getName();
        String expectedCfgNameDev = "test2";
        Assertions.assertTrue(expectedCfgNameDev.equals(cfgNameFoundDev),
                "The expected configuration of " + expectedCfgNameDev + " was not returned. Configuration returned:: " + cfgNameFoundDev);

        long expectedTimeDev = 1000000000003L;
        long timeFoundDev = Long.valueOf(lastRunConfigDev.getAttribute(MainTab.PROJECT_RUN_TIME, "0"));
        Assertions.assertTrue(timeFoundDev == expectedTimeDev,
                "The configuration found does not contain the expected value of " + expectedTimeDev + ". Time found: " + timeFoundDev);

        // Test 2. Container run.
        List<ILaunchConfiguration> filteredListDevc = LaunchConfigurationDelegateLauncher.filterLaunchConfigurations(
                rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project1", RuntimeEnv.CONTAINER);
        Assertions.assertTrue(filteredListDevc.size() == 3,
                "The resulting list should have contained 3 entries. List size: " + filteredListDevc.size());

        ILaunchConfiguration lastRunConfigDevc = LaunchConfigurationDelegateLauncher.getLastRunConfiguration(filteredListDevc);

        String cfgNameFoundDevc = lastRunConfigDevc.getName();
        String expectedCfgNameDevc = "test6";
        Assertions.assertTrue(expectedCfgNameDevc.equals(cfgNameFoundDevc),
                "The expected configuration of " + expectedCfgNameDevc + " was not returned. Configuration returned:: " + cfgNameFoundDevc);

        long expectedTimeDevc = 1000000000006L;
        long timeFoundDevc = Long.valueOf(lastRunConfigDevc.getAttribute(MainTab.PROJECT_RUN_TIME, "0"));
        Assertions.assertTrue(timeFoundDevc == expectedTimeDevc,
                "The configuration found does not contain the expected value of " + expectedTimeDevc + ". Time found: " + timeFoundDevc);

        // Test 3: Normal run. Configurations with equal minimum time. Configuration with max time expected.
        List<ILaunchConfiguration> filteredListT3Dev = LaunchConfigurationDelegateLauncher
                .filterLaunchConfigurations(rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project2", RuntimeEnv.LOCAL);
        Assertions.assertTrue(filteredListT3Dev.size() == 3,
                "The resulting list should have contained 3 entries. List size: " + filteredListT3Dev.size());

        ILaunchConfiguration lastRunConfigT3Dev = LaunchConfigurationDelegateLauncher.getLastRunConfiguration(filteredListT3Dev);

        String cfgNameFoundT3Dev = lastRunConfigT3Dev.getName();
        String expectedCfgNameT3Dev = "test11";
        Assertions.assertTrue(expectedCfgNameT3Dev.equals(cfgNameFoundT3Dev), "The expected configuration of " + expectedCfgNameT3Dev
                + " was not returned. Configuration returned:: " + cfgNameFoundT3Dev);

        // Test 4: Container run. Configurations with equal max time. One of the max times is returned.
        // In this particular case, is the second entry after the entries are sorted.
        List<ILaunchConfiguration> filteredListT4Devc = LaunchConfigurationDelegateLauncher.filterLaunchConfigurations(
                rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project3", RuntimeEnv.CONTAINER);
        Assertions.assertTrue(filteredListT4Devc.size() == 3,
                "The resulting list should have contained 3 entries. List size: " + filteredListT4Devc.size());

        ILaunchConfiguration lastRunConfigT4Devc = LaunchConfigurationDelegateLauncher.getLastRunConfiguration(filteredListT4Devc);

        String cfgNameFoundT4Devc = lastRunConfigT4Devc.getName();
        String expectedCfgNameT4Devc = "test16";
        Assertions.assertTrue(expectedCfgNameT4Devc.equals(cfgNameFoundT4Devc), "The expected configuration of " + expectedCfgNameT4Devc
                + " was not returned. Configuration returned:: " + cfgNameFoundT4Devc);

        // Test 5: This is the start... case where we do not really know the runtime environment to be used to run dev mode.
        // In this case, it is expected that the configuration that ran last irrespective of runtime environment should be returned.
        List<ILaunchConfiguration> filteredListT5Dev = LaunchConfigurationDelegateLauncher.filterLaunchConfigurations(
                rawCfgList.toArray(new ILaunchConfiguration[rawCfgList.size()]), "project1", RuntimeEnv.UNKNOWN);
        Assertions.assertTrue(filteredListT5Dev.size() == 6,
                "The resulting list should have contained 6 entries. List size: " + filteredListT5Dev.size());

        ILaunchConfiguration lastRunConfigT5Dev = LaunchConfigurationDelegateLauncher.getLastRunConfiguration(filteredListT5Dev);

        String cfgNameFoundT5Dev = lastRunConfigT5Dev.getName();
        String expectedCfgNameT5Dev = "test6";
        Assertions.assertTrue(expectedCfgNameT5Dev.equals(cfgNameFoundT5Dev), "The expected configuration of " + expectedCfgNameT5Dev
                + " was not returned. Configuration returned:: " + cfgNameFoundT5Dev);
    }

    private List<ILaunchConfiguration> getDefaultConfigurationList() throws CoreException {
        ArrayList<ILaunchConfiguration> configList = new ArrayList<ILaunchConfiguration>();
        configList.add(mockLaunchConfiguration(Map.of("name", "test1", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000001", MainTab.PROJECT_RUN_IN_CONTAINER, false)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test2", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000003", MainTab.PROJECT_RUN_IN_CONTAINER, false)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test3", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000002", MainTab.PROJECT_RUN_IN_CONTAINER, false)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test4", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000004", MainTab.PROJECT_RUN_IN_CONTAINER, true)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test5", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000005", MainTab.PROJECT_RUN_IN_CONTAINER, true)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test6", MainTab.PROJECT_NAME, "project1", MainTab.PROJECT_RUN_TIME,
                "1000000000006", MainTab.PROJECT_RUN_IN_CONTAINER, true)));

        configList.add(mockLaunchConfiguration(Map.of("name", "test10", MainTab.PROJECT_NAME, "project2", MainTab.PROJECT_RUN_TIME,
                "1000000000010", MainTab.PROJECT_RUN_IN_CONTAINER, false)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test11", MainTab.PROJECT_NAME, "project2", MainTab.PROJECT_RUN_TIME,
                "1000000000011", MainTab.PROJECT_RUN_IN_CONTAINER, false)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test12", MainTab.PROJECT_NAME, "project2", MainTab.PROJECT_RUN_TIME,
                "1000000000010", MainTab.PROJECT_RUN_IN_CONTAINER, false)));

        configList.add(mockLaunchConfiguration(Map.of("name", "test15", MainTab.PROJECT_NAME, "project3", MainTab.PROJECT_RUN_TIME,
                "1000000000011", MainTab.PROJECT_RUN_IN_CONTAINER, true)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test16", MainTab.PROJECT_NAME, "project3", MainTab.PROJECT_RUN_TIME,
                "1000000000011", MainTab.PROJECT_RUN_IN_CONTAINER, true)));
        configList.add(mockLaunchConfiguration(Map.of("name", "test17", MainTab.PROJECT_NAME, "project3", MainTab.PROJECT_RUN_TIME,
                "1000000000010", MainTab.PROJECT_RUN_IN_CONTAINER, true)));

        return configList;
    }

    public static ILaunchConfiguration mockLaunchConfiguration(Map<String, Object> attributes) throws CoreException {
        ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getName()).thenReturn((String) attributes.get("name"));
        when(config.getAttribute(eq(MainTab.PROJECT_NAME), anyString())).thenReturn(((String) attributes.get(MainTab.PROJECT_NAME)));
        when(config.getAttribute(eq(MainTab.PROJECT_RUN_TIME), anyString()))
                .thenReturn(((String) attributes.get(MainTab.PROJECT_RUN_TIME)));
        when(config.getAttribute(eq(MainTab.PROJECT_RUN_IN_CONTAINER), anyBoolean()))
                .thenReturn(((Boolean) attributes.get(MainTab.PROJECT_RUN_IN_CONTAINER)).booleanValue());

        return config;
    }
}