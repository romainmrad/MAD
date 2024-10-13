import torch
import torch.nn as nn
import torch.optim as optim
import random
import hackagames.gameMoveIt.gameEngine as ge

# Erwan Coyaud et Romain Mrad
# Deep Q-Learning pour MoveIt
# Possibilité de changer la configuration de la partie (dans ce cas, nb de robots et d'humains à préciser en arguments du bot)

class Bot():
    def __init__(self, nbRobots=1, nbHumains=2, learning_rate=0.001, gamma=0.99, epsilon_start=1.0, epsilon_end=0.001, epsilon_decay=0.999):
        self.output_size = 7  # Dimension de l'espace d'action en sortie du nn : les actions (0 à 6)
        self.learning_rate = learning_rate #lr
        self.gamma = gamma  # Discount factor pour les récompenses futures
        self.epsilon = epsilon_start  # Taux d'exploration initial
        self.epsilon_end = epsilon_end  # Taux d'exploration final
        self.epsilon_decay = epsilon_decay  # Taux de décroissance pour l'exploration
        self.batch_size = 32  # Taille de l'échantillon pour l'entraînement du réseau
        self.target_update_freq = 2000  # Fréquence de mise à jour du réseau cible (1000 équivaut à une update toutes les 6 parties)
        
        # Dimension de l'entrée du réseau de neuronnes ; adapte la taille des états au nombre de mobiles pour le scale-up
        self.input_size = 3 * nbRobots + 2 * nbHumains  # Un robot est défini par (x,y,dir de l'objectif), et les humains par (x,y)

        # Initialiser les réseaux
        self.online_network = self.build_network()
        self.target_network = self.build_network()
        self.target_network.load_state_dict(self.online_network.state_dict()) # copie des paramètres entre les réseaux

        # Intialisation des fonctions de perte et d'optimisation pour les réseaux de neuronnes
        self.criterion = nn.CrossEntropyLoss() # Log loss. Meilleurs résultats qu'avec du L1 loss ou MSE
        self.optimizer = optim.Adam(self.online_network.parameters(), lr=self.learning_rate) # optimiseur Adam pour mettre à jour les poids

        # Initialiser la mémoire pour stocker les expériences
        self.replay_memory = []

        # Attributs pour stocker l'état, l'action, la récompense et l'état suivant
        self.state = None
        self.action = None
        self.reward = None
        self.next_state = None

    def build_network(self):
        # Construire un modèle de réseau de neuronnes en utilisant PyTorch.
        return nn.Sequential(
            nn.Linear(self.input_size, 64),  # Couche d'entrée avec 64 neurones
            nn.ReLU(),  # Fonction d'activation classique : Rectified linear unit
            nn.Linear(64, self.output_size)  # Couche de sortie
        )

    def wakeUp(self, playerId, numberOfPlayers, gamePod):
        # Initialisation
        self._board = ge.Hexaboard()
        self._board.fromPod(gamePod)
        nbRobots, nbMobiles = gamePod.flag(3), gamePod.flag(4)
        self._mobiles = ge.defineMobiles(nbRobots, nbMobiles)
        self._board.setupMobiles(self._mobiles)
        # Définition des actions possibles sous forme numérique pour l'agent
        # Malheureusement, l'action "pass" entraîne des plantages et a donc été retirée
        self.actions = list(range(7))  
        self.score = 0


    def perceive(self, statePod):
        # Update du board
        self._board.mobilesFromPod(statePod)
        self._countTic= statePod.flag(1)
        self._countCycle= statePod.flag(2)

        # Récupération du nouvel état
        state = self.extract_state()

        # Calcul de la récompense en fonction du changement de score
        new_score = statePod.value(1)
        self.reward = new_score - self.score

        # Récompense bonus sur les prises d'objectifs pour les encourager
        # Permet aux robots de prendre de meilleurs décisions sur les parties avec peu d'humains
        if self.reward > 0:
            self.reward += 100

        # Stocker l'expérience dans la mémoire, avec l'état précédent, l'action effectuée, la récompense et le nouvel état
        if self.state is not None: # Permet de gérer le 1er passage dans perceive
            action_value = int(self.action.split()[1])
            self.replay_memory.append((self.state, action_value, self.reward, state))

        # Alignement des poids du réseau cible avec le réseau apprenant à la fréquence définie
        if len(self.replay_memory) % self.target_update_freq == 0:
            self.target_network.load_state_dict(self.online_network.state_dict())
        
        # Mise à jour du score et de l'état
        self.score = new_score
        self.state = state

    def decide(self):
        # Lister les actions possibles et supprimer celles interdites (qui mènent à un obstacle ou à l'extérieur du plateau)
        possible_actions = self.actions
        forbidden_actions = []
        for action in possible_actions:
            if self.is_forbidden_move(action):
                forbidden_actions.append(action)
        valid_actions = [action for action in possible_actions if action not in forbidden_actions]
        if len(valid_actions) == 0: # debug en cas d'absence d'action valide par sécurité, normalement impossible
            valid_actions = [0]
        # Choix epsilon-greedy
        if random.random() < self.epsilon:
            action_value = random.choice(valid_actions)
        # Sinon choix selon la q-valeur maximale
        else:
            with torch.no_grad():
                q_values = self.online_network(torch.FloatTensor(self.state))
                valid_q_values = q_values[valid_actions]
                action_index = valid_q_values.argmax().item()
                action_value = valid_actions[action_index]

        # Réduction progressive d'epsilon 
        self.epsilon = max(self.epsilon_end, self.epsilon * self.epsilon_decay)

        self.action = f"move {action_value}"
        return self.action

    def sleep(self, result):
        # Effectuer l'apprentissage à partir des expériences stockées dans la mémoire
        self.learn()

        # Réinitialiser l'état et la récompense totale pour la prochaine partie
        self.state = None
        self.score = 0

    def is_forbidden_move(self, direction):
        # Position du robot
        robot_x, robot_y = self._mobiles[0].x(), self._mobiles[0].y()
        # Prochaine position du robot
        next_x, next_y = self._board.at_dir(robot_x, robot_y, direction)
        # Vérifier la présence de bordure
        if not self._board.isCoordinate(next_x, next_y):
            return True
        # Ou d'obstacles
        elif self._board.at(next_x, next_y).isObstacle():
            return True
        # Sinon l'action est valide
        return False

    def learn(self):
        # Ne pas apprendre au début quand l'échantillon est trop faible
        if len(self.replay_memory) < self.batch_size:
            return

        # Échantillonner une part des informations stockées en mémoire par perceive
        batch = random.sample(self.replay_memory, self.batch_size)
        states, actions, rewards, next_states= zip(*batch)

        states = torch.FloatTensor(states)
        actions = torch.LongTensor(actions)
        rewards = torch.FloatTensor(rewards)
        next_states = torch.FloatTensor(next_states)

        # Calcul des Q-valeurs du réseau apprenant
        q_values = self.online_network(states)
        q_values = q_values.gather(1, actions.unsqueeze(1)).squeeze(1)

        # Calcul des Q-valeurs via la formule dans le réseau cible
        target_q_values = self.target_network(next_states).max(1)[0]
        target_q_values = rewards + self.gamma * target_q_values # r + γ Q(s', a')

        # Calculer la perte
        loss = self.criterion(q_values, target_q_values)

        # Mettre à jour les poids du réseau de neuronnes
        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()

        

    def extract_state(self):
        # Encoder l'état actuel du jeu dans un format adapté pour l'entrée du réseau neuronal.
        state = []
        for mobile in self._mobiles:
            # Pour un robot, l'état est la position et la direction de l'objectif
            if mobile.isRobot():
                direction = self._board.path(mobile.x(), mobile.y(), mobile.goalx(), mobile.goaly())[0]
                state += [mobile.x(), mobile.y(), direction]
            # Pour un humain, l'état est seulement la position
            elif mobile.isHuman():
                state += [mobile.x(), mobile.y()]
        return state