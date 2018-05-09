package cr0s.warpdrive.render;


public class RenderBlockTransporterBeacon {

}
/* @TODO MC1.10 ISBRH
public class RenderBlockTransporterBeacon implements ISimpleBlockRenderingHandler {
	
	public static int renderId = 0;
	public static RenderBlockTransporterBeacon instance = new RenderBlockTransporterBeacon();
	
	private static final double CORE_RADIUS                   =  2.0D / 32.0D;
	private static final double CORE_Y_MIN_PACKED_INACTIVE    =  0.0D / 32.0D;
	private static final double CORE_Y_MIN_PACKED_ACTIVE      =  0.0D / 32.0D;
	private static final double CORE_Y_MIN_DEPLOYED_INACTIVE  =  1.0D / 32.0D;
	private static final double CORE_Y_MIN_DEPLOYED_ACTIVE    =  1.0D / 32.0D;
	private static final double CORE_Y_MAX_PACKED_INACTIVE    = 11.0D / 32.0D;
	private static final double CORE_Y_MAX_PACKED_ACTIVE      = 15.0D / 32.0D;
	private static final double CORE_Y_MAX_DEPLOYED_INACTIVE  = 12.0D / 32.0D;
	private static final double CORE_Y_MAX_DEPLOYED_ACTIVE    = 20.0D / 32.0D;
	private static final double BRANCH_HEIGHT                 = 24.0D / 32.0D;
	private static final double BRANCH_RADIUS                 = 16.0D / 32.0D;
	
	@Override
	public void renderInventoryBlock(final Block block, final int metadata, final int modelId, final RenderBlocks renderer) {
		if (!(block instanceof BlockTransporterBeacon)) {
			return;
		}
		
		final EnumTransporterBeaconState enumTransporterBeaconState = EnumTransporterBeaconState.get(metadata & 0x7);
		if (enumTransporterBeaconState == null) {
			return;
		}
		
		final IIcon icon = RenderBlocks.getInstance().getBlockIconFromSideAndMetadata(block, 0, enumTransporterBeaconState.getMetadata());
		
		GL11.glPushMatrix();
		
		final Tessellator tessellator = Tessellator.instance;
		
		// (block bounds aren't used in our render => no need to grab them here)
		
		// disable lightning in item rendering, no need to set brightness
		GL11.glDisable(GL11.GL_LIGHTING);
		
		// (blending already by caller)
		// (color already set by caller?)
		// (transformation already done by caller)
		
		tessellator.startDrawingQuads();
		renderTransporterBeacon(tessellator, 0.0D, 0.0D, 0.0D, enumTransporterBeaconState, icon);
		tessellator.draw();
		
		GL11.glEnable(GL11.GL_LIGHTING);
		
		GL11.glPopMatrix();
	}
	
	@Override
	public boolean renderWorldBlock(final IBlockAccess blockAccess, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
		if (!(block instanceof BlockTransporterBeacon)) {
			return false;
		}
		final TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityTransporterBeacon)) {
			return false;
		}
		
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		final EnumTransporterBeaconState enumTransporterBeaconState = EnumTransporterBeaconState.get(metadata);
		if (enumTransporterBeaconState == null) {
			return false;
		}
		
		final IIcon icon = RenderBlocks.getInstance().getBlockIconFromSideAndMetadata(block, 0, metadata);
		
		final Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y, z));
		tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
		
		renderTransporterBeacon(tessellator, x, y, z, enumTransporterBeaconState, icon);
		
		return true;
	}
	
	private void renderTransporterBeacon(final Tessellator tessellator,
	                                     final double x, final double y, final double z,
	                                     final EnumTransporterBeaconState enumTransporterBeaconState,
	                                     final IIcon icon) {
		// texture coordinates
		final double uMin_side   = icon.getInterpolatedU( 0.0D);
		final double vMin_side   = icon.getInterpolatedV( 4.0D);
		final double uMax_side   = icon.getInterpolatedU(16.0D);
		final double vMax_side   = icon.getInterpolatedV(16.0D);
		final double uMin_top    = icon.getInterpolatedU( 1.0D);
		final double vMin_top    = icon.getInterpolatedV( 1.0D);
		final double uMax_top    = icon.getInterpolatedU( 3.0D);
		final double vMax_top    = icon.getInterpolatedV( 3.0D);
		final double uMin_bottom = icon.getInterpolatedU( 5.0D);
		final double vMin_bottom = icon.getInterpolatedV( 1.0D);
		final double uMax_bottom = icon.getInterpolatedU( 7.0D);
		final double vMax_bottom = icon.getInterpolatedV( 3.0D);
		
		// vertex coordinates
		final double xCenter = x + 0.5D;
		final double zCenter = z + 0.5D;
		final double xMin_core = xCenter - CORE_RADIUS;
		final double xMax_core = xCenter + CORE_RADIUS;
		final double zMin_core = zCenter - CORE_RADIUS;
		final double zMax_core = zCenter + CORE_RADIUS;
		final double xMin_branch = xCenter - BRANCH_RADIUS;
		final double xMax_branch = xCenter + BRANCH_RADIUS;
		final double zMin_branch = zCenter - BRANCH_RADIUS;
		final double zMax_branch = zCenter + BRANCH_RADIUS;
		
		final double yMin_branch = y + 0.0D;
		final double yMin_core;
		final double yMax_core;
		switch (enumTransporterBeaconState) {
		default:
		case PACKED_INACTIVE:
			yMin_core = y + CORE_Y_MIN_PACKED_INACTIVE;
			yMax_core = y + CORE_Y_MAX_PACKED_INACTIVE;
			break;
		
		case PACKED_ACTIVE:
			yMin_core = y + CORE_Y_MIN_PACKED_ACTIVE;
			yMax_core = y + CORE_Y_MAX_PACKED_ACTIVE;
			break;
		
		case DEPLOYED_INACTIVE:
			yMin_core = y + CORE_Y_MIN_DEPLOYED_INACTIVE;
			yMax_core = y + CORE_Y_MAX_DEPLOYED_INACTIVE;
			break;
		
		case DEPLOYED_ACTIVE:
			yMin_core = y + CORE_Y_MIN_DEPLOYED_ACTIVE;
			yMax_core = y + CORE_Y_MAX_DEPLOYED_ACTIVE;
			break;
		}
		final double yMax_branch = y + BRANCH_HEIGHT;
		
		// add top face
		tessellator.addVertexWithUV(xMin_core  , yMax_core  , zMin_core  , uMin_top, vMin_top);
		tessellator.addVertexWithUV(xMin_core  , yMax_core  , zMax_core  , uMin_top, vMax_top);
		tessellator.addVertexWithUV(xMax_core  , yMax_core  , zMax_core  , uMax_top, vMax_top);
		tessellator.addVertexWithUV(xMax_core  , yMax_core  , zMin_core  , uMax_top, vMin_top);
		
		// add bottom face
		tessellator.addVertexWithUV(xMax_core  , yMin_core  , zMin_core  , uMax_bottom, vMin_bottom);
		tessellator.addVertexWithUV(xMax_core  , yMin_core  , zMax_core  , uMax_bottom, vMax_bottom);
		tessellator.addVertexWithUV(xMin_core  , yMin_core  , zMax_core  , uMin_bottom, vMax_bottom);
		tessellator.addVertexWithUV(xMin_core  , yMin_core  , zMin_core  , uMin_bottom, vMin_bottom);
		
		// add side/branch faces
		tessellator.addVertexWithUV(xMin_core  , yMax_branch, zMin_branch, uMin_side, vMin_side);
		tessellator.addVertexWithUV(xMin_core  , yMin_branch, zMin_branch, uMin_side, vMax_side);
		tessellator.addVertexWithUV(xMin_core  , yMin_branch, zMax_branch, uMax_side, vMax_side);
		tessellator.addVertexWithUV(xMin_core  , yMax_branch, zMax_branch, uMax_side, vMin_side);
		
		tessellator.addVertexWithUV(xMax_core  , yMax_branch, zMax_branch, uMin_side, vMin_side);
		tessellator.addVertexWithUV(xMax_core  , yMin_branch, zMax_branch, uMin_side, vMax_side);
		tessellator.addVertexWithUV(xMax_core  , yMin_branch, zMin_branch, uMax_side, vMax_side);
		tessellator.addVertexWithUV(xMax_core  , yMax_branch, zMin_branch, uMax_side, vMin_side);
		
		tessellator.addVertexWithUV(xMin_branch, yMax_branch, zMax_core  , uMin_side, vMin_side);
		tessellator.addVertexWithUV(xMin_branch, yMin_branch, zMax_core  , uMin_side, vMax_side);
		tessellator.addVertexWithUV(xMax_branch, yMin_branch, zMax_core  , uMax_side, vMax_side);
		tessellator.addVertexWithUV(xMax_branch, yMax_branch, zMax_core  , uMax_side, vMin_side);
		
		tessellator.addVertexWithUV(xMax_branch, yMax_branch, zMin_core  , uMin_side, vMin_side);
		tessellator.addVertexWithUV(xMax_branch, yMin_branch, zMin_core  , uMin_side, vMax_side);
		tessellator.addVertexWithUV(xMin_branch, yMin_branch, zMin_core  , uMax_side, vMax_side);
		tessellator.addVertexWithUV(xMin_branch, yMax_branch, zMin_core  , uMax_side, vMin_side);
	}
	
	@Override
	public boolean shouldRender3DInInventory(final int modelId) {
		return true;
	}
	
	@Override
	public int getRenderId() {
		return renderId;
	}
}
/**/