globals [
  road-width
  line-width
  pit-width
  pit-length
]

breed [cars car]

cars-own [index fuel consumption speed askToStop isInPit laps tires ticksLeft]

to setup
  ; clearing
  clear-all
  ; drawind road and pits
  setup-road
  setup-pits
  ; adding cars
  create-cars 1 [
    set index 0
    set fuel 200
    set speed 2
    set tires "Standard"
    set askToStop 0
    set isInPit 0
    set laps 0
    set ticksLeft 0
    set xcor -15
    set ycor (road-width / 2 - 0.5)
    set shape "car"
    set color 65
    set size 1.5
    set heading 90
  ]
  create-cars 1 [
    set index 1
    set fuel 200
    set speed 2
    set tires "Standard"
    set askToStop 0
    set isInPit 0
    set laps 0
    set ticksLeft 0
    set xcor -15
    set ycor (- road-width / 2 + 0.5)
    set shape "car"
    set color 25
    set size 1.5
    set heading 90
  ]
  reset-ticks
end

; draw road
to setup-road
  set road-width 4
  set line-width 1

  ask patches [
    (ifelse
      ; drawing black road
      (pycor >= (- road-width / 2) and pycor <= (- line-width / 2)) or (pycor <= (road-width / 2) and pycor >= (line-width / 2)) [
        set pcolor black
      ]
      ; drawing with lines above and below it
      (pycor >= (- road-width / 2 - line-width) and pycor <= (- road-width / 2)) or (pycor >= (road-width / 2) and pycor <= (road-width / 2 + line-width)) [
        set pcolor white
      ]
      ; drawing center white line
      (pycor >= (- line-width / 2) and pycor <= (line-width / 2)) [
        set pcolor white
      ]
      [
      set pcolor green
    ])
  ]
end

; draw pits
to setup-pits
  set pit-width 2
  set pit-length 6

  ask patches [
    (ifelse
      pycor >= (road-width / 2 + line-width) and pycor <= (road-width / 2 + line-width + pit-width) and pxcor >= (- pit-length / 2) and pxcor <= (pit-length / 2) [
        set pcolor black
      ]
      pycor <= (- road-width / 2 - line-width) and pycor >= (- road-width / 2 - line-width - pit-width) and pxcor >= (- pit-length / 2) and pxcor <= (pit-length / 2) [
        set pcolor black
      ])
  ]
end

; main function
to go
  tick-advance 1
  ifelse getTimer 0 = 0 [
    moveToPitCar0
    moveOutOfPitCar0
    updateCarState 0
  ][decreaseTimer 0]
  ifelse getTimer 1 = 0[
    moveToPitCar1
    moveOutOfPitCar1
    updateCarState 1
  ][decreaseTimer 1]
  race
end

; race function
to race
  ask cars with [askToStop = 0][
    fd speed
  ]
end

; update car state
to updateCarState [i]
  ask cars with [index = i][
    updateConsumptionAndSpeed i
    if pxcor > 13[
        ; incrementing number of laps
        set laps (laps + 1)
        ; decreasing fuel level
        ifelse fuel - consumption <= 0[
          set askToStop 1
        ][
          set fuel (fuel - consumption)
        ]
    ]
  ]
end

; get car laps
to-report getCarLaps [i]
  let returnedLaps 0
  ask cars with [index = i][
    set returnedLaps laps
  ]
  report returnedLaps
end

; get car speed
to-report getCarSpeed [i]
  let returnedSpeed 0
  ask cars with [index = i][
    set returnedSpeed speed
  ]
  report returnedSpeed
end

; get car fuel
to-report getCarFuel [i]
  let returnedFuel 0
  ask cars with [index = i][
    set returnedFuel fuel
  ]
  report returnedFuel
end

; get car consumption
to-report getCarConsumption [i]
  let returnedConsumption 0
  ask cars with [index = i][
    set returnedConsumption consumption
  ]
  report returnedConsumption
end

; get car y cor
to-report getCarYCor [i]
  let y 0
  ask cars with [index = i][
    set y pycor
  ]
  report y
end

; get car x cor
to-report getCarXCor [i]
  let x 0
  ask cars with [index = i][
    set x pxcor
  ]
  report x
end

; set a timer for a car
to setTimer [i t]
  ask cars with [index = i][
    set ticksLeft t
  ]
end

; get current car timer
to-report getTimer [i]
  let returnedTicks 0
  ask cars with [index = i][
    set returnedTicks ticksLeft
  ]
  report returnedTicks
end

; decrease timer
to decreaseTimer [i]
  ask cars with [index = i][
    set ticksLeft (ticksLeft - 1)
  ]
end

; pit stop a car
to pitstopCar [i]
  ask cars with [index = i] with [isInPit = 0] [
    set askToStop 1
  ]
end

