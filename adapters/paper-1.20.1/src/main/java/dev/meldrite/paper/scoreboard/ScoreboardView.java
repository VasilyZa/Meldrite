package dev.meldrite.paper.scoreboard;
import dev.meldrite.service.ScoreboardService;
import dev.meldrite.paper.generated.org.bukkit.scoreboard.*;
import java.util.*;
import org.bukkit.scoreboard.*;
/** Identity-preserving thin projection; state rules reside only in ScoreboardService. */
public final class ScoreboardView {
    public final ScoreboardService.Board state;private final ScoreboardAdapter board;
    private final Map<ScoreboardService.Objective,ObjectiveAdapter> objectives=new IdentityHashMap<>();private final Map<ScoreboardService.Team,TeamAdapter> teams=new IdentityHashMap<>();
    public ScoreboardView(ScoreboardService.Board state){this.state=Objects.requireNonNull(state);board=new ScoreboardAdapter(this);state.owner().register(()->{synchronized(this){objectives.clear();teams.clear();}});}
    public Scoreboard board(){return board;}
    public synchronized ObjectiveAdapter objective(ScoreboardService.Objective state){return objectives.computeIfAbsent(state,key->new ObjectiveAdapter(this,key));}
    public synchronized TeamAdapter team(ScoreboardService.Team state){return teams.computeIfAbsent(state,key->new TeamAdapter(this,key));}
    public Objective getObjective(String name){return state.objective(name).map(this::objective).orElse(null);}
    public Team getTeam(String name){return state.team(name).map(this::team).orElse(null);}
    public Team entryTeam(String entry){return state.entryTeam(entry).map(this::team).orElse(null);}
    public Set<Objective> objectives(){var values=new LinkedHashSet<Objective>();state.objectives().forEach(value->values.add(objective(value)));return Collections.unmodifiableSet(values);}
    public Set<Objective> byCriteria(String criteria){var values=new LinkedHashSet<Objective>();state.objectivesByCriteria(criteria).forEach(value->values.add(objective(value)));return Collections.unmodifiableSet(values);}
    public Set<Team> teams(){var values=new LinkedHashSet<Team>();state.teams().forEach(value->values.add(team(value)));return Collections.unmodifiableSet(values);}
    public Objective registerObjective(String name,String criteria){return objective(state.registerObjective(name,criteria));}
    public Team registerTeam(String name){return team(state.registerTeam(name));}
    public Set<Score> scores(String entry){var values=new LinkedHashSet<Score>();for(var objective:state.scoredObjectives(entry))values.add(new ScoreAdapter(this,objective,entry));return Collections.unmodifiableSet(values);}
    public void remove(ScoreboardService.Objective objective){objective.unregister();synchronized(this){objectives.remove(objective);}}
    public void remove(ScoreboardService.Team team){team.unregister();synchronized(this){teams.remove(team);}}
}
