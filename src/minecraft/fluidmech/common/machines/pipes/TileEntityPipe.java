package fluidmech.common.machines.pipes;

import fluidmech.common.FluidMech;
import hydraulic.api.ColorCode;
import hydraulic.api.IColorCoded;
import hydraulic.api.IPipeConnection;
import hydraulic.api.IReadOut;
import hydraulic.fluidnetwork.HydraulicNetwork;
import hydraulic.fluidnetwork.IFluidNetworkPart;
import hydraulic.helpers.FluidHelper;

import java.io.IOException;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

import org.bouncycastle.util.Arrays;

import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityAdvanced;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPipe extends TileEntityAdvanced implements ITankContainer, IReadOut, IColorCoded, IFluidNetworkPart, IPacketReceiver
{

	/* TANK TO FAKE OTHER TILES INTO BELIVING THIS HAS AN INTERNAL STORAGE */
	private LiquidTank fakeTank = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME);
	/* CURRENTLY CONNECTED TILE ENTITIES TO THIS */
	private TileEntity[] connectedBlocks = new TileEntity[6];
	public boolean[] renderConnection = new boolean[6];
	public IPipeExtention[] subEntities = new IPipeExtention[6];
	/* RANDOM INSTANCE USED BY THE UPDATE TICK */
	private Random random = new Random();
	/* NETWORK INSTANCE THAT THIS PIPE USES */
	private HydraulicNetwork pipeNetwork;

	private boolean shouldAutoDrain = false;

	public enum PacketID
	{
		PIPE_CONNECTIONS, EXTENTION_CREATE, EXTENTION_UPDATE;
	}

	@Override
	public void initiate()
	{
		this.updateAdjacentConnections();
		if (this.subEntities[0] == null)
		{
			// this.addNewExtention(0, TileEntityPipeWindow.class);
		}
		if (!worldObj.isRemote)
		{

			for (int i = 0; i < 6; i++)
			{
				TileEntity entity = (TileEntity) this.subEntities[i];
				if (entity != null)
				{
					this.initSubTile(i);
				}
			}
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (ticks > 1)
		{
			this.updateSubEntities();
		}
		if (!worldObj.isRemote)
		{
			if (ticks % ((int) random.nextInt(5) * 40 + 20) == 0)
			{
				this.updateAdjacentConnections();
			}
			if (ticks % ((int) random.nextInt(5) * 60 + 20) == 0)
			{
				for (int i = 0; i < 6; i++)
				{
					if (this.subEntities[i] != null)
					{
						this.initSubTile(i);
					}
				}
			}
		}
	}

	/**
	 * Builds and sends data to client for all PipeExtentions
	 */
	private void updateSubEntities()
	{

		for (int i = 0; i < 6; i++)
		{
			if (subEntities[i] instanceof IPipeExtention && subEntities[i] instanceof TileEntity)
			{
				IPipeExtention extention = subEntities[i];
				if (this.ticks % extention.updateTick() == 0)
				{
					((TileEntity) extention).updateEntity();
					if (extention.shouldSendPacket(!this.worldObj.isRemote) && extention.getExtentionPacketData(!this.worldObj.isRemote) != null)
					{
						Packet packet = PacketManager.getPacket(FluidMech.CHANNEL, this, PacketID.EXTENTION_UPDATE.ordinal(), ForgeDirection.getOrientation(i), extention.getExtentionPacketData(!this.worldObj.isRemote));
						PacketManager.sendPacketToClients(packet, worldObj, new Vector3(this), 50);
					}
				}
			}
		}
	}

	@Override
	public void invalidate()
	{
		if (!this.worldObj.isRemote)
		{
			this.getNetwork().splitNetwork(this.worldObj, this);
		}

		super.invalidate();
	}

	@Override
	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			PacketID id = PacketID.values()[dataStream.readInt()];
			if (this.worldObj.isRemote)
			{
				if (id == PacketID.PIPE_CONNECTIONS)
				{
					this.renderConnection[0] = dataStream.readBoolean();
					this.renderConnection[1] = dataStream.readBoolean();
					this.renderConnection[2] = dataStream.readBoolean();
					this.renderConnection[3] = dataStream.readBoolean();
					this.renderConnection[4] = dataStream.readBoolean();
					this.renderConnection[5] = dataStream.readBoolean();
				}
				else if (id == PacketID.EXTENTION_CREATE)
				{
					System.out.println("Handling Packet for Pipe addon");
					int side = dataStream.readInt();
					NBTTagCompound tag = PacketManager.readNBTTagCompound(dataStream);
					this.loadOrCreateSubTile(side, tag);

				}
				else if (id == PacketID.EXTENTION_UPDATE)
				{
					int side = dataStream.readInt();
					if (this.subEntities[side] instanceof IPipeExtention)
					{
						this.subEntities[side].handlePacketData(network, type, packet, player, dataStream);
					}
				}
			}
		}
		catch (IOException e)
		{
			System.out.print("Error with reading packet for TileEntityPipe");
			e.printStackTrace();
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(FluidMech.CHANNEL, this, PacketID.PIPE_CONNECTIONS.ordinal(), this.renderConnection[0], this.renderConnection[1], this.renderConnection[2], this.renderConnection[3], this.renderConnection[4], this.renderConnection[5]);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		LiquidStack liquid = LiquidStack.loadLiquidStackFromNBT(nbt.getCompoundTag("tank"));
		if (liquid != null)
		{
			this.fakeTank.setLiquid(liquid);
		}
		for (int i = 0; i < 6; i++)
		{
			if (nbt.hasKey("Addon" + i))
			{
				this.loadOrCreateSubTile(i, nbt.getCompoundTag("Addon" + i));
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.fakeTank.containsValidLiquid())
		{
			nbt.setTag("stored", this.fakeTank.getLiquid().writeToNBT(new NBTTagCompound()));
		}
		for (int i = 0; i < 6; i++)
		{
			if (this.subEntities[i] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				((TileEntity) this.subEntities[i]).writeToNBT(tag);
				nbt.setTag("Addon" + i, tag);
			}
		}
	}

	public boolean addNewExtention(int side, Class<? extends TileEntity> partClass)
	{
		if (partClass == null)
		{
			return false;
		}
		try
		{
			TileEntity tile = partClass.newInstance();
			if (tile instanceof IPipeExtention)
			{
				this.subEntities[side] = (IPipeExtention) tile;
				this.initSubTile(side);
			}
		}
		catch (Exception e)
		{
			System.out.print("Failed to add a Pipe Extention using Class " + partClass.toString());
			e.printStackTrace();
		}
		return false;
	}

	public void loadOrCreateSubTile(int side, NBTTagCompound tag)
	{
		if (tag != null && tag.hasKey("id"))
		{
			TileEntity tile = TileEntity.createAndLoadEntity(tag);
			if (tile instanceof IPipeExtention)
			{
				this.subEntities[side] = (IPipeExtention) tile;
				this.initSubTile(side);
				if (worldObj != null)
				{
					System.out.println("Creating addon " + (worldObj.isRemote ? "Client" : "Server"));
				}
				else
				{
					System.out.println("Creating addon Unkown side");
				}
			}
		}
	}

	public void initSubTile(int side)
	{
		if (this.subEntities[side] instanceof TileEntity)
		{
			TileEntity tile = (TileEntity) subEntities[side];
			((IPipeExtention) tile).setPipe(this);
			((IPipeExtention) tile).setDirection(ForgeDirection.getOrientation(side));
			tile.worldObj = this.worldObj;
			tile.xCoord = this.xCoord;
			tile.yCoord = this.yCoord;
			tile.zCoord = this.zCoord;

			sendExtentionToClient(side);
		}
	}

	/**
	 * Sends the save data for the tileEntity too the client
	 */
	public void sendExtentionToClient(int side)
	{
		if (worldObj != null && !worldObj.isRemote && this.subEntities[side] instanceof TileEntity)
		{
			NBTTagCompound tag = new NBTTagCompound();
			((TileEntity) this.subEntities[side]).writeToNBT(tag);
			if (tag != null && tag.hasKey("id"))
			{
				System.out.println("Sending TileEntity to Client");
				Packet packet = PacketManager.getPacket(FluidMech.CHANNEL, this, PacketID.EXTENTION_CREATE.ordinal(), ForgeDirection.getOrientation(side), tag);
				PacketManager.sendPacketToClients(packet, this.worldObj, new Vector3(this), 50);
			}
		}
	}

	public TileEntity getEntitySide(ForgeDirection side)
	{
		return (TileEntity) this.subEntities[side.ordinal() & 5];

	}

	/**
	 * gets the current color mark of the pipe
	 */
	@Override
	public ColorCode getColor()
	{
		return ColorCode.get(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
	}

	/**
	 * sets the current color mark of the pipe
	 */
	@Override
	public void setColor(Object cc)
	{
		ColorCode code = ColorCode.get(cc);
		if (!worldObj.isRemote && code != this.getColor())
		{
			this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, code.ordinal() & 15, 3);
		}
	}

	@Override
	public String getMeterReading(EntityPlayer user, ForgeDirection side)
	{
		/* DEBUG CODE ACTIVATERS */
		boolean testConnections = false;
		boolean testNetwork = false;
		boolean testSubs = false;

		/* NORMAL OUTPUT */
		String string = this.getNetwork().pressureProduced + "p " + this.getNetwork().getStorageFluid() + " Extra";

		/* DEBUG CODE */
		if (testConnections)
		{
			for (int i = 0; i < 6; i++)
			{
				string += ":" + (this.renderConnection[i] ? "T" : "F") + (this.getAdjacentConnections()[i] != null ? "T" : "F");
			}
		}
		if (testNetwork)
		{
			string += " " + this.getNetwork().toString();
		}
		if (testSubs)
		{
			string += " ";
			for (int i = 0; i < 6; i++)
			{
				if (this.subEntities[i] == null)
				{
					string += ":" + "Null";
				}
				else
				{
					string += ":" + this.subEntities[i].toString();
				}
			}
			string += " ";
		}

		return string;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		if (resource == null || !this.getColor().isValidLiquid(resource))
		{
			return 0;
		}
		return this.getNetwork().addFluidToNetwork(worldObj.getBlockTileEntity(xCoord + from.offsetX, yCoord + from.offsetY, zCoord + from.offsetZ), resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		if (tankIndex != 0 || resource == null || !this.getColor().isValidLiquid(resource))
		{
			return 0;
		}
		return this.getNetwork().addFluidToNetwork(this, resource, doFill);
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		return new ILiquidTank[] { this.fakeTank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		if (this.getColor().isValidLiquid(type))
		{
			return this.fakeTank;
		}
		return null;
	}

	/**
	 * Checks to make sure the connection is valid to the tileEntity
	 * 
	 * @param tileEntity - the tileEntity being checked
	 * @param side - side the connection is too
	 */
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote && tileEntity != null)
		{
			if (this.subEntities[side.ordinal()] != null)
			{
				connectedBlocks[side.ordinal()] = null;
				return;
			}
			if (tileEntity instanceof IPipeConnection)
			{
				if (((IPipeConnection) tileEntity).canPipeConnect(this, side))
				{
					if (tileEntity instanceof IFluidNetworkPart)
					{
						if (((IFluidNetworkPart) tileEntity).getColor() == this.getColor())
						{
							this.getNetwork().mergeNetworks(((IFluidNetworkPart) tileEntity).getNetwork());
							connectedBlocks[side.ordinal()] = tileEntity;
						}
					}
					else
					{
						connectedBlocks[side.ordinal()] = tileEntity;
					}
				}
			}
			else if (tileEntity instanceof IColorCoded)
			{
				if (this.getColor() == ColorCode.NONE || this.getColor() == ((IColorCoded) tileEntity).getColor())
				{
					connectedBlocks[side.ordinal()] = tileEntity;
				}
			}
			else if (tileEntity instanceof ITankContainer)
			{
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

	@Override
	public void updateAdjacentConnections()
	{

		if (this.worldObj != null && !this.worldObj.isRemote)
		{

			boolean[] previousConnections = this.renderConnection.clone();
			this.connectedBlocks = new TileEntity[6];

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				this.validateConnectionSide(this.worldObj.getBlockTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ), dir);

				this.renderConnection[i] = this.connectedBlocks[i] != null;

				if (this.renderConnection[i] && this.connectedBlocks[i] instanceof ITankContainer && !(this.connectedBlocks[i] instanceof IFluidNetworkPart))
				{
					ITankContainer tankContainer = (ITankContainer) this.connectedBlocks[i];
					this.getNetwork().addEntity(tankContainer);

					/* LITTLE TRICK TO AUTO DRAIN TANKS ON EACH CONNECTION UPDATE */

					LiquidStack stack = tankContainer.drain(dir, LiquidContainerRegistry.BUCKET_VOLUME, false);
					if (stack != null && stack.amount > 0)
					{
						int fill = this.getNetwork().addFluidToNetwork((TileEntity) tankContainer, stack, true);
						tankContainer.drain(dir, fill, true);
					}
				}
			}

			/**
			 * Only send packet updates if visuallyConnected changed.
			 */
			if (!Arrays.areEqual(previousConnections, this.renderConnection))
			{
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	@Override
	public boolean canPipeConnect(TileEntity entity, ForgeDirection dir)
	{
		return this.subEntities[dir.ordinal()] == null;
	}

	@Override
	public double getMaxPressure(ForgeDirection side)
	{
		return 350;
	}

	@Override
	public HydraulicNetwork getNetwork()
	{
		if (this.pipeNetwork == null)
		{
			this.setNetwork(new HydraulicNetwork(this.getColor(), this));
		}
		return this.pipeNetwork;
	}

	@Override
	public void setNetwork(HydraulicNetwork network)
	{
		this.pipeNetwork = network;
	}

	@Override
	public int getMaxFlowRate(LiquidStack stack, ForgeDirection side)
	{
		return FluidHelper.getDefaultFlowRate(stack) * 2;
	}

	@Override
	public boolean onOverPressure(Boolean damageAllowed)
	{
		if (damageAllowed)
		{
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, yCoord, 0, 0);
			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
	}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		return this.connectedBlocks;
	}

	@Override
	public int getTankSize()
	{
		return LiquidContainerRegistry.BUCKET_VOLUME * 2;
	}

	@Override
	public ILiquidTank getTank()
	{
		return this.fakeTank;
	}

	@Override
	public void setTankContent(LiquidStack stack)
	{
		this.fakeTank.setLiquid(stack);

	}

}
