'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

const takeoffAlt = 10
const alt = 30
const dist = 0.0003

function distance (p1, p2) {
  const x = p2[0] - p1[0]
  const y = p2[1] - p1[1]
  return Math.sqrt(x * x + y * y)
}

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('ArduCopter')

  arduCopter.on('armed', () => console.log('ARMED'))
  arduCopter.on('mode', data => console.log(JSON.stringify(data)))

  arduCopter.on('disarmed', () => co(function *() {
    console.log('DISARMED')
    yield arduCopter.disconnect()
    yield asleep(1000)
    yield caller.disconnect()
  }).catch((err) => {
    console.error(err)
    caller.disconnect()
  }))

  let landing = true
  let climbing = false
  let moving = false
  let coordinate
  let goal
  arduCopter.on('position', data => {
    console.log(JSON.stringify(data))
    coordinate = data.coordinate

    if (landing) {
      if (Math.abs(takeoffAlt - coordinate[2]) < 1) {
        landing = false
        climbing = true

        console.log('CLIMB TO ' + alt);
        // なぜか途中で止まるので繰り返す
        (function loop () {
          if (!climbing) {
            return
          }
          arduCopter.climbTo(alt)
          setTimeout(loop, 10000)
        })()
      }
    } else if (climbing) {
      if (Math.abs(alt - coordinate[2]) < 1) {
        climbing = false
        moving = true
        goal = [coordinate[0] + dist, coordinate[1]]

        console.log('GO TO ' + goal);
        // なぜか途中で止まるので繰り返す
        (function loop () {
          if (!moving) {
            return
          }
          arduCopter.goTo(goal[0], goal[1])
          setTimeout(loop, 10000)
        })()
      }
    } else if (moving) {
      if (distance(coordinate, goal) < dist / 10) {
        moving = false

        console.log('LAND')
        arduCopter.land()
      }
    }
  })

  yield arduCopter.disableEvents(null)
  yield arduCopter.enableEvents([
    'armed',
    'disarmed',
    'mode',
    'position'
  ])

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(3000)
  yield arduCopter.setMode('Guided')
  yield asleep(1000)
  yield arduCopter.arm(true)
  yield asleep(1000)

  console.log('TAKEOFF')
  yield arduCopter.takeoff(takeoffAlt)
}).catch((err) => console.error(err))
