package dev.meldrite.service;
import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.foundation.*;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.function.BooleanSupplier;
/** One scoped recipe catalog and preview/decision/whole-container commit path. */
public final class RecipeService implements AutoCloseable {
    public record Ingredient(ResourceKey type,int count){public Ingredient{Objects.requireNonNull(type);if(count<=0)throw new IllegalArgumentException("Nonpositive ingredient count");}}
    public record Definition(ResourceKey key,Map<Integer,Ingredient> ingredients,ItemSnapshot result){public Definition{Objects.requireNonNull(key);ingredients=Map.copyOf(ingredients);Objects.requireNonNull(result);if(ingredients.isEmpty()||result.count()<=0)throw new IllegalArgumentException("Empty recipe");if(ingredients.keySet().stream().anyMatch(slot->slot<0))throw new IllegalArgumentException("Negative recipe slot");}}
    private final ExecutionPort execution;private final InventoryService inventory;private final Map<ResourceKey,Registration> recipes=new LinkedHashMap<>();private boolean closed;
    public RecipeService(ExecutionPort execution,InventoryService inventory){this.execution=Objects.requireNonNull(execution);this.inventory=Objects.requireNonNull(inventory);}
    private void open(){if(closed)throw new IllegalStateException("RECIPE_SERVICE_CLOSED");}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("PRIMARY_THREAD_REQUIRED");}
    public final class Registration implements AutoCloseable {
        private final Definition definition;private final PluginScope scope;private volatile boolean removed;
        private Registration(Definition definition,PluginScope scope){this.definition=definition;this.scope=scope;}
        public boolean isClosed(){return removed||scope.isClosed();}
        public Definition definition(){synchronized(RecipeService.this){open();if(isClosed())throw new IllegalStateException("RECIPE_REMOVED");return definition;}}
        public void close(){synchronized(RecipeService.this){if(removed)return;removed=true;recipes.remove(definition.key(),this);}}
    }
    public synchronized Registration register(PluginScope scope,Definition definition){primary();open();Objects.requireNonNull(scope);Objects.requireNonNull(definition);if(recipes.containsKey(definition.key()))throw new IllegalArgumentException("DUPLICATE_RECIPE");var registration=new Registration(definition,scope);scope.register(registration);recipes.put(definition.key(),registration);return registration;}
    public synchronized Optional<Registration> resolve(ResourceKey key){open();var registration=recipes.get(Objects.requireNonNull(key));return registration==null||registration.isClosed()?Optional.empty():Optional.of(registration);}
    public Preview preview(EntityService.Identity identity,ResourceKey key,int resultSlot)throws Exception {
        primary();inventory.requireWritable(identity);Registration registration=resolve(key).orElseThrow(()->new IllegalArgumentException("UNKNOWN_RECIPE: "+key));var recipe=registration.definition();var before=inventory.snapshot(identity);if(resultSlot<0||resultSlot>=before.size())throw new IndexOutOfBoundsException("Result slot");if(recipe.ingredients().containsKey(resultSlot))throw new IllegalArgumentException("Result slot is an ingredient");if(before.get(resultSlot).count()!=0)throw new IllegalStateException("RESULT_SLOT_OCCUPIED");var after=new ArrayList<>(before);
        for(var entry:recipe.ingredients().entrySet()){int slot=entry.getKey();if(slot>=before.size())throw new IndexOutOfBoundsException("Ingredient slot");var item=before.get(slot);var need=entry.getValue();if(!item.type().equals(need.type())||item.count()<need.count())throw new IllegalArgumentException("RECIPE_MISMATCH");after.set(slot,inventory.resize(item,item.count()-need.count()));}
        for(int slot=0;slot<before.size();slot++)if(slot!=resultSlot&&!recipe.ingredients().containsKey(slot)&&before.get(slot).count()!=0)throw new IllegalArgumentException("EXTRA_RECIPE_INGREDIENT");after.set(resultSlot,recipe.result());return new Preview(identity,registration,resultSlot,before,List.copyOf(after));
    }
    public final class Preview {
        private final EntityService.Identity identity;private final Registration registration;private final int resultSlot;private final List<ItemSnapshot> before,after;private boolean attempted;
        private Preview(EntityService.Identity identity,Registration registration,int resultSlot,List<ItemSnapshot> before,List<ItemSnapshot> after){this.identity=identity;this.registration=registration;this.resultSlot=resultSlot;this.before=before;this.after=after;}
        public Registration recipe(){inventory.snapshot(identity);registration.definition();return registration;}
        public List<ItemSnapshot> contents(){recipe();var view=new ArrayList<>(before);view.set(resultSlot,registration.definition().result());return List.copyOf(view);}
        public List<ItemSnapshot> matrix(){var view=contents();var matrix=new ArrayList<ItemSnapshot>();for(int slot=0;slot<view.size();slot++)if(slot!=resultSlot)matrix.add(view.get(slot));return List.copyOf(matrix);}
        public ItemSnapshot result(){return recipe().definition().result();}
        public boolean commit(BooleanSupplier cancelled)throws Exception {
            primary();inventory.requireWritable(identity);Objects.requireNonNull(cancelled);synchronized(this){if(attempted)throw new IllegalStateException("CRAFT_ALREADY_DECIDED");attempted=true;}
            registration.definition();if(cancelled.getAsBoolean())return false;registration.definition();inventory.commit(identity,before,after);return true;
        }
    }
    public void close(){List<Registration> active;synchronized(this){if(closed)return;closed=true;active=List.copyOf(recipes.values());recipes.clear();}active.forEach(Registration::close);}
}
