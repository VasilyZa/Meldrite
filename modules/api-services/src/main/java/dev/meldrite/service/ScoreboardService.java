package dev.meldrite.service;
import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
/** Detached, owned scoreboard state; native display/packet effects are separate bridge work. */
public final class ScoreboardService implements AutoCloseable {
    public record Snapshot(Map<String,Map<String,Integer>> scores,Map<String,Set<String>> teams){public Snapshot{var scoreCopy=new LinkedHashMap<String,Map<String,Integer>>();scores.forEach((name,values)->scoreCopy.put(name,Map.copyOf(values)));scores=Collections.unmodifiableMap(scoreCopy);var teamCopy=new LinkedHashMap<String,Set<String>>();teams.forEach((name,values)->teamCopy.put(name,Set.copyOf(values)));teams=Collections.unmodifiableMap(teamCopy);}}
    private final ExecutionPort execution;private final Set<Board> boards=Collections.newSetFromMap(new IdentityHashMap<>());private boolean closed;
    public ScoreboardService(ExecutionPort execution){this.execution=Objects.requireNonNull(execution);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("PRIMARY_THREAD_REQUIRED");}
    private void open(){if(closed)throw new IllegalStateException("SCOREBOARD_SERVICE_CLOSED");}
    private static String name(String name){Objects.requireNonNull(name);if(name.isEmpty()||name.length()>32767)throw new IllegalArgumentException("Invalid scoreboard name/entry");return name;}
    public synchronized Board create(PluginScope owner){primary();open();var board=new Board(Objects.requireNonNull(owner));owner.register(board);boards.add(board);return board;}
    public synchronized int ownedRegistrations(){return boards.stream().mapToInt(board->1+board.objectives.size()+board.teams.size()).sum();}
    public final class Board implements AutoCloseable {
        private final PluginScope owner;private final Map<String,Objective> objectives=new LinkedHashMap<>();private final Map<String,Team> teams=new LinkedHashMap<>();private final Map<String,Team> memberships=new LinkedHashMap<>();private boolean removed;
        private Board(PluginScope owner){this.owner=owner;}
        public PluginScope owner(){return owner;}
        private void alive(){open();if(removed||owner.isClosed())throw new IllegalStateException("SCOREBOARD_CLOSED");}
        public Objective registerObjective(String name,String criteria){synchronized(ScoreboardService.this){primary();alive();name=ScoreboardService.name(name);Objects.requireNonNull(criteria);if(!criteria.equals("dummy"))throw new UnsupportedOperationException("UNSUPPORTED_SCOREBOARD_CRITERIA: "+criteria);if(objectives.containsKey(name))throw new IllegalArgumentException("DUPLICATE_OBJECTIVE");var value=new Objective(this,name);objectives.put(name,value);return value;}}
        public Team registerTeam(String name){synchronized(ScoreboardService.this){primary();alive();name=ScoreboardService.name(name);if(teams.containsKey(name))throw new IllegalArgumentException("DUPLICATE_TEAM");var value=new Team(this,name);teams.put(name,value);return value;}}
        public Optional<Objective> objective(String name){synchronized(ScoreboardService.this){alive();return Optional.ofNullable(objectives.get(ScoreboardService.name(name)));}}
        public Optional<Team> team(String name){synchronized(ScoreboardService.this){alive();return Optional.ofNullable(teams.get(ScoreboardService.name(name)));}}
        public List<Objective> objectives(){synchronized(ScoreboardService.this){alive();return List.copyOf(objectives.values());}}
        public List<Objective> objectivesByCriteria(String criteria){synchronized(ScoreboardService.this){alive();Objects.requireNonNull(criteria);return criteria.equals("dummy")?List.copyOf(objectives.values()):List.of();}}
        public List<Objective> scoredObjectives(String entry){synchronized(ScoreboardService.this){alive();ScoreboardService.name(entry);return objectives.values().stream().filter(objective->objective.scores.containsKey(entry)).toList();}}
        public List<Team> teams(){synchronized(ScoreboardService.this){alive();return List.copyOf(teams.values());}}
        public Optional<Team> entryTeam(String entry){synchronized(ScoreboardService.this){alive();return Optional.ofNullable(memberships.get(ScoreboardService.name(entry)));}}
        public Set<String> entries(){synchronized(ScoreboardService.this){alive();var entries=new LinkedHashSet<>(memberships.keySet());objectives.values().forEach(objective->entries.addAll(objective.scores.keySet()));return Collections.unmodifiableSet(entries);}}
        public void resetScores(String entry){synchronized(ScoreboardService.this){primary();alive();ScoreboardService.name(entry);objectives.values().forEach(objective->objective.scores.remove(entry));}}
        public Snapshot snapshot(){synchronized(ScoreboardService.this){alive();var scores=new LinkedHashMap<String,Map<String,Integer>>();objectives.forEach((key,value)->scores.put(key,value.scores));var groups=new LinkedHashMap<String,Set<String>>();teams.forEach((key,value)->groups.put(key,value.entries));return new Snapshot(scores,groups);}}
        public void close(){synchronized(ScoreboardService.this){if(removed)return;removed=true;objectives.values().forEach(value->{value.removed=true;value.scores.clear();});teams.values().forEach(value->{value.removed=true;value.entries.clear();});objectives.clear();teams.clear();memberships.clear();boards.remove(this);}}
    }
    public final class Objective {
        private final Board board;private final String name;private final Map<String,Integer> scores=new LinkedHashMap<>();private boolean removed;
        private Objective(Board board,String name){this.board=board;this.name=name;}
        private void alive(){board.alive();if(removed)throw new IllegalStateException("OBJECTIVE_UNREGISTERED");}
        public boolean attached(){synchronized(ScoreboardService.this){return !closed&&!removed&&!board.removed&&!board.owner.isClosed();}}
        public String name(){synchronized(ScoreboardService.this){alive();return name;}}
        public String criteria(){synchronized(ScoreboardService.this){alive();return "dummy";}}
        public boolean modifiable(){synchronized(ScoreboardService.this){alive();return true;}}
        public int score(String entry){synchronized(ScoreboardService.this){alive();return scores.getOrDefault(ScoreboardService.name(entry),0);}}
        public boolean scoreSet(String entry){synchronized(ScoreboardService.this){alive();return scores.containsKey(ScoreboardService.name(entry));}}
        public void score(String entry,int value){synchronized(ScoreboardService.this){primary();alive();scores.put(ScoreboardService.name(entry),value);}}
        public void reset(String entry){synchronized(ScoreboardService.this){primary();alive();scores.remove(ScoreboardService.name(entry));}}
        public void unregister(){synchronized(ScoreboardService.this){primary();alive();removed=true;scores.clear();board.objectives.remove(name,this);}}
    }
    public final class Team {
        private final Board board;private final String name;private final Set<String> entries=new LinkedHashSet<>();private boolean removed;
        private Team(Board board,String name){this.board=board;this.name=name;}
        private void alive(){board.alive();if(removed)throw new IllegalStateException("TEAM_UNREGISTERED");}
        public boolean attached(){synchronized(ScoreboardService.this){return !closed&&!removed&&!board.removed&&!board.owner.isClosed();}}
        public String name(){synchronized(ScoreboardService.this){alive();return name;}}
        public Set<String> entries(){synchronized(ScoreboardService.this){alive();return Collections.unmodifiableSet(new LinkedHashSet<>(entries));}}
        public boolean has(String entry){synchronized(ScoreboardService.this){alive();return entries.contains(ScoreboardService.name(entry));}}
        public void add(String entry){synchronized(ScoreboardService.this){primary();alive();entry=ScoreboardService.name(entry);Team previous=board.memberships.put(entry,this);if(previous!=null)previous.entries.remove(entry);entries.add(entry);}}
        public boolean remove(String entry){synchronized(ScoreboardService.this){primary();alive();entry=ScoreboardService.name(entry);boolean changed=entries.remove(entry);board.memberships.remove(entry,this);return changed;}}
        public void unregister(){synchronized(ScoreboardService.this){primary();alive();removed=true;for(String entry:entries)board.memberships.remove(entry,this);entries.clear();board.teams.remove(name,this);}}
    }
    public void close(){synchronized(this){if(closed)return;closed=true;List.copyOf(boards).forEach(Board::close);}}
}
