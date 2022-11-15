# Releasing a new Mojarra version

In below example we assume 3.0.3.

1. Check beforehand that this version does NOT exist in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/jakarta.faces/) else bump the version
2. Go to [Mojarra CI](https://ci.eclipse.org/mojarra/)
3. [Log in](https://ci.eclipse.org/mojarra/login?from=%2Fmojarra%2F)
4. Open [1_mojarra_build_and_stage](https://ci.eclipse.org/mojarra/job/1_mojarra-build-and-stage/)
5. Click [Build with parameters](https://ci.eclipse.org/mojarra/job/1_mojarra-build-and-stage/build) in menu 
    - `BRANCH` = `3.0`
    - `RELEASE_VERSION` = `3.0.3`
    - `JDK` = `JDK8`
    - click [Build] button
6. Wait for it to finish successfully
7. Drill down into this build e.g. [build 95](https://ci.eclipse.org/mojarra/job/1_mojarra-build-and-stage/95)
8. Click [Console Output](https://ci.eclipse.org/mojarra/job/1_mojarra-build-and-stage/95/console) in menu
9. Ctrl+F 'orgglassfish', to find release ID, e.g. `Created staging repository with ID "orgglassfish-1230"`, remember this for `STAGING_RELEASE_ID` in a later step
10. Verify that it's present in [staging](https://jakarta.oss.sonatype.org/content/repositories/staging/org/glassfish/jakarta.faces/)
11. Verify that a new [3.0.3](https://github.com/eclipse-ee4j/mojarra/tree/3.0.3) branch is created 
12. Verify that a new [3.0.3-RELEASE](https://github.com/eclipse-ee4j/mojarra/releases/tag/3.0.3-RELEASE) tag is created 
13. Open [2_mojarra-run-tck-against-staged-build_3_0](https://ci.eclipse.org/mojarra/job/2_mojarra-run-tck-against-staged-build_3_0) (there are separate ones for 2.3 and 4.0)
14. Click [Build with parameters](https://ci.eclipse.org/mojarra/job/2_mojarra-run-tck-against-staged-build_3_0/build) in menu
    - `IMPL_SOURCE` = `STAGE`
    - `IMPL_VERSION` = `3.0.3`
    - `IMPL_BRANCH` = `3.0`
    - click [Build] button
15. Wait for it to finish successfully
16. Open [3_mojarra-staging-to-release](https://ci.eclipse.org/mojarra/job/3_mojarra-staging-to-release/)
17. Click [Build with parameters](https://ci.eclipse.org/mojarra/job/3_mojarra-staging-to-release/build) in menu
    - `STAGING_RELEASE_ID` = `orgglassfish-1230`
    - click [Build] button
18. Wait for it to finish successfully
19. Verify that it's present in [Maven Central](https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/) (might take up to a hour)
20. If everything is OK, then merge 3.0.3 branch into 3.0 via PR
21. Delete the 3.0.3 branch after merge
