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
val sprojectSlug = "ae2-craftcore"

version = modVersion
group = modGroupId
base.archivesName.set(sprojectSlug)
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
val guideme = "org.appliedenergistics:guideme:21.1.22"
val extendedAe = "maven.modrinth:JiOqfoFM:bC302UUP"
val glodium = "maven.modrinth:UhW5uCKw:pfbmdJ3b"
val extendedAePlus = "maven.modrinth:xr109llC:Bm8LwH2q"
val jei = "mezz.jei:jei-$minecraftVersion-neoforge:19.76.1.482"
val emi = "dev.emi:emi-neoforge:1.1.22+$minecraftVersion"
val rei = "me.shedaniel:RoughlyEnoughItems-neoforge:16.0.799"

dependencies {
    compileOnly(ae2)
    compileOnly(guideme)
    compileOnly(glodium)
    compileOnly(extendedAe)
    compileOnly(extendedAePlus)
    compileOnly(jei)
    compileOnly(emi)
    compileOnly(rei)

    runtimeOnly(ae2)
    runtimeOnly(guideme)
    runtimeOnly(glodium)
    runtimeOnly(extendedAe)
    runtimeOnly(extendedAePlus)

    //runtimeOnly(jei)
    //runtimeOnly(emi)
    runtimeOnly(rei)
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
        "ae2_version" to ae2Version
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
    }

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1606104"
        projectSlug = sprojectSlug
        minecraftVersions.add(minecraftVersion)
        server = true
        client = true
        changelog = fullChangelog
    }
}

fun prop(name: String): String = providers.gradleProperty(name).get()
