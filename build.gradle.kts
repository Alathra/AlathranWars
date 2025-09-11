import org.jooq.meta.jaxb.Logging
import java.time.Instant

plugins {
    `java-library`

    alias(libs.plugins.shadow) // Shades and relocates dependencies, See https://imperceptiblethoughts.com/shadow/introduction/
    alias(libs.plugins.run.paper) // Adds runServer and runMojangMappedServer tasks for testing
    alias(libs.plugins.plugin.yml.bukkit) // Automatic plugin.yml generation
    alias(libs.plugins.flyway) // Database migrations
    alias(libs.plugins.jooq)

    eclipse
    idea
}

val mainPackage = "${project.group}.${project.name.lowercase()}"
applyCustomVersion()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21)) // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    withJavadocJar() // Enable javadoc jar generation
    withSourcesJar() // Enable sources jar generation
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://mvn-repo.arim.space/lesser-gpl3/")

    maven("https://repo.glaremasters.me/repository/towny/") {
        content { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://jitpack.io/") {
        content {
            includeGroup("com.github.MilkBowl") // VaultAPI
            includeGroup("com.palmergames.bukkit.towny")
            includeGroup("com.github.Gecolay.GSit")
            includeGroup("com.github.neznamy")
        }
    }

    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("dev.jorel") }
        content { includeGroup("com.github.retrooper") }
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        content { includeGroup("me.clip") }
    }

    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://maven.evokegames.gg/snapshots/") // EntityLib
}

dependencies {
    compileOnly(libs.annotations)
    annotationProcessor(libs.annotations)
    compileOnly(libs.paper.api)
    implementation(libs.morepaperlib)

    // API
    implementation(libs.wordweaver) {
        exclude("com.google.code.gson") // Already ships with Paper
    }
    implementation(libs.crate.api)
    implementation(libs.crate.yaml)
    implementation(libs.colorparser) {
        exclude("net.kyori")
    }
    implementation(libs.command.api.paper)
    implementation(libs.entitylib)

    // Plugin Dependencies
    implementation(libs.bstats)
    compileOnly(libs.vault)
//    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0-SNAPSHOT")
    compileOnly(libs.placeholderapi) {
        exclude("me.clip.placeholderapi.libs", "kyori")
    }
    compileOnly(libs.towny) {
        exclude("com.palmergames.adventure")
    }
    compileOnly(libs.tab.api)
    compileOnly(libs.gsit)
    compileOnly(libs.packetevents)

    compileOnly(files("lib/Graves-4.9.jar"))
    compileOnly(files("lib/Skulls.jar"))
    compileOnly(files("lib/HeadsPlus-7.0.14.jar"))

    // Database Dependencies (Core)
    implementation(libs.hikaricp)
    library(libs.bundles.flyway)
    compileOnly(libs.jakarta) // Compiler bug, see: https://github.com/jOOQ/jOOQ/issues/14865#issuecomment-2077182512
    library(libs.jooq)
    jooqCodegen(libs.h2)

    // Database Dependencies (JDBC Drivers)
    library(libs.bundles.jdbcdrivers)

    // Testing (Core)
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)

    // Testing (Database Dependencies)
    testImplementation(libs.hikaricp)
    testImplementation(libs.bundles.flyway)
    testImplementation(libs.jooq)

    // Testing (JDBC Drivers)
    testImplementation(libs.bundles.jdbcdrivers)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jooqCodegen {
        dependsOn(flywayMigrate)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
        options.compilerArgs.addAll(arrayListOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))

        dependsOn(jooqCodegen) // Generate jOOQ sources before compilation
    }

    javadoc {
        isFailOnError = false
        exclude("**/database/schema/**") // Exclude generated jOOQ sources from javadocs
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
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${mainPackage}.lib.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("com.github.milkdrinkers.crate", "crate")
        reloc("com.github.milkdrinkers.colorparser", "colorparser")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("com.zaxxer.hikari", "hikaricp")
        reloc("org.bstats", "bstats")
        reloc("me.tofaa.entitylib", "entitylib")

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
            github("PlaceholderAPI", "PlaceholderAPI", "2.11.6", "PlaceholderAPI-2.11.6.jar")
            url("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/build/libs/ProtocolLib.jar")
            github("retrooper", "packetevents", "v2.5.0", "packetevents-spigot-2.5.0.jar")
//            url("https://www.spigotmc.org/resources/skulls-the-ultimate-head-database.90098/download?version=520217/Skulls.jar")
        }
    }
}

tasks.named<Jar>("sourcesJar") { // Required for sources jar generation with jOOQ
    dependsOn(tasks.jooqCodegen)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

bukkit { // Options: https://github.com/Minecrell/plugin-yml#bukkit
    // Plugin main class (required)
    main = "${mainPackage}.${project.name}"

    // Plugin Information
    name = project.name
    prefix = project.name
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("darksaid98", "ShermansWorld", "NinjaMandalorian", "AubriTheHuman")
    contributors = listOf()
    apiVersion = "1.20"

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "ProtocolLib", "Towny")
    softDepend = listOf("PacketEvents", "PlaceholderAPI", "TAB", "Skulls", "HeadsPlus", "Essentials")
}

flyway {
    url = "jdbc:h2:${project.layout.buildDirectory.get()}/generated/flyway/db;AUTO_SERVER=TRUE;MODE=MYSQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE"
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
        "filesystem:src/main/resources/db/migration",
        "classpath:db/migration"
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

fun applyCustomVersion() {
    // Apply custom version arg or append snapshot version
    val ver = properties["altVer"]?.toString() ?: "${rootProject.version}-SNAPSHOT-${Instant.now().epochSecond}"

    // Strip prefixed "v" from version tag
    rootProject.version = (if (ver.first().equals('v', true)) ver.substring(1) else ver.uppercase()).uppercase()
}