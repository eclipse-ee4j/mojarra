#!/usr/bin/env groovy
//
// Mojarra release pipeline.
//
// Stages: Prepare -> Build & install -> TCK -> Deploy to Maven Central -> Bump to next snapshot
//   -> Publish to GitHub. Maven Central deploy and GitHub push run only after the TCK passes,
//   so a TCK failure leaves no half-published external state.
//
// This pipeline releases the mojarra IMPL only; it never publishes the standalone jakarta.faces-api
// (5.0+), which is released separately via the jakartaee/faces release job. The api version is
// auto-resolved from impl/pom.xml's jakarta.faces-api dependency: a concrete (milestone/GA) version
// is used as-is; a -SNAPSHOT dep resolves to this run's RELEASE_VERSION (impl and api are cut in
// lockstep at the same version). On a real release the resolved version must already be on Maven
// Central — the impl consumes it as a normal dependency (pinned into impl/pom.xml) and Prepare fails
// fast if it is missing. As a convenience, a DRY_RUN against a -SNAPSHOT dep whose version is not yet
// on Central instead builds the api from the faces/ submodule into the reactor (-Papi), so the impl
// build can be validated before the api is published.
//
// The faces/ git submodule (5.0+) thus feeds two things: the -Papi dry-run fallback above, and the
// TCK when it is built from source (tckVersion = -SNAPSHOT).
//
// Maven Central publication is gated by the EE4J parent's `-Poss-release` profile (activated only
// by this Jenkinsfile). It wires central-publishing-maven-plugin (incl. GPG signing, sources and
// javadoc jars) and sets autoPublish=true. A bare `mvn deploy` from a developer machine does NOT
// activate this profile and does NOT reach Maven Central.
//

// JDK install root layout on Eclipse CI: /opt/tools/java/<distro>/jdk-<N>/latest. The distro
// (Adoptium "temurin" vs. the OpenJDK reference build) is a function of the major version, used
// for both the build JDK and the TCK JDK.
def JDK_DISTRO_BY_VERSION = [
    '11': 'openjdk',
    '17': 'openjdk',
    '21': 'temurin',
    '25': 'temurin',
]

// Choices for the JDK / TCK_JDK params; "" is the "auto-infer from RELEASE_LINE" sentinel.
def JDK_VERSION_CHOICES = [''] + JDK_DISTRO_BY_VERSION.keySet().toList()

// ---- Per-branch configuration ---------------------------------------------
// Adding a new release line = one entry here. The map key is the MAJOR.MINOR version family the
// release line represents (also used as the path segment on download.eclipse.org/jakartaee/faces/
// and as the required prefix for RELEASE_VERSION). It deliberately differs from the actual
// mojarra git branch — the head of mojarra development sits on `master`, not on a `5.x` branch.
//
// Fields:
//   implBranch      : mojarra git branch holding the impl source for this release line.
//   apiBranch       : marks a release line that has a SEPARATE standalone jakarta.faces-api
//                     artifact on Maven Central (5.0+); null for lines that bundle the API spec
//                     into the impl jar (4.x). When non-null, the api version is resolved from
//                     impl/pom.xml's jakarta.faces-api dep and consumed from Maven Central (pinned
//                     into the impl), except on the DRY_RUN submodule fallback (see header). The
//                     value is the faces branch name, used for the submodule/TCK label.
//   apiVersion      : 4.x only. jakarta.faces-api version the TCK is run against (passed as
//                     -Dfaces.version). Ignored when apiBranch != null.
//   jdk             : major JDK version used to build the impl (per Faces spec).
//   tckJdk          : major JDK version used to run the TCK. Differs from jdk when the GlassFish
//                     container needs a newer JDK than the spec. Also runs the Prepare-stage
//                     Sonatype cred-check, whose central-staging plugin is compiled for Java 17,
//                     so this must stay >= 17 on every line (the 4.x impl still builds on its own
//                     older `jdk`).
//   tckVersion      : Faces TCK version. A released version (e.g. "4.0.3") is downloaded as a zip
//                     from download.eclipse.org/jakartaee/faces/<line>/. A -SNAPSHOT value (e.g.
//                     "5.0.0-SNAPSHOT") instead builds the TCK from the faces submodule's tck/
//                     directly — used while a release line's TCK has not yet shipped.
//   gfVersion       : GlassFish Maven coordinate version used by the TCK.
//   seleniumEnabled : whether the BaseITNG (Selenium/Chrome) tests run. The agent pod ships
//                     current Chrome; set false for branches whose TCK pins a CDP major outside
//                     Selenium's fudge range (e.g. 4.0 pins CDP v108).
//   threadCount     : Maven `-T` value for the TCK reactor. Set >1 only for release lines whose
//                     TCK ships gf-pool (5.0+); 4.x TCKs run a single managed GlassFish per
//                     module and cannot share it across parallel module builds, so threadCount
//                     stays at 1 there. The pod's cpu/memory should comfortably accommodate
//                     this many concurrent GlassFish + test JVM pairs.
def BRANCH_CONFIG = [
    '4.0': [ implBranch: '4.0',    apiBranch: null,  apiVersion: '4.0.1', jdk: '11', tckJdk: '17', tckVersion: '4.0.4',          gfVersion: '7.1.1'   , seleniumEnabled: false, threadCount: 1 ],
    '4.1': [ implBranch: '4.1',    apiBranch: null,  apiVersion: '4.1.0', jdk: '17', tckJdk: '21', tckVersion: '4.1.2',          gfVersion: '8.0.3'   , seleniumEnabled: true , threadCount: 1 ],
    '5.0': [ implBranch: 'master', apiBranch: '5.0', apiVersion: null,    jdk: '17', tckJdk: '21', tckVersion: '5.0.0-SNAPSHOT', gfVersion: '9.0.0-M2', seleniumEnabled: true , threadCount: 2 ],
]

// Reusable shell snippet: GPG keyring import + trust. Idempotent. Required wherever the build
// signs artifacts (javadoc/sources in Build & install, everything in Deploy to Maven Central).
// Inside ''' Groovy strings, ${...} stays literal so bash sees ${KEYRING}.
def GPG_INIT = '''
    gpg --batch --import "${KEYRING}"
    for fpr in $(gpg --list-keys --with-colons | awk -F: '/fpr:/ {print $10}' | sort -u); do
        echo -e "5\\ny\\n" | gpg --batch --command-fd 0 --expert --edit-key "${fpr}" trust
    done
'''

// Reusable shell snippet: bot git identity. Sets local (per-repo) config, so must run inside the
// working tree of the repo about to be committed to.
def GIT_IDENTITY = '''
    git config user.email "mojarra-bot@eclipse.org"
    git config user.name  "Eclipse Mojarra Bot"
'''

// Reusable shell snippet: pre-populate known_hosts so `git push origin git@github.com:...` doesn't
// fail with "Host key verification failed". Jenkins' GitSCM step handles this internally for the
// initial checkout, but pushes from shell steps inside `sshagent` need it explicitly. Idempotent.
def KNOWN_HOSTS_INIT = '''
    mkdir -p ~/.ssh
    ssh-keyscan -t rsa,ed25519,ecdsa github.com >> ~/.ssh/known_hosts 2>/dev/null
    chmod 600 ~/.ssh/known_hosts
'''

