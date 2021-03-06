package hydraulic.fluidnetwork;

import hydraulic.api.IColorCoded;
import hydraulic.api.IPipeConnection;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import universalelectricity.core.block.IConnectionProvider;

/**
 * A machine that acts as one with the liquid network using the networks pressure for some function
 * that doesn't change the over all network pressure. So pipes, gauges, tubes, buffers, decor
 * blocks.
 */
public interface IFluidNetworkPart extends IPipeConnection, IColorCoded, ITankContainer
{
	/**
	 * gets the devices pressure from a given side for input
	 */
	public double getMaxPressure(ForgeDirection side);

	/**
	 * The max amount of liquid that can flow per request
	 */
	public int getMaxFlowRate(LiquidStack stack, ForgeDirection side);

	/**
	 * The Fluid network that this machine is part of
	 */
	public HydraulicNetwork getNetwork();

	/**
	 * sets the machines network
	 */
	public void setNetwork(HydraulicNetwork network);

	/**
	 * Called when the pressure on the machine reachs max
	 * 
	 * @param damageAllowed - can this tileEntity cause grief damage
	 * @return true if the device over pressured and destroyed itself
	 */
	public boolean onOverPressure(Boolean damageAllowed);

	/**
	 * size of the pipes liquid storage ability
	 */
	public int getTankSize();
	
	public ILiquidTank getTank();
	
	public void setTankContent(LiquidStack stack);
	
	/**
	 * Gets a list of all the connected TileEntities that this conductor is connected to. The
	 * array's length should be always the 6 adjacent wires.
	 * 
	 * @return
	 */
	public TileEntity[] getAdjacentConnections();

	/**
	 * Instantly refreshes all connected blocks around the conductor, recalculating the connected
	 * blocks.
	 */
	public void updateAdjacentConnections();

}
