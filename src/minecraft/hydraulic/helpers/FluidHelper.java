package hydraulic.helpers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import universalelectricity.core.vector.Vector3;

public class FluidHelper
{
	/**
	 * The default built in flow rate of the liquid threw the pipes. Will correct this later to use
	 * a visc value instead of flow value so that the size of the pipe can play a factor in flow
	 */
	public static int getDefaultFlowRate(LiquidStack stack)
	{
		if (stack != null)
		{
			String stackName = LiquidDictionary.findLiquidName(stack);
			if (stackName.equalsIgnoreCase("UraniumHexafluoride"))
			{
				return 1000;
			}
			else if (stackName.equalsIgnoreCase("steam"))
			{
				return 1000;
			}
			else if (stackName.equalsIgnoreCase("methane"))
			{
				return 1000;
			}
			else if (stackName.equalsIgnoreCase("lava"))
			{
				return 250;
			}
		}
		return 500;
	}

	/**
	 * Creates a new LiquidStack using the sample stack
	 * 
	 * @param stack - liquidLiquid being used to create the stack
	 * @param vol - amount or volume of the stack
	 * @return a new @LiquidStack
	 */
	public static LiquidStack getStack(LiquidStack stack, int vol)
	{
		if (stack == null)
		{
			return null;
		}
		return new LiquidStack(stack.itemID, vol, stack.itemMeta);
	}

	/**
	 * Consumes one item of a the ItemStack
	 */
	public static ItemStack consumeItem(ItemStack stack)
	{
		if (stack.stackSize == 1)
		{
			if (stack.getItem().hasContainerItem())
			{
				return stack.getItem().getContainerItemStack(stack);
			}
			else
			{
				return null;
			}
		}
		else
		{
			stack.splitStack(1);
			return stack;
		}
	}

	/**
	 * gets the blockID/ItemID of the Still liquid
	 * 
	 * @param id - blockID
	 * @return will return -1 if its not a valid liquid Block
	 */
	public static int getLiquidId(int id)
	{
		if (id == Block.waterStill.blockID || id == Block.waterMoving.blockID)
		{
			return Block.waterStill.blockID;
		}
		else if (id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
		{
			return Block.lavaStill.blockID;
		}
		else if (Block.blocksList[id] instanceof ILiquid)
		{
			return ((ILiquid) Block.blocksList[id]).stillLiquidId();
		}
		else
		{
			return -1;
		}
	}

	/**
	 * gets the liquidStack of the block
	 * 
	 * @param id - block's ID
	 */
	public static LiquidStack getLiquidFromBlockId(int id)
	{
		if (id == Block.waterStill.blockID || id == Block.waterMoving.blockID)
		{
			return new LiquidStack(Block.waterStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
		}
		else if (id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
		{
			return new LiquidStack(Block.lavaStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
		}
		else if (Block.blocksList[id] instanceof ILiquid)
		{
			ILiquid liquid = (ILiquid) Block.blocksList[id];
			if (liquid.isMetaSensitive())
			{
				return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, liquid.stillLiquidMeta());
			}
			else
			{
				return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, 0);
			}
		}
		return null;
	}

	/**
	 * Is the location a liquid source block
	 */
	public static boolean isSourceBlock(World world, Vector3 vec)
	{
		LiquidStack liquid = FluidHelper.getLiquidFromBlockId(vec.getBlockID(world));
		if ((liquid != null && vec.getBlockMetadata(world) == 0))
		{
			return true;
		}
		return false;
	}

	/**
	 * Gets the number of source liquids blocks around the locaiton
	 */
	public static int getConnectedSources(World world, Vector3 vec)
	{
		int sources = 0;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			Vector3 pos = vec.clone().modifyPositionFromSide(direction);
			if (isSourceBlock(world, pos))
			{
				sources++;
			}
		}
		return sources;
	}

	/**
	 * Gets the number of liquid fillable blocks around the location
	 */
	public static int getConnectedFillables(World world, Vector3 vec)
	{
		int sources = 0;
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			Vector3 pos = vec.clone().modifyPositionFromSide(direction);
			LiquidStack liquid = FluidHelper.getLiquidFromBlockId(pos.getBlockID(world));
			if ((liquid != null || pos.getBlockID(world) == 0) && getConnectedSources(world, pos) > 0)
			{
				sources++;
			}
		}
		return sources;
	}
}
