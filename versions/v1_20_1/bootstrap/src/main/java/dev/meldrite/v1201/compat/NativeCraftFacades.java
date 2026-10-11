package dev.meldrite.v1201.compat;

import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.bridge.NativeWorldPort;

public final class NativeCraftFacades {
    private NativeCraftFacades() {}
    public static void install(FacadeContext context, NativeEntityPort entities, NativeWorldPort worlds) {
        context.installCreators(
                delegate -> {
                    net.minecraft.entity.Entity nativeEntity = entities.require(delegate.identity().handle());
                    return cached(nativeEntity, context, () -> {
                        if (nativeEntity instanceof net.minecraft.entity.decoration.ArmorStandEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftArmorStand(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.ItemEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.ChestBoatEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftChestBoat(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.BoatEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftBoat(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.ChestMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartChest(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.HopperMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartHopper(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.FurnaceMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartFurnace(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.TntMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartTNT(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.CommandBlockMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartCommand(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.SpawnerMinecartEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartMobSpawner(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.vehicle.AbstractMinecartEntity minecart) return minecart.getMinecartType() == net.minecraft.entity.vehicle.AbstractMinecartEntity.Type.RIDEABLE
                                ? new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartRideable(delegate, context, entities)
                                : new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecart(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.CaveSpiderEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCaveSpider(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.SpiderEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSpider(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.BlazeEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftBlaze(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.SilverfishEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSilverfish(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.GiantEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGiant(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.FallingBlockEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFallingBlock(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.ExperienceOrbEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftExperienceOrb(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.TntEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTNTPrimed(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.boss.dragon.EnderDragonPart) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderDragonPart(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderDragon(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.boss.WitherEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWither(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.CreeperEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCreeper(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.MagmaCubeEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMagmaCube(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.SlimeEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSlime(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ElderGuardianEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftElderGuardian(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.GuardianEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGuardian(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.PhantomEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPhantom(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.GhastEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGhast(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.EndermanEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderman(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.WardenEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWarden(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.HoglinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftHoglin(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.PiglinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPiglin(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.PiglinBruteEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPiglinBrute(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.EndermiteEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEndermite(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.AllayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAllay(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.BatEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftBat(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.VillagerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftVillager(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.WanderingTraderEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWanderingTrader(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ZoglinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftZoglin(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ZombieVillagerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftVillagerZombie(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.HuskEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftHusk(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.DrownedEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftDrowned(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ZombifiedPiglinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPigZombie(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ZombieEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftZombie(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.WitherSkeletonEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWitherSkeleton(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.StrayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftStray(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.SkeletonEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSkeleton(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.AbstractSkeletonEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAbstractSkeleton(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.VexEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftVex(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.EvokerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEvoker(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.IllusionerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftIllusioner(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.PillagerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPillager(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.VindicatorEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftVindicator(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.RavagerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftRavager(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.WitchEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWitch(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.IronGolemEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftIronGolem(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SnowGolemEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSnowman(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ShulkerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftShulker(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.CodEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCod(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.TadpoleEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTadpole(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SalmonEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSalmon(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.TropicalFishEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTropicalFish(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.PufferfishEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPufferFish(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.GlowSquidEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGlowSquid(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SquidEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSquid(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.DolphinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftDolphin(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SchoolingFishEntity) return new io.papermc.paper.entity.PaperSchoolableFish(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.FishEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFish(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.WaterCreatureEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWaterMob(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.ChickenEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftChicken(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.MooshroomEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMushroomCow(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.PolarBearEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPolarBear(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.StriderEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftStrider(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.CowEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCow(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.PigEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPig(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SheepEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSheep(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.WolfEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWolf(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.CatEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCat(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.OcelotEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftOcelot(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.ParrotEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftParrot(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.RabbitEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftRabbit(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.PandaEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPanda(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.FoxEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFox(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.BeeEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftBee(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.GoatEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGoat(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.AxolotlEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAxolotl(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.FrogEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFrog(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.TurtleEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTurtle(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.SnifferEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSniffer(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.TraderLlamaEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTraderLlama(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.LlamaEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLlama(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.HorseEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftHorse(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.DonkeyEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftDonkey(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.MuleEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMule(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.SkeletonHorseEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSkeletonHorse(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.ZombieHorseEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftZombieHorse(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.CamelEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCamel(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.AbstractDonkeyEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftChestedHorse(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.AbstractHorseEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAbstractHorse(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.TameableEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTameableAnimal(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.MerchantEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAbstractVillager(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.AbstractPiglinEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPiglinAbstract(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.AmbientEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAmbient(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.FlyingEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFlying(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.AnimalEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAnimals(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.PassiveEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAgeable(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.passive.GolemEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGolem(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.PathAwareEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftCreature(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.MobEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.ShulkerBulletEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftShulkerBullet(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.LlamaSpitEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLlamaSpit(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.DragonFireballEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftDragonFireball(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.SmallFireballEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSmallFireball(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.WitherSkullEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftWitherSkull(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.FireballEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLargeFireball(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.TridentEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTrident(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.SpectralArrowEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSpectralArrow(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.FireworkRocketEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFirework(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.FishingBobberEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftFishHook(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.thrown.PotionEntity potion) {
                            var item = potion.getStack().getItem();
                            if (item == net.minecraft.item.Items.LINGERING_POTION) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLingeringPotion(delegate, context, entities);
                            if (item == net.minecraft.item.Items.SPLASH_POTION) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSplashPotion(delegate, context, entities);
                            return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftThrownPotion(delegate, context, entities);
                        }
                        if (nativeEntity instanceof net.minecraft.entity.projectile.thrown.SnowballEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftSnowball(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.thrown.EggEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEgg(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.thrown.EnderPearlEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderPearl(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.thrown.ExperienceBottleEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftThrownExpBottle(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.ArrowEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTippedArrow(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftBlockDisplay(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftItemDisplay(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftTextDisplay(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.InteractionEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftInteraction(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.GlowItemFrameEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftGlowItemFrame(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.ItemFrameEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftItemFrame(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.painting.PaintingEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPainting(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.LeashKnotEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLeash(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.MarkerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftMarker(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.AreaEffectCloudEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftAreaEffectCloud(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.mob.EvokerFangsEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEvokerFangs(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.LightningEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLightningStrike(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.DisplayEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftDisplay(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.AbstractDecorationEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftHanging(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.decoration.EndCrystalEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderCrystal(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.EyeOfEnderEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderSignal(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.projectile.ProjectileEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftProjectile(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.player.PlayerEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity(delegate, context, entities);
                        if (nativeEntity instanceof net.minecraft.entity.LivingEntity) return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity(delegate, context, entities);
                        return new org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity(delegate, context, entities);
                    });
                },
                delegate -> cached(entities.require(delegate.identity().handle()), context,
                        () -> new org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer(delegate, context, entities)),
                delegate -> new org.bukkit.craftbukkit.v1_20_R1.CraftWorld(delegate, context, worlds));
        context.installBlockCreator(delegate -> new org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock(delegate, context, worlds));
        NativeEntityExtensions.bind(context, entities);
    }
    private static org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity cached(net.minecraft.entity.Entity nativeEntity,
            FacadeContext context, java.util.function.Supplier<org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity> creator) {
        var state = NativeEntityExtensions.state(nativeEntity);
        var existing = state.meldrite$getCachedBukkitEntity();
        if (existing != null && existing.meldrite$isCurrentFor(context, nativeEntity)) return existing;
        var entity = creator.get();
        state.meldrite$setCachedBukkitEntity(entity);
        return entity;
    }
}
