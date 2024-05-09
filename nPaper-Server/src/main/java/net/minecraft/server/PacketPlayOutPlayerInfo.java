package net.minecraft.server;

import java.io.IOException;

import org.bukkit.craftbukkit.util.CraftChatMessage;

// Spigot start - protocol patch
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.com.mojang.authlib.properties.PropertyMap;

public class PacketPlayOutPlayerInfo extends Packet {

    private PlayerInfo action;
    // private int length; We don't batch (yet)
    private GameProfile player;

    private int gamemode;
    private int ping;
    private String username;

    /* removed to force breaking
    public PacketPlayOutPlayerInfo(String s, boolean flag, int i) {
        this.a = s;
        this.b = flag;
        this.c = i;
    }
    */

    public PacketPlayOutPlayerInfo() {}

    public PacketPlayOutPlayerInfo(EntityPlayer player, PlayerInfo action) {
        this.action = action;
        this.username = player.listName;
        this.player = player.getProfile();
        switch (action) {
            case ADD_PLAYER:
                this.ping = player.ping;
                this.gamemode = player.playerInteractManager.getGameMode().getId();
                break;
            case UPDATE_LATENCY:
                this.ping = player.ping;
                break;
            case UPDATE_GAMEMODE:
                this.gamemode = player.playerInteractManager.getGameMode().getId();
                break;
            default:break;
        }
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        // Not needed
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        // PaperSpigot start - Fix scoreboard prefix and suffix in tab list
        String username = this.username;
        if (packetdataserializer.version >= 47 && action == PlayerInfo.ADD_PLAYER && username != null && username.equals(player.getName())) {
            username = null;
        }
        // PaperSpigot end
        if ( packetdataserializer.version >= 20 )
        {
            packetdataserializer.b(action.getValue());
            packetdataserializer.b( 1 );
            packetdataserializer.writeUUID( player.getId() );
            switch ( action )
            {
                case ADD_PLAYER:
                    packetdataserializer.a( player.getName() );
                    PropertyMap properties = player.getProperties();
                    packetdataserializer.b( properties.size() );
                    for ( Property property : properties.values() )
                    {
                        packetdataserializer.a( property.getName() );
                        packetdataserializer.a( property.getValue() );
                        packetdataserializer.writeBoolean( property.hasSignature() );
                        if ( property.hasSignature() )
                        {
                            packetdataserializer.a( property.getSignature() );
                        }
                    }
                    packetdataserializer.b( gamemode );
                    packetdataserializer.b( ping );
                    packetdataserializer.writeBoolean( username != null );
                    if ( username != null )
                    {
                        packetdataserializer.a( ChatSerializer.a( CraftChatMessage.fromString( username )[0] ) );
                    }
                    break;
                case UPDATE_GAMEMODE:
                    packetdataserializer.b( gamemode );
                    break;
                case UPDATE_LATENCY:
                    packetdataserializer.b( ping );
                    break;
                case UPDATE_DISPLAY_NAME:
                    packetdataserializer.writeBoolean( username != null );
                    if ( username != null )
                    {
                        packetdataserializer.a( ChatSerializer.a( CraftChatMessage.fromString( username )[0] ) );
                    }
                    break;
                case REMOVE_PLAYER:
                    break;

            }
        } else {
            packetdataserializer.a( username );
            packetdataserializer.writeBoolean( action != PlayerInfo.REMOVE_PLAYER );
            packetdataserializer.writeShort( ping );
        }
    }

    public void a(PacketPlayOutListener packetplayoutlistener) {
        packetplayoutlistener.a(this);
    }

    public void handle(PacketListener packetlistener) {
        this.a((PacketPlayOutListener) packetlistener);
    }

    public enum PlayerInfo {
        ADD_PLAYER(0),
        UPDATE_GAMEMODE(1),
        UPDATE_LATENCY(2),
        UPDATE_DISPLAY_NAME(3),
        REMOVE_PLAYER(4);

        private int value;

        PlayerInfo(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
// Spigot end