package com.hbm.blocks.network;

import api.hbm.block.IToolable;

import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.ILookOverlay;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.RenderBlockMultipass;
import com.hbm.tileentity.network.TileEntityPipeExhaust;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctPaintableBlockExhaust extends FluidDuctBase implements IToolable, IBlockMultiPass, ILookOverlay {
	
	@SideOnly(Side.CLIENT) protected IIcon overlay;
	
	public FluidDuctPaintableBlockExhaust() {
        super(Material.iron);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPipeExhaustPaintable();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(RefStrings.MODID + ":fluid_duct_paintable_block_exhaust");
		this.overlay = reg.registerIcon(RefStrings.MODID + ":fluid_duct_paintable_overlay");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileEntityPipeExhaustPaintable) {
			TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;

			if(pipe.block != null) {
				if(RenderBlockMultipass.currentPass == 1) {
					return this.overlay;
				} else {
					return pipe.block.getIcon(side, pipe.meta);
				}
			}
		}

		return this.blockIcon;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;

		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileEntityPipeExhaustPaintable) {
			TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;

			if(pipe.block != null) {
				pipe.block = null;
				world.markBlockForUpdate(x, y, z);
				pipe.markDirty();
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fX, float fY, float fZ) {
		
		ItemStack stack = player.getHeldItem();

		if(stack != null && stack.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock) stack.getItem();
			Block block = ib.field_150939_a;

			if(block.renderAsNormalBlock() && block != this) {

				TileEntity tile = world.getTileEntity(x, y, z);

				if(tile instanceof TileEntityPipeExhaustPaintable) {
					TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;

					if(pipe.block == null) {
						pipe.block = block;
						pipe.meta = stack.getItemDamage() & 15;
						world.markBlockForUpdate(x, y, z);
						pipe.markDirty();
						return true;
					}
				}
			}
		}

		return super.onBlockActivated(world, x, y, z, player, side, fX, fY, fZ);
	}

	@Override
	public int getPasses() {
		return 2;
	}

	@Override
	public int getRenderType(){
		return IBlockMultiPass.getRenderType();
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		List<String> text = new ArrayList();
		text.add(Fluids.SMOKE.getLocalizedName());
		text.add(Fluids.SMOKE_LEADED.getLocalizedName());
		text.add(Fluids.SMOKE_POISON.getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}
	
	public static class TileEntityPipeExhaustPaintable extends TileEntityPipeExhaust implements ICopiable {

		private Block block;
		private int meta;
		private Block lastBlock;
		private int lastMeta;
		
		@Override
		public void updateEntity() {
			super.updateEntity();
			
			if(worldObj.isRemote && (lastBlock != block || lastMeta != meta)) {
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				lastBlock = block;
				lastMeta = meta;
			}
		}
		
		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.func_148857_g());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			int id = nbt.getInteger("block");
			this.block = id == 0 ? null : Block.getBlockById(id);
			this.meta = nbt.getInteger("meta");
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(block != null) nbt.setInteger("block", Block.getIdFromBlock(block));
			nbt.setInteger("meta", meta);
		}

		@Override
		public NBTTagCompound getSettings(World world, int x, int y, int z) {
			NBTTagCompound nbt = new NBTTagCompound();
			if(block != null) {
				nbt.setInteger("paintblock", Block.getIdFromBlock(block));
				nbt.setInteger("paintmeta", meta);
			}
			return nbt;
		}

		@Override
		public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
			if(nbt.hasKey("paintblock")) {
				this.block = Block.getBlockById(nbt.getInteger("paintblock"));
				this.meta = nbt.getInteger("paintmeta");
			}
		}
	}
}
