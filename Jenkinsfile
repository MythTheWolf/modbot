pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Test') {
      parallel {
        stage('Test') {
          steps {
            catchError() {
              sh 'mvn test'
            }
            
          }
        }
        stage('Make docs') {
          steps {
            sh 'mvn javadoc:javadoc'
          }
        }
      }
    }
    stage('Publish') {
      steps {
        archiveArtifacts 'target/*'
        
        publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/site/apidocs', reportFiles: 'index.html', reportName: 'Javadoc', reportTitles: ''])

        
      }
    }
  }
}
