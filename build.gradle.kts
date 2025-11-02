import net.minecrell.pluginyml.paper.PaperPluginDescription
import org.jooq.meta.jaxb.Logging

plugins {
    `java-library`

    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.plugin.yml.bukkit) // Automatic plugin.yml generation
    alias(libs.plugins.plugin.yml.paper) // Automatic plugin.yml generation
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    flywaypatches
    projectextensions
    versioner

    eclipse
    idea
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt())) // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    withJavadocJar() // Enable javadoc jar generation
    withSourcesJar() // Enable sources jar generation
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn-repo.arim.space/lesser-gpl3/")

    maven("https://maven.athyrium.eu/releases")

    maven("https://repo.glaremasters.me/repository/towny/") {
        content { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://jitpack.io/") {
        content {
            includeGroup("com.github.MilkBowl") // VaultAPI
            includeGroup("com.palmergames.bukkit.towny")
            includeGroup("com.github.gecolay")
            includeGroup("com.github.gecolay.GSit")
            includeGroup("com.github.NEZNAMY")
            includeGroup("com.github.Tofaa2.EntityLib")
            includeGroup("com.github.milkdrinkers")
            includeGroup("ca.tweetzy")
            includeGroup("com.github.Thatsmusic99")
        }
    }

    maven("https://repo.codemc.org/repository/maven-public/") {
        content {
            includeGroup("dev.jorel")
            includeGroup("com.github.retrooper")
        }
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }

    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.essentialsx.net/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://maven.evokegames.gg/snapshots/") {
        content { includeGroup("me.tofaa.entitylib") }
    }
    maven("https://repo.cwhead.dev/repository/maven-public/") {
        content { includeGroup("com.ranull") }
    }
    maven("https://repo.triumphteam.dev/snapshots/") {
        content { includeGroup("dev.triumphteam") }
    }
}

val cleanAlathraPorts by tasks.registering(Jar::class) {
    from(zipTree("lib/AlathraPorts-1.0.5.jar")) {
        exclude("io/github/milkdrinkers/**")
    }
    archiveFileName.set("AlathraPorts-1.0.5-cleaned.jar")
    destinationDirectory.set(layout.buildDirectory.dir("cleaned-libs"))
}

val cleanActiveUpkeep by tasks.registering(Jar::class) {
    from(zipTree("lib/ActiveUpkeep-1.0.0-SNAPSHOT-1754074990.jar")) {
        exclude("io/github/milkdrinkers/**")
    }
    archiveFileName.set("ActiveUpkeep-1.0.0-SNAPSHOT-1754074990.jar")
    destinationDirectory.set(layout.buildDirectory.dir("cleaned-libs"))
}

dependencies {
    // Core
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)
    compileOnly(libs.paper.api)
    implementation(libs.morepaperlib)

    // API
    implementation(libs.javasemver)
    implementation(libs.versionwatch)
    implementation(libs.wordweaver)
    implementation(libs.crate.yaml)
    implementation(libs.colorparser)
    implementation(libs.threadutil.bukkit)
    implementation(libs.commandapi.shade)
    implementation(libs.triumph.gui)
    implementation(libs.itemutils)

    // Plugin Dependencies
    implementation(libs.bstats)
    compileOnly(libs.vault)
    compileOnly(libs.packetevents)
    implementation(libs.entitylib) {
        exclude("org.jetbrains")
    }
    compileOnly(libs.placeholderapi) {
        exclude("me.clip.placeholderapi.libs", "kyori")
    }
    compileOnly(libs.towny) {
        exclude("com.palmergames.adventure")
    }
    compileOnly(libs.tabapi)
//    compileOnly(files("lib/AlathraPorts-1.0.5.jar"))
//    compileOnly(files("lib/ActiveUpkeep-1.0.0-SNAPSHOT-1754074990.jar"))
    compileOnly(files(cleanAlathraPorts))
    compileOnly(files(cleanActiveUpkeep))
//    compileOnly(libs.gravesx)
    compileOnly(files("lib/Graves-4.9.jar"))
//    compileOnly(libs.gsit)
    compileOnly(files("lib/GSit-2.4.3.jar"))
    compileOnly(files("lib/HeadDrop.jar"))
