package the_fireplace.grandexchange.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.tuple.Pair;
import the_fireplace.grandeconomy.api.GrandEconomyApi;
import the_fireplace.grandexchange.TransactionDatabase;
import the_fireplace.grandexchange.market.BuyOffer;
import the_fireplace.grandexchange.market.SellOffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CommandBuyOffers extends CommandBase {
    private static final String blue = "§3";
    @Override
    @Nonnull
    public String getName() {
        return "buyoffers";
    }

    @Override
    @Nonnull
    public String getUsage(@Nullable ICommandSender sender) {
        return "/buyoffers [page]";
    }

    @Override
    public void execute(@Nullable MinecraftServer server, @Nonnull ICommandSender sender, @Nullable String[] args) throws CommandException {
        List<BuyOffer> offers = Lists.newArrayList();
        for(List<BuyOffer> offerList : TransactionDatabase.getBuyOffers().values())
            offers.addAll(offerList);
        int page = 1;
        if(args != null && args.length == 1)
        try {
            page = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            throw new CommandException("Invalid page number!");
        }
        //Expand page to be the first entry on the page
        page *= 50;
        //Subtract 50 because the first page starts with entry 0
        page -= 50;
        int termLength = 50;
        for(BuyOffer offer : offers) {
            if(page-- > 0)
                continue;
            if(termLength-- <= 0)
                break;
            sender.sendMessage(new TextComponentString(blue + offer.getAmount() + ' ' + offer.getItemResourceName() + ' ' + offer.getItemMeta() + " wanted for " + offer.getPrice() + ' ' + GrandEconomyApi.getCurrencyName(offer.getAmount()) + " each"));
        }
        //noinspection RedundantArrayCreation
        throw new WrongUsageException("/buyoffers [page]", new Object[0]);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}