package the_fireplace.grandexchange.commands;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.grandeconomy.api.GrandEconomyApi;
import the_fireplace.grandexchange.market.SellOffer;
import the_fireplace.grandexchange.util.TransactionDatabase;
import the_fireplace.grandexchange.util.translation.TranslationUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSellThis extends CommandBase {
    @Override
    public String getName() {
        return "sellthis";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TranslationUtil.getRawTranslationString(sender, "commands.ge.sellthis.usage");
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 2) {
            if(sender instanceof EntityPlayerMP) {
                boolean isValidRequest = !((EntityPlayerMP) sender).getHeldItemMainhand().isEmpty() || !((EntityPlayerMP) sender).getHeldItemOffhand().isEmpty();
                if(!isValidRequest)
                    throw new CommandException(TranslationUtil.getRawTranslationString(((EntityPlayer) sender).getUniqueID(), "commands.ge.common.not_holding_anything"));
                ItemStack selling = ((EntityPlayerMP) sender).getHeldItemMainhand().isEmpty() ? ((EntityPlayerMP) sender).getHeldItemOffhand() : ((EntityPlayerMP) sender).getHeldItemMainhand();
                int amount = parseInt(args[0]);
                if(amount <= 0)
                    throw new CommandException(TranslationUtil.getRawTranslationString(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.common.invalid_amount"));
                long price = parseLong(args[1]);
                if (price < 0)
                    throw new CommandException(TranslationUtil.getRawTranslationString(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.common.invalid_price"));
                int itemCount = 0;
                for(ItemStack stack: ((EntityPlayerMP) sender).inventory.mainInventory) {
                    //noinspection ConstantConditions
                    if(!stack.isEmpty() && stack.getItem().getRegistryName().equals(selling.getItem().getRegistryName()) && stack.getMetadata() == selling.getMetadata() && ((!stack.hasTagCompound() && !selling.hasTagCompound()) || stack.getTagCompound().toString().equals(selling.getTagCompound().toString())) && TransactionDatabase.canTransactItem(stack)){
                        if(stack.getCount() + itemCount >= amount)
                            itemCount = amount;
                        else
                            itemCount += stack.getCount();
                    }
                }
                if(itemCount < amount)
                    throw new CommandException(TranslationUtil.getRawTranslationString(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.sell.not_enough_items"));
                int i = 0;
                for(ItemStack stack: ((EntityPlayerMP) sender).inventory.mainInventory) {
                    //noinspection ConstantConditions
                    while(!stack.isEmpty() && stack.getItem().getRegistryName().equals(selling.getItem().getRegistryName()) && stack.getMetadata() == selling.getMetadata() && ((!stack.hasTagCompound() && !selling.hasTagCompound()) || stack.getTagCompound().toString().equals(selling.getTagCompound().toString())) && itemCount > 0 && TransactionDatabase.canTransactItem(stack)){
                        itemCount--;
                        if(stack.getCount() > 1)
                            stack.setCount(stack.getCount() - 1);
                        else {
                            ((EntityPlayerMP) sender).inventory.mainInventory.set(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                    i++;
                }
                if(itemCount > 0)
                    throw new CommandException(TranslationUtil.getRawTranslationString(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.sell.failed"));

                boolean madePurchase = TransactionDatabase.getInstance().makeOffer(new SellOffer(selling.getItem().getRegistryName().toString(), selling.getMetadata(), amount, price, ((EntityPlayerMP) sender).getUniqueID(), selling.hasTagCompound() ? Objects.requireNonNull(selling.getTagCompound()).toString() : null));

                if(madePurchase)
                    sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.common.offer_fulfilled_balance", GrandEconomyApi.getBalance(((EntityPlayerMP) sender).getUniqueID())));
                else
                    sender.sendMessage(TranslationUtil.getTranslation(((EntityPlayerMP) sender).getUniqueID(), "commands.ge.common.offer_made"));
                return;
            } else
                throw new CommandException(TranslationUtil.getRawTranslationString(sender, "commands.ge.common.not_player"));
        }
        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        //TODO Tab completions
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
