package fluidmech.client.render.pipeextentions;

import hydraulic.api.ColorCode;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import fluidmech.client.model.ModelLargePipe;
import fluidmech.common.FluidMech;
import fluidmech.common.machines.pipes.IPipeExtention;
import fluidmech.common.machines.pipes.TileEntityPipe;

public class RenderPipe extends TileEntitySpecialRenderer
{
	private ModelLargePipe SixPipe;
	private boolean[] renderSide = new boolean[6];

	public RenderPipe()
	{
		SixPipe = new ModelLargePipe();
	}

	public void renderAModelAt(TileEntity te, double d, double d1, double d2, float f)
	{
		// Texture file
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);

		int meta = 0;

		if (te instanceof TileEntityPipe)
		{
			meta = te.getBlockMetadata();
			TileEntityPipe pipe = ((TileEntityPipe) te);
			this.renderSide = pipe.renderConnection;
			for (int i = 0; i < 6; i++)
			{
				IPipeExtention extention = (IPipeExtention) pipe.subEntities[i];
				if (extention != null)
				{
					Object ob;
					try
					{
						ob = extention.getExtentionRenderClass().newInstance();

						if (ob instanceof IPipeExtentionRender)
						{
							IPipeExtentionRender render = (IPipeExtentionRender) ob;
							if (render != null)
							{
								System.out.println("Rendering Pipe Addon side " + i);
								render.renderAModelAt(this, pipe, new Vector3(0, 0, 0), f, ForgeDirection.getOrientation(i));
							}
						}
					}
					catch (Exception e)
					{
						System.out.println("Failed to render a pipe extention");
						e.printStackTrace();
					}
				}
			}
		}
		this.render(meta, renderSide);
		GL11.glPopMatrix();

	}

	public void bindTextureForPipe(String texture)
	{
		this.bindTextureByName(texture);
	}

	public static String getPipeTexture(int meta)
	{
		return FluidMech.MODEL_TEXTURE_DIRECTORY + "pipes/" + ColorCode.get(meta).getName() + "Pipe.png";
	}

	public void render(int meta, boolean[] side)
	{
		bindTextureByName(this.getPipeTexture(meta));
		if (side[0])
		{
			SixPipe.renderBottom();
		}
		if (side[1])
		{
			SixPipe.renderTop();
		}
		if (side[3])
		{
			SixPipe.renderFront();
		}
		if (side[2])
		{
			SixPipe.renderBack();
		}
		if (side[5])
		{
			SixPipe.renderRight();
		}
		if (side[4])
		{
			SixPipe.renderLeft();
		}
		SixPipe.renderMiddle();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double var2, double var4, double var6, float var8)
	{
		this.renderAModelAt(tileEntity, var2, var4, var6, var8);
	}

}