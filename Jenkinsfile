pipeline {
    agent none

    environment {
        BITREADER_URL = 'http://localhost:8080'
    }

    stages {
        stage('Test') {
            parallel {
                stage('Test on OSX') {
                    agent {
                        label 'osx'
                    }
                    steps {
                        testOsx('parser-native')
                        script {
                        docker.image('sanitytool/bitreader-service') { c ->
                            /* Wait until mysql service is up 
                                sh 'while ! mysqladmin ping -h0.0.0.0 --silent; do sleep 1; done'
                                Run some tests which require MySQL */
                            testOsx('parser-remote')
                        }
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
                stage('Test on Windows') {
                    agent {
                        label 'win32'
                    }
                    steps {
                        //todo testWin32('parser-native')
                        script {
                        docker.image('sanitytool/bitreader-service') { c ->
                            /* Wait until mysql service is up 
                                sh 'while ! mysqladmin ping -h0.0.0.0 --silent; do sleep 1; done'
                                Run some tests which require MySQL */
                            testOsx('parser-remote')
                        }
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

def testOsx(profile) {
    testClang('clang', profile)
    testClang('/usr/local/opt/llvm/bin/clang', profile)
    testClang('clang-3.3', profile)
    testClang('clang-3.6', profile)
    testClang('clang-3.7', profile)
    testClang('clang-3.8', profile)
    testClang('/usr/local/opt/llvm@4/bin/clang-4.0', profile)
    testClang('/usr/local/opt/llvm@5/bin/clang-5.0', profile)

    testRust('rustc', profile)
    testRust("$env.HOME/.cargo/bin/rustc", profile)
}

def testWin32(profile) {
    testClang('C:\\Program Files\\LLVM\\bin\\clang.exe', profile)
}

def testClang(clangBin, profile) {
    sh "CLANG_BIN=$clangBin mvn test -P $profile"
}

def testRust(rustBin, profile) {
    sh "RUST_BIN=$rustBin mvn test -P $profile"
}
