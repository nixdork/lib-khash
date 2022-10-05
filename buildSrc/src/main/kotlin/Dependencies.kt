import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.ScriptHandlerScope
import org.gradle.kotlin.dsl.exclude
import org.gradle.plugin.use.PluginDependenciesSpec
import java.lang.Runtime.Version
import java.nio.charset.CoderResult

object Plugins {
    // Kotlin / Jetbrains
    val Dokka = PluginSpec("org.jetbrains.dokka", Versions.Dokka)
    val Kotlin = PluginSpec("kotlin", Versions.Kotlin)
    val KotlinJvm = PluginSpec("org.jetbrains.kotlin.jvm", Versions.Kotlin)

    // 3rd party
    val Detekt = PluginSpec("io.gitlab.arturbosch.detekt", Versions.GradleDetekt)
    val GithubRelease = PluginSpec("com.github.breadmoirai.github-release", Versions.GithubRelease)
    val GradleKover = PluginSpec("org.jetbrains.kotlinx.kover", Versions.GradleKover)
    val GradleOwaspDependencyCheck = PluginSpec("org.owasp.dependencycheck", Versions.GradleOwaspDependencyCheck)
    val GradleVersions = PluginSpec("com.github.ben-manes.versions", Versions.GradleVersions)
    val GradleTasktree = PluginSpec("com.dorongold.task-tree", Versions.GradleTasktree)
    val GradleTestlogger = PluginSpec("com.adarshr.test-logger", Versions.GradleTestlogger)
}

object Dependencies {
    // Kotlin
    object Kotlin {
        val KotlinxDatetime = DependencySpec("org.jetbrains.kotlinx:kotlinx-datetime", Versions.KotlinxDatetime)
        val StdlbJdk8 = DependencySpec("org.jetbrains.kotlin:kotlin-stdlib-jdk8", Versions.Kotlin)
    }

    object Gradle {
        val GradleDetekt = DependencySpec("io.gitlab.arturbosch.detekt:detekt-gradle-plugin", Versions.GradleDetekt)
        val GradleKover = DependencySpec("org.jetbrains.kotlinx.kover:org.jetbrains.kotlinx.kover.gradle.plugin", Versions.GradleKover)
        val GradleOwaspDependencyCheck = DependencySpec("org.owasp.dependencycheck:org.owasp.dependencycheck.gradle.plugin", Versions.GradleOwaspDependencyCheck)
        val GradleTasktree = DependencySpec("com.dorongold.plugins:task-tree", Versions.GradleTasktree)
        val GradleTestlogger = DependencySpec("com.adarshr:gradle-test-logger-plugin", Versions.GradleTestlogger)
        val GradleVersions = DependencySpec("com.github.ben-manes:gradle-versions-plugin", Versions.GradleVersions)

        val DetektFormatting = DependencySpec("io.gitlab.arturbosch.detekt:detekt-formatting", Versions.GradleDetekt)
    }

    object Kotest {
        val Core = DependencySpec("io.kotest:kotest-runner-junit5-jvm", Versions.KotestCore)
        val Assertions = DependencySpec("io.kotest:kotest-assertions-core-jvm", Versions.KotestCore)
        val Datatest = DependencySpec("io.kotest:kotest-framework-datatest", Versions.KotestCore)
        val Property = DependencySpec("io.kotest:kotest-property", Versions.KotestCore)

        val Hamkrest = DependencySpec("com.natpryce:hamkrest", Versions.Hamkrest)
        val KotlinFaker = DependencySpec("io.github.serpro69:kotlin-faker", Versions.KotlinFaker)
        val Mockk = DependencySpec("io.mockk:mockk", Versions.Mockk)
    }

    val MuLogging = DependencySpec("io.github.microutils:kotlin-logging-jvm", Versions.MuLogging)
    val SemVer = DependencySpec("net.swiftzer.semver:semver", Versions.SemVer)

    // bouncycastle-core = { module = "org.bouncycastle:bcprov-jdk15on", version.ref = "bouncycastle" }
    // bouncycastle-ext = { module = "org.bouncycastle:bcprov-ext-jdk15on", version.ref = "bouncycastle" }
    // bouncycastle-pkix = { module = "org.bouncycastle:bcpkix-jdk15on", version.ref = "bouncycastle" }
}

data class PluginSpec(
    val id: String,
    val version: String = ""
) {
    fun addTo(scope: PluginDependenciesSpec) {
        scope.also {
            it.id(id).version(version.takeIf { v -> v.isNotEmpty() })
        }
    }

    fun addTo(action: ObjectConfigurationAction) {
        action.plugin(this.id)
    }
}

data class DependencySpec(
    val name: String,
    val version: String,
    val isChanging: Boolean = false,
    val exclude: List<String> = emptyList()
) {
    fun plugin(scope: PluginDependenciesSpec) {
        scope.apply {
            id(name).version(version.takeIf { it.isNotEmpty() })
        }
    }

    fun classpath(scope: ScriptHandlerScope) {
        val spec = this
        with(scope) {
            dependencies {
                classpath(spec.toDependencyNotation())
            }
        }
    }

    fun implementation(handler: DependencyHandlerScope) {
        val spec = this
        with(handler) {
            "implementation".invoke(spec.toDependencyNotation()) {
                isChanging = spec.isChanging
                spec.exclude.forEach { excludeDependencyNotation ->
                    val (group, module) = excludeDependencyNotation.split(":", limit = 2)
                    this.exclude(group = group, module = module)
                }
            }
        }
    }

    fun testImplementation(handler: DependencyHandlerScope) {
        val spec = this
        with(handler) {
            "testImplementation".invoke(spec.toDependencyNotation()) {
                isChanging = spec.isChanging
                spec.exclude.forEach { excludeDependencyNotation ->
                    val (group, module) = excludeDependencyNotation.split(":", limit = 2)
                    this.exclude(group = group, module = module)
                }
            }
        }
    }

    fun toDependencyNotation(): String =
        listOfNotNull(
            name,
            version.takeIf { it.isNotEmpty() }
        ).joinToString(":")
}
