package org.github.paperspigot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PaperSpigotConfig
{

    private static final File CONFIG_FILE = new File( "paper.yml" );
    private static final String HEADER = "This is the main configuration file for PaperSpigot.\n"
            + "As you can see, there's tons to configure. Some options may impact gameplay, so use\n"
            + "with caution, and make sure you know what each option does before configuring.\n"
            + "\n"
            + "If you need help with the configuration or have any questions related to PaperSpigot,\n"
            + "join us at the IRC.\n"
            + "\n"
            + "IRC: #paperspigot @ irc.spi.gt ( http://irc.spi.gt/iris/?channels=PaperSpigot )\n";
    /*========================================================================*/
    static YamlConfiguration config;
    static int version;
    static Map<String, Command> commands;
    /*========================================================================*/

    static { init(); }

    public static void init()
    {
        config = new YamlConfiguration();
        try
        {
            config.load ( CONFIG_FILE );
        } catch ( IOException ex )
        {
        } catch ( InvalidConfigurationException ex )
        {
            Bukkit.getLogger().log( Level.SEVERE, "Could not load paper.yml, please correct your syntax errors", ex );
            throw Throwables.propagate( ex );
        }
        config.options().header( HEADER );
        config.options().copyDefaults( true );

        commands = new HashMap<String, Command>();

        version = getInt( "config-version", 6 );
        set( "config-version", 6 );
        readConfig( PaperSpigotConfig.class, null );
    }

    public static void registerCommands()
    {
        for ( Map.Entry<String, Command> entry : commands.entrySet() )
        {
            MinecraftServer.getServer().server.getCommandMap().register( entry.getKey(), "PaperSpigot", entry.getValue() );
        }
    }

    static void readConfig(Class<?> clazz, Object instance)
    {
        for ( Method method : clazz.getDeclaredMethods() )
        {
            if ( Modifier.isPrivate( method.getModifiers() ) )
            {
                if ( method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE )
                {
                    try
                    {
                        method.setAccessible( true );
                        method.invoke( instance );
                    } catch ( InvocationTargetException ex )
                    {
                        throw Throwables.propagate( ex.getCause() );
                    } catch ( Exception ex )
                    {
                        Bukkit.getLogger().log( Level.SEVERE, "Error invoking " + method, ex );
                    }
                }
            }
        }

        try
        {
            config.save( CONFIG_FILE );
        } catch ( IOException ex )
        {
            Bukkit.getLogger().log( Level.SEVERE, "Could not save " + CONFIG_FILE, ex );
        }
    }

    private static void set(String path, Object val)
    {
        config.set( path, val );
    }

    private static boolean getBoolean(String path, boolean def)
    {
        config.addDefault( path, def );
        return config.getBoolean( path, config.getBoolean( path ) );
    }

    private static double getDouble(String path, double def)
    {
        config.addDefault( path, def );
        return config.getDouble( path, config.getDouble( path ) );
    }

    private static float getFloat(String path, float def)
    {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble(path, (double) def);
    }

    private static int getInt(String path, int def)
    {
        config.addDefault( path, def );
        return config.getInt( path, config.getInt( path ) );
    }

    private static <T> List getList(String path, T def)
    {
        config.addDefault( path, def );
        return (List<T>) config.getList( path, config.getList( path ) );
    }

    private static String getString(String path, String def)
    {
        config.addDefault( path, def );
        return config.getString( path, config.getString( path ) );
    }

    public static double babyZombieMovementSpeed;
    private static void babyZombieMovementSpeed()
    {
        babyZombieMovementSpeed = getDouble( "settings.baby-zombie-movement-speed", 0.5D); // Player moves at 0.1F, for reference
    }

    public static boolean asyncCatcherFeature;
    private static void asyncCatcherFeature()
    {
        asyncCatcherFeature = getBoolean( "settings.async-plugin-bad-magic-catcher", true );
        if (!asyncCatcherFeature) {
            Bukkit.getLogger().log( Level.INFO, "Disabling async plugin bad ju-ju catcher, this may be bad depending on your plugins" );
        }
    }

    public static boolean interactLimitEnabled;
    private static void interactLimitEnabled()
    {
        interactLimitEnabled = getBoolean( "settings.limit-player-interactions", true );
        if (!interactLimitEnabled) {
            Bukkit.getLogger().log( Level.INFO, "Disabling player interaction limiter, your server may be more vulnerable to malicious users" );
        }
    }

    public static double strengthEffectModifier;
    public static double weaknessEffectModifier;
    private static void effectModifiers()
    {
        strengthEffectModifier = getDouble( "effect-modifiers.strength", 1.3D );
        weaknessEffectModifier = getDouble( "effect-modifiers.weakness", -0.5D );
    }

    public static int maxPacketsPerPlayer;
    private static void maxPacketsPerPlayer()
    {
        maxPacketsPerPlayer = getInt( "max-packets-per-player", 1000 );
    }

    public static boolean stackableLavaBuckets;
    public static boolean stackableWaterBuckets;
    public static boolean stackableMilkBuckets;
    private static void stackableBuckets()
    {
        stackableLavaBuckets = getBoolean( "stackable-buckets.lava", false );
        stackableWaterBuckets = getBoolean( "stackable-buckets.water", false );
        stackableMilkBuckets = getBoolean( "stackable-buckets.milk", false );

        Field maxStack;

        try {
            maxStack = Material.class.getDeclaredField("maxStack");
            maxStack.setAccessible(true);

            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(maxStack, maxStack.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            if (stackableLavaBuckets) {
                maxStack.set(Material.LAVA_BUCKET, Material.BUCKET.getMaxStackSize());
            }

            if (stackableWaterBuckets) {
                maxStack.set(Material.WATER_BUCKET, Material.BUCKET.getMaxStackSize());
            }

            if (stackableMilkBuckets) {
                maxStack.set(Material.MILK_BUCKET, Material.BUCKET.getMaxStackSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean fixEnderPearlGlitch;
    private static void fixEnderPearlGlitch() {
        fixEnderPearlGlitch = getBoolean("npaper.ender-pearl.fix-glitch", false);
    }

    public static boolean enderPearlCollision;
    private static void enderPearlCollision() {
        enderPearlCollision = getBoolean("npaper.ender-pearl.hcf-collisions", false);
    }

    public static boolean enderPearlCreativeThrow;
    private static void enderPearlCreativeThrow() {
        enderPearlCreativeThrow = getBoolean("npaper.ender-pearl.creative-throw", false);
    }

    public static boolean rodCatchPlayersAndItems;
    private static void rodCatchPlayersAndItems() {
        rodCatchPlayersAndItems = getBoolean("npaper.rod-catch-players-and-items", false);
    }


    public static float potionGravityVelocity;
    private static void potionGravityVelocity() {
        potionGravityVelocity = getFloat("npaper.potion.gravity-velocity", 0.05F);
    }

    public static float potionVelocity;
    private static void potionVelocity() {
        potionVelocity = getFloat("npaper.potion.velocity", 0.5F);
    }

    public static float potionInaccuracy;
    private static void potionInaccuracy() {
        potionInaccuracy = getFloat("npaper.potion.inaccuracy", -20.0F);
    }

    // Print user ip in the console when he's joining the server
    public static boolean logPlayerIp;
    private static void logPlayerIp() {
        logPlayerIp = getBoolean("npaper.log-player-ip", true);
    }
    
    public static boolean obfuscatePlayerHealth = false;
    private static void obfuscatePlayerHealth() {
    	obfuscatePlayerHealth = getBoolean("npaper.settings.obfuscate-player-health", obfuscatePlayerHealth);
    }

    public static boolean savePlayerData = true;
    private static void savePlayerData() {
        savePlayerData = getBoolean("npaper.settings.save-player-data", savePlayerData);
    }

    public static boolean hitSprintDesync = true;
    private static void hitSprintDesync() {
        hitSprintDesync = getBoolean("npaper.settings.hitSprintDesync", hitSprintDesync);
    }
}
