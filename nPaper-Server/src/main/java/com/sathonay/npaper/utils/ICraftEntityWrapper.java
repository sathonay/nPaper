package com.sathonay.npaper.utils;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;

public interface ICraftEntityWrapper {
    CraftEntity toCraftEntity(CraftServer server);
}