//    compileOnly(libs.skulls)
    compileOnly(libs.headsplus)
    compileOnly(libs.essentialsx.core) {
        exclude("org.spigotmc")
    }
    compileOnly(libs.essentialsx.spawn) {
        exclude("org.spigotmc")
    }

    // Database dependencies - Core
    implementation(libs.hikaricp)
    library(libs.bundles.flyway)
    compileOnly(libs.jakarta) // Compiler bug, see: https://github.com/jOOQ/jOOQ/issues/14865#issuecomment-2077182512
    library(libs.jooq)
    jooqCodegen(libs.h2)

    // Database dependencies - JDBC drivers
    library(libs.bundles.jdbcdrivers)

    // Testing - Core
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.paper.api)

    // Testing - Database dependencies
    testImplementation(libs.hikaricp)
    testImplementation(libs.bundles.flyway)
    testImplementation(libs.jooq)

    // Testing - JDBC drivers
    testImplementation(libs.bundles.jdbcdrivers)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(libs.versions.java.get().toInt())
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))
    }

    javadoc {
        isFailOnError = false
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
        options.overview = "src/main/javadoc/overview.html"
        options.windowTitle = "${rootProject.name} Javadoc"
        options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
        options.addStringOption("Xdoclint:none", "-quiet")
        options.use()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.relocationPackage}.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("io.github.milkdrinkers", "milkdrinkers")
        reloc("org.snakeyaml", "snakeyaml")
        reloc("org.json", "json")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("com.zaxxer.hikari", "hikaricp")
        reloc("org.bstats", "bstats")
        reloc("me.tofaa.entitylib", "entitylib")
        reloc("dev.triumphteam.gui", "triumphgui")

        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
        failFast = false
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion(libs.versions.paper.run.get())

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-DIReallyKnowWhatIAmDoingISwear", "-Dpaper.playerconnection.keepalive=6000")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        // Automatically install dependencies
        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
            modrinth("tab-was-taken", "4.1.8")
            modrinth("PlaceholderAPI", "2.11.6")
            github("retrooper", "packetevents", "v2.9.5", "packetevents-spigot-2.9.5.jar")
//            url("https://www.spigotmc.org/resources/skulls-the-ultimate-head-database.90098/download?version=520217/Skulls.jar")
        }
    }
}

bukkit { // Options: https://github.com/Minecrell/plugin-yml#bukkit
    // Plugin main class (required)
    main = project.entryPointClass

    // Plugin Information
    name = project.name
    prefix = project.name
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("darksaid98", "ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf()
    apiVersion = libs.versions.paper.api.get().substringBefore("-R")

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "Towny")
    softDepend = listOf("PacketEvents", "PlaceholderAPI", "AlathraPorts", "GravesX", "Graves", "TAB", "UnlimitedNametags", "Skulls", "HeadsPlus", "HeadDatabase", "HeadDrop", "Essentials")
}

paper { // Options: https://github.com/eldoriarpg/plugin-yml/wiki/Paper
    main = project.entryPointClass
    loader = project.entryPointClass + "PluginLoader"
    generateLibrariesJson = true
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    // Info
    name = project.name
    prefix = project.name
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("darksaid98", "ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf()
    apiVersion = libs.versions.paper.api.get().substringBefore("-R")
    foliaSupported = false

    // Dependencies
    hasOpenClassloader = true
    bootstrapDependencies {}
    serverDependencies {
        // Hard depends
        register("Vault") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("Towny") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }


        // Soft depends
        register("PacketEvents") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("AlathraPorts") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("GravesX") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Graves") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("TAB") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("UnlimitedNametags") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Skulls") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadsPlus") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadDatabase") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadDrop") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Essentials") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
    provides = listOf()
}

flyway {
    url = "jdbc:h2:${project.layout.buildDirectory.get()}/generated/flyway/db;AUTO_SERVER=TRUE;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE"
    user = "sa"
    password = ""
    schemas = listOf("PUBLIC").toTypedArray()
    placeholders = mapOf( // Substitute placeholders for flyway
        "tablePrefix" to "",
    )
    validateMigrationNaming = true
    baselineOnMigrate = true
    cleanDisabled = false
    locations = arrayOf(
        "filesystem:${project.tasks.named<AssimilateMigrationsTask>("assimilateMigrations").get().outputDir.get().dir("h2")}",
        "classpath:${mainPackage.replace(".", "/")}/database/migration/migrations"
    )
}

jooq {
    configuration {
        logging = Logging.ERROR
        jdbc {
            driver = "org.h2.Driver"
            url = flyway.url
            user = flyway.user
            password = flyway.password
        }
        generator {
            database {
                name = "org.jooq.meta.h2.H2Database"
                includes = ".*"
                excludes = "(flyway_schema_history)|(?i:information_schema\\..*)|(?i:system_lobs\\..*)"  // Exclude database specific files
                inputSchema = "PUBLIC"
                schemaVersionProvider = "SELECT :schema_name || '_' || MAX(\"version\") FROM \"flyway_schema_history\"" // Grab version from Flyway
            }
            target {
                packageName = "${mainPackage}.database.schema"
                withClean(true)
            }
        }
    }
}