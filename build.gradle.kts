plugins {
    id("java")
    id("fabric-loom") version ("1.8.9") apply (false)

    // Mixin config plugin is a subproject for creating lithium's settings from annotations in each mixin package.
    id("net.caffeinemc.mixin-config-plugin") version ("1.0-SNAPSHOT") apply (false)
}

// Fabric: https://fabricmc.net/develop/
// Neoforge: https://neoforged.net/
val MINECRAFT_VERSION by extra { "1.21.3" }
val NEOFORGE_VERSION by extra { "21.3.16-beta" }
val FABRIC_LOADER_VERSION by extra { "0.16.9" }
val FABRIC_API_VERSION by extra { "0.107.3+1.21.3" }

// This value can be set to null to disable Parchment.
// TODO: Add Parchment
val PARCHMENT_VERSION by extra { null }

// https://semver.org/
val MOD_VERSION by extra { "0.14.1" }

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    enabled = false
}

subprojects {
    apply(plugin = "maven-publish")

    java.toolchain.languageVersion = JavaLanguageVersion.of(21)


    fun createVersionString(): String {
        val builder = StringBuilder()

        val isReleaseBuild = project.hasProperty("build.release")
        val buildId = System.getenv("GITHUB_RUN_NUMBER")

        if (isReleaseBuild) {
            builder.append(MOD_VERSION)
        } else {
            builder.append(MOD_VERSION.substringBefore('-'))
            builder.append("-snapshot")
        }

        builder.append("+mc").append(MINECRAFT_VERSION)

        if (!isReleaseBuild) {
            if (buildId != null) {
                builder.append("-build.${buildId}")
            } else {
                builder.append("-local")
            }
        }

        return builder.toString()
    }

    tasks.processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to createVersionString()))
        }
    }

    version = createVersionString()
    group = "net.caffeinemc.mods"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }

    //make builds more reproducible
    tasks.withType<AbstractArchiveTask>().configureEach {
        isReproducibleFileOrder = true
        isPreserveFileTimestamps = false
    }
}