// Reusable shell snippet: install GitHub CLI into ~/bin and prepend it to PATH. The Eclipse CI pod
// image (basic-ubuntu-chrome) doesn't ship `gh`, so any sh that calls it must run this first.
// Idempotent: skips the download when gh is already on PATH (e.g. from an earlier sh in the same
// pod, where ~/bin survives because jenkins-home is the pod's HOME volume).
def GH_INSTALL = '''
    if ! command -v gh >/dev/null 2>&1; then
        GH_VERSION=2.62.0
        mkdir -p "${HOME}/bin"
        curl -sSL "https://github.com/cli/cli/releases/download/v${GH_VERSION}/gh_${GH_VERSION}_linux_amd64.tar.gz" \\
            | tar -xz -C "${HOME}/bin" --strip-components=2 "gh_${GH_VERSION}_linux_amd64/bin/gh"
    fi
    export PATH="${HOME}/bin:${PATH}"
'''

def GPG_GIT_INIT = GPG_INIT + GIT_IDENTITY

// Reusable shell snippet: refuse to start the release if origin already carries this version's
// branch or tag. The check runs even on dry-runs so a conflict fails fast (minute zero) rather
// than after burning the whole TCK only to bomb the moment DRY_RUN is flipped off. Recovery for
// any such conflict is to bump the version and re-run. Setting TAG_ONLY=true skips the branch
// check (used for milestone/RC runs that never push the branch).
// Expects bash variables BRANCH_NAME and TAG_NAME to be set in the surrounding script.
def REMOTE_REF_CONFLICT_CHECK = '''
    if [ "${TAG_ONLY:-false}" != "true" ] && git ls-remote --heads origin | grep -q "refs/heads/${BRANCH_NAME}$"; then
        echo "Release branch ${BRANCH_NAME} already exists on origin; bump the version." >&2; exit 1
    fi
    if git ls-remote --tags origin | grep -q "refs/tags/${TAG_NAME}$"; then
        echo "Release tag ${TAG_NAME} already exists on origin; bump the version." >&2; exit 1
    fi
    git branch -D "${BRANCH_NAME}" 2>/dev/null || true
    git tag    -d "${TAG_NAME}"    2>/dev/null || true
    git checkout -b "${BRANCH_NAME}"
'''

