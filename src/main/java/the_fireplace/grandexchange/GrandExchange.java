package the_fireplace.grandexchange;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import the_fireplace.grandexchange.commands.CommandGe;
import the_fireplace.grandexchange.db.IDatabaseHandler;
import the_fireplace.grandexchange.db.JsonDatabase;
import the_fireplace.grandexchange.util.TransactionDatabase;

@SuppressWarnings("WeakerAccess")
@Mod(modid = GrandExchange.MODID, name = GrandExchange.MODNAME, version = GrandExchange.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", acceptableRemoteVersions = "*", dependencies="required-after:grandeconomy@[1.3.1,)")
public final class GrandExchange {
    public static final String MODID = "grandexchange";
    public static final String MODNAME = "Grand Exchange";
    public static final String VERSION = "${version}";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    private static IDatabaseHandler db = null;

    public static IDatabaseHandler getDatabase() {
        if(db == null)//TODO Check config for database type once implemented
            db = new JsonDatabase();
        return db;
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandGe());
        //TODO Remove this old code when porting to 1.14+
        TransactionDatabase.getInstance();
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        getDatabase().onServerStop();
    }

    @Config(modid=MODID, name=MODNAME)
    public static class cfg {
        @Config.Comment("Server locale - the client's locale takes precedence if Grand Exchange is installed there.")
        public static String locale = "en_us";
    }
}
