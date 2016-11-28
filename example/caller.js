'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('arduCopter')

  yield arduCopter.connect('udp', '192.168.1.33')

  yield asleep(10000)

  yield arduCopter.takeoff(100)

  caller.disconnect()
}).catch((err) => console.error(err))
