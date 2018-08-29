pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args '-v /root/.m2:/root/.m2'
    }

  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests clean package'
      }
    }
    stage('Test') {
      parallel {
        stage('Test') {
          steps {
            sh 'mvn test'
          }
        }
        stage('Create Docs') {
          steps {
            sh 'mvn javadoc:javadoc'
          }
        }
      }
    }
    stage('Publish') {
      steps {
        archiveArtifacts 'target/*.jar'
     publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: false, reportDir: 'target/site/apidocs', reportFiles: 'index.html', reportName: 'JavaDoc', reportTitles: 'JavaDoc'])
      }
    }
  }
}
