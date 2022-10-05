import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `java-gradle-plugin`
    `java-library`
    idea

    // `maven-publish`
    // id("com.github.breadmoirai.github-release")

    Plugins.Dokka.addTo(this)
    Plugins.KotlinJvm.addTo(this)
    Plugins.Detekt.addTo(this)
    Plugins.GithubRelease.addTo(this)

    Plugins.GradleKover.addTo(this)
    Plugins.GradleOwaspDependencyCheck.addTo(this)
    Plugins.GradleVersions.addTo(this)
    Plugins.GradleTasktree.addTo(this)
    Plugins.GradleTestlogger.addTo(this)
}

dependencies {
    listOf(
        Dependencies.Kotlin.StdlbJdk8,
        Dependencies.Kotlin.KotlinxDatetime,
        Dependencies.MuLogging,
        Dependencies.Gradle.GradleKover,
        // github release?
    ).forEach { dep ->
        dep.implementation(this)
    }

    listOf(
        Dependencies.Kotest.Core,
        Dependencies.Kotest.Assertions,
        Dependencies.Kotest.Datatest,
        Dependencies.Kotest.Property,
        Dependencies.Kotest.Hamkrest,
        Dependencies.Kotest.Mockk,
        Dependencies.Kotest.KotlinFaker,
        Dependencies.Gradle.GradleKover,
        Dependencies.Gradle.GradleTestlogger,
    ).forEach { testDep ->
        testDep.testImplementation(this)
    }

    detektPlugins(Dependencies.Gradle.DetektFormatting.toDependencyNotation())
}

configurations.all {
    exclude("org.slf4j", "slf4j-nop")
    if (isKotlinJvm) {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
        resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
            jvmTarget = "17"
            allWarningsAsErrors = false
            languageVersion = "1.7"
            apiVersion = "1.7"
        }
    }
    withType<Test>().configureEach {
        maxParallelForks = 1
        useJUnitPlatform()
        testLogging {
            setExceptionFormat("full")
            setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
        }
    }
    withType<Detekt>().configureEach {
        reports {
            html.required.set(true) // observe findings in your browser with structure and code snippets
            xml.required.set(false) // checkstyle like format mainly for integrations like Jenkins
            txt.required.set(false) // similar to console output, contains issue signature to edit baseline files
            // standardized SARIF format (https://sarifweb.azurewebsites.net/)
            // to support integrations with Github Code Scanning
            sarif.required.set(true)
        }
        jvmTarget = "17"
    }
    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "17"
    }
    test {
        extensions.configure(KoverTaskExtension::class) {
            isDisabled.set(false)
            reportFile.set(file("$buildDir/custom/result.bin"))
            includes.set(listOf("org.nixdork.hash.*"))
            //excludes = listOf("com\\.example\\.subpackage\\..*")
        }
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    revision = "release"
    rejectVersionIf { // reject all non stable versions
        isNonStable(candidate.version)
    }
    rejectVersionIf { // disallow release candidates as upgradable versions from stable versions
        isStable(currentVersion) && isNonStable(candidate.version)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("detekt"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

detekt {
    toolVersion = Versions.GradleDetekt
    buildUponDefaultConfig = true
    // allRules = false // activate all available (even unstable) rules.
    config = files("${rootDir.path}/detekt.yml")
    source = files("src/main/kotlin", "src/test/kotlin")
}

dependencyCheck {
    failOnError = true

    analyzers.experimentalEnabled = false
    analyzers.assemblyEnabled = false
    analyzers.msbuildEnabled = false
    analyzers.nuspecEnabled = false
    analyzers.nugetconfEnabled = false
    analyzers.pyPackageEnabled = false
    analyzers.pyDistributionEnabled = false
    analyzers.rubygemsEnabled = false
}

configure<TestLoggerExtension> {
    theme = ThemeType.STANDARD
    showCauses = true
    slowThreshold = 1000
    showSummary = true
    showStandardStreams = false
}

internal val Project.isKotlinJvm: Boolean
    get() = pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")

fun isNonStable(version: String): Boolean {
    val containsStableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val simpleSemverRegex = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)".toRegex()
    val isStable = containsStableKeyword || simpleSemverRegex.matches(version)
        && !listOf("alpha", "beta", "rc").any { version.toLowerCase().contains(it) } && !version.contains("M")
    return isStable.not()
}

fun isStable(version: String): Boolean = !isNonStable(version)
