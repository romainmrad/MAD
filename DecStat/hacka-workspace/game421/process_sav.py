import json
import numpy as np

data = {}

# Load data..
with open("log-SAV.csv", "r") as logFile:
    for line in logFile:
        state, action, value = tuple(line.split(', '))
        value = float(value)
        if state not in data:
            data[state] = {action: [value]}
        elif action not in data[state]:
            data[state][action] = [value]
        else:
            data[state][action].append(value)

for key, value in data.items():
    for action in value:
        data[key][action] = np.mean(data[key][action])

for key, value in data.items():
    data[key] = max(value, key=value.get)

with open("policy-WELCOME.json", "w") as policyFile:
    json.dump(data, policyFile, sort_keys=True, indent=2)
