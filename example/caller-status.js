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

  arduCopter.on('armed', () => console.log('ARMED'))
  arduCopter.on('attitude', data => console.log(JSON.stringify(data)))
  arduCopter.on('battery', data => console.log(JSON.stringify(data)))
  arduCopter.on('commandReached', data => console.log(JSON.stringify(data)))
  arduCopter.on('connected', () => console.log('CONNECTED'))
  arduCopter.on('disarmed', () => console.log('DISARMED'))
  arduCopter.on('disconnected', () => console.log('DISCONNECTED'))
  arduCopter.on('gimbalOrientation', data => console.log(JSON.stringify(data)))
  arduCopter.on('home', data => console.log(JSON.stringify(data)))
  arduCopter.on('mission', data => console.log(JSON.stringify(data)))
  arduCopter.on('missionSaved', () => console.log('MISSION SAVED'))
  arduCopter.on('mode', data => console.log(JSON.stringify(data)))
  arduCopter.on('position', data => console.log(JSON.stringify(data)))
  arduCopter.on('speed', data => console.log(JSON.stringify(data)))
  arduCopter.on('type', data => console.log(JSON.stringify(data)))

  yield arduCopter.enableEvents(null)

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(3000)
  yield arduCopter.setMode('Guided')

  yield asleep(10000)

  yield arduCopter.disconnect()
  yield asleep(1000)
  yield caller.disconnect()
}).catch((err) => console.error(err))
