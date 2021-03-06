pipeline {
    agent any;

     tools {
        maven 'maven'
        jdk 'JAVA_HOME'
     }

    stages {

        stage('SCM') {
            steps {
                checkout scm
            }
        }

        stage('Commit') {
            steps {
                sh 'mvn compile'
            }
        }

        stage ('Testing') {
            parallel {
                stage ('Unit Test') {
                    steps {
                        //sh 'mvn test -Pdev'
                        sh 'echo TEST UNIT'
                    }
                }

                stage('Integration Test') {
                    steps {
                        sh 'mvn test -Pdev'
                    }
                    post {
                        always {
                            junit '**/surefire-reports/**/*.xml'
                        }
                    }
                }
            }
        }

        stage('SonarQube') {
            environment {
                scannerHome = tool 'SonarQube Scanner'
            }
            steps {
                //sh 'mvn clean verify'
                withSonarQubeEnv('Sonarqube') {
                    sh 'mvn clean verify -Psonar sonar:sonar'
                    //sh "${scannerHome}/bin/sonar-scanner"
                    //sh "mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dmaven.test.failure.ignore=true -Dsonar.jacoco.reportPaths=${env.WORKSPACE}/target/jacoco.exec"
                }
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        /* stage('Build') {
            steps {
                sh 'mvn package'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**//* target *//*.jar', fingerprint: true
                }
            }
        }
        stage('Cleaning') {
            steps {
                //sh 'mvn -U clean'
                sh 'echo DONT DELETE'
            }
        } */


        stage ('Deploy to Nexus') {
            steps {
                sh 'echo DEPLOY TO NEXUS'
            }
        }

        stage ('Deploy') {
            stages {
                // Development Environment
                stage ('development') {
                    when {
                        branch 'development'
                    }

                    stages {
                        stage ('deploy to develop') {
                            steps {
                                sh 'echo DEPLOY TO DEV'
                            }
                        }
                    }
                }

                // Feature
                stage ('feature') {
                    when {
                        expression {
                            env.BRANCH_NAME.startsWith('feature')
                        }
                    }
                    stages {
                        stage('compile') {
                            steps {
                                sh 'echo COMPILE FEATURE'
                            }
                        }
                    }
                }

                stage ('hotfix') {
                    when {
                        expression {
                            env.BRANCH_NAME.startsWith('hotfix')
                        }
                    }
                    stages {
                        stage('compile') {
                            steps {
                                sh 'echo COMPILE HOTFIX'
                            }
                        }
                    }
                }

                // Production Environment
                stage ('production') {
                    when {
                        branch 'release'
                    }

                    stages {
                        stage('build') {
                            steps {
                                sh 'docker-compose build .'
                            }
                        }

                        stage('Deploy') {
                            steps ('deploy to production') {
                                sh 'echo DEPLOY TO PRODUCTION'
                            }
                        }
                    }
                }
            }
        }
    }


    /* agent any;


    stages {
        stage('checkout commit') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            parallel {
                stage('Compile') {
                    steps {
                        sh 'mvn compile'
                    }
                }
            }
        }

        stage('testing') {
            when {
                anyOf {
                    branch 'master';
                    branch 'development'
                }
            }
            parallel {
                stage('Unit test') {
                    steps {
                        sh 'mvn test'
                    }
                }
                stage('Integration Test') {
                    steps {
                        sh 'mvn test -Pint'
                    }
                }
            }
        }
        stage('Code Quality Analysis') {
                parallel {
                    stage('JavaDoc') {
                        steps {
                            sh './gradlew alljavadoc'
                            step([
                                $class: 'JavadocArchiver',
                                javadocDir: 'product-services/build/docs/javadoc/',
                                keepAll: 'true'
                                ])
                        }
                    }
                    stage('SonarQube') {
                        environment {
                            scannerHome = tool 'SonarQube Scanner'
                        }

                        steps {
                            withSonarQubeEnv('sonarqube') {
                                 sh './gradlew jacocoTestReport sonarqube'
                            }
                            timeout(time: 15, unit: 'MINUTES') {
                                waitForQualityGate abortPipeline: true
                            }
                        }
                    }
                }
                post {
                    always {
                        // Using warning next gen plugin
                        recordIssues aggregatingResults: true, tools: [javaDoc(), checkStyle(pattern: '**//* target/checkstyle-result.xml'), findBugs(pattern: '**//* target/findbugsXml.xml', useRankAsPriority: true), pmdParser(pattern: '**//* target/pmd.xml')]
                    }
                }
        }

        stage('Deploy Artifact To Nexus') {
                when {
                    anyOf { branch 'master'; branch 'develop' }
                }
                steps {
                    script {
                        unstash 'pom'
                        unstash 'artifact'
                        // Read POM xml file using 'readMavenPom' step , this step 'readMavenPom' is included in: https://plugins.jenkins.io/pipeline-utility-steps
                        pom = readMavenPom file: "pom.xml";
                        // Find built artifact under target folder
                        filesByGlob = findFiles(glob: "target *//*.${pom.packaging}");
                        // Print some info from the artifact found
                        echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                        // Extract the path from the File found
                        artifactPath = filesByGlob[0].path;
                        // Assign to a boolean response verifying If the artifact name exists
                        artifactExists = fileExists artifactPath;
                        if (artifactExists) {
                            nexusArtifactUploader(
                                nexusVersion: NEXUS_VERSION,
                                protocol: NEXUS_PROTOCOL,
                                nexusUrl: NEXUS_URL,
                                groupId: pom.groupId,
                                version: pom.version,
                                repository: NEXUS_REPOSITORY,
                                credentialsId: NEXUS_CREDENTIAL_ID,
                                artifacts: [
                                    // Artifact generated such as .jar, .ear and .war files.
                                    [artifactId: pom.artifactId,
                                        classifier: '',
                                        file: artifactPath,
                                        type: pom.packaging
                                    ],
                                    // Lets upload the pom.xml file for additional information for Transitive dependencies
                        [artifactId: pom.artifactId, classifier: '', file: "pom.xml", type: "pom"]])
                    } else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
            }
        }
    } */
}
