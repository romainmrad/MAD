import random


class Bot:
    def __init__(self):
        self.horizon = None
        self.dices = None

    # Player interface :
    def wakeUp(self, playerId, numberOfPlayers, gameConf):
        self._trace = []
        self._actions = ['keep-keep-keep', 'keep-keep-roll', 'keep-roll-keep', 'keep-roll-roll',
                         'roll-keep-keep', 'roll-keep-roll', 'roll-roll-keep', 'roll-roll-roll']

    def perceive(self, gameState):
        elements = gameState.children()
        self.horizon = elements[0].flag(1)
        self.dices = elements[1].flags()

    def decide(self):
        action = random.choice(self._actions)
        self._trace.append({
            'state': '-'.join([str(d) for d in self.dices]),
            'action': action
        })
        return action

    def sleep(self, result):
        # open a State.Action.Value file in append mode
        with open("log-421-SAV.csv", "a") as logFile:
            # For each reccorded trace
            for t in self._trace:
                # add a line in the file
                logFile.write(f"{t['state']}, {t['action']}, {result}\n")
