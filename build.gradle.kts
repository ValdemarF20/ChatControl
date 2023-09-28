import groovy.lang.MissingPropertyException
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // https://github.com/Minecrell/plugin-yml
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "net.foster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "Aikar (ACF)"
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") // ACF (Command framework)

    bukkitLibrary("org.xerial:sqlite-jdbc:3.43.0.0") // SQLite (Load bukkit-only plugin when the plugin is loaded in server (avoid shading it))
}

bukkit {
    main = "net.foster.chatcontrol.ChatControl"
    author = "Valdemar"


    /*
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP // or POSTWORLD
    authors = listOf("Notch", "Notch2")
    contributors = listOf("Notch3", "Notch4")
    depend = listOf("WorldEdit")
    softDepend = listOf("Essentials")
    loadBefore = listOf("BrokenPlugin")
    prefix = "TEST"
    defaultPermission = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
    provides = listOf("TestPluginOldName", "TestPlug")

    permissions {
        register("testplugin.*") {
            children = listOf("testplugin.test") // Defaults permissions to true
            // You can also specify the values of the permissions
            childrenMap = mapOf("testplugin.test" to true)
        }
        register("testplugin.test") {
            description = "Allows you to run the test command"
            default = BukkitPluginDescription.Permission.Default.OP // TRUE, FALSE, OP or NOT_OP
        }
    }
     */
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
            options.release.set(targetJavaVersion)
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        relocate("co.airkar.commands", "net.advancius.acf")
        relocate("co.airkar.locales", "net.advancius.locales")

        try {
            if (project.findProperty("destination") != null) {
                destinationDirectory.set(file(project.findProperty("destination").toString()))
            }
        } catch (_: MissingPropertyException) {
        }
    }

    runServer {
        minecraftVersion.set("1.18.2")
        jvmArgs =
            listOf("-DIReallyKnowWhatIAmDoingISwear --nogui ")

    }
}