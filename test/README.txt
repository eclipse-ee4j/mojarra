
Introduction
============

 This README contains helpful information to get you started with testing.
 
 Note this README does not deal with building Mojarra from source, see the
 top level directory README for more information on that.

Install the required JAR files
==============================

  You will need to install some JAR files into your local Maven repository.
  Execute the following command line from the top level test directory.

     mvn clean install
 

Use Glassfish for testing
=========================

  1. Configure settings.xml
  2. Prepare Glassfish
  3. Start Glassfish
  4. Deploy the web application(s) you want to test
  5. Run the tests
  6. Stop Glassfish

 
 Configure settings.xml
 ----------------------

  Make sure you have a glassfish.patch.home and glassfish.cargo.home defined
  in your settings.xml

    <properties>
        <glassfish.cargo.home>C:/Glassfish4.0</glassfish.cargo.home>
        <glassfish.patch.home>C:/Glassfish4.0</glassfish.patch.home>
    </properties>
      
 
 Prepare Glassfish
 -----------------

  Copy the Mojarra version you want to test against into the Glassfish modules
  directory.

    mvn -N -Pglassfish-patch [-Djsf.version=x.y.z] validate

  Note if you do not pass jsf.version it will default to the version under 
  development.


 Start your Glassfish server
 ---------------------------

  Start your Glassfish server.

    mvn -N -Pglassfish-cargo cargo:start
  

 Deploy the web application(s) you want to test
 ----------------------------------------------

  From any of the sub directories within the top level test directory you can
  deploy all the web applications in that directory, which will cause all of
  the web applications inside that directory to be deployed.

  E.g. if you want to deploy all the tests that should work on a Servlet 3.0
  compliant container you would issue the following command from within the
  servlet30 directory

    mvn -Pglassfish-cargo 
      [-Dwebapp.projectStage=[Production|Development]]
      [-Dwebapp.partialStateSaving=[true|false]] 
      [-Dwebapp.stateSavingMethod=[server|client]] cargo:redeploy

  Note: if you do not pass -Dwebapp.projectStage it will default to Production,
  likewise if you do not pass -Dwebapp.partialStateSaving it will default to
  true and last if you do not pass -Dwebapp.stateSavingMethod it defaults to
  server.


 Run the tests
 -------------

  Once you have deployed the tests to Glassfish you are ready to go ahead and
  test. From the same directory you deployed the web application(s) you will 
  run the tests.

    mvn -Pintegration
      [-Dwebapp.projectStage=[Production|Development]]
      [-Dwebapp.partialStateSaving=[true|false]] 
      [-Dwebapp.stateSavingMethod=[server|client]] verify

  Note: Make sure you pass in the same -Dwebapp.xxx properties if you used them 
        during deployment.


 Stop your Glassfish server
 ---------------------------

  Stop your Glassfish server.

    mvn -N -Pglassfish-cargo cargo:stop


Convenience scripts
===================

 You will find some convenience scripts in the test/bin directory that can make
 it easier to do testing as they do the invocations described above in order.
 

More specific testing
=====================


 Running a single test
 ---------------------

  If you are using NetBeans, you can usually just put the cursor in the
  test method you want to run, set a breakpoint, and do "Debug Focused
  Test Method".

  To run a single test against a previously deployed web application, go into
  the project directory of the deployed web application and issue the following:

    mvn -Pintegration -Dit.test=IndexPageIT clean verify

  Note: IndexPageIT is the name of the test class (note you can use regular
        expressions to match more than one test class here).

  Note: Make sure you pass in the same -Dwebapp.xxx properties if you used them 
        during deployment.

 
 Running a single test and a single method
 -----------------------------------------

  To run a single test and single method against a running container use:

    mvn -Pintegration -Dit.test=VersionPageIT#testVerifyMojarraVersion verify

  Note: VersionPageIT is the name of the test class, and testVerifyMojarraVersion
        the name of the test method (note you can use regular expressions to 
        match multiple test and methods).

  To run a single unit test:

    mvn -Dtest=FactoryFinderTestCase#testNoFacesContextInitially test

    Note that when you make a change in the core code you must re-run
    mvn.deploy.snapshot.local at the top level to cause the unit tests
    to see the change.

Testing using other servers
===========================

  Please see README-Weblogic.text for using Weblogic.

Pitfalls
========

 Since we are assuming a particular way of deployment you as the developer will
 have to be aware of the following pitfalls that might crop up if you are doing
 testing against different application servers.

    1. Make sure the context root in glassfish-web.xml matches project.build.finalName,
       if you do not the test harness will not be able to run the tests since it
       relies on the integration.url used by the integration profile to be build up 
       in the following way 

        ${integration.protocol}://${integration.serverName}:$integration.serverPort}/${project.build.finalName}/

Function of sub-directories in top-level test directory
=======================================================

 * agnostic         - Application server agnostic tests (do not put any new projects in here).
 * cluster          - Cluster specific tests.
 * glassfish        - Glassfish specific tests.
 * javaee6          - JavaEE 6 specific tests.
 * javaee6web       - JavaEE 6 Web-Profile specific tests.
 * javaee7          - JavaEE 7 specific tests.
 * javaee8          - JavaEE 8 specific tests.
 * osgi             - Glassfish OSGi specific tests.
 * performance      - Performance related tests.
 * protocolaware    - Protocol-aware tests (HTTP(S) switching related).
 * quality          - Quality project that tracks source code quality.
 * selenium         - Selenium tests.
 * servlet30        - Servlet 3.0 specific tests.
 * servlet31        - Servlet 3.1 specific tests.
 * servlet40        - Servlet 4.0 specific tests.
 * unit             - Unit tests.
 * util             - Utililty JAR project for use by test projects.
 * weblogic         - Weblogic specific tests.

--END
