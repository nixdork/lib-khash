import org.gradle.api.Project

fun Project.artifactVersion(): String =
    this.findProperty("artifactVersion")?.toString() ?: "1.0-snapshot"

object Versions {
    const val Kotlin = "1.7.20"

    const val KotlinxDatetime = "0.4.+"

    const val KotestCore = "5.5.+"

    const val GradleDetekt = "1.21.+"
    const val GradleKover = "0.6.+"
    const val GradleOwaspDependencyCheck = "7.2.+"
    const val GradleTasktree = "2.1.+"
    const val GradleTestlogger = "3.2.+"
    const val GradleVersions = "0.42.+"

    const val Dokka = "1.7.10"
    const val GithubRelease = "2.4.+"
    const val Hamkrest = "1.8.0.1"
    const val KotlinFaker = "1.11.0"
    const val Mockk = "1.13.+"
    const val MuLogging = "3.0.0"
    const val SemVer = "1.2.+"
}
