/**
 * Deploy driver for project deployment.
 *
 * @param projectDeployDirectory The project's deployment directory.
 * @param ansibleVaultToken The ansible vault pass key.
 * @param platform The platform to deploy to, for example "Azure" or "AWS".
 * @param environment The environment to deploy to, for example "dev" or "test".
 */
void deploy(String projectDeployDirectory, String ansibleVaultToken, String platform, String environment) {
  String externalScriptPath = "${projectDeployDirectory}/JenkinsPipeline.groovy"
  echo "Loading supporting Jenkins pipeline scripts ${externalScriptPath}..."

  def external = load(externalScriptPath)
  if (external != null) {
      withCredentials([string(credentialsId: ansibleVaultToken, variable: 'ANSIBLE_VAULT_TOKEN')]) {
          external.deploy(projectDeployDirectory, "${platform}:${environment}")
      }
  } else {
      echo "External Jenkins pipeline script reference is null."
  }
}

String readBuildVersion(String projectDeployDirectory) {
  return readFile("${projectDeployDirectory}/metadata/build.version").trim()
}

String readBuildRevision(String projectDeployDirectory) {
  return readFile("${projectDeployDirectory}/metadata/build.revision").trim()
}

def readBuildProperties(String projectDeployDirectory) {
  def properties = readProperties file: "${projectDeployDirectory}/metadata/build.properties"
  return properties
}

String getApiKey(String projectDeployDirectory) {
  def properties = readBuildProperties(projectDeployDirectory) 
  return properties.API_KEY
}

boolean requestDeploymentDecision(Integer secondsToTimeout, String environmentType) {
  def userInput = true
  def didTimeout = false
  def user = "None"

  // Catch any exceptions that will help us determine who aborted the build or if there was a timeout.
  try {
    timeout(time: secondsToTimeout, unit: 'SECONDS') { // change to a convenient timeout for you
      userInput = input(
        id: 'DeployToEnv',
        message: "Deploy this to ${environmentType}?",
        ok: 'Vote',
        parameters: [
          [
            $class: 'BooleanParameterDefinition',
            defaultValue: true,
            description: 'Please make your selection below:',
            name: "Cast my vote in favor of pushing to ${environmentType}"
          ]
        ]
      )
    }
  } catch(err) { // timeout reached or input false
    echo "Errors: [${err}]"
    user = err.getCauses()[0].getUser()
    if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
      didTimeout = true
    } else {
      userInput = false
      echo "Aborted by: [${user}]"
    }
  }

  if (!userInput) {
    echo "The user decided to abort deployment..."
    currentBuild.result = 'ABORTED'
    //throw new Exception("Aborted by [${user}]")
  } else if (didTimeout) {
    echo "A user was undecided about a [${environmentType}] deployment."
    //currentBuild.result = 'NOT_BUILT'
    //throw new Exception("No Deployment Vote")
  } else if (userInput) {
    // If user input was a positive vote.
    return (true)
  } else {
    echo "The decision reached, was not expected."
    currentBuild.result = 'UNSTABLE'
    throw new Exception("Unexpected voting conditions")
  }

  return (false)
}

return this;
