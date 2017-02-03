// 映像を記録してみる

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

  arduCopter.on('mode', data => console.log(JSON.stringify(data)))

  yield arduCopter.disableEvents(null)
  yield arduCopter.enableEvents([
    'mode'
  ])

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(3000)
  yield arduCopter.setMode('Guided')
  yield asleep(1000)

  yield arduCopter.startVideoRecording()
  yield asleep(30000)
  yield arduCopter.stopVideoRecording()

  yield asleep(1000)
  yield arduCopter.disconnect()
  yield asleep(1000)
  yield caller.disconnect()
}).catch((err) => console.error(err))
