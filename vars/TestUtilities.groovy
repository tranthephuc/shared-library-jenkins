/**
 * Preappend JUnit style report testsuite elements with a given tag.
 *
 * @param tag The tag to prefix testsuite elements with.
 * @param resultPath The directory containing JUnit style reports to be preappended with a tag. Applied recursively.
 */
void labelTestResults(String tag, String resultPath) {
  echo "tag = ${tag}"
  echo "test result path = ${resultPath}"
  
  withEnv(["TAG=${tag}",
           "RESULT_PATH=${resultPath}"]) {
  
    dir(resultPath) {
      sh '''#!/bin/bash
      
      set -x -e
    
      pwd
      echo "TAG=${TAG}"
      echo "RESULT_PATH=${RESULT_PATH}"
      
      grep -rFIl "<testsuite name=\\"" . | xargs -r sed -i "s~<testsuite name=\\"~<testsuite name=\\"${TAG} - ~g"
      '''
    }
  }
}

/**
 * Run Postman tests.
 *
 * @param projectDeployDirectory The base directory where the deployment package is located.
 * @param platform The platform to test against, for example AWS or Azure.
 * @param environment The environment to test against, for example dev or test.
 */ 
void runPostman(String projectDeployDirectory, String platform, String environment) {
  String externalScriptPath = "${projectDeployDirectory}/JenkinsPipeline.groovy"
  echo "Loading supporting Jenkins pipeline scripts ${externalScriptPath}"

  def external = load(externalScriptPath)
  try {
      if (external != null) {
          external.runPostman(projectDeployDirectory, "${platform}:${environment}")
      } else {
          echo "External Jenkins pipeline script reference is null."
      }
  } catch(err) {
      echo "error= $err"
      if ("${SUPPRESS_TEST_FAILURES}" != "true") {
          throw err
      }
  } finally {
      if (external != null) {
          String resultPath = external.getPostmanResults(projectDeployDirectory, environment)
          echo "test result path = ${resultPath}"
          junit resultPath
      }
  }
}

/**
 * Run SoapUI tests.
 *
 * @param projectDeployDirectory The base directory where the deployment package is located.
 * @param mavenSettingsFileId The Jenkins config file id for the expected maven settings.xml.
 * @param platform The platform to test against, for example AWS or Azure.
 * @param environment The environment to test against, for example dev or test.
 */
void runSoapUI(String projectDeployDirectory, String mavenSettingsFileId, String platform, String environment) {
  String externalScriptPath = "${projectDeployDirectory}/JenkinsPipeline.groovy"
  echo "Loading supporting Jenkins pipeline scripts ${externalScriptPath}"
  echo "maven settings file id = ${mavenSettingsFileId}"
  
  def external = load(externalScriptPath)
  try {
      if (external != null) {
          configFileProvider([configFile(fileId: mavenSettingsFileId, variable: 'MAVEN_SETTINGS')]) {
              external.runSoapUI(projectDeployDirectory, "${platform}:${environment}")
          }
      } else {
          echo "External Jenkins pipeline script reference is null."
      } 
  } catch(err) {
      echo "error= $err"
      if ("${SUPPRESS_TEST_FAILURES}" != "true") {
          throw err
      }
  } finally {
      if (external != null) {
          String resultPath = external.getSoapUIResults(projectDeployDirectory, environment)
          echo "test result path = ${resultPath}"
          junit resultPath
      }
  }
}

/**
 * Run SoapUI tests.
 *
 * @param projectDeployDirectory The base directory where the deployment package is located.
 * @param mavenSettingsFileId The Jenkins config file id for the expected maven settings.xml.
 * @param platform The platform to test against, for example AWS or Azure.
 * @param environment The environment to test against, for example dev or test.
 */
void runAutomated(String projectDeployDirectory, String mavenSettingsFileId, String platform, String environment) {
  String externalScriptPath = "${projectDeployDirectory}/JenkinsPipeline.groovy"
  echo "Loading supporting Jenkins pipeline scripts ${externalScriptPath}"
  echo "maven settings file id = ${mavenSettingsFileId}"
  
  def external = load(externalScriptPath)
  try {
      if (external != null) {
          configFileProvider([configFile(fileId: mavenSettingsFileId, variable: 'MAVEN_SETTINGS')]) {
              external.runAutomated(projectDeployDirectory, "${platform}:${environment}")
          }
      } else {
          echo "External Jenkins pipeline script reference is null."
      } 
  } catch(err) {
      echo "error= $err"
      if ("${SUPPRESS_TEST_FAILURES}" != "true") {
          throw err
      }
  } finally {
      if (external != null) {
          String resultPath = external.getAutomatedResults(projectDeployDirectory, environment)
          echo "test result path = ${resultPath}"
          junit resultPath
      }
  }
}
  
return this;