; update fuel consumption and speed according to current fuel level and tires
to updateConsumptionAndSpeed [i]
  ask cars with [index = i][
    (ifelse
      tires = "Soft" and fuel <= 50[
        set consumption 6
        set speed 1.5
      ]
      tires = "Standard" and fuel <= 50[
        set consumption 4
        set speed 1.25
      ]
      tires = "Hard" and fuel <= 50[
        set consumption 2
        set speed 1
      ]
      tires = "Soft" and fuel <= 100[
        set consumption 8
        set speed 1.75
      ]
      tires = "Standard" and fuel <= 100[
        set consumption 6
        set speed 1.5
      ]
      tires = "Hard" and fuel <= 100[
        set consumption 4
        set speed 1.25
      ]
      tires = "Soft" and fuel <= 150[
        set consumption 10
        set speed 2
      ]
      tires = "Standard" and fuel <= 150[
        set consumption 8
        set speed 1.75
      ]
      tires = "Hard" and fuel <= 150[
        set consumption 6
        set speed 1.5
      ]
      tires = "Soft" and fuel <= 200[
        set consumption 12
        set speed 2.25
      ]
      tires = "Standard" and fuel <= 200[
        set consumption 10
        set speed 2
      ]
      tires = "Hard" and fuel <= 25[
        set consumption 8
        set speed 1.75
    ])
  ]
end

;pitstop car 0
to moveToPitCar0
  (ifelse
    getCarXCor 0 >= (- pit-length / 2 + 1) and getCarXCor 0 <= 0 and getCarYCor 0 <= (road-width / 2 + line-width)[
      ask cars with [index = 0] with [isInPit = 0] with [askToStop = 1] [set heading 0 fd 0.5]
    ]
    getCarYCor 0 >= (road-width / 2 + line-width)[
      ask cars with [index = 0] with [isInPit = 0] with [askToStop = 1] [set isInPit 1]
      (ifelse
        getCarFuel 0 - getCarConsumption 0 <= 0 [fillTank 0 setTimer 0 800]
        [pitstopCar0 pitstopAction0])
    ][
      ask cars with [index = 0] with [isInPit = 0] with [askToStop = 1] [fd speed]
    ])
end

; move car 0 out of pit
to moveOutOfPitCar0
  ask cars with [index = 0] with [isInPit = 1] with [askToStop = 1] [
    ifelse pycor > (road-width / 2 + 0.5) [
      set heading 180
      fd 1
    ]
    [
      set isInPit 0
      set askToStop 0
      set heading 90
    ]
  ]
end

;pitstop car 1
to moveToPitCar1
  (ifelse
    getCarXCor 1 >= (- pit-length / 2 + 1) and getCarXCor 1 <= 0 and getCarYCor 1 >= (- road-width / 2 - line-width)[
      ask cars with [index = 1] with [isInPit = 0] with [askToStop = 1] [set heading 180 fd 0.5]
    ]
    getCarYCor 1 <= (- road-width / 2 - line-width)[
      ask cars with [index = 1] with [isInPit = 0] with [askToStop = 1] [set isInPit 1]
      (ifelse
        getCarFuel 1 - getCarConsumption 1 <= 0 [fillTank 1 setTimer 1 800]
        [pitstopCar1 pitstopAction1])
    ][
      ask cars with [index = 1] with [isInPit = 0] with [askToStop = 1] [fd speed]
    ])
end

; move car 1 out of pit
to moveOutOfPitCar1
  ask cars with [index = 1] with [isInPit = 1] with [askToStop = 1] [
    ifelse pycor < (- road-width / 2 - 0.5) [
      set heading 0
      fd 1
    ]
    [
      set isInPit 0
      set askToStop 0
      set heading 90
    ]
  ]
end

; refuel 1/4 tank
to quarterTankFill [i]
  ask cars with [index = i] with [isInPit = 1] with [askToStop = 1][ifelse fuel + 50 > 200[set fuel 200][set fuel (fuel + 50)]]
end

; refuel 1/2 tank
to halfTankFill [i]
  ask cars with [index = i] with [isInPit = 1] with [askToStop = 1][ifelse fuel + 100 > 200[set fuel 200][set fuel (fuel + 100)]]
end

; refuel 3/4 tank
to threeQuartersfTankFill [i]
  ask cars with [index = i] with [isInPit = 1] with [askToStop = 1][ifelse fuel + 150 > 200[set fuel 200][set fuel (fuel + 150)]]
end

; refuel full tank
to fillTank [i]
  ask cars with [index = i] with [isInPit = 1] with [askToStop = 1][set fuel 200]
end

; change tires
to changeTires [i]
  ask cars with [index = i] with [isInPit = 1] with [askToStop = 1][ifelse i = 0[set tires car0TireType][set tires car1TireType]]
end

; pitstop car 0 with action
to pitstopCar0 [action]
  (ifelse
    action = "change tires"[changeTires 0 setTimer 0 200]
    action = "1/4 tank"[quarterTankFill 0 setTimer 0 200]
    action = "1/2 tank"[halfTankFill 0 setTimer 0 400]
    action = "3/4 tank"[threeQuartersfTankFill 0 setTimer 0 600]
    action = "fill tank"[fillTank 0 setTimer 0 800])
