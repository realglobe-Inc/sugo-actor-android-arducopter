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
  const arduCopter = actor.get('ArduCopter')

  arduCopter.on('position', data => console.log(JSON.stringify(data)))
  arduCopter.on('mode', data => console.log(JSON.stringify(data)))

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(5000)
  yield arduCopter.disableEvents(null)
  yield arduCopter.enableEvents([
    'mode',
    'position'
  ])
  yield arduCopter.setMode('Guided')

  yield asleep(5000)
  yield arduCopter.disconnect()

  yield caller.disconnect()
}).catch((err) => console.error(err))
