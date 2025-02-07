package org.xxdc.oss.example.analysis;

import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Integrator;
import org.xxdc.oss.example.GameState;

public class Analyzers {

  static class GathererState {
    private GameState prevGameState;
    private int currMoveNumber;

    public boolean add(GameState gameState) {
      if (prevGameState == null) {
        prevGameState = gameState;
        currMoveNumber =
            gameState.board().dimension() * gameState.board().dimension()
                - gameState.availableMoves().size()
                + 1;
        return true;
      } else {
        prevGameState = gameState;
        currMoveNumber++;
        return true;
      }
    }
  }

  public static Gatherer<GameState, GathererState, StrategicTurningPoint> strategicTurningPoints() {
    return Gatherer.ofSequential(
        // Initializer<State>: state - track the previous game state, move counter
        GathererState::new,
        // Integrator<State, Upstream, Downstream>: discover and emit strategic turning points
        Integrator.<GathererState, GameState, StrategicTurningPoint>of(
            (state, currGameState, downstream) -> {
              if (state.prevGameState != null) {
                StrategicTurningPoint.from(state.prevGameState, currGameState, state.currMoveNumber)
                    .ifPresent(downstream::push);
              }

              return state.add(currGameState);
            }));
  }
}
