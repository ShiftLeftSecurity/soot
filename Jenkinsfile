#!groovy
properties([disableConcurrentBuilds(), pipelineTriggers([pollSCM('H/3 * * * *')])])

env.REPO_NAME="github.com/ShiftLeftSecurity/soot"

node {
	try {
	    stage('cleanDir') {
            	try {
                	deleteDir()
            	} catch (err) {
                	println("WARNING: Failed to delete directory: " + err)
            	}
	    }
    	stage('getSrc') { // for display purposes
      	    // Get code from GitHub repository
      		checkout([$class: 'GitSCM', branches: [[name: '*/shiftleft-dev']], doGenerateSubmoduleConfigurations: false, submoduleCfg: [], userRemoteConfigs: [[credentialsId: '4b3482c3-735f-4c31-8d1b-d8d3bd889348', url: "ssh://git@${env.REPO_NAME}"]]])
    	}
    	stage('runBuild-Soot') {
      		withEnv(["JAVA_HOME=${ tool 'JDK8u121' }","ANT_HOME=${ tool 'Ant-1.10.0' }","MAVEN_HOME=${ tool 'Maven-3.3.9' }", "PATH+MAVEN=${tool 'Gradle-2.12'}/bin:${env.JAVA_HOME}/bin:${tool 'Ant-1.10.0'}/bin:${tool 'Maven-3.3.9'}/bin"]) {
         	    sh "mvn clean test deploy"
      		}     	    
    	}
    	stage('archiveBuild') {
      		archiveArtifacts 'target/soot*.jar'  
    	}
    	stage('archiveTestResults') {
      		//step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])
    	}
    	stage('archiveReports') {
      		//publishHTML (target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/surefire-reports/testng-junit-results/', reportFiles: 'index.html', reportName: "JUnit Test Results"])
      		//publishHTML (target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/surefire-reports/testng-native-results/', reportFiles: 'index.html', reportName: "Native Test Results"])
    	}
    	stage('checkFixed') {
    	    myBuildNumber = sh(returnStdout: true, script: 'echo $(($BUILD_NUMBER-1))').trim()
    	    withEnv(["PREV_BUILD_NUMBER=${myBuildNumber}"]) {
    	        myResult = sh(returnStatus: true, script: 'curl --silent --user admin:1ea44cdc86eefbf888dc2d480a9c9493 http://localhost:8080/job/$JOB_NAME/$PREV_BUILD_NUMBER/api/json | grep -q \"FAILURE\"')
      		}
      		if (myResult) {   
                // last build was success so ignore
            } else {  // send fixed email
                slackSend (channel: '#team-code-science', color: '#22FF00', message: "FIXED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
                emailext body: 'Build URL: $BUILD_URL (to view full results, click on "Console Output")', attachLog: true, recipientProviders: [[$class: 'CulpritsRecipientProvider']], subject: 'Notice: Jenkins $JOB_NAME #$BUILD_NUMBER FIXED!', to: 'build-notify-code-science@shiftleft.io'
            }
    	}     	
	} catch (e) {
	    currentBuild.result = "FAILED"
		notifyFailed()
	} 
}

def notifyFailed() {
  slackSend (channel: '#codescience', color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
  emailext body: 'Build URL: $BUILD_URL (to view full results, click on "Console Output")', attachLog: true, recipientProviders: [[$class: 'CulpritsRecipientProvider']], subject: 'Action Required: Jenkins $JOB_NAME #$BUILD_NUMBER FAILED', to: 'builds-notify-code-science@shiftleft.io'
}
