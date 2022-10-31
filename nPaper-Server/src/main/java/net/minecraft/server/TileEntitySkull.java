package net.minecraft.server;

import java.util.UUID;

import net.minecraft.util.com.google.common.collect.Iterables;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;

// Spigot start
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.util.com.mojang.authlib.Agent;
// Spigot end

public class TileEntitySkull extends TileEntity {

    private int a;
    private int i;
    private GameProfile j = null;
    // Spigot start
    public static final Executor executor = Executors.newFixedThreadPool(3,
            new ThreadFactoryBuilder()
                    .setNameFormat("Head Conversion Thread - %1$d")
                    .build()
    );
    public static final Cache<String, GameProfile> skinCache = CacheBuilder.newBuilder()
            .maximumSize( 5000 )
            .expireAfterAccess( 60, TimeUnit.MINUTES )
            .build( new CacheLoader<String, GameProfile>()
            {
                @Override
                public GameProfile load(String key) throws Exception
                {
                    GameProfile[] profiles = new GameProfile[1];
                    GameProfileLookup gameProfileLookup = new GameProfileLookup(profiles);

                    MinecraftServer.getServer().getGameProfileRepository().findProfilesByNames(new String[] { key }, Agent.MINECRAFT, gameProfileLookup);

                    GameProfile profile = profiles[ 0 ];
                    if (profile == null) {
                        UUID uuid = EntityHuman.a(new GameProfile(null, key));
                        profile = new GameProfile(uuid, key);

                        gameProfileLookup.onProfileLookupSucceeded(profile);
                    } else
                    {

                        Property property = Iterables.getFirst( profile.getProperties().get( "textures" ), null );

                        if ( property == null )
                        {
                            profile = MinecraftServer.getServer().av().fillProfileProperties( profile, true );
                        }
                    }


                    return profile;
                }
            } );
    // Spigot end

    public TileEntitySkull() {}

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setByte("SkullType", (byte) (this.a & 255));
        nbttagcompound.setByte("Rot", (byte) (this.i & 255));
        if (this.j != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            GameProfileSerializer.serialize(nbttagcompound1, this.j);
            nbttagcompound.set("Owner", nbttagcompound1);
            nbttagcompound.setString("ExtraType", nbttagcompound1.getString("Name")); // Spigot
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.a = nbttagcompound.getByte("SkullType");
        this.i = nbttagcompound.getByte("Rot");
        if (this.a == 3) {
            if (nbttagcompound.hasKeyOfType("Owner", 10)) {
                this.j = GameProfileSerializer.deserialize(nbttagcompound.getCompound("Owner"));
            } else if (nbttagcompound.hasKeyOfType("ExtraType", 8) && !UtilColor.b(nbttagcompound.getString("ExtraType"))) {
                this.j = new GameProfile((UUID) null, nbttagcompound.getString("ExtraType"));
                this.d();
            }
        }
    }

    public GameProfile getGameProfile() {
        return this.j;
    }

    public Packet getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.b(nbttagcompound);
        return new PacketPlayOutTileEntityData(this.x, this.y, this.z, 4, nbttagcompound);
    }

    public void setSkullType(int i) {
        this.a = i;
        this.j = null;
    }

    public void setGameProfile(GameProfile gameprofile) {
        this.a = 3;
        this.j = gameprofile;
        this.d();
    }

    private void d() {
        if (this.j != null && !UtilColor.b(this.j.getName())) {
            if (!this.j.isComplete() || !this.j.getProperties().containsKey("textures")) {
                // Spigot start - Handle async
                final String name = this.j.getName();
                setSkullType( 0 ); // Work around a client bug
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        GameProfile profile = skinCache.getIfPresent( name.toLowerCase() );

                        if (profile != null) {
                            final GameProfile finalProfile = profile;
                            MinecraftServer.getServer().processQueue.add(new Runnable() {
                                @Override
                                public void run() {
                                    a = 3;
                                    j = finalProfile;
                                    world.notify( x, y, z );
                                }
                            });
                        } else {
                            MinecraftServer.getServer().processQueue.add(new Runnable() {
                                @Override
                                public void run() {
                                    a = 3;
                                    j = new GameProfile( null, name );
                                    world.notify( x, y, z );
                                }
                            });
                        }
                    }
                });
                // Spigot end
            }
        }
    }

    public int getSkullType() {
        return this.a;
    }

    public void setRotation(int i) {
        this.i = i;
    }

    // CraftBukkit start - add method
    public int getRotation() {
        return this.i;
    }
    // CraftBukkit end
}
