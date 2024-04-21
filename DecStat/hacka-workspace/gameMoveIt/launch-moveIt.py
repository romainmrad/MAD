from hackagames.gameMoveIt.gameEngine import GameMoveIt
from gameMoveIt.qvalues_bot import Bot
import matplotlib.pyplot as plt

game = GameMoveIt(seed=128)
results = game.testPlayer(Bot(), 10000)
print(f"Average score: {sum(results) / len(results)}")

scores = []
step = 100
size = len(results)
for i in range(0, size, step):
    s = min(i + step, size)
    scores.append(sum([x for x in results[i:s]]) / (s - i))
plt.plot([step * i for i in range(len(scores))], scores)
plt.show()
