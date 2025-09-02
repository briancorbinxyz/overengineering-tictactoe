import org.xxdc.oss.example.Game;

void main() throws Exception {
  try (var game = new Game()) {
    game.play();
  }
}
