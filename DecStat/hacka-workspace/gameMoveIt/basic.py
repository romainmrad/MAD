import hackagames.gameMoveIt.gameEngine as ge


class Bot:
    def __init__(self):
        self._score = None
        self._countCycle = None
        self._countTic = None
        self._mobiles = None
        self._board = None

    # Player interface :
    def wakeUp(self, playerId, numberOfPlayers, gamePod):
        # Initialize from gamePod:
        self._board = ge.Hexaboard()
        self._board.fromPod(gamePod)
        nbRobots, nbMobiles = gamePod.flag(3), gamePod.flag(4)
        self._mobiles = ge.defineMobiles(nbRobots, nbMobiles)
        self._board.setupMobiles(self._mobiles)

        # Initialize state variable:
        self._countTic = 0
        self._countCycle = 0
        self._score = 0

    def perceive(self, statePod):
        # update the game state:
        self._board.mobilesFromPod(statePod)
        self._countTic = statePod.flag(1)
        self._countCycle = statePod.flag(2)
        self._score = statePod.value(1)

    def decide(self):
        action = "move"
        robot = self._mobiles[0]
        path = self._board.path(robot.x(), robot.y(), robot.goalx(), robot.goaly())
        directory = path[0]
        action += " " + str(directory)
        return action

    def sleep(self, result):
        pass
