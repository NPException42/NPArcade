package de.npe.mcmods.nparcade.common.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.npe.mcmods.nparcade.arcade.ArcadeGameRegistry;
import de.npe.mcmods.nparcade.arcade.ArcadeGameWrapper;
import de.npe.mcmods.nparcade.arcade.DummyGames;
import de.npe.mcmods.nparcade.arcade.api.IGameCartridge;
import de.npe.mcmods.nparcade.common.lib.Strings;
import me.jezza.oc.common.interfaces.IItemTooltip;
import me.jezza.oc.common.items.ItemAbstract;
import me.jezza.oc.common.utils.Localise;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by NPException (2015)
 */
public class ItemCartridge extends ItemAbstract implements IGameCartridge {

	public ItemCartridge(String name) {
		super(name);
		setTextureless();
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void addInformation(ItemStack stack, EntityPlayer player, IItemTooltip tooltip) {
		tooltip.defaultInfoList();

		StringBuilder idInfo = new StringBuilder();
		idInfo.append(Localise.translate(Strings.LANG_TOOLTIP_CARTRIDGE_CONTENT)).append(": ");

		String gameID = getGameID(stack);
		if (DummyGames.EMPTY_GAME_WRAPPER.gameID().equals(gameID)) {
			idInfo.append(Localise.translate(Strings.LANG_TOOLTIP_CARTRIDGE_CONTENT_NONE));
			tooltip.addToShiftList(idInfo.toString());
			return;
		}

		ArcadeGameWrapper wrapper = ArcadeGameRegistry.gameForID(gameID);
		if (wrapper == DummyGames.UNKNOWN_GAME_WRAPPER) {
			tooltip.addAllToShiftList(Localise.wrapToSize(Localise.translate(Strings.LANG_TOOLTIP_CARTRIDGE_UNKNOWN_EXPLANATION), 42));
			tooltip.addToShiftList("");
		} else if (wrapper.gameDescription() != null) {
			// add description
			tooltip.addToShiftList(Localise.translate(Strings.LANG_TOOLTIP_CARTRIDGE_DESCRIPTION) + ":");
			for(String line : Localise.wrapToSize(wrapper.gameDescription(),40)) {
				tooltip.addToShiftList("  §6" + line);
			}
			tooltip.addToShiftList("");
		}

		idInfo.append("§3").append(gameID);
		tooltip.addToShiftList(idInfo.toString());
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String gameID = getGameID(stack);
		ArcadeGameWrapper wrapper = ArcadeGameRegistry.gameForID(gameID);
		if (!DummyGames.isDummyGame(wrapper.gameID())) {
			return wrapper.gameTitle();
		} else if (wrapper == DummyGames.UNKNOWN_GAME_WRAPPER) {
			return Localise.translate(Strings.LANG_TOOLTIP_CARTRIDGE_UNKNOWN);
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public Item getCartridgeItem() {
		return this;
	}

	@Override
	public String getGameID(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey(Strings.NBT_GAME) ? tag.getString(Strings.NBT_GAME) : DummyGames.EMPTY_GAME_WRAPPER.gameID();
	}

	@Override
	public void setGameID(ItemStack stack, String gameID) {
		if (gameID == null) {
			stack.setTagCompound(null);
			return;
		}

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
		}
		tag.setString(Strings.NBT_GAME, gameID);
	}
}