pipeline {
    // Run the entire pipeline inside a kubernetes pod with Chrome pre-installed, so the TCK's
    // BaseITNG (Selenium) tests can drive a real browser without us bootstrapping it.
    // eclipsecbijenkins/basic-ubuntu-chrome layers Chrome onto jiro-agent-basic-ubuntu, so the
    // standard Eclipse CI tooling layout (/opt/tools/java, /opt/tools/apache-maven, settings.xml
    // mounts) is preserved.
    agent {
        kubernetes {
            defaultContainer 'jnlp-with-chrome'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: jnlp-with-chrome
      image: 'eclipsecbijenkins/basic-ubuntu-chrome:latest'
      tty: true
      # Bash reaper as PID 1 so reparented orphans (asadmin clients, surefire forks, sh wrappers)
      # don't accumulate as zombies and exhaust the pod's pids cgroup over a long TCK run. The
      # image doesn't ship tini; bash + `wait -n` is the zero-dependency equivalent. The outer
      # `while :;` keeps the loop alive when `wait -n` returns 127 (no children) at startup.
      command:
        - /bin/bash
        - -c
        - 'while :; do wait -n 2>/dev/null || sleep 5; done'
      env:
        - name: HOME
          value: /home/jenkins
        # The agent image sets JAVA_TOOL_OPTIONS globally with OpenJ9-only flags
        # (PortableSharedCache, Xshareclasses, IdleTuningGcOnIdle). On a Temurin JDK these are
        # inert thanks to -XX:+IgnoreUnrecognizedVMOptions but they pollute every "java -version"
        # line and make CI logs confusing. Empty it for the whole pod.
        - name: JAVA_TOOL_OPTIONS
          value: ""
      # Capped at the Eclipse Jiro namespace quota (8Gi/2 CPU per container; pod total 8704Mi/2300m
      # with the rest going to the jnlp sidecar). Raising it requires a helpdesk request. 5.0's
      # -T 4 still helps despite 2:1 CPU oversubscription because gf-pool slots are I/O-bound.
      resources:
        limits:
          memory: 8Gi
          cpu: '2'
        requests:
          memory: 8Gi
          cpu: '2'
      volumeMounts:
        - name: jenkins-home
          mountPath: /home/jenkins
          readOnly: false
        - name: settings-xml
          mountPath: /home/jenkins/.m2/settings.xml
          subPath: settings.xml
          readOnly: true
        - name: settings-security-xml
          mountPath: /home/jenkins/.m2/settings-security.xml
          subPath: settings-security.xml
          readOnly: true
        - name: m2-repo
          mountPath: /home/jenkins/.m2/repository
        - name: tools
          mountPath: /opt/tools
  volumes:
    - name: jenkins-home
      emptyDir: {}
    - name: settings-xml
      secret:
        secretName: m2-secret-dir
        items:
          - key: settings.xml
            path: settings.xml
    - name: settings-security-xml
      secret:
        secretName: m2-secret-dir
        items:
          - key: settings-security.xml
            path: settings-security.xml
    - name: m2-repo
      emptyDir: {}
    - name: tools
      persistentVolumeClaim:
        claimName: tools-claim-jiro-mojarra
"""
        }
    }

    parameters {
        choice(name: 'RELEASE_LINE', choices: ['4.0', '4.1', '5.0'], description: 'Release line to cut.')
        string(name: 'MILESTONE_VERSION' , defaultValue: '', description: 'Leave blank for a GA release; otherwise the suffix for a milestone/RC release. Must match ^(M|RC)[0-9]+$ (e.g. M1, M2, RC1). When set, the release version is auto-derived as <pom-base-version>-<MILESTONE_VERSION> (e.g. 5.0.0-M2), tagged exactly that (no -RELEASE suffix), and the source branch is left untouched: PR-merge, milestone management, GitHub release creation, and snapshot bump are all skipped.')
        choice(name: 'JDK', choices: JDK_VERSION_CHOICES, description: 'Leave blank to auto-infer from RELEASE_LINE (11 for 4.0, 17 for 4.1 and 5.0). This is the JDK used to run the build & install.')
        choice(name: 'TCK_JDK', choices: JDK_VERSION_CHOICES, description: 'Leave blank to auto-infer from RELEASE_LINE (11 for 4.0, 21 for 4.1 and 5.0). This is the JDK used to run the TCK (the GlassFish container may need a newer JDK than the spec).')
        string(name: 'TCK_VERSION', defaultValue: '', description: 'Leave blank to auto-infer from RELEASE_LINE. A released value (e.g. 4.0.3) downloads the published TCK zip from download.eclipse.org; a -SNAPSHOT value (e.g. 5.0.0-SNAPSHOT) builds the TCK from the faces/tck submodule directory instead.')
        string(name: 'GF_VERSION', defaultValue: '', description: 'Leave blank to auto-infer from RELEASE_LINE. When using GF_BUNDLE_URL, set this to match the artifact version inside the zip (e.g. 8.0.0-X).')
        string(name: 'GF_BUNDLE_URL', defaultValue: '', description: 'Leave blank to resolve GlassFish from Maven Central via GF_VERSION; otherwise an explicit zip URL override (GF_VERSION must match the artifact version inside the zip).')
        choice(name: 'THREAD_COUNT', choices: ['', '1', '2', '3', '4', '5', '6', '7', '8'], description: '5.0+ only. Leave blank to auto-infer from RELEASE_LINE (1 for 4.x, 2 for 5.0). Maven `-T` value and gf.pool.size for the TCK reactor. Values >1 only work on TCKs that ship gf-pool (5.0+); selecting one on 4.x errors out.')
        booleanParam(name: 'RUN_TCK', defaultValue: true, description: 'Run the Faces TCK after build.')
        booleanParam(name: 'SKIP_OLD_TCK', defaultValue: false, description: 'Requires RUN_TCK. 4.x only. Skip the old-tck JavaTest modules (excluded from the reactor entirely via -pl); cuts nearly 3 hours off the TCK run. No-op on 5.0+ where these modules no longer exist. The old-tck-selenium failsafe-driven modules are unaffected by this flag.')
        booleanParam(name: 'SMOKE_TEST', defaultValue: false, description: 'Requires RUN_TCK and DRY_RUN. Filter the TCK to a tiny representative subset for fast iteration on the pipeline itself (one failsafe IT + one sigtest IT + one old-tck-selenium IT, plus one old-tck JavaTest path when SKIP_OLD_TCK is unchecked).')
        booleanParam(name: 'DRY_RUN', defaultValue: true, description: 'Skip Maven Central deploy and GitHub push.')
        booleanParam(name: 'SKIP_DEPLOY', defaultValue: false, description: 'Requires DRY_RUN unchecked. Skip the Maven Central deploy stage only (still pushes branch/tag and creates the GitHub release). Use for resuming a previous run after Maven Central already published, or for pipeline-debug runs that exercise Publish to GitHub without re-deploying.')
        booleanParam(name: 'SKIP_CRED_CHECK', defaultValue: false, description: 'Skip the Prepare-stage credential checks (Sonatype Central, GitHub SSH push, GitHub bot token). Use for pipeline-debug runs where the publish credentials are unavailable or known-stale and should not abort the run; on a real release leave this unchecked so a bad credential fails fast.')
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '300', numToKeepStr: '20'))
        timestamps()
    }

    environment {
        TOOLS_PREFIX  = '/opt/tools'
        MVN_HOME      = "${TOOLS_PREFIX}/apache-maven/latest"
        MVN_EXTRA     = '--batch-mode --no-transfer-progress'
        VERSIONS_PLUGIN = 'org.codehaus.mojo:versions-maven-plugin:2.18.0'
        HELP_PLUGIN     = 'org.apache.maven.plugins:maven-help-plugin:3.5.1'
        CENTRAL_PLUGIN  = 'org.eclipse.cbi.central:central-staging-plugins:1.4.7'
    }

    stages {

        stage('Prepare') {
            steps {
                cleanWs()
                script {
                    def cfg = BRANCH_CONFIG[params.RELEASE_LINE]
                    if (cfg == null) error "Unknown RELEASE_LINE: ${params.RELEASE_LINE}"

                    // Enforce the apiBranch/apiVersion XOR invariant so a misconfigured BRANCH_CONFIG
                    // entry fails fast here rather than producing a confusing error mid-release.
                    if ((cfg.apiBranch == null) == (cfg.apiVersion == null)) {
                        error "BRANCH_CONFIG['${params.RELEASE_LINE}'] must set exactly one of apiBranch / apiVersion (apiBranch=${cfg.apiBranch}, apiVersion=${cfg.apiVersion})."
                    }

                    // Reject inert checkbox combinations up front rather than silently ignoring them.
                    if (params.SKIP_OLD_TCK && !params.RUN_TCK) error "SKIP_OLD_TCK requires RUN_TCK."
                    if (params.SMOKE_TEST   && !params.RUN_TCK) error "SMOKE_TEST requires RUN_TCK."
                    if (params.SMOKE_TEST   && !params.DRY_RUN) error "SMOKE_TEST requires DRY_RUN (filtered run is not TCK-conformant and must never be published)."
                    if (params.SKIP_DEPLOY  &&  params.DRY_RUN) error "SKIP_DEPLOY requires DRY_RUN unchecked (DRY_RUN already skips deploy)."
                    if (params.THREAD_COUNT?.trim() && cfg.threadCount == 1) error "THREAD_COUNT is 5.0+ only (4.x TCKs run a single managed GlassFish per module and cannot parallelize)."

                    env.RESOLVED_JDK         = params.JDK?.trim()         ?: cfg.jdk
                    env.RESOLVED_TCK_JDK     = params.TCK_JDK?.trim()     ?: cfg.tckJdk
                    env.RESOLVED_TCK_VERSION = params.TCK_VERSION?.trim() ?: cfg.tckVersion
                    env.RESOLVED_GF_VERSION  = params.GF_VERSION?.trim()  ?: cfg.gfVersion
                    env.SELENIUM_ENABLED     = cfg.seleniumEnabled ? 'true' : 'false'
                    env.TCK_THREAD_COUNT     = params.THREAD_COUNT?.trim() ?: cfg.threadCount.toString()
                    env.RELEASE_LINE         = params.RELEASE_LINE
                    env.IMPL_BRANCH          = cfg.implBranch
                    env.API_BRANCH           = cfg.apiBranch ?: ''
                    if (!JDK_DISTRO_BY_VERSION.containsKey(env.RESOLVED_JDK)) {
                        error "No JDK distro configured for JDK ${env.RESOLVED_JDK}. Update JDK_DISTRO_BY_VERSION at the top of Jenkinsfile."
                    }
                    if (!JDK_DISTRO_BY_VERSION.containsKey(env.RESOLVED_TCK_JDK)) {
                        error "No JDK distro configured for TCK JDK ${env.RESOLVED_TCK_JDK}. Update JDK_DISTRO_BY_VERSION at the top of Jenkinsfile."
                    }
                    env.JAVA_HOME      = "${env.TOOLS_PREFIX}/java/${JDK_DISTRO_BY_VERSION[env.RESOLVED_JDK]}/jdk-${env.RESOLVED_JDK}/latest"
                    env.TCK_JAVA_HOME  = "${env.TOOLS_PREFIX}/java/${JDK_DISTRO_BY_VERSION[env.RESOLVED_TCK_JDK]}/jdk-${env.RESOLVED_TCK_JDK}/latest"
                    env.PATH           = "${env.MVN_HOME}/bin:${env.JAVA_HOME}/bin:${env.PATH}"

                    sh 'java -version && mvn -v'
                }
                // Mojarra checkout. When .gitmodules is present (5.0+), initialize the faces/ submodule
                // tracking the configured branch tip rather than the recorded SHA, so a -SNAPSHOT TCK
                // builds against the latest TCK sources. The submodule is used only by the TCK stage;
                // the api artifact itself comes from Maven Central, not this checkout.
                checkout([$class: 'GitSCM',
                    branches: [[name: "*/${env.IMPL_BRANCH}"]],
                    userRemoteConfigs: [[url: 'git@github.com:eclipse-ee4j/mojarra.git',
                                         credentialsId: 'github-bot-ssh']],
                    extensions: [
                        [$class: 'SubmoduleOption',
                         disableSubmodules: false,
                         parentCredentials: true,
                         recursiveSubmodules: false,
                         trackingSubmodules: true]
                    ]])
                script {
                    def cfg = BRANCH_CONFIG[params.RELEASE_LINE]

                    // Read snapshot version from pom.xml; the release version is always derived from it.
                    // -Doutput (over returnStdout): help:evaluate writes the bare value to the file, while
                    // Maven's own logging — including [ERROR] on a resolution failure — stays on the
                    // console instead of being captured into the discarded-on-throw return value.
                    sh "mvn -B ${env.HELP_PLUGIN}:evaluate -Dexpression=project.version -q -Doutput=pom-version.txt"
                    def snapshot = readFile('pom-version.txt').trim()
                    if (!(snapshot ==~ /.*-SNAPSHOT$/)) {
                        error "Top-level pom version '${snapshot}' is not a -SNAPSHOT; refusing to release."
                    }
                    env.SNAPSHOT_VERSION = snapshot
                    def baseVersion = snapshot.replace('-SNAPSHOT', '')

                    // Milestone/RC release path: derive RELEASE_VERSION as <pom-base>-<MILESTONE_VERSION>,
                    // skip the GA-format check and the next-snapshot bump, and tag without -RELEASE suffix.
                    def milestoneSuffix = params.MILESTONE_VERSION?.trim()
                    if (milestoneSuffix) {
                        if (!(milestoneSuffix ==~ /^(M|RC)\d+$/)) {
                            error "MILESTONE_VERSION '${milestoneSuffix}' must match ^(M|RC)[0-9]+\$ (e.g. M1, M2, RC1, RC2)."
                        }
                        env.IS_MILESTONE    = 'true'
                        env.RELEASE_VERSION = "${baseVersion}-${milestoneSuffix}"
                        env.RELEASE_TAG     = env.RELEASE_VERSION
                        env.RELEASE_BRANCH  = env.RELEASE_VERSION
                    } else {
                        env.IS_MILESTONE    = 'false'
                        env.RELEASE_VERSION = baseVersion
                        requireGaVersion('RELEASE_VERSION', env.RELEASE_VERSION, params.RELEASE_LINE)
                        env.NEXT_VERSION    = bumpLastComponent(env.RELEASE_VERSION) + '-SNAPSHOT'
                        env.RELEASE_TAG     = "${env.RELEASE_VERSION}-RELEASE"
                        env.RELEASE_BRANCH  = env.RELEASE_VERSION
                    }

                    // Skip old-tck by excluding its modules from the reactor entirely (-pl), so
                    // they aren't even parsed/built — faster and cleaner than -Dtck.old.skip=true,
                    // which leaves the modules in the reactor and only short-circuits their
                    // antrun executions. old-tck-selenium is a separate failsafe-driven path and
                    // is not affected.
                    env.SKIP_OLD_TCK_FLAG = params.SKIP_OLD_TCK ? '-pl -:old-faces-tck-parent,-:old-tck-build,-:old-tck-run' : ''

                    // SMOKE_TEST: smoke-test subset for iterating on the pipeline itself. Filters
                    // failsafe ITs to three representative classes and old-tck JavaTest to one
                    // small path, dropping a 30+ min cycle to ~3 min (or ~12 min with old-tck
                    // enabled). Hard-gated on DRY_RUN: the filtered run is not TCK-conformant,
                    // and must never produce a published release.
                    //   -Drun.test=...  : antrun config in old-tck/run/pom.xml flips to
                    //                     `ant runclient -Dmultiple.tests=${run.test}` when set.
                    env.SMOKE_TEST_FLAGS = (params.SMOKE_TEST && params.DRY_RUN) \
                        ? "-Dit.test=**/JSFSigTestIT.java,**/ChildCountTestIT.java,**/AjaxTestsIT.java -Dfailsafe.failIfNoSpecifiedTests=false -Drun.test=com/sun/ts/tests/jsf/api/jakarta_faces/application/facesmessage" \
                        : ''

                    // Resolve the jakarta.faces-api version the impl depends on and the TCK runs
                    // against (-Dfaces.version). Normally consumed from Maven Central (released via
                    // the jakartaee/faces job) and NOT built here.
                    //   - 5.0+ (apiBranch != null): read impl/pom.xml's jakarta.faces-api dep. A
                    //     concrete (milestone/GA) dep is used as-is; a -SNAPSHOT dep resolves to this
                    //     run's RELEASE_VERSION (impl + api are cut in lockstep at the same version).
                    //     If that version is on Central -> consume it (impl-only build). If it isn't:
                    //       * DRY_RUN + -SNAPSHOT dep -> build the api from the faces/ submodule into
                    //         the reactor (-Papi) so the impl build can still be validated;
                    //       * otherwise -> error (release the api via the jakartaee/faces job first).
                    //   - 4.x (apiBranch == null): impl bundles the spec API into its own jar and
                    //     declares no jakarta.faces-api dep, so use the per-branch cfg.apiVersion.
                    if (cfg.apiBranch != null) {
                        def apiDepVersion = readImplApiDepVersion()
                        if (apiDepVersion == '') {
                            error "impl/pom.xml does not declare a jakarta.faces-api dependency."
                        }
                        def isSnapshotDep = apiDepVersion.endsWith('-SNAPSHOT')
                        def concreteApi = isSnapshotDep ? env.RELEASE_VERSION : apiDepVersion
                        // curl -fsI HEADs the Central pom; exit 0 iff the api is already published.
                        def apiOnCentral = (sh(returnStatus: true, script: """#!/bin/bash
                            curl -fsI 'https://repo1.maven.org/maven2/jakarta/faces/jakarta.faces-api/${concreteApi}/jakarta.faces-api-${concreteApi}.pom' >/dev/null
                        """) == 0)
                        if (apiOnCentral) {
                            env.RESOLVED_API_VERSION = concreteApi
                            env.MVN_API_PROFILE = ''
                            env.API_SOURCE = 'Maven Central'
                            echo "[api-check] jakarta.faces-api ${concreteApi} present on Maven Central."
                        } else if (params.DRY_RUN && isSnapshotDep) {
                            // DRY_RUN fallback: consume the unreleased -SNAPSHOT built from the faces/
                            // submodule reactor, so a dry-run doesn't require the api to be published.
                            env.RESOLVED_API_VERSION = apiDepVersion
                            env.MVN_API_PROFILE = '-Papi'
                            env.API_SOURCE = 'faces/ submodule'
                            echo "[api-check] jakarta.faces-api ${concreteApi} not on Maven Central; DRY_RUN will build ${apiDepVersion} from the faces/ submodule."
                        } else {
                            error "jakarta.faces-api ${concreteApi} is not on Maven Central. Release it first via the jakartaee/faces release job, then re-run."
                        }
                        env.IMPL_API_DEP_VERSION = env.RESOLVED_API_VERSION
                    } else {
                        env.IMPL_API_DEP_VERSION = cfg.apiVersion
                        env.MVN_API_PROFILE = ''
                    }

                    def jdkLabel = (env.RESOLVED_JDK == env.RESOLVED_TCK_JDK)
                        ? "JDK${env.RESOLVED_JDK}"
                        : "JDK${env.RESOLVED_JDK}/TCK-JDK${env.RESOLVED_TCK_JDK}"
                    def tckLabel = params.RUN_TCK
                        ? "TCK ${env.RESOLVED_TCK_VERSION}" + (env.TCK_THREAD_COUNT == '1' ? '' : " -T${env.TCK_THREAD_COUNT}")
                        : "TCK skipped"
                    // old-TCK exists only on 4.x; on 5.0+ the module is gone so the flag is a no-op.
                    def skipOldTckLabel = (params.RELEASE_LINE.startsWith('4.') && params.RUN_TCK && params.SKIP_OLD_TCK) ? ', old-TCK skipped' : ''
                    def smokeTestLabel = (params.RUN_TCK && params.SMOKE_TEST && params.DRY_RUN) ? ', smoke-test' : ''
                    def milestoneLabel = (env.IS_MILESTONE == 'true') ? ', milestone' : ''
                    def dryRunLabel = params.DRY_RUN ? ', dry-run' : ''
                    currentBuild.description = "${params.RELEASE_LINE} → ${env.RELEASE_VERSION}" +
                        (env.RESOLVED_API_VERSION ? " (API ${env.RESOLVED_API_VERSION} from ${env.API_SOURCE})" : '') +
                        " (${jdkLabel}, GF ${env.RESOLVED_GF_VERSION}, ${tckLabel}${skipOldTckLabel}${smokeTestLabel}${milestoneLabel}${dryRunLabel})"
                    echo renderBanner(buildBannerLines(params, env, cfg))
                }
                // Validate every credential the publish path will need, even on DRY_RUN, so a
                // revoked/expired credential fails in minute zero rather than after a multi-hour
                // TCK run. Skippable via SKIP_CRED_CHECK for pipeline-debug runs where the publish
                // credentials are unavailable or known-stale. GPG is not pinged here because Build
                // & install signs sources/javadoc and would fail on a bad keyring within minutes.
                // Maven offers no native dryRun for Central deploys; the rc-list probe below is the
                // only zero-cost auth check.
                script {
                    if (!params.SKIP_CRED_CHECK) {
                        // Probe the Sonatype Central Portal credentials by listing the namespace's
                        // deployments via the eclipse-cbi central-staging plugin. The plugin reads
                        // <server id=central> from the mounted settings.xml and decrypts a
                        // maven-encrypted ({...}) password through SecDispatcher exactly as the real
                        // deploy does, so the probe authenticates with the same token the deploy will
                        // use whether it is stored plaintext or encrypted. A hand-rolled curl can't:
                        // help:effective-settings -DshowPasswords=true only unmasks, it does not run
                        // SecDispatcher, so an encrypted password reaches curl as the literal {...}
                        // blob and yields a spurious 401. rc-list throws on any non-2xx (IOException ->
                        // MojoFailureException -> non-zero exit), so a bad/expired token aborts the run
                        // in minute zero rather than after a multi-hour TCK. central.bearerCreate builds
                        // the Portal bearer token from the decrypted token user/password.
                        sh '''#!/bin/bash -e
                            # central-staging-plugins is compiled for Java 17; the 4.x impl builds on
                            # JDK 11, so run this probe under TCK_JAVA_HOME (>= 17 on every line).
                            export JAVA_HOME="${TCK_JAVA_HOME}"
                            export PATH="${JAVA_HOME}/bin:${PATH}"
                            mvn -B ${MVN_EXTRA} ${CENTRAL_PLUGIN}:rc-list \\
                                -Dcentral.namespace=org.glassfish \\
                                -Dcentral.bearerCreate=true \\
                                -Dcentral.showAllDeployments=true \\
                                -Dcentral.showArtifacts=false
                            echo "[cred-check] Sonatype Central Portal: ok"
                        '''
                        // GitHub SSH push: --dry-run performs the full receive-pack handshake (incl. the
                        // write-permission check on protected refs) but transmits no objects and never
                        // creates the remote ref.
                        sshagent(credentials: ['github-bot-ssh']) {
                            sh '#!/bin/bash -e\n' + KNOWN_HOSTS_INIT + '''
                                git push --dry-run git@github.com:eclipse-ee4j/mojarra.git HEAD:refs/heads/__cred_check__
                                echo "[cred-check] GitHub SSH push (mojarra): ok"
                            '''
                        }
                        // GitHub bot token: gh auth status calls /user under the hood, so it fails on an
                        // expired/revoked token. Same withCredentials shape Publish to GitHub uses later.
                        withCredentials([usernamePassword(credentialsId: 'github-bot',
                                                          usernameVariable: 'GH_USER',
                                                          passwordVariable: 'GH_TOKEN')]) {
                            sh '#!/bin/bash -e\n' + GH_INSTALL + '''
                                gh auth status
                                echo "[cred-check] GitHub bot token: ok"
                            '''
                        }
                    }
                }
            }
        }

        stage('Build & install') {
            steps {
                sshagent(credentials: ['github-bot-ssh']) {
                    withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
                        // GPG init + git identity + branch/tag conflict check + local release branch.
                        // TAG_ONLY=${IS_MILESTONE} skips the branch check on milestone runs (where the
                        // local branch is never pushed).
                        sh '#!/bin/bash -ex\nexport BRANCH_NAME="${RELEASE_BRANCH}" TAG_NAME="${RELEASE_TAG}" TAG_ONLY="${IS_MILESTONE}"\n' +
                           GPG_GIT_INIT + REMOTE_REF_CONFLICT_CHECK
                        // Set the release version (cascades to impl). When consuming the api from
                        // Central, pin impl's jakarta.faces-api dep to the resolved concrete version so
                        // the released impl declares it. On the DRY_RUN submodule fallback (-Papi) the
                        // dep is left at its -SNAPSHOT to match the reactor-built api.
                        sh '''#!/bin/bash -ex
                            mvn -U -B ${MVN_EXTRA} \\
                                -DnewVersion="${RELEASE_VERSION}" -DgenerateBackupPoms=false \\
                                clean ${VERSIONS_PLUGIN}:set

                            if [ -z "${MVN_API_PROFILE}" ] && [ -n "${RESOLVED_API_VERSION:-}" ]; then
                                mvn -U -B ${MVN_EXTRA} -pl impl ${VERSIONS_PLUGIN}:use-dep-version \\
                                    -Dincludes=jakarta.faces:jakarta.faces-api \\
                                    -DdepVersion="${RESOLVED_API_VERSION}" \\
                                    -DforceVersion=true -DgenerateBackupPoms=false
                            fi
                            git add -A '*pom.xml'
                            git commit -m "Prepare release ${RELEASE_VERSION}"
                        '''
                        // Build & install. -Papi builds the api from the faces/ submodule into the
                        // reactor (DRY_RUN fallback only); otherwise impl-only, api from Maven Central.
                        sh '''#!/bin/bash -ex
                            mvn -U -B ${MVN_EXTRA} ${MVN_API_PROFILE} \\
                                -DskipTests -Ddoclint=none \\
                                -pl impl -am clean install
                        '''
                        // Tag locally; the push is deferred to "Publish to GitHub" after Maven Central deploy.
                        sh '''#!/bin/bash -ex
                            git tag "${RELEASE_TAG}" -m "Release ${RELEASE_VERSION}"
                        '''
                    }
                }
            }
        }

        stage('TCK') {
            when { expression { return params.RUN_TCK } }
            steps {
                sh '''#!/bin/bash -ex
                    set -o pipefail
                    # GlassFish may need a newer JDK than the impl was built with.
                    export JAVA_HOME="${TCK_JAVA_HOME}"
                    export PATH="${JAVA_HOME}/bin:${PATH}"

                    if [[ "${RESOLVED_TCK_VERSION}" == *-SNAPSHOT ]]; then
                        # -SNAPSHOT: build the TCK directly from the faces submodule (already checked
                        # out at faces/) instead of downloading a not-yet-published zip. Used while a
                        # release line's TCK has not yet shipped. The submodule's tck/ runs standalone
                        # (its parent is the faces repo top-level pom, which we don't need installed).
                        if [ ! -d faces/tck ]; then
                            echo "Cannot run -SNAPSHOT TCK: faces submodule (with tck/) is not checked out." >&2
                            exit 1
                        fi
                        TCK_BUNDLE_DIR="faces"
                        TCK_SOURCE="faces submodule @ $(cd faces && git rev-parse HEAD) (branch: ${API_BRANCH:-?})"
                    else
                        # Released TCK: download the published zip from download.eclipse.org.
                        rm -rf "faces-tck-${RESOLVED_TCK_VERSION}"
                        mkdir -p download
                        TCK_BUNDLE_NAME="jakarta-faces-tck-${RESOLVED_TCK_VERSION}"
                        TCK_BUNDLE_DIR="faces-tck-${RESOLVED_TCK_VERSION}"
                        TCK_URL="https://download.eclipse.org/jakartaee/faces/${RELEASE_LINE}/${TCK_BUNDLE_NAME}.zip"

                        wget -q "${TCK_URL}" -O "download/${TCK_BUNDLE_NAME}.zip"
                        unzip -q -o "download/${TCK_BUNDLE_NAME}.zip"
                        TCK_SOURCE="${TCK_URL} (sha256 $(sha256sum "download/${TCK_BUNDLE_NAME}.zip" | awk '{print $1}'))"

                        # Workaround for an upstream TCK packaging typo: tck/faces23/converter/pom.xml
                        # declares <finalName>test-faces23-ajax</finalName>, colliding with the ajax
                        # module's deploy and breaking Issue4070IT. Drop once a fixed TCK zip ships and
                        # BRANCH_CONFIG.tckVersion is bumped past it.
                        CONVERTER_POM="${TCK_BUNDLE_DIR}/tck/faces23/converter/pom.xml"
                        if [ -f "${CONVERTER_POM}" ] && grep -q "<finalName>test-faces23-ajax</finalName>" "${CONVERTER_POM}"; then
                            sed -i.bak 's|<finalName>test-faces23-ajax</finalName>|<finalName>test-faces23-converter</finalName>|' "${CONVERTER_POM}"
                            echo "[tck-patch] fixed finalName typo in ${CONVERTER_POM}"
                        fi
                    fi

                    if [ -n "${GF_BUNDLE_URL}" ]; then
                        wget -q "${GF_BUNDLE_URL}" -O glassfish.zip
                        mvn ${MVN_EXTRA} install:install-file -Dfile=./glassfish.zip \\
                            -DgroupId=org.glassfish.main.distributions \\
                            -DartifactId=glassfish -Dversion="${RESOLVED_GF_VERSION}" -Dpackaging=zip
                    fi

                    # Surface the exact TCK bundle source into run.log so post-run forensics don't
                    # have to dig through the Jenkins console log to figure out which faces SHA (or
                    # which downloaded zip's sha256) was actually built against.
                    echo "[tck-bundle] ${TCK_SOURCE}" | tee -a "${WORKSPACE}/run.log"

                    # Failsafe gates on test failures via its own non-zero exit; per-module
                    # failsafe-summary.xml files are aggregated below into summary.txt.
                    cd "${TCK_BUNDLE_DIR}/tck"
                    # -T / -Dgf.pool.size: parallel reactor with the gf-pool pre-provisioned to
                    # match. Set per-line in BRANCH_CONFIG.threadCount (>1 only for TCKs with
                    # gf-pool support; 4.x stays at 1 — single managed GlassFish per module).
                    # env -u JAVA_TOOL_OPTIONS: strip the (intentionally empty) inherited var so
                    # child JVMs don't print the "Picked up JAVA_TOOL_OPTIONS:" banner on each fork.
                    env -u JAVA_TOOL_OPTIONS \\
                    mvn ${MVN_EXTRA} -T ${TCK_THREAD_COUNT} clean install \\
                        ${SKIP_OLD_TCK_FLAG} -Dtest.selenium=${SELENIUM_ENABLED} \\
                        -Dgf.pool.size=${TCK_THREAD_COUNT} \\
                        -Dwdm.cachePath=/home/jenkins/agent/caches/selenium \\
                        -DskipAssembly=true -Pstaging \\
                        -Dglassfish.version="${RESOLVED_GF_VERSION}" \\
                        -Dmojarra.version="${RELEASE_VERSION}" \\
                        -Dfaces.version="${IMPL_API_DEP_VERSION}" \\
                        ${SMOKE_TEST_FLAGS} \\
                        | tee "${WORKSPACE}/run.log"

                    cd "${WORKSPACE}"
                    # Each new-TCK module writes its own target/failsafe-reports/failsafe-summary.xml
                    # during the failsafe verify phase. The TCK pom doesn't aggregate these into a
                    # single XML at the parent (the `-Daggregate=true` flag aggregates HTML, not
                    # XML), so sum across all modules manually. Old-tck modules use ant/javatest
                    # and don't produce a failsafe-summary.xml; their results live in run.log only.
                    # XML format per module:
                    #   <failsafe-summary ...>
                    #     <completed>N</completed>  (= passed + failed + errors, excluding skipped)
                    #     <errors>N</errors>
                    #     <failures>N</failures>
                    #     <skipped>N</skipped>
                    #   </failsafe-summary>
                    SUMMARIES=$(find "${TCK_BUNDLE_DIR}/tck" -path '*/target/failsafe-reports/failsafe-summary.xml')
                    if [ -z "${SUMMARIES}" ]; then
                        echo "No failsafe-summary.xml files found under ${TCK_BUNDLE_DIR}/tck." >&2
                        exit 1
                    fi
                    echo "Aggregating $(echo "${SUMMARIES}" | wc -l) failsafe-summary.xml files..."
                    # Silence trace for the rest of the parse/aggregation as it's quite noisy; restored at end.
                    set +x

                    # Failsafe (new-TCK + old-tck-selenium): per-module XML aggregation.
                    extract() { sed -n "s|.*<$2>\\([0-9]*\\)</$2>.*|\\1|p" "$1" | head -1; }
                    COMPLETED=0; ERRORS=0; FAILED=0
                    for f in ${SUMMARIES}; do
                        c=$(extract "$f" completed); c=${c:-0}
                        e=$(extract "$f" errors);    e=${e:-0}
                        F=$(extract "$f" failures);  F=${F:-0}
                        COMPLETED=$(( COMPLETED + c ))
                        ERRORS=$(( ERRORS + e ))
                        FAILED=$(( FAILED + F ))
                    done
                    echo "TCK results: completed=${COMPLETED} failed=${FAILED} errors=${ERRORS}"

                    # Old-tck (ant/JavaTest harness) doesn't write failsafe-summary.xml; its summary
                    # lands in run.log as four canonical lines emitted by the harness:
                    #   Completed running N tests.
                    #   Number of Tests Passed      = N
                    #   Number of Tests Failed      = N
                    #   Number of Tests with Errors = N
                    # Take the last occurrence (in case the harness ran more than once) and fold its
                    # counts into the totals. No-op when SKIP_OLD_TCK=true or on 5.0+.
                    OLD_TCK_LINE=$(grep -E "Completed running [0-9]+ tests" "${WORKSPACE}/run.log" | tail -1 || true)
                    if [ -n "${OLD_TCK_LINE}" ]; then
                        old_count() { grep -E "$1" "${WORKSPACE}/run.log" | tail -1 | sed -E "s/.*=[[:space:]]*([0-9]+).*/\\1/"; }
                        OLD_PASSED=$(old_count "Number of Tests Passed");      OLD_PASSED=${OLD_PASSED:-0}
                        OLD_FAILED=$(old_count "Number of Tests Failed");      OLD_FAILED=${OLD_FAILED:-0}
                        OLD_ERRORS=$(old_count "Number of Tests with Errors"); OLD_ERRORS=${OLD_ERRORS:-0}
                        echo "Old-TCK results: passed=${OLD_PASSED} failed=${OLD_FAILED} errors=${OLD_ERRORS}"
                        COMPLETED=$(( COMPLETED + OLD_PASSED + OLD_FAILED + OLD_ERRORS ))
                        ERRORS=$(( ERRORS + OLD_ERRORS ))
                        FAILED=$(( FAILED + OLD_FAILED ))
                    fi
                    PASSED=$(( COMPLETED - ERRORS - FAILED ))
                    set -x

                    {
                        echo "******************************************************"
                        echo "Mojarra ${RELEASE_VERSION} (built with JDK${RESOLVED_JDK}) on GlassFish ${RESOLVED_GF_VERSION} (TCK run with JDK${RESOLVED_TCK_JDK})"
                        if [ -n "${RESOLVED_API_VERSION:-}" ]; then
                            echo "jakarta.faces-api ${RESOLVED_API_VERSION}"
                        fi
                        echo "Faces TCK ${RESOLVED_TCK_VERSION}"
                        echo "Passed: ${PASSED}  Failed: ${FAILED}  Errors: ${ERRORS}"
                        echo "TCK source : ${TCK_SOURCE}"
                        if [ -n "${TCK_URL:-}" ] && [ -f "download/${TCK_BUNDLE_NAME}.zip" ]; then
                            echo "TCK download: ${TCK_URL}"
                            echo "SHA256 TCK : $(sha256sum download/${TCK_BUNDLE_NAME}.zip | awk '{print $1}')"
                        fi
                        echo "SHA256 IMPL: $(sha256sum ${TCK_BUNDLE_DIR}/tck/target/glassfish*/glassfish/modules/jakarta.faces.jar | awk '{print $1}')"
                        echo "JDK: $(java -version 2>&1 | head -1)"
                        echo "OS : $(lsb_release -ds 2>/dev/null || cat /etc/os-release | head -1)"
                        echo "******************************************************"
                    } > summary.txt

                    # Machine-readable validation record, consumed by the jakartaee/faces API
                    # release job to gate its Central publish on a green TCK. Emitted only for
                    # release lines that HAVE a standalone api (API_BRANCH set, i.e. 5.0+) and only
                    # on a clean pass; -e means a failing TCK never reaches this point.
                    if [ -n "${API_BRANCH}" ] && [ "${FAILED}" -eq 0 ] && [ "${ERRORS}" -eq 0 ]; then
                        FACES_SHA=$( [ -d faces ] && git -C faces rev-parse HEAD || echo "" )
                        cat > tck-validation.json <<EOF
{
  "line":       "${RELEASE_LINE}",
  "apiVersion": "${IMPL_API_DEP_VERSION}",
  "facesSha":   "${FACES_SHA}",
  "mojarraSha": "$(git rev-parse HEAD)",
  "dryRun":     ${DRY_RUN},
  "runTck":     true,
  "tckVersion": "${RESOLVED_TCK_VERSION}",
  "glassfish":  "${RESOLVED_GF_VERSION}",
  "passed":     ${PASSED},
  "result":     "SUCCESS",
  "buildUrl":   "${BUILD_URL}",
  "timestamp":  "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF
                    fi
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'run.log, summary.txt, tck-validation.json',
                                     allowEmptyArchive: true, fingerprint: true
                }
            }
        }

        stage('Deploy to Maven Central') {
            when { expression { return !params.DRY_RUN && !params.SKIP_DEPLOY } }
            steps {
                withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
                    // -Poss-release activates the EE4J parent's release profile, which wires
                    // central-publishing-maven-plugin (Sonatype Portal), GPG signing, and the
                    // sources/javadoc jars Maven Central requires. Without this profile, `mvn deploy`
                    // does not reach Maven Central, so only CI publishes. Impl-only; jakarta.faces-api
                    // resolves from Maven Central.
                    sh '#!/bin/bash -ex\n' + GPG_INIT + '''
                        mvn -U -B ${MVN_EXTRA} -Poss-release \\
                            -DskipTests -Ddoclint=none \\
                            -pl impl -am deploy
                    '''
                }
            }
        }

        stage('Bump to next snapshot') {
            when { expression { return env.IS_MILESTONE != 'true' } }
            steps {
                sh '''#!/bin/bash -ex
                    mvn -U -B ${MVN_EXTRA} \\
                        -DnewVersion="${NEXT_VERSION}" -DgenerateBackupPoms=false \\
                        ${VERSIONS_PLUGIN}:set
                    git add -A '*pom.xml'
                    git commit -m "Prepare next development cycle for ${NEXT_VERSION}"
                '''
            }
        }

        stage('Publish to GitHub') {
            when { expression { return !params.DRY_RUN } }
            steps {
                // Push the tag (and the release branch on GA runs only — milestone runs leave the
                // source branch untouched and never push the local release branch).
                sshagent(credentials: ['github-bot-ssh']) {
                    sh '#!/bin/bash -ex\n' + KNOWN_HOSTS_INIT + '''
                        if [ "${IS_MILESTONE}" != "true" ]; then
                            git push origin "${RELEASE_BRANCH}"
                        fi
                        git push origin "${RELEASE_TAG}"
                    '''
                }
                // GA-only: squash-merge the release branch into the source branch so "Prepare release"
                // + "Prepare next development cycle" land as a single commit titled
                // "<version> has been released", manage milestones, and draft+publish a GitHub release.
                script {
                    if (env.IS_MILESTONE != 'true') {
                        withCredentials([usernamePassword(credentialsId: 'github-bot',
                                                          usernameVariable: 'GH_USER',
                                                          passwordVariable: 'GH_TOKEN')]) {
                            sh '#!/bin/bash -ex\n' + GH_INSTALL + '''
                                gh pr create --base "${IMPL_BRANCH}" --head "${RELEASE_BRANCH}" \\
                                    --title "Mojarra ${RELEASE_VERSION} has been released" \\
                                    --body "${BUILD_URL}"
                                gh pr merge "${RELEASE_BRANCH}" --squash \\
                                    --subject "Mojarra ${RELEASE_VERSION} has been released" \\
                                    --body "${BUILD_URL}"
                            '''
                            // Close the just-released milestone (if it exists), open a milestone for the
                            // next snapshot, and draft+publish a GitHub release at the just-pushed tag
                            // with auto-generated notes prepended by a one-line summary, the Maven Central
                            // link, and a link to the closed milestone. Best-effort on milestones — a
                            // missing or pre-existing milestone must not fail this stage.
                            sh '#!/bin/bash -ex\n' + GH_INSTALL + '''
                                NEXT_MILESTONE="${NEXT_VERSION%-SNAPSHOT}"
                                REPO_SLUG=$(gh repo view --json nameWithOwner --jq .nameWithOwner)

                                MILESTONE_NUMBER=$(gh api "repos/{owner}/{repo}/milestones?state=open&per_page=100" \\
                                    --jq ".[] | select(.title==\\"${RELEASE_VERSION}\\") | .number")
                                if [ -n "${MILESTONE_NUMBER}" ]; then
                                    gh api -X PATCH "repos/{owner}/{repo}/milestones/${MILESTONE_NUMBER}" -f state=closed
                                else
                                    echo "No open milestone titled '${RELEASE_VERSION}' to close; skipping."
                                fi

                                if gh api "repos/{owner}/{repo}/milestones?state=all&per_page=100" \\
                                        --jq ".[] | select(.title==\\"${NEXT_MILESTONE}\\") | .number" | grep -q .; then
                                    echo "Milestone '${NEXT_MILESTONE}' already exists; skipping create."
                                else
                                    gh api -X POST "repos/{owner}/{repo}/milestones" -f title="${NEXT_MILESTONE}"
                                fi

                                # Anchor the auto-generated notes to the previous *-RELEASE tag in the same
                                # major.minor family; otherwise GitHub picks the most recent semver tag
                                # repo-wide, which may belong to a different release line.
                                PREVIOUS_TAG=$(git tag -l "${RELEASE_LINE}.*-RELEASE" \\
                                    | grep -v "^${RELEASE_TAG}$" | sort -V | tail -1)
                                if [ -n "${PREVIOUS_TAG}" ]; then
                                    GENERATED=$(gh api -X POST "repos/{owner}/{repo}/releases/generate-notes" \\
                                        -f tag_name="${RELEASE_TAG}" -f target_commitish="${IMPL_BRANCH}" \\
                                        -f previous_tag_name="${PREVIOUS_TAG}" --jq .body)
                                else
                                    GENERATED=$(gh api -X POST "repos/{owner}/{repo}/releases/generate-notes" \\
                                        -f tag_name="${RELEASE_TAG}" -f target_commitish="${IMPL_BRANCH}" --jq .body)
                                fi

                                # Drop the noise bullets generated for prior squash-merged release PRs
                                # (titled "<version> has been released"), which carry no real changelog value.
                                GENERATED=$(echo "${GENERATED}" | grep -v "has been released" || true)

                                {
                                    echo "## ${RELEASE_VERSION} has been released"
                                    echo
                                    echo "- Maven Central: https://repo1.maven.org/maven2/org/glassfish/jakarta.faces/${RELEASE_VERSION}/"
                                    if [ -n "${MILESTONE_NUMBER}" ]; then
                                        echo "- Milestone: https://github.com/${REPO_SLUG}/milestone/${MILESTONE_NUMBER}?closed=1"
                                    fi
                                    echo
                                    echo "${GENERATED}"
                                } > release-notes.md

                                gh release create "${RELEASE_TAG}" --target "${IMPL_BRANCH}" \\
                                    --title "${RELEASE_VERSION}" \\
                                    --notes-file release-notes.md \\
                                    --latest=true
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def kind = (env.IS_MILESTONE == 'true') ? 'Milestone' : 'Released'
                echo "${kind} ${env.RELEASE_VERSION} from ${params.RELEASE_LINE}."
            }
        }
        failure { echo "Release of ${env.RELEASE_VERSION} from ${params.RELEASE_LINE} FAILED." }
    }
}

// Bump the last numeric component of a dotted version: "5.0.0" -> "5.0.1", "4.1.10" -> "4.1.11".
// Caller must ensure the last component is numeric (see requireGaVersion).
def bumpLastComponent(String version) {
    def parts = version.tokenize('.')
    parts[-1] = (parts[-1].toInteger() + 1).toString()
    return parts.join('.')
}

// Validate that `version` is a dotted-numeric GA version with at least three components (rejects
// -M2/-RC1 and ambiguous two-component values like "5.0" which would bumpLastComponent to a minor
// rather than a patch). When `expectedPrefix` is non-null, also verifies the prefix match.
// Aborts the build on mismatch.
def requireGaVersion(String paramName, String version, String expectedPrefix) {
    if (!(version ==~ /\d+\.\d+\.\d+(\.\d+)*/)) {
        error "${paramName} '${version}' is not a dotted-numeric GA version with at least 3 components (e.g. 4.1.5). Milestone/RC and two-component versions are not supported."
    }
    if (expectedPrefix != null && !version.startsWith(expectedPrefix + '.')) {
        error "${paramName} '${version}' does not match expected prefix '${expectedPrefix}.'."
    }
}

// Compose the human-readable banner lines printed at the end of the Prepare stage. Always-on lines
// describe the artifacts being released and the build/test environment; conditional lines call out
// active toggles (DRY_RUN, SMOKE_TEST, SKIP_OLD_TCK, SKIP_DEPLOY, SKIP_CRED_CHECK, RUN_TCK off).
def buildBannerLines(params, env, cfg) {
    def lines = []
    if (env.IS_MILESTONE == 'true') {
        lines << "Mojarra ${env.RELEASE_VERSION} milestone (snapshot ${env.SNAPSHOT_VERSION} left untouched)"
    } else {
        lines << "Mojarra ${env.RELEASE_VERSION} release (snapshot ${env.SNAPSHOT_VERSION}, next ${env.NEXT_VERSION})"
    }
    if (cfg.apiBranch != null) {
        lines << "depends on jakarta.faces-api ${env.RESOLVED_API_VERSION} (from ${env.API_SOURCE})"
    }
    def jdkLabel = (env.RESOLVED_JDK == env.RESOLVED_TCK_JDK) \
        ? "JDK${env.RESOLVED_JDK}" \
        : "JDK${env.RESOLVED_JDK} (build) / JDK${env.RESOLVED_TCK_JDK} (TCK)"
    def tckBannerLabel = params.RUN_TCK
        ? ", Faces TCK ${env.RESOLVED_TCK_VERSION}" + (env.TCK_THREAD_COUNT == '1' ? '' : " (-T ${env.TCK_THREAD_COUNT})")
        : ''
    lines << "${jdkLabel}, GlassFish ${env.RESOLVED_GF_VERSION}${tckBannerLabel}"
    if (!params.RUN_TCK)    lines << "- RUN_TCK off: TCK skipped entirely"
    if (params.SKIP_OLD_TCK && params.RELEASE_LINE.startsWith('4.')) lines << "- SKIP_OLD_TCK: old-tck JavaTest modules excluded from reactor"
    if (params.SMOKE_TEST)  lines << "- SMOKE_TEST: smoke-test subset only (NOT TCK-conformant)"
    if (params.DRY_RUN)     lines << "- DRY_RUN: skips Maven Central deploy and GitHub push"
    if (params.SKIP_DEPLOY) lines << "- SKIP_DEPLOY: skips deploy but still pushes branch/tag and creates GitHub release"
    if (params.SKIP_CRED_CHECK) lines << "- SKIP_CRED_CHECK: skips Prepare-stage credential checks"
    return lines
}

// Render banner lines into a multi-line string with an ASCII border, padded to the longest line.
def renderBanner(List<String> lines) {
    int width = 0
    for (line in lines) {
        if (line.length() > width) width = line.length()
    }
    def border = '*' * (width + 4)
    def out = new StringBuilder('\n').append(border).append('\n')
    for (line in lines) {
        out.append("* ").append(line.padRight(width)).append(" *\n")
    }
    out.append(border)
    return out.toString()
}

// Resolve the jakarta.faces:jakarta.faces-api version that impl/pom.xml effectively depends on.
// help:effective-pom applies inheritance and interpolation, so the resolved <version> is present
// even when it comes from a parent pom's dependencyManagement rather than a literal child of
// impl/pom.xml. effective-pom builds only the project model and resolves no dependency artifacts,
// so a dependency carrying an unpublished -SNAPSHOT parent cannot break this read -- unlike
// dependency:tree, which resolves the whole graph.
def readImplApiDepVersion() {
    return sh(returnStdout: true, script: '''#!/bin/bash -e
        EFF=$(mktemp)
        trap 'rm -f "$EFF"' EXIT
        # Maven logs to stdout; route it to stderr so [ERROR] on failure reaches the console
        # without polluting the version string captured by returnStdout.
        mvn -pl impl -B -q ${HELP_PLUGIN}:effective-pom -Doutput="$EFF" 1>&2
        awk 'BEGIN{RS="<dependency>"} /<groupId>jakarta\\.faces<\\/groupId>/ && /<artifactId>jakarta\\.faces-api<\\/artifactId>/' "$EFF" \\
            | grep -oP '<version>\\K[^<]+' | head -1
    ''').trim()
}