end

; pitstop car 1 with action
to pitstopCar1 [action]
  (ifelse
    action = "change tires"[changeTires 1 setTimer 1 200]
    action = "1/4 tank"[quarterTankFill 1 setTimer 1 200]
    action = "1/2 tank"[halfTankFill 1 setTimer 1 400]
    action = "3/4 tank"[threeQuartersfTankFill 1 setTimer 1 600]
    action = "fill tank"[halfTankFill 1  setTimer 1 800])
end


@#$#@#$#@
GRAPHICS-WINDOW
191
10
882
702
-1
-1
20.7
1
10
1
1
1
0
1
1
1
-16
16
-16
16
0
0
1
ticks
30.0

BUTTON
22
14
88
47
NIL
setup\n
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
932
66
1043
99
Pitstop Car 0
pitstopCar 0
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
88
14
151
47
NIL
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
1060
66
1171
99
Pitstop Car 1
pitstopCar 1
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

MONITOR
932
99
1043
144
Car 0 fuel level
getCarFuel 0
17
1
11

MONITOR
1060
99
1171
144
Car 1 fuel level
getCarFuel 1
17
1
11

CHOOSER
932
233
1043
278
car0TireType
car0TireType
"Soft" "Standard" "Hard"
2

CHOOSER
1060
233
1171
278
car1TireType
car1TireType
"Soft" "Standard" "Hard"
1

CHOOSER
932
278
1043
323
pitstopAction0
pitstopAction0
"change tires" "1/4 tank" "1/2 tank" "3/4 tank" "fill tank"
0

CHOOSER
1060
278
1171
323
pitstopAction1
pitstopAction1
"change tires" "1/4 tank" "1/2 tank" "3/4 tank" "fill tank"
2

MONITOR
932
143
1043
188
Car 0 laps
getCarLaps 0
17
1
11

MONITOR
1060
143
1171
188
Car 1 laps
getCarLaps 1
17
1
11

MONITOR
932
188
1043
233
Car 0 speed
getCarSpeed 0
17
1
11

MONITOR
1060
188
1171
233
Car 1 speed
getCarSpeed 1
17
1
11

TEXTBOX
958
42
1020
60
Green car
11
0.0
1

TEXTBOX
1076
40
1226
58
Orange car
11
0.0
1

@#$#@#$#@
## WHAT IS IT?

The model simulates a race between two cars. The goal is to simulate which car goes faster with each strategy. A strategy is a fuel level and a tire type. 

## HOW IT WORKS

The cars race until they run out of fuel. Then, they go the their respective pitstop and fill their tanks back up. 

## HOW TO USE IT

The user can decide when to pitstop a car (even if the fuel level is not low) to fill up the tank by 1/4, 1/2, 3/4 or change tires. 

## THINGS TO NOTICE

It's important to track the number of laps each car has completed to know which car is ahead of the other. 

## THINGS TO TRY

Not much user experience, just a selection of what to do when to pitstop and the tire type the user wants to change.

## EXTENDING THE MODEL

I would add a better modelling of fuel consumption and speed for fuel levels and tire types. 

## NETLOGO FEATURES

Make one turtle wait while the simulation goes on

## RELATED MODELS

None

## CREDITS AND REFERENCES

None
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

sheep
false
15
Circle -1 true true 203 65 88
Circle -1 true true 70 65 162
Circle -1 true true 150 105 120
Polygon -7500403 true false 218 120 240 165 255 165 278 120
Circle -7500403 true false 214 72 67
Rectangle -1 true true 164 223 179 298
Polygon -1 true true 45 285 30 285 30 240 15 195 45 210
Circle -1 true true 3 83 150
Rectangle -1 true true 65 221 80 296
Polygon -1 true true 195 285 210 285 210 240 240 210 195 210
Polygon -7500403 true false 276 85 285 105 302 99 294 83
Polygon -7500403 true false 219 85 210 105 193 99 201 83

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

wolf
false
0
Polygon -16777216 true false 253 133 245 131 245 133
Polygon -7500403 true true 2 194 13 197 30 191 38 193 38 205 20 226 20 257 27 265 38 266 40 260 31 253 31 230 60 206 68 198 75 209 66 228 65 243 82 261 84 268 100 267 103 261 77 239 79 231 100 207 98 196 119 201 143 202 160 195 166 210 172 213 173 238 167 251 160 248 154 265 169 264 178 247 186 240 198 260 200 271 217 271 219 262 207 258 195 230 192 198 210 184 227 164 242 144 259 145 284 151 277 141 293 140 299 134 297 127 273 119 270 105
Polygon -7500403 true true -1 195 14 180 36 166 40 153 53 140 82 131 134 133 159 126 188 115 227 108 236 102 238 98 268 86 269 92 281 87 269 103 269 113

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270
@#$#@#$#@
NetLogo 6.4.0
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180
@#$#@#$#@
0
@#$#@#$#@
