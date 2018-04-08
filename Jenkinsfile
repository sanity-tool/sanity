pipeline {
    agent none
    stages {
        stage {
            parallel {
                stage('Test on OSX') {
                    agent {
                        label 'osx'
                    }
                    stages {
                        testClang('clang')
                        testClang('/usr/local/opt/llvm/bin/clang')
                        testClang('clang-3.3')
                        testClang('clang-3.6')
                        testClang('clang-3.7')
                        testClang('clang-3.8')
                        testClang('/usr/local/opt/llvm@4/bin/clang-4.0')
                        testClang('/usr/local/opt/llvm@5/bin/clang-5.0')
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

def testClang(clangBin) {
    stage('clangBin') {
        steps {
            sh "CLANG_BIN=$clangBin mvn test -P parser-native"
        }
        post {
            success {
                junit 'tests/target/surefire-reports/**/*.xml'
            }
        }
    }
}