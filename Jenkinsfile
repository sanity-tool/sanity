pipeline {
    agent { label 'osx' }
    tools {
        maven 'Maven 3.3.9'
        jdk '1.8'
    }
    stages {
        stage ('Build') {
            steps {
                timestamps {
                    sh 'scripts/test_all_osx.sh'
                    //sh 'mvn clean test -P parser-native'
                }
            }
            post {
                success {
                    junit 'tests/target/surefire-reports/**/*.xml'
                }
            }
        }
    }
}
