package hydraulic.api;

import net.minecraft.block.Block;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

public enum ColorCode
{
	BLACK("Black"), RED("Red"), GREEN("Green"), BROWN("Brown"), BLUE("Blue"), PURPLE("Purple"), CYAN("Cyan"), SILVER("Silver"), GREY("Grey"), PINK("Pink"), LIME("Lime"), YELLOW("Yellow"), LIGHTBLUE("LightBlue"), WHITE("White"), ORANGE("Orange"), NONE("");

	String name;

	private ColorCode(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	/**
	 * gets a ColorCode from any of the following
	 * 
	 * @param obj - Integer,String,LiquidData,ColorCode
	 * @return Color NONE if it can't find it
	 */
	public static ColorCode get(Object obj)
	{
		if (obj instanceof Integer && ((Integer) obj) < ColorCode.values().length)
		{
			return ColorCode.values()[((Integer) obj)];
		}
		else if (obj instanceof ColorCode)
		{
			return (ColorCode) obj;
		}
		else if (obj instanceof String)
		{
			for (int i = 0; i < ColorCode.values().length; i++)
			{
				if (((String) obj).equalsIgnoreCase(ColorCode.get(i).getName()))
				{
					return ColorCode.get(i);
				}
			}
		}
		return NONE;
	}

	/**
	 * checks to see if the liquidStack is valid for the given color
	 */
	public boolean isValidLiquid(LiquidStack stack)
	{
		if (this != BLUE && this != RED && this != BLACK && this != YELLOW && this != ORANGE)
		{
			return true;
		}
		else if (stack == null)
		{
			return false;
		}
		else if (this == BLUE && stack.isLiquidEqual(new LiquidStack(Block.waterStill, LiquidContainerRegistry.BUCKET_VOLUME)))
		{
			return true;
		}
		else if (this == RED && stack.isLiquidEqual(new LiquidStack(Block.lavaStill, LiquidContainerRegistry.BUCKET_VOLUME)))
		{
			return true;
		}
		else if (this == BLACK && stack.isLiquidEqual(LiquidDictionary.getCanonicalLiquid("Oil")))
		{
			return true;
		}
		else if (this == YELLOW && stack.isLiquidEqual(LiquidDictionary.getCanonicalLiquid("Fuel")))
		{
			return true;
		}
		else if (this == ORANGE && stack.isLiquidEqual(LiquidDictionary.getCanonicalLiquid("Steam")))
		{
			return true;
		}
		return false;
	}
}
