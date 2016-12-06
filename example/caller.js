'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('arduCopter')

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)

  yield asleep(5000)

  const takeoffAlt = 10
  const maxAlt = 100

  arduCopter.on('arming', (isArmed) => {
    if (isArmed) {
      arduCopter.takeoff(takeoffAlt)
    }
  })

  // 1: 離陸
  // 2: 上昇
  // 3: 着陸
  let phase = 1
  arduCopter.on('altitude', (altitude) => {
    console.log(altitude)
    switch (phase) {
      case 1: {
        if (altitude > takeoffAlt * 0.8) {
          arduCopter.climbTo(maxAlt)
          phase = 2
        }
        break
      }
      case 2: {
        if (altitude > maxAlt * 0.8) {
          arduCopter.land()
          phase = 3
        }
        break
      }
      case 3: {
        if (altitude < 10) {
          caller.disconnect()
        }
        break
      }
    }
  })

  yield arduCopter.arm(true)
}).catch((err) => console.error(err))
