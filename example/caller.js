'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

function distance (p1, p2) {
  const x = p2[0] - p1[0]
  const y = p2[1] - p1[1]
  return Math.sqrt(x * x + y * y)
}

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('arduCopter')

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)

  yield asleep(5000)

  const takeoffAlt = 10
  const maxAlt = 50
  const moveDist = 0.001

  // 1: 離陸
  // 2: 上昇
  // 3: 移動
  let phase = 1

  arduCopter.on('altitude', data => {
    switch (phase) {
      case 1: {
        console.log('alt: ' + data.altitude)
        if (data.altitude > takeoffAlt * 0.9) {
          phase = 2
          console.log('CLIMB TO ' + maxAlt)
          arduCopter.climbTo(maxAlt)
        }
        break
      }
      case 2: {
        console.log('alt: ' + data.altitude)
        if (data.altitude > maxAlt * 0.9) {
          phase = 3
          console.log('GO TO ' + goal);

          // なぜか途中で止まるので繰り返す
          (function loop () {
            if (phase !== 3) {
              return
            }
            arduCopter.goTo(goal[0], goal[1])
            setTimeout(loop, 10000)
          })()
        }
        break
      }
      case 4: {
        console.log('alt: ' + data.altitude)
        if (data.altitude < 3) {
          phase = 5
          console.log('DISCONNECT')
          caller.disconnect()
        }
        break
      }
    }
  })

  var goal
  var cur
  arduCopter.on('gpsPosition', data => {
    cur = data.coordinate
    if (typeof goal === 'undefined') {
      console.log('START ' + cur)
      const diff = moveDist / Math.sqrt(2)
      goal = [cur[0] + diff, cur[1] + diff]
    }
    switch (phase) {
      case 3: {
        const dist = distance(cur, goal)
        console.log('dist: ' + dist)
        if (dist < moveDist / 10) {
          phase = 4
          console.log('LAND')
          arduCopter.land()
        }
        break
      }
      default: {
      }
    }
  })

  arduCopter.on('arming', data => {
    if (data.arming) {
      console.log('TAKEOFF')
      arduCopter.takeoff(takeoffAlt)
    }
  })

  yield arduCopter.setMode('GUIDED')
  yield asleep(1000)
  yield arduCopter.arm(true)
}).catch((err) => console.error(err))
