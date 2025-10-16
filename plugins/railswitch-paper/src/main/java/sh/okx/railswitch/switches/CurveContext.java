package sh.okx.railswitch.switches;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;

/**
 * Represents the context of a curved rail switch, including the curve block and its possible shapes.
 */
public final class CurveContext {
    public final Block curve;
    public final BlockFace incoming;
    public final Rail.Shape off_shape;
    public final Rail.Shape on_shape;

    /**
     * Creates a new curve context.
     *
     * @param curve The curved rail block
     * @param incoming The direction the minecart is coming from
     * @param off_shape The rail shape when not powered
     * @param on_shape The rail shape when powered
     */
    public CurveContext(Block curve, BlockFace incoming, Rail.Shape off_shape, Rail.Shape on_shape) {
        this.curve = curve;
        this.incoming = incoming;
        this.off_shape = off_shape;
        this.on_shape = on_shape;
    }
}