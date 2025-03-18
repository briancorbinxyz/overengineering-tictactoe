![Over-Engineering TicTacToe](oe-tictactoe.png)

Over-Engineering Tic-Tac-Toe
---

Tic-Tac-Toe in Java deliberately over-engineered to apply features of Java introduced over time.

Developed to pair with the ongoing blog post: [Road to JDK 25 - Over-Engineering Tic-Tac-Toe](https://sympatheticengineering.com/Library/03-Resources/Road-to-JDK-25---Over-Engineering-Tic-Tac-Toe!) also serialized to Medium @ [Road to JDK 25 - Over-Engineering Tic-Tac-Toe On Medium](https://briancorbinxyz.medium.com/list/road-to-jdk-25-d0f656f66a8f)

---

### Features

https://openjdk.org/projects/jdk/24/
- **JEP496**:   Quantum-Resistant Module-Lattice-Based Key Encapsulation Mechanism
- **JEP485**:   Stream Gatherers
- **JEP483**:   Ahead-of-Time Class Loading & Linking
- **JEP484**:   Class-File API

https://openjdk.org/projects/jdk/23/

- **JEP467**:   Markdown Documentation Comments	
- **JEP474**:   ZGC: Generational Mode by Default
- **JEP471**:   Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal

https://openjdk.org/projects/jdk/22/

- **JEP454**:	Foreign Function & Memory API
- **JEP456**:	Unnamed Variables & Patterns

https://openjdk.org/projects/jdk/21/

- **JEP431**:	Sequenced Collections
- **JEP439**:	Generational ZGC
- **JEP440**:	Record Patterns
- **JEP441**:	Pattern Matching for switch
- **JEP444**:	Virtual Threads
- **JEP452**:	Key Encapsulation Mechanism API

https://openjdk.org/projects/jdk/20/

- No new features

https://openjdk.org/projects/jdk/19/

- No new features

https://openjdk.org/projects/jdk/18/

- **JEP400**:	UTF-8 by Default
- **JEP408**:	Simple Web Server
- **JEP413**:	Code Snippets in Java API Documentation
- **JEP421**:	Deprecate Finalization for Removal

https://openjdk.org/projects/jdk/17/

- **JEP421**:	Deprecate Finalization for Removal
- **JEP409**:	Sealed Classes
- **JEP410**:	Remove the Experimental AOT and JIT Compiler
- **JEP415**:	Context-Specific Deserialization Filters

---

### Algorithms

The following algorithms are used by the AI BOT in this project - for a detailed discussion see [Road to JDK 25 - An Algorithmic Interlude](https://briancorbinxyz.medium.com/over-engineering-tic-tac-toe-an-algorithmic-interlude-8af3aa13173a):

- [Random](https://en.wikipedia.org/wiki/Randomness) See: [Random.java](api/src/main/java/org/xxdc/oss/example/bot/Random.java)
- [Minimax](https://en.wikipedia.org/wiki/Minimax) See: [Minimax.java](api/src/main/java/org/xxdc/oss/example/bot/Minimax.java)
- [Minimax w. Alpha-Beta](https://en.wikipedia.org/wiki/Alpha-beta_pruning) See: [AlphaBeta.java](api/src/main/java/org/xxdc/oss/example/bot/AlphaBeta.java)
- [MaxN](https://en.wikipedia.org/wiki/Maxn_algorithm) See: [MaxN.java](api/src/main/java/org/xxdc/oss/example/bot/MaxN.java)
- [Paranoid](https://en.wikipedia.org/wiki/Paranoid_AI) See: [Paranoid.java](api/src/main/java/org/xxdc/oss/example/bot/Paranoid.java)
- [Monte Carlo Tree Search](https://en.wikipedia.org/wiki/Monte_Carlo_method) See [MonteCarloTreeSearch.java](api/src/main/java/org/xxdc/oss/example/bot/MonteCarloTreeSearch.java)


---

### Quick Start

- To run the single game application, use the following command: `./gradlew run`

- If you don't have Java 24 installed on your system you can install it first with [SDKMAN](https://sdkman.io/):

```bash
curl -s "https://get.sdkman.io" | bash

sdk install java 24-tem

./gradlew run
```

---

### Related

- [Over-Engineering Tic-Tac-Toe - CLI](https://github.com/briancorbinxyz/overengineering-tictactoe-cli) Run overengineered tic-tac-toe from the command line
