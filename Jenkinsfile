pipeline {
  agent {
    label 'osx'
  }
  stages {
    stage('Build') {
      parallel {
        stage('Build') {
          steps {
            timestamps() {
              sh 'scripts/test_all_osx.sh'
            }
            
          }
          post {
            success {
              junit 'tests/target/surefire-reports/**/*.xml'
              
            }
            
          }
        }
        stage('clang 3.7') {
          steps {
            sh 'CLANG_BIN=clang-3.7 mvn clean test -P parser-native'
          }
        }
        stage('clang 5.0') {
          steps {
            sh 'CLANG_BIN=/usr/local/opt/llvm@5/bin/clang-5.0 mvn clean test -P parser-native'
          }
        }
      }
    }
  }
  tools {
    maven 'Maven 3.3.9'
    jdk '1.8'
  }
}