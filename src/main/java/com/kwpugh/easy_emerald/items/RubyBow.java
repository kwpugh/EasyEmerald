package com.kwpugh.easy_emerald.items;


import java.util.function.Predicate;

import com.kwpugh.easy_emerald.init.ItemInit;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class RubyBow extends BowItem
{
   public RubyBow(Item.Properties builder)
   {
      super(builder);
   }

   public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft)
   {
      if (entityLiving instanceof PlayerEntity)
      {
         PlayerEntity playerentity = (PlayerEntity)entityLiving;
         boolean flag = playerentity.abilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
         ItemStack itemstack = playerentity.findAmmo(stack);

         int i = this.getUseDuration(stack) - timeLeft;
         i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, playerentity, i, !itemstack.isEmpty() || flag);
         
         if (i < 0) return;

         if (!itemstack.isEmpty() || flag)
         {
            if (itemstack.isEmpty())
            {
               itemstack = new ItemStack(Items.ARROW);
            }

            float f = getArrowVelocity(i);
            if (!((double)f < 0.1D))
            {
               boolean flag1 = playerentity.abilities.isCreativeMode || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, stack, playerentity));
               if (!worldIn.isRemote)
               {
                  ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                  AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, itemstack, playerentity);
                  abstractarrowentity = customeArrow(abstractarrowentity);
                  abstractarrowentity.setDirectionAndMovement(playerentity, playerentity.rotationPitch, playerentity.rotationYaw, 0.0F, f * 4.5F, 1.0F);
                  if (f == 1.0F)
                  {
                     abstractarrowentity.setIsCritical(true);
                  }

                  int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
                  if (j > 0)
                  {
                     abstractarrowentity.setDamage(abstractarrowentity.getDamage() + (double)j * 0.75D + 0.75D);
                  }

                  int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
                  if (k > 0)
                  {
                     abstractarrowentity.setKnockbackStrength(k);
                  }

                  if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
                  {
                     abstractarrowentity.setFire(100);
                  }

                  stack.damageItem(1, playerentity, (p_220009_1_) -> {
                     p_220009_1_.sendBreakAnimation(playerentity.getActiveHand());
                  });
                  if (flag1 || playerentity.abilities.isCreativeMode && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW))
                  {
                     abstractarrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                  }

                  worldIn.addEntity(abstractarrowentity);
               }

               worldIn.playSound((PlayerEntity)null, playerentity.getPosX(), playerentity.getPosY(), playerentity.getPosZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if (!flag1 && !playerentity.abilities.isCreativeMode)
               {
                  itemstack.shrink(1);
                  if (itemstack.isEmpty())
                  {
                     playerentity.inventory.deleteStack(itemstack);
                  }
               }

               playerentity.addStat(Stats.ITEM_USED.get(this));
            }
         }
      }
   }

   public static float getArrowVelocity(int charge)
   {
      float f = (float)charge / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F)
      {
         f = 1.0F;
      }

      return f;
   }

   public int getUseDuration(ItemStack stack)
   {
      return 54000;
   }

   public UseAction getUseAction(ItemStack stack)
   {
      return UseAction.BOW;
   }

   public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
   {
      ItemStack itemstack = playerIn.getHeldItem(handIn);
      boolean flag = !playerIn.findAmmo(itemstack).isEmpty();

      ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
      if (ret != null) return ret;

      if (!playerIn.abilities.isCreativeMode && !flag)
      {
         return ActionResult.resultFail(itemstack);
      }
      else
      {
         playerIn.setActiveHand(handIn);
         return ActionResult.resultConsume(itemstack);
      }
   }

   public Predicate<ItemStack> getInventoryAmmoPredicate() {
      return ARROWS;
   }

   public AbstractArrowEntity customeArrow(AbstractArrowEntity arrow) {
      return arrow;
   }
   
   @Override
   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
   {
	   return repair.getItem() == ItemInit.RUBY.get();
   }
}