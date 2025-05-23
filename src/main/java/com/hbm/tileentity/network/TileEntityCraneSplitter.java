package com.hbm.tileentity.network;

import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TileEntityCraneSplitter extends TileEntityLoadedBase {

	/* false: left belt is preferred, true: right belt is preferred */
	public int position = 0;
	public int split = 2;
	public boolean side = false;

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {this.readFromNBT(pkt.func_148857_g());}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.position = nbt.getInteger("pos");
		this.split = nbt.getInteger("split");
		this.side = nbt.getBoolean("side");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("pos", this.position);
		nbt.setInteger("split", this.split);
		nbt.setBoolean("side", this.side);
	}
}
