package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;

/** Every server obligation receives a named domain; unmatched symbols remain an explicit S7h work item. */
public final class CapabilityWorklist {
    public record Assignment(String batch,String owner,String capability,String adapterFile) {}
    private CapabilityWorklist() {}
    public static Assignment assign(JsonNode symbol){
        String owner=symbol.path("owner").asText(),name=symbol.path("name").asText();String batch,service,capability;
        if(owner.contains("/permissions/")||owner.endsWith("/Permissible")||owner.endsWith("/ServerOperator")){batch="S7a";service="PermissionService";capability="permissions";}
        else if(owner.contains("/messaging/")||owner.contains("/ServicesManager")||owner.endsWith("/RegisteredServiceProvider")){batch="S7b";service="ServiceDirectory/PluginMessageService";capability="services-messaging";}
        else if(owner.contains("/configuration/")||owner.contains("/serialization/")){batch="S7c";service="ConfigurationService";capability="configuration";}
        else if(owner.contains("/persistence/")||owner.contains("/inventory/meta/")||owner.endsWith("/ItemStack")){batch="S7d";service="PersistentDataService/InventoryService";capability="pdc-itemmeta";}
        else if(owner.contains("/inventory/")){batch="S7e";service="RecipeService/InventoryService";capability="recipe-inventory";}
        else if(owner.contains("/scoreboard/")){batch="S7f";service="ScoreboardService";capability="scoreboard";}
        else if(owner.contains("/entity/")||owner.contains("/World")||owner.contains("/Chunk")||owner.contains("/block/")){batch="S7g";service="WorldService/EntityService";capability="world-entity";}
        else if(owner.contains("/scheduler/")){batch="S5";service="TickScheduler/AsyncScheduler";capability="scheduler";}
        else if(owner.contains("/command/")){batch="S6";service="CommandRegistry";capability="commands";}
        else if(owner.contains("/event/")){batch="S4";service="EventCoordinator";capability="events";}
        else if(owner.endsWith("/Registry")){batch="S3";service="RegistryCatalog";capability="registry";}
        else if(owner.contains("/plugin/")){batch="R3";service="PluginRuntime";capability="plugin-lifecycle";}
        else if(name.toLowerCase(Locale.ROOT).contains("message")||name.toLowerCase(Locale.ROOT).contains("audience")){batch="S2";service="AudienceService";capability="adventure";}
        else {batch="S7h";service="StatisticsService/MapService/RemainingApiServices";capability="remaining-api";}
        String adapter="adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/generated/"+owner.replace('$','_')+"Adapter.java";
        return new Assignment(batch,service,capability,adapter);
    }
    public static ObjectNode create(JsonNode inventory,JsonNode bindings){
        ObjectNode result=ApiInventory.JSON.createObjectNode().put("schemaVersion",1);ObjectNode batches=result.putObject("batches");
        Set<String> assigned=new HashSet<>();
        for(JsonNode symbol:inventory.path("symbols")){
            if(!symbol.path("requiresImplementation").asBoolean())continue;
            String id=symbol.path("id").asText();Assignment assignment=assign(symbol);
            ObjectNode batch=batches.has(assignment.batch())?(ObjectNode)batches.get(assignment.batch()):batches.putObject(assignment.batch());
            if(!batch.has("symbols")){batch.put("owner",assignment.owner());batch.putArray("symbols");batch.putArray("prerequisites").add("F3").add("S1");}
            ((ArrayNode)batch.get("symbols")).addObject().put("symbolId",id).put("adapterFile",bindings.path("entries").path(id).path("adapterFile").asText()).put("serviceOwner",assignment.owner()).put("scenarioId",assignment.batch()+":"+id).put("status","UNSUPPORTED");
            if(!assigned.add(id))throw new IllegalStateException("DUPLICATE_WORK_ITEM: "+id);
        }
        result.put("serverObligations",assigned.size());return result;
    }
}
