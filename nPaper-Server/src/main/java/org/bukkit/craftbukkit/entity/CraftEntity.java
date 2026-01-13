package org.bukkit.craftbukkit.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import com.sathonay.npaper.utils.ICraftEntityWrapper;
import net.minecraft.server.*;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public abstract class CraftEntity implements org.bukkit.entity.Entity {
    protected final CraftServer server;
    protected Entity entity;
    private EntityDamageEvent lastDamageEvent;
    private float verticalKnockbackReduction = 0.0f;
    private float horizontalKnockbackReduction = 0.0f;

    public CraftEntity(final CraftServer server, final Entity entity) {
        this.server = server;
        this.entity = entity;
    }

    @Override
    public void extinguish() {
        entity.extinguish();
    }

    private static final Map<Class<?>, BiFunction<CraftServer, Entity, CraftEntity>> entityMappings = new HashMap<>();
    static {
    	// Players
        //entityMappings.put(EntityPlayer.class, (srv, ent) -> new CraftPlayer(srv, (EntityPlayer) ent));
        entityMappings.put(EntityHuman.class, (srv, ent) -> new CraftHumanEntity(srv, (EntityHuman) ent));
        
        // Animals
        entityMappings.put(EntityChicken.class, (srv, ent) -> new CraftChicken(srv, (EntityChicken) ent));
        entityMappings.put(EntityMushroomCow.class, (srv, ent) -> new CraftMushroomCow(srv, (EntityMushroomCow) ent));
        entityMappings.put(EntityCow.class, (srv, ent) -> new CraftCow(srv, (EntityCow) ent));
        entityMappings.put(EntityPig.class, (srv, ent) -> new CraftPig(srv, (EntityPig) ent));
        entityMappings.put(EntityWolf.class, (srv, ent) -> new CraftWolf(srv, (EntityWolf) ent));
        entityMappings.put(EntityOcelot.class, (srv, ent) -> new CraftOcelot(srv, (EntityOcelot) ent));
        entityMappings.put(EntitySheep.class, (srv, ent) -> new CraftSheep(srv, (EntitySheep) ent));
        entityMappings.put(EntityHorse.class, (srv, ent) -> new CraftHorse(srv, (EntityHorse) ent));
        entityMappings.put(EntityAnimal.class, (srv, ent) -> new CraftAnimals(srv, (EntityAnimal) ent));

        // Monsters
        entityMappings.put(EntityPigZombie.class, (srv, ent) -> new CraftPigZombie(srv, (EntityPigZombie) ent));
        entityMappings.put(EntityZombie.class, (srv, ent) -> new CraftZombie(srv, (EntityZombie) ent));
        entityMappings.put(EntityCreeper.class, (srv, ent) -> new CraftCreeper(srv, (EntityCreeper) ent));
        entityMappings.put(EntityEnderman.class, (srv, ent) -> new CraftEnderman(srv, (EntityEnderman) ent));
        entityMappings.put(EntitySilverfish.class, (srv, ent) -> new CraftSilverfish(srv, (EntitySilverfish) ent));
        entityMappings.put(EntityGiantZombie.class, (srv, ent) -> new CraftGiant(srv, (EntityGiantZombie) ent));
        entityMappings.put(EntitySkeleton.class, (srv, ent) -> new CraftSkeleton(srv, (EntitySkeleton) ent));
        entityMappings.put(EntityBlaze.class, (srv, ent) -> new CraftBlaze(srv, (EntityBlaze) ent));
        entityMappings.put(EntityWitch.class, (srv, ent) -> new CraftWitch(srv, (EntityWitch) ent));
        entityMappings.put(EntityWither.class, (srv, ent) -> new CraftWither(srv, (EntityWither) ent));
        entityMappings.put(EntityCaveSpider.class, (srv, ent) -> new CraftCaveSpider(srv, (EntityCaveSpider) ent));
        entityMappings.put(EntitySpider.class, (srv, ent) -> new CraftSpider(srv, (EntitySpider) ent));
        entityMappings.put(EntityMonster.class, (srv, ent) -> new CraftMonster(srv, (EntityMonster) ent));

        // Water Animals
        entityMappings.put(EntitySquid.class, (srv, ent) -> new CraftSquid(srv, (EntitySquid) ent));
        entityMappings.put(EntityWaterAnimal.class, (srv, ent) -> new CraftWaterMob(srv, (EntityWaterAnimal) ent));

        // Golems
        entityMappings.put(EntitySnowman.class, (srv, ent) -> new CraftSnowman(srv, (EntitySnowman) ent));
        entityMappings.put(EntityIronGolem.class, (srv, ent) -> new CraftIronGolem(srv, (EntityIronGolem) ent));

        // Villagers
        entityMappings.put(EntityVillager.class, (srv, ent) -> new CraftVillager(srv, (EntityVillager) ent));

        // Slimes
        entityMappings.put(EntityMagmaCube.class, (srv, ent) -> new CraftMagmaCube(srv, (EntityMagmaCube) ent));
        entityMappings.put(EntitySlime.class, (srv, ent) -> new CraftSlime(srv, (EntitySlime) ent));

        // Flying entities
        entityMappings.put(EntityGhast.class, (srv, ent) -> new CraftGhast(srv, (EntityGhast) ent));
        entityMappings.put(EntityFlying.class, (srv, ent) -> new CraftFlying(srv, (EntityFlying) ent));

        // Ender Dragon
        entityMappings.put(EntityEnderDragon.class, (srv, ent) -> new CraftEnderDragon(srv, (EntityEnderDragon) ent));

        // Ambient
        entityMappings.put(EntityBat.class, (srv, ent) -> new CraftBat(srv, (EntityBat) ent));
        entityMappings.put(EntityAmbient.class, (srv, ent) -> new CraftAmbient(srv, (EntityAmbient) ent));

        // Living entities
        entityMappings.put(EntityLiving.class, (srv, ent) -> new CraftLivingEntity(srv, (EntityLiving) ent));

        // Complex Parts
        entityMappings.put(EntityComplexPart.class, (srv, ent) -> {
            EntityComplexPart part = (EntityComplexPart) ent;
            if (part.owner instanceof EntityEnderDragon) {
                return new CraftEnderDragonPart(srv, part);
            }
            return new CraftComplexPart(srv, part);
        });

        // Experience Orbs
        entityMappings.put(EntityExperienceOrb.class, (srv, ent) -> new CraftExperienceOrb(srv, (EntityExperienceOrb) ent));

        // Arrows
        entityMappings.put(EntityArrow.class, (srv, ent) -> new CraftArrow(srv, (EntityArrow) ent));

        // Boats
        entityMappings.put(EntityBoat.class, (srv, ent) -> new CraftBoat(srv, (EntityBoat) ent));

        // Projectiles
        entityMappings.put(EntityEgg.class, (srv, ent) -> new CraftEgg(srv, (EntityEgg) ent));
        entityMappings.put(EntitySnowball.class, (srv, ent) -> new CraftSnowball(srv, (EntitySnowball) ent));
        entityMappings.put(EntityPotion.class, (srv, ent) -> new CraftThrownPotion(srv, (EntityPotion) ent));
        entityMappings.put(EntityEnderPearl.class, (srv, ent) -> new CraftEnderPearl(srv, (EntityEnderPearl) ent));
        entityMappings.put(EntityThrownExpBottle.class, (srv, ent) -> new CraftThrownExpBottle(srv, (EntityThrownExpBottle) ent));

        // Falling Blocks
        entityMappings.put(EntityFallingBlock.class, (srv, ent) -> new CraftFallingSand(srv, (EntityFallingBlock) ent));

        // Fireballs
        entityMappings.put(EntitySmallFireball.class, (srv, ent) -> new CraftSmallFireball(srv, (EntitySmallFireball) ent));
        entityMappings.put(EntityLargeFireball.class, (srv, ent) -> new CraftLargeFireball(srv, (EntityLargeFireball) ent));
        entityMappings.put(EntityWitherSkull.class, (srv, ent) -> new CraftWitherSkull(srv, (EntityWitherSkull) ent));
        entityMappings.put(EntityFireball.class, (srv, ent) -> new CraftFireball(srv, (EntityFireball) ent));

        // Ender Signal
        entityMappings.put(EntityEnderSignal.class, (srv, ent) -> new CraftEnderSignal(srv, (EntityEnderSignal) ent));

        // Ender Crystal
        entityMappings.put(EntityEnderCrystal.class, (srv, ent) -> new CraftEnderCrystal(srv, (EntityEnderCrystal) ent));

        // Fishing Hook
        entityMappings.put(EntityFishingHook.class, (srv, ent) -> new CraftFish(srv, (EntityFishingHook) ent));

        // Items
        entityMappings.put(EntityItem.class, (srv, ent) -> new CraftItem(srv, (EntityItem) ent));

        // Weather
        entityMappings.put(EntityLightning.class, (srv, ent) -> new CraftLightningStrike(srv, (EntityLightning) ent));
        entityMappings.put(EntityWeather.class, (srv, ent) -> new CraftWeather(srv, (EntityWeather) ent));

        // Minecarts
        entityMappings.put(EntityMinecartFurnace.class, (srv, ent) -> new CraftMinecartFurnace(srv, (EntityMinecartFurnace) ent));
        entityMappings.put(EntityMinecartChest.class, (srv, ent) -> new CraftMinecartChest(srv, (EntityMinecartChest) ent));
        entityMappings.put(EntityMinecartTNT.class, (srv, ent) -> new CraftMinecartTNT(srv, (EntityMinecartTNT) ent));
        entityMappings.put(EntityMinecartHopper.class, (srv, ent) -> new CraftMinecartHopper(srv, (EntityMinecartHopper) ent));
        entityMappings.put(EntityMinecartMobSpawner.class, (srv, ent) -> new CraftMinecartMobSpawner(srv, (EntityMinecartMobSpawner) ent));
        entityMappings.put(EntityMinecartRideable.class, (srv, ent) -> new CraftMinecartRideable(srv, (EntityMinecartRideable) ent));
        entityMappings.put(EntityMinecartCommandBlock.class, (srv, ent) -> new CraftMinecartCommand(srv, (EntityMinecartCommandBlock) ent));

        // Hanging entities
        entityMappings.put(EntityPainting.class, (srv, ent) -> new CraftPainting(srv, (EntityPainting) ent));
        entityMappings.put(EntityItemFrame.class, (srv, ent) -> new CraftItemFrame(srv, (EntityItemFrame) ent));
        entityMappings.put(EntityLeash.class, (srv, ent) -> new CraftLeash(srv, (EntityLeash) ent));
        entityMappings.put(EntityHanging.class, (srv, ent) -> new CraftHanging(srv, (EntityHanging) ent));

        // TNT
        entityMappings.put(EntityTNTPrimed.class, (srv, ent) -> new CraftTNTPrimed(srv, (EntityTNTPrimed) ent));

        // Fireworks
        entityMappings.put(EntityFireworks.class, (srv, ent) -> new CraftFirework(srv, (EntityFireworks) ent));
    }

    public static CraftEntity getEntity(CraftServer server, Entity entity) {

        if (entity instanceof ICraftEntityWrapper wrapper)
            return wrapper.toCraftEntity(server);

        final Class<?> entityClass = entity.getClass();
        final BiFunction<CraftServer, Entity, CraftEntity> mapper = entityMappings.get(entityClass);
        if (mapper != null) {
            return mapper.apply(server, entity);
        }

        throw new AssertionError("Unknown entity " + entity == null ? null : entityClass);
    }

    public Location getLocation() {
        return new Location(getWorld(), entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(entity.locX);
            loc.setY(entity.locY);
            loc.setZ(entity.locZ);
            loc.setYaw(entity.yaw);
            loc.setPitch(entity.pitch);
        }

        return loc;
    }

    public Vector getVelocity() {
        return new Vector(entity.motX, entity.motY, entity.motZ);
    }

    public void setVelocity(Vector vel) {
        entity.motX = vel.getX();
        entity.motY = vel.getY();
        entity.motZ = vel.getZ();
        entity.velocityChanged = true;
    }

    public boolean isOnGround() {
        if (entity instanceof EntityArrow) {
            return ((EntityArrow) entity).isInGround();
        }
        return entity.onGround;
    }

    public World getWorld() {
        return entity.world.getWorld();
    }

    public boolean teleport(Location location) {
        return teleport(location, TeleportCause.PLUGIN);
    }

    public boolean teleport(Location location, TeleportCause cause) {
        if (entity.passenger != null || entity.dead) {
            return false;
        }

        // If this entity is riding another entity, we must dismount before teleporting.
        entity.mount(null);

        // Spigot start
        if (!location.getWorld().equals(getWorld())) {
          entity.teleportTo(location, cause.equals(TeleportCause.NETHER_PORTAL));
          return true;
        }

        // entity.world = ((CraftWorld) location.getWorld()).getHandle();
        // Spigot end
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // entity.setLocation() throws no event, and so cannot be cancelled
        return true;
    }

    public boolean teleport(org.bukkit.entity.Entity destination) {
        return teleport(destination.getLocation());
    }

    public boolean teleport(org.bukkit.entity.Entity destination, TeleportCause cause) {
        return teleport(destination.getLocation(), cause);
    }

    public List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        @SuppressWarnings("unchecked")
        List<Entity> notchEntityList = entity.world.getEntities(entity, entity.boundingBox.grow(x, y, z));
        List<org.bukkit.entity.Entity> bukkitEntityList = new java.util.ArrayList<org.bukkit.entity.Entity>(notchEntityList.size());

        for (Entity e : notchEntityList) {
            bukkitEntityList.add(e.getBukkitEntity());
        }
        return bukkitEntityList;
    }

    public int getEntityId() {
        return entity.getId();
    }

    public int getFireTicks() {
        return entity.fireTicks;
    }

    public int getMaxFireTicks() {
        return entity.maxFireTicks;
    }

    public void setFireTicks(int ticks) {
        entity.fireTicks = ticks;
    }

    public void remove() {
        entity.dead = true;
    }

    public boolean isDead() {
        return !entity.isAlive();
    }

    public boolean isValid() {
        return entity.isAlive() && entity.valid;
    }

    public Server getServer() {
        return server;
    }
    
    public void setVerticalKnockbackReduction(float reduction) {
		this.verticalKnockbackReduction = reduction;
	}
      
	public float getVerticalKnockbackReduction() {
		return this.verticalKnockbackReduction;
	}
	
	public void setHorizontalKnockbackReduction(float reduction) {
		this.horizontalKnockbackReduction = reduction;
	}
      
	public float getHorizontalKnockbackReduction() {
		return this.horizontalKnockbackReduction;
	}

    public Vector getMomentum() {
        return getVelocity();
    }

    public void setMomentum(Vector value) {
        setVelocity(value);
    }

    public org.bukkit.entity.Entity getPassenger() {
        return isEmpty() ? null : getHandle().passenger.getBukkitEntity();
    }

    public boolean setPassenger(org.bukkit.entity.Entity passenger) {
        if (passenger instanceof CraftEntity) {
            ((CraftEntity) passenger).getHandle().setPassengerOf(getHandle());
            return true;
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return getHandle().passenger == null;
    }

    public boolean eject() {
        if (getHandle().passenger == null) {
            return false;
        }

        getHandle().passenger.setPassengerOf(null);
        return true;
    }

    public float getFallDistance() {
        return getHandle().fallDistance;
    }

    public void setFallDistance(float distance) {
        getHandle().fallDistance = distance;
    }

    public void setLastDamageCause(EntityDamageEvent event) {
        lastDamageEvent = event;
    }

    public EntityDamageEvent getLastDamageCause() {
        return lastDamageEvent;
    }

    public UUID getUniqueId() {
        return getHandle().uniqueID;
    }

    public int getTicksLived() {
        return getHandle().ticksLived;
    }

    public void setTicksLived(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Age must be at least 1 tick");
        }
        getHandle().ticksLived = value;
    }

    public Entity getHandle() {
        return entity;
    }

    public void playEffect(EntityEffect type) {
        this.getHandle().world.broadcastEntityEffect(getHandle(), type.getData());
    }

    public void setHandle(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "CraftEntity{" + "id=" + getEntityId() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftEntity other = (CraftEntity) obj;
        return (this.getEntityId() == other.getEntityId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.getEntityId();
        return hash;
    }

    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    public boolean hasMetadata(String metadataKey) {
        return server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    public boolean isInsideVehicle() {
        return getHandle().vehicle != null;
    }

    public boolean leaveVehicle() {
        if (getHandle().vehicle == null) {
            return false;
        }

        getHandle().setPassengerOf(null);
        return true;
    }

    public org.bukkit.entity.Entity getVehicle() {
        if (getHandle().vehicle == null) {
            return null;
        }

        return getHandle().vehicle.getBukkitEntity();
    }

    @Override
    public boolean isInWater() {
        return getHandle().inWater;
    }

    @Override
    public boolean isInLava() {
        return getHandle().isInLava;
    }

    @Override
    public boolean isInFluid() {
        return isInWater() || isInLava();
    }

    // Spigot start
    private final Spigot spigot = new Spigot()
    {
        @Override
        public boolean isInvulnerable()
        {
            return getHandle().isInvulnerable();
        }
    };

    public Spigot spigot()
    {
        return spigot;
    }
    // Spigot end
}
