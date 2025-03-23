plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.dnpplugins"
version = "PLATFORM2023.3_0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
}

intellij {
    version.set("2023.3") // IDE version
    type.set("IU") // IU WebStorm, IC IDEA COmmunity
    downloadSources.set(true)
    plugins.set(listOf("Git4Idea"))
}

tasks {
    patchPluginXml {
        changeNotes.set("""
            Initial release of Git Activity Logger.
        """.trimIndent())
    }
}