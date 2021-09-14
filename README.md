# JBPM Process Testing Using Cucumber

This project to test the JBPM processes with Cucumber by implementing some useful integrations and default step definitions.  Here are the features:

1. BPMN assets to be tested are included as a dependency in the [pom.xml](pom.xml) file.  This way the tests can be separate from the process definitions.
  - The example KJAR in the pom.xml can be found at https://github.com/rmuppane/insurancequote
2. Default [step definitions](src/test/java/com/garanti/internal/process/InsuranceQuoteSteps.java) and [feature file](src/test/resources/features/insurance-quote.feature) define an easy way to interact with processes.
  - As of version 1.0.0, the default step definitions include some task steps
3. [KieServicesClientHelper](src/test/java/com/garanti/internal/helpers/KieServicesClientHelper.java) used to connet the kie-server. [InsuranceQuoteSteps](src/test/java/com/garanti/internal/process/InsuranceQuoteSteps.java) have the connectivity details, Update the kie.server.url, kie.server.user and kie.server.password to communicated with kie-server.
4. A [Test class](src/test/java/com/garanti/internal/process/RunnerTest.java) is used for step definitions.


Instructions:

1. Clone this repo.
2. If using and IDE, recommend adding the [Cucumber plugin](https://cucumber.io/docs/tools/java/)
3. Include your own KJAR(s) in the [pom.xml](pom.xml) file.  The [loanapproval.feature](src/test/resources/features/loanapproval.feature) file uses the KJAR from  https://github.com/rmuppane/loanapproval. So clone it and perform `mvn clean install`.
4. Run the test using `mvn clean test` or test through your IDE.
5. Modify the [user groups properties file](src/test/resources/usergroups.properties) to customize user group callback.

Environment:

1. Java 8 or 11
2. Product version and librarys: 7.11, 7.52.0.Final-redhat-00008 or 7.11.1.redhat-00001
3. Junit 4
4. Cucumber 6.11.0
