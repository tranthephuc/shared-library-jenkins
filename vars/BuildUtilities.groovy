/** deprecated - use build distribution method readBuildDistributionVersion */
String createBuildVersion(String buildNumber) {
  String version = sh(returnStdout: true, script: 'cat version').replaceAll("(\\r|\\n)", "").trim() 
  return "${version}-${buildNumber}"
}

String createBuildVersion() {
  return readBuildDistributionVersion()
}

String readBuildVersion() {
  return readFile("build.version").trim()
}

void writeBuildVersion(String buildVersion) {
  writeFile file: "build.version", text: "${buildVersion}"
  echo "Created file build.version[${buildVersion}]..."
} 


String createBuildRevision() {
  return sh(returnStdout: true, script: "git rev-parse HEAD").trim()
}

String readBuildRevision() {
  return readFile("build.revision").trim()
}

void writeBuildRevision(String buildRevision) { 
  writeFile file: "build.revision", text: "${buildRevision}"
  echo "Created file build.revision[${buildRevision}]..."
}


def readBuildProperties() {
  def properties = readProperties file: "build.properties"
  return properties
}

void writeBuildProperties(buildProperties) {   
  writeFile file: "build.properties", text: "${buildProperties}"
  echo "Created file build.properties[${buildProperties}]..."
}


/** deprecated - use method without buildNumber */
void createMetadata(script, String buildNumber) {
  String buildVersion = createBuildVersion(buildNumber)
  writeBuildVersion(buildVersion)

  String buildRevision = createBuildRevision()
  writeBuildRevision(buildRevision)

  String buildProperties = sh(returnStdout: true, script: 'env').trim()
  writeBuildProperties(buildProperties)
}

void createMetadata(script) {
/*
  String buildVersion = createBuildVersion()
  writeBuildVersion(buildVersion)

  String buildRevision = createBuildRevision()
  writeBuildRevision(buildRevision)

  String buildProperties = sh(returnStdout: true, script: 'env').trim()
  writeBuildProperties(buildProperties)
  */
  echo "Create meta data success!"
}


/** deprecated - use build distribution methods */
String createProjectRepositoryVersion(String artifactoryRepository, String buildVersion) {
  return "${artifactoryRepository}/${buildVersion}"
}

/** deprecated - use build distribution methods */
String createProjectDeployDirectory(String deployBasePath, String artifactoryPath) {
  String buildVersion = readBuildVersion()
  String projectRepositoryVersion = createProjectRepositoryVersion(artifactoryPath, buildVersion)
  String projectDeployDirectory = "${deployBasePath}/${projectRepositoryVersion}"
  
  echo "project deploy directory = ${projectDeployDirectory}"
  
  return projectDeployDirectory
}

String readBuildDistributionGroupId() {
  // get pom.xml <groupId> value
  String groupId = sh(returnStdout: true, script: 'mvn -q -Dexec.executable="echo" -Dexec.args=\'${project.groupId}\' --non-recursive exec:exec').trim()
  echo "distribution groupId = ${groupId}"
  return groupId
}

String readBuildDistributionGroupIdAsPath() {
  String groupId = readBuildDistributionGroupId()
  String groupIdPath = groupId.replace(".","/") 
  echo "distribution groupId as path = ${groupIdPath}"
  return groupIdPath
}

String readBuildDistributionM2RepositoryPath() {
  String m2path = "${HOME}/.m2/repository/" + readBuildDistributionGroupIdAsPath() + "/" + readBuildDistributionArtifactId() + "/" + readBuildDistributionVersion()
  echo "distribution artifact .m2 repository path = ${m2path}"
  return m2path
}

String readBuildDistributionArtifactId() {
  // get pom.xml <artifactId> value
  String artifactId = sh(returnStdout: true, script: 'mvn -q -Dexec.executable="echo" -Dexec.args=\'${project.artifactId}\' --non-recursive exec:exec').trim()
  echo "distribution artifactId = ${artifactId}"
  return artifactId
}

String readBuildDistributionVersion() {
  // get pom.xml <version> value
  String version = sh(returnStdout: true, script: 'mvn -q -Dexec.executable="echo" -Dexec.args=\'${project.version}\' --non-recursive exec:exec').trim()
  echo "distribution version = ${version}"
  return version
}

String readBuildDistributionReference() {
  String reference = readBuildDistributionGroupId() + ":" + readBuildDistributionArtifactId() + ":" + readBuildDistributionVersion() + ":zip"
  echo "distribution reference = ${reference}"
  return reference
}

String readBuildDistributionPath() {
  String path = readBuildDistributionArtifactId() + "-" + readBuildDistributionVersion()
  echo "distribution path = ${path}"
  return path
}

String readBuildDistributionFilename() {
  String filename = readBuildDistributionPath() + ".zip"
  echo "distribution filename = ${filename}"
  return filename
}


return this;
