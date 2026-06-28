package io.github.simonxwei.lithome.mixin;

import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkAccess;
import io.github.simonxwei.lithome.world.level.chunk.LithomeChunkSection;
import io.github.simonxwei.lithome.world.level.lithome.Lithome;
import io.github.simonxwei.lithome.world.level.lithome.LithomeResolver;
import io.github.simonxwei.lithome.world.level.lithome.LithomeClimateSampler;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author simonxwei
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin implements LithomeChunkAccess {

    @Override
    public Holder<Lithome> lithome$getNoiseLithome(final int quartX, final int quartY, final int quartZ) {
        final ChunkAccess self = (ChunkAccess) (Object) this;
        try {
            final int quartMinY = QuartPos.fromBlock(self.getMinY());
            final int quartMaxY = quartMinY + QuartPos.fromBlock(self.getHeight()) - 1;
            final int clampedQuartY = Mth.clamp(quartY, quartMinY, quartMaxY);
            final int sectionIndex = self.getSectionIndex(QuartPos.toBlock(clampedQuartY));
            return ((LithomeChunkSection) self.getSection(sectionIndex)).lithome$getNoiseLithome(quartX & 3, clampedQuartY & 3, quartZ & 3);
        } catch (final Throwable throwable) {
            final CrashReport report = CrashReport.forThrowable(throwable, "Getting Lithome");
            final CrashReportCategory category = report.addCategory("Lithome being got");
            category.setDetail("Location", () -> CrashReportCategory.formatLocation(self, quartX, quartY, quartZ));
            throw new ReportedException(report);
        }
    }

    @Override
    public void lithome$fillLithomesFromNoise(final LithomeResolver resolver, final LithomeClimateSampler sampler) {
        final ChunkAccess self = (ChunkAccess) (Object) this;
        final ChunkPos chunkPos = self.getPos();
        final int quartMinX = QuartPos.fromBlock(chunkPos.getMinBlockX());
        final int quartMinZ = QuartPos.fromBlock(chunkPos.getMinBlockZ());
        final LevelHeightAccessor heightAccessor = self.getHeightAccessorForGeneration();

        for (int sectionY = heightAccessor.getMinSectionY(); sectionY <= heightAccessor.getMaxSectionY(); ++sectionY) {
            final LevelChunkSection section = self.getSection(self.getSectionIndexFromSectionY(sectionY));
            final int quartMinY = QuartPos.fromSection(sectionY);
            ((LithomeChunkSection) section).lithome$fillLithomesFromNoise(resolver, sampler, quartMinX, quartMinY, quartMinZ);
        }
    }
}
