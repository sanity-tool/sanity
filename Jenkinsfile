pipeline {
    agent {
        label 'osx'
    }
    stages {
        stage('Build') {
            parallel {
                stage('clang (default)') {
                    steps {
                        sh 'CLANG_BIN=clang mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang from /usr/local/opt/llvm') {
                    steps {
                        sh 'CLANG_BIN=/usr/local/opt/llvm/bin/clang mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 3.3') {
                    steps {
                        sh 'CLANG_BIN=clang-3.3 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 3.6') {
                    steps {
                        sh 'CLANG_BIN=clang-3.6 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 3.7') {
                    steps {
                        sh 'CLANG_BIN=clang-3.7 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 3.8') {
                    steps {
                        sh 'CLANG_BIN=clang-3.8 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 4.0') {
                    steps {
                        sh 'CLANG_BIN=/usr/local/opt/llvm@5/bin/clang-4.0 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                    }
                }
                stage('clang 5.0') {
                    steps {
                        sh 'CLANG_BIN=/usr/local/opt/llvm@5/bin/clang-5.0 mvn test -P parser-native'
                    }
                    post {
                        success {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
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