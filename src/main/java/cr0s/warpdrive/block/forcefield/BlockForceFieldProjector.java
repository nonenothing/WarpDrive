package cr0s.warpdrive.block.forcefield;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumForceFieldShape;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.item.ItemForceFieldShape;
import cr0s.warpdrive.item.ItemForceFieldUpgrade;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockForceFieldProjector extends BlockAbstractForceField {
	
	public BlockForceFieldProjector(final String registryName, final byte tier) {
		super(tier, Material.IRON);
		isRotating = true;
		setUnlocalizedName("warpdrive.forcefield.projector" + tier);
		setRegistryName(registryName);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlockForceFieldProjector(this));
		GameRegistry.registerTileEntity(TileEntityForceFieldProjector.class, WarpDrive.PREFIX + registryName);
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(@Nonnull Item item, CreativeTabs creativeTab, List<ItemStack> list) {
		for (int i = 0; i < 2; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		// TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(blockPos);
		// return tileEntityForceFieldProjector.isDoubleSided ? 1 : 0;
		return 0;	// @TODO MC1.10 drop double sided projector
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLiving, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLiving, itemStack);
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector)world.getTileEntity(blockPos);
		if (!itemStack.hasTagCompound()) {
			tileEntityForceFieldProjector.isDoubleSided = (itemStack.getItemDamage() == 1);
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer, EnumHand hand, @Nullable ItemStack itemStackHeld, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (!(tileEntity instanceof TileEntityForceFieldProjector)) {
			return false;
		}
		TileEntityForceFieldProjector tileEntityForceFieldProjector = (TileEntityForceFieldProjector) tileEntity;
		int metadata = blockState.getBlock().getMetaFromState(blockState);
		
		EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.NONE;
		if (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {
			enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStackHeld.getItemDamage());
		}
		
		// sneaking with an empty hand or an upgrade/shape item in hand to dismount current upgrade/shape
		if (entityPlayer.isSneaking()) {
			// using an upgrade item or no shape defined means dismount upgrade, otherwise dismount shape
			if ( (itemStackHeld != null && itemStackHeld.getItem() instanceof ItemForceFieldUpgrade)
			  || (tileEntityForceFieldProjector.getShape() == EnumForceFieldShape.NONE)
			/*  || (side != (metadata & 7) && (!tileEntityForceFieldProjector.isDoubleSided || EnumFacing.OPPOSITES[side] != (metadata & 7))) /* @TODO MC1.10 projector double sided */ ) {
				// find a valid upgrade to dismount
				if (!tileEntityForceFieldProjector.hasUpgrade(enumForceFieldUpgrade)) {
					enumForceFieldUpgrade = (EnumForceFieldUpgrade)tileEntityForceFieldProjector.getFirstUpgradeOfType(EnumForceFieldUpgrade.class, EnumForceFieldUpgrade.NONE);
				}
				
				if (enumForceFieldUpgrade == EnumForceFieldUpgrade.NONE) {
					// no more upgrades to dismount
					WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.noUpgradeToDismount"));
					return true;
				}
				
				if (!entityPlayer.capabilities.isCreativeMode) {
					// dismount the current upgrade item
					ItemStack itemStackDrop = ItemForceFieldUpgrade.getItemStackNoCache(enumForceFieldUpgrade, 1);
					EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
					entityItem.setNoPickupDelay();
					world.spawnEntityInWorld(entityItem);
				}
				
				tileEntityForceFieldProjector.dismountUpgrade(enumForceFieldUpgrade);
				// upgrade dismounted
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.dismounted"));
				return false;
				
			} else {// default to dismount shape
				if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
					if (side == EnumFacing.UP) { /* side == (metadata & 7) || (tileEntityForceFieldProjector.isDoubleSided && EnumFacing.OPPOSITES[side] == (metadata & 7))) { /* @TODO MC1.10 projector double sided */
						if (!entityPlayer.capabilities.isCreativeMode) {
							// dismount the shape item(s)
							ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
							EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
							entityItem.setNoPickupDelay();
							world.spawnEntityInWorld(entityItem);
						}
						
						tileEntityForceFieldProjector.setShape(EnumForceFieldShape.NONE);
						// shape dismounted
						WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.shape.result.dismounted"));
					} else {
						// wrong side
						WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.shape.result.wrongSide"));
						return true;
					}
					/* @TODO MC1.10 projector double sided */
				} else {
					// no shape to dismount
					WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.shape.result.noShapeToDismount"));
					return true;
				}
			}
			
		} else if (itemStackHeld == null) {// no sneaking and no item in hand => show status
			WarpDrive.addChatMessage(entityPlayer, tileEntityForceFieldProjector.getStatus());
			return true;
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldShape) {// no sneaking and shape in hand => mounting a shape
			if (side == EnumFacing.UP) { /* side == (metadata & 7) || (((TileEntityForceFieldProjector) tileEntity).isDoubleSided && EnumFacing.OPPOSITES[side] == (metadata & 7))) { /* @TODO MC1.10 projector double sided */
				if (!entityPlayer.capabilities.isCreativeMode) {
					// validate quantity
					if (itemStackHeld.stackSize < (tileEntityForceFieldProjector.isDoubleSided ? 2 : 1)) {
						// not enough shape items
						WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation(
							tileEntityForceFieldProjector.isDoubleSided ?
								"warpdrive.forcefield.shape.result.notEnoughShapes.double" : "warpdrive.forcefield.shape.result.notEnoughShapes.single"));
						return true;
					}
					
					// update player inventory
					itemStackHeld.stackSize -= tileEntityForceFieldProjector.isDoubleSided ? 2 : 1;
					
					// dismount the current shape item(s)
					if (tileEntityForceFieldProjector.getShape() != EnumForceFieldShape.NONE) {
						ItemStack itemStackDrop = ItemForceFieldShape.getItemStackNoCache(tileEntityForceFieldProjector.getShape(), tileEntityForceFieldProjector.isDoubleSided ? 2 : 1);
						EntityItem entityItem = new EntityItem(world, entityPlayer.posX, entityPlayer.posY + 0.5D, entityPlayer.posZ, itemStackDrop);
						entityItem.setNoPickupDelay();
						world.spawnEntityInWorld(entityItem);
					}
				}
				
				// mount the new shape item(s)
				tileEntityForceFieldProjector.setShape(EnumForceFieldShape.get(itemStackHeld.getItemDamage()));
				// shape mounted
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.shape.result.mounted"));
				
			} else {
				// wrong side
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.shape.result.wrongSide"));
				return true;
			}
			
		} else if (itemStackHeld.getItem() instanceof ItemForceFieldUpgrade) {// no sneaking and an upgrade in hand => mounting an upgrade
			// validate type
			if (tileEntityForceFieldProjector.getUpgradeMaxCount(enumForceFieldUpgrade) <= 0) {
				// invalid upgrade type
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.invalidProjectorUpgrade"));
				return true;
			}
			if (!tileEntityForceFieldProjector.canUpgrade(enumForceFieldUpgrade)) {
				// too many upgrades
				WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.tooManyUpgrades",
					tileEntityForceFieldProjector.getUpgradeMaxCount(enumForceFieldUpgrade)));
				return true;
			}
			
			if (!entityPlayer.capabilities.isCreativeMode) {
				// validate quantity
				if (itemStackHeld.stackSize < 1) {
					// not enough upgrade items
					WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.notEnoughUpgrades"));
					return true;
				}
				
				// update player inventory
				itemStackHeld.stackSize -= 1;
			}
			
			// mount the new upgrade item
			tileEntityForceFieldProjector.mountUpgrade(enumForceFieldUpgrade);
			// upgrade mounted
			WarpDrive.addChatMessage(entityPlayer, new TextComponentTranslation("warpdrive.forcefield.upgrade.result.mounted"));
		}
		
		return false;
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new TileEntityForceFieldProjector();
	}
}
