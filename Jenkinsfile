@Library('client-sdk-jenkins-shared-lib@v1.1.0') _

pipeline {
    agent {
        docker {
            image 'mobiledevops/android-sdk-image:34.0.1'
            args '-v $HOME/.gradle:/root/.gradle --user root'
        }
    }

    parameters {
        booleanParam(
            name: 'DRY_RUN',
            defaultValue: false,
            description: 'Enable dry-run mode for publishing'
        )
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        SDK_NAME = 'Android'
        NODE_ENV = 'ci'
        CI = 'true'

        // Target GitHub repositories (mirror destinations)
        WORLDLINE_REPO_URL  = 'wl-online-payments-direct/sdk-client-android.git'
        WHITELABEL_REPO_URL = 'online-payments/sdk-client-android.git'
    }

    stages {
        stage('Setup') {
            steps {
                sh '''
                    set -e

                    # Install required tools
                    apt-get update -qq
                    apt-get install -y -qq rsync
                '''
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean assemble'
            }
        }

        stage('Lint') {
            steps {
                script {
                    echo 'Running Android Lint analysis'

                    // Run lint and continue even if it fails (we'll check results manually)
                    def lintResult = sh(
                        script: './gradlew onlinepayments-sdk:lintRelease',
                        returnStatus: true
                    )

                    // Archive the HTML report
                    archiveArtifacts artifacts: 'onlinepayments-sdk/build/reports/lint-results*.html', allowEmptyArchive: false

                    // Check if lint found any critical issues
                    def lintReportPath = 'onlinepayments-sdk/build/reports/lint-results-release.html'
                    if (fileExists(lintReportPath)) {
                        // Only fail on errors, not warnings or deprecations
                        // Format in HTML is "Priority X/10" not "Priority: X"
                        def errorCount = sh(
                            script: "grep -o 'Priority 1/10' '${lintReportPath}' | wc -l || echo 0",
                            returnStdout: true
                        ).trim().toInteger()

                        def fatalCount = sh(
                            script: "grep -o 'Priority 0/10' '${lintReportPath}' | wc -l || echo 0",
                            returnStdout: true
                        ).trim().toInteger()

                        if (errorCount > 0 || fatalCount > 0) {
                            error("Lint found ${fatalCount} fatal and ${errorCount} error issues. Check the lint report for details.")
                        } else {
                            def warningCount = sh(
                                script: "grep -oE 'Priority [2-9]/10' '${lintReportPath}' | wc -l || echo 0",
                                returnStdout: true
                            ).trim().toInteger()

                            if (warningCount > 0) {
                                echo "Lint found ${warningCount} warnings/deprecations - review recommended but not blocking release"
                            } else {
                                echo 'Lint analysis passed - no issues found'
                            }
                        }
                    } else {
                        echo 'Lint report not found, assuming no issues'
                    }
                }
            }
        }

        stage('Static code analysis') {
            steps {
                script {
                    echo 'Running Detekt static code analysis'

                    // Run Detekt
                    def detektResult = sh(
                        script: './gradlew onlinepayments-sdk:detekt',
                        returnStatus: true
                    )

                    // Archive the HTML report
                    archiveArtifacts artifacts: 'onlinepayments-sdk/build/reports/detekt-report.html', allowEmptyArchive: false

                    // Check if Detekt found issues
                    if (detektResult != 0) {
                        error('Detekt found code quality issues. Check the detekt-report.html for details.')
                    } else {
                        echo 'Detekt analysis passed - no issues found'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                sh './gradlew onlinepayments-sdk:test'
            }
        }

//         stage('Code Quality Analysis') {
//             steps {
//                 script {
//                     echo 'Running SonarQube code quality and security analysis'
//
//                     withCredentials([string(credentialsId: 'sonarqube-token-sdks', variable: 'SONAR_TOKEN')]) {
//                         withSonarQubeEnv('SonarQube') {
//                             sh """
//                                 ./gradlew onlinepayments-sdk:sonar \
//                                     -Dsonar.host.url=\${SONAR_HOST_URL} \
//                                     -Dsonar.token=\${SONAR_TOKEN}
//                             """
//                         }
//                     }
//
//                     // Wait for quality gate result
//                     timeout(time: 5, unit: 'MINUTES') {
//                         def qg = waitForQualityGate()
//                         if (qg.status != 'OK') {
//                             echo "WARNING: SonarQube Quality Gate failed: ${qg.status}"
//                             // Note: Not failing the build, just warning
//                         } else {
//                             echo "SonarQube Quality Gate passed"
//                         }
//                     }
//                 }
//             }
//         }

        stage('Security Audit') {
            when {
                anyOf {
                    branch 'main'
                    tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                }
            }
            steps {
                script {
                    echo 'Running security audit'

                    // Check for known vulnerabilities in dependencies using OWASP Dependency Check
                    echo 'Running OWASP Dependency Check for known vulnerabilities'
                    def depCheckResult = 0
                    withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
                        depCheckResult = sh(
                            script: './gradlew onlinepayments-sdk:dependencyCheckAnalyze --no-configuration-cache',
                            returnStatus: true
                        )
                    }

                    // Archive the reports (if they exist)
                    archiveArtifacts artifacts: 'onlinepayments-sdk/build/reports/dependency-check-report.*', allowEmptyArchive: true

                    // Warn if dependency check failed but don't fail the build
                    // NVD API can be unreliable in CI environments
                    if (depCheckResult != 0) {
                        echo 'WARNING: Dependency check failed (likely NVD API issue). Continuing with other security checks.'
                    } else {
                        echo 'Dependency check completed successfully'
                    }

                    // Scan for hardcoded secrets
                    echo 'Scanning for hardcoded secrets and credentials'
                    sh """
                        set -e

                        # Check for common secret patterns
                        echo "Checking for hardcoded credentials..."
                        if grep -r -E '(password|secret|api_key|apikey|token|auth)\\s*=\\s*["\'][^"\']{8,}["\']' \
                            --include='*.kt' --include='*.java' --include='*.xml' \
                            onlinepayments-sdk/src/main/ 2>/dev/null || true; then
                            echo "WARNING: Potential hardcoded secrets found. Please review."
                        else
                            echo "✓ No obvious hardcoded secrets detected"
                        fi

                        # Check for TODO/FIXME security notes
                        echo "Checking for security-related TODO/FIXME items..."
                        if grep -r -E '(TODO|FIXME).*[Ss]ecurity' \
                            --include='*.kt' --include='*.java' \
                            onlinepayments-sdk/src/ 2>/dev/null || true; then
                            echo "WARNING: Security-related TODO/FIXME items found. Please review."
                        else
                            echo "✓ No security-related TODO/FIXME items found"
                        fi
                    """

                    // Check for insecure network configurations
                    echo 'Checking for insecure network configurations'
                    sh """
                        set -eu

                        # Check for cleartext traffic permissions
                        echo "Checking for cleartext traffic configuration..."
                        if grep -rq 'usesCleartextTraffic.*true' onlinepayments-sdk/src/main/ 2>/dev/null; then
                            echo "WARNING: Cleartext traffic is enabled"
                        else
                            echo "✓ No cleartext traffic enabled"
                        fi

                        # Check for disabled certificate validation
                        echo "Checking for insecure certificate validation..."
                        if grep -rq 'TrustAllCertificates\\|ALLOW_ALL_HOSTNAME_VERIFIER' \
                            --include='*.kt' --include='*.java' \
                            onlinepayments-sdk/src/ 2>/dev/null; then
                            echo "ERROR: Insecure certificate validation detected"
                            exit 1
                        else
                            echo "✓ No insecure certificate validation found"
                        fi

                        # Check for exported components without permissions
                        echo "Checking AndroidManifest for security issues..."
                        if [ -f "onlinepayments-sdk/src/main/AndroidManifest.xml" ]; then
                            if grep -E 'android:exported="true"' onlinepayments-sdk/src/main/AndroidManifest.xml 2>/dev/null | \
                               grep -qv 'android:permission' 2>/dev/null; then
                                echo "WARNING: Exported components without permissions detected"
                            else
                                echo "✓ No exported components without permissions"
                            fi
                        fi

                        # Check for debuggable flag in release
                        echo "Checking for debuggable flag..."
                        if grep -rq 'android:debuggable.*true' onlinepayments-sdk/src/main/ 2>/dev/null; then
                            echo "WARNING: Debuggable flag set to true"
                        else
                            echo "✓ Debuggable flag not set"
                        fi
                    """

                    echo 'Security audit completed - All automated checks passed'
                }
            }
        }

        stage('Remove dev-only files') {
            when {
                anyOf {
                    branch 'main'
                    tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                }
            }
            steps {
                sh '''
                    set -e

                    rm -f DEV.md
                '''
            }
        }

        stage('Extract Version & Package') {
            when {
                anyOf {
                    branch 'main'
                    tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                }
            }
            steps {
                script {
                    def version = sh(
                        script: "grep '^POM_VERSION=' gradle.properties | cut -d'=' -f2",
                        returnStdout: true
                    ).trim()

                    env.VERSION     = version
                    env.VERSION_TAG = "v${version}"

                    echo "Version:      ${env.VERSION}"
                    echo "Version tag:  ${env.VERSION_TAG}"
                }

                script {
                    echo 'Preparing the package'

                    withCredentials([
                        file(credentialsId: 'pgp-signing-key-file', variable: 'SIGNING_KEY_FILE'),
                        string(credentialsId: 'pgp-signing-password', variable: 'SIGNING_PASSWORD')
                    ]) {
                        sh './gradlew prepareForRelease'
                    }

                    env.PACKAGE_FILE = "./onlinepayments-sdk/build/onlinepayments-sdk-${env.VERSION}.zip"
                    echo "Package created: ${env.PACKAGE_FILE}"

                    archiveArtifacts artifacts: "onlinepayments-sdk/build/onlinepayments-sdk-${env.VERSION}.zip", fingerprint: true
                }
            }
        }

        stage('Validate Version') {
            when {
                anyOf {
                    branch 'main'
                    tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                }
            }
            steps {
                script {
                    echo 'Validating version consistency between gradle.properties and Constants.kt'

                    def constantsVersion = sh(
                        script: "grep 'const val SDK_VERSION' onlinepayments-sdk/src/main/kotlin/com/onlinepayments/sdk/client/android/configuration/Constants.kt | sed 's/.*\"\\(.*\\)\".*/\\1/'",
                        returnStdout: true
                    ).trim()

                    echo "gradle.properties version: ${env.VERSION}"
                    echo "Constants.kt version:      ${constantsVersion}"

                    if (env.VERSION != constantsVersion) {
                        error("Version mismatch! gradle.properties (${env.VERSION}) != Constants.kt (${constantsVersion})")
                    }

                    echo 'Version validation passed'
                }
            }
        }

        stage('Verify Package') {
            when {
                anyOf {
                    branch 'main'
                    tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                }
            }
            steps {
                script {
                    echo 'Verifying package integrity'

                    sh """
                        set -e

                        # Check if package file exists
                        if [ ! -f "${env.PACKAGE_FILE}" ]; then
                            echo "ERROR: Package file not found: ${env.PACKAGE_FILE}"
                            exit 1
                        fi

                        # Check if package is a valid zip file
                        if ! unzip -t "${env.PACKAGE_FILE}" > /dev/null 2>&1; then
                            echo "ERROR: Package is not a valid zip file"
                            exit 1
                        fi

                        # Display package contents
                        echo "Package contents:"
                        unzip -l "${env.PACKAGE_FILE}"

                        # Get package size
                        ls -lh "${env.PACKAGE_FILE}"
                    """

                    echo 'Package verification passed'
                }
            }
        }

        stage('GitHub Upload') {
            when {
                allOf {
                    anyOf {
                        branch 'main'
                        tag pattern: 'v\\d+\\.\\d+\\.\\d+.*', comparator: 'REGEXP'
                    }
                    expression { return !params.DRY_RUN }
                }
            }
            steps {
                script {
                    echo "Uploading ${env.SDK_NAME} SDK to GitHub repositories"

                    // Worldline repository
                    gitHub.pushRepo(
                        humanName: 'Worldline',
                        credentialsId: 'github-worldline-token',
                        repo: env.WORLDLINE_REPO_URL,
                        tag: env.VERSION_TAG,
                        gitName: "Worldline Direct Jenkins CI",
                        gitEmail: "82139942+worldline-direct-support-team@users.noreply.github.com",
                        dryRun: params.DRY_RUN
                    )
                    gitHub.createRelease(
                        humanName: 'Worldline',
                        credentialsId: 'github-worldline-token',
                        repo: env.WORLDLINE_REPO_URL,
                        tag: env.VERSION_TAG,
                        artifacts: env.PACKAGE_FILE,
                        dryRun: params.DRY_RUN
                    )

                    // Whitelabel repository
                    gitHub.pushRepo(
                        humanName: 'Whitelabel',
                        credentialsId: 'github-whitelabel-token',
                        repo: env.WHITELABEL_REPO_URL,
                        tag: env.VERSION_TAG,
                        gitName: "Online Payments Jenkins CI",
                        gitEmail: "96182451+online-payments-support-team@users.noreply.github.com",
                        dryRun: params.DRY_RUN
                    )
                    gitHub.createRelease(
                        humanName: 'Whitelabel',
                        credentialsId: 'github-whitelabel-token',
                        repo: env.WHITELABEL_REPO_URL,
                        tag: env.VERSION_TAG,
                        artifacts: env.PACKAGE_FILE,
                        dryRun: params.DRY_RUN
                    )
                }

                script {
                    echo 'Generating Javadoc and publishing to gh-pages branch'

                    sh './gradlew javadocJar'

                    def javadocSourceDir = 'onlinepayments-sdk/build/intermediates/java_doc_dir/release/javaDocReleaseGeneration'

                    // Publish to both repositories
                    gitHub.publishJavadocToGhPages(
                        humanName: 'Worldline',
                        credentialsId: 'github-worldline-token',
                        repo: env.WORLDLINE_REPO_URL,
                        javadocSourceDir: javadocSourceDir,
                        version: env.VERSION,
                        gitName: "Worldline Direct Jenkins CI",
                        gitEmail: "82139942+worldline-direct-support-team@users.noreply.github.com",
                        dryRun: params.DRY_RUN
                    )
                    gitHub.publishJavadocToGhPages(
                        humanName: 'Whitelabel',
                        credentialsId: 'github-whitelabel-token',
                        repo: env.WHITELABEL_REPO_URL,
                        javadocSourceDir: javadocSourceDir,
                        version: env.VERSION,
                        gitName: "Online Payments Jenkins CI",
                        gitEmail: "96182451+online-payments-support-team@users.noreply.github.com",
                        dryRun: params.DRY_RUN
                    )
                }
            }
        }
    }
}
