package de.npe.mcmods.nparcade.arcade;

import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;

import org.lwjgl.opengl.GL11;

import de.npe.api.nparcade.IArcadeGame;
import de.npe.api.nparcade.IArcadeMachine;
import de.npe.api.nparcade.util.Size;
import de.npe.mcmods.nparcade.arcade.api.IGameCartridge;
import de.npe.mcmods.nparcade.common.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.Item;

/**
 * This class holds information about the game, and (if on the client side)
 * offers a method to create a new instance of that game.
 */
public final class ArcadeGameWrapper {
	private final String id;
	private final String title;
	private final String description;

	private boolean hasColor;
	private float colorRed = 1F;
	private float colorGreen = 1F;
	private float colorBlue = 1F;

	private final BufferedImage label;
	private final Size labelSize;
	private int textureID = -1;

	private final Class<? extends IArcadeGame> gameClass;
	private final Constructor<? extends IArcadeGame> constructor;
	private final IGameCartridge customCartridge;

	ArcadeGameWrapper(String id, String title, String description, BufferedImage label, int color, Class<? extends IArcadeGame> gameClass, IGameCartridge customCartridge) {
		this.id = id;
		this.title = title;
		this.description = description;

		// this.label = (label == null) ? null : label.getRGB(0,0, label.getWidth(), label.getHeight(), null, 0, label.getHeight());
		this.label = (label == null) ? null : new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
		labelSize = (label == null) ? null : new Size(label.getWidth(), label.getHeight());
		if (label != null) {
			this.label.createGraphics().drawImage(label, 0, 0, null);
		}

		this.gameClass = gameClass;
		try {
			constructor = gameClass != null
					? gameClass.getConstructor(IArcadeMachine.class)
					: null;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Class " + gameClass.getCanonicalName() + " is missing a public constructor which takes an IArcadeMachine!", ex);
		}

		if (customCartridge != null && !(customCartridge instanceof Item)) {
			throw new IllegalArgumentException(
					"customCartridge is not an instance of " + Item.class.getCanonicalName()
							+ ". Class of customCartridge: " + customCartridge.getClass());
		}

		this.customCartridge = customCartridge;

		if (color != -1) {
			hasColor = true;
			int r = ((color >> 16) & 0xFF);
			colorRed = r / 255F;
			int g = ((color >> 8) & 0xFF);
			colorGreen = g / 255F;
			int b = (color & 0xFF);
			colorBlue = b / 255F;
		}
	}

	public String gameID() {
		return id;
	}

	public String gameTitle() {
		return title;
	}

	public IGameCartridge cartridge() {
		return customCartridge != null ? customCartridge : ModItems.cartridge;
	}

	public Class<? extends IArcadeGame> gameClass() {
		return gameClass;
	}

	/////////////////////////
	// CLIENT ONLY METHODS //
	/////////////////////////

	@SideOnly(Side.CLIENT)
	public String gameDescription() {
		return description;
	}

	@SideOnly(Side.CLIENT)
	public boolean hasLabel() {
		return label != null;
	}

	@SideOnly(Side.CLIENT)
	public Size labelSize() {
		return labelSize;
	}

	@SideOnly(Side.CLIENT)
	public int prepareLabelTexture() {
		// allocate new texture if not yet initialized
		if (textureID == -1) {
			textureID = GL11.glGenTextures();
			TextureUtil.allocateTexture(textureID, labelSize.width, labelSize.height);

			// upload pixels to texture
			int[] pixels = new int[labelSize.width * labelSize.height];
			label.getRGB(0, 0, labelSize.width, labelSize.height, pixels, 0, labelSize.width);
			TextureUtil.uploadTexture(textureID, pixels, labelSize.width, labelSize.height);
		}
		return textureID;
	}

	@SideOnly(Side.CLIENT)
	public boolean hasColor() {
		return hasColor;
	}

	@SideOnly(Side.CLIENT)
	public float colorRed() {
		return colorRed;
	}

	@SideOnly(Side.CLIENT)
	public float colorGreen() {
		return colorGreen;
	}

	@SideOnly(Side.CLIENT)
	public float colorBlue() {
		return colorBlue;
	}

	@SideOnly(Side.CLIENT)
	public IArcadeGame createGameInstance(IArcadeMachine arcadeMachine) {
		try {
			return constructor.newInstance(arcadeMachine);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
