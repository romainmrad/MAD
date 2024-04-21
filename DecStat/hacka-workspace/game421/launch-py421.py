# Set up a game:
from hackagames.gamePy421.gameEngine import GameSolo as Game
import matplotlib.pyplot as plt

# Set up a player:
# from initial_bot import Bot
# from policy_bot import Bot
from qvalues_bot import Bot

# Instantiate and start 100 games
game = Game()
player = Bot("policy-421-sav.json")
results = game.testPlayer(player, 10000)

# Analyze the result
print(f"Average score: {sum(results) / len(results)}")

scores = []
step = 50
size = len(results)
for i in range(0, size, step):
    s = min(i + step, size)
    scores.append(sum([x for x in results[i:s]]) / (s - i))
plt.plot([step * i for i in range(len(scores))], scores)
plt.show()
