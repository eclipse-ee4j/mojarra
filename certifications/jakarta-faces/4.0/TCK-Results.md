TCK Results
===========

As required by the
[Eclipse Foundation Technology Compatibility Kit License](https://www.eclipse.org/legal/tck.php),
following is a summary of the TCK results for releases of Jakarta Faces.

# Eclipse Mojarra 4.0.0, Certification Summary

- Product Name, Version and download URL (if applicable): <br/>
  [Mojarra 4.0.0-M7](https://github.com/eclipse-ee4j/mojarra/releases/download/4.0.0-M7-RELEASE/jakarta.faces-4.0.0-M7.jar)
  Vehicle: [Eclipse Glassfish 7.0.0-M4](https://repo1.maven.org/maven2/org/glassfish/main/distributions/glassfish/7.0.0-M4/glassfish-7.0.0-M4.zip)
- Specification Name, Version and download URL: <br/>
  [Jakarta Server Faces, 4.0](https://jakarta.ee/specifications/faces/4.0)
- TCK Version, digital SHA-256 fingerprint and download URL: <br/>
  [Jakarta Server Faces TCK 4.0.0](https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-faces-tck-4.0.0.zip), SHA-256: `0211baea7856dc376a978a326deeba4efe7956e80e887adc6151e28bc9380f00`
- Public URL of TCK Results Summary: <br/>
  [TCK results summary](TCK-Results.html)
- Any Additional Specification Certification Requirements: <br/>
  None
- Java runtime used to run the implementation: <br/>
  OpenJDK 11.0.2, 17.0.2
- Summary of the information for the certification environment, operating system, cloud, ...: <br/>
  Debian GNU/Linux 10 and macOS 12.3 (on M1)

Test results:

```
********************************************************************************
Completed running 260 tests.
Number of Tests Failed      = 0
Number of Tests with Errors = 0
********************************************************************************

 [exec] [javatest.batch] ********************************************************************************
 [exec] [javatest.batch] Completed running 5400 tests.
 [exec] [javatest.batch] Number of Tests Passed      = 5400
 [exec] [javatest.batch] Number of Tests Failed      = 0
 [exec] [javatest.batch] Number of Tests with Errors = 0
 [exec] [javatest.batch] ********************************************************************************

SHA256_IMPL=
SHA256_TCK=0211baea7856dc376a978a326deeba4efe7956e80e887adc6151e28bc9380f00
TCK_download=https://download.eclipse.org/ee4j/jakartaee-tck/jakartaee10/staged/eftl/jakarta-faces-tck-4.0.0.zip
OS2=Debian GNU/Linux 10
OS3=10.12
OS4=PRETTY_NAME="Debian GNU/Linux 10 (buster)" NAME="Debian GNU/Linux" VERSION_ID="10" VERSION="10 (buster)" VERSION_CODENAME=buster ID=debian HOME_URL="https://www.debian.org/" SUPPORT_URL="https://www.debian.org/support" BUG_REPORT_URL="https://bugs.debian.org/"
JDK_VERSION=openjdk version "11.0.2" 2019-01-15 OpenJDK Runtime Environment 18.9 (build 11.0.2+9) OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
JDK_VERSION=OpenJDK Runtime Environment (build 17.0.2+8-86) OpenJDK 64-Bit Server VM (build 17.0.2+8-86, mixed mode, sharing)
```
