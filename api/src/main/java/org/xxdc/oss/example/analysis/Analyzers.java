package org.xxdc.oss.example.analysis;

import java.util.stream.Gatherer;
import java.util.stream.Gatherer.Integrator;
import org.xxdc.oss.example.GameState;

public class Analyzers {

  static class GathererState {
    private GameState prevGameState;
    private int moveCounter;

    public boolean add(GameState gameState) {
      if (prevGameState == null) {
        prevGameState = gameState;
        moveCounter =
            gameState.board().dimension() * gameState.board().dimension()
                - gameState.availableMoves().size();
        return false;
      } else {
        prevGameState = gameState;
        moveCounter++;
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
            (state, gameState, downstream) -> {
              if (state.prevGameState != null) {
                StrategicTurningPoint.from(state.prevGameState, gameState, state.moveCounter)
                    .ifPresent(downstream::push);
              }

              state.add(gameState);
              return true;
            }));
  }
}
