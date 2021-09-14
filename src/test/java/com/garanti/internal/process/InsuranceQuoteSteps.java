package com.garanti.internal.process;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garanti.insurancequote.Decision;
import com.garanti.insurancequote.Person;
import com.garanti.internal.pam.helpers.KieServicesClientHelper;
import com.google.inject.Inject;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class InsuranceQuoteSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsuranceQuoteSteps.class);

    private static final String URL = System.getProperty("kie.server.url",
            "http://localhost:8080/kie-server/services/rest/server");
    private static final String USERNAME = System.getProperty("kie.server.user", "rhpamAdmin");
    private static final String PASSWORD = System.getProperty("kie.server.password", "Pa$$w0rd");
    private static final String CONTAINER_ID = "insurancequote";
    private static final String PROCESS_DEFINITION_ID = "insurancequote.quote";

    RetryPolicy<Object> retryPolicy = new RetryPolicy<>() //
        .handle(Exception.class) //
        .withDelay(Duration.ofSeconds(1)) //
        .withMaxRetries(3);

    @Inject
    private SharedState sharedState;

    @Before
    public void beforTest(Scenario scenario) {
        LOGGER.info("######################### INIT SCENARIO {} #########################", scenario.getName());
        final KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID,
                new ReleaseId("com.garanti", "insurancequote", "1.0.0-SNAPSHOT"));
        getBaseServiceClient().createContainer(CONTAINER_ID, containerResource);
    }

    @After
    public void afterTest(Scenario scenario) {
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final QueryServicesClient queryServicesClient = getBaseServiceClient().getServicesClient(QueryServicesClient.class);
        final List<ProcessInstance> activeProcesses = queryServicesClient.findProcessInstancesByProcessId("insurancequote.quote", Arrays.asList(1) , 0, Integer.MAX_VALUE); //Arrays.asList("open")
       //  List<ProcessInstance> activeProcesses = processServicesClient.findProcessInstances(CONTAINER_ID, 0, Integer.MAX_VALUE);
    	activeProcesses.stream().forEach(pi -> processServicesClient.abortProcessInstance(CONTAINER_ID, pi.getId()));
        getBaseServiceClient().deactivateContainer(CONTAINER_ID);
        getBaseServiceClient().disposeContainer(CONTAINER_ID);
        LOGGER.info("######################### END SCENARIO {} #########################", scenario.getName());
    }

    private KieServicesClient getBaseServiceClient(){
        return KieServicesClientHelper.getInstance().getKieServicesClient(USERNAME, PASSWORD, URL, Person.class, Decision.class);
    }

    @Given("^a customer approached for insurance$")
    public void setup() throws Throwable {
        LOGGER.info("a process instance for definition id '{}' is started$", PROCESS_DEFINITION_ID);
    }
    
    @When("^a customer approached for insurance with the following data$")
    public void startProcessInstance() throws Throwable {
        LOGGER.info("a process instance for definition id '{}' is started$", PROCESS_DEFINITION_ID);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final AtomicReference<Long> processId = new AtomicReference<>();
        Failsafe.with(retryPolicy).run(() -> processId.set(processServicesClient.startProcess(CONTAINER_ID, PROCESS_DEFINITION_ID)));
        sharedState.setProcessId(processId.get());
        sharedState.setProcessDefinitionId(PROCESS_DEFINITION_ID);
    }

    @And("^the human task '(.*?)' is claimed by '(.*?)'$")
    public void humantaskClaimedBy(String humantaskName, String actorName) throws Throwable {
        LOGGER.info("the human task '{}' is claimed by '{}'", humantaskName, actorName);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final UserTaskServicesClient userTaskServicesClient = getBaseServiceClient().getServicesClient(UserTaskServicesClient.class);
        final AtomicReference<List<TaskSummary>> tasks = new AtomicReference<>();
        Failsafe.with(retryPolicy).run(() -> tasks.set(Arrays.asList(processServicesClient.getProcessInstance(CONTAINER_ID, sharedState.getProcessId()).getActiveUserTasks().getTasks())));
        assertEquals(1, tasks.get().size());
        final TaskSummary task = tasks.get().get(0);
        assertEquals(humantaskName, task.getName());
        Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.claimTask(CONTAINER_ID, task.getId(), actorName));
    }
    
    @And("^the human task '(.*?)' is '(.*?)' by '(.*?)'$")
    public void humantaskActionWithActor(String humantaskName, String action, String actorName) throws Throwable {
        LOGGER.info("the human task '{}' is '{}' by '{}'", humantaskName, action, actorName);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final UserTaskServicesClient userTaskServicesClient = getBaseServiceClient().getServicesClient(UserTaskServicesClient.class);
        final AtomicReference<List<TaskSummary>> tasks = new AtomicReference<>();
        Failsafe.with(retryPolicy).run(() -> tasks.set(Arrays.asList(processServicesClient.getProcessInstance(CONTAINER_ID, sharedState.getProcessId()).getActiveUserTasks().getTasks())));
        assertEquals(1, tasks.get().size());
        final TaskSummary task = tasks.get().get(0);
        assertEquals(humantaskName, task.getName());
        switch(action){
            case "Started":
                Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.startTask(CONTAINER_ID, task.getId(), actorName));
                break;
            case "Completed":
                Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.completeTask(CONTAINER_ID, task.getId(), actorName, null));
                break;
        }
    }

    @And("^the human task '(.*?)' is '(.*?)' by '(.*?)' with parameters$")
    public void humantaskActionWithActorAndParameters(String humantaskName, String action, String actorName, DataTable table) throws Throwable {
        LOGGER.info("the human task '{}' is '{}' by '{}'", humantaskName, action, actorName);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final UserTaskServicesClient userTaskServicesClient = getBaseServiceClient().getServicesClient(UserTaskServicesClient.class);
        final AtomicReference<List<TaskSummary>> tasks = new AtomicReference<>();
        Failsafe.with(retryPolicy).run(() -> tasks.set(Arrays.asList(processServicesClient.getProcessInstance(CONTAINER_ID, sharedState.getProcessId()).getActiveUserTasks().getTasks())));
        assertEquals(1, tasks.get().size());
        final TaskSummary task = tasks.get().get(0);
        assertEquals(humantaskName, task.getName());
        switch(action){
            case "Completed":
                final List<Map<String, String>> rows = table.asMaps(String.class, String.class);
                if(humantaskName.equals("DataEntry")) {
                    final Map<String, Object> parameters = new HashMap<>();
                    final Person person = new Person();
                    person.setFirstName(rows.get(0).get("firstName"));
                    person.setLastName(rows.get(0).get("lastName"));
                    person.setDob(rows.get(0).get("DOB"));
                    person.setFaceAmount(Integer.parseInt(rows.get(0).get("faceAmount")));
                    person.setIncome(rows.get(0).get("income"));
                    parameters.put("person", person);
                    Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.completeTask(CONTAINER_ID, task.getId(), actorName, parameters));
                } else if(humantaskName.equals("UW Decision")) {
                	final Map<String, Object> parameters = new HashMap<>();
                	Decision decison = new Decision();
                	decison.setUwDecision("Approved");
                	parameters.put("uwDecision", decison);
                	Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.completeTask(CONTAINER_ID, task.getId(), actorName, parameters));
                } else if(humantaskName.equals("Prepare Policy Documentation")) {
                	final Map<String, Object> parameters = new HashMap<>();
                	Failsafe.with(retryPolicy).run(() -> userTaskServicesClient.completeTask(CONTAINER_ID, task.getId(), actorName, parameters));
                } 
                break;
        }
    }
    
    @Then("^the decision from decision chart is")
    public void decisionChartResult(DataTable table) throws Throwable {
        LOGGER.info("a process instance for definition id '{}' is started$", PROCESS_DEFINITION_ID);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        final AtomicReference<Map<String, Object>> processVariables = new AtomicReference<>();
        Failsafe.with(retryPolicy).run(() -> processVariables.set(processServicesClient.getProcessInstance(CONTAINER_ID, sharedState.getProcessId(), true).getVariables()));
        assertEquals(3, processVariables.get().size());
        final List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        assertEquals(processVariables.get().get("dmnDecision"), rows.get(0).get("chartDecision"));
    }

    @And("^the signal '(.*?)' is '(.*?)'")
    public void signalCheckStatus(String signalName, String action) throws Throwable {
        LOGGER.info("the signal  '{}' is '{}'", signalName, action, action);
        final ProcessServicesClient processServicesClient = getBaseServiceClient().getServicesClient(ProcessServicesClient.class);
        switch(action){
            case "Received":
                if(signalName.equals("Received completed VAA from supplier")) {
                	Failsafe.with(retryPolicy).run(() -> processServicesClient.signal(CONTAINER_ID, "VAA_Completion_Email_Received", null));
                }
                break;
        }
    }

    
    @And("^the service task '(.*?)' is '(.*?)'$")
    public void serviceTaskCheckStatus(String serviceTaskName, String status) throws Throwable {
        LOGGER.info("the service task '{}' status is '{}'", serviceTaskName, status);
    }

}
