// 飛び上がって下りるだけ

'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

const alt = 1

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('ArduCopter')

  arduCopter.on('armed', () => console.log('ARMED'))
  arduCopter.on('commandReached', data => console.log(JSON.stringify(data)))
  arduCopter.on('missionSaved', () => console.log('MISSION SAVED'))
  arduCopter.on('mode', data => console.log(JSON.stringify(data)))
  arduCopter.on('position', data => console.log(JSON.stringify(data)))

  arduCopter.on('disarmed', () => co(function *() {
    console.log('DISARMED')
    yield arduCopter.disconnect()
    yield asleep(1000)
    yield caller.disconnect()
  }).catch((err) => {
    console.error(err)
    caller.disconnect()
  }))

  yield arduCopter.disableEvents(null)
  yield arduCopter.enableEvents([
    'armed',
    'commandReached',
    'disarmed',
    'missionSaved',
    'mode',
    'position'
  ])

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(3000)

  yield arduCopter.saveMission([{
    type: 'takeoff',
    altitude: alt / 2
  }, { // 上昇
    type: 'waypoint',
    coordinate: [0, 0, alt]
  }, {
    type: 'land'
  }])
  yield asleep(1000)
  yield arduCopter.setMode('Guided')
  yield asleep(1000)
  console.log('START MISSION after 30 seconds')
  yield arduCopter.armAndSetAutoWithDelay(30)
}).catch((err) => console.error(err))
