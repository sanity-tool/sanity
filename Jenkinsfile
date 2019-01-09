pipeline {
    agent none
    stages {
        stage('Test') {
            parallel {
                stage('Test on OSX') {
                    agent {
                        label 'osx'
                    }
                    steps {
                        testClang('/usr/local/opt/llvm/bin/clang')
                        testClang('clang-3.3')
                        testClang('clang-3.6')
                        testClang('clang-3.7')
                        testClang('clang-3.8')
                        testClang('/usr/local/opt/llvm@3.9/bin/clang-3.9')
                        testClang('/usr/local/opt/llvm@4/bin/clang-4.0')
                        testClang('/usr/local/opt/llvm@5/bin/clang-5.0')

                        withEnv(['LLVM_CONFIG=/usr/local/opt/llvm/bin/llvm-config']) {
                            testClang('/usr/local/opt/llvm/bin/clang')
                        }
                        withEnv(['LLVM_CONFIG=llvm-config-3.3']) {
                            testClang('clang-3.3')
                        }
                        withEnv(['LLVM_CONFIG=llvm-config-3.6']) {
                            testClang('clang-3.6')
                        }
                        withEnv(['LLVM_CONFIG=llvm-config-3.7']) {
                            testClang('clang-3.7')
                        }
                        withEnv(['LLVM_CONFIG=llvm-config-3.8']) {
                            testClang('clang-3.8')
                        }
                        withEnv(['LLVM_CONFIG=/usr/local/opt/llvm@3.9/bin/llvm-config-3.9']) {
                            testClang('/usr/local/opt/llvm@3.9/bin/clang-3.9')
                        }
                        withEnv(['LLVM_CONFIG=/usr/local/opt/llvm@4/bin/llvm-config-4.0']) {
                            testClang('/usr/local/opt/llvm@4/bin/clang-4.0')
                        }
                        withEnv(['LLVM_CONFIG=/usr/local/opt/llvm@5/bin/clang-5.0/llvm-config-5.0']) {
                            testClang('/usr/local/opt/llvm@5/bin/clang-5.0')
                        }
                    }
                    post {
                        always {
                            junit 'tests/target/surefire-reports/**/*.xml'
                        }
                        cleanup {
                            cleanWs()
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

def testClang(clangBin) {
    sh "CLANG_BIN=$clangBin mvn test -P parser-native"
}

def testRust(rustBin) {
    sh "RUST_BIN=$rustBin mvn test -P parser-native"
}
