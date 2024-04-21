import json
import os
import random


def log(txt):
    pass
    # print(txt)


class Bot:

    def __init__(self, data='.json', learningRate=0.1):
        self.action = None
        self.state = None
        self.first = None
        self.actions = None
        self.dices = None
        self.horizon = None
        self.qvalues = {}
        self.learningRate = learningRate
        self.epsilon = 0.1
        self.dataFile = 'data-Q-Values.json'
        # Optional
        if os.path.exists(self.dataFile):
            with open(self.dataFile, 'r') as file:
                self.qvalues = json.load(file)

    # Learner :
    def stateStr(self):
        if self.action == 'keep-keep-keep':
            self.horizon = 0  # If we already keep all dice the game is ended
        state = str(self.horizon)
        for d in self.dices:
            state += '-' + str(d)
        return state

    def updateQValues(self, previous_state, previous_action, reward, current_state, current_action):
        oldValue = self.qvalues[previous_state][previous_action]
        newValue = reward + 0.999 * self.qvalues[current_state][current_action]
        self.qvalues[previous_state][previous_action] = (1 - self.learningRate) * oldValue
        self.qvalues[previous_state][previous_action] += self.learningRate * newValue

    def bestAction(self, state):
        action = list(self.qvalues[state].keys())[0]
        value = self.qvalues[state][action]
        for a in self.qvalues[state]:
            if self.qvalues[state][a] > value:
                action = a
                value = self.qvalues[state][a]
        return action

    # Player interface :
    def wakeUp(self, playerId, numberOfPlayers, gameConf):
        log(f'---\nwake-up player-{playerId} ({numberOfPlayers} players)')
        log(gameConf)
        self.actions = ['keep-keep-keep', 'keep-keep-roll', 'keep-roll-keep', 'keep-roll-roll',
                        'roll-keep-keep', 'roll-keep-roll', 'roll-roll-keep', 'roll-roll-roll']
        self.action = 'go'
        self.first = True

    def perceive(self, gameState):
        # get elements:
        elements = gameState.children()
        self.horizon = elements[0].flag(1)
        self.dices = elements[1].flags()
        # print:
        log(f'H: {self.horizon} DICES: {self.dices} REWARD: {0.0}')
        # learn stat reward:
        reachedState = self.stateStr()
        if reachedState not in self.qvalues:
            self.qvalues[reachedState] = {"keep-keep-keep": 0}
        if not self.first:  # i.e. it is not the first time we pass here from the last wakeUp.
            self.updateQValues(self.state, self.action, 0.0, reachedState, self.bestAction(reachedState))
        self.first = False
        # switch state:
        self.state = reachedState

    def decide(self):
        # random action:
        self.action = random.choice(self.actions)
        if self.action not in self.qvalues[self.state]:
            self.qvalues[self.state][self.action] = 0.0
        # epsilon chose:
        elif random.random() > self.epsilon:
            self.action = self.bestAction(self.state)
        log(f'Action: {self.action}')
        return self.action

    def sleep(self, result):
        if "End" not in self.qvalues:
            self.qvalues["End"] = {"sleep": 0}
        self.updateQValues(self.state, "keep-keep-keep", result, "End", "sleep")
        with open(self.dataFile, 'w') as file:
            file.write(json.dumps(self.qvalues, sort_keys=True, indent=4))
        log(f'--- Results: {str(result)}')
