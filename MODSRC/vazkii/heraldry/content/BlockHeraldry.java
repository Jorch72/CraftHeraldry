package vazkii.heraldry.content;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import vazkii.heraldry.core.data.CrestData;
import vazkii.heraldry.core.proxy.CommonProxy;
import vazkii.heraldry.lib.LibContent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHeraldry extends BlockContainer {

	public BlockHeraldry() {
		super(Material.wood);
		setHardness(0.2F);
		setResistance(0.2F);
		setBlockName(LibContent.HERALDRY_BLOCK_NAME);
	}

	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		// NO-OP
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		TileEntity tile = par1World.getTileEntity(par2, par3, par4);
		ItemStack stack = par5EntityPlayer.getCurrentEquippedItem();

		if(tile != null && tile instanceof TileEntityBanner && !par1World.isRemote) {
			boolean holding = stack != null;
			TileEntityBanner banner = (TileEntityBanner) tile;

			if(!holding && par5EntityPlayer.isSneaking()) {
				banner.locked = !banner.locked;
				par5EntityPlayer.addChatMessage(new ChatComponentText("Banner " + (banner.locked ? "Locked" : "Unlocked") + "."));
			}

			if(holding && stack.getItem() == CommonProxy.itemHeraldry && stack.getItemDamage() == 0) {
				if(banner.locked) {
					par5EntityPlayer.addChatMessage(new ChatComponentText("This banner is locked. Shift-Right click with an empty hand to unlock it."));
				} else {
					CrestData data = ItemHeraldry.readCrestData(stack);
					NBTTagCompound cmp = new NBTTagCompound();
					data.writeToCmp(cmp);
					banner.data = data;
					par1World.markBlockForUpdate(par2, par3, par4);
				}
			}
		}

		return true;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return Blocks.planks.getIcon(0, 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		if(isHanging(meta)) {
			float thickness = 0.1F;
			switch(getOrientation(meta)) {
			case 0 : {
				setBlockBounds(0F, 0F, 0F, thickness, 1F, 1F);
				break;
			}
			case 2 : {
				setBlockBounds(0F, 0F, 0F, 1F, 1F, thickness);
				break;
			}
			case 4 : {
				setBlockBounds(1F - thickness, 0F, 0F, 1F, 1F, 1F);
				break;
			}
			case 6 : {
				setBlockBounds(0F, 0F, 1F - thickness, 1F, 1F, 1F);
				break;
			}
			}
			return AxisAlignedBB.getBoundingBox(par2 + minX, par3 + minY - 0.5, par4 + minZ, par2 + maxX, par3 + maxY, par4 + maxZ);
		} else {
			float o = 0.0625F;
			setBlockBounds(o, 0F, o, 1F - o, 1F, 1F - o);
			return AxisAlignedBB.getBoundingBox(par2 + minX, par3 + minY, par4 + minZ, par2 + maxX, par3 + maxY + 0.8, par4 + maxZ);
		}
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return CommonProxy.itemHeraldry;
	}

	@Override
	public int damageDropped(int par1) {
		return isHanging(par1) ? 2 : 1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return null;
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
		return true;
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		if(!canBlockStay(par1World, par2, par3, par4)) {
			dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
			par1World.setBlockToAir(par2, par3, par4);
		}
	}

	@Override
	public boolean canBlockStay(World par1World, int par2, int par3, int par4) {
		boolean canStay = true;
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		boolean hanging = isHanging(meta);
		if(hanging) {
			canStay = par1World.isAirBlock(par2, par3 - 1, par4);
			if(canStay) {
				switch(getOrientation(meta)) {
				case 0 : {
					canStay = par1World.isSideSolid(par2 - 1, par3, par4, ForgeDirection.EAST, false);
					break;
				}
				case 2 : {
					canStay = par1World.isSideSolid(par2, par3, par4 - 1, ForgeDirection.SOUTH, false);
					break;
				}
				case 4 : {
					canStay = par1World.isSideSolid(par2 + 1, par3, par4, ForgeDirection.WEST, false);
					break;
				}
				default : {
					canStay = par1World.isSideSolid(par2, par3, par4 + 1, ForgeDirection.NORTH, false);
					break;
				}
				}
			}
		} else canStay = par1World.isSideSolid(par2, par3 - 1, par4, ForgeDirection.UP, false) && par1World.isAirBlock(par2, par3 + 1, par4);

		return canStay;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(CommonProxy.itemHeraldry, 1, isHanging(world.getBlockMetadata(x, y, z)) ? 2 : 1);
	}

	public static boolean isHanging(int meta) {
		return meta >= 8;
	}

	public static int getOrientation(int meta) {
		return isHanging(meta) ? meta - 8 : meta;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBanner();
	}

}
