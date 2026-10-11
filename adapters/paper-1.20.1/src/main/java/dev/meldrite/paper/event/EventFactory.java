package dev.meldrite.paper.event;
import dev.meldrite.bridge.*;
import dev.meldrite.foundation.*;
import dev.meldrite.event.EventCoordinator.UnrepresentableInput;
import dev.meldrite.paper.registry.MaterialProjection;
import dev.meldrite.registry.ProjectionResult;
import java.util.*;
import java.util.function.Function;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
/** Profile owns event construction; neutral inputs are resolved through live facade projections. */
public final class EventFactory {
    private final Function<EntityHandle,ProjectionResult<Player>> players;private final Function<BlockPosition,ProjectionResult<Block>> blocks;private final MaterialProjection materials;
    public EventFactory(Function<EntityHandle,ProjectionResult<Player>> players,Function<BlockPosition,ProjectionResult<Block>> blocks,MaterialProjection materials){this.players=Objects.requireNonNull(players);this.blocks=Objects.requireNonNull(blocks);this.materials=Objects.requireNonNull(materials);}
    private <T>T exact(ProjectionResult<T> result,String label){if(result instanceof ProjectionResult.Exact<T> exact)return exact.value();throw new UnrepresentableInput(label+": "+result);}
    public Event create(ActionSnapshot snapshot){
        if(!snapshot.kind().equals("block-break"))throw new UnrepresentableInput("UNSUPPORTED_ACTION_EVENT: "+snapshot.kind());
        var input=snapshot.blockBreak().orElseThrow(()->new UnrepresentableInput("Missing typed block-break input"));
        var material=exact(materials.block(input.blockType()),"block type");
        Player player=exact(players.apply(input.actor()),"player");Block block=exact(blocks.apply(input.block()),"block");
        if(!player.getUniqueId().equals(input.actor().uuid())||block.getX()!=input.block().x()||block.getY()!=input.block().y()||block.getZ()!=input.block().z()||block.getType()!=material)throw new UnrepresentableInput("Facade identity does not match the action");
        var event=new BlockBreakEvent(block,player);event.setExpToDrop(input.experience());event.setDropItems(input.dropItems());return event;
    }
    public ActionDecision decision(Event event){if(event instanceof BlockBreakEvent block)return ActionDecision.blockBreak(block.isCancelled(),block.getExpToDrop(),block.isDropItems());throw new UnrepresentableInput("No native decision binding for "+event.getClass().getName());}
}
