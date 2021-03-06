package fluidmech.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fluidmech.common.FluidMech;
import fluidmech.common.TabFluidMech;

public class ItemBasic extends Item
{
	protected final Icon[] icons = new Icon[256];

	public ItemBasic(String name, int id)
	{
		super(id);
		this.setUnlocalizedName(name);
		this.setCreativeTab(TabFluidMech.INSTANCE);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateIcons(IconRegister iconRegister)
	{
		List<ItemStack> list = new ArrayList<ItemStack>();
		this.getSubItems(this.itemID, this.getCreativeTab(), list);

		if (list.size() < this.icons.length)
		{
			for (ItemStack itemStack : list)
			{
				this.icons[list.indexOf(itemStack)] = iconRegister.registerIcon(this.getUnlocalizedName(itemStack).replace("item.", FluidMech.TEXTURE_NAME_PREFIX));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int damage)
	{
		if (this.icons.length > damage && !this.isDamageable())
		{
			return this.icons[damage];
		}

		return icons[0];
	}
}