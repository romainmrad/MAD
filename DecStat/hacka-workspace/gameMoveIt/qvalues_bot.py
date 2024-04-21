import os
import json, random
import hackagames.gameMoveIt.gameEngine as ge


# Erwan Coyaud et Romain Mrad
# Préférer >3000 itérations pour bien voir l'évolution de l'apprentissage

class Bot:
    def __init__(self, data='.json', learningRate=0.01):  # lr Ã  adapter selon le nombre d'itérations
        self.action = None
        self._countCycle = None
        self._countTic = None
        self.state = None
        self.actions = None
        self._score = None
        self.first = None
        self._mobiles = None
        self._board = None
        self.qvalues = {}
        self.learningRate = learningRate
        self.epsilon = 0.001

        # Ã  mettre en commentaire si pas d'écriture des Q-Valeurs voulue Ã  sleep
        # if os.path.exists(data) :
        #     dataFile= open(data, 'r')
        #     self.qvalues= json.load( dataFile )
        # self.dataFile = 'moveItQValues.json'

    def bestAction(self, state, available_actions):
        # Initialiser les nouvelles actions disponibles Ã  0
        for action in available_actions:
            if "move" in action:  # debug la présence de Q-valeurs ne correspondant pas à une action, et initialise
                # celles qui y correspondent
                if action not in self.qvalues[state]:
                    self.qvalues[state][action] = 0.0
        # Choisir celle avec la plus haute valeur
        action = max(self.qvalues[state], key=self.qvalues[state].get)
        return action

    def get_state(self):
        # state = robot + objectif + humains
        robot, human1, human2 = tuple(self._mobiles)
        pathGoal = self._board.path(robot.x(), robot.y(), robot.goalx(), robot.goaly())
        state = f"r: {robot.x()}-{robot.y()}-{pathGoal[0]}"
        state += f" h1: {human1.x()}-{human1.y()}"
        state += f" h2: {human2.x()}-{human2.y()}"
        return state

    # Formule de Q-learning
    def updateQValues(self, previous_state, previous_action, reward, current_state, current_action):
        oldValue = self.qvalues[previous_state][previous_action]
        newValue = reward + 0.999 * self.qvalues[current_state][current_action]
        self.qvalues[previous_state][previous_action] = ((1 - self.learningRate) * oldValue + self.learningRate *
                                                         newValue)

    def wakeUp(self, playerId, numberOfPlayers, gamePod):
        # Initialisation
        self._board = ge.Hexaboard()
        self._board.fromPod(gamePod)
        nbRobots, nbMobiles = gamePod.flag(3), gamePod.flag(4)
        self._mobiles = ge.defineMobiles(nbRobots, nbMobiles)
        self._board.setupMobiles(self._mobiles)
        self.first = True
        self._score = 0
        self.actions = ["move 0", "move 1", "move 2", "move 3", "move 4", "move 5",
                        "move 6"]  # pass non utilisé car généralement mauvais

    def perceive(self, statePod):
        # Update du board
        self._board.mobilesFromPod(statePod)
        self._countTic = statePod.flag(1)
        self._countCycle = statePod.flag(2)

        newScore = statePod.value(1)
        reward = newScore - self._score
        self._score = newScore

        reachedState = self.get_state()

        # Check if the state exists in qvalues dictionary
        if reachedState not in self.qvalues:
            # Initialise l'action optimale théorique à une Q-valeur positive pour accélérer l'apprentissage
            robot = self._mobiles[0]
            path_to_target = self._board.path(robot.x(), robot.y(), robot.goalx(), robot.goaly())[0]
            self.qvalues[reachedState] = {f"move {path_to_target}": 0.01}

        # si ce n'est pas la 1ʳᵉ action, l'initialiser si besoin et mettre à jour les Q valeurs
        if not self.first:
            if self.action not in self.qvalues[self.state]:
                self.qvalues[self.state][self.action] = 0.0
            self.updateQValues(self.state, self.action, reward, reachedState,
                               self.bestAction(reachedState, self.action))
        self.first = False
        # switch state:
        self.state = reachedState

    def decide(self):
        # Récupérer la liste des actions et interdire celles qui entrainent une collision directe
        possible_actions = self.actions
        forbidden_actions = []
        for action in possible_actions:
            direction = int(action.split()[-1])
            if self.is_forbidden_move(direction):
                forbidden_actions.append(action)
        available_actions = [action for action in possible_actions if action not in forbidden_actions]
        # Epsilon-greedy policy
        if random.random() < self.epsilon:
            # Action random parmi celles cohérentes
            self.action = random.choice(available_actions)
        else:
            # Sinon en temps normal, choisir la meilleur action
            self.action = self.bestAction(self.state, available_actions)

        return self.action

    def is_forbidden_move(self, direction):
        # Position du robot
        robot_x, robot_y = self._mobiles[0].x(), self._mobiles[0].y()
        # Prochaine position du robot
        next_x, next_y = self._board.at_dir(robot_x, robot_y, direction)
        # Vérifier la présence d'obstacle
        if self._board.isCoordinate(next_x, next_y) and self._board.at(next_x, next_y).isObstacle():
            return True
        # Vérifier la présence d'humains
        for human in self._mobiles[1:]:
            if (next_x, next_y) == (human.x(), human.y()):
                return True
        # Sinon move autorisé
        return False

    def sleep(self, result):
        if "End" not in self.qvalues:
            self.qvalues["End"] = {"sleep": 0}
        robot = self._mobiles[0]
        path_to_target = self._board.path(robot.x(), robot.y(), robot.goalx(), robot.goaly())[0]
        self.updateQValues(self.state, f"move {path_to_target}", result, "End", "sleep")

        # Note les Q-valeurs dans un fichier, Ã  mettre en commentaire pour accélérer drastiquement la vitesse de run
        # with open(self.dataFile, 'w') as dataFile:
        #     json.dump(self.qvalues, dataFile, sort_keys=True, indent =4)
