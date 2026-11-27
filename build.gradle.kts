import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
    `maven-publish`
    idea
    id("net.neoforged.moddev") version "2.0.144"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

repositories {
    maven("https://modmaven.dev")
    maven("https://maven.terraformersmc.com/")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.architectury.dev/")
    maven("https://maven.shedaniel.me/")
    maven("https://cursemaven.com")
}

val modVersion = prop("mod_version")
val modGroupId = prop("mod_group_id")
val modId = prop("mod_id")
val modName = prop("mod_name")
val modLicense = prop("mod_license")
val modAuthors = prop("mod_authors")
val modDescription = prop("mod_description")
val neoVersion = prop("neo_version")
val minecraftVersion = prop("minecraft_version")
val loaderVersionRange = prop("loader_version_range")
val ae2Version = prop("ae2_version")
val jeiVersion = prop("jei_version")
val reiVersion = prop("rei_version")
val sprojectSlug = "ae2-craftcore"

version = modVersion
group = modGroupId
base.archivesName.set(sprojectSlug)
java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
java.withSourcesJar()
idea.module.isDownloadJavadoc = true

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

neoForge {
    version = neoVersion
    mods.create(modId).sourceSet(sourceSets.main.get())

    runs {
        create("client") {
            client()
            gameDirectory.set(file("run/client"))
        }

        create("client-second") {
            client()
            gameDirectory.set(file("run/client-second"))
            programArgument("--username")
            programArgument("Dev2")
        }

        create("server") {
            server()
            programArgument("--nogui")
            gameDirectory.set(file("run/server"))
        }

        configureEach {
            systemProperty("guideme.ae2.guide.sources", file("src/main/resources/assets/$modId/ae2guide").absolutePath)
            systemProperty("guideme.ae2.guide.sourcesNamespace", modId)
            logLevel = org.slf4j.event.Level.DEBUG
            additionalRuntimeClasspathConfiguration.extendsFrom(configurations.implementation.get())
        }
    }
}

val ae2 = "org.appliedenergistics:appliedenergistics2:$ae2Version"
val guideme = "org.appliedenergistics:guideme:26.1.17-beta"
val extendedAe = "curse.maven:ex-pattern-provider-892005:9746796"
val glodium = "curse.maven:glodium-957920:8196173"
val jei = "mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion"
val rei = "me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion"
// val emi = "dev.emi:emi-neoforge:1.1.22+$minecraftVersion"

dependencies {
    compileOnly(ae2)
    compileOnly(guideme)
    compileOnly(glodium)
    compileOnly(extendedAe)
    compileOnly(jei)
    compileOnly(rei)

    runtimeOnly(ae2)
    runtimeOnly(guideme)
    runtimeOnly(glodium)
    runtimeOnly(extendedAe)

    runtimeOnly(jei)
    //runtimeOnly(rei)
}

tasks.processResources {
    exclude("**/*.py")

    val replaceProperties = mapOf(
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to "[$minecraftVersion]",
        "neo_version" to neoVersion,
        "neo_version_range" to "[$neoVersion,)",
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
        "ae2_version" to ae2Version,
        "jei_version" to jeiVersion,
        "rei_version" to reiVersion
    )
    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

publishMods {
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
    type = STABLE
    modLoaders.add("neoforge")

    val fullChangelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "koO20Z1s"
        minecraftVersions.add(minecraftVersion)
        changelog = fullChangelog
        requires("ae2")
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1606104"
        projectSlug = sprojectSlug
        minecraftVersions.add(minecraftVersion)
        server = true
        client = true
        changelog = fullChangelog
        requires("applied-energistics-2")
    }
}

fun prop(name: String): String = providers.gradleProperty(name).get()