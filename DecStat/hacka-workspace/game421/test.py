import json

with open("policy-WELCOME.json", "r") as file:
    data = json.load(file)

with open('res.txt', "w") as file:
    for key, value in data.items():
        file.write(f'\tpolicy.put("{key}", "{value}")\n')
