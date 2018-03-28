pipeline {
    agent any
    tools {
        maven 'Maven 3.3.9'
        jdk '1.8'
    }
    stages {
        stage ('Build') {
            steps {
                bat 'mvn clean test'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
    }
}
