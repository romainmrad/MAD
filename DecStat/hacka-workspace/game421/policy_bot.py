import json


class Bot:
    def __init__(self, policyFilePath):
        super().__init__()
        with open(policyFilePath, 'r') as policyFile:
            self.policy = json.load(policyFile)

    # Player interface :
    def wakeUp(self, playerId, numberOfPlayers, gameConf):
        pass

    def perceive(self, gameState):
        elements = gameState.children()
        self.horizon = elements[0].flag(1)
        self.dices = elements[1].flags()

    def decide(self):
        action = self.policy['-'.join([str(d) for d in self.dices])]
        return action

    def sleep(self, result):
        pass
