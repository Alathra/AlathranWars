plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1" // Shades and relocates dependencies, See https://imperceptiblethoughts.com/shadow/introduction/
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Automatic plugin.yml generation
}

group = "me.ShermansWorld.AlathraWar"
version = "2.1.0"
description = ""

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
}

repositories {
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/")

    maven("https://repo.glaremasters.me/repository/towny/") {
        content { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://jitpack.io/") {
        content {
            includeGroup("com.github.milkdrinkers")
            includeGroup("com.github.MilkBowl")
            includeGroup("com.palmergames.bukkit.towny")
        }
    }

    maven("https://repo.kryptonmc.org/releases/") {
        content { includeGroup("me.neznamy") }
    }

    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("dev.jorel") }
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")

    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    implementation("com.github.milkdrinkers:colorparser:1.0.5")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    compileOnly("com.palmergames.bukkit.towny:towny:0.98.6.0")

    compileOnly("me.neznamy:tab-api:4.0.0")

    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")
    compileOnly("dev.jorel:commandapi-annotations:9.0.3")
    annotationProcessor("dev.jorel:commandapi-annotations:9.0.3")

    compileOnly("me.clip:placeholderapi:2.11.3")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
        options.compilerArgs.add("-Xlint:-deprecation")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        // helper function to relocate a package into our package
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.group}.${targetPkg}")

        reloc("dev.jorel.commandapi", "commandapi")
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion("1.19.3")
    }
}

bukkit {
    // Plugin main class (required)
    main = "${project.group}.Main"

    // Plugin Information
    name = project.name
    prefix = "AlathraWar"
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf("darksaid98")
    apiVersion = "1.19"

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "Towny")
    softDepend = listOf("TAB")

    commands {
        register("war") {
            description = "Used to create, delete, join and leave wars"
            usage = "Just use /war!"
        }
        register("siege") {
            description = "Used to start, end, and view sieges"
            usage = "Just use /siege!"
        }
        register("raid") {
            description = "Used to start, end, and view raids"
            usage = "Just use /raid!"
        }
        register("alathrawaradmin") {
            description = "Used to administrate wars and modify existing events"
            usage = "Just use /alathrawaradmin!"
            aliases = listOf("awa")
        }
    }
}