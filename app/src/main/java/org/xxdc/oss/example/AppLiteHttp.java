import org.xxdc.oss.example.Game;

import java.net.URI;
import module java.net.http;

void main() {
    final String url = "https://api.github.com/zen";
    try (var client = HttpClient.newHttpClient()) {
      long t0 = System.nanoTime();
      var res =
          client.send(
              HttpRequest.newBuilder(URI.create(url))
                  .header("User-Agent", "overengineering-tictactoe/3.0")
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      int status = res.statusCode();
      long latencyMs = (System.nanoTime() - t0) / 1_000_000;
      String message = status == 200 ? res.body() : ("status=" + status);
      IO.println("[Lite] " + message);

      try (var game =
          new Game(
              ctx ->
                  ctx.put("app.entry", "AppLiteHttp")
                      .put("http.url", url)
                      .put("http.status", Integer.toString(status))
                      .put("http.latencyMs", Long.toString(latencyMs))
                      .put("http.message", message))) {
        game.playWithAction(
            g -> {
              if (g.moveNumber() == 1) Game.gameContext().ifPresent(c -> IO.println(c));
            });
      }
    } catch (Exception e) {
      IO.println("[Lite] Failed; ending game");
      IO.println(e.getMessage());
      e.printStackTrace();
    }
  }
