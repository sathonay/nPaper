package net.minecraft.server;

import java.util.Iterator;

public class WorldManager implements IWorldAccess {

    private MinecraftServer server;
    public WorldServer world; // CraftBukkit - private -> public

    public WorldManager(MinecraftServer minecraftserver, WorldServer worldserver) {
        this.server = minecraftserver;
        this.world = worldserver;
    }

    public void a(String s, double d0, double d1, double d2, double d3, double d4, double d5) {}

    public void a(Entity entity) {
        this.world.getTracker().track(entity);
    }

    public void b(Entity entity) {
        this.world.getTracker().untrackEntity(entity);
    }

    public void a(String s, double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit - this.world.dimension
        this.server.getPlayerList().sendPacketNearby(d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, this.world.dimension, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1));
    }

    public void a(EntityHuman entityhuman, String s, double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit - this.world.dimension
        boolean includingSelf = (s.equals("random.drink") || s.contains("step") || s.contains("player") || s.equals("random.eat"));
        this.server.getPlayerList().sendPacketNearby(entityhuman, d0, d1, d2, f > 1.0F ? (double) (16.0F * f) : 16.0D, this.world.dimension, new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1), !includingSelf);
    }

    public void a(int i, int j, int k, int l, int i1, int j1) {}

    public void a(int i, int j, int k) {
        this.world.getPlayerChunkMap().flagDirty(i, j, k);
    }

    public void b(int i, int j, int k) {}

    public void a(String s, int i, int j, int k) {}

    public void a(EntityHuman entityhuman, int i, int j, int k, int l, int i1) {
        // CraftBukkit - this.world.dimension
        boolean includingSelf = (i != 2001);
        this.server.getPlayerList().sendPacketNearby(entityhuman, (double) j, (double) k, (double) l, 64.0D, this.world.dimension, new PacketPlayOutWorldEvent(i, j, k, l, i1, false), includingSelf);
    }

    public void a(int i, int j, int k, int l, int i1) {
        this.server.getPlayerList().sendAll(new PacketPlayOutWorldEvent(i, j, k, l, i1, true));
    }


    public void b(final int i, final int j, final int k, final int l, final int i1) {
        final Iterator iterator = this.server.getPlayerList().players.iterator();
        EntityHuman entityhuman = null;
        final Entity entity = this.world.getEntity(i);
        if (entity instanceof EntityHuman) {
            entityhuman = (EntityHuman)entity;
        }

        PacketPlayOutBlockBreakAnimation packet = null;

        while (iterator.hasNext()) {
            final EntityPlayer entityplayer = (EntityPlayer) iterator.next();
            if (entityplayer != null && entityplayer.world == this.world && entityplayer.getId() != i) {
                final double d0 = j - entityplayer.locX;
                final double d2 = k - entityplayer.locY;
                final double d3 = l - entityplayer.locZ;
                if ((entityhuman != null && entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(((EntityPlayer)entityhuman).getBukkitEntity())) || d0 * d0 + d2 * d2 + d3 * d3 >= 1024.0) {
                    continue;
                }
                if (packet == null) packet = new PacketPlayOutBlockBreakAnimation(i, j, k, l, i1);
                entityplayer.playerConnection.sendPacket(packet);
            }
        }
    }

    public void b() {}
}
