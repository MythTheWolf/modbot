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
        sh '''rm -rf /var/www/mythserver/doc/modbot/*
cp target/site/apidocs/* /var/www/mythserver/doc/modbot/'''
      }
    }
  }
}