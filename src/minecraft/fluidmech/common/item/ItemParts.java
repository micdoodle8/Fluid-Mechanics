package fluidmech.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import fluidmech.common.TabFluidMech;

/**
 * A metadata item containing parts of various machines in Liquid Mechanics Mod.
 * 
 * @author Rs
 */
public class ItemParts extends ItemBasic
{
	public enum Parts
	{
		Bronze("BronzeTube"), Iron("IronTube"), Obby("ObbyTube"), Nether("NetherTube"), Seal("LeatherSeal"), SlimeSeal("SlimeSeal"), Tank("UnfinishedTank"), Valve("ValvePart");

		public String name;

		private Parts(String name)
		{
			this.name = name;
		}
	}

	public ItemParts(int par1)
	{
		super("lmPart", par1);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setCreativeTab(TabFluidMech.INSTANCE);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "item." + Parts.values()[itemStack.getItemDamage()].name;
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for (int i = 0; i < Parts.values().length; i++)
		{
			par3List.add(new ItemStack(this, 1, i));
		}
	}
}
