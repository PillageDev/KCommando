![](http://image-write-app.herokuapp.com/?x=880&y=33&size=130&text=koply&url=https%3A%2F%2Fimage-write-app.herokuapp.com%2F%3Fx%3D45%26y%3D25%26size%3D150%26text%3DKCommando%26url%3Dhttps%3A%2F%2Fwww.afcapital.ru%2Fa%2Fpgs%2Fimages%2Fcontent-grid-bg.png)

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/koply/KCommando.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/koply/KCommando/context:java)
[![Build Status](https://travis-ci.com/koply/kcommando.svg?branch=master)](https://travis-ci.com/koply/kcommando)
[![jitpack-version](https://jitpack.io/v/koply/KCommando.svg)](https://jitpack.io/#koply/KCommando)
![LICENSE](https://img.shields.io/github/license/koply/KCommando?style=flat)

Annotation-based multifunctional command handler framework for JDA & Javacord. KCommando has a external plugin system.

## Features
All these features have a modular structure and you can edit all of these modules and integrate them for your projects.
1. [Integrations](#kcommando-integrations)
2. [JDA Section](#integration-usage-for-jda)
3. [Javacord Section](#javacord-section)
4. [Command Features](#command-features)
	- [Argument Methods](#argument-methods)
	- [Possible Handle Methods](#possible-handle-methods)
	- [Command Callbacks](#command-callbacks) *(onFalse, ownerOnly, guildOnly, privateOnly, cooldown)*
5. [Cool Features](#cool-features)
	- [Suggested Commands](#how-to-use-suggested-commands)
	- [Custom Prefixes](#how-to-use-custom-prefixes)
	- [Blacklist User](#blacklist-user)
	- [Blacklist Member](#blacklist-member)
	- [Blacklist Channel](#blacklist-channel)
	- [Data Preservence (Blacklist-Prefix)](#data-preservence)
	- [Callback For Blacklisted Usage](#callback-for-blacklisted-usages)
6. [Plugin System](#plugin-system)
	- [How To Use Plugins](#how-to-use-plugins)
	- [How To Create A Plugin](#how-to-create-plugins)
7. [CRON Service](#cron-service)
8. [Install](#how-to-install)
	- [Maven](#with-maven)
	- [Gradle](#with-gradle)
9. [Example Repositories](#example-repositories)
	
# KCommando Integrations

### Integration Usage For JDA
```java
public class Main extends JDAIntegration {
    
    public Main(JDA jda) { super(jda); }

    public void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault("TOKEN").build();
        jda.awaitReady();
        
        File dataFile = new File("./data.json");
        // data file asset control
        if (!dataFile.exists())
            dataFile.createNewFile();

        KCommando<MessageReceivedEvent> kcommando = new KCommando<>(new Main(jda))
              .setCooldown(5000L) // 5 seconds as 5000 ms
              .setOwners("FIRST_OWNER_ID", "SECOND_OWNER_ID") // varargs
              .setPackage("com.example.mybot.commands") // command classes package path
              .setPrefix("!")
              .setReadBotMessages(false) // default false
              .useCaseSensitivity() // case sensitivity in all commands
              .setDataFile(dataFile) // data file for blacklist and prefix preservence
              .setPluginsPath(new File("./plugins/")) // plugins folder for external plugins
              .build();
    }
}
```

That's it. Now, we need a command.

### How To Create A Command For JDA
```java
@Commando(name = "Ping!",
          aliases = "ping",
          description = "Pong!", /* "-"   default */
          guildOnly = false,     /* false default */
          ownerOnly = false,     /* false default */
          privateOnly = false,   /* false default */
          sync = false,          /* false default */
          onlyArguments = false  /* false default */)
public class BasicCommand extends JDACommand {
    
    public BasicCommand() {
        // when handle method returns false, runs the declared callback like this
        getInfo().setOnFalseCallback( (JRunnable) e -> e.getMessage().addReaction("⛔").queue() );
    }

    @Override
    public boolean handle(MessageReceivedEvent e /* optionally String[] args*/ ) {
        e.getChannel().sendMessage( "Pong!" ).queue();
        return true;
        // if your command is completed successfully, you must return "true"
    }
    
    @Argument(arg = "test")
    public boolean test(MessageReceivedEvent e) {
        e.getChannel.sendMessage("Test!").queue();
        return true;
    }
}
```

_Optionally you can use the class and handle method as *final* to reduce compile time._

Aliases field can be an array: `aliases = {"ping", "pingu"}`

## Javacord Section

### Integration Usage For Javacord
```java
public class Main extends JavacordIntegration {
    
    public Main(DiscordApi discordApi) { super(discordApi); }

    public void main(String[] args) {
        DiscordApi discordApi = new DiscordApiBuilder().setToken(token)
            .login().join();
        
        KCommando<MessageCreateEvent> kcommando = new KCommando<>(new Main(discordApi))
              .setPackage("com.example.mybot.commands") // command classes package path
              .setPrefix("!")
              .build();
    }
}
```

### How To Create A Command For Javacord
```java
@Commando(name = "Ping!",
          aliases = {"ping", "pong"}) /* aliases can be an array as stated above */
public class BasicCommand extends JavacordCommand {
    
    public BasicCommand() {
        // if handle method returns false, this callback will be called
        getInfo().setOnFalseCallback( (JRunnable) e -> e.getMessage().addReaction("⛔") );
    }

    @Override
    public boolean handle(MessageCreateEvent e /* optionally String[] args*/ ) {
        e.getChannel().sendMessage( "Pong!" );
        return true;
        // if your command has been completed successfully, you must return "true"
    }
    
    @Argument(arg = "test")
    public boolean test(MessageCreateEvent e) {
       e.getChannel().sendMessage( "Test!" );
       return true;
    }
}
```

# Command Features

## Possible Handle Methods

You can use just one of these in your command class. Parameters will never be null. You don't need null checks.

```java
boolean handle(<Event> e) // CommandType.EVENT -> 0x01
boolean handle(<Event> e, String[] args)  // CommandType.ARGNEVENT -> 0x02
boolean handle(<Event> e, String[] args, String prefix)  // CommandType.PREFIXED -> 0x03
```

### Properties of the *args* and *prefix* parameters
Args are splitted by the "space" characters. The 0. index is the command text itself (without the prefix).
```
Entered Command: "!ping test 123"
args[0]: "ping"
args[1]: "test"
args[2]: "123"

prefix: "!"
```

## Argument Methods
Argument methods must be public and boolean. If the argument method returns false, the `onFalse` callback will be run. Method names are not important. 

If the Commando annotation of the command has `onlyArguments = true` option, the command is only available for pure usage and use with arguments.
There is no limit to using arguments, you can use as many arguments as you want. Arguments __has__ case sensitivity.

The parameters you can use are:
```java
boolean name(<Event> e) // CommandType.EVENT -> 0x01
boolean name(<Event> e, String[] args)  // CommandType.ARGNEVENT -> 0x02
boolean name(<Event> e, String[] args, String prefix)  // CommandType.PREFIXED -> 0x03
```

#### Example:

```java
@Argument(arg = "jump")
public boolean nameIsNotImportant(<Event> e){
    // somethings
    if (fail) return false;
    return true;
}

@Argument(arg = {"think", "thonk"})
public boolean anotherArgumentMethod(<Event> e){
    // somethings
    if (fail) return false;
    return true;
}
```

## Command Callbacks
**Note:** These lines should be inside the constructor of your command.

#### On False Callback: 
```java
// This callback is called when the command handle method returns false.
getInfo().setOnFalseCallback( (JRunnable) e -> e.getMessage().addReaction("⛔") );
```

#### Owner Only Callback: 
```java
// This callback is called when the command for the bot owner is used by a normal user.
getInfo().setOwnerOnlyCallback( (JRunnable) e ->  e.getMessage().addReaction("⛔") );
```

#### Guild Only Callback: 
```java
// This callback is called when the command for guilds is used in the private message.
getInfo().setGuildOnlyCallback( (JRunnable) e ->  e.getMessage().addReaction("⛔") );
```

#### Private Only Callback:
```java
// This callback is called when the command for private conversations is used in the guild.
getInfo().setPrivateOnlyCallback( (JRunnable) e ->  e.getMessage().addReaction("⛔") );
```

#### Cooldown Callback:
```java
// This callback is called when the command is rejected due to cooldown.
getInfo().setCooldownCallback( (JRunnable) e ->  e.getMessage().addReaction("⛔") );
```

# Cool Features

## How To Use Suggested Commands

This callback will be called with the suggestions list and the event object when an incorrect command is used.
Currently, the suggestions are based on the JaroWrinklerDistance algorithm.
You must change the `**event**` part according to the API you use.

```java
Integration#setSuggestionsCallback((SuggestionsCallback<**Event**>) (e,suggestions) -> {
    if (suggestions.isEmpty()) {
        // no suggestions found
        return;
    }
    
    StringBuilder sb = new StringBuilder();
    for (CommandInfo<**Event**> info : suggestions) {
        sb.append( Arrays.toString(info.getAliases()) ).append(" - ");
    }
    e.getChannel().sendMessage("Last command could not be recognized. Suggestions: \n"+sb.toString()).queue();
});
```

## How To Use Custom Prefixes

You can add custom prefixes for guilds.

```java
// adds a prefix for the selected guild.
Integration#addCustomPrefix(long guildID, String prefix) 

// removes a prefix for the selected guild. This method is safe to use.
Integration#removeCustomPrefix(long guildID, String prefix)

// removes all custom prefixes for selected guild. 
Integration#removeAllCustomPrefixes(long guildID) 
```

If a guild has a custom prefix, the normal prefix will be overridden for that guild but it is possible to use more than one prefix at the same time. You can remove and disable custom prefixes for the single guild.


## How To Use Blacklist

We prefer using a singleton of a subclass of Integration. You can look at the tests of jda and javacord integrations for reference.

### Blacklist User
```java
// blocks the specified user from using all commands in the bot.
Integration#getBlacklistedUsers().add(long userID) 

// unblocks the specified user.
Integration#getBlacklistedUsers().remove(long userID) 
```

### Blacklist Member
```java
// returns all blacklisted members with guilds. (a map as `guildID -> the set of the blacklisted members`)
Integration#getBlacklistedMembers() 

// returns all blacklisted members in the specified guild.
Integration#getBlacklistedMembers(long guildID) 
```

### Blacklist Channel
```java
// returns all blacklisted channels with guilds. (a map as `guildID -> the set of the blacklisted channels`)
Integration#getBlacklistedChannels()

// returns all blacklisted channels in the specified guild.
Integration#getBlacklistedChannels(long guildID)
```

### Data Preservence
Your blacklist and prefix data are automatically saved by KCommando to the file you predetermined. KCommando doesn't have an autosave system ***yet***. If you want to use your own database system, you can write your own [DataManager](https://github.com/MusaBrt/KCommando/blob/master/core/src/main/java/me/koply/kcommando/DataManager.java) class and give your own DataManager class to KCommando with `KCommando#setDataManager(DataManager)`. If you use your own DataManager instance, you don't need to use `KCommando#setDataFile(File)`. Because this method sets the data manager to a new data manager that uses the given file as its database.

Pure data preservence usage isn't recommended on advanced bots. You should create your own DataManager class. That's the reason for the modular design.

Data File Structure:
```json
{
  "guildDatas": [
    {
      "id": 00000000L,
      "blacklistedMembers": [0000000L, 0000000L],
      "blacklistedChannels": [0000000L, 00000000L],
      "customPrefixes": ["!", "."]
    }
  ],
  "blacklistedUsers": [0000000L, 0000000L]
}
```

All these blacklist and prefix callbacks are concurrent for thread safety.

### Callback For Blacklisted Usages

When a command is rejected due to a blacklist, this callback is called.
```java
Integration#setBlacklistCallback( (JRunnable) e -> e.getMessage().addReaction("⛔") )
```

## Plugin System

### How To Use Plugins

KCommando has a plugin system to make it easier for everyone to use and make bots.

If the plugin folder isn't exists, KCommando will create it. Plugin folder selection:
```java
KCommando#setPluginsFolder(File folder)
```

### How To Create A Plugin
You can visit the references below for examples.

[Sample JDA Plugin](https://github.com/MusaBrt/KCommando/tree/master/sample-plugin-jda) - [Sample Javacord Plugin](https://github.com/MusaBrt/KCommando/tree/master/sample-plugin-javacord)

[Sample JDA Plugin's pom.xml](https://github.com/MusaBrt/KCommando/blob/master/sample-plugin-jda/pom.xml) - [Sample Javacord Plugin's pom.xml](https://github.com/MusaBrt/KCommando/blob/master/sample-plugin-javacord/pom.xml)

Plugins must have a `plugin.yml` file. This file contains some critical information.

An example `plugin.yml` file:
```yml
author: Koply
main: me.koply.plugin.Main
version: 1.0
name: AmazingPlugin
description: I'm Thor's Mjöllnir.
```

An example Main class for JDA:
```java
public class Main extends JDAPlugin {

    public Main() {
        getLogger().info("Hello from a plugin's constructor.");
    }

    @Override
    public void onEnable() {
        getLogger().info("Hello from a plugin's onEnable.");

        addCommand(PluginCommand.class); // not instance
        addListener(new MyListener());
    }

    @Override
    public void onDisable() { 
        // ignored for temporarily
    }
}
```

An example command class for JDA:
```java
@Commando(name = "Plugin Command",
          aliases = "plugincommand")
public class PluginCommand extends JDACommand {

    @Override
    public boolean handle(MessageReceivedEvent e, String[] args, String prefix) {
        e.getChannel().sendMessage("Hello from plugin!").queue();
        return true;
    }

    @Argument(arg = "test")
    public boolean test(MessageReceivedEvent e) {
        e.getChannel().sendMessage("Test!").queue();
        return true;
    }
}
```

An example listener class for JDA:
```java
public class MyListener extends ListenerAdapter {
    // somethings
}
```

That's it.

## Cron Service
KCommando has a minute-based async CronService and you can use it.
```java
CronService.getInstance().addRunnable(() -> {
	// do stuff
}, 5); /* every 5 minutes */
```

## How To Install

To always use the latest version, you can write '-SNAPSHOT' in the version field. This use is not recommended because new versions may not always be fully backwards compatible.

### With Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>


<!-- FOR JDA -->
<dependency>
    <groupId>com.github.koply.KCommando</groupId>
    <artifactId>jda-integration</artifactId>
    <version>JITPACK-VERSION</version>
</dependency>

<!-- FOR JAVACORD -->
<dependency>
    <groupId>com.github.koply.KCommando</groupId>
    <artifactId>javacord-integration</artifactId>
    <version>JITPACK-VERSION</version>
</dependency>
```
### With Gradle:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

// FOR JDA
dependencies {
    implementation 'com.github.koply.KCommando:jda-integration:JITPACK-VERSION'
}

// FOR JAVACORD
dependencies {
    implementation 'com.github.koply.KCommando:javacord-integration:JITPACK-VERSION'
}
```

**Please change 'JITPACK-VERSION' fields to the latest release version.**

Github packages are ignored. Please use jitpack repositories.

## Example Repositories
 | [Rae Discord Bot](https://github.com/koply/Rae)

Tests are includes help and prefix usage. [JDA Test Area](https://github.com/koply/KCommando/tree/master/jda-integration/src/main/test/me/koply/jdatest) - [Javacord Test Area](https://github.com/koply/KCommando/tree/master/javacord-integration/src/main/test/me/koply/javacordtest)

[Sample JDA Plugin](https://github.com/koply/KCommando/tree/master/sample-plugin-jda) - [Sample Javacord Plugin](https://github.com/koply/KCommando/tree/master/sample-plugin-javacord)

# Don't be afraid to contribute!
